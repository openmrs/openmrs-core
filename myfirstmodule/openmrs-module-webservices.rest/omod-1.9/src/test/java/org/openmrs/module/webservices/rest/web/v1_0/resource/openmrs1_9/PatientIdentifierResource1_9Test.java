/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_9;

import org.openmrs.module.webservices.rest.web.RestTestConstants1_8;
import org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_8.PatientIdentifierResource1_8;
import org.openmrs.PatientIdentifier;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResourceTest;

public class PatientIdentifierResource1_9Test extends BaseDelegatingResourceTest<PatientIdentifierResource1_8, PatientIdentifier> {
	
	@Override
	public PatientIdentifier newObject() {
		return Context.getService(PatientService.class).getPatientIdentifierByUuid(getUuidProperty());
	}
	
	@Override
	public void validateDefaultRepresentation() throws Exception {
		super.validateDefaultRepresentation();
		assertPropEquals("identifier", getObject().getIdentifier());
		assertPropPresent("identifierType");
		assertPropPresent("location");
		assertPropEquals("preferred", getObject().getPreferred());
		assertPropEquals("voided", getObject().getVoided());
	}
	
	@Override
	public void validateFullRepresentation() throws Exception {
		super.validateFullRepresentation();
		assertPropEquals("identifier", getObject().getIdentifier());
		assertPropPresent("identifierType");
		assertPropPresent("location");
		assertPropEquals("preferred", getObject().getPreferred());
		assertPropEquals("voided", getObject().getVoided());
		assertPropPresent("auditInfo");
	}
	
	@Override
	public String getDisplayProperty() {
		return "OpenMRS Identification Number = 101-6";
	}
	
	@Override
	public String getUuidProperty() {
		return RestTestConstants1_8.PATIENT_IDENTIFIER_UUID;
	}
	
}
