/*
 * IMOS data delivery project
 * Written by Peter Wiley
 * This code is copyright Peter Wiley 1998-2008
 * It is made available under the BSD Software Licence in the hope that it may be useful.
 * It has NO WARRANTY OF FITNESS OR SUITABILITY FOR ANY PURPOSE.
 * Feel free to fix any bugs that you may find.
 *
 */
package org.imos.abos.forms;

import java.awt.Color;
import java.io.IOException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Vector;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.imos.abos.calc.SalinityCalculator;
import org.imos.abos.dbms.Instrument;
import org.imos.abos.dbms.Mooring;
import org.imos.abos.dbms.ProcessedInstrumentData;
import org.wiley.core.Common;
import org.wiley.core.forms.MemoryWindow;
import org.wiley.util.StringUtilities;
import org.wiley.util.TextFileLogger;

/**
 *
 * @author peter
 */
public class SeabirdSBE16CalculationForm extends MemoryWindow implements DataProcessor
{

    private static Logger logger = Logger.getLogger(SeabirdSBE37CalculationForm.class.getName());

    private Mooring selectedMooring =null;
    private Instrument sourceInstrument = null;
    private Instrument targetInstrument = null;

    private ArrayList<SBE16Data> dataSet = new ArrayList();
    /**
     * Creates new form SeabirdSBE16CalculationForm
     */
    public SeabirdSBE16CalculationForm()
    {
        initComponents();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        sourceInstrumentCombo = new org.imos.abos.dbms.fields.InstrumentSelectorCombo();
        mooringCombo1 = new org.imos.abos.dbms.fields.MooringCombo();
        mooringDescriptionField = new org.wiley.util.basicField();
        targetInstrumentCombo = new org.imos.abos.dbms.fields.InstrumentSelectorCombo();
        deleteDataBox = new javax.swing.JCheckBox();
        jPanel2 = new javax.swing.JPanel();
        runButton = new javax.swing.JButton();
        quitButton = new javax.swing.JButton();

        setTitle("Seabird SBE16 Data Processor Form");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        sourceInstrumentCombo.setLabel("Source Instrument");
        sourceInstrumentCombo.setOrientation(0);

        mooringCombo1.setOrientation(0);
        mooringCombo1.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                mooringCombo1PropertyChange(evt);
            }
        });

        mooringDescriptionField.setEnabled(false);

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
                    .add(sourceInstrumentCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 562, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(mooringCombo1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 169, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(mooringDescriptionField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 343, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(targetInstrumentCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 562, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(deleteDataBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 398, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(27, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, mooringCombo1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 21, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, mooringDescriptionField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(sourceInstrumentCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 22, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(targetInstrumentCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 22, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(deleteDataBox)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
                    .add(jPanel2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    @Override
    public void initialise()
    {
        //initComponents();

        this.setVisible(true);
    }
    
    private void mooringCombo1PropertyChange(java.beans.PropertyChangeEvent evt)//GEN-FIRST:event_mooringCombo1PropertyChange
    {//GEN-HEADEREND:event_mooringCombo1PropertyChange
        String propertyName = evt.getPropertyName();
        //logger.debug(evt.getPropertyName());
        if (propertyName.equalsIgnoreCase("MOORING_SELECTED"))
        {
            Mooring selectedItem = (Mooring) evt.getNewValue();
            sourceInstrumentCombo.setMooring(selectedItem);
            targetInstrumentCombo.setMooring(selectedItem);
        }
    }//GEN-LAST:event_mooringCombo1PropertyChange

    private void deleteDataBoxActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_deleteDataBoxActionPerformed
    {//GEN-HEADEREND:event_deleteDataBoxActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_deleteDataBoxActionPerformed
    
    @Override
    public String paramToString()
    {
        return "MOORING=" + selectedMooring.getMooringID() + ",SRC="+sourceInstrument.getInstrumentID() + ",TARGET="+targetInstrument.getInstrumentID();
    }
    
    private void runButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_runButtonActionPerformed
    {//GEN-HEADEREND:event_runButtonActionPerformed

        //
        // hard-coded instrument for testing etc
        //
        //sourceInstrument = Instrument.selectByInstrumentID(4);

        sourceInstrument = sourceInstrumentCombo.getSelectedInstrument();
        targetInstrument = targetInstrumentCombo.getSelectedInstrument();

        selectedMooring = mooringCombo1.getSelectedMooring();

        if (selectedMooring == null)
        {
            Common.showMessage(this, "No Mooring Selected", "You must select a mooring before running any calculations");
            return;
        }

        if (deleteDataBox.isSelected())
        {
            ProcessedInstrumentData.deleteDataForMooringAndInstrument(selectedMooring.getMooringID(),
                    targetInstrument.getInstrumentID());
        }

        logger.debug(paramToString());

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

    public void displayData()
    {
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
        String $HOME = System.getProperty("user.home");

        //String filename = $HOME + "/sbe37_data_" + df.format(Common.current());
        String filename = "sbe16_data_" + df.format(Common.current());
        TextFileLogger file = new TextFileLogger(filename,"csv");

        try
        {
            String header = "Timestamp,"
                            + " Temp,"
                            + " Conduct,"
                            + " Press,"
                            + " Calc Salinity,"
                            ;

            file.open();

            file.receiveLine(header);

            System.out.println(header);

            for(int i = 0; i < dataSet.size(); i++)
            {
                SBE16Data row = dataSet.get(i);

                System.out.println(
                        row.dataTimestamp
                        + ","
                        + row.temperatureValue
                        + ","
                        + row.conductivityValue
                        + ","
                        + row.pressureValue
                        + ","
                        + row.calculatedSalinityValue
                        );

                file.receiveLine(
                        row.dataTimestamp
                        + ","
                        + row.temperatureValue
                        + ","
                        + row.conductivityValue
                        + ","
                        + row.pressureValue
                        + ","
                        + row.calculatedSalinityValue
                        );
            }

            file.close();
        }
        catch(IOException ioex)
        {
            logger.error(ioex);
        }
    }
    
    private void quitButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_quitButtonActionPerformed
    {//GEN-HEADEREND:event_quitButtonActionPerformed
        cleanup();
    }//GEN-LAST:event_quitButtonActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt)//GEN-FIRST:event_formWindowClosing
    {//GEN-HEADEREND:event_formWindowClosing
        cleanup();
    }//GEN-LAST:event_formWindowClosing

    /**
     * @param args the command line arguments
     */
    public static void main(String args[])
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
        Common.build("ABOS.conf");

        SeabirdSBE16CalculationForm form = new SeabirdSBE16CalculationForm();

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

    public boolean setupFromString(String s)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void calculateDataValues()
    {
        logger.info("Calculate " + selectedMooring.getMooringID() + " " + sourceInstrument.toString() + " " + targetInstrument.toString());
        
        Connection conn = null;
        CallableStatement proc = null;
        ResultSet results = null;

        try
        {
            String storedProc = "{ ? = call xtract_sbe16data_selector"
                                 + "("
                                 + sourceInstrument.getInstrumentID()
                                 + " , "
                                 + StringUtilities.quoteString(selectedMooring.getMooringID())
                                 + ") }";
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

                SBE16Data sbe = new SBE16Data();
                sbe.setData(data);

                dataSet.add(sbe);
            }

            results.close();
            proc.close();
            
            logger.info("source rows " + dataSet.size());
        
            insertData();
                        
            conn.setAutoCommit(true);
        }
        catch(SQLException sex)
        {
            logger.error(sex);
        }
        catch (Exception e)
        {
            logger.error(e);
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
                sex.printStackTrace();
                logger.error(sex);
            }
        }
    }
    
    private void insertData()
    {
        boolean ok = true;

        for(int i = 0; i < dataSet.size(); i++)
        {
            SBE16Data sbe = dataSet.get(i);
            ProcessedInstrumentData row = new ProcessedInstrumentData();

            row.setDataTimestamp(sbe.dataTimestamp);
            row.setDepth(sbe.instrumentDepth);
            row.setInstrumentID(targetInstrument.getInstrumentID());
            row.setLatitude(selectedMooring.getLatitudeIn());
            row.setLongitude(selectedMooring.getLongitudeIn());
            row.setMooringID(selectedMooring.getMooringID());
            row.setParameterCode("TEMP");
            row.setParameterValue(sbe.temperatureValue);
            row.setSourceFileID(sbe.sourceFileID);
            row.setQualityCode("RAW");

            ok = row.insert();

            row.setParameterCode("PRES");
            row.setParameterValue(sbe.pressureValue);

            ok = row.insert();

            row.setParameterCode("CNDC");
            row.setParameterValue(sbe.conductivityValue);

            ok = row.insert();
            
            row.setParameterCode("PSAL");
            row.setParameterValue(sbe.calculatedSalinityValue);
            row.setQualityCode("DERIVED");

            ok = row.insert();

        }
    }
    
    private class SBE16Data
    {
        public Timestamp dataTimestamp;
        public Integer sourceFileID;
        public Double instrumentDepth;

        public Double temperatureValue;
        public Double pressureValue;
        public Double conductivityValue;

        public Double calculatedSalinityValue;


        public void setData(Vector row)
        {
            int i = 0;

            dataTimestamp = (Timestamp) row.elementAt(i++);
            sourceFileID = ((Number)row.elementAt(i++)).intValue();
            instrumentDepth = ((Number)row.elementAt(i++)).doubleValue();

            temperatureValue = ((Number)row.elementAt(i++)).doubleValue();
            pressureValue = ((Number)row.elementAt(i++)).doubleValue();
            conductivityValue = ((Number)row.elementAt(i++)).doubleValue();
            //
            // conductivity is recorded in different units to what the salinity calculator requires
            // so has to be multiplied by 10
            //
            calculatedSalinityValue = SalinityCalculator.calculateSalinityForITS90Temperature(temperatureValue,
                                                                                            conductivityValue * 10,
                                                                                            pressureValue
                                                                                            );

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
}
