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
import org.imos.dwm.instrument.WETLabsPARConstants;

/**
 *
 * @author peter
 */
public class WETLabsPARCalculator
{
    private static Logger logger = Logger.getLogger(WETLabsPARCalculator.class.getName());

    private static WETLabsPARConstants constants = null;

    public static void setConstants(WETLabsPARConstants c)
    {
        constants = c;
    }

    /**
     * calculate instrument PAR value in umol photons/m2/s
     * @param voltage
     * @return PAR value in umol photons/m2/s
     */
    public static Double calculatePARValue(Double voltage)
    {
        if(constants == null)
        {
            logger.error("Cannot calculate PAR without calibration coefficients!");
            return null;
        }
        if( voltage == null)
        {
            logger.error("Cannot calculate PAR without valid voltage value!");
            return null;
        }

        Double powerFactor = (voltage - constants.A0AnalogCoefficient) / constants.A1AnalogCoefficient;

        Double PARValue = constants.ImAnalogCoefficient * Math.pow(10, powerFactor);
        return PARValue;
    }
    public static Double calculatePARValueCount(Double count)
    {
        if(constants == null)
        {
            logger.error("Cannot calculate PAR without calibration coefficients!");
            return null;
        }
        if( count == null)
        {
            logger.error("Cannot calculate PAR without valid voltage value!");
            return null;
        }

        Double powerFactor = (count - constants.A0DigitalCoefficient) / constants.A1DigitalCoefficient;

        Double PARValue = constants.ImDigitalCoefficient * Math.pow(10, powerFactor);
        return PARValue;
    }

}
