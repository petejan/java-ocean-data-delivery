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
public class PC02Parser extends AbstractDataParser
{

    @Override
    protected boolean isHeader(String dataLine)
    {
        return !dataLine.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}.+");
    }

    @Override
    protected void parseHeader(String dataLine) throws ParseException, NoSuchElementException
    {
        // don't need these
    }

    // 2011-11-10 03:17:00, 100.55 , 573.637096267707  , 387.339293754156  , NaN  , NaN  , NaN 
    // 2011-11-10 06:17:00, 100.75 , 518.813767853876  , 389.714575035098  , NaN  , NaN  , NaN     

    @Override
    protected void parseData(String dataLine) throws ParseException, NoSuchElementException
    {
        SimpleDateFormat dateParser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateParser.setTimeZone(tz);
        
        Timestamp dataTimestamp = null;
        
        Double presVal = null;
        Double pc02SW = null;
        Double pc02AIR = null;

        String[] splt = dataLine.split(",");
        
        dataTimestamp = new Timestamp(dateParser.parse(splt[0]).getTime());
        
        presVal = getDouble(splt[1]) * 10; // pressure in file is kPa, convert to hectopascal
        pc02SW = getDouble(splt[2]);
        pc02AIR = getDouble(splt[3]);
        
        insertData(dataTimestamp, presVal, pc02SW, pc02AIR);
    }

    protected boolean insertData(Timestamp dataTimestamp, Double presVal, Double pc02SW, Double pc02AIR)
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

        row.setParameterCode("CAPH");
        row.setParameterValue(presVal);
        boolean ok = row.insert();

        row.setParameterCode("PCO2");
        row.setParameterValue(pc02SW);
        ok = row.insert();
        
        row.setParameterCode("PCO2_AIR");
        row.setParameterValue(pc02AIR);
        ok = row.insert();

        return ok;
    }

}
