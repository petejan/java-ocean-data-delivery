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
import org.imos.abos.calc.SalinityCalculator;
import org.imos.abos.dbms.Instrument;
import org.imos.abos.dbms.InstrumentCalibrationFile;
import org.imos.abos.dbms.Mooring;
import org.imos.abos.dbms.ProcessedInstrumentData;
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

    /** Creates new form AanderraOptodeCalculationForm */
    public AanderraOptodeCalculationForm()
    {
        initComponents();
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
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
        deleteDataBox = new javax.swing.JCheckBox();
        jPanel2 = new javax.swing.JPanel();
        runButton = new javax.swing.JButton();
        quitButton = new javax.swing.JButton();

        setTitle("Aanderra Optode Data Processing Form");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        mooringCombo1.setDescriptionField(mooringDescriptionField);
        mooringCombo1.setOrientation(0);
        mooringCombo1.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                mooringCombo1PropertyChange(evt);
            }
        });

        mooringDescriptionField.setEnabled(false);

        calibrationFileCombo1.setLabel("Values From Calibration File");

        sourceInstrumentCombo.setLabel("Source Instrument");
        sourceInstrumentCombo.setOrientation(0);

        targetInstrumentCombo.setLabel("Target Instrument");
        targetInstrumentCombo.setOrientation(0);

        deleteDataBox.setSelected(true);
        deleteDataBox.setText("Delete any existing processed data for target instrument");
        deleteDataBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteDataBoxActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(deleteDataBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 398, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(mooringCombo1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 169, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(mooringDescriptionField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 343, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(sourceInstrumentCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 562, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(targetInstrumentCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 562, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(calibrationFileCombo1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 562, Short.MAX_VALUE))
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
                .add(18, 18, 18)
                .add(calibrationFileCombo1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(deleteDataBox)
                .add(274, 274, 274))
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
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(jPanel2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 606, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 218, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jPanel2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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

        //
        // hard-coded instrument for testing etc
        //
        //sourceInstrument = Instrument.selectByInstrumentID(4);
        sourceInstrument = sourceInstrumentCombo.getSelectedInstrument();
        targetInstrument = targetInstrumentCombo.getSelectedInstrument();

        selectedMooring = mooringCombo1.getSelectedMooring();
        selectedFile = calibrationFileCombo1.getSelectedFile();

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
        }

        final Color bg = runButton.getBackground();
        runButton.setText("Running...");
        runButton.setBackground(Color.RED);
        runButton.setForeground(Color.WHITE);

        String insProc = new String("INSERT INTO instrument_data_processors (processors_pk, mooring_id, class_name, parameters, processing_date, display_code) VALUES ("
                + "nextval('instrument_data_processor_sequence'),"
                + "'" + selectedMooring.getMooringID() + "',"
                + "'" + this.getClass().getName() + "',"
                + "'" + paramToString() + "',"
                + "'" + Common.current() + "',"
                + "'Y'"
                + ")");

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
            sourceInstrumentCombo.setMooring(selectedItem);
            targetInstrumentCombo.setMooring(selectedItem);
            calibrationFileCombo1.setMooring(selectedItem);
        }
    }//GEN-LAST:event_mooringCombo1PropertyChange

    private void deleteDataBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteDataBoxActionPerformed
        // TODO add your handling code here:
}//GEN-LAST:event_deleteDataBoxActionPerformed

    public void calculateDataValues()
    {
        Connection conn = null;
        CallableStatement proc = null;
        ResultSet results = null;

        try
        {
            String storedProc = new String("{ ? = call xtract_optode_data_selector"
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

        calculateOxygenValues();

        insertData();
    }

    private void calculateOxygenValues()
    {
        constants = new AanderraOptodeConstants();
        constants.setInstrumentAndMooring(targetInstrument, selectedMooring);

        AanderraOptodeOxygenCalculator.setOptodeConstants(constants);

        boolean ok = true;

        for(int i = 0; i < dataSet.size(); i++)
        {
            optodeData row = dataSet.get(i);
            row.calculatedSalinityValue = SalinityCalculator.calculateSalinityForITS90Temperature(row.salinityTemperature,
                                                                                        row.conductivityValue * 10,
                                                                                        row.pressureValue
                                                                                        );
            //
            // convert temperature voltage value to temperature in deg C
            //
            row.optodeTemperatureValue = constants.A_Coefficient + (constants.B_Coefficient * row.optodeTemperatureVolts);
            //
            // convert BPhase voltage value to Optode BPhase
            //
            row.optodeBPhaseValue = constants.BPhaseVoltConstant + (row.optodeBPhaseVolts * constants.BPhaseVoltMultiplier);
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
                                                                                            row.calculatedSalinityValue,
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

            ProcessedInstrumentData pid = new ProcessedInstrumentData();

            pid.setDataTimestamp(row.dataTimestamp);
            pid.setDepth(row.instrumentDepth);
            pid.setInstrumentID(targetInstrument.getInstrumentID());
            pid.setLatitude(selectedMooring.getLatitudeIn());
            pid.setLongitude(selectedMooring.getLongitudeIn());
            pid.setMooringID(selectedMooring.getMooringID());
            pid.setParameterCode("DOX2");
            pid.setParameterValue(row.calculatedDissolvedOxygenPerKg);
            pid.setSourceFileID(row.sourceFileID);
            pid.setQualityCode("RAW");

            ok = pid.insert();

            pid.setInstrumentID(sourceInstrument.getInstrumentID());
            pid.setParameterCode("PSAL");
            pid.setParameterValue(row.calculatedSalinityValue);

            ok = pid.insert();

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
            String header = "Timestamp," +
                    "Optode Temp," +
                    "Optode BPhase," +
                    "Optode DPhase, " +
                    "Salinity Temp, " +
                    "Salinity Conduct, " +
                    "Salinity Press, " +
                    "Calc Salinity, " +
                    "Calc Oxygen (uM/kg)";

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
                        + row.salinityTemperature
                        + ","
                        + row.conductivityValue
                        + ","
                        + row.pressureValue
                        + ","
                        + row.calculatedSalinityValue
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
                        + row.salinityTemperature
                        + ","
                        + row.conductivityValue
                        + ","
                        + row.pressureValue
                        + ","
                        + row.calculatedSalinityValue
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

        if(args.length == 0)
        {
            PropertyConfigurator.configure("log4j.properties");
            Common.build("ABOS.conf");
        }

        AanderraOptodeCalculationForm form = new AanderraOptodeCalculationForm();
        form.initialise();
    }

    public String paramToString()
    {
        return "MOORING="+selectedMooring.getMooringID() + 
                ",SRC_INST="+sourceInstrument.getInstrumentID()+
                ",TGT_INST="+targetInstrument.getInstrumentID()+",FILE="+selectedFile.getDataFilePrimaryKey();
    }

    public boolean setupFromString(String s)
    {
        Matcher mat = Pattern.compile("(?:MOORING= *)(([^,]*))").matcher(s);
        mat.find();
        
        String mooringId = mat.group(2);
        
        mat = Pattern.compile("(?:SRC= *)(([^,]*))").matcher(s);
        mat.find();
        
        int srcInstrumentId = Integer.parseInt(mat.group(2));
        
        mat = Pattern.compile("(?:TGT= *)(([^,]*))").matcher(s);
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
        public Integer sourceFileID;
        public Double instrumentDepth;

        public Double optodeTemperatureVolts;
        public Double optodeBPhaseVolts;

        public Double optodeTemperatureValue;
        public Double optodeBPhaseValue;
        public Double optodeDPhaseValue;

        public Double salinityTemperature;

        public Double pressureValue;
        public Double conductivityValue;

        public Double calculatedSalinityValue;
        public Double calculatedDissolvedOxygenPerKg;

        public void setData(Vector row)
        {
            int i = 0;

            dataTimestamp = (Timestamp) row.elementAt(i++);
            sourceFileID = ((Number)row.elementAt(i++)).intValue();
            instrumentDepth = ((Number)row.elementAt(i++)).doubleValue();

            optodeTemperatureVolts = ((Number)row.elementAt(i++)).doubleValue();
            optodeBPhaseVolts = ((Number)row.elementAt(i++)).doubleValue();

            salinityTemperature = ((Number)row.elementAt(i++)).doubleValue();
            pressureValue = ((Number)row.elementAt(i++)).doubleValue();
            conductivityValue = ((Number)row.elementAt(i++)).doubleValue();
        }
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private org.imos.abos.dbms.fields.CalibrationFileCombo calibrationFileCombo1;
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
