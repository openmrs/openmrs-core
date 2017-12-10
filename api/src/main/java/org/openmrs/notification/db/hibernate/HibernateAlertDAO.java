/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.notification.db.hibernate;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.openmrs.User;
import org.openmrs.api.db.DAOException;
import org.openmrs.notification.Alert;
import org.openmrs.notification.db.AlertDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Hibernate specific implementation of the
 */
public class HibernateAlertDAO implements AlertDAO {
	
	private static final Logger log = LoggerFactory.getLogger(HibernateAlertDAO.class);
	
	/**
	 * Hibernate session factory
	 */
	private SessionFactory sessionFactory;
	
	public HibernateAlertDAO() {
	}
	
	/**
	 * Set session factory
	 *
	 * @param sessionFactory
	 */
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	/**
	 * @see org.openmrs.notification.db.AlertDAO#saveAlert(org.openmrs.notification.Alert)
	 */
	@Override
	public Alert saveAlert(Alert alert) throws DAOException {
		sessionFactory.getCurrentSession().saveOrUpdate(alert);
		return alert;
	}
	
	/**
	 * @see org.openmrs.notification.db.AlertDAO#getAlert(java.lang.Integer)
	 */
	@Override
	public Alert getAlert(Integer alertId) throws DAOException {
		return (Alert) sessionFactory.getCurrentSession().get(Alert.class, alertId);
	}
	
	/**
	 * @see org.openmrs.notification.db.AlertDAO#deleteAlert(org.openmrs.notification.Alert)
	 */
	@Override
	public void deleteAlert(Alert alert) throws DAOException {
		sessionFactory.getCurrentSession().delete(alert);
	}
	
	/**
	 * @see org.openmrs.notification.AlertService#getAllAlerts(boolean)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<Alert> getAllAlerts(boolean includeExpired) throws DAOException {
		Criteria crit = sessionFactory.getCurrentSession().createCriteria(Alert.class);
		
		// exclude the expired alerts unless requested
		if (!includeExpired) {
			crit.add(Restrictions.or(Restrictions.isNull("dateToExpire"), Restrictions.gt("dateToExpire", new Date())));
		}
		
		return crit.list();
	}
	
	/**
	 * @see org.openmrs.notification.db.AlertDAO#getAlerts(org.openmrs.User, boolean, boolean)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<Alert> getAlerts(User user, boolean includeRead, boolean includeExpired) throws DAOException {
		log.debug("Getting alerts for user " + user + " read? " + includeRead + " expired? " + includeExpired);
		
		Criteria crit = sessionFactory.getCurrentSession().createCriteria(Alert.class, "alert");
		
		if (user != null && user.getUserId() != null) {
			crit.createCriteria("recipients", "recipient");
			crit.add(Restrictions.eq("recipient.recipient", user));
		} else {
			// getting here means we passed in no user or a blank user.
			// a null recipient column means get stuff for the anonymous user
			
			// returning an empty list for now because the above throws an error.
			// we may need to remodel how recipients are handled to get anonymous users alerts
			return Collections.emptyList();
		}
		
		// exclude the expired alerts unless requested
		if (!includeExpired) {
			crit.add(Restrictions.or(Restrictions.isNull("dateToExpire"), Restrictions.gt("dateToExpire", new Date())));
		}
		
		// exclude the read alerts unless requested
		if (!includeRead && user.getUserId() != null) {
			crit.add(Restrictions.eq("alertRead", false));
			crit.add(Restrictions.eq("recipient.alertRead", false));
		}
		
		crit.addOrder(Order.desc("dateChanged"));
		
		return crit.list();
	}
	
}
