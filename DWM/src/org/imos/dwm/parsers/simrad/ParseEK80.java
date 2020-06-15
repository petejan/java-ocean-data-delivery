package org.imos.dwm.parsers.simrad;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.apache.log4j.Logger;

public class ParseEK80
{
	SimpleDateFormat sdf;
    private static Logger log = Logger.getLogger(ParseEK80.class);
    long windowsTimeOffset = -11644473600000L; // offset milliseconds from Jan 1, 1601 to Jan 1, 1970

	public ParseEK80()
	{
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

        sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSS");
		
        try
		{
			windowsTimeOffset = sdf.parse("1601-01-01 00:00:00.0000").getTime();
			//log.info("windows time offset " + windowsTimeOffset);
		}
		catch (ParseException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void read(String file)
	{
		log.info("parse file " + file);
		
        RandomAccessFile aFile;
		try
		{
			aFile = new RandomAccessFile(file, "r");
	        FileChannel inChannel = aFile.getChannel();
	        ByteBuffer buffer = ByteBuffer.allocate(4 * 32 / 8); // read 4 longs (32 bit values)
	        buffer.order(ByteOrder.LITTLE_ENDIAN);
	        int read = 0;
	        while((read = inChannel.read(buffer)) > 0)
	        {
	        		buffer.rewind();
	        		
	        		//log.info("read " + read);
	        		
	        		long length = buffer.getInt() & 0xffffffff;
	        		//log.info("Packet length " + length);
	        		
	        		byte type[] = new byte[4];
	        		buffer.get(type);
	        		
	        		String s = new String(type);
	        		
	        		log.info("type " + s);
	        		
	        		long lowDataTime = buffer.getInt() & 0xffffffff;
	        		long highDataTime = buffer.getInt() & 0xffffffff;
	        		
	        		long ntTime = (highDataTime << 32) + lowDataTime;
	        		
	        		//log.info("nt Time " + ntTime);

	        	    long javaTime = ntTime  / 10000 + windowsTimeOffset;     // convert 100-nanosecond intervals to milliseconds
	                            
	        	    Date date = new Date(javaTime);
	    	
	        		log.info("date " + sdf.format(date));
	        		
	        		ByteBuffer content = ByteBuffer.allocate((int) length - 8 - 4);
	        		inChannel.read(content);
	        		content.order(ByteOrder.LITTLE_ENDIAN);
	        		content.rewind();
	        		if (s.contentEquals("XML0"))
	        		{
	        			parseXML0(content);
	        		}
	        		else if (s.contentEquals("FIL1"))
	        		{
	        			parseFIL1(content);
	        		}
	        		else if (s.contentEquals("RAW3"))
	        		{
	        			parseRAW3(content);
	        		}
	        		
	        		//log.info("position " + inChannel.position());
	        		
	        		ByteBuffer lenEndBuffer = ByteBuffer.allocate(4);
	        		lenEndBuffer.order(ByteOrder.LITTLE_ENDIAN);
	        		inChannel.read(lenEndBuffer);
	        		lenEndBuffer.rewind();
	        		long lenEnd = lenEndBuffer.getInt() & 0xffffffff;
	        		
	        		log.info("End packet Length " + lenEnd);
	        		
	        		buffer.clear(); // do something with the data and clear/compact it.
	        }
	        inChannel.close();
	        aFile.close();
		}
		catch (FileNotFoundException e)
		{
			log.error(e);
		}
		catch (IOException e)
		{
			log.error(e);
		}
	}
	
	private void parseXML0(ByteBuffer content)
	{
		String sCont = new String(content.array());
		System.out.print(sCont);
		System.out.println();
	}
	private void parseFIL1(ByteBuffer content)
	{
		//byte hdg[] = new byte[4];
		//content.get(hdg);
		int stage = content.getShort() & 0xffff;
		byte spare[] = new byte[2];
		content.get(spare);
		byte channelID[] = new byte[128];
		content.get(channelID);
		int noOfCoeff = content.getShort() & 0xffff;
		int decimationFact = content.getShort() & 0xffff;

		log.info("FIL1 stage " + stage + " noCoeff " + noOfCoeff + " channel " + new String(channelID));

		float coeffRe[] = new float[noOfCoeff];
		float coeffIm[] = new float[noOfCoeff];
		for (int i=0;i<noOfCoeff;i++)
		{
			coeffRe[i] = content.getFloat();
			coeffIm[i] = content.getFloat();
		}
		
	}
	private void parseRAW3(ByteBuffer content)
	{
		byte channelID[] = new byte[128];
		content.get(channelID);
		int dataType = content.getShort() & 0xffff;
		
		int nDataPerSample = dataType >> 8;
		
		byte spare[] = new byte[2];
		content.get(spare);
		int offset = content.getInt() & 0xffffffff;
		int count = content.getInt() & 0xffffffff;
		
		log.info("RAW3 data noPerSample " + nDataPerSample + " type " + (dataType & 0xf) + " offset " + offset + " count " + count);
				
		float samplesRe[] = new float[count * nDataPerSample];
		float samplesIm[] = new float[count * nDataPerSample];
		for (int i=0;i<(count * nDataPerSample);i++)
		{
			samplesRe[i] = content.getFloat();
			samplesIm[i] = content.getFloat();
		}
		
	}

	public static void main(String[] args)
	{
		ParseEK80 p = new ParseEK80();
		
		p.read(args[0]);
		
	}

}
