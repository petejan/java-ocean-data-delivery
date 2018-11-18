/*
 * IMOS data delivery project
 * Written by Peter Wiley
 * This code is copyright (c) Peter Wiley 2000 - ?
 * It is made available under the BSD Software Licence in the hope that it may be useful.
 * It has NO WARRANTY OF FITNESS OR SUITABILITY FOR ANY PURPOSE.
 * Feel free to fix any bugs that you may find.
 */

/*
 * InterpolatedWaterParamsDataCreationForm.java
 *
 * Created on Apr 4, 2012, 10:20:36 AM
 */

package org.imos.abos.forms;

import java.awt.Color;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.interpolation.SplineInterpolator;
import org.apache.commons.math3.analysis.interpolation.UnivariateInterpolator;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.imos.abos.calc.OxygenSolubilityCalculator;
import org.imos.abos.calc.SalinityCalculator;
import org.imos.abos.calc.SeawaterParameterCalculator;
import org.imos.abos.dbms.*;
import org.wiley.core.Common;
import org.wiley.core.forms.MemoryWindow;
import org.wiley.util.StringUtilities;

/**
 *
 * @author peter
 */
public class InterpolatedWaterParamsDataCreationForm extends MemoryWindow implements DataProcessor
{

    private static Logger logger = Logger.getLogger(InterpolatedWaterParamsDataCreationForm.class.getName());

    private Mooring selectedMooring = null;
    private Instrument sourceInstrument = null;
    private Instrument targetInstrument = null;

    /** Creates new form ProcessedDataCreationForm */
    public InterpolatedWaterParamsDataCreationForm()
    {
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel2 = new javax.swing.JPanel();
        runButton = new javax.swing.JButton();
        quitButton = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        mooringCombo1 = new org.imos.abos.dbms.fields.MooringCombo();
        deleteDataBox = new javax.swing.JCheckBox();
        mooringDescriptionField = new org.wiley.util.basicField();
        sourceInstrumentCombo = new org.imos.abos.dbms.fields.InstrumentSelectorCombo();
        targetInstrumentCombo = new org.imos.abos.dbms.fields.InstrumentSelectorCombo();

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

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

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        mooringCombo1.setOrientation(0);
        mooringCombo1.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                mooringCombo1PropertyChange(evt);
            }
        });

        deleteDataBox.setSelected(true);
        deleteDataBox.setText("Delete any existing processed data for target instrument & parameter");
        deleteDataBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteDataBoxActionPerformed(evt);
            }
        });

        mooringDescriptionField.setEnabled(false);

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
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(mooringCombo1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 169, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(mooringDescriptionField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 343, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(sourceInstrumentCombo, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 562, Short.MAX_VALUE)
                    .add(targetInstrumentCombo, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 562, Short.MAX_VALUE)
                    .add(deleteDataBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 488, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jPanel1Layout.linkSize(new java.awt.Component[] {sourceInstrumentCombo, targetInstrumentCombo}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

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
                .add(82, 82, 82)
                .add(deleteDataBox)
                .add(262, 262, 262))
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(jPanel2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 606, Short.MAX_VALUE))
                    .add(layout.createSequentialGroup()
                        .add(11, 11, 11)
                        .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .add(37, 37, 37))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 222, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jPanel2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

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

        if(selectedMooring == null)
        {
            Common.showMessage(this,"No Mooring Selected","You must select a mooring before running any calculations");
            return;
        }

        if(deleteDataBox.isSelected())
        {
            RawInstrumentData.deleteDataForMooringAndInstrumentAndParameter(selectedMooring.getMooringID(),
                                                                                  targetInstrument.getInstrumentID(),
                                                                                  "TEMP")
                                                                      ;
            RawInstrumentData.deleteDataForMooringAndInstrumentAndParameter(selectedMooring.getMooringID(),
                                                                                  targetInstrument.getInstrumentID(),
                                                                                  "PRES")
                                                                      ;
            RawInstrumentData.deleteDataForMooringAndInstrumentAndParameter(selectedMooring.getMooringID(),
                                                                                  targetInstrument.getInstrumentID(),
                                                                                  "PSAL")
                                                                      ;
            RawInstrumentData.deleteDataForMooringAndInstrumentAndParameter(selectedMooring.getMooringID(),
                                                                                  targetInstrument.getInstrumentID(),
                                                                                  "OXSOL")
                                                                      ;
            RawInstrumentData.deleteDataForMooringAndInstrumentAndParameter(selectedMooring.getMooringID(),
                                                                                  targetInstrument.getInstrumentID(),
                                                                                  "DENSITY")
                                                                      ;
//            RawInstrumentData.deleteDataForMooringAndInstrumentAndParameter(selectedMooring.getMooringID(),
//                                                                                  targetInstrument.getInstrumentID(),
//                                                                                  "DEPTH")
//                                                                      ;
        }

        final Color bg = runButton.getBackground();
        runButton.setText("Running...");
        runButton.setBackground(Color.RED);
        runButton.setForeground(Color.WHITE);
        
        // TODO: should only form ones do this, or should all create a record in the table?
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

    public void calculateDataValues()
    {
        ArrayList<RawInstrumentData> waterTemp = RawInstrumentData.selectInstrumentAndMooringAndParameter
                                                    (
                                                    sourceInstrument.getInstrumentID(),
                                                    selectedMooring.getMooringID(),
                                                    "TEMP"
                                                    );
        ArrayList<RawInstrumentData> waterCond = RawInstrumentData.selectInstrumentAndMooringAndParameter
                                                    (
                                                    sourceInstrument.getInstrumentID(),
                                                    selectedMooring.getMooringID(),
                                                    "CNDC"
                                                    );
        ArrayList<RawInstrumentData> waterPressure = RawInstrumentData.selectInstrumentAndMooringAndParameter
                                                    (
                                                    sourceInstrument.getInstrumentID(),
                                                    selectedMooring.getMooringID(),
                                                    "PRES"
                                                    );
        long startTs = 0, endTs = 0;

        RawInstrumentData srcInst = waterTemp.get(0);

        double xTemp[] = new double[waterTemp.size()];
        double yTemp[] = new double[waterTemp.size()];

        RawInstrumentData t;
        for(int i = 0; i < waterTemp.size(); i++)
        {
            t = waterTemp.get(i);
            xTemp[i] = t.getDataTimestamp().getTime();
            yTemp[i] = t.getParameterValue();
        }            
        double xCond[] = new double[waterCond.size()];
        double yCond[] = new double[waterCond.size()];

        for(int i = 0; i < waterCond.size(); i++)
        {
            t = waterCond.get(i);
            xCond[i] = t.getDataTimestamp().getTime();
            yCond[i] = t.getParameterValue();
        }            
        double xPres[] = new double[waterPressure.size()];
        double yPres[] = new double[waterPressure.size()];

        for(int i = 0; i < waterPressure.size(); i++)
        {
            t = waterPressure.get(i);
            xPres[i] = t.getDataTimestamp().getTime();
            yPres[i] = t.getParameterValue();
        }          
        startTs = waterCond.get(0).getDataTimestamp().getTime();
        endTs = waterCond.get(waterCond.size()-1).getDataTimestamp().getTime();

        UnivariateInterpolator iTemp = new SplineInterpolator();
        UnivariateInterpolator iPres = new SplineInterpolator();
        UnivariateInterpolator iCond = new SplineInterpolator();
        UnivariateFunction fTemp = iTemp.interpolate(xTemp, yTemp);
        UnivariateFunction fPres = null;
        if (!waterPressure.isEmpty())
        {
               fPres = iPres.interpolate(xPres, yPres);
        }
        UnivariateFunction fCond = iCond.interpolate(xCond, yCond);
        
        // get the target instrument parameter to select the time stamps on
        String SQL = "SELECT DISTINCT(data_timestamp) FROM raw_instrument_data WHERE mooring_id = "
                        + StringUtilities.quoteString(selectedMooring.getMooringID())
                        + " AND instrument_id = " + targetInstrument.getInstrumentID() + " order by data_timestamp";
        Connection conn = Common.getConnection();
        Statement proc;
        Timestamp param;
        
        int count = 0;
        try
        {
            proc = conn.createStatement();
            conn.setAutoCommit(false);
            proc.execute(SQL);  
            ResultSet results = (ResultSet) proc.getResultSet();
            
            while(results.next())
            {
            
                param = results.getTimestamp(1);
        
                RawInstrumentData rid = new RawInstrumentData();

                long ts = param.getTime();
                
                if ((ts > startTs) && (ts < endTs))
                {
                    rid.setDataTimestamp(new Timestamp(ts));
                    rid.setDepth(srcInst.getDepth());
                    rid.setInstrumentID(targetInstrument.getInstrumentID());
                    rid.setLatitude(srcInst.getLatitude());
                    rid.setLongitude(srcInst.getLongitude());
                    rid.setMooringID(srcInst.getMooringID());
                    rid.setSourceFileID(srcInst.getSourceFileID());
                    rid.setQualityCode("INTERPOLATED");

                    double dWaterTemp = fTemp.value((double)ts);
                    double dWaterCond = fCond.value((double)ts);
                    double dWaterPres = 0;
                    if (waterPressure.isEmpty())
                    {
                        dWaterPres = srcInst.getDepth();                
                    }
                    else
                    {
                        dWaterPres = fPres.value((double)ts);
                    }

                    rid.setParameterCode("TEMP");
                    rid.setParameterValue(dWaterTemp);
                    boolean ok = rid.insert();                                

                    rid.setParameterCode("PRES");
                    rid.setParameterValue(dWaterPres);
                    ok = rid.insert();                                

                    double calculatedSalinityValue = SalinityCalculator.calculateSalinityForITS90Temperature(dWaterTemp, dWaterCond * 10, dWaterPres );
                    rid.setParameterCode("PSAL");
                    rid.setParameterValue(calculatedSalinityValue);
                    ok = rid.insert();                                

                    System.out.println(rid.getDataTimestamp() + " ," + rid.getParameterCode() + " ," + rid.getParameterValue());
                    
                    double calculatedDensityValue = SeawaterParameterCalculator.calculateSeawaterDensityAtPressure(calculatedSalinityValue, dWaterTemp, dWaterPres);                
                    rid.setParameterCode("DENSITY");
                    rid.setParameterValue(calculatedDensityValue);
                    ok = rid.insert();                                

//                    double calculatedDepth = SeawaterParameterCalculator.depth(dWaterPres, selectedMooring.getLatitudeIn());
//                    rid.setParameterCode("DEPTH");
//                    rid.setParameterValue(calculatedDepth);
//                    ok = rid.insert();                                

                    double calculatedOxygenSolubilityValue = OxygenSolubilityCalculator.calculateOxygenSolubilityInUMolesPerKg(dWaterTemp, calculatedSalinityValue);
                    rid.setParameterCode("OXSOL");
                    rid.setParameterValue(calculatedOxygenSolubilityValue);
                    ok = rid.insert();   
                    
                    conn.commit();
                    count++;
                }
            }
        }
        catch (SQLException ex)
        {
            java.util.logging.Logger.getLogger(InterpolatedWaterParamsDataCreationForm.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        String update = "UPDATE instrument_data_processors SET " 
                            + "processing_date = '" + Common.current() + "',"
                            + "count = "+ count
                            + " WHERE "
                            + "mooring_id = '" + selectedMooring.getMooringID() + "'"
                            + " AND class_name = '" + this.getClass().getName() + "'"
                            + " AND parameters = '" + paramToString()  + "'";

        //Connection conn = Common.getConnection();

        Statement stmt;
        try
        {
            stmt = conn.createStatement();
            stmt.executeUpdate(update);
            logger.debug("Update raw table count " + count);
        }
        catch (SQLException ex)
        {
            logger.error(ex);
        }

    }

    private void quitButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_quitButtonActionPerformed
        cleanup();
}//GEN-LAST:event_quitButtonActionPerformed

    private void mooringCombo1PropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_mooringCombo1PropertyChange
        String propertyName = evt.getPropertyName();
        //logger.debug(evt.getPropertyName());
        if(propertyName.equalsIgnoreCase("MOORING_SELECTED"))
        {
            Mooring selectedItem = (Mooring) evt.getNewValue();
            sourceInstrumentCombo.setMooringParam(selectedItem, "CNDC");
            targetInstrumentCombo.setMooring(selectedItem);
        }
}//GEN-LAST:event_mooringCombo1PropertyChange

    private void deleteDataBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteDataBoxActionPerformed
        // TODO add your handling code here:
}//GEN-LAST:event_deleteDataBoxActionPerformed

    @Override
    public void initialise()
    {
        initComponents();

        this.setVisible(true);
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
        Common.build("ABOS.properties");

        InterpolatedWaterParamsDataCreationForm form = new InterpolatedWaterParamsDataCreationForm();
        
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

    // Variables declaration - do not modify//GEN-BEGIN:variables
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

    @Override
    public String paramToString()
    {
        return "MOORING="+selectedMooring.getMooringID() + 
                ",SRC_INST="+sourceInstrument.getInstrumentID() +
                ",TGT_INST="+targetInstrument.getInstrumentID();
    }

    @Override
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
        
        return true;
    }

}
