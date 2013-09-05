/*
 * IMOS Data Delivery Project
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
import org.imos.abos.dbms.RawInstrumentData;
import org.wiley.util.NullStringTokenizer;

/**
 *
 * @author peter
 */
public class VemcoDataParser_Pulse6 extends AbstractDataParser
{
    public VemcoDataParser_Pulse6()
    {
        super();
    }

    @Override
    protected void parseData(String dataLine)
           throws ParseException, NoSuchElementException
    {
        SimpleDateFormat dateParser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        DecimalFormat deciFormat = new DecimalFormat("-######.0#");

        String timestampString;

        String yearString;
        String monthString;
        String dayString;
        String hourString;
        String minuteString;
        String secondString;

        String temperature;
        String AtoDTemperature;

        Timestamp dataTimestamp = null;
        Double waterTemp = null;
        Double AtoDWaterTemp = null;

        NullStringTokenizer st = new NullStringTokenizer(dataLine,",");
        try
        {
            yearString = st.nextToken();
            monthString = st.nextToken();
            dayString = st.nextToken();
            hourString = st.nextToken();
            minuteString = st.nextToken();
            secondString = st.nextToken();

            temperature = st.nextToken();
            AtoDTemperature  = st.nextToken();

            timestampString = yearString
                            + "-"
                            + monthString
                            + "-"
                            + dayString
                            + " "
                            + hourString
                            + ":"
                            + minuteString
                            + ":"
                            + secondString
                            ;
            try
            {
                java.util.Date d = dateParser.parse(timestampString);
                dataTimestamp = new Timestamp(d.getTime());
            }
            catch(ParseException pex)
            {
                throw new ParseException("Timestamp parse failed for text '" + timestampString + "'",0);
            }

            waterTemp = getDouble(temperature);
            AtoDWaterTemp = getDouble(AtoDTemperature);

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

    @Override
    protected boolean isHeader(String dataLine)
    {
        if (dataLine.startsWith("*"))
        {
            //
            // it's a header row
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
        // the headers for this instrument are NOT well formed for parsing
        //
        NullStringTokenizer st = new NullStringTokenizer(dataLine," ");
        try
        {
            //
            // not all these tokens will be there....
            //
            String star = st.nextToken();
            String code = st.nextToken();
            String value = st.nextToken();
            String depthString = st.nextToken();

            if(depthString.endsWith("m"))
            {
                //
                // hopefully it's the depth of the instrument
                //
                char[] contents = depthString.trim().toCharArray();
                StringBuffer x = new StringBuffer();

                for( int i = 0; i < contents.length; i++)
                {
                    if(Character.isDigit(contents[i]))
                        x.append(contents[i]);
                }

                String test = x.toString();
                instrumentDepth = new Double(test);
            }

        }
        catch (NoSuchElementException nse)
        {
          throw nse;
        }
    }

}
