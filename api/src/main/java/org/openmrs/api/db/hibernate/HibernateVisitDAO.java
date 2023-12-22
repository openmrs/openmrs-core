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

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.openmrs.Concept;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.VisitAttribute;
import org.openmrs.VisitAttributeType;
import org.openmrs.VisitType;
import org.openmrs.api.db.DAOException;
import org.openmrs.api.db.VisitDAO;
import org.springframework.transaction.annotation.Transactional;

/**
 * Hibernate specific visit related functions This class should not be used directly. All calls
 * should go through the {@link org.openmrs.api.VisitService} methods.
 *
 * @since 1.9
 */
public class HibernateVisitDAO implements VisitDAO {
	
	private SessionFactory sessionFactory;
	
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	private Session getCurrentSession() {
		return sessionFactory.getCurrentSession();
	}
	
	/**
	 * @see org.openmrs.api.db.VisitDAO#getAllVisitTypes()
	 */
	@Override
	@Transactional(readOnly = true)
	public List<VisitType> getAllVisitTypes() {
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<VisitType> cq = cb.createQuery(VisitType.class);
		cq.from(VisitType.class);
		
		return session.createQuery(cq).getResultList();
	}
	
	/**
	 * @see org.openmrs.api.db.VisitDAO#getAllVisitTypes(boolean)
	 */
	@Override
	public List<VisitType> getAllVisitTypes(boolean includeRetired) {
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<VisitType> cq = cb.createQuery(VisitType.class);
		Root<VisitType> root = cq.from(VisitType.class);

		if (!includeRetired) {
			cq.where(cb.equal(root.get("retired"), includeRetired));
		}

		return session.createQuery(cq).getResultList();
	}
	
	/**
	 * @see org.openmrs.api.db.VisitDAO#getVisitType(java.lang.Integer)
	 */
	@Override
	@Transactional(readOnly = true)
	public VisitType getVisitType(Integer visitTypeId) {
		return sessionFactory.getCurrentSession().get(VisitType.class, visitTypeId);
	}
	
	/**
	 * @see org.openmrs.api.db.VisitDAO#getVisitTypeByUuid(java.lang.String)
	 */
	@Override
	@Transactional(readOnly = true)
	public VisitType getVisitTypeByUuid(String uuid) {
		return HibernateUtil.getUniqueEntityByUUID(sessionFactory, VisitType.class, uuid);
	}
	
	/**
	 * @see org.openmrs.api.db.VisitDAO#getVisitTypes(java.lang.String)
	 */
	@Override
	@Transactional(readOnly = true)
	public List<VisitType> getVisitTypes(String fuzzySearchPhrase) {
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<VisitType> cq = cb.createQuery(VisitType.class);
		Root<VisitType> root = cq.from(VisitType.class);
		
		cq.where(cb.like(cb.lower(root.get("name")), MatchMode.ANYWHERE.toLowerCasePattern(fuzzySearchPhrase)));
		cq.orderBy(cb.asc(root.get("name")));
		
		return session.createQuery(cq).getResultList();
	}
	
	/**
	 * @see org.openmrs.api.db.VisitDAO#saveVisitType(org.openmrs.VisitType)
	 */
	@Override
	@Transactional
	public VisitType saveVisitType(VisitType visitType) {
		sessionFactory.getCurrentSession().saveOrUpdate(visitType);
		return visitType;
	}
	
	/**
	 * @see org.openmrs.api.db.VisitDAO#purgeVisitType(org.openmrs.VisitType)
	 */
	@Override
	@Transactional
	public void purgeVisitType(VisitType visitType) {
		sessionFactory.getCurrentSession().delete(visitType);
	}
	
	/**
	 * @see org.openmrs.api.db.VisitDAO#getVisit(java.lang.Integer)
	 */
	@Override
	@Transactional(readOnly = true)
	public Visit getVisit(Integer visitId) throws DAOException {
		return getCurrentSession().get(Visit.class, visitId);
	}
	
	/**
	 * @see org.openmrs.api.db.VisitDAO#getVisitByUuid(java.lang.String)
	 */
	@Override
	@Transactional(readOnly = true)
	public Visit getVisitByUuid(String uuid) throws DAOException {
		return HibernateUtil.getUniqueEntityByUUID(sessionFactory, Visit.class, uuid);
	}
	
	/**
	 * @see org.openmrs.api.db.VisitDAO#saveVisit(org.openmrs.Visit)
	 */
	@Override
	@Transactional
	public Visit saveVisit(Visit visit) throws DAOException {
		getCurrentSession().saveOrUpdate(visit);
		return visit;
	}
	
	/**
	 * @see org.openmrs.api.db.VisitDAO#deleteVisit(org.openmrs.Visit)
	 */
	@Override
	@Transactional
	public void deleteVisit(Visit visit) throws DAOException {
		getCurrentSession().delete(visit);
	}
	
	/**
	 * @see org.openmrs.api.db.VisitDAO#getVisits(java.util.Collection, java.util.Collection,
	 *      java.util.Collection, java.util.Collection, java.util.Date, java.util.Date,
	 *      java.util.Date, java.util.Date, java.util.Map, boolean, boolean)
	 */
	@Override
	@Transactional(readOnly = true)
	public List<Visit> getVisits(Collection<VisitType> visitTypes, Collection<Patient> patients,
	        Collection<Location> locations, Collection<Concept> indications, Date minStartDatetime, Date maxStartDatetime,
	        Date minEndDatetime, Date maxEndDatetime, final Map<VisitAttributeType, String> serializedAttributeValues,
	        boolean includeInactive, boolean includeVoided) {
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<Visit> cq = cb.createQuery(Visit.class);
		Root<Visit> root = cq.from(Visit.class);

		List<Predicate> predicates = new ArrayList<>();

		if (visitTypes != null && !visitTypes.isEmpty()) {
			predicates.add(root.get("visitType").in(visitTypes));
		}
		if (patients != null && !patients.isEmpty()) {
			predicates.add(root.get("patient").in(patients));
		}
		if (locations != null && !locations.isEmpty()) {
			predicates.add(root.get("location").in(locations));
		}
		if (indications != null && !indications.isEmpty()) {
			predicates.add(root.get("indication").in(indications));
		}
		if (minStartDatetime != null) {
			predicates.add(cb.greaterThanOrEqualTo(root.get("startDatetime"), minStartDatetime));
		}
		if (maxStartDatetime != null) {
			predicates.add(cb.lessThanOrEqualTo(root.get("startDatetime"), maxStartDatetime));
		}

		// active visits have null end date, so it doesn't make sense to search against it if include inactive is set to false
		if (!includeInactive) {
			// the user only asked for currently active visits, so stop time needs to be null or after right now
			predicates.add(cb.or(cb.isNull(root.get("stopDatetime")), cb.greaterThan(root.get("stopDatetime"), new Date())));
		} else {
			if (minEndDatetime != null) {
				predicates.add(cb.or(cb.isNull(root.get("stopDatetime")), cb.greaterThanOrEqualTo(root.get("stopDatetime"),
					minEndDatetime)));
			}
			if (maxEndDatetime != null) {
				predicates.add(cb.lessThanOrEqualTo(root.get("stopDatetime"), maxEndDatetime));
			}
		}

		if (!includeVoided) {
			predicates.add(cb.isFalse(root.get("voided")));
		}

		cq.where(predicates.toArray(new Predicate[]{}))
			.orderBy(cb.desc(root.get("startDatetime")), cb.desc(root.get("visitId")));

		List<Visit> visits = session.createQuery(cq).getResultList();

		if (serializedAttributeValues != null) {
			CollectionUtils.filter(visits, new AttributeMatcherPredicate<Visit, VisitAttributeType>(
				serializedAttributeValues));
		}

		return visits;
	}
	
	/**
	 * @see org.openmrs.api.db.VisitDAO#getAllVisitAttributeTypes()
	 */
	@Override
	@Transactional(readOnly = true)
	public List<VisitAttributeType> getAllVisitAttributeTypes() {
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<VisitAttributeType> cq = cb.createQuery(VisitAttributeType.class);
		cq.from(VisitAttributeType.class);
		
		return session.createQuery(cq).getResultList();
	}
	
	/**
	 * @see org.openmrs.api.db.VisitDAO#getVisitAttributeType(java.lang.Integer)
	 */
	@Override
	@Transactional(readOnly = true)
	public VisitAttributeType getVisitAttributeType(Integer id) {
		return getCurrentSession().get(VisitAttributeType.class, id);
	}
	
	/**
	 * @see org.openmrs.api.db.VisitDAO#getVisitAttributeTypeByUuid(java.lang.String)
	 */
	@Override
	@Transactional(readOnly = true)
	public VisitAttributeType getVisitAttributeTypeByUuid(String uuid) {
		return HibernateUtil.getUniqueEntityByUUID(sessionFactory, VisitAttributeType.class, uuid);
	}
	
	/**
	 * @see org.openmrs.api.db.VisitDAO#saveVisitAttributeType(org.openmrs.VisitAttributeType)
	 */
	@Override
	@Transactional
	public VisitAttributeType saveVisitAttributeType(VisitAttributeType visitAttributeType) {
		getCurrentSession().saveOrUpdate(visitAttributeType);
		return visitAttributeType;
	}
	
	/**
	 * @see org.openmrs.api.db.VisitDAO#deleteVisitAttributeType(org.openmrs.VisitAttributeType)
	 */
	@Override
	@Transactional
	public void deleteVisitAttributeType(VisitAttributeType visitAttributeType) {
		getCurrentSession().delete(visitAttributeType);
	}
	
	/**
	 * @see org.openmrs.api.db.VisitDAO#getVisitAttributeByUuid(java.lang.String)
	 */
	@Override
	@Transactional(readOnly = true)
	public VisitAttribute getVisitAttributeByUuid(String uuid) {
		return HibernateUtil.getUniqueEntityByUUID(sessionFactory, VisitAttribute.class, uuid);
	}
	
	/**
	 * @see org.openmrs.api.db.VisitDAO#getNextVisit(Visit, Collection, Date)
	 */
	@Override
	public Visit getNextVisit(Visit previousVisit, Collection<VisitType> visitTypes, Date maximumStartDate) {
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<Visit> cq = cb.createQuery(Visit.class);
		Root<Visit> root = cq.from(Visit.class);

		List<Predicate> predicates = new ArrayList<>();

		predicates.add(cb.isFalse(root.get("voided")));
		predicates.add(cb.greaterThan(root.get("visitId"), (previousVisit != null) ? previousVisit.getVisitId() : 0));
		predicates.add(cb.isNull(root.get("stopDatetime")));

		if (maximumStartDate != null) {
			predicates.add(cb.lessThanOrEqualTo(root.get("startDatetime"), maximumStartDate));
		}

		if (CollectionUtils.isNotEmpty(visitTypes)) {
			predicates.add(root.get("visitType").in(visitTypes));
		}

		cq.where(predicates.toArray(new Predicate[]{}))
			.orderBy(cb.asc(root.get("visitId")));

		return session.createQuery(cq).setMaxResults(1).uniqueResult();
	}
}
