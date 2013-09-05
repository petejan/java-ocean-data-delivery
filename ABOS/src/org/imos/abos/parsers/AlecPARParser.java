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
import static org.imos.abos.parsers.AbstractDataParser.logger;
import org.wiley.util.NullStringTokenizer;

/**
 *
 * @author peter
 * 
 * This parser is for the Alec MDS5 PAR instrument 
 *
 */
public class AlecPARParser extends AbstractDataParser
{
    @Override
    protected void parseHeader(String dataLine) throws ParseException, NoSuchElementException
    {
        if (dataLine.contains("Inst_No") || dataLine.contains("InstNo"))
        {
            int end = dataLine.indexOf(",");
            if (end < 0)
            {
                end = dataLine.length();
            }
            
            String info = "File Serial Number " + dataLine.substring(dataLine.indexOf("=")+1, end);
            logger.info(info);
            if (parentForm != null)
            {
                parentForm.updateMessageArea(info + "\n");
            }

        }
        
        return;
    }

    @Override
    protected void parseData(String dataLine) throws ParseException, NoSuchElementException
    {
        SimpleDateFormat dateParser = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

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
        int tCount = st.countTokens();
        try
        {
            if (tCount > 4)
                sampleCount = st.nextToken();
            
            if (tCount >= 12)
            {
                dateString = st.nextToken();
                timeString = st.nextToken();
                constructTimestamp = dateString.trim() + " " + timeString.trim();
            }
            else
            {
                constructTimestamp = st.nextToken();
            }
            if (tCount > 4)
                dayNum = st.nextToken();
            
            lightString  = st.nextToken();
            
            if (tCount > 4)
                lightVal  = st.nextToken();


            try
            {
                java.util.Date d = dateParser.parse(constructTimestamp);
                dataTimestamp = new Timestamp(d.getTime());
            }
            catch(ParseException pex)
            {
                throw new ParseException("Timestamp parse failed for text '" + constructTimestamp + "'",0);
            }

            PARVal = getDouble(lightString);

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
