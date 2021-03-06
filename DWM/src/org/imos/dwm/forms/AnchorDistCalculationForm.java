/*
 * IMOS data delivery project
 * Written by Peter Wiley
 * This code is copyright Peter Wiley 1998-2008
 * It is made available under the BSD Software Licence in the hope that it may be useful.
 * It has NO WARRANTY OF FITNESS OR SUITABILITY FOR ANY PURPOSE.
 * Feel free to fix any bugs that you may find.
 *
 */
package org.imos.dwm.forms;

import java.awt.Color;
import java.io.IOException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.geotools.nature.SeaWater;
import org.geotools.referencing.GeodeticCalculator;
import org.imos.dwm.calc.OxygenSolubilityCalculator;
import org.imos.dwm.calc.SalinityCalculator;
import org.imos.dwm.calc.SeawaterParameterCalculator;
import org.imos.dwm.dbms.Instrument;
import org.imos.dwm.dbms.InstrumentCalibrationFile;
import org.imos.dwm.dbms.Mooring;
import org.imos.dwm.dbms.ProcessedInstrumentData;
import org.imos.dwm.dbms.RawInstrumentData;
import org.wiley.core.Common;
import org.wiley.core.forms.MemoryWindow;
import org.wiley.util.StringUtilities;
import org.wiley.util.TextFileLogger;

/**
 *
 * @author peter
 */
public class AnchorDistCalculationForm extends MemoryWindow implements DataProcessor
{

    private static Logger logger = Logger.getLogger(AnchorDistCalculationForm.class.getName());

    private Mooring selectedMooring =null;

//    private ArrayList<SeaWaterData> dataSet = new ArrayList();
    /**
     * Creates new form SeabirdSBE16CalculationForm
     */
    public AnchorDistCalculationForm()
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
    private void initComponents()
    {

        jPanel1 = new javax.swing.JPanel();
        mooringCombo1 = new org.imos.dwm.dbms.fields.MooringCombo();
        mooringDescriptionField = new org.wiley.util.basicField();
        deleteDataBox = new javax.swing.JCheckBox();
        jPanel2 = new javax.swing.JPanel();
        runButton = new javax.swing.JButton();
        quitButton = new javax.swing.JButton();

        setTitle("Anchor Distance Data Processor Form");
        addWindowListener(new java.awt.event.WindowAdapter()
        {
            public void windowClosing(java.awt.event.WindowEvent evt)
            {
                formWindowClosing(evt);
            }
        });

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        mooringCombo1.setOrientation(0);
        mooringCombo1.addPropertyChangeListener(new java.beans.PropertyChangeListener()
        {
            public void propertyChange(java.beans.PropertyChangeEvent evt)
            {
                mooringCombo1PropertyChange(evt);
            }
        });

        mooringDescriptionField.setEnabled(false);

        deleteDataBox.setSelected(true);
        deleteDataBox.setText("Delete any existing processed data for target instrument");
        deleteDataBox.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
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
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(mooringCombo1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 169, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(mooringDescriptionField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 343, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(deleteDataBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 398, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(71, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, mooringCombo1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 21, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, mooringDescriptionField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(73, 73, 73)
                .add(deleteDataBox)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
                .addContainerGap(273, Short.MAX_VALUE))
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
        }
    }//GEN-LAST:event_mooringCombo1PropertyChange

    private void deleteDataBoxActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_deleteDataBoxActionPerformed
    {//GEN-HEADEREND:event_deleteDataBoxActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_deleteDataBoxActionPerformed
    
    @Override
    public String paramToString()
    {
        String s = "MOORING=" + selectedMooring.getMooringID();
        return s;
    }
    
    private void runButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_runButtonActionPerformed
    {//GEN-HEADEREND:event_runButtonActionPerformed

        //
        // hard-coded instrument for testing etc
        //
        //sourceInstrument = Instrument.selectByInstrumentID(4);

        selectedMooring = mooringCombo1.getSelectedMooring();

        if (selectedMooring == null)
        {
            Common.showMessage(this, "No Mooring Selected", "You must select a mooring before running any calculations");
            return;
        }

        if (deleteDataBox.isSelected())
        {
                RawInstrumentData.deleteDataForMooringAndParameter(selectedMooring.getMooringID(), "ADIST");
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
        Common.build("ABOS.properties");

        AnchorDistCalculationForm form = new AnchorDistCalculationForm();

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
        Matcher mat = Pattern.compile("(?:MOORING= *)(([^,]*))").matcher(s);
        mat.find();
        
        String mooringId = mat.group(2);
                
        selectedMooring = Mooring.selectByMooringID(mooringId);
        
        return true;
    }

    public void calculateDataValues()
    {
        logger.info("Calculate " + selectedMooring.getMooringID());
        
        Connection conn = null;
        Statement proc = null;
        ResultSet results = null;

        try
        {
            String tab;
            conn = Common.getConnection();
            conn.setAutoCommit(false);
            proc = conn.createStatement();
            
            tab = "SELECT data_timestamp, source_file_id, instrument_id, depth, parameter_value AS xpos, depth AS pres INTO TEMP pos FROM raw_instrument_data WHERE parameter_code = 'XPOS' AND quality_code != 'BAD' AND mooring_id = "+StringUtilities.quoteString(selectedMooring.getMooringID()) +" ORDER BY data_timestamp";
            proc.execute(tab);
            
            tab = "ALTER TABLE pos ADD ypos  numeric";
            proc.execute(tab);            
            tab = "UPDATE pos SET ypos = d.parameter_value FROM raw_instrument_data d WHERE d.data_timestamp = pos.data_timestamp AND parameter_code = 'YPOS' AND d.instrument_id = pos.instrument_id";
            proc.execute(tab);

            
            proc.execute("SELECT data_timestamp, source_file_id, instrument_id, depth, xpos, ypos FROM pos");
            results = (ResultSet) proc.getResultSet();
            ResultSetMetaData resultsMetaData = results.getMetaData();
            int colCount        = resultsMetaData.getColumnCount();

            conn.setAutoCommit(false);
            results.setFetchSize(50);
            
            Vector data = new Vector();
            
            RawInstrumentData row = new RawInstrumentData();  
            row.setLatitude(selectedMooring.getLatitudeIn());
            row.setLongitude(selectedMooring.getLongitudeIn());
            row.setMooringID(selectedMooring.getMooringID());

            Position pos = new Position();

            SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
            String $HOME = System.getProperty("user.home");

            //String filename = $HOME + "/sbe37_data_" + df.format(Common.current());
            String filename = "pos_data_" + df.format(Common.current());
            TextFileLogger file = new TextFileLogger(filename,"csv");

            try
            {
                String header = "Timestamp,"
                                + " xpos,"
                                + " ypos,"
                                ;

                file.open();

                file.receiveLine(header);

                System.out.println(header);
            
                boolean ok;
                boolean dataMissing;
                int count = 0;

                int numcol;
                Object o;
                while (results.next())
                {
                    dataMissing = false;
                    data.clear();
                    for (numcol = 1; numcol <= colCount; numcol++)
                    {
                        o = results.getObject(numcol);
                        if (!results.wasNull())
                        {
                            data.addElement(o);
                        }
                        else
                        {
                            data.addElement(null);
                            dataMissing = true;
                        }
                    }
                    //if (!dataMissing)
                    {
                        pos.setData(data);

                        System.out.println(pos.dataTimestamp + ","
                                + pos.xpos + ","
                                + pos.ypos + ","
                                + pos.calculatedDistance);

                        file.receiveLine(pos.dataTimestamp + ","
                                + pos.xpos + ","
                                + pos.ypos + ","
                                + pos.calculatedDistance);                        
                        
                        row.setDataTimestamp(pos.dataTimestamp);
                        row.setDepth(pos.instrumentDepth);
                        row.setSourceFileID(pos.sourceFileID);
                        row.setQualityCode("RAW");
                        row.setInstrumentID(pos.instrument_id);

                        if (pos.calculatedDistance != null)
                        {
                            row.setParameterCode("ADIST");
                            row.setParameterValue(pos.calculatedDistance);
                            row.setQualityCode("DERIVED");

                            ok = row.insert();
                            
                            row.setParameterCode("ADIR");
                            row.setParameterValue(pos.calculatedDirection);
                            row.setQualityCode("DERIVED");

                            ok = row.insert();
                            
                            count++;
                        }

                    }
                }

                results.close();

                logger.info("output rows " + count);
                
                String update = "UPDATE instrument_data_processors SET " 
                                    + "processing_date = '" + Common.current() + "',"
                                    + "count = "+ count
                                    + " WHERE "
                                    + "mooring_id = '" + selectedMooring.getMooringID() + "'"
                                    + " AND class_name = '" + this.getClass().getName() + "'"
                                    + " AND parameters = '" + paramToString()  + "'";

                Statement stmt;
                stmt = conn.createStatement();
                stmt.executeUpdate(update);
                logger.debug("Update processed table count " + count);

                file.close();
            }
            catch (IOException ioex)
            {
                logger.error(ioex);
            }
        
            proc.execute("DROP TABLE pos");
            
            proc.close();

//            insertData();
                        
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
    }
    
    private class Position
    {
        public Timestamp dataTimestamp;
        public Integer sourceFileID;
        public Double instrumentDepth;
        public Integer instrument_id;

        public Double xpos;
        public Double ypos;

        public Double calculatedDistance;
        public Double calculatedDirection;

        public void setData(Vector row)
        {
            int i = 0;

            dataTimestamp = (Timestamp) row.elementAt(i++);
            sourceFileID = ((Number)row.elementAt(i++)).intValue();
            instrument_id = ((Number)row.elementAt(i++)).intValue();
            instrumentDepth = ((Number)row.elementAt(i++)).doubleValue();

            xpos = ((Number)row.elementAt(i++)).doubleValue();
            ypos = ((Number)row.elementAt(i++)).doubleValue();
            
            
            GeodeticCalculator gc = new GeodeticCalculator();
            gc.setStartingGeographicPoint(xpos, ypos);
            gc.setDestinationGeographicPoint(selectedMooring.getLongitudeIn(), selectedMooring.getLatitudeIn());
            
            calculatedDistance = gc.getOrthodromicDistance()/1000;
            calculatedDirection = gc.getAzimuth();

        }
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox deleteDataBox;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private org.imos.dwm.dbms.fields.MooringCombo mooringCombo1;
    private org.wiley.util.basicField mooringDescriptionField;
    private javax.swing.JButton quitButton;
    private javax.swing.JButton runButton;
    // End of variables declaration//GEN-END:variables
}
