package gui;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
import org.eclipse.wb.swt.SWTResourceManager;

import dba.DBManager;
import entities.ActivityLog;
import entities.AppSettings;
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
	private Label lblStatusMessage;
	private volatile boolean isAppFaulty = false;
	private volatile String faultyText = "";

	private Map<Integer, Integer> preConditions = new HashMap();

	// Injected from Spring
	private SettingsPage settings;
	private GraphView graph;
	private CARBRemainderPage carb;
	private DBManager dbMgr;

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
	 * @param sugarLevel
	 */
	public void setSugarLevel(int sugarLevel) {
		Display.getDefault().asyncExec((new Runnable() {
			public void run() {
				progressBarSugarLevel.setSelection(sugarLevel);
				progressBarSugarLevel.setState(SWT.NORMAL);
				progressBarSugarLevel.setToolTipText(sugarLevel + "mg/dl");
				if (sugarLevel <= 70 || sugarLevel > 140) {
					dbMgr.setActivity("Sugar level is beyound the range. Sugar level is " + sugarLevel + " md/dl", Constants.ACTIVITY_STATUS_ERROR);
					progressBarSugarLevel.setState(SWT.ERROR);
				}else{
//					dbMgr.setActivity("Sugar level is within the range. Sugar level is " + sugarLevel + " md/dl", Constants.ACTIVITY_STATUS_OK);
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
	public void setBatteryLife(int value) {
		pbBattery.setSelection(value);
		pbBattery.setState(SWT.NORMAL);
		pbBattery.setToolTipText(value + "%");
		setStatus(Constants.STATUS_OK_IMG, "ALL IS WELL!");
		if (value <= 30) {
			dbMgr.setActivity("Battery power too low!", Constants.ACTIVITY_STATUS_ERROR);
			isAppFaulty = true;
			pbBattery.setState(SWT.ERROR);
			setStatus(Constants.STATUS_ERROR_IMG, "Critical Indicator/s failed!");
		} else if (value > 30 && value <= 40) {
			dbMgr.setActivity("Battery power is weak!", Constants.ACTIVITY_STATUS_WARNING);
			pbBattery.setState(SWT.PAUSED);
			setStatus(Constants.STATUS_WARNING_IMG, "Check Critical Indicators!");
		}else{
//			dbMgr.setActivity("Battery power stable", Constants.ACTIVITY_STATUS_OK);
		}
	}

	/**
	 * Set the status text & icon
	 * 
	 * @param iconPath
	 * @param statusTxt
	 */
	public void setStatus(String iconPath, String statusTxt) {
		// Don't over ride if the app is already in error state
		String icon = iconPath;
		if (isAppFaulty) {
			if (faultyText.isEmpty()) {
				faultyText = statusTxt;
			}
			statusTxt = faultyText;
			icon = Constants.STATUS_ERROR_IMG;
		}
		lblStatusIndicator.setImage(SWTResourceManager.getImage(HomeScreen.class, icon));
		lblStatusMessage.setText(statusTxt);
	}

	public void setInsulinReservoir(int value) {
		pbInsulinReservoir.setSelection(value);
		pbInsulinReservoir.setState(SWT.NORMAL);
		pbInsulinReservoir.setToolTipText(value + "%");
		setStatus(Constants.STATUS_OK_IMG, "ALL IS WELL!");
		if (value <= 30) {
			isAppFaulty = true;
			pbInsulinReservoir.setState(SWT.ERROR);
			setStatus(Constants.STATUS_ERROR_IMG, "Critical Indicator/s failed!");
			dbMgr.setActivity("Insulin reservoir in critical state. Please refill it", Constants.ACTIVITY_STATUS_ERROR);
		} else if (value > 30 && value <= 40) {
			pbInsulinReservoir.setState(SWT.PAUSED);
			setStatus(Constants.STATUS_WARNING_IMG, "Check Critical Indicators!");
			dbMgr.setActivity("Insulin reservoir needs attention. Please refill it", Constants.ACTIVITY_STATUS_WARNING);
		}else{
//			dbMgr.setActivity("Insulin reservoir is good enough", Constants.ACTIVITY_STATUS_OK);
		}
	}

	/**
	 * @param value
	 */
	public void setGlucagonReservoir(int value) {
		pbGlucagonReservoir.setSelection(value);
		pbGlucagonReservoir.setState(SWT.NORMAL);
		pbGlucagonReservoir.setToolTipText(value + "%");
		setStatus(Constants.STATUS_OK_IMG, "ALL IS WELL!");
		if (value <= 30) {
			isAppFaulty = true;
			pbGlucagonReservoir.setState(SWT.ERROR);
			setStatus(Constants.STATUS_ERROR_IMG, "Critical Indicator/s failed!");
			dbMgr.setActivity("Glucagon reservoir needs to be refilled immediately.", Constants.ACTIVITY_STATUS_ERROR);
		} else if (value > 30 && value <= 40) {
			pbGlucagonReservoir.setState(SWT.PAUSED);
			setStatus(Constants.STATUS_WARNING_IMG, "Check Critical Indicators!");
			dbMgr.setActivity("Glucagon reservoir is draining. Please refill", Constants.ACTIVITY_STATUS_WARNING);
		}else{
//			dbMgr.setActivity("Glucagon reservoir is good enough", Constants.ACTIVITY_STATUS_OK);
		}
	}

	/**
	 * Set the meal name & remaining time
	 */
	public void setMealTime(AppSettings setting) {
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
		String str = sdf.format(new Date());
		float currTime = Integer.parseInt(str.split(":")[0]) + Integer.parseInt(str.split(":")[1]) / 100;
		float breakFastTime = Integer.parseInt(setting.getBreakfastTime().split(":")[0])
				+ Integer.parseInt(setting.getBreakfastTime().split(":")[1]) / 100;
		float lunchTime = Integer.parseInt(setting.getLunchTime().split(":")[0])
				+ Integer.parseInt(setting.getLunchTime().split(":")[1]) / 100;
		float dinnerTime = Integer.parseInt(setting.getDinnerTime().split(":")[0])
				+ Integer.parseInt(setting.getDinnerTime().split(":")[1]) / 100;

		Display.getDefault().asyncExec((new Runnable() {
			public void run() {
				String timeDiff = "00:00";
				if (currTime <= breakFastTime || currTime > dinnerTime) {
					timeDiff = Utility.getTimeDifference(str, setting.getBreakfastTime());
				} else if (currTime > breakFastTime && currTime <= lunchTime) {
					timeDiff = Utility.getTimeDifference(str, setting.getLunchTime());
				} else {
					timeDiff = Utility.getTimeDifference(str, setting.getDinnerTime());
				}
				lblMealTime.setText(timeDiff.split(":")[0] + " Hrs : " + timeDiff.split(":")[1] + " Mins");

				float diffTime = Float.parseFloat(timeDiff.split(":")[0])
						+ Float.parseFloat(timeDiff.split(":")[1]) / 100;
				if (diffTime <= 0.15) {
					dbMgr.setActivity("Next meal time warning activated!", Constants.ACTIVITY_STATUS_WARNING);
					Utility.makeNoise(Constants.SOUND_REMINDER);
				}
			}
		}));
	}

	/**
	 * /** BATTERY = 1 INSULIN_RESERVOIR = 2 GLUCAGON_RESERVOIR =3 PUMP = 4
	 * BLOOD_GLU_SENSOR =5 NEEDLE_ASSEMBLY =6 ALARM =7 CURRENT_SUGAR_LEVEL = 8
	 * 
	 * @param status
	 * @param component
	 */
	public void setPreConditions(Map<Integer, Integer> pcs) {
		isAppFaulty = false;
		dbMgr.setActivity("Performing check on all critical indicators", Constants.ACTIVITY_STATUS_OK);
		Display.getDefault().asyncExec((new Runnable() {
			public void run() {
				for (Entry<Integer, Integer> entry : pcs.entrySet()) {
					switch (entry.getKey()) {
					case Constants.BATTERY:
						setBatteryLife(entry.getValue());
						break;

					case Constants.INSULIN_RESERVOIR:
						setInsulinReservoir(entry.getValue());
						break;

					case Constants.GLUCAGON_RESERVOIR:
						setGlucagonReservoir(entry.getValue());
						break;

					case Constants.PUMP:
						if (entry.getValue() != 0) { // All is well
							lblPumpStatus.setText("OK");
							setStatus(Constants.STATUS_OK_IMG, "ALL IS WELL!");
							lblPumpStatus.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_GREEN));
						} else {
							isAppFaulty = true;
							lblPumpStatus.setText("FAULTY");
							setStatus(Constants.STATUS_ERROR_IMG, "Check Critical Indicators!");
							lblPumpStatus.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_DARK_RED));
						}
						break;

					case Constants.BLOOD_GLU_SENSOR:
						if (entry.getValue() != 0) { // All is well
							lblGlucoseSensorstatus.setText("OK");
							setStatus(Constants.STATUS_OK_IMG, "ALL IS WELL!");
//							dbMgr.setActivity("Blood Glucose sensor assembly is not working as expected", Constants.ACTIVITY_STATUS_ERROR);
							lblGlucoseSensorstatus.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_GREEN));
						} else {
							isAppFaulty = true;
							dbMgr.setActivity("Blood Glucose sensor assembly is not working as expected", Constants.ACTIVITY_STATUS_ERROR);
							lblGlucoseSensorstatus.setText("FAULTY");
							lblGlucoseSensorstatus
									.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_DARK_RED));
							setStatus(Constants.STATUS_ERROR_IMG, "Check Critical Indicators!");
						}

						break;

					case Constants.NEEDLE_ASSEMBLY:
						if (entry.getValue() != 0) { // All is well
//							dbMgr.setActivity("Needle assembly is working as expected", Constants.ACTIVITY_STATUS_OK);
							lblNeedleStatus.setText("OK");
							setStatus(Constants.STATUS_OK_IMG, "ALL IS WELL!");
							lblNeedleStatus.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_GREEN));
						} else {
							isAppFaulty = true;
							dbMgr.setActivity("Needle assembly is not working as expected", Constants.ACTIVITY_STATUS_ERROR);
							lblNeedleStatus.setText("FAULTY");
							lblNeedleStatus.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_DARK_RED));
							setStatus(Constants.STATUS_ERROR_IMG, "Check Critical Indicators!");
						}
						break;

					case Constants.ALARM:
						if (entry.getValue() != 0) { // All is well
//							dbMgr.setActivity("Alarm is working as expected", Constants.ACTIVITY_STATUS_OK);
							lblAlarmStatus.setText("OK");
							setStatus(Constants.STATUS_OK_IMG, "ALL IS WELL!");
							lblAlarmStatus.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_GREEN));
						} else {
							dbMgr.setActivity("Alarm is not working as expected", Constants.ACTIVITY_STATUS_ERROR);
							lblAlarmStatus.setText("FAULTY");
							isAppFaulty = true;
							lblAlarmStatus.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_DARK_RED));
							setStatus(Constants.STATUS_ERROR_IMG, "Check Critical Indicators!");
						}
						break;

					case Constants.CURRENT_SUGAR_LEVEL:
						System.out.println("");
						break;

					default:
						break;
					}
				}
			}
		}));
	}

	/**
	 * Create contents of the window.
	 */
	protected void createContents() {
		shlHomeScreen = new Shell(Display.getDefault(), SWT.TITLE | SWT.CLOSE | SWT.BORDER);
		shlHomeScreen.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		shlHomeScreen.setSize(748, 520);
		shlHomeScreen.setText("TWO HARMONE SIMULATOR PUMP : GROUP D");

		Group grpCriticalIndicators = new Group(shlHomeScreen, SWT.NONE);
		grpCriticalIndicators.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		grpCriticalIndicators.setFont(SWTResourceManager.getFont("Calibri", 12, SWT.BOLD));
		grpCriticalIndicators.setText("Critical Indicators");
		grpCriticalIndicators.setBounds(457, 43, 265, 388);

		pbBattery = new ProgressBar(grpCriticalIndicators, SWT.NONE);
		pbBattery.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		pbBattery.setBounds(132, 32, 123, 25);

		Label lblBattery = new Label(grpCriticalIndicators, SWT.NONE);
		lblBattery.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		lblBattery.setBounds(10, 42, 55, 15);
		lblBattery.setText("Battery");

		pbInsulinReservoir = new ProgressBar(grpCriticalIndicators, SWT.NONE);
		pbInsulinReservoir.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		pbInsulinReservoir.setBounds(132, 79, 123, 25);

		Label lblInsulinReservoir = new Label(grpCriticalIndicators, SWT.NONE);
		lblInsulinReservoir.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		lblInsulinReservoir.setBounds(10, 89, 102, 15);
		lblInsulinReservoir.setText("Insulin Reservoir");

		pbGlucagonReservoir = new ProgressBar(grpCriticalIndicators, SWT.NONE);
		pbGlucagonReservoir.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		pbGlucagonReservoir.setBounds(132, 131, 123, 25);

		Label lblGlucagonReservoir = new Label(grpCriticalIndicators, SWT.NONE);
		lblGlucagonReservoir.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		lblGlucagonReservoir.setBounds(10, 141, 116, 15);
		lblGlucagonReservoir.setText("Glucagon Reservoir");

		Label lblPump = new Label(grpCriticalIndicators, SWT.NONE);
		lblPump.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		lblPump.setBounds(10, 207, 116, 15);
		lblPump.setText("Pump");

		Label lblBloodGlucoseSensor = new Label(grpCriticalIndicators, SWT.NONE);
		lblBloodGlucoseSensor.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		lblBloodGlucoseSensor.setBounds(10, 251, 129, 15);
		lblBloodGlucoseSensor.setText("Blood Glucose Sensor");

		Label lblNeedleAssembly = new Label(grpCriticalIndicators, SWT.NONE);
		lblNeedleAssembly.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		lblNeedleAssembly.setBounds(10, 294, 116, 15);
		lblNeedleAssembly.setText("Needle Assembly");

		Label lblAalarm = new Label(grpCriticalIndicators, SWT.NONE);
		lblAalarm.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		lblAalarm.setBounds(10, 342, 116, 15);
		lblAalarm.setText("Alarm");

		lblPumpStatus = new Label(grpCriticalIndicators, SWT.NONE);
		lblPumpStatus.setText("OK");
		lblPumpStatus.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.BOLD));
		lblPumpStatus.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		lblPumpStatus.setBounds(171, 202, 68, 25);

		lblGlucoseSensorstatus = new Label(grpCriticalIndicators, SWT.NONE);
		lblGlucoseSensorstatus.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.BOLD));
		lblGlucoseSensorstatus.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		lblGlucoseSensorstatus.setText("OK");
		lblGlucoseSensorstatus.setBounds(171, 251, 55, 25);

		lblNeedleStatus = new Label(grpCriticalIndicators, SWT.NONE);
		lblNeedleStatus.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.BOLD));
		lblNeedleStatus.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		lblNeedleStatus.setText("OK");
		lblNeedleStatus.setBounds(171, 294, 55, 25);

		lblAlarmStatus = new Label(grpCriticalIndicators, SWT.NONE);
		lblAlarmStatus.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.BOLD));
		lblAlarmStatus.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		lblAlarmStatus.setText("OK");
		lblAlarmStatus.setBounds(171, 342, 55, 25);

		Group grpNextBolusDosage = new Group(shlHomeScreen, SWT.NONE);
		grpNextBolusDosage.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		grpNextBolusDosage.setFont(SWTResourceManager.getFont("Calibri", 12, SWT.BOLD));
		grpNextBolusDosage.setText("Next Bolus Dosage");
		grpNextBolusDosage.setBounds(10, 133, 441, 61);

		lblMealName = new Label(grpNextBolusDosage, SWT.NONE);
		lblMealName.setText("Next Meal in :- ");
		lblMealName.setFont(SWTResourceManager.getFont("Calibri", 12, SWT.NORMAL));
		lblMealName.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		lblMealName.setBounds(10, 25, 101, 20);

		Button btnAdjustCarb = new Button(grpNextBolusDosage, SWT.NONE);
		btnAdjustCarb.addSelectionListener(new SelectionAdapter() {
			// .setVisible(true)@Override
			public void widgetSelected(SelectionEvent e) {
				Display display = Display.getDefault();
				createContents();
				CARBRemainderPage crp = new CARBRemainderPage();
				crp.open(false);
				while (null != crp.shlCarbAndRemainder && !crp.shlCarbAndRemainder.isDisposed()) {
					if (!display.readAndDispatch()) {
						display.sleep();
					}
				}
			}
		});
		btnAdjustCarb.setBounds(238, 24, 86, 22);
		btnAdjustCarb.setText("Adjust CARB");

		lblMealTime = new Label(grpNextBolusDosage, SWT.NONE);
		lblMealTime.setFont(SWTResourceManager.getFont("Segoe UI", 10, SWT.BOLD | SWT.ITALIC));
		lblMealTime.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		lblMealTime.setBounds(117, 26, 101, 20);

		Button buttonInjBolus = new Button(grpNextBolusDosage, SWT.NONE);
		buttonInjBolus.setFont(SWTResourceManager.getFont("Segoe UI", 8, SWT.BOLD));
		buttonInjBolus.setText("Inject Bolus");
		buttonInjBolus.setBounds(330, 24, 101, 21);

		Group grpActivityLog = new Group(shlHomeScreen, SWT.NONE);
		grpActivityLog.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		grpActivityLog.setFont(SWTResourceManager.getFont("Calibri", 12, SWT.BOLD));
		grpActivityLog.setText("Activity Log");
		grpActivityLog.setBounds(10, 200, 441, 271);

		txtActivityLog = new StyledText(grpActivityLog, SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
		txtActivityLog.setFont(SWTResourceManager.getFont("Segoe UI", 11, SWT.NORMAL));
		txtActivityLog.setBounds(10, 21, 421, 240);
		Button btnHelp = new Button(shlHomeScreen, SWT.NONE);
		btnHelp.setBounds(467, 446, 81, 25);
		btnHelp.setText("Help");

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
		btnSettings.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Display display = Display.getDefault();
				createContents();
				settings.open(dbMgr);
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
		lblClock.setForeground(SWTResourceManager.getColor(SWT.COLOR_DARK_RED));
		lblClock.setFont(SWTResourceManager.getFont("Calibri", 12, SWT.BOLD));
		lblClock.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		lblClock.setBounds(456, 19, 152, 25);
		lblClock.setText(new SimpleDateFormat("HH:mm:ss").format(new Date()));
		new Thread() {
			public void run() {
				while (true) {
					try {
						Display display = Display.getDefault();
						Thread.sleep(1000);
						display.asyncExec(new Runnable() {
							@Override
							public void run() {
								lblClock.setText(new SimpleDateFormat("d MMM yyyy, HH:mm:ss ").format(new Date()));
							}
						});
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}.start();

		Label lblLogo = new Label(shlHomeScreen, SWT.NONE);
		lblLogo.setImage(SWTResourceManager.getImage(HomeScreen.class, "/resources/Uni_Logo.gif"));
		lblLogo.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		lblLogo.setBounds(614, 0, 112, 50);

		Group group = new Group(shlHomeScreen, SWT.NONE);

		Browser browser = new Browser(shlHomeScreen, SWT.NONE);
		browser.setBounds(467, 439, 64, 32);

		Group grpStatus = new Group(shlHomeScreen, SWT.NONE);
		grpStatus.setBounds(10, 0, 441, 74);
		grpStatus.setFont(SWTResourceManager.getFont("Segoe UI", 13, SWT.BOLD));
		grpStatus.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));

		lblStatusIndicator = new Label(grpStatus, SWT.NONE);
		lblStatusIndicator.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		lblStatusIndicator.setImage(SWTResourceManager.getImage(HomeScreen.class, Constants.STATUS_OK_IMG));
		lblStatusIndicator.setBounds(10, 22, 38, 42);

		lblStatusMessage = new Label(grpStatus, SWT.NONE);
		lblStatusMessage.setBounds(54, 35, 377, 29);
		lblStatusMessage.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		lblStatusMessage.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.BOLD));
		lblStatusMessage.setText("Application Started!");

		Label lblLatestSugarLevels = new Label(shlHomeScreen, SWT.NONE);
		lblLatestSugarLevels.setBounds(22, 92, 123, 23);
		lblLatestSugarLevels.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		lblLatestSugarLevels.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.BOLD | SWT.ITALIC));
		lblLatestSugarLevels.setText("Sugar Level Indicator");

		Label lblNewLabel = new Label(shlHomeScreen, SWT.NONE);
		lblNewLabel.setBounds(262, 115, 169, 17);
		lblNewLabel.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		lblNewLabel.setText("Expected Value 70 - 120 mg/dl");

		progressBarSugarLevel = new ProgressBar(shlHomeScreen, SWT.NONE);
		progressBarSugarLevel.setMaximum(300);
		progressBarSugarLevel.setMinimum(40);
		progressBarSugarLevel.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		progressBarSugarLevel.setBounds(166, 92, 274, 17);

	}

}
