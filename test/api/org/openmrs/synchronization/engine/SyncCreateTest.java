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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.openmrs.BaseContextSensitiveTest;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PatientProgram;
import org.openmrs.PersonName;
import org.openmrs.Privilege;
import org.openmrs.Program;
import org.openmrs.ProgramWorkflow;
import org.openmrs.ProgramWorkflowState;
import org.openmrs.Role;
import org.openmrs.User;
import org.openmrs.api.ConceptService;
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
	
	public void testCreateProgram() throws Exception {
		runSyncTest(new SyncTestHelper() {
			int numBefore = 0;
			public void runOnChild() {
				numBefore = Context.getProgramWorkflowService().getPrograms().size();
				ConceptService cs = Context.getConceptService();
				Concept tbProgram = cs.getConceptByName("TB PROGRAM");
				Concept txStatus = cs.getConceptByName("TREATMENT STATUS");
				Concept following = cs.getConceptByName("FOLLOWING");
				Concept cured = cs.getConceptByName("PATIENT CURED");

				Program prog = new Program();
				prog.setConcept(tbProgram);
				Context.getProgramWorkflowService().createOrUpdateProgram(prog);
				
				ProgramWorkflowState followState = new ProgramWorkflowState();
				followState.setConcept(following);
				followState.setInitial(true);
				followState.setTerminal(false);
				ProgramWorkflowState cureState = new ProgramWorkflowState();
				cureState.setConcept(cured);
				cureState.setInitial(false);
				cureState.setTerminal(true);
				ProgramWorkflow wf = new ProgramWorkflow();
				wf.setConcept(txStatus);
				wf.addState(followState);
				wf.addState(cureState);
				wf.setProgram(prog);
				Context.getProgramWorkflowService().createWorkflow(wf);
			}
			public void runOnParent() {
				assertEquals("Failed to create program",
				             numBefore + 1,
				             Context.getProgramWorkflowService().getPrograms().size());
				Program p = Context.getProgramWorkflowService().getProgram("TB PROGRAM");
				assertEquals("Wrong number of workflows", p.getWorkflows().size(), 1);

				ProgramWorkflow wf = p.getWorkflowByName("TREATMENT STATUS");
				assertNotNull(wf);
				List<String> names = new ArrayList<String>();
				for (ProgramWorkflowState s : wf.getStates())
					names.add(s.getConcept().getName().getName());
				assertEquals("Wrong number of states", names.size(), 2);
				names.remove("FOLLOWING");
				names.remove("PATIENT CURED");
				assertEquals("States have wrong names", names.size(), 0);
			}
		});
	}
	
	public void testEnrollInProgram() throws Exception {
		runSyncTest(new SyncTestHelper() {
			int numberEnrolledBefore = 0;
			Date dateEnrolled = new Date();
			Program hivProgram = null;
			public void runOnChild() {
				Patient p = Context.getPatientService().getPatient(2);
				numberEnrolledBefore = Context.getProgramWorkflowService().getPatientPrograms(p).size();
				hivProgram = Context.getProgramWorkflowService().getProgram("HIV PROGRAM");
				PatientProgram pp = Context.getProgramWorkflowService().enrollPatientInProgram(p, hivProgram, dateEnrolled, null);
			}
			public void runOnParent() {
				Patient p = Context.getPatientService().getPatient(2);
				assertEquals("Enrollment failed",
				             numberEnrolledBefore + 1,
				             Context.getProgramWorkflowService().getPatientPrograms(p).size());
				for (PatientProgram pp : Context.getProgramWorkflowService().getPatientPrograms(p)) {
					if (pp.getProgram().equals(hivProgram)) {
						assertEquals("Wrong enrollment date", pp.getDateEnrolled(), dateEnrolled);
						assertEquals("Wrong completion date", pp.getDateCompleted(), null);					}
				}
			}
		});
	}
	
	public void testEnrollInProgramAndState() throws Exception {
		runSyncTest(new SyncTestHelper() {
			int numberEnrolledBefore = 0;
			Date dateEnrolled = new Date();
			Program hivProgram = null;
			ProgramWorkflow txStat = null;
			ProgramWorkflowState curedState = null;
			public void runOnChild() {
				Patient p = Context.getPatientService().getPatient(2);
				numberEnrolledBefore = Context.getProgramWorkflowService().getPatientPrograms(p).size();
				hivProgram = Context.getProgramWorkflowService().getProgram("HIV PROGRAM");
				txStat = hivProgram.getWorkflowByName("TREATMENT STATUS");
				curedState = txStat.getStateByName("PATIENT CURED");
				PatientProgram pp = Context.getProgramWorkflowService().enrollPatientInProgram(p, hivProgram, dateEnrolled, null);
				Context.getProgramWorkflowService().changeToState(pp, txStat, curedState, dateEnrolled);
			}
			public void runOnParent() {
				Patient p = Context.getPatientService().getPatient(2);
				assertEquals("Enrollment failed",
				             numberEnrolledBefore + 1,
				             Context.getProgramWorkflowService().getPatientPrograms(p).size());
				for (PatientProgram pp : Context.getProgramWorkflowService().getPatientPrograms(p)) {
					if (pp.getProgram().equals(hivProgram)) {
						assertEquals("Wrong enrollment date", pp.getDateEnrolled(), dateEnrolled);
						assertEquals("Wrong completion date", pp.getDateCompleted(), null);
						assertEquals("Wrong state", pp.getCurrentState(txStat), curedState);
					}
				}
			}
		});
	}
	
	public void testCreateEncounter() throws Exception {
		runSyncTest(new SyncTestHelper() {
			int numEncountersSoFar = 0;
			Date dateOfNewEncounter = new Date();
			Concept weight = null;
			Concept reason = null;
			Concept other = null;
			Location loc = null;
			public void runOnChild() {
				ConceptService cs = Context.getConceptService();
				weight = cs.getConceptByName("WEIGHT");
				reason = cs.getConceptByName("REASON ORDER STOPPED");
				other = cs.getConceptByName("OTHER NON-CODED");
				loc = Context.getEncounterService().getLocationByName("Someplace");
				User u = Context.getUserService().getUser(1);
				Patient p = Context.getPatientService().getPatient(2);
				numEncountersSoFar = Context.getEncounterService().getEncounters(p).size();
				
				Encounter enc = new Encounter();
				enc.setPatient(p);
				enc.setLocation(loc);
				enc.setProvider(u);
				enc.setEncounterDatetime(dateOfNewEncounter);
				Obs o1 = new Obs();
				o1.setConcept(weight);
				o1.setValueNumeric(74.0);
				o1.setObsDatetime(dateOfNewEncounter);
				Obs o2 = new Obs();
				o2.setConcept(reason);
				o2.setValueCoded(other);
				o2.setObsDatetime(dateOfNewEncounter);
				enc.addObs(o1);
				enc.addObs(o2);
				Context.getEncounterService().createEncounter(enc);
			}
			
			public void runOnParent() {
				Patient p = Context.getPatientService().getPatient(2);
				Set<Encounter> encs = Context.getEncounterService().getEncounters(p);
				assertEquals("Should now have one more encounter than before",
				             numEncountersSoFar + 1,
				             encs.size());
				Encounter lookAt = null;
				for (Encounter e : encs) {
					if (e.getEncounterDatetime().equals(dateOfNewEncounter)) {
						lookAt = e;
						break;
					}
				}
				assertEquals(lookAt.getLocation(), loc);
				assertEquals("Should have two obs", lookAt.getObs().size(), 2);
				for (Obs o : lookAt.getObs()) {
					if (o.getConcept().equals(weight)) {
						assertEquals("Weight should be 74.0", o.getValueNumeric(), 74.0);
					} else {
						assertEquals("Reason should be OTHER NON-CODED", o.getValueCoded(), other);
					}
				}
			}
		});
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
