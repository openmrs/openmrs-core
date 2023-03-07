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
import org.openmrs.Diagnosis;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResourceTest;
import org.openmrs.module.webservices.rest.web.v1_0.RestTestConstants2_2;

/**
 * Tests functionality of {@link DiagnosisResource2_2}.
 */
public class DiagnosisResource2_2Test extends BaseDelegatingResourceTest<DiagnosisResource2_2, Diagnosis> {
	
	@Before
	public void before() throws Exception {
		executeDataSet(RestTestConstants2_2.DIAGNOSIS_TEST_DATA_XML);
	}
	
	@Override
	public Diagnosis newObject() {
		return Context.getDiagnosisService().getDiagnosisByUuid(getUuidProperty());
	}
	
	@Override
	public String getDisplayProperty() {
		return "";
	}
	
	@Override
	public String getUuidProperty() {
		return RestTestConstants2_2.DIAGNOSIS_UUID;
	}
	
	@Override
	public void validateDefaultRepresentation() throws Exception {
		super.validateDefaultRepresentation();
		assertPropPresent("uuid");
		assertPropEquals("diagnosis", getObject().getDiagnosis());
		assertPropPresent("patient");
		assertPropPresent("condition");
		assertPropPresent("encounter");
		assertPropEquals("certainty", getObject().getCertainty());
		assertPropEquals("rank", getObject().getRank());
		assertPropEquals("voided", getObject().getVoided());
		assertPropPresent("display");
	}
	
	@Override
	public void validateFullRepresentation() throws Exception {
		super.validateDefaultRepresentation();
		assertPropPresent("uuid");
		assertPropEquals("diagnosis", getObject().getDiagnosis());
		assertPropPresent("patient");
		assertPropPresent("condition");
		assertPropEquals("certainty", getObject().getCertainty());
		assertPropEquals("rank", getObject().getRank());
		assertPropEquals("voided", getObject().getVoided());
		assertPropPresent("auditInfo");
		assertPropPresent("display");
	}
}
