package entities;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class SugarSensor {

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private int id;
	
	private int sugarLevel;

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
}
