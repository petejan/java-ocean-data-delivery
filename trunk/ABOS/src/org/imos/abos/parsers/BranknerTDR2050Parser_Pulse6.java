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
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import org.imos.abos.dbms.RawInstrumentData;

/**
 *
 * @author peter
 */
public class BranknerTDR2050Parser_Pulse6 extends AbstractDataParser
{

    private Timestamp startTime;
    private GregorianCalendar cal = (GregorianCalendar) Calendar.getInstance(tz);

    private long samplePeriod = 0;

    @Override
    protected boolean isHeader(String dataLine)
    {
        //
        // a valid data line has the format
        //  11.9397    8.0304
        //
        // so anything else is a header
        //

        dataLine = dataLine.trim();
        char c = dataLine.charAt(0);
        if(! Character.isDigit(c) )
        {
            //
            // it is a header
            //
            return true;
        }
        else
        {
            //
            // some single digit values for calibration coefficients in the header stuff so....
            //
            StringTokenizer st = new StringTokenizer(dataLine," ");
            if(st.countTokens() == 1)
                return true;
            else
                return false;
        }
    }

    @Override
    protected void parseHeader(String dataLine) throws ParseException, NoSuchElementException
    {
        SimpleDateFormat dateParser = new SimpleDateFormat("yy/MM/dd HH:mm:ss");
        SimpleDateFormat timeParser = new SimpleDateFormat("HH:mm:ss");

        if(dataLine.startsWith("Logging start"))
        {
            String timeString = dataLine.substring(13).trim();
            startTime = new Timestamp(dateParser.parse(timeString).getTime());
            cal.setTime(startTime);
        }

        if(dataLine.startsWith("Sample period"))
        {
            Date anchor = new Date();
            String period = dataLine.substring(14).trim();

            Date tempDate = timeParser.parse(period);
            long magicNumber = tempDate.getTime()/1000;
            //long magicNumber = tempDate.getTime() - anchor.getTime();
            logger.debug("Time increment in seconds is " + magicNumber);
            samplePeriod = magicNumber;
        }
    }

    @Override
    protected void parseData(String dataLine) throws ParseException, NoSuchElementException
    {
        SimpleDateFormat dateParser = new SimpleDateFormat("yy/MM/dd HH:mm:ss");
        DecimalFormat deciFormat = new DecimalFormat("-######.0#");

        String temperatureString;
        String pressureString;

        Timestamp dataTimestamp = null;
        Double waterTemp = null;
        Double pressure = null;

        StringTokenizer st = new StringTokenizer(dataLine," ");
        try
        {
            temperatureString = st.nextToken();
            pressureString  = st.nextToken();

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

            int currentSecondValue = cal.get(Calendar.SECOND);
            int newSecondValue = currentSecondValue + (int) samplePeriod;
            cal.set(Calendar.SECOND, newSecondValue);

            dataTimestamp = new Timestamp(cal.getTimeInMillis());
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

        }
        catch (NoSuchElementException nse)
        {
          throw nse;
        }
    }

}
