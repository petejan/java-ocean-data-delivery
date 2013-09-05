/*
 * IMOS Data Delivery Project
 * Written by Peter Wiley
 * This code is copyright (c) Peter Wiley 2000 - ?
 * It is made available under the BSD Software Licence in the hope that it may be useful.
 * It has NO WARRANTY OF FITNESS OR SUITABILITY FOR ANY PURPOSE.
 * Feel free to fix any bugs that you may find.
 */

/*
 * AanderraOptodeCalculationForm.java
 *
 * Created on Mar 7, 2012, 11:09:39 AM
 */

package org.imos.abos.forms;

import java.awt.Color;
import java.io.IOException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.TimeZone;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.imos.abos.calc.AanderraOptodeOxygenCalculator;
import org.imos.abos.calc.OxygenSolubilityCalculator;
import org.imos.abos.calc.SalinityCalculator;
import org.imos.abos.dbms.Instrument;
import org.imos.abos.dbms.InstrumentCalibrationFile;
import org.imos.abos.dbms.Mooring;
import org.imos.abos.dbms.ProcessedInstrumentData;
import org.imos.abos.dbms.RawInstrumentData;
import org.imos.abos.instrument.AanderraOptodeConstants;
import org.wiley.core.Common;
import org.wiley.core.forms.MemoryWindow;
import org.wiley.util.StringUtilities;
import org.wiley.util.TextFileLogger;

/**
 *
 * @author peter
 */
public class AanderraOptodeCalculationForm extends MemoryWindow implements DataProcessor
{

    private static Logger logger = Logger.getLogger(AanderraOptodeCalculationForm.class.getName());

    private Mooring selectedMooring =null;
    private InstrumentCalibrationFile selectedFile = null;
    private Instrument sourceInstrument = null;
    private Instrument targetInstrument = null;

    private ArrayList<optodeData> dataSet = new ArrayList();
    private AanderraOptodeConstants constants = null;
    private String algo = "Uchida";
    
    /** Creates new form AanderraOptodeCalculationForm */
    public AanderraOptodeCalculationForm()
    {
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
    }

    @Override
    public void initialise()
    {
        initComponents();
        
        this.setVisible(true);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents()
    {

        buttonGroup1 = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel();
        mooringCombo1 = new org.imos.abos.dbms.fields.MooringCombo();
        mooringDescriptionField = new org.wiley.util.basicField();
        calibrationFileCombo1 = new org.imos.abos.dbms.fields.CalibrationFileCombo();
        sourceInstrumentCombo = new org.imos.abos.dbms.fields.InstrumentSelectorCombo();
        targetInstrumentCombo = new org.imos.abos.dbms.fields.InstrumentSelectorCombo();
        deleteDataBox = new javax.swing.JCheckBox();
        aanderraAlgorithmButton = new javax.swing.JRadioButton();
        csiroUchidaButton = new javax.swing.JRadioButton();
        jPanel2 = new javax.swing.JPanel();
        runButton = new javax.swing.JButton();
        quitButton = new javax.swing.JButton();

        setTitle("Aanderra Optode Data Processing Form");
        addWindowListener(new java.awt.event.WindowAdapter()
        {
            public void windowClosing(java.awt.event.WindowEvent evt)
            {
                formWindowClosing(evt);
            }
        });

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        mooringCombo1.setDescriptionField(mooringDescriptionField);
        mooringCombo1.setOrientation(0);
        mooringCombo1.addPropertyChangeListener(new java.beans.PropertyChangeListener()
        {
            public void propertyChange(java.beans.PropertyChangeEvent evt)
            {
                mooringCombo1PropertyChange(evt);
            }
        });

        mooringDescriptionField.setEnabled(false);

        calibrationFileCombo1.setLabel("Values From Calibration File");

        sourceInstrumentCombo.setLabel("Source Instrument");
        sourceInstrumentCombo.setOrientation(0);
        sourceInstrumentCombo.addPropertyChangeListener(new java.beans.PropertyChangeListener()
        {
            public void propertyChange(java.beans.PropertyChangeEvent evt)
            {
                sourceInstrumentComboPropertyChange(evt);
            }
        });

        targetInstrumentCombo.setLabel("Target Instrument");
        targetInstrumentCombo.setOrientation(0);

        deleteDataBox.setSelected(true);
        deleteDataBox.setText("Delete any existing processed data for target instrument");
        deleteDataBox.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                deleteDataBoxActionPerformed(evt);
            }
        });

        buttonGroup1.add(aanderraAlgorithmButton);
        aanderraAlgorithmButton.setText("Use Aanderra Supplied algorithm");

        buttonGroup1.add(csiroUchidaButton);
        csiroUchidaButton.setText("Use CSIRO/Uchida Supplied algorithm");
        csiroUchidaButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                csiroUchidaButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(calibrationFileCombo1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(deleteDataBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 398, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jPanel1Layout.createSequentialGroup()
                                .add(mooringCombo1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 169, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(mooringDescriptionField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 343, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(sourceInstrumentCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 562, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(targetInstrumentCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 562, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jPanel1Layout.createSequentialGroup()
                                .add(6, 6, 6)
                                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(csiroUchidaButton)
                                    .add(aanderraAlgorithmButton))))
                        .add(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, mooringCombo1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 21, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, mooringDescriptionField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(sourceInstrumentCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 26, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(targetInstrumentCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 26, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(18, 18, 18)
                .add(calibrationFileCombo1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(aanderraAlgorithmButton)
                .add(4, 4, 4)
                .add(csiroUchidaButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(deleteDataBox)
                .add(218, 218, 218))
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        runButton.setText("Run");
        runButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                runButtonActionPerformed(evt);
            }
        });
        jPanel2.add(runButton);

        quitButton.setText("Quit");
        quitButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                quitButtonActionPerformed(evt);
            }
        });
        jPanel2.add(quitButton);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(jPanel2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 606, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 261, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void quitButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_quitButtonActionPerformed
        cleanup();
    }//GEN-LAST:event_quitButtonActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        cleanup();
    }//GEN-LAST:event_formWindowClosing

    private void runButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_runButtonActionPerformed

        sourceInstrument = sourceInstrumentCombo.getSelectedInstrument();
        targetInstrument = targetInstrumentCombo.getSelectedInstrument();

        selectedMooring = mooringCombo1.getSelectedMooring();
        selectedFile = calibrationFileCombo1.getSelectedFile();
        if (aanderraAlgorithmButton.isSelected())
        {
            algo = "Aanderra";
        }

        if(selectedMooring == null)
        {
            Common.showMessage(this,"No Mooring Selected","You must select a mooring before running any calculations");
            return;
        }

        if(selectedFile == null)
        {
            Common.showMessage(this,"No File Selected","You must select a file before running any calculations");
            return;
        }

        if(deleteDataBox.isSelected())
        {
            ProcessedInstrumentData.deleteDataForMooringAndInstrument(selectedMooring.getMooringID(),
                                                                      targetInstrument.getInstrumentID())
                                                                      ;
            RawInstrumentData.deleteDataForMooringAndInstrumentAndParameter(selectedMooring.getMooringID(),
                                                                      targetInstrument.getInstrumentID(),
                                                                      "DOX2");
        }

        final Color bg = runButton.getBackground();
        runButton.setText("Running...");
        runButton.setBackground(Color.RED);
        runButton.setForeground(Color.WHITE);

        String insProc = "INSERT INTO instrument_data_processors (processors_pk, mooring_id, class_name, parameters, processing_date, display_code) VALUES ("
     + "nextval('instrument_data_processor_sequence'),"
     + "'" + selectedMooring.getMooringID() + "',"
     + "'" + this.getClass().getName() + "',"
     + "'" + paramToString() + "',"
     + "'" + Common.current() + "',"
     + "'Y'"
     + ")";

        Connection conn = Common.getConnection();

        Statement stmt;
        try
        {
           stmt = conn.createStatement();
           stmt.executeUpdate(insProc);            
        }
        catch (SQLException ex)
        {
            logger.error(ex);
        }                                         
                
        Thread worker = new Thread()
        {
            @Override
            public void run()
            {
                calculateDataValues();
                displayData();

                SwingUtilities.invokeLater(new Runnable()
                {
                @Override
                    public void run()
                    {
                        runButton.setBackground(bg);
                        runButton.setForeground(Color.BLACK);
                        runButton.setText("Run");
                        repaint();
                    }
                });
            }
        };

        worker.start();      
        
    }//GEN-LAST:event_runButtonActionPerformed

    private void mooringCombo1PropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_mooringCombo1PropertyChange
        String propertyName = evt.getPropertyName();
        //logger.debug(evt.getPropertyName());
        if(propertyName.equalsIgnoreCase("MOORING_SELECTED")) {
            Mooring selectedItem = (Mooring) evt.getNewValue();
            sourceInstrumentCombo.setMooringParam(selectedItem, "OPTODE_BPHASE");
            targetInstrumentCombo.setMooring(selectedItem);
            calibrationFileCombo1.setMooring(selectedItem);
        }
    }//GEN-LAST:event_mooringCombo1PropertyChange

    private void deleteDataBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteDataBoxActionPerformed
        // TODO add your handling code here:
}//GEN-LAST:event_deleteDataBoxActionPerformed

    private void csiroUchidaButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_csiroUchidaButtonActionPerformed
    {//GEN-HEADEREND:event_csiroUchidaButtonActionPerformed
        // nothing to do - ignore
    }//GEN-LAST:event_csiroUchidaButtonActionPerformed

    private void sourceInstrumentComboPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_sourceInstrumentComboPropertyChange
        sourceInstrument = sourceInstrumentCombo.getSelectedInstrument();        
        if (sourceInstrumentCombo.getSelectedItem() != null)
        {
            targetInstrumentCombo.setSelectedItem(sourceInstrumentCombo.getSelectedItem().toString());
        }
    }//GEN-LAST:event_sourceInstrumentComboPropertyChange

    public void calculateDataValues()
    {
        Connection conn = null;
        Statement proc = null;
        ResultSet results = null;

        try
        {
            // need something like?
            // SELECT time, value AS BatteryVoltage INTO TEMP sofs2 FROM data WHERE obs_code='BatteryVoltage' AND set_code = 1011 AND time BETWEEN :s AND :e ORDER BY time, set_code;

            // ALTER TABLE sofs2 ADD SignificantWaveHeight  numeric;
            // UPDATE sofs2 SET SignificantWaveHeight = d.value FROM data d WHERE d.time = sofs2.time AND obs_code='SignificantWaveHeight';

            String tab;
            conn = Common.getConnection();
            conn.setAutoCommit(false);
            proc = conn.createStatement();
            
            tab = "SELECT date_trunc('hour', data_timestamp) AS out_timestamp, data_timestamp, source_file_id, depth, parameter_value as optode_bphase INTO TEMP aanderra FROM raw_instrument_data WHERE parameter_code = 'OPTODE_BPHASE' AND mooring_id = "+StringUtilities.quoteString(selectedMooring.getMooringID())+" AND instrument_id = "+ sourceInstrument.getInstrumentID()+" ORDER BY data_timestamp";           
                
            proc.execute(tab);            
            tab = "ALTER TABLE aanderra ADD optode_temp  numeric";
            proc.execute(tab);            
            tab = "UPDATE aanderra SET optode_temp = d.parameter_value FROM raw_instrument_data d WHERE d.data_timestamp = aanderra.data_timestamp AND parameter_code = 'OPTODE_TEMP' AND instrument_id = "+ sourceInstrument.getInstrumentID();
                
            proc.execute(tab);
            
            tab = "ALTER TABLE aanderra ADD TEMPerature  numeric";
            proc.execute(tab);
            tab = "UPDATE aanderra SET TEMPerature = d.parameter_value FROM raw_instrument_data d WHERE d.data_timestamp = aanderra.data_timestamp AND d.depth = aanderra.depth AND parameter_code = 'TEMP'";
            proc.execute(tab);
            
            tab = "ALTER TABLE aanderra ADD pressure  numeric";
            proc.execute(tab);
            tab = "UPDATE aanderra SET pressure = d.parameter_value FROM raw_instrument_data d WHERE d.data_timestamp = aanderra.data_timestamp AND d.depth = aanderra.depth AND parameter_code = 'PRES'";
            proc.execute(tab);
            
            tab = "ALTER TABLE aanderra ADD psal  numeric";
            proc.execute(tab);
            tab = "UPDATE aanderra SET psal = d.parameter_value FROM raw_instrument_data d WHERE d.data_timestamp = aanderra.data_timestamp AND d.depth = aanderra.depth AND parameter_code = 'PSAL'";
            proc.execute(tab);

            tab = "ALTER TABLE aanderra ADD density  numeric";
            proc.execute(tab);
            tab = "UPDATE aanderra SET density = d.parameter_value FROM raw_instrument_data d WHERE d.data_timestamp = aanderra.data_timestamp AND d.depth = aanderra.depth AND parameter_code = 'WATER_DENSITY'";
            proc.execute(tab);

            proc.execute("SELECT out_timestamp, data_timestamp, source_file_id, depth, optode_temp, optode_bphase, TEMPerature, pressure, psal, density FROM aanderra");
            results = (ResultSet) proc.getResultSet();
            ResultSetMetaData resultsMetaData = results.getMetaData();
            int colCount        = resultsMetaData.getColumnCount();

            while (results.next())
            {
                Vector data = new Vector();

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
                    }
                }

                optodeData row = new optodeData();
                row.setData(data);

                dataSet.add(row);
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

        if(algo.equals("Aanderra"))
            calculateOxygenValuesUsingAanderraAlgorithm();
        else
            calculateOxygenValuesUsingCSIROUchidaAlgorithm();

        insertData();
        
        String update = "UPDATE instrument_data_processors SET " 
                            + "processing_date = '" + Common.current() + "',"
                            + "count = "+ dataSet.size()
                            + " WHERE "
                            + "mooring_id = '" + selectedMooring.getMooringID() + "'"
                            + " AND class_name = '" + this.getClass().getName() + "'"
                            + " AND parameters = '" + paramToString()  + "'";

        Statement stmt;
        try
        {
            stmt = conn.createStatement();
            stmt.executeUpdate(update);
            logger.debug("Update processed table count " + dataSet.size());
        }
        catch (SQLException ex)
        {
            logger.error(ex);
        }                    
    }

    private void calculateOxygenValuesUsingAanderraAlgorithm()
    {
        constants = new AanderraOptodeConstants();
        constants.setInstrumentAndMooring(targetInstrument, selectedMooring);

        AanderraOptodeOxygenCalculator.setOptodeConstants(constants);

        boolean ok = true;

        for(int i = 0; i < dataSet.size(); i++)
        {
            optodeData row = dataSet.get(i);
            
            //
            // convert BPhase to DPhase
            //
            row.optodeDPhaseValue = constants.BPhaseConstant + (constants.BPhaseMultiplier * row.optodeBPhaseValue);
            //
            // finally use Aanderra supplied algorithm to convert DPhase value to dissolved oxygen
            //
            row.calculatedDissolvedOxygenPerKg = AanderraOptodeOxygenCalculator.calculateDissolvedOxygenInUMolesPerKg(
                                                                                            row.optodeTemperatureValue,
                                                                                            row.optodeDPhaseValue,
                                                                                            row.psal,
                                                                                            row.pressureValue
                                                                                            );
        }
    }
    
    private void calculateOxygenValuesUsingCSIROUchidaAlgorithm()
    {
        constants = new AanderraOptodeConstants();
        constants.setInstrumentAndMooring(targetInstrument, selectedMooring);

        AanderraOptodeOxygenCalculator.setOptodeConstants(constants);

        boolean ok = true;

        for(int i = 0; i < dataSet.size(); i++)
        {
            optodeData row = dataSet.get(i);
            
            row.calculatedDissolvedOxygenPerKg = AanderraOptodeOxygenCalculator.UchidaCalculateDissolvedOxygenInUMolesPerKg(
                                                                                            row.optodeTemperatureValue,
                                                                                            row.optodeBPhaseValue,
                                                                                            row.psal,
                                                                                            row.pressureValue
                                                                                            );
        }
    }

    private void insertData()
    {
        boolean ok = true;

        for(int i = 0; i < dataSet.size(); i++)
        {
            optodeData row = dataSet.get(i);

            RawInstrumentData rid = new RawInstrumentData();

            rid.setDataTimestamp(row.rawTimestamp);
            rid.setDepth(row.instrumentDepth);
            rid.setInstrumentID(sourceInstrument.getInstrumentID());
            rid.setLatitude(selectedMooring.getLatitudeIn());
            rid.setLongitude(selectedMooring.getLongitudeIn());
            rid.setMooringID(selectedMooring.getMooringID());
            rid.setParameterCode("DOX2");
            rid.setParameterValue(row.calculatedDissolvedOxygenPerKg);
            rid.setSourceFileID(row.sourceFileID);
            rid.setQualityCode("DERIVED");

            ok = rid.insert();
        }
    }

    private void displayData()
    {
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
        String $HOME = System.getProperty("user.home");

        //String filename = $HOME + "/OptodeData_" + df.format(Common.current());
        String filename = "OptodeData_" + df.format(Common.current());
        TextFileLogger file = new TextFileLogger(filename,"csv");

        try
        {
            String header = "Timestamp," 
                    + "Optode Temp,"
                    + "Optode BPhase,"
                    + "Optode DPhase, "
                    + "Salinity Temp, " 
                    + "Salinity Conduct, " 
                    + "Salinity Press, " 
                    + "Calc Salinity, " 
                    + "Calc OxySol (uM/kg),"
                    + "Calc Oxygen (uM/kg)";

            file.open();

            file.receiveLine(header);

            System.out.println(header);

            for(int i = 0; i < dataSet.size(); i++)
            {
                optodeData row = dataSet.get(i);

                System.out.println(
                        row.dataTimestamp
                        + ","
                        + row.optodeTemperatureValue
                        + ","
                        + row.optodeBPhaseValue
                        + ","
                        + row.optodeDPhaseValue
                        + ","
                        + row.waterTemperature
                        + ","
                        + row.psal
                        + ","
                        + row.pressureValue
                        + ","
                        + row.density
                        + ","
                        + row.calculatedDissolvedOxygenPerKg
                        );

                file.receiveLine(
                        row.dataTimestamp
                        + ","
                        + row.optodeTemperatureValue
                        + ","
                        + row.optodeBPhaseValue
                        + ","
                        + row.optodeDPhaseValue
                        + ","
                        + row.waterTemperature
                        + ","
                        + row.psal
                        + ","
                        + row.pressureValue
                        + ","
                        + row.density
                        + ","
                        + row.calculatedDissolvedOxygenPerKg
                        );
            }

            file.close();
        }
        catch(IOException ioex)
        {
            logger.error(ioex);
        }
    }
    /**
    * @param args the command line arguments
    */
    public static void main(String[] args)
    {
        try
        {
	    // Set cross-platform Java L&F (also called "Metal")
            UIManager.setLookAndFeel(
            UIManager.getCrossPlatformLookAndFeelClassName());
        }
        catch (UnsupportedLookAndFeelException e)
        {
           // handle exception
        }
        catch (ClassNotFoundException e)
        {
           // handle exception
        }
        catch (InstantiationException e)
        {
           // handle exception
        }
        catch (IllegalAccessException e)
        {
           // handle exception
        }

        String $HOME = System.getProperty("user.home");
        PropertyConfigurator.configure("log4j.properties");
        Common.build($HOME + "/ABOS/ABOS.properties");

        AanderraOptodeCalculationForm form = new AanderraOptodeCalculationForm();
        if (args.length > 0)
        {
            form.setupFromString(args[0]);
            form.calculateDataValues();
        }
        else
        {       
            form.initialise();
        }
    }

    public String paramToString()
    {
        return "MOORING="+selectedMooring.getMooringID() + 
                ",SRC_INST="+sourceInstrument.getInstrumentID()+
                ",TGT_INST="+targetInstrument.getInstrumentID()+
                ",ALGO="+algo+
                ",FILE="+selectedFile.getDataFilePrimaryKey();
    }

    public boolean setupFromString(String s)
    {
        Matcher mat = Pattern.compile("(?:MOORING= *)(([^,]*))").matcher(s);
        mat.find();
        
        String mooringId = mat.group(2);
        
        mat = Pattern.compile("(?:SRC_INST= *)(([^,]*))").matcher(s);
        mat.find();
        
        int srcInstrumentId = Integer.parseInt(mat.group(2));
        
        mat = Pattern.compile("(?:TGT_INST= *)(([^,]*))").matcher(s);
        mat.find();
                
        int tgtInstrumentId = Integer.parseInt(mat.group(2));
        
        selectedMooring = Mooring.selectByMooringID(mooringId);
        
        sourceInstrument = Instrument.selectByInstrumentID(srcInstrumentId);
        targetInstrument = Instrument.selectByInstrumentID(tgtInstrumentId);
        
        mat = Pattern.compile("(?:FILE= *)(([^,]*))").matcher(s);
        mat.find();
        
        int file = Integer.parseInt(mat.group(2));
        
        selectedFile = InstrumentCalibrationFile.selectByDatafilePrimaryKey(file);
        
        return true;
    }

    private class optodeData
    {
        public Timestamp dataTimestamp;
        public Timestamp rawTimestamp;
        public Integer sourceFileID;
        public Double instrumentDepth;

        public Double optodeTemperatureValue;
        public Double optodeBPhaseValue;
        public Double optodeDPhaseValue;

        public Double waterTemperature;

        public Double pressureValue;
        public Double psal;
        public Double density;

        public Double calculatedDissolvedOxygenPerKg;

        public void setData(Vector row)
        {
            int i = 0;

            dataTimestamp = (Timestamp) row.elementAt(i++);
            rawTimestamp = (Timestamp) row.elementAt(i++);
            sourceFileID = ((Number)row.elementAt(i++)).intValue();
            instrumentDepth = ((Number)row.elementAt(i++)).doubleValue();

            optodeTemperatureValue = ((Number)row.elementAt(i++)).doubleValue();
            optodeBPhaseValue = ((Number)row.elementAt(i++)).doubleValue();                

            waterTemperature = ((Number)row.elementAt(i++)).doubleValue();
            pressureValue = ((Number)row.elementAt(i++)).doubleValue();
            psal = ((Number)row.elementAt(i++)).doubleValue();
            density = ((Number)row.elementAt(i++)).doubleValue();
        }
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JRadioButton aanderraAlgorithmButton;
    private javax.swing.ButtonGroup buttonGroup1;
    private org.imos.abos.dbms.fields.CalibrationFileCombo calibrationFileCombo1;
    private javax.swing.JRadioButton csiroUchidaButton;
    private javax.swing.JCheckBox deleteDataBox;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private org.imos.abos.dbms.fields.MooringCombo mooringCombo1;
    private org.wiley.util.basicField mooringDescriptionField;
    private javax.swing.JButton quitButton;
    private javax.swing.JButton runButton;
    private org.imos.abos.dbms.fields.InstrumentSelectorCombo sourceInstrumentCombo;
    private org.imos.abos.dbms.fields.InstrumentSelectorCombo targetInstrumentCombo;
    // End of variables declaration//GEN-END:variables

}
