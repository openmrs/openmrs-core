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

import java.util.Date;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.FlushMode;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Property;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Subqueries;
import org.openmrs.Concept;
import org.openmrs.ConceptName;
import org.openmrs.Encounter;
import org.openmrs.Location;
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
	 * @see org.openmrs.api.ObsService#deleteObs(org.openmrs.Obs)
	 */
	@Override
	public void deleteObs(Obs obs) throws DAOException {
		sessionFactory.getCurrentSession().delete(obs);
	}
	
	/**
	 * @see org.openmrs.api.ObsService#getObs(java.lang.Integer)
	 */
	@Override
	public Obs getObs(Integer obsId) throws DAOException {
		return (Obs) sessionFactory.getCurrentSession().get(Obs.class, obsId);
	}
	
	/**
	 * @see org.openmrs.api.db.ObsDAO#saveObs(org.openmrs.Obs)
	 */
	@Override
	public Obs saveObs(Obs obs) throws DAOException {
		if (obs.hasGroupMembers() && obs.getObsId() != null) {
			// hibernate has a problem updating child collections
			// if the parent object was already saved so we do it
			// explicitly here
			for (Obs member : obs.getGroupMembers()) {
				if (member.getObsId() == null) {
					saveObs(member);
				}
			}
		}
		
		sessionFactory.getCurrentSession().saveOrUpdate(obs);
		
		return obs;
	}
	
	/**
	 * @see org.openmrs.api.db.ObsDAO#getObservations(List, List, List, List, List, List, List,
	 *      Integer, Integer, Date, Date, boolean, String)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<Obs> getObservations(List<Person> whom, List<Encounter> encounters, List<Concept> questions,
	        List<Concept> answers, List<PERSON_TYPE> personTypes, List<Location> locations, List<String> sortList,
	        Integer mostRecentN, Integer obsGroupId, Date fromDate, Date toDate, boolean includeVoidedObs,
	        String accessionNumber) throws DAOException {
		
		Criteria criteria = createGetObservationsCriteria(whom, encounters, questions, answers, personTypes, locations,
		    sortList, mostRecentN, obsGroupId, fromDate, toDate, null, includeVoidedObs, accessionNumber);
		
		return criteria.list();
	}
	
	/**
	 * @see org.openmrs.api.db.ObsDAO#getObservationCount(List, List, List, List, List, List, Integer, Date, Date, List, boolean, String)
	 */
	@Override
	public Long getObservationCount(List<Person> whom, List<Encounter> encounters, List<Concept> questions,
	        List<Concept> answers, List<PERSON_TYPE> personTypes, List<Location> locations, Integer obsGroupId,
	        Date fromDate, Date toDate, List<ConceptName> valueCodedNameAnswers, boolean includeVoidedObs,
	        String accessionNumber) throws DAOException {
		Criteria criteria = createGetObservationsCriteria(whom, encounters, questions, answers, personTypes, locations,
		    null, null, obsGroupId, fromDate, toDate, valueCodedNameAnswers, includeVoidedObs, accessionNumber);
		criteria.setProjection(Projections.rowCount());
		return (Long) criteria.list().get(0);
	}
	
	/**
	 * A utility method for creating a criteria based on parameters (which are optional)
	 *
	 * @param whom
	 * @param encounters
	 * @param questions
	 * @param answers
	 * @param personTypes
	 * @param locations
	 * @param sortList If a field needs to be in <i>asc</i> order, <code>" asc"</code> has to be appended to the field name. For example: <code>fieldname asc</code>
	 * @param mostRecentN
	 * @param obsGroupId
	 * @param fromDate
	 * @param toDate
	 * @param includeVoidedObs
	 * @param accessionNumber
	 * @return
	 */
	private Criteria createGetObservationsCriteria(List<Person> whom, List<Encounter> encounters, List<Concept> questions,
	        List<Concept> answers, List<PERSON_TYPE> personTypes, List<Location> locations, List<String> sortList,
	        Integer mostRecentN, Integer obsGroupId, Date fromDate, Date toDate, List<ConceptName> valueCodedNameAnswers,
	        boolean includeVoidedObs, String accessionNumber) {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Obs.class, "obs");
		
		if (CollectionUtils.isNotEmpty(whom)) {
			criteria.add(Restrictions.in("person", whom));
		}
		
		if (CollectionUtils.isNotEmpty(encounters)) {
			criteria.add(Restrictions.in("encounter", encounters));
		}
		
		if (CollectionUtils.isNotEmpty(questions)) {
			criteria.add(Restrictions.in("concept", questions));
		}
		
		if (CollectionUtils.isNotEmpty(answers)) {
			criteria.add(Restrictions.in("valueCoded", answers));
		}
		
		if (CollectionUtils.isNotEmpty(personTypes)) {
			getCriteriaPersonModifier(criteria, personTypes);
		}
		
		if (CollectionUtils.isNotEmpty(locations)) {
			criteria.add(Restrictions.in("location", locations));
		}
		
		if (CollectionUtils.isNotEmpty(sortList)) {
			for (String sort : sortList) {
				if (StringUtils.isNotEmpty(sort)) {
					// Split the sort, the field name shouldn't contain space char, so it's safe
					String[] split = sort.split(" ", 2);
					String fieldName = split[0];
					
					if (split.length == 2 && "asc".equals(split[1])) {
						/* If asc is specified */
						criteria.addOrder(Order.asc(fieldName));
					} else {
						/* If the field hasn't got ordering or desc is specified */
						criteria.addOrder(Order.desc(fieldName));
					}
				}
			}
		}
		
		if (mostRecentN != null && mostRecentN > 0) {
			criteria.setMaxResults(mostRecentN);
		}
		
		if (obsGroupId != null) {
			criteria.createAlias("obsGroup", "og");
			criteria.add(Restrictions.eq("og.obsId", obsGroupId));
		}
		
		if (fromDate != null) {
			criteria.add(Restrictions.ge("obsDatetime", fromDate));
		}
		
		if (toDate != null) {
			criteria.add(Restrictions.le("obsDatetime", toDate));
		}
		
		if (CollectionUtils.isNotEmpty(valueCodedNameAnswers)) {
			criteria.add(Restrictions.in("valueCodedName", valueCodedNameAnswers));
		}
		
		if (!includeVoidedObs) {
			criteria.add(Restrictions.eq("voided", false));
		}
		
		if (accessionNumber != null) {
			criteria.add(Restrictions.eq("accessionNumber", accessionNumber));
		}
		
		return criteria;
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
		
		return criteria;
	}
	
	/**
	 * @see org.openmrs.api.db.ObsDAO#getObsByUuid(java.lang.String)
	 */
	@Override
	public Obs getObsByUuid(String uuid) {
		return (Obs) sessionFactory.getCurrentSession().createQuery("from Obs o where o.uuid = :uuid").setString("uuid",
		    uuid).uniqueResult();
	}

	/**
	 * @see org.openmrs.api.db.ObsDAO#getRevisionObs(org.openmrs.Obs)
	 */
	@Override
	public Obs getRevisionObs(Obs initialObs) {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Obs.class, "obs");
		criteria.add(Restrictions.eq("previousVersion", initialObs));
		return (Obs) criteria.uniqueResult();
	}
	
	/**
	 * @see org.openmrs.api.db.ObsDAO#getSavedStatus(org.openmrs.Obs)
	 */
	@Override
	public Obs.Status getSavedStatus(Obs obs) {
		// avoid premature flushes when this internal method is called from inside a service method
		Session session = sessionFactory.getCurrentSession();
		FlushMode flushMode = session.getHibernateFlushMode();
		session.setHibernateFlushMode(FlushMode.MANUAL);
		try {
			SQLQuery sql = session.createSQLQuery("select status from obs where obs_id = :obsId");
			sql.setInteger("obsId", obs.getObsId());
			return Obs.Status.valueOf((String) sql.uniqueResult());
		}
		finally {
			session.setHibernateFlushMode(flushMode);
		}
	}
	
}
