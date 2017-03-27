/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.customdatatype;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.iterableWithSize;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;

import org.junit.Test;
import org.openmrs.attribute.BaseAttribute;
import org.openmrs.attribute.BaseAttributeType;
import org.openmrs.customdatatype.Customizable;

public class CustomizableTest {
	
	/**
	 * @see Customizable#addAttribute(A)
	 * @verifies add attribute if attributes collection is empty
	 */
	@Test
	public void addAttribute_shouldAddAttributeIfAttributesCollectionIsEmpty() throws Exception {
		SimpleCustomizable customizable = new SimpleCustomizable();
		assertThat(customizable.getAttributes(), empty());
		
		SimpleAttribute attribute = new SimpleAttribute();
		customizable.addAttribute(attribute);
		
		assertThat(customizable.getAttributes(), allOf(iterableWithSize(1), hasItem(attribute)));
	}
	
	/**
	 * @see Customizable#addAttribute(A)
	 * @verifies add attribute if attributes collection is not empty
	 */
	@Test
	public void addAttribute_shouldAddAttributeIfAttributesCollectionIsNotEmpty() throws Exception {
		SimpleCustomizable customizable = new SimpleCustomizable();
		customizable.setAttributes(new HashSet<>());
		
		assertThat(customizable.getAttributes(), empty());
		
		SimpleAttribute attribute = new SimpleAttribute();
		customizable.addAttribute(attribute);
		
		assertThat(customizable.getAttributes(), allOf(iterableWithSize(1), hasItem(attribute)));
	}
	
	/**
	 * @see Customizable#getActiveAttributes()
	 * @verifies return empty collection if attributes collection is empty
	 */
	@Test
	public void getActiveAttributes_shouldReturnEmptyCollectionIfAttributesCollectionIsEmpty() throws Exception {
		SimpleCustomizable customizable = new SimpleCustomizable();
		Collection<SimpleAttribute> activeAttributes = customizable.getActiveAttributes();
		
		assertThat(activeAttributes, empty());
	}
	
	/**
	 * @see Customizable#getActiveAttributes()
	 * @verifies return only non voided attributes
	 */
	@Test
	public void getActiveAttributes_shouldReturnOnlyNonVoidedAttributes() throws Exception {
		SimpleCustomizable customizable = new SimpleCustomizable();
		
		SimpleAttribute nonVoidedAttribute1 = new SimpleAttribute();
		customizable.addAttribute(nonVoidedAttribute1);
		
		SimpleAttribute nonVoidedAttribute2 = new SimpleAttribute();
		customizable.addAttribute(nonVoidedAttribute2);
		
		SimpleAttribute voidedAttribute1 = new SimpleVoidedAttribute();
		customizable.addAttribute(voidedAttribute1);
		
		SimpleAttribute voidedAttribute2 = new SimpleVoidedAttribute();
		customizable.addAttribute(voidedAttribute2);
		
		Collection<SimpleAttribute> activeAttributes = customizable.getActiveAttributes();
		
		assertThat(activeAttributes, allOf(iterableWithSize(2), hasItems(nonVoidedAttribute1, nonVoidedAttribute2)));
		
	}
	
	/**
	 * @see Customizable#getActiveAttributes(CustomValueDescriptor)
	 * @verifies return empty collection if attributes collection is empty
	 */
	@Test
	public void getActiveAttributesOfType_shouldReturnEmptyCollectionIfAttributesCollectionIsEmpty() throws Exception {
		SimpleCustomizable customizable = new SimpleCustomizable();
		SimpleAttributeType attributeType = new SimpleAttributeType();
		Collection<SimpleAttribute> activeAttributes = customizable.getActiveAttributes(attributeType);
		
		assertThat(activeAttributes, empty());
	}
	
	/**
	 * @see Customizable#getActiveAttributes(CustomValueDescriptor)
	 * @verifies return only non voided attributes of type
	 */
	@Test
	public void getActiveAttributesOfType_shouldReturnOnlyNonVoidedAttributesOfType() throws Exception {
		SimpleCustomizable customizable = new SimpleCustomizable();
		
		SimpleAttributeType attributeType = new SimpleAttributeType();
		SimpleAttributeType2 attributeType2 = new SimpleAttributeType2();
		
		SimpleAttribute nonVoidedAttribute1 = new SimpleAttribute();
		nonVoidedAttribute1.setAttributeType(attributeType);
		customizable.addAttribute(nonVoidedAttribute1);
		
		SimpleAttribute nonVoidedAttribute2 = new SimpleAttribute();
		nonVoidedAttribute2.setAttributeType(attributeType);
		customizable.addAttribute(nonVoidedAttribute2);
		
		SimpleAttribute voidedAttribute1 = new SimpleVoidedAttribute();
		voidedAttribute1.setAttributeType(attributeType);
		customizable.addAttribute(voidedAttribute1);
		
		SimpleAttribute voidedAttribute2 = new SimpleVoidedAttribute();
		voidedAttribute2.setAttributeType(attributeType);
		customizable.addAttribute(voidedAttribute2);
		
		Collection<SimpleAttribute> activeAttributes = customizable.getActiveAttributes(attributeType);
		assertThat(activeAttributes, allOf(iterableWithSize(2), hasItems(nonVoidedAttribute1, nonVoidedAttribute2)));
		
		activeAttributes = customizable.getActiveAttributes(attributeType2);
		assertThat(activeAttributes, empty());
	}
	
	/**
	 * @see Customizable#setAttribute(A)
	 * @verifies add attribute if attribute collection is null
	 */
	@Test
	public void setAttribute_shouldAddAttributeIfAttributeCollectionIsNull() throws Exception {
		SimpleCustomizable customizable = new SimpleCustomizable();
		
		SimpleAttribute attribute = new SimpleAttribute();
		customizable.setAttribute(attribute);
		
		assertThat(customizable.getAttributes(), allOf(iterableWithSize(1), hasItem(attribute)));
	}
	
	/**
	 * @see Customizable#setAttribute(A)
	 * @verifies do nothing if has single existing attribute of same type and value
	 */
	@Test
	public void setAttribute_shouldDoNothingIfHasSingleExistingAttributeOfSameTypeAndValue() throws Exception {
		final Object value = "value";
		
		SimpleCustomizable customizable = new SimpleCustomizable();
		
		SimpleAttribute attribute = new SimpleAttribute();
		attribute.setValue(value);
		SimpleAttributeType attributeType = new SimpleAttributeType();
		attribute.setAttributeType(attributeType);
		customizable.addAttribute(attribute);
		
		SimpleAttribute newAttribute = new SimpleAttribute();
		newAttribute.setAttributeType(attributeType);
		newAttribute.setValue(value);
		customizable.setAttribute(newAttribute);
		
		assertThat(customizable.getAttributes(), allOf(iterableWithSize(1), hasItem(attribute)));
	}
	
	/**
	 * @see Customizable#setAttribute(A)
	 * @verifies remove existing attribute with id null and add attribute
	 */
	@Test
	public void setAttribute_shouldRemoveExistingAttributeWithIdNullAndAddAttribute() throws Exception {
		final Object value = "value";
		final Object otherValue = "otherValue";
		
		SimpleCustomizable customizable = new SimpleCustomizable();
		
		SimpleAttribute attribute = new SimpleAttribute();
		attribute.setValue(value);
		SimpleAttributeType attributeType = new SimpleAttributeType();
		attribute.setAttributeType(attributeType);
		customizable.addAttribute(attribute);
		
		SimpleAttribute newAttribute = new SimpleAttribute();
		newAttribute.setAttributeType(attributeType);
		newAttribute.setValue(otherValue);
		customizable.setAttribute(newAttribute);
		
		assertThat(customizable.getAttributes(), allOf(iterableWithSize(1), hasItem(newAttribute)));
	}
	
	/**
	 * @see Customizable#setAttribute(A)
	 * @verifies void the attribute if an attribute with same attribute type already exists and add
	 */
	@Test
	public void setAttribute_shouldVoidTheAttributeIfAnAttributeWithSameAttributeTypeAlreadyExistsAndAdd() throws Exception {
		final Object value = "value";
		final Object otherValue = "otherValue";
		final int id = 1;
		
		SimpleCustomizable customizable = new SimpleCustomizable();
		
		SimpleAttribute attribute = new SimpleAttribute();
		attribute.setId(id);
		attribute.setValue(value);
		SimpleAttributeType attributeType = new SimpleAttributeType();
		attribute.setAttributeType(attributeType);
		customizable.addAttribute(attribute);
		
		SimpleAttribute newAttribute = new SimpleAttribute();
		newAttribute.setAttributeType(attributeType);
		newAttribute.setValue(otherValue);
		customizable.setAttribute(newAttribute);
		
		assertThat(customizable.getAttributes(), allOf(iterableWithSize(2), hasItems(attribute, newAttribute)));
		assertThat(customizable.getActiveAttributes(), allOf(iterableWithSize(1), hasItem(newAttribute)));
		assertTrue("existing attribute should be voided", attribute.getVoided());
	}
	
	/**
	 * @see Customizable#setAttribute(A)
	 * @verifies set all existing attributes with non null id to voided and add attribute
	 */
	@Test
	public void setAttribute_shouldSetAllExistingAttributesWithNonNullIdToVoidedAndAddAttribute() throws Exception {
		final Object value = "value";
		final Object otherValue = "otherValue";
		final int id = 1;
		final int id2 = 1;
		
		SimpleCustomizable customizable = new SimpleCustomizable();
		SimpleAttributeType attributeType = new SimpleAttributeType();
		
		SimpleAttribute attributeWithId = new SimpleAttribute();
		attributeWithId.setId(id);
		attributeWithId.setValue(value);
		attributeWithId.setAttributeType(attributeType);
		customizable.addAttribute(attributeWithId);
		
		SimpleAttribute attribute2WithId = new SimpleAttribute();
		attribute2WithId.setId(id2);
		attribute2WithId.setValue(value);
		attribute2WithId.setAttributeType(attributeType);
		customizable.addAttribute(attribute2WithId);
		
		SimpleAttribute newAttribute = new SimpleAttribute();
		newAttribute.setAttributeType(attributeType);
		newAttribute.setValue(otherValue);
		customizable.setAttribute(newAttribute);
		
		assertThat(customizable.getAttributes(),
		    allOf(iterableWithSize(3), hasItems(attributeWithId, attribute2WithId, newAttribute)));
		assertThat(customizable.getActiveAttributes(), allOf(iterableWithSize(1), hasItem(newAttribute)));
		assertTrue("existing attribute should be voided", attributeWithId.getVoided());
		assertTrue("existing attribute should be voided", attribute2WithId.getVoided());
	}
	
	/**
	 * @see Customizable#setAttribute(A)
	 * @verifies set all existing attributes with non null id to voided and remove attribute with
	 *           null
	 */
	@Test
	public void setAttribute_shouldSetAllExistingAttributesWithNonNullIdToVoidedAndRemoveAttributeWithNull()
	    throws Exception {
		final Object value = "value";
		final Object otherValue = "otherValue";
		final int id = 1;
		
		SimpleCustomizable customizable = new SimpleCustomizable();
		SimpleAttributeType attributeType = new SimpleAttributeType();
		
		SimpleAttribute attributeWithId = new SimpleAttribute();
		attributeWithId.setId(id);
		attributeWithId.setValue(value);
		attributeWithId.setAttributeType(attributeType);
		customizable.addAttribute(attributeWithId);
		
		SimpleAttribute attribute2WithIdNull = new SimpleAttribute();
		attribute2WithIdNull.setId(null);
		attribute2WithIdNull.setValue(value);
		attribute2WithIdNull.setAttributeType(attributeType);
		customizable.addAttribute(attribute2WithIdNull);
		
		SimpleAttribute newAttribute = new SimpleAttribute();
		newAttribute.setAttributeType(attributeType);
		newAttribute.setValue(otherValue);
		customizable.setAttribute(newAttribute);
		
		assertThat(customizable.getAttributes(), allOf(iterableWithSize(2), hasItems(attributeWithId, newAttribute)));
		assertThat(customizable.getActiveAttributes(), allOf(iterableWithSize(1), hasItem(newAttribute)));
		assertTrue("existing attribute should be voided", attributeWithId.getVoided());
		
	}
	
	private static class SimpleCustomizable implements Customizable<SimpleAttribute> {
		
		private Collection<SimpleAttribute> attributes = new LinkedHashSet<SimpleAttribute>();
		
		@Override
		public Collection<SimpleAttribute> getAttributes() {
			return attributes;
		}
		
		@Override
		public void setAttributes(Collection<SimpleAttribute> attributes) {
			this.attributes = attributes;
		}
		
	}
	
	private static class SimpleAttribute extends BaseAttribute<SimpleAttributeType, SimpleCustomizable> {
		
		private Integer id;
		
		@Override
		public Integer getId() {
			return id;
		}
		
		@Override
		public void setId(Integer id) {
			this.id = id;
		}
	}
	
	private static class SimpleVoidedAttribute extends SimpleAttribute {
		
		SimpleVoidedAttribute() {
			setVoided(true);
		}
	}
	
	private static class SimpleAttributeType extends BaseAttributeType<SimpleCustomizable> {
		
		private Integer id;
		
		@Override
		public Integer getId() {
			return id;
		}
		
		@Override
		public void setId(Integer id) {
			this.id = id;
		}
	}
	
	private static class SimpleAttributeType2 extends BaseAttributeType<SimpleCustomizable> {
		
		private Integer id;
		
		@Override
		public Integer getId() {
			return id;
		}
		
		@Override
		public void setId(Integer id) {
			this.id = id;
		}
	}
}
