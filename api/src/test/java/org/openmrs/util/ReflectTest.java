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
package org.openmrs.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.openmrs.BaseOpenmrsObject;
import org.openmrs.OpenmrsObject;
import org.openmrs.test.Verifies;

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
		
		Assert.assertEquals("genericCollectionField", allFields.get(0).getName());
		Assert.assertTrue(reflect.hasField(allFields.get(0)));
	}
	
	/**
	 * @see Reflect#hasField(Field)
	 */
	@Test
	@Verifies(value = "should return false if given field is not declared in parameterized class or its sub classes", method = "hasField(Field)")
	public void xhasField_shouldReturnFalseIfGivenFieldIsNotDeclaredInParameterizedClassOrItsSubClass() throws Exception {
		Reflect reflect = new Reflect(OpenmrsObject.class);
		List<Field> allFields = Reflect.getAllFields(OpenmrsObjectImp.class);
		
		Assert.assertEquals("normalClassField", allFields.get(3).getName());
		Assert.assertFalse(reflect.hasField(allFields.get(3)));
	}
	
	/**
	 * @see {@link Reflect#getAllFields(Class<*>)}
	 */
	@Test
	@Verifies(value = "should return all fields include private and super classes", method = "getAllFields(Class<*>)")
	public void getAllFields_shouldReturnAllFieldsIncludePrivateAndSuperClasses() throws Exception {
		List<Field> allFields = Reflect.getAllFields(OpenmrsObjectImp.class);
		
		Assert.assertEquals(4, allFields.size());
		Assert.assertEquals("subClassField", allFields.get(1).getName());
		Assert.assertEquals("normalClassField", allFields.get(3).getName());
	}
	
	/**
	 * @see {@link Reflect#isCollection(Class<*>)}
	 */
	@Test
	@Verifies(value = "should return false if given fieldClass is not a Collection class", method = "isCollection(Class<*>)")
	public void isCollection_shouldReturnFalseIfGivenFieldClassIsNotACollectionClass() throws Exception {
		Reflect reflect = new Reflect(OpenmrsObject.class);
		List<Field> allFields = Reflect.getAllFields(OpenmrsObjectImp.class);
		
		Assert.assertEquals("nonCollectionField", allFields.get(2).getName());
		Assert.assertFalse(reflect.isCollectionField(allFields.get(2)));
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
		
		Assert.assertEquals(3, fields.size());
		Assert.assertEquals("normalClassField", allFields.get(3).getName());
		Assert.assertFalse(fields.contains(allFields.get(3)));
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
		
		Assert.assertEquals("genericCollectionField", allFields.get(0).getName());
		Assert.assertFalse(reflect.isCollectionField(allFields.get(0)));
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
