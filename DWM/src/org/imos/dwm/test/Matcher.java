/*
 * IMOS data delivery project
 * Written by Peter Jansen
 * This code is copyright (c) Peter Jansen 2012
 * It is made available under the BSD Software Licence in the hope that it may be useful.
 * It has NO WARRANTY OF FITNESS OR SUITABILITY FOR ANY PURPOSE.
 * Feel free to fix any bugs that you may find.
 */

package org.imos.dwm.test;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 *
 * @author peter
 */
public class Matcher
{
    public Matcher()
    {
    }
    
    public void run(String patten, String s)
    {
        Pattern p = Pattern.compile(patten);
        java.util.regex.Matcher m = p.matcher(s);
        
        System.out.println("matches " + s.matches(patten));
        
        if (m.find())
        {
            System.out.println(" count " + m.groupCount() + " startIndex " + m.start());
            for(int i=0;i<m.groupCount();i++)
            {
                System.out.println("Found " + m.group(i));
            }
        }
    }
    
    public static void main(String[] args)
    {
        Matcher z = new Matcher();
        
        z.run(args[0], args[1]);
    }
    
}
