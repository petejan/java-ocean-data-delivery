/*
 * This code is copyright Peter Wiley 1998-2008
 * It is made available under the BSD Software Licence in the hope that it may be useful.
 * It has NO WARRANTY OF FITNESS OR SUITABILITY FOR ANY PURPOSE.
 * Feel free to fix any bugs that you may find.
 *
 */

package org.imos.dwm.parsers;

import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.NoSuchElementException;

import org.imos.dwm.dbms.RawInstrumentData;
import org.wiley.util.NullStringTokenizer;

/**
 *
 * @author peter
 */
public class VemcoTempDepthDataParser_Pulse8 extends VemcoDataParser_Pulse8
{
    public VemcoTempDepthDataParser_Pulse8()
    {
        super();
    }

    @Override
    protected void parseData(String dataLine)
           throws ParseException, NoSuchElementException
    {
        SimpleDateFormat dateParser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        DecimalFormat deciFormat = new DecimalFormat("-######.0#");

        String timestamp;
        
        String dateString;
        String timeString;
        String temperature;
        String depth;

        Timestamp dataTimestamp = null;
        Double waterTemp = null;
        Double waterDepth;

        NullStringTokenizer st = new NullStringTokenizer(dataLine,",");
        try
        {
            dateString = st.nextToken();
            timeString = st.nextToken();
            temperature = st.nextToken();
            depth = st.nextToken();

            timestamp = dateString + " " + timeString;
            
            try
            {
                java.util.Date d = dateParser.parse(timestamp);
                dataTimestamp = new Timestamp(d.getTime());
            }
            catch(ParseException pex)
            {
                throw new ParseException("Timestamp parse failed for text '" + timestamp + "'",0);
            }

            try
            {
                waterTemp = new Double(temperature.trim());
            }

            catch(NumberFormatException pex)
            {
                try
                {
                    Number n = deciFormat.parse(temperature.trim());
                    waterTemp = n.doubleValue();
                }
                catch(ParseException pexx)
                {
                    throw new ParseException("parse failed for text '" + temperature + "'",0);
                }
            }

            try
            {
                waterDepth = new Double(depth.trim());
            }

            catch(NumberFormatException pex)
            {
                try
                {
                    Number n = deciFormat.parse(depth.trim());
                    waterDepth = n.doubleValue();
                }
                catch(ParseException pexx)
                {
                    throw new ParseException("parse failed for text '" + depth + "'",0);
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

            row.setDataTimestamp(dataTimestamp);
            row.setDepth(instrumentDepth);
            row.setInstrumentID(currentInstrument.getInstrumentID());
            row.setLatitude(currentMooring.getLatitudeIn());
            row.setLongitude(currentMooring.getLongitudeIn());
            row.setMooringID(currentMooring.getMooringID());
            row.setParameterCode("PRES");
            row.setParameterValue(waterDepth);
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
