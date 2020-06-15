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

package org.imos.dwm.forms;

import java.awt.Color;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
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
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.interpolation.LinearInterpolator;
import org.apache.commons.math3.analysis.interpolation.SplineInterpolator;
import org.apache.commons.math3.analysis.interpolation.UnivariateInterpolator;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.imos.dwm.dbms.*;
import org.wiley.core.Common;
import org.wiley.core.forms.MemoryWindow;
import org.wiley.util.SQLWrapper;
import org.wiley.util.StringUtilities;

/**
 *
 * @author peter
 */
public class InterpolatedProcessedDataCreationForm extends MemoryWindow implements DataProcessor
{

    private static Logger logger = Logger.getLogger(InterpolatedProcessedDataCreationForm.class.getName());
    protected static SQLWrapper query = new SQLWrapper();

    private Mooring selectedMooring = null;
    final static long outputPeriod = 60 * 60 * 1000; // 1 hour in msec

    /** Creates new form ProcessedDataCreationForm */
    public InterpolatedProcessedDataCreationForm()
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
        mooringCombo1 = new org.imos.dwm.dbms.fields.MooringCombo();
        deleteDataBox = new javax.swing.JCheckBox();
        mooringDescriptionField = new org.wiley.util.basicField();

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

        if(deleteDataBox.isSelected())
        {
            logger.info("Deleting Data for " + selectedMooring.getMooringID());

            ProcessedInstrumentData.deleteDataForMooring(selectedMooring.getMooringID());
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

    private int interpolate(ParamsToProcess p, ResultSet results) throws SQLException
    {
        int count = 0;

        double[] x = null;
        double[] y = null;
        UnivariateInterpolator interpolator = new LinearInterpolator();
        UnivariateFunction function = null;

        int s = (int) ((p.start.getTime()) / (outputPeriod));
        int e = (int) ((p.end.getTime()) / (outputPeriod));
        count = e - s;

        x = new double[(int) p.count];
        y = new double[(int) p.count];
        String[] quality = new String[(int) p.count];

        ProcessedInstrumentData pid = new ProcessedInstrumentData();

        int i = 0;

        Timestamp ts;
        Integer sf = null;
        Double d = null;
        Double lat = null;
        Double lon = null;
        Double value = null;

        while (results.next())
        {
            ts = results.getTimestamp(1);
            sf = results.getInt(2);
            d = results.getDouble(3);
            lat = results.getDouble(4);
            lon = results.getDouble(5);
            value = results.getDouble(6);
            if (i < p.count)
            {
	            quality[i] = results.getString(7);

	            x[i] = ts.getTime();
	            y[i] = value;
	            i++;
            }
        }

        function = interpolator.interpolate(x, y);

        count = i;
        pid.setDepth(d);
        pid.setInstrumentID(p.instrument_id);
        pid.setLatitude(lat);
        pid.setLongitude(lon);
        pid.setMooringID(selectedMooring.getMooringID());
        pid.setParameterCode(p.param);
        pid.setSourceFileID(sf);
        double t;
        i = 0;
        pid.setQualityCode(quality[i]);
        for (t = s + 1; t < e; t++)
        {
        	while ( (i < count) && (x[i] < (t * outputPeriod)) )
        	{
        		i++;
        		if (i < p.count)
        			pid.setQualityCode(quality[i]);
        		else
        			pid.setQualityCode(quality[(int) (p.count-1)]);
                //logger.debug("Quality " + quality[i]);
        	}
        	pid.setDataTimestamp(new Timestamp((long) t * outputPeriod));
        	if (i < p.count)
        	{
        		pid.setParameterValue(function.value(t * outputPeriod) * p.coeffs[1] + p.coeffs[0]);

        		boolean ok = pid.insert();
        	}
        }


        return count;
    }

    private int decimate(ParamsToProcess p, ResultSet results) throws SQLException
    {
        int count = 0;
        int t;

        int s = (int)((p.start.getTime())/(outputPeriod));
        int e = (int)((p.end.getTime())/(outputPeriod));
        count = e - s;

        ProcessedInstrumentData pid = new ProcessedInstrumentData();

        Timestamp ts;
        Integer sf = null;
        Double d = null;
        Double lat = null;
        Double lon = null;
        Double value = null;
        String q = null;

        // Averaging goes here...
        Timestamp cellStart = new Timestamp(0);
        Timestamp cellEnd = new Timestamp(0);

        t = s + 1;

        cellStart.setTime(t * outputPeriod - outputPeriod/2);
        cellEnd.setTime(t * outputPeriod + outputPeriod/2);

        SummaryStatistics valueStats = new SummaryStatistics();

        while (results.next())
        {
            ts = results.getTimestamp(1);
            sf = results.getInt(2);
            d = results.getDouble(3);
            lat = results.getDouble(4);
            lon = results.getDouble(5);
            value = results.getDouble(6);
            q = results.getString(7);

            if (ts.after(cellEnd))
            {
                pid.setDepth(d);
                pid.setInstrumentID(p.instrument_id);
                pid.setLatitude(lat);
                pid.setLongitude(lon);
                pid.setMooringID(selectedMooring.getMooringID());
                pid.setParameterCode(p.param);
                pid.setSourceFileID(sf);
                pid.setQualityCode(q);
                pid.setDataTimestamp(new Timestamp((long)t * outputPeriod));
                pid.setParameterValue(valueStats.getMean() * p.coeffs[1] + p.coeffs[0]);
                //logger.debug(pid.getDataTimestamp() + " ," + pid.getParameterValue() + ": " + valueStats.getN() + " " + valueStats.getMean());
                //logger.debug("ts " + ts + " cell start " + cellStart + " cell end " + cellEnd);

                //logger.debug("print output time " + new Timestamp((long)t * outputPeriod));
                
                valueStats.clear();

                boolean ok = pid.insert();

                t++;

                cellStart.setTime(t * outputPeriod - outputPeriod/2);
                cellEnd.setTime(t * outputPeriod + outputPeriod/2);
            }
            if (ts.after(cellStart))
            {
                valueStats.addValue(value);
            }
        }

        return count;
    }

    public class ParamsToProcess
    {
        String param;
        double depth;
        int instrument_id;
        long count;
        double[] coeffs = {0.0, 1.0};
        Timestamp start;
        Timestamp end;
        int sampleTime;
        int sampleInterval;

        ParamsToProcess(Vector row)
        {
            depth = ((Number)(row.get(0))).doubleValue();
            param = ((String)(row.get(1))).trim();
            instrument_id = (Integer)(row.get(2));
            count = (Long)(row.get(3));
            start = (Timestamp)(row.get(4));
            end = (Timestamp)(row.get(5));
        }

        public String toString()
        {
            return "instrument=" + instrument_id + ", param=" + param + ", depth=" + depth + ", count=" + count;
        }

        public void addCalCoef(int i, double d)
        {
            coeffs[i] = d;
        }

        public void addSampleInterval(int i)
        {
            sampleInterval = i;
        }
        public void addSampleTime(int i)
        {
            sampleTime = i;
        }
    }

    public void calculateDataValues()
    {
        // get a list of paramters and information to work with
        // TODO: NTRI has issues because we have many samples per hour (40 sec burst), need to make a NTRI averaged product

        String SQL = "SELECT depth, parameter_code, instrument_id, count(*), min(data_timestamp), max(data_timestamp) " +
                        "FROM raw_instrument_data JOIN parameters ON (parameter_code = code) " +
                        "WHERE mooring_id = " + StringUtilities.quoteString(selectedMooring.getMooringID()) + " " +
//                        " AND parameter_code in ('TEMP', 'CNDC', 'PSAL', 'DENSITY', 'OXSOL', 'SBE43_OXY_VOLTAGE', 'OPTODE_BPHASE', 'OPTODE_TEMP', 'OPTODE_VOLT', 'DOX2', 'PRES', " +
//                                                "'TOTAL_GAS_PRESSURE', 'GTD_TEMPERATURE', 'PAR', 'NTU', 'CAPH', 'NTRI', 'CPHL', 'ECO_FLNTUS_CHL', 'ECO_FLNTUS_TURB', 'TURB', 'XPOS', "+
//                                                "'YPOS', 'SIG_WAVE_HEIGHT', " +
//                                                "'PCO2', 'PCO2_AIR', 'MLD', 'WSPD', 'AIRT') " +
//                        " AND parameter_code in ('XPOS', 'YPOS') " +
                        " AND process = 'Y' " +
                        " AND quality_code != 'BAD'" +
                        " AND quality_code != 'INTERPOLATED'" +
                        " GROUP BY depth, parameter_code, instrument_id " +
                        "ORDER BY depth, parameter_code";

        logger.debug(SQL);

        query.setConnection(Common.getConnection());
        query.executeQuery(SQL);
        Vector attributeSet = query.getData();
        ArrayList<ParamsToProcess> params = new ArrayList();

        if (attributeSet != null && attributeSet.size() > 0)
        {
            for (int i = 0; i < attributeSet.size(); i++)
            {
                Vector row = (Vector) attributeSet.get(i);

                ParamsToProcess p = new ParamsToProcess(row);
                params.add(p);

                logger.debug("Param " + p);
            }
        }
        try
        {
            Common.getConnection().setAutoCommit(false);
        }
        catch (SQLException ex)
        {
            logger.debug(ex);
        }
        // Iterate over each parameter to process
        int count = 0;
        int totalCount = 0;
        for (ParamsToProcess p : params)
        {
            logger.info("----");
            logger.info("Processing " + p);
            ArrayList<InstrumentCalibrationValue> calValues = InstrumentCalibrationValue.selectByInstrumentAndMooring(p.instrument_id, selectedMooring.getMooringID());
            //ArrayList<InstrumentCalibrationValue> calValues = InstrumentCalibrationValue.selectByInstrument(p.instrument_id);

            // get any calibration values that should be applied to this paramter
            if(calValues != null && calValues.size() > 0)
            {
                for(int i = 0; i < calValues.size(); i++)
                {
                    if (calValues.get(i).getParameterCode().trim().compareTo(p.param.trim()) == 0)
                    {
                        logger.info("Parameter has Calibration " + calValues.get(i).getParameterValue());

                        StringTokenizer st = new StringTokenizer(calValues.get(i).getParameterValue(), ",");

                        int j = 0;
                        while(st.hasMoreTokens())
                        {
                            String s = st.nextToken();
                            try
                            {
                                Double d = new Double(s);
                                p.addCalCoef(j, d);
                                logger.debug("calculateDataValues::calibration:parseData: " + j + " input " + s + " " + p.coeffs[j]);
                                j++;
                            }
                            catch(NumberFormatException nex)
                            {
                                logger.error(nex);
                            }
                        }

                    }
                }
            }

            // get the data sampling time and sample interval

            SQL = "SELECT min(timestamp_in) AS in, " +
                    "	max(timestamp_out)AS out, " +
                    "	date_trunc('hour', data_timestamp) AS hour, "+
                    "   count(*), " +
                    "	min(((date_part('epoch', data_timestamp)::integer) + 1800) % 3600) -1800 as sample_time_min, " +
                    "	avg(((date_part('epoch', data_timestamp)::integer) + 1800) % 3600) -1800 as sample_time_avg, " +
                    "	max(((date_part('epoch', data_timestamp)::integer) + 1800) % 3600) -1800 as sample_time_max " +
                            "FROM raw_instrument_data JOIN mooring USING (mooring_id) " +
                            "WHERE mooring_id = " + StringUtilities.quoteString(selectedMooring.getMooringID()) + " " +
                            " AND parameter_code = " + StringUtilities.quoteString(p.param) + " " +
                            " AND instrument_id = " + p.instrument_id +
                            " AND quality_code != 'BAD'" +
                            " AND data_timestamp BETWEEN timestamp_in AND timestamp_out "+
                            " GROUP BY date_trunc('hour', data_timestamp) " +
                            "ORDER BY 3";

            logger.debug(SQL);

            query.setConnection(Common.getConnection());
            query.executeQuery(SQL);
            attributeSet = query.getData();
            Timestamp sample_1 = null;
            Timestamp sample_N = null;
            double sampleTime = 0.0;
            double hourCount = 0;

            if (attributeSet != null && attributeSet.size() > 2)
            {
                for (int i = 0; i < attributeSet.size(); i++)
                {
                    Vector row = (Vector) attributeSet.get(i);
                    if (sample_1 == null)
                    {
                        sample_1 = (Timestamp) row.get(2);
                    }
                    sample_N = (Timestamp) row.get(2);

                    sampleTime = ((Number)(row.get(5))).doubleValue();
                    hourCount += ((Number)(row.get(3))).doubleValue();
                }
                long interval = sample_N.getTime()/1000 - sample_1.getTime()/1000;

                p.addSampleTime((int) sampleTime);
                p.addSampleInterval((int) (interval/hourCount));
            }

            logger.info("Data Points     : " + p.count);
            logger.info("Start Time      : " + p.start + " end " + p.end);
            logger.info("Sample Time     : " + p.sampleTime);
            logger.info("Sample Interval : " + p.sampleInterval);

            Connection conn = null;
            Statement proc = null;
            ResultSet results = null;

            // now finally select the data
            if (p.count > 100)
            {
                try
                {
                    String tab;
                    conn = Common.getConnection();
                    proc = conn.createStatement();

                    tab = "SELECT data_timestamp, source_file_id, depth, latitude, longitude, parameter_value, quality_code FROM raw_instrument_data " +
                            " WHERE mooring_id = " + StringUtilities.quoteString(selectedMooring.getMooringID()) + " " +
                                    " AND parameter_code = " + StringUtilities.quoteString(p.param) + " " +
                                    " AND instrument_id = " + p.instrument_id +
                                    " AND quality_code != 'BAD'" +
                            " ORDER BY data_timestamp";

                    logger.debug(tab);

                    proc.execute(tab);
                    results = (ResultSet) proc.getResultSet();

                    conn.setAutoCommit(false);
                    results.setFetchSize(50);

                    count = 0;
                    if ((p.sampleInterval > (10 * 60)) || (p.count < 1000)) // should use (p.end - p.start) in hours maybe
                    {
                        count = interpolate(p, results);
                        logger.info("interpolate count " + count);
                    }
                    else
                    {
                        count = decimate(p, results);
                        logger.info("decimate count " + count);
                    }
                    totalCount += count;

                    conn.commit();
                }
                catch(SQLException sex)
                {
                    logger.error(sex);
                }
            }
        }

        String update = "UPDATE instrument_data_processors SET "
                            + "processing_date = '" + Common.current() + "',"
                            + "count = "+ totalCount
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
            logger.debug("Update processed table count " + totalCount);
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

        InterpolatedProcessedDataCreationForm form = new InterpolatedProcessedDataCreationForm();

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
    private org.imos.dwm.dbms.fields.MooringCombo mooringCombo1;
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