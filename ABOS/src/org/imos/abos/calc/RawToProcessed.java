/*
 * IMOS Data Delivery Project
 * Written by Peter Jansen
 * This code is copyright (c) Peter Wiley 2000 - ?
 * It is made available under the BSD Software Licence in the hope that it may be useful.
 * It has NO WARRANTY OF FITNESS OR SUITABILITY FOR ANY PURPOSE.
 * Feel free to fix any bugs that you may find.
 */

package org.imos.abos.calc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.TimeZone;
import java.util.logging.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.imos.abos.forms.DataProcessor;
import org.wiley.core.Common;

/**
 *
 * @author Peter Jansen <peter.jansen@utas.edu.au>
 */
public class RawToProcessed
{
    private static Logger logger = Logger.getLogger(RawToProcessed.class.getName());

    public RawToProcessed()
    {
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
    }
    
    public void ProcessMooingId(String mooringId)
    {
        String sel = new String("SELECT class_name, parameters, processors_pk FROM instrument_data_processors WHERE mooring_id = '" + mooringId + "' ORDER BY processing_date");

        Connection conn = Common.getConnection();

        Statement stmt;
        try
        {
           stmt = conn.createStatement();
           ResultSet rs = stmt.executeQuery(sel);
           
           String className;
           String params;
           int pk;
           
           ResultSetMetaData rsmd = rs.getMetaData();
           
           while (rs.next())
           {
                className = rs.getString(1);
                params = rs.getString(2);
                pk = rs.getInt(3);

                logger.info("Class = " + className + " pk " + pk + " params " + params);

                final Object component;

                component = Class.forName(className).newInstance();
                if (component instanceof DataProcessor)
                {

                    final DataProcessor parser = (DataProcessor) component;

                    parser.setupFromString(params);
                    parser.calculateDataValues();
                }
           }

        }
        catch (InstantiationException ex)
        {
            java.util.logging.Logger.getLogger(RawToProcessed.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (IllegalAccessException ex)
        {
            java.util.logging.Logger.getLogger(RawToProcessed.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (ClassNotFoundException ex)
        {
            java.util.logging.Logger.getLogger(RawToProcessed.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (SQLException ex)
        {
            logger.error(ex);
        }
    
    }
    
    public static void main (String args[])
    {
        String $HOME = System.getProperty("user.home");

        PropertyConfigurator.configure("log4j.properties");
        Common.build("ABOS.properties");
        
        RawToProcessed rtp = new RawToProcessed();
        
        rtp.ProcessMooingId(args[0]);
    }
}
