package org.imos.abos.parsers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

// Java program to decode ASIMET binary data files
//
// Copyright Peter Jansen, University of Tasmania
//
// 2010-05-04 Initial Issue
// 2010-05-16 Added all sensor types
// 2011-11-05 Added Iridium SBD decoder
// 2013-01-16 Added Sonic Wind reader, cleanup
// 2013-02-27 Fix module date problem
// 2013-11-10 Module timestamp is at end of sampling period
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program.  If not, see <http://www.gnu.org/licenses/>.

public class AsimetDecode
{
	protected static Logger log = Logger.getLogger(AsimetDataParser.class.getName());       

	int used;

	public void output(Date d, String hdr, String fmt, double v)
	{
		System.out.printf("," + hdr + "=" + fmt, v);
	}

	public void outputNewTs(String t)
	{
		System.out.print(t);        
	}

	public void outputNext()
	{
		System.out.println();        
	}

	public class reader
	{
		private int hour, min, sec, day, mon, year;

		int recordsProcessed = 0;
		int record = 0;
		int length = 0;
		String header = null;
		String[] headers = null;
		String fmt = null;
		String fmts[] = null;
		boolean raw = false;
		String fileName;
		Calendar ts = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date recordTs;
		Date nextTs = null;

		public reader()
		{
			TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
			sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		}

		public void setFileName(String file)
		{
			fileName = file;
		}

		public int read(FileChannel inChannel) throws IOException
		{
			return 0;
		}

		public void print()
		{

		}

		public void printArrayVars(double[] ...list)
		{
			// record time is written at end of timeperiod, so first record is t - 59 seconds
			ts.set(Calendar.SECOND, 0);
			ts.add(Calendar.MINUTE, -59);
			Date d = ts.getTime();
			for (int i = 0; i < 60; i++)
			{
				outputNewTs(sdf.format(d));

				int j = 0;
				for (double[] v : list)
				{
					output(d, headers[j], fmts[j], v[i]);
					j++;
				}
				ts.add(Calendar.MINUTE, 1);
				d = ts.getTime();

				outputNext();
			}
		}

		public void printVars(double... list)
		{
			Date d = getTime();

			outputNewTs(sdf.format(d));
			int i = 0;
			for (double v : list)
			{
				output(d, headers[i], fmts[i], v);

				i++;
			}
		}

		public void setRaw(boolean b)
		{
			raw = b;
		}

		private Date createDate()
		{
			ts.clear();
			ts.set(year, mon - 1, day, hour, min, sec);

			return ts.getTime();
		}

		public Date readModuleTimestamp(ByteBuffer buf)
		{
			hour = buf.get(0) & 0xff;
			min = buf.get(1) & 0xff;
			sec = buf.get(2) & 0xff;
			day = buf.get(3) & 0xff;
			mon = buf.get(5) & 0xff;
			year = (buf.getShort(6) & 0xffff);

			//System.out.println("day " + day + " Hour " + hour + " min " + min + " sec " + sec);
			recordTs = createDate();
			if ((nextTs == null) || (recordTs.compareTo(nextTs) != 0))
			{
				log.info("Time Jump : " + recordTs + " expecting " + nextTs);
			}
			nextTs = new Date(recordTs.getTime() + 1000 * 60 * 60);

			return recordTs;
		}

		int lastMin = -1;
		public Date readLoggerTimestamp(ByteBuffer buf)
		{
			hour = buf.get(0) & 0xff;
			min = buf.get(1) & 0xff;
			sec = 0;
			day = buf.get(2) & 0xff;
			mon = buf.get(3) & 0xff;
			year = (buf.get(4) & 0xff) + 2000;

			//            if ((lastMin == 41) && (min == 32))
				//            {
			//                min = 42; // Hack for LSR23 on SOFS-1, some problem with minutes
			//            }
			lastMin = min;

			//System.out.println("day " + day + " Hour " + hour + " min " + min + " sec " + sec);
			recordTs = createDate();

			return recordTs;
		}

		public Date getTime()
		{
			return recordTs;
		}
	}

	public class readLSR extends reader
	{
		public readLSR()
		{
			length = 64;
			header = "we, wn, wsavg, wmax, wmin, wdavg, compass, bp, rh, AirT, sr, dome, body, tpile, lwflux, prlev, sct, scc"; //, op_param, bat1, bat2, bat3, bat4";
			headers = header.split(", ");
			fmt = "%.2f, %.2f, %.2f, %.2f, %.2f, %.1f, %.1f, %.2f, %.2f, %.3f, %.1f, %.2f, %.2f, %.1f, %.1f, %.2f, %.3f, %.4f";
			fmts = fmt.split(", ");
		}

		int record;
		int mux_parm;
		double we, wn;
		double wsavg, wmax, wmin;
		double vdavg, compass;
		double bp;
		double rh;
		double th;
		double sr;
		double dome, body;
		double tpile;
		double lwflux;
		double prlev;
		double sct;
		double scc;
		double bat1, bat2, bat3, bat4;
		long op_param;
		int spare1, spare2, spare3;
		int outCount = 0;

		public int read(FileChannel inChannel) throws IOException
		{
			recordsProcessed = 0;

			// Decode format from
			// http://frodo.whoi.edu/logr53/logr_ntas_rec.html
			ByteBuffer buf = ByteBuffer.allocate(length);
			buf.order(ByteOrder.BIG_ENDIAN);

			// System.out.println(header);
			while (inChannel.read(buf) != -1)
			{
				// System.out.println("pos " + inChannel.position());
				buf.position(0);
				readLoggerTimestamp(buf);

				record = (buf.getShort(5) & 0xffff);

				mux_parm = (buf.getShort(7) & 0xffff);

				we = buf.getShort(8) / 100.0;
				wn = buf.getShort(10) / 100.0;

				wsavg = (buf.getShort(12) & 0xffff) / 100.0;
				wmax = (buf.getShort(14) & 0xffff) / 100.0;
				wmin = (buf.getShort(16) & 0xffff) / 100.0;

				vdavg = buf.getShort(18) / 10.0;
				compass = buf.getShort(20) / 10.0;

				bp = ((buf.getShort(22) & 0xffff) / 100.0) + 900.0;

				rh = buf.getShort(24) / 100.0;
				th = ((buf.getShort(26) & 0xffff) / 1000.0) - 20.0;

				sr = buf.getShort(28) / 10.0;
				dome = (buf.getShort(30) & 0xffff) / 100.0;
				body = (buf.getShort(32) & 0xffff) / 100.0;

				tpile = buf.getShort(34) / 10.0;
				lwflux = buf.getShort(36) / 10.0;
				prlev = buf.getShort(38) / 100.0;

				sct = ((buf.getShort(40) & 0xffff) / 1000.0) - 5.0;
				scc = (buf.getShort(42) & 0xffff) / 10000.0;

				bat1 = buf.getShort(44) / 1000.0;
				bat2 = buf.getShort(46) / 1000.0;
				bat3 = buf.getShort(48) / 1000.0;
				bat4 = buf.getShort(50) / 1000.0;

				op_param = buf.getInt(52);

				used = (buf.getShort(62) & 0xffff);

				if (used == 0xa5a5)
				{
					if ((nextTs == null) || (recordTs.compareTo(nextTs) != 0))
					{
						log.info("Time Jump : " + recordTs + " expecting " + nextTs);	
						outCount++;
					}
					else
					{
						outCount = 0;
					}
					if ((outCount >= 1) && (outCount < 3) && (nextTs != null))
					{
						recordTs = nextTs;
					}
					nextTs = new Date(recordTs.getTime() + 1000 * 60);

					recordsProcessed++;
					//System.out.println("bytes read " + inChannel.position());
					print();
					outputNext();
				}
			}

			return recordsProcessed;
		}

		public void print()
		{
			printVars(we, wn, wsavg, wmax, wmin, vdavg, compass, bp, rh, th, sr, dome, body, tpile, lwflux, prlev, sct, scc); //, op_param, bat1, bat2, bat3, bat4);
		}

	}

	public class readBPR extends reader
	{
		public readBPR()
		{
			length = 256;
			header = "bpr";
			headers = header.split(", ");
			fmt = "%.2f";
			fmts = fmt.split(", ");
		}

		double bpr[] = new double[60];

		public int read(FileChannel inChannel) throws IOException
		{
			recordsProcessed = 0;

			// Decode format from
			// http://frodo.whoi.edu/asimet_cf/bprcflash.html
			ByteBuffer buf = ByteBuffer.allocate(length);
			buf.order(ByteOrder.BIG_ENDIAN);

			log.info("AsimetDeocode::" + this.getClass().getSimpleName());

			while (inChannel.read(buf) != -1)
			{
				buf.position(0);
				readModuleTimestamp(buf);

				for (int i = 0; i < 60; i++)
				{
					bpr[i] = buf.getFloat(8 + 4 * i);
				}

				used = (buf.getShort(252) & 0xffff);
				if (used == 0xa5a5)
				{
					recordsProcessed++;
					print();
				}
			}

			return recordsProcessed;
		}

		public void print()
		{
			printArrayVars(bpr);
		}
	}

	public class readPRC extends readBPR
	{
		public readPRC()
		{
			length = 256;
			header = "prc";
			headers = header.split(", ");
			fmt = "%.2f";
			fmts = fmt.split(", ");
		}
	}

	public class readSWR extends readBPR
	{
		public readSWR()
		{
			length = 256;
			header = "swr";
			headers = header.split(", ");
			fmt = "%.2f";
			fmts = fmt.split(", ");
		}
	}

	public class readHRH extends reader
	{
		public readHRH()
		{
			length = 512;
			header = "AirT, rh";
			headers = header.split(", ");
			fmt = "%.2f, %.2f";
			fmts = fmt.split(", ");
		}

		double rh_cal[] = new double[60];
		double tmp_cal[] = new double[60];

		public int read(FileChannel inChannel) throws IOException
		{
			recordsProcessed = 0;

			// Decode format from
			// http://frodo.whoi.edu/asimet_cf/bprcflash.html
			ByteBuffer buf = ByteBuffer.allocate(length);
			buf.order(ByteOrder.BIG_ENDIAN);

			log.info("AsimetDecode:: HRH reader");

			while (inChannel.read(buf) != -1)
			{
				buf.position(0);
				readModuleTimestamp(buf);

				for (int i = 0; i < 60; i++)
				{
					rh_cal[i] = buf.getFloat(8 + 4 * i);
				}
				for (int i = 0; i < 60; i++)
				{
					tmp_cal[i] = buf.getFloat(248 + 4 * i);
				}

				used = (buf.getShort(508) & 0xffff);
				if (used == 0xa5a5)
				{
					recordsProcessed++;
					print();
				}
			}

			return recordsProcessed;
		}

		public void print()
		{
			printArrayVars(tmp_cal, rh_cal);
		}
	}

	public class readWND extends reader
	{
		public readWND()
		{
			length = 740;
			header = "we, wn, WSpeed, WSMax, LastVane, LastCompass, TiltX, TiltY";
			headers = header.split(", ");
			fmt = "%.2f, %.2f, %.2f, %.2f, %.2f, %.2f, %.2f, %.2f";
			fmts = fmt.split(", ");
		}

		double ve[] = new double[60];
		double vn[] = new double[60];
		double WSpeed[] = new double[60];
		double WSMax[] = new double[60];
		double LastVane[] = new double[60];
		double LastCompass[] = new double[60];
		double TiltX[] = new double[60];
		double TiltY[] = new double[60];

		public int read(FileChannel inChannel) throws IOException
		{
			recordsProcessed = 0;

			// Decode format from
			// http://frodo.whoi.edu/asimet_cf/bprcflash.html
			ByteBuffer buf = ByteBuffer.allocate(length);
			buf.order(ByteOrder.BIG_ENDIAN);

			log.info("AsimetDecode:: WND reader");

			while (inChannel.read(buf) != -1)
			{
				buf.position(0);
				readModuleTimestamp(buf);

				for (int i = 0; i < 60; i++)
				{
					ve[i] = buf.getShort(8 + 2 * i) / 100.0;
				}
				for (int i = 0; i < 60; i++)
				{
					vn[i] = buf.getShort(128 + 2 * i) / 100.0;
				}
				for (int i = 0; i < 60; i++)
				{
					WSpeed[i] = (buf.get(248 + i) & 0xff) / 5.0;
				}
				for (int i = 0; i < 60; i++)
				{
					WSMax[i] = (buf.get(308 + i) & 0xff) / 5.0;
				}
				for (int i = 0; i < 60; i++)
				{
					LastVane[i] = (buf.getShort(368 + 2 * i) & 0xffff) / 10.0;
				}
				for (int i = 0; i < 60; i++)
				{
					LastCompass[i] = (buf.getShort(488 + 2 * i) & 0xffff) / 10.0;
				}
				for (int i = 0; i < 60; i++)
				{
					TiltX[i] = (buf.get(608 + i) & 0xff) / 5.0;
				}
				for (int i = 0; i < 60; i++)
				{
					TiltY[i] = (buf.get(668 + i) & 0xff) / 5.0;
				}

				used = (buf.getShort(736) & 0xffff);
				if (used == 0xa5a5)
				{
					recordsProcessed++;
					print();
				}
			}

			return recordsProcessed;
		}

		public void print()
		{
			printArrayVars(ve, vn, WSpeed, WSMax, LastVane, LastCompass, TiltX, TiltY);
		}
	}

	public class readSWND extends reader
	{
		public readSWND()
		{
			length = 1212;
			header = "we, wn, ws, wsMax, LastVane, LastCompass, TiltX, TiltY, GillSOS, GillTemp";
			headers = header.split(", ");
			fmt = "%.2f, %.2f, %.2f, %.2f, %.2f, %.2f, %.2f, %.2f, %.2f, %.2f";
			fmts = fmt.split(", ");
		}

		double ve[] = new double[60];
		double vn[] = new double[60];
		double WSpeed[] = new double[60];
		double WSMax[] = new double[60];
		double LastVane[] = new double[60];
		double LastCompass[] = new double[60];
		double TiltX[] = new double[60];
		double TiltY[] = new double[60];
		double GillSOS[] = new double[60];
		double GillTemp[] = new double[60];

		public int read(FileChannel inChannel) throws IOException
		{
			recordsProcessed = 0;

			// Decode format from
			// http://frodo.whoi.edu/asimet_cf/swndcflash.html
			ByteBuffer buf = ByteBuffer.allocate(length);
			buf.order(ByteOrder.BIG_ENDIAN);

			log.info("AsimetDecode:: " + getClass().getSimpleName());

			while (inChannel.read(buf) != -1)
			{
				buf.position(0);
				readModuleTimestamp(buf);

				for (int i = 0; i < 60; i++)
				{
					ve[i] = buf.getShort(8 + 2 * i) / 100.0;
				}
				for (int i = 0; i < 60; i++)
				{
					vn[i] = buf.getShort(128 + 2 * i) / 100.0;
				}
				for (int i = 0; i < 60; i++)
				{
					WSpeed[i] = (buf.get(248 + i) & 0xff) / 5.0;
				}
				for (int i = 0; i < 60; i++)
				{
					WSMax[i] = (buf.get(308 + i) & 0xff) / 5.0;
				}
				for (int i = 0; i < 60; i++)
				{
					LastVane[i] = (buf.getShort(368 + 2 * i) & 0xffff) / 10.0;
				}
				for (int i = 0; i < 60; i++)
				{
					LastCompass[i] = (buf.getShort(488 + 2 * i) & 0xffff) / 10.0;
				}
				for (int i = 0; i < 60; i++)
				{
					TiltX[i] = (buf.get(608 + i)) / 5.0;
				}
				for (int i = 0; i < 60; i++)
				{
					TiltY[i] = (buf.get(668 + i)) / 5.0;
				}
				for (int i = 0; i < 60; i++)
				{
					GillSOS[i] = buf.getFloat(728 + 4 * i);
				}
				for (int i = 0; i < 60; i++)
				{
					GillTemp[i] = buf.getFloat(968 + 4 * i);
				}

				used = (buf.getShort(1208) & 0xffff);
				if (used == 0xa5a5)
				{
					recordsProcessed++;
					print();
				}
				record++;
			}

			return recordsProcessed;
		}

		public void print()
		{
			printArrayVars(ve, vn, WSpeed, WSMax, LastVane, LastCompass, TiltX, TiltY, GillSOS, GillTemp);
		}
	}

	public class readLWR extends reader
	{
		public readLWR()
		{
			length = 612;
			header = "temp_dome, temp_body, volts_pile, lw_flux";
			headers = header.split(", ");
			fmt = "%.2f, %.2f, %.2f, %.2f, %.2f, %.2f, %.2f, %.2f";
			fmts = fmt.split(", ");
		}

		double temp_dome[] = new double[60];
		double temp_body[] = new double[60];
		double volts_pile[] = new double[60];
		double lw_flux[] = new double[60];

		public int read(FileChannel inChannel) throws IOException
		{
			recordsProcessed = 0;

			// Decode format from
			// http://frodo.whoi.edu/asimet_cf/bprcflash.html
			ByteBuffer buf = ByteBuffer.allocate(length);
			buf.order(ByteOrder.BIG_ENDIAN);

			log.info("AsimetDecode:: LWR reader");

			while (inChannel.read(buf) != -1)
			{
				buf.position(0);
				readModuleTimestamp(buf);

				for (int i = 0; i < 60; i++)
				{
					temp_dome[i] = (buf.getShort(8 + 2 * i) & 0xffff) / 100.0;
				}
				for (int i = 0; i < 60; i++)
				{
					temp_body[i] = (buf.getShort(128 + 2 * i) & 0xffff) / 100.0;
				}
				for (int i = 0; i < 60; i++)
				{
					volts_pile[i] = buf.getFloat(248 + 4 * i);
				}
				for (int i = 0; i < 60; i++)
				{
					lw_flux[i] = (buf.getShort(488 + 2 * i) & 0xffff) / 10.0;
				}

				used = (buf.getShort(608) & 0xffff);
				if (used == 0xa5a5)
				{
					recordsProcessed++;
					print();
					log.trace(String.format("Used 0x%04x\n", used));
				}
			}

			return recordsProcessed;
		}

		public void print()
		{
			printArrayVars(temp_dome, temp_body, volts_pile, lw_flux);
		}
	}

	public class readIridium extends reader
	{
		public readIridium()
		{
			length = 32;
			header = "record, wn, we, compass, bpr, AirT, rh, swr, lwr, pcr, sst, ssc, used";
		}

		int record = 0;

		double wn, we;
		double compass;
		double bpr;
		double rh;
		double th;
		double swr;
		double lwr;
		double pcr;
		double sst;
		double ssc;
		FileChannel inChannel;

		public int read(FileChannel inChannel) throws IOException
		{
			this.inChannel = inChannel;
			recordsProcessed = 0;

			// Decode format from
			// proc_logr_avr.c
      //
      // http://uop.whoi.edu/UOPinstruments/frodo/logr53/logr_sbd_msg_spurs2.html

			ByteBuffer buf = ByteBuffer.allocate(length);
			buf.order(ByteOrder.LITTLE_ENDIAN);

			log.info("AsimetDecode:: Iridium reader");

			while (inChannel.read(buf) != -1)
			{
				buf.position(0);
				readLoggerTimestamp(buf);

				record = (buf.getShort() & 0xffff);

				wn = (buf.getShort() & 0xffff) / 100.0;
				we = (buf.getShort() & 0xffff) / 100.0;
				compass = (buf.getShort() & 0xffff) / 10.0;
				bpr = ((buf.getShort() & 0xffff) / 100.0) + 900.0;
				rh = (buf.getShort() & 0xffff) / 100.0;
				th = ((buf.getShort() & 0xffff) / 1000.0) - 20.0;
				swr = (buf.getShort() & 0xffff) / 10.0;
				lwr = (buf.getShort() & 0xffff) / 10.0;
				pcr = (buf.getShort() & 0xffff) / 100.0;
				sst = (buf.getShort() & 0xffff) / 1000.0 - 5.0;
				ssc = (buf.getShort() & 0xffff) / 10000.0;

				used = (buf.getShort(30) & 0xffff);
				if (used == 0xa5a5)
				{
					recordsProcessed++;
				}
				print();
				buf.position(0); // re-wind buffer to its start
			}

			return recordsProcessed;
		}

		public void print()
		{
			System.out.printf("AsimetDecode:: %s ,", fileName);

			printVars(record, wn, we, compass, bpr, th, rh, swr, lwr, pcr, sst, ssc, used);
		}
	}

	boolean raw = false;

	public void setRaw(boolean b)
	{
		raw = b;
	}

	public void read(String type, String file) throws IOException
	{
		File aFile = new File(file);

		FileInputStream inFile = new FileInputStream(aFile);

		FileChannel inChannel = inFile.getChannel();
		log.warn("AsimetDecode::read() file " + file + " size " + inChannel.size());

		reader r = null;
		if (raw)
		{
			inChannel.position(322 * 512); // skip to data sector
		}

		if (type.startsWith("LSR"))
		{
			r = (reader) new readLSR();
			r.setRaw(raw);
		}
		else if (type.startsWith("BPR"))
		{
			r = (reader) new readBPR();
		}
		else if (type.startsWith("HRH"))
		{
			r = (reader) new readHRH();
		}
		else if (type.startsWith("PRC"))
		{
			r = (reader) new readPRC();
		}
		else if (type.startsWith("SWR"))
		{
			r = (reader) new readSWR();
		}
		else if (type.startsWith("LWR"))
		{
			r = (reader) new readLWR();
		}
		else if (type.startsWith("WND"))
		{
			r = (reader) new readWND();
		}
		else if (type.startsWith("SWND"))
		{
			r = (reader) new readSWND();
		}
		else if (type.startsWith("IRIDIUM"))
		{
			r = (reader) new readIridium();
		}
		else
		{
			log.warn("AsimetDecode:: unknown type");
		}

		if (r != null)
		{
			r.setFileName(file);

			int read = r.read(inChannel);

			log.info("AsimetDecode::read() records out " + read);
		}
	}

	public static void main(String[] args)
	{
		PropertyConfigurator.configure("log4j.properties");

		if (args.length < 2)
		{
			log.info("usage: AsimetDecode <type> <raw filename");
			log.info("                    known <types> LSR - logger");
			log.info("                                  BPR - Pressure");
			log.info("                                  HRH - Temp/Humid");
			log.info("                                  PRC - Rain");
			log.info("                                  SWR - Short Wave Radiation");
			log.info("                                  LWR - Long Wave Radiation");
			log.info("                                  WND - Wind");
			log.info("                                  SWND - Sonic Wind");
			log.info("                                  IRIDIUM - Iridium SBD message");

			System.exit(-1);
		}
		AsimetDecode ald = new AsimetDecode();

		int s = 0;

		if (args[0].startsWith("-b"))
		{
			ald.setRaw(true);
			s++;
		}

		try
		{
			ald.read(args[s], args[s + 1]);
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

	}

}
