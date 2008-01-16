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

import java.util.Date;
import java.util.List;

import org.openmrs.BaseContextSensitiveTest;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PersonName;
import org.openmrs.Privilege;
import org.openmrs.Role;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.synchronization.server.RemoteServer;

/**
 * Tests creating various pieces of data via synchronization
 */
public class SyncCreateTest extends BaseContextSensitiveTest {
	
	protected void setupSyncTestChild() throws Exception {
		initializeInMemoryDatabase();
		authenticate();
		executeDataSet("org/openmrs/synchronization/engine/include/SyncCreateTest.xml");
	}
	
	protected void setupSyncTestParent() throws Exception {
		deleteAllData();
		initializeInMemoryDatabase();
		executeDataSet("org/openmrs/synchronization/engine/include/SyncCreateTest.xml");
		executeDataSet("org/openmrs/synchronization/engine/include/SyncRemoteChildServer.xml");
	}
	
	public void runSyncTest(SyncTestHelper testMethods) throws Exception {
		Context.openSession();
		
		initializeInMemoryDatabase();
		executeDataSet("org/openmrs/synchronization/engine/include/SyncCreateTest.xml");
		authenticate();

		testMethods.runOnChild();
		
		this.transactionManager.commit(this.transactionStatus);
		Context.closeSession();
		Context.openSession();

		List<SyncRecord> syncRecords = Context.getSynchronizationService().getSyncRecords();
		if (syncRecords == null || syncRecords.size() == 0)
			assertFalse("No changes found (i.e. sync records size is 0)", true);
		
		deleteAllData();
		initializeInMemoryDatabase();
		executeDataSet("org/openmrs/synchronization/engine/include/SyncCreateTest.xml");
		executeDataSet("org/openmrs/synchronization/engine/include/SyncRemoteChildServer.xml");
		RemoteServer origin = Context.getSynchronizationService().getRemoteServer(1);
		for (SyncRecord syncRecord : syncRecords) {
			Context.getSynchronizationIngestService().processSyncRecord(syncRecord, origin);
		}
		
		testMethods.runOnParent();
		Context.closeSession();
	}
	
	public void testCreateRoleAndPrivilege() throws Exception {
		long l = System.currentTimeMillis();
		runSyncTest(new SyncTestHelper() {
			public void runOnChild() {
				Privilege priv = new Privilege("Kitchen Use");
				priv.setDescription("Can step into the kitchen");
				Context.getAdministrationService().createPrivilege(priv);
				Role role = new Role("Chef");
				role.setDescription("One who cooks");
				role.addPrivilege(priv);
				Context.getAdministrationService().createRole(role);
			}
			public void runOnParent() {
				Privilege priv = Context.getUserService().getPrivilege("Kitchen Use");
				assertEquals("Privilege failed", "Can step into the kitchen", priv.getDescription());
				Role role = Context.getUserService().getRole("Chef");
				assertEquals("Role failed", "One who cooks", role.getDescription());
			}
		});
		System.out.println("took " + (System.currentTimeMillis() - l) + " ms");
	}

	public void testCreatePatient() throws Exception {
		long l = System.currentTimeMillis();
		runSyncTest(new SyncTestHelper() {
			public void runOnChild() {
				Location loc = Context.getEncounterService().getLocationByName("Someplace");
				PatientIdentifierType pit = Context.getPatientService().getPatientIdentifierType(2);
				if (pit.getGuid() == null)
					throw new RuntimeException("pit.guid is null! " + pit);
				else
					System.out.println("pit.guid = " + pit.getGuid() + " , pit = " + pit);
				Patient p = new Patient();
				p.addName(new PersonName("Darius", "Graham", "Jazayeri"));
				p.addIdentifier(new PatientIdentifier("999", pit, loc));
				p.setGender("m");
				p.setBirthdate(new Date());
				Context.getPatientService().createPatient(p);
				List<PatientIdentifier> ids = Context.getPatientService().getPatientIdentifiers("999", pit);
				assertNotNull(ids);
				if (ids.size() != 1)
					assertFalse("Can't find patient we just created. ids.size()==" + ids.size(), true);
				System.out.println("Patients at end " + Context.getPatientService().findPatients("Darius", true).size());
			}
			public void runOnParent() {
				System.out.println("Patients at beginning " + Context.getPatientService().findPatients("Darius", true).size());
				Location loc = Context.getEncounterService().getLocationByName("Someplace");
				PatientIdentifierType pit = Context.getPatientService().getPatientIdentifierType(2);
				PersonName name = new PersonName("Darius", "Graham", "Jazayeri");
				PatientIdentifier id = new PatientIdentifier("999", pit, loc);

				List<PatientIdentifier> ids = Context.getPatientService().getPatientIdentifiers("999", pit);
				assertNotNull(ids);
				if (ids.size() != 1)
					assertFalse("Should only find one patient, not " + ids.size(), true);
				Patient p = ids.get(0).getPatient();				
				assertEquals(p.getPersonName(), name);
				assertEquals(p.getIdentifiers().iterator().next(), id);
			}
		});
		System.out.println("took " + (System.currentTimeMillis() - l) + " ms");
	}

}
