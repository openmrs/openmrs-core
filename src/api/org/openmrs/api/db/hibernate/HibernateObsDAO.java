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

import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Property;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Subqueries;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Location;
import org.openmrs.MimeType;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.User;
import org.openmrs.api.db.DAOException;
import org.openmrs.api.db.ObsDAO;
import org.openmrs.util.OpenmrsConstants.PERSON_TYPE;

/**
 * Hibernate specific Observation related functions This class should not be used directly. All
 * calls should go through the {@link org.openmrs.api.ObsService} methods.
 * 
 * @see org.openmrs.api.db.ObsDAO
 * @see org.openmrs.api.ObsService
 */
public class HibernateObsDAO implements ObsDAO {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	protected SessionFactory sessionFactory;
	
	/**
	 * Set session factory that allows us to connect to the database that Hibernate knows about.
	 * 
	 * @param sessionFactory
	 */
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	/**
	 * @see org.openmrs.api.db.ObsService#deleteObs(org.openmrs.Obs)
	 */
	public void deleteObs(Obs obs) throws DAOException {
		sessionFactory.getCurrentSession().delete(obs);
	}
	
	/**
	 * @see org.openmrs.api.db.ObsService#getObs(java.lang.Integer)
	 */
	public Obs getObs(Integer obsId) throws DAOException {
		return (Obs) sessionFactory.getCurrentSession().get(Obs.class, obsId);
	}
	
	/**
	 * @see org.openmrs.api.db.ObsDAO#getMimeType(java.lang.Integer)
	 */
	public MimeType getMimeType(Integer mimeTypeId) throws DAOException {
		return (MimeType) sessionFactory.getCurrentSession().get(MimeType.class, mimeTypeId);
	}
	
	/**
	 * @see org.openmrs.api.db.ObsDAO#getAllMimeTypes(boolean)
	 */
	@SuppressWarnings("unchecked")
	public List<MimeType> getAllMimeTypes(boolean includeRetired) throws DAOException {
		Criteria crit = sessionFactory.getCurrentSession().createCriteria(MimeType.class);
		
		if (includeRetired == false)
			crit.add(Expression.eq("retired", Boolean.FALSE));
		
		return crit.list();
	}
	
	/**
	 * @see org.openmrs.api.db.ObsSDAO#saveMimeType(org.openmrs.MimeType)
	 */
	public MimeType saveMimeType(MimeType mimeType) throws DAOException {
		sessionFactory.getCurrentSession().saveOrUpdate(mimeType);
		return mimeType;
	}
	
	/**
	 * @see org.openmrs.api.db.ObsDAO#deleteMimeType(org.openmrs.MimeType)
	 */
	public void deleteMimeType(MimeType mimeType) throws DAOException {
		sessionFactory.getCurrentSession().delete(mimeType);
	}
	
	/**
	 * @see org.openmrs.api.db.ObsDAO#saveObs(org.openmrs.Obs)
	 */
	public Obs saveObs(Obs obs) throws DAOException {
		if (obs.hasGroupMembers() && obs.getObsId() != null) {
			// hibernate has a problem updating child collections
			// if the parent object was already saved so we do it 
			// explicitly here
			for (Obs member : obs.getGroupMembers())
				if (member.getObsId() == null)
					saveObs(member);
		}
		
		sessionFactory.getCurrentSession().saveOrUpdate(obs);
		
		return obs;
	}
	
	/**
	 * @see org.openmrs.api.db.ObsDAO#getObservations(java.util.List, java.util.List,
	 *      java.util.List, java.util.List, java.util.List, java.util.List, java.lang.String,
	 *      java.lang.Integer, java.lang.Integer, java.util.Date, java.util.Date, boolean)
	 */
	@SuppressWarnings("unchecked")
	public List<Obs> getObservations(List<Person> whom, List<Encounter> encounters, List<Concept> questions,
	                                 List<Concept> answers, List<PERSON_TYPE> personTypes, List<Location> locations,
	                                 List<String> sortList, Integer mostRecentN, Integer obsGroupId, Date fromDate,
	                                 Date toDate, boolean includeVoidedObs) throws DAOException {
		
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Obs.class, "obs");
		
		if (whom.size() > 0)
			criteria.add(Restrictions.in("person", whom));
		
		if (encounters.size() > 0)
			criteria.add(Restrictions.in("encounter", encounters));
		
		if (questions.size() > 0)
			criteria.add(Restrictions.in("concept", questions));
		
		if (answers.size() > 0)
			criteria.add(Restrictions.in("valueCoded", answers));
		
		getCriteriaPersonModifier(criteria, personTypes);
		
		if (locations.size() > 0)
			criteria.add(Restrictions.in("location", locations));
		
		// TODO add an option for each sort item to be asc/desc
		if (sortList.size() > 0) {
			for (String sort : sortList) {
				if (sort != null && !"".equals(sort))
					criteria.addOrder(Order.desc(sort));
			}
		}
		
		if (mostRecentN > 0)
			criteria.setMaxResults(mostRecentN);
		
		if (obsGroupId != null) {
			criteria.createAlias("obsGroup", "og");
			criteria.add(Restrictions.eq("og.obsId", obsGroupId));
		}
		
		if (fromDate != null)
			criteria.add(Restrictions.ge("obsDatetime", fromDate));
		
		if (toDate != null)
			criteria.add(Restrictions.le("obsDatetime", toDate));
		
		if (includeVoidedObs == false)
			criteria.add(Restrictions.eq("voided", false));
		
		return criteria.list();
	}
	
	/**
	 * Convenience method that adds an expression to the given <code>criteria</code> according to
	 * what types of person objects is wanted
	 * 
	 * @param criteria
	 * @param personType
	 * @return the given criteria (for chaining)
	 */
	private Criteria getCriteriaPersonModifier(Criteria criteria, List<PERSON_TYPE> personTypes) {
		if (personTypes.contains(PERSON_TYPE.PATIENT)) {
			DetachedCriteria crit = DetachedCriteria.forClass(Patient.class, "patient").setProjection(
			    Property.forName("patientId"));
			criteria.add(Subqueries.propertyIn("person.personId", crit));
		}
		
		if (personTypes.contains(PERSON_TYPE.USER)) {
			DetachedCriteria crit = DetachedCriteria.forClass(User.class, "user").setProjection(Property.forName("userId"));
			criteria.add(Subqueries.propertyIn("person.personId", crit));
		}
		
		if (personTypes.contains(PERSON_TYPE.PERSON)) {
			// all observations are already on person's.  Limit to non-patient and non-users here?
			//criteria.createAlias("Person", "person");
			//criteria.add(Restrictions.eqProperty("obs.person.personId", "person.personId"));
		}
		
		return criteria;
	}
	
}
