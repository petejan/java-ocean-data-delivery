/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.imos.abos.test;

import java.sql.Array;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.wiley.core.Common;

/**
 *
 * @author peter
 */
public class ArraySelect
{
    public static void main(String args[])
    {
        String $HOME = System.getProperty("user.home");

        if(args.length == 0)
        {
            PropertyConfigurator.configure("log4j.properties");
            Common.build("ABOS.properties");
        }
        
        Connection con = Common.getConnection();
        Statement stmt;
        try
        {
            stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT parameter_value FROM array_instrument_data");

            while (rs.next())
            {
                Array z = rs.getArray("parameter_value");
                System.out.println("z type " + z.getBaseTypeName() + " = "+ z);
                Object a = z.getArray();
                if (a instanceof Double[])
                {
                    Double v[] = (Double [])z.getArray();
                    System.out.println("length " + v.length);
                }
                else if (a instanceof Double[][])
                {
                    Double v[][] = (Double [][])z.getArray();
                    System.out.println("length " + v.length + " by " + v[0].length);
                    
                }
                
            }
        }
        catch (SQLException ex)
        {
            Logger.getLogger(ArraySelect.class.getName()).log(Level.SEVERE, null, ex);
        }
        

    }    
}
