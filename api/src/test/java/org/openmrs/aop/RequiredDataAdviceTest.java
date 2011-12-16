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

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.openmrs.BaseOpenmrsData;
import org.openmrs.BaseOpenmrsObject;
import org.openmrs.Location;
import org.openmrs.OpenmrsObject;
import org.openmrs.User;
import org.openmrs.api.APIException;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.api.handler.BaseVoidHandler;
import org.openmrs.api.handler.OpenmrsObjectSaveHandler;
import org.openmrs.api.handler.SaveHandler;
import org.openmrs.api.handler.VoidHandler;
import org.openmrs.api.impl.ConceptServiceImpl;
import org.openmrs.test.Verifies;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * Tests the {@link RequiredDataAdvice} class.
 */
@SuppressWarnings( { "unchecked" })
@RunWith(PowerMockRunner.class)
@PrepareForTest(Context.class)
public class RequiredDataAdviceTest {
	
	private SaveHandler saveHandler;
	
	private VoidHandler voidHandler;
	
	private RequiredDataAdvice requiredDataAdvice;
	
	@Before
	public void setUp() {
		this.requiredDataAdvice = new RequiredDataAdvice();
		
		PowerMockito.mockStatic(Context.class);
		
		saveHandler = mock(OpenmrsObjectSaveHandler.class);
		voidHandler = mock(BaseVoidHandler.class);
		
		when(Context.getRegisteredComponents(SaveHandler.class)).thenReturn(Arrays.asList(saveHandler));
		when(Context.getRegisteredComponents(VoidHandler.class)).thenReturn(Arrays.asList(voidHandler));
		AdministrationService administrationService = mock(AdministrationService.class);
		when(Context.getAdministrationService()).thenReturn(administrationService);
	}
	
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
	 * @see {@link RequiredDataAdvice#getChildCollection(OpenmrsObject, Field)}
	 */
	@Test
	@Verifies(value = "should get value of given child collection on given field", method = "getChildCollection(OpenmrsObject,Field)")
	public void getChildCollection_shouldGetValueOfGivenChildCollectionOnGivenField() throws Exception {
		MiniOpenmrsObject oo = new MiniOpenmrsObject();
		List<Location> locs = new ArrayList<Location>();
		Location location = new Location(1);
		locs.add(location);
		oo.setLocations(locs);
		Collection<OpenmrsObject> fetchedLocations = RequiredDataAdvice.getChildCollection(oo, MiniOpenmrsObject.class
		        .getDeclaredField("locations"));
		Assert.assertTrue(fetchedLocations.contains(location));
	}
	
	/**
	 * @see {@link RequiredDataAdvice#getChildCollection(OpenmrsObject, Field)}
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
	@SuppressWarnings( { "UnusedDeclaration" })
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
	 * @see {@link RequiredDataAdvice#getChildCollection(OpenmrsObject, Field)}
	 */
	@Test(expected = APIException.class)
	@Verifies(value = "should throw APIException if getter method not found", method = "getChildCollection(OpenmrsObject,Field)")
	public void getChildCollection_shouldThrowAPIExceptionIfGetterMethodNotFound() throws Exception {
		ClassWithBadGetter oo = new ClassWithBadGetter();
		oo.setMyLocations(new HashSet<Location>());
		RequiredDataAdvice.getChildCollection(oo, ClassWithBadGetter.class.getDeclaredField("locations"));
	}
	
	/**
	 * A class that has normal fields and non{@link OpenmrsObject} on it.
	 */
	@SuppressWarnings( { "UnusedDeclaration" })
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
		List<String> list = new LinkedList<String>();
		list.add("Test");
		Assert.assertFalse(RequiredDataAdvice.isOpenmrsObjectCollection(list));
		
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
	 * @see RequiredDataAdvice#isOpenmrsObjectCollection(Class<*>,Object)
	 */
	@Test
	@Verifies(value = "should return true if class is openmrsObject list", method = "isOpenmrsObjectCollection(Object)")
	public void isOpenmrsObjectCollection_shouldReturnTrueIfClassIsOpenmrsObjectList() throws Exception {
		List<Location> locations = new ArrayList<Location>();
		Location location = new Location();
		locations.add(location);
		Assert.assertTrue(RequiredDataAdvice.isOpenmrsObjectCollection(locations));
	}
	
	/**
	 * @see RequiredDataAdvice#isOpenmrsObjectCollection(Class<*>,Object)
	 */
	@Test
	@Verifies(value = "should return true if class is openmrsObject set", method = "isOpenmrsObjectCollection(Object)")
	public void isOpenmrsObjectCollection_shouldReturnTrueIfClassIsOpenmrsObjectSet() throws Exception {
		Set<Location> locations = new HashSet<Location>();
		Location location = new Location();
		locations.add(location);
		Assert.assertTrue(RequiredDataAdvice.isOpenmrsObjectCollection(locations));
	}
	
	/**
	 * @see RequiredDataAdvice#isOpenmrsObjectCollection(Class<*>,Object)
	 */
	@Test
	@Verifies(value = "should return false if collection is empty regardless of type held", method = "isOpenmrsObjectCollection(Object)")
	public void isOpenmrsObjectCollection_shouldReturnFalseIfCollectionIsEmptyRegardlessOfTypeHeld() throws Exception {
		Set<Location> locations = new HashSet<Location>();
		Assert.assertFalse(RequiredDataAdvice.isOpenmrsObjectCollection(locations));
	}
	
	/**
	 * @see {@link RequiredDataAdvice#before(Method, null, Object)}
	 */
	@Test
	@Verifies(value = "should not fail on update method with no arguments", method = "before(Method,null,Object)")
	public void before_shouldNotFailOnUpdateMethodWithNoArguments() throws Throwable {
		Method method = ConceptServiceImpl.class.getMethod("updateConceptWords", (Class[]) null);
		requiredDataAdvice.before(method, null, new ConceptServiceImpl());
		requiredDataAdvice.before(method, new Object[] {}, new ConceptServiceImpl());
	}
	
	@Test
	public void before_shouldNotCallHandlerOnSaveWithNullOrNoArguments() throws Throwable {
		
		Method m = WithAppropriatelyNamedMethod.class.getMethod("saveSomeOpenmrsData", SomeOpenmrsData.class);
		SomeOpenmrsData openmrsObject = new SomeOpenmrsData();
		requiredDataAdvice.before(m, null, new WithAppropriatelyNamedMethod());
		requiredDataAdvice.before(m, new Object[] {}, new WithAppropriatelyNamedMethod());
		verify(saveHandler, never()).handle(eq(openmrsObject), Matchers.<User> anyObject(), Matchers.<Date> anyObject(),
		    anyString());
	}
	
	@Test
	public void before_shouldCallHandlerOnSaveWithOpenmrsObjectArgument() throws Throwable {
		
		Method m = WithAppropriatelyNamedMethod.class.getMethod("saveSomeOpenmrsData", SomeOpenmrsData.class);
		SomeOpenmrsData openmrsObject = new SomeOpenmrsData();
		requiredDataAdvice.before(m, new Object[] { openmrsObject }, new WithAppropriatelyNamedMethod());
		verify(saveHandler, times(1)).handle(eq(openmrsObject), Matchers.<User> anyObject(), Matchers.<Date> anyObject(),
		    anyString());
	}
	
	@Test
	public void before_shouldNotCallHandlerOnSaveMethodNameNotMatchingDomainObject() throws Throwable {
		
		Method m = WithAppropriatelyNamedMethod.class.getMethod("saveSomeOpenmrsDataButNotReally", SomeOpenmrsData.class);
		SomeOpenmrsData openmrsObject = new SomeOpenmrsData();
		requiredDataAdvice.before(m, new Object[] { openmrsObject }, new WithAppropriatelyNamedMethod());
		verify(saveHandler, never()).handle(eq(openmrsObject), Matchers.<User> anyObject(), Matchers.<Date> anyObject(),
		    anyString());
	}
	
	@Test
	public void before_shouldCallHandlerOnSaveMethodNameWithCollectionArgument() throws Throwable {
		
		Method m = WithAppropriatelyNamedMethod.class.getMethod("saveSomeOpenmrsDatas", List.class);
		List<SomeOpenmrsData> openmrsObjects = Arrays.asList(new SomeOpenmrsData(), new SomeOpenmrsData());
		requiredDataAdvice.before(m, new Object[] { openmrsObjects }, new WithAppropriatelyNamedMethod());
		verify(saveHandler, times(2)).handle(Matchers.<SomeOpenmrsData> anyObject(), Matchers.<User> anyObject(),
		    Matchers.<Date> anyObject(), anyString());
	}
	
	@Test
	public void before_shouldNotCallHandlerOnVoidWithNullOrNoArguments() throws Throwable {
		
		Method m = WithAppropriatelyNamedMethod.class.getMethod("voidSomeOpenmrsData", SomeOpenmrsData.class);
		SomeOpenmrsData openmrsObject = new SomeOpenmrsData();
		requiredDataAdvice.before(m, null, new WithAppropriatelyNamedMethod());
		requiredDataAdvice.before(m, new Object[] {}, new WithAppropriatelyNamedMethod());
		verify(voidHandler, never()).handle(eq(openmrsObject), Matchers.<User> anyObject(), Matchers.<Date> anyObject(),
		    anyString());
	}
	
	@Test
	public void before_shouldCallHandlerOnVoidMethodNameMatchingDomainObject() throws Throwable {
		
		Method m = WithAppropriatelyNamedMethod.class.getMethod("voidSomeOpenmrsData", SomeOpenmrsData.class);
		SomeOpenmrsData openmrsObject = new SomeOpenmrsData();
		requiredDataAdvice.before(m, new Object[] { openmrsObject, "void reason" }, new WithAppropriatelyNamedMethod());
		verify(voidHandler, times(1)).handle(eq(openmrsObject), Matchers.<User> anyObject(), Matchers.<Date> anyObject(),
		    anyString());
	}
	
	@Test
	public void before_shouldCallHandlerOnVoidMethodWhenDomainObjectIsAssignableFromMethodNameObject() throws Throwable {
		
		Method m = WithAppropriatelyNamedMethod.class.getMethod("voidSomeOpenmrsData", SomeOpenmrsData.class);
		SomeOpenmrsData openmrsObjectSubClass = new SomeOpenmrsDataSubClass();
		requiredDataAdvice.before(m, new Object[] { openmrsObjectSubClass, "void reason" },
		    new WithAppropriatelyNamedMethod());
		verify(voidHandler, times(1)).handle(eq(openmrsObjectSubClass), Matchers.<User> anyObject(),
		    Matchers.<Date> anyObject(), anyString());
	}
	
	@Test
	public void before_shouldNotCallHandlerOnVoidMethodNameNotMatchingDomainObject() throws Throwable {
		
		Method m = WithAppropriatelyNamedMethod.class.getMethod("voidSomeOpenmrsDataButNotReally", SomeOpenmrsData.class);
		SomeOpenmrsData openmrsObject = new SomeOpenmrsData();
		requiredDataAdvice.before(m, new Object[] { openmrsObject }, new WithAppropriatelyNamedMethod());
		verify(voidHandler, never()).handle(eq(openmrsObject), Matchers.<User> anyObject(), Matchers.<Date> anyObject(),
		    anyString());
	}
	
	class SomeOpenmrsData extends BaseOpenmrsData {
		
		@Override
		public Integer getId() {
			return null;
		}
		
		@Override
		public void setId(Integer id) {
		}
	}
	
	public class SomeOpenmrsDataSubClass extends SomeOpenmrsData {

	}
	
	@SuppressWarnings( { "UnusedDeclaration" })
	public class WithAppropriatelyNamedMethod {
		
		public void saveSomeOpenmrsData(SomeOpenmrsData oo) {
		}
		
		public void saveSomeOpenmrsData(SomeOpenmrsData oo, String reason) {
		}
		
		public void saveSomeOpenmrsDatas(List<SomeOpenmrsData> list) {
		}
		
		public void saveSomeOpenmrsDataButNotReally(SomeOpenmrsData oo) {
		}
		
		public void voidSomeOpenmrsData(SomeOpenmrsData oo) {
		}
		
		public void voidSomeOpenmrsDataButNotReally(SomeOpenmrsData oo) {
		}
	}
	
}
