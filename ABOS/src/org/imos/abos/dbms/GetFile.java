/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.imos.abos.dbms;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.imos.abos.forms.NetCDFcreateForm;
import org.wiley.core.Common;
import org.wiley.util.SQLWrapper;

/**
 *
 * @author jan079
 */
public class GetFile
{    
    private static Logger logger = Logger.getLogger(GetFile.class.getName());
    protected static SQLWrapper query = new SQLWrapper();    

     public static void main(String[] args)
     {
        String $HOME = System.getProperty("user.home");
        PropertyConfigurator.configure("log4j.properties");
        Common.build("ABOS.properties");

        Connection conn = Common.getConnection();

        
        for (String id : args)
        {
	        String SQL = "SELECT file_name, file_data, mooring_id FROM instrument_data_files WHERE datafile_pk = " + id;
	        
	        Statement st;
	        try
	        {
	            st = conn.createStatement();
	        
	            ResultSet rs = st.executeQuery(SQL);
	            while (rs.next()) 
	            {
	                String name = rs.getString(1).trim();
	                byte[] imgBytes = rs.getBytes(2);
	                String mooring = rs.getString(3).trim();
	                
	                System.out.println("mooring " + mooring);
	                
	                File dir = new File(mooring);
	                
	                dir.mkdir();
	                
	                System.out.println("File name " + name);
	                
	                File f = new File(mooring + "/" + name);
	                
	                FileOutputStream output = new FileOutputStream(f);
	                
	                output.write(imgBytes);
	                
	                output.close();
	            }
	            rs.close();
	            
	        }
	        catch (SQLException ex)
	        {
	            logger.error(ex);
	        }
	        catch (IOException ex)
	        {
	            logger.error(ex);
	        }
        }
     }
}
