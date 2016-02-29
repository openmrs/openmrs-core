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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.APIException;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.notification.SentMessage;
import org.openmrs.notification.SentMessageService;
import org.openmrs.notification.db.SentMessageDAO;
import org.springframework.transaction.annotation.Transactional;

/**
 * Default implementation of the sentMessage service. This class should not be used on its own. The
 * current OpenMRS implementation should be fetched from the Context via
 * <code>Context.getSentMessageService()</code>
 * 
 * @see org.openmrs.api.context.Context
 * @see org.openmrs.notification.SentMessageService
 */
@Transactional
public class SentMessageServiceImpl extends BaseOpenmrsService implements SentMessageService {

	private Log log = LogFactory.getLog(this.getClass());
	
	private SentMessageDAO dao;
	
	/**
	 * Default constructor
	 */
	public SentMessageServiceImpl() {}
	
	/**
	 * @see org.openmrs.notification.SentMessageService#setSentMessageDAO(org.openmrs.notification.db.SentMessageDAO)
	 */
	@Override
	public void setSentMessageDAO(SentMessageDAO dao) {
		this.dao = dao;
	}

	/**
	 * @see org.openmrs.notification.SentMessageService#saveSentMessage(org.openmrs.notification.SentMessage)
	 */
	@Override
	public SentMessage saveSentMessage(SentMessage sentMessage) throws APIException {
		return dao.saveSentMessage(sentMessage);
	}

	/**
	 * @see org.openmrs.notification.SentMessageService#getSentMessage(java.lang.Integer)
	 */
	@Override
	public SentMessage getSentMessage(Integer sentMessageId) throws APIException {
		return dao.getSentMessage(sentMessageId);
	}

	/**
	 * @see org.openmrs.notification.SentMessageService#purgeSentMessage(org.openmrs.notification.SentMessage)
	 */
	@Override
	public void purgeSentMessage(SentMessage sentMessage) throws APIException {
		dao.deleteSentMessage(sentMessage);
	}

	/**
	 * @see org.openmrs.notification.SentMessageService#getAllSentMessage()
	 */
	@Override
	public List<SentMessage> getAllSentMessages() throws APIException {
		return dao.getAllSentMessages();
	}
}
