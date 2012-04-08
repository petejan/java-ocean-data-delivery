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

/**
 *
 * @author peter
 */
public class OxygenSolubilityCalculator
{
    private static Logger logger = Logger.getLogger(OxygenSolubilityCalculator.class.getName());
    
    


    /**
     * calculate oxygen solubility in ml/l as per Seabird reference above.
     *
     * @param temperature - Double not null
     * @param salinity  -   Double not null
     *
     * @return oxygen solubility in ml/l
     */
    public static Double calculateOxygenSolubilityInMlPerLitre(Double temperature, Double salinity)
    {
        if(temperature == null || salinity == null)
        {
            logger.error("Cannot calculate oxygen solubility if temperature or salinity is null");
            return null;
        }
        //
        // ok, calculate TS value
        //
        //
        // magic numbers (coefficients) from Seabird Application Note 64 Revised Feb 2011
        // SBE43 Dissolved Oxygen Sensor Appendix A
        //

        double A0 = 2.00907;
        double A1 = 3.22014;
        double A2 = 4.0501;
        double A3 = 4.94457;
        double A4 = -0.256847;
        double A5 = 3.88767;

        double B0 = -0.00624523;
        double B1 = -0.00737614;
        double B2 = -0.010341;
        double B3 = -0.00817083;

        double C0 = -0.000000488682;

        Double TS = Math.log((298.15-temperature)/(273.15+temperature));
        //
        // salinity component
        //
        Double salinityComponent = salinity * (
                                                B0
                                                + B1 * TS
                                                + B2 * Math.pow(TS, 2)
                                                + B3 * Math.pow(TS, 3)
                                                )
                                                + C0 * Math.pow(salinity, 2)
                                                ;
        Double oxySol = Math.exp(
                                A0
                                + (A1*TS)
                                + (A2*Math.pow(TS,2))
                                + (A3*Math.pow(TS,3))
                                + (A4*Math.pow(TS,4))
                                + (A5*Math.pow(TS,5))
                                + salinityComponent
                                );

        logger.debug("Calc oxySol for temp "
                    + temperature
                    + " and salinity "
                    + salinity
                    + " is "
                    + oxySol
                    );
        
        return oxySol;
    }

    /**
     * calculate oxygen solubility in ml/l as per Seabird reference above.
     *
     * @param temperature - Double not null
     * @param salinity  -   Double not null
     *
     * @return oxygen solubility in ml/l
     */
    public static Double calculateOxygenSolubilityInUMolesPerKg(Double temperature, Double salinity)
    {
        if(temperature == null || salinity == null)
        {
            logger.error("Cannot calculate oxygen solubility if temperature or salinity is null");
            return null;
        }
        //
        // ok, calculate TS value
        //
        //
        // magic numbers (coefficients) from Seabird Application Note 64 Revised Feb 2011
        // SBE43 Dissolved Oxygen Sensor Appendix A
        //

        double A0 = 5.80871;
        double A1 = 3.20291;
        double A2 = 4.17887;
        double A3 = 5.10006;
        double A4 = -9.86643E-2;
        double A5 = 3.80369;

        double B0 = -7.01577E-3;
        double B1 = -7.70028E-3;
        double B2 = -1.13864E-2;
        double B3 = -9.51519E-3;

        double C0 = -2.75915E-7;

        Double TS = Math.log((298.15-temperature)/(273.15+temperature));
        //
        // salinity component
        //
        Double salinityComponent = salinity * (
                                                B0
                                                + B1 * TS
                                                + B2 * Math.pow(TS, 2)
                                                + B3 * Math.pow(TS, 3)
                                                )
                                                + C0 * Math.pow(salinity, 2)
                                                ;
        Double oxySol = Math.exp(
                                A0
                                + (A1*TS)
                                + (A2*Math.pow(TS,2))
                                + (A3*Math.pow(TS,3))
                                + (A4*Math.pow(TS,4))
                                + (A5*Math.pow(TS,5))
                                + salinityComponent
                                );

        logger.debug("Calc oxySol for temp "
                    + temperature
                    + " and salinity "
                    + salinity
                    + " is "
                    + oxySol
                    );

        return oxySol;
    }
}
