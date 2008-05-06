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

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.Calendar;

import org.openmrs.Concept;
import org.openmrs.ConceptAnswer;
import org.openmrs.ConceptName;
import org.openmrs.ConceptNumeric;
import org.openmrs.ConceptSet;
import org.openmrs.ConceptSynonym;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.ConceptService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.context.Context;

/**
 *
 */
public class SyncEncounterTest extends SyncBaseTest {

	@Override
    public String getInitialDataset() {
	    return "org/openmrs/test/synchronization/engine/include/SyncCreateTest.xml";
    }

	public void testCreateEncounterType() throws Exception {
		runSyncTest(new SyncTestHelper() {			
			AdministrationService adminService = Context.getAdministrationService();
			EncounterService encounterService = Context.getEncounterService();

			public void runOnChild() {
				
				EncounterType encounterType = new EncounterType();
				encounterType.setName("name");
				encounterType.setDescription("description");
				adminService.createEncounterType(encounterType);
			}
			public void runOnParent() {
				EncounterType encounterType = encounterService.getEncounterType("name");
				assertNotNull(encounterType);
			}
		});
	}	

	
	
	public void testUpdateEncounterType() throws Exception {
		runSyncTest(new SyncTestHelper() {			
			AdministrationService adminService = Context.getAdministrationService();
			EncounterService encounterService = Context.getEncounterService();
			public void runOnChild() {
				
				EncounterType encounterType = new EncounterType();
				encounterType.setName("name");
				encounterType.setDescription("description");
				adminService.createEncounterType(encounterType);	
				
				EncounterType updateEncounterType = encounterService.getEncounterType("name");
				encounterType.setName("new name");
				adminService.updateEncounterType(updateEncounterType);
			}
			public void runOnParent() {
				EncounterType encounterType = encounterService.getEncounterType("name");
				assertNull(encounterType);
				
				encounterType = encounterService.getEncounterType("new name");				
				assertNotNull(encounterType);
			}
		});
	}
	
	
	public void testDeleteEncounterType() throws Exception { 
		
		runSyncTest(new SyncTestHelper() {			
			public void runOnChild() {
				EncounterType existing = Context.getEncounterService().getEncounterType("DELETETEST");
				assertNotNull(existing);
				Context.getAdministrationService().deleteEncounterType(existing);
			}
			public void runOnParent() {
				EncounterType encounterType = Context.getEncounterService().getEncounterType("DELETETEST");
				assertNull(encounterType);
			}
		});
		
	}

	public void testCreateEncounter() throws Exception {
		runSyncTest(new SyncTestHelper() {			
			String eid = null;
			Calendar c;

			public void runOnChild() {
				
				Encounter e = new Encounter();
				c = Calendar.getInstance();
				c.set(2000,1,1);
				e.setCreator(Context.getAuthenticatedUser());
				e.setEncounterDatetime(c.getTime());
				e.setPatient(Context.getPatientService().getPatient(2));
				e.setEncounterType(Context.getEncounterService().getEncounterType("ADULTINITIAL"));
				Context.getEncounterService().createEncounter(e);
				eid = e.getGuid();
			}
			public void runOnParent() {
				Encounter e = Context.getEncounterService().getEncounterByGuid(eid);
				assertNotNull(e);
				assertEquals(c.getTime(),e.getEncounterDatetime());
				assertEquals(e.getEncounterType(),Context.getEncounterService().getEncounterType("ADULTINITIAL"));
			}
		});
	}	

	public void testDeleteEncounter() throws Exception {
		runSyncTest(new SyncTestHelper() {			

			public void runOnChild() {
				
				//delete existing
				Encounter existing = Context.getEncounterService().getEncounter(1);
				assertNotNull(existing);
				Context.getEncounterService().deleteEncounter(existing);

			}
			public void runOnParent() {
				Encounter e = null;
				e = Context.getEncounterService().getEncounter(1);
				assertNull(e);
			}
		});
	}	
	
}
