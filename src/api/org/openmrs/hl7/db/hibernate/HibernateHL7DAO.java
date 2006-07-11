package org.openmrs.hl7.db.hibernate;

import java.util.Collection;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;
import org.hibernate.Session;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.DAOException;
import org.openmrs.api.db.hibernate.HibernateUtil;
import org.openmrs.hl7.HL7InArchive;
import org.openmrs.hl7.HL7InError;
import org.openmrs.hl7.HL7InQueue;
import org.openmrs.hl7.HL7Source;
import org.openmrs.hl7.db.HL7DAO;

public class HibernateHL7DAO implements HL7DAO {

	protected final Log log = LogFactory.getLog(getClass());

	private Context context;
	
	public HibernateHL7DAO() { }

	public HibernateHL7DAO(Context context) {
		this.context = context;
	}	
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.hl7.db.HL7DAO#createHL7Source(org.openmrs.hl7.HL7Source)
	 */
	public void createHL7Source(HL7Source hl7Source) throws DAOException {
		Session session = HibernateUtil.currentSession();

		// TODO Creator needs to be set by client  
		hl7Source.setCreator(context.getAuthenticatedUser());
		hl7Source.setDateCreated(new Date());
		try {
			HibernateUtil.beginTransaction();
			session.save(hl7Source);
			HibernateUtil.commitTransaction();
		} catch (Exception e) {
			HibernateUtil.rollbackTransaction();
			throw new DAOException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.hl7.db.HL7DAO#getHL7Source(java.lang.Integer)
	 */
	public HL7Source getHL7Source(Integer hl7SourceId) throws DAOException {
		Session session = HibernateUtil.currentSession();

		HL7Source hl7Source;
		hl7Source = (HL7Source) session.get(HL7Source.class, hl7SourceId);

		return hl7Source;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.hl7.db.HL7DAO#getHL7Sources()
	 */
	public Collection<HL7Source> getHL7Sources() throws DAOException {
		Session session = HibernateUtil.currentSession();

		// return session.createQuery("from HL7Source order by hL7SourceId")
		return session.createQuery("from HL7Source")
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
		else {
			Session session = HibernateUtil.currentSession();

			try {
				HibernateUtil.beginTransaction();
				session.saveOrUpdate(hl7Source);
				HibernateUtil.commitTransaction();
			} catch (Exception e) {
				HibernateUtil.rollbackTransaction();
				throw new DAOException(e);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.hl7.db.HL7DAO#deleteHL7Source(org.openmrs.hl7.HL7Source)
	 */
	public void deleteHL7Source(HL7Source hl7Source) throws DAOException {
		Session session = HibernateUtil.currentSession();

		try {
			HibernateUtil.beginTransaction();
			session.delete(hl7Source);
			HibernateUtil.commitTransaction();
		} catch (Exception e) {
			HibernateUtil.rollbackTransaction();
			throw new DAOException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.hl7.db.HL7DAO#createHL7InQueue(org.openmrs.hl7.HL7InQueue)
	 */
	public void createHL7InQueue(HL7InQueue hl7InQueue) throws DAOException {
		Session session = HibernateUtil.currentSession();

		hl7InQueue.setDateCreated(new Date());
		try {
			HibernateUtil.beginTransaction();
			session.save(hl7InQueue);
			HibernateUtil.commitTransaction();
		} catch (Exception e) {
			HibernateUtil.rollbackTransaction();
			throw new DAOException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.hl7.db.HL7DAO#getHL7InQueue(java.lang.Integer)
	 */
	public HL7InQueue getHL7InQueue(Integer hl7InQueueId) throws DAOException {
		Session session = HibernateUtil.currentSession();

		HL7InQueue hl7InQueue;
		hl7InQueue = (HL7InQueue) session.get(HL7InQueue.class, hl7InQueueId);

		return hl7InQueue;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.hl7.db.HL7DAO#getHL7InQueues()
	 */
	public Collection<HL7InQueue> getHL7InQueues() throws DAOException {
		Session session = HibernateUtil.currentSession();

		return session.createQuery("from HL7InQueue order by hl7InQueueId")
				.list();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.hl7.db.HL7DAO#getNextHL7InQueue()
	 */
	public HL7InQueue getNextHL7InQueue() throws DAOException {
		Session session = HibernateUtil.currentSession();

		Query query = session
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
		Session session = HibernateUtil.currentSession();

		try {
			HibernateUtil.beginTransaction();
			session.delete(hl7InQueue);
			HibernateUtil.commitTransaction();
		} catch (Exception e) {
			HibernateUtil.rollbackTransaction();
			throw new DAOException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.hl7.db.HL7DAO#createHL7InArchive(org.openmrs.hl7.HL7InArchive)
	 */
	public void createHL7InArchive(HL7InArchive hl7InArchive)
			throws DAOException {
		Session session = HibernateUtil.currentSession();

		hl7InArchive.setDateCreated(new Date());
		try {
			HibernateUtil.beginTransaction();
			session.save(hl7InArchive);
			HibernateUtil.commitTransaction();
		} catch (Exception e) {
			HibernateUtil.rollbackTransaction();
			throw new DAOException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.hl7.db.HL7DAO#getHL7InArchive(java.lang.Integer)
	 */
	public HL7InArchive getHL7InArchive(Integer hl7InArchiveId)
			throws DAOException {
		Session session = HibernateUtil.currentSession();

		HL7InArchive hl7InArchive;
		hl7InArchive = (HL7InArchive) session.get(HL7InArchive.class,
				hl7InArchiveId);

		return hl7InArchive;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.hl7.db.HL7DAO#getHL7InArchives()
	 */
	public Collection<HL7InArchive> getHL7InArchives() throws DAOException {
		Session session = HibernateUtil.currentSession();

		return session.createQuery("from HL7InArchive order by hL7InArchiveId")
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
		else {
			Session session = HibernateUtil.currentSession();

			try {
				HibernateUtil.beginTransaction();
				session.saveOrUpdate(hl7InArchive);
				HibernateUtil.commitTransaction();
			} catch (Exception e) {
				HibernateUtil.rollbackTransaction();
				throw new DAOException(e);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.hl7.db.HL7DAO#deleteHL7InArchive(org.openmrs.hl7.HL7InArchive)
	 */
	public void deleteHL7InArchive(HL7InArchive hl7InArchive)
			throws DAOException {
		Session session = HibernateUtil.currentSession();

		try {
			HibernateUtil.beginTransaction();
			session.delete(hl7InArchive);
			HibernateUtil.commitTransaction();
		} catch (Exception e) {
			HibernateUtil.rollbackTransaction();
			throw new DAOException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.hl7.db.HL7DAO#createHL7InException(org.openmrs.hl7.HL7InException)
	 */
	public void createHL7InError(HL7InError hl7InError) throws DAOException {
		Session session = HibernateUtil.currentSession();

		hl7InError.setDateCreated(new Date());
		try {
			HibernateUtil.beginTransaction();
			session.save(hl7InError);
			HibernateUtil.commitTransaction();
		} catch (Exception e) {
			HibernateUtil.rollbackTransaction();
			throw new DAOException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.hl7.db.HL7DAO#getHL7InException(java.lang.Integer)
	 */
	public HL7InError getHL7InError(Integer hl7InErrorId) throws DAOException {
		Session session = HibernateUtil.currentSession();

		HL7InError hl7InError;
		hl7InError = (HL7InError) session.get(HL7InError.class, hl7InErrorId);

		return hl7InError;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.hl7.db.HL7DAO#getHL7InExceptions()
	 */
	public Collection<HL7InError> getHL7InErrors() throws DAOException {
		Session session = HibernateUtil.currentSession();

		return session.createQuery("from HL7InError order by hL7InErrorId")
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
		else {
			Session session = HibernateUtil.currentSession();

			try {
				HibernateUtil.beginTransaction();
				session.saveOrUpdate(hl7InErr);
				HibernateUtil.commitTransaction();
			} catch (Exception e) {
				HibernateUtil.rollbackTransaction();
				throw new DAOException(e);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.hl7.db.HL7DAO#deleteHL7InException(org.openmrs.hl7.HL7InException)
	 */
	public void deleteHL7InError(HL7InError hl7InError) throws DAOException {
		Session session = HibernateUtil.currentSession();

		try {
			HibernateUtil.beginTransaction();
			session.delete(hl7InError);
			HibernateUtil.commitTransaction();
		} catch (Exception e) {
			HibernateUtil.rollbackTransaction();
			throw new DAOException(e);
		}
	}

	public void garbageCollect() {
		HibernateUtil.clear();
	}

}
