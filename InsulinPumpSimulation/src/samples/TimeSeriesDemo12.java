package samples;

import java.awt.BasicStroke;
import java.awt.Color;
import java.text.SimpleDateFormat;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.time.Minute;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

/**
 * A demo.
 * 
 */
public class TimeSeriesDemo12 extends ApplicationFrame {

    /**
     * A demo.
     * 
     * @param title  the frame title.
     */
    public TimeSeriesDemo12(final String title) {

        super(title);

        final XYDataset dataset = createDataset();
        final JFreeChart chart = createChart(dataset);
        final ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
        chartPanel.setMouseZoomable(true, false);
        setContentPane(chartPanel);

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
     * Creates a chart.
     * 
     * @param dataset  a dataset.
     * 
     * @return A chart.
     */
    private JFreeChart createChart(final XYDataset dataset) {

        final JFreeChart chart = ChartFactory.createTimeSeriesChart(
            "Sample Chart",
            "Date", 
            "Value",
            dataset,
            true,
            true,
            false
        );

        chart.setBackgroundPaint(Color.white);
        
//        final StandardLegend sl = (StandardLegend) chart.getLegend();
  //      sl.setDisplaySeriesShapes(true);

        final XYPlot plot = chart.getXYPlot();
        //plot.setOutlinePaint(null);
        plot.setBackgroundPaint(Color.lightGray);
        plot.setDomainGridlinePaint(Color.white);
        plot.setRangeGridlinePaint(Color.white);
    //    plot.setAxisOffset(new Spacer(Spacer.ABSOLUTE, 5.0, 5.0, 5.0, 5.0));
        plot.setDomainCrosshairVisible(true);
        plot.setRangeCrosshairVisible(false);
        
        final XYItemRenderer renderer = plot.getRenderer();
        if (renderer instanceof StandardXYItemRenderer) {
            final StandardXYItemRenderer rr = (StandardXYItemRenderer) renderer;
           // rr.setPlotShapes(true);
            rr.setShapesFilled(true);
            renderer.setSeriesStroke(0, new BasicStroke(2.0f));
            renderer.setSeriesStroke(1, new BasicStroke(2.0f));
           }
        
        final DateAxis axis = (DateAxis) plot.getDomainAxis();
        axis.setDateFormatOverride(new SimpleDateFormat("hh:mma"));
        
        return chart;

    }
    
    /**
     * Creates a sample dataset.
     *
     * @return the dataset.
     */
    private XYDataset createDataset() {

        final TimeSeriesCollection dataset = new TimeSeriesCollection();
        dataset.setDomainIsPointsInTime(true);
        
        final TimeSeries s1 = new TimeSeries("Series 1", Minute.class);
        s1.add(new Minute(0, 0, 7, 12, 2003), 1.2);
        s1.add(new Minute(30, 12, 7, 12, 2003), 3.0);
        s1.add(new Minute(15, 14, 7, 12, 2003), 8.0);
        
        final TimeSeries s2 = new TimeSeries("Series 2", Minute.class);
        s2.add(new Minute(0, 3, 7, 12, 2003), 0.0);
        s2.add(new Minute(30, 9, 7, 12, 2003), 0.0);
        s2.add(new Minute(15, 10, 7, 12, 2003), 0.0);
        
        dataset.addSeries(s1);
        dataset.addSeries(s2);

        return dataset;

    }

    /**
     * Starting point for the demonstration application.
     *
     * @param args  ignored.
     */
    public static void main(final String[] args) {

        final TimeSeriesDemo12 demo = new TimeSeriesDemo12("Time Series Demo 12");
        demo.pack();
        RefineryUtilities.centerFrameOnScreen(demo);
        demo.setVisible(true);

    }

}
