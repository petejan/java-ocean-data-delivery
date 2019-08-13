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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;
import org.imos.abos.dbms.RawInstrumentData;

/**
 *
 * @author peter
 */
public class SeabirdCnvParser extends AbstractDataParser
{
    protected static Logger logger = Logger.getLogger(SeabirdCnvParser.class.getName());
    protected ArrayList names;
            
    protected class Column
    {
        Column(String s)
        {
            line = s;
        }
        String line;
        int colNo;
        String obs;
        String text;
        String units;
        String obsCode;
    }
    
    protected int timeCol = -1;
    protected HashMap col2obs = new HashMap();
    protected Pattern p;
    protected Matcher m;
    protected long tOffset = 0;
    
    public SeabirdCnvParser()
    {
        super();
        col2obs.put("tv290C", "TEMP");
        
        col2obs.put("cond0S/m", "CNDC");
        col2obs.put("c0S/m", "CNDC");
        
        col2obs.put("prdM", "PRES");
        col2obs.put("sal00", "PSAL");
        
        col2obs.put("oxsolMm/Kg", "OXSOL");
        col2obs.put("sbeox0Mm/Kg", "DOX2");
        col2obs.put("sbeopoxMm/Kg", "DOX2");
        
        col2obs.put("v0", "VOLT1");
        col2obs.put("v1", "VOLT2");
        col2obs.put("v2", "VOLT3");
        col2obs.put("v3", "VOLT4");
        col2obs.put("v4", "VOLT5");
        col2obs.put("v5", "VOLT6");
        
        //p = Pattern.compile("# name (\\d*) = ([^:]*): ([^\\[]*)\\[([^\\]]*).*");
        p = Pattern.compile("# name (\\d*) = ([^:]*):(.*)");

        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        try
        {
            tOffset = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").parse("01/01/2000 00:00:00").getTime();
        }
        catch (ParseException ex)
        {
            java.util.logging.Logger.getLogger(SeabirdCnvParser.class.getName()).log(Level.SEVERE, null, ex);
        }
        names = new ArrayList();
    }
    
    @Override
    protected boolean isHeader(String dataLine)
    {
//        # name 0 = timeJV2: Time, Instrument [julian days]
//        # name 1 = timeK: Time, Instrument [seconds]
//        # name 2 = tv290C: Temperature [ITS-90, deg C]
//        # name 3 = cond0S/m: Conductivity [S/m]
//        # name 4 = prdM: Pressure, Strain Gauge [db]
//        # name 5 = depSM: Depth [salt water, m], lat = -46.00
//        # name 6 = sal00: Salinity, Practical [PSU]
//        # name 7 = oxsolMm/Kg: Oxygen Saturation, Garcia & Gordon [umol/kg]
//        # name 8 = oxsolML/L: Oxygen Saturation, Garcia & Gordon [ml/l]
//        # name 9 = sbeopoxML/L: Oxygen, SBE 63 [ml/l]
//        # name 10 = sbeopoxMm/Kg: Oxygen, SBE 63 [umol/kg]
//        # name 11 = flag:  0.000e+00

//        # file_type = ascii
//        *END*
//          80.000417  480211236    17.8782   0.000023      0.081      0.080     0.0091  296.98332    6.64074      6.446     288.29  0.000e+00

        char c = dataLine.charAt(0);
        if((c == '*') || (c == '#'))
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
        m = p.matcher(dataLine);
        if (m.find())
        {
            for(int i=0;i<=m.groupCount();i++)
            {
                logger.debug("Group " + i + " :" + m.group(i));
            }
            Column c = new Column(dataLine);
            
            c.colNo = Integer.parseInt(m.group(1));
            c.obs = m.group(2);
            c.text = m.group(3);
            //c.units = m.group(4);
            
            if (c.obs.startsWith("timeK"))
            {
                timeCol = c.colNo;
            }
            c.obsCode = (String)col2obs.get(c.obs);
            
            logger.debug("OBS_CODE " + c.obsCode);
            
            names.add(c);            
        }
    }

    @Override
    protected void parseData(String dataLine) throws ParseException, NoSuchElementException
    {
        Timestamp dataTimestamp = null;
        Double v = Double.NaN;
        
        String[] st = dataLine.split(" +");
        
        long t = Long.parseLong(st[timeCol]) * (long)1000;
        t += tOffset;
        
        dataTimestamp = new Timestamp(t);
        
        for (Object c : names)
        {
            Column cn = (Column)c;
            if (cn.obsCode != null)
            {
                v = new Double(st[cn.colNo]);

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
                row.setParameterCode(cn.obsCode);
                row.setParameterValue(v);
                row.setSourceFileID(currentFile.getDataFilePrimaryKey());
                row.setQualityCode("RAW");

                boolean ok = row.insert();
            }

        }
    }
}
