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
import org.apache.log4j.Logger;
import org.imos.abos.dbms.RawInstrumentData;

/**
 *
 * @author peter
 */
public class SeabirdSBE37Parser extends AbstractDataParser
{
    protected static Logger logger = Logger.getLogger(SeabirdSBE37Parser.class.getName());
    
    @Override
    protected boolean isHeader(String dataLine)
    {
        /*
         * data lines have the format
         *  19.0084, 0.00000,    0.122, 07 Sep 2010, 03:49:23
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
        //
        // don't care about the headers
        //
    }

    @Override
    protected void parseData(String dataLine) throws ParseException, NoSuchElementException
    {
        SimpleDateFormat dateParser = new SimpleDateFormat("dd MMM yyyy HH:mm:ss");
        DecimalFormat deciFormat = new DecimalFormat("-######.0#");

        String temperatureString;
        String CNDCString;
        String pressureString = null;


        String dateString;
        String timeString;


        Timestamp dataTimestamp = null;
        Double waterTemp = null;
        Double pressure = null;
        Double CNDC = null;

        String constructTimestamp;

        StringTokenizer st = new StringTokenizer(dataLine,",");
        int tCount = st.countTokens();
        try
        {
            temperatureString = st.nextToken();
            CNDCString  = st.nextToken();
            if (tCount > 4)
                pressureString  = st.nextToken();
            dateString = st.nextToken();
            timeString = st.nextToken();

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

            if (tCount > 5)
            {
                try
                {
                    pressure = new Double(pressureString.trim());
                }

                catch(NumberFormatException pex)
                {
                    try
                    {
                        Number n = deciFormat.parse(pressureString.trim());
                        pressure = n.doubleValue();
                    }
                    catch(ParseException pexx)
                    {
                        throw new ParseException("parse failed for text '" + pressureString + "'",0);
                    }
                }
            }

            try
            {
                CNDC = new Double(CNDCString.trim());
            }

            catch(NumberFormatException pex)
            {
                try
                {
                    Number n = deciFormat.parse(CNDCString.trim());
                    CNDC = n.doubleValue();
                }
                catch(ParseException pexx)
                {
                    throw new ParseException("parse failed for text '" + CNDCString + "'",0);
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

            if (pressure != null)
            {
                row.setParameterCode("PRES");
                row.setParameterValue(pressure);
                row.setSourceFileID(currentFile.getDataFilePrimaryKey());
                row.setQualityCode("RAW");

                ok = row.insert();
            }

            row.setParameterCode("CNDC");
            row.setParameterValue(CNDC);
            row.setSourceFileID(currentFile.getDataFilePrimaryKey());
            row.setQualityCode("RAW");

            ok = row.insert();



        }
        catch (NoSuchElementException nse)
        {
          throw nse;
        }
    }

}
