/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
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
	
	public static void setHl7MigrationThread(Hl7InArchivesMigrateThread hl7MigrationThread) {
		DWRHL7Service.hl7MigrationThread = hl7MigrationThread;
	}
	
	/**
	 * Handles the ajax call for starting the migration of hl7 in archives to the file system
	 *
	 * @return an object array with a boolean value at index 0 indicating if the migration was
	 *         started or not, at the second index is an optional descriptive message.
	 */
	public Object[] startHl7ArchiveMigration(Integer daysToKeep) {
		if (Hl7InArchivesMigrateThread.isActive()) {
			return new Object[] { false,
			        Context.getMessageSourceService().getMessage("Hl7InArchive.migrate.already.running") };
		}
		
		try {
			// create a new thread and get it started
			Hl7InArchivesMigrateThread.setDaysKept(daysToKeep);
			Hl7InArchivesMigrateThread.setActive(true);
			setHl7MigrationThread(new Hl7InArchivesMigrateThread());
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
		setHl7MigrationThread(null);
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
			if (Hl7InArchivesMigrateThread.getNumberOfFailedTransfers() > 0) {
				statusMap.put("areAllTransferred", false);
			} else {
				statusMap.put("areAllTransferred", true);
			}
		}
		
		return statusMap;
	}
	
}
