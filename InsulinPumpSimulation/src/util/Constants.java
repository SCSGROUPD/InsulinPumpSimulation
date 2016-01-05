package util;

public class Constants {
	
	public static volatile String CURRENT_CYCLE_STATUS ="";
	
	public static volatile int MEAL_REMAINDER_INTERVAL = 15; // in minutes
	
	// triggered from postpone button @ Home screen
	public static volatile long MEAL_POSTPONED_TIME =0;
	// triggered from postpone button @ Home screen
	public static volatile boolean IS_MEAL_POSTPONED =false;
	
	public static final int BREAKFAST_BOLUS=1;
	
	public static final int LUNCH_BOLUS=2;
	
	public static final int DINNER_BOLUS=3;
	
	public static volatile boolean APP_IN_MANUAL_MODE = true;
	
	public static volatile int CURRENT_BOLUS_SESSION =0;
	
	public static volatile int RECENT_INJECTED_BOLUS =0;
	
	public static volatile long LAST_BOLUS_INJECTED_TIME = 0;
	
	public static final int ACTIVITY_STATUS_OK = 1;
	
	public static final int ACTIVITY_STATUS_WARNING = 2;
	
	public static final int ACTIVITY_STATUS_ERROR = 3;
	
	public static final String SOUND_REMINDER= "../InsulinPumpSimulation/src/resources/reminder.wav";
	
	public static final String SOUND_FAILURE= "../InsulinPumpSimulation/src/resources/preCondFail.wav";
	
	public static final String SOUND_PHONE= "../InsulinPumpSimulation/src/resources/old-phone-ringing.wav";
	
	public static final String ICON_OK_IMG ="/resources/ok-icon.png";
	
	public static final String ICON_WARNING_IMG ="/resources/warning-icon.png";
	
	public static final String ICON_ERROR_IMG ="/resources/notok-icon.png";
	
	public static final String COMPONENT_PUMP ="COMPONENT_PUMP";
	public static final String COMPONENT_GLUCOSE_SENSOR ="GlucoseSensor";
	
	public static final String COMPONENT_NEEDLE_ASSEMBLY ="NeedleAssembly";
	public static final String COMPONENT_ALARM ="Alarm";
	
	/* Precondition data */
	 public static final int BATTERY = 1;
	 public static final int INSULIN_RESERVOIR = 2;
	 public static final int GLUCAGON_RESERVOIR =3;
	 public static final int PUMP = 4;
	 public static final int BLOOD_GLU_SENSOR =5;
	 public static final int NEEDLE_ASSEMBLY =6;
	 public static final int ALARM =7;
	 public static final int CURRENT_SUGAR_LEVEL = 8	;
	

}
