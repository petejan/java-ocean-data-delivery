/*
 * IMOS data delivery project
 * Written by Peter Wiley
 * This code is copyright (c) Peter Wiley 2000 - ?
 * It is made available under the BSD Software Licence in the hope that it may be useful.
 * It has NO WARRANTY OF FITNESS OR SUITABILITY FOR ANY PURPOSE.
 * Feel free to fix any bugs that you may find.
 */

package org.imos.dwm.parsers;

import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import org.imos.dwm.dbms.RawInstrumentData;
import org.wiley.util.StringUtilities;

/**
 *
 * @author peter
 */
public class SeabirdSBE56Parser extends AbstractDataParser
{

    @Override
    protected boolean isHeader(String dataLine)
    {
        /*
         * header lines have the format
         * % Instrument type = SBE56
         * 
         * EXCEPT for the column header descriptors which are quoted as is the data....
         * 
         * data lines have the format
         *  "2011-08-02","00:00:00","16.9037"
         * anything else is a header
         */
        //
        // get rid of quotes as they are a PITA
        //
        dataLine = StringUtilities.stripDoubleQuote(dataLine);
        
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
        SimpleDateFormat dateParser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        DecimalFormat deciFormat = new DecimalFormat("-######.0#");

        String dateString;
        String timeString;
        String temperatureString;
        

        Timestamp dataTimestamp = null;
        Double waterTemp = null;
        
        String constructTimestamp;

        StringTokenizer st = new StringTokenizer(dataLine,",");
        try
        {
            dateString = StringUtilities.stripDoubleQuote(st.nextToken());
            timeString = StringUtilities.stripDoubleQuote(st.nextToken());
            temperatureString = StringUtilities.stripDoubleQuote(st.nextToken());
            
            constructTimestamp = dateString.trim() + " " + timeString.trim();

            try
            {
                java.util.Date d = dateParser.parse(constructTimestamp);
                dataTimestamp = new Timestamp(d.getTime());
            }
            catch(ParseException pex)
            {
                throw new ParseException("Timestamp parse failed for text '" + constructTimestamp + "'",0);
            }

            try
            {
                waterTemp = new Double(temperatureString.trim());
            }

            catch(NumberFormatException pex)
            {
                try
                {
                    Number n = deciFormat.parse(temperatureString.trim());
                    waterTemp = n.doubleValue();
                }
                catch(ParseException pexx)
                {
                    throw new ParseException("parse failed for text '" + temperatureString + "'",0);
                }
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
            row.setParameterCode("TEMP");
            row.setParameterValue(waterTemp);
            row.setSourceFileID(currentFile.getDataFilePrimaryKey());
            row.setQualityCode("RAW");

            boolean ok = row.insert();           
        }
        catch (NoSuchElementException nse)
        {
          throw nse;
        }
    }

}
