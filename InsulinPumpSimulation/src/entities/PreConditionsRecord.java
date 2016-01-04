package entities;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class PreConditionsRecord {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	private Date createdAt = new Date();
	
	private boolean batteryTestResult;
	
	private boolean insulinReservoirTestResult;

	private boolean glucagonTestResult;
	
	private boolean pumpTestResult;
	
	private boolean alarmTestResult;
	
	private boolean sensorTestResult;
	
	private boolean needleAssemblyTestResult;

	/**
	 * 
	 * @return
	 */
	public boolean getCurrentStatus(){
//		System.out.println("================================================================");
//		System.out.println("batteryTestResult=>"+batteryTestResult);
//		System.out.println("insulinReservoirTestResult=>"+insulinReservoirTestResult);
//		System.out.println("glucagonTestResult=>"+glucagonTestResult);
//		System.out.println("pumpTestResult=>"+pumpTestResult);
//		System.out.println("alarmTestResult=>"+alarmTestResult);
//		System.out.println("sensorTestResult=>"+sensorTestResult);
//		System.out.println("needleAssemblyTestResult=>"+needleAssemblyTestResult);
		
		if(batteryTestResult && insulinReservoirTestResult &&
				glucagonTestResult && pumpTestResult
				&& alarmTestResult && sensorTestResult && needleAssemblyTestResult){
			return true;
		}
		return false;
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public boolean isBatteryTestResult() {
		return batteryTestResult;
	}

	public void setBatteryTestResult(boolean batteryTestResult) {
		this.batteryTestResult = batteryTestResult;
	}

	public boolean isInsulinReservoirTestResult() {
		return insulinReservoirTestResult;
	}

	public void setInsulinReservoirTestResult(boolean insulinReservoirTestResult) {
		this.insulinReservoirTestResult = insulinReservoirTestResult;
	}

	public boolean isGlucagonTestResult() {
		return glucagonTestResult;
	}

	public void setGlucagonTestResult(boolean glucagonTestResult) {
		this.glucagonTestResult = glucagonTestResult;
	}

	public boolean isPumpTestResult() {
		return pumpTestResult;
	}

	public void setPumpTestResult(boolean pumpTestResult) {
		this.pumpTestResult = pumpTestResult;
	}

	public boolean isAlarmTestResult() {
		return alarmTestResult;
	}

	public void setAlarmTestResult(boolean alarmTestResult) {
		this.alarmTestResult = alarmTestResult;
	}

	public boolean isSensorTestResult() {
		return sensorTestResult;
	}

	public void setSensorTestResult(boolean sensorTestResult) {
		this.sensorTestResult = sensorTestResult;
	}

	public boolean isNeedleAssemblyTestResult() {
		return needleAssemblyTestResult;
	}

	public void setNeedleAssemblyTestResult(boolean needleAssemblyTestResult) {
		this.needleAssemblyTestResult = needleAssemblyTestResult;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}
	
}
