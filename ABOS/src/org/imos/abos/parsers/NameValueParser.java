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
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import org.imos.abos.dbms.RawInstrumentData;

/**
 *
 * @author peter
 */
public class NameValueParser extends AbstractDataParser
{

    @Override
    protected boolean isHeader(String dataLine)
    {
        /*
         * data lines have the format
         *  2011-11-08 22:12:44 INFO: 7 done time 97 ,BV=15.3918 ,PT=16.88542 ,OBP=29.79 ,OT=16.2 ,CHL=46 ,NTU=173 PAR=0.7660786 ,meanAccel=-9.765544 ,meanLoad=46.95232
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
        SimpleDateFormat dateParser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        String dateString;
        Timestamp dataTimestamp = null;
        String sName;
        String sValue;
        double value;
        int paramsSent = 0;
        HashMap<String, String> hm = new HashMap(); 
        hm.put("OBP", "OPTODE_BPHASE");
        hm.put("OT", "OPTODE_TEMP");
        hm.put("OptodeBPhase", "OPTODE_BPHASE");
        hm.put("OptodeTemp", "OPTODE_TEMP");
        hm.put("bp", "CAPH");
        hm.put("sct", "TEMP");
        hm.put("scc", "CNDC");
        hm.put("swh", "SIG_WAVE_HEIGHT");
        hm.put("th", "AIRT");
        hm.put("rh", "RELH");
        hm.put("we", "UWND");
        hm.put("wn", "VWND");
        hm.put("wsavg", "WSPD");
        hm.put("wsdir", "WDIR");

        StringTokenizer st = new StringTokenizer(dataLine, ",");
        try
        {
            dateString = st.nextToken();
            try
            {
                java.util.Date d = dateParser.parse(dateString);
                dataTimestamp = new Timestamp(d.getTime());
            }
            catch(ParseException pex)
            {
                throw new ParseException("Timestamp parse failed for text '" + dateString + "'",0);
            }

            while(st.hasMoreElements())
            {
                String nvp = st.nextToken();
                StringTokenizer nv = new StringTokenizer(nvp, "=");
                sName = nv.nextToken();
                if (hm.get(sName) != null)
                {
                    if (nv.hasMoreTokens())
                    {
                        sValue = nv.nextToken();

                        value = new Double(sValue.trim());
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
                        row.setParameterCode(hm.get(sName));
                        row.setParameterValue(value);
                        row.setSourceFileID(currentFile.getDataFilePrimaryKey());
                        row.setQualityCode("RAW");
                        paramsSent++;

                        boolean ok = row.insert();
                    }
                }
            }
            // logger.debug("params" + paramsSent);
        }
        catch (NoSuchElementException nse)
        {
          throw nse;
        }
    }

}
