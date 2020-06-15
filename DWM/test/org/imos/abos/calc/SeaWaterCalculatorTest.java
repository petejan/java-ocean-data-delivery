/*
 * IMOS data delivery project
 * Written by Peter Wiley
 * This code is copyright (c) Peter Wiley 2000 - ?
 * It is made available under the BSD Software Licence in the hope that it may be useful.
 * It has NO WARRANTY OF FITNESS OR SUITABILITY FOR ANY PURPOSE.
 * Feel free to fix any bugs that you may find.
 */

package org.imos.abos.calc;

import org.geotools.nature.SeaWater;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author peter
 */
public class SeaWaterCalculatorTest
{

    public SeaWaterCalculatorTest()
    {
    }

    @BeforeClass
    public static void setUpClass() throws Exception
    {
    }

    @AfterClass
    public static void tearDownClass() throws Exception
    {
    }

    @Before
    public void setUp()
    {
    }

    @After
    public void tearDown()
    {
    }

    /**
     * Test of calculateSalinityForITS68Temperature method, of class SalinityCalculator.
     */
    @Test
    public void testCalculateSalinityForT90Temperature()
    {
        System.out.println("SeaWater Parameters");
        
        //pressure [dbar] = 1000.000
        //temperature [IPTS-68, deg C] = 15.003600
        //temperature [ITS-90, deg C] = 15.000000
        //Conductivity [S m^-1] = 4.334962
        //Practical Salinity [PSU] = 35.00000
        //reference pressure [dbar] = 0.00
        //latitude [deg] = -46.0
        //depth [salt water, m] = 989.409
        //depth [fresh water, m] = 1019.716
        //density [sigma-t, kg m^-3] = 25.97196
        //density [sigma-theta, kg m^-3] = 26.00594
        //density [sigma-ref p, kg m^-3] = 26.00594
        //potential temperature [IPTS-68, deg C] = 14.84846
        //sound velocity [Chen-Millero, m s^-1] = 1523.250
        //sound velocity [Wilson, m s^-1] = 1523.768
        //sound velocity [Delgrosso, m s^-1] = 1523.069
        //specific volume anomaly [10^-8 * m^3 kg^-1] = 230.570
        //oxygen saturation, Weiss [ml l^-1] = 5.688
        //gravity [m s^-2] = 9.807095
        
        double result = SalinityCalculator.calculateSalinityForITS90Temperature(15.0, 4.334962*10.0, 1000.000);
        result = SeaWater.salinity(43.34962, 15.003600, 1000.000);
        double expr = 35.0;
        System.out.println("SeaWaterCalculatorTest::Salinity " + result + " expected " + expr + " error " + (result - expr));        
        assertEquals(expr, result, 1.0e-5);

        result = SeawaterParameterCalculator.PoTemp(35.0, 15.003600, 1000.0, 0.0);
        expr = 14.84846;
        System.out.println("SeaWaterCalculatorTest::PoTemp " + result + " expected " + expr + " error " + (result - expr));
        assertEquals(expr, result, 1.0e-5);

        result = SeaWater.depth(1000.0, Math.toRadians(-46.0));
        expr = 989.409;
        System.out.println("SeaWaterCalculatorTest::depth " + result + " expected " + expr + " error " + (result - expr));
        assertEquals(expr, result, 1.0e-3);

        // Sea-Bird Pulse-7 processed data:
        //# name 0 = tv290C: Temperature [ITS-90, deg C]
        //# name 1 = c0S/m: Conductivity [S/m]
        //# name 2 = sal00: Salinity, Practical [PSU]
        //# name 3 = prdM: Pressure, Strain Gauge [db]
        //# name 4 = density00: Density [density, kg/m^3]
        //# name 5 = sigma-é00: Density [sigma-theta, kg/m^3]
        //# name 6 = depSM: Depth [salt water, m], lat = -46.00
        //# name 7 = sbeox0V: Oxygen raw, SBE 43 [V]
        //# name 8 = sbeox0ML/L: Oxygen, SBE 43 [ml/l]
        //# name 9 = sbeox0Mm/Kg: Oxygen, SBE 43 [umol/kg]
        //# name 10 = oxsatML/L: Oxygen Saturation, Weiss [ml/l]
        //# name 11 = oxsatMm/Kg: Oxygen Saturation, Weiss [umol/kg]
        //# name 12 = oxsolML/L: Oxygen Saturation, Garcia & Gordon [ml/l]
        //# name 13 = oxsolMm/Kg: Oxygen Saturation, Garcia & Gordon [umol/kg]
        //# name 14 = v1: Voltage 1
        //# name 15 = v2: Voltage 2
        //# name 16 = v3: Voltage 3
        //# name 17 = v4: Voltage 4
        //# name 18 = v5: Voltage 5
        //# name 19 = timeJV2: Time, Instrument [julian days]
        //# name 20 = timeK: Time, Instrument [seconds]
        //# name 21 = flag:  0.000e+00
        
        // 9.1252   3.693159    34.6362     33.788  1026.9666    26.8137     33.508     2.3618     5.8288    253.515    6.45683  280.83196    6.45175  280.61103     0.0130     0.1229     0.0752     1.9023     1.5716 264.500498  338385643  0.000e+00
        // 9.1303   3.693716    34.6370     33.765  1026.9663    26.8135     33.485     2.3637     5.8340    253.741    6.45606  280.79825    6.45099  280.57778     0.0131     0.1159     0.0743     1.9019     1.5729 264.542164  338389243  0.000e+00               
        
        result = SeaWater.density(34.6362, 9.1252*1.00024, 33.788);
        expr = 1026.9666;
        System.out.println("SeaWater.density " + result + " expected " + expr + " error " + (result - expr));
        assertEquals(expr, result, 0.0001);
        
        result = SeaWater.densitySigmaT(34.6362, SeawaterParameterCalculator.PoTemp(34.6362, 9.1252*1.00024, 33.788, 0), 0);
        expr = 26.8137;
        System.out.println("SeaWater.densitySigmaTheta " + result + " expected " + expr + " error " + (result - expr));
        assertEquals(expr, result, 0.0001);
        
        result = SeawaterParameterCalculator.calculateSeawaterDensityAtPressure(34.6362, 9.1252, 33.788);
        expr = 1026.9666;
        System.out.println("SeawaterParameterCalculator.calculateSeawaterDensityAtPressure " + result + " expected " + expr + " error " + (result - expr));
        assertEquals(expr, result, 0.0001);

        result = SeaWater.depth(33.788, Math.toRadians(-46.0));
        expr = 33.508;
        System.out.println("SeaWater.depth " + result + " expected " + expr + " error " + (result - expr));
        assertEquals(expr, result, 0.001);
             
        result = SeawaterParameterCalculator.depth(33.788, -46.0);
        expr = 33.508;
        System.out.println("SeawaterParameterCalculator.depth " + result + " expected " + expr + " error " + (result - expr));
        assertEquals(expr, result, 0.001);
               
        result = SeaWater.saturationO2(34.6362, 9.1252*1.00024);
        expr = 280.61103;
        System.out.println("SeaWater.saturationO2 " + result + " expected " + expr + " error " + (result - expr));
        assertEquals(expr, result, 0.001);
             

    }

}
