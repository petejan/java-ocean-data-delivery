/*
 * Neonatal Screening Software Project
 * Written by Peter Wiley
 * This code is copyright (c) Peter Wiley 2000 - ?
 * It is made available under the BSD Software Licence in the hope that it may be useful.
 * It has NO WARRANTY OF FITNESS OR SUITABILITY FOR ANY PURPOSE.
 * Feel free to fix any bugs that you may find.
 */
package org.imos.abos.calc;

/**
 *
 * @author peter
 *
 * essentially a copy of original code by Gordon Kieth, AAD, 1997.
 *
 *	Caculate salinity from temperature, conductivity and pressure.
 *	Based on UNESCO fortran routine SAL78.
 *
 * @param  temperature      Temperature in degrees C (T68).
 * @param  conductivity      Conductivity in mmho/cm (1 mmho/cm = 10 Siemens/m)
 * @param  pressure  Pressure in decibars (0 = 1 atmosphere)
 * @return           Salinity (PSS-78)
 */
public class SalinityCalculator
{

    /**  Conductivity standard for PSS-78 for salinity 35 at temperature 15 at 1 atmosphere. */
    private final static double C35150 = 42.914;

    /**
     *
     * @param temperature       Temperature in degrees C (ITS90).
     * @param conductivity      Conductivity in mmho/cm (1 mmho/cm = 10 Siemens/m)
     * @param pressure          Pressure in decibars (0 = 1 atmosphere)
     * @return                  Salinity (PSS-78)
     */
    public static double calculateSalinityForITS90Temperature(double temperature, double conductivity, double pressure)
    {
        double correctedTemperature = temperature * 1.00024;
        return calculateSalinityForITS68Temperature(correctedTemperature, conductivity, pressure);
    }
    
    /**
     *
     * @param temperature       Temperature in degrees C (ITS68).
     * @param conductivity      Conductivity in mmho/cm (1 mmho/cm = 10 Siemens/m)
     * @param pressure          Pressure in decibars (0 = 1 atmosphere)
     * @return                  Salinity (PSS-78)
     */
    public static double calculateSalinityForITS68Temperature(double temperature, double conductivity, double pressure)
    {
        // Convert parameters to horrible fortran names.
        double T = temperature;
        double R = conductivity / C35150;
        double DT = temperature - 15.0;
        double P = pressure;
        // Calculate Salinity using converted copies of the Fortran subroutines.
        // All names have been preserved so that its just a case of cut and paste
        // and tidying up the white space.
        // GJK - Simplification for pressure == 0 was not in the Fortran.
        double RT;
        if (pressure == 0.0)
        {
            RT = R / RT35(T);
        }
        else
        {
            RT = R / (RT35(T) * (1.0 + C(P) / (B(T) + A(T) * R)));
        }
        RT = Math.sqrt(Math.abs(RT));
        return SAL(RT, DT);
    }

    /**
     *  Description of the Method
     *
     * @param  XT  Description of the Parameter
     * @return     Description of the Return Value
     */
    private static double RT35(double XT)
    {
        return (((1.0031E-9 * XT - 6.9698E-7) * XT + 1.104259E-4) * XT
                + 2.00564E-2) * XT + 0.6766097;
    }

    /**
     *  Description of the Method
     *
     * @param  XP  Description of the Parameter
     * @return     Description of the Return Value
     */
    private static double C(double XP)
    {
        return ((3.989E-15 * XP - 6.370E-10) * XP + 2.070E-5) * XP;
    }

    /**
     *  Description of the Method
     *
     * @param  XT  Description of the Parameter
     * @return     Description of the Return Value
     */
    private static double B(double XT)
    {
        return (4.464E-4 * XT + 3.426E-2) * XT + 1.0;
    }

    /**
     *  Description of the Method
     *
     * @param  XT  Description of the Parameter
     * @return     Description of the Return Value
     */
    private static double A(double XT)
    {
        return -3.107E-3 * XT + 0.4215;
    }

    /**
     *  Description of the Method
     *
     * @param  XR  Description of the Parameter
     * @param  XT  Description of the Parameter
     * @return     Description of the Return Value
     */
    private static double SAL(double XR, double XT)
    {
        return ((((2.7081 * XR - 7.0261) * XR + 14.0941) * XR + 25.3851) * XR
                - 0.1692) * XR
                + 0.0080
                + (XT / (1.0 + 0.0162 * XT))
                * (((((-0.0144 * XR + 0.0636) * XR - 0.0375) * XR - 0.0066) * XR
                - 0.0056)
                * XR + 0.0005);
    }
}
