/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.hl7.db.hibernate;

import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.type.StandardBasicTypes;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.DAOException;
import org.openmrs.api.db.hibernate.HibernateUtil;
import org.openmrs.api.db.hibernate.JpaUtils;
import org.openmrs.api.db.hibernate.MatchMode;
import org.openmrs.hl7.HL7Constants;
import org.openmrs.hl7.HL7InArchive;
import org.openmrs.hl7.HL7InError;
import org.openmrs.hl7.HL7InQueue;
import org.openmrs.hl7.HL7Source;
import org.openmrs.hl7.Hl7InArchivesMigrateThread;
import org.openmrs.hl7.db.HL7DAO;

/**
 * OpenMRS HL7 API database default hibernate implementation This class shouldn't be instantiated by
 * itself. Use the {@link org.openmrs.api.context.Context}
 *
 * @see org.openmrs.hl7.HL7Service
 * @see org.openmrs.hl7.db.HL7DAO
 */
public class HibernateHL7DAO implements HL7DAO {

	/**
	 * Hibernate session factory
	 */
	private SessionFactory sessionFactory;
	
	public HibernateHL7DAO() {
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
	 * @see org.openmrs.hl7.db.HL7DAO#saveHL7Source(org.openmrs.hl7.HL7Source)
	 */
	@Override
	public HL7Source saveHL7Source(HL7Source hl7Source) throws DAOException {
		sessionFactory.getCurrentSession().saveOrUpdate(hl7Source);
		return hl7Source;
	}
	
	/**
	 * @see org.openmrs.hl7.db.HL7DAO#getHL7Source(java.lang.Integer)
	 */
	@Override
	public HL7Source getHL7Source(Integer hl7SourceId) throws DAOException {
		return sessionFactory.getCurrentSession().get(HL7Source.class, hl7SourceId);
	}
	
	/**
	 * @see org.openmrs.hl7.db.HL7DAO#getHL7SourceByName(java.lang.String)
	 */
	@Override
	public HL7Source getHL7SourceByName(String name) throws DAOException {
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<HL7Source> cq = cb.createQuery(HL7Source.class);
		Root<HL7Source> root = cq.from(HL7Source.class);

		cq.where(cb.equal(root.get("name"), name));

		return session.createQuery(cq).uniqueResult();
	}

	/**
	 * @see org.openmrs.hl7.db.HL7DAO#getAllHL7Sources()
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<HL7Source> getAllHL7Sources() throws DAOException {
		return sessionFactory.getCurrentSession().createQuery("from HL7Source").list();
	}
	
	/**
	 * @see org.openmrs.hl7.db.HL7DAO#deleteHL7Source(org.openmrs.hl7.HL7Source)
	 */
	@Override
	public void deleteHL7Source(HL7Source hl7Source) throws DAOException {
		sessionFactory.getCurrentSession().delete(hl7Source);
	}
	
	/**
	 * @see org.openmrs.hl7.db.HL7DAO#saveHL7InQueue(org.openmrs.hl7.HL7InQueue)
	 */
	@Override
	public HL7InQueue saveHL7InQueue(HL7InQueue hl7InQueue) throws DAOException {
		sessionFactory.getCurrentSession().saveOrUpdate(hl7InQueue);
		return hl7InQueue;
	}
	
	/**
	 * @see org.openmrs.hl7.db.HL7DAO#getHL7InQueue(java.lang.Integer)
	 */
	@Override
	public HL7InQueue getHL7InQueue(Integer hl7InQueueId) throws DAOException {
		return sessionFactory.getCurrentSession().get(HL7InQueue.class, hl7InQueueId);
	}
	
	@Override
	public HL7InQueue getHL7InQueueByUuid(String uuid) throws DAOException {
		return HibernateUtil.getUniqueEntityByUUID(sessionFactory, HL7InQueue.class, uuid);
	}
	
	/**
	 * @see org.openmrs.hl7.db.HL7DAO#getAllHL7InQueues()
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<HL7InQueue> getAllHL7InQueues() throws DAOException {
		return sessionFactory.getCurrentSession()
		        .createQuery("from HL7InQueue where messageState = ?0 order by HL7InQueueId").setParameter(0,
		            HL7Constants.HL7_STATUS_PENDING, StandardBasicTypes.INTEGER).list();
	}
	
	/**
	 * creates a Criteria object for use with counting and finding HL7InQueue objects
	 *
	 * @param messageState status of HL7InQueue object
	 * @param query string query to match against
	 * @return a Criteria object
	 */
	@SuppressWarnings("rawtypes")
	private List<Predicate> getHL7SearchCriteria(CriteriaBuilder cb, Root<?> root, Class<?> clazz, Integer messageState, String query) throws DAOException {
		if (clazz == null) {
			throw new DAOException("no class defined for HL7 search");
		}

		List<Predicate> predicates = new ArrayList<>();

		if (query != null && !query.isEmpty()) {
			String pattern = MatchMode.ANYWHERE.toCaseSensitivePattern(query);

			if (clazz == HL7InError.class) {
				Predicate hl7DataPredicate = cb.like(root.get("HL7Data"), pattern);
				Predicate errorDetailsPredicate = cb.like(root.get("errorDetails"), pattern);
				Predicate errorPredicate = cb.like(root.get("error"), pattern);

				predicates.add(cb.or(hl7DataPredicate, errorDetailsPredicate, errorPredicate));
			} else {
				predicates.add(cb.like(root.get("HL7Data"), pattern));
			}
		}

		if (messageState != null) {
			predicates.add(cb.equal(root.get("messageState"), messageState));
		}

		return predicates;
	}

	/**
	 * @see org.openmrs.hl7.db.HL7DAO#getHL7Batch(Class, int, int, Integer, String)
	 */
	@Override
	@SuppressWarnings( { "rawtypes", "unchecked" })
	public <T> List<T> getHL7Batch(Class clazz, int start, int length, Integer messageState, String query)
	        throws DAOException {
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<T> cq = cb.createQuery(clazz);
		Root<T> root = cq.from(clazz);
		
		List<Predicate> predicates = getHL7SearchCriteria(cb, root, clazz, messageState, query);

		cq.where(predicates.toArray(new Predicate[]{}))
			.orderBy(cb.asc(root.get("dateCreated")));

		TypedQuery<T> typedQuery = session.createQuery(cq);
		typedQuery.setFirstResult(start);
		typedQuery.setMaxResults(length);

		return typedQuery.getResultList();
	}
	
	/**
	 * @see org.openmrs.hl7.db.HL7DAO#countHL7s(Class, Integer, String)
	 */
	@Override
	@SuppressWarnings("rawtypes")
	public Long countHL7s(Class clazz, Integer messageState, String query) {
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<Long> cq = cb.createQuery(Long.class);
		Root<?> root = cq.from(clazz);

		List<Predicate> predicates = getHL7SearchCriteria(cb, root, clazz, messageState, query);
		cq.select(cb.count(root))
			.where(predicates.toArray(new Predicate[]{}));

		return session.createQuery(cq).getSingleResult();
	}
	
	/**
	 * @see org.openmrs.hl7.db.HL7DAO#getNextHL7InQueue()
	 */
	@Override
	public HL7InQueue getNextHL7InQueue() throws DAOException {
		Query query = sessionFactory.getCurrentSession().createQuery(
		    "from HL7InQueue as hiq where hiq.messageState = ?0 order by HL7InQueueId").setParameter(0,
		    HL7Constants.HL7_STATUS_PENDING, StandardBasicTypes.INTEGER).setMaxResults(1);
		if (query == null) {
			return null;
		}
		return JpaUtils.getSingleResultOrNull(query);
	}
	
	/**
	 * @see org.openmrs.hl7.db.HL7DAO#deleteHL7InQueue(org.openmrs.hl7.HL7InQueue)
	 */
	@Override
	public void deleteHL7InQueue(HL7InQueue hl7InQueue) throws DAOException {
		sessionFactory.getCurrentSession().delete(hl7InQueue);
	}
	
	/**
	 * @see org.openmrs.hl7.db.HL7DAO#saveHL7InArchive(org.openmrs.hl7.HL7InArchive)
	 */
	@Override
	public HL7InArchive saveHL7InArchive(HL7InArchive hl7InArchive) throws DAOException {
		sessionFactory.getCurrentSession().save(hl7InArchive);
		return hl7InArchive;
	}
	
	/**
	 * @see org.openmrs.hl7.db.HL7DAO#getHL7InArchive(java.lang.Integer)
	 */
	@Override
	public HL7InArchive getHL7InArchive(Integer hl7InArchiveId) throws DAOException {
		return sessionFactory.getCurrentSession().get(HL7InArchive.class, hl7InArchiveId);
	}
	
	/**
	 * @see org.openmrs.hl7.db.HL7DAO#getHL7InArchiveByState(Integer state)
	 */
	@Override
	public List<HL7InArchive> getHL7InArchiveByState(Integer state) throws DAOException {
		return getHL7InArchiveByState(state, null);
	}
	
	/**
	 * limits results of getHL7InArchiveByState
	 */
	@SuppressWarnings("unchecked")
	private List<HL7InArchive> getHL7InArchiveByState(Integer state, Integer maxResults) throws DAOException {
		Query q = sessionFactory.getCurrentSession().createQuery("from HL7InArchive where messageState = ?0").setParameter(0,
		    state, StandardBasicTypes.INTEGER);
		if (maxResults != null) {
			q.setMaxResults(maxResults);
		}
		return q.getResultList();
	}
	
	/**
	 * @see org.openmrs.hl7.db.HL7DAO#getHL7InQueueByState(Integer stateId)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<HL7InQueue> getHL7InQueueByState(Integer state) throws DAOException {
		return sessionFactory.getCurrentSession().createQuery("from HL7InQueue where messageState = ?0").setParameter(0,
		    state, StandardBasicTypes.INTEGER).list();
	}
	
	/**
	 * @see org.openmrs.hl7.db.HL7DAO#getAllHL7InArchives()
	 */
	@Override
	public List<HL7InArchive> getAllHL7InArchives() throws DAOException {
		return getAllHL7InArchives(null);
	}
	
	/**
	 * @see org.openmrs.hl7.db.HL7DAO#getAllHL7InArchives(Integer)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<HL7InArchive> getAllHL7InArchives(Integer maxResults) {
		Query q = sessionFactory.getCurrentSession().createQuery("from HL7InArchive order by HL7InArchiveId");
		if (maxResults != null) {
			q.setMaxResults(maxResults);
		}
		return q.getResultList();
	}
	
	/**
	 * @see org.openmrs.hl7.db.HL7DAO#deleteHL7InArchive(org.openmrs.hl7.HL7InArchive)
	 */
	@Override
	public void deleteHL7InArchive(HL7InArchive hl7InArchive) throws DAOException {
		sessionFactory.getCurrentSession().delete(hl7InArchive);
	}
	
	/**
	 * @see org.openmrs.hl7.db.HL7DAO#saveHL7InError(HL7InError)
	 */
	@Override
	public HL7InError saveHL7InError(HL7InError hl7InError) throws DAOException {
		sessionFactory.getCurrentSession().save(hl7InError);
		return hl7InError;
	}
	
	/**
	 * @see org.openmrs.hl7.db.HL7DAO#getHL7InError(Integer)
	 */
	@Override
	public HL7InError getHL7InError(Integer hl7InErrorId) throws DAOException {
		return sessionFactory.getCurrentSession().get(HL7InError.class, hl7InErrorId);
	}
	
	@Override
	public HL7InError getHL7InErrorByUuid(String uuid) throws DAOException {
		return HibernateUtil.getUniqueEntityByUUID(sessionFactory, HL7InError.class, uuid);
	}
	
	/**
	 * @see org.openmrs.hl7.db.HL7DAO#getAllHL7InErrors()
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<HL7InError> getAllHL7InErrors() throws DAOException {
		return sessionFactory.getCurrentSession().createQuery("from HL7InError order by HL7InErrorId").list();
	}
	
	/**
	 * @see org.openmrs.hl7.db.HL7DAO#deleteHL7InError(HL7InError)
	 */
	@Override
	public void deleteHL7InError(HL7InError hl7InError) throws DAOException {
		sessionFactory.getCurrentSession().delete(hl7InError);
	}
	
	/**
	 * @see org.openmrs.hl7.db.HL7DAO#garbageCollect()
	 */
	@Override
	public void garbageCollect() {
		Context.clearSession();
	}
	
	/**
	 * @see org.openmrs.hl7.db.HL7DAO#getHL7InArchiveByUuid(java.lang.String)
	 */
	@Override
	public HL7InArchive getHL7InArchiveByUuid(String uuid) throws DAOException {
		Query query = sessionFactory.getCurrentSession().createQuery("from HL7InArchive where uuid = ?0").setParameter(0,
		    uuid, StandardBasicTypes.STRING);
		Object record = JpaUtils.getSingleResultOrNull(query);
		if (record == null) {
			return null;
		}
		return (HL7InArchive) record;
	}
	
	/**
	 * @see org.openmrs.hl7.db.HL7DAO#getHL7InArchivesToMigrate()
	 */
	@Override
	public List<HL7InArchive> getHL7InArchivesToMigrate() {
		Integer daysToKeep = Hl7InArchivesMigrateThread.getDaysKept();
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<HL7InArchive> cq = cb.createQuery(HL7InArchive.class);
		Root<HL7InArchive> root = cq.from(HL7InArchive.class);

		List<Predicate> predicates = getHL7SearchCriteria(cb, root, HL7InArchive.class, HL7Constants.HL7_STATUS_PROCESSED, null);

		if (daysToKeep != null) {
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DATE, -1 * daysToKeep);
			predicates.add(cb.lessThan(root.get("dateCreated"), cal.getTime()));
		}

		cq.where(predicates.toArray(new Predicate[]{}));
		return session.createQuery(cq)
			.setMaxResults(HL7Constants.MIGRATION_MAX_BATCH_SIZE)
			.getResultList();
	}
	
}
