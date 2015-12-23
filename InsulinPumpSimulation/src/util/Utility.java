package util;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;

public class Utility {

	/**
	 * Get time difference in HH:MM format
	 * 
	 * @param t1
	 * @param t2
	 * @return
	 */
	public static String getTimeDifference(String t1, String t2) {
		String retVal = "";
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
			retVal = Long.toString(diffHours) + ":" + Long.toString(diffMinutes);

		} catch (Exception e) {

		}

		return retVal;
	}

	/**
	 * Method to raise Alarm or beep
	 * 
	 * @throws Exception
	 */
	public static void makeNoise(String wavPath) {
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
