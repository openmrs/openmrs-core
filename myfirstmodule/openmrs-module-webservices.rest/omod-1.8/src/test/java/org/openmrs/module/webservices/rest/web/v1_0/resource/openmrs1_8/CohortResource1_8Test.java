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

import org.openmrs.module.webservices.rest.web.RestTestConstants1_8;
import org.junit.Before;
import org.openmrs.Cohort;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResourceTest;

public class CohortResource1_8Test extends BaseDelegatingResourceTest<CohortResource1_8, Cohort> {
	
	@Before
	public void before() throws Exception {
		executeDataSet(RestTestConstants1_8.RESOURCE_TEST_DATASET);
	}
	
	@Override
	public Cohort newObject() {
		return Context.getCohortService().getCohortByUuid(getUuidProperty());
	}
	
	@Override
	public void validateDefaultRepresentation() throws Exception {
		super.validateDefaultRepresentation();
		assertPropEquals("name", getObject().getName());
		assertPropEquals("description", getObject().getDescription());
		assertPropEquals("voided", getObject().getVoided());
		assertPropPresent("memberIds");
	}
	
	@Override
	public void validateFullRepresentation() throws Exception {
		super.validateFullRepresentation();
		assertPropEquals("name", getObject().getName());
		assertPropEquals("description", getObject().getDescription());
		assertPropEquals("voided", getObject().getVoided());
		assertPropPresent("memberIds");
		assertPropPresent("auditInfo");
	}
	
	@Override
	public String getDisplayProperty() {
		return "B13 deficit";
	}
	
	@Override
	public String getUuidProperty() {
		return RestTestConstants1_8.COHORT_UUID;
	}
	
}
