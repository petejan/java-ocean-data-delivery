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
import java.util.NoSuchElementException;
import org.imos.abos.dbms.RawInstrumentData;

/**
 *
 * @author peter
 */
public class ISUSConcParser extends AbstractDataParser
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

// Time of File Creation: 04-Apr-2014 11:08:27
// Start Pixel:            36
// End Pixel:              64
// Start Wavelength:      217.11
// End Wavelength:        239.89
// Baseline:              Baseline 1
// Darks:                 Seawater
// dd-mm-yyyy hh:mm:ss	      NO3	      ASW	    T*ASW	     BL-0	     BL-1
// 2012-07-23 00:00:29	  13.8404	  36.3872	  -332.09	-0.0775754	0.000287262
// 2012-07-23 00:00:31	  13.4819	  35.6331	 -298.975	-0.0684548	0.000251301
    
    @Override
    protected void parseData(String dataLine) throws ParseException, NoSuchElementException
    {
        SimpleDateFormat dateParser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateParser.setTimeZone(tz);
        
        DecimalFormat deciFormat = new DecimalFormat("-######.0#");

        Timestamp dataTimestamp = null;
        
        Double ntrVal = null;

        String[] splt = dataLine.split("\t");
        
        dataTimestamp = new Timestamp(dateParser.parse(splt[0]).getTime());
        
        ntrVal = getDouble(splt[1]);
        
        insertData(dataTimestamp, ntrVal);
    }

    protected boolean insertData(Timestamp dataTimestamp, Double ntrVal)
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

        row.setParameterCode("NTRI_RAW");
        row.setParameterValue(ntrVal);

        boolean ok = row.insert();

        return ok;
    }

}
