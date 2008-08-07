/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.api.db.hibernate;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
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
		Query query = sessionFactory.getCurrentSession().createQuery("from Cohort c where :patientId in elements(c.memberIds) order by name");
		query.setInteger("patientId", patientId);
		return (List<Cohort>) query.list();
	}

	/**
     * @see org.openmrs.api.db.CohortDAO#deleteCohort(org.openmrs.Cohort)
     */
    public Cohort deleteCohort(Cohort cohort) throws DAOException {
	    // TODO Auto-generated method stub
	    return null;
    }

	/**
     * @see org.openmrs.api.db.CohortDAO#findCohorts(java.lang.String)
     */
    public List<Cohort> getCohorts(String nameFragment) throws DAOException {
	    // TODO Auto-generated method stub
	    return null;
    }

	/**
     * @see org.openmrs.api.db.CohortDAO#getAllCohorts(boolean)
     */
    @SuppressWarnings("unchecked")
    public List<Cohort> getAllCohorts(boolean includeVoided) throws DAOException {
    	Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Cohort.class);
    	
    	criteria.addOrder(Order.asc("name"));
    	
    	if (!includeVoided)
    		criteria.add(Restrictions.eq("voided", false));
    	
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