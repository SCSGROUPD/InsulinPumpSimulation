package cron;

import java.util.Map;

import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import dba.DBManager;
import entities.AppSettings;
import entities.SugarLevelRecord;
import gui.HomeScreen;

@EnableAsync
@EnableScheduling
public class ApplicationMonitor {
	// Global variables
	public static volatile boolean preConditionStatus= false;
	public static volatile int latestBolusUnits =0;
	public static volatile long latestBolusTime=0;
	
	// Injected from Spring
	private DBManager dbMgr;
	private HomeScreen appHomeScreen;

    
	@Scheduled(initialDelay=5000, fixedRate=5000)
    public void startMonitorThread() {
    	Map<Integer, Integer> pcs = dbMgr.getPreconditions();
    	AppSettings settings = dbMgr.getAppSettings();
    	int sugarLevel = dbMgr.getSugarLevel();
    	appHomeScreen.setSugarLevel(sugarLevel);
    	SugarLevelRecord record = new SugarLevelRecord();
    	record.setSugarLevel(sugarLevel);
    	dbMgr.save(record);
    	appHomeScreen.setActivityLog(dbMgr.getActivities());
    	appHomeScreen.setPreConditions(pcs);
    	appHomeScreen.setMealTime(settings);
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
	private void calculateBasal(AppSettings settings){
		
		if(ApplicationMonitor.preConditionStatus && settings.getBasal() != 0){
			int basalUnit = settings.getBasal()/48;
		}
	}
    
}
