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
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.ContextAuthenticationException;
import org.openmrs.scheduler.TaskDefinition;

/**
 *  Implementation of the stateful task that sends an email.
 *
 */
public class OpenmrsTestTask extends AbstractTask { 

	// Logger 
	private Log log = LogFactory.getLog( OpenmrsTestTask.class );
	
	/**
	 * @see org.openmrs.scheduler.tasks.AbstractTask#initialize(org.openmrs.scheduler.TaskConfig)
	 */
	public void initialize(TaskDefinition config) { 
		log.info("Initializing task ...");
	} 
	
	/**
	 * @see org.openmrs.scheduler.tasks.AbstractTask#execute()
	 */
	public void execute() {		
		log.info("Running task ...");
		if (!Context.isAuthenticated()) { 
			log.info("Authenticating ...");
			authenticate();
		}
		
	}


	/**
	 * @see org.openmrs.scheduler.tasks.AbstractTask#shutdown()
	 */
    public void shutdown() {
    	log.info("Shutting down task ...");
    	super.shutdown();
    }
	
	
}
