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
import org.imos.abos.dbms.Instrument;
import org.imos.abos.dbms.InstrumentCalibrationFile;
import org.imos.abos.dbms.Mooring;
import org.imos.abos.instrument.AanderraOptodeConstants;
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
public class AanderaaOxygenCalculatorTest
{

    public AanderaaOxygenCalculatorTest()
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
     * Test of calculateOxygenValueInMlPerLitre method, of class SeabirdSBE43OxygenCalculator.
     */
    @Test
    public void testCalculateOxygenValue()
    {
        AanderraOptodeOxygenCalculator aooc = new AanderraOptodeOxygenCalculator();
        
        AanderraOptodeConstants aoc = new AanderraOptodeConstants();
        
        aoc.setInstrumentAndMooring(Instrument.selectByInstrumentID(620), Mooring.selectByMooringID("PULSE_7")); // Optode-1161, Pulse-7
        
        aooc.setOptodeConstants(aoc);
        
        System.out.println("Constants " + aoc);
        
        double dox = aooc.calculateDissolvedOxygenInUMolesPerKg(8.707, 37.36512725, 34.56124872, 34.387); // Values from 2010-10-01 00:00
        
        System.out.println("DO = " + dox);
        
        assertEquals(274.3184975, dox, 0.1);
    }

}
