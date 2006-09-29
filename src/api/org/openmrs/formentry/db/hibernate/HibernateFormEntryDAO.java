package org.openmrs.formentry.db.hibernate;

import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.DAOException;
import org.openmrs.formentry.FormEntryArchive;
import org.openmrs.formentry.FormEntryError;
import org.openmrs.formentry.FormEntryQueue;
import org.openmrs.formentry.db.FormEntryDAO;

public class HibernateFormEntryDAO implements FormEntryDAO {

	protected final Log log = LogFactory.getLog(getClass());
	
	/**
	 * Hibernate session factory
	 */
	private SessionFactory sessionFactory;
	
	/**
	 * Default public constructor
	 */
	public HibernateFormEntryDAO() { }
	
	/**
	 * Set session factory
	 * 
	 * @param sessionFactory
	 */
	public void setSessionFactory(SessionFactory sessionFactory) { 
		this.sessionFactory = sessionFactory;
	}

	/**
	 * @see org.openmrs.form.db.FormEntryQueueDAO#createFormEntryQueue(org.openmrs.form.FormEntryQueue)
	 */
	public void createFormEntryQueue(FormEntryQueue feq) throws DAOException {
		sessionFactory.getCurrentSession().save(feq);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.form.db.FormEntryQueueDAO#getFormEntryQueue(int)
	 */
	public FormEntryQueue getFormEntryQueue(Integer formEntryQueueId)
			throws DAOException {
		
		FormEntryQueue feq;
		feq = (FormEntryQueue) sessionFactory.getCurrentSession().get(FormEntryQueue.class,
				formEntryQueueId);

		return feq;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.form.db.FormEntryDAO#getNextFormEntryQueue()
	 */
	public FormEntryQueue getNextFormEntryQueue() throws DAOException {
		FormEntryQueue feq = null;
		Query query = sessionFactory.getCurrentSession()
				.createQuery("from FormEntryQueue f1 where f1.formEntryQueueId = (select min(formEntryQueueId) from FormEntryQueue)");
		if (query != null)
			feq = (FormEntryQueue) query.uniqueResult();
		return feq;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.form.db.FormEntryQueueDAO#getFormEntryQueue(int)
	 */
	@SuppressWarnings("unchecked")
	public Collection<FormEntryQueue> getFormEntryQueues() throws DAOException {
		return sessionFactory.getCurrentSession().createQuery(
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
			sessionFactory.getCurrentSession().saveOrUpdate(formEntryQueue);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.form.db.FormEntryQueueDAO#deleteFormEntryQueue(org.openmrs.form.FormEntryQueue)
	 */
	public void deleteFormEntryQueue(FormEntryQueue feq) throws DAOException {
		sessionFactory.getCurrentSession().delete(feq);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.formentry.db.FormEntryDAO#getFormEntryQueueSize()
	 */
	public Integer getFormEntryQueueSize() throws DAOException {

		Integer size = (Integer) sessionFactory.getCurrentSession().createQuery(
				"select count(*) from FormEntryQueue").uniqueResult();

		return size;
	}

	public void createFormEntryArchive(FormEntryArchive formEntryArchive)
			throws DAOException {
		sessionFactory.getCurrentSession().save(formEntryArchive);
	}

	public FormEntryArchive getFormEntryArchive(Integer formEntryArchiveId)
			throws DAOException {
		FormEntryArchive formEntryArchive;
		formEntryArchive = (FormEntryArchive) sessionFactory.getCurrentSession().get(
				FormEntryArchive.class, formEntryArchiveId);

		return formEntryArchive;
	}

	@SuppressWarnings("unchecked")
	public Collection<FormEntryArchive> getFormEntryArchives()
			throws DAOException {
		return sessionFactory.getCurrentSession().createQuery(
				"from FormEntryArchive order by formEntryArchiveId").list();
	}

	public void deleteFormEntryArchive(FormEntryArchive formEntryArchive)
			throws DAOException {
		sessionFactory.getCurrentSession().delete(formEntryArchive);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.formentry.db.FormEntryDAO#getFormEntryArchiveSize()
	 */
	public Integer getFormEntryArchiveSize() throws DAOException {
		Integer size = (Integer) sessionFactory.getCurrentSession().createQuery(
				"select count(*) from FormEntryArchive").uniqueResult();

		return size;
	}

	public void createFormEntryError(FormEntryError formEntryError)
			throws DAOException {
		sessionFactory.getCurrentSession().save(formEntryError);
	}

	public FormEntryError getFormEntryError(Integer formEntryErrorId)
			throws DAOException {
		FormEntryError formEntryError;
		formEntryError = (FormEntryError) sessionFactory.getCurrentSession().get(FormEntryError.class,
				formEntryErrorId);

		return formEntryError;
	}

	@SuppressWarnings("unchecked")
	public Collection<FormEntryError> getFormEntryErrors() throws DAOException {
		return sessionFactory.getCurrentSession().createQuery(
				"from FormEntryError order by formEntryErrorId").list();
	}

	public void updateFormEntryError(FormEntryError formEntryError)
			throws DAOException {
		if (formEntryError.getFormEntryErrorId() == 0)
			createFormEntryError(formEntryError);
		else {
			sessionFactory.getCurrentSession().saveOrUpdate(formEntryError);
		}

	}

	public void deleteFormEntryError(FormEntryError formEntryError)
			throws DAOException {
		sessionFactory.getCurrentSession().delete(formEntryError);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.formentry.db.FormEntryDAO#getFormEntryErrorSize()
	 */
	public Integer getFormEntryErrorSize() throws DAOException {
		Integer size = (Integer) sessionFactory.getCurrentSession().createQuery(
				"select count(*) from FormEntryError").uniqueResult();

		return size;
	}

	public void garbageCollect() {
		Context.clearSession();
	}

}
