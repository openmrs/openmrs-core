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

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
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
		return sessionFactory.getCurrentSession().get(Alert.class, alertId);
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
	public List<Alert> getAllAlerts(boolean includeExpired) throws DAOException {
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<Alert> cq = cb.createQuery(Alert.class);
		Root<Alert> root = cq.from(Alert.class);

		// exclude the expired alerts unless requested
		if (!includeExpired) {
			cq.where(cb.or(
				cb.isNull(root.get("dateToExpire")), 
				cb.greaterThan(root.get("dateToExpire"), new Date()))
			);
		}

		return session.createQuery(cq).getResultList();
	}

	/**
	 * @see org.openmrs.notification.db.AlertDAO#getAlerts(org.openmrs.User, boolean, boolean)
	 */
	@Override
	public List<Alert> getAlerts(User user, boolean includeRead, boolean includeExpired) throws DAOException {
		log.debug("Getting alerts for user " + user + " read? " + includeRead + " expired? " + includeExpired);

		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<Alert> cq = cb.createQuery(Alert.class);
		Root<Alert> root = cq.from(Alert.class);

		List<Predicate> predicates = new ArrayList<>();

		if (user != null && user.getUserId() != null) {
			predicates.add(cb.equal(root.join("recipients").get("recipientId"), user.getUserId()));
		} else {
			// getting here means we passed in no user or a blank user.
			// a null recipient column means get stuff for the anonymous user

			// returning an empty list for now because the above throws an error.
			// we may need to remodel how recipients are handled to get anonymous users alerts
			return Collections.emptyList();
		}

		// exclude the expired alerts unless requested
		if (!includeExpired) {
			Predicate dateToExpireIsNull = cb.isNull(root.get("dateToExpire"));
			Predicate dateToExpireIsGreater = cb.greaterThan(root.get("dateToExpire"), new Date());
			predicates.add(cb.or(dateToExpireIsNull, dateToExpireIsGreater));
		}

		// exclude the read alerts unless requested
		if (!includeRead && user.getUserId() != null) {
			predicates.add(cb.isFalse(root.get("alertRead")));
			predicates.add(cb.isFalse(root.join("recipients").get("alertRead")));
		}

		cq.where(predicates.toArray(new Predicate[]{}))
			.orderBy(cb.desc(root.get("dateChanged")));

		return session.createQuery(cq).getResultList();
	}

}
