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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.GlobalProperty;
import org.openmrs.Location;
import org.openmrs.Program;
import org.openmrs.ProgramWorkflow;
import org.openmrs.ProgramWorkflowState;
import org.openmrs.api.ConceptService;
import org.openmrs.api.ProgramWorkflowService;
import org.openmrs.api.context.Context;
import org.openmrs.test.testutil.SkipBaseSetup;
import org.springframework.test.annotation.NotTransactional;

/**
 *
 */
public class SyncAdminTest extends SyncBaseTest {

	@Override
    public String getInitialDataset() {
	    return "org/openmrs/test/synchronization/engine/include/SyncCreateTest.xml";
    }

	@Test
    @NotTransactional
	public void shouldCreateProgram() throws Exception {
		runSyncTest(new SyncTestHelper() {
			int numBefore = 0;
			public void runOnChild() {
				numBefore = Context.getProgramWorkflowService().getAllPrograms().size();
				ConceptService cs = Context.getConceptService();
				Concept tbProgram = cs.getConceptByName("TB PROGRAM");
				Concept txStatus = cs.getConceptByName("TREATMENT STATUS");
				Concept following = cs.getConceptByName("FOLLOWING");
				Concept cured = cs.getConceptByName("PATIENT CURED");

				Program prog = new Program();
				prog.setName("TB PROGRAM");
				prog.setDescription("TB PROGRAM DESCRIPTION");
				prog.setConcept(tbProgram);

				
				ProgramWorkflow wf = new ProgramWorkflow();
				wf.setConcept(txStatus);
				prog.addWorkflow(wf);

				
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
				Context.getProgramWorkflowService().saveProgram(prog);
			}
			public void runOnParent() {
				assertEquals("Failed to create program", numBefore + 1, Context.getProgramWorkflowService().getPrograms().size());
				Program p = Context.getProgramWorkflowService().getProgram("TB PROGRAM");
				System.out.println("TB Program = " + p);
				assertNotNull("Workflows is null", p.getWorkflows());
				assertEquals("Wrong number of workflows", 1, p.getWorkflows().size());

				ProgramWorkflow wf = p.getWorkflowByName("TREATMENT STATUS");
				assertNotNull(wf);
				List<String> names = new ArrayList<String>();
				for (ProgramWorkflowState s : wf.getStates())
					names.add(s.getConcept().getName().getName());
				assertEquals("Wrong number of states", 2, names.size());
				names.remove("FOLLOWING");
				names.remove("PATIENT CURED");
				assertEquals("States have wrong names", 0, names.size());
			}
		});
	}

	@Test
    @NotTransactional
    @Ignore
	public void shouldEditProgram() throws Exception {
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
				ps.saveProgram(hiv);
			}
			public void runOnParent() {
				Program hiv = Context.getProgramWorkflowService().getProgram("HIV PROGRAM");
				assertEquals(hiv.getWorkflows().size(), 1);
				ProgramWorkflow wf = hiv.getWorkflows().iterator().next();
				assertEquals(wf.getStates().size(), numStatesBefore + 1);
			}
		});
	}

	@Test
    @NotTransactional	
    @Ignore
	public void shouldCreateLocation() throws Exception {
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

	@Test
    @NotTransactional
    @Ignore
	public void shouldEditLocation() throws Exception {
		runSyncTest(new SyncTestHelper() {
			public void runOnChild() {
				Location loc = Context.getLocationService().getLocation("Someplace");
				loc.setName("Over the rainbow");
				Context.getLocationService().saveLocation(loc);
			}
			public void runOnParent() {
				Location loc = Context.getLocationService().getLocation("Someplace");
				assertNull(loc);
				loc = Context.getLocationService().getLocation("Over the rainbow");
				assertNotNull(loc);
			}
		});
	}
	
	
	/**
	 * Global props should not sync unless SynchronizableInstance.setIsSynchronizable(true) is set
	 * 
	 * @throws Exception
	 */
	@Test
    @NotTransactional
    @Ignore
	public void shouldEditSaveGlobalProperty() throws Exception {
		runSyncTest(new SyncTestHelper() {
			public void runOnChild() {
				GlobalProperty gp1 = Context.getAdministrationService().saveGlobalProperty(new GlobalProperty("sync.test1", "test1"));
				assertNotNull(gp1.getGuid());
				GlobalProperty gp2 = new GlobalProperty("sync.test2","test2");
				gp2.setIsSynchronizable(true);
				gp2 = Context.getAdministrationService().saveGlobalProperty(gp2);
				gp2.setPropertyValue("test2 - changed");
				Context.getAdministrationService().saveGlobalProperty(gp2);
			}
			public void runOnParent() {
				assertNull(Context.getAdministrationService().getGlobalProperty("sync.test1"));
				assertEquals("test2 - changed",Context.getAdministrationService().getGlobalProperty("sync.test2"));
			}
		});
	}

	
}
