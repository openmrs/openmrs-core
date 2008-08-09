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
package org.openmrs.test.synchronization.engine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;

import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PersonName;
import org.openmrs.RelationshipType;
import org.openmrs.api.context.Context;
import org.openmrs.PersonAttributeType;
import org.springframework.test.annotation.NotTransactional;

/**
 * Testing of delete methods and whether that action is synchronized
 */
public class SyncOnDeleteTest extends SyncBaseTest {

	@Override
    public String getInitialDataset() {
		return "org/openmrs/test/synchronization/engine/include/SyncCreateTest.xml";
    }

	@Test
    @NotTransactional
	public void testDeletePatientIdentfierType() throws Exception {
		runSyncTest(new SyncTestHelper() {
			public void runOnChild(){				
				// make sure the patient identifier type is there
				PatientIdentifierType pit = Context.getPatientService().getPatientIdentifierType(1);
				assertNotNull("The patient identifier type could not be found in child server!", pit);
				
				// do the deleting
				Context.getAdministrationService().deletePatientIdentifierType(pit);
				
				pit = Context.getPatientService().getPatientIdentifierType(1);
				assertNull("The patient identifier type should have been deleted!", pit);
			}
			public void runOnParent() {
				// make sure it was deleted by sync
				PatientIdentifierType pit = Context.getPatientService().getPatientIdentifierType(1);
				assertNull("The patient identifier type should have been deleted!", pit);
			}
		});
	}	
	
	@Test
    @NotTransactional
	public void testDeleteRelationshipType() throws Exception {
		runSyncTest(new SyncTestHelper() {
			public void runOnChild(){				
				// make sure the patient identifier type is there
				RelationshipType rt = Context.getPersonService().getRelationshipType(1);
				assertNotNull("The relationship type could not be found in child server!", rt);
				
				// do the deleting
				Context.getPersonService().deleteRelationshipType(rt);
				
				rt = Context.getPersonService().getRelationshipType(1);
				assertNull("The relationship type should have been deleted!", rt);
			}
			public void runOnParent() {
				// make sure it was deleted by sync
				RelationshipType rt = Context.getPersonService().getRelationshipType(1);
				assertNull("The relationship type should have been deleted!", rt);
			}
		});
	}
	
	@Test
    @NotTransactional
	public void testDeletePersonAttributeType() throws Exception {
		runSyncTest(new SyncTestHelper() {
			public void runOnChild(){				
				// make sure the patient identifier type is there
				PersonAttributeType pat = Context.getPersonService().getPersonAttributeType(1);
				assertNotNull("The PersonAttributeType could not be found in child server!", pat);
				
				// do the deleting
				Context.getPersonService().deletePersonAttributeType(pat);
				
				pat = Context.getPersonService().getPersonAttributeType(1);
				assertNull("The PersonAttributeType should have been deleted!", pat);
			}
			public void runOnParent() {
				// make sure it was deleted by sync
				PersonAttributeType pat = Context.getPersonService().getPersonAttributeType(1);
				assertNull("The PersonAttributeType should have been deleted!", pat);
			}
		});
	}
	
	@Test
    @NotTransactional
	public void testDeletePatientName() throws Exception {
		runSyncTest(new SyncTestHelper() {
			PatientIdentifierType pit;
			public void runOnChild() {
				pit = Context.getPatientService().getPatientIdentifierType(2);
				Location loc = Context.getEncounterService().getLocationByName("Someplace");
				Patient p = Context.getPatientService().getPatient(2);
				p.removeName(p.getPersonName());
				p.addName(new PersonName("Peter", null, "Parker"));
				p.addIdentifier(new PatientIdentifier("super123", pit, loc));
				Context.getPatientService().updatePatient(p);
			}
			public void runOnParent() {
				Patient p = Context.getPatientService().getPatient(2);
				assertEquals("Name should be Peter Parker", p.getPersonName().toString(), "Peter Parker");
				boolean found = false;
				for (PatientIdentifier id : p.getIdentifiers())
					if (id.getIdentifier().equals("super123") && id.getIdentifierType().equals(pit))
						found = true;
				assertTrue("Couldn't find new ID", found);
			}
		});
	}
	
	@Test
    @NotTransactional
	public void testDeletePatient() throws Exception {
		runSyncTest(new SyncTestHelper() {
			public void runOnChild() {				
				Patient p = Context.getPatientService().getPatient(2);
				Context.getPatientService().deletePatient(p);				
			}
			public void runOnParent() {
				/*
				Patient p = Context.getPatientService().getPatient(2);
				assertNull("Patient should have been deleted!", p);
				*/
			}
		});
	}
	
}
