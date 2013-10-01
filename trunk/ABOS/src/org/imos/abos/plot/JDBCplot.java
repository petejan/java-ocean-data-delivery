/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.imos.abos.plot;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.TimeZone;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import org.apache.log4j.PropertyConfigurator;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.wiley.core.Common;
import org.wiley.util.SQLWrapper;

/**
 *
 * @author jan079
 */
public class JDBCplot extends JfreeChartDemo
{
    protected static SQLWrapper query = new SQLWrapper();
    static StringList parameter_code = new StringList("Parameters");
    static StringList mooring = new StringList("Moorings");
    static String table = "processed_instrument_data";
    static JTable mooringList = null;
    static JTable parameterList = null;
    static JComboBox tableList = null;
    
    public JDBCplot(final String title)
    {
        super(title);
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }
 
    public static class StringList
    {
        ArrayList<String> strings = new ArrayList<String>();
        String[] stringColNames = new String[1];
        JTable jtable = null;
        
        public StringList(String name)
        {
            stringColNames[0] = name;
        }
        
        public boolean add(String s)
        {
            return strings.add(s);
        }
        
        public String[][] asArray()
        {
            String [][] array = new String[strings.size()][1];
            for (int i = 0;i<strings.size();i++)
            {
                array[i][0] = (String)strings.get(i).trim();
            }
            
            return array;
        }
        
        public String toString()
        {
            if (jtable == null)
            {
                asJTable();
            }
            String s = new String();
            System.out.println(stringColNames[0] + ": Selected Rows " + jtable.getSelectedRowCount());
            
            if ((jtable == null) || (jtable.getSelectedRowCount() == 0))
            {
                for (int i = 0;i<strings.size();i++)
                {
                    s += strings.get(i).trim();
                    if (i < strings.size()-1)
                    {
                        s += "-";
                    }
                }
            }
            else
            {
                int[] rows = jtable.getSelectedRows();

                for (int i = 0;i<rows.length;i++)
                {
                    s += jtable.getValueAt(rows[i], 0).toString().trim();
                    if (i < rows.length-1)
                    {
                        s += "-";
                    }
                }
            }
            
            return s;
        }
        
        public String toSelectString()
        {
            String s = new String();
            if (jtable == null)
            {
                asJTable();
            }
            System.out.println(stringColNames[0] + ": Selected Rows " + jtable.getSelectedRowCount());
            
            if ((jtable == null) || (jtable.getSelectedRowCount() == 0))
            {
                for (int i = 0;i<strings.size();i++)
                {
                    s += "'" + strings.get(i).trim() + "'";
                    if (i < strings.size()-1)
                    {
                        s += ",";
                    }
                }
            }
            else
            {
                int[] rows = jtable.getSelectedRows();

                for (int i = 0;i<rows.length;i++)
                {
                    s += "'" + jtable.getValueAt(rows[i], 0).toString().trim() + "'";
                    if (i < rows.length-1)
                    {
                        s += ",";
                    }
                }
            }
            
            return s;
        }
        
        public JTable asJTable()
        {
            if (jtable == null)
            {               
                jtable =  new JTable(asArray(), stringColNames);
            }
            
            return jtable;
        }
    }
    
    public static JPanel createSelectPanel() 
    {
        JPanel p = new JPanel(new BorderLayout());
        
        query.setConnection(Common.getConnection());
        query.executeQuery("SELECT mooring_id FROM mooring ORDER BY mooring_id");
        Vector attributeSet = query.getData();
        if (attributeSet != null && attributeSet.size() > 0)
        {
            for (int i = 0; i < attributeSet.size(); i++)
            {
                Vector row = (Vector) attributeSet.get(i);
                mooring.add((String)(row.get(0)));   
            }
        }
        query.executeQuery("SELECT code FROM parameters ORDER BY parameters");
        attributeSet = query.getData();
        if (attributeSet != null && attributeSet.size() > 0)
        {
            for (int i = 0; i < attributeSet.size(); i++)
            {
                Vector row = (Vector) attributeSet.get(i);
                parameter_code.add((String)(row.get(0)));   
            }
        }
                
        mooringList = mooring.asJTable();
        mooringList.setColumnSelectionAllowed(false);
        parameterList = parameter_code.asJTable();
        
        JScrollPane mooringScrollPane = new JScrollPane(mooringList);
        JScrollPane parameterScrollPane = new JScrollPane(parameterList);
 
        //Lay out the demo.
        String[] tableStrings = { "processed_instrument_data", "raw_instrument_data"};

        tableList = new JComboBox(tableStrings);     
        
        p.add(tableList, BorderLayout.NORTH);
        
        p.add(mooringScrollPane, BorderLayout.WEST);
        p.add(parameterScrollPane, BorderLayout.EAST);
        
        JButton plotButton = new JButton("Plot");
        
        plotButton.addActionListener(new ActionListener() 
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                //mooring = ((String)mooringList.getSelectedItem()).trim();
                //parameter_code = ((String)parameterList.getSelectedItem()).trim();
                
                final JDBCplot demo = new JDBCplot(mooring.toString());
                demo.pdfName = mooring + "-" + parameter_code + ".pdf";
                
                demo.createChartPanel();
                demo.createAndShowGUI();
                demo.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
                demo.addWindowListener(new WindowAdapter() 
                {
                    public void windowClosing(WindowEvent e) 
                    {
                        System.out.println("WindowClosing");
                    }

                });
                
            }
        });
        
        p.add(plotButton, BorderLayout.SOUTH);
        
        p.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
        
        return p;
    }
 
    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
    private static void createAndShowSelectGUI() 
    {
        //Create and set up the window.
        JFrame frame = new JFrame("Extract Plot");
        //Create and set up the content pane.
        JComponent newContentPane = createSelectPanel();
        newContentPane.setOpaque(true); //content panes must be opaque
        frame.setContentPane(newContentPane);
        //Display the window.
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }
    
    public class DepthParam
    {
        String param;
        double depth;
        int instrument;        
        String make;
        
        public DepthParam(String p, double d, int i, String m)
        {
            param = p;
            depth = d;
            instrument = i;
            make = m;
        }
        
        public String toString()
        {
            return make + "-" + param + "-" + depth + "m";
        }
    }

    @Override
    protected TimeSeriesCollection createDataset()
    {
        final TimeSeriesCollection collection = new TimeSeriesCollection();

//        SwingUtilities.invokeLater(new Runnable() 
//        {
//            public void run() 
//            {
                try
                {            
                    System.out.println("Param " + parameter_code + " plot " + plot);
                    
                    // plot.getRangeAxis().setLabel(parameter_code.toString());

                    //Class.forName("org.postgresql.Driver");
                    //Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost/ABOS", "peter", "password");
                    Connection connection = Common.getConnection();
                    ResultSet resultSet = null;
                    Statement statement = null;
                    if (tableList != null)
                    {
                        table = ((String)tableList.getSelectedItem()).trim();
                    }
                    String where = "mooring_id IN (" + mooring.toSelectString() + ") AND parameter_code IN ("+parameter_code.toSelectString()+") AND quality_code != 'BAD'";
//                    String where = "data_timestamp BETWEEN (SELECT (min(timestamp_in) - interval '1 day') FROM mooring WHERE mooring_id IN (" + mooring.toSelectString() + ")) " +
//                                                     " AND (SELECT (max(timestamp_out) + interval '1 day') FROM mooring WHERE mooring_id IN (" + mooring.toSelectString() + ")) " + 
//                                                     " AND parameter_code IN (" + parameter_code.toSelectString() + ") " + 
//                                                     " AND quality_code != 'BAD'";

                    statement = connection.createStatement();
                    // used source_file_id to select different sensors, maynot work for two DOX2 measurements from the same file on different instruments
                    resultSet = statement.executeQuery("SELECT * FROM (SELECT DISTINCT ON (instrument_id, parameter_code ) depth, instrument_id, parameter_code, model FROM "+table+" JOIN instrument USING (instrument_id) WHERE " + where + ") AS a ORDER BY depth");

                    // double depths[] = {30, 100, 160};
                    ArrayList<DepthParam> depths = new ArrayList<DepthParam>();
                    while (resultSet.next())
                    {
                        Double d = resultSet.getDouble(1);
                        Integer i = resultSet.getInt(2);
                        String p = resultSet.getString(3);
                        String m = resultSet.getString(4);
                        DepthParam prms = new DepthParam(p.trim(), d, i, m.trim());
                        depths.add(prms);
                        System.out.println("Param : " + prms);
                    }

                    for (DepthParam d : depths)
                    {
                        System.out.println("Selecting " + d);

                        resultSet = statement.executeQuery("SELECT data_timestamp, parameter_value FROM "+table+" WHERE " + where
                                + " AND depth = " + d.depth 
                                + " AND parameter_code = '" + d.param + "'"
                                //+ " AND source_file_id = " + d.file 
                                + " AND instrument_id = " + d.instrument 
                                + " ORDER BY data_timestamp");
                        
                        ResultSetMetaData metaData = resultSet.getMetaData();
                        int numberOfColumns = metaData.getColumnCount();

                        TimeSeries ts = new TimeSeries(d.make + "-" + d.param + "-" + d.depth);
                        while (resultSet.next()) 
                        {
                            Timestamp tstamp = resultSet.getTimestamp(1);
                            double v = resultSet.getDouble(2);
                            Second s = new Second(tstamp);
                            ts.addOrUpdate(s, v);
                        }
                        resultSet.close();
                        collection.addSeries(ts);
                    }
                }
                catch (SQLException ex)
                {
                    Logger.getLogger(JDBCplot.class.getName()).log(Level.SEVERE, null, ex);
                }
//            }
//        });
        
        return collection;
    }

    public static void main(final String[] args)
    {
        String $HOME = System.getProperty("user.home");

        PropertyConfigurator.configure("log4j.properties");
        Common.build($HOME + "/ABOS/ABOS.properties");
                
        if (args.length == 0)
        {
            SwingUtilities.invokeLater(new Runnable() 
            {
                public void run() 
                {
                    createAndShowSelectGUI();
                }
            });                        
        }
        else
        {
            String m = args[0].trim();
            String p = args[1].trim();  

            JDBCplot pl = new JDBCplot(m + "-" + p);
            mooring.add(m);
            parameter_code.add(p);
            
            pl.createChartPanel();                       
            
            pl.createPDF(m + "-" + p + ".pdf");            
        }
    }
    
}
