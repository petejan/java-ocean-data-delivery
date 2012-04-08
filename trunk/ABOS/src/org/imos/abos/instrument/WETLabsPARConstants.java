/*
 * IMOS data delivery project
 * Written by Peter Wiley
 * This code is copyright (c) Peter Wiley 2000 - ?
 * It is made available under the BSD Software Licence in the hope that it may be useful.
 * It has NO WARRANTY OF FITNESS OR SUITABILITY FOR ANY PURPOSE.
 * Feel free to fix any bugs that you may find.
 */

package org.imos.abos.instrument;

import org.imos.abos.dbms.InstrumentCalibrationValue;

/**
 *
 * @author peter
 */
public class WETLabsPARConstants extends AbstractInstrumentConstants
{

    public Double ImAnalogCoefficient;
    public Double A0AnalogCoefficient;
    public Double A1AnalogCoefficient;

    public Double ImDigitalCoefficient;
    public Double A0DigitalCoefficient;
    public Double A1DigitalCoefficient;

    @Override
    protected void parse(InstrumentCalibrationValue row)
    {
        //
        // here we get into hard-coded parameter names so be wary....
        //
        String paramName = row.getParameterCode();
        String paramValue = row.getParameterValue();
        String dataType = row.getDataType();

        if(paramName.equalsIgnoreCase("IM_ANALOG_COEFFICIENT"))
        {
            ImAnalogCoefficient = parseDouble(paramValue);
            return;
        }

        if(paramName.equalsIgnoreCase("A0_ANALOG_COEFFICIENT"))
        {
            A0AnalogCoefficient = parseDouble(paramValue);
            return;
        }

        if(paramName.equalsIgnoreCase("A1_ANALOG_COEFFICIENT"))
        {
            A1AnalogCoefficient = parseDouble(paramValue);
            return;
        }

        if(paramName.equalsIgnoreCase("IM_DIGITAL_COEFFICIENT"))
        {
            ImDigitalCoefficient = parseDouble(paramValue);
            return;
        }

        if(paramName.equalsIgnoreCase("A0_DIGITAL_COEFFICIENT"))
        {
            A0DigitalCoefficient = parseDouble(paramValue);
            return;
        }

        if(paramName.equalsIgnoreCase("A1_DIGITAL_COEFFICIENT"))
        {
            A1DigitalCoefficient = parseDouble(paramValue);
            return;
        }
        //
        // if none of these, see if it's a base defined value
        //
        super.parse(row);
    }
}
