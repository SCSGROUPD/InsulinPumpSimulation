package dba;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.criterion.Order;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.transform.Transformers;

import entities.ActivityLog;
import entities.AppSettings;
import entities.PreConditions;
import entities.SugarSensor;
import util.Constants;
import util.GraphData;

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
	 * Get all the data for graph
	 * @param fromDate
	 * @param toDate
	 * @return
	 */
	public List<GraphData> getGraphData(String fromDate, String toDate) {
		// restructure date
		fromDate = fromDate + " 00:00:00";
		toDate = toDate + " 23:59:59";
		List<GraphData> result = null;
		try {
			DateFormat inputDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
			// SQL Query
/*			String queryString = "SELECT avg(x.sugarLevel) as sugarLevel, x.recordedTime as  day FROM "
					+ "insulinsimulation.sugarlevelrecord x where x.recordedTime "
					+ "between ? and ? GROUP BY DATE_FORMAT(x.recordedTime, '%d/%m/%Y')  order by day;";
							Query query = session.createSQLQuery("queryString").addScalar("sugarLevel", new DoubleType())
					.addScalar("day", new DateType())
					.setResultTransformer(Transformers.aliasToBean(GraphData.class));*/

			Session session = this.sessionFactory.openSession();
			Query query = session.getNamedQuery("SQL_GET_GRAPH_DATA");
			query.setResultTransformer(Transformers.aliasToBean(GraphData.class));
			
			query.setParameter(0, inputDateFormat.parse(fromDate));
			query.setParameter(1, inputDateFormat.parse(toDate));
			result = query.list();
			session.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;

	}

	/**
	 * Method to save activities
	 * 
	 * @param activity
	 */
	public void setActivity(String activity, int testStatus) {
		String date = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss").format(new Date());
		if (testStatus == Constants.ACTIVITY_STATUS_OK) {
			activity = date + "::Success : " + activity;
		} else if (testStatus == Constants.ACTIVITY_STATUS_WARNING) {
			activity = date + "::Warning : " + activity;
		} else {
			activity = date + "::Critical : " + activity;
		}
		ActivityLog al = new ActivityLog();
		al.setActivity(activity + "\n");
		save(al);
	}

	/**
	 * 
	 * @return
	 */
	public List<ActivityLog> getActivities() {
		Session session = this.sessionFactory.openSession();
		Criteria criteria = session.createCriteria(ActivityLog.class);
		criteria.addOrder(Order.desc("created"));
		criteria.setMaxResults(100);
		List<ActivityLog> list = criteria.list();
		session.close();
		return list;
	}

	/**
	 * Set the default Preconditions
	 * 
	 */
	public void setPreconditions() {
		try {
			if (getSugarLevel() == 0) {
				SugarSensor sensor = new SugarSensor();
				sensor.setSugarLevel(100);
				save(sensor);
			}
			for (int i = 0; i < 8; i++) {
				PreConditions pc = new PreConditions();
				pc.setComponent(i + 1);
				pc.setValue(75);
				if (i == 7) {
					pc.setValue(111);
				}
				save(pc);
			}
		} catch (ConstraintViolationException e) {
			return;
			// ignore duplicate exception
		}
	}

	/**
	 * Reads all the current Precondition values
	 * 
	 * @return
	 */
	public Map<Integer, Integer> getPreconditions() {
		Session session = this.sessionFactory.openSession();
		List<PreConditions> retVal = session.createQuery("from PreConditions").list();
		session.close();
		Map<Integer, Integer> retMap = new HashMap<>();
		if (retVal != null && !retVal.isEmpty()) {
			for (PreConditions pc : retVal) {
				retMap.put(pc.getComponent(), pc.getValue());
			}
		}
		return retMap;

	}

	/**
	 * Get settings
	 * 
	 * @return
	 */
	public AppSettings getAppSettings() {
		Session session = this.sessionFactory.openSession();
		List<AppSettings> retVal = session.createQuery("from AppSettings").list();
		session.close();
		if (!retVal.isEmpty() && retVal.size() > 0) {
			return retVal.get(0);
		}
		return null;
	}

	/**
	 * Get settings
	 * 
	 * @return
	 */
	public int getSugarLevel() {
		Session session = this.sessionFactory.openSession();
		List<SugarSensor> retVal = session.createQuery("from SugarSensor").list();
		session.close();
		if (!retVal.isEmpty() && retVal.size() > 0) {
			return retVal.get(0).getSugarLevel();
		}

		return 0;
	}

	/**
	 * Save/Persist a object
	 * 
	 * @param obj
	 */
	public Object save(Object obj) {
		Session session = this.sessionFactory.openSession();
		Transaction tx = session.beginTransaction();
		session.persist(obj);
		tx.commit();
		session.close();
		return obj;
	}

	/**
	 * Save/Persist a object
	 * 
	 * @param obj
	 */
	public void merge(Object obj) {
		Session session = this.sessionFactory.openSession();
		Transaction tx = session.beginTransaction();
		session.merge(obj);
		tx.commit();
		session.close();
	}

	/**
	 * Query all the records and return as list
	 * 
	 * @return
	 */
	public List<?> getAllRecords(String tableName) {
		Session session = this.sessionFactory.openSession();
		List<ActivityLog> retVal = session.createQuery("from " + tableName).list();
		session.close();
		return retVal;
	}
}
