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
 *
 * @author peter
 */
public class BranknerDR1050Parser_Pulse9 extends RBRDR1050Parser
{
    public BranknerDR1050Parser_Pulse9()
    {
        super();
        dateParser = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");
        dateParser.setTimeZone(tz);
    }

}
