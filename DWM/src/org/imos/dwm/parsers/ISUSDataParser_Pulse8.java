/*
 * This code is copyright Peter Wiley 1998-2008
 * It is made available under the BSD Software Licence in the hope that it may be useful.
 * It has NO WARRANTY OF FITNESS OR SUITABILITY FOR ANY PURPOSE.
 * Feel free to fix any bugs that you may find.
 *
 */

package org.imos.dwm.parsers;

import java.text.ParseException;
import java.util.NoSuchElementException;

/**
 *
 * @author peter
 */
public class ISUSDataParser_Pulse8 extends ISUSDataParser
{

    @Override
    protected void parseData(String dataLine) throws ParseException, NoSuchElementException
    {
        int colonPosn = dataLine.indexOf(":") + 1;
        
        super.parseData(dataLine.substring(colonPosn));
        
    }
        
}
