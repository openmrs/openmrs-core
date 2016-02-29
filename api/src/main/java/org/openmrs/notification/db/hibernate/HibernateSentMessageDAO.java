package org.openmrs.notification.db.hibernate;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SessionFactory;
import org.openmrs.api.db.DAOException;
import org.openmrs.notification.SentMessage;
import org.openmrs.notification.db.SentMessageDAO;

public class HibernateSentMessageDAO implements SentMessageDAO {

private final Log log = LogFactory.getLog(getClass());
	
	/**
	 * Hibernate session factory
	 */
	private SessionFactory sessionFactory;
	
	public HibernateSentMessageDAO() {
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
	 * @see org.openmrs.notification.db.SentMessageDAO#saveSentMessage(org.openmrs.notification.SentMessage)
	 */
	@Override
	public SentMessage saveSentMessage(SentMessage sentMessage) throws DAOException {
		sessionFactory.getCurrentSession().saveOrUpdate(sentMessage);
		return sentMessage;
	}

	/**
	 * @see org.openmrs.notification.db.SentMessageDAO#getSentMessage(java.lang.Integer)
	 */
	@Override
	public SentMessage getSentMessage(Integer sentMessageId) throws DAOException {
		return (SentMessage) sessionFactory.getCurrentSession().get(SentMessage.class, sentMessageId);
	}

	/**
	 * @see org.openmrs.notification.db.SentMessageDAO#deleteSentMessage(org.openmrs.notification.SentMessage)
	 */
	@Override
	public void deleteSentMessage(SentMessage sentMessage) throws DAOException {
		sessionFactory.getCurrentSession().delete(sentMessage);
	}

	/**
	 * @see org.openmrs.notification.db.SentMessageDAO#getAllSentMessages(org.openmrs.User, boolean, boolean)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<SentMessage> getAllSentMessages() {
		List<SentMessage> sentMessages = sessionFactory.getCurrentSession().createQuery("from sent_message").list();
		return sentMessages;
	}
}