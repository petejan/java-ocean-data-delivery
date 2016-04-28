/*
 * IMOS data delivery project
 * Written by Peter Wiley
 * This code is copyright (c) Peter Wiley 2000 - ?
 * It is made available under the BSD Software Licence in the hope that it may be useful.
 * It has NO WARRANTY OF FITNESS OR SUITABILITY FOR ANY PURPOSE.
 * Feel free to fix any bugs that you may find.
 */

package org.imos.abos.parsers;

import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import org.imos.abos.dbms.RawInstrumentData;

/**
 *
 * @author peter
 */
public class SeabirdSBE37ParserASC extends AbstractDataParser
{

    @Override
    protected boolean isHeader(String dataLine)
    {
        /*
         * Tv290C	Cond0S/m	Sal00	DD MMM YYYY HH:MM:SS
         * 18.1659	-0.000040	0.0000	04 Nov 2011 04:35:01         
         * anything else is a header
         */
        char c = dataLine.charAt(0);
        if(! Character.isDigit(c) )
        {
            //
            // it is a header
            //
            return true;
        }
        else
            return false;
    }
    
    public class Param
    {
        public Param(int i, String c)
        {
            index = i;
            code = c;
        }
        int index;
        String code;
    }

    ArrayList paramList = new ArrayList();
    
    int tsIndex = -1;
    
    @Override
    protected void parseHeader(String dataLine) throws ParseException, NoSuchElementException
    {
        StringTokenizer st = new StringTokenizer(dataLine, "\t");
        int tokenCount = st.countTokens();
        
        for(int i=0;i<tokenCount;i++)
        {
            String token = st.nextToken().trim();
            if (token.compareTo("Tv290C") == 0)
            {
                paramList.add(new Param(i, "TEMP"));
            }
            else if (token.compareTo("Cond0S/m") == 0)
            {
                paramList.add(new Param(i, "CNDC"));
            }
            else if (token.compareTo("Sal00") == 0)
            {
                paramList.add(new Param(i, "PSAL"));
            }
            else if (token.compareTo("PrdM") == 0)
            {
                paramList.add(new Param(i, "PRES"));
            }
            else if (token.compareTo("SbeopoxMm/Kg") == 0)
            {
                paramList.add(new Param(i, "DOX2"));
            }
            else if (token.compareTo("Density00") == 0)
            {
                paramList.add(new Param(i, "DENSITY"));
            }
            else if (token.compareTo("DD MMM YYYY HH:MM:SS") == 0)
            {
                tsIndex = i;
            }
        }
        logger.info("tsIndex " + tsIndex + " params " + paramList.size());
    }

    @Override
    protected void parseData(String dataLine) throws ParseException, NoSuchElementException
    {
        SimpleDateFormat dateParser = new SimpleDateFormat("dd MMM yyyy HH:mm:ss");
        DecimalFormat deciFormat = new DecimalFormat("-######.0#");

        Timestamp dataTimestamp = null;

        String[] st = dataLine.split("\t");
        try
        {
            java.util.Date d = dateParser.parse(st[tsIndex]);
            dataTimestamp = new Timestamp(d.getTime());
            
            RawInstrumentData row = new RawInstrumentData();

            row.setDataTimestamp(dataTimestamp);
            row.setDepth(instrumentDepth);
            row.setInstrumentID(currentInstrument.getInstrumentID());
            row.setLatitude(currentMooring.getLatitudeIn());
            row.setLongitude(currentMooring.getLongitudeIn());
            row.setMooringID(currentMooring.getMooringID());
            row.setSourceFileID(currentFile.getDataFilePrimaryKey());
            row.setQualityCode("RAW");

            Iterator i = paramList.iterator();
            double v;
            while(i.hasNext())
            {
                Param p = (Param) i.next();
                v = getDouble(st[p.index]);
                
                row.setParameterCode(p.code);
                row.setParameterValue(v);                

                boolean ok = row.insert();
            }
        }
        catch (NoSuchElementException nse)
        {
          throw nse;
        }
    }

}
