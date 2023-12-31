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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.FlushMode;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
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
	public List<Obs> getObservations(List<Person> whom, List<Encounter> encounters, List<Concept> questions,
	        List<Concept> answers, List<PERSON_TYPE> personTypes, List<Location> locations, List<String> sortList,
	        Integer mostRecentN, Integer obsGroupId, Date fromDate, Date toDate, boolean includeVoidedObs,
	        String accessionNumber) throws DAOException {
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<Obs> cq = cb.createQuery(Obs.class);
		Root<Obs> root = cq.from(Obs.class);

		List<Predicate> predicates = createGetObservationsCriteria(cb, root, whom, encounters, questions, answers, personTypes, locations,
			obsGroupId, fromDate, toDate, null, includeVoidedObs, accessionNumber);

		cq.where(predicates.toArray(new Predicate[]{}));

		cq.orderBy(createOrderList(cb, root, sortList));

		TypedQuery<Obs> query = session.createQuery(cq);
		
		if (mostRecentN != null && mostRecentN > 0) {
			query.setMaxResults(mostRecentN);
		}
		
		return query.getResultList();
	}
						
	/**
	 * @see org.openmrs.api.db.ObsDAO#getObservationCount(List, List, List, List, List, List, Integer, Date, Date, List, boolean, String)
	 */
	@Override
	public Long getObservationCount(List<Person> whom, List<Encounter> encounters, List<Concept> questions,
	        List<Concept> answers, List<PERSON_TYPE> personTypes, List<Location> locations, Integer obsGroupId,
	        Date fromDate, Date toDate, List<ConceptName> valueCodedNameAnswers, boolean includeVoidedObs,
	        String accessionNumber) throws DAOException {
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<Long> criteriaQuery = cb.createQuery(Long.class);
		Root<Obs> root = criteriaQuery.from(Obs.class);

		criteriaQuery.select(cb.count(root));

		List<Predicate> predicates = createGetObservationsCriteria(cb, root, whom, encounters, questions, answers,
			personTypes, locations, obsGroupId, fromDate, toDate,
			valueCodedNameAnswers, includeVoidedObs, accessionNumber);

		criteriaQuery.where(predicates.toArray(new Predicate[]{}));

		return session.createQuery(criteriaQuery).getSingleResult();
	}
	
	/**
	 * A utility method for creating a criteria based on parameters (which are optional)
	 *
	 * @param cb
	 * @param root
	 * @param whom
	 * @param encounters
	 * @param questions
	 * @param answers
	 * @param personTypes
	 * @param locations
	 * @param obsGroupId
	 * @param fromDate
	 * @param toDate
	 * @param includeVoidedObs
	 * @param accessionNumber
	 * @return a list of predicates that can form part of a query
	 */
	private List<Predicate> createGetObservationsCriteria(CriteriaBuilder cb, Root<Obs> root, List<Person> whom, List<Encounter> encounters, List<Concept> questions,
	        List<Concept> answers, List<PERSON_TYPE> personTypes, List<Location> locations, Integer obsGroupId, Date fromDate, Date toDate, List<ConceptName> valueCodedNameAnswers,
	        boolean includeVoidedObs, String accessionNumber) {
		
		List<Predicate> predicates = new ArrayList<>();

		if (CollectionUtils.isNotEmpty(whom)) {
			predicates.add(root.get("person").in(whom));
		}

		if (CollectionUtils.isNotEmpty(encounters)) {
			predicates.add(root.get("encounter").in(encounters));
		}

		if (CollectionUtils.isNotEmpty(questions)) {
			predicates.add(root.get("concept").in(questions));
		}

		if (CollectionUtils.isNotEmpty(answers)) {
			predicates.add(root.get("valueCoded").in(answers));
		}

		if (CollectionUtils.isNotEmpty(personTypes)) {
			predicates.addAll(getCriteriaPersonModifier(cb, root, personTypes));
		}

		if (CollectionUtils.isNotEmpty(locations)) {
			predicates.add(root.get("location").in(locations));
		}

		if (obsGroupId != null) {
			predicates.add(cb.equal(root.get("obsGroup").get("obsId"), obsGroupId));
		}

		if (fromDate != null) {
			predicates.add(cb.greaterThanOrEqualTo(root.get("obsDatetime"), fromDate));
		}

		if (toDate != null) {
			predicates.add(cb.lessThanOrEqualTo(root.get("obsDatetime"), toDate));
		}

		if (CollectionUtils.isNotEmpty(valueCodedNameAnswers)) {
			predicates.add(root.get("valueCodedName").in(valueCodedNameAnswers));
		}

		if (!includeVoidedObs) {
			predicates.add(cb.isFalse(root.get("voided")));
		}

		if (accessionNumber != null) {
			predicates.add(cb.equal(root.get("accessionNumber"), accessionNumber));
		}

		return predicates;
	}

	private List<Order> createOrderList(CriteriaBuilder cb, Root<Obs> root, List<String> sortList) {
		List<Order> orders = new ArrayList<>();
		if (CollectionUtils.isNotEmpty(sortList)) {
			for (String sort : sortList) {
				if (StringUtils.isNotEmpty(sort)) {
					// Split the sort, the field name shouldn't contain space char, so it's safe
					String[] split = sort.split(" ", 2);
					String fieldName = split[0];

					if (split.length == 2 && "asc".equals(split[1])) {
						/* If asc is specified */
						orders.add(cb.asc(root.get(fieldName)));
					} else {
						/* If the field hasn't got ordering or desc is specified */
						orders.add(cb.desc(root.get(fieldName)));
					}
				}
			}
		}
		return orders;
	}

	/**
	 * Convenience method that adds an expression to a list of predicates according to the types of person objects
	 * that are required.
	 *
	 * @param cb          instance of CriteriaBuilder
	 * @param root        Root entity in the JPA criteria query
	 * @param personTypes list of person types as filters
	 * @return a list of javax.persistence.criteria.Predicate instances.
	 */
	private List<Predicate> getCriteriaPersonModifier(CriteriaBuilder cb, Root<Obs> root, List<PERSON_TYPE> personTypes) {
		List<Predicate> predicates = new ArrayList<>();

		if (personTypes.contains(PERSON_TYPE.PATIENT)) {
			Subquery<Integer> patientSubquery = cb.createQuery().subquery(Integer.class);
			Root<Patient> patientRoot = patientSubquery.from(Patient.class);
			patientSubquery.select(patientRoot.get("patientId"));

			predicates.add(cb.in(root.get("person").get("personId")).value(patientSubquery));
		}

		if (personTypes.contains(PERSON_TYPE.USER)) {
			Subquery<Integer> userSubquery = cb.createQuery().subquery(Integer.class);
			Root<User> userRoot = userSubquery.from(User.class);
			userSubquery.select(userRoot.get("userId"));

			predicates.add(cb.in(root.get("person").get("personId")).value(userSubquery));
		}

		return predicates;
	}
	
	/**
	 * @see org.openmrs.api.db.ObsDAO#getObsByUuid(java.lang.String)
	 */
	@Override
	public Obs getObsByUuid(String uuid) {
		return HibernateUtil.getUniqueEntityByUUID(sessionFactory, Obs.class, uuid);
	}

	/**
	 * @see org.openmrs.api.db.ObsDAO#getRevisionObs(org.openmrs.Obs)
	 */
	@Override
	public Obs getRevisionObs(Obs initialObs) {
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<Obs> cq = cb.createQuery(Obs.class);
		Root<Obs> root = cq.from(Obs.class);

		cq.where(cb.equal(root.get("previousVersion"), initialObs));

		return session.createQuery(cq).uniqueResult();
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
			sql.setParameter("obsId", obs.getObsId());
			return Obs.Status.valueOf((String) sql.uniqueResult());
		}
		finally {
			session.setHibernateFlushMode(flushMode);
		}
	}
	
}
