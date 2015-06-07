/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.db.hibernate;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.openmrs.Cohort;
import org.openmrs.api.db.CohortDAO;
import org.openmrs.api.db.DAOException;

/**
 * Hibernate implementation of the CohortDAO
 *
 * @see CohortDAO
 * @see org.openmrs.api.context.Context
 * @see org.openmrs.api.CohortService
 */
public class HibernateCohortDAO implements CohortDAO {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	private SessionFactory sessionFactory;
	
	/**
	 * Auto generated method comment
	 *
	 * @param sessionFactory
	 */
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	/**
	 * @see org.openmrs.api.db.CohortDAO#getCohort(java.lang.Integer)
	 */
	public Cohort getCohort(Integer id) throws DAOException {
		return (Cohort) sessionFactory.getCurrentSession().get(Cohort.class, id);
	}
	
	/**
	 * @see org.openmrs.api.db.CohortDAO#getCohortsContainingPatientId(java.lang.Integer)
	 */
	@SuppressWarnings("unchecked")
	public List<Cohort> getCohortsContainingPatientId(Integer patientId) throws DAOException {
		Query query = sessionFactory.getCurrentSession().createQuery(
		    "from Cohort c where :patientId in elements(c.memberIds) and c.voided = false order by name");
		query.setInteger("patientId", patientId);
		return (List<Cohort>) query.list();
	}
	
	/**
	 * @see org.openmrs.api.db.CohortDAO#getCohortByUuid(java.lang.String)
	 */
	public Cohort getCohortByUuid(String uuid) {
		return (Cohort) sessionFactory.getCurrentSession().createQuery("from Cohort c where c.uuid = :uuid").setString(
		    "uuid", uuid).uniqueResult();
	}
	
	/**
	 * @see org.openmrs.api.db.CohortDAO#deleteCohort(org.openmrs.Cohort)
	 */
	public Cohort deleteCohort(Cohort cohort) throws DAOException {
		sessionFactory.getCurrentSession().delete(cohort);
		return null;
	}
	
	/**
	 * @see org.openmrs.api.db.CohortDAO#getCohorts(java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	public List<Cohort> getCohorts(String nameFragment) throws DAOException {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Cohort.class);
		criteria.add(Restrictions.ilike("name", nameFragment, MatchMode.ANYWHERE));
		criteria.addOrder(Order.asc("name"));
		return criteria.list();
	}
	
	/**
	 * @see org.openmrs.api.db.CohortDAO#getAllCohorts(boolean)
	 */
	@SuppressWarnings("unchecked")
	public List<Cohort> getAllCohorts(boolean includeVoided) throws DAOException {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Cohort.class);
		
		criteria.addOrder(Order.asc("name"));
		
		if (!includeVoided) {
			criteria.add(Restrictions.eq("voided", false));
		}
		
		return (List<Cohort>) criteria.list();
	}
	
	/**
	 * @see org.openmrs.api.db.CohortDAO#getCohort(java.lang.String)
	 */
	public Cohort getCohort(String name) {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Cohort.class);
		
		criteria.add(Restrictions.eq("name", name));
		criteria.add(Restrictions.eq("voided", false));
		
		return (Cohort) criteria.uniqueResult();
	}
	
	/**
	 * @see org.openmrs.api.db.CohortDAO#saveCohort(org.openmrs.Cohort)
	 */
	public Cohort saveCohort(Cohort cohort) throws DAOException {
		sessionFactory.getCurrentSession().saveOrUpdate(cohort);
		return cohort;
	}
	
}
