/*
 * IMOS data delivery project
 * Written by Peter Wiley
 * This code is copyright (c) Peter Wiley 2000 - ?
 * It is made available under the BSD Software Licence in the hope that it may be useful.
 * It has NO WARRANTY OF FITNESS OR SUITABILITY FOR ANY PURPOSE.
 * Feel free to fix any bugs that you may find.
 */

package org.imos.abos.calc;

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
public class SalinityCalculatorTest
{

    public SalinityCalculatorTest()
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
        System.out.println("calculateSalinityForITS90Temperature");
        
        //double result = SalinityCalculator.calculateSalinityForITS68Temperature(temperature, conductivity, pressure);
        //assertEquals(expResult, result, 0.0);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
        //
        // some real data....
        //
        double[] Pressure = new double[] {     10,      50,     125,     250,    600,   1000};
        double[] Conductivity = new double[] {34.5487, 34.7275, 34.8605, 34.6810, 34.5680, 34.5600};
        double[] Temperature = new double[] {28.7856, 28.4329, 22.8103, 10.2600, 6.8863, 4.4036};

        double[] expectedResult = new double[] { 20.009869599086951,
                                                20.265511864874270,
                                                22.981513062527689,
                                                31.204503263727982,
                                                34.032315787432829,
                                                36.400308494388170
                                                };

        for (int i = 0; i < 6; i++)
        {
            double result = SalinityCalculator.calculateSalinityForITS90Temperature(Temperature[i], Conductivity[i], Pressure[i]);
            assertEquals(expectedResult[i], result, 0.00000001);
        }
    }

}
