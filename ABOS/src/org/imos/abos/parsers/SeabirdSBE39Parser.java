/*
 * IMOS data delivery project
 * Written by Peter Wiley
 * This code is copyright (c) Peter Wiley 2000 - ?
 * It is made available under the BSD Software Licence in the hope that it may be useful.
 * It has NO WARRANTY OF FITNESS OR SUITABILITY FOR ANY PURPOSE.
 * Feel free to fix any bugs that you may find.
 */

package org.imos.abos.parsers;

import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import org.imos.abos.dbms.RawInstrumentData;
import org.wiley.util.StringUtilities;

import java.util.Locale;

/**
 *
 * @author peter
 */
public class SeabirdSBE39Parser extends AbstractDataParser
{

    @Override
    protected boolean isHeader(String dataLine)
    {
        /*
         * header lines have the format
         * ds
         * SBE 39 V 3.1b   SERIAL NO. 5269    04 Nov 2013  09:41:08
         * 
         * data lines have the format
         *   21.4668, 04 Apr 2013, 14:19:20
         * anything else is a header
         */
        
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

    @Override
    protected void parseHeader(String dataLine) throws ParseException, NoSuchElementException
    {
    }

    @Override
    protected void parseData(String dataLine) throws ParseException, NoSuchElementException
    {
        SimpleDateFormat dateParser = new SimpleDateFormat("dd MMM yyyy HH:mm:ss", Locale.US);
        dateParser.setTimeZone(tz);
        DecimalFormat deciFormat = new DecimalFormat("-######.0#");

        String[] split = dataLine.split(",");
        int nSplit = split.length;
        
        Timestamp dataTimestamp = new Timestamp(dateParser.parse(split[nSplit-2].trim() + " " + split[nSplit-1].trim()).getTime());
        Double waterTemp = getDouble(split[0]);
        
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
        row.setSourceFileID(currentFile.getDataFilePrimaryKey());
        row.setQualityCode("RAW");

        if (instrumentDepth < 0)
        {
        	row.setParameterCode("AIRT");
        }
        else
        {
        	row.setParameterCode("TEMP");        	
        }
        row.setParameterValue(waterTemp);

        boolean ok = row.insert();           

        if (nSplit > 3)
        {
            Double pres = getDouble(split[1]);
            row.setParameterCode("PRES");        	
	        row.setParameterValue(pres);
	
	        ok = row.insert();
        }

    }

}
