/*
 * This code is copyright Peter Wiley 1998-2008
 * It is made available under the BSD Software Licence in the hope that it may be useful.
 * It has NO WARRANTY OF FITNESS OR SUITABILITY FOR ANY PURPOSE.
 * Feel free to fix any bugs that you may find.
 *
 */

package org.imos.abos.test;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 *
 * @author peter
 */
public class DateParserTest 
{
    
    public static void main(String[] args)
    {
        DateParserTest z = new DateParserTest();
        
        z.run();
    }

    public void run()
    {
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
        
        SimpleDateFormat dateParser = new SimpleDateFormat("yyyy/MMM/dd HH:mm:ss");
        dateParser.setTimeZone(TimeZone.getTimeZone("GMT"));
        
        String constructTimestamp = "2011/Aug/02 00:00:30.000";
        try
            {
                java.util.Date d = dateParser.parse(constructTimestamp);
                Timestamp dataTimestamp = new Timestamp(d.getTime());
                
                System.out.println("Timestamp is '" + dataTimestamp + "'");
            }
            catch(ParseException pex)
            {
                System.out.println("Timestamp parse failed for text '" + constructTimestamp + "'");
            }
    }
}
