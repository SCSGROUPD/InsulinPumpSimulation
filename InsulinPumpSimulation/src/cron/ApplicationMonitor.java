package cron;

import java.util.Map;

import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import dba.DBManager;
import gui.HomeScreen;

@EnableAsync
@EnableScheduling
public class ApplicationMonitor {
	
	// Injected from Spring
	private DBManager dbMgr;
	private HomeScreen appHomeScreen;

    
	@Scheduled(initialDelay=5000, fixedRate=5000)
    public void startMonitorThread() {
    	Map<Integer, Integer> pcs = dbMgr.getPreconditions();
    	appHomeScreen.setPreConditions(pcs);
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
    
}
