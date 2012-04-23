/*
 * IMOS data delivery project
 * Written by Peter Jansen
 * This code is copyright (c) Peter Jansen 2012
 * It is made available under the BSD Software Licence in the hope that it may be useful.
 * It has NO WARRANTY OF FITNESS OR SUITABILITY FOR ANY PURPOSE.
 * Feel free to fix any bugs that you may find.
 */

package org.imos.abos.test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author peter
 */
public class TimeZ
{
    SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // constructor with host timezone
    SimpleDateFormat sdfz = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
    SimpleDateFormat sdf2;
    
    public TimeZ()
    {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

        sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z"); // has TimeZone of UTC now
    }
    
    public void run()
    {
        System.out.println("Default TimeZone " + TimeZone.getDefault());
        System.out.println();
        Date d;
        try
        {
            d = sdf1.parse("2012-04-23 20:00:00"); // Has TimeZone from Host because created before constructor
            
            System.out.println("sdf1 date " + d + " zone " + sdf1.getTimeZone());
            System.out.println();
            
            System.out.println("sdfz " + sdfz.format(d));
            System.out.println();
            
            System.out.println("sdf2 " + sdf2.format(d));
            System.out.println();

            
            sdf1.setTimeZone(TimeZone.getTimeZone("UTC")); // Now has UTC timezone
            d = sdf1.parse("2012-04-23 20:00:00");
            
            System.out.println("sdf1 date " + d + " zone " + sdf1.getTimeZone());
            System.out.println();
            
            System.out.println("sdfz " + sdfz.format(d));
            System.out.println();
            
            System.out.println("sdf2 " + sdf2.format(d));
            System.out.println();
            
        }
        catch (ParseException ex)
        {
            Logger.getLogger(TimeZ.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    public static void main(String[] args)
    {
        TimeZ z = new TimeZ();
        
        z.run();
    }
    
}
