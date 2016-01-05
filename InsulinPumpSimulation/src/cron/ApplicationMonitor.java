package cron;

import java.text.DecimalFormat;
import java.util.Map;

import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import dba.DBManager;
import entities.AppSettings;
import entities.PreConditionsRecord;
import entities.SugarLevelRecord;
import gui.HomeScreen;
import util.Constants;
import util.Utility;

@EnableAsync
@EnableScheduling
public class ApplicationMonitor {
	// Global variables
	public static volatile int latestBolusUnits = 0;
	public static volatile long latestBolusTime = 0;
	private int basalCounter = 0;

	// Injected from Spring
	private DBManager dbMgr;
	private HomeScreen appHomeScreen;
	private AppSettings settings;

	@Scheduled(initialDelay = 2000, fixedRate = 5000)
	public void startMonitorThread() {
		Constants.CURRENT_CYCLE_STATUS = "";
		Map<Integer, Integer> pcs = dbMgr.getPreconditions();
		settings = dbMgr.getAppSettings();
		int sugarLevel = dbMgr.getSugarLevel();
		appHomeScreen.setSugarLevel(sugarLevel);
		appHomeScreen.setActivityLog(dbMgr.getActivities());
		// check Preconditions
		PreConditionsRecord pcr = new PreConditionsRecord();
		appHomeScreen.setPreConditions(pcs, pcr);
		appHomeScreen.setMealTime(settings);
		PreConditionsRecord pc = (PreConditionsRecord) dbMgr.save(pcr);

		/*
		 * if(pcr.getCurrentStatus()){
		 * appHomeScreen.setStatus(Constants.ICON_OK_IMG,
		 * "Application works fine!"); }
		 */
		SugarLevelRecord record = new SugarLevelRecord();
		// inject Basal once in every 4 cycles
		if (basalCounter == 2) {
			basalCounter = 0;
			injectBasal(record, pc.getCurrentStatus());
		}
		basalCounter++;
		record.setSugarLevel(sugarLevel);
		injectCorrection(sugarLevel, record);
		dbMgr.save(record);
	}

	/**
	 * Gets the DBManager injected from Spring Context
	 * 
	 * @return
	 */
	public DBManager getDbMgr() {
		return dbMgr;
	}

	public void setDbMgr(DBManager dbMgr) {
		this.dbMgr = dbMgr;
	}

	public HomeScreen getAppHomeScreen() {
		return appHomeScreen;
	}

	public void setAppHomeScreen(HomeScreen appHomeScreen) {
		this.appHomeScreen = appHomeScreen;
	}

	/**
	 * 
	 * @param sugarLevel
	 * @param slr
	 */
	public void injectCorrection(int sugarLevel, SugarLevelRecord slr){
		DecimalFormat twoDForm = new DecimalFormat("#.##");
		if(sugarLevel > 120){
			float diff = sugarLevel - 100;
			Double bolus = Double.valueOf(twoDForm.format(diff/(1800/settings.getTdd())));
			dbMgr.setActivity("Injected Correction BOLUS of " + bolus + "mg/dl", Constants.ACTIVITY_STATUS_OK);
			appHomeScreen.setStatus(Constants.ICON_OK_IMG, "Injected Correction BOLUS of " + bolus + "mg/dl");
			slr.setBolusInjectedInjected(slr.getBolusInjectedInjected() + bolus);
		}else if(sugarLevel < 70){
			float diff = 100 - sugarLevel;
			Double glucagon  = Double.valueOf(twoDForm.format(diff/15));
			dbMgr.setActivity("Injected Correction Glucagon of " + glucagon + "gms", Constants.ACTIVITY_STATUS_OK);
			appHomeScreen.setStatus(Constants.ICON_OK_IMG, "Injected Correction BOLUS of " + glucagon + "gms");
			slr.setGlucagonInjected(slr.getGlucagonInjected() + glucagon);
		}
	}
	
	
/*	private void mealRemainder() {
		if (Constants.IS_MEAL_POSTPONED && (System.currentTimeMillis() - Constants.MEAL_POSTPONED_TIME)
				/ (1000 * 60) > Constants.MEAL_REMAINDER_INTERVAL) {
		}

	}*/

	/**
	 * 
	 * @param settings
	 */
	public void injectBasal(SugarLevelRecord sl, boolean pcStatus) {
		System.out.println("Precondition status =====> " + pcStatus);
		if ((settings.getBasal() > 0) && (pcStatus == true)) {
			DecimalFormat df = new DecimalFormat("####0.00");
			Double basalUnit = Double.valueOf(df.format((double) settings.getBasal() / 48.00));
			dbMgr.setActivity(basalUnit + " md/dl of BASAL INJECTED.", Constants.ACTIVITY_STATUS_OK);
			appHomeScreen.setStatus(Constants.ICON_OK_IMG, basalUnit + " md/dl of BASAL INJECTED ");
			sl.setBasalInjectedInjected(basalUnit);
		} else {
			System.out.println("Precondition status =====> " + settings.getBasal());
			dbMgr.setActivity("BASAL INJECTION FAILED. Some pre-condition test failed!",
					Constants.ACTIVITY_STATUS_ERROR);
			appHomeScreen.setStatus(Constants.ICON_ERROR_IMG,
					"BASAL INJECTION FAILED. Some pre-condition test failed!");
		}
	}

}
