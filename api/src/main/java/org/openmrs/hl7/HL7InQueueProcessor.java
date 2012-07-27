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
package org.openmrs.hl7;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
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
	
	private final Log log = LogFactory.getLog(this.getClass());
	
	private static Boolean isRunning = false; // allow only one running
	
	private static Integer count = 0;
	
	// processor per JVM
	
	/**
	 * Empty constructor (requires context to be set using <code>setContext(Context)</code> method
	 * before any other calls are made)
	 */
	public HL7InQueueProcessor() {
	}
	
	/**
	 * Process a single queue entry from the inbound HL7 queue
	 * 
	 * @param hl7InQueue queue entry to be processed
	 */
	public void processHL7InQueue(HL7InQueue hl7InQueue) {
		
		if (log.isDebugEnabled())
			log.debug("Processing HL7 inbound queue (id=" + hl7InQueue.getHL7InQueueId() + ",key="
			        + hl7InQueue.getHL7SourceKey() + ")");
		
		try {
			Context.getHL7Service().processHL7InQueue(hl7InQueue);
		}
		catch (HL7Exception e) {
			log.error("Unable to process hl7 in queue", e);
		}
		
		if (++count > 25) {
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
		synchronized (isRunning) {
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
