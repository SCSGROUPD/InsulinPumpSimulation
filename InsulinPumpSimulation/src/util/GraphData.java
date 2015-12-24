package util;

import java.util.Date;
public class GraphData {
	
	
	private double sugarLevel;
	
	private Date day;

	public double getBasal() {
		return basal;
	}

	public void setBasal(double basal) {
		this.basal = basal;
	}

	public double getBolus() {
		return bolus;
	}

	public void setBolus(double bolus) {
		this.bolus = bolus;
	}

	private double glucogan;
	
	private double basal;
	
	private double bolus;
	
	public double getGlucogan() {
		return glucogan;
	}

	public void setGlucogan(double glucogan) {
		this.glucogan = glucogan;
	}

	public double getSugarLevel() {
		return sugarLevel;
	}

	public void setSugarLevel(double sugarLevel) {
		this.sugarLevel = sugarLevel;
	}

	public Date getDay() {
		return day;
	}

	public void setDay(Date day) {
		this.day = day;
	}

}
