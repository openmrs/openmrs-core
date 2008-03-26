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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.openmrs.Concept;
import org.openmrs.ConceptName;
import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.Program;
import org.openmrs.ProgramWorkflow;
import org.openmrs.ProgramWorkflowState;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.ConceptService;
import org.openmrs.api.ProgramWorkflowService;
import org.openmrs.api.context.Context;

/**
 *
 */
public class SyncAdminTest extends SyncBaseTest {

	@Override
    public String getInitialDataset() {
	    return "org/openmrs/test/synchronization/engine/include/SyncCreateTest.xml";
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
				
				ProgramWorkflow wf = new ProgramWorkflow();
				wf.setConcept(txStatus);
				prog.addWorkflow(wf);
				Context.getProgramWorkflowService().createWorkflow(wf);
				
				ProgramWorkflowState followState = new ProgramWorkflowState();
				followState.setConcept(following);
				followState.setInitial(true);
				followState.setTerminal(false);
				ProgramWorkflowState cureState = new ProgramWorkflowState();
				cureState.setConcept(cured);
				cureState.setInitial(false);
				cureState.setTerminal(true);
				wf.addState(followState);
				wf.addState(cureState);
				Context.getProgramWorkflowService().updateWorkflow(wf);
			}
			public void runOnParent() {
				assertEquals("Failed to create program",
				             numBefore + 1,
				             Context.getProgramWorkflowService().getPrograms().size());
				Program p = Context.getProgramWorkflowService().getProgram("TB PROGRAM");
				assertNotNull("Workflows is null", p.getWorkflows());
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
	
	public void testEditProgram() throws Exception {
		runSyncTest(new SyncTestHelper() {
			ProgramWorkflowService ps = Context.getProgramWorkflowService();
			int numStatesBefore;
			public void runOnChild() {
				Program hiv = Context.getProgramWorkflowService().getProgram("HIV PROGRAM");
				assertEquals(hiv.getWorkflows().size(), 1);
				ProgramWorkflow wf = hiv.getWorkflows().iterator().next();
				numStatesBefore = wf.getStates().size();

				ProgramWorkflowState st = new ProgramWorkflowState();
				st.setConcept(Context.getConceptService().getConceptByName("NONE"));
				st.setInitial(false);
				st.setTerminal(true);
				wf.addState(st);
				ps.createState(st);
				ps.createOrUpdateProgram(hiv);
			}
			public void runOnParent() {
				Program hiv = Context.getProgramWorkflowService().getProgram("HIV PROGRAM");
				assertEquals(hiv.getWorkflows().size(), 1);
				ProgramWorkflow wf = hiv.getWorkflows().iterator().next();
				assertEquals(wf.getStates().size(), numStatesBefore + 1);
			}
		});
	}

	
	public void testCreateLocation() throws Exception {
		runSyncTest(new SyncTestHelper() {
			public void runOnChild() {
				Location loc = new Location();
				loc.setName("Boston");
				loc.setDescription("A US city");
				Context.getAdministrationService().createLocation(loc);
			}
			public void runOnParent() {
				assertNotNull("Location not created", Context.getEncounterService().getLocationByName("Boston"));
			}
		});
	}
	
	public void testEditLocation() throws Exception {
		runSyncTest(new SyncTestHelper() {
			public void runOnChild() {
				Location loc = Context.getEncounterService().getLocationByName("Someplace");
				loc.setName("Over the rainbow");
				Context.getAdministrationService().updateLocation(loc);
			}
			public void runOnParent() {
				Location loc = Context.getEncounterService().getLocationByName("Someplace");
				assertNull(loc);
				loc = Context.getEncounterService().getLocationByName("Over the rainbow");
				assertNotNull(loc);
			}
		});
	}
	
	

	
}
