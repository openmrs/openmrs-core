/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.notification.db;

import java.util.List;

import org.openmrs.api.db.DAOException;
import org.openmrs.notification.SentMessage;

public interface SentMessageDAO {
	
	/**
	 * @see org.openmrs.notification.SentMessageService#saveSentMessage(org.openmrs.notification.SentMessage)
	 */
	public SentMessage saveSentMessage(SentMessage sentMessage) throws DAOException;
	
	/**
	 * @see org.openmrs.notification.SentMessageService#getSentMessage(Integer)
	 */
	public SentMessage getSentMessage(Integer sentMessageId) throws DAOException;

	/**
	 * @see org.openmrs.notification.SentMessageService#purgeSentMessage(org.openmrs.notification.SentMessage)
	 */
	public void deleteSentMessage(SentMessage sentMessage) throws DAOException;

	/**
	 * @see org.openmrs.notification.SentMessageService#getSentMessages()
	 */
	public List<SentMessage> getAllSentMessages();
}
