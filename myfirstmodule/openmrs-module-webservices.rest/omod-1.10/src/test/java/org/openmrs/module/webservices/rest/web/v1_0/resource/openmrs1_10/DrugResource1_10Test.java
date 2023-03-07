/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_10;

import org.junit.Before;
import org.openmrs.Drug;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.RestTestConstants1_10;
import org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResourceTest;

public class DrugResource1_10Test extends BaseDelegatingResourceTest<DrugResource1_10, Drug> {
	
	@Before
	public void before() throws Exception {
		executeDataSet(RestTestConstants1_10.DRUG_REFERENCE_MAP_TEST_DATASET);
	}
	
	@Override
	public Drug newObject() {
		return Context.getConceptService().getDrugByUuid(getUuidProperty());
	}
	
	@Override
	public String getDisplayProperty() {
		return "Panadol";
	}
	
	@Override
	public String getUuidProperty() {
		return RestTestConstants1_10.DRUG_REFERENCE_DRUG_UUID;
	}
	
	@Override
	public void validateDefaultRepresentation() throws Exception {
		super.validateDefaultRepresentation();
		assertPropPresent("drugReferenceMaps");
	}
	
	@Override
	public void validateFullRepresentation() throws Exception {
		super.validateDefaultRepresentation();
		assertPropPresent("drugReferenceMaps");
	}
}
