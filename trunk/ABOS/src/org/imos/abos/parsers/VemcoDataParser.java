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
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.imos.abos.dbms.RawInstrumentData;
import static org.imos.abos.parsers.AbstractDataParser.logger;
import org.wiley.util.NullStringTokenizer;

/**
 *
 * @author peter
 */
public class VemcoDataParser extends AbstractDataParser
{

    public VemcoDataParser()
    {
        super();
    }
    
    int tsFormat = 0;
    
    public Timestamp parseTs(String date) throws ParseException
    {
        SimpleDateFormat dateParser;
        String[] formats = {"yyyy,MM,dd,HH,mm,ss", "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd,HH:mm:ss"};
        java.util.Date d;        
        Timestamp t = null;
        
        for(int i=tsFormat;i<formats.length;i++)
        {
            try
            {
                dateParser = new SimpleDateFormat(formats[i]);
                dateParser.setTimeZone(TimeZone.getTimeZone("UTC"));
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
   
    @Override
    protected void parseData(String dataLine)
           throws ParseException, NoSuchElementException
    {
        DecimalFormat deciFormat = new DecimalFormat("-######.0#");

        String timestamp;
        String sTemperature;
        String sAtoDTemperature;
        String sPress; // vemco call it depth, but its most certainly pressure relative to the surface
        String sAtoDpress;        

        Timestamp dataTimestamp = null;
        Double temperature = null;
        Double AtoDTemperature = null;
        Double press = null;
        Double AtoDpress = null;        

        // Pulse 6 T  ( 8): 2009,09,22,12,00,00,12.0,89
        // Pulse 6 TD (10): 2009,09,22,12,00,00,12.1,88,0.0,11
        // Pulse 7 T  ( 3): 2010-09-08 00:00:00,17.63,30
        // Pulse 7 TD ( 5): 2010-09-08 00:00:00,17.34,33,-1.8,10
        // Pulse 8 T  ( 3): 2011-08-27,04:18:00,11.22
        // Pulse 8 TD ( 4): 2011-08-02,04:41:40,15.59,-1.76
        // Pulse 9 T  ( 3): 2012-07-12,00:00:00,13.89

        dataTimestamp = parseTs(dataLine);
                
        boolean hasPress = false;
        boolean hasRaw = false;
        NullStringTokenizer st = new NullStringTokenizer(dataLine,",");
        //System.out.println("Tokens " + st.countTokens());
        try
        {
            switch (tsFormat)
            {
                case 0: 
                    st.nextToken();
                    st.nextToken();
                    st.nextToken();
                    st.nextToken();
                    st.nextToken();
                    st.nextToken();
                    if (st.countTokens() >= 4)
                    {
                        hasPress = true;
                    }
                    hasRaw = true;
                    break;                
                case 1: 
                    st.nextToken();
                    if (st.countTokens() >= 4)
                    {
                        hasPress = true;
                    }
                    hasRaw = true;
                    break;                
                case 2: 
                    st.nextToken();
                    st.nextToken();
                    if (st.countTokens() >= 2)
                    {
                        hasPress = true;
                    }
                    break;                
            }
            //System.out.println("Tokens remaining " + st.countTokens());
            
            sTemperature = st.nextToken();
            temperature = getDouble(sTemperature);
            if (hasRaw)
            {
                sAtoDTemperature  = st.nextToken();
                AtoDTemperature = getDouble(sAtoDTemperature);
            }
            if (hasPress)
            {
                sPress = st.nextToken();
                press = getDouble(sPress);
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
            row.setParameterValue(temperature);
            row.setSourceFileID(currentFile.getDataFilePrimaryKey());
            row.setQualityCode("RAW");

            boolean ok = row.insert();
            
            if (hasPress)
            {
                row.setDataTimestamp(dataTimestamp);
                row.setDepth(instrumentDepth);
                row.setInstrumentID(currentInstrument.getInstrumentID());
                row.setLatitude(currentMooring.getLatitudeIn());
                row.setLongitude(currentMooring.getLongitudeIn());
                row.setMooringID(currentMooring.getMooringID());
                row.setParameterCode("PRES");
                row.setParameterValue(press);
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

    @Override
    protected void parseHeader(String dataLine) throws ParseException, NoSuchElementException
    {
        if (dataLine.contains("Study"))
        {
            Pattern pattern = Pattern.compile("\\d+m");
            Matcher m = pattern.matcher(dataLine);
            if (m.find())
            {
                instrumentDepth = new Double(dataLine.substring(m.start(), m.end()-1));
                if (parentForm != null)
                {
                    parentForm.updateMessageArea("File Depth " + instrumentDepth + "\n");
                }
                
            }
        }
        else if (dataLine.contains("Serial Number"))
        {
            String info = "File Serial Number " + dataLine.substring(dataLine.indexOf("=")+1);
            logger.info(info);
            if (parentForm != null)
            {
                parentForm.updateMessageArea(info + "\n");
            }

        }
        else if (dataLine.contains("Source Device: "))
        {
            String info = "File Source Device " + dataLine.substring(dataLine.indexOf(":")+2);
            logger.info(info);
            if (parentForm != null)
            {
                parentForm.updateMessageArea(info + "\n");
            }

        }
    }

    @Override
    protected boolean isHeader(String dataLine)
    {
        char c = dataLine.charAt(0);
        
        if (dataLine.startsWith("*"))
        {
            return true; // it's a header row
        }
        else if(! Character.isDigit(c) )
        {
            return true; // it's a header row
        }
        else 
            return false;
    }
}
