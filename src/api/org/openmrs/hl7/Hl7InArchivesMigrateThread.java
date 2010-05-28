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

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.APIAuthenticationException;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.UserContext;

/**
 * Separate thread to move the hl7 in archives from the database tables to the filesystem. It is
 * highly recommended to start this thread by calling "startHl7ArchiveMigration(UserContext)" method
 * in the service layer as opposed to calling the thread's start() method to ensure the thread is
 * started after making all the necessary checks.
 * 
 * @see {@link HL7Service#startHl7ArchiveMigration()}
 */
public class Hl7InArchivesMigrateThread extends Thread {
	
	private static final Log log = LogFactory.getLog(Hl7InArchivesMigrateThread.class);
	
	//Map holds data about the progress of the transfer process, that is numberTransferred and numberOfFailedTransfers
	private static Map<String, Integer> progressStatusMap;
	
	/**
	 * The different states this thread can be in at a given point during migration
	 */
	public enum Status {
		RUNNING, STOPPED, COMPLETED, ERROR, NONE
	}
	
	//a flag to keep track of the status of the migration process
	private static Status transferStatus = Status.NONE;
	
	private static Hl7InArchivesMigrateThread hl7InArchivesMigrateThread;
	
	/**
	 * User Context to be used for authentication and privilege checks
	 */
	private UserContext userContext;
	
	/**
	 * Private constructor prevents instantiation from other classes
	 */
	private Hl7InArchivesMigrateThread() {
		this.userContext = Context.getUserContext();
	}
	
	/**
	 * Create an instance of the migration thread and starts it, calling this method is the
	 * preferred way to start the thread instead of calling thread.start()
	 * 
	 * @return true is the migration successfully started otherwise false
	 */
	public synchronized static boolean startArchiveMigration() {
		
		//if no other user is running migration
		if (transferStatus == Status.NONE) {
			hl7InArchivesMigrateThread = new Hl7InArchivesMigrateThread();
			progressStatusMap = new HashMap<String, Integer>();
			progressStatusMap.put(HL7Constants.NUMBER_TRANSFERRED_KEY, 0);
			progressStatusMap.put(HL7Constants.NUMBER_OF_FAILED_TRANSFERS_KEY, 0);
			hl7InArchivesMigrateThread.start();
			//flag to block others from running until we are done
			transferStatus = Status.RUNNING;
			if (log.isDebugEnabled())
				log.debug("Hl7 migration has been started");
			
			return true;
		}
		
		if (log.isDebugEnabled())
			log.debug("Someone else is already running the hl7 in archive migration...");
		return false;
		
	}
	
	/**
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		
		try {
			Context.openSession();
			//if user's session expired or isn't yet logged in
			Context.setUserContext(userContext);
			Context.getHL7Service().migrateHl7InArchivesToFileSystem(progressStatusMap);
			
			//if transfer is done when user didn't just stop it
			if (transferStatus != Status.STOPPED)
				transferStatus = Status.COMPLETED;
			//user stopped the process
			else if (transferStatus == Status.STOPPED)
				transferStatus = Status.STOPPED;
		}
		catch (Exception e) {
			transferStatus = Status.ERROR;
			log.warn("Some error occurred while migrating hl7 archives", e);
		}
		finally {
			//do the clean up process
			resetStateProperties();
			Context.closeSession();
		}
		
	}
	
	/**
	 * @return the hl7InArchivesMigrateThread
	 */
	public static Hl7InArchivesMigrateThread getHl7InArchivesMigrateThread() {
		return hl7InArchivesMigrateThread;
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
		return progressStatusMap.get(HL7Constants.NUMBER_TRANSFERRED_KEY);
	}
	
	/**
	 * Gets the number of failed transfers during migration, this could be that the system couldn't
	 * write them to the file system or couldn't be deleted from the database.
	 * 
	 * @return the numberOfFailedTransfers
	 */
	public static Integer getNumberOfFailedTransfers() {
		return progressStatusMap.get(HL7Constants.NUMBER_OF_FAILED_TRANSFERS_KEY);
	}
	
	/**
	 * @return the userContext
	 */
	public UserContext getUserContext() {
		return this.userContext;
	}
	
	/**
	 * Method can be called externally from other classes to stop the migration process, It lets the
	 * user to stop it at anytime.
	 */
	public void stopArchiveMigration() throws APIAuthenticationException {
		
		//if the migration is in progress
		if (transferStatus == Status.RUNNING)
			transferStatus = Status.STOPPED;
		
	}
	
	/**
	 * Method resets state related properties for the next run of the migration process, Calling
	 * this method basically brings the migration process to a complete stop.
	 */
	private void resetStateProperties() {
		
		try {
			//since we expect calls to monitor the status, wait for the
			//next call to notify the user that the process has successfully completed
			//before the status is updated to Status.NONE
			Thread.sleep(HL7Constants.THREAD_SLEEP_PERIOD);
			
		}
		catch (InterruptedException e) {
			log.warn("Hl7 in archive migration thread has been abnormally interrupted", e);
		}
		finally {
			progressStatusMap = null;
			if (transferStatus != Status.NONE)
				transferStatus = Status.NONE;
			
			if (hl7InArchivesMigrateThread != null)
				hl7InArchivesMigrateThread = null;
			
			if (log.isDebugEnabled())
				log.debug("Migration has come to a stop successfully..");
		}
		
	}
}
