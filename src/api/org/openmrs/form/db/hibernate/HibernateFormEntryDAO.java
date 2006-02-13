package org.openmrs.form.db.hibernate;

import java.math.BigInteger;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;
import org.hibernate.Session;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.DAOException;
import org.openmrs.api.db.hibernate.HibernateUtil;
import org.openmrs.form.FormEntryQueue;
import org.openmrs.form.db.FormEntryDAO;

public class HibernateFormEntryDAO implements FormEntryDAO {

	protected final Log log = LogFactory.getLog(getClass());

	private Context context;

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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.form.db.FormEntryQueueDAO#getFormEntryQueue(int)
	 */
	public FormEntryQueue getFormEntryQueue(int formEntryQueueId)
			throws DAOException {
		Session session = HibernateUtil.currentSession();

		FormEntryQueue feq = new FormEntryQueue();
		feq = (FormEntryQueue) session.get(FormEntryQueue.class,
				formEntryQueueId);

		return feq;
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
	 * @see org.openmrs.form.db.FormEntryDAO#getNextFormEntryQueue()
	 */
	public FormEntryQueue getNextFormEntryQueue() throws DAOException {
		Session session = HibernateUtil.currentSession();

		FormEntryQueue formEntryQueue = null;
		try {
			HibernateUtil.beginTransaction();
			Query query = session
					.createQuery("select min(FormEntryQueueId) from FormEntryQueue feq where feq.formData is not null");
			HibernateUtil.commitTransaction();
			Integer formEntryQueueId = ((BigInteger) query.uniqueResult())
					.intValue();
			formEntryQueue = (FormEntryQueue) session.get(FormEntryQueue.class,
					formEntryQueueId);

		} catch (Exception e) {
			HibernateUtil.rollbackTransaction();
			throw new DAOException(e);
		}
		return formEntryQueue;
	}

}
