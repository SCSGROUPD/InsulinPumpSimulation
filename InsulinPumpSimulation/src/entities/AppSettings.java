package entities;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class AppSettings {
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private int id;
	
	private Date dob;
	
	private boolean isManualInterventionRequired;
	
	private String patientId;
	
	private boolean isBolusConfirmed;
	
	private int weight;
	
	private int basal;
	
	private int bolus;
	
	private int breakfastCalories;
	
	private int lunchcalories;
	
	private int dinnerCalories;
	
	private String breakfastTime;
	
	private String lunchTime;
	
	private String dinnerTime;
	
	private String password;
	
	private int tdd;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Date getDob() {
		return dob;
	}

	public void setDob(Date dob) {
		this.dob = dob;
	}

	public String getPatientId() {
		return patientId;
	}

	public void setPatientId(String patientId) {
		this.patientId = patientId;
	}

	public boolean isBolusConfirmed() {
		return isBolusConfirmed;
	}

	public void setBolusConfirmed(boolean isBolusConfirmed) {
		this.isBolusConfirmed = isBolusConfirmed;
	}

	public int getWeight() {
		return weight;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}

	public int getBasal() {
		return basal;
	}

	public void setBasal(int basal) {
		this.basal = basal;
	}

	public int getBolus() {
		return bolus;
	}

	public void setBolus(int bolus) {
		this.bolus = bolus;
	}

	public int getBreakfastCalories() {
		if(breakfastCalories ==0){
			breakfastCalories = 400;
		}
		return breakfastCalories;
	}

	public void setBreakfastCalories(int breakfastCalories) {
		this.breakfastCalories = breakfastCalories;
	}

	public int getLunchcalories() {
		if(lunchcalories ==0){
			lunchcalories = 450;
		}
		return lunchcalories;
	}

	public void setLunchcalories(int lunchcalories) {
		this.lunchcalories = lunchcalories;
	}

	public int getDinnerCalories() {
		if(dinnerCalories ==0){
			dinnerCalories = 350;
		}
		return dinnerCalories;
	}

	public void setDinnerCalories(int dinnerCalories) {
		this.dinnerCalories = dinnerCalories;
	}

	public String getBreakfastTime() {
		if(breakfastTime == null){
			breakfastTime = "7:30";
		}
		return breakfastTime;
	}

	public void setBreakfastTime(String breakfastTime) {
		this.breakfastTime = breakfastTime;
	}

	public String getLunchTime() {
		if(lunchTime == null){
			lunchTime = "12:15";
		}
		return lunchTime;
	}

	public void setLunchTime(String lunchTime) {
		this.lunchTime = lunchTime;
	}

	public String getDinnerTime() {
		if(dinnerTime == null){
			dinnerTime = "19:30";
		}
		return dinnerTime;
	}

	public void setDinnerTime(String dinnerTime) {
		this.dinnerTime = dinnerTime;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public int getTdd() {
		return tdd;
	}

	public void setTdd(int tdd) {
		this.tdd = tdd;
	}

	public boolean isManualInterventionRequired() {
		return isManualInterventionRequired;
	}

	public void setManualInterventionRequired(boolean isManualInterventionRequired) {
		this.isManualInterventionRequired = isManualInterventionRequired;
	}
	

}
