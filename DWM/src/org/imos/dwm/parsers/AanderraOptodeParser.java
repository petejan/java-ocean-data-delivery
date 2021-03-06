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
import java.text.ParseException;
import java.util.NoSuchElementException;

import org.imos.dwm.dbms.RawInstrumentData;

/**
 *
 * @author peter
 */
public class AanderraOptodeParser extends AbstractDataParser
{

    @Override
    protected boolean isHeader(String dataLine)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void parseHeader(String dataLine) throws ParseException, NoSuchElementException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void parseData(String dataLine) throws ParseException, NoSuchElementException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    protected boolean insertData(Timestamp dataTimestamp, Double dPhaseVal, Double tempVal)
    {
        return insertData(dataTimestamp, dPhaseVal, tempVal, "OPTODE_BPHASE_VOLT");
    }
    protected boolean insertData(Timestamp dataTimestamp, Double dPhaseVal, Double tempVal, String param)
    {
        RawInstrumentData row = new RawInstrumentData();

        row.setDataTimestamp(dataTimestamp);
        row.setDepth(instrumentDepth);
        row.setInstrumentID(currentInstrument.getInstrumentID());
        row.setLatitude(currentMooring.getLatitudeIn());
        row.setLongitude(currentMooring.getLongitudeIn());
        row.setMooringID(currentMooring.getMooringID());
        row.setParameterCode(param);
        row.setParameterValue(dPhaseVal);
        row.setSourceFileID(currentFile.getDataFilePrimaryKey());
        row.setQualityCode("RAW");

        boolean ok = row.insert();

        row.setParameterCode("OPTODE_TEMP_VOLT");
        row.setParameterValue(tempVal);
        row.setSourceFileID(currentFile.getDataFilePrimaryKey());
        row.setQualityCode("RAW");

        ok = row.insert();

        return ok;
    }

}
