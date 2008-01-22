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
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

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
import org.openmrs.util.OpenmrsUtil;

/**
 * Tests creating various pieces of data via synchronization
 */
public class SyncPatientTest extends SyncBaseTest {
	
	@Override
    public String getInitialDataset() {
	    return "org/openmrs/synchronization/engine/include/SyncCreateTest.xml";
    }
	
	
	public void testEnrollInProgram() throws Exception {
		runSyncTest(new SyncTestHelper() {
			int numberEnrolledBefore = 0;
			Date dateEnrolled = new Date(System.currentTimeMillis() - 100000);
			Date dateCompleted = new Date(System.currentTimeMillis() - 10000);
			Program hivProgram = null;
			public void runOnChild() {
				Patient p = Context.getPatientService().getPatient(2);
				numberEnrolledBefore = Context.getProgramWorkflowService().getPatientPrograms(p).size();
				hivProgram = Context.getProgramWorkflowService().getProgram("HIV PROGRAM");
				PatientProgram pp = Context.getProgramWorkflowService().enrollPatientInProgram(p, hivProgram, dateEnrolled, dateCompleted);
			}
			public void runOnParent() {
				Patient p = Context.getPatientService().getPatient(2);
				assertEquals("Enrollment failed",
				             numberEnrolledBefore + 1,
				             Context.getProgramWorkflowService().getPatientPrograms(p).size());
				for (PatientProgram pp : Context.getProgramWorkflowService().getPatientPrograms(p)) {
					if (pp.getProgram().equals(hivProgram)) {
						assertEquals("Wrong enrollment date", pp.getDateEnrolled(), dateEnrolled);
						assertEquals("Wrong completion date", pp.getDateCompleted(), dateCompleted);
					}
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
	
	public void testChangeState() throws Exception {
		runSyncTest(new SyncTestHelper() {
			Program hivProgram;
			ProgramWorkflow txStat;
			ProgramWorkflowState curedState;
			public void runOnChild() {
				hivProgram = Context.getProgramWorkflowService().getProgram("HIV PROGRAM");
				txStat = hivProgram.getWorkflowByName("TREATMENT STATUS");
				curedState = txStat.getStateByName("PATIENT CURED");

				Patient p = Context.getPatientService().getPatient(3);
				Collection<PatientProgram> temp = Context.getProgramWorkflowService().getPatientPrograms(p);
				assertEquals("Before test, patient record does not have the expected number of program enrollments", temp.size(), 1);
				PatientProgram pp = temp.iterator().next();
				assertNotSame("Before test, patient record not in expected state", pp.getCurrentState(txStat), curedState);
				Context.getProgramWorkflowService().changeToState(pp, txStat, curedState, new Date());
			}
			public void runOnParent() {
				Patient p = Context.getPatientService().getPatient(3);
				PatientProgram pp = Context.getProgramWorkflowService().getPatientPrograms(p).iterator().next();
				assertEquals("State not set", pp.getCurrentState(txStat).getState(), curedState);
			}
		});
	}
	
	public void testCreateEncounterAndObs() throws Exception {
		runSyncTest(new SyncTestHelper() {
			int numEncountersSoFar = 0;
			Date dateOfNewEncounter = new Date();
			Date anotherDate = new Date(System.currentTimeMillis() - 20000l);
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
				
				Obs noEnc = new Obs();
				noEnc.setConcept(weight);
				noEnc.setValueNumeric(12.3);
				noEnc.setObsDatetime(anotherDate);
				Context.getObsService().createObs(noEnc);
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
				
				boolean found = false;
				for (Obs o : Context.getObsService().getObservations(p, false)) {
					if (o.getObsDatetime().equals(anotherDate) && o.getConcept().equals(weight) && o.getValueNumeric().equals(12.3))
						found = true;
				}
				assertTrue("Cannot find newly created encounter-less obs", found);
			}
		});
	}
	
	public void testEditEncounter() throws Exception {
		runSyncTest(new SyncTestHelper() {
			Date d1 = ymd.parse("1978-01-01");
			Date d2 = ymd.parse("1978-12-31");
			public void runOnChild(){
				Patient p = Context.getPatientService().getPatient(2);
				Collection<Encounter> encs = Context.getEncounterService().getEncounters(p, d1, d2);
				assertEquals(encs.size(), 1);
				Encounter e = encs.iterator().next();
				e.setEncounterDatetime(d2);
				Context.getEncounterService().updateEncounter(e);
			}
			public void runOnParent() {
				Patient p = Context.getPatientService().getPatient(2);
				Collection<Encounter> encs = Context.getEncounterService().getEncounters(p, d1, d2);
				assertEquals(encs.size(), 1);
				Encounter e = encs.iterator().next();
				assertEquals("Failed to change date", e.getEncounterDatetime(), d2);
			}
		});
	}
	
	public void testEditObs() throws Exception {
		runSyncTest(new SyncTestHelper() {
			Date d = ymd.parse("1978-04-11");
			Concept weight = null;
			public void runOnChild(){
				weight = Context.getConceptService().getConceptByName("WEIGHT");
				Patient p = Context.getPatientService().getPatient(2);
				Obs obs = null;
				for (Obs o : Context.getObsService().getObservations(p, weight, false)) {
					if (OpenmrsUtil.compare(o.getObsDatetime(), d) == 0)
						obs = o;
				}
				assertNotNull("Before test, could not find expected obs", obs);
				Context.getObsService().voidObs(obs, "Data entry error");
				
				Obs newObs = new Obs();
				newObs.setPerson(obs.getPerson());
				newObs.setConcept(obs.getConcept());
				newObs.setObsDatetime(obs.getObsDatetime());
				newObs.setLocation(obs.getLocation());
				newObs.setCreator(Context.getAuthenticatedUser());
				newObs.setDateCreated(new Date());
				newObs.setValueNumeric(99.9);
				newObs.setEncounter(obs.getEncounter());
				Context.getObsService().createObs(newObs);
			}
			public void runOnParent() {
				Patient p = Context.getPatientService().getPatient(2);
				boolean found = false;
				for (Obs o : Context.getObsService().getObservations(p, weight, false))
					if (o.getObsDatetime().equals(d)) {
						assertEquals(o.getEncounter().getObs().size(), 3);
						assertEquals(o.getValueNumeric(), 99.9);
						found = true;
					}
				assertTrue(found);
			}
		});
	}

	public void testCreatePatient() throws Exception {
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
				assertEquals(p.getPersonName().toString(), name.toString());
				assertEquals(p.getIdentifiers().iterator().next(), id);
			}
		});
	}
	
	public void testEditPatient() throws Exception {
		runSyncTest(new SyncTestHelper() {
			PatientIdentifierType pit;
			public void runOnChild() {
				pit = Context.getPatientService().getPatientIdentifierType(2);
				Location loc = Context.getEncounterService().getLocationByName("Someplace");
				Patient p = Context.getPatientService().getPatient(2);
				p.setGender("F");
				p.removeName(p.getPersonName());
				p.addName(new PersonName("Peter", null, "Parker"));
				p.addIdentifier(new PatientIdentifier("super123", pit, loc));
				Context.getPatientService().updatePatient(p);
			}
			public void runOnParent() {
				Patient p = Context.getPatientService().getPatient(2);
				assertEquals("Gender didn't change", p.getGender(), "F");
				assertEquals("Name should be Peter Parker", p.getPersonName().toString(), "Peter Parker");
				boolean found = false;
				for (PatientIdentifier id : p.getIdentifiers())
					if (id.getIdentifier().equals("super123") && id.getIdentifierType().equals(pit))
						found = true;
				assertTrue("Couldn't find new ID", found);
			}
		});
	}

}
