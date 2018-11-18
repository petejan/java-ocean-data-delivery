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
import org.apache.log4j.PropertyConfigurator;
import org.imos.abos.dbms.Instrument;
import org.imos.abos.dbms.Mooring;
import org.imos.abos.instrument.AanderraOptodeConstants;
import static org.junit.Assert.assertEquals;
import org.junit.*;
import org.wiley.core.Common;

/**
 *
 * @author peter
 */
public class AanderaaOxygenCalculatorTest
{

    public AanderaaOxygenCalculatorTest()
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
     * Test of calculateOxygenValueInMlPerLitre method, of class SeabirdSBE43OxygenCalculator.
     */
//    @Test
//    public void testCalculateOxygenValue()
//    {
//        AanderraOptodeOxygenCalculator aooc = new AanderraOptodeOxygenCalculator();
//        
//        AanderraOptodeConstants aoc = new AanderraOptodeConstants();
//        
//        aoc.setInstrumentAndMooring(Instrument.selectByInstrumentID(620), Mooring.selectByMooringID("PULSE_7")); // Optode-1161, Pulse-7
//        
//        AanderraOptodeOxygenCalculator.setOptodeConstants(aoc);
//        
//        System.out.println("Constants " + aoc);
//        
//        double dox = AanderraOptodeOxygenCalculator.calculateDissolvedOxygenInUMolesPerKg(8.707, 37.36512725, 34.56124872, 34.387); // Values from 2010-10-01 00:00
//        
//        System.out.println("DO = " + dox);
//        
//        assertEquals(274.3184975, dox, 0.1);
//    }
    
    @Test
    public void testCalculateCSIROUchidaOxygenValue()
    {
        ArrayList<UchidaData> testSet = getUchidaTestData();
        
        AanderraOptodeConstants aoc = new AanderraOptodeConstants();
        
        aoc.setInstrumentAndMooring(Instrument.selectByInstrumentID(727), Mooring.selectByMooringID("PULSE_8")); // Optode-1419, Pulse-8
        //
        // override cal values for test sheet values
        // 
        aoc.UchidaC1Coefficient = 0.002558965;
        aoc.UchidaC2Coefficient = 0.0001136163;
        aoc.UchidaC3Coefficient = 1.895434E-006;
        aoc.UchidaC4Coefficient = 234.836;
        aoc.UchidaC5Coefficient = -0.2874212;
        aoc.UchidaC6Coefficient = -33.12026;
        aoc.UchidaC7Coefficient = 4.074909;
        
        AanderraOptodeOxygenCalculator.setOptodeConstants(aoc);
        
        System.out.println("Constants " + aoc);
        
        for(int i = 0; i < testSet.size(); i++)
        {
            UchidaData row = testSet.get(i);
            double dox = AanderraOptodeOxygenCalculator.UchidaCalculateDissolvedOxygenRaw(row.temperature, row.bPhase); 
        
            System.out.println("DO = " + dox);
        
            assertEquals(row.optode_O2, dox, 0.1);
        }
    }
    
    private ArrayList<UchidaData> getUchidaTestData()
    {
        ArrayList<UchidaData> set = new ArrayList();
        
        set.add(addRow(65.8, 0.38, -0.46));
        set.add(addRow(42.402, 0.374, 260.15 + 1.4712));
        set.add(addRow(35.283, 0.351, 431.15 + 0.2835));
        set.add(addRow(31.13, 0.368, 578.38 - 0.04058));
        set.add(addRow(30.93, 0.344, 587.89 - 0.45405));
        set.add(addRow(55.523, 0.374, 82.89 - 0.10416));
        set.add(addRow(65.128, 9.834, 0 - 0.29097));
        set.add(addRow(40.042, 9.825, 203.02 + 0.19857));
        
        set.add(addRow(32.91, 9.835, 336.86 - 0.70547));
        set.add(addRow(28.75, 9.813, 456.82 - 0.17849));
        set.add(addRow(28.12, 9.809, 479.44 - 0.15856));
        set.add(addRow(53.15, 9.809, 68.25 + 0.39055));
        set.add(addRow(64.411, 19.808, 0 - 0.16165));
        
        return set;
    }
    
    private UchidaData addRow(double bPhase, double temp, double O2)
    {
        UchidaData row = new UchidaData();
        
        row.bPhase = bPhase;
        row.temperature = temp;
        row.optode_O2 = O2;
        
        return row;
    }
    
    private class UchidaData
    {
        double bPhase;
        double temperature;
        
        double residual;
        double optode_O2;
    }

}
