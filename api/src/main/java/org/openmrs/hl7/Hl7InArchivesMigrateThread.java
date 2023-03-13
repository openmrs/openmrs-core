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

import java.util.HashMap;
import java.util.Map;

import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.UserContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Separate thread to move the hl7 in archives from the database tables to the filesystem. It is
 * highly recommended to start this thread via DWRHL7Service as opposed to calling the thread's
 * start() method to ensure the thread is started after making all the necessary checks.
 */
public class Hl7InArchivesMigrateThread extends Thread {
	
	private static final Logger log = LoggerFactory.getLogger(Hl7InArchivesMigrateThread.class);
	
	/**
	 * Map holds data about the progress of the transfer process, that is numberTransferred and
	 * numberOfFailedTransfers
	 */
	private static Map<String, Integer> progressStatusMap;
	
	/**
	 * number of days to keep when migrating
	 */
	private static Integer daysKept = 365;
	
	/**
	 * Whether or not activity should continue with this thread
	 */
	private static boolean active = false;
	
	/**
	 * User Context to be used for authentication and privilege checks
	 */
	private UserContext userContext;
	
	/**
	 * Flag to keep track of the status of the migration process
	 */
	private static Status transferStatus = Status.NONE;
	
	/**
	 * The different states this thread can be in at a given point during migration
	 */
	public enum Status {
		RUNNING,
		STOPPED,
		COMPLETED,
		ERROR,
		NONE
	}
	
	public static void setProgressStatusMap(Map<String, Integer> progressStatusMap) {
		Hl7InArchivesMigrateThread.progressStatusMap = progressStatusMap;
	}
	
	/**
	 * Constructor to initialize variables
	 */
	public Hl7InArchivesMigrateThread() {
		this.userContext = Context.getUserContext();
		setProgressStatusMap(new HashMap<>());
		progressStatusMap.put(HL7Constants.NUMBER_TRANSFERRED_KEY, 0);
		progressStatusMap.put(HL7Constants.NUMBER_OF_FAILED_TRANSFERS_KEY, 0);
	}
	
	/**
	 * @return the daysKept
	 */
	public static Integer getDaysKept() {
		return daysKept;
	}
	
	/**
	 * @param daysKept the daysKept to set
	 */
	public static void setDaysKept(Integer daysKept) {
		Hl7InArchivesMigrateThread.daysKept = daysKept;
	}
	
	/**
	 * @return the active
	 */
	public static boolean isActive() {
		return active;
	}
	
	/**
	 * @param active the active to set
	 */
	public static void setActive(boolean active) {
		Hl7InArchivesMigrateThread.active = active;
	}
	
	public static void setTransferStatus(Status transferStatus) {
		Hl7InArchivesMigrateThread.transferStatus = transferStatus;
	}
	
	/**
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		
		Context.openSession();
		Context.setUserContext(userContext);
		setTransferStatus(Status.RUNNING);
		
		while (isActive() && transferStatus == Status.RUNNING) {
			try {
				// migrate the archives
				if (isActive()) {
					Context.getHL7Service().migrateHl7InArchivesToFileSystem(progressStatusMap);
				}
				
				//if transfer is done when user didn't just stop it
				if (transferStatus != Status.STOPPED) {
					setTransferStatus(Status.COMPLETED);
				}
				
			}
			catch (APIException api) {
				// log this as a debug, because we want to swallow minor errors 
				log.debug("Unable to migrate HL7 archive", api);
				
				try {
					Thread.sleep(HL7Constants.THREAD_SLEEP_PERIOD);
				}
				catch (InterruptedException e) {
					log.warn("Hl7 in archive migration thread has been abnormally interrupted", e);
				}
				
			}
			catch (Exception e) {
				setTransferStatus(Status.ERROR);
				log.warn("Some error occurred while migrating hl7 archives", e);
			}
		}
		// clean up
		Context.closeSession();
		setActive(false);
	}
	
	/**
	 * convenience method to set transfer status and active flag to stop migration
	 */
	public static void stopMigration() {
		transferStatus = Status.STOPPED;
		setActive(false);
	}
	
	/**
	 * @return the transferStatus
	 */
	public static Status getTransferStatus() {
		return transferStatus;
	}
	
	/**
	 * @return the numberTransferred at a given time during migration
	 */
	public static Integer getNumberTransferred() {
		if (progressStatusMap == null) {
			return 0;
		}
		return progressStatusMap.get(HL7Constants.NUMBER_TRANSFERRED_KEY);
	}
	
	/**
	 * Gets the number of failed transfers during migration, this could be that the system couldn't
	 * write them to the file system or couldn't be deleted from the database.
	 *
	 * @return the numberOfFailedTransfers
	 */
	public static Integer getNumberOfFailedTransfers() {
		if (progressStatusMap == null) {
			return 0;
		}
		return progressStatusMap.get(HL7Constants.NUMBER_OF_FAILED_TRANSFERS_KEY);
	}
	
	/**
	 * @return the userContext
	 */
	public UserContext getUserContext() {
		return this.userContext;
	}
	
}
