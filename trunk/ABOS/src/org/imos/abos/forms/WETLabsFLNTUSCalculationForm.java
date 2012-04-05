/*
 * Neonatal Screening Software Project
 * Written by Peter Wiley
 * This code is copyright (c) Peter Wiley 2000 - ?
 * It is made available under the BSD Software Licence in the hope that it may be useful.
 * It has NO WARRANTY OF FITNESS OR SUITABILITY FOR ANY PURPOSE.
 * Feel free to fix any bugs that you may find.
 */

/*
 * WETLabsFLNTUSCalculationForm.java
 *
 * Created on Mar 20, 2012, 8:33:33 PM
 */

package org.imos.abos.forms;

import java.awt.Color;
import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Vector;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.imos.abos.calc.WETLabsFLNTUSCalculator;
import org.imos.abos.dbms.Instrument;
import org.imos.abos.dbms.InstrumentCalibrationFile;
import org.imos.abos.dbms.Mooring;
import org.imos.abos.dbms.ProcessedInstrumentData;
import org.imos.abos.instrument.WETLabsFLNTUSConstants;
import org.wiley.core.Common;
import org.wiley.core.forms.MemoryWindow;
import org.wiley.util.StringUtilities;
import org.wiley.util.TextFileLogger;

/**
 *
 * @author peter
 * xtract_wetlabs_flntus_selector
 */
public class WETLabsFLNTUSCalculationForm extends MemoryWindow
{

    private static Logger logger = Logger.getLogger(SeabirdSBE43CalculationForm.class.getName());

    private Mooring selectedMooring =null;
    private InstrumentCalibrationFile selectedFile = null;
    private Instrument sourceInstrument = null;
    private Instrument targetInstrument = null;

    private ArrayList<FLNTUData> dataSet = new ArrayList();

    /** Creates new form WETLabsFLNTUSCalculationForm */
    public WETLabsFLNTUSCalculationForm()
    {
        initComponents();
    }

    @Override
    public void initialise()
    {
        this.setVisible(true);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        mooringCombo1 = new org.imos.abos.dbms.fields.MooringCombo();
        mooringDescriptionField = new org.wiley.util.basicField();
        calibrationFileCombo1 = new org.imos.abos.dbms.fields.CalibrationFileCombo();
        sourceInstrumentCombo = new org.imos.abos.dbms.fields.InstrumentSelectorCombo();
        targetInstrumentCombo = new org.imos.abos.dbms.fields.InstrumentSelectorCombo();
        jPanel2 = new javax.swing.JPanel();
        runButton = new javax.swing.JButton();
        quitButton = new javax.swing.JButton();

        setTitle("WETLabs FLNTUS Data Processing Form");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        mooringCombo1.setOrientation(0);
        mooringCombo1.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                mooringCombo1PropertyChange(evt);
            }
        });

        mooringDescriptionField.setEnabled(false);

        calibrationFileCombo1.setLabel("Values From Calibration File");
        calibrationFileCombo1.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                calibrationFileCombo1PropertyChange(evt);
            }
        });

        sourceInstrumentCombo.setLabel("Source Instrument");
        sourceInstrumentCombo.setOrientation(0);

        targetInstrumentCombo.setLabel("Target Instrument");
        targetInstrumentCombo.setOrientation(0);

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(sourceInstrumentCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 562, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(mooringCombo1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 169, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(mooringDescriptionField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 343, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, calibrationFileCombo1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 562, Short.MAX_VALUE)
                    .add(targetInstrumentCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 562, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap(20, Short.MAX_VALUE)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, mooringCombo1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 21, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, mooringDescriptionField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(sourceInstrumentCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 26, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(targetInstrumentCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 26, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(77, 77, 77)
                .add(calibrationFileCombo1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(248, 248, 248))
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        runButton.setText("Run");
        runButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                runButtonActionPerformed(evt);
            }
        });
        jPanel2.add(runButton);

        quitButton.setText("Quit");
        quitButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                quitButtonActionPerformed(evt);
            }
        });
        jPanel2.add(quitButton);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 606, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 240, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        cleanup();
    }//GEN-LAST:event_formWindowClosing

    private void mooringCombo1PropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_mooringCombo1PropertyChange
        String propertyName = evt.getPropertyName();
        //logger.debug(evt.getPropertyName());
        if(propertyName.equalsIgnoreCase("MOORING_SELECTED")) {
            Mooring selectedItem = (Mooring) evt.getNewValue();
            sourceInstrumentCombo.setMooring(selectedItem);
            targetInstrumentCombo.setMooring(selectedItem);
            calibrationFileCombo1.setMooring(selectedItem);
        }
}//GEN-LAST:event_mooringCombo1PropertyChange

    private void calibrationFileCombo1PropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_calibrationFileCombo1PropertyChange
        String propertyName = evt.getPropertyName();
        //logger.debug(evt.getPropertyName());
}//GEN-LAST:event_calibrationFileCombo1PropertyChange

    private void runButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_runButtonActionPerformed

        //
        // hard-coded instrument for testing etc
        //
        //sourceInstrument = Instrument.selectByInstrumentID(4);
        sourceInstrument = sourceInstrumentCombo.getSelectedInstrument();
        targetInstrument = targetInstrumentCombo.getSelectedInstrument();

        selectedMooring = mooringCombo1.getSelectedMooring();
        selectedFile = calibrationFileCombo1.getSelectedFile();

        if(selectedMooring == null) {
            Common.showMessage(this,"No Mooring Selected","You must select a mooring before running any calculations");
            return;
        }

        if(selectedFile == null) {
            Common.showMessage(this,"No File Selected","You must select a file before running any calculations");
            return;
        }

        final Color bg = runButton.getBackground();
        runButton.setText("Running...");
        runButton.setBackground(Color.RED);
        runButton.setForeground(Color.WHITE);
        Thread worker = new Thread() {
            @Override
            public void run() {
                calculateDataValues();
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
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

    private void calculateDataValues()
    {
        Connection conn = null;
        CallableStatement proc = null;
        ResultSet results = null;

        WETLabsFLNTUSConstants constants = new WETLabsFLNTUSConstants();
        constants.setInstrumentCalibrationFile(selectedFile);

        WETLabsFLNTUSCalculator.setConstants(constants);

        try
        {
            
            String storedProc = new String("{ ? = call xtract_wetlabs_flntus_selector"
                                            + "("
                                            + sourceInstrument.getInstrumentID()
                                            + " , "
                                            + StringUtilities.quoteString(selectedMooring.getMooringID())
                                            + ") }"
                                            );
             
            conn = Common.getConnection();
            conn.setAutoCommit(false);

            proc = conn.prepareCall(storedProc);
            proc.registerOutParameter(1, Types.OTHER);
            proc.execute();
            results = (ResultSet) proc.getObject(1);
            ResultSetMetaData resultsMetaData = results.getMetaData();
            int colCount        = resultsMetaData.getColumnCount();

            //AanderraOptodeData dataset = new AanderraOptodeData();
            //dataset.setInstrumentCalibrationFile(selectedFile);

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

                FLNTUData row = new FLNTUData();
                row.setData(data);

                row.calculatedChlorophyllValue = WETLabsFLNTUSCalculator.calculateChlorophyllValue(row.chlorophyllVoltage);
                row.calculatedTurbidityValue = WETLabsFLNTUSCalculator.calculateTurbidityValue(row.turbidityVoltage);
                
                dataSet.add(row);
            }
            results.close();
            proc.close();
            conn.setAutoCommit(true);
        }
        catch(SQLException sex)
        {
            logger.error(sex);
        }
        finally
        {
            try
            {
                if(results != null)
                    results.close();
                if(proc != null)
                    proc.close();
                if(conn != null)
                {
                    conn.rollback();
                    conn.setAutoCommit(true);
                }
            }
            catch(SQLException sex)
            {
                logger.error(sex);
            }
        }

        displayData();
        insertData();
    }

    private void insertData()
    {
        for(int i = 0; i < dataSet.size(); i++)
        {
            FLNTUData sbe = dataSet.get(i);

            ProcessedInstrumentData row = new ProcessedInstrumentData();

            row.setDataTimestamp(sbe.dataTimestamp);
            row.setDepth(sbe.instrumentDepth);
            row.setInstrumentID(targetInstrument.getInstrumentID());
            row.setLatitude(selectedMooring.getLatitudeIn());
            row.setLongitude(selectedMooring.getLongitudeIn());
            row.setMooringID(selectedMooring.getMooringID());
            row.setParameterCode("TURBIDITY");
            row.setParameterValue(sbe.calculatedTurbidityValue);
            row.setSourceFileID(sbe.sourceFileID);
            row.setQualityCode("RAW");

            boolean ok = row.insert();

            row.setParameterCode("CHLOROPHYLL");
            row.setParameterValue(sbe.calculatedChlorophyllValue);
            row.setSourceFileID(sbe.sourceFileID);
            row.setQualityCode("RAW");

            ok = row.insert();

        }
    }

    private void displayData()
    {
        String $HOME = System.getProperty("user.home");

        String filename = $HOME + "/WETLabsFLNTUS_data";
        TextFileLogger file = new TextFileLogger(filename,"csv");

        try
        {
            String header = "Timestamp,"
                            + "Chlorophyll Volts,"
                            + " Turbidity Volts,"
                            + " Calc Chlorophyll (ug/l),"
                            + " Calc Turbidity (NTU)"
                            ;

            file.open();

            file.receiveLine(header);

            System.out.println(header);

            for(int i = 0; i < dataSet.size(); i++)
            {
                FLNTUData row = dataSet.get(i);

                System.out.println(
                        row.dataTimestamp
                        + ","
                        + row.chlorophyllVoltage
                        + ","
                        + row.turbidityVoltage
                        + ","
                        + row.calculatedChlorophyllValue
                        + ","
                        + row.calculatedTurbidityValue
                        );

                file.receiveLine(
                        row.dataTimestamp
                        + ","
                        + row.chlorophyllVoltage
                        + ","
                        + row.turbidityVoltage
                        + ","
                        + row.calculatedChlorophyllValue
                        + ","
                        + row.calculatedTurbidityValue
                        );
            }

            file.close();
        }
        catch(IOException ioex)
        {
            logger.error(ioex);
        }
    }

    private void quitButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_quitButtonActionPerformed
        cleanup();
}//GEN-LAST:event_quitButtonActionPerformed

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

        if(args.length == 0)
        {
            PropertyConfigurator.configure($HOME + "/ABOS/log4j.properties");
            Common.build($HOME + "/ABOS/ABOS.conf");
        }

        WETLabsFLNTUSCalculationForm form = new WETLabsFLNTUSCalculationForm();
        form.initialise();
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private org.imos.abos.dbms.fields.CalibrationFileCombo calibrationFileCombo1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private org.imos.abos.dbms.fields.MooringCombo mooringCombo1;
    private org.wiley.util.basicField mooringDescriptionField;
    private javax.swing.JButton quitButton;
    private javax.swing.JButton runButton;
    private org.imos.abos.dbms.fields.InstrumentSelectorCombo sourceInstrumentCombo;
    private org.imos.abos.dbms.fields.InstrumentSelectorCombo targetInstrumentCombo;
    // End of variables declaration//GEN-END:variables

    private class FLNTUData
    {
        public Timestamp dataTimestamp;
        public Integer sourceFileID;
        public Double instrumentDepth;

        public Double chlorophyllVoltage;
        public Double turbidityVoltage;

        public Double calculatedChlorophyllValue;
        public Double calculatedTurbidityValue;

        public void setData(Vector row)
        {
            int i = 0;

            dataTimestamp = (Timestamp) row.elementAt(i++);
            sourceFileID = ((Number)row.elementAt(i++)).intValue();
            instrumentDepth = ((Number)row.elementAt(i++)).doubleValue();
            chlorophyllVoltage = ((Number)row.elementAt(i++)).doubleValue();
            turbidityVoltage = ((Number)row.elementAt(i++)).doubleValue();

        }
    }
}
