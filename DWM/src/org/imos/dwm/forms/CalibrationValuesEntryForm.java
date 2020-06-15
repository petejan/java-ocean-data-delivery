/*
 * IMOS data delivery project
 * Written by Peter Wiley
 * This code is copyright (c) Peter Wiley 2000 - ?
 * It is made available under the BSD Software Licence in the hope that it may be useful.
 * It has NO WARRANTY OF FITNESS OR SUITABILITY FOR ANY PURPOSE.
 * Feel free to fix any bugs that you may find.
 */

/*
 * CalibrationValuesEntryForm.java
 *
 * Created on Mar 4, 2012, 6:40:49 PM
 */

package org.imos.dwm.forms;

import java.awt.Frame;
import java.sql.SQLException;
import java.util.TimeZone;
import java.util.logging.Level;
import javax.swing.*;
import javax.swing.table.TableColumnModel;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.imos.dwm.dbms.*;
import org.imos.dwm.dbms.fields.MooringCombo;
import org.wiley.core.Common;
import org.wiley.core.forms.MemoryWindow;
import org.wiley.table.ComboBoxEditor;
import org.wiley.table.EditableJTable;
import org.wiley.table.StringEditor;
import org.wiley.util.SettableCaseJTextField;

/**
 *
 * @author peter
 */
public class CalibrationValuesEntryForm extends MemoryWindow
{
    private static Logger logger = Logger.getLogger(CalibrationValuesEntryForm.class.getName());

    private JFrame parentFrame = null;
    private EditableJTable parameterDataTable = new EditableJTable();

    private InstrumentCalibrationFile currentCalFile = null;
    private Instrument currentInstrument = null;
    private Mooring selectedMooring = null;

    private InstrumentCalibrationValueCollection collection = null;

    /** Creates new form CalibrationValuesEntryForm */
    public CalibrationValuesEntryForm()
    {
        initComponents();
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
    }

    public void setParent(Frame parent)
    {
        parentFrame = (JFrame) parent;
    }

    public void setCalibrationFile(InstrumentCalibrationFile f)
    {
        currentCalFile = f;
    }

    @Override
    public void initialise()
    {
        super.initialise();

        if(currentCalFile != null)
        {
            currentInstrument = Instrument.selectByInstrumentID(currentCalFile.getInstrumentID());

            calFileDisplayField.setText(currentCalFile.getFileName());
            instrumentDisplayField.setText(currentInstrument.getMake()
                                            + "/"
                                            + currentInstrument.getModel()
                                            + "/"
                                            + currentInstrument.getSerialNumber()
                                            );
        }

        this.setVisible(true);
    }


    private void saveUncommittedEdits()
    {
       if(parameterDataTable != null)
        {
            //
            // see if there is an outstanding row not saved
            //
            boolean finishedEditingOK = parameterDataTable.finishEditing();
            if(! finishedEditingOK)
            {
                Common.showMessage(this,"Save Failure","Save of data on focused row failed.");
            }

        }
    }

    @Override
    protected void cleanup()
    {
        saveUncommittedEdits();
        super.cleanup();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        topPanel = new javax.swing.JPanel();
        mooringCombo1 = new org.imos.dwm.dbms.fields.MooringCombo();
        mooringDescriptionField = new org.wiley.util.basicField();
        instrumentDisplayField = new org.wiley.util.basicField();
        calFileDisplayField = new org.wiley.util.basicField();
        jPanel1 = new javax.swing.JPanel();
        runButton = new javax.swing.JButton();
        cloneButton = new javax.swing.JButton();
        cloneValuesButton = new javax.swing.JButton();
        dataScrollPane = new javax.swing.JScrollPane();
        dataTable = new javax.swing.JTable();
        buttonPanel = new javax.swing.JPanel();
        quitButton = new javax.swing.JButton();

        setTitle("Calibration Values Entry Form");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        topPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        mooringCombo1.setDescriptionField(mooringDescriptionField);
        mooringCombo1.setOrientation(0);

        mooringDescriptionField.setEnabled(false);

        instrumentDisplayField.setEnabled(false);
        instrumentDisplayField.setLabel("Instrument");

        calFileDisplayField.setEnabled(false);
        calFileDisplayField.setLabel("Calibration File");

        runButton.setText("Run");
        runButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                runButtonActionPerformed(evt);
            }
        });
        jPanel1.add(runButton);

        cloneButton.setText("Clone For New Mooring");
        cloneButton.setEnabled(false);
        cloneButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cloneButtonActionPerformed(evt);
            }
        });
        jPanel1.add(cloneButton);

        cloneValuesButton.setText("Clone For New Instrument/Mooring");
        cloneValuesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cloneValuesButtonActionPerformed(evt);
            }
        });
        jPanel1.add(cloneValuesButton);

        org.jdesktop.layout.GroupLayout topPanelLayout = new org.jdesktop.layout.GroupLayout(topPanel);
        topPanel.setLayout(topPanelLayout);
        topPanelLayout.setHorizontalGroup(
            topPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, topPanelLayout.createSequentialGroup()
                .add(topPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, topPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 622, Short.MAX_VALUE))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, topPanelLayout.createSequentialGroup()
                        .add(20, 20, 20)
                        .add(instrumentDisplayField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 622, Short.MAX_VALUE))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, topPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .add(calFileDisplayField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 622, Short.MAX_VALUE))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, topPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .add(mooringCombo1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 169, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(mooringDescriptionField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 453, Short.MAX_VALUE)))
                .addContainerGap())
        );
        topPanelLayout.setVerticalGroup(
            topPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(topPanelLayout.createSequentialGroup()
                .addContainerGap(20, Short.MAX_VALUE)
                .add(instrumentDisplayField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(calFileDisplayField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(topPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, mooringCombo1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 18, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, mooringDescriptionField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(17, 17, 17))
        );

        dataTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Param Code", "Description", "Type", "Value"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Object.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        dataScrollPane.setViewportView(dataTable);

        buttonPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        quitButton.setText("Quit");
        quitButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                quitButtonActionPerformed(evt);
            }
        });
        buttonPanel.add(quitButton);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, buttonPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 666, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, dataScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 666, Short.MAX_VALUE)
                    .add(topPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .add(28, 28, 28))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(20, 20, 20)
                .add(topPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(dataScrollPane, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 410, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(buttonPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 36, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void quitButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_quitButtonActionPerformed
        cleanup();
    }//GEN-LAST:event_quitButtonActionPerformed

    private void runButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_runButtonActionPerformed
        
        selectedMooring = mooringCombo1.getSelectedMooring();
        if(selectedMooring == null)
        {
            Common.showMessage(this,"No Selected Mooring","You must select a mooring before selecting/adding data");
            return;
        }

        cloneButton.setEnabled(true);
        fetchDataForFile();
    }//GEN-LAST:event_runButtonActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        cleanup();
    }//GEN-LAST:event_formWindowClosing

    private void cloneButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cloneButtonActionPerformed
        if(selectedMooring == null)
        {
            Common.showMessage(this,"No Selected Mooring","You must select a mooring before cloning data");
            return;
        }

        String[] ConnectOptionNames = { "Select", "Cancel" };
        MooringCombo mc = new MooringCombo();

        int selectedOption = JOptionPane.showOptionDialog(
                                                      this,
                                                      mc,
                                                      "Select Target Mooring For Values Clone",
                                                      JOptionPane.DEFAULT_OPTION,
                                                      JOptionPane.INFORMATION_MESSAGE,
                                                      null,
                                                      ConnectOptionNames,
                                                      ConnectOptionNames[0]
                                                      )
                                                      ;

        if (selectedOption == 0)
        {
            // got something
            Mooring targetMooring = mc.getSelectedMooring();
            if(targetMooring != null)
            {
                logger.debug("Selected mooring " + targetMooring.getMooringID());
                
                for(int i = 0; i < collection.getRowCount(); i++)
                {
                    InstrumentCalibrationValue v = (InstrumentCalibrationValue) collection.getSelectedRow(i);
                    
                    try
                    {
                        InstrumentCalibrationValue v2 = (InstrumentCalibrationValue) v.clone();
                        v2.setMooringID(targetMooring.getMooringID());
                        v2.insert();
                        Common.getConnection().commit();
                    }
                    catch(CloneNotSupportedException cne)
                    {
                        logger.error(cne);
                    }
                    catch (SQLException ex)
                    {
                        logger.error(ex);
                    }
                }
            }
        }
    }//GEN-LAST:event_cloneButtonActionPerformed

    private void cloneValuesButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_cloneValuesButtonActionPerformed
    {//GEN-HEADEREND:event_cloneValuesButtonActionPerformed
        CalibrationValuesCloneForm form = new CalibrationValuesCloneForm(this, true);
        form.setVisible(true);
     
        fetchDataForFile();
    }//GEN-LAST:event_cloneValuesButtonActionPerformed

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
            Common.build("ABOS.properties");
        }

        CalibrationValuesEntryForm form = new CalibrationValuesEntryForm();
        form.initialise();
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel buttonPanel;
    private org.wiley.util.basicField calFileDisplayField;
    private javax.swing.JButton cloneButton;
    private javax.swing.JButton cloneValuesButton;
    private javax.swing.JScrollPane dataScrollPane;
    private javax.swing.JTable dataTable;
    private org.wiley.util.basicField instrumentDisplayField;
    private javax.swing.JPanel jPanel1;
    private org.imos.dwm.dbms.fields.MooringCombo mooringCombo1;
    private org.wiley.util.basicField mooringDescriptionField;
    private javax.swing.JButton quitButton;
    private javax.swing.JButton runButton;
    private javax.swing.JPanel topPanel;
    // End of variables declaration//GEN-END:variables

    private void fetchDataForFile()
    {
        collection = new InstrumentCalibrationValueCollection();
        
        collection.setSelectedMooring(selectedMooring);
        collection.setParentCalibrationFile(currentCalFile);
        collection.loadFromPersistentStore();
        collection.addBlankRows(20);
        collection.setParentFrame(this);

        parameterDataTable.setModel(collection);
        parameterDataTable.initialise();

        dataScrollPane.remove(dataTable);
        dataScrollPane.getViewport().add(parameterDataTable, null);

        String[] dataTypes = InstrumentCalibrationValue.definedDataTypes;

        CBBox selector = new CBBox();
        for(int i = 0; i < dataTypes.length; i++)
        {
            selector.addItem(dataTypes[i]);
        }

        ComboBoxEditor cbe = new ComboBoxEditor(selector, collection);

        StringEditor us = new StringEditor(new SettableCaseJTextField("MIXED"), collection);

        parameterDataTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        TableColumnModel tm = parameterDataTable.getColumnModel();
        tm.getColumn(0).setCellEditor(us);      // parameter code
        tm.getColumn(2).setCellEditor(cbe);     // defined data types

        int[] columnWidths = collection.getColumnWidths();
        for(int i = 0; i < tm.getColumnCount(); i++)
        {
            tm.getColumn(i).setMinWidth(columnWidths[i]);
            tm.getColumn(i).setPreferredWidth(columnWidths[i]);
        }

        dataScrollPane.validate();
        parameterDataTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        dataScrollPane.validate();
    }

    private class CBBox extends JComboBox
    {
        public CBBox()
        {
            super();
        }

        @Override
        public void setSelectedItem( Object val )
        {
            //logger.debug("Setting selected item to " + val);
            if(val != null && val instanceof String)
                super.setSelectedItem( ((String)val).trim());
        }
    }

}