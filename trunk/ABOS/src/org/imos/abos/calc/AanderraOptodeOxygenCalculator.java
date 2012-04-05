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
import org.imos.abos.instrument.AanderraOptodeConstants;

/**
 *
 * @author peter
 */
public class AanderraOptodeOxygenCalculator
{

    private static Logger logger = Logger.getLogger(AanderraOptodeOxygenCalculator.class.getName());
    //
    // PDW 20120308
    //
    // these correction values are from the Aanderra optode manual and can be assumed correct & immutable
    //
    // hah
    //
    // correct for salinity effect using optode relationship
    // for atm P = 1 and internal salinity optode = 0

    private static double b0 = -6.24097e-3;
    private static double b1 = -6.93498e-3;
    private static double b2 = -6.90358e-3;
    private static double b3 = -4.29155e-3;
    private static double c0 = -3.11680e-7;

//    private static double C0Coeffs[];
//    private static double C1Coeffs[];
//    private static double C2Coeffs[];
//    private static double C3Coeffs[];
//    private static double C4Coeffs[];
    //*/

    private static AanderraOptodeConstants constants;

    public AanderraOptodeOxygenCalculator()
    {
    }


    public static void setOptodeConstants(AanderraOptodeConstants c)
    {
        constants = c;
    }

    /**
     * calculate oxygen concentration in uMole/kg of seawater
     *
     * @param optode temperature in degrees C
     * @param optode DPhase value (dimensionless)
     * @param salinity
     * 
     * @return dissolved oxygen in micromoles per kg of water
     */
    public static Double calculateOxygenValue(Double optodeTemperature, Double optodeDPhaseValue, Double salinity)
    {
        if(constants == null)
        {
            logger.error("No constants class assigned, no computation is possible!");
            return null;
        }

        if (optodeTemperature == null || optodeDPhaseValue == null || salinity == null)
        {
            return null;
        }

        /*
        logger.debug("Salinity: " + salinity
                + " Optode Temp: " + optodeTemperature
                + " Optode dphase: " + optodeDPhaseValue);

        */
        double tarray[] =
        {
            1, optodeTemperature, Math.pow(optodeTemperature, 2), Math.pow(optodeTemperature, 3)
        };

        /*
        logger.debug("ta[0] = " + tarray[0]
                + " ta[1] = " + tarray[1]
                + " ta[2] = " + tarray[2]
                + " ta[3] = " + tarray[3]);
        */
        //;
        //; multiply elements of tarray by elements of C and add to
        //; obtain 5 coefficients to multiply with dphase to
        //; calculate O2 concn
        //
        double av = constants.C0Coeffs[0] * tarray[0] + constants.C0Coeffs[1] * tarray[1] + constants.C0Coeffs[2] * tarray[2]
                + constants.C0Coeffs[3] * tarray[3];

        double aw = constants.C1Coeffs[0] * tarray[0] + constants.C1Coeffs[1] * tarray[1] + constants.C1Coeffs[2] * tarray[2]
                + constants.C1Coeffs[3] * tarray[3];

        double ax = constants.C2Coeffs[0] * tarray[0] + constants.C2Coeffs[1] * tarray[1] + constants.C2Coeffs[2] * tarray[2]
                + constants.C2Coeffs[3] * tarray[3];

        double ay = constants.C3Coeffs[0] * tarray[0] + constants.C3Coeffs[1] * tarray[1] + constants.C3Coeffs[2] * tarray[2]
                + constants.C3Coeffs[3] * tarray[3];

        double az = constants.C4Coeffs[0] * tarray[0] + constants.C4Coeffs[1] * tarray[1] + constants.C4Coeffs[2] * tarray[2]
                + constants.C4Coeffs[3] * tarray[3];

        //logger.debug("av = " + av + " aw = " + aw + " ax = " + ax + " ay = " + ay + " az = " + az);

        double o2 = av
                + (aw * optodeDPhaseValue)
                + (ax * (Math.pow(optodeDPhaseValue, 2)))
                + (ay * (Math.pow(optodeDPhaseValue, 3)))
                + (az * (Math.pow(optodeDPhaseValue, 4)));

        //logger.debug("Oxygen before temperature adjustment: " + o2);

        //;
        //; scale Temperature
        //;
        double ts = Math.log((298.15 - optodeTemperature) / (273.15 + optodeTemperature));
        o2 = o2 * Math.exp(salinity.doubleValue() * (b0 + b1 * ts + (b2 * Math.pow(ts, 2)) + (b3 * Math.pow(ts, 3)))
                + c0 * Math.pow(salinity.doubleValue(), 2));

        //logger.debug("Temperature adjusted oxygen before density adjustment: " + o2);

        //;
        //;convert to umol/kg
        //;
        double density = seawaterDensity(salinity.doubleValue(), optodeTemperature) / 1000.0;
        //;
        o2 = o2 / density;

        //logger.debug("Temperature & density adjusted oxygen: " + o2);
        return new Double(o2);
    }

    private static double seawaterDensity(double salinity, double temperature)
    {
        //; DEFINE CONSTANTS
        //;----------------------
        //;     UNESCO 1983 eqn(13) p17.

        double B0 = 8.24493e-1;
        double B1 = -4.0899e-3;
        double B2 = 7.6438e-5;
        double B3 = -8.2467e-7;
        double B4 = 5.3875e-9;
        double C0 = -5.72466e-3;
        double C1 = +1.0227e-4;
        double C2 = -1.6546e-6;
        double D0 = 4.8314e-4;

        double sw_dens0 = sw_smow(temperature)
                + (B0 + (B1 + (B2 + (B3 + B4 * temperature) * temperature) * temperature) * temperature) * salinity
                + (C0 + (C1 + C2 * temperature) * temperature) * salinity * Math.sqrt(salinity)
                + D0 * Math.pow(salinity, 2);
        return sw_dens0;
    }

    private static double sw_smow(double temp)
    {
        /*
        ; SW_SMOW    Denisty of standard mean ocean water (pure water)
        ;=======================================================================
        ; SW_SMOW  $Revision: 1.1 $  $Date: 2007/03/20 00:31:30 $
        ;          Copyright (C) CSIRO, Phil Morgan 1992.
        ;
        ; USAGE:  dens = sw_smow(T)
        ;
        ; DESCRIPTION:
        ;    Denisty of Standard Mean Ocean Water (Pure Water) using EOS 1980.
        ;
        ; INPUT:
        ;   T = temperature [degree C (IPTS-68)]
        ;
        ; OUTPUT:
        ;   dens = density  [kg/m^3]
        ;
        ; AUTHOR:  Phil Morgan 92-11-05  (morgan@ml.csiro.au)
        ;
        ; IDL CONVERSION: Andrew Lenton (andrew.lenton@marine.csiro.au) 1999
        ;
        ; DISCLAIMER:
        ;   This software is provided "as is" without warranty of any kind.
        ;   See the file sw_copy.m for conditions of use and licence.
        ;
        ; REFERENCES:
        ;     Unesco 1983. Algorithms for computation of fundamental properties
        ;     of seawater, 1983. _Unesco Tech. Pap. in Mar. Sci._, No. 44, 53 pp
        ;     UNESCO 1983 p17  Eqn(14)
        ;
        ;     Millero, F.J & Poisson, A.
        ;     INternational one-atmosphere equation of state for seawater.
        ;     Deep-Sea Research Vol28A No.6. 1981 625-629.    Eqn (6)
        ;=======================================================================

        Java conversion Peter Wiley, Australian Antarctic Division 2007-02-03
         */

        double a0 = 999.842594d;
        double a1 = 6.793952e-2;
        double a2 = -9.095290e-3;
        double a3 = 1.001685e-4;
        double a4 = -1.120083e-6;
        double a5 = 6.536332e-9;
        double val = a0 + (a1 + (a2 + (a3 + (a4 + a5 * temp) * temp) * temp) * temp) * temp;
        return val;
    }
}

