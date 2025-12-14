package org.openmrs.api;

import static org.junit.Assert.*;

import org.junit.Test;
import org.openmrs.*;
import org.openmrs.customdatatype.datatype.LocationDatatype;
import org.openmrs.test.BaseContextSensitiveTest;

/**
 * Tests OrderAttribute behavior for fulfillerLocation WITHOUT persistence.
 * Persistence is intentionally avoided due to valueReference limitations.
 */
public class OrderServiceOrderAttributeTest extends BaseContextSensitiveTest {

	@Test
	public void shouldAttachAndRetrieveFulfillerLocationAttributeOnOrder() {

		// Attribute type
		OrderAttributeType attributeType = new OrderAttributeType();
		attributeType.setName("Fulfiller Location");
		attributeType.setDatatypeClassname(LocationDatatype.class.getName());

		// Order (no DB save)
		DrugOrder order = new DrugOrder();

		// Location
		Location location = new Location();
		location.setUuid("test-location-uuid");

		// Attribute
		OrderAttribute attribute = new OrderAttribute();
		attribute.setAttributeType(attributeType);

		// Correct lifecycle
		order.addAttribute(attribute);
		attribute.setValue(location);

		// Verify
		OrderAttribute retrieved =
			order.getAttributes().iterator().next();

		assertNotNull(retrieved);
		assertEquals(location, retrieved.getValue());
	}
}
