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

import org.junit.Before;
import org.openmrs.Allergy;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.v1_0.RestTestConstants2_0;
import org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResourceTest;

public class PatientAllergyResource2_0Test extends BaseDelegatingResourceTest<PatientAllergyResource2_0, Allergy> {
	
	@Before
	public void init() throws Exception {
		executeDataSet(RestTestConstants2_0.ALLERGY_TEST_DATA_XML);
	}
	
	@Override
	public Allergy newObject() {
		return Context.getPatientService().getAllergyByUuid(RestTestConstants2_0.ALLERGY_UUID);
	}
	
	@Override
	public void validateDefaultRepresentation() throws Exception {
		super.validateDefaultRepresentation(); // allergy does not have uuid so this fails
		assertPropPresent("allergen");
		assertPropPresent("severity");
		assertPropPresent("reactions");
		assertPropPresent("patient");
	}
	
	@Override
	public void validateFullRepresentation() throws Exception {
		super.validateFullRepresentation(); // allergy does not have uuid so this fails
		assertPropPresent("allergen");
		assertPropPresent("severity");
		assertPropEquals("comment", getObject().getComment());
		assertPropPresent("reactions");
		assertPropPresent("patient");
		assertPropEquals("voided", getObject().getVoided());
		assertPropPresent("auditInfo");
	}
	
	@Override
	public String getDisplayProperty() {
		return "STAVUDINE LAMIVUDINE AND NEVIRAPINE";
	}
	
	@Override
	public String getUuidProperty() {
		return RestTestConstants2_0.ALLERGY_UUID;
	}
	
}
