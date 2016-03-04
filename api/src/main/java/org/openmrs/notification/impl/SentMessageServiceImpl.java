/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.notification.impl;

import java.util.List;

import org.openmrs.User;
import org.openmrs.api.APIException;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.notification.SentMessage;
import org.openmrs.notification.SentMessageService;
import org.openmrs.notification.db.SentMessageDAO;

/**
 * default implementation for SentMessageService
 * @see org.openmrs.notification.SentMessageService
 */
public class SentMessageServiceImpl extends BaseOpenmrsService implements SentMessageService {

	private SentMessageDAO dao;
	
	/**
	 * @see org.openmrs.notification.SentMessageService#setSentMessageDAO(SentMessageDAO)
	 */
	@Override
	public void setSentMessageDAO(SentMessageDAO dao) {
		this.dao = dao;
	}

	/**
	 * @see org.openmrs.notification.SentMessageService#saveSentMessage(SentMessage)
	 */
	@Override
	public SentMessage saveSentMessage(SentMessage sentMessage) throws APIException {
		return dao.saveSentMessage(sentMessage);
	}

	/**
	 * @see org.openmrs.notification.SentMessageService#getSentMessage(Integer)
	 */
	@Override
	public SentMessage getSentMessage(Integer messageId) throws APIException {
		return dao.getSentMessage(messageId);
	}

	/**
	 * @see org.openmrs.notification.SentMessageService#getSentMessages(User)
	 */
	@Override
	public List<SentMessage> getSentMessages(User user) throws APIException {
		return dao.getSentMessages(user);
	}

	/**
	 * @see org.openmrs.notification.SentMessageService#purgeSentMessage(SentMessage)
	 */
	@Override
	public void purgeSentMessage(SentMessage sentMessage) throws APIException {
		dao.deleteSentMessage(sentMessage);
	}

	/**
	 * @see org.openmrs.notification.SentMessageService#getAllSentMessages()
	 */
	@Override
	public List<SentMessage> getAllSentMessages() throws APIException {
		return dao.getAllSentMessages();
	}
}
