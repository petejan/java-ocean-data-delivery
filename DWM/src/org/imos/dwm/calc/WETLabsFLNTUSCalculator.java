/*
 * IMOS data delivery project
 * Written by Peter Wiley
 * This code is copyright (c) Peter Wiley 2000 - ?
 * It is made available under the BSD Software Licence in the hope that it may be useful.
 * It has NO WARRANTY OF FITNESS OR SUITABILITY FOR ANY PURPOSE.
 * Feel free to fix any bugs that you may find.
 */

package org.imos.dwm.calc;

import org.apache.log4j.Logger;
import org.imos.dwm.instrument.WETLabsFLNTUSConstants;

/**
 *
 * @author peter
 */
public class WETLabsFLNTUSCalculator
{
    private static Logger logger = Logger.getLogger(WETLabsFLNTUSCalculator.class.getName());

    private static WETLabsFLNTUSConstants constants = null;

    public static void setConstants(WETLabsFLNTUSConstants c)
    {
        constants = c;
    }

    // calculate the digital value from voltage
    public static Double calculateChlorophyllCount(Double voltage)
    {
        if(constants == null)
        {
            logger.error("Cannot calculate chlorophyll without calibration coefficients!");
            return null;
        }
        if( voltage == null)
        {
            logger.error("Cannot calculate chlorophyll without valid voltage value!");
            return null;
        }


        Double count = constants.ChlorophyllDigitalMaxOutput * ((voltage - constants.ChlorophyllAnalogDarkCount)/constants.ChlorophyllAnalogMaxOutput) + constants.ChlorophyllDigitalDarkCount;
        return (double) Math.round(count);
    }

    // calculate the chlorophyll from the counts
    public static Double calculateChlorophyllValue(Double count)
    {
        if(constants == null)
        {
            logger.error("Cannot calculate chlorophyll without calibration coefficients!");
            return null;
        }
        if( count == null)
        {
            logger.error("Cannot calculate chlorophyll without valid voltage value!");
            return null;
        }

        Double chlorophyllValue = constants.ChlorophyllDigitalScaleFactor * (count - constants.ChlorophyllDigitalDarkCount);
        return chlorophyllValue;
    }

    public static Double calculateTurbidityCount(Double voltage)
    {
        if(constants == null)
        {
            logger.error("Cannot calculate turbidity without calibration coefficients!");
            return null;
        }
        if( voltage == null)
        {
            logger.error("Cannot calculate turbidity without valid voltage value!");
            return null;
        }

        Double count = constants.TurbidityDigitalMaxOutput * ((voltage - constants.TurbidityAnalogDarkCount)/constants.TurbidityAnalogMaxOutput) + constants.TurbidityDigitalDarkCount;
        return (double) Math.round(count);
    }
    
    public static Double calculateTurbidityValue(Double count)
    {
        if(constants == null)
        {
            logger.error("Cannot calculate turbidity without calibration coefficients!");
            return null;
        }
        if( count == null)
        {
            logger.error("Cannot calculate turbidity without valid count value!");
            return null;
        }

        Double turbidityValue = constants.TurbidityDigitalScaleFactor * (count - constants.TurbidityDigitalDarkCount);
        return turbidityValue;
    }
}
