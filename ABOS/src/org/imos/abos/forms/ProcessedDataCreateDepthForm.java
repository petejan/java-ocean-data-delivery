/*
 * IMOS data delivery project
 * Written by Peter Wiley
 * This code is copyright (c) Peter Wiley 2000 - ?
 * It is made available under the BSD Software Licence in the hope that it may be useful.
 * It has NO WARRANTY OF FITNESS OR SUITABILITY FOR ANY PURPOSE.
 * Feel free to fix any bugs that you may find.
 */

/*
 * InterpolatedDataCreationForm.java
 *
 * Created on Apr 4, 2012, 10:20:36 AM
 */

package org.imos.abos.forms;

import java.awt.Color;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.Vector;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.interpolation.LinearInterpolator;
import org.apache.commons.math3.analysis.interpolation.SplineInterpolator;
import org.apache.commons.math3.analysis.interpolation.UnivariateInterpolator;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.imos.abos.dbms.*;
import org.wiley.core.Common;
import org.wiley.core.forms.MemoryWindow;
import org.wiley.util.SQLWrapper;
import org.wiley.util.StringUtilities;

/**
 *
 * @author peter
 */
public class ProcessedDataCreateDepthForm extends MemoryWindow implements DataProcessor
{

    private static Logger logger = Logger.getLogger(ProcessedDataCreateDepthForm.class.getName());
    protected static SQLWrapper query = new SQLWrapper();        

    private Mooring selectedMooring = null;

    /** Creates new form ProcessedDataCreationForm */
    public ProcessedDataCreateDepthForm()
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
    private void initComponents()
    {

        jPanel2 = new javax.swing.JPanel();
        runButton = new javax.swing.JButton();
        quitButton = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        mooringCombo1 = new org.imos.abos.dbms.fields.MooringCombo();
        deleteDataBox = new javax.swing.JCheckBox();
        mooringDescriptionField = new org.wiley.util.basicField();

        addWindowListener(new java.awt.event.WindowAdapter()
        {
            public void windowClosing(java.awt.event.WindowEvent evt)
            {
                formWindowClosing(evt);
            }
        });

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

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        mooringCombo1.setOrientation(0);
        mooringCombo1.addPropertyChangeListener(new java.beans.PropertyChangeListener()
        {
            public void propertyChange(java.beans.PropertyChangeEvent evt)
            {
                mooringCombo1PropertyChange(evt);
            }
        });

        deleteDataBox.setSelected(true);
        deleteDataBox.setText("Delete any existing processed data for target instrument & parameter");
        deleteDataBox.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                deleteDataBoxActionPerformed(evt);
            }
        });

        mooringDescriptionField.setEnabled(false);

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
                    .add(deleteDataBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 488, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(73, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, mooringCombo1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 21, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, mooringDescriptionField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(145, 145, 145)
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

        selectedMooring = mooringCombo1.getSelectedMooring();

        if(selectedMooring == null)
        {
            Common.showMessage(this,"No Mooring Selected","You must select a mooring before running any calculations");
            return;
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

    public class InputDepths
    {
        ArrayList<Double> wireDepth;
        ArrayList<Double> measuredDepth;
        Timestamp ts;
        
        InputDepths(Timestamp i)
        {
            wireDepth = new ArrayList();
            measuredDepth = new ArrayList();
            ts = i;
        }
        void add(Double w, Double m)
        {
            wireDepth.add(w);
            measuredDepth.add(m);
        }
        
        public String toString()
        {
            return "timestamp=" + ts + ", wireDepth=" + wireDepth + ", measuredDepth=" + measuredDepth;
        }
    }

    public void calculateDataValues()
    {
        Connection con = Common.getConnection();
        int count = 0;

        try
        {
            PreparedStatement  st = con.prepareStatement ( "SELECT depth FROM processed_instrument_data WHERE data_timestamp = ? AND (depth > 10) AND (depth < 500) " +
                                                                 " mooring_id = " + StringUtilities.quoteString(selectedMooring.getMooringID()),
                                                           ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE );
            
            String SQL = "SELECT data_timestamp, instrument_id, depth, parameter_value " + 
                            "FROM processed_instrument_data " + 
                            "WHERE mooring_id = " + StringUtilities.quoteString(selectedMooring.getMooringID()) + " " +
                            " AND parameter_code = 'PRES'" +
                            " AND (depth > 10) AND (depth < 500)" +
                            " AND quality_code != 'BAD'" +
                            " ORDER BY data_timestamp, depth, instrument_id";

            query.setConnection(Common.getConnection());
            query.executeQuery(SQL);

            Vector measurementSet = query.getData();
            Timestamp lastTs = null;

            InputDepths id = null;

            double x[], y[];
            UnivariateInterpolator interpolator = new LinearInterpolator();

            if (measurementSet != null && measurementSet.size() > 0)
            {
                for (int i = 0; i < measurementSet.size(); i++)
                {
                    Vector row = (Vector) measurementSet.get(i);

                    Timestamp ts = ((Timestamp)(row.get(0)));
                    Integer instrument = (Integer)(row.get(1));                
                    Double wireDepth = ((Number)(row.get(2))).doubleValue();
                    Double measuredDepth = ((Number)(row.get(3))).doubleValue();

                    if ((lastTs == null) || (!lastTs.equals(ts)))
                    {
                        if (lastTs != null)
                        {
                            Double[] X = (Double[])id.measuredDepth.toArray();
                            x = new double[X.length];
                            x = ArrayUtils.toPrimitive(X);
                            Double[] Y = (Double[])id.measuredDepth.toArray();
                            y = new double[Y.length];
                            y = ArrayUtils.toPrimitive(Y);
                            UnivariateFunction function = interpolator.interpolate(x, y);

                            st.setTimestamp(1, ts);
                            ResultSet rs = st.executeQuery();
                            while (rs.next())
                            {
                                Double d = rs.getDouble(1);
                                rs.updateDouble(1, function.value(d));
                                rs.updateRow();
                            }
                            rs.close();
                            
                            count++;
                        }
                        id = new InputDepths(ts);                    
                    }
                    else
                    {
                        id.add(wireDepth, measuredDepth);                    
                    }
                    lastTs = (Timestamp)ts.clone();
                }
            }
        }
        catch (SQLException ex)
        {
            java.util.logging.Logger.getLogger(ProcessedDataCreateDepthForm.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        String update = "UPDATE instrument_data_processors SET " 
                            + "processing_date = '" + Common.current() + "',"
                            + "count = "+ count
                            + " WHERE "
                            + "mooring_id = '" + selectedMooring.getMooringID() + "'"
                            + " AND class_name = '" + this.getClass().getName() + "'"
                            + " AND parameters = '" + paramToString()  + "'";

        Connection conn = Common.getConnection();

        Statement stmt;
        try
        {
            stmt = conn.createStatement();
            stmt.executeUpdate(update);
            logger.debug("Update processed table count " + count);
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

        ProcessedDataCreateDepthForm form = new ProcessedDataCreateDepthForm();
        
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
    // End of variables declaration//GEN-END:variables

    @Override
    public String paramToString()
    {
        return "MOORING="+selectedMooring.getMooringID();
    }

    @Override
    public boolean setupFromString(String s)
    {
        Matcher mat = Pattern.compile("(?:MOORING= *)(([^,]*))").matcher(s);
        mat.find();
        
        String mooringId = mat.group(2);
        selectedMooring = selectedMooring = Mooring.selectByMooringID(mooringId);
        
        return true;
    }

}
