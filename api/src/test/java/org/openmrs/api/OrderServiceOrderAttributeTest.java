/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.openmrs.DrugOrder;
import org.openmrs.Location;
import org.openmrs.OrderAttribute;
import org.openmrs.OrderAttributeType;
import org.openmrs.customdatatype.datatype.LocationDatatype;
import org.openmrs.test.BaseContextSensitiveTest;

/**
 * Tests OrderAttribute behavior for fulfiller location without persistence.
 */
public class OrderServiceOrderAttributeTest extends BaseContextSensitiveTest {

	@Test
	public void shouldAttachAndRetrieveFulfillerLocationAttributeOnOrder() {

		OrderAttributeType attributeType = new OrderAttributeType();
		attributeType.setName("Fulfiller Location");
		attributeType.setDatatypeClassname(LocationDatatype.class.getName());

		DrugOrder order = new DrugOrder();

		Location location = new Location();
		location.setUuid("test-location-uuid");

		OrderAttribute attribute = new OrderAttribute();
		attribute.setAttributeType(attributeType);

		order.addAttribute(attribute);
		attribute.setValue(location);

		assertNotNull(order.getAttributes());
		assertEquals(1, order.getAttributes().size());

		OrderAttribute savedAttribute = order.getAttributes().iterator().next();
		assertEquals(location, savedAttribute.getValue());
	}
}
