/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.notification.db.hibernate;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.openmrs.User;
import org.openmrs.api.db.DAOException;
import org.openmrs.notification.MessageReceiver;
import org.openmrs.notification.SentMessage;
import org.openmrs.notification.db.SentMessageDAO;

/**
 * Hibernate specific implementation of SentMessageDAO
 */
public class HibernateSentMessageDAO implements SentMessageDAO {

	/**
	 * Hibernate session factory
	 */
	private SessionFactory sessionFactory;
	
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

	@Override
	public SentMessage getSentMessage(Integer messageId) throws DAOException {
		return (SentMessage)sessionFactory.getCurrentSession().get(SentMessage.class, messageId);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<SentMessage> getSentMessages(User user) throws DAOException {
		Criteria cr = sessionFactory.getCurrentSession().createCriteria(SentMessage.class);
		cr.add(Restrictions.eq("sender", user));
		return cr.list();
	}

	@Override
	public void deleteSentMessage(SentMessage sentMessage) throws DAOException {
		sessionFactory.getCurrentSession().delete(sentMessage);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<SentMessage> getAllSentMessages() throws DAOException {
		return sessionFactory.getCurrentSession().createCriteria(SentMessage.class).list();
	}

	@Override
	public MessageReceiver saveSentMessageReceiver(MessageReceiver messageReceiver) throws DAOException {
		sessionFactory.getCurrentSession().saveOrUpdate(messageReceiver);
		return messageReceiver;
	}
}
