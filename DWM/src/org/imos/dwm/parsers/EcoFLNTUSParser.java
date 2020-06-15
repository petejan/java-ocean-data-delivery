/*
 * IMOS data delivery project
 * Written by Peter Wiley
 * This code is copyright (c) Peter Wiley 2000 - ?
 * It is made available under the BSD Software Licence in the hope that it may be useful.
 * It has NO WARRANTY OF FITNESS OR SUITABILITY FOR ANY PURPOSE.
 * Feel free to fix any bugs that you may find.
 */

package org.imos.dwm.parsers;

import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.NoSuchElementException;

import org.imos.dwm.dbms.RawInstrumentData;

/**
 *
 * @author peter
 */
public class EcoFLNTUSParser extends AbstractDataParser
{

    @Override
    protected boolean isHeader(String dataLine)
    {
        return !dataLine.matches("\\d{2}/\\d{2}/\\d{2}[\\t ]\\d{2}:\\d{2}:\\d{2}[\\t ].+");
    }

    @Override
    protected void parseHeader(String dataLine) throws ParseException, NoSuchElementException
    {
        // don't need these
    }

    // 10/27/13	18:00:33	695	50	700	238	562	16380
    // 10/27/13	19:00:29	695	48	700	239	564	16380

    @Override
    protected void parseData(String dataLine) throws ParseException, NoSuchElementException
    {
        SimpleDateFormat dateParser = new SimpleDateFormat("MM/dd/yy HH:mm:ss");
        dateParser.setTimeZone(tz);
        
        DecimalFormat deciFormat = new DecimalFormat("-######.0#");

        Timestamp dataTimestamp = null;
        
        Double chlVal = null;
        Double turbVal = null;
        Double tempVal = null;

        String[] splt = dataLine.split("[\t ]");
        
        dataTimestamp = new Timestamp(dateParser.parse(splt[0] + " " + splt[1]).getTime());
        
        chlVal = getDouble(splt[3]);
        turbVal = getDouble(splt[5]);
        tempVal = getDouble(splt[6]);
        
        insertData(dataTimestamp, chlVal, turbVal, tempVal);
    }

    protected boolean insertData(Timestamp dataTimestamp, Double chlVal, Double turbVal, Double tempVal)
    {
        RawInstrumentData row = new RawInstrumentData();

        row.setDataTimestamp(dataTimestamp);
        row.setDepth(instrumentDepth);
        row.setInstrumentID(currentInstrument.getInstrumentID());
        row.setLatitude(currentMooring.getLatitudeIn());
        row.setLongitude(currentMooring.getLongitudeIn());
        row.setMooringID(currentMooring.getMooringID());
        row.setSourceFileID(currentFile.getDataFilePrimaryKey());
        row.setQualityCode("RAW");

        row.setParameterCode("ECO_FLNTUS_CHL");
        row.setParameterValue(chlVal);

        boolean ok = row.insert();

        row.setParameterCode("ECO_FLNTUS_TURB");
        row.setParameterValue(turbVal);

        ok = row.insert();

        row.setParameterCode("ECO_FLNTUS_TEMP");
        row.setParameterValue(tempVal);

        ok = row.insert();

        return ok;
    }

}
