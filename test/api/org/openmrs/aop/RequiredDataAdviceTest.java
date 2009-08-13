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
package org.openmrs.aop;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.BaseOpenmrsObject;
import org.openmrs.Concept;
import org.openmrs.ConceptNumeric;
import org.openmrs.Location;
import org.openmrs.OpenmrsObject;
import org.openmrs.api.APIException;
import org.openmrs.api.impl.ConceptServiceImpl;
import org.openmrs.test.Verifies;

/**
 * Tests the {@link RequiredDataAdvice} class.
 */
public class RequiredDataAdviceTest {
	
	/**
	 * Class that extends {@link OpenmrsObject} so can
	 */
	private class MiniOpenmrsObject extends BaseOpenmrsObject {
		
		private List<Location> locations;

		public List<Location> getLocations() {
			return locations;
		}
		
		public void setLocations(List<Location> locs) {
			this.locations = locs;
		}

		public Integer getId() {
			return null;
		}
		
		public void setId(Integer id) {
		}
	}
	
	/**
	 * @see RequiredDataAdvice#getAllInheritedFields(Class, List)
	 */
	@Test
	@Verifies(value = "should get all declared fields on given class", method = "getAllInheritedFields(Class,List)")
	public void getAllInheritedFields_shouldGetAllDeclaredFieldsOnGivenClass() throws Exception {
		List<Field> fields = new ArrayList<Field>();
		RequiredDataAdvice.getAllInheritedFields(MiniOpenmrsObject.class, fields);
		Assert.assertEquals(3, fields.size());
	}
	
	/**
	 * @see {@link RequiredDataAdvice#getAllInheritedFields(Class,List)}
	 */
	@Test
	@Verifies(value = "should get all declared fields on parent class as well", method = "getAllInheritedFields(Class,List)")
	public void getAllInheritedFields_shouldGetAllDeclaredFieldsOnParentClassAsWell() throws Exception {
		List<Field> fields = new ArrayList<Field>();
		RequiredDataAdvice.getAllInheritedFields(ConceptNumeric.class, fields);
		Field conceptNamesField = Concept.class.getDeclaredField("names");
		Assert.assertTrue(fields.contains(conceptNamesField));
	}
	
	/**
	 * @see {@link RequiredDataAdvice#getChildCollection(OpenmrsObject,Field)}
	 */
	@Test
	@Verifies(value = "should get value of given child collection on given field", method = "getChildCollection(OpenmrsObject,Field)")
	public void getChildCollection_shouldGetValueOfGivenChildCollectionOnGivenField() throws Exception {
		MiniOpenmrsObject oo = new MiniOpenmrsObject();
		List<Location> locs = new ArrayList<Location>();
		locs.add(new Location(1));
		oo.setLocations(locs);
		Collection<OpenmrsObject> fetchedLocations = RequiredDataAdvice.getChildCollection(oo, MiniOpenmrsObject.class
		        .getDeclaredField("locations"));
		Assert.assertTrue(fetchedLocations.contains(new Location(1)));
	}
	
	/**
	 * @see {@link RequiredDataAdvice#getChildCollection(OpenmrsObject,Field)}
	 */
	@Test
	@Verifies(value = "should be able to get private fields in fieldAccess list", method = "getChildCollection(OpenmrsObject,Field)")
	public void getChildCollection_shouldBeAbleToGetPrivateFieldsInFieldAccessList() throws Exception {
		MiniOpenmrsObject oo = new MiniOpenmrsObject();
		oo.setLocations(new ArrayList<Location>());
		Assert.assertNotNull(RequiredDataAdvice
		        .getChildCollection(oo, MiniOpenmrsObject.class.getDeclaredField("locations")));
	}
	
	/**
	 * Class that has a mismatched getter name instead of the correct getter name
	 */
	private class ClassWithBadGetter extends BaseOpenmrsObject {
		
		private Set<Location> locations;
		
		public Set<Location> getMyLocations() {
			return locations;
		}
		
		public void setMyLocations(Set<Location> locs) {
			this.locations = locs;
		}
		
		public Integer getId() {
			return null;
		}
		
		public void setId(Integer id) {
		}
	}
	
	/**
	 * @see {@link RequiredDataAdvice#getChildCollection(OpenmrsObject,Field)}
	 */
	@Test(expected = APIException.class)
	@Verifies(value = "should throw APIException if getter method not found", method = "getChildCollection(OpenmrsObject,Field)")
	public void getChildCollection_shouldThrowAPIExceptionIfGetterMethodNotFound() throws Exception {
		ClassWithBadGetter oo = new ClassWithBadGetter();
		oo.setMyLocations(new HashSet<Location>());
		RequiredDataAdvice.getChildCollection(oo, ClassWithBadGetter.class.getDeclaredField("locations"));
	}
	
	/**
	 * @see {@link RequiredDataAdvice#isOpenmrsObjectCollection(Field)}
	 */
	@Test
	@Verifies(value = "should return true if field is openmrsObject list", method = "isOpenmrsObjectCollection(Field)")
	public void isOpenmrsObjectCollection_shouldReturnTrueIfFieldIsOpenmrsobjectList() throws Exception {
		Assert.assertTrue(RequiredDataAdvice
		        .isOpenmrsObjectCollection(MiniOpenmrsObject.class.getDeclaredField("locations")));
	}
	
	/**
	 * @see {@link RequiredDataAdvice#isOpenmrsObjectCollection(Field)}
	 */
	@Test
	@Verifies(value = "should return true if field is openmrsObject set", method = "isOpenmrsObjectCollection(Field)")
	public void isOpenmrsObjectCollection_shouldReturnTrueIfFieldIsOpenmrsobjectSet() throws Exception {
		Assert.assertTrue(RequiredDataAdvice.isOpenmrsObjectCollection(ClassWithBadGetter.class
		        .getDeclaredField("locations")));
	}
	
	/**
	 * A class that has normal fields and non{@link OpenmrsObject} on it.
	 */
	private class ClassWithOtherFields extends BaseOpenmrsObject {
		
		private Set<Locale> locales;
		
		private List<Map<String, String>> nestedGenericProperty;
		
		private Integer id;

        public List<Map<String, String>> getNestedGenericProperty() {
        	return nestedGenericProperty;
        }

        public void setNestedGenericProperty(List<Map<String, String>> nestedGenericProperty) {
        	this.nestedGenericProperty = nestedGenericProperty;
        }
		
		public Set<Locale> getLocales() {
			return locales;
		}
		
		public void setLocales(Set<Locale> locs) {
			this.locales = locs;
		}
		
		public Integer getId() {
			return id;
		}
		
		public void setId(Integer id) {
			this.id = id;
		}
	}
	
	/**
	 * @see {@link RequiredDataAdvice#isOpenmrsObjectCollection(Field)}
	 */
	@Test
	@Verifies(value = "should return false if field is collection of other objects", method = "isOpenmrsObjectCollection(Field)")
	public void isOpenmrsObjectCollection_shouldReturnFalseIfFieldIsCollectionOfOtherObjects() throws Exception {
		Assert.assertFalse(RequiredDataAdvice.isOpenmrsObjectCollection(ClassWithOtherFields.class
		        .getDeclaredField("locales")));
	}
	
	/**
	 * @see {@link RequiredDataAdvice#isOpenmrsObjectCollection(Field)}
	 */
	@Test
	@Verifies(value = "should return false if field is collection of parameterized type", method = "isOpenmrsObjectCollection(Field)")
	public void isOpenmrsObjectCollection_shouldReturnFalseIfFieldIsCollectionOfParameterizedType() throws Exception {
		Assert.assertFalse(RequiredDataAdvice.isOpenmrsObjectCollection(ClassWithOtherFields.class
				.getDeclaredField("nestedGenericProperty")));
	}
	
	/**
	 * @see {@link RequiredDataAdvice#isOpenmrsObjectCollection(Field)}
	 */
	@Test
	@Verifies(value = "should return false if field is not a collection", method = "isOpenmrsObjectCollection(Field)")
	public void isOpenmrsObjectCollection_shouldReturnFalseIfFieldIsNotACollection() throws Exception {
		Assert.assertFalse(RequiredDataAdvice.isOpenmrsObjectCollection(ClassWithOtherFields.class.getDeclaredField("id")));
	}
	
	/**
	 * @see RequiredDataAdvice#isOpenmrsObjectCollection(Class,Object)
	 */
	@Test
	@Verifies(value = "should return false if class is collection of other objects", method = "isOpenmrsObjectCollection(Class<*>,Object)")
	public void isOpenmrsObjectCollection_shouldReturnFalseIfClassIsCollectionOfOtherObjects() throws Exception {
		Set<Locale> locales = new HashSet<Locale>();
		RequiredDataAdvice.isOpenmrsObjectCollection(locales.getClass(), locales);
	}
	
	/**
	 * @see RequiredDataAdvice#isOpenmrsObjectCollection(Class,Object)
	 */
	@Test
	@Verifies(value = "should return false if class is not a collection", method = "isOpenmrsObjectCollection(Class<*>,Object)")
	public void isOpenmrsObjectCollection_shouldReturnFalseIfClassIsNotACollection() throws Exception {
		Location location = new Location();
		RequiredDataAdvice.isOpenmrsObjectCollection(location.getClass(), location);
	}
	
	/**
	 * @see RequiredDataAdvice#isOpenmrsObjectCollection(Class<*>,Object)
	 */
	@Test
	@Verifies(value = "should return true if class is openmrsObject list", method = "isOpenmrsObjectCollection(Class<*>,Object)")
	public void isOpenmrsObjectCollection_shouldReturnTrueIfClassIsOpenmrsObjectList() throws Exception {
		List<Location> locations = new ArrayList<Location>();
		RequiredDataAdvice.isOpenmrsObjectCollection(locations.getClass(), locations);
	}
	
	/**
	 * @see RequiredDataAdvice#isOpenmrsObjectCollection(Class<*>,Object)
	 */
	@Test
	@Verifies(value = "should return true if class is openmrsObject set", method = "isOpenmrsObjectCollection(Class<*>,Object)")
	public void isOpenmrsObjectCollection_shouldReturnTrueIfClassIsOpenmrsObjectSet() throws Exception {
		Set<Location> locations = new HashSet<Location>();
		RequiredDataAdvice.isOpenmrsObjectCollection(locations.getClass(), locations);
	}
	
	/**
	 * @see {@link RequiredDataAdvice#before(Method,null,Object)}
	 */
	@Test
	@Verifies(value = "should not fail on update method with no arguments", method = "before(Method,null,Object)")
	public void before_shouldNotFailOnUpdateMethodWithNoArguments() throws Throwable {
		Method method = ConceptServiceImpl.class.getMethod("updateConceptWords", (Class[]) null);
		new RequiredDataAdvice().before(method, null, new ConceptServiceImpl());
		new RequiredDataAdvice().before(method, new Object[] {}, new ConceptServiceImpl());
	}
}
