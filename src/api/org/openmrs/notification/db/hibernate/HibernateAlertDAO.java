package org.openmrs.notification.db.hibernate;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.Order;
import org.hibernate.sql.JoinFragment;
import org.openmrs.Role;
import org.openmrs.User;
import org.openmrs.api.db.DAOException;
import org.openmrs.api.db.hibernate.HibernateUtil;
import org.openmrs.notification.Alert;
import org.openmrs.notification.db.AlertDAO;

public class HibernateAlertDAO implements AlertDAO {

	private final Log log = LogFactory.getLog(getClass());

	public HibernateAlertDAO() {
	}

	public void createAlert(Alert alert) throws DAOException {
		Session session = HibernateUtil.currentSession();

		try {
			HibernateUtil.beginTransaction();
			session.save(alert);
			HibernateUtil.commitTransaction();
		} catch (Exception e) {
			HibernateUtil.rollbackTransaction();
			throw new DAOException(e);
		}
	}

	public void updateAlert(Alert alert) throws DAOException {
		if (alert.getAlertId() == null)
			createAlert(alert);
		else {
			try {
				Session session = HibernateUtil.currentSession();
				HibernateUtil.beginTransaction();
				session.saveOrUpdate(alert);
				HibernateUtil.commitTransaction();
			} catch (Exception e) {
				HibernateUtil.rollbackTransaction();
				throw new DAOException(e);
			}
		}
	}

	public Alert getAlert(Integer alertId) throws DAOException {
		Session session = HibernateUtil.currentSession();
		Alert alert = (Alert) session.get(Alert.class, alertId);

		return alert;
	}

	public List<Alert> getAlerts(User user, Set<Role> userRoles,
			boolean includeRead, boolean includeExpired) throws DAOException {
		Session session = HibernateUtil.currentSession();
		log.debug("Getting alerts for user " + user + " read? " + includeRead
				+ " expired? " + includeExpired);

		// return list
		List<Alert> alerts = new Vector<Alert>();

		// a null userRoles object would break later code, set to empty list
		if (userRoles == null)
			userRoles = new HashSet<Role>();

		// only find roles users that have roles or an id
		if (user != null && (!userRoles.isEmpty() || user.getUserId() != null)) {

			Criteria crit = session.createCriteria(Alert.class, "alert");
			// We only want one alert displayed even if a user is
			// qualified for it more than once
			crit.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
			if (user != null) {

				if (user.getUserId() == null) {
					if (!userRoles.isEmpty())
						// If the userId is null, only check the roles
						// (Anonymous)
						crit.add(Expression.in("role", userRoles));
				} else {
					if (userRoles.isEmpty())
						// user does not have any roles, only check on userId
						crit.add(Expression.eq("user", user));
					else
						// check both the userId and the user's roles
						crit.add(Expression.or(Expression.eq("user", user),
								Expression.in("role", userRoles)));
				}
			}
			// include the expired alerts if requested
			if (includeExpired == false)
				crit.add(Expression.or(Expression.isNull("dateToExpire"),
						Expression.gt("dateToExpire", new Date())));
			// include the read alerts if requested
			if (includeRead == false && user.getUserId() != null) {
				crit.createCriteria("readByUsers", "readBy",
						JoinFragment.LEFT_OUTER_JOIN);
				crit.add(Expression.or(Expression.isNull("readBy.userId"),
						Expression.ne("readBy.userId", user.getUserId())));
			}
			crit.addOrder(Order.desc("dateChanged"));

			alerts = crit.list();
		}

		return alerts;
	}

	public List<Alert> getAllAlerts(boolean includeExpired) throws DAOException {
		Session session = HibernateUtil.currentSession();

		// return list
		List<Alert> alerts = new Vector<Alert>();

		Criteria crit = session.createCriteria(Alert.class, "alert");
		// We only want one alert displayed even if a user is
		// qualified for it more than once
		crit.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
		// include the expired alerts if requested
		if (includeExpired == false)
			crit.add(Expression.or(Expression.isNull("dateToExpire"),
					Expression.gt("dateToExpire", new Date())));
		crit.addOrder(Order.desc("dateChanged"));

		alerts = crit.list();

		return alerts;
	}
}