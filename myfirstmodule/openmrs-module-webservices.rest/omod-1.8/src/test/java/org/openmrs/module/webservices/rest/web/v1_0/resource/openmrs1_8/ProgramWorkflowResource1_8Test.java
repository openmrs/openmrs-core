/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_8;

import org.openmrs.ProgramWorkflow;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.RestTestConstants1_8;
import org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResourceTest;

public class ProgramWorkflowResource1_8Test extends BaseDelegatingResourceTest<ProgramWorkflowResource1_8, ProgramWorkflow> {
	
	@Override
	public ProgramWorkflow newObject() {
		return Context.getProgramWorkflowService().getWorkflowByUuid(getUuidProperty());
	}
	
	@Override
	public void validateFullRepresentation() throws Exception {
		super.validateFullRepresentation();
		assertPropEquals("description", getObject().getDescription());
		assertPropEquals("retired", getObject().getRetired());
		assertPropPresent("concept");
	}
	
	@Override
	public void validateRefRepresentation() throws Exception {
		super.validateRefRepresentation();
		assertPropEquals("retired", getObject().getRetired());
		assertPropPresent("concept");
	}
	
	@Override
	public void validateDefaultRepresentation() throws Exception {
		super.validateDefaultRepresentation();
		assertPropEquals("description", getObject().getDescription());
		assertPropEquals("retired", getObject().getRetired());
		assertPropPresent("concept");
	}
	
	@Override
	public String getDisplayProperty() {
		return getObject().getName();
	}
	
	@Override
	public String getUuidProperty() {
		return RestTestConstants1_8.WORKFLOW_UUID;
	}
}
