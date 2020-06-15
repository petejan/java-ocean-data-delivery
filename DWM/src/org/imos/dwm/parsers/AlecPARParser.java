/*
 * IMOS data delivery project
 * Written by Peter Wiley
 * This code is copyright (c) Peter Wiley 2000 - ?
 * It is made available under the BSD Software Licence in the hope that it may be useful.
 * It has NO WARRANTY OF FITNESS OR SUITABILITY FOR ANY PURPOSE.
 * Feel free to fix any bugs that you may find.
 */

package org.imos.dwm.parsers;

import static org.imos.dwm.parsers.AbstractDataParser.logger;

import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import org.imos.dwm.dbms.RawInstrumentData;
import org.wiley.util.NullStringTokenizer;

/**
 *
 * @author peter
 * 
 * This parser is for the Alec MDS5 PAR instrument 
 *
 */
public class AlecPARParser extends AbstractDataParser
{
    @Override
    protected void parseHeader(String dataLine) throws ParseException, NoSuchElementException
    {
        if (dataLine.contains("Inst_No") || dataLine.contains("InstNo") || dataLine.contains("SondeNo"))
        {
            int end = dataLine.indexOf(",");
            if (end < 0)
            {
                end = dataLine.length();
            }
            
            String info = "File Serial Number " + dataLine.substring(dataLine.indexOf("=")+1, end);
            logger.info(info);
            if (parentForm != null)
            {
                parentForm.updateMessageArea(info + "\n");
            }

        }
        
        return;
    }

    @Override
    protected void parseData(String dataLine) throws ParseException, NoSuchElementException
    {
        SimpleDateFormat dateParser = null;
        SimpleDateFormat mmddyyyy = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss aa"); // Alec data files are dependant on conversion machine settings
        SimpleDateFormat yyyymmdd = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss"); // Alec data files are dependant on conversion machine settings

        ///   Sample YYYY/MM/DD hh:mm:ss     Day Light[Micromol]      Light 
        //         1 2013/04/28 00:00:00      28       0.00          1 

///   Sample YYYY/MM/DD hh:mm:ss     Day Light[Micromol]      Light 
//         1 3/22/2015  12:00:00 AM    22       1.14          2 
//         2 3/22/2015  12:01:00 AM    22       0.00          1 
//         3 3/22/2015  12:02:00 AM    22       0.00          1 
//         4 3/22/2015  12:03:00 AM    22       0.00          1 
        
//        [Item]
//        		TimeStamp,Quantum [umol/(m^2s)],Batt. [V],
//        		2015/03/22 00:00:00,1.2,1.5,
//        		2015/03/22 00:01:00,1.2,1.5,
//        		2015/03/22 00:02:00,1.2,1.5,
//        		2015/03/22 00:03:00,1.1,1.5,
                
//        [Item],
//        /   Sample,YYYY/MM/DD,hh:mm:ss,Day,Light[Micromol],Light,
//        1,2015/03/22,00:00:00,22,1.53,2,
//        2,2015/03/22,00:01:00,22,1.53,2,
//        3,2015/03/22,00:02:00,22,1.53,2,
//        4,2015/03/22,00:03:00,22,3.06,3,
//        5,2015/03/22,00:04:00,22,0.00,1,
//        6,2015/03/22,00:05:00,22,1.53,2,
//        7,2015/03/22,00:06:00,22,1.53,2,
        
        String sampleCount;
        String dateString;
        String timeString;
        String dayNum;
        String lightString;
        String lightVal;

        String constructTimestamp;

        Timestamp dataTimestamp = null;
        Double PARVal = null;

        String dataSplit[] = dataLine.split("[, ]");
        int tCount = dataSplit.length;
        int lightn = 2;
        try
        {
        		if (tCount > 4)
                sampleCount = dataSplit[0];
            
            if (tCount > 4)
            {
                dateString = dataSplit[1];
                timeString = dataSplit[2];
                if (dataSplit[3].matches("AM|PM"))
                {
	                String AMPMString = dataSplit[3];
	                //String AMPMString = "";
	                constructTimestamp = dateString.trim() + " " + timeString.trim() + " " + AMPMString;
	                dateParser = mmddyyyy;
                }
                else
                {
	                constructTimestamp = dateString.trim() + " " + timeString.trim();
	                dateParser = yyyymmdd;                	
                }
                lightn = 5;
            }
            else if (tCount == 4)
            {
                dateString = dataSplit[0];
                timeString = dataSplit[1];
                constructTimestamp = dateString.trim() + " " + timeString.trim();
                dateParser = yyyymmdd;
                lightn = 2;
            }
            else
            {
                constructTimestamp = dataSplit[1];
                dateParser = yyyymmdd;
                lightn = 2;
            }
            if (tCount > 4)
                dayNum = dataSplit[3];
            
            lightString  = dataSplit[lightn];
            
            try
            {
                java.util.Date d = dateParser.parse(constructTimestamp);
                dataTimestamp = new Timestamp(d.getTime());
            }
            catch(ParseException pex)
            {
                throw new ParseException("Timestamp parse failed for text '" + constructTimestamp + "'",0);
            }

            PARVal = getDouble(lightString);

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
            row.setParameterCode("PAR");
            row.setParameterValue(PARVal);
            row.setSourceFileID(currentFile.getDataFilePrimaryKey());
            row.setQualityCode("RAW");

            boolean ok = row.insert();

        }
        catch (NoSuchElementException nse)
        {
          throw nse;
        }
    }

    @Override
    protected boolean isHeader(String dataLine)
    {
        char c = dataLine.charAt(0);
        if(! Character.isDigit(c) )
        {
            //
            // it is a header
            //
            return true;
        }
        else
            return false;
    }

}
