package util;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;

import entities.AppSettings;

public class Utility {

	public static long getTimeForNextMeal(AppSettings setting, boolean considerMealPostpone) {

		// If meal is postponed
		long postPoneTime = 0;
		if (Constants.IS_MEAL_POSTPONED && considerMealPostpone) {
			// In minutes
			postPoneTime = Constants.MEAL_REMAINDER_INTERVAL
					- (System.currentTimeMillis() - Constants.MEAL_POSTPONED_TIME) / (1000 * 60 * 60);
			// postpone time exceeded
			if(postPoneTime == 0){
				Constants.IS_MEAL_POSTPONED = false;
				postPoneTime =0;
			}
		}

		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
		String str = sdf.format(new Date());

		long currTime = Integer.parseInt(str.split(":")[0]) * 60 + Integer.parseInt(str.split(":")[1]);
/*		long lastBolusTime = 0;
		if (Constants.LAST_BOLUS_INJECTED_TIME != 0) {
			String lastBolus = sdf.format(Constants.LAST_BOLUS_INJECTED_TIME);
			// All time in minutes
			lastBolusTime = currTime
					- (Integer.parseInt(lastBolus.split(":")[0]) * 60 + Integer.parseInt(lastBolus.split(":")[1]));
		}*/

		long breakFastTime = postPoneTime + Integer.parseInt(setting.getBreakfastTime().split(":")[0]) * 60
				+ Integer.parseInt(setting.getBreakfastTime().split(":")[1]);
		long lunchTime = postPoneTime + Integer.parseInt(setting.getLunchTime().split(":")[0]) * 60
				+ Integer.parseInt(setting.getLunchTime().split(":")[1]);
		long dinnerTime = postPoneTime + Integer.parseInt(setting.getDinnerTime().split(":")[0]) * 60
				+ Integer.parseInt(setting.getDinnerTime().split(":")[1]);

		
		// For first time
		if(Constants.RECENT_INJECTED_BOLUS == 0){
			if(currTime <= breakFastTime){
				Constants.RECENT_INJECTED_BOLUS = Constants.DINNER_BOLUS;
			}else if(currTime <= lunchTime){
				Constants.RECENT_INJECTED_BOLUS = Constants.BREAKFAST_BOLUS;
			}else{
				Constants.RECENT_INJECTED_BOLUS = Constants.LUNCH_BOLUS;
			}
		}
		
		long timeDiff = 0;
		Calendar c = new GregorianCalendar();
		c.setTimeInMillis(System.currentTimeMillis());
		
		if (Constants.RECENT_INJECTED_BOLUS == Constants.DINNER_BOLUS
				&& (currTime <= breakFastTime || currTime > dinnerTime)) {
			Constants.CURRENT_BOLUS_SESSION = Constants.BREAKFAST_BOLUS;
			timeDiff = breakFastTime - currTime; // Utility.getTimeDifference(str,

		} else if (Constants.RECENT_INJECTED_BOLUS == Constants.BREAKFAST_BOLUS && currTime > breakFastTime
				&& currTime < dinnerTime) {
			Constants.CURRENT_BOLUS_SESSION = Constants.LUNCH_BOLUS;
			timeDiff = lunchTime - currTime;
		} else if(Constants.RECENT_INJECTED_BOLUS == Constants.LUNCH_BOLUS && currTime > lunchTime){
			Constants.CURRENT_BOLUS_SESSION = Constants.DINNER_BOLUS;
			timeDiff = dinnerTime - currTime;
		}
		

		return timeDiff;

	}

	/**
	 * Get time difference in HH:MM format
	 * 
	 * @param t1
	 * @param t2
	 * @return
	 */
	public static long getTimeDifference(String t1, String t2) {

		long retVal = 0;
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
		Date d1 = null;
		Date d2 = null;
		try {
			d1 = sdf.parse(t1);
			d2 = sdf.parse(t2);
			// in milliseconds
			long diff = Math.abs(d2.getTime() - d1.getTime());

			long diffMinutes = diff / (60 * 1000) % 60;
			long diffHours = diff / (60 * 60 * 1000) % 24;
			retVal = diffHours * 60 + diffMinutes;

		} catch (Exception e) {

		}

		return retVal;
	}

	/**
	 * Method to raise Alarm or beep
	 * 
	 * @throws Exception
	 */
	public static void initiateAlarm(String wavPath) {
		try {
			File soundFile = new File(wavPath);
			AudioInputStream sound = AudioSystem.getAudioInputStream(soundFile);

			DataLine.Info info = new DataLine.Info(Clip.class, sound.getFormat());
			Clip clip = (Clip) AudioSystem.getLine(info);
			clip.open(sound);

			clip.addLineListener(new LineListener() {
				public void update(LineEvent event) {
					if (event.getType() == LineEvent.Type.STOP) {
						event.getLine().close();
					}
				}
			});

			clip.start();
			Thread.sleep(2000);
			if (clip.isOpen()) {
				clip.close();
				sound.close();
			}
		} catch (Exception e) {
		}
	}

}
