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
public class SeabirdSBE37ParserASC extends AbstractDataParser
{

    @Override
    protected boolean isHeader(String dataLine)
    {
        /*
         * Tv290C	Cond0S/m	Sal00	DD MMM YYYY HH:MM:SS
         * 18.1659	-0.000040	0.0000	04 Nov 2011 04:35:01         
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

    int tempIndex = -1;
    int condIndex = -1;
    int presIndex = -1;
    int salIndex = -1;
    int tsIndex = -1;
    
    @Override
    protected void parseHeader(String dataLine) throws ParseException, NoSuchElementException
    {
        StringTokenizer st = new StringTokenizer(dataLine, "\t");
        int tokenCount = st.countTokens();
        
        for(int i=0;i<tokenCount;i++)
        {
            String token = st.nextToken().trim();
            if (token.compareTo("Tv290C") == 0)
            {
                tempIndex = i;
            }
            else if (token.compareTo("Cond0S/m") == 0)
            {
                condIndex = i;
            }
            else if (token.compareTo("Sal00") == 0)
            {
                salIndex = i;
            }
            else if (token.compareTo("DD MMM YYYY HH:MM:SS") == 0)
            {
                tsIndex = i;
            }
        }
    }

    @Override
    protected void parseData(String dataLine) throws ParseException, NoSuchElementException
    {
        SimpleDateFormat dateParser = new SimpleDateFormat("dd MMM yyyy HH:mm:ss");
        DecimalFormat deciFormat = new DecimalFormat("-######.0#");

        String temperatureString = null;
        String CNDCString = null;
        String salinityString = null;
        String pressureString = null;

        String dateString = null;

        Timestamp dataTimestamp = null;
        Double waterTemp = null;
        Double CNDC = null;
        Double pres = null;

        StringTokenizer st = new StringTokenizer(dataLine, "\t");
        int tokenCount = st.countTokens();
        try
        {
            for(int i=0;i<tokenCount;i++)
            {
                String token = st.nextToken().trim();
                if (i == tempIndex)
                {
                    temperatureString = token;
                    waterTemp = getDouble(temperatureString);
                }
                else if (i == condIndex)
                {
                    CNDCString = token;
                    CNDC = getDouble(CNDCString);
                }
                else if (i == presIndex)
                {
                    pressureString = token;
                    pres = getDouble(pressureString);
                }
                else if (i == salIndex)
                {
                    salinityString = token;
                }
                else if (i == tsIndex)
                {
                    dateString = token;
                    try
                    {
                        java.util.Date d = dateParser.parse(dateString);
                        dataTimestamp = new Timestamp(d.getTime());
                    }
                    catch(ParseException pex)
                    {
                        throw new ParseException("Timestamp parse failed for text '" + dateString + "'",0);
                    }
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
            row.setParameterCode("TEMP");
            row.setParameterValue(waterTemp);
            row.setSourceFileID(currentFile.getDataFilePrimaryKey());
            row.setQualityCode("RAW");

            boolean ok = row.insert();

            row.setParameterCode("CNDC");
            row.setParameterValue(CNDC);
            row.setSourceFileID(currentFile.getDataFilePrimaryKey());
            row.setQualityCode("RAW");

            ok = row.insert();
            
            if (presIndex != -1)
            {
                row.setParameterCode("PRES");
                row.setParameterValue(CNDC);
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
