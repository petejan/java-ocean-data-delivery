/*
 * Neonatal Screening Software Project
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

/**
 *
 * @author peter
 */
public class SeabirdSBE37Parser extends AbstractDataParser
{

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
        String conductivityString;
        String pressureString;


        String dateString;
        String timeString;


        Timestamp dataTimestamp = null;
        Double waterTemp = null;
        Double pressure = null;
        Double conductivity = null;

        String constructTimestamp;

        StringTokenizer st = new StringTokenizer(dataLine,",");
        try
        {
            temperatureString = st.nextToken();
            conductivityString  = st.nextToken();
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

            try
            {
                conductivity = new Double(conductivityString.trim());
            }

            catch(NumberFormatException pex)
            {
                try
                {
                    Number n = deciFormat.parse(conductivityString.trim());
                    conductivity = n.doubleValue();
                }
                catch(ParseException pexx)
                {
                    throw new ParseException("parse failed for text '" + conductivityString + "'",0);
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
            row.setParameterCode("WATER_TEMP");
            row.setParameterValue(waterTemp);
            row.setSourceFileID(currentFile.getDataFilePrimaryKey());
            row.setQualityCode("RAW");

            boolean ok = row.insert();

            row.setParameterCode("WATER_PRESSURE");
            row.setParameterValue(pressure);
            row.setSourceFileID(currentFile.getDataFilePrimaryKey());
            row.setQualityCode("RAW");

            ok = row.insert();

            row.setParameterCode("CONDUCTIVITY");
            row.setParameterValue(conductivity);
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
