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

	@Scheduled(initialDelay = 2000, fixedRate = 4000)
	public void startMonitorThread() {
		basalCounter++;
		// set low glu
		if (Constants.LOW_SUGAR_LEVEL) {
			Constants.CURRENT_CYCLE_STATUS ="Emergency mode activated!"; 
			appHomeScreen.setStatus(Constants.ICON_ERROR_IMG, "");
			Utility.getSugarLevel(5, 0);
			appHomeScreen.setSugarLevel();
			Utility.playAlarm(Constants.SOUND_FAILURE);
			basalCounter++;
			return;
		} else {
			Utility.getSugarLevel(0, 0);
			PreConditionsRecord pcr = new PreConditionsRecord();
			pcr.setCurrentStatus(1);
			Constants.CURRENT_CYCLE_STATUS = "";
			Map<Integer, Integer> pcs = dbMgr.getPreconditions();
			settings = dbMgr.getAppSettings();
			// int sugarLevel = dbMgr.getSugarLevel();
			if(Constants.activities.isEmpty()){
				Constants.activities = dbMgr.getActivities();
			}
			appHomeScreen.setActivityLog(Constants.activities);

			//
			appHomeScreen.setPreConditions(pcs, pcr);
			appHomeScreen.setMealTime(settings);
			PreConditionsRecord pc = (PreConditionsRecord) dbMgr.save(pcr);

			/*if (pc.getCurrentStatus() == 0) {
				isPreconditionStatusUpdated = false;
				appHomeScreen.setStatus(Constants.ICON_SAD_SMILEY_IMG, "OOPS! Faulty Critical Components!");
				return;
			} else if (isPreconditionStatusUpdated) {
				isPreconditionStatusUpdated = true;
				appHomeScreen.setStatus(Constants.ICON_OK_IMG, "Application is healthy!");
			}*/

			SugarLevelRecord record = new SugarLevelRecord();
			// inject Basal once in every 4 cycles
			if (Constants.CURRENT_INSULIN_RESERVOIR < 20) {
				Constants.CURRENT_INSULIN_RESERVOIR = 100;
			}
			if (Constants.CURRENT_GLU_RESERVOIR < 20) {
				Constants.CURRENT_GLU_RESERVOIR = 100;
			}
			
			if (Constants.BLOOD_SUGAR_LEVEL > 160 || Constants.BLOOD_SUGAR_LEVEL < 70) {
				injectCorrection(record);
//				if (pc.getCurrentStatus() != 0 || Constants.BLOOD_SUGAR_LEVEL < 70) {
//				}
			}
			
			injectBasal(record, pc.getCurrentStatus());
			record.setSugarLevel(Constants.BLOOD_SUGAR_LEVEL);
			dbMgr.save(record);
			appHomeScreen.setSugarLevel();
		}
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
	public void injectCorrection(SugarLevelRecord slr) {
		final int sugarLevel = Constants.BLOOD_SUGAR_LEVEL;
		DecimalFormat twoDForm = new DecimalFormat("#.##");
		if (sugarLevel > 120) {
			float diff = sugarLevel - 100;
			Double bolus = Double.valueOf(twoDForm.format(diff / (1800 / settings.getTdd())));
			dbMgr.setActivity("Injected Correction BOLUS of " + bolus + "mg/dl", Constants.ACTIVITY_STATUS_OK);
			appHomeScreen.setStatus(Constants.ICON_INJECTION_IMG, "Injected Correction BOLUS of " + bolus + "mg/dl");
			slr.setBolusInjectedInjected(slr.getBolusInjectedInjected() + bolus);
			Constants.CURRENT_INSULIN_RESERVOIR = Constants.CURRENT_INSULIN_RESERVOIR - 7;
		} else if (sugarLevel < 70) {
			float diff = 100 - sugarLevel;
			Double glucagon = Double.valueOf(twoDForm.format(diff / 15));
			dbMgr.setActivity("Injected Correction Glucagon of " + glucagon + "mg \n@Blood Sugar level of "
					+ Constants.BLOOD_SUGAR_LEVEL, Constants.ACTIVITY_STATUS_OK);
			appHomeScreen.setStatus(Constants.ICON_INJECTION_IMG, "Injected Correction Glucagon of " + glucagon
					+ "mg \n@Blood Sugar level of " + Constants.BLOOD_SUGAR_LEVEL);
			slr.setGlucagonInjected(slr.getGlucagonInjected() + glucagon);
			Constants.CURRENT_GLU_RESERVOIR = Constants.CURRENT_GLU_RESERVOIR - 10;
		}
		Utility.getSugarLevel(1, 100);
		appHomeScreen.setSugarLevel();
	}

	/**
	 * 
	 * @param settings
	 */
	public void injectBasal(SugarLevelRecord sl, int pcStatus) {
		System.out.println("Precondition status =====> " + pcStatus);
		if ((settings.getBasal() > 0) /*&& (pcStatus == 1)*/) {
			DecimalFormat df = new DecimalFormat("####0.00");
			Double basalUnit = Double.valueOf(df.format((double) settings.getBasal() / 48.00));
			dbMgr.setActivity(basalUnit + " mg/dl of BASAL INJECTED.", Constants.ACTIVITY_STATUS_OK);
			appHomeScreen.setStatus(Constants.ICON_INJECTION_IMG, basalUnit + " mg/dl of BASAL INJECTED ");
			sl.setBasalInjectedInjected(basalUnit);
			Constants.CURRENT_INSULIN_RESERVOIR = Constants.CURRENT_INSULIN_RESERVOIR - 4;
		} else {
			System.out.println("Precondition status =====> " + settings.getBasal());
			dbMgr.setActivity("BASAL INJECTION FAILED. Some pre-condition test failed!",
					Constants.ACTIVITY_STATUS_ERROR);
			appHomeScreen.setStatus(Constants.ICON_ERROR_IMG,
					"BASAL INJECTION FAILED. Some pre-condition test failed!");
		}

	}

}
