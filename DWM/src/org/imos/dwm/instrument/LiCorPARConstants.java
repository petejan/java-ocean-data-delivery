/*
 * IMOS data delivery project
 * Written by Peter Wiley
 * This code is copyright (c) Peter Wiley 2000 - ?
 * It is made available under the BSD Software Licence in the hope that it may be useful.
 * It has NO WARRANTY OF FITNESS OR SUITABILITY FOR ANY PURPOSE.
 * Feel free to fix any bugs that you may find.
 */

package org.imos.dwm.instrument;

import org.imos.dwm.dbms.InstrumentCalibrationValue;

/**
 *
 * @author peter
 */
public class LiCorPARConstants extends AbstractInstrumentConstants
{
    public Double offsetAnalogCoefficient;
    public Double slopeAnalogCoefficient;
    @Override
    protected void parse(InstrumentCalibrationValue row)
    {
        //
        // here we get into hard-coded parameter names so be wary....
        //
        String paramName = row.getParameterCode();
        String paramValue = row.getParameterValue();
        String dataType = row.getDataType();

        if(paramName.equalsIgnoreCase("OFFSET_ANALOG_COEFFICIENT"))
        {
            offsetAnalogCoefficient = parseDouble(paramValue);
            return;
        }

        if(paramName.equalsIgnoreCase("SLOPE_ANALOG_COEFFICIENT"))
        {
            slopeAnalogCoefficient = parseDouble(paramValue);
            return;
        }

        //
        // if none of these, see if it's a base defined value
        //
        super.parse(row);
    }
}
