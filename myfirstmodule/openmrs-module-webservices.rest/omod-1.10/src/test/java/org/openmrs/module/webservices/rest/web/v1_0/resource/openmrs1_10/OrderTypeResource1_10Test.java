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

import org.openmrs.OrderType;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.RestTestConstants1_10;
import org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResourceTest;

public class OrderTypeResource1_10Test extends BaseDelegatingResourceTest<OrderTypeResource1_10, OrderType> {
	
	@Override
	public OrderType newObject() {
		return Context.getOrderService().getOrderTypeByUuid(getUuidProperty());
	}
	
	@Override
	public String getDisplayProperty() {
		return "Test order";
	}
	
	@Override
	public String getUuidProperty() {
		return RestTestConstants1_10.ORDER_TYPE_UUID;
	}
	
	@Override
	public void validateDefaultRepresentation() throws Exception {
		super.validateDefaultRepresentation();
		assertPropEquals("name", getObject().getName());
		assertPropEquals("description", getObject().getDescription());
		assertPropEquals("javaClassName", getObject().getJavaClassName());
		assertPropEquals("retired", getObject().isRetired());
		assertPropPresent("parent");
		assertPropPresent("conceptClasses");
		assertPropNotPresent("auditInfo");
	}
	
	@Override
	public void validateFullRepresentation() throws Exception {
		super.validateFullRepresentation();
		assertPropEquals("name", getObject().getName());
		assertPropEquals("description", getObject().getDescription());
		assertPropEquals("javaClassName", getObject().getJavaClassName());
		assertPropEquals("retired", getObject().isRetired());
		assertPropPresent("parent");
		assertPropPresent("conceptClasses");
		assertPropPresent("auditInfo");
		
	}
	
}
