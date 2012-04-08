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
        String conductivityString;
        String pressureString;

        String SBE43OxyString;
        String parString;
        String ecoFlntsCHLString;
        String ecoFlntsTURBString;
        String optodeDPhaseString;
        String optodeTempString;
        String GTDPressureString;
        String GTDTemperatureString;

        String dateString;

        Timestamp dataTimestamp = null;
        Double waterTemp = null;
        Double pressure = null;
        Double conductivity = null;
        Double SBE43OxyVal = null;
        Double PARVal = null;
        Double ecoFlntsCHLVal = null;
        Double ecoFlntsTURBVal = null;
        Double optodeDPhaseVal = null;
        Double optodeTempVal = null;
        Double GTDPressureVal = null;
        Double GTDTemperatureVal = null;

        String constructTimestamp;

        StringTokenizer st = new StringTokenizer(dataLine,",");
        try
        {
            temperatureString = st.nextToken();
            conductivityString  = st.nextToken();
            pressureString  = st.nextToken();

            SBE43OxyString = st.nextToken();
            parString = st.nextToken();
            ecoFlntsCHLString = st.nextToken();
            ecoFlntsTURBString = st.nextToken();
            optodeDPhaseString = st.nextToken();
            optodeTempString = st.nextToken();
            GTDPressureString = st.nextToken();
            GTDTemperatureString = st.nextToken();

            dateString = st.nextToken();

            constructTimestamp = dateString.trim();

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

            try
            {
                SBE43OxyVal = new Double(SBE43OxyString.trim());
            }
            catch(NumberFormatException pex)
            {
                try
                {
                    Number n = deciFormat.parse(SBE43OxyString.trim());
                    SBE43OxyVal = n.doubleValue();
                }
                catch(ParseException pexx)
                {
                    throw new ParseException("parse failed for text '" + SBE43OxyString + "'",0);
                }
            }

            try
            {
                PARVal = new Double(parString.trim());
            }
            catch(NumberFormatException pex)
            {
                try
                {
                    Number n = deciFormat.parse(parString.trim());
                    PARVal = n.doubleValue();
                }
                catch(ParseException pexx)
                {
                    throw new ParseException("parse failed for text '" + parString + "'",0);
                }
            }
            try
            {
                ecoFlntsCHLVal = new Double(ecoFlntsCHLString.trim());
            }
            catch(NumberFormatException pex)
            {
                try
                {
                    Number n = deciFormat.parse(ecoFlntsCHLString.trim());
                    ecoFlntsCHLVal = n.doubleValue();
                }
                catch(ParseException pexx)
                {
                    throw new ParseException("parse failed for text '" + ecoFlntsCHLString + "'",0);
                }
            }
            try
            {
                ecoFlntsTURBVal = new Double(ecoFlntsTURBString.trim());
            }
            catch(NumberFormatException pex)
            {
                try
                {
                    Number n = deciFormat.parse(ecoFlntsTURBString.trim());
                    ecoFlntsTURBVal = n.doubleValue();
                }
                catch(ParseException pexx)
                {
                    throw new ParseException("parse failed for text '" + ecoFlntsTURBString + "'",0);
                }
            }

            try
            {
                optodeDPhaseVal = new Double(optodeDPhaseString.trim());
            }
            catch(NumberFormatException pex)
            {
                try
                {
                    Number n = deciFormat.parse(optodeDPhaseString.trim());
                    optodeDPhaseVal = n.doubleValue();
                }
                catch(ParseException pexx)
                {
                    throw new ParseException("parse failed for text '" + optodeDPhaseString + "'",0);
                }
            }
            try
            {
                optodeTempVal = new Double(optodeTempString.trim());
            }
            catch(NumberFormatException pex)
            {
                try
                {
                    Number n = deciFormat.parse(optodeTempString.trim());
                    optodeTempVal = n.doubleValue();
                }
                catch(ParseException pexx)
                {
                    throw new ParseException("parse failed for text '" + optodeTempString + "'",0);
                }
            }

            try
            {
                GTDPressureVal = new Double(GTDPressureString.trim());
            }
            catch(NumberFormatException pex)
            {
                try
                {
                    Number n = deciFormat.parse(GTDPressureString.trim());
                    GTDPressureVal = n.doubleValue();
                }
                catch(ParseException pexx)
                {
                    throw new ParseException("parse failed for text '" + GTDPressureString + "'",0);
                }
            }
            try
            {
                GTDTemperatureVal = new Double(GTDTemperatureString.trim());
            }
            catch(NumberFormatException pex)
            {
                try
                {
                    Number n = deciFormat.parse(GTDTemperatureString.trim());
                    GTDTemperatureVal = n.doubleValue();
                }
                catch(ParseException pexx)
                {
                    throw new ParseException("parse failed for text '" + GTDTemperatureString + "'",0);
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
            //
            // now for the other stuff.....
            //
            row.setParameterCode("SBE43_OXY_VOLTAGE");
            row.setParameterValue(SBE43OxyVal);
            row.setSourceFileID(currentFile.getDataFilePrimaryKey());
            row.setQualityCode("RAW");

            ok = row.insert();

            row.setParameterCode("PAR");
            row.setParameterValue(PARVal);
            row.setSourceFileID(currentFile.getDataFilePrimaryKey());
            row.setQualityCode("RAW");

            ok = row.insert();

            row.setParameterCode("GTD_PRESSURE");
            row.setParameterValue(GTDPressureVal);
            row.setSourceFileID(currentFile.getDataFilePrimaryKey());
            row.setQualityCode("RAW");

            ok = row.insert();

            row.setParameterCode("GTD_TEMPERATURE");
            row.setParameterValue(GTDTemperatureVal);
            row.setSourceFileID(currentFile.getDataFilePrimaryKey());
            row.setQualityCode("RAW");

            ok = row.insert();

            AanderraOptodeParser aap = new AanderraOptodeParser();
            aap.setInstrumentDataFile(currentFile);
            aap.setInstrument(currentInstrument);
            aap.setInstrumentDepth(instrumentDepth);
            aap.setMooring(currentMooring);
            aap.insertData(dataTimestamp, optodeDPhaseVal, optodeTempVal);

            EcoFLNTUSParser efp = new EcoFLNTUSParser();
            efp.setInstrumentDataFile(currentFile);
            efp.setInstrument(currentInstrument);
            efp.setInstrumentDepth(instrumentDepth);
            efp.setMooring(currentMooring);
            efp.insertData(dataTimestamp, ecoFlntsCHLVal, ecoFlntsTURBVal);


        }
        catch (NoSuchElementException nse)
        {
          throw nse;
        }
    }

}
