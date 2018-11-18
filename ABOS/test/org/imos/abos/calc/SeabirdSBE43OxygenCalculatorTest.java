/*
 * IMOS data delivery project
 * Written by Peter Wiley
 * This code is copyright (c) Peter Wiley 2000 - ?
 * It is made available under the BSD Software Licence in the hope that it may be useful.
 * It has NO WARRANTY OF FITNESS OR SUITABILITY FOR ANY PURPOSE.
 * Feel free to fix any bugs that you may find.
 */

package org.imos.abos.calc;

import org.apache.log4j.PropertyConfigurator;
import org.geotools.nature.SeaWater;
import org.imos.abos.dbms.InstrumentCalibrationFile;
import org.imos.abos.instrument.SeabirdSBE43Constants;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.wiley.core.Common;
import static org.junit.Assert.*;

/**
 *
 * @author peter
 */
public class SeabirdSBE43OxygenCalculatorTest
{

    public SeabirdSBE43OxygenCalculatorTest()
    {
        String $HOME = System.getProperty("user.home");

        PropertyConfigurator.configure($HOME + "/ABOS/log4j.properties");
        Common.build("ABOS.properties");
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
     * Test of setSBE43Constants method, of class SeabirdSBE43OxygenCalculator.
     
    @Test
    public void testSetSBE43Constants()
    {
        System.out.println("setSBE43Constants");
        SeabirdSBE43Constants c = null;
        SeabirdSBE43OxygenCalculator.setSBE43Constants(c);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    */
    /**
     * Test of calculateOxygenValueInMlPerLitre method, of class SeabirdSBE43OxygenCalculator.
     */
    @Test
    public void testCalculateOxygenValue()
    {
        InstrumentCalibrationFile calFile = InstrumentCalibrationFile.selectByDatafilePrimaryKey(100209);
        SeabirdSBE43Constants constants = new SeabirdSBE43Constants();
        constants.setInstrumentCalibrationFile(calFile);

        SeabirdSBE43OxygenCalculator.setSBE43Constants(constants);
        
        System.out.println("calculateOxygenValue");

        double[] temperature = new double[] {2.00,  12.00, 20.00};
        double[] pressure = new double[] {0,  0, 0};
        double[] salinity    = new double[] {0.00,  0.01, 0.01};
        double[] voltage     = new double[] {0.750, 1.626, 2.728};
        double[] expResult   = new double[] {1.23,  4.11, 6.78};
        
        for(int i = 0; i < temperature.length; i++)
        {
            double result = SeabirdSBE43OxygenCalculator.calculateOxygenValueInMlPerLitre(temperature[i], pressure[i], salinity[i], voltage[i]);
            System.out.println("SBE43OxygenCalculatorTest::" + result + " expected " + expResult[i] + " error " + (result - expResult[i]));
            assertEquals(expResult[i], result, 0.01);
        }
        
        // From Sea-Bird AN64 Appendix 2
        double result = OxygenSolubilityCalculator.calculateOxygenSolubilityInMlPerLitre(10.0, 30.0);
        double expr = 6.52;
        System.out.println("OxygenSolubilityCalculator::calculateOxygenSolubilityInMlPerLitre " + result + " expected " + expr + " error " + (result - expr));
        assertEquals(expr, result, 0.001);
        
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
        
        result = SeabirdSBE43OxygenCalculator.calculateOxygenValueInUMolesPerKg(9.1252, 33.788, 34.6362, 2.3618);
        expr = 253.515;
        System.out.println("SBE43OxygenCalculatorTest::calculateOxygenValueInUMolesPerKg " + result + " expected " + expr + " error " + (result - expr));
        assertEquals(expr, result, 0.02);

        result = OxygenSolubilityCalculator.calculateOxygenSolubilityInMlPerLitre(9.1252, 34.6362);
        expr = 6.45175;
        System.out.println("OxygenSolubilityCalculator::calculateOxygenSolubilityInMlPerLitre " + result + " expected " + expr + " error " + (result - expr));
        assertEquals(expr, result, 0.0001);
        
        calFile = InstrumentCalibrationFile.selectByDatafilePrimaryKey(100224);
        constants.setInstrumentCalibrationFile(calFile);
        SeabirdSBE43OxygenCalculator.setSBE43Constants(constants);
        
        // Value from Pulse-9 SN1635 24-Sep-11 calibration
        result = SeabirdSBE43OxygenCalculator.calculateOxygenValueInMlPerLitre(20.0, 0.0, 0.06, 2.626);
        expr = 6.84;
        System.out.println("SBE43OxygenCalculatorTest::" + result + " expected " + expr + " error " + (result - expr));
        assertEquals(expr, result, 0.01);        
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

}
