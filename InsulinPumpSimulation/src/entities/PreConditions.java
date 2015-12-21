package entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class PreConditions {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	/**
	 * BATTERY = 1
	 * INSULIN_RESERVOIR = 2
	 * GLUCAGON_RESERVOIR =3
	 * PUMP = 4
	 * BLOOD_GLU_SENSOR =5
	 * NEEDLE_ASSEMBLY =6
	 * ALARM =7
	 * CURRENT_SUGAR_LEVEL = 8
	 * */
	@Column(unique=true)
	private int component;
	
	private int value;
	
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getComponent() {
		return component;
	}

	public void setComponent(int component) {
		this.component = component;
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}


}
