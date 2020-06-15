package org.imos.dwm.parsers;

import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.TimeZone;

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
public class BranknerTDR2050Parser extends AbstractDataParser
{
    SimpleDateFormat dateParser;
    int tsFormat = 0;
    
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
                //System.out.println("Trying " + formats[i]);
                
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

    public BranknerTDR2050Parser()
    {
        super();
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
        DecimalFormat deciFormat = new DecimalFormat("-######.0#");

        String dateString;
        String timeString;
        String temperatureString;
        String pressureString;
        String depthString;

        Timestamp dataTimestamp = null;
        Double waterTemp = null;
        Double pressure = null;
        Double depth = null;
        
        dataTimestamp = parseTs(dataLine);

        StringTokenizer st = new StringTokenizer(dataLine," ");
        try
        {
            dateString = st.nextToken();
            timeString = st.nextToken();
            temperatureString = st.nextToken();
            pressureString  = st.nextToken();
            depthString  = st.nextToken();

            waterTemp = getDouble(temperatureString);
            pressure = getDouble(pressureString);
            depth = getDouble(depthString);
            
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
            
            ok = row.insert();
            
        }
        catch (NoSuchElementException nse)
        {
          throw nse;
        }
    }

    @Override
    protected boolean isHeader(String dataLine)
    {
        //
        // a valid data line has the format
        // 10/09/08 00:00:00.000   17.0605125     8.4384576    -1.6802278
        //
        // so anything else is a header
        //
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

}
