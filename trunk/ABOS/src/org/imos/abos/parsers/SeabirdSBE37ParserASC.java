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
    int ox2Index = -1;
    
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
            else if (token.compareTo("PrdM") == 0)
            {
                presIndex = i;
            }
            else if (token.compareTo("SbeopoxMm/Kg") == 0)
            {
                ox2Index = i;
            }
            else if (token.compareTo("DD MMM YYYY HH:MM:SS") == 0)
            {
                tsIndex = i;
            }
        }
        logger.info("tsIndex " + tsIndex + " presIndex " + presIndex);
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
        String ox2String = null;

        String dateString = null;

        Timestamp dataTimestamp = null;
        Double waterTemp = null;
        Double CNDC = null;
        Double pres = null;
        Double psal = null;
        Double ox2 = null;

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
                    psal = getDouble(salinityString);
                }
                else if (i == ox2Index)
                {
                    ox2String = token;
                    ox2 = getDouble(ox2String);
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
            row.setSourceFileID(currentFile.getDataFilePrimaryKey());
            row.setQualityCode("RAW");
            
            row.setParameterCode("TEMP");
            row.setParameterValue(waterTemp);

            boolean ok = row.insert();

            row.setParameterCode("CNDC");
            row.setParameterValue(CNDC);

            ok = row.insert();
            
            if (presIndex != -1)
            {
                row.setParameterCode("PRES");
                row.setParameterValue(pres);

                ok = row.insert();
            }
            if (salIndex != -1)
            {
                row.setParameterCode("PSAL");
                row.setParameterValue(psal);

                ok = row.insert();
            }
            if (ox2Index != -1)
            {
                row.setParameterCode("DOX2");
                row.setParameterValue(ox2);

                ok = row.insert();
            }
        }
        catch (NoSuchElementException nse)
        {
          throw nse;
        }
    }

}
