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

import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.hl7.HL7InQueueProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.hl7v2.HL7Exception;

/**
 * Implementation of a task that process all form entry queues. NOTE: This class does not need to be
 * StatefulTask as we create the context in the constructor.
 * 
 * @version 1.1 1.1 - made processor static to ensure only one HL7 processor runs
 */
public class ProcessHL7InQueueTask extends AbstractTask {
	
	// Logger
	private static final Logger log = LoggerFactory.getLogger(ProcessHL7InQueueTask.class);
	
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
	@Override
	public void execute() {
		Context.openSession();
		try {
			log.debug("Processing HL7 queue ... ");
			processor.processHL7InQueue();
		}
		catch (HL7Exception e) {
			log.error("Error running hl7 in queue task", e);
			throw new APIException("Hl7inQueue.error.running", null, e);
		}
		finally {
			Context.closeSession();
		}
	}
	
}
