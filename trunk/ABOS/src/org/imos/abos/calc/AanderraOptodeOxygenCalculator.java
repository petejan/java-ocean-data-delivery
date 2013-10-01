/*
 * IMOS data delivery project
 * Written by Peter Wiley
 * This code is copyright (c) Peter Wiley 2000 - ?
 * It is made available under the BSD Software Licence in the hope that it may be useful.
 * It has NO WARRANTY OF FITNESS OR SUITABILITY FOR ANY PURPOSE.
 * Feel free to fix any bugs that you may find.
 */

package org.imos.abos.calc;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.imos.abos.dbms.Instrument;
import org.imos.abos.dbms.Mooring;
import org.imos.abos.instrument.AanderraOptodeConstants;
import org.wiley.core.Common;

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

    private static AanderraOptodeConstants constants;

    public AanderraOptodeOxygenCalculator()
    {
    }


    public static void setOptodeConstants(AanderraOptodeConstants c)
    {
        constants = c;
    }

    protected static double correctSalinity(double salinity, double temperature)
    {
        double ts = Math.log((298.15 - temperature) / (273.15 + temperature));
        
        return Math.exp(salinity * (b0 + b1 * ts + (b2 * Math.pow(ts, 2)) + (b3 * Math.pow(ts, 3))) + c0 * Math.pow(salinity, 2));        
    }
    
    protected static double correctDepth(double pressure)
    {
        return 1-(0.032*pressure/1000);
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
    public static Double AanderaaCalculateDissolvedOxygenInUMolesPerKg(Double optodeTemperature, Double optodeDPhaseValue, Double salinity, Double pressure)
    {
        //logger.debug("optodeTemp " + optodeTemperature + " optodeDPhase " + optodeDPhaseValue + " Salinity " + salinity + " pressure " + pressure);
        
        if(constants == null)
        {
            logger.error("No constants class assigned, no computation is possible!");
            return null;
        }

        if (optodeTemperature == null || optodeDPhaseValue == null || salinity == null || pressure == null)
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

        
        //logger.debug("ta[0] = " + tarray[0] + " ta[1] = " + tarray[1] + " ta[2] = " + tarray[2] + " ta[3] = " + tarray[3]);
        //logger.debug("CoCoeffs " + constants.C0Coeffs[0] + " " + constants.C0Coeffs[1] + " " + constants.C0Coeffs[2] + " " + constants.C0Coeffs[3]);
        //logger.debug("CoCoeffs " + constants.C1Coeffs[0] + " " + constants.C1Coeffs[1] + " " + constants.C1Coeffs[2] + " " + constants.C1Coeffs[3]);
        //logger.debug("CoCoeffs " + constants.C2Coeffs[0] + " " + constants.C2Coeffs[1] + " " + constants.C2Coeffs[2] + " " + constants.C2Coeffs[3]);
        //logger.debug("CoCoeffs " + constants.C3Coeffs[0] + " " + constants.C3Coeffs[1] + " " + constants.C3Coeffs[2] + " " + constants.C3Coeffs[3]);
        //logger.debug("CoCoeffs " + constants.C4Coeffs[0] + " " + constants.C4Coeffs[1] + " " + constants.C4Coeffs[2] + " " + constants.C4Coeffs[3]);
        
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

        //logger.debug("Oxygen before salinity adjustment: " + o2);

        o2 = o2 * correctSalinity(salinity.doubleValue(), optodeTemperature);

        //logger.debug("salinity adjusted oxygen before density adjustment: " + o2);
        
        o2 = o2 * correctDepth(pressure);

        //logger.debug("salinity & depth djusted oxygen: " + o2);

        double sigma_theta = SeawaterParameterCalculator.PoTemp(salinity, optodeTemperature * 1.00024, pressure, 0);
        
        o2 = o2 * (1000.0/SeawaterParameterCalculator.calculateSeawaterDensityAtPressure(salinity, sigma_theta, 0.0));        
        
        return new Double(o2);
    }
    
    /**
     * calculate dissolved oxygen using the CSIRO/Uchida algorithm
     * 
     * @param optodeTemperature
     * @param optodeBPhaseValue
     * @param salinity
     * @param pressure
     * @return 
     */
    
    public static Double UchidaCalculateDissolvedOxygenInUMolesPerKg(Double optodeTemperature, Double optodeBPhaseValue, Double salinity, Double pressure)
    {
        double dissolvedOxygen = AanderraOptodeOxygenCalculator.UchidaCalculateDissolvedOxygenRaw(optodeTemperature, optodeBPhaseValue);
        
        //logger.debug("Oxygen before salinity adjustment: " + dissolvedOxygen);

        dissolvedOxygen = dissolvedOxygen * correctSalinity(salinity.doubleValue(), optodeTemperature);

        //logger.debug("salinity adjusted oxygen before density adjustment: " + dissolvedOxygen);
        
        dissolvedOxygen = dissolvedOxygen * correctDepth(pressure);

        double sigma_theta = SeawaterParameterCalculator.PoTemp(salinity, optodeTemperature * 1.00024, pressure, 0);
        
        dissolvedOxygen = dissolvedOxygen * (1000.0/SeawaterParameterCalculator.calculateSeawaterDensityAtPressure(salinity, sigma_theta, 0.0));        

        //logger.debug("salinity & depth djusted oxygen: " + dissolvedOxygen);
        
        return dissolvedOxygen;
        
    }
    public static Double UchidaCalculateDissolvedOxygenRaw(Double optodeTemperature, Double optodeBPhaseValue)
    {
        if(constants == null)
        {
            logger.error("No constants class assigned, no computation is possible!");
            return null;
        }

        if (optodeTemperature == null || optodeBPhaseValue == null )
        {
            return null;
        }
        
        if(
                constants.UchidaC1Coefficient == null 
                || constants.UchidaC2Coefficient == null
                || constants.UchidaC3Coefficient == null
                || constants.UchidaC4Coefficient == null
                || constants.UchidaC5Coefficient == null
                || constants.UchidaC6Coefficient == null
                || constants.UchidaC7Coefficient == null
          )
        {
            logger.error("At least 1 Uchida constant missing, no computation is possible!");
            return null;
        }
        
        double p1 = (constants.UchidaC4Coefficient + constants.UchidaC5Coefficient * optodeTemperature);
        double p2 = (constants.UchidaC6Coefficient + constants.UchidaC7Coefficient * optodeBPhaseValue);
        
        double p3 = constants.UchidaC1Coefficient
                    + constants.UchidaC2Coefficient * optodeTemperature
                    + constants.UchidaC3Coefficient * Math.pow(optodeTemperature, 2)
                ;
        
        double dissolvedOxygen = ((p1/p2) - 1)/p3;
                        
        return dissolvedOxygen;
    }
    
    public static void main(String args[])
    {
        String $HOME = System.getProperty("user.home");

        if(args.length == 0)
        {
            PropertyConfigurator.configure("log4j.properties");
            Common.build($HOME + "/ABOS/ABOS.properties");
        }
        
        AanderraOptodeOxygenCalculator aooc = new AanderraOptodeOxygenCalculator();
        
        AanderraOptodeConstants aoc = new AanderraOptodeConstants();
        
        aoc.setInstrumentAndMooring(Instrument.selectByInstrumentID(620), Mooring.selectByMooringID("Pulse-7-2010")); // Optode-1161, Pulse-7
        
        AanderraOptodeOxygenCalculator.setOptodeConstants(aoc);
        
        System.out.println("Constants " + aoc);
        
        System.out.println("DO = " + AanderraOptodeOxygenCalculator.UchidaCalculateDissolvedOxygenInUMolesPerKg(9.445, 32.7004, 34.6727, 31.682) + " umol/kg");
                
    }
}

