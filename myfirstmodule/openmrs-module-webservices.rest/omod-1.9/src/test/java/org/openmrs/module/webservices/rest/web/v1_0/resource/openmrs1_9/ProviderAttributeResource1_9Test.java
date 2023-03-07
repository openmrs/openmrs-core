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

import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.junit.Before;
import org.openmrs.ProviderAttribute;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.RestTestConstants1_9;
import org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResourceTest;

public class ProviderAttributeResource1_9Test extends BaseDelegatingResourceTest<ProviderAttributeResource1_9, ProviderAttribute> {
	
	@Before
	public void before() throws Exception {
		executeDataSet(RestTestConstants1_9.TEST_DATASET);
	}
	
	@Override
	public ProviderAttribute newObject() {
		return Context.getProviderService().getProviderAttributeByUuid(getUuidProperty());
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
		try {
			return "Joining Date: " + new SimpleDateFormat("yyyy-MM-dd").parse("2011-04-25");
		}
		catch (ParseException ex) {
			ex.printStackTrace();
			return null;
		}
	}
	
	@Override
	public String getUuidProperty() {
		return RestTestConstants1_9.PROVIDER_ATTRIBUTE_UUID;
	}
}
