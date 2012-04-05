/*
 * Neonatal Screening Software Project
 * Written by Peter Wiley
 * This code is copyright (c) Peter Wiley 2000 - ?
 * It is made available under the BSD Software Licence in the hope that it may be useful.
 * It has NO WARRANTY OF FITNESS OR SUITABILITY FOR ANY PURPOSE.
 * Feel free to fix any bugs that you may find.
 */

package org.imos.abos.calc;

import org.apache.log4j.Logger;
import org.imos.abos.instrument.WETLabsFLNTUSConstants;

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

    public static Double calculateChlorophyllValue(Double voltage)
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

        Double chlorophyllValue = constants.ChlorophyllAnalogScaleFactor * (voltage - constants.ChlorophyllAnalogDarkCount);
        return chlorophyllValue;
    }

    public static Double calculateTurbidityValue(Double voltage)
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

        Double turbidityValue = constants.TurbidityAnalogScaleFactor * (voltage - constants.TurbidityAnalogDarkCount);
        return turbidityValue;
    }
}
