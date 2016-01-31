package gui;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.SwingUtilities;

import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.DynamicTimeSeriesCollection;
import org.jfree.data.time.Second;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.Layer;

import dba.DBManager;
import entities.ActivityLog;
import entities.AppSettings;
import entities.PreConditionsRecord;
import entities.SugarLevelRecord;
import util.Constants;
import util.Utility;

/**
 * The Main SWT Screen of Application
 * 
 * @author Bala
 *
 */
public class HomeScreen  {

	protected Shell shlHomeScreen;
	private Label lblMealTime;
	protected Label lblClock;
	private Label lblMealName;
	protected ProgressBar pbBattery;
	protected ProgressBar pbInsulinReservoir;
	protected ProgressBar pbGlucagonReservoir;
	private Label lblPumpStatus;
	private Label lblGlucoseSensorstatus;
	private Label lblNeedleStatus;
	private Label lblAlarmStatus;
	private Label lblStatusIndicator;
	private volatile String faultyText = "";
	private Button btnPostPone;
	private Button btnSkipBolus;
	private Button buttonInjBolus;
	private AppSettings appSettings;

	// graph data set
	final DynamicTimeSeriesCollection dataset = new DynamicTimeSeriesCollection(1, 600, new Second());

	/** Timer to refresh graph after every 1/4th of a second */
	private javax.swing.Timer gTimer = null;

	private Composite compGraph;
	/// End Graph

	private Map<Integer, Integer> preConditions = new HashMap();

	// Injected from Spring
	private SettingsPage settings;
	private GraphView graph;
	private CARBRemainderPage carb;
	private DBManager dbMgr;
	private Text lblStatusMessage;
	private Text txtSugarLevel;

	public SettingsPage getSettings() {
		return settings;
	}

	public void setSettings(SettingsPage settings) {
		this.settings = settings;
	}

	public DBManager getDbMgr() {
		return dbMgr;
	}

	public void setDbMgr(DBManager dbMgr) {
		this.dbMgr = dbMgr;
	}

	public GraphView getGraph() {
		return graph;
	}

	public void setGraph(GraphView graph) {
		this.graph = graph;
	}

	public CARBRemainderPage getCarb() {
		return carb;
	}

	public void setCarb(CARBRemainderPage carb) {
		this.carb = carb;
	}

	/**
	 * Launch the application.
	 * 
	 * @param args
	 */

	public static void main(String[] args) {

		try {
			HomeScreen window = new HomeScreen("");
			window.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private JFreeChart chart;

	public HomeScreen(String title) {
		//super(title);

		final DynamicTimeSeriesCollection dataset = new DynamicTimeSeriesCollection(1, 120, new Second());
		dataset.setTimeBase(new Second(0, 0, 0, 1, 1, 2016));
		dataset.addSeries(gaussianData(), 0, "Blood Sugar Level");
		chart = createChart(dataset);

		gTimer = new javax.swing.Timer(700, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				float[] newData = new float[1];

				Display.getDefault().asyncExec((new Runnable() {
					public void run() {
						newData[0] = Constants.BLOOD_SUGAR_LEVEL;
						dataset.advanceTime();
						dataset.appendData(newData);
					}
				}));
			}
		});

		gTimer.setInitialDelay(1000);
	}

	public void open() {
		Display display = Display.getDefault();
		createContents();
		shlHomeScreen.open();
		shlHomeScreen.layout();
		while (!shlHomeScreen.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	/**
	 * Update the latest sugar level
	 * 
	 * @param sugarLevel
	 */
	public void setSugarLevel() {
		final int sugarLevel = Utility.getSugarLevel(0, 0);
		Display.getDefault().asyncExec((new Runnable() {
			public void run() {
				txtSugarLevel.setText(Integer.toString(sugarLevel) + "mg/dl");
				if ((sugarLevel >= 65 && sugarLevel <= 75) || (sugarLevel >= 115 && sugarLevel <= 125)) {
					txtSugarLevel.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_DARK_YELLOW));
				} else if (sugarLevel <= 64 || sugarLevel > 125) {
					dbMgr.setActivity("Sugar level is beyound the range. Sugar level is " + sugarLevel + " md/dl",
							Constants.ACTIVITY_STATUS_ERROR);
					txtSugarLevel.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_RED));
				} else {
					txtSugarLevel.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_DARK_GREEN));
				}
			}
		}));
	}

	public void setActivityLog(List<ActivityLog> logs) {
		Display.getDefault().asyncExec((new Runnable() {
			public void run() {
				String txt = "";
				for (int i = 0; i < logs.size(); i++) {
					txt += (logs.get(i).getActivity() + "\n");
				}
				txtActivityLog.setText(txt);
			}
		}));
	}

	/**
	 * Sets the battery life value Asynchronously
	 * 
	 * @param value
	 */
	public void setBatteryLife(int value, PreConditionsRecord pcr) {
		pbBattery.setSelection(value);
		pbBattery.setState(SWT.NORMAL);
		pcr.setBatteryTestResult(true);
		pbBattery.setToolTipText(value + "%");
		if (value <= 30) {
			pcr.setCurrentStatus(0);
			dbMgr.setActivity("Battery power too low!", Constants.ACTIVITY_STATUS_ERROR);
			pcr.setBatteryTestResult(false);
			pbBattery.setState(SWT.ERROR);
		} else if (value > 30 && value <= 40) {
			dbMgr.setActivity("Battery power is weak!", Constants.ACTIVITY_STATUS_WARNING);
			pbBattery.setState(SWT.PAUSED);
		}
	}

	/**
	 * Set the status text & icon
	 * 
	 * @param iconPath
	 * @param statusTxt
	 */
	public void setStatus(String iconPath, final String statusTxt) {
		Display.getDefault().asyncExec((new Runnable() {
			public void run() {
				// Don't over ride if the app is already in error state
				String icon = iconPath;
				if (Constants.ICON_ERROR_IMG.equals(iconPath)) {
					if (faultyText.isEmpty()) {
						faultyText = statusTxt;
					}
					icon = Constants.ICON_ERROR_IMG;
				}
				Constants.CURRENT_CYCLE_STATUS += statusTxt + "\n";
				lblStatusIndicator.setImage(SWTResourceManager.getImage(HomeScreen.class, icon));
				lblStatusMessage.setText(Constants.CURRENT_CYCLE_STATUS);
			}
		}));
	}

	public void setInsulinReservoir(int value, PreConditionsRecord pcr) {
		pbInsulinReservoir.setSelection(Constants.CURRENT_INSULIN_RESERVOIR);
		pcr.setInsulinReservoirTestResult(true);
		pbInsulinReservoir.setState(SWT.NORMAL);
		pbInsulinReservoir.setToolTipText(value + "%");
		if (value <= 30) {
			pcr.setCurrentStatus(0);
			pcr.setInsulinReservoirTestResult(false);
			pbInsulinReservoir.setState(SWT.ERROR);
			dbMgr.setActivity("Insulin reservoir in critical state. Please refill it", Constants.ACTIVITY_STATUS_ERROR);
		} else if (value > 30 && value <= 40) {
			pbInsulinReservoir.setState(SWT.PAUSED);
			// setStatus(Constants.ICON_WARNING_IMG, "Check Critical
			// Indicators!");
			dbMgr.setActivity("Insulin reservoir needs attention. Please refill it", Constants.ACTIVITY_STATUS_WARNING);
		}
	}

	/**
	 * @param value
	 */
	public void setGlucagonReservoir(int value, PreConditionsRecord pcr) {
		pbGlucagonReservoir.setSelection(value);
		pbGlucagonReservoir.setState(SWT.NORMAL);
		pcr.setGlucagonTestResult(true);
		pbGlucagonReservoir.setToolTipText(value + "%");
		if (value <= 30) {
			pcr.setGlucagonTestResult(false);
			pcr.setCurrentStatus(0);
			pbGlucagonReservoir.setState(SWT.ERROR);
			dbMgr.setActivity("Glucagon reservoir needs to be refilled immediately.", Constants.ACTIVITY_STATUS_ERROR);
		} else if (value > 30 && value <= 40) {
			pbGlucagonReservoir.setState(SWT.PAUSED);
			setStatus(Constants.ICON_WARNING_IMG, "Check Critical Indicators!");
			dbMgr.setActivity("Glucagon reservoir is draining. Please refill", Constants.ACTIVITY_STATUS_WARNING);
		} else {
			// dbMgr.setActivity("Glucagon reservoir is good enough",
			// Constants.ACTIVITY_STATUS_OK);
		}
	}

	/**
	 * Set the meal name & remaining time
	 */
	public void setMealTime(AppSettings setting) {
		this.appSettings = setting;
		Display.getDefault().asyncExec((new Runnable() {
			public void run() {
				final long timeDiff = Utility.getTimeForNextMeal(setting, true);
				long hours = timeDiff / 60;
				long mins = timeDiff % 60;
				lblMealTime.setText(hours + " Hrs : " + mins + " Mins");

				if (timeDiff <= Constants.MEAL_REMAINDER_INTERVAL) {
					setBolusButtons(true);
					dbMgr.setActivity("Next meal time warning activated!", Constants.ACTIVITY_STATUS_WARNING);
					Utility.playAlarm(Constants.SOUND_REMINDER);
				} else if (timeDiff == 0 && !setting.isManualInterventionRequired()) {
					injectBolus();
				}
			}
		}));
	}

	/**
	 * 
	 */
	public void injectBolus() {
		Constants.LAST_BOLUS_INJECTED_TIME = System.currentTimeMillis();
		Constants.IS_MEAL_POSTPONED = false;
		Constants.MEAL_POSTPONED_TIME = 0;
		Constants.RECENT_INJECTED_BOLUS = Constants.CURRENT_BOLUS_SESSION;
		setBolusButtons(false);
		AppSettings setting = dbMgr.getAppSettings();

		float bolus = 0;
		if (Constants.CURRENT_BOLUS_SESSION == Constants.BREAKFAST_BOLUS) {
			bolus = setting.getBreakfastCalories() / (500 / setting.getTdd());
		} else if (Constants.CURRENT_BOLUS_SESSION == Constants.LUNCH_BOLUS) {
			bolus = setting.getLunchcalories() / (500 / setting.getTdd());
		} else {
			bolus = setting.getDinnerCalories() / (500 / setting.getTdd());
		}

		Constants.CURRENT_INSULIN_RESERVOIR = Constants.CURRENT_INSULIN_RESERVOIR -10;
		int value = Constants.BLOOD_SUGAR_LEVEL - Constants.BLOOD_SUGAR_LEVEL / 2;
		Utility.getSugarLevel(3, value);

		SugarLevelRecord slr = new SugarLevelRecord();
		slr.setBolusInjectedInjected(bolus);
		dbMgr.save(slr);

		dbMgr.setActivity("Bolus injected : " + bolus + "mg/dl", Constants.ACTIVITY_STATUS_OK);

		setStatus(Constants.ICON_INJECTION_IMG, "Bolus injected : " + bolus + "mg/dl");

	}

	/**
	 * /** BATTERY = 1 INSULIN_RESERVOIR = 2 GLUCAGON_RESERVOIR =3 PUMP = 4
	 * BLOOD_GLU_SENSOR =5 NEEDLE_ASSEMBLY =6 ALARM =7 CURRENT_SUGAR_LEVEL = 8
	 * 
	 * @param status
	 * @param component
	 */
	public void setPreConditions(Map<Integer, Integer> pcs, PreConditionsRecord pcr) {
		dbMgr.setActivity("Performing check on all critical indicators", Constants.ACTIVITY_STATUS_OK);

		Display.getDefault().asyncExec((new Runnable() {
			public void run() {
				for (Entry<Integer, Integer> entry : pcs.entrySet()) {
					switch (entry.getKey()) {
					case Constants.BATTERY:
						setBatteryLife(entry.getValue(), pcr);
						break;

					case Constants.INSULIN_RESERVOIR:
						setInsulinReservoir(entry.getValue(), pcr);
						break;

					case Constants.GLUCAGON_RESERVOIR:
						setGlucagonReservoir(entry.getValue(), pcr);
						break;

					case Constants.PUMP:
						if (entry.getValue() != 0) { // All is well
							lblPumpStatus.setText("OK");
							lblPumpStatus.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_GREEN));
							pcr.setPumpTestResult(true);
						} else {
							pcr.setPumpTestResult(false);
							pcr.setCurrentStatus(0);
							lblPumpStatus.setText("FAULTY");
							lblPumpStatus.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_DARK_RED));
						}
						break;

					case Constants.BLOOD_GLU_SENSOR:
						if (entry.getValue() != 0) { // All is well
							pcr.setSensorTestResult(true);
							lblGlucoseSensorstatus.setText("OK");
							lblGlucoseSensorstatus.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_GREEN));
						} else {
							pcr.setCurrentStatus(0);
							pcr.setSensorTestResult(false);
							dbMgr.setActivity("Blood Glucose sensor assembly is not working as expected",
									Constants.ACTIVITY_STATUS_ERROR);
							lblGlucoseSensorstatus.setText("FAULTY");
							lblGlucoseSensorstatus
									.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_DARK_RED));
							// setStatus(Constants.ICON_ERROR_IMG, "Check
							// Critical Indicators!");
						}

						break;

					case Constants.NEEDLE_ASSEMBLY:
						if (entry.getValue() != 0) { // All is well
							pcr.setNeedleAssemblyTestResult(true);
							lblNeedleStatus.setText("OK");
							lblNeedleStatus.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_GREEN));
						} else {
							pcr.setNeedleAssemblyTestResult(false);
							pcr.setCurrentStatus(0);
							dbMgr.setActivity("Needle assembly is not working as expected",
									Constants.ACTIVITY_STATUS_ERROR);
							lblNeedleStatus.setText("FAULTY");
							lblNeedleStatus.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_DARK_RED));
							setStatus(Constants.ICON_ERROR_IMG, "Check Critical Indicators!");
						}
						break;

					case Constants.ALARM:
						if (entry.getValue() != 0) { // All is well
							pcr.setAlarmTestResult(true);
							lblAlarmStatus.setText("OK");
							lblAlarmStatus.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_GREEN));
						} else {
							pcr.setAlarmTestResult(false);
							pcr.setCurrentStatus(0);
							dbMgr.setActivity("Alarm is not working as expected", Constants.ACTIVITY_STATUS_ERROR);
							lblAlarmStatus.setText("FAULTY");
							lblAlarmStatus.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_DARK_RED));
							setStatus(Constants.ICON_ERROR_IMG, "Check Critical Indicators!");
						}
						break;

					case Constants.CURRENT_SUGAR_LEVEL:
						break;

					default:
						break;
					}
				}

				if (pcr.getCurrentStatus() == 0) {
					Utility.playAlarm(Constants.SOUND_REMINDER);
				}
			}
		}));

	}

	/**
	 * Method to enable all the bolus buttons
	 */
	public void setBolusButtons(final boolean value) {
		Display.getDefault().asyncExec((new Runnable() {
			public void run() {
				boolean v = value;
				if (!appSettings.isManualInterventionRequired()) {
					v = false;
				}
				btnPostPone.setEnabled(v);
				btnSkipBolus.setEnabled(v);
				buttonInjBolus.setEnabled(v);
			}
		}));
	}

	/**
	 * Create contents of the window.
	 */
	protected void createContents() {
		shlHomeScreen = new Shell(Display.getDefault(), SWT.TITLE | SWT.CLOSE | SWT.BORDER);
		shlHomeScreen.setImage(SWTResourceManager.getImage(HomeScreen.class, "/resources/health.png"));
		shlHomeScreen.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		shlHomeScreen.setSize(806, 683);
		shlHomeScreen.setText("TWO HARMONE SIMULATOR PUMP : GROUP D");

		Group grpCriticalIndicators = new Group(shlHomeScreen, SWT.NONE);
		grpCriticalIndicators.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		grpCriticalIndicators.setFont(SWTResourceManager.getFont("Calibri", 12, SWT.BOLD));
		grpCriticalIndicators.setText("Critical Indicators");
		grpCriticalIndicators.setBounds(515, 448, 275, 201);

		pbBattery = new ProgressBar(grpCriticalIndicators, SWT.NONE);
		pbBattery.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		pbBattery.setBounds(132, 32, 135, 17);

		Label lblBattery = new Label(grpCriticalIndicators, SWT.NONE);
		lblBattery.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		lblBattery.setBounds(10, 32, 37, 15);
		lblBattery.setText("Battery");

		pbInsulinReservoir = new ProgressBar(grpCriticalIndicators, SWT.NONE);
		pbInsulinReservoir.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		pbInsulinReservoir.setBounds(132, 55, 135, 17);

		Label lblInsulinReservoir = new Label(grpCriticalIndicators, SWT.NONE);
		lblInsulinReservoir.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		lblInsulinReservoir.setBounds(10, 55, 86, 15);
		lblInsulinReservoir.setText("Insulin Reservoir");

		pbGlucagonReservoir = new ProgressBar(grpCriticalIndicators, SWT.NONE);
		pbGlucagonReservoir.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		pbGlucagonReservoir.setBounds(132, 78, 135, 17);

		Label lblGlucagonReservoir = new Label(grpCriticalIndicators, SWT.NONE);
		lblGlucagonReservoir.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		lblGlucagonReservoir.setBounds(10, 78, 102, 15);
		lblGlucagonReservoir.setText("Glucagon Reservoir");

		Label lblPump = new Label(grpCriticalIndicators, SWT.NONE);
		lblPump.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		lblPump.setBounds(10, 106, 32, 15);
		lblPump.setText("Pump");

		Label lblBloodGlucoseSensor = new Label(grpCriticalIndicators, SWT.NONE);
		lblBloodGlucoseSensor.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		lblBloodGlucoseSensor.setBounds(10, 127, 114, 15);
		lblBloodGlucoseSensor.setText("Blood Glucose Sensor");

		Label lblNeedleAssembly = new Label(grpCriticalIndicators, SWT.NONE);
		lblNeedleAssembly.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		lblNeedleAssembly.setBounds(10, 148, 91, 15);
		lblNeedleAssembly.setText("Needle Assembly");

		Label lblAalarm = new Label(grpCriticalIndicators, SWT.NONE);
		lblAalarm.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		lblAalarm.setBounds(10, 169, 32, 15);
		lblAalarm.setText("Alarm");

		lblPumpStatus = new Label(grpCriticalIndicators, SWT.NONE);
		lblPumpStatus.setText("OK");
		lblPumpStatus.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.BOLD));
		lblPumpStatus.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		lblPumpStatus.setBounds(171, 101, 65, 21);

		lblGlucoseSensorstatus = new Label(grpCriticalIndicators, SWT.NONE);
		lblGlucoseSensorstatus.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.BOLD));
		lblGlucoseSensorstatus.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		lblGlucoseSensorstatus.setText("OK");
		lblGlucoseSensorstatus.setBounds(171, 127, 65, 21);

		lblNeedleStatus = new Label(grpCriticalIndicators, SWT.NONE);
		lblNeedleStatus.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.BOLD));
		lblNeedleStatus.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		lblNeedleStatus.setText("OK");
		lblNeedleStatus.setBounds(171, 148, 65, 21);

		lblAlarmStatus = new Label(grpCriticalIndicators, SWT.NONE);
		lblAlarmStatus.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.BOLD));
		lblAlarmStatus.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		lblAlarmStatus.setText("OK");
		lblAlarmStatus.setBounds(171, 169, 65, 21);

		Group grpNextBolusDosage = new Group(shlHomeScreen, SWT.NONE);
		grpNextBolusDosage.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		grpNextBolusDosage.setFont(SWTResourceManager.getFont("Calibri", 12, SWT.BOLD));
		grpNextBolusDosage.setText("Next Bolus Dosage");
		grpNextBolusDosage.setBounds(10, 133, 441, 61);

		lblMealName = new Label(grpNextBolusDosage, SWT.NONE);
		lblMealName.setText("Next Meal in :- ");
		lblMealName.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));
		lblMealName.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		lblMealName.setBounds(10, 25, 94, 20);

		btnSkipBolus = new Button(grpNextBolusDosage, SWT.NONE);
		btnSkipBolus.setForeground(SWTResourceManager.getColor(SWT.COLOR_DARK_RED));
		btnSkipBolus.setEnabled(false);
		btnSkipBolus.addSelectionListener(new SelectionAdapter() {
			// .setVisible(true)@Override
			public void widgetSelected(SelectionEvent e) {
				Constants.LAST_BOLUS_INJECTED_TIME = System.currentTimeMillis();
				Constants.RECENT_INJECTED_BOLUS = Constants.CURRENT_BOLUS_SESSION;
				Constants.IS_MEAL_POSTPONED = false;
				setBolusButtons(false);
			}
		});
		btnSkipBolus.setBounds(283, 23, 66, 22);
		btnSkipBolus.setText("Skip Bolus");

		lblMealTime = new Label(grpNextBolusDosage, SWT.NONE);
		lblMealTime.setForeground(SWTResourceManager.getColor(SWT.COLOR_DARK_RED));
		lblMealTime.setFont(SWTResourceManager.getFont("Segoe UI", 9, SWT.BOLD | SWT.ITALIC));
		lblMealTime.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		lblMealTime.setBounds(110, 27, 101, 20);

		buttonInjBolus = new Button(grpNextBolusDosage, SWT.NONE);
		buttonInjBolus.setForeground(SWTResourceManager.getColor(SWT.COLOR_DARK_RED));
		buttonInjBolus.setEnabled(false);
		buttonInjBolus.setFont(SWTResourceManager.getFont("Segoe UI", 8, SWT.BOLD));
		buttonInjBolus.setText("Inject Bolus");
		buttonInjBolus.setBounds(355, 24, 76, 21);
		buttonInjBolus.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				injectBolus();
			}
		});

		btnPostPone = new Button(grpNextBolusDosage, SWT.NONE);
		btnPostPone.setForeground(SWTResourceManager.getColor(SWT.COLOR_DARK_RED));
		btnPostPone.setEnabled(false);
		btnPostPone.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				btnPostPone.setEnabled(false);
				Constants.MEAL_POSTPONED_TIME = System.currentTimeMillis();
				Constants.IS_MEAL_POSTPONED = true;
			}
		});
		btnPostPone.setBounds(217, 23, 60, 22);
		btnPostPone.setText("Postpone");
		Button btnHelp = new Button(shlHomeScreen, SWT.NONE);
		btnHelp.setForeground(SWTResourceManager.getColor(SWT.COLOR_DARK_RED));
		btnHelp.setFont(SWTResourceManager.getFont("Segoe UI", 10, SWT.BOLD));
		btnHelp.setBounds(686, 140, 104, 54);
		btnHelp.setText("Help");

		btnHelp.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Help help = new Help();
				help.open();
			}
		});

		Button btnGraphView = new Button(shlHomeScreen, SWT.NONE);
		btnGraphView.setForeground(SWTResourceManager.getColor(SWT.COLOR_DARK_RED));
		btnGraphView.setFont(SWTResourceManager.getFont("Segoe UI", 10, SWT.BOLD));
		btnGraphView.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Display display = Display.getDefault();
				createContents();
				graph.open(dbMgr);
				while (null != graph.shlGraphView && !graph.shlGraphView.isDisposed()) {
					if (!display.readAndDispatch()) {
						display.sleep();
					}
				}
			}
		});
		btnGraphView.setBounds(470, 140, 100, 54);
		btnGraphView.setText("Graph View");

		Button btnSettings = new Button(shlHomeScreen, SWT.NONE);
		btnSettings.setForeground(SWTResourceManager.getColor(SWT.COLOR_DARK_RED));
		btnSettings.setFont(SWTResourceManager.getFont("Segoe UI", 10, SWT.BOLD));
		HomeScreen hs = this;
		btnSettings.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Display display = Display.getDefault();
				createContents();
				settings.open(dbMgr, hs);
				while (null != settings.shlSettings && !settings.shlSettings.isDisposed()) {
					if (!display.readAndDispatch()) {
						display.sleep();
					}
				}
			}

		});
		btnSettings.setBounds(576, 140, 104, 54);
		btnSettings.setText("Settings");

		lblClock = new Label(shlHomeScreen, SWT.NONE);
		lblClock.setAlignment(SWT.RIGHT);
		lblClock.setForeground(SWTResourceManager.getColor(SWT.COLOR_DARK_RED));
		lblClock.setFont(SWTResourceManager.getFont("Calibri", 12, SWT.BOLD));
		lblClock.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		lblClock.setBounds(638, 87, 152, 20);
		lblClock.setText(new SimpleDateFormat("HH:mm:ss").format(new Date()));

		// Timer to display running date & Time
		// Updates for every 1 sec
		Timer timer = new Timer("clock timer", true);
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				Display display = Display.getDefault();
				display.asyncExec(new Runnable() {
					@Override
					public void run() {
						lblClock.setText(new SimpleDateFormat("d MMM yyyy, HH:mm:ss ").format(new Date()));
					}
				});
			}
		}, 1000l, 1000l);

		Label lblLogo = new Label(shlHomeScreen, SWT.NONE);
		lblLogo.setImage(SWTResourceManager.getImage(HomeScreen.class, "/resources/Uni_Logo.gif"));
		lblLogo.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		lblLogo.setBounds(680, 10, 110, 54);

		//Group group = new Group(shlHomeScreen, SWT.NONE);

		Label lblLatestSugarLevels = new Label(shlHomeScreen, SWT.NONE);
		lblLatestSugarLevels.setBounds(10, 102, 123, 23);
		lblLatestSugarLevels.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		lblLatestSugarLevels.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.BOLD | SWT.ITALIC));
		lblLatestSugarLevels.setText("Sugar Level Indicator");

		Label lblNewLabel = new Label(shlHomeScreen, SWT.NONE);
		lblNewLabel.setFont(SWTResourceManager.getFont("Segoe UI", 9, SWT.BOLD | SWT.ITALIC));
		lblNewLabel.setBounds(412, 102, 193, 17);
		lblNewLabel.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		lblNewLabel.setText("Expected Value 70 - 120 mg/dl");

		lblStatusIndicator = new Label(shlHomeScreen, SWT.NONE);
		lblStatusIndicator.setAlignment(SWT.CENTER);
		lblStatusIndicator.setBounds(10, 10, 53, 44);
		lblStatusIndicator.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		lblStatusIndicator.setImage(SWTResourceManager.getImage(HomeScreen.class, Constants.ICON_OK_IMG));

		Label label = new Label(shlHomeScreen, SWT.SEPARATOR | SWT.HORIZONTAL);
		label.setBackground(SWTResourceManager.getColor(SWT.COLOR_DARK_RED));
		label.setBounds(0, 72, 800, 9);

		lblStatusMessage = new Text(shlHomeScreen, SWT.MULTI);
		lblStatusMessage.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		lblStatusMessage.setFont(SWTResourceManager.getFont("Baskerville Old Face", 20, SWT.BOLD));
		lblStatusMessage.setText("Application Started !");
		lblStatusMessage.setEditable(false);
		lblStatusMessage.setBounds(76, 10, 598, 44);

		txtSugarLevel = new Text(shlHomeScreen, SWT.CENTER);
		txtSugarLevel.setFont(SWTResourceManager.getFont("Yu Mincho Demibold", 25, SWT.BOLD));
		txtSugarLevel.setText("85 mg/dl");
		txtSugarLevel.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		txtSugarLevel.setEditable(false);
		txtSugarLevel.setBounds(165, 90, 214, 45);

		txtActivityLog = new Text(shlHomeScreen, SWT.BORDER | SWT.READ_ONLY | SWT.WRAP | SWT.V_SCROLL | SWT.MULTI);
		txtActivityLog.setFont(SWTResourceManager.getFont("Times New Roman Greek", 9, SWT.NORMAL));
		txtActivityLog.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		txtActivityLog.setEditable(false);
		txtActivityLog.setBounds(10, 448, 499, 201);
		
		compGraph = new Composite(shlHomeScreen, SWT.EMBEDDED);
		compGraph.setLayout(new FillLayout());
		compGraph.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		compGraph.setBounds(10, 200, 780, 245);

		java.awt.Frame fileTableFrame = SWT_AWT.new_Frame(compGraph);
		fileTableFrame.setForeground(Color.WHITE);
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				ChartPanel chartPanel = new ChartPanel(chart);
				chartPanel.setForeground(Color.WHITE);
				fileTableFrame.add(chartPanel, BorderLayout.CENTER);
			}
		});
		gTimer.start();
	}

	private JFreeChart createChart(final XYDataset dataset) {
		final JFreeChart result = ChartFactory.createTimeSeriesChart("", "Time",
				"Blood Sugar Level", dataset, true, true, false);
		final XYPlot plot = result.getXYPlot();
		ValueAxis xaxis = plot.getDomainAxis();
		xaxis.setAutoRange(true);

		// Domain axis would show data of 60 seconds for a time
		xaxis.setFixedAutoRange(60000.0); // 60 seconds
		xaxis.setVerticalTickLabels(true);

		ValueAxis yaxis = plot.getRangeAxis();
		yaxis.setRange(0.0, 250.0);

		Marker marker = new ValueMarker(70);
        marker.setOutlinePaint(Color.green);
        marker.setPaint(Color.green);
        marker.setStroke(new BasicStroke(1.0f));
        plot.addRangeMarker(marker, Layer.FOREGROUND);
        
		Marker m2 = new ValueMarker(120);
		m2.setOutlinePaint(Color.green);
		m2.setPaint(Color.green);
		m2.setStroke(new BasicStroke(1.0f));
        plot.addRangeMarker(m2, Layer.FOREGROUND);
        
		return result;
	}

	private static final Random random = new Random();
	private Text txtActivityLog;

	private float randomValue() {
		return (float) (random.nextGaussian() * 500 / 3);
	}

	private float[] gaussianData() {
		float[] a = new float[120];
		for (int i = 0; i < a.length; i++) {
			a[i] = randomValue();
		}
		return a;
	}
}
