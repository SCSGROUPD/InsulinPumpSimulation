package gui;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;

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
public class HomeScreen {
	private StyledText txtActivityLog;
	private ProgressBar progressBarSugarLevel;
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

	private Map<Integer, Integer> preConditions = new HashMap();

	// Injected from Spring
	private SettingsPage settings;
	private GraphView graph;
	private CARBRemainderPage carb;
	private DBManager dbMgr;
	private Text lblStatusMessage;

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
			HomeScreen window = new HomeScreen();
			window.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
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
	public void setSugarLevel(int sugarLevel) {
		Display.getDefault().asyncExec((new Runnable() {
			public void run() {
				progressBarSugarLevel.setSelection(100);
				progressBarSugarLevel.setToolTipText(sugarLevel + "mg/dl");
				if ((sugarLevel >= 65 && sugarLevel <= 75) || (sugarLevel >= 115 && sugarLevel <= 125)) {
					progressBarSugarLevel.setState(SWT.PAUSE);
				} else if (sugarLevel <= 64 || sugarLevel > 125) {
					dbMgr.setActivity("Sugar level is beyound the range. Sugar level is " + sugarLevel + " md/dl",
							Constants.ACTIVITY_STATUS_ERROR);
					progressBarSugarLevel.setState(SWT.ERROR);
				}else{
					progressBarSugarLevel.setState(SWT.NORMAL);
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
			pcr.setCurrentStatus(false);
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
				/*
				 * if (isPreConditionsFailed) { if (faultyText.isEmpty()) {
				 * faultyText = statusTxt; } statusText = faultyText; icon =
				 * Constants.ICON_ERROR_IMG; }
				 */
				if (Constants.ICON_ERROR_IMG.equals(iconPath)) {
					if (faultyText.isEmpty()) {
						faultyText = statusTxt;
					}
					icon = Constants.ICON_ERROR_IMG;
				}
				Constants.CURRENT_CYCLE_STATUS += statusTxt +"\n";
				lblStatusIndicator.setImage(SWTResourceManager.getImage(HomeScreen.class, icon));
				lblStatusMessage.setText(Constants.CURRENT_CYCLE_STATUS);
			}
		}));
	}

	public void setInsulinReservoir(int value, PreConditionsRecord pcr) {
		pbInsulinReservoir.setSelection(value);
		pcr.setInsulinReservoirTestResult(true);
		pbInsulinReservoir.setState(SWT.NORMAL);
		pbInsulinReservoir.setToolTipText(value + "%");
		if (value <= 30) {
			pcr.setCurrentStatus(false);
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
			pcr.setCurrentStatus(false);
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
		
		float bolus =0;
		if(Constants.CURRENT_BOLUS_SESSION == Constants.BREAKFAST_BOLUS){
			bolus = setting.getBreakfastCalories()/(500/setting.getTdd());
		}else if(Constants.CURRENT_BOLUS_SESSION == Constants.LUNCH_BOLUS){
			bolus = setting.getLunchcalories()/(500/setting.getTdd());
		}else{
			bolus = setting.getDinnerCalories()/(500/setting.getTdd());
		}
		
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
							pcr.setCurrentStatus(false);
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
							pcr.setCurrentStatus(false);
							pcr.setSensorTestResult(false);
							dbMgr.setActivity("Blood Glucose sensor assembly is not working as expected",
									Constants.ACTIVITY_STATUS_ERROR);
							lblGlucoseSensorstatus.setText("FAULTY");
							lblGlucoseSensorstatus
									.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_DARK_RED));
							//setStatus(Constants.ICON_ERROR_IMG, "Check Critical Indicators!");
						}

						break;

					case Constants.NEEDLE_ASSEMBLY:
						if (entry.getValue() != 0) { // All is well
							pcr.setNeedleAssemblyTestResult(true);
							lblNeedleStatus.setText("OK");
							lblNeedleStatus.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_GREEN));
						} else {
							pcr.setNeedleAssemblyTestResult(false);
							pcr.setCurrentStatus(false);
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
							pcr.setCurrentStatus(false);
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

				if (!pcr.getCurrentStatus()) {
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
				boolean v =value;
				if(!appSettings.isManualInterventionRequired()){
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
		shlHomeScreen.setSize(748, 520);
		shlHomeScreen.setText("TWO HARMONE SIMULATOR PUMP : GROUP D");

		Group grpCriticalIndicators = new Group(shlHomeScreen, SWT.NONE);
		grpCriticalIndicators.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		grpCriticalIndicators.setFont(SWTResourceManager.getFont("Calibri", 12, SWT.BOLD));
		grpCriticalIndicators.setText("Critical Indicators");
		grpCriticalIndicators.setBounds(457, 133, 305, 279);

		pbBattery = new ProgressBar(grpCriticalIndicators, SWT.NONE);
		pbBattery.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		pbBattery.setBounds(132, 32, 135, 17);

		Label lblBattery = new Label(grpCriticalIndicators, SWT.NONE);
		lblBattery.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		lblBattery.setBounds(10, 32, 37, 15);
		lblBattery.setText("Battery");

		pbInsulinReservoir = new ProgressBar(grpCriticalIndicators, SWT.NONE);
		pbInsulinReservoir.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		pbInsulinReservoir.setBounds(132, 68, 135, 17);

		Label lblInsulinReservoir = new Label(grpCriticalIndicators, SWT.NONE);
		lblInsulinReservoir.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		lblInsulinReservoir.setBounds(10, 68, 86, 15);
		lblInsulinReservoir.setText("Insulin Reservoir");

		pbGlucagonReservoir = new ProgressBar(grpCriticalIndicators, SWT.NONE);
		pbGlucagonReservoir.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		pbGlucagonReservoir.setBounds(132, 102, 135, 17);

		Label lblGlucagonReservoir = new Label(grpCriticalIndicators, SWT.NONE);
		lblGlucagonReservoir.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		lblGlucagonReservoir.setBounds(10, 102, 102, 15);
		lblGlucagonReservoir.setText("Glucagon Reservoir");

		Label lblPump = new Label(grpCriticalIndicators, SWT.NONE);
		lblPump.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		lblPump.setBounds(10, 149, 32, 15);
		lblPump.setText("Pump");

		Label lblBloodGlucoseSensor = new Label(grpCriticalIndicators, SWT.NONE);
		lblBloodGlucoseSensor.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		lblBloodGlucoseSensor.setBounds(10, 180, 114, 15);
		lblBloodGlucoseSensor.setText("Blood Glucose Sensor");

		Label lblNeedleAssembly = new Label(grpCriticalIndicators, SWT.NONE);
		lblNeedleAssembly.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		lblNeedleAssembly.setBounds(10, 216, 91, 15);
		lblNeedleAssembly.setText("Needle Assembly");

		Label lblAalarm = new Label(grpCriticalIndicators, SWT.NONE);
		lblAalarm.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		lblAalarm.setBounds(10, 255, 32, 15);
		lblAalarm.setText("Alarm");

		lblPumpStatus = new Label(grpCriticalIndicators, SWT.NONE);
		lblPumpStatus.setText("OK");
		lblPumpStatus.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.BOLD));
		lblPumpStatus.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		lblPumpStatus.setBounds(171, 144, 65, 21);

		lblGlucoseSensorstatus = new Label(grpCriticalIndicators, SWT.NONE);
		lblGlucoseSensorstatus.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.BOLD));
		lblGlucoseSensorstatus.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		lblGlucoseSensorstatus.setText("OK");
		lblGlucoseSensorstatus.setBounds(171, 180, 65, 21);

		lblNeedleStatus = new Label(grpCriticalIndicators, SWT.NONE);
		lblNeedleStatus.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.BOLD));
		lblNeedleStatus.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		lblNeedleStatus.setText("OK");
		lblNeedleStatus.setBounds(171, 216, 65, 21);

		lblAlarmStatus = new Label(grpCriticalIndicators, SWT.NONE);
		lblAlarmStatus.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.BOLD));
		lblAlarmStatus.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		lblAlarmStatus.setText("OK");
		lblAlarmStatus.setBounds(171, 255, 65, 21);

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
		btnSkipBolus.setEnabled(false);
		btnSkipBolus.addSelectionListener(new SelectionAdapter() {
			// .setVisible(true)@Override
			public void widgetSelected(SelectionEvent e) {
				Constants.LAST_BOLUS_INJECTED_TIME = System.currentTimeMillis();
				Constants.RECENT_INJECTED_BOLUS = Constants.CURRENT_BOLUS_SESSION;
				Constants.IS_MEAL_POSTPONED = false;
				setBolusButtons(false);
	/*			Display display = Display.getDefault();
				createContents();
				CARBRemainderPage crp = new CARBRemainderPage();
				crp.open(false);
				while (null != crp.shlCarbAndRemainder && !crp.shlCarbAndRemainder.isDisposed()) {
					if (!display.readAndDispatch()) {
						display.sleep();
					}
				}*/
			}
		});
		btnSkipBolus.setBounds(283, 23, 66, 22);
		btnSkipBolus.setText("Skip Bolus");

		lblMealTime = new Label(grpNextBolusDosage, SWT.NONE);
		lblMealTime.setFont(SWTResourceManager.getFont("Segoe UI", 9, SWT.BOLD | SWT.ITALIC));
		lblMealTime.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		lblMealTime.setBounds(110, 27, 101, 20);

		buttonInjBolus = new Button(grpNextBolusDosage, SWT.NONE);
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

		Group grpActivityLog = new Group(shlHomeScreen, SWT.NONE);
		grpActivityLog.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		grpActivityLog.setFont(SWTResourceManager.getFont("Calibri", 12, SWT.BOLD));
		grpActivityLog.setText("Activity Log");
		grpActivityLog.setBounds(10, 200, 441, 271);

		txtActivityLog = new StyledText(grpActivityLog, SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
		txtActivityLog.setDoubleClickEnabled(false);
		txtActivityLog.setEditable(false);
		txtActivityLog.setFont(SWTResourceManager.getFont("Verdana", 8, SWT.NORMAL));
		txtActivityLog.setBounds(10, 21, 421, 240);
		Button btnHelp = new Button(shlHomeScreen, SWT.NONE);
		btnHelp.setBounds(467, 446, 81, 25);
		btnHelp.setText("Help");

		btnHelp.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Help help = new Help();
				help.open();
			}
		});
		
		Button btnGraphView = new Button(shlHomeScreen, SWT.NONE);
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
		btnGraphView.setBounds(554, 446, 81, 25);
		btnGraphView.setText("Graph View");

		Button btnSettings = new Button(shlHomeScreen, SWT.NONE);
		HomeScreen hs = this;
		btnSettings.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Display display = Display.getDefault();
				createContents();
				settings.open(dbMgr,hs);
				while (null != settings.shlSettings && !settings.shlSettings.isDisposed()) {
					if (!display.readAndDispatch()) {
						display.sleep();
					}
				}
			}

		});
		btnSettings.setBounds(641, 446, 81, 25);
		btnSettings.setText("Settings");

		lblClock = new Label(shlHomeScreen, SWT.NONE);
		lblClock.setAlignment(SWT.RIGHT);
		lblClock.setForeground(SWTResourceManager.getColor(SWT.COLOR_DARK_RED));
		lblClock.setFont(SWTResourceManager.getFont("Calibri", 12, SWT.BOLD));
		lblClock.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		lblClock.setBounds(570, 102, 152, 25);
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
		lblLogo.setBounds(614, 0, 110, 54);

		Group group = new Group(shlHomeScreen, SWT.NONE);

		Browser browser = new Browser(shlHomeScreen, SWT.NONE);
		browser.setBounds(467, 439, 64, 32);

		Label lblLatestSugarLevels = new Label(shlHomeScreen, SWT.NONE);
		lblLatestSugarLevels.setBounds(22, 92, 123, 23);
		lblLatestSugarLevels.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		lblLatestSugarLevels.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.BOLD | SWT.ITALIC));
		lblLatestSugarLevels.setText("Sugar Level Indicator");

		Label lblNewLabel = new Label(shlHomeScreen, SWT.NONE);
		lblNewLabel.setBounds(289, 115, 169, 17);
		lblNewLabel.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		lblNewLabel.setText("Expected Value 70 - 120 mg/dl");

		progressBarSugarLevel = new ProgressBar(shlHomeScreen, SWT.NONE);
		progressBarSugarLevel.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		progressBarSugarLevel.setBounds(166, 92, 285, 17);
				
						lblStatusIndicator = new Label(shlHomeScreen, SWT.NONE);
						lblStatusIndicator.setAlignment(SWT.CENTER);
						lblStatusIndicator.setBounds(10, 10, 53, 44);
						lblStatusIndicator.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
						lblStatusIndicator.setImage(SWTResourceManager.getImage(HomeScreen.class, Constants.ICON_OK_IMG));
						
						Label label = new Label(shlHomeScreen, SWT.SEPARATOR | SWT.HORIZONTAL);
						label.setBackground(SWTResourceManager.getColor(SWT.COLOR_DARK_RED));
						label.setBounds(0, 72, 743, 5);
						
						lblStatusMessage = new Text(shlHomeScreen, SWT.MULTI);
						lblStatusMessage.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
						lblStatusMessage.setFont(SWTResourceManager.getFont("Baskerville Old Face", 20, SWT.BOLD));
						lblStatusMessage.setText("Application Started !");
						lblStatusMessage.setEditable(false);
						lblStatusMessage.setBounds(76, 10, 532, 44);

	}
}
