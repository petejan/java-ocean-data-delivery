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
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import org.imos.abos.dbms.RawInstrumentData;

/**
 *
 * @author peter
 */
public class StarOddiParser extends AbstractDataParser
{

    @Override
    protected boolean isHeader(String dataLine)
    {
        /*
         * #33	Limit Temp. Corr. OTCR:	0
         * 1	04.08.11 00:00:00	11,022	-4,37	0,9	-10,7
         * 
         * 10	17.03.2017 00:45:00	21,33
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

    @Override
    protected void parseHeader(String dataLine) throws ParseException, NoSuchElementException
    {
        //
        // don't care about the headers
        //
    }

    @Override
    protected void parseData(String dataLine) throws ParseException, NoSuchElementException
    {
        //SimpleDateFormat dateParser = new SimpleDateFormat("dd.MM.yy HH:mm:ss");
        SimpleDateFormat dateParser = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

        Timestamp dataTimestamp = null;
        Double waterTemp = null;
        Double pressure = null;
        Double pitch = null;
        Double roll = null;

        //NumberFormat nf = NumberFormat.getInstance(Locale.FRANCE); // Who would do that !
        NumberFormat nf = NumberFormat.getInstance();
        
        String[] dataValues = dataLine.split("\t");
        try
        {
            try
            {
                java.util.Date d = dateParser.parse(dataValues[1]);
                dataTimestamp = new Timestamp(d.getTime());
            }
            catch(ParseException pex)
            {
                throw new ParseException("Timestamp parse failed for text '" + dataValues[1] + "'",0);
            }

            //
            // ok, we have parsed out the values we need, can now construct the raw data class
            //
            RawInstrumentData row = new RawInstrumentData();

            waterTemp = new Double(nf.parse(dataValues[2].trim()).doubleValue());

            row.setDataTimestamp(dataTimestamp);
            row.setDepth(instrumentDepth);
            row.setInstrumentID(currentInstrument.getInstrumentID());
            row.setLatitude(currentMooring.getLatitudeIn());
            row.setLongitude(currentMooring.getLongitudeIn());
            row.setMooringID(currentMooring.getMooringID());
            row.setParameterCode("TEMP");
            row.setParameterValue(waterTemp);
            row.setSourceFileID(currentFile.getDataFilePrimaryKey());
            row.setQualityCode("RAW");

            boolean ok = row.insert();

            if (dataValues.length > 3)
            {
	            pressure = new Double(nf.parse(dataValues[3].trim()).doubleValue());
	
	            row.setParameterCode("PRES");
	            row.setParameterValue(pressure);
	            row.setSourceFileID(currentFile.getDataFilePrimaryKey());
	            row.setQualityCode("RAW");
	
	            ok = row.insert();
            }
            if (dataValues.length > 4)
            {
	            pitch = new Double(nf.parse(dataValues[4].trim()).doubleValue());
	            
	
	            row.setParameterCode("PITCH");
	            row.setParameterValue(pitch);
	            row.setSourceFileID(currentFile.getDataFilePrimaryKey());
	            row.setQualityCode("RAW");
	
	            ok = row.insert();
            }
            if (dataValues.length > 5)
            {
	            roll = new Double(nf.parse(dataValues[5].trim()).doubleValue());
	
	            row.setParameterCode("ROLL");
	            row.setParameterValue(roll);
	            row.setSourceFileID(currentFile.getDataFilePrimaryKey());
	            row.setQualityCode("RAW");
	
	            ok = row.insert();
            }
        }
        catch (NoSuchElementException nse)
        {
          throw nse;
        }
    }

}
