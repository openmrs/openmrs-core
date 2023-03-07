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

import org.junit.Before;
import org.openmrs.Provider;
import org.openmrs.api.ProviderService;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.RestTestConstants1_9;
import org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResourceTest;

public class ProviderResource1_9Test extends BaseDelegatingResourceTest<ProviderResource1_9, Provider> {
	
	@Before
	public void before() throws Exception {
		executeDataSet(RestTestConstants1_9.TEST_DATASET);
	}
	
	@Override
	public Provider newObject() {
		return Context.getService(ProviderService.class).getProviderByUuid(getUuidProperty());
	}
	
	@Override
	public void validateDefaultRepresentation() throws Exception {
		super.validateDefaultRepresentation();
		assertPropPresent("person");
		assertPropEquals("identifier", getObject().getIdentifier());
		assertPropPresent("attributes");
		assertPropEquals("retired", getObject().getRetired());
	}
	
	@Override
	public void validateFullRepresentation() throws Exception {
		super.validateFullRepresentation();
		assertPropPresent("person");
		assertPropEquals("identifier", getObject().getIdentifier());
		assertPropPresent("attributes");
		assertPropEquals("retired", getObject().getRetired());
		assertPropPresent("auditInfo");
	}
	
	@Override
	public String getDisplayProperty() {
		return "Test2 - Hippocrates of Cos";
	}
	
	@Override
	public String getUuidProperty() {
		return RestTestConstants1_9.PROVIDER_UUID;
	}
}
