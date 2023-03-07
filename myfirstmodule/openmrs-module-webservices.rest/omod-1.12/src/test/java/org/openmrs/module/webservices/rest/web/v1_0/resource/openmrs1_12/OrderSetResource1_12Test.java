/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_12;

import org.junit.Before;
import org.openmrs.OrderSet;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.RestTestConstants1_12;
import org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResourceTest;

/**
 * Contains tests for the {@link OrderSetResource1_12}
 */
public class OrderSetResource1_12Test extends BaseDelegatingResourceTest<OrderSetResource1_12, OrderSet> {
	
	@Before
	public void before() throws Exception {
		executeDataSet(RestTestConstants1_12.TEST_DATA_SET);
	}
	
	@Override
	public OrderSet newObject() {
		return Context.getOrderSetService().getOrderSetByUuid(getUuidProperty());
	}
	
	@Override
	public void validateDefaultRepresentation() throws Exception {
		super.validateDefaultRepresentation();
		assertPropEquals("name", getObject().getName());
		assertPropEquals("uuid", getObject().getUuid());
		assertPropEquals("description", getObject().getDescription());
		assertPropEquals("operator", getObject().getOperator());
	}
	
	@Override
	public void validateFullRepresentation() throws Exception {
		super.validateFullRepresentation();
		assertPropEquals("name", getObject().getName());
		assertPropEquals("description", getObject().getDescription());
		assertPropEquals("retired", getObject().getRetired());
		assertPropEquals("retireBy", getObject().getRetiredBy());
		assertPropPresent("auditInfo");
	}
	
	@Override
	public String getDisplayProperty() {
		return "orderSet1";
	}
	
	@Override
	public String getUuidProperty() {
		return RestTestConstants1_12.ORDER_SET_UUID;
	}
}
