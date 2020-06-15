/*
 * Cross tabe the raw/processed data
 * Peter Jansen 2013
 * 
 */
package org.imos.dwm.test;

import java.sql.Connection;
import java.sql.ResultSet;
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
public class CrossTabDataAWCP
{
    private static Logger logger = Logger.getLogger(CrossTabData.class.getName());    
    protected static SQLWrapper query = new SQLWrapper();
    protected static String mooring_id = "'SOFS-3-2012', 'SOFS-4-2013'";
    protected static String instrument_id = "1574, 2238"; // AWCP 55046 and 55052
    
    
    public void CrossTabData()
    {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }
    
    public int getData()
    {
        Connection conn = null;
        Statement proc = null;
        ResultSet results = null;
        int count = 0;

        conn = Common.getConnection();
        
//        logger.debug("Searching for parameters");
        String SQL = "SELECT DISTINCT(parameter_code || '-' || trim(to_char(depth, 'MI009')) || 'm-' || model) "
                      + " FROM raw_instrument_data JOIN instrument USING (instrument_id) "
                      + " WHERE mooring_id IN (" + mooring_id + ")"
                      + " AND instrument_id IN ("+instrument_id+")"
                      + " AND parameter_value != 'NaN'"                
                      + " ORDER BY parameter_code || '-' || trim(to_char(depth, 'MI009')) || 'm-' || model";
//        logger.debug(SQL);
        CrossTabData.query.setConnection(Common.getConnection());
        CrossTabData.query.executeQuery(SQL);
        Vector dataSet = CrossTabData.query.getData();
        
        String s;
        int params = dataSet.size();
        HashMap map = new HashMap();
        System.out.print("ts");
        for (int i = 0; i < params; i++)
        {
            s = (String)((Vector)dataSet.get(i)).get(0);
            map.put(s, new Integer(i));
            System.out.print("," + s);
        }
        System.out.println();
        
//        logger.debug("Searching for data");
        SQL = "SELECT data_timestamp AT TIME ZONE 'UTC'," 
                + " (parameter_code || '-' || trim(to_char(depth, 'MI009')) || 'm-' || model),"
                + " parameter_value"
                + " FROM raw_instrument_data JOIN instrument USING (instrument_id)"
                + " WHERE quality_code != 'BAD'"
                + " AND mooring_id IN (" + mooring_id + ")"
                + " AND instrument_id IN ("+instrument_id+")"
                + " AND parameter_value != 'NaN'"
                + " ORDER BY data_timestamp, model, parameter_code, depth"
                ;
//        logger.debug(SQL);
        
        try
        {
            conn.setAutoCommit(false);
            proc = conn.createStatement();
            proc.setFetchSize(50);

            proc.execute(SQL);
            results = (ResultSet) proc.getResultSet();

            results.setFetchSize(50);
            Timestamp currentTs = new Timestamp(0);
            CrossTabDataAWCP.ParamDatum[] foo = new CrossTabDataAWCP.ParamDatum[params];

            while (results.next())
            {
                Timestamp t = results.getTimestamp(1);
                String p = results.getString(2);
                int col = ((Integer) map.get(p)).intValue();
                Double d = results.getDouble(3);
                CrossTabDataAWCP.ParamDatum dd = new CrossTabDataAWCP.ParamDatum(t, p, d);
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

        if(args.length == 0)
        {
            PropertyConfigurator.configure("log4j.properties");
            Common.build("ABOS.properties");
        }

        CrossTabDataAWCP ctd = new CrossTabDataAWCP();
        
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
