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
import org.openmrs.OrderSetMember;
import org.openmrs.api.OrderSetService;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.RestTestConstants1_12;
import org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResourceTest;

public class OrderSetMemberResource1_12Test extends BaseDelegatingResourceTest<OrderSetMemberResource1_12, OrderSetMember> {
	
	private OrderSetService orderSetService;
	
	@Before
	public void init() throws Exception {
		orderSetService = Context.getOrderSetService();
		executeDataSet(RestTestConstants1_12.TEST_DATA_SET);
	}
	
	@Override
	public void validateDefaultRepresentation() throws Exception {
		super.validateDefaultRepresentation();
		assertPropEquals("name", getObject().getName());
		assertPropEquals("uuid", getObject().getUuid());
		assertPropEquals("description", getObject().getDescription());
		assertPropPresent("orderTemplateType");
	}
	
	@Override
	public void validateFullRepresentation() throws Exception {
		super.validateFullRepresentation();
		assertPropEquals("name", getObject().getName());
		assertPropEquals("description", getObject().getDescription());
		assertPropEquals("retired", getObject().getRetired());
		assertPropEquals("retireBy", getObject().getRetiredBy());
		assertPropPresent("auditInfo");
		assertPropPresent("orderTemplateType");
	}
	
	@Override
	public OrderSetMember newObject() {
		return orderSetService.getOrderSetMemberByUuid(RestTestConstants1_12.ORDER_SET_MEMBER_UUID);
	}
	
	@Override
	public String getDisplayProperty() {
		return null;
	}
	
	@Override
	public String getUuidProperty() {
		return RestTestConstants1_12.ORDER_SET_MEMBER_UUID;
	}
}
