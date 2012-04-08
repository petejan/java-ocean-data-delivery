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
public class WETLabsFLNTUSConstants extends AbstractInstrumentConstants
{
    public Double ChlorophyllAnalogDarkCount;
    public Double ChlorophyllAnalogScaleFactor;
    public Double ChlorophyllAnalogMaxOutput;
    public Double ChlorophyllAnalogResolution;

    public Double ChlorophyllDigitalDarkCount;
    public Double ChlorophyllDigitalScaleFactor;
    public Double ChlorophyllDigitalMaxOutput;
    public Double ChlorophyllDigitalResolution;

    public Double TurbidityAnalogDarkCount;
    public Double TurbidityAnalogNTUSolutionValue;
    public Double TurbidityAnalogScaleFactor;
    public Double TurbidityAnalogMaxOutput;
    public Double TurbidityAnalogResolution;

    public Double TurbidityDigitalDarkCount;
    public Double TurbidityDigitalNTUSolutionValue;
    public Double TurbidityDigitalScaleFactor;
    public Double TurbidityDigitalMaxOutput;
    public Double TurbidityDigitalResolution;


    @Override
    protected void parse(InstrumentCalibrationValue row)
    {
        //
        // here we get into hard-coded parameter names so be wary....
        //
        String paramName = row.getParameterCode();
        String paramValue = row.getParameterValue();
        String dataType = row.getDataType();

        if(paramName.equalsIgnoreCase("CH_ANALOG_DARK_COUNT"))
        {
            ChlorophyllAnalogDarkCount = parseDouble(paramValue);
            return;
        }

        if(paramName.equalsIgnoreCase("CH_ANALOG_MAX_OUTPUT"))
        {
            ChlorophyllAnalogMaxOutput = parseDouble(paramValue);
            return;
        }

        if(paramName.equalsIgnoreCase("CH_ANALOG_RESOLUTION"))
        {
            ChlorophyllAnalogResolution = parseDouble(paramValue);
            return;
        }

        if(paramName.equalsIgnoreCase("CH_ANALOG_SCALE_FACTOR"))
        {
            ChlorophyllAnalogScaleFactor = parseDouble(paramValue);
            return;
        }

        if(paramName.equalsIgnoreCase("CH_DIGITAL_DARK_COUNT"))
        {
            ChlorophyllDigitalDarkCount = parseDouble(paramValue);
            return;
        }

        if(paramName.equalsIgnoreCase("CH_DIGITAL_MAX_OUTPUT"))
        {
            ChlorophyllDigitalMaxOutput = parseDouble(paramValue);
            return;
        }

        if(paramName.equalsIgnoreCase("CH_DIGITAL_RESOLUTION"))
        {
            ChlorophyllDigitalResolution = parseDouble(paramValue);
            return;
        }

        if(paramName.equalsIgnoreCase("CH_DIGITAL_SCALE_FACTOR"))
        {
            ChlorophyllDigitalScaleFactor = parseDouble(paramValue);
            return;
        }

        if(paramName.equalsIgnoreCase("TURB_ANALOG_DARK_COUNT"))
        {
            TurbidityAnalogDarkCount = parseDouble(paramValue);
            return;
        }

        if(paramName.equalsIgnoreCase("TURB_ANALOG_NTU_VALUE"))
        {
            TurbidityAnalogNTUSolutionValue = parseDouble(paramValue);
            return;
        }

        if(paramName.equalsIgnoreCase("TURB_ANALOG_MAX_OUTPUT"))
        {
            TurbidityAnalogMaxOutput = parseDouble(paramValue);
            return;
        }

        if(paramName.equalsIgnoreCase("TURB_ANALOG_RESOLUTION"))
        {
            TurbidityAnalogResolution = parseDouble(paramValue);
            return;
        }

        if(paramName.equalsIgnoreCase("TURB_ANALOG_SCALE_FACTOR"))
        {
            TurbidityAnalogScaleFactor = parseDouble(paramValue);
            return;
        }

        if(paramName.equalsIgnoreCase("TURB_DIGITAL_DARK_COUNT"))
        {
            TurbidityDigitalDarkCount = parseDouble(paramValue);
            return;
        }

        if(paramName.equalsIgnoreCase("TURB_DIGITAL_NTU_VALUE"))
        {
            TurbidityDigitalNTUSolutionValue = parseDouble(paramValue);
            return;
        }

        if(paramName.equalsIgnoreCase("TURB_DIGITAL_MAX_OUTPUT"))
        {
            TurbidityDigitalMaxOutput = parseDouble(paramValue);
            return;
        }

        if(paramName.equalsIgnoreCase("TURB_DIGITAL_RESOLUTION"))
        {
            TurbidityDigitalResolution = parseDouble(paramValue);
            return;
        }

        if(paramName.equalsIgnoreCase("TURB_DIGITAL_SCALE_FACTOR"))
        {
            TurbidityDigitalScaleFactor = parseDouble(paramValue);
            return;
        }
        //
        // if none of these, see if it's a base defined value
        //
        super.parse(row);
    }
}
