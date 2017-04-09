/*
 * IMOS data delivery project
 * Written by Peter Wiley
 * This code is copyright (c) Peter Wiley 2000 - ?
 * It is made available under the BSD Software Licence in the hope that it may be useful.
 * It has NO WARRANTY OF FITNESS OR SUITABILITY FOR ANY PURPOSE.
 * Feel free to fix any bugs that you may find.
 */

/*
 * InstrumentDataForm.java
 *
 * Created on Jan 5, 2012, 2:56:37 PM
 */

package org.imos.abos.forms;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;
import java.util.TimeZone;
import javax.swing.JFrame;
import org.apache.log4j.Logger;
import org.imos.abos.dbms.Instrument;
import org.imos.abos.dbms.InstrumentDataFile;
import org.imos.abos.dbms.Mooring;
import org.wiley.core.Common;
import org.wiley.core.forms.MemoryWindow;

/**
 *
 * @author peter
 */
public class InstrumentDataForm extends MemoryWindow
{

    private boolean isDataEditable = true;
    private JFrame parentFrame = null;
    private static Logger logger = Logger.getLogger(InstrumentDataForm.class.getName());

    private Instrument parentInstrument = null;
    private InstrumentDataFile editableItem = null;
    private InstrumentDataFile savedItem = null;

    private boolean insertMode          = true;
    private boolean updateMode          = false;

    boolean isUpdateMode = false;

    File optionsFile = null;
    
    /** Creates new form InstrumentDataForm */
    public InstrumentDataForm()
    {
        initComponents();
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
    }

    
    public class myFilter implements FileFilter
    {
        String filter;
        public myFilter(String s)
        {
            filter = s;
        }
        @Override
        public boolean accept(File pathname)
        {
            return pathname.getName().contains(filter);
        }        
    }
    @Override
    public void initialise()
    {
       super.initialise();

       if(! isDataEditable)
       {
           setFieldsEditable(isDataEditable);
           saveButton.setEnabled(isDataEditable);
           cancelButton.setEnabled(isDataEditable);

           saveButton.setVisible(isDataEditable);
           cancelButton.setVisible(isDataEditable);
       }
        String $HOME = System.getProperty("user.home");

        optionsFile = new File("ABOS.properties");
        
        BufferedReader br = null;
        try
        {
            Properties p = new Properties();
            br = new BufferedReader(new FileReader(optionsFile));
            p.load(br);
            String dir = p.getProperty("datafile-dir");
            File[] files = (new File(dir)).listFiles(new myFilter(parentInstrument.getSerialNumber()));
            if ((files == null) || (files.length < 1))
            {
                dataFileField.setDefaultDirectory(dir + "/*.*");                
            }
            else
            {
                dataFileField.setDefaultDirectory(files[0].getAbsolutePath());
            }
        }
        catch (FileNotFoundException ex)
        {
            logger.warn(ex);
        }
        catch (IOException ex)
        {
            logger.warn(ex);
        }
        finally
        {
            try
            {
                br.close();
            }
            catch (IOException ex)
            {
                logger.warn(ex);
            }
        }
       
    }

    public void setDataEditable(boolean b)
    {
        isDataEditable = b;
    }

    public void setParent(Frame parent)
    {
        parentFrame = (JFrame) parent;
    }

    private void setFieldsEditable(boolean editable)
    {
    }


    public InstrumentDataFile getInstrumentDataFile()
    {
        return savedItem;
    }

    public void setInstrument(Instrument ins)
    {
        parentInstrument = ins;
        instrumentIDPanel.setInstrument(ins);
    }
    
    public void setMooring(Mooring mooring)
    {
        mooringCombo.setSelectedItem(mooring.getMooringID());        
    }

    public void setNew()
    {
        editableItem = new InstrumentDataFile();
        savedItem = new InstrumentDataFile();

        updateMode = false;
        insertMode = true;
    }

    public void setNew(InstrumentDataFile newRow)
    {
        editableItem = newRow;
        savedItem = new InstrumentDataFile();

        updateMode = false;
        insertMode = true;
    }

    public void setInstrumentDataFile(InstrumentDataFile f)
    {
        savedItem = f;
        try
        {
            editableItem = (InstrumentDataFile)f.clone();
        }
        catch( CloneNotSupportedException cne )
        {
            logger.error( "attempt to clone object failed.");
            return;
        }

        instrumentIDPanel.setInstrument(parentInstrument);
        mooringCombo.setSelectedItem(savedItem.getMooringID());
        dataFileField.setFileName(savedItem.getFilePath());

        updateMode = true;
        insertMode = false;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        dataFileField = new org.wiley.util.FileSelectorField();
        buttonPanel = new javax.swing.JPanel();
        saveButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        quitButton = new javax.swing.JButton();
        mooringCombo = new org.imos.abos.dbms.fields.MooringCombo();
        mooringDescriptionField = new org.wiley.util.basicField();
        instrumentIDPanel = new org.imos.abos.dbms.fields.InstrumentIDPanel();

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        buttonPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        saveButton.setMnemonic('s');
        saveButton.setText("Save");
        saveButton.setPreferredSize(new java.awt.Dimension(70, 29));
        saveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveButtonActionPerformed(evt);
            }
        });

        cancelButton.setMnemonic('u');
        cancelButton.setText("Undo");
        cancelButton.setPreferredSize(new java.awt.Dimension(70, 29));
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        quitButton.setMnemonic('q');
        quitButton.setText("Quit");
        quitButton.setMaximumSize(new java.awt.Dimension(80, 29));
        quitButton.setPreferredSize(new java.awt.Dimension(70, 29));
        quitButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                quitButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout buttonPanelLayout = new org.jdesktop.layout.GroupLayout(buttonPanel);
        buttonPanel.setLayout(buttonPanelLayout);
        buttonPanelLayout.setHorizontalGroup(
            buttonPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, buttonPanelLayout.createSequentialGroup()
                .addContainerGap(232, Short.MAX_VALUE)
                .add(saveButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(cancelButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(110, 110, 110)
                .add(quitButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(71, 71, 71))
        );
        buttonPanelLayout.setVerticalGroup(
            buttonPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(buttonPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(buttonPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(quitButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(cancelButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(saveButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(8, Short.MAX_VALUE))
        );

        mooringCombo.setDescriptionField(mooringDescriptionField);
        mooringCombo.setOrientation(0);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(20, 20, 20)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(mooringCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 158, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(mooringDescriptionField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 466, Short.MAX_VALUE)
                        .addContainerGap())
                    .add(instrumentIDPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
            .add(layout.createSequentialGroup()
                .add(11, 11, 11)
                .add(buttonPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(dataFileField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 624, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(instrumentIDPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.CENTER)
                    .add(mooringCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(mooringDescriptionField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(8, 8, 8)
                .add(dataFileField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(buttonPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 58, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void saveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveButtonActionPerformed
        boolean OK = false;

        if ( insertMode )
        {
            
            //editableItem.setDataFilePrimaryKey(InstrumentDataFile.getNextSequenceNumber());
            editableItem.setInstrumentID(parentInstrument.getInstrumentID()) ;
            editableItem.setMooringID(mooringCombo.getSelectedItem());
            editableItem.setProcessingStatus("UNPROCESSED");
            editableItem.setSelectedFile(dataFileField.getSelectedFile());

            OK = editableItem.insert();

            if ( OK )
            {
                savedItem = editableItem;
                //
                // add this instrument to the list of attached instruments for the mooring
                //
                Mooring.assignAttachedInstrument(mooringCombo.getSelectedMooring().getMooringID(), 
                                                 parentInstrument.getInstrumentID(), 0.0);
                
                quitButtonActionPerformed( new ActionEvent(this, 0, "SUCCESS") );
            } 
            else
            {
                Common.showMessage("SQL Error on Insert", editableItem.getLastErrorMessage());
            }
        }
        else if ( updateMode )
        {
            editableItem.setInstrumentID(parentInstrument.getInstrumentID()) ;
            editableItem.setMooringID(mooringCombo.getSelectedItem());
            editableItem.setProcessingStatus("UNPROCESSED");
            editableItem.setSelectedFile(dataFileField.getSelectedFile());

            OK = editableItem.update();
            if ( OK )
            {
                quitButtonActionPerformed( new ActionEvent(this, 0, "SUCCESS") );
            } 
            else
            {
                Common.showMessage("SQL Error on Update", editableItem.getLastErrorMessage());
            }
        }
        if ( OK )
        {
            BufferedReader br = null;
            try
            {
                Properties p = new Properties();
                br = new BufferedReader(new FileReader(optionsFile));
                p.load(br);
                p.setProperty("datafile-dir", "" + dataFileField.getSelectedFile().getParent());
                BufferedWriter bw = new BufferedWriter(new FileWriter(optionsFile));
                p.store(bw, "ABOS user config");
            }
            catch (FileNotFoundException ex)
            {
                logger.warn(ex);
            }
            catch (IOException ex)
            {
                logger.warn(ex);
            }
            finally
            {
                try
                {
                    br.close();
                }
                catch (IOException ex)
                {
                    logger.warn(ex);
                }
            }
        }
        
}//GEN-LAST:event_saveButtonActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        if( updateMode)
        {
            instrumentIDPanel.setInstrument(parentInstrument);
            mooringCombo.setSelectedItem(savedItem.getMooringID());
            dataFileField.setFileName(savedItem.getFilePath());
        } 
        else
        {
            clearForm();
        }
}//GEN-LAST:event_cancelButtonActionPerformed

    public void clearForm()
    {
        mooringCombo.setSelectedItem("");
        dataFileField.setFileName("");
    }

    private void quitButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_quitButtonActionPerformed
        if( !(evt.getActionCommand().equalsIgnoreCase("SUCCESS")) )
        {
            // failed insert/update and user is quitting
            if (insertMode)
                savedItem = null;
            else if (updateMode)
                cancelButtonActionPerformed( null );
        }

        this.setVisible( false );
}//GEN-LAST:event_quitButtonActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        cleanup();
    }//GEN-LAST:event_formWindowClosing

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new InstrumentDataForm().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JButton cancelButton;
    private org.wiley.util.FileSelectorField dataFileField;
    private org.imos.abos.dbms.fields.InstrumentIDPanel instrumentIDPanel;
    private org.imos.abos.dbms.fields.MooringCombo mooringCombo;
    private org.wiley.util.basicField mooringDescriptionField;
    private javax.swing.JButton quitButton;
    private javax.swing.JButton saveButton;
    // End of variables declaration//GEN-END:variables

}
