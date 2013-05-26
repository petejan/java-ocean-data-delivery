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
public class AanderraCurrentMeterParser extends AbstractDataParser
{
    
    protected SimpleDateFormat dateParser = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    
    public AanderraCurrentMeterParser()
    {
        super();
        dateParser.setTimeZone(tz);
    }

    @Override
    protected boolean isHeader(String dataLine)
    {
        //
        // a valid data line has the format
        // 09,07,2010,12, 0, 0,108.50,-11.20,-112.30
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

        String yearString;
        String monthString;
        String dayString;
        String hourString;
        String minuteString;
        String secondString;
        
        String UString;
        String VString;
        String WString;


        Timestamp dataTimestamp = null;
        Double U_Value = null;
        Double V_Value = null;
        Double W_Value = null;

        String constructTimestamp;

        StringTokenizer st = new StringTokenizer(dataLine,",");
        try
        {
            monthString = st.nextToken();
            dayString = st.nextToken();
            yearString = st.nextToken();
            hourString = st.nextToken();
            minuteString = st.nextToken();
            secondString = st.nextToken();
            
            UString = st.nextToken();
            VString = st.nextToken();
            WString = st.nextToken();
            
           
            constructTimestamp = yearString.trim() 
                                + "/" 
                                + monthString.trim()
                                + "/"
                                + dayString.trim()
                                + " "
                                + hourString.trim()
                                + ":"
                                + minuteString.trim()
                                + ":"
                                + secondString.trim()
                                ;

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
                U_Value = new Double(UString.trim());
            }

            catch(NumberFormatException pex)
            {
                try
                {
                    Number n = deciFormat.parse(UString.trim());
                    U_Value = n.doubleValue();
                }
                catch(ParseException pexx)
                {
                    throw new ParseException("parse failed for text '" + UString + "'",0);
                }
            }
            
            try
            {
                V_Value = new Double(VString.trim());
            }

            catch(NumberFormatException pex)
            {
                try
                {
                    Number n = deciFormat.parse(VString.trim());
                    V_Value = n.doubleValue();
                }
                catch(ParseException pexx)
                {
                    throw new ParseException("parse failed for text '" + VString + "'",0);
                }
            }
            
            try
            {
                W_Value = new Double(WString.trim());
            }

            catch(NumberFormatException pex)
            {
                try
                {
                    Number n = deciFormat.parse(WString.trim());
                    W_Value = n.doubleValue();
                }
                catch(ParseException pexx)
                {
                    throw new ParseException("parse failed for text '" + WString + "'",0);
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
            row.setParameterCode("U_VALUE");
            row.setParameterValue(U_Value);
            row.setSourceFileID(currentFile.getDataFilePrimaryKey());
            row.setQualityCode("RAW");

            boolean ok = row.insert();

            row.setParameterCode("V_VALUE");
            row.setParameterValue(V_Value);
            
            ok = row.insert();
            
            row.setParameterCode("W_VALUE");
            row.setParameterValue(W_Value);
            
            ok = row.insert();

            

        }
        catch (NoSuchElementException nse)
        {
          throw nse;
        }
    }
}
