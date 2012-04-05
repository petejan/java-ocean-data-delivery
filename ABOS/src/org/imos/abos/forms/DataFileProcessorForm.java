/*
 * Neonatal Screening Software Project
 * Written by Peter Wiley
 * This code is copyright (c) Peter Wiley 2000 - ?
 * It is made available under the BSD Software Licence in the hope that it may be useful.
 * It has NO WARRANTY OF FITNESS OR SUITABILITY FOR ANY PURPOSE.
 * Feel free to fix any bugs that you may find.
 */

/*
 * DataFileProcessorForm.java
 *
 * Created on Jan 7, 2012, 8:05:18 PM
 */

package org.imos.abos.forms;

import java.awt.Color;
import java.awt.Frame;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import org.apache.log4j.Logger;
import org.imos.abos.dbms.Instrument;
import org.imos.abos.dbms.InstrumentDataFile;
import org.imos.abos.dbms.InstrumentDataParser;
import org.imos.abos.dbms.Mooring;
import org.imos.abos.dbms.RawInstrumentData;
import org.imos.abos.parsers.AbstractDataParser;
import org.wiley.core.Common;
import org.wiley.core.forms.MemoryWindow;

/**
 *
 * @author peter
 */
public class DataFileProcessorForm extends MemoryWindow
{

    private JFrame parentFrame = null;
    private static Logger logger = Logger.getLogger(DataFileProcessorForm.class.getName());

    private InstrumentDataFile currentFile;
    private Instrument currentInstrument;
    private Mooring currentMooring;

    /** Creates new form DataFileProcessorForm */
    public DataFileProcessorForm()
    {
        initComponents();
    }

    @Override
    public void initialise()
    {
       super.initialise();
    }

    public void setParent(Frame parent)
    {
        parentFrame = (JFrame) parent;
    }

    public void setInstrumentDataFile(InstrumentDataFile f)
    {
        currentFile = f;
        currentInstrument = Instrument.selectByInstrumentID(currentFile.getInstrumentID());
        currentMooring = Mooring.selectByMooringID(currentFile.getMooringID());

        instrumentDataParserCombo.setSelectedItem(currentFile.getProcessingClass());
        overrideDepthField.setValue(currentFile.getInstrumentDepth());

        this.setTitle("Processing File " + currentFile.getFileName());
    }
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonPanel = new javax.swing.JPanel();
        runButton = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        messageArea = new javax.swing.JTextArea();
        instrumentDataParserCombo = new org.imos.abos.dbms.fields.InstrumentDataParserCombo();
        parserDescriptionField = new org.wiley.util.basicField();
        overrideDepthField = new org.wiley.util.labelledDecimalField();
        jLabel1 = new javax.swing.JLabel();

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        buttonPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        runButton.setText("Run");
        runButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                runButtonActionPerformed(evt);
            }
        });
        buttonPanel.add(runButton);

        messageArea.setColumns(20);
        messageArea.setRows(5);
        jScrollPane1.setViewportView(messageArea);

        instrumentDataParserCombo.setDescriptionField(parserDescriptionField);

        parserDescriptionField.setColonsSuppressed(true);
        parserDescriptionField.setEnabled(false);

        overrideDepthField.setDecimals(2);
        overrideDepthField.setLabel("Override Depth Value");

        jLabel1.setText("m");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 581, Short.MAX_VALUE)
                            .add(buttonPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 581, Short.MAX_VALUE)
                            .add(instrumentDataParserCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 369, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(parserDescriptionField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 525, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                    .add(layout.createSequentialGroup()
                        .add(28, 28, 28)
                        .add(overrideDepthField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 210, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jLabel1)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(instrumentDataParserCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(parserDescriptionField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(18, 18, 18)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.CENTER)
                    .add(overrideDepthField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 24, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(30, 30, 30)
                .add(buttonPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 251, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    public void updateMessageArea(String info)
    {
        messageArea.append(info);
    }

    private void runButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_runButtonActionPerformed

        final Object component;

        InstrumentDataParser ip = instrumentDataParserCombo.getSelectedItem();
        if(ip == null)
        {
            Common.showMessage(this, "No Selected Parser","You must select a parser to process the file.");
            return;
        }

        final String className = ip.getParserClassName().trim();
        try
        {
            
            component = Class.forName(className).newInstance();
            //
            // cast it to an AbstractDataParser as everything subclasses this
            //
            if(component instanceof AbstractDataParser)
            {
                final Color bg = runButton.getBackground();
                runButton.setText("Running...");
                runButton.setBackground(Color.RED);
                runButton.setForeground(Color.WHITE);

                Thread worker = new Thread()
                {
                    @Override
                    public void run()
                    {
                        //
                        // clean up any existing data...
                        //
                        RawInstrumentData.deleteDataForFile(currentFile.getDataFilePrimaryKey());

                        final AbstractDataParser parser = (AbstractDataParser) component;

                        parser.setParentForm(DataFileProcessorForm.this);
                        parser.setInstrument(currentInstrument);
                        parser.setInstrumentDataFile(currentFile);
                        parser.setMooring(currentMooring);
                        parser.setInstrumentDepth(overrideDepthField.getDoubleValue());
                        
                        parser.run();

                        currentFile.setProcessingDate(Common.current());
                        currentFile.setProcessingStatus("PROCESSED");
                        currentFile.setProcessingClass(className);
                        currentFile.setInstrumentDepth(parser.getInstrumentDepth());
                        currentFile.setFileHeaders(parser.getHeaders());

                        currentFile.update();

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
            }
            else
            {
                //
                // shouldn't happen - bitch so we know to fix it
                //
                Common.showMessage(this,
                        "Incorrect Inheritance",
                        "Class " + className + " does not inherit from AbstractDataParser, can't create!"
                        );
            }
        }
        catch(LinkageError le)
        {
            System.err.println("Link Error Failure creating " + className );
            le.printStackTrace();
        }
        catch(ClassNotFoundException cnfe)
        {
            Common.showMessage(this,
                        "Program Not Found",
                        "Class " + className + " is not in the classpath, can't create!\n"
                        +"(Check class name, path and spelling in the defining menu item)."
                        );
            System.err.println("Class not found exception for " + className );
            cnfe.printStackTrace();
        }
        catch(Exception exex)
        {
            System.err.println("Failure creating " + className);
            exex.printStackTrace();
        }
        
    }//GEN-LAST:event_runButtonActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        cleanup();
    }//GEN-LAST:event_formWindowClosing

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new DataFileProcessorForm().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel buttonPanel;
    private org.imos.abos.dbms.fields.InstrumentDataParserCombo instrumentDataParserCombo;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea messageArea;
    private org.wiley.util.labelledDecimalField overrideDepthField;
    private org.wiley.util.basicField parserDescriptionField;
    private javax.swing.JButton runButton;
    // End of variables declaration//GEN-END:variables

}
