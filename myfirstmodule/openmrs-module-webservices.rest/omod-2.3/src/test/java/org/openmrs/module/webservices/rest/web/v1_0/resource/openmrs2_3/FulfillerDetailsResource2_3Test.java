/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs2_3;

import org.openmrs.Order;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResourceTest;
import org.openmrs.module.webservices.rest.web.v1_0.RestTestConstants2_3;

public class FulfillerDetailsResource2_3Test extends BaseDelegatingResourceTest<FulfillerDetailsResource2_3, FulfillerDetails2_3> {

	private String fulfillerComment = "Some example comment";

	private String accessionNumber = "123-abc";

	private Order.FulfillerStatus fulfillerStatus = Order.FulfillerStatus.RECEIVED;

	/**
	 * @see BaseDelegatingResourceTest#newObject()
	 */

	@Override
	public FulfillerDetails2_3 newObject() {

		Order order = Context.getOrderService().getOrderByUuid(RestTestConstants2_3.ORDER_UUID);
		FulfillerDetails2_3 fillerDetailsRepresentation = new FulfillerDetails2_3();
		fillerDetailsRepresentation.setOrder(order);
		fillerDetailsRepresentation.setFulfillerComment(fulfillerComment);
		fillerDetailsRepresentation.setFulfillerStatus(fulfillerStatus);
		fillerDetailsRepresentation.setAccessionNumber(accessionNumber);

		return fillerDetailsRepresentation;
	}

	/**
	 * @see BaseDelegatingResourceTest#validateDefaultRepresentation()
	 */
	@Override
	public void validateDefaultRepresentation() throws Exception {
		assertPropEquals("fulfillerStatus", fulfillerStatus);
		assertPropEquals("fulfillerComment", fulfillerComment);
		assertPropEquals("accessionNumber", accessionNumber);
	}

	/**
	 * @see BaseDelegatingResourceTest#validateFullRepresentation()
	 */
	public void validateFullRepresentation() throws Exception {
		assertPropEquals("fulfillerStatus", fulfillerStatus);
		assertPropEquals("fulfillerComment", fulfillerComment);
        assertPropEquals("accessionNumber", accessionNumber);
	}

	/**
	 * @see BaseDelegatingResourceTest#validateRefRepresentation()
	 */
	public void validateRefRepresentation() throws Exception {
		assertPropEquals("fulfillerStatus", fulfillerStatus);
		assertPropEquals("fulfillerComment", fulfillerComment);
        assertPropEquals("accessionNumber", accessionNumber);
	}

	@Override
	public String getDisplayProperty() {
		return null;
	}

	@Override
	public String getUuidProperty() {
		return null;
	}
}
