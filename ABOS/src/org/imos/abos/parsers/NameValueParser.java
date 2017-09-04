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

import org.imos.abos.dbms.ProcessedInstrumentData;
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
        dateParser.setTimeZone(tz);

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
        hm.put("waveheight", "SIG_WAVE_HEIGHT");
        hm.put("AirT", "AIRT");
        hm.put("temp", "TEMP");
        hm.put("RH", "RELH");
        hm.put("we", "UWND");
        hm.put("wn", "VWND");
        hm.put("wsavg", "WSPD");
        hm.put("wsdir", "WDIR");
        hm.put("lat", "YPOS");
        hm.put("lon", "XPOS");
        hm.put("PAR", "PAR_VOLT");

        hm.put("MaxWH", "MAX_WAVE_HEIGHT");
        hm.put("MaxWP", "MAX_WAVE_PERIOD");
        hm.put("MaxWD", "MAX_WAVE_DIR");
        hm.put("MeanWP", "AVG_PERIOD");
        hm.put("MeanWD", "AVG_WAVE_DIR");
        hm.put("MeanWH", "AVG_WAVE_HEIGHT");
        hm.put("SWD", "SIG_WAVE_DIR");
        hm.put("SWH", "SIG_WAVE_HEIGHT");
        hm.put("SWP", "SIG_WAVE_PERIOD");
        hm.put("t10WP", "T10_WAVE_PERIOD");
        hm.put("t10WH", "T10_WAVE_HEIGHT");
        hm.put("t10WD", "T10_WAVE_DIR");

        hm.put("CHL", "ECO_FLNTUS_CHL");
        hm.put("NTU", "ECO_FLNTUS_TURB");
        
        hm.put("CHL_UGL", "CPHL");
        hm.put("BB", "BB");

        hm.put("MLD", "MLD");

        hm.put("PHOS", "PHOS");
        hm.put("SLCA", "SLCA");
        hm.put("NTRI", "NTRI");
        hm.put("TALK", "TALK");
        hm.put("TCO2", "TCO2");
        hm.put("WATER_SAMPLE", "WATER_SAMPLE");
        hm.put("WEIGHT", "WEIGHT");
        
        String[] st = dataLine.split(",");
        try
        {
            dateString = st[0];
            try
            {
                java.util.Date d = dateParser.parse(dateString);
                dataTimestamp = new Timestamp(d.getTime());
            }
            catch(ParseException pex)
            {
                throw new ParseException("Timestamp parse failed for text '" + dateString + "'",0);
            }

            String sQ;
            for (int i=1;i<st.length;i++)
            {
            	sQ = "RAW";
                String nvp = st[i];
                String[] nv = nvp.split("[=() ]");
                if (nv.length > 1)
                {
                	if (hm.get(nv[0]) != null)
                	{
                        sValue = nv[1];

                        try
                        {
                            value = new Double(sValue.trim());
                            
                            //
                            // ok, we have parsed out the values we need, can now construct the raw data class
                            //
                            
                            if (nv.length > 2)
                            {
                            	// TODO: should the QC data go into RawData or ProcessedData?
                            	
                            	RawInstrumentData row = new RawInstrumentData(); 
	
	                            row.setDataTimestamp(dataTimestamp);
	                            row.setDepth(instrumentDepth);
	                            row.setInstrumentID(currentInstrument.getInstrumentID());
	                            row.setLatitude(currentMooring.getLatitudeIn());
	                            row.setLongitude(currentMooring.getLongitudeIn());
	                            row.setMooringID(currentMooring.getMooringID());
	                            row.setParameterCode(hm.get(nv[0]));
	                            row.setParameterValue(value);
	                            row.setSourceFileID(currentFile.getDataFilePrimaryKey());
                            	int quality = Integer.parseInt(nv[2]);
                            	switch (quality)
                            	{
                            		case 1: sQ = "GOOD"; break;
                            		case 2: sQ = "PGOOD"; break;
                            		case 3: sQ = "PBAD"; break;
                            		case 4: sQ = "BAD"; break;
                            		default : sQ = "RAW";
                            	}
                            	row.setQualityCode(sQ);
                                paramsSent++;

                                boolean ok = row.insert();
                            }
                            else
                            {
	                            RawInstrumentData row = new RawInstrumentData();
	                        	
	                            row.setDataTimestamp(dataTimestamp);
	                            row.setDepth(instrumentDepth);
	                            row.setInstrumentID(currentInstrument.getInstrumentID());
	                            row.setLatitude(currentMooring.getLatitudeIn());
	                            row.setLongitude(currentMooring.getLongitudeIn());
	                            row.setMooringID(currentMooring.getMooringID());
	                            row.setParameterCode(hm.get(nv[0]));
	                            row.setParameterValue(value);
	                            row.setSourceFileID(currentFile.getDataFilePrimaryKey());
                            	row.setQualityCode(sQ);

                            	paramsSent++;

                                boolean ok = row.insert();                            	
                            }
                            logger.trace("name " + nv[0] + " = " + value + " quality " + sQ);

                        }
                        catch (NumberFormatException ex)
                        {
                            // just ignore, don't insert into database
                        }
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
