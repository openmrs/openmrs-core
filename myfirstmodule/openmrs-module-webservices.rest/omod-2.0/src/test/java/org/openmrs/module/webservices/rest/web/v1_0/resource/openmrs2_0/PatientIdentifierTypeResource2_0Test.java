/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs2_0;

import org.openmrs.PatientIdentifierType;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.RestTestConstants1_8;
import org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResourceTest;

public class PatientIdentifierTypeResource2_0Test extends BaseDelegatingResourceTest<PatientIdentifierTypeResource2_0, PatientIdentifierType> {
	
	@Override
	public PatientIdentifierType newObject() {
		return Context.getService(PatientService.class).getPatientIdentifierTypeByUuid(getUuidProperty());
	}
	
	@Override
	public void validateDefaultRepresentation() throws Exception {
		super.validateDefaultRepresentation();
		assertPropEquals("name", getObject().getName());
		assertPropEquals("description", getObject().getDescription());
		assertPropEquals("format", getObject().getFormat());
		assertPropEquals("formatDescription", getObject().getFormatDescription());
		assertPropEquals("required", getObject().getRequired());
		assertPropEquals("validator", getObject().getValidator());
		assertPropPresent("locationBehavior");
		assertPropEquals("retired", getObject().getRetired());
		
		assertPropNotPresent("checkDigit");
	}
	
	@Override
	public void validateFullRepresentation() throws Exception {
		super.validateFullRepresentation();
		assertPropEquals("name", getObject().getName());
		assertPropEquals("description", getObject().getDescription());
		assertPropEquals("format", getObject().getFormat());
		assertPropEquals("formatDescription", getObject().getFormatDescription());
		assertPropEquals("required", getObject().getRequired());
		assertPropEquals("validator", getObject().getValidator());
		assertPropPresent("locationBehavior");
		assertPropEquals("retired", getObject().getRetired());
		assertPropPresent("auditInfo");
		
		assertPropNotPresent("checkDigit");
	}
	
	@Override
	public String getDisplayProperty() {
		return "OpenMRS Identification Number";
	}
	
	@Override
	public String getUuidProperty() {
		return RestTestConstants1_8.PATIENT_IDENTIFIER_TYPE_UUID;
	}
}
