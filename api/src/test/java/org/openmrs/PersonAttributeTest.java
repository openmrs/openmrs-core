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

import java.util.Date;
import org.junit.Assert;
import org.junit.Test;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.Verifies;

/**
 * Tests methods on the PersonAttribute class
 */
public class PersonAttributeTest extends BaseContextSensitiveTest {
	
	/**
	 * @see {@link PersonAttribute#toString()}
	 */
	@Test
	@Verifies(value = "should return toString of hydrated value", method = "toString()")
	public void toString_shouldReturnToStringOfHydratedValue() throws Exception {
		// type = CIVIL STATUS, concept = MARRIED
		PersonAttributeType type = Context.getPersonService().getPersonAttributeType(8);
		PersonAttribute attr = new PersonAttribute(type, "6");
		Assert.assertEquals("MARRIED", attr.toString());
	}
	
	/**
	 * @see {@link PersonAttribute#compareTo(PersonAttribute)}
	 */
	@Test
	@Verifies(value = "should return negative if other attribute is voided", method = "compareTo(PersonAttribute)")
	public void compareTo_shouldReturnNegativeIfOtherAttributeIsVoided() throws Exception {
		PersonAttribute pa = new PersonAttribute();
		pa.setAttributeType(new PersonAttributeType(1));
		PersonAttribute other = new PersonAttribute();
		other.setVoided(true);
		Assert.assertTrue(pa.compareTo(other) < 0);
	}
	
	/**
	 * @see {@link PersonAttribute#compareTo(PersonAttribute)}
	 */
	@Test
	@Verifies(value = "should return negative if other attribute has earlier date created", method = "compareTo(PersonAttribute)")
	public void compareTo_shouldReturnNegativeIfOtherAttributeHasEarlierDateCreated() throws Exception {
		PersonAttribute pa = new PersonAttribute();
		pa.setAttributeType(new PersonAttributeType(1));
		pa.setDateCreated(new Date());
		PersonAttribute other = new PersonAttribute();
		pa.setDateCreated(new Date(pa.getDateCreated().getTime() - 1000));
		Assert.assertTrue(pa.compareTo(other) < 0);
	}
	
	/**
	 * @see {@link PersonAttribute#compareTo(PersonAttribute)}
	 */
	@Test
	@Verifies(value = "should return negative if this attribute has lower attribute type than argument", method = "compareTo(PersonAttribute)")
	public void compareTo_shouldReturnNegativeIfThisAttributeHasLowerAttributeTypeThanArgument() throws Exception {
		PersonAttribute pa = new PersonAttribute();
		pa.setAttributeType(new PersonAttributeType(1));
		PersonAttribute other = new PersonAttribute();
		other.setAttributeType(new PersonAttributeType(2));
		
		Assert.assertTrue(pa.compareTo(other) < 0);
	}
	
	/**
	 * @see {@link PersonAttribute#compareTo(PersonAttribute)}
	 */
	@Test
	@Verifies(value = "should not throw exception if attribute type is null", method = "compareTo(PersonAttribute)")
	public void compareTo_shouldNotThrowExceptionIfAttributeTypeIdIsNull() throws Exception {
		Assert.assertTrue(new PersonAttribute(1).compareTo(new PersonAttribute(1)) == 0);
	}
	
	/**
	 * @see {@link PersonAttribute#compareTo(PersonAttribute)}
	 */
	@Test
	@Verifies(value = "should return negative if other attribute has lower value", method = "compareTo(PersonAttribute)")
	public void compareTo_shouldReturnNegativeIfOtherAttributeHasLowerValue() throws Exception {
		PersonAttribute pa = new PersonAttribute();
		pa.setAttributeType(new PersonAttributeType(1));
		pa.setValue("2");
		PersonAttribute other = new PersonAttribute();
		other.setAttributeType(new PersonAttributeType(1));
		other.setValue("1");
		
		Assert.assertTrue(pa.compareTo(other) > 0);
	}
	
	/**
	 * @see {@link PersonAttribute#compareTo(PersonAttribute)}
	 */
	@Test
	@Verifies(value = "should return negative if this attribute has lower attribute id than argument", method = "compareTo(PersonAttribute)")
	public void compareTo_shouldReturnNegativeIfThisAttributeHasLowerAttributeIdThanArgument() throws Exception {
		PersonAttribute pa = new PersonAttribute(1);
		pa.setAttributeType(new PersonAttributeType(1));
		PersonAttribute other = new PersonAttribute(2);
		other.setAttributeType(new PersonAttributeType(1));
		
		Assert.assertTrue(pa.compareTo(other) < 0);
	}
	
	/**
	 * @see {@link PersonAttribute#equalsContent(PersonAttribute)}
	 */
	@Test
	@Verifies(value = "should return true if attributeType value and void status are the same", method = "equalsContent(PersonAttribute)")
	public void equalsContent_shouldReturnTrueIfAttributeTypeValueAndVoidStatusAreTheSame() throws Exception {
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
	 * @see {@link PersonAttribute#getHydratedObject()}
	 */
	@Test
	@Verifies(value = "should load class in format property", method = "getHydratedObject()")
	public void getHydratedObject_shouldLoadClassInFormatProperty() throws Exception {
		PersonAttributeType type = new PersonAttributeType();
		type.setFormat("org.openmrs.Concept");
		
		PersonAttribute pa = new PersonAttribute(2);
		pa.setAttributeType(type);
		pa.setValue("5089");
		
		Concept concept = (Concept) pa.getHydratedObject();
		Assert.assertEquals(5089, concept.getConceptId().intValue());
	}
	
	/**
	 * @see {@link PersonAttribute#getHydratedObject()}
	 */
	@Test
	@Verifies(value = "should load user class in format property", method = "getHydratedObject()")
	public void getHydratedObject_shouldLoadUserClassInFormatProperty() throws Exception {
		PersonAttributeType type = new PersonAttributeType();
		type.setFormat("org.openmrs.User");
		
		PersonAttribute pa = new PersonAttribute(2);
		
		pa.setAttributeType(type);
		pa.setValue("1");
		
		Object value = pa.getHydratedObject();
		Assert.assertTrue("should load user class in format property", (value instanceof User));
	}
	
	/**
	 * @see {@link PersonAttribute#getHydratedObject()}
	 */
	@Test
	@Verifies(value = "should still load class in format property if not Attributable", method = "getHydratedObject()")
	public void getHydratedObject_shouldStillLoadClassInFormatPropertyIfNotAttributable() throws Exception {
		PersonAttributeType type = new PersonAttributeType();
		type.setFormat("java.lang.String");
		
		PersonAttribute pa = new PersonAttribute(2);
		
		pa.setAttributeType(type);
		pa.setValue("lalapalooza");
		
		String value = (String) pa.getHydratedObject();
		Assert.assertEquals("lalapalooza", value);
	}
	
	/**
	 * @see {@link PersonAttribute#voidAttribute(String)}
	 */
	@Test
	@Verifies(value = "should set voided bit to true", method = "voidAttribute(String)")
	public void voidAttribute_shouldSetVoidedBitToTrue() throws Exception {
		PersonAttribute pa = new PersonAttribute(2);
		pa.setVoided(false);
		pa.voidAttribute("Because");
		Assert.assertTrue(pa.isVoided());
	}
	
}
