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
package org.openmrs.web.dwr;

import java.util.HashMap;
import java.util.Map;

import org.openmrs.api.APIAuthenticationException;
import org.openmrs.api.context.Context;
import org.openmrs.hl7.Hl7InArchivesMigrateThread;
import org.openmrs.hl7.Hl7InArchivesMigrateThread.Status;

/**
 * DWR archive migration methods. The methods in here are used in the webapp to start and stop the
 * hl7 archive migration process via javascript calls. Also status messages are read from the server
 * to be displayed to the user in the browser via ajax
 */
public class DWRHL7Service {
	
	private static Hl7InArchivesMigrateThread hl7MigrationThread = null;
	
	/**
	 * Handles the ajax call for starting the migration of hl7 in archives to the file system
	 * 
	 * @return an object array with a boolean value at index 0 indicating if the migration was
	 *         started or not, at the second index is an optional descriptive message.
	 */
	public Object[] startHl7ArchiveMigration(Integer daysToKeep) {
		if (Hl7InArchivesMigrateThread.isActive())
			return new Object[] { false,
			        Context.getMessageSourceService().getMessage("Hl7InArchive.migrate.already.running") };
		
		try {
			// create a new thread and get it started
			Hl7InArchivesMigrateThread.setDaysKept(daysToKeep);
			Hl7InArchivesMigrateThread.setActive(true);
			hl7MigrationThread = new Hl7InArchivesMigrateThread();
			hl7MigrationThread.setName("HL7 Archive Migration Thread");
			hl7MigrationThread.start();
			return new Object[] { true };
		}
		catch (APIAuthenticationException e) {
			return new Object[] { false,
			        Context.getMessageSourceService().getMessage("Hl7InArchive.migrate.authentication.fail") };
		}
	}
	
	/**
	 * Handles the ajax call to stop hl7 migration process
	 * 
	 * @return a descriptive message
	 */
	public String stopHl7ArchiveMigration() {
		Hl7InArchivesMigrateThread.stopMigration();
		hl7MigrationThread = null;
		return Context.getMessageSourceService().getMessage("Hl7InArchive.migrate.stop.success");
	}
	
	/**
	 * Processes the ajax call for retrieving the progress and status
	 * 
	 * @return a map containing the number migrated, the state of the migrate thread at a given time
	 *         when it is running and a message string.
	 */
	public Map<String, Object> getMigrationStatus() {
		Map<String, Object> statusMap = new HashMap<String, Object>();
		statusMap.put("numberMigrated", Hl7InArchivesMigrateThread.getNumberTransferred());
		Status status = Hl7InArchivesMigrateThread.getTransferStatus();
		statusMap.put("status", status.toString());
		
		if (status == Status.COMPLETED) {
			if (Hl7InArchivesMigrateThread.getNumberOfFailedTransfers() > 0)
				statusMap.put("areAllTransferred", false);
			else
				statusMap.put("areAllTransferred", true);
		}
		
		return statusMap;
	}
	
}
