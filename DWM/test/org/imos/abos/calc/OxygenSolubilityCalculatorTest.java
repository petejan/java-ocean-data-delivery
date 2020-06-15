/*
 * IMOS data delivery project
 * Written by Peter Wiley
 * This code is copyright (c) Peter Wiley 2000 - ?
 * It is made available under the BSD Software Licence in the hope that it may be useful.
 * It has NO WARRANTY OF FITNESS OR SUITABILITY FOR ANY PURPOSE.
 * Feel free to fix any bugs that you may find.
 */

package org.imos.abos.calc;

import java.util.ArrayList;
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
public class OxygenSolubilityCalculatorTest
{

    ArrayList<testRow> values = new ArrayList();

    public OxygenSolubilityCalculatorTest()
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
        addRow(10.0, 35.00, 6.315);

        addRow(-2.00, 0.00, 10.84);
        addRow(0.00, 0.00, 10.23);
        addRow(2.00, 0.00, 9.68);
        addRow(4.00, 0.00, 9.17);
        addRow(6.00, 0.00, 8.71);
        addRow(8.00, 0.00, 8.29);
        addRow(10.00, 0.00, 7.90);
        addRow(12.00, 0.00, 7.54);
        addRow(14.00, 0.00, 7.21);
        addRow(16.00, 0.00, 6.91);
        addRow(18.00, 0.00, 6.62);
        addRow(20.00, 0.00, 6.36);
        addRow(22.00, 0.00, 6.12);
        addRow(24.00, 0.00, 5.89);
        addRow(26.00, 0.00, 5.68);
        addRow(28.00, 0.00, 5.48);
        addRow(30.00, 0.00, 5.29);
        addRow(32.00, 0.00, 5.11);

        addRow(-2.00, 15.00, 9.74);
        addRow(0.00, 15.00, 9.21);
        addRow(2.00, 15.00, 8.73);
        addRow(4.00, 15.00, 8.29);
        addRow(6.00, 15.00, 7.89);
        addRow(8.00, 15.00, 7.52);
        addRow(10.00, 15.00, 7.18);
        addRow(12.00, 15.00, 6.86);
        addRow(14.00, 15.00, 6.57);
        addRow(16.00, 15.00, 6.31);
        addRow(18.00, 15.00, 6.06);
        addRow(20.00, 15.00, 5.82);
        addRow(22.00, 15.00, 5.61);
        addRow(24.00, 15.00, 5.41);
        addRow(26.00, 15.00, 5.22);
        addRow(28.00, 15.00, 5.04);
        addRow(30.00, 15.00, 4.87);
        addRow(32.00, 15.00, 4.71);

        addRow(-2.00, 35.00, 8.45);
        addRow(0.00, 35.00, 8.01);
        addRow(2.00, 35.00, 7.61);
        addRow(4.00, 35.00, 7.24);
        addRow(6.00, 35.00, 6.91);
        addRow(8.00, 35.00, 6.60);
        addRow(10.00, 35.00, 6.31);
        addRow(12.00, 35.00, 6.05);
        addRow(14.00, 35.00, 5.81);
        addRow(16.00, 35.00, 5.58);
        addRow(18.00, 35.00, 5.37);
        addRow(20.00, 35.00, 5.17);
        addRow(22.00, 35.00, 4.99);
        addRow(24.00, 35.00, 4.82);
        addRow(26.00, 35.00, 4.66);
        addRow(28.00, 35.00, 4.51);
        addRow(30.00, 35.00, 4.36);
        addRow(32.00, 35.00, 4.23);

        addRow(10.0, 35.00, 6.315);
    }

    private void addRow(Double t, Double s, Double o)
    {
        testRow row = new testRow();
        row.temp = t;
        row.salinity = s;
        row.oxySol = o;

        values.add(row);
    }

    @After
    public void tearDown()
    {
    }

    /**
     * Test of calculateOxygenSolubilityInMlPerLitre method, of class OxygenSolubilityCalculator.
     */
    @Test
    public void testCalculateOxygenSolubility()
    {
        System.out.println("calculateOxygenSolubility");
        
        for (int i = 0; i < values.size(); i++)
        {
            testRow t = values.get(i);
            System.out.println("Testing values t:" + t.temp + " s: " + t.salinity + " oxySol: " + t.oxySol);

            Double result = OxygenSolubilityCalculator.calculateOxygenSolubilityInMlPerLitre(t.temp, t.salinity);
            assertEquals(t.oxySol, result, 0.01);
        }
        
        System.out.println("Testing  uM/kg with values t:" + 10.0 + " s: " + 35.00 + " oxySol: " + 274.610);
        Double result = OxygenSolubilityCalculator.calculateOxygenSolubilityInUMolesPerKg(10.0, 35.00);
        assertEquals(274.610, result, 0.001);
    }

    private class testRow
    {
        public Double temp;
        public Double salinity;
        public Double oxySol;
    }

}
