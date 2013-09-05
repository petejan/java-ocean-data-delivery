/*
 * Cross tabe the raw/processed data
 * Peter Jansen 2013
 * 
 */
package org.imos.abos.test;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TimeZone;
import java.util.Vector;
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
    protected static String mooring_id = "Pulse-8-2011";
    
    public void CrossTabData()
    {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }
    
    public int getData()
    {
//        logger.debug("Searching for parameters");
        String SQL = "SELECT DISTINCT(parameter_code || '-' || trim(to_char(depth, 'MI009')) || 'm-' || model) "
                      + " FROM processed_instrument_data JOIN instrument USING (instrument_id) "
                      + " WHERE mooring_id = '" + mooring_id + "'"
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
        
        ArrayList<CrossTabData.ParamDatum[]> set = new ArrayList();
//        logger.debug("Searching for data");
        SQL = "SELECT data_timestamp AT TIME ZONE 'UTC'," 
                + " (parameter_code || '-' || trim(to_char(depth, 'MI009')) || 'm-' || model),"
                + " parameter_value"
                + " FROM processed_instrument_data JOIN instrument USING (instrument_id)"
                + " WHERE quality_code != 'BAD'"
                + " AND mooring_id = '" + mooring_id + "'"
                + " ORDER BY data_timestamp, model, parameter_code, depth"
                ;
//        logger.debug(SQL);
        CrossTabData.query.setConnection(Common.getConnection());
        CrossTabData.query.executeQuery(SQL);
        Timestamp currentTs = new Timestamp(0);
        dataSet = CrossTabData.query.getData();
        
        CrossTabData.ParamDatum foo[];
                
        if (dataSet != null && dataSet.size() > 0)
        {
            foo = new CrossTabData.ParamDatum[params];
            
            for (int i = 0; i < dataSet.size(); i++)
            {
                Vector row = (Vector) dataSet.get(i);
                Timestamp t = (Timestamp) row.get(0);
                String p = (String) row.get(1);
                int col = ((Integer)map.get(p)).intValue();
                Double d = ((Number) row.get(2)).doubleValue();
                CrossTabData.ParamDatum dd = new CrossTabData.ParamDatum(t, p, d);
                // System.out.println("data " + col + " " + dd);
                if (i == 0)
                {
                    currentTs = dd.ts;
                }
                else
                {
                    if ((!dd.ts.equals(currentTs)))
                    {
                        currentTs = dd.ts;
                        set.add(foo);
                        
                        foo = new CrossTabData.ParamDatum[params];
                    }
                }
                foo[col] = dd;
            }
            set.add(foo);
//            logger.debug("Found " + dataSet.size() + " records");
//            logger.debug("Created " + set.size() + " sets of parameter/instrument");

            int x;
            for(int i=0;i<set.size();i++)
            {
                foo = set.get(i);
                x=0;
                while(foo[x] == null)
                {
                    x++;
                }
                System.out.print(foo[x].ts + "," );
                for(int j=0;j<params;j++)
                {
                    if (foo[j] != null)
                    {
                        System.out.print(foo[j].val);
                    }
                    if (j != (params-1))
                        System.out.print(",");
                }
                System.out.println();
            }
        }
        
        return set.size();        
    }
    
    public static void main(String args[])
    {
        String $HOME = System.getProperty("user.home");

        if(args.length == 0)
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
