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
 * Only reason for this class is because the Pulse 7 mooring instruments did something 'odd' and
 * the Pulse 8 ones are the same as Pulse 6. So this is a wrapper class for clarity only.
 * @author peter
 */
public class AlecPARParser_Pulse8 extends AlecPARParser_Pulse6
{
    
    @Override
    protected boolean isHeader(String dataLine)
    {
        return super.isHeader(dataLine);
    }
    
   @Override
    protected void parseHeader(String dataLine) throws ParseException, NoSuchElementException
    {
    }
   

   @Override
    protected void parseData(String dataLine) throws ParseException, NoSuchElementException
    {
        super.parseData(dataLine);
    }
}
