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
public class GTDParser extends AbstractDataParser
{

    @Override
    protected boolean isHeader(String dataLine)
    {
        return !dataLine.matches("P \\d{4},.*");
    }

    @Override
    protected void parseHeader(String dataLine) throws ParseException, NoSuchElementException
    {
        // don't need these
    }

	//
	//File Contents:
	//Measurement type,Year,Month,Day,Hour,Minute,Second,Pressure,Temperature,Analog in 1 A/D,Analog in 2 A/D, Digital in 1,Digital in 2
	//%s,%.3f,%.3f,%04.0f,%04.0f,%01.0f,%01.0f
	//P 2018,08,21,06,59,59,1010.256,10.326,6.5,2435,1823,0,0
	//P 2018,08,21,07,29,59,1010.332,10.255,6.6,2414,1832,0,0
	//P 2018,08,21,07,59,59,1010.180,10.247,6.5,2453,1843,0,0
	//P 2018,08,21,08,29,59,1010.456,10.245,6.5,2446,1838,0,0
	//P 2018,08,21,08,59,59,1011.201,10.249,6.5,2456,1843,0,0
	//P 2018,08,21,09,29,59,1010.941,10.250,6.5,2414,1819,0,0
	//P 2018,08,21,09,59,59,1011.288,10.252,6.5,2456,1843,0,0

    @Override
    protected void parseData(String dataLine) throws ParseException, NoSuchElementException
    {
        SimpleDateFormat dateParser = new SimpleDateFormat("yyyy MM dd HH mm ss");
        dateParser.setTimeZone(tz);
        
        DecimalFormat deciFormat = new DecimalFormat("-######.0#");

        Timestamp dataTimestamp = null;
        
        Double presVal = null;
        Double tempVal = null;

        String[] splt = dataLine.split("[, ]");
        
        dataTimestamp = new Timestamp(dateParser.parse(splt[1] + " " + splt[2]+ " " + splt[3]+ " " + splt[4]+ " " + splt[5]+ " " + splt[6]).getTime());
        
        presVal = getDouble(splt[7]);
        tempVal = getDouble(splt[8]);
        
        //logger.debug("GTD " + dataTimestamp + " " + presVal);
        
        insertData(dataTimestamp, presVal, tempVal);
    }

    protected boolean insertData(Timestamp dataTimestamp, Double presVal, Double tempVal)
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

        row.setParameterCode("TOTAL_GAS_PRESSURE");
        row.setParameterValue(presVal);

        boolean ok = row.insert();

        row.setParameterCode("GTD_TEMPERATURE");
        row.setParameterValue(tempVal);

        ok = row.insert();

        return ok;
    }

}
