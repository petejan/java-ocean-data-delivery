/*
 * IMOS data delivery project
 * Written by Peter Wiley
 * This code is copyright (c) Peter Wiley 2000 - ?
 * It is made available under the BSD Software Licence in the hope that it may be useful.
 * It has NO WARRANTY OF FITNESS OR SUITABILITY FOR ANY PURPOSE.
 * Feel free to fix any bugs that you may find.
 */

package org.imos.abos.parsers;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.imos.abos.dbms.RawInstrumentData;

/**
 *
 * @author peter
 */
public class AsimetDataParser extends AbstractDataParser
{
    protected static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AsimetDataParser.class.getName());       

    @Override
    protected boolean isHeader(String dataLine)
    {
        return false;
    }

    @Override
    protected void parseHeader(String dataLine) throws ParseException, NoSuchElementException
    {
        // don't need these
    }
    
    Timestamp t = new Timestamp(0);
    
    public class AsimetBinaryDecode extends AsimetDecode
    {
        int rowCount = 0;
        
        public void output(Date d, String hdr, String fmt, double v)
        {
            // System.out.printf(d + " " + hdr + "=" + fmt + "\n", v);
            t.setTime(d.getTime());
            String obs = hm.get(hdr);
            if (obs != null)
            {
                insertData(t, obs, v);
            }
            else
            {
                if (rowCount < 100)
                    System.out.println("AsimetDinaryDecode::output() unknown obs " + hdr);
            }
        }
        public void outputNext()
        {
            rowCount++;
            if (rowCount % 1000 == 0)
            {
                logger.debug("Processed " + rowCount + " rows.");
                if(parentForm != null)
                    parentForm.updateMessageArea("Processed " + rowCount + " rows.\n");
            }
            
        }
        
        public void outputNewTs(String t)
        {
            
        }
    }

    HashMap<String, String> hm = new HashMap<String, String>();
    
    protected void processFile(File dataFile)
    {
        hm.put("we", "UWND");
        hm.put("wn", "VWND");
        hm.put("wsavg", "WSPD");
        hm.put("wmax", "WSPD_MAX");
        hm.put("wmin", "WSPD_MIN");
        hm.put("wdavg", "WDIR");
        hm.put("compass", "COMPASS");
        hm.put("bp", "CAPH");
        hm.put("rh", "RELH");
        hm.put("AirT", "AIRT");
        
        hm.put("sr", "SW");
        hm.put("dome", "TDOME");
        hm.put("body", "TBODY");
        hm.put("tpile", "VPILE");
        hm.put("lwflux", "LW");
        hm.put("prlev", "RAIT");
        hm.put("sct", "TEMP");
        hm.put("scc", "CNDC");
        
        hm.put("bpr", "CAPH");

        hm.put("prc", "RAIT");

        hm.put("WSpeed", "WSPD");
        hm.put("WSMax", "WSPD_MAX");
        hm.put("LastVane", "WDIR");
        hm.put("LastCompass", "COMPASS");
        hm.put("TiltX", "ROLL");
        hm.put("TiltY", "PITCH");

        hm.put("wsMax", "WSPD_MAX");
        hm.put("LastVane", "WDIR");
        hm.put("ws", "WSPD");

        //hm.put("GillSOS", "SOS");
        //hm.put("GillTemp", "TEMP");

        hm.put("swr", "SW");

        hm.put("temp_dome", "TDOME");
        hm.put("temp_body", "TBODY");
        hm.put("volts_pile", "VPILE");
        hm.put("lw_flux", "LW");
        
        row.setDepth(instrumentDepth);
        row.setInstrumentID(currentInstrument.getInstrumentID());
        row.setLatitude(currentMooring.getLatitudeIn());
        row.setLongitude(currentMooring.getLongitudeIn());
        row.setMooringID(currentMooring.getMooringID());
        row.setSourceFileID(currentFile.getDataFilePrimaryKey());
        row.setQualityCode("RAW");
        
        AsimetBinaryDecode ad = new AsimetBinaryDecode();
        
        String filename = dataFile.getAbsolutePath();
        try
        {
            String fName = dataFile.getName();
            String type = "UNKNOWN";
            
            if (fName.contains("LSR"))
            {
                type = "LSR";
            }
            else if (fName.contains("LGR"))
            {
                type = "LSR";
            }
            else if (fName.matches("L\\d\\d.*"))
            {
                type = "LSR";
            }
            else if (fName.matches("SOFS.L\\d\\d.*"))
            {
                type = "LSR";
            }
            else if (fName.contains("BPR"))
            {
                type = "BPR";
            }
            else if (fName.contains("HRH"))
            {
                type = "HRH";
            }
            else if (fName.contains("PRC"))
            {
                type = "PRC";
            }
            else if (fName.contains("LWR"))
            {
                type = "LWR";
            }
            else if (fName.contains("SWR"))
            {
                type = "SWR";
            }
            else if (fName.contains("SWND"))
            {
                type = "SWND"; // SOFS-1 uses the old WND format for the Sonic Wind Sensors
            }
            else if (fName.contains("WND"))
            {
                type = "SWND"; 
            }
            logger.info("File Type : " + type);
            
            ad.read(type, filename);
        }
        catch (IOException ex)
        {
            Logger.getLogger(AsimetDataParser.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    protected void parseData(String dataLine) throws ParseException, NoSuchElementException
    {
    }

    RawInstrumentData row = new RawInstrumentData();

    protected boolean insertData(Timestamp dataTimestamp, String obs, Double parVal)
    {
        row.setDataTimestamp(dataTimestamp);

        row.setParameterCode(obs);
        row.setParameterValue(parVal);

        //System.out.println("AsimetDataParser::insertData() " + dataTimestamp + " " + obs + " " + parVal);
        
        boolean ok = row.insert();

        return ok;
    }

}
