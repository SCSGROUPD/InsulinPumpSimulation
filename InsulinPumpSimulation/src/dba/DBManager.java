package dba;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.exception.ConstraintViolationException;

import entities.ActivityLog;
import entities.AppSettings;
import entities.PreConditions;
import gui.SettingsPage;

public class DBManager {

	// Injected from Spring
	private SessionFactory sessionFactory;

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	public SessionFactory getSessionFactory() {
		return this.sessionFactory;
	}

	/**
	 * Set the default Preconditions 
	 
	 */
	public void setPreconditions(){
		try{
			for (int i = 0; i < 8; i++) {
				PreConditions pc = new PreConditions();
				pc.setComponent(i+1);
				pc.setValue(75);
				if(i == 7){
					pc.setValue(111);
				}
				save(pc);
			}
		}catch(ConstraintViolationException e){
			return;
			// ignore duplicate exception
		}
	}
	
	/**
	 * Reads all the current Precondition values
	 * @return
	 */
	public Map<Integer, Integer> getPreconditions(){
		Session session = this.sessionFactory.openSession();
		List<PreConditions> retVal = session.createQuery("from PreConditions").list();
		session.close();
		Map<Integer, Integer> retMap = new HashMap<>();
		if(retVal != null && !retVal.isEmpty()){
			for (PreConditions pc : retVal) {
				retMap.put(pc.getComponent(), pc.getValue());
			}
		}
		return retMap;
		
	}
	
	/**
	 * Get settings
	 * @return
	 */
	public AppSettings getAppSettings(){
		Session session = this.sessionFactory.openSession();
		List<AppSettings> retVal = session.createQuery("from AppSettings").list();
		session.close();
		if(!retVal.isEmpty() && retVal.size()>0){
			return retVal.get(0);
		}
		return null;
	}
	
	/**
	 * Save/Persist a object
	 * @param obj
	 */
	public void save(Object obj) {
		Session session = this.sessionFactory.openSession();
		Transaction tx = session.beginTransaction();
		session.persist(obj);
		tx.commit();
		session.close();
	}

	/**
	 * Query all the records and return as list
	 * @return
	 */
	public List<?> getAllRecords(String tableName) {
		Session session = this.sessionFactory.openSession();
		List<ActivityLog> retVal = session.createQuery("from " +tableName).list();
		session.close();
		return retVal;
	}
}
