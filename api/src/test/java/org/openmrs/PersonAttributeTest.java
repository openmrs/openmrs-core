/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;

/**
 * Tests methods on the PersonAttribute class
 */
public class PersonAttributeTest extends BaseContextSensitiveTest {
	
	/**
	 * @see PersonAttribute#toString()
	 */
	@Test
	public void toString_shouldReturnToStringOfHydratedValue() {
		// type = CIVIL STATUS, concept = MARRIED
		PersonAttributeType type = Context.getPersonService().getPersonAttributeType(8);
		PersonAttribute attr = new PersonAttribute(type, "6");
		Assert.assertEquals("MARRIED", attr.toString());
	}
	
	/**
	 * @see PersonAttribute#equalsContent(PersonAttribute)
	 */
	@Test
	public void equalsContent_shouldReturnTrueIfAttributeTypeValueAndVoidStatusAreTheSame() {
		PersonAttribute pa = new PersonAttribute(2); // a different personAttributeid than below
		pa.setAttributeType(new PersonAttributeType(1));
		pa.setValue("1");
		pa.setVoided(false);
		PersonAttribute other = new PersonAttribute(1); // a different personAttributeid than above
		pa.setAttributeType(new PersonAttributeType(1));
		pa.setValue("1");
		pa.setVoided(false);
		
		Assert.assertTrue(pa.equalsContent(other));
	}
	
	/**
	 * @see PersonAttribute#getHydratedObject()
	 */
	@Test
	public void getHydratedObject_shouldLoadClassInFormatProperty() {
		PersonAttributeType type = new PersonAttributeType();
		type.setFormat("org.openmrs.Concept");
		
		PersonAttribute pa = new PersonAttribute(2);
		pa.setAttributeType(type);
		pa.setValue("5089");
		
		Concept concept = (Concept) pa.getHydratedObject();
		Assert.assertEquals(5089, concept.getConceptId().intValue());
	}
	
	/**
	 * @see PersonAttribute#getHydratedObject()
	 */
	@Test
	public void getHydratedObject_shouldLoadUserClassInFormatProperty() {
		PersonAttributeType type = new PersonAttributeType();
		type.setFormat("org.openmrs.User");
		
		PersonAttribute pa = new PersonAttribute(2);
		
		pa.setAttributeType(type);
		pa.setValue("1");
		
		Object value = pa.getHydratedObject();
		Assert.assertTrue("should load user class in format property", (value instanceof User));
	}
	
	/**
	 * @see PersonAttribute#getHydratedObject()
	 */
	@Test
	public void getHydratedObject_shouldStillLoadClassInFormatPropertyIfNotAttributable() {
		PersonAttributeType type = new PersonAttributeType();
		type.setFormat("java.lang.String");
		
		PersonAttribute pa = new PersonAttribute(2);
		
		pa.setAttributeType(type);
		pa.setValue("lalapalooza");
		
		String value = (String) pa.getHydratedObject();
		Assert.assertEquals("lalapalooza", value);
	}
	
	/**
	 * @see PersonAttribute#voidAttribute(String)
	 */
	@Test
	public void voidAttribute_shouldSetVoidedBitToTrue() {
		PersonAttribute pa = new PersonAttribute(2);
		pa.setVoided(false);
		pa.voidAttribute("Because");
		Assert.assertTrue(pa.getVoided());
	}
	
}
