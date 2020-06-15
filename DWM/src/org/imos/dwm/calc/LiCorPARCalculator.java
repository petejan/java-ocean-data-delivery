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
import org.imos.dwm.instrument.LiCorPARConstants;

/**
 *
 * @author peter
 */
public class LiCorPARCalculator
{
    private static Logger logger = Logger.getLogger(LiCorPARCalculator.class.getName());

    private static LiCorPARConstants constants = null;

    public static void setConstants(LiCorPARConstants c)
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

        Double PARValue = (voltage - constants.offsetAnalogCoefficient) * constants.slopeAnalogCoefficient;

        return PARValue;
    }
}
