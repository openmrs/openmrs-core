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

import org.openmrs.PatientIdentifierType;
import org.openmrs.RelationshipType;
import org.openmrs.api.context.Context;

/**
 * Testing of delete methods and whether that action is synchronized
 */
public class SyncOnDeleteTest extends SyncBaseTest {

	@Override
    public String getInitialDataset() {
		return "org/openmrs/synchronization/engine/include/SyncCreateTest.xml";
    }

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
}
