/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.controller.openmrs2_0;

import org.junit.Test;
import org.openmrs.ProgramWorkflow;
import org.openmrs.ProgramWorkflowState;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.RestTestConstants1_8;
import org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ProgramWorkflowStateController2_0Test extends MainResourceControllerTest {

	@Override
	public String getURI() {
		return "workflow/" + RestTestConstants1_8.WORKFLOW_UUID + "/state/";
	}

	@Override
	public String getUuid() {
		return "e938129e-248a-482a-acea-f85127251472";
	}

	@Override
	public long getAllCount() {
		return 3;
	}

	@Test
	public void shouldCreateWorkflowState() throws Exception {
		String json =
				"{\"concept\": \"" + RestTestConstants1_8.CONCEPT_UUID + "\",\"initial\": true,\"terminal\": false}";

		SimpleObject newWorkflowState = deserialize(handle(newPostRequest(getURI(), json)));

		assertNotNull(newWorkflowState);
		String uuid = newWorkflowState.get("uuid");

		ProgramWorkflowState createdWorkflowState = Context.getProgramWorkflowService().getStateByUuid(uuid);
		assertEquals(RestTestConstants1_8.CONCEPT_UUID, createdWorkflowState.getConcept().getUuid());
		assertEquals(true, createdWorkflowState.getInitial());
		assertEquals(false, createdWorkflowState.getTerminal());
	}

	@Test
	public void shouldDeleteWorkflowState() throws Exception {
		// create new state
		ProgramWorkflow workflow = Context.getProgramWorkflowService().getWorkflowByUuid(RestTestConstants1_8.WORKFLOW_UUID);

		ProgramWorkflowState state = new ProgramWorkflowState();
		state.setConcept(Context.getConceptService().getConceptByUuid(RestTestConstants1_8.CONCEPT2_UUID));
		state.setInitial(true);
		state.setTerminal(false);

		workflow.addState(state);

		int workflowSizeBefore = workflow.getStates().size();
		Context.getProgramWorkflowService().saveProgram(workflow.getProgram());

		// call DELETE on the newly created state
		handle(newDeleteRequest(getURI() + "/" + state.getUuid()));

		// verify that state has been removed
		workflow = Context.getProgramWorkflowService().getWorkflowByUuid(RestTestConstants1_8.WORKFLOW_UUID);
		assertEquals(workflowSizeBefore - 1, workflow.getStates().size());
	}
}
