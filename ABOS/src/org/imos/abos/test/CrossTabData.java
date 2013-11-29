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

/**
 *
 * @author Peter Jansen
 */
public class CrossTabData
{

    private static Logger logger = Logger.getLogger(CrossTabData.class.getName());
    protected static SQLWrapper query = new SQLWrapper();
    protected static String mooring_id = "SOFS-4-2013";
    protected static String table = "raw_instrument_data";
    protected static String paramid = "parameter_code || '-' || trim(to_char(depth, 'MI009')) || 'm-' || model || '-' || serial_number";

    public void CrossTabData()
    {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    public int getData()
    {
        Connection conn = null;
        Statement proc = null;
        ResultSet results = null;
        int count = -1;

        conn = Common.getConnection();
        try
        {
            conn.setAutoCommit(false);
            proc = conn.createStatement();

//        logger.debug("Searching for parameters " + mooring_id + " table " + table);
        
            String SQL = "SELECT DISTINCT("+paramid+") "
                    + " FROM "+table+" JOIN instrument USING (instrument_id) "
                    + " WHERE mooring_id = '" + mooring_id + "'" + " AND depth < 0"
                    + " ORDER BY "+paramid+"";
//        logger.debug(SQL);

            proc.execute(SQL);
            results = (ResultSet) proc.getResultSet();

            String s;
            int params = 0;
            HashMap map = new HashMap();
            System.out.print("timestamp");
            while (results.next())
            {
                s = results.getString(1);
                map.put(s, new Integer(params++));
                System.out.print(" ," + s);
            }
            System.out.println();

//        logger.debug("Searching for data");
            SQL = "SELECT date_trunc('hour', data_timestamp) AT TIME ZONE 'UTC',"
                    + " ("+paramid+"),"
                    + " avg(parameter_value)"
                    + " FROM "+table+" JOIN instrument USING (instrument_id)"
                    + " WHERE quality_code != 'BAD'"
                    + " AND mooring_id = '" + mooring_id + "'" + " AND depth < 0"
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
                            System.out.print(" ," + foo[i].val);
                        }
                        else
                        {
                            System.out.print(" ,");
                        }
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

        if (args.length == 0)
        {
            PropertyConfigurator.configure("log4j.properties");
            Common.build($HOME + "/ABOS/ABOS.properties");
        }

        CrossTabData ctd = new CrossTabData();

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
