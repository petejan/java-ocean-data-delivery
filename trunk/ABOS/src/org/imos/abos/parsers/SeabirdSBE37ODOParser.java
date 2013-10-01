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
import org.imos.abos.calc.SalinityCalculator;
import org.imos.abos.calc.SeawaterParameterCalculator;
import org.imos.abos.dbms.RawInstrumentData;

/**
 *
 * @author peter
 */
public class SeabirdSBE37ODOParser extends AbstractDataParser
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
        String CNDCString;
        String pressureString;
        String oxygenString;

        String dateString;
        String timeString;

        Timestamp dataTimestamp = null;
        Double waterTemp = null;
        Double pressure = null;
        Double CNDC = null;
        Double oxygen = null;

        String constructTimestamp;

        StringTokenizer st = new StringTokenizer(dataLine,",");
        int tokenCount = st.countTokens();
        try
        {
            temperatureString = st.nextToken();
            CNDCString  = st.nextToken();
            pressureString  = st.nextToken();
            oxygenString = st.nextToken();
            if (tokenCount > 6)
            {
                st.nextToken(); // ignore any salinity
            }
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

            waterTemp = getDouble(temperatureString);
            pressure = getDouble(pressureString);
            CNDC = getDouble(CNDCString);
            
            if (!oxygenString.contains("nan"))
            {
                oxygen = getDouble(oxygenString);
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

            if (oxygen != null)
            {
                // convert oxygen from ml/l to uM/kg

                double calculatedSalinityValue = SalinityCalculator.calculateSalinityForITS90Temperature(waterTemp,
                                                                                                CNDC * 10,
                                                                                                pressure
                                                                                                );
                double calculatedSeawaterDensity = SeawaterParameterCalculator.calculateSeawaterDensityAtPressure(calculatedSalinityValue,
                                                                                                        waterTemp,
                                                                                                        pressure);


                row.setParameterCode("DOX2");
                row.setParameterValue(oxygen*44660.0/calculatedSeawaterDensity);
                row.setSourceFileID(currentFile.getDataFilePrimaryKey());
                row.setQualityCode("RAW");

                ok = row.insert();
            }

        }
        catch (NoSuchElementException nse)
        {
          throw nse;
        }
    }

}
