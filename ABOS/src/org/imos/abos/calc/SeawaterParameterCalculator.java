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
 */
public class SeawaterParameterCalculator
{

    public static double calculateSeawaterDensityAtSurface(double salinity, double temperature)
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

    /**
     * calculate the density of seawater at the specified depth (pressure)
     * @param salinity
     * @param temperature
     * @param pressure
     * @return
     */
    public static Double calculateSeawaterDensityAtDepth(Double salinity, Double temperature, Double pressure)
    {
        /*
         % SW_DENS    Density of sea water
        %=========================================================================
        % SW_DENS  $Id: sw_dens.m,v 1.1 2003/12/12 04:23:22 pen078 Exp $
        %          Copyright (C) CSIRO, Phil Morgan 1992.
        %
        % USAGE:  dens = sw_dens(S,T,P)
        %
        % DESCRIPTION:
        %    Density of Sea Water using UNESCO 1983 (EOS 80) polynomial.
        %
        % INPUT:  (all must have same dimensions)
        %   S = salinity    [psu      (PSS-78)]
        %   T = temperature [degree C (ITS-90)]
        %   P = pressure    [db]
        %       (P may have dims 1x1, mx1, 1xn or mxn for S(mxn) )
        %
        % OUTPUT:
        %   dens = density  [kg/m^3]
        %
        % AUTHOR:  Phil Morgan 92-11-05, Lindsay Pender (Lindsay.Pender@csiro.au)
        %
        % DISCLAIMER:
        %   This software is provided "as is" without warranty of any kind.
        %   See the file sw_copy.m for conditions of use and licence.
        %
        % REFERENCES:
        %    Fofonoff, P. and Millard, R.C. Jr
        %    Unesco 1983. Algorithms for computation of fundamental properties of
        %    seawater, 1983. _Unesco Tech. Pap. in Mar. Sci._, No. 44, 53 pp.
        %
        %    Millero, F.J., Chen, C.T., Bradshaw, A., and Schleicher, K.
        %    " A new high pressure equation of state for seawater"
        %    Deap-Sea Research., 1980, Vol27A, pp255-264.
        %=========================================================================

        Java conversion Peter Wiley, UTAS 2012-04-15
         */
        Double densP0 = sw_dens0(salinity,temperature);
        Double K      = sw_seck(salinity,temperature,pressure);
        pressure      = pressure/10;  // convert from db to atm pressure units
        Double dens   = densP0/(1-pressure/K);

        return dens;
    }

    private static Double sw_seck(Double salinity, Double temperature, Double pressure)
    {
        /*
        % SW_SECK    Secant bulk modulus (K) of sea water
        %=========================================================================
        % SW_SECK  $Id: sw_seck.m,v 1.1 2003/12/12 04:23:22 pen078 Exp $
        %          Copyright (C) CSIRO, Phil Morgan 1992.
        %
        % USAGE:  dens = sw_seck(S,T,P)
        %
        % DESCRIPTION:
        %    Secant Bulk Modulus (K) of Sea Water using Equation of state 1980.
        %    UNESCO polynomial implementation.
        %
        % INPUT:  (all must have same dimensions)
        %   S = salinity    [psu      (PSS-78) ]
        %   T = temperature [degree C (ITS-90)]
        %   P = pressure    [db]
        %       (alternatively, may have dimensions 1*1 or 1*n where n is columns in S)
        %
        % OUTPUT:
        %   K = Secant Bulk Modulus  [bars]
        %
        % AUTHOR:  Phil Morgan 92-11-05, Lindsay Pender (Lindsay.Pender@csiro.au)
        %
        % DISCLAIMER:
        %   This software is provided "as is" without warranty of any kind.
        %   See the file sw_copy.m for conditions of use and licence.
        %
        % REFERENCES:
        %    Fofonoff, P. and Millard, R.C. Jr
        %    Unesco 1983. Algorithms for computation of fundamental properties of
        %    seawater, 1983. _Unesco Tech. Pap. in Mar. Sci._, No. 44, 53 pp.
        %    Eqn.(15) p.18
        %
        %    Millero, F.J. and  Poisson, A.
        %    International one-atmosphere equation of state of seawater.
        %    Deep-Sea Res. 1981. Vol28A(6) pp625-629.
        %=========================================================================

        % Modifications
        % 99-06-25. Lindsay Pender, Fixed transpose of row vectors.
        % 03-12-12. Lindsay Pender, Converted to ITS-90.

        Java conversion Peter Wiley, UTAS 2012-04-15

        */



        pressure = pressure/10;  //convert from db to atmospheric pressure units
        Double T68 = temperature * 1.00024;

        // Pure water terms of the secant bulk modulus at atmos pressure.
        // UNESCO eqn 19 p 18

        double h3 = -5.77905E-7;
        double h2 = +1.16092E-4;
        double h1 = +1.43713E-3;
        double h0 = +3.239908;

        double AW  = h0 + (h1 + (h2 + h3*T68)*T68)*T68;

        double k2 =  5.2787E-8;
        double k1 = -6.12293E-6;
        double k0 =  +8.50935E-5;

        double BW  = k0 + (k1 + k2*T68)*T68;

        double e4 = -5.155288E-5;
        double e3 = +1.360477E-2;
        double e2 = -2.327105;
        double e1 = +148.4206;
        double e0 = 19652.21;

        double KW  = e0 + (e1 + (e2 + (e3 + e4*T68)*T68)*T68)*T68;   // eqn 19

        //--------------------------------------------------------------------
        // SEA WATER TERMS OF SECANT BULK MODULUS AT ATMOS PRESSURE.
        //--------------------------------------------------------------------

        double j0 = 1.91075E-4;

        double i2 = -1.6078E-6;
        double i1 = -1.0981E-5;
        double i0 =  2.2838E-3;

        double SR = Math.sqrt(salinity);

        double A  = AW + (i0 + (i1 + i2*T68)*T68 + j0*SR)*salinity;


        double m2 =  9.1697E-10;
        double m1 = +2.0816E-8;
        double m0 = -9.9348E-7;

        double B = BW + (m0 + (m1 + m2*T68)*T68)*salinity;   // eqn 18

        double f3 =  -6.1670E-5;
        double f2 =  +1.09987E-2;
        double f1 =  -0.603459;
        double f0 = +54.6746;

        double g2 = -5.3009E-4;
        double g1 = +1.6483E-2;
        double g0 = +7.944E-2;

        double K0 = KW + (
                            f0
                            + (f1
                            + (f2 + f3*T68)
                            *T68)
                            *T68
                            + (g0
                            + (g1 + g2*T68)*T68)
                            *SR
                         )*salinity;      // eqn 16

        Double K = K0 + (A + B*pressure)*pressure;  // eqn 15

        return K;
    }

    private static Double sw_dens0(Double salinity, Double temperature)
    {
        /*
        % SW_DENS0   Denisty of sea water at atmospheric pressure
        %=========================================================================
        % SW_DENS0  $Id: sw_dens0.m,v 1.1 2003/12/12 04:23:22 pen078 Exp $
        %           Copyright (C) CSIRO, Phil Morgan 1992
        %
        % USAGE:  dens0 = sw_dens0(S,T)
        %
        % DESCRIPTION:
        %    Density of Sea Water at atmospheric pressure using
        %    UNESCO 1983 (EOS 1980) polynomial.
        %
        % INPUT:  (all must have same dimensions)
        %   S = salinity    [psu      (PSS-78)]
        %   T = temperature [degree C (ITS-90)]
        %
        % OUTPUT:
        %   dens0 = density  [kg/m^3] of salt water with properties S,T,
        %           P=0 (0 db gauge pressure)
        %
        % AUTHOR:  Phil Morgan 92-11-05, Lindsay Pender (Lindsay.Pender@csiro.au)
        %
        % DISCLAIMER:
        %   This software is provided "as is" without warranty of any kind.
        %   See the file sw_copy.m for conditions of use and licence.
        %
        % REFERENCES:
        %     Unesco 1983. Algorithms for computation of fundamental properties of
        %     seawater, 1983. _Unesco Tech. Pap. in Mar. Sci._, No. 44, 53 pp.
        %
        %     Millero, F.J. and  Poisson, A.
        %     International one-atmosphere equation of state of seawater.
        %     Deep-Sea Res. 1981. Vol28A(6) pp625-629.
        %=========================================================================

        % Modifications
        % 03-12-12. Lindsay Pender, Converted to ITS-90.

        Java conversion Peter Wiley, UTAS 2012-04-15

        */



        double T68 = temperature * 1.00024;

        //     UNESCO 1983 eqn(13) p17.

        double b0 =  8.24493e-1;
        double b1 = -4.0899e-3;
        double b2 =  7.6438e-5;
        double b3 = -8.2467e-7;
        double b4 =  5.3875e-9;

        double c0 = -5.72466e-3;
        double c1 = +1.0227e-4;
        double c2 = -1.6546e-6;

        double d0 = 4.8314e-4;

        double dens = sw_smow(temperature)
                        + (b0
                        + (b1 + (b2 + (b3 + b4*T68)*T68)*T68)*T68)*salinity
                        + (c0 + (c1 + c2*T68)*T68)*salinity*Math.sqrt(salinity) + d0*Math.pow(salinity, 2);

        return dens;
    }

}
