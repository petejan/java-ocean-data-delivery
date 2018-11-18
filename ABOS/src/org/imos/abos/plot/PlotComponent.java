/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.imos.abos.plot;

import com.itextpdf.awt.PdfGraphics2D;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.PdfWriter;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Rectangle2D;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import org.apache.log4j.PropertyConfigurator;
import org.imos.abos.forms.NetCDFcreateForm;
import static org.imos.abos.plot.JDBCplot.query;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.chart.event.ChartChangeEvent;
import org.jfree.chart.event.ChartChangeListener;
import org.jfree.chart.labels.StandardXYItemLabelGenerator;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.general.SeriesException;
import org.jfree.data.time.Day;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.time.TimeSeriesDataItem;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;
import org.wiley.core.Common;
import org.wiley.core.forms.MemoryWindow;
import org.wiley.util.SQLWrapper;

/**
 *
 * @author jan079
 */
public class PlotComponent extends MemoryWindow
{

    /**
     * Creates a new demo instance.
     *
     * @param title the frame title.
     */
    public PlotComponent()
    {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    public class PlotChartPanel extends JPanel
    {
        JFreeChart chart;
        ChartPanel chartPanel;
        TimeSeriesCollection dataset;
        JTextField yMax, yMin;
        XYPlot plot;
        String pdfName = "test.pdf";

        public PlotChartPanel()
        {
            dataset = createDataset();
            chart = createChart(dataset);
            
            chartPanel = new ChartPanel(chart);
            chartPanel.addChartMouseListener(new MouseClick());
            chartPanel.setPreferredSize(new java.awt.Dimension(1500, 1100));
            chartPanel.setMouseZoomable(true, false);
            chart.addChangeListener(cc);
            
            plot = chart.getXYPlot();
            plot.setBackgroundPaint(Color.WHITE);
            plot.setDomainGridlinePaint(Color.GRAY);
            plot.setDomainGridlinesVisible(true);
            plot.setRangeGridlinePaint(Color.GRAY);
            plot.setAxisOffset(RectangleInsets.ZERO_INSETS);
            XYItemRenderer r = plot.getRenderer();
            r.setBaseToolTipGenerator(new StandardXYToolTipGenerator(StandardXYToolTipGenerator.DEFAULT_TOOL_TIP_FORMAT, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"), new DecimalFormat("#0.000")));
            for (int i=1;i<dataset.getSeriesCount();i+=2)
            {
	            if (r instanceof XYLineAndShapeRenderer)
	            {
	                XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) r;
	                renderer.setSeriesShapesVisible(i, true);
	                renderer.setSeriesLinesVisible(i, false);
	            }
            }

            chart.getLegend().setPosition(RectangleEdge.RIGHT);

            final DateAxis axis = (DateAxis) plot.getDomainAxis();
            axis.setDateFormatOverride(new SimpleDateFormat("yyyy-MM-dd HH:mm"));

            query.setConnection(Common.getConnection());
            query.executeQuery("SELECT min(timestamp_in), max(timestamp_out) FROM mooring WHERE mooring_id IN (" + mooring.toSelectString() + ")");
            Vector attributeSet = query.getData();
            if (attributeSet != null && attributeSet.size() > 0)
            {
                for (int i = 0; i < attributeSet.size(); i++)
                {
                    Vector row = (Vector) attributeSet.get(i);
                    in = (Timestamp)(row.get(0));
                    out = (Timestamp)(row.get(1));
                }
            }
            
            axis.setRange(in, out);
            
            plot.getRangeAxis().setLabel(parameter_code.toString() + " (" + parameter_unit.strings.get(parameter_code.oneSelected) + ")");
        }

        protected TimeSeriesCollection createDataset()
        {
            final TimeSeriesCollection collection = new TimeSeriesCollection(TimeZone.getTimeZone("UTC"));

            //        SwingUtilities.invokeLater(new Runnable() 
            //        {
            //            public void run() 
            //            {
            try
            {
                System.out.println("Param " + parameter_code);

                
                // plot.getRangeAxis().setLabel(parameter_code.toString());

                //Class.forName("org.postgresql.Driver");
                //Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost/ABOS", "peter", "password");
                Connection connection = Common.getConnection();
                ResultSet resultSet = null;
                Statement statement = null;
                if (tableList != null)
                {
                    table = ((String) tableList.getSelectedItem()).trim();
                }
                String where = "mooring_id IN (" + mooring.toSelectString() + ") AND parameter_code IN (" + parameter_code.toSelectString() + ") AND quality_code != 'BAD' AND quality_code != 'INTERPOLATED'";
                //                    String where = "data_timestamp BETWEEN (SELECT (min(timestamp_in) - interval '1 day') FROM mooring WHERE mooring_id IN (" + mooring.toSelectString() + ")) " +
                //                                                     " AND (SELECT (max(timestamp_out) + interval '1 day') FROM mooring WHERE mooring_id IN (" + mooring.toSelectString() + ")) " + 
                //                                                     " AND parameter_code IN (" + parameter_code.toSelectString() + ") " + 
                //                                                     " AND quality_code != 'BAD'";

                statement = connection.createStatement();
                // used source_file_id to select different sensors, maynot work for two DOX2 measurements from the same file on different instruments
                resultSet = statement.executeQuery("SELECT * FROM (SELECT DISTINCT ON (instrument_id, mooring_id, parameter_code ) depth, instrument_id, parameter_code, model, serial_number, mooring_id FROM " + table + " JOIN instrument USING (instrument_id) WHERE " + where + ") AS a ORDER BY depth, parameter_code, model");

                // double depths[] = {30, 100, 160};
                ArrayList<DepthParam> depths = new ArrayList<DepthParam>();
                while (resultSet.next())
                {
                    Double d = resultSet.getDouble(1);
                    Integer i = resultSet.getInt(2);
                    String p = resultSet.getString(3);
                    String m = resultSet.getString(4);
                    String s = resultSet.getString(5);
                    DepthParam prms = new DepthParam(p.trim(), d, i, m.trim());
                    depths.add(prms);
                    System.out.println("Param : " + prms + " serial number " + s);
                }

                for (DepthParam d : depths)
                {
                    System.out.println("Selecting " + d);

                    resultSet = statement.executeQuery("SELECT data_timestamp, parameter_value, quality_code FROM " + table + " WHERE " + where
                            + " AND depth = " + d.depth
                            + " AND parameter_code = '" + d.param + "'"
                            //+ " AND source_file_id = " + d.file 
                            + " AND instrument_id = " + d.instrument
                            + " ORDER BY data_timestamp");

                    ResultSetMetaData metaData = resultSet.getMetaData();
                    int numberOfColumns = metaData.getColumnCount();

                    TimeSeries ts = new TimeSeries(d.instrument + "-" + d.param + "-" + d.depth);
                    TimeSeries tsQ = new TimeSeries(d.make + "-" + d.param + "-" + d.depth + "-BAD");
                    while (resultSet.next())
                    {
                        Timestamp tstamp = resultSet.getTimestamp(1);
                        double v = resultSet.getDouble(2);
                        String q = resultSet.getString(3);
                        
                        Second s = new Second(tstamp);
                        ts.addOrUpdate(s, v);
                        if (q.startsWith("PBAD") || q.startsWith("BAD"))
                        {
                        	tsQ.addOrUpdate(s, v);
                        }
                    }
                    resultSet.close();
                    collection.addSeries(ts);
                    collection.addSeries(tsQ);
                }
            }
            catch (SQLException ex)
            {
                Logger.getLogger(PlotComponent.class.getName()).log(Level.SEVERE, null, ex);
            }
            //            }
            //        });

            return collection;
        }

        public void createChartPanel()
        {            
            JButton autoZoom = new JButton("Auto Scale");
            autoZoom.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    System.out.println("Auto Zoom");

                    chart.getXYPlot().getDomainAxis().setAutoRange(true);
                    chart.getXYPlot().getRangeAxis().setAutoRange(true);
                }
            });
            JButton pdfButton = new JButton("PDF");
            pdfButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    System.out.println("PDF");
                    createPDF(pdfName);
                }
            });
            yMax = new JTextField(10);
            yMax.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    System.out.println("yMax Set " + yMax.getText());
                    double max = new Double(yMax.getText());
                    double min = chart.getXYPlot().getRangeAxis().getRange().getLowerBound();
                    chart.getXYPlot().getRangeAxis().setRange(min, max);
                }
            });
            yMin = new JTextField(10);
            yMin.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    System.out.println("yMin Set " + yMin.getText());
                    double min = new Double(yMin.getText());
                    double max = chart.getXYPlot().getRangeAxis().getRange().getUpperBound();
                    chart.getXYPlot().getRangeAxis().setRange(min, max);
                }
            });
            JCheckBox showPoints = new JCheckBox("Show Points");
            showPoints.addItemListener(new ItemListener()
            {
                @Override
                public void itemStateChanged(ItemEvent e)
                {
                    if (e.getStateChange() == ItemEvent.DESELECTED)
                    {
                        XYItemRenderer r = plot.getRenderer();
                        if (r instanceof XYLineAndShapeRenderer)
                        {
                            XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) r;
                            renderer.setBaseShapesVisible(false);
                        }
                    }
                    else
                    {
                        if (e.getStateChange() == ItemEvent.SELECTED)
                        {
                            XYItemRenderer r = plot.getRenderer();
                            if (r instanceof XYLineAndShapeRenderer)
                            {
                                XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) r;
                                renderer.setBaseShapesVisible(true);
                            }
                        }
                    }
                }
            });
            JCheckBox linesPoints = new JCheckBox("Lines Points");
            linesPoints.setSelected(true);
            linesPoints.addItemListener(new ItemListener()
            {
                @Override
                public void itemStateChanged(ItemEvent e)
                {
                    if (e.getStateChange() == ItemEvent.DESELECTED)
                    {
                        XYItemRenderer r = plot.getRenderer();
                        if (r instanceof XYLineAndShapeRenderer)
                        {
                            XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) r;
                            renderer.setBaseLinesVisible(false);
                        }
                    }
                    else
                    {
                        if (e.getStateChange() == ItemEvent.SELECTED)
                        {
                            XYItemRenderer r = plot.getRenderer();
                            if (r instanceof XYLineAndShapeRenderer)
                            {
                                XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) r;
                                renderer.setBaseLinesVisible(true);
                            }
                        }
                    }
                }
            });

            setLayout(new BorderLayout());
            add(chartPanel, BorderLayout.CENTER);
            JPanel right = new JPanel();
            JPanel ypmax = new JPanel();
            ypmax.add(yMax);
            JPanel ypmin = new JPanel();
            ypmin.add(yMin);
            right.setLayout(new BoxLayout(right, BoxLayout.PAGE_AXIS));
            right.add(ypmax);
            right.add(ypmin);
            right.add(autoZoom);
            right.add(pdfButton);
            right.add(showPoints);
            right.add(linesPoints);
            add(right, BorderLayout.LINE_END);

            setLocationRelativeTo(null);
            setVisible(true);
        }

        public class MouseClick implements ChartMouseListener
        {

            public void chartMouseClicked(ChartMouseEvent event)
            {
                ChartEntity entity = event.getEntity();
                if (entity == null)
                {
                    return;
                }

                if (entity instanceof XYItemEntity)
                {
                    // Get entity details
                    String tooltip = ((XYItemEntity) entity).getToolTipText();
                    XYDataset dataset = ((XYItemEntity) entity).getDataset();
                    int seriesIndex = ((XYItemEntity) entity).getSeriesIndex();
                    int item = ((XYItemEntity) entity).getItem();

                    // You have the dataset the data point belongs to, the index of the series in that dataset of the data point, and the specific item index in the series of the data point.
                    TimeSeries series = ((TimeSeriesCollection) dataset).getSeries(seriesIndex);
                    TimeSeriesDataItem xyItem = series.getDataItem(item);

                    double d = dataset.getXValue(seriesIndex, item);
                    Date dt = new Date((long) d);

                    System.out.println("xyItem " + xyItem + " xyItem " + xyItem.getValue() + " item " + item + " value " + series.getValue(item) + " X " + dataset.getXValue(seriesIndex, item) + " X " + series.getTimePeriod(item));
                }
            }

            public void chartMouseMoved(ChartMouseEvent event)
            {
            }
        }
        ChartChange cc = new ChartChange();

        public class ChartChange implements ChartChangeListener
        {

            DecimalFormat df = new DecimalFormat("#0.0");

            @Override
            public void chartChanged(ChartChangeEvent event)
            {
                System.out.println("Chart Change " + event);
                System.out.println("Event type " + event.getType());
                JFreeChart c = event.getChart();

                if (c != null)
                {
                    XYPlot xy = c.getXYPlot();
                    ValueAxis x = xy.getDomainAxis();
                    System.out.println("Domain Axis (" + x.getLowerBound() + ", " + x.getUpperBound() + ")");
                    ValueAxis y = xy.getRangeAxis();
                    System.out.println("Range  Axis (" + y.getLowerBound() + ", " + y.getUpperBound() + ")");
                    if ((yMax != null) && (yMin != null))
                    {
                        yMax.setText(df.format(y.getUpperBound()));
                        yMin.setText(df.format(y.getLowerBound()));
                    }
                }
            }
        }

        /**
         * Creates a sample chart.
         *
         * @param dataset the dataset.
         *
         * @return A sample chart.
         */
        private JFreeChart createChart(final XYDataset dataset)
        {
            return ChartFactory.createTimeSeriesChart(
                    "",
                    "timestamp",
                    "Value",
                    dataset,
                    true,
                    true,
                    false);
        }

        protected void createPDF(String filename)
        {
            try
            {
                Rectangle page = PageSize.A4.rotate();

                // step 1
                Document document = new Document(page);
                // step 2
                PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(filename));
                // step 3
                document.open();
                // step 4
                PdfContentByte cb = writer.getDirectContent();
                float width = page.getWidth();
                float height = page.getHeight();
                // add chart
                PdfTemplate pie = cb.createTemplate(width, height);
                Graphics2D g2d1 = new PdfGraphics2D(pie, width, height);
                Rectangle2D r2d1 = new Rectangle2D.Double(0, 0, width, height);
                chart.draw(g2d1, r2d1);
                g2d1.dispose();
                cb.addTemplate(pie, 0, 0);
                // step 5
                document.close();
            }
            catch (DocumentException ex)
            {
                Logger.getLogger(org.imos.abos.plot.JfreeChartDemo.class.getName()).log(Level.SEVERE, null, ex);
            }
            catch (FileNotFoundException ex)
            {
                Logger.getLogger(org.imos.abos.plot.JfreeChartDemo.class.getName()).log(Level.SEVERE, null, ex);
            }

            System.out.println("PDF finsihed");
        }
    }
    protected SQLWrapper query = new SQLWrapper();
    StringList parameter_code = new StringList("Parameters");
    StringList parameter_unit = new StringList("Unit");
    StringList mooring = new StringList("Moorings");
    String table = "processed_instrument_data";
    JTable mooringList = null;
    JTable parameterList = null;
    JComboBox tableList = null;

    public class StringList
    {

        ArrayList<String> strings = new ArrayList<String>();
        String[] stringColNames = new String[1];
        JTable jtable = null;
        int oneSelected = -1;

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
            String[][] array = new String[strings.size()][1];
            for (int i = 0; i < strings.size(); i++)
            {
                array[i][0] = (String) strings.get(i).trim();
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
                for (int i = 0; i < strings.size(); i++)
                {
                    s += strings.get(i).trim();
                    if (i < strings.size() - 1)
                    {
                        s += "-";
                    }
                }
            }
            else
            {
                int[] rows = jtable.getSelectedRows();

                for (int i = 0; i < rows.length; i++)
                {
                    oneSelected = rows[i];
                    s += jtable.getValueAt(oneSelected, 0).toString().trim();
                    if (i < rows.length - 1)
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
                for (int i = 0; i < strings.size(); i++)
                {
                    s += "'" + strings.get(i).trim() + "'";
                    if (i < strings.size() - 1)
                    {
                        s += ",";
                    }
                }
            }
            else
            {
                int[] rows = jtable.getSelectedRows();

                for (int i = 0; i < rows.length; i++)
                {
                    s += "'" + jtable.getValueAt(rows[i], 0).toString().trim() + "'";
                    if (i < rows.length - 1)
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
                jtable = new JTable(asArray(), stringColNames);
            }

            return jtable;
        }
    }

    Timestamp in;
    Timestamp out;
    
    public class PlotSelectPanel extends JPanel
    {
        public PlotSelectPanel()
        {
            this.setLayout(new BorderLayout());

            query.setConnection(Common.getConnection());
            query.executeQuery("SELECT mooring_id, timestamp_in, timestamp_out FROM mooring ORDER BY mooring_id");
            Vector attributeSet = query.getData();
            if (attributeSet != null && attributeSet.size() > 0)
            {
                for (int i = 0; i < attributeSet.size(); i++)
                {
                    Vector row = (Vector) attributeSet.get(i);
                    mooring.add((String) (row.get(0)));
                }
            }
            query.executeQuery("SELECT code, units, netcdf_std_name, netcdf_long_name FROM parameters ORDER BY parameters");
            attributeSet = query.getData();
            if (attributeSet != null && attributeSet.size() > 0)
            {
                for (int i = 0; i < attributeSet.size(); i++)
                {
                    Vector row = (Vector) attributeSet.get(i);
                    parameter_code.add((String) (row.get(0)));
                    parameter_unit.add((String) (row.get(1)));
                }
            }

            mooringList = mooring.asJTable();
            mooringList.setColumnSelectionAllowed(false);
            parameterList = parameter_code.asJTable();

            JScrollPane mooringScrollPane = new JScrollPane(mooringList);
            JScrollPane parameterScrollPane = new JScrollPane(parameterList);

            //Lay out the demo.
            String[] tableStrings =
            {
                "processed_instrument_data", "raw_instrument_data"
            };

            tableList = new JComboBox(tableStrings);

            add(tableList, BorderLayout.NORTH);

            add(mooringScrollPane, BorderLayout.WEST);
            add(parameterScrollPane, BorderLayout.EAST);

            JButton plotButton = new JButton("Plot");

            plotButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    //mooring = ((String)mooringList.getSelectedItem()).trim();
                    //parameter_code = ((String)parameterList.getSelectedItem()).trim();

                    JFrame demo = new JFrame();
                    JPanel p = createChartPanel();
                    demo.add(p);
                    demo.setSize(1500, 1100);
                    demo.setLocationRelativeTo(null);
                    demo.setVisible(true);                    
                }
            });

            add(plotButton, BorderLayout.SOUTH);

            setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        }

    }
    
    public PlotChartPanel createChartPanel()
    {
        PlotChartPanel pf = new PlotChartPanel();
        pf.createChartPanel();
        pf.pdfName = mooring + "-" + parameter_code + ".pdf";
        
        return pf;
                    
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

    public void initialise()
    {
        PlotSelectPanel psp = new PlotSelectPanel();

//        //Create and set up the window.
//        JFrame frame = new JFrame("Extract Plot");
        //Create and set up the content pane.

        psp.setOpaque(true); //content panes must be opaque
        this.setContentPane(psp);
        //Display the window.
        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);        
    }
    
    public static void main(String[] args)
    {
        try
        {
            // Set cross-platform Java L&F (also called "Metal")
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
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

        if (args.length == 0)
        {
            SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    PlotComponent pc = new PlotComponent();           
                    pc.initialise();
                }
            });
        }
        else
        {
            String m = args[0].trim();
            String p = args[1].trim();  
            String u = args[2].trim();  

            PlotComponent pl = new PlotComponent();
            
            pl.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);       
            pl.mooring.add(m);
            pl.parameter_code.add(p);
            pl.parameter_code.oneSelected = 0;
            pl.parameter_unit.add(u);
            
            PlotChartPanel pc = pl.createChartPanel();                       
            
            pc.createPDF(m + "-" + p + ".pdf");            
            pl.cleanup();
            pc.setVisible(false);
            pl.setVisible(false);
        }

    }
}
