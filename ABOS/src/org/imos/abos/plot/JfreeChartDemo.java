
/* ===========================================================
 * JFreeChart : a free chart library for the Java(tm) platform
 * ===========================================================
 *
 * (C) Copyright 2000-2004, by Object Refinery Limited and Contributors.
 *
 * Project Info:  http://www.jfree.org/jfreechart/index.html
 *
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation;
 * either version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * library; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307, USA.
 *
 * [Java is a trademark or registered trademark of Sun Microsystems, Inc. 
 * in the United States and other countries.]
 *
 * --------------------
 * TimeSeriesDemo5.java
 * --------------------
 * (C) Copyright 2001-2004, by Object Refinery Limited and Contributors.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * $Id: TimeSeriesDemo5.java,v 1.12 2004/04/26 19:12:03 taqua Exp $
 *
 * Changes (from 24-Apr-2002)
 * --------------------------
 * 24-Apr-2002 : Added standard header (DG);
 * 10-Oct-2002 : Renamed JFreeChartDemo2 --> TimeSeriesDemo5 (DG);
 *
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
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.geom.Rectangle2D;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import org.apache.log4j.PropertyConfigurator;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.Axis;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.chart.event.ChartChangeEvent;
import org.jfree.chart.event.ChartChangeListener;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.general.DatasetGroup;
import org.jfree.data.general.SeriesException;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.time.TimeSeriesDataItem;
import org.jfree.data.xy.AbstractIntervalXYDataset;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;
import org.jfree.ui.RefineryUtilities;
import org.wiley.core.Common;
import org.wiley.core.forms.MemoryWindow;

/**
 * A time series chart with 4000 data points, to get an idea of how JFreeChart
 * performs with a larger dataset. You can see that it slows down significantly,
 * so this needs to be worked on (4000 points is not that many!).
 *
 */
public class JfreeChartDemo extends ApplicationFrame
{
    JFreeChart chart;
    ChartPanel chartPanel;
    TimeSeriesCollection dataset;
    JTextField yMax, yMin;
    XYPlot plot;
    String pdfName = "test.pdf";
    
    /**
     * Creates a new demo instance.
     *
     * @param title the frame title.
     */
    public JfreeChartDemo(final String title)
    {
        super(title);
    }
    
    public void createChartPanel()
    {
        dataset = createDataset();
        chart = createChart(dataset);
        chartPanel = new ChartPanel(chart);
        chartPanel.addChartMouseListener(new MouseClick());
        chartPanel.setPreferredSize(new java.awt.Dimension(1024, 768));
        chartPanel.setMouseZoomable(true, false);
        chart.addChangeListener(cc);
        plot = chart.getXYPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinePaint(Color.GRAY);
        plot.setDomainGridlinesVisible(true);
        plot.setRangeGridlinePaint(Color.GRAY);
        plot.setAxisOffset(RectangleInsets.ZERO_INSETS);
        chart.getLegend().setPosition(RectangleEdge.RIGHT);
        
        final DateAxis axis = (DateAxis) plot.getDomainAxis();
        axis.setDateFormatOverride(new SimpleDateFormat("yyyy-MM-dd HH:mm"));
    }
    
    public void createAndShowGUI()
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
                else if (e.getStateChange() == ItemEvent.SELECTED)
                {
                    XYItemRenderer r = plot.getRenderer();
                    if (r instanceof XYLineAndShapeRenderer) 
                    {
                        XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) r;
                        renderer.setBaseShapesVisible(true);
                    }                                                
                }
            }
            
        });
        
        final JPanel jpanel = new JPanel();
        jpanel.setLayout(new BorderLayout());
        jpanel.add(chartPanel, BorderLayout.CENTER);
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
        jpanel.add(right, BorderLayout.LINE_END);
        
        setContentPane(jpanel);
        
        pack();
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
                Date dt = new Date((long)d);

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
     * Creates a sample dataset.
     *
     * @return A sample dataset.
     */
    protected TimeSeriesCollection createDataset()
    {
        TimeSeriesCollection collection = new TimeSeriesCollection();
        
        final TimeSeries series = new TimeSeries("Random Data");
        Day current = new Day(1, 1, 2000);
        double value = 100.0;
        for (int i = 0; i < 4000; i++)
        {
            try
            {
                value = value + Math.random() - 0.5;
                series.add(current, new Double(value));
                current = (Day) current.next();
            }
            catch (SeriesException e)
            {
                System.err.println("Error adding to series");
            }
        }
        collection.addSeries(series);
        
        return collection;
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
            Logger.getLogger(JfreeChartDemo.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (FileNotFoundException ex)
        {
            Logger.getLogger(JfreeChartDemo.class.getName()).log(Level.SEVERE, null, ex);
        }

        System.out.println("PDF finsihed");        
    }
    // ****************************************************************************
    // * JFREECHART DEVELOPER GUIDE                                               *
    // * The JFreeChart Developer Guide, written by David Gilbert, is available   *
    // * to purchase from Object Refinery Limited:                                *
    // *                                                                          *
    // * http://www.object-refinery.com/jfreechart/guide.html                     *
    // *                                                                          *
    // * Sales are used to provide funding for the JFreeChart project - please    * 
    // * support us so that we can continue developing free software.             *
    // ****************************************************************************
    /**
     * Starting point for the application.
     *
     * @param args ignored.
     */
    public static void main(final String[] args)
    {
        final String title = "Demo Plot";
        
        String $HOME = System.getProperty("user.home");
        PropertyConfigurator.configure("log4j.properties");
        Common.build("ABOS.properties");        
        
        final JfreeChartDemo demo = new JfreeChartDemo(title);
        
        demo.createChartPanel();
        demo.createAndShowGUI();

    }
}
