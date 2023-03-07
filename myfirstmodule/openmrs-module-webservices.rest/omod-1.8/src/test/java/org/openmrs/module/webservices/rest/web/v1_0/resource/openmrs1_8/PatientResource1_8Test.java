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

import org.openmrs.Patient;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.RestTestConstants1_8;
import org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResourceTest;

public class PatientResource1_8Test extends BaseDelegatingResourceTest<PatientResource1_8, Patient> {

	@Override
	public Patient newObject() {
		return Context.getService(PatientService.class).getPatientByUuid(getUuidProperty());
	}

	@Override
	public void validateDefaultRepresentation() throws Exception {
		super.validateDefaultRepresentation();
		assertPropPresent("uuid");
		assertPropEquals("gender", getObject().getGender());
		assertPropEquals("age", getObject().getAge());
		assertPropEquals("birthdate", getObject().getBirthdate());
		assertPropEquals("birthdateEstimated", getObject().getBirthdateEstimated());
		assertPropEquals("dead", getObject().getDead());
		assertPropEquals("deathDate", getObject().getDeathDate());
		assertPropPresent("causeOfDeath");
		assertPropPresent("preferredName");
		assertPropPresent("preferredAddress");
		assertPropPresent("attributes");
		assertPropPresent("display");
		assertPropEquals("voided", getObject().getVoided());
	}

	@Override
	public void validateFullRepresentation() throws Exception {
		super.validateFullRepresentation();
		assertPropPresent("uuid");
		assertPropEquals("gender", getObject().getGender());
		assertPropEquals("age", getObject().getAge());
		assertPropEquals("birthdate", getObject().getBirthdate());
		assertPropEquals("birthdateEstimated", getObject().getBirthdateEstimated());
		assertPropEquals("dead", getObject().getDead());
		assertPropEquals("deathDate", getObject().getDeathDate());
		assertPropPresent("causeOfDeath");
		assertPropPresent("preferredName");
		assertPropPresent("preferredAddress");
		assertPropPresent("attributes");
		assertPropPresent("display");
		assertPropEquals("voided", getObject().getVoided());
		assertPropPresent("auditInfo");
	}

	@Override
	public String getDisplayProperty() {
		return "Mr. Horatio Test Hornblower Esq.";
	}

	@Override
	public String getUuidProperty() {
		return RestTestConstants1_8.PATIENT_UUID;
	}

}
