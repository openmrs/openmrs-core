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

import org.openmrs.PatientState;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.RestTestConstants1_8;
import org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResourceTest;

public class PatientStateResource1_8Test extends BaseDelegatingResourceTest<PatientStateResource1_8, PatientState> {
	
	@Override
	public PatientState newObject() {
		return Context.getProgramWorkflowService().getPatientStateByUuid(getUuidProperty());
	}
	
	@Override
	public void validateRefRepresentation() throws Exception {
		assertPropEquals("startDate", getObject().getStartDate());
		assertPropEquals("endDate", getObject().getEndDate());
		assertPropPresent("state");
		assertPropEquals("uuid", getObject().getUuid());
	}
	
	@Override
	public void validateDefaultRepresentation() throws Exception {
		assertPropPresent("patientProgram");
		validateRefRepresentation();
	}
	
	@Override
	public void validateFullRepresentation() throws Exception {
		validateRefRepresentation();
		assertPropPresent("patientProgram");
		assertPropEquals("voided", getObject().getVoided());
		assertPropPresent("auditInfo");
	}
	
	@Override
	public String getDisplayProperty() {
		return getObject().getState().getConcept().getDisplayString();
	}
	
	@Override
	public String getUuidProperty() {
		return RestTestConstants1_8.PATIENT_STATE_UUID;
	}
}
