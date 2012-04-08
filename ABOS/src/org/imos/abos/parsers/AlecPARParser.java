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
import org.imos.abos.dbms.RawInstrumentData;
import org.wiley.util.NullStringTokenizer;

/**
 *
 * @author peter
 * 
 * This parser is for the Alec MDS5 PAR instrument as used & datafile created for
 * the Pulse-7 mooring
 *
 */
public class AlecPARParser extends AbstractDataParser
{

    @Override
    protected void parseHeader(String dataLine) throws ParseException, NoSuchElementException
    {
        return;
    }

    @Override
    protected void parseData(String dataLine) throws ParseException, NoSuchElementException
    {
        SimpleDateFormat dateParser = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        DecimalFormat deciFormat = new DecimalFormat("-######.0#");

        String sampleCount;
        String dateString;
        String timeString;
        String dayNum;
        String lightString;
        String lightVal;

        String constructTimestamp;

        Timestamp dataTimestamp = null;
        Double PARVal = null;

        NullStringTokenizer st = new NullStringTokenizer(dataLine,",");
        try
        {
            sampleCount = st.nextToken();
            dateString = st.nextToken();
            timeString = st.nextToken();
            dayNum = st.nextToken();
            lightString  = st.nextToken();
            lightVal  = st.nextToken();

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
                PARVal = new Double(lightString.trim());
            }

            catch(NumberFormatException pex)
            {
                try
                {
                    Number n = deciFormat.parse(lightString.trim());
                    PARVal = n.doubleValue();
                }
                catch(ParseException pexx)
                {
                    throw new ParseException("parse failed for text '" + lightString + "'",0);
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
            row.setParameterCode("PAR");
            row.setParameterValue(PARVal);
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
    protected boolean isHeader(String dataLine)
    {
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
