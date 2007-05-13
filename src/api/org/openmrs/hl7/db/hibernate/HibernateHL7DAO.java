package org.openmrs.hl7.db.hibernate;

import java.util.Collection;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.DAOException;
import org.openmrs.hl7.HL7InArchive;
import org.openmrs.hl7.HL7InError;
import org.openmrs.hl7.HL7InQueue;
import org.openmrs.hl7.HL7Source;
import org.openmrs.hl7.db.HL7DAO;

public class HibernateHL7DAO implements HL7DAO {

	protected final Log log = LogFactory.getLog(getClass());

	/**
	 * Hibernate session factory
	 */
	private SessionFactory sessionFactory;
	
	public HibernateHL7DAO() { }
	
	/**
	 * Set session factory
	 * 
	 * @param sessionFactory
	 */
	public void setSessionFactory(SessionFactory sessionFactory) { 
		this.sessionFactory = sessionFactory;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.hl7.db.HL7DAO#createHL7Source(org.openmrs.hl7.HL7Source)
	 */
	public void createHL7Source(HL7Source hl7Source) throws DAOException {
		// TODO Creator needs to be set by client  
		hl7Source.setCreator(Context.getAuthenticatedUser());
		hl7Source.setDateCreated(new Date());
		sessionFactory.getCurrentSession().save(hl7Source);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.hl7.db.HL7DAO#getHL7Source(java.lang.Integer)
	 */
	public HL7Source getHL7Source(Integer hl7SourceId) throws DAOException {
		return (HL7Source) sessionFactory.getCurrentSession().get(HL7Source.class, hl7SourceId);
	}
	
	public HL7Source getHL7Source(String name) throws DAOException {
		Criteria crit = sessionFactory.getCurrentSession().createCriteria(HL7Source.class);
		crit.add(Restrictions.eq("name", name));
		return (HL7Source) crit.uniqueResult();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.hl7.db.HL7DAO#getHL7Sources()
	 */
	@SuppressWarnings("unchecked")
	public Collection<HL7Source> getHL7Sources() throws DAOException {
		// return session.createQuery("from HL7Source order by hL7SourceId")
		return sessionFactory.getCurrentSession().createQuery("from HL7Source")
				.list();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.hl7.db.HL7DAO#updateHL7Source(org.openmrs.hl7.HL7Source)
	 */
	public void updateHL7Source(HL7Source hl7Source) throws DAOException {
		if (hl7Source.getHL7SourceId() == 0)
			createHL7Source(hl7Source);
		else
			sessionFactory.getCurrentSession().saveOrUpdate(hl7Source);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.hl7.db.HL7DAO#deleteHL7Source(org.openmrs.hl7.HL7Source)
	 */
	public void deleteHL7Source(HL7Source hl7Source) throws DAOException {
		sessionFactory.getCurrentSession().delete(hl7Source);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.hl7.db.HL7DAO#createHL7InQueue(org.openmrs.hl7.HL7InQueue)
	 */
	public void createHL7InQueue(HL7InQueue hl7InQueue) throws DAOException {
		hl7InQueue.setDateCreated(new Date());
		sessionFactory.getCurrentSession().save(hl7InQueue);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.hl7.db.HL7DAO#getHL7InQueue(java.lang.Integer)
	 */
	public HL7InQueue getHL7InQueue(Integer hl7InQueueId) throws DAOException {
		return (HL7InQueue) sessionFactory.getCurrentSession().get(HL7InQueue.class, hl7InQueueId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.hl7.db.HL7DAO#getHL7InQueues()
	 */
	@SuppressWarnings("unchecked")
	public Collection<HL7InQueue> getHL7InQueues() throws DAOException {
		return sessionFactory.getCurrentSession().createQuery("from HL7InQueue order by hL7InQueueId")
				.list();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.hl7.db.HL7DAO#getNextHL7InQueue()
	 */
	public HL7InQueue getNextHL7InQueue() throws DAOException {
		Query query = sessionFactory.getCurrentSession()
				.createQuery("from HL7InQueue as hiq where hiq.HL7InQueueId = (select min(HL7InQueueId) from HL7InQueue)");
		if (query == null)
			return null;
		return (HL7InQueue) query.uniqueResult();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.hl7.db.HL7DAO#deleteHL7InQueue(org.openmrs.hl7.HL7InQueue)
	 */
	public void deleteHL7InQueue(HL7InQueue hl7InQueue) throws DAOException {
		sessionFactory.getCurrentSession().delete(hl7InQueue);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.hl7.db.HL7DAO#createHL7InArchive(org.openmrs.hl7.HL7InArchive)
	 */
	public void createHL7InArchive(HL7InArchive hl7InArchive)
			throws DAOException {
		hl7InArchive.setDateCreated(new Date());
		sessionFactory.getCurrentSession().save(hl7InArchive);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.hl7.db.HL7DAO#getHL7InArchive(java.lang.Integer)
	 */
	public HL7InArchive getHL7InArchive(Integer hl7InArchiveId)
			throws DAOException {
		return (HL7InArchive) sessionFactory.getCurrentSession().get(HL7InArchive.class,
				hl7InArchiveId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.hl7.db.HL7DAO#getHL7InArchives()
	 */
	@SuppressWarnings("unchecked")
	public Collection<HL7InArchive> getHL7InArchives() throws DAOException {
		return sessionFactory.getCurrentSession().createQuery("from HL7InArchive order by hL7InArchiveId")
				.list();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.hl7.db.HL7DAO#updateHL7InArchive(org.openmrs.hl7.HL7InArchive)
	 */
	public void updateHL7InArchive(HL7InArchive hl7InArchive)
			throws DAOException {
		if (hl7InArchive.getHL7InArchiveId() == 0)
			createHL7InArchive(hl7InArchive);
		else
			sessionFactory.getCurrentSession().saveOrUpdate(hl7InArchive);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.hl7.db.HL7DAO#deleteHL7InArchive(org.openmrs.hl7.HL7InArchive)
	 */
	public void deleteHL7InArchive(HL7InArchive hl7InArchive)
			throws DAOException {
		sessionFactory.getCurrentSession().delete(hl7InArchive);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.hl7.db.HL7DAO#createHL7InException(org.openmrs.hl7.HL7InException)
	 */
	public void createHL7InError(HL7InError hl7InError) throws DAOException {
		hl7InError.setDateCreated(new Date());
		sessionFactory.getCurrentSession().save(hl7InError);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.hl7.db.HL7DAO#getHL7InException(java.lang.Integer)
	 */
	public HL7InError getHL7InError(Integer hl7InErrorId) throws DAOException {
		return (HL7InError) sessionFactory.getCurrentSession().get(HL7InError.class, hl7InErrorId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.hl7.db.HL7DAO#getHL7InExceptions()
	 */
	@SuppressWarnings("unchecked")
	public Collection<HL7InError> getHL7InErrors() throws DAOException {
		return sessionFactory.getCurrentSession().createQuery("from HL7InError order by hL7InErrorId")
				.list();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.hl7.db.HL7DAO#updateHL7InException(org.openmrs.hl7.HL7InException)
	 */
	public void updateHL7InError(HL7InError hl7InErr) throws DAOException {
		if (hl7InErr.getHL7InErrorId() == 0)
			createHL7InError(hl7InErr);
		else
			sessionFactory.getCurrentSession().saveOrUpdate(hl7InErr);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.hl7.db.HL7DAO#deleteHL7InException(org.openmrs.hl7.HL7InException)
	 */
	public void deleteHL7InError(HL7InError hl7InError) throws DAOException {
		sessionFactory.getCurrentSession().delete(hl7InError);
	}

	public void garbageCollect() {
		Context.clearSession();
	}

}
