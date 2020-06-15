/*
 * IMOS data delivery project
 * Written by Peter Wiley
 * This code is copyright (c) Peter Wiley 2000 - ?
 * It is made available under the BSD Software Licence in the hope that it may be useful.
 * It has NO WARRANTY OF FITNESS OR SUITABILITY FOR ANY PURPOSE.
 * Feel free to fix any bugs that you may find.
 */

package org.imos.dwm.parsers;

import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.imos.dwm.dbms.RawInstrumentData;

/**
 *
 * @author peter
 */
public class CR1000TOA5Parser extends AbstractDataParser
{
    String[] hdr = null;
    List<String> list = new ArrayList<String>();
    
    @Override
    protected boolean isHeader(String dataLine)
    {
        return !dataLine.matches("\"\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}\".+");
    }

    @Override
    protected void parseHeader(String dataLine) throws ParseException, NoSuchElementException
    {
        if (dataLine.startsWith("\"TIMESTAMP\""))
        {
            Matcher m = Pattern.compile("([^\"],*|\".+?\"),*").matcher(dataLine);
            while (m.find())
                list.add(m.group(1).replace("\"", "")); 
                        
            hdr = list.toArray(new String[]{});
            hm.put("OptodeBPhase", "OPTODE_BPHASE");
            hm.put("OptodeTemp", "OPTODE_TEMP");
            hm.put("flntusCHL", "ECO_FLNTUS_CHL");
            hm.put("flntusNTU", "ECO_FLNTUS_TURB");
            hm.put("PAR", "PAR_VOLT");
        }
    }

    // "TOA5","CR1000_38400","CR1000","44238","CR1000.Std.24","CPU:SOFS-3.CR1","4011","Log"
    // "TIMESTAMP","RECORD","BattV_Min","PTemp_C","PAR","OptodeBPhase","OptodeTemp","flntusCHL","flntusNTU","meanZAccel","stdZAccel","meanLoad"
    // "TS","RN","Volts","Deg C","","","","","","","",""
    // "","","Min","Smp","Smp","Smp","Smp","Smp","Smp","Smp","Smp","Smp"
    // "2012-03-20 04:00:00",0,12.12,23.12,"NAN",-1,-98,-1,-1,-9.82,0.03,-134.8
    // "2012-03-20 05:00:00",1,12.11,22.03,"NAN",-1,-98,-1,-1,-9.83,0.03,-119.4
    // "2012-03-20 06:00:00",2,12.12,22.25,"NAN",-1,-98,-1,-1,-9.83,0.027,-111.6
    // "2012-03-20 07:00:00",3,12.11,22.95,"NAN",-1,-98,-1,-1,-9.82,0.027,-119.2
    // "2012-03-20 08:00:00",4,12.11,23.18,"NAN",-1,-98,-1,-1,-9.82,0.028,-121.8
    // "2012-03-20 09:00:00",5,12.1,23.24,"NAN",-1,-98,-1,-1,-9.82,0.028,-123.5
    // "2012-03-20 10:00:00",6,12.11,23.27,"NAN",-1,-98,-1,-1,-9.82,0.028,-118.1
    // "2012-03-20 11:00:00",7,12.11,23.21,"NAN",-1,-98,-1,-1,-9.82,0.027,-138.3
    
    HashMap<String, String> hm = new HashMap(); 
    @Override
    protected void parseData(String dataLine) throws ParseException, NoSuchElementException
    {
        SimpleDateFormat dateParser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateParser.setTimeZone(tz);
        
        DecimalFormat deciFormat = new DecimalFormat("-######.0#");

        Timestamp dataTimestamp = null;
        
        Double val = null;

        String[] splt = dataLine.split(",");
        
        dataTimestamp = new Timestamp(dateParser.parse(splt[0].replace("\"", "")).getTime());
        
        for (int i=0;i<hdr.length;i++)
        {
            if (hm.get(hdr[i]) != null)
            {
                val = getDouble(splt[i]);

                insertData(dataTimestamp, hm.get(hdr[i]), val);                
            }
        }
    }

    protected boolean insertData(Timestamp dataTimestamp, String param, Double val)
    {
        RawInstrumentData row = new RawInstrumentData();

        row.setDataTimestamp(dataTimestamp);
        row.setDepth(instrumentDepth);
        row.setInstrumentID(currentInstrument.getInstrumentID());
        row.setLatitude(currentMooring.getLatitudeIn());
        row.setLongitude(currentMooring.getLongitudeIn());
        row.setMooringID(currentMooring.getMooringID());
        row.setSourceFileID(currentFile.getDataFilePrimaryKey());
        row.setQualityCode("RAW");

        row.setParameterCode(param);
        row.setParameterValue(val);

        boolean ok = row.insert();

        return ok;
    }

}
