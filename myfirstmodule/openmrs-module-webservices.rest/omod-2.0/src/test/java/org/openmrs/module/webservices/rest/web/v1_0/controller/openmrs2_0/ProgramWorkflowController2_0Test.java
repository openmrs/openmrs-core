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

import org.openmrs.ProgramWorkflow;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.RestTestConstants1_8;
import org.junit.Test;
import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;
import org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ProgramWorkflowController2_0Test extends MainResourceControllerTest {

	@Override
	@Test(expected = ResourceDoesNotSupportOperationException.class)
	public void shouldGetAll() throws Exception {
		// resource doesn't support get all operation
		super.shouldGetAll();
	}

	@Override
	public String getURI() {
		return "workflow";
	}

	@Override
	public String getUuid() {
		return RestTestConstants1_8.WORKFLOW_UUID;
	}

	@Override
	public long getAllCount() {
		return 1;
	}

	@Test
	public void shouldCreateWorkflow() throws Exception {
		String json =
				"{\"program\": \"" + RestTestConstants1_8.PROGRAM_UUID + "\",\"concept\": \"" + RestTestConstants1_8.CONCEPT_UUID + "\"}";

		SimpleObject newWorkflow = deserialize(handle(newPostRequest(getURI(), json)));

		assertNotNull(newWorkflow);
		String uuid = newWorkflow.get("uuid");

		ProgramWorkflow createdWorkflow = Context.getProgramWorkflowService().getWorkflowByUuid(uuid);
		assertEquals(RestTestConstants1_8.PROGRAM_UUID, createdWorkflow.getProgram().getUuid());
		assertEquals(RestTestConstants1_8.CONCEPT_UUID, createdWorkflow.getConcept().getUuid());
	}
}
