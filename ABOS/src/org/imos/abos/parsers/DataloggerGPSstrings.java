/* This code is copyright Peter Wiley 1998-2008
 * It is made available under the BSD Software Licence in the hope that it may be useful.
 * It has NO WARRANTY OF FITNESS OR SUITABILITY FOR ANY PURPOSE.
 * Feel free to fix any bugs that you may find.
 *
 */

package org.imos.abos.parsers;

import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import org.imos.abos.dbms.RawInstrumentData;

/**
 *
 * @author peter
 */
public class DataloggerGPSstrings extends AbstractDataParser
{

    
@Override
    protected void parseData(String dataLine) throws ParseException, NoSuchElementException
    {
        /*
         * data sample
         * string GPGGA,011131.99,4253.16569,S,14720.23279,E,1,6,2.15,10.6,M,-8.9,M,,
		 * string GPRMC,011131.99,A,4253.16569,S,14720.23279,E,0.350,0.00,190215,,,A
		 * string GPGGA,011132.00,4253.16575,S,14720.23275,E,1,6,2.15,10.3,M,-8.9,M,,
		 * string GPRMC,011132.00,A,4253.16575,S,14720.23275,E,0.175,0.00,190215,,,A
         */
	
	// create with
	// grep -h "string GPRMC,[0-9]*.\d\d,A" /Volumes/DWM-2019/SOFS-7.5-2018/data/Datalogger/*.TXT > SOFS-7.5-GPSfixes.txt
	
        SimpleDateFormat dateParser = new SimpleDateFormat("ddMMyy HHmmss");
        DecimalFormat deciFormat = new DecimalFormat("-######.0#");

        String[] splitLine = dataLine.split(",");
        
        String latitudeString;
        String longitudeString;

        Timestamp dataTimestamp = null;
        Double latitude = null;
        Double longitude = null;

        String constructTimestamp;

        if (splitLine[0].contains("string GPRMC"))
        {
        	if (splitLine.length > 9)
        	{
	            constructTimestamp = splitLine[9] + " " + splitLine[1];
	            try
	            {
	                java.util.Date d = dateParser.parse(constructTimestamp);
	                dataTimestamp = new Timestamp(d.getTime());
	
		        	latitudeString = splitLine[3];
		            try
		            {
		                latitude = new Double(latitudeString.trim());
		            }
		            catch(NumberFormatException pex)
		            {
		                try
		                {
		                    Number n = deciFormat.parse(latitudeString.trim());
		                    latitude = n.doubleValue();
		                }
		                catch(ParseException pexx)
		                {
		                    throw new ParseException("parse failed for text '" + latitudeString.trim() + "'",0);
		                }
		            }
		            latitude = ddmmToDecimal(latitude);
		
		        	longitudeString = splitLine[5]; 	            
		            try
		            {
		                longitude = new Double(longitudeString.trim());
		            }	
		            catch(NumberFormatException pex)
		            {
		                try
		                {
		                    Number n = deciFormat.parse(longitudeString.trim());
		                    longitude = n.doubleValue();
		                }
		                catch(ParseException pexx)
		                {
		                    throw new ParseException("parse failed for text '" + longitudeString.trim() + "'",0);
		                }
		            }
		            longitude = ddmmToDecimal(longitude);
	
		            if (splitLine[4].contains("S"))
		        	{
		            	latitude = -latitude;
		        	}
		        	if (splitLine[6].contains("W"))
		        	{
		        		longitude = -longitude;
		        	}
			
		            //
		            // ok, we have parsed out the values we need, can now construct the raw data class
		            //
		            RawInstrumentData row = new RawInstrumentData();
		
		            row.setDataTimestamp(dataTimestamp);
		            row.setDepth(instrumentDepth);
		            row.setInstrumentID(currentInstrument.getInstrumentID());
		            row.setLatitude(currentMooring.getLatitudeIn());
		            row.setLongitude(currentMooring.getLongitudeIn());
		            row.setMooringID(currentMooring.getMooringID());
		            row.setParameterCode("YPOS");
		            row.setParameterValue(latitude);
		            row.setSourceFileID(currentFile.getDataFilePrimaryKey());
		            row.setQualityCode("RAW");
		
		            boolean ok = row.insert();
		
		            row.setParameterCode("XPOS");
		            row.setParameterValue(longitude);
		            row.setSourceFileID(currentFile.getDataFilePrimaryKey());
		            row.setQualityCode("RAW");
		
		            ok = row.insert();
	            }
	            catch(ParseException pex)
	            {
//	                throw new ParseException("Timestamp parse failed for text '" + constructTimestamp + "'",0);
	            }

	        }
        }

    }

	private Double ddmmToDecimal(Double ddmm)
	{
		double d = Math.floor(ddmm / 100);
		double m = ddmm % 100;
		
		return new Double(d + m/60);
	}

	@Override
	protected boolean isHeader(String dataLine)
	{
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	protected void parseHeader(String dataLine) throws ParseException, NoSuchElementException
	{
		// TODO Auto-generated method stub
		
	}
}
