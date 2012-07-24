/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
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

        } finally {
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
