package gui;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.wb.swt.SWTResourceManager;
import org.swtchart.Chart;
import org.swtchart.IAxis;
import org.swtchart.IAxisTick;
import org.swtchart.IBarSeries;
import org.swtchart.ISeries;
import org.swtchart.ISeries.SeriesType;
import org.swtchart.ISeriesLabel;

import dba.DBManager;
import util.GraphData;

public class GraphView {
	protected Shell shlGraphView;
	protected static boolean highlight;
	/* Used to remember the location of the highlight point */
	private static int highlightX;
	private static int highlightY;
	private DBManager dbMgr;
	Composite compGraph;

	DateTime dateTimeFrom;
	DateTime dateTimeTo;

	/**
	 * Launch the application.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			GraphView window = new GraphView();
			window.open(null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Open the window.
	 */
	public void open(DBManager dbMgr) {
		this.dbMgr = dbMgr;
		Display display = Display.getDefault();
		createContents();
		shlGraphView.open();
		shlGraphView.layout();
		while (!shlGraphView.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	/**
	 * Create contents of the window.
	 */
	protected void createContents() {
		shlGraphView = new Shell(Display.getDefault(), SWT.TITLE | SWT.CLOSE | SWT.BORDER);
		shlGraphView.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		shlGraphView.setSize(810, 600);
		shlGraphView.setText("Graph View");

		compGraph = new Composite(shlGraphView, SWT.NONE);
		compGraph.setBounds(10, 50, 800, 500);
		compGraph.setLayout(new FillLayout());

		Label lblFrom = new Label(shlGraphView, SWT.NONE);
		lblFrom.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		lblFrom.setBounds(10, 29, 34, 15);
		lblFrom.setText("From");

		Label lblTo = new Label(shlGraphView, SWT.NONE);
		lblTo.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		lblTo.setBounds(138, 29, 14, 15);
		lblTo.setText("To");

		Button btnApply = new Button(shlGraphView, SWT.NONE);

		btnApply.setBounds(263, 19, 75, 25);
		btnApply.setText("Apply");

		dateTimeFrom = new DateTime(shlGraphView, SWT.BORDER);
		dateTimeFrom.setBounds(50, 20, 80, 24);

		dateTimeTo = new DateTime(shlGraphView, SWT.BORDER);
		dateTimeTo.setBounds(159, 19, 80, 24);

		btnApply.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				Calendar cal = new GregorianCalendar();
				cal.set(dateTimeFrom.getYear(), dateTimeFrom.getMonth(), dateTimeFrom.getDay());
				Date fromDate = cal.getTime();

				cal.set(dateTimeTo.getYear(), dateTimeTo.getMonth(), dateTimeTo.getDay());
				Date toDate = cal.getTime();

				List<GraphData> gData = dbMgr.getGraphData(sdf.format(fromDate), sdf.format(toDate));

				Date[] xAxisData = new Date[gData.size()];
				double[] yAxisData = new double[gData.size()];

				if (gData != null && !gData.isEmpty()) {
					for (int i = 0; i < gData.size(); i++) {
						cal.setTime(gData.get(i).getDay());
						cal.set(Calendar.HOUR, 12);
						cal.set(Calendar.MINUTE, 0);
						cal.set(Calendar.SECOND, 0);
						cal.set(Calendar.MILLISECOND, 0);
						xAxisData[i] = cal.getTime();
						yAxisData[i] = gData.get(i).getSugarLevel();
					}
				}

				// remove old graph, if any
				for (Control kid : compGraph.getChildren()) {
					kid.dispose();
				}
				createChart(compGraph, xAxisData, yAxisData, gData);
			}
		});

	}

	/**
	 * create the chart.
	 * 
	 * @param parent
	 *            The parent composite
	 * @return The created chart
	 */
	static public Chart createChart(Composite parent, final Date[] xAxis,
			final double[] yAxis, List<GraphData> gData ) {

		// create a chart
		final Chart chart = new Chart(parent, SWT.NONE);

		// set titles
		chart.getTitle().setText("Blood Sugar Level");

		// create bar series
		IBarSeries barSeries = (IBarSeries) chart.getSeriesSet().createSeries(SeriesType.BAR, "bar series");
		barSeries.setYSeries(yAxis);
		barSeries.setXDateSeries(xAxis);
		barSeries.setBarPadding(10);
		IAxisTick xTick = chart.getAxisSet().getXAxis(0).getTick();
		xTick.setTickLabelAngle(90);
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		xTick.setFormat(format);

		// adjust the axis range
		chart.getAxisSet().adjustRange();

		/* Get the plot area and add the mouse listeners */
		final Composite plotArea = chart.getPlotArea();

		plotArea.addListener(SWT.MouseHover, new Listener() {

			@Override
			public void handleEvent(Event event) {
				IAxis xIAxis = chart.getAxisSet().getXAxis(0);
				double x = xIAxis.getDataCoordinate(event.x);
				ISeries[] series = chart.getSeriesSet().getSeries();

				/* over all series */
				int index =0;
				for (ISeries serie : series) {
					double[] xS = serie.getXSeries();

					/* check all data points */
					for (int i = 0; i < xS.length; i++) {
						if(i > 0){
							// rounding to nearest value
							if(x>= xS[i-1] && x<= xS[i]){
								if((x-xS[i-1]) > (xS[i]-x)){
									index = i;
									break;
								}else{
									index = i-1;
									break;
								}
							}
						}
					}
				plotArea.setToolTipText("Glucogan ="+ gData.get(index).getGlucogan() 
						+"mg\nInsulin ="+ gData.get(index).getInsulin() + "mg/dl");
				}
			}
		});

/*		plotArea.addListener(SWT.MouseMove, new Listener() {

			@Override
			public void handleEvent(Event arg0) {
				highlight = false;

				plotArea.redraw();
			}
		});

		plotArea.addListener(SWT.Paint, new Listener() {

			@Override
			public void handleEvent(Event event) {
				if (highlight) {
					GC gc = event.gc;

					gc.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_RED));
					gc.setAlpha(128);

					gc.fillOval(highlightX - 5, highlightY - 5, 10, 10);
				}
			}
		});*/

		return chart;
	}
}
