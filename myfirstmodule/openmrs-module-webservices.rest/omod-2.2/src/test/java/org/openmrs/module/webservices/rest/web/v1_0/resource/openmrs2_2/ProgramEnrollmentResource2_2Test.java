/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs2_2;

import org.junit.Before;
import org.openmrs.PatientProgram;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResourceTest;

public class ProgramEnrollmentResource2_2Test extends BaseDelegatingResourceTest<ProgramEnrollmentResource2_2, PatientProgram> {
	
	protected static final String PROGRAM_ATTRIBUTES_XML = "ProgramAttributesDataset.xml";
	
	@Before
	public void before() throws Exception {
		executeDataSet("programEnrollmentDataSet.xml");
	}
	
	@Override
	public PatientProgram newObject() {
		return Context.getProgramWorkflowService().getPatientProgramByUuid(getUuidProperty());
	}
	
	@Override
	public void validateDefaultRepresentation() throws Exception {
		super.validateDefaultRepresentation();
		assertPropPresent("attributes");
	}
	
	@Override
	public String getDisplayProperty() {
		return "HIV Program";
	}
	
	@Override
	public String getUuidProperty() {
		return "9119b9f8-af3d-4ad8-9e2e-2317c3de91c6";
	}
}
