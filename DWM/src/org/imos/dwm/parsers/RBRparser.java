package org.imos.dwm.parsers;

import static org.imos.dwm.parsers.AbstractDataParser.logger;

import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.imos.dwm.dbms.RawInstrumentData;

/*
 * IMOS data delivery project
 * Written by Peter Wiley
 * This code is copyright (c) Peter Wiley 2000 - ?
 * It is made available under the BSD Software Licence in the hope that it may be useful.
 * It has NO WARRANTY OF FITNESS OR SUITABILITY FOR ANY PURPOSE.
 * Feel free to fix any bugs that you may find.
 */

/**
 *
 * @author peter
 */
public class RBRparser extends AbstractDataParser
{
    SimpleDateFormat dateParser;
    int tsFormat = 0;
    private Timestamp startTime;
    private GregorianCalendar cal = (GregorianCalendar) Calendar.getInstance(tz);

    private long samplePeriod = 0;
    
    public Timestamp parseTs(String date) throws ParseException
    {
        String[] formats = {"yy/MM/dd HH:mm:ss", "yyyy/MMM/dd HH:mm:ss", "dd-MMM-yyyy HH:mm:ss"};
        java.util.Date d;        
        Timestamp t = null;
        
        for(int i=tsFormat;i<formats.length;i++)
        {
            try
            {
                dateParser = new SimpleDateFormat(formats[i]);
                dateParser.setTimeZone(tz);
                // System.out.println("Trying " + formats[i]);
                
                d = dateParser.parse(date);

                t = new Timestamp(d.getTime());
                tsFormat = i;

                return t;
            }
            catch (ParseException pe)
            {

            }
        }
                
        throw new ParseException("Timestamp parse failed for text '" + date + "'", 0);        
    }

    public RBRparser()
    {
        super();
    }
    
    @Override
    protected void parseHeader(String dataLine) throws ParseException, NoSuchElementException
    {
        if (dataLine.contains("Serial"))
        {
            String info = "File Serial Number " + dataLine.substring(dataLine.indexOf("=")+1);
            logger.info(info);
            if (parentForm != null)
            {
                parentForm.updateMessageArea(info + "\n");
            }
        }
        
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
        DecimalFormat deciFormat = new DecimalFormat("-######.0#");

        String temperatureString;
        String pressureString;
        String depthString;

        Timestamp dataTimestamp = null;
        Double waterTemp = null;
        Double pressure = null;
        Double depth = null;
        
        StringTokenizer st = new StringTokenizer(dataLine," ");
        int tsCount = st.countTokens();
        
        if (tsCount < 2)
        {
            return;
        }

        // Pulse-6  TDR-2050 :    11.9397    8.0304 
        // Pulse-7  TDR-2050 :    10/09/08 00:00:00.000   16.9344501     9.0141177    -1.1092620 
        // Pulse-8  TDR-2050 : 2011/Aug/02 00:00:00.000   14.4874672     8.7803044    -1.3411686 
        // Pulse-9  DR-1050  : 12-Jul-2012 00:00:00.000   10.1162830    -0.0160844 
        // Pulse-9  TDR-1050 : 12-Jul-2012 00:00:00.000   14.2620953     9.7817173    -0.3479219 
        // SAZ-12   TDR-2050 : 2009/09/26 00:00:00   19.8871    8.7332   -1.3879 
        // SAZ-12   DR-1050  : 2009/09/26 00:00:00    8.6436   -1.4768 
        
        try
        {
            if (tsCount > 2) // has timestamp
            {
                dataTimestamp = parseTs(dataLine);
                st.nextToken();
                st.nextToken();
            }
            else
            {
                int currentSecondValue = cal.get(Calendar.SECOND);
                int newSecondValue = currentSecondValue + (int) samplePeriod;
                cal.set(Calendar.SECOND, newSecondValue);

                dataTimestamp = new Timestamp(cal.getTimeInMillis());            
            }

            if ((tsCount > 4) || (tsCount == 2))
            {
                temperatureString = st.nextToken();
                waterTemp = getDouble(temperatureString);
            }

            pressureString  = st.nextToken();
            pressure = getDouble(pressureString) - 10.1325; // absoulte pressure to relative pressure
            
            if (tsCount >= 3)
            {
                depthString  = st.nextToken();
                depth = getDouble(depthString);
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
            row.setSourceFileID(currentFile.getDataFilePrimaryKey());
            row.setQualityCode("RAW");
            boolean ok = false;
            
            if (waterTemp != null)
            {
                row.setParameterCode("TEMP");
                row.setParameterValue(waterTemp);
                ok = row.insert();
            }
            
            row.setParameterCode("PRES");
            row.setParameterValue(pressure);
            
            ok = row.insert();
            
        }
        catch (NoSuchElementException nse)
        {
          throw nse;
        }
    }

    int dataBlock = 0;
    @Override
    protected boolean isHeader(String dataLine)
    {
        //
        // a valid data line has the format
        // 10/09/08 00:00:00.000   17.0605125     8.4384576    -1.6802278
        //
        // so anything else is a header
        //
        // first line after the Pres header line
        if (dataLine.contains(" Pres"))
        {
            dataBlock = 1;
        }
        else
        {
            if ((dataBlock == 1) && (Character.isDigit(dataLine.charAt(0))))
            {
                return false;
            }
        }

        return true;
    }

}
