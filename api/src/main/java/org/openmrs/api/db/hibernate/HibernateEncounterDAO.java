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

import javax.persistence.CacheRetrieveMode;
import javax.persistence.CacheStoreMode;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import liquibase.pro.packaged.Q;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.FlushMode;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.openmrs.Cohort;
import org.openmrs.Encounter;
import org.openmrs.EncounterProvider;
import org.openmrs.EncounterRole;
import org.openmrs.EncounterType;
import org.openmrs.Form;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.PersonName;
import org.openmrs.Provider;
import org.openmrs.Visit;
import org.openmrs.VisitType;
import org.openmrs.api.EncounterService;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.DAOException;
import org.openmrs.api.db.EncounterDAO;
import org.openmrs.parameter.EncounterSearchCriteria;

/**
 * Hibernate specific dao for the {@link EncounterService} All calls should be made on the
 * Context.getEncounterService() object
 *
 * @see EncounterDAO
 * @see EncounterService
 */
public class HibernateEncounterDAO implements EncounterDAO {

	/**
	 * Hibernate session factory
	 */
	private SessionFactory sessionFactory;
	
	/**
	 * Set session factory
	 *
	 * @param sessionFactory
	 */
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	/**
	 * @see org.openmrs.api.db.EncounterDAO#saveEncounter(org.openmrs.Encounter)
	 */
	@Override
	public Encounter saveEncounter(Encounter encounter) throws DAOException {
		sessionFactory.getCurrentSession().saveOrUpdate(encounter);
		return encounter;
	}
	
	/**
	 * @see org.openmrs.api.EncounterService#deleteEncounter(org.openmrs.Encounter)
	 */
	@Override
	public void deleteEncounter(Encounter encounter) throws DAOException {
		sessionFactory.getCurrentSession().delete(encounter);
	}
	
	/**
	 * @see org.openmrs.api.EncounterService#getEncounter(java.lang.Integer)
	 */
	@Override
	public Encounter getEncounter(Integer encounterId) throws DAOException {
		return sessionFactory.getCurrentSession().get(Encounter.class, encounterId);
	}

	/**
	 * @see org.openmrs.api.db.EncounterDAO#getEncountersByPatientId(java.lang.Integer)
	 */
	@Override
	public List<Encounter> getEncountersByPatientId(Integer patientId) throws DAOException {
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<Encounter> cq = cb.createQuery(Encounter.class);
		Root<Encounter> encounterRoot = cq.from(Encounter.class);
		
		Join<Encounter, Patient> patientJoin = encounterRoot.join("patient");

		cq.select(encounterRoot).where(
			cb.equal(patientJoin.get("patientId"), patientId), 
			cb.isFalse(encounterRoot.get("voided"))
		).orderBy(cb.desc(encounterRoot.get("encounterDatetime")));
		
		return session.createQuery(cq).getResultList();
	}

	/**
	 * @see org.openmrs.api.db.EncounterDAO#getEncounters(org.openmrs.parameter.EncounterSearchCriteria)
	 */
	@Override
	public List<Encounter> getEncounters(EncounterSearchCriteria searchCriteria) {
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<Encounter> cq = cb.createQuery(Encounter.class);
		Root<Encounter> encounter = cq.from(Encounter.class);

		List<Predicate> predicates = new ArrayList<>();

		if (searchCriteria.getPatient() != null && searchCriteria.getPatient().getPatientId() != null) {
			predicates.add(cb.equal(encounter.get("patient"), searchCriteria.getPatient()));
		}
		if (searchCriteria.getLocation() != null && searchCriteria.getLocation().getLocationId() != null) {
			predicates.add(cb.equal(encounter.get("location"), searchCriteria.getLocation()));
		}
		if (searchCriteria.getFromDate() != null) {
			predicates.add(cb.greaterThanOrEqualTo(encounter.get("encounterDatetime"), searchCriteria.getFromDate()));
		}
		if (searchCriteria.getToDate() != null) {
			predicates.add(cb.lessThanOrEqualTo(encounter.get("encounterDatetime"), searchCriteria.getToDate()));
		}
		if (searchCriteria.getDateChanged() != null) {
			predicates.add(cb.or(
				cb.and(
					cb.isNull(encounter.get("dateChanged")),
					cb.greaterThanOrEqualTo(encounter.get("dateCreated"), searchCriteria.getDateChanged())
				),
				cb.greaterThanOrEqualTo(encounter.get("dateChanged"), searchCriteria.getDateChanged())
			));
		}
		if (searchCriteria.getEnteredViaForms() != null && !searchCriteria.getEnteredViaForms().isEmpty()) {
			predicates.add(encounter.get("form").in(searchCriteria.getEnteredViaForms()));
		}
		if (searchCriteria.getEncounterTypes() != null && !searchCriteria.getEncounterTypes().isEmpty()) {
			predicates.add(encounter.get("encounterType").in(searchCriteria.getEncounterTypes()));
		}
		if (searchCriteria.getProviders() != null && !searchCriteria.getProviders().isEmpty()) {
			Join<Encounter, EncounterProvider> encounterProvider = encounter.join("encounterProviders");
			predicates.add(encounterProvider.get("provider").in(searchCriteria.getProviders()));
		}
		if (searchCriteria.getVisitTypes() != null && !searchCriteria.getVisitTypes().isEmpty()) {
			Join<Encounter, Visit> visit = encounter.join("visit");
			predicates.add(visit.get("visitType").in(searchCriteria.getVisitTypes()));
		}
		if (searchCriteria.getVisits() != null && !searchCriteria.getVisits().isEmpty()) {
			predicates.add(encounter.get("visit").in(searchCriteria.getVisits()));
		}
		if (!searchCriteria.getIncludeVoided()) {
			predicates.add(cb.isFalse(encounter.get("voided")));
		}

		cq.select(encounter).where(predicates.toArray(new Predicate[]{}))
			.orderBy(cb.asc(encounter.get("encounterDatetime")));

		return session.createQuery(cq).getResultList();
	}
	
	/**
	 * @see org.openmrs.api.db.EncounterDAO#saveEncounterType(org.openmrs.EncounterType)
	 */
	@Override
	public EncounterType saveEncounterType(EncounterType encounterType) {
		sessionFactory.getCurrentSession().saveOrUpdate(encounterType);
		return encounterType;
	}
	
	/**
	 * @see org.openmrs.api.db.EncounterDAO#deleteEncounterType(org.openmrs.EncounterType)
	 */
	@Override
	public void deleteEncounterType(EncounterType encounterType) throws DAOException {
		sessionFactory.getCurrentSession().delete(encounterType);
	}
	
	/**
	 * @see org.openmrs.api.EncounterService#getEncounterType(java.lang.Integer)
	 */
	@Override
	public EncounterType getEncounterType(Integer encounterTypeId) throws DAOException {
		return sessionFactory.getCurrentSession().get(EncounterType.class, encounterTypeId);
	}

	/**
	 * @see org.openmrs.api.EncounterService#getEncounterType(java.lang.String)
	 */
	@Override
	public EncounterType getEncounterType(String name) throws DAOException {
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<EncounterType> cq = cb.createQuery(EncounterType.class);
		Root<EncounterType> root = cq.from(EncounterType.class);
		
		cq.where(cb.isFalse(root.get("retired")), cb.equal(root.get("name"), name));

		return session.createQuery(cq).uniqueResult();
	}

	/**
	 * @see org.openmrs.api.db.EncounterDAO#getAllEncounterTypes(java.lang.Boolean)
	 */
	@Override
	public List<EncounterType> getAllEncounterTypes(Boolean includeRetired) throws DAOException {
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<EncounterType> cq = cb.createQuery(EncounterType.class);
		Root<EncounterType> root = cq.from(EncounterType.class);
		
		cq.orderBy(cb.asc(root.get("name")));

		if (!includeRetired) {
			cq.where(cb.isFalse(root.get("retired")));
		}

		return session.createQuery(cq).getResultList();
	}

	/**
	 * @see org.openmrs.api.db.EncounterDAO#findEncounterTypes(java.lang.String)
	 */
	@Override
	public List<EncounterType> findEncounterTypes(String name) throws DAOException {
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<EncounterType> cq = cb.createQuery(EncounterType.class);
		Root<EncounterType> root = cq.from(EncounterType.class);

		// Case-insensitive 'like' predicate
		Predicate namePredicate = cb.like(cb.lower(root.get("name")), MatchMode.START.toLowerCasePattern(name));

		cq.where(namePredicate).orderBy(cb.asc(root.get("name")), cb.asc(root.get("retired")));

		return session.createQuery(cq).getResultList();
	}
	
	/**
	 * @see org.openmrs.api.db.EncounterDAO#getSavedEncounterDatetime(org.openmrs.Encounter)
	 */
	@Override
	public Date getSavedEncounterDatetime(Encounter encounter) {
		//Usages of this method currently are internal and don't require a flush
		//Otherwise we end up with premature flushes of Immutable types like Obs
		//that are associated to the encounter before we void and replace them
		Session session = sessionFactory.getCurrentSession();
		FlushMode flushMode = session.getHibernateFlushMode();
		session.setHibernateFlushMode(FlushMode.MANUAL);
		try {
			SQLQuery sql = session
			        .createSQLQuery("select encounter_datetime from encounter where encounter_id = :encounterId");
			sql.setInteger("encounterId", encounter.getEncounterId());
			return (Date) sql.uniqueResult();
		}
		finally {
			session.setHibernateFlushMode(flushMode);
		}
	}
	
	/**
	 * @see org.openmrs.api.db.EncounterDAO#getEncounterByUuid(java.lang.String)
	 */
	@Override
	public Encounter getEncounterByUuid(String uuid) {
		return getClassByUuid(Encounter.class, uuid);
	}
	
	/**
	 * @see org.openmrs.api.db.EncounterDAO#getEncounterTypeByUuid(java.lang.String)
	 */
	@Override
	public EncounterType getEncounterTypeByUuid(String uuid) {
		return getClassByUuid(EncounterType.class, uuid);
	}
	
	/**
	 * @see org.openmrs.api.db.EncounterDAO#getEncounters(String, Integer, Integer, Integer,
	 *      boolean)
	 */
	@Override
	public List<Encounter> getEncounters(String query, Integer patientId, Integer start, Integer length,
	        boolean includeVoided) {
		if (StringUtils.isBlank(query) && patientId == null) {
			return Collections.emptyList();
		}

		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<Encounter> cq = cb.createQuery(Encounter.class);
		Root<Encounter> root = cq.from(Encounter.class);

		QueryResult queryResult = createEncounterByQueryPredicates(cb, root, query, patientId, includeVoided, true);
		
		cq.where(queryResult.getPredicates().toArray(new Predicate[]{}))
			.orderBy(queryResult.getOrders());

		TypedQuery<Encounter> typedQuery = session.createQuery(cq);
		if (start != null) {
			typedQuery.setFirstResult(start);
		}
		if (length != null && length > 0) {
			typedQuery.setMaxResults(length);
		}
		
		return typedQuery.getResultList().stream().distinct().collect(Collectors.toList());
	}
	
	/**
	 * @see org.openmrs.api.db.EncounterDAO#getSavedEncounterLocation(org.openmrs.Encounter)
	 */
	@Override
	public Location getSavedEncounterLocation(Encounter encounter) {
		Session session = sessionFactory.getCurrentSession();
		FlushMode flushMode = session.getHibernateFlushMode();
		session.setHibernateFlushMode(FlushMode.MANUAL);
		try {
			SQLQuery sql = session.createSQLQuery("select location_id from encounter where encounter_id = :encounterId");
			sql.setInteger("encounterId", encounter.getEncounterId());
			return Context.getLocationService().getLocation((Integer) sql.uniqueResult());
		}
		finally {
			session.setHibernateFlushMode(flushMode);
		}
	}
	
	/**
	 * @see EncounterDAO#getAllEncounters(org.openmrs.Cohort)
	 */
	@Override
	public Map<Integer, List<Encounter>> getAllEncounters(Cohort patients) {
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<Encounter> cq = cb.createQuery(Encounter.class);
		Root<Encounter> root = cq.from(Encounter.class);

		List<Predicate> predicates = createEncounterPredicates(cb, root, patients);
		cq.where(predicates.toArray(new Predicate[]{}));

		cq.orderBy(
			cb.desc(root.get("patient").get("personId")), 
			cb.desc(root.get("encounterDatetime"))
		);

		TypedQuery<Encounter> query = session.createQuery(cq);
		query.setHint("javax.persistence.cache.retrieveMode", CacheRetrieveMode.BYPASS);
		query.setHint("javax.persistence.cache.storeMode", CacheStoreMode.BYPASS);

		Map<Integer, List<Encounter>> encountersBypatient = new HashMap<>();
		List<Encounter> allEncounters = query.getResultList();
		for (Encounter encounter : allEncounters) {
			Integer patientId = encounter.getPatient().getPersonId();
			List<Encounter> encounters = encountersBypatient.get(patientId);

			if (encounters == null) {
				encounters = new ArrayList<>();
			}

			encounters.add(encounter);
			if (!encountersBypatient.containsKey(patientId)) {
				encountersBypatient.put(patientId, encounters);
			}
		}
		return encountersBypatient;
	}


	/**
	 * Create the criteria for fetching all encounters based on cohort
	 *
	 * @param patients
	 * @return a map of patient with their encounters
	 */
	private List<Predicate> createEncounterPredicates(CriteriaBuilder cb, Root<Encounter> root, Cohort patients) {
		List<Predicate> predicates = new ArrayList<>();
		predicates.add(cb.isFalse(root.get("voided")));

		// only include this where clause if patients were passed in
		if (patients != null) {
			ArrayList<Integer> patientIds = new ArrayList<>();
			patients.getMemberships().forEach(m -> patientIds.add(m.getPatientId()));
			predicates.add(root.get("patient").get("personId").in(patientIds));
		}

		return predicates;
	}

	/**
	 * @see org.openmrs.api.db.EncounterDAO#getCountOfEncounters(java.lang.String,
	 *      java.lang.Integer, boolean)
	 */
	@Override
	public Long getCountOfEncounters(String query, Integer patientId, boolean includeVoided) {
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<Long> cq = cb.createQuery(Long.class);
		Root<Encounter> root = cq.from(Encounter.class);

		QueryResult queryResult = createEncounterByQueryPredicates(cb, root, query, patientId, includeVoided, false);
		cq.select(cb.countDistinct(root.get("encounterId")))
			.where(queryResult.getPredicates().toArray(new Predicate[]{}));

		return session.createQuery(cq).getSingleResult();
	}
	
	/**
	 * Utility method that returns a criteria for searching for patient encounters that match the
	 * specified search phrase
	 *
	 * @param query patient name or identifier
	 * @param patientId the patient id
	 * @param includeVoided Specifies whether voided encounters should be included
	 * @param orderByNames specifies whether the encounters should be ordered by person names
	 * @return List<Predicate>
	 */
	private QueryResult createEncounterByQueryPredicates(CriteriaBuilder cb, Root<Encounter> encounterRoot, String query,
	        Integer patientId, boolean includeVoided, boolean orderByNames) {
		List<Predicate> predicates = new ArrayList<>();

		if (!includeVoided) {
			predicates.add(cb.isFalse(encounterRoot.get("voided")));
		}

		Join<Encounter, Patient> patientJoin = encounterRoot.join("patient");
		if (patientId != null) {
			predicates.add(cb.equal(patientJoin.get("patientId"), patientId));

			if (StringUtils.isNotBlank(query)) {
				Join<Encounter, Location> locationJoin = encounterRoot.join("location");
				Join<Encounter, EncounterType> encounterTypeJoin = encounterRoot.join("encounterType");
				Join<Encounter, Form> formJoin = encounterRoot.join("form");
				Join<Encounter, EncounterProvider> encounterProviderJoin = encounterRoot.join("encounterProviders");
				Join<EncounterProvider, Provider> providerJoin = encounterProviderJoin.join("provider");
				Join<Provider, Person> personJoin = providerJoin.join("person", JoinType.LEFT);
				Join<Person, PersonName> personNameJoin = personJoin.join("names", JoinType.LEFT);

				String queryMatchingPattern = MatchMode.ANYWHERE.toLowerCasePattern(query);
				Predicate locationNamePredicate = cb.like(cb.lower(locationJoin.get("name")), queryMatchingPattern);
				Predicate encounterTypeNamePredicate = cb.like(cb.lower(encounterTypeJoin.get("name")), queryMatchingPattern);
				Predicate formNamePredicate = cb.like(cb.lower(formJoin.get("name")), queryMatchingPattern);
				Predicate providerNamePredicate = cb.like(cb.lower(providerJoin.get("name")), queryMatchingPattern);
				Predicate providerIdentifierPredicate = cb.like(cb.lower(providerJoin.get("identifier")), queryMatchingPattern);

				List<Predicate> orPredicates = new ArrayList<>();
				orPredicates.add(locationNamePredicate);
				orPredicates.add(encounterTypeNamePredicate);
				orPredicates.add(formNamePredicate);
				orPredicates.add(providerNamePredicate);
				orPredicates.add(providerIdentifierPredicate);

				String[] splitNames = query.split(" ");
				List<Predicate> personNamePredicates = new ArrayList<>();
				for (String splitName : splitNames) {
					String splitNamePattern = MatchMode.ANYWHERE.toLowerCasePattern(splitName);
					personNamePredicates.add(cb.like(cb.lower(personNameJoin.get("givenName")), splitNamePattern));
					personNamePredicates.add(cb.like(cb.lower(personNameJoin.get("middleName")), splitNamePattern));
					personNamePredicates.add(cb.like(cb.lower(personNameJoin.get("familyName")), splitNamePattern));
					personNamePredicates.add(cb.like(cb.lower(personNameJoin.get("familyName2")), splitNamePattern));
				}

				//OUTPUT for provider criteria: 
				//prov.name like '%query%' OR prov.identifier like '%query%'
				//OR ( personName.voided = false 
				//		 AND (  personName.givenName like '%query%' 
				//			OR personName.middleName like '%query%' 
				//			OR personName.familyName like '%query%'
				//			OR personName.familyName2 like '%query%'
				//			)
				//	 )

				Predicate nameOr = cb.or(personNamePredicates.toArray(new Predicate[]{}));

				Predicate notVoided = cb.isFalse(personNameJoin.get("voided"));
				Predicate personNameConjunction = cb.and(notVoided, nameOr);
				orPredicates.add(personNameConjunction);

				predicates.add(cb.or(orPredicates.toArray(new Predicate[]{})));
			}
			return new QueryResult(predicates, Collections.emptyList());
		} else {
			//As identifier could be all alpha, no heuristic here will work in determining intent of user for querying by name versus identifier
			//So search by both!
			QueryResult queryResult = new PatientSearchCriteria(sessionFactory).prepareCriteria(cb, patientJoin, query, query,
				new ArrayList<>(), true, orderByNames, true);
			queryResult.addPredicates(predicates);
			return queryResult;
		}
	}
	
	/**
	 * @see org.openmrs.api.db.EncounterDAO#getEncountersByVisit(Visit, boolean)
	 */
	@Override
	public List<Encounter> getEncountersByVisit(Visit visit, boolean includeVoided) {
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<Encounter> cq = cb.createQuery(Encounter.class);
		Root<Encounter> root = cq.from(Encounter.class);

		List<Predicate> predicates = new ArrayList<>();
		predicates.add(cb.equal(root.get("visit"), visit));

		if (!includeVoided) {
			predicates.add(cb.isFalse(root.get("voided")));
		}

		cq.where(predicates.toArray(new Predicate[]{}))
			.orderBy(cb.asc(root.get("encounterDatetime")));

		return session.createQuery(cq).getResultList();
	}

	/**
	 * @see org.openmrs.api.db.EncounterDAO#saveEncounterRole(EncounterRole encounterRole)
	 */
	@Override
	public EncounterRole saveEncounterRole(EncounterRole encounterRole) throws DAOException {
		sessionFactory.getCurrentSession().saveOrUpdate(encounterRole);
		return encounterRole;
	}
	
	/**
	 * @see org.openmrs.api.db.EncounterDAO#deleteEncounterRole(org.openmrs.EncounterRole)
	 */
	@Override
	public void deleteEncounterRole(EncounterRole encounterRole) throws DAOException {
		sessionFactory.getCurrentSession().delete(encounterRole);
	}
	
	/**
	 * @see org.openmrs.api.db.EncounterDAO#getEncounterRole(Integer)
	 */
	@Override
	public EncounterRole getEncounterRole(Integer encounterRoleId) throws DAOException {
		return sessionFactory.getCurrentSession().get(EncounterRole.class, encounterRoleId);
	}
	
	/**
	 * @see org.openmrs.api.db.EncounterDAO#getEncounterRoleByUuid(String)
	 */
	@Override
	public EncounterRole getEncounterRoleByUuid(String uuid) {
		return getClassByUuid(EncounterRole.class, uuid);
	}
	
	/**
	 * @see org.openmrs.api.db.EncounterDAO#getAllEncounterRoles(boolean)
	 */
	@Override
	public List<EncounterRole> getAllEncounterRoles(boolean includeRetired) {
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<EncounterRole> cq = cb.createQuery(EncounterRole.class);
		Root<EncounterRole> root = cq.from(EncounterRole.class);

		if (!includeRetired) {
			cq.where(cb.equal(root.get("retired"), includeRetired));
		}

		return session.createQuery(cq).getResultList();
	}

	/**
	 * @see org.openmrs.api.db.EncounterDAO#getEncounterRoleByName(String)
	 */
	@Override
	public EncounterRole getEncounterRoleByName(String name) {
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<EncounterRole> cq = cb.createQuery(EncounterRole.class);
		Root<EncounterRole> root = cq.from(EncounterRole.class);

		cq.where(cb.equal(root.get("name"), name));

		return session.createQuery(cq).uniqueResult();
	}

	/**
	 * Convenience method since this DAO fetches several different domain objects by uuid
	 *
	 * @param uuid uuid to fetch
	 * @param table a simple classname (e.g. "Encounter")
	 * @return
	 */
	private <T> T getClassByUuid(Class<T> clazz, String uuid) {
		return HibernateUtil.getUniqueEntityByUUID(sessionFactory, clazz, uuid);
	}
	
	@Override
	public List<Encounter> getEncountersNotAssignedToAnyVisit(Patient patient) {
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<Encounter> cq = cb.createQuery(Encounter.class);
		Root<Encounter> root = cq.from(Encounter.class);

		List<Predicate> predicates = new ArrayList<>();
		predicates.add(cb.equal(root.get("patient"), patient));
		predicates.add(cb.isNull(root.get("visit")));
		predicates.add(cb.isFalse(root.get("voided")));

		cq.where(predicates.toArray(new Predicate[]{}))
			.orderBy(cb.desc(root.get("encounterDatetime")));

		return session.createQuery(cq).setMaxResults(100).getResultList();
	}


	/**
	 * @see org.openmrs.api.db.EncounterDAO#getEncountersByVisitsAndPatient(org.openmrs.Patient,
	 *      boolean, java.lang.String, java.lang.Integer, java.lang.Integer)
	 */
	@Override
	public List<Encounter> getEncountersByVisitsAndPatient(Patient patient, boolean includeVoided, String query,
														   Integer start, Integer length) {
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();

		// Query for Encounters
		CriteriaQuery<Encounter> encounterQuery = cb.createQuery(Encounter.class);
		Root<Encounter> encounterRoot = encounterQuery.from(Encounter.class);
		encounterQuery.where(createEncountersByPatientPredicates(cb, encounterRoot, patient, includeVoided, query)
			.toArray(new Predicate[]{}));
		encounterQuery.orderBy(
			cb.desc(encounterRoot.get("visit").get("startDatetime")),
			cb.desc(encounterRoot.get("visit").get("visitId")),
			cb.desc(encounterRoot.get("encounterDatetime")),
			cb.desc(encounterRoot.get("encounterId")));

		List<Encounter> encounters = session.createQuery(encounterQuery).getResultList();

		// Query for Empty Visits
		CriteriaQuery<Visit> visitQuery = cb.createQuery(Visit.class);
		Root<Visit> visitRoot = visitQuery.from(Visit.class);
		visitQuery.where(createEmptyVisitsByPatientPredicates(cb, visitRoot, patient, includeVoided, query)
			.toArray(new Predicate[]{}));
		visitQuery.orderBy(
			cb.desc(visitRoot.get("startDatetime")),
			cb.desc(visitRoot.get("visitId")));

		List<Visit> emptyVisits = session.createQuery(visitQuery).getResultList();

		if (!emptyVisits.isEmpty()) {
			for (Visit emptyVisit : emptyVisits) {
				Encounter mockEncounter = new Encounter();
				mockEncounter.setVisit(emptyVisit);
				encounters.add(mockEncounter);
			}

			encounters.sort((o1, o2) -> {
				Date o1Date = (o1.getVisit() != null) ? o1.getVisit().getStartDatetime() : o1.getEncounterDatetime();
				Date o2Date = (o2.getVisit() != null) ? o2.getVisit().getStartDatetime() : o2.getEncounterDatetime();
				return o2Date.compareTo(o1Date);
			});
		}

		if (start == null) {
			start = 0;
		}
		if (length == null) {
			length = encounters.size();
		}
		int end = start + length;
		if (end > encounters.size()) {
			end = encounters.size();
		}

		return encounters.subList(start, end);
	}

	/**
	 * @see org.openmrs.api.db.EncounterDAO#getEncountersByVisitsAndPatientCount(org.openmrs.Patient,
	 *      boolean, java.lang.String)
	 */
	@Override
	public Integer getEncountersByVisitsAndPatientCount(Patient patient, boolean includeVoided, String query) {
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<Long> visitQuery = cb.createQuery(Long.class);
		Root<Visit> visitRoot = visitQuery.from(Visit.class);
		
		visitQuery.select(cb.count(visitRoot));

		List<Predicate> visitPredicates = createEmptyVisitsByPatientPredicates(cb, visitRoot, patient, includeVoided, query);
		visitQuery.where(visitPredicates.toArray(new Predicate[]{}));

		Long visitCount = session.createQuery(visitQuery).getSingleResult();

		CriteriaQuery<Long> encounterQuery = cb.createQuery(Long.class);
		Root<Encounter> encounterRoot = encounterQuery.from(Encounter.class);
		encounterQuery.select(cb.count(encounterRoot));

		List<Predicate> encounterPredicates = createEncountersByPatientPredicates(cb, encounterRoot, patient, includeVoided, query);
		encounterQuery.where(encounterPredicates.toArray(new Predicate[]{}));

		Long encounterCount = session.createQuery(encounterQuery).getSingleResult();

		return visitCount.intValue() + encounterCount.intValue();
	}

	private List<Predicate> createEmptyVisitsByPatientPredicates(CriteriaBuilder cb, Root<Visit> root,
	        Patient patient, boolean includeVoided, String query) {
		List<Predicate> predicates = new ArrayList<>();

		predicates.add(cb.equal(root.get("patient"), patient));
		predicates.add(cb.isEmpty(root.get("encounters")));

		if (!includeVoided) {
			predicates.add(cb.isFalse(root.get("voided")));
		}

		if (query != null && !StringUtils.isBlank(query)) {
			Join<Visit, VisitType> visitTypeJoin = root.join("visitType", JoinType.LEFT);
			Join<Visit, Location> locationJoin = root.join("location", JoinType.LEFT);

			predicates.add(
				cb.or(
					cb.like(cb.lower(visitTypeJoin.get("name")), MatchMode.ANYWHERE.toLowerCasePattern(query)),
					cb.like(cb.lower(locationJoin.get("name")), MatchMode.ANYWHERE.toLowerCasePattern(query))
				)
			);
		}

		return predicates;
	}

	private List<Predicate> createEncountersByPatientPredicates(CriteriaBuilder cb, Root<Encounter> root,
			Patient patient, boolean includeVoided, String query) {
		List<Predicate> predicates = new ArrayList<>();

		predicates.add(cb.equal(root.get("patient"), patient));

		Join<Encounter, Visit> visitJoin = root.join("visit", JoinType.LEFT);

		if (!includeVoided) {
			predicates.add(cb.equal(root.get("voided"), false));
		}

		if (query != null && !StringUtils.isBlank(query)) {
			Join<Visit, VisitType> visitTypeJoin = visitJoin.join("visitType", JoinType.LEFT);
			Join<Visit, Location> visitLocationJoin = visitJoin.join("location", JoinType.LEFT);
			Join<Encounter, Location> locationJoin = root.join("location", JoinType.LEFT);
			Join<Encounter, EncounterType> encounterTypeJoin = root.join("encounterType", JoinType.LEFT);
			
			String likePattern = MatchMode.ANYWHERE.toLowerCasePattern(query);
			predicates.add(
				cb.or(
					cb.like(cb.lower(visitTypeJoin.get("name")), likePattern),
					cb.like(cb.lower(visitLocationJoin.get("name")), likePattern),
					cb.like(cb.lower(locationJoin.get("name")), likePattern),
					cb.like(cb.lower(encounterTypeJoin.get("name")), likePattern)
				)
			);
		}

		return predicates;
	}

	/**
	 * @see org.openmrs.api.db.EncounterDAO#getEncounterRolesByName(String)
	 */
	@Override
	public List<EncounterRole> getEncounterRolesByName(String name) {
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<EncounterRole> cq = cb.createQuery(EncounterRole.class);
		Root<EncounterRole> root = cq.from(EncounterRole.class);

		cq.where(cb.equal(root.get("name"), name));

		return session.createQuery(cq).getResultList();
	}
}
