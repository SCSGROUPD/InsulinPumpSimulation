package util;

import java.util.Date;
public class GraphData {
	
	
	private double sugarLevel;
	
	private Date day;

	private double glucogan;
	
	private double insulin;
	
	public double getGlucogan() {
		return glucogan;
	}

	public void setGlucogan(double glucogan) {
		this.glucogan = glucogan;
	}

	public double getInsulin() {
		return insulin;
	}

	public void setInsulin(double insulin) {
		this.insulin = insulin;
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
