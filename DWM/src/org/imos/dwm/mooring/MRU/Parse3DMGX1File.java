package org.imos.dwm.mooring.MRU;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.TimeZone;

import org.apache.log4j.Logger;

public class Parse3DMGX1File
{
	protected static Logger log = Logger.getLogger(Parse3DMGX1File.class.getName());

	public boolean readLoad = false;
	public boolean sofs1 = false;

	public Parse3DMGX1File()
	{
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
		sdf.setTimeZone(TimeZone.getDefault());
	}

	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	RandomAccessFile raf = null;
	int sumOk = 0;
	boolean validPacket = false;
	byte ch = 0;
	ByteBuffer readOnlyBuffer = null;
	File openFile;
	
	int readState = 0;

	public void open(File f) throws IOException
	{
		sumOk = 0;
		validPacket = false;
		openFile = f;

		raf = new RandomAccessFile(f, "r");

		FileChannel roChannel = raf.getChannel();
		long fileSize = roChannel.size();

		readOnlyBuffer = roChannel.map(FileChannel.MapMode.READ_ONLY, 0, (int) fileSize);
		readOnlyBuffer.order(ByteOrder.BIG_ENDIAN);

		log.info("open File " + f.getName() + " file size " + fileSize);

		ch = 0;    
		
		sentTs = false;
		
		readState = 1;
	}

	public class MruStabQ
	{
		public int ticks;
		double ttime;
		int command;

		public quaternion stab;
		public vector mag, accel, gyro, accelWorld;
		public vector pry;

		public MruStabQ()
		{
			stab = new quaternion();
			mag = new vector();
			accel = new vector();
			gyro = new vector();
		}

		public String toString()
		{
			return new String("pry ," + pry + ",accel ," + accel + " , gyro ," + gyro + " ,accel-w ," + accelWorld);
		}
	}

	int tickRoll = 0;
	protected MruStabQ readStabQ(int command, int[] ints)
	{
		MruStabQ r = new MruStabQ();

		r.command = command;

		r.stab.Q1 = ints[1] / 8192.0;
		r.stab.Q2 = ints[2] / 8192.0;
		r.stab.Q3 = ints[3] / 8192.0;
		r.stab.Q4 = ints[4] / 8192.0;

		log.trace("stab " + r.stab + " euler (P,R,Y) " + r.stab.eulerAngles());

		r.mag.x = ints[5] / (32768000.0 / 2000);
		r.mag.y = ints[6] / (32768000.0 / 2000);
		r.mag.z = ints[7] / (32768000.0 / 2000);

		log.trace("mag (Guass) " + r.mag);

		r.accel.x = 9.81 * ints[8] / (32768000.0 / 8500);
		r.accel.y = 9.81 * ints[9] / (32768000.0 / 8500);
		r.accel.z = 9.81 * ints[10] / (32768000.0 / 8500);
		log.trace("Accel (m/s^2) " + r.accel);

		r.gyro.x = ints[11] / (32768000.0 / 10000);
		r.gyro.y = ints[12] / (32768000.0 / 10000);
		r.gyro.z = ints[13] / (32768000.0 / 10000);
		log.trace("AngleRate (rad/s) " + r.gyro);

		r.ticks = ints[14];

		r.accelWorld = r.stab.rotate(r.accel);
		log.trace("World Coord Accel (m/s^2) " + r.accelWorld);

		// the MRU is upside down
		r.pry = r.stab.eulerAngles();
//		if (r.pry.y < 0)
//		{
//			r.pry.y = r.pry.y + 180;
//		}
//		else
//		{
//			r.pry.y = r.pry.y - 180;
//		}

		log.trace((r.ticks + tickRoll * 0.0065536 * 65535) + "," + r.accelWorld);

		return r;
	}
	
	boolean sentTs = false;
	
	public Object read() throws IOException
	{
		if (readOnlyBuffer.hasRemaining())
		{
			try
			{
				switch (readState)
				{
					case 1: // open
					{
						ch = readOnlyBuffer.get();
						log.trace(String.format("open char %d, pos %d", ch, readOnlyBuffer.position()));
						
						if (ch == 0x0c)
						{
							readState = 2;
						}
						else 
						{
							readState = 4;
						}
						break;
					}
					case 2: // MRU packet
					{
						int packetStart = readOnlyBuffer.position();

						log.trace(String.format("0x0C command Pos %d 0x%02x", packetStart, ch));

						int sum = 0x0c;
						int ints[] = new int[15];
						int v;
						ints[0] = 0x0c;
						for (int i = 1; i < 15;i++)
						{
							ints[i] = readOnlyBuffer.getShort();
							v = ints[i] & 0xffff;
							sum = (v + sum) & 0xffff;

							log.trace(String.format("%2d INT 0x%04x SUM 0x%04x", i, v, sum));
						}
						int b;
						if (sofs1)
						{
							b = (readOnlyBuffer.get() & 0xff) << 8;
							sum = sum & 0xff00;
						}
						else
							b = readOnlyBuffer.getShort() & 0xffff;
							
						readState = 1;

						if (b != sum) 
						{
							log.error(String.format("Check Sum error 0x%04x 0x%04x", b, sum));
							readOnlyBuffer.position(packetStart+1);
						}      
						else
						{
							sumOk++;
							log.trace("Sum Ok " + sumOk);
							validPacket = true;

							if (readLoad)
								readState = 3;

							return readStabQ(0x0c, ints);
						}

						
						return sumOk;
					}
					case 3: 
					{
						int loadStart = readOnlyBuffer.position();
						log.trace("load pos " + loadStart);
						
						float load ;

						readState = 1;
						
						if (sofs1)
						{
							load = readOnlyBuffer.getShort() & 0xffff;
                            // VRef = Vdd = 3.3V, 12 Bit mode, loadcell 1827.896 kg/V, 0.104 V offset = 190 kg,  1827.896 * (3.3/4096) = 1.47267 kg/bit
                            // double dLoad = ((load * 1.47267) - 190);
                            double dload = load * 3300/4096; // in mVolts

//                            if (readOnlyBuffer.hasRemaining())
//                            	readOnlyBuffer.get(); // extra character?

                            log.trace("Load " + dload);
                            
                            return new Float(dload);
						}
						else
						{
							load = readOnlyBuffer.getFloat();
	
							log.trace("Load " + load);

							return new Float(load);
						}
					}
					case 4:
					{
						String s = "";

						while ((ch >= ' ') && (ch <= 'z'))
						{
							s = s + (char)ch;            		
							ch = readOnlyBuffer.get();
						}        		
						log.debug("String " + s);

						log.trace(String.format("string read char 0x%02x", ch));

						if (ch == 0x0c)
							readState = 2;
						else
							readState = 1;
						
						if (s.length() > 2)
							return s;
						else
							return (int)-1;
					}
					default:
						readState = 1;
				}
				
				return (int)-1;
			}
			catch (BufferUnderflowException bufx)
			{
				return null;
			}

		}

		return null;
	}

	public void close()
	{
		if (raf != null)
			try
		{
				raf.close();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void main(String args[])
	{
		Parse3DMGX1File pn = new Parse3DMGX1File();
		pn.readLoad = true;

		int k = 0;
		if (args[0].startsWith("-1"))
		{
			pn.sofs1 = true;
			k++;
		}
		File f = new File(args[k]);

		log.debug("Time Zone " + pn.sdf.getTimeZone());

		try
		{
			pn.open(f);
			Object r;
			while((r = pn.read()) != null)
			{
				log.debug("Read " + r);
			}

			log.info("Sum OK " + pn.sumOk);

		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally
		{
			pn.close();
		}
	}

}
