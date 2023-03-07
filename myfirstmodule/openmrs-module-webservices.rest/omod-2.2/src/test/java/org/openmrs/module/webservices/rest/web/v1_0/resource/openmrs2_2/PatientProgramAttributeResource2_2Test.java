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

import org.openmrs.PatientProgramAttribute;
import org.openmrs.api.ProgramWorkflowService;
import org.junit.Before;
import org.openmrs.module.webservices.rest.web.v1_0.RestTestConstants2_2;
import org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResourceTest;
import org.springframework.beans.factory.annotation.Autowired;

public class PatientProgramAttributeResource2_2Test extends BaseDelegatingResourceTest<PatientProgramAttributeResource2_2, PatientProgramAttribute> {
	
	@Autowired
	private ProgramWorkflowService programWorkflowService;
	
	@Before
	public void before() throws Exception {
		executeDataSet("programEnrollmentDataSet.xml");
	}
	
	@Override
	public PatientProgramAttribute newObject() {
		
		return programWorkflowService.getPatientProgramAttributeByUuid(getUuidProperty());
	}
	
	@Override
	public void validateDefaultRepresentation() throws Exception {
		super.validateDefaultRepresentation();
		assertPropEquals("value", getObject().getValue());
		assertPropPresent("attributeType");
		assertPropEquals("voided", getObject().getVoided());
	}
	
	@Override
	public void validateFullRepresentation() throws Exception {
		super.validateFullRepresentation();
		assertPropEquals("value", getObject().getValue());
		assertPropPresent("attributeType");
		assertPropEquals("voided", getObject().getVoided());
		assertPropPresent("auditInfo");
	}
	
	@Override
	public String getDisplayProperty() {
		return "stage: Stage1";
	}
	
	@Override
	public String getUuidProperty() {
		return RestTestConstants2_2.PATIENT_PROGRAM_ATTRIBUTE_UUID;
	}
}
