package app;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import dba.DBManager;
import entities.PreConditionsRecord;
import gui.HomeScreen;
import util.Constants;

@Configuration
@ComponentScan
@EnableAutoConfiguration
public class Application {

	private HomeScreen appHomeScreen;
	
	public static void main(String[] args) {
		Application app = new Application();
		app.start(args);

	}

	/**
	 * Start of the application.
	 * Application instantiates Spring, Hibernate & 
	 * launches the application
	 * @param args
	 */
	private void start(String[] args) {
		// Instantiate Spring Context
		ApplicationContext context = new ClassPathXmlApplicationContext("resources/Spring-Module.xml");

		Constants.BLOOD_SUGAR_LEVEL = 150;
		
		// Set the default Pr-Condition Values
		DBManager dbMgr = (DBManager) context.getBean("dbMgr");
		dbMgr.setPreconditions();
		
		// Opens the Home Screen
		appHomeScreen = (HomeScreen) context.getBean("app");
		// sets the initial pre-condition values
		PreConditionsRecord pcr = new PreConditionsRecord();
		appHomeScreen.setPreConditions(dbMgr.getPreconditions(), pcr);
		appHomeScreen.setMealTime(dbMgr.getAppSettings());
		dbMgr.save(pcr);
		appHomeScreen.open();
	}

}
