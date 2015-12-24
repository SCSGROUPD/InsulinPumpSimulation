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

@EnableAsync
@EnableScheduling
public class ApplicationMonitor {
	// Global variables
	public static volatile int latestBolusUnits =0;
	public static volatile long latestBolusTime=0;
	private int basalCounter=0;
	
	
	// Injected from Spring
	private DBManager dbMgr;
	private HomeScreen appHomeScreen;
	private AppSettings settings;

    
	@Scheduled(initialDelay=2000, fixedRate=5000)
    public void startMonitorThread() {
    	Map<Integer, Integer> pcs = dbMgr.getPreconditions();
    	settings = dbMgr.getAppSettings();
    	int sugarLevel = dbMgr.getSugarLevel();
    	appHomeScreen.setSugarLevel(sugarLevel);
    	appHomeScreen.setActivityLog(dbMgr.getActivities());
    	PreConditionsRecord pcr = new PreConditionsRecord();
    	appHomeScreen.setPreConditions(pcs,pcr);
    	appHomeScreen.setMealTime(settings);
    	if(pcr.getCurrentStatus()){
    		appHomeScreen.setStatus(Constants.ICON_OK_IMG, "Application works fine!");
    	}
    	SugarLevelRecord record = new SugarLevelRecord();
    	// inject Basal once in every 4 cycles
    	if(basalCounter == 3){
    		basalCounter =0;
    		injectBasal(record, pcr);
    	}
    	basalCounter++;
    	record.setSugarLevel(sugarLevel);
    	dbMgr.save(record);
    	dbMgr.save(pcr);
    }

    /**
     * Gets the DBManager injected from Spring Context
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
	 * @param settings
	 */
	public void injectBasal(SugarLevelRecord sl, PreConditionsRecord pcr){
		if(pcr.getCurrentStatus() && settings.getBasal() != 0){
			DecimalFormat df = new DecimalFormat("####0.00");
			Double basalUnit = Double.valueOf(df.format((double)settings.getBasal()/48.00));
			dbMgr.setActivity(basalUnit + " md/dl of BASAL INJECTED.", Constants.ACTIVITY_STATUS_OK);
			appHomeScreen.setStatus(Constants.ICON_OK_IMG, basalUnit + " md/dl of BASAL INJECTED.");
			sl.setBasalInjectedInjected(basalUnit);
		}else{
			dbMgr.setActivity("BASAL INJECTION FAILED. Some pre-condition test failed!", Constants.ACTIVITY_STATUS_ERROR);
			appHomeScreen.setStatus(Constants.ICON_ERROR_IMG, "BASAL INJECTION FAILED. Some pre-condition test failed!");
		}
	}
    
}
