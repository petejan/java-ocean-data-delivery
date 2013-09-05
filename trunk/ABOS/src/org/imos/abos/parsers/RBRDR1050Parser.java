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
public class RBRDR1050Parser extends AbstractDataParser
{

    protected SimpleDateFormat dateParser = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    
    public RBRDR1050Parser()
    {
        super();
        dateParser.setTimeZone(tz);
    }
    
    @Override
    protected boolean isHeader(String dataLine)
    {
        //
        // a valid data line has the format
        // 2010/09/11 00:00:00  -14.0874  -24.0224 
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

        String dateString;
        String timeString;
        String pressureString;
        String depthString;

        Timestamp dataTimestamp = null;
        Double pressure = null;
        Double depth = null;

        String constructTimestamp;

        StringTokenizer st = new StringTokenizer(dataLine," ");
        
        if(st.countTokens() < 4)
            return;
        
        try
        {
            dateString = st.nextToken();
            timeString = st.nextToken();
            pressureString  = st.nextToken();
            depthString  = st.nextToken();

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
                depth = new Double(depthString.trim());
            }

            catch(NumberFormatException pex)
            {
                try
                {
                    Number n = deciFormat.parse(depthString.trim());
                    depth = n.doubleValue();
                }
                catch(ParseException pexx)
                {
                    throw new ParseException("parse failed for text '" + depthString + "'",0);
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
            row.setParameterCode("PRES");
            row.setParameterValue(pressure);
            row.setSourceFileID(currentFile.getDataFilePrimaryKey());
            row.setQualityCode("RAW");

            boolean ok = row.insert();

            row.setParameterCode("DEPTH");
            row.setParameterValue(depth);
            
            ok = row.insert();
        }
        catch (NoSuchElementException nse)
        {
          throw nse;
        }
    }

}
