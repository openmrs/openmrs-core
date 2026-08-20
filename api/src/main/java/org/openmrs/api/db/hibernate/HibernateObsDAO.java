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
import org.openmrs.ObsReferenceRange;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.User;
import org.openmrs.Visit;
import org.openmrs.api.db.DAOException;
import org.openmrs.api.db.ObsDAO;
import org.openmrs.parameter.ObsSearchCriteria;
import org.openmrs.util.OpenmrsConstants.PERSON_TYPE;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * Hibernate specific Observation related functions This class should not be used directly. All
 * calls should go through the {@link org.openmrs.api.ObsService} methods.
 *
 * @see org.openmrs.api.db.ObsDAO
 * @see org.openmrs.api.ObsService
 */
public class HibernateObsDAO implements ObsDAO {

	private static final Logger log = LoggerFactory.getLogger(HibernateObsDAO.class);

	private static final String OBS_DATETIME = "obsDatetime";

	private static final String OBS_ID = "obsId";

	private static final String ASCENDING = "asc";

	private static final String DESCENDING = "desc";

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
		
		return this.getObservations(whom, encounters, questions, answers, personTypes, locations, sortList, null, mostRecentN, obsGroupId, 
				fromDate, toDate, includeVoidedObs, accessionNumber);
	}
	
	/**
	 * @see org.openmrs.api.db.ObsDAO#getObservations(List, List, List, List, List, List, List, List,
	 *      Integer, Integer, Date, Date, boolean, String)
	 */
	@Override
	public List<Obs> getObservations(List<Person> whom, List<Encounter> encounters, List<Concept> questions,
	        List<Concept> answers, List<PERSON_TYPE> personTypes, List<Location> locations, List<String> sortList,
	        List<Visit> visits, Integer mostRecentN, Integer obsGroupId, Date fromDate, Date toDate,
	        boolean includeVoidedObs, String accessionNumber) throws DAOException {
		return this.getObservations(new ObsSearchCriteria(whom, encounters, questions, answers, personTypes, locations,
		        sortList, visits, mostRecentN, obsGroupId, fromDate, toDate, includeVoidedObs, accessionNumber, null, null));
	}

	/**
	 * @see org.openmrs.api.db.ObsDAO#getObservations(ObsSearchCriteria)
	 */
	@Override
	public List<Obs> getObservations(ObsSearchCriteria obsSearchCriteria) throws DAOException {
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<Obs> cq = cb.createQuery(Obs.class);
		Root<Obs> root = cq.from(Obs.class);

		List<Predicate> predicates = createGetObservationsCriteria(cb, root, obsSearchCriteria.getWhom(),
		    obsSearchCriteria.getEncounters(), obsSearchCriteria.getQuestions(), obsSearchCriteria.getAnswers(),
		    obsSearchCriteria.getPersonTypes(), obsSearchCriteria.getLocations(), obsSearchCriteria.getObsGroupId(),
		    obsSearchCriteria.getFromDate(), obsSearchCriteria.getToDate(), null, obsSearchCriteria.getVisits(),
		    obsSearchCriteria.isIncludeVoidedObs(), obsSearchCriteria.getAccessionNumber());

		cq.where(predicates.toArray(new Predicate[]{}));

		// the bounds decide the sort keys as well as the LIMIT/OFFSET, so they are resolved in one place:
		// resolved separately, the two could drift apart and cost paged callers their stable ordering
		ResultBounds bounds = resolveResultBounds(obsSearchCriteria);

		cq.orderBy(createOrderList(cb, root, createSortList(obsSearchCriteria.getSort(), bounds.paged)));

		TypedQuery<Obs> query = session.createQuery(cq);

		if (bounds.firstResult != null) {
			query.setFirstResult(bounds.firstResult);
		}
		if (bounds.maxRows != null) {
			query.setMaxResults(bounds.maxRows);
		}
		
		return query.getResultList();
	}
						
	/**
	 * Builds the list of sort instructions to apply to an observation query.
	 * <p>
	 * The caller's list is copied rather than modified in place, since callers may hand us an immutable
	 * or shared list.
	 * <p>
	 * A paged query also gets a trailing sort on the obs id, so that its ordering is total: rows tied
	 * on the requested sort columns would otherwise come back in whatever order the database happens to
	 * produce, which lets a row appear on two pages, or on none, as a client walks through the pages of
	 * one result set. The tiebreaker follows the direction of the last requested sort key, so that it
	 * reads as a continuation of that key rather than reversing it. Unpaged queries are left with
	 * exactly the ordering they have always had, since callers already depend on how ties happen to
	 * fall today.
	 *
	 * @param requestedSort the sort instructions requested by the caller, may be null or empty
	 * @param paged whether the caller is walking the result set a page at a time
	 * @return sort instructions in the form understood by
	 *         {@link #createOrderList(CriteriaBuilder, Root, List)}
	 */
	private List<String> createSortList(List<String> requestedSort, boolean paged) {
		List<String> sortList = new ArrayList<>();

		if (requestedSort != null) {
			for (String sort : requestedSort) {
				if (StringUtils.isNotBlank(sort)) {
					sortList.add(sort.trim());
				}
			}
		}

		if (sortList.isEmpty()) {
			sortList.add(OBS_DATETIME);
		}

		if (paged) {
			String lastSort = sortList.get(sortList.size() - 1);
			boolean sortedByObsId = sortList.stream().anyMatch(sort -> OBS_ID.equals(getSortField(sort)));

			if (!sortedByObsId) {
				sortList.add(ASCENDING.equals(getSortDirection(lastSort)) ? OBS_ID + " " + ASCENDING : OBS_ID);
			}
		}

		return sortList;
	}

	/**
	 * Works out which rows the database should return, so that only those rows are hydrated instead of
	 * every matching observation being loaded and then discarded.
	 * <p>
	 * A positive mostRecentN predates the paging parameters and is the only bound the older
	 * getObservations() signatures can express, so it wins when both are supplied. Bounds that cannot
	 * bound anything are logged and ignored rather than rejected, to match how a non-positive
	 * mostRecentN has always been treated; since that lets a call return more rows than were asked for,
	 * every ignored value gets a warning.
	 *
	 * @param obsSearchCriteria the criteria carrying the requested bounds
	 * @return the bounds to apply to the query
	 */
	private ResultBounds resolveResultBounds(ObsSearchCriteria obsSearchCriteria) {
		Integer mostRecentN = obsSearchCriteria.getMostRecentN();
		Integer startIndex = obsSearchCriteria.getStartIndex();
		Integer maxResults = obsSearchCriteria.getMaxResults();

		// supplying either paging parameter at all means the caller is walking a result set and needs a
		// total ordering, even on the first page, where the start index is 0 or absent. Only the paging
		// signatures can supply them, so the older ones are unaffected
		boolean paged = startIndex != null || maxResults != null;

		if (mostRecentN != null && mostRecentN > 0) {
			// a startIndex of 0 asks for the same rows the query already starts with, so being ignored
			// costs the caller nothing worth warning about
			if ((startIndex != null && startIndex != 0) || maxResults != null) {
				log.info("mostRecentN is set to {}, so the requested startIndex ({}) and maxResults ({}) will be ignored",
				    mostRecentN, startIndex, maxResults);
			}

			return new ResultBounds(null, mostRecentN, false);
		}

		if (startIndex != null && startIndex < 0) {
			log.warn("A startIndex of {} is not a valid offset, so results will be returned from the first row", startIndex);
		}

		if (maxResults != null && maxResults <= 0) {
			log.warn("A maxResults of {} is not a valid page size, so every matching observation will be returned",
			    maxResults);
		}

		return new ResultBounds(startIndex != null && startIndex > 0 ? startIndex : null,
		        maxResults != null && maxResults > 0 ? maxResults : null, paged);
	}

	/**
	 * @see org.openmrs.api.db.ObsDAO#getObservations(List, List, List, List, List, List, List, List,
	 *      Integer, Integer, Date, Date, boolean, String, Integer, Integer)
	 */
	@Override
	@SuppressWarnings("squid:S107")
	public List<Obs> getObservations(List<Person> whom, List<Encounter> encounters, List<Concept> questions,
	        List<Concept> answers, List<PERSON_TYPE> personTypes, List<Location> locations, List<String> sortList,
	        List<Visit> visits, Integer mostRecentN, Integer obsGroupId, Date fromDate, Date toDate,
	        boolean includeVoidedObs, String accessionNumber, Integer startIndex, Integer maxResults) throws DAOException {
		return this.getObservations(
		    new ObsSearchCriteria(whom, encounters, questions, answers, personTypes, locations, sortList, visits,
		            mostRecentN, obsGroupId, fromDate, toDate, includeVoidedObs, accessionNumber, startIndex, maxResults));
	}

	/**
	 * @see org.openmrs.api.db.ObsDAO#getObservationCount(List, List, List, List, List, List, Integer,
	 *      Date, Date, List, boolean, String)
	 */
	@Override
	public Long getObservationCount(List<Person> whom, List<Encounter> encounters, List<Concept> questions,
	        List<Concept> answers, List<PERSON_TYPE> personTypes, List<Location> locations, Integer obsGroupId,
	        Date fromDate, Date toDate, List<ConceptName> valueCodedNameAnswers, boolean includeVoidedObs,
	        String accessionNumber) throws DAOException {
		
		return this.getObservationCount(whom, encounters, questions, answers, personTypes, locations, obsGroupId, 
				fromDate, toDate, valueCodedNameAnswers, null, includeVoidedObs, accessionNumber);
	}
	
	/**
	 * @see org.openmrs.api.db.ObsDAO#getObservationCount(List, List, List, List, List, List, Integer, Date, Date, List, List, boolean, String)
	 */
	@Override
	public Long getObservationCount(List<Person> whom, List<Encounter> encounters, List<Concept> questions,
	        List<Concept> answers, List<PERSON_TYPE> personTypes, List<Location> locations, Integer obsGroupId,
	        Date fromDate, Date toDate, List<ConceptName> valueCodedNameAnswers, List<Visit> visits, boolean includeVoidedObs,
	        String accessionNumber) throws DAOException {
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<Long> criteriaQuery = cb.createQuery(Long.class);
		Root<Obs> root = criteriaQuery.from(Obs.class);

		criteriaQuery.select(cb.count(root));

		List<Predicate> predicates = createGetObservationsCriteria(cb, root, whom, encounters, questions, answers,
			personTypes, locations, obsGroupId, fromDate, toDate,
			valueCodedNameAnswers, visits, includeVoidedObs, accessionNumber);

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
	        List<Visit> visits, boolean includeVoidedObs, String accessionNumber) {
		
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
		
		if (CollectionUtils.isNotEmpty(visits)) {
			predicates.add(root.get("encounter").get("visit").in(visits));
		}

		if (obsGroupId != null) {
			predicates.add(cb.equal(root.get("obsGroup").get(OBS_ID), obsGroupId));
		}

		if (fromDate != null) {
			predicates.add(cb.greaterThanOrEqualTo(root.get(OBS_DATETIME), fromDate));
		}

		if (toDate != null) {
			predicates.add(cb.lessThanOrEqualTo(root.get(OBS_DATETIME), toDate));
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
				if (StringUtils.isNotBlank(sort)) {
					String fieldName = getSortField(sort);

					if (ASCENDING.equals(getSortDirection(sort))) {
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
	 * Reads the field name out of a sort instruction such as <code>"obsDatetime asc"</code>. A field
	 * name cannot contain a space, so the first token is the whole of it.
	 *
	 * @param sort a non-blank, trimmed sort instruction
	 * @return the name of the field to sort on
	 */
	private static String getSortField(String sort) {
		return sort.split(" ", 2)[0];
	}

	/**
	 * Reads the direction out of a sort instruction such as <code>"obsDatetime asc"</code>.
	 * <p>
	 * An instruction that names no direction, or names one this method does not recognise, has always
	 * meant descending here, and callers depend on that; only exactly <code>"asc"</code> asks for
	 * ascending, so <code>"ASC"</code> and <code>"obsDatetime&nbsp;&nbsp;asc"</code> both sort
	 * descending. A direction that is present but unrecognised is worth a warning, because paging turns
	 * a misread direction into a different set of rows rather than merely a different order.
	 *
	 * @param sort a non-blank, trimmed sort instruction
	 * @return either {@link #ASCENDING} or {@link #DESCENDING}
	 */
	private static String getSortDirection(String sort) {
		String[] split = sort.split(" ", 2);
		if (split.length < 2) {
			return DESCENDING;
		}

		if (ASCENDING.equals(split[1])) {
			return ASCENDING;
		}

		if (!DESCENDING.equals(split[1])) {
			log.warn("'{}' in the sort instruction '{}' is not a recognised direction; sorting descending. Use '{}' or '{}'",
			    split[1], sort, ASCENDING, DESCENDING);
		}

		return DESCENDING;
	}

	/**
	 * Convenience method that adds an expression to a list of predicates according to the types of
	 * person objects that are required.
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

	/**
	 * The row bounds resolved from a set of search criteria: which row to start at, how many rows to
	 * return, and whether that amounts to a page the caller means to walk through. Nothing outside
	 * {@link #resolveResultBounds(ObsSearchCriteria)} interprets the criteria's bounds, so the sort
	 * keys and the LIMIT/OFFSET are always chosen from the same reading of them.
	 */
	private static final class ResultBounds {

		private final Integer firstResult;

		private final Integer maxRows;

		private final boolean paged;

		private ResultBounds(Integer firstResult, Integer maxRows, boolean paged) {
			this.firstResult = firstResult;
			this.maxRows = maxRows;
			this.paged = paged;
		}
	}
}
