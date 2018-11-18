package org.imos.abos.parsers;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import ucar.ma2.ArrayByte;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayFloat;
import ucar.ma2.ArrayInt;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.Variable;

public class Gps9dofParser
{
	public enum State {SEARCH, IMU, UBXHDR, NMEA, UBX};

	public static void main(String[] args)
	{
		long start = System.nanoTime();
		byte[] bytes = new byte[32*1024];
		int len;
		FileInputStream fis;
		State state = State.SEARCH;
		int msgIdx = 0;
		byte[] message = new byte[1024];
		String s;
		int toRead = 0;
		int ubxMessages;
		int nmeaMessages;
		int imuMessages;
		
		long anchorTime;
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SS");
		SimpleDateFormat dfNMEAdate = new SimpleDateFormat("ddMMyy HHmmss.SS");
		anchorTime = 0;
		try
		{
			Date ts = df.parse("1950-01-01 00:00:00.00");
			anchorTime = ts.getTime();
		}
		catch (ParseException pex)
		{
			System.err.println(pex);
		}

		long offsetTime;
		double elapsedHours;
		Date ts;
		Date gpsDate;
		String gpsRMCdate = null;
		
		try
		{
			NetcdfFileWriter dataFile = NetcdfFileWriter.createNew(NetcdfFileWriter.Version.netcdf4, "gpsIMU.nc"); 
			
			Dimension timeDim = dataFile.addUnlimitedDimension("TIME");
			Variable vTime = dataFile.addVariable(null, "TIME", DataType.DOUBLE, "TIME");
			vTime.addAttribute(new Attribute("units", "days since 1950-01-01 00:00:00"));
			vTime.addAttribute(new Attribute("calendar", "gregorian"));
			
			Dimension vectorDim = dataFile.addDimension(null, "vector", 3);
			Dimension quatDim = dataFile.addDimension(null, "quaternion", 4);
			
			Variable vAccel = dataFile.addVariable(null, "accel", DataType.FLOAT, "TIME vector");
			Variable vGyro = dataFile.addVariable(null, "gyro", DataType.FLOAT, "TIME vector");
			Variable vMag = dataFile.addVariable(null, "mag", DataType.FLOAT, "TIME vector");
			Variable vQuat = dataFile.addVariable(null, "orientation", DataType.FLOAT, "TIME quaternion");
			Variable vGpsIdx = dataFile.addVariable(null, "gpsIdx", DataType.INT, "TIME");

			Dimension gpsTimeDim = dataFile.addUnlimitedDimension("GPS_TIME");
			
			Variable vGpsTime = dataFile.addVariable(null, "gpsTIME", DataType.DOUBLE, "GPS_TIME");
			vGpsTime.addAttribute(new Attribute("units", "days since 1950-01-01 00:00:00"));
			vGpsTime.addAttribute(new Attribute("calendar", "gregorian"));
			
			Variable vGpsSats = dataFile.addVariable(null, "gpsSATS", DataType.BYTE, "GPS_TIME");
			Variable vGpsLLH = dataFile.addVariable(null, "gpsPOS", DataType.FLOAT, "GPS_TIME vector");
			
            ArrayDouble.D1 time = new ArrayDouble.D1(1);
            ArrayInt.D1 gpsIdx = new ArrayInt.D1(1);
            
            ArrayFloat.D2 accel = new ArrayFloat.D2(1, 3);
            ArrayFloat.D2 gyro = new ArrayFloat.D2(1, 3);
            ArrayFloat.D2 mag = new ArrayFloat.D2(1, 3);
            ArrayFloat.D2 quat = new ArrayFloat.D2(1, 4);

            int[] time_origin = new int[] { 0 };
            int[] vector_origin = new int[] { 0 , 0 };
            int[] quat_origin = new int[] { 0 , 0 };

            ArrayFloat.D2 gpsLLH = new ArrayFloat.D2(1, 3);
            ArrayByte.D1 gpsSats = new ArrayByte.D1(1);

            int[] gps_time_origin = new int[] { 0 };
            int[] gps_vector_origin = new int[] { 0 , 0 };
            
            String split[];
            int lastGps = -1;
            int ubxPos = 0;
            int nmeaPos = 0;
            int imuCount = 0;
            int ubxRaw = 0;
			
			dataFile.create();

			for (String fn : args)
			{
				ubxMessages = 0;
				nmeaMessages = 0;
				imuMessages = 0;
				
				fis = new FileInputStream(fn);
				while((len = fis.read(bytes)) > 0)
				{
					for (int i=0;i<len;i++)
					{
						//System.out.println("I" + i + " IDX " +msgIdx + " byte " + (char)bytes[i] + " state " + state);
						switch (state)
						{
							case SEARCH:
								msgIdx = 0;
								if (bytes[i] == '2')
								{
									message[msgIdx++] = bytes[i];
									state = State.IMU;
								}
								else if (bytes[i] == 'U')
								{
									message[msgIdx++] = bytes[i];
									state = State.UBXHDR;						
								}
								else if (bytes[i] == '$')
								{
									message[msgIdx++] = bytes[i];
									state = State.NMEA;						
								}
								else
								{
									state = State.SEARCH;
								}
								break;
							case IMU: // IMU
								if ((bytes[i] >= ' ') && (bytes[i] < 'z'))
								{
									message[msgIdx++] = bytes[i];
								}
								else
								{
									s = new String(message, 0, msgIdx);
									//System.out.println("IMU : " + msgIdx + " " + s);

									try
									{
										ts = df.parse(s);
										System.out.println("IMU time " + df.format(ts));
										
										offsetTime = (ts.getTime() - anchorTime);
					            		elapsedHours = ((double) offsetTime) / (3600 * 24) / 1000;
					            		time.setDouble(0, elapsedHours);
					            							            		
					            		split = s.split(",");
					            		if (split.length >= 16)
					            		{
						            		accel.set(0,  0, Float.parseFloat(split[2]));
						            		accel.set(0,  1, Float.parseFloat(split[3]));
						            		accel.set(0,  2, Float.parseFloat(split[4]));

						            		gyro.set(0,  0, Float.parseFloat(split[5]));
						            		gyro.set(0,  1, Float.parseFloat(split[6]));
						            		gyro.set(0,  2, Float.parseFloat(split[7]));
	
						            		mag.set(0,  0, Float.parseFloat(split[8]));
						            		mag.set(0,  1, Float.parseFloat(split[9]));
						            		mag.set(0,  2, Float.parseFloat(split[10]));
	
						            		quat.set(0,  0, Float.parseFloat(split[11]));
						            		quat.set(0,  1, Float.parseFloat(split[12]));
						            		quat.set(0,  2, Float.parseFloat(split[13]));
						            		quat.set(0,  3, Float.parseFloat(split[14]));

						            		gpsIdx.set(0, lastGps);

						            		dataFile.write(vTime, time_origin, time);						            		
						            		dataFile.write(vGpsIdx, time_origin, gpsIdx);
						            		
						            		dataFile.write(vAccel, vector_origin, accel);
						            		dataFile.write(vGyro, vector_origin, gyro);
						            		dataFile.write(vMag, vector_origin, mag);
						            		
						            		dataFile.write(vQuat, quat_origin, quat);

						            		time_origin[0]++;
						            		vector_origin[0]++;
						            		quat_origin[0]++;
						            			
											imuMessages++;
					            		}
									}
									catch (ParseException pe)
									{
										System.out.println("IMU " + pe);
									}
									catch (NumberFormatException pe)
									{
										System.out.println("IMU " + pe);
									}
									state = State.SEARCH;
								}
								break;
							case UBXHDR:
								// UBX class = 2, ID = 17 end len = 50 :
								if (bytes[i] == ':')
								{
									s = new String(message, 0, msgIdx);
									//System.out.println("UBX : " + s + " len " + s.substring(s.lastIndexOf('=') + 2, msgIdx - 1));
									
									try
									{
										toRead = Integer.parseInt(s.substring(s.lastIndexOf('=') + 2, msgIdx - 1)) + 1;
										//System.out.println("UBX : " + s + " to read " + toRead);
		
										state = State.UBX;
									}
									catch (NumberFormatException nfe)
									{
										state = State.SEARCH;										
									}
									catch (StringIndexOutOfBoundsException nfe)
									{
										state = State.SEARCH;										
									}
									msgIdx = 0;
								}
								else
									message[msgIdx++] = bytes[i];
	
								if (bytes[i] < ' ' || bytes[i] > 'z' || bytes[i] == 10)
									state = State.SEARCH;
								
								break;
							case NMEA: // MNEA
								if (bytes[i] >= ' ' && bytes[i] <= 'Z')
								{
									message[msgIdx++] = bytes[i];
								}
								else
								{
									s = new String(message, 0, msgIdx);
				            		split = s.split(",");

			            			try
									{
										System.out.println("NMEA : " + s);

										if (split[0].startsWith("$GPRMC") && (split.length > 9))
					            		{
					            			// $GPRMC,010010.00,A,4253.17061,S,14720.24306,E,0.012,,160317,,,D*6B
					            			gpsRMCdate = split[9];
					            			
					            			//System.out.println("RMC date " + gpsRMCdate);					            			
					            		}
					            		else if (split[0].startsWith("$GPGGA") && (split.length > 14))
					            		{
					            			// $GPGGA,010010.00,4253.17061,S,14720.24306,E,2,10,1.08,-6.0,M,-8.9,M,,0000*7F
					            			if (gpsRMCdate != null)
					            			{
						            			gpsDate = dfNMEAdate.parse(gpsRMCdate + " " + split[1]);
						            			
						            			//System.out.println("GGA date " + gpsRMCdate + " " + split[1] + " " + df.format(gpsDate));
												offsetTime = (gpsDate.getTime() - anchorTime) / 1000;
							            		elapsedHours = ((double) offsetTime) / (3600 * 24);
							            		
							            		time.setDouble(0, elapsedHours);
							            		
							            		float lat = Float.parseFloat(split[2]);
							            		lat = (float) (Math.floor(lat/100) + (lat - Math.floor(lat/100) * 100)/ 60);
							            		if (split[3] == "S")
							            			lat = -lat;
							            		float lon = Float.parseFloat(split[4]);
							            		lon = (float) (Math.floor(lon/100) + (lon - Math.floor(lon/100) * 100)/ 60);
							            		if (split[5] == "W")
							            			lon = -lon;
							            		float alt = Float.parseFloat(split[9]);
							            		byte sats = (byte) Integer.parseInt(split[7]);
							            		System.out.println("lat " + lat + " lon " + lon + " alt " + alt + " sats " + sats);
							            		
							            		gpsLLH.set(0,  0, lat);
							            		gpsLLH.set(0,  1, lon);
							            		gpsLLH.set(0,  2, alt);

							            		gpsSats.set(0, sats);

							            		dataFile.write(vGpsTime, gps_time_origin, time);
							            		dataFile.write(vGpsSats, gps_time_origin, gpsSats);

							            		dataFile.write(vGpsLLH, gps_vector_origin, gpsLLH);

							            		lastGps = gps_time_origin[0];
							            		gps_time_origin[0]++;	
							            		gps_vector_origin[0]++;	
							            		
							            		nmeaPos++;
					            			}
					            		}
		
										nmeaMessages ++;
									}
									catch (ParseException e)
									{
										System.out.println("NMEA " + e);
									}
									catch (NumberFormatException e)
									{
										System.out.println("NMEA " + e);
									}

									state = State.SEARCH;
								}
								break;
							case UBX:
								if (msgIdx > 0)
									message[msgIdx-1] = bytes[i];
								msgIdx++;
								toRead--;
								if (toRead == 0)
								{
									ubxMessages++;
									state = State.SEARCH;
									ByteBuffer msgBB = ByteBuffer.wrap(message).order(ByteOrder.LITTLE_ENDIAN);
									System.out.println("UBX message len " + msgIdx);
									System.out.println("sync " + String.format("0x%04x", msgBB.getShort() & 0xffff));
									System.out.println("class " + String.format("0x%02x", msgBB.get()));
									System.out.println("id    " + String.format("0x%02x", msgBB.get()));
									System.out.println("len   " + String.format("%d", msgBB.getShort() & 0xffff));
									if (message[2] == 0x01) // NAV
									{
										if (message[3] == 0x02) // POSLLH
										{
											int tow = msgBB.getInt() & 0xffffffff;
											double lon = msgBB.getInt()/1e7;
											double lat = msgBB.getInt()/1e7;
											double hellip = msgBB.getInt()/1e3;
											double hmsl = msgBB.getInt()/1e3;
											System.out.println("POSLLH : tow " + tow + " lat " + lat + " lon " + lon + " hmsl " + hmsl);
											ubxPos++;
										}
										
									}
									else if (message[2] == 0x02) // RXM
									{
										if (message[3] == 0x10) // RAW
										{
											long tow = msgBB.getInt() & 0xffffffff;
											int week = msgBB.getShort();
											System.out.println("RXM-RAW tow " + tow + " week " + week);
											try
											{
												Date epoch = df.parse("1980-01-06 00:00:00.00");
												long epochl = epoch.getTime();
												long t = epochl + week * 7l * 86400l * 1000l + tow - 18 * 1000; // 18 leap seconds in 2017-01-01
												Date td = new Date(t);
												System.out.println("GPS time " + df.format(td));
												ubxRaw++;
											}
											catch (ParseException e)
											{
												// TODO Auto-generated catch block
												e.printStackTrace();
											}
										}
										
									}
									else
										System.out.println("Unknown class");
								}
								break;
						}
					}
//					if (nmeaMessages > 20)
//						break;
				}
				
				fis.close();
				System.out.println(fn + " UBX " + ubxMessages + " IMU " + imuMessages + " NMEA " + nmeaMessages);
				System.out.println("ubx Pos " + ubxPos + " ubx RAW " + ubxRaw + " nmea " + nmeaPos + " imu " + imuMessages);
			}
			
			dataFile.close();
		}
		catch (IOException | InvalidRangeException e)
		{
			e.printStackTrace();
		}

		long time = System.nanoTime() - start;
		System.out.printf("Took %.3f seconds%n", time/1e9);		
	}

}
