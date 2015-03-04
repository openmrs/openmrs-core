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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;

/**
 * Implementation of the stateful task that sends an email.
 */
public class SendEmailTask extends AbstractTask {
	
	// Logger 
	private Log log = LogFactory.getLog(SendEmailTask.class);
	
	/**
	 * Process the next form entry in the database and then remove the form entry from the database.
	 */
	public void execute() {
		try {
			Context.openSession();
			log.info("****************************** SEND EMAIL TASK:  Executing task ...");
			if (!Context.isAuthenticated()) {
				authenticate();
			}
			
		}
		finally {
			Context.closeSession();
		}
		
	}
	
	/**
	 * @see org.openmrs.scheduler.tasks.AbstractTask#shutdown()
	 */
	public void shutdown() {
		log.info("****************************** SEND EMAIL TASK:  Shutting down task ...");
	}
	
}
