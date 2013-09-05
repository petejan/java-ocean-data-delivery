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

/**
 *
 * @author peter
 */
public class SeabirdSBE16Pulse7Parser extends AbstractDataParser
{
    @Override
    protected boolean isHeader(String dataLine)
    {
        /*
         * data lines have the format
         *  19.1826, -0.00003,    0.134, 2.5071, 0.0001, 4.2111, 4.9350, 1.6651, 2.6558, 101484500, 18.955, 17 Aug 2010 06:02:38
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
        String pressureString;

        String volt1String;
        String volt2String;
        String volt3String;
        String volt4String;
        String volt5String;
        String volt6String;
        String GTDPressureString;
        String GTDTemperatureString;

        String dateString;

        Timestamp dataTimestamp = null;
        
        Double waterTemp = null;
        Double pressure = null;
        Double CNDC = null;
        
        Double volt1 = null;
        Double volt2 = null;
        Double volt3 = null;
        Double volt4 = null;
        Double volt5 = null;
        Double volt6 = null;
        Double GTDPressureVal = null;
        Double GTDTemperatureVal = null;

        String constructTimestamp;

        StringTokenizer st = new StringTokenizer(dataLine,",");
        int tokenCount = st.countTokens();
        try
        {
            temperatureString = st.nextToken();
            CNDCString  = st.nextToken();
            pressureString  = st.nextToken();

            volt1String = st.nextToken();
            volt2String = st.nextToken();
            volt3String = st.nextToken();
            volt4String = st.nextToken();
            volt5String = st.nextToken();
            volt6String = st.nextToken();
            GTDPressureString = st.nextToken();
            GTDTemperatureString = st.nextToken();
            if (tokenCount > 12)
            {
                st.nextToken();
            }

            dateString = st.nextToken();

            constructTimestamp = dateString.trim();

            try
            {
                java.util.Date d = dateParser.parse(constructTimestamp);
                dataTimestamp = new Timestamp(d.getTime());
            }
            catch(ParseException pex)
            {
                throw new ParseException("Timestamp parse failed for text '" + constructTimestamp + "'", 0);
            }

            waterTemp = getDouble(temperatureString);
            pressure = getDouble(pressureString);
            CNDC = getDouble(CNDCString);
            
            volt1 = getDouble(volt1String);
            volt2 = getDouble(volt2String);
            volt3 = getDouble(volt3String);
            volt4 = getDouble(volt4String);
            volt5 = getDouble(volt5String);
            volt6 = getDouble(volt6String);
            GTDPressureVal = getDouble(GTDPressureString);
            GTDTemperatureVal = getDouble(GTDTemperatureString);
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

            row.setParameterCode("PRES");
            row.setParameterValue(pressure);
            row.setSourceFileID(currentFile.getDataFilePrimaryKey());
            row.setQualityCode("RAW");

            ok = row.insert();

            row.setParameterCode("CNDC");
            row.setParameterValue(CNDC);
            row.setSourceFileID(currentFile.getDataFilePrimaryKey());
            row.setQualityCode("RAW");

            ok = row.insert();
            //
            // now for the other stuff.....
            //
            row.setParameterCode("VOLT1");
            row.setParameterValue(volt1);
            row.setSourceFileID(currentFile.getDataFilePrimaryKey());
            row.setQualityCode("RAW");
            ok = row.insert();

            row.setParameterCode("VOLT2");
            row.setParameterValue(volt2);
            row.setSourceFileID(currentFile.getDataFilePrimaryKey());
            row.setQualityCode("RAW");
            ok = row.insert();

            row.setParameterCode("VOLT3");
            row.setParameterValue(volt3);
            row.setSourceFileID(currentFile.getDataFilePrimaryKey());
            row.setQualityCode("RAW");
            ok = row.insert();

            row.setParameterCode("VOLT4");
            row.setParameterValue(volt4);
            row.setSourceFileID(currentFile.getDataFilePrimaryKey());
            row.setQualityCode("RAW");
            ok = row.insert();

            row.setParameterCode("VOLT5");
            row.setParameterValue(volt5);
            row.setSourceFileID(currentFile.getDataFilePrimaryKey());
            row.setQualityCode("RAW");
            ok = row.insert();

            row.setParameterCode("VOLT6");
            row.setParameterValue(volt6);
            row.setSourceFileID(currentFile.getDataFilePrimaryKey());
            row.setQualityCode("RAW");
            ok = row.insert();

            row.setParameterCode("DISSOLVED_AIR_PRESS");
            row.setParameterValue(GTDPressureVal);
            row.setSourceFileID(currentFile.getDataFilePrimaryKey());
            row.setQualityCode("RAW");

            ok = row.insert();

            row.setParameterCode("GTD_TEMPERATURE");
            row.setParameterValue(GTDTemperatureVal);
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
