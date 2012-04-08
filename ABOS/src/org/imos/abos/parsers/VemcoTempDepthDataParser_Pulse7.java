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
import org.imos.abos.dbms.RawInstrumentData;
import org.wiley.util.NullStringTokenizer;

/**
 *
 * @author peter
 *
 * this parser is for the Vemco instruments with both temperature & depth sensors
 */
public class VemcoTempDepthDataParser_Pulse7 extends VemcoDataParser_Pulse7
{

    public VemcoTempDepthDataParser_Pulse7()
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
        String temperature;
        String AtoDTemperature;
        String depth;
        String AtoDDepth;

        Timestamp dataTimestamp = null;
        Double waterTemp = null;
        Double AtoDWaterTemp = null;
        Double waterDepth;
        Double AtoDWaterDepth;

        NullStringTokenizer st = new NullStringTokenizer(dataLine,",");
        try
        {
            timestamp = st.nextToken();
            temperature = st.nextToken();
            AtoDTemperature  = st.nextToken();
            depth = st.nextToken();
            AtoDDepth = st.nextToken();

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
                AtoDWaterTemp = new Double(AtoDTemperature.trim());
            }

            catch(NumberFormatException pex)
            {
                try
                {
                    Number n = deciFormat.parse(AtoDTemperature.trim());
                    AtoDWaterTemp = n.doubleValue();
                }
                catch(ParseException pexx)
                {
                    throw new ParseException("parse failed for text '" + AtoDTemperature + "'",0);
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
                    Number n = deciFormat.parse(AtoDTemperature.trim());
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
            row.setParameterCode("WATER_TEMP");
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
            row.setParameterCode("WATER_DEPTH");
            row.setParameterValue(waterTemp);
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
