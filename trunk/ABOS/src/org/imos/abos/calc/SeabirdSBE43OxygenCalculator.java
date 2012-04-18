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
import org.imos.abos.instrument.SeabirdSBE43Constants;

/**
 *
 * @author peter
 */
public class SeabirdSBE43OxygenCalculator
{
    private static Logger logger = Logger.getLogger(SeabirdSBE43OxygenCalculator.class.getName());

    private static SeabirdSBE43Constants constants;

    /**
     * set the constants as entered on the calibration sheet specific to the instrument
     * @param c
     */
    public static void setSBE43Constants(SeabirdSBE43Constants c)
    {
        constants = c;
    }

    /**
     *
     * @param temperature   in degrees C
     * @param pressure      in dBar
     * @param salinity      in PSU
     * @param voltage       as output from SBE43
     * 
     * @return dissolved oxygen value in ml/litre
     */
    public static Double calculateOxygenValueInMlPerLitre(Double temperature, Double pressure, Double salinity, Double voltage)
    {
        if(constants == null)
        {
            logger.error("Cannot calculate oxygen without calibration coefficients!");
            return null;
        }
        if(temperature == null  || pressure == null || salinity == null || voltage == null)
        {
            logger.error("Cannot calculate oxygen without valid temperature, pressure, salinity and voltage values!");
            return null;
        }
        //
        // ok, got everything we need - calculate
        //
        Double kelvinTemperature = new Double(273.15 + temperature);
        
        Double DO2 = constants.Soc
                    *
                    (voltage + constants.VOffset)
                    *
                    (1.0 + constants.A_Coefficient * temperature
                         + (constants.B_Coefficient * Math.pow(temperature, 2))
                         + (constants.C_Coefficient * Math.pow(temperature, 3))
                    )
                    *
                    OxygenSolubilityCalculator.calculateOxygenSolubilityInMlPerLitre(temperature, salinity)
                    *
                    Math.exp(constants.E_Nominal_Coefficient * pressure/kelvinTemperature);
        
        return DO2;
    }

    /**
     *
     * @param temperature   in degrees C
     * @param pressure      in dBar
     * @param salinity      in PSU
     * @param voltage       as output from SBE43
     *
     * @return dissolved oxygen value in uM/kg
     */
    public static Double calculateOxygenValueInUMolesPerKg(Double temperature, Double pressure, Double salinity, Double voltage)
    {
        if(constants == null)
        {
            logger.error("Cannot calculate oxygen without calibration coefficients!");
            return null;
        }
        if(temperature == null  || pressure == null || salinity == null || voltage == null)
        {
            logger.error("Cannot calculate oxygen without valid temperature, pressure, salinity and voltage values!");
            return null;
        }
        //
        // ok, got everything we need - calculate
        //
//        Double kelvinTemperature = new Double(273.15 + temperature);
//
//        Double DO2 = constants.Soc
//                    *
//                    (voltage + constants.VOffset)
//                    *
//                    (1.0 + constants.A_Coefficient * temperature
//                         + (constants.B_Coefficient * Math.pow(temperature, 2))
//                         + (constants.C_Coefficient * Math.pow(temperature, 3))
//                    )
//                    *
//                    OxygenSolubilityCalculator.calculateOxygenSolubilityInUMolesPerKg(temperature, salinity)
//                    *
//                    Math.exp(constants.E_Nominal_Coefficient * pressure/kelvinTemperature);
        
        Double DO2 = calculateOxygenValueInMlPerLitre(temperature, pressure, salinity, voltage);
        //
        // correct for pressure
        //
        DO2 = DO2 * (44660/SeawaterParameterCalculator.calculateSeawaterDensityAtDepth(salinity, temperature, pressure));

        return DO2;
    }
}
