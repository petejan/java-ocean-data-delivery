/*
 * This code is copyright Peter Wiley 1998-2008
 * It is made available under the BSD Software Licence in the hope that it may be useful.
 * It has NO WARRANTY OF FITNESS OR SUITABILITY FOR ANY PURPOSE.
 * Feel free to fix any bugs that you may find.
 *
 */

package org.imos.dwm.parsers;

import java.text.SimpleDateFormat;

/**
 * date format in the ASCII file has changed so we need a different parser for it.
 * @author peter
 */
public class BranknerTDR2050Parser_Pulse8 extends BranknerTDR2050Parser
{
    
    public BranknerTDR2050Parser_Pulse8()
    {
        super();
        dateParser = new SimpleDateFormat("yyyy/MMM/dd HH:mm:ss");
        dateParser.setTimeZone(tz);
    }
}
