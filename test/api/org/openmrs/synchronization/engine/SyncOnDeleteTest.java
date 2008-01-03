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
package org.openmrs.synchronization.engine;

import java.util.List;

import org.openmrs.BaseContextSensitiveTest;
import org.openmrs.PatientIdentifierType;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.PatientService;
import org.openmrs.api.SynchronizationIngestService;
import org.openmrs.api.SynchronizationService;
import org.openmrs.api.context.Context;
import org.openmrs.synchronization.server.RemoteServer;

/**
 * Testing of delete methods and whether that action is synchronized
 */
public class SyncOnDeleteTest extends BaseContextSensitiveTest {

    /**
	 * Tests that the deletion of an identifier type cascades through the 
	 * sync process
	 * 
	 * @throws Exception
	 */
	public void testDeleteIdentfierType() throws Exception {
		initializeInMemoryDatabase();
		authenticate();
		
		SynchronizationIngestService syncIngestService = Context.getSynchronizationIngestService();
		SynchronizationService syncService = Context.getSynchronizationService();
		PatientService patientService = Context.getPatientService();
		
		// set up the initial "child" server and its database 
		executeDataSet("org/openmrs/synchronization/engine/include/SyncOnDeleteTest-createPatientIdentifierType.xml");
		
		// make sure the patient identifier type is there
		PatientIdentifierType pit = patientService.getPatientIdentifierType(1);
		assertNotNull("The patient identifier type could not be found in child server!", pit);
		
		// do the deleting
		AdministrationService adminService = Context.getAdministrationService();
		adminService.deletePatientIdentifierType(pit);
		
		pit = patientService.getPatientIdentifierType(1);
		assertNull("The patient identifier type should have been deleted!", pit);
		
		// save the sync records to be "sent" to the remote server
		List<SyncRecord> syncRecords = syncService.getSyncRecords();
		
		//
		// Now for the "parent server" part
		//
		// clear out the server
		deleteAllData();
		
		// set up the database how it was on the child server so as to mimic that
		// the parent and child contained the same data
		initializeInMemoryDatabase();
		executeDataSet("org/openmrs/synchronization/engine/include/SyncOnDeleteTest-createPatientIdentifierType.xml");
		
		// this is new to the parent server db. it needs to know about the child server 
		executeDataSet("org/openmrs/synchronization/engine/include/SyncRemoteChildServer.xml");
		RemoteServer origin = syncService.getRemoteServer(1);
		
		// make sure the type is there
		pit = patientService.getPatientIdentifierType(1);
		assertNotNull("The patient identifier type could not be found when in parent server!", pit);
		
		// "receive" sync records from the child server
		for (SyncRecord syncRecord : syncRecords) {
			syncIngestService.processSyncRecord(syncRecord, origin);
		}
		
		// make sure it was deleted by sync
		pit = patientService.getPatientIdentifierType(1);
		assertNull("The patient identifier type should have been deleted!", pit);
		
	}
	
}
