/*
 * IMOS Data Delivery Project
 * Written by Peter Jansen
 * This code is copyright (c) Peter Jansen 2000 - ?
 * It is made available under the BSD Software Licence in the hope that it may be useful.
 * It has NO WARRANTY OF FITNESS OR SUITABILITY FOR ANY PURPOSE.
 * Feel free to fix any bugs that you may find.
 */

/* Fixup class to read Optode voltage from SBE16, output from the aanderaa as Oxygen, and back calculate the BPhase value */

package org.imos.abos.calc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
import org.apache.commons.math3.analysis.solvers.NewtonRaphsonSolver;
import org.apache.log4j.PropertyConfigurator;
import org.imos.abos.dbms.Instrument;
import org.imos.abos.dbms.InstrumentCalibrationFile;
import org.imos.abos.dbms.Mooring;
import org.imos.abos.dbms.RawInstrumentData;
import org.wiley.core.Common;
import org.wiley.util.StringUtilities;

/**
 *
 * @author Peter Jansen <peter.jansen@utas.edu.au>
 */
public class AanderaaOptodeOxygen2BPhase
{
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AanderaaOptodeOxygen2BPhase.class.getName());

    private Mooring selectedMooring =null;
    private InstrumentCalibrationFile selectedFile = null;
    private Instrument sourceInstrument = null;
    private Instrument targetInstrument = null;
    
    double vTemp;
    double vOxygen;
    double o;
    double t;
    
    public AanderaaOptodeOxygen2BPhase()
    {
    
    }
    public AanderaaOptodeOxygen2BPhase(double nt, double no)
    {
        setVolt(nt, no);
    }

    public void setVolt(double nt, double no)
    {
        vTemp = nt;
        vOxygen = no;

        o = vOxygen * 100;
        t = vTemp * 9 - 5;
        
        //System.out.println("Temp = " + t + " oyxgen " + o);    
        
    }
//PhaseCoef	  3830	  1420	-6.566452E+00	 1.187430E+00	 0.000000E+00	 0.000000E+00
//TempCoef	  3830	  1420	 2.217112E+01	-3.046009E-02	 2.810936E-06	-4.137151E-09
//FoilNo	  3830	  1420	  5009
//C0Coef	  3830	  1420	 4.537931E+03	-1.625950E+02	 3.295740E+00	-2.792849E-02
//C1Coef	  3830	  1420	-2.509530E+02	 8.023220E+00	-1.583980E-01	 1.311410E-03
//C2Coef	  3830	  1420	 5.664169E+00	-1.596469E-01	 3.079099E-03	-2.462650E-05
//C3Coef	  3830	  1420	-5.994490E-02	 1.483260E-03	-2.821099E-05	 2.151560E-07
//C4Coef	  3830	  1420	 2.436140E-04	-5.267590E-06	 1.000640E-07	-7.143200E-10

    
//MEASUREMENT	  3830	  1420	Oxygen: 	   247.94	Saturation: 	    91.36	Temperature: 	    22.25	DPhase: 	    31.46	BPhase: 	    32.02	RPhase: 	     0.00	BAmp: 	   264.89	BPot: 	     0.00	RAmp: 	     0.00	RawTem.: 	    -2.66	
// 0-5V  Output 1: Oxygen           2.479 V, use scaling coef. A:= 0.000000E+00 B:= 1.000000E+02
// 0-5V  Output 2: Temperature      3.028 V, use scaling coef. A:=-5.000000E+00 B:= 9.000000E+00

    public double calcOxygen()
    {
        //double[] c0coef = {4.537931E+03, -1.625950E+02, 3.295740E+00, -2.792849E-02};
        //double[] c1coef = {-2.509530E+02, 8.023220E+00, -1.583980E-01, 1.311410E-03};
        //double[] c2coef = {5.664169E+00, -1.596469E-01, 3.079099E-03, -2.462650E-05};
        //double[] c3coef = {-5.994490E-02, 1.483260E-03, -2.821099E-05, 2.151560E-07};
        //double[] c4coef = {2.436140E-04, -5.267590E-06, 1.000640E-07, -7.143200E-10};
        
        // SN1161 2008-05-30
        double[] c0coef = {5.27602E+03, -1.78336E+02, 3.60337E+00, -3.17257E-02};        
        double[] c1coef = {-2.83515E+02, 8.53926E+00, -1.70712E-01, 1.51927E-03};        
        double[] c2coef = {6.14613E+00, -1.62949E-01, 3.25579E-03, -2.94146E-05};        
        double[] c3coef = {-6.20004E-02, 1.43629E-03, -2.90879E-05, 2.67188E-07};
        double[] c4coef = {2.39283E-04, -4.79250E-06, 1.00060E-07, -9.33184E-10};
        
        PolynomialFunction c0p = new PolynomialFunction(c0coef);
        PolynomialFunction c1p = new PolynomialFunction(c1coef);
        PolynomialFunction c2p = new PolynomialFunction(c2coef);
        PolynomialFunction c3p = new PolynomialFunction(c3coef);
        PolynomialFunction c4p = new PolynomialFunction(c4coef);

        double[] c = {c0p.value(t)-o, c1p.value(t), c2p.value(t), c3p.value(t), c4p.value(t)};
        
        PolynomialFunction dp2oxygen = new PolynomialFunction(c);
        
        NewtonRaphsonSolver sol = new NewtonRaphsonSolver();
        
        dp = sol.solve(10, dp2oxygen, 30); // find zero in dphase poly for this oyxgen value

        //System.out.println("eval " + sol.getEvaluations());
        
        //System.out.println("calc dphase = " + dp);
        
        return dp;
    }
    
    double dp;
    
    double calcBPhase()
    {
        NewtonRaphsonSolver sol = new NewtonRaphsonSolver();
        
        //double[] phaseCoef = {-6.566452E+00 - dp, 1.187430E+00, 0.000000E+00, 0.000000E+00};
        
        // SN1161 2009-02-24
        double[] phaseCoef = {8.94295E-01 - dp, 1.10633E00, 0.00000E00, 0.00000E00};
        
        PolynomialFunction phasePoly = new PolynomialFunction(phaseCoef); // don't really nead to use this as its a 1st order function, but its clearer
        
        double bp = sol.solve(10, phasePoly, 30); // find the bphase value, for the DPhase value
        
        //System.out.println("eval " + sol.getEvaluations());
        
        //System.out.println("dphase = " + dp + " bphase " + bp);
        
        return bp;
    }

    public void calculateDataValues()
    {
        Connection conn = null;
        Statement proc = null;
        ResultSet results = null;
        //selectedMooring = Mooring.selectByMooringID("Pulse-10-2013");
        //sourceInstrument = Instrument.selectByInstrumentID(740); // SBE16 6330
        //targetInstrument = Instrument.selectByInstrumentID(855); // Optode 1420
        selectedMooring = Mooring.selectByMooringID("Pulse-6-2009");
        sourceInstrument = Instrument.selectByInstrumentID(4); // SBE16 6331
        targetInstrument = Instrument.selectByInstrumentID(620); // Optode 1161
        SimpleDateFormat ts = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        ts.setTimeZone(TimeZone.getTimeZone("UTC"));

        try
        {
            String tab;
            conn = Common.getConnection();
            conn.setAutoCommit(false);
            proc = conn.createStatement();
            
            tab = "SELECT data_timestamp, source_file_id, depth, parameter_value as optode_volt INTO TEMP aanderra FROM raw_instrument_data WHERE parameter_code = 'VOLT5' AND mooring_id = "+StringUtilities.quoteString(selectedMooring.getMooringID())+" AND instrument_id = "+ sourceInstrument.getInstrumentID()+" ORDER BY data_timestamp";           
                
            proc.execute(tab);            
            tab = "ALTER TABLE aanderra ADD optode_temp  numeric";
            proc.execute(tab);            
            tab = "UPDATE aanderra SET optode_temp = d.parameter_value FROM raw_instrument_data d WHERE d.data_timestamp = aanderra.data_timestamp AND parameter_code = 'VOLT6' AND instrument_id = "+ sourceInstrument.getInstrumentID();
                
            proc.execute(tab);
            
            proc.execute("SELECT data_timestamp, source_file_id, depth, optode_temp, optode_volt FROM aanderra");
            results = (ResultSet) proc.getResultSet();
            ResultSetMetaData resultsMetaData = results.getMetaData();
            int colCount        = resultsMetaData.getColumnCount();

            boolean nullData;
            while (results.next())
            {
                Vector data = new Vector();

                nullData = false;
                for ( int numcol = 1; numcol <= colCount; numcol++ )
                {
                    Object o = new Object();
                    o        = results.getObject(numcol);
                    if ( ! results.wasNull() )
                    {
                        data.addElement( o );
                    }
                    else
                    {
                        data.addElement( null );
                        nullData = true;
                    }
                }

                if (!nullData)
                {
                    optodeData row = new optodeData();
                    row.setData(data);

                    System.out.printf("%s , OBP,%6.3f ,OTEMP,%6.3f, OXY,%6.2f\n", ts.format(row.dataTimestamp), row.optodeBPhaseValue, row.optodeTemperatureValue, row.optodeVoltValue);
                    
                    RawInstrumentData rid = new RawInstrumentData();

                    rid.setDataTimestamp(row.dataTimestamp);
                    rid.setDepth(row.instrumentDepth);
                    rid.setInstrumentID(targetInstrument.getInstrumentID());
                    rid.setLatitude(selectedMooring.getLatitudeIn());
                    rid.setLongitude(selectedMooring.getLongitudeIn());
                    rid.setMooringID(selectedMooring.getMooringID());
                    rid.setSourceFileID(row.sourceFileID);
                    rid.setQualityCode("DERIVED");
                    rid.setParameterCode("OPTODE_BPHASE");
                    rid.setParameterValue(row.optodeBPhaseValue);

                    boolean ok = rid.insert();                    
                    
                    rid.setParameterCode("OPTODE_TEMP");
                    rid.setParameterValue(row.optodeTemperatureValue);

                    ok = rid.insert();                    
                }
            }

            proc.execute("DROP Table aanderra");

            results.close();
            proc.close();
            conn.setAutoCommit(true);
        }
        catch(SQLException sex)
        {
            logger.error(sex);
            if (conn != null)
            {
                try
                {
                    conn.rollback();
                    conn.setAutoCommit(true);
                }
                catch (SQLException ex)
                {
                    logger.error(sex);
                }
            }
        }
        finally
        {
            try
            {
                if(results != null)
                    results.close();
                if(proc != null)
                    proc.close();
            }
            catch(SQLException sex)
            {
                logger.error(sex);
            }
        }
    }
    
    
    static public void main(String[] args)
    {
        String $HOME = System.getProperty("user.home");
        PropertyConfigurator.configure("log4j.properties");
        Common.build("ABOS.properties");

        BufferedReader input = null;
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        SimpleDateFormat dateParser = new SimpleDateFormat("dd MMM yyyy HH:mm:ss");
        SimpleDateFormat ts = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        AanderaaOptodeOxygen2BPhase opb = new AanderaaOptodeOxygen2BPhase();
        try
        {
            if (args.length == 0)
            {
                opb.calculateDataValues();
            }
            else
            {
                input = new BufferedReader(new FileReader(new File(args[0])));
                while(input.ready())
                {
                    String l = input.readLine();
                    String[] split = l.split(",");
                    if (split.length == 12)
                    {
                        try
                        {
                            Date d = dateParser.parse(split[11]);

                            double vOxy = Double.parseDouble(split[7]);
                            double vOTemp = Double.parseDouble(split[8]);

                            opb.setVolt(vOTemp, vOxy);

                            opb.calcOxygen();
                            double bp = opb.calcBPhase();

                            System.out.printf("%s ,TEMP,%s ,CNDC,%s ,PRES,%s ,OBP,%6.3f ,OTEMP,%6.3f, OXY,%6.2f\n", ts.format(d), split[0].trim(), split[1].trim(), split[2].trim(), bp, opb.t, opb.o);
                        }
                        catch (ParseException ex)
                        {
                            Logger.getLogger(AanderaaOptodeOxygen2BPhase.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }

                }
            }
        }
        catch (FileNotFoundException ex)
        {
            Logger.getLogger(AanderaaOptodeOxygen2BPhase.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (IOException ex)
        {
            Logger.getLogger(AanderaaOptodeOxygen2BPhase.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private class optodeData
    {
        public Timestamp dataTimestamp;
        public Integer sourceFileID;
        public Double instrumentDepth;

        public Double optodeTemperatureVoltValue;
        public Double optodeVoltValue;
        
        public Double optodeBPhaseValue;
        public Double optodeTemperatureValue;

        public void setData(Vector row)
        {
            int i = 0;

            dataTimestamp = (Timestamp) row.elementAt(i++);
            sourceFileID = ((Number)row.elementAt(i++)).intValue();
            instrumentDepth = ((Number)row.elementAt(i++)).doubleValue();

            optodeTemperatureVoltValue = ((Number)row.elementAt(i++)).doubleValue();
            optodeVoltValue = ((Number)row.elementAt(i++)).doubleValue();           
            
            setVolt(optodeTemperatureVoltValue, optodeVoltValue);
            calcOxygen();
                        
            optodeTemperatureValue = t;
            optodeBPhaseValue = calcBPhase();
        }
    }
    
}
