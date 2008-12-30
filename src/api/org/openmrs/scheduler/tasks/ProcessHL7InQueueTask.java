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
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.hl7.HL7InQueueProcessor;

import ca.uhn.hl7v2.HL7Exception;

/**
 * Implementation of a task that process all form entry queues. NOTE: This class does not need to be
 * StatefulTask as we create the context in the constructor.
 * 
 * @version 1.1 1.1 - made processor static to ensure only one HL7 processor runs
 */
public class ProcessHL7InQueueTask extends AbstractTask {
	
	// Logger
	private static Log log = LogFactory.getLog(ProcessHL7InQueueTask.class);
	
	// Instance of hl7 processor
	private static HL7InQueueProcessor processor = null;
	
	/**
	 * Default Constructor (Uses SchedulerConstants.username and SchedulerConstants.password
	 */
	public ProcessHL7InQueueTask() {
		if (processor == null) {
			processor = new HL7InQueueProcessor();
		}
	}
	
	/**
	 * Process the next form entry in the database and then remove the form entry from the database.
	 */
	public void execute() {
		Context.openSession();
		try {
			log.debug("Processing HL7 queue ... ");
			if (!Context.isAuthenticated()) {
				authenticate();
			}
			processor.processHL7InQueue();
		}
		catch (HL7Exception e) {
			log.error("Error running hl7 in queue task", e);
			throw new APIException("Error running hl7 error queue task", e);
		}
		finally {
			Context.closeSession();
		}
	}
	
}
