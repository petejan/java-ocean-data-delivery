/*
 * This code is copyright Peter Wiley 1998-2008
 * It is made available under the BSD Software Licence in the hope that it may be useful.
 * It has NO WARRANTY OF FITNESS OR SUITABILITY FOR ANY PURPOSE.
 * Feel free to fix any bugs that you may find.
 *
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
public class SeabirdSBR37ODOParser extends SeabirdSBE37Parser
{

    /**
     * this subclass is needed because the data file has 2 extra columns for internally
     * calculated dissolved oxygen and salinity.
     * 
     * As of 20130528 we are not loading these parameters into the database so this method
     * simply reads the values (tokens) from the data line and ignores them
     * 
     * @param dataLine
     * @throws ParseException
     * @throws NoSuchElementException 
     */
    @Override
    protected void parseData(String dataLine) throws ParseException, NoSuchElementException
    {
        SimpleDateFormat dateParser = new SimpleDateFormat("dd MMM yyyy HH:mm:ss");
        DecimalFormat deciFormat = new DecimalFormat("-######.0#");

        String temperatureString;
        String CNDCString;
        String pressureString;
        String dissolvedOxyString;
        String salinityString;

        String dateString;
        String timeString;


        Timestamp dataTimestamp = null;
        Double waterTemp = null;
        Double pressure = null;
        Double CNDC = null;

        String constructTimestamp;

        StringTokenizer st = new StringTokenizer(dataLine,",");
        try
        {
            temperatureString = st.nextToken();
            CNDCString  = st.nextToken();
            pressureString  = st.nextToken();
            
            dissolvedOxyString = st.nextToken();
            salinityString  = st.nextToken();
            
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



        }
        catch (NoSuchElementException nse)
        {
          throw nse;
        }
    }
}
