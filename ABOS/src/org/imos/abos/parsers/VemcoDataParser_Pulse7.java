/*
 * Neonatal Screening Software Project
 * Written by Peter Wiley
 * This code is copyright (c) Peter Wiley 2000 - ?
 * It is made available under the BSD Software Licence in the hope that it may be useful.
 * It has NO WARRANTY OF FITNESS OR SUITABILITY FOR ANY PURPOSE.
 * Feel free to fix any bugs that you may find.
 */

package org.imos.abos.parsers;

import java.text.ParseException;
import java.util.NoSuchElementException;
import org.wiley.util.NullStringTokenizer;

/**
 * This subclass is required because the file header data format changed!
 *
 * @author peter
 */
public class VemcoDataParser_Pulse7 extends VemcoDataParser
{

    public VemcoDataParser_Pulse7()
    {
        super();
    }

    @Override
    protected void parseHeader(String dataLine) throws ParseException, NoSuchElementException
    {
        //
        // the headers for this instrument are NOT well formed for parsing
        //
        NullStringTokenizer st = new NullStringTokenizer(dataLine," ");
        try
        {
            //
            // not all these tokens will be there....
            //
            String star = st.nextToken();
            String code = st.nextToken();
            String value = st.nextToken();

            if(value.endsWith("m"))
            {
                //
                // hopefully it's the depth of the instrument
                //
                int cut = value.indexOf("-");
                String depthString = value.substring(cut);

                char[] contents = depthString.trim().toCharArray();
                StringBuffer x = new StringBuffer();

                for( int i = 0; i < contents.length; i++)
                {
                    if(Character.isDigit(contents[i]))
                        x.append(contents[i]);
                }

                String test = x.toString();
                instrumentDepth = new Double(test);
            }

        }
        catch (NoSuchElementException nse)
        {
          throw nse;
        }
    }

}
