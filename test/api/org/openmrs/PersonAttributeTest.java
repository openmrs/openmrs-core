/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
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
	 * @see {@link PersonAttribute#equals(Object)}
	 */
	@Test
	@Verifies(value = "should return true if personAttributeIds match", method = "equals(Object)")
	public void equals_shouldReturnTrueIfPersonAttributeIdsMatch() throws Exception {
		PersonAttribute pa = new PersonAttribute(1);
		PersonAttribute other = new PersonAttribute(1);
		
		Assert.assertTrue(pa.equals(other));
	}
	
	/**
	 * @see {@link PersonAttribute#equals(Object)}
	 */
	@Test
	@Verifies(value = "should return false if personAttributeIds dont match", method = "equals(Object)")
	public void equals_shouldReturnFalseIfPersonAttributeIdsDontMatch() throws Exception {
		PersonAttribute pa = new PersonAttribute(2);
		PersonAttribute other = new PersonAttribute(1);
		
		Assert.assertFalse(pa.equals(other));
	}
	
	/**
	 * @see {@link PersonAttribute#equals(Object)}
	 */
	@Test
	@Verifies(value = "should match on object equality if a personAttributeId is null", method = "equals(Object)")
	public void equals_shouldMatchOnObjectEqualityIfAPersonAttributeIdIsNull() throws Exception {
		PersonAttribute pa = new PersonAttribute();
		
		Assert.assertTrue(pa.equals(pa));
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
