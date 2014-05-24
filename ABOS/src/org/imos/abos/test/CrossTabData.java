/*
 * Cross tabe the raw/processed data
 * Peter Jansen 2013
 * 
 */
package org.imos.abos.test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TimeZone;
import java.util.Vector;
import java.util.logging.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.wiley.core.Common;
import org.wiley.util.SQLWrapper;
import org.wiley.util.StringUtilities;

//        % MATLAB:: read SOFS generated csv file
//
//        fid = fopen('SOFS-2-RAW.csv');
//
//        tline=fgetl(fid); % Header  
//        split = strsplit(tline, ',');
//        iMLD = find(cellfun('isempty', strfind(split, 'MLD')) == 0);
//        iTemp = find(cellfun('isempty', strfind(split, 'TEMP')) == 0);
//
//        tline=fgetl(fid); % Units
//        units = strsplit(tline, ',');
//
//        i = 1;
//        while (! feof (fid))
//                tline=fgetl(fid); % data
//                sl = strsplit(tline, ','); 
//                ts(i) = datenum(sl(1), 'YYYY-mm-dd HH:MM:SS.0');
//                temp(i,:) = cellfun(@(x) sscanf(x, "%f"), sl(iTemp));
//                mld(i,:) = cellfun(@(x) sscanf(x, "%f"), sl(iMLD));
//                i = i + 1;
//        end
//
//        fclose(fid);




/**
 *
 * @author Peter Jansen
 */
public class CrossTabData
{

    private static Logger logger = Logger.getLogger(CrossTabData.class.getName());
    protected static SQLWrapper query = new SQLWrapper();
    protected static String mooring_id = "'SOFS-3-2012','SOFS-2-2011','SOFS-4-2013'";
    protected static String table = "raw_instrument_data";
    protected static String paramid = "parameter_code || '-' || trim(to_char(depth, 'MI009')) || 'm-' || model || '-' || serial_number";
    //protected static String paramid = "parameter_code || '-' || trim(to_char(depth, 'MI009')) || 'm-' || model";
    
    public void CrossTabData()
    {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    public void setMooring(String m)
    {
        mooring_id = m;
    }
    
    public void setTable(String t)
    {
        table = t;
    }
    
    boolean limit = false;
    public void setLimitDeploymentData(boolean l)
    {
        limit = l;
    }
    
    public int getData()
    {
        Connection conn = null;
        Statement proc = null;
        ResultSet results = null;
        int count = 0;

        conn = Common.getConnection();
        try
        {
            conn.setAutoCommit(false);
            proc = conn.createStatement();

//        logger.debug("Searching for parameters " + mooring_id + " table " + table);
        
            String SQL = "SELECT DISTINCT("+paramid+"), units,  netcdf_std_name, netcdf_long_name, depth "
                    + " FROM "+table+" JOIN instrument USING (instrument_id) "
                    + " JOIN parameters ON (code = parameter_code)"
                    + " WHERE mooring_id IN (" + mooring_id + ")"
                    + " OR (mooring_id LIKE 'Pulse%' AND parameter_code IN ('DOX2', 'TOTAL_GAS_PRESSURE'))"
                    + " ORDER BY "+paramid+"";
//        logger.debug(SQL);

            proc.execute(SQL);
            results = (ResultSet) proc.getResultSet();

            // print a header
            String s, u, sn, ln, de;
            String hdr, units, depth, name, short_name;
            int params = 0;
            HashMap map = new HashMap();
            hdr = "timestamp";
            units = "";
            name = "";
            depth = "";
            short_name = "";
            while (results.next())
            {
                s = results.getString(1);
                u = results.getString(2);
                sn = results.getString(3);
                ln = results.getString(4);
                de = results.getString(5);
                map.put(s, new Integer(params++));
                hdr += " ," + s;
                if (u != null)
                {
                    units += " ," + u.trim();
                }
                else
                {
                    units += " ,";
                }
                if (sn != null)
                {
                    name += " ," + sn.trim();
                }
                else if(ln != null)
                {
                    name += " ," + ln.trim();
                }
                else
                {
                    name += " ,";
                }
                depth += " ," + de.trim();
            }
            System.out.println(hdr);
            //System.out.println(name);
            //System.out.println(depth);
            System.out.println(units);

//        logger.debug("Searching for data");
            SQL = "SELECT date_trunc('hour', data_timestamp) AT TIME ZONE 'UTC',"
                    + " ("+paramid+"),"
                    + " avg(parameter_value)"
                    + " FROM "+table+" JOIN instrument USING (instrument_id) JOIN mooring USING (mooring_id)"
                    + " WHERE quality_code != 'BAD'";
            if (limit)
            {
                SQL += " AND data_timestamp BETWEEN timestamp_in AND timestamp_out";
            }
            SQL +=  " AND mooring_id IN (" + mooring_id + ")" 
                    + " OR (mooring_id LIKE 'Pulse%' AND parameter_code IN('DOX2', 'TOTAL_GAS_PRESSURE'))"                    
                    + " GROUP BY date_trunc('hour', data_timestamp), " + paramid
                    + " ORDER BY date_trunc('hour', data_timestamp), " + paramid;
//        logger.debug(SQL);

            proc.execute(SQL);
            results = (ResultSet) proc.getResultSet();

            conn.setAutoCommit(false);
            results.setFetchSize(50);
            Timestamp currentTs = new Timestamp(0);
            CrossTabData.ParamDatum[] foo = new CrossTabData.ParamDatum[params];

            while (results.next())
            {
                Timestamp t = results.getTimestamp(1);
                String p = results.getString(2);
                int col = ((Integer) map.get(p)).intValue();
                Double d = results.getDouble(3);
                CrossTabData.ParamDatum dd = new CrossTabData.ParamDatum(t, p, d);
                foo[col] = dd;
                // System.out.println("data " + col + " " + dd);
                if (count == 0)
                {
                    currentTs = dd.ts;
                }
                if ((!dd.ts.equals(currentTs)))
                {
                    currentTs = dd.ts;
                    System.out.print(currentTs);
                    for (int i = 0; i < params; i++)
                    {
                        if (foo[i] != null)
                        {
                            System.out.print("," + foo[i].val);
                        }
                        else
                        {
                            System.out.print(",NaN");
                        }
                        foo[i] = null;
                    }
                    System.out.println();
                }
                count++;
            }
        }
        catch (SQLException ex)
        {
            java.util.logging.Logger.getLogger(CrossTabData.class.getName()).log(Level.SEVERE, null, ex);
        }

        return count;
    }

    public static void main(String args[])
    {
        String $HOME = System.getProperty("user.home");

        PropertyConfigurator.configure("log4j.properties");
        Common.build($HOME + "/ABOS/ABOS.properties");

        CrossTabData ctd = new CrossTabData();
        
        ctd.setLimitDeploymentData(true);
        
        if (args.length > 0)
        {
            ctd.setMooring(args[0]);
        }

        ctd.getData();
    }

    protected class ParamDatum
    {
        public Timestamp ts;
        public String paramCode;
        public Double val;

        public ParamDatum()
        {
            super();
        }

        public ParamDatum(Timestamp t, String p, Double d)
        {
            super();
            ts = t;
            paramCode = p.trim();
            val = d;
        }

        public String toString()
        {
            return ts + " " + paramCode + "=" + val;
        }
    }

}
