package gui;

import java.io.File;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;

public class CARBRemainderPage {

	protected Shell shlCarbAndRemainder;
	private Text textAuthenticationPIN;

	/**
	 * Launch the application.
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			CARBRemainderPage window = new CARBRemainderPage();
			window.open(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	/**
	 * Open the window.
	 */
	public void open(boolean soundRequired) {
		Display display = Display.getDefault();
		createContents();
		shlCarbAndRemainder.open();
		shlCarbAndRemainder.layout();
		
		if(soundRequired){
			makeNoise(); 
		}
			
		
		while (!shlCarbAndRemainder.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	private void makeNoise() {
		try {
	    	File soundFile = new File("../InsulinPumpSimulation/src/resource/old-phone-ringing.wav");
			AudioInputStream sound = AudioSystem.getAudioInputStream(soundFile);
			DataLine.Info info = new DataLine.Info(Clip.class, sound.getFormat());
			Clip clip = (Clip) AudioSystem.getLine(info);
		    clip.open(sound);

		    // due to bug in Java Sound, explicitly exit the VM when
		    // the sound has stopped.
		    clip.addLineListener(new LineListener() {
				@Override
				public void update(LineEvent event) {
			        if (event.getType() == LineEvent.Type.STOP) {
			          event.getLine().close();
			          System.exit(0);
			        }
				}
			});

		    // play the sound clip
		    clip.start();
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Create contents of the window.
	 */
	protected void createContents() {
		shlCarbAndRemainder = new Shell(Display.getDefault(), SWT.TITLE|SWT.CLOSE|SWT.BORDER);
		shlCarbAndRemainder.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		shlCarbAndRemainder.setSize(323, 262);
		shlCarbAndRemainder.setText("CARB and Remainder");
		
		Group grpCarbSetting = new Group(shlCarbAndRemainder, SWT.NONE);
		grpCarbSetting.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		grpCarbSetting.setFont(SWTResourceManager.getFont("Calibri", 12, SWT.BOLD));
		grpCarbSetting.setText("CARB Setting");
		
		grpCarbSetting.setBounds(10, 0, 295, 106);
		
		Label lblCARBValue = new Label(grpCarbSetting, SWT.NONE);
		lblCARBValue.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		lblCARBValue.setBounds(10, 32, 149, 22);
		lblCARBValue.setText("CARB Value");
		
		Label lblAuthenticationPIN = new Label(grpCarbSetting, SWT.NONE);
		lblAuthenticationPIN.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		lblAuthenticationPIN.setBounds(10, 68, 149, 22);
		lblAuthenticationPIN.setText("Authentication PIN");
		
		Spinner spinnerCARBValue = new Spinner(grpCarbSetting, SWT.BORDER);
		spinnerCARBValue.setBounds(206, 29, 72, 22);
		
		textAuthenticationPIN = new Text(grpCarbSetting, SWT.BORDER|SWT.PASSWORD);
		textAuthenticationPIN.setBounds(206, 65, 72, 21);
				
		Group grpRemainder = new Group(shlCarbAndRemainder, SWT.NONE);
		grpRemainder.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		grpRemainder.setFont(SWTResourceManager.getFont("Calibri", 12, SWT.BOLD));
		grpRemainder.setText("Remainder");
		grpRemainder.setBounds(10, 112, 296, 106);
		
		Button btnInjectBolus = new Button(grpRemainder, SWT.NONE);
		btnInjectBolus.setFont(SWTResourceManager.getFont("Segoe UI", 10, SWT.BOLD));
		btnInjectBolus.setBounds(183, 38, 103, 41);
		btnInjectBolus.setText("INJECT BOLUS");
		
		Button btnCancel = new Button(grpRemainder, SWT.NONE);
		btnCancel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				shlCarbAndRemainder.close();
			}
		});
		btnCancel.setBounds(10, 38, 80, 41);
		btnCancel.setText("Cancel");
		
		Button btnSnooze = new Button(grpRemainder, SWT.NONE);
		btnSnooze.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
			}
		});
		btnSnooze.setBounds(96, 38, 81, 41);
		btnSnooze.setText("Snooze");

	}
}
