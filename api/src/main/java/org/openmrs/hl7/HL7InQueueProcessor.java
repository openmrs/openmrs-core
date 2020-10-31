/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.hl7;

import org.openmrs.api.context.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import ca.uhn.hl7v2.HL7Exception;

/**
 * Processes message in the HL7 inbound queue. Messages are moved into either the archive or error
 * table depending on success or failure of the processing. You may, however, set a global property
 * that causes the processor to ignore messages regarding unknown patients from a non-local HL7
 * source. (i.e. those messages neither go to the archive or the error table.)
 *
 * @version 1.0
 */
@Transactional
public class HL7InQueueProcessor /* implements Runnable */{
	
	private static final Logger log = LoggerFactory.getLogger(HL7InQueueProcessor.class);
	
	private static Boolean isRunning = false; // allow only one running

	private static final Object lock = new Object();
	
	private static Integer count = 0;
	
	// processor per JVM
	
	/**
	 * Empty constructor (requires context to be set using <code>setContext(Context)</code> method
	 * before any other calls are made)
	 */
	public HL7InQueueProcessor() {
	}
	
	public static void setCount(Integer count) {
		HL7InQueueProcessor.count = count;
	}
	
	/**
	 * Process a single queue entry from the inbound HL7 queue
	 *
	 * @param hl7InQueue queue entry to be processed
	 */
	public void processHL7InQueue(HL7InQueue hl7InQueue) {
		
		log.debug("Processing HL7 inbound queue (id={} ,key={})", hl7InQueue.getHL7InQueueId(),
		    hl7InQueue.getHL7SourceKey());
		
		try {
			Context.getHL7Service().processHL7InQueue(hl7InQueue);
		}
		catch (HL7Exception e) {
			log.error("Unable to process hl7 in queue", e);
		}
		setCount(count + 1);
		if (count > 25) {
			// clean up memory after processing each queue entry (otherwise, the
			// memory-intensive process may crash or eat up all our memory)
			try {
				Context.getHL7Service().garbageCollect();
			}
			catch (Exception e) {
				log.error("Exception while performing garbagecollect in hl7 inbound processor", e);
			}
		}
		
	}
	
	/**
	 * Transform the next pending HL7 inbound queue entry. If there are no pending items in the
	 * queue, this method simply returns quietly.
	 *
	 * @return true if a queue entry was processed, false if queue was empty
	 */
	public boolean processNextHL7InQueue() {
		boolean entryProcessed = false;
		HL7Service hl7Service = Context.getHL7Service();
		HL7InQueue hl7InQueue = hl7Service.getNextHL7InQueue();
		if (hl7InQueue != null) {
			processHL7InQueue(hl7InQueue);
			entryProcessed = true;
		}
		return entryProcessed;
	}
	
	/**
	 * Starts up a thread to process all existing HL7InQueue entries
	 */
	public void processHL7InQueue() throws HL7Exception {
		synchronized (lock) {
			if (isRunning) {
				log.warn("HL7 processor aborting (another processor already running)");
				return;
			}
			isRunning = true;
		}
		try {
			log.debug("Start processing hl7 in queue");
			while (processNextHL7InQueue()) {
				// loop until queue is empty
			}
			log.debug("Done processing hl7 in queue");
		}
		finally {
			isRunning = false;
		}
	}
	
}
