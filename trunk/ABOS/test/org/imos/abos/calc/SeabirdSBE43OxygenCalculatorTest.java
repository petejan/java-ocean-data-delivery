/*
 * Neonatal Screening Software Project
 * Written by Peter Wiley
 * This code is copyright (c) Peter Wiley 2000 - ?
 * It is made available under the BSD Software Licence in the hope that it may be useful.
 * It has NO WARRANTY OF FITNESS OR SUITABILITY FOR ANY PURPOSE.
 * Feel free to fix any bugs that you may find.
 */

package org.imos.abos.calc;

import org.apache.log4j.PropertyConfigurator;
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
        Common.build($HOME + "/ABOS/ABOS.conf");
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
        InstrumentCalibrationFile calFile = InstrumentCalibrationFile.selectByDatafilePrimaryKey(100075);
        SeabirdSBE43Constants constants = new SeabirdSBE43Constants();
        constants.setInstrumentCalibrationFile(calFile);

        SeabirdSBE43OxygenCalculator.setSBE43Constants(constants);
        
        System.out.println("calculateOxygenValue");

        Double pressure = 1.0;

        double[] temperature = new double[] {2.00,  6.00, 12.00, 20.00, 26.00, 30.00,  6.00,  2.00, 12.00, 20.00, 26.00, 30.00};
        double[] salinity    = new double[] {0.00,  0.01,  0.01,  0.01,  0.01,  0.02,  0.01,  0.00,  0.01,  0.01,  0.01,  0.02};
        double[] voltage     = new double[] {0.750, 0.781, 0.831, 0.901, 0.957, 0.998, 1.466, 1.364, 1.626, 1.847, 2.032, 2.164};
        double[] expResult   = new double[] {1.23,  1.23,  1.24,  1.25,  1.26,  1.26,  4.10,  4.11,  4.11,  4.12,  4.14,  4.15};

        for(int i = 0; i < temperature.length; i++)
        {
            double result = SeabirdSBE43OxygenCalculator.calculateOxygenValueInMlPerLitre(temperature[i], pressure, salinity[i], voltage[i]);
            assertEquals(expResult[i], result, 0.01);
        }
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

}