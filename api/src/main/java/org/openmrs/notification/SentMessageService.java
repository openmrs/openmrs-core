/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.notification;

import java.util.List;

import org.openmrs.api.APIException;
import org.openmrs.api.OpenmrsService;
import org.openmrs.notification.db.SentMessageDAO;

public interface SentMessageService extends OpenmrsService {
	
	/**
	 * Used by Spring to set the specific/chosen database access implementation
	 * 
	 * @param dao The dao implementation to use
	 */
	public void setSentMessageDAO(SentMessageDAO dao);
	
	/**
	 * Save the given <code>sentMessage</code> in the database
	 * 
	 * @param sentMessage the SentMessage object to save
	 * @return The saved sentMessage object
	 * @throws APIException
	 */
	public SentMessage saveSentMessage(SentMessage sentMessage) throws APIException;
	
	/**
	 * Get sentMessage by internal identifier
	 * 
	 * @param sentMessageId internal sentMessage identifier
	 * @return sentMessage with given internal identifier
	 * @throws APIException
	 */
	public SentMessage getSentMessage(Integer sentMessageId) throws APIException;
	
	/**
	 * Completely delete the given sentMessage from the database
	 * 
	 * @param sentMessage the SentMessage to purge/delete
	 * @throws APIException
	 */
	public void purgeSentMessage(SentMessage sentMessage) throws APIException;
	
	/**
	 * Get all sentMessages for all users
	 * 
	 * @return list of sentMessages
	 * @throws APIException
	 */
	public List<SentMessage> getAllSentMessages() throws APIException;
}