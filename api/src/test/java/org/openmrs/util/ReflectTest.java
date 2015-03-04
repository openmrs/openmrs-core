/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.util;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.openmrs.BaseOpenmrsObject;
import org.openmrs.OpenmrsObject;
import org.openmrs.Visit;
import org.openmrs.VisitAttribute;
import org.openmrs.test.Verifies;
import org.springframework.util.ReflectionUtils;

/**
 * Tests the {@link Reflect} class.
 */
public class ReflectTest {
	
	/**
	 * @see Reflect#hasField(Field)
	 */
	@Test
	@Verifies(value = "should return true if given field is declared in parameterized class or its sub classes", method = "hasField(Field)")
	public void xhasField_shouldReturnTrueIfGivenFieldIsDeclaredInParameterizedClassOrSubClass() throws Exception {
		Reflect reflect = new Reflect(OpenmrsObject.class);
		List<Field> allFields = Reflect.getAllFields(OpenmrsObjectImp.class);
		
		Field genericCollectionField = findFieldByName(allFields, "genericCollectionField");
		Assert.assertTrue(reflect.hasField(genericCollectionField));
	}
	
	/**
	 * @see Reflect#hasField(Field)
	 */
	@Test
	@Verifies(value = "should return false if given field is not declared in parameterized class or its sub classes", method = "hasField(Field)")
	public void xhasField_shouldReturnFalseIfGivenFieldIsNotDeclaredInParameterizedClassOrItsSubClass() throws Exception {
		Reflect reflect = new Reflect(OpenmrsObject.class);
		List<Field> allFields = Reflect.getAllFields(OpenmrsObjectImp.class);
		
		Field normalClassField = findFieldByName(allFields, "normalClassField");
		
		Assert.assertFalse(reflect.hasField(normalClassField));
	}
	
	private Field findFieldByName(List<Field> fields, String name) {
		for (Field field : fields) {
			if (name.equals(field.getName())) {
				return field;
			}
		}
		throw new IllegalArgumentException("Field not found!");
	}
	
	/**
	 * @see {@link Reflect#getAllFields(Class<*>)}
	 */
	@Test
	@Verifies(value = "should return all fields include private and super classes", method = "getAllFields(Class<*>)")
	public void getAllFields_shouldReturnAllFieldsIncludePrivateAndSuperClasses() throws Exception {
		List<Field> allFields = Reflect.getAllFields(OpenmrsObjectImp.class);
		
		findFieldByName(allFields, "subClassField");
		findFieldByName(allFields, "normalClassField");
		findFieldByName(allFields, "nonCollectionField");
		findFieldByName(allFields, "genericCollectionField");
	}
	
	/**
	 * @see {@link Reflect#isCollection(Class<*>)}
	 */
	@Test
	@Verifies(value = "should return false if given fieldClass is not a Collection class", method = "isCollection(Class<*>)")
	public void isCollection_shouldReturnFalseIfGivenFieldClassIsNotACollectionClass() throws Exception {
		Reflect reflect = new Reflect(OpenmrsObject.class);
		List<Field> allFields = Reflect.getAllFields(OpenmrsObjectImp.class);
		
		Field nonCollectionField = findFieldByName(allFields, "nonCollectionField");
		Assert.assertFalse(reflect.isCollectionField(nonCollectionField));
	}
	
	/**
	 * @see {@link Reflect#isCollection(Class<*>)}
	 */
	@Test
	@Verifies(value = "should return true if given fieldClass is Collection class", method = "isCollection(Class<*>)")
	public void isCollection_shouldReturnTrueIfGivenFieldClassIsCollectionClass() throws Exception {
		Assert.assertTrue(Reflect.isCollection(ArrayList.class));
	}
	
	/**
	 * @see {@link Reflect#isCollection(Object)}
	 */
	@Test
	@Verifies(value = "should return false if given object is not a Collection", method = "isCollection(Object)")
	public void isCollection_shouldReturnFalseIfGivenObjectIsNotACollection() throws Exception {
		Assert.assertFalse(Reflect.isCollection(new NormalClass()));
	}
	
	/**
	 * @see {@link Reflect#isCollection(Object)}
	 */
	@Test
	@Verifies(value = "should return true if given object is Collection class", method = "isCollection(Object)")
	public void isCollection_shouldReturnTrueIfGivenObjectIsCollectionClass() throws Exception {
		Assert.assertTrue(Reflect.isCollection(new ArrayList<Object>()));
	}
	
	/**
	 * @see {@link Reflect#Reflect(Class)}
	 */
	@Test(expected = NullPointerException.class)
	@Verifies(value = "should throw exception when null is passed", method = "Reflect(Class)")
	public void Reflect_shouldThrowExceptionWhenNullIsPassed() throws Exception {
		new Reflect(null);
	}
	
	/**
	 * @see {@link Reflect#getInheritedFields(Class)}
	 */
	@Test
	@Verifies(value = "should return only the sub class fields of given parameterized class", method = "getInheritedFields(Class)")
	public void getInheritedFields_shouldReturnOnlyTheSubClassFieldsOfGivenParameterizedClass() throws Exception {
		Reflect reflect = new Reflect(OpenmrsObject.class);
		List<Field> fields = reflect.getInheritedFields(OpenmrsObjectImp.class);
		
		List<Field> allFields = Reflect.getAllFields(OpenmrsObjectImp.class);
		
		findFieldByName(fields, "subClassField");
		findFieldByName(fields, "nonCollectionField");
		findFieldByName(fields, "genericCollectionField");
		
		Field normalClassField = findFieldByName(allFields, "normalClassField");
		Assert.assertFalse(fields.contains(normalClassField));
	}
	
	/**
	 * @see {@link Reflect#isCollectionField(Field)}
	 */
	@Test
	@Verifies(value = "should return true if given field is Collection and its element type is given parameterized", method = "isCollectionField(Field)")
	public void isCollectionField_shouldReturnTrueIfGivenFieldIsCollectionAndItsElementTypeIsGivenParameterized()
	        throws Exception {
		Reflect reflect = new Reflect(OpenmrsObject.class);
		List<Field> allFields = Reflect.getAllFields(OpenmrsObjectImp.class);
		
		Assert.assertEquals("subClassField", allFields.get(1).getName());
		Assert.assertTrue(reflect.isCollectionField(allFields.get(1)));
	}
	
	/**
	 * @see {@link Reflect#isCollectionField(Field)}
	 */
	@Test
	@Verifies(value = "should return false if given field is not a Collection", method = "isCollectionField(Field)")
	public void isCollectionField_shouldReturnFalseIfGivenFieldIsNotACollection() throws Exception {
		Assert.assertFalse(Reflect.isCollection(NormalClass.class));
	}
	
	/**
	 * @see {@link Reflect#isCollectionField(Field)}
	 */
	@Test
	@Verifies(value = "should return false if given field is Collection and element type is other than given", method = "isCollectionField(Field)")
	public void isCollectionField_shouldReturnFalseIfGivenFieldIsCollectionAndElementTypeIsOtherThanGiven() throws Exception {
		Reflect reflect = new Reflect(OpenmrsObject.class);
		List<Field> allFields = Reflect.getAllFields(OpenmrsObjectImp.class);
		
		Field genericCollectionField = findFieldByName(allFields, "genericCollectionField");
		Assert.assertFalse(reflect.isCollectionField(genericCollectionField));
	}
	
	/**
	 * @see {@link Reflect#isSuperClass(Class)}
	 */
	@Test
	@Verifies(value = "should return false if given subClass is not accessible from given parameterized class", method = "isSuperClass(Class)")
	public void isSuperClass_shouldReturnFalseIfGivenSubClassIsNotAccessibleFromGivenParameterizedClass() throws Exception {
		Reflect reflect = new Reflect(OpenmrsObject.class);
		
		Assert.assertFalse(reflect.isSuperClass(new NormalClass()));
	}
	
	/**
	 * @see {@link Reflect#isSuperClass(Class)}
	 */
	@Test
	@Verifies(value = "should return true if given subClass is accessible from given parameterized class", method = "isSuperClass(Class)")
	public void isSuperClass_shouldReturnTrueIfGivenSubClassIsAccessibleFromGivenParameterizedClass() throws Exception {
		Reflect reflect = new Reflect(OpenmrsObject.class);
		
		Assert.assertTrue(reflect.isSuperClass(OpenmrsObjectImp.class));
	}
	
	/**
	 * @see {@link Reflect#isSuperClass(Object)}
	 */
	@Test
	@Verifies(value = "should return false if given object is not accessible from given parameterized class", method = "isSuperClass(Object)")
	public void isSuperClass_shouldReturnFalseIfGivenObjectIsNotAccessibleFromGivenParameterizedClass() throws Exception {
		Reflect reflect = new Reflect(OpenmrsObject.class);
		
		Assert.assertFalse(reflect.isSuperClass(NormalClass.class));
	}
	
	/**
	 * @see {@link Reflect#isSuperClass(Object)}
	 */
	@Test
	@Verifies(value = "should return true if given object is accessible from given parameterized class", method = "isSuperClass(Object)")
	public void isSuperClass_shouldReturnTrueIfGivenObjectIsAccessibleFromGivenParameterizedClass() throws Exception {
		Reflect reflect = new Reflect(OpenmrsObject.class);
		
		Assert.assertTrue(reflect.isSuperClass(new OpenmrsObjectImp()));
	}
	
	/**
	 * @see Reflect#isSuperClass(Type)
	 * @verifies return true for a generic whose bound is a subclass
	 */
	@Test
	public void isSuperClass_shouldReturnTrueForAGenericWhoseBoundIsASubclass() throws Exception {
		Reflect reflect = new Reflect(OpenmrsObject.class);
		Field field = ReflectionUtils.findField(Visit.class, "attributes");
		ParameterizedType setOfAttr = (ParameterizedType) field.getGenericType();
		Type genericType = setOfAttr.getActualTypeArguments()[0];
		Assert.assertTrue(reflect.isSuperClass(genericType));
	}
	
	/**
	 * @see Reflect#isSuperClass(Type)
	 * @verifies return false for a generic whose bound is not a subclass
	 */
	@Test
	public void isSuperClass_shouldReturnFalseForAGenericWhoseBoundIsNotASubclass() throws Exception {
		Reflect reflect = new Reflect(Number.class);
		Field field = ReflectionUtils.findField(Visit.class, "attributes");
		ParameterizedType setOfAttr = (ParameterizedType) field.getGenericType();
		Type genericType = setOfAttr.getActualTypeArguments()[0];
		Assert.assertFalse(reflect.isSuperClass(genericType));
	}
	
}

class NormalClass {
	
	private String normalClassField;
	
	public String getNormalClassField() {
		return normalClassField;
	}
	
	public void setNormalClassField(String normalClassField) {
		this.normalClassField = normalClassField;
	}
}

class OpenmrsObjectImp extends NormalClass implements OpenmrsObject {
	
	protected Collection<BaseOpenmrsObject> subClassField;
	
	@SuppressWarnings("unused")
	private String nonCollectionField;
	
	@SuppressWarnings("unchecked")
	Collection genericCollectionField;
	
	public Integer getId() {
		return null;
	}
	
	public String getUuid() {
		return null;
	}
	
	public void setId(Integer id) {
	}
	
	public void setUuid(String uuid) {
	}
	
}
