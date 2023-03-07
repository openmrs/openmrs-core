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
import org.openmrs.Order;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.RestTestConstants1_10;
import org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResourceTest;

public class OrderResource1_10Test extends BaseDelegatingResourceTest<OrderResource1_10, Order> {
	
	protected static final String ORDER_ENTRY_DATASET_XML = "org/openmrs/api/include/OrderEntryIntegrationTest-other.xml";
	
	@Before
	public void before() throws Exception {
		executeDataSet(ORDER_ENTRY_DATASET_XML);
	}
	
	/**
	 * @see BaseDelegatingResourceTest#validateDefaultRepresentation()
	 */
	@Override
	public void validateDefaultRepresentation() throws Exception {
		super.validateDefaultRepresentation();
		assertPropPresent("orderNumber");
		assertPropPresent("accessionNumber");
		assertPropPresent("patient");
		assertPropPresent("concept");
		assertPropPresent("action");
		assertPropPresent("careSetting");
		assertPropPresent("previousOrder");
		assertPropPresent("dateActivated");
		assertPropPresent("scheduledDate");
		assertPropPresent("dateStopped");
		assertPropPresent("autoExpireDate");
		assertPropPresent("encounter");
		assertPropPresent("orderer");
		assertPropPresent("orderReason");
		assertPropPresent("orderReasonNonCoded");
		assertPropPresent("orderType");
		assertPropPresent("urgency");
		assertPropPresent("instructions");
		assertPropPresent("commentToFulfiller");
		assertPropPresent("display");
	}
	
	/**
	 * @see BaseDelegatingResourceTest#validateFullRepresentation()
	 */
	@Override
	public void validateFullRepresentation() throws Exception {
		super.validateFullRepresentation();
		assertPropPresent("orderNumber");
		assertPropPresent("accessionNumber");
		assertPropPresent("patient");
		assertPropPresent("concept");
		assertPropPresent("action");
		assertPropPresent("careSetting");
		assertPropPresent("previousOrder");
		assertPropPresent("dateActivated");
		assertPropPresent("scheduledDate");
		assertPropPresent("dateStopped");
		assertPropPresent("autoExpireDate");
		assertPropPresent("encounter");
		assertPropPresent("orderer");
		assertPropPresent("orderReason");
		assertPropPresent("orderReasonNonCoded");
		assertPropPresent("orderType");
		assertPropPresent("urgency");
		assertPropPresent("instructions");
		assertPropPresent("commentToFulfiller");
		assertPropPresent("display");
		assertPropPresent("auditInfo");
	}
	
	@Override
	public Order newObject() {
		return Context.getOrderService().getOrderByUuid(getUuidProperty());
	}
	
	@Override
	public String getDisplayProperty() {
		return "CD4 COUNT";
	}
	
	@Override
	public String getUuidProperty() {
		return RestTestConstants1_10.ORDER_UUID;
	}
	
}
