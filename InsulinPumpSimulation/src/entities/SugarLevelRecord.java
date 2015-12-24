package entities;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.NamedQueries;
import org.hibernate.annotations.NamedQuery;

@Entity
@NamedQueries({
		@NamedQuery(name = "SQL_GET_GRAPH_DATA", 
				query = "SELECT avg(x.sugarLevel) as sugarLevel, sum(x.bolusInjectedInjected) as bolus,"
						+ "sum(x.basalInjectedInjected) as basal, sum(x.glucagonInjected) as glucogan, x.recordedTime as  day FROM "
						+ "SugarLevelRecord x where x.recordedTime "
						+ "between ? and ? GROUP BY DATE_FORMAT(x.recordedTime, '%d/%m/%Y') order by day") })
public class SugarLevelRecord {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;

	private int sugarLevel;
	
	private double bolusInjectedInjected;
	
	private double basalInjectedInjected;
	
	private double glucagonInjected;


	public double getBolusInjectedInjected() {
		return bolusInjectedInjected;
	}

	public void setBolusInjectedInjected(double bolusInjectedInjected) {
		this.bolusInjectedInjected = bolusInjectedInjected;
	}

	public double getBasalInjectedInjected() {
		return basalInjectedInjected;
	}

	public void setBasalInjectedInjected(double basalInjectedInjected) {
		this.basalInjectedInjected = basalInjectedInjected;
	}

	public double getGlucagonInjected() {
		return glucagonInjected;
	}

	public void setGlucagonInjected(double glucagonInjected) {
		this.glucagonInjected = glucagonInjected;
	}

	@Temporal(TemporalType.TIMESTAMP)
	private Date recordedTime = new Date();

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getSugarLevel() {
		return sugarLevel;
	}

	public void setSugarLevel(int sugarLevel) {
		this.sugarLevel = sugarLevel;
	}

	public Date getRecordedTime() {
		return recordedTime;
	}

	public void setRecordedTime(Date recordedTime) {
		this.recordedTime = recordedTime;
	}

}
