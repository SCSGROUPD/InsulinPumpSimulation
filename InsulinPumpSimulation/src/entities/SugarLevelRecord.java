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
				query = "SELECT avg(x.sugarLevel) as sugarLevel, avg(x.insulinInjected) as insulin,"
						+ "avg(x.glucagonInjected) as glucogan, x.recordedTime as  day FROM "
						+ "SugarLevelRecord x where x.recordedTime "
						+ "between ? and ? GROUP BY DATE_FORMAT(x.recordedTime, '%d/%m/%Y') order by day") })
public class SugarLevelRecord {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;

	private int sugarLevel;
	
	private double insulinInjected = 2;
	
	private double glucagonInjected = 0.25;

	public double getInsulinInjected() {
		return insulinInjected;
	}

	public void setInsulinInjected(double insulinInjected) {
		this.insulinInjected = insulinInjected;
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
