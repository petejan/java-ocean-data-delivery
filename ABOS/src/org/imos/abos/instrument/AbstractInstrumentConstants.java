/*
 * IMOS data delivery project
 * Written by Peter Wiley
 * This code is copyright (c) Peter Wiley 2000 - ?
 * It is made available under the BSD Software Licence in the hope that it may be useful.
 * It has NO WARRANTY OF FITNESS OR SUITABILITY FOR ANY PURPOSE.
 * Feel free to fix any bugs that you may find.
 */

package org.imos.abos.instrument;

import java.util.ArrayList;
import org.imos.abos.dbms.InstrumentCalibrationFile;
import org.imos.abos.dbms.InstrumentCalibrationValue;

/**
 *
 * @author peter
 */
public abstract class AbstractInstrumentConstants
{
    protected InstrumentCalibrationFile calFile = null;
    protected boolean parseSuccess = false;

    public  String serialNumber;
    public  String calibrationDate;

    public void setInstrumentCalibrationFile(InstrumentCalibrationFile f)
    {
        calFile = f;
        if(calFile != null)
        {
            //
            // get any/all calibration data values we need
            //
            ArrayList<InstrumentCalibrationValue> values = InstrumentCalibrationValue.selectByCalibrationFileID(calFile.getDataFilePrimaryKey());
            if(values != null && values.size() > 0)
            {

                for(int i = 0; i < values.size(); i++)
                {
                    parse(values.get(i));

                }
            }
        }
    }

    protected void parse(InstrumentCalibrationValue row)
    {
        String paramName = row.getParameterCode();
        String paramValue = row.getParameterValue();
        String dataType = row.getDataType();

        if(paramName.equalsIgnoreCase("SERIAL_NUMBER"))
        {
            serialNumber = paramValue;
            return;
        }

        if(paramName.equalsIgnoreCase("CALIBRATION_DATE"))
        {
            calibrationDate = paramValue;
            return;
        }
    }

    protected Double parseDouble(String s)
    {
        Double d = new Double(s);
        return d;
    }

}
