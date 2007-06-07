package org.openmrs.notification.db.hibernate;

import java.util.Date;
import java.util.List;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.Order;
import org.openmrs.User;
import org.openmrs.api.db.DAOException;
import org.openmrs.notification.Alert;
import org.openmrs.notification.db.AlertDAO;

public class HibernateAlertDAO implements AlertDAO {

	private final Log log = LogFactory.getLog(getClass());

	/**
	 * Hibernate session factory
	 */
	private SessionFactory sessionFactory;
	
	public HibernateAlertDAO() { }

	/**
	 * Set session factory
	 * 
	 * @param sessionFactory
	 */
	public void setSessionFactory(SessionFactory sessionFactory) { 
		this.sessionFactory = sessionFactory;
	}

	public void createAlert(Alert alert) throws DAOException {
		sessionFactory.getCurrentSession().save(alert);
	}

	public void updateAlert(Alert alert) throws DAOException {
		if (alert.getAlertId() == null)
			createAlert(alert);
		else
			sessionFactory.getCurrentSession().saveOrUpdate(alert);
	}

	public Alert getAlert(Integer alertId) throws DAOException {
		return (Alert) sessionFactory.getCurrentSession().get(Alert.class, alertId);
	}

	@SuppressWarnings("unchecked")
	public List<Alert> getAlerts(User user, boolean includeRead, 
				boolean includeExpired) throws DAOException {
		log.debug("Getting alerts for user " + user + " read? " + includeRead
				+ " expired? " + includeExpired);

		// return list
		List<Alert> alerts = new Vector<Alert>();

		Criteria crit = sessionFactory.getCurrentSession().createCriteria(Alert.class, "alert");
		crit.createCriteria("recipients", "recipient");
		
		if (user != null && user.getUserId() != null)
			crit.add(Expression.eq("recipient.recipient", user));
		else
			crit.add(Expression.isNull("recipient.recipient"));
		
		// exclude the expired alerts unless requested
		if (includeExpired == false)
			crit.add(Expression.or(Expression.isNull("dateToExpire"),
					Expression.gt("dateToExpire", new Date())));
		
		// exclude the read alerts unless requested
		if (includeRead == false && user.getUserId() != null) {
			crit.add(Expression.eq("alertRead", false));
			crit.add(Expression.eq("recipient.alertRead", false));
		}
		
		crit.addOrder(Order.desc("dateChanged"));

		alerts = crit.list();

		/*	
		if (user != null && user.getUserId() != null) {
			String sql = "select alert from Alert alert where recipient.recipientId = :userId";
			String order = " order by alert.dateChanged desc";
			
			if (includeExpired == false)
				sql = sql + " and (alert.dateToExpire is null or alert.dateToExpire > current_date())";
			
			if (includeRead == false)
				sql = sql + " and alert.alertRead = false and recipient.alertRead = false";
			
			Query query = session.createQuery(sql + order);
			query.setInteger("userId", user.getUserId());
			
			alerts = query.list();
		}
		*/

		return alerts;
	}

	@SuppressWarnings("unchecked")
	public List<Alert> getAllAlerts(boolean includeExpired) throws DAOException {
		// return list
		List<Alert> alerts = new Vector<Alert>();

		Criteria crit = sessionFactory.getCurrentSession().createCriteria(Alert.class, "alert");
		// exclude the expired alerts unless requested
		if (includeExpired == false)
			crit.add(Expression.or(Expression.isNull("dateToExpire"),
					Expression.gt("dateToExpire", new Date())));
		crit.addOrder(Order.desc("dateChanged"));

		alerts = crit.list();

		return alerts;
	}
}