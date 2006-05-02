package org.openmrs.formentry.db.hibernate;

import java.util.Collection;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.DAOException;
import org.openmrs.api.db.hibernate.HibernateUtil;
import org.openmrs.formentry.FormEntryArchive;
import org.openmrs.formentry.FormEntryError;
import org.openmrs.formentry.FormEntryQueue;
import org.openmrs.formentry.db.FormEntryDAO;

public class HibernateFormEntryDAO implements FormEntryDAO {

	protected final Log log = LogFactory.getLog(getClass());

	private Context context;
	/*	 *  Default public constructor	 */
	public HibernateFormEntryDAO() {}

	/*	 *  Single-arg Public constructor	 */
	public HibernateFormEntryDAO(Context context) {
		this.context = context;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.form.db.FormEntryQueueDAO#createFormEntryQueue(org.openmrs.form.FormEntryQueue)
	 */
	public void createFormEntryQueue(FormEntryQueue feq) throws DAOException {
		Session session = HibernateUtil.currentSession();

		feq.setCreator(context.getAuthenticatedUser());
		feq.setDateCreated(new Date());
		try {
			HibernateUtil.beginTransaction();
			session.save(feq);
			HibernateUtil.commitTransaction();
		} catch (Exception e) {
			HibernateUtil.rollbackTransaction();
			throw new DAOException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.form.db.FormEntryQueueDAO#getFormEntryQueue(int)
	 */
	public FormEntryQueue getFormEntryQueue(Integer formEntryQueueId)
			throws DAOException {
		Session session = HibernateUtil.currentSession();

		FormEntryQueue feq;
		feq = (FormEntryQueue) session.get(FormEntryQueue.class,
				formEntryQueueId);

		return feq;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.form.db.FormEntryDAO#getNextFormEntryQueue()
	 */
	public FormEntryQueue getNextFormEntryQueue() throws DAOException {
		Session session = HibernateUtil.currentSession();

		return (FormEntryQueue) session
				.createQuery(
						"from FormEntryQueue f1 where f1.formEntryQueueId = (select min(formEntryQueueId) from FormEntryQueue)")
				.uniqueResult();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.form.db.FormEntryQueueDAO#getFormEntryQueue(int)
	 */
	public Collection<FormEntryQueue> getFormEntryQueues() throws DAOException {
		Session session = HibernateUtil.currentSession();

		return session.createQuery(
				"from FormEntryQueue order by formEntryQueueId").list();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.form.db.FormEntryQueueDAO#updateFormEntryQueue(org.openmrs.form.FormEntryQueue)
	 */
	public void updateFormEntryQueue(FormEntryQueue formEntryQueue)
			throws DAOException {

		if (formEntryQueue.getFormEntryQueueId() == 0)
			createFormEntryQueue(formEntryQueue);
		else {
			Session session = HibernateUtil.currentSession();

			try {
				HibernateUtil.beginTransaction();
				session.saveOrUpdate(formEntryQueue);
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
	 * @see org.openmrs.form.db.FormEntryQueueDAO#deleteFormEntryQueue(org.openmrs.form.FormEntryQueue)
	 */
	public void deleteFormEntryQueue(FormEntryQueue feq) throws DAOException {
		Session session = HibernateUtil.currentSession();

		try {
			HibernateUtil.beginTransaction();
			session.delete(feq);
			HibernateUtil.commitTransaction();
		} catch (Exception e) {
			HibernateUtil.rollbackTransaction();
			throw new DAOException(e);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.openmrs.formentry.db.FormEntryDAO#getFormEntryQueueSize()
	 */
	public Integer getFormEntryQueueSize() throws DAOException {
		Session session = HibernateUtil.currentSession();
		
		Integer size = (Integer) session.createQuery("select count(*) from FormEntryQueue" )
			.uniqueResult();
		
		return size;
	}

	public void createFormEntryArchive(FormEntryArchive formEntryArchive)
			throws DAOException {
		Session session = HibernateUtil.currentSession();

		formEntryArchive.setCreator(context.getAuthenticatedUser());
		formEntryArchive.setDateCreated(new Date());
		try {
			HibernateUtil.beginTransaction();
			session.save(formEntryArchive);
			HibernateUtil.commitTransaction();
		} catch (Exception e) {
			HibernateUtil.rollbackTransaction();
			throw new DAOException(e);
		}
	}

	public FormEntryArchive getFormEntryArchive(Integer formEntryArchiveId)
			throws DAOException {
		Session session = HibernateUtil.currentSession();

		FormEntryArchive formEntryArchive;
		formEntryArchive = (FormEntryArchive) session.get(
				FormEntryArchive.class, formEntryArchiveId);

		return formEntryArchive;
	}

	public Collection<FormEntryArchive> getFormEntryArchives()
			throws DAOException {
		Session session = HibernateUtil.currentSession();

		return session.createQuery(
				"from FormEntryArchive order by formEntryArchiveId").list();
	}

	public void deleteFormEntryArchive(FormEntryArchive formEntryArchive)
			throws DAOException {
		Session session = HibernateUtil.currentSession();

		try {
			HibernateUtil.beginTransaction();
			session.delete(formEntryArchive);
			HibernateUtil.commitTransaction();
		} catch (Exception e) {
			HibernateUtil.rollbackTransaction();
			throw new DAOException(e);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.openmrs.formentry.db.FormEntryDAO#getFormEntryArchiveSize()
	 */
	public Integer getFormEntryArchiveSize() throws DAOException {
		Session session = HibernateUtil.currentSession();
		
		Integer size = (Integer) session.createQuery("select count(*) from FormEntryArchive" )
			.uniqueResult();
		
		return size;
	}

	public void createFormEntryError(FormEntryError formEntryError)
			throws DAOException {
		Session session = HibernateUtil.currentSession();

		formEntryError.setCreator(context.getAuthenticatedUser());
		formEntryError.setDateCreated(new Date());
		try {
			HibernateUtil.beginTransaction();
			session.save(formEntryError);
			HibernateUtil.commitTransaction();
		} catch (Exception e) {
			HibernateUtil.rollbackTransaction();
			throw new DAOException(e);
		}
	}

	public FormEntryError getFormEntryError(Integer formEntryErrorId)
			throws DAOException {
		Session session = HibernateUtil.currentSession();

		FormEntryError formEntryError;
		formEntryError = (FormEntryError) session.get(FormEntryError.class,
				formEntryErrorId);

		return formEntryError;
	}

	public Collection<FormEntryError> getFormEntryErrors() throws DAOException {
		Session session = HibernateUtil.currentSession();

		return session.createQuery(
				"from FormEntryError order by formEntryErrorId").list();
	}

	public void updateFormEntryError(FormEntryError formEntryError)
			throws DAOException {
		if (formEntryError.getFormEntryErrorId() == 0)
			createFormEntryError(formEntryError);
		else {
			Session session = HibernateUtil.currentSession();

			try {
				HibernateUtil.beginTransaction();
				session.saveOrUpdate(formEntryError);
				HibernateUtil.commitTransaction();
			} catch (Exception e) {
				HibernateUtil.rollbackTransaction();
				throw new DAOException(e);
			}
		}

	}

	public void deleteFormEntryError(FormEntryError formEntryError)
			throws DAOException {
		Session session = HibernateUtil.currentSession();

		try {
			HibernateUtil.beginTransaction();
			session.delete(formEntryError);
			HibernateUtil.commitTransaction();
		} catch (Exception e) {
			HibernateUtil.rollbackTransaction();
			throw new DAOException(e);
		}
	}

	/* (non-Javadoc)
	 * @see org.openmrs.formentry.db.FormEntryDAO#getFormEntryErrorSize()
	 */
	public Integer getFormEntryErrorSize() throws DAOException {
		Session session = HibernateUtil.currentSession();
		
		Integer size = (Integer) session.createQuery("select count(*) from FormEntryError" )
			.uniqueResult();
		
		return size;
	}
	
	public void garbageCollect() {
		HibernateUtil.clear();
	}

}
