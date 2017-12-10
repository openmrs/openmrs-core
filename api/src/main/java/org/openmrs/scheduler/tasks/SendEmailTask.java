/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.scheduler.tasks;

import org.openmrs.api.context.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the stateful task that sends an email.
 */
public class SendEmailTask extends AbstractTask {
	
	// Logger 
	private static final Logger log = LoggerFactory.getLogger(SendEmailTask.class);
	
	/**
	 * Process the next form entry in the database and then remove the form entry from the database.
	 */
	@Override
	public void execute() {
		try {
			Context.openSession();
			log.info("****************************** SEND EMAIL TASK:  Executing task ...");
		}
		finally {
			Context.closeSession();
		}
		
	}
	
	/**
	 * @see org.openmrs.scheduler.tasks.AbstractTask#shutdown()
	 */
	@Override
	public void shutdown() {
		log.info("****************************** SEND EMAIL TASK:  Shutting down task ...");
	}
	
}
