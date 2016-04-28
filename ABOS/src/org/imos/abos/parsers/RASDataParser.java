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
public class RASDataParser extends AbstractDataParser
{

    public RASDataParser()
    {
        super();
    }
    
    @Override
    protected void parseData(String dataLine)
           throws ParseException, NoSuchElementException
    {
        if (!dataLine.startsWith("Event"))
        {
            return;
        }
        
        DecimalFormat deciFormat = new DecimalFormat("-######.0#");

        SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        
        String timestamp;
        String sEvent;
        
        Timestamp dataTimestamp = null;
        int event;
        
        // Event  1 of 48 @ 09/12/2010 02:00:00

        try
        {
            StringTokenizer st = new StringTokenizer(dataLine, " ");

//            while(st.hasMoreTokens())
//            {
//                System.out.println("Tokens " + st.nextToken());
//            }
            st.nextToken(); // Event
            event = Integer.parseInt(st.nextToken());
            st.nextToken(); // Of
            st.nextToken(); // 48
            st.nextToken(); // @
                    
            dataTimestamp = new Timestamp(df.parse(st.nextToken() + " " + st.nextToken()).getTime());

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
            row.setParameterCode("WATER_SAMPLE");
            row.setParameterValue(event);
            row.setSourceFileID(currentFile.getDataFilePrimaryKey());
            row.setQualityCode("RAW");

            boolean ok = row.insert();

        }
        catch (NoSuchElementException nse)
        {
          throw nse;
        }
    }

    @Override
    protected void parseHeader(String dataLine) throws ParseException, NoSuchElementException
    {
    }

    @Override
    protected boolean isHeader(String dataLine)
    {
        return false; // not just a header followed by data
    }
}
