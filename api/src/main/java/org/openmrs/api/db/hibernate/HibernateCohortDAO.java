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
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.openmrs.Cohort;
import org.openmrs.CohortMembership;
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
	
	private static final String VOIDED = "voided";
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
	@Override
	public Cohort getCohort(Integer id) throws DAOException {
		return sessionFactory.getCurrentSession().get(Cohort.class, id);
	}

	/**
	 * @see org.openmrs.api.db.CohortDAO#getCohortsContainingPatientId(Integer, boolean, Date)
	 */
	@Override
	public List<Cohort> getCohortsContainingPatientId(Integer patientId, boolean includeVoided,
													  Date asOfDate) throws DAOException {
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<Cohort> cq = cb.createQuery(Cohort.class);
		Root<Cohort> root = cq.from(Cohort.class);

		Join<Cohort, CohortMembership> membershipJoin = root.join("memberships");

		List<Predicate> predicates = new ArrayList<>();

		if (asOfDate != null) {
			predicates.add(cb.lessThanOrEqualTo(membershipJoin.get("startDate"), asOfDate));

			Predicate endDateNullPredicate = cb.isNull(membershipJoin.get("endDate"));
			Predicate endDateGtPredicate = cb.greaterThan(membershipJoin.get("endDate"), asOfDate);
			predicates.add(cb.or(endDateNullPredicate, endDateGtPredicate));
		}
		predicates.add(cb.equal(membershipJoin.get("patientId"), patientId));

		if (!includeVoided) {
			predicates.add(cb.equal(root.get(VOIDED), includeVoided));
		}

		cq.distinct(true).where(predicates.toArray(new Predicate[]{}));

		return session.createQuery(cq).getResultList();
	}
	
	/**
	 * @see org.openmrs.api.db.CohortDAO#getCohortByUuid(java.lang.String)
	 */
	@Override
	public Cohort getCohortByUuid(String uuid) {
		return HibernateUtil.getUniqueEntityByUUID(sessionFactory, Cohort.class, uuid);
	}
	
	/**
	 * @see org.openmrs.api.db.CohortDAO#getCohortMembershipByUuid(java.lang.String)
	 */
	@Override
	public CohortMembership getCohortMembershipByUuid(String uuid) {
		return HibernateUtil.getUniqueEntityByUUID(sessionFactory, CohortMembership.class, uuid);
	}

	/**
	 * @see org.openmrs.api.db.CohortDAO#deleteCohort(org.openmrs.Cohort)
	 */
	@Override
	public Cohort deleteCohort(Cohort cohort) throws DAOException {
		sessionFactory.getCurrentSession().delete(cohort);
		return null;
	}

	/**
	 * @see org.openmrs.api.db.CohortDAO#getCohorts(java.lang.String)
	 */
	@Override
	public List<Cohort> getCohorts(String nameFragment) throws DAOException {
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<Cohort> cq = cb.createQuery(Cohort.class);
		Root<Cohort> root = cq.from(Cohort.class);

		cq.where(cb.like(cb.lower(root.get("name")), 
			MatchMode.ANYWHERE.toLowerCasePattern(nameFragment)));
		cq.orderBy(cb.asc(root.get("name")));

		return session.createQuery(cq).getResultList();
	}

	/**
	 * @see org.openmrs.api.db.CohortDAO#getAllCohorts(boolean)
	 */
	@Override
	public List<Cohort> getAllCohorts(boolean includeVoided) throws DAOException {
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<Cohort> cq = cb.createQuery(Cohort.class);
		Root<Cohort> root = cq.from(Cohort.class);

		if (!includeVoided) {
			cq.where(cb.isFalse(root.get(VOIDED)));
		}

		cq.orderBy(cb.asc(root.get("name")));

		return session.createQuery(cq).getResultList();
	}

	/**
	 * @see org.openmrs.api.db.CohortDAO#getCohort(java.lang.String)
	 */
	@Override
	public Cohort getCohort(String name) {
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<Cohort> cq = cb.createQuery(Cohort.class);
		Root<Cohort> root = cq.from(Cohort.class);

		cq.where(cb.equal(root.get("name"), name), cb.isFalse(root.get(VOIDED)));

		return session.createQuery(cq).uniqueResult();
	}
	
	/**
	 * @see org.openmrs.api.db.CohortDAO#saveCohort(org.openmrs.Cohort)
	 */
	@Override
	public Cohort saveCohort(Cohort cohort) throws DAOException {
		sessionFactory.getCurrentSession().saveOrUpdate(cohort);
		return cohort;
	}

	@Override
	public List<CohortMembership> getCohortMemberships(Integer patientId, Date activeOnDate, boolean includeVoided) {
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<CohortMembership> cq = cb.createQuery(CohortMembership.class);
		Root<CohortMembership> root = cq.from(CohortMembership.class);

		List<Predicate> predicates = new ArrayList<>();

		predicates.add(cb.equal(root.get("patientId"), patientId));

		if (activeOnDate != null) {
			predicates.add(cb.lessThanOrEqualTo(root.get("startDate"), activeOnDate));

			Predicate endDateIsNull = cb.isNull(root.get("endDate"));
			Predicate endDateIsGreater = cb.greaterThanOrEqualTo(root.get("endDate"), activeOnDate);

			predicates.add(cb.or(endDateIsNull, endDateIsGreater));
		}

		if (!includeVoided) {
			predicates.add(cb.isFalse(root.get(VOIDED)));
		}

		cq.where(predicates.toArray(new Predicate[]{}));

		return session.createQuery(cq).getResultList();
	}
	
	@Override
	public CohortMembership saveCohortMembership(CohortMembership cohortMembership) {
		sessionFactory.getCurrentSession().saveOrUpdate(cohortMembership);
		return cohortMembership;
	}
}
