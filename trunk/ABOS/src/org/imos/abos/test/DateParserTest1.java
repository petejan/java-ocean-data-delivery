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
public class DateParserTest1 
{
    
    public static void main(String[] args)
    {
        DateParserTest1 z = new DateParserTest1();
        
        z.run();
    }
    
    int format = 0;
    
    public Timestamp parseTs(String date)
    {
        SimpleDateFormat dateParser;
        String[] formats = {"yyyy,MM,dd,HH,mm,ss", "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd,HH:mm:ss"};
        java.util.Date d;        
        Timestamp t = null;
        
        for(int i=0;i<formats.length;i++)
        {
            try
            {
                dateParser = new SimpleDateFormat(formats[i]);
                dateParser.setTimeZone(TimeZone.getTimeZone("UTC"));
                System.out.println("Trying " + formats[i]);
                
                d = dateParser.parse(date);

                t = new Timestamp(d.getTime());
                format = i;

                return t;
            }
            catch (ParseException pe)
            {

            }
        }
                
        return t;
        
    }

    public void run()
    {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        String testTs;
        Timestamp dataTimestamp;
        
        testTs = "2009,09,22,12,00,00,12.0,89";
        dataTimestamp = parseTs(testTs);
        System.out.println("Timestamp parse for text '" + dataTimestamp + "'");
        testTs = "2009,09,22,12,00,00,12.1,88,0.0,11";
        dataTimestamp = parseTs(testTs);
        System.out.println("Timestamp parse for text '" + dataTimestamp + "'");
        testTs = "2010-09-08 00:00:00,17.34,33,-1.8,10";
        dataTimestamp = parseTs(testTs);
        System.out.println("Timestamp parse for text '" + dataTimestamp + "'");
    }
}
