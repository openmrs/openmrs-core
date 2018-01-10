/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.aop;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Spy;
import org.openmrs.BaseOpenmrsData;
import org.openmrs.BaseOpenmrsMetadata;
import org.openmrs.BaseOpenmrsObject;
import org.openmrs.Concept;
import org.openmrs.Location;
import org.openmrs.OpenmrsObject;
import org.openmrs.Person;
import org.openmrs.PersonName;
import org.openmrs.Role;
import org.openmrs.User;
import org.openmrs.annotation.AllowDirectAccess;
import org.openmrs.annotation.DisableHandlers;
import org.openmrs.api.APIException;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.ServiceContext;
import org.openmrs.api.handler.BaseVoidHandler;
import org.openmrs.api.handler.OpenmrsObjectSaveHandler;
import org.openmrs.api.handler.RequiredDataHandler;
import org.openmrs.api.handler.RetireHandler;
import org.openmrs.api.handler.SaveHandler;
import org.openmrs.api.handler.UnretireHandler;
import org.openmrs.api.handler.UnvoidHandler;
import org.openmrs.api.handler.VoidHandler;
import org.openmrs.api.impl.ConceptServiceImpl;
import org.openmrs.test.BaseContextMockTest;
import org.openmrs.util.HandlerUtil;
import org.openmrs.util.Reflect;
import org.openmrs.util.RoleConstants;
import org.springframework.context.ApplicationContext;

/**
 * Tests the {@link RequiredDataAdvice} class.
 */
public class RequiredDataAdviceTest extends BaseContextMockTest {
	
	@Mock
	AdministrationService administrationService;
	
	@Mock
	ApplicationContext applicationContext;
	
	@Mock
	Context context;
	
	@Mock
	ServiceContext serviceContext;
	
	@Spy
	OpenmrsObjectSaveHandler saveHandler;
	
	@Spy
	BaseVoidHandler voidHandler;
	
	RequiredDataAdvice requiredDataAdvice = new RequiredDataAdvice();
	
	@Before
	public void setUp() {

		Context.setUserContext(userContext);
		context.setServiceContext(serviceContext);
		Context.setContext(serviceContext);
		serviceContext.setApplicationContext(applicationContext);
		
		User user = new User();
		user.setUuid("1010d442-e134-11de-babe-001e378eb67e");
		user.setUserId(1);
		user.setUsername("admin");
		user.addRole(new Role(RoleConstants.SUPERUSER));
		Person person = new Person();
		person.setUuid("6adb7c42-cfd2-4301-b53b-ff17c5654ff7");
		person.setId(1);
		person.addName(new PersonName("Bob", "", "Smith"));
		Calendar calendar = Calendar.getInstance();
		calendar.set(1980, 01, 01);
		person.setBirthdate(calendar.getTime());
		person.setGender("M");
		user.setPerson(person);
		when(userContext.getAuthenticatedUser()).thenReturn(user);
		when(userContext.isAuthenticated()).thenReturn(true);
		
		Map<String, SaveHandler> saveHandlers = new HashMap<>();
		saveHandlers.put("saveHandler", saveHandler);
		when(applicationContext.getBeansOfType(SaveHandler.class)).thenReturn(saveHandlers);
		
		Map<String, VoidHandler> voidHandlers = new HashMap<>();
		voidHandlers.put("voidHandler", voidHandler);
		when(applicationContext.getBeansOfType(VoidHandler.class)).thenReturn(voidHandlers);
		
		//Clear cache since handlers are updated
		HandlerUtil.clearCachedHandlers();
	}
	
	/**
	 * Class with a private field without getter
	 */
	private class MiniOpenmrsObject extends BaseOpenmrsObject {
		
		@AllowDirectAccess
		private List<Location> locations;
		
		public void setLocations(List<Location> locs) {
			this.locations = locs;
		}
		
		@Override
		public Integer getId() {
			return null;
		}
		
		@Override
		public void setId(Integer id) {
		}
	}
	
	/**
	 * @see RequiredDataAdvice#getChildCollection(OpenmrsObject, Field)
	 */
	@Test
	public void getChildCollection_shouldGetValueOfGivenChildCollectionOnGivenField() throws Exception {
		MiniOpenmrsObject oo = new MiniOpenmrsObject();
		List<Location> locs = new ArrayList<>();
		Location location = new Location(1);
		locs.add(location);
		oo.setLocations(locs);
		Collection<OpenmrsObject> fetchedLocations = RequiredDataAdvice.getChildCollection(oo, MiniOpenmrsObject.class
		        .getDeclaredField("locations"));
		Assert.assertTrue(fetchedLocations.contains(location));
	}
	
	/**
	 * @see RequiredDataAdvice#getChildCollection(OpenmrsObject,Field)
	 */
	@Test
	public void getChildCollection_shouldShouldBeAbleToGetAnnotatedPrivateFields() throws Exception {
		MiniOpenmrsObject oo = new MiniOpenmrsObject();
		oo.setLocations(new ArrayList<>());
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
		
		@Override
		public Integer getId() {
			return null;
		}
		
		@Override
		public void setId(Integer id) {
		}
	}
	
	/**
	 * @see RequiredDataAdvice#getChildCollection(OpenmrsObject, Field)
	 */
	@Test(expected = APIException.class)
	public void getChildCollection_shouldThrowAPIExceptionIfGetterMethodNotFound() throws Exception {
		ClassWithBadGetter oo = new ClassWithBadGetter();
		oo.setMyLocations(new HashSet<>());
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
		
		@Override
		public Integer getId() {
			return id;
		}
		
		@Override
		public void setId(Integer id) {
			this.id = id;
		}
	}
	
	/**
	 * @see RequiredDataAdvice#isOpenmrsObjectCollection(Field)
	 */
	@Test
	public void isOpenmrsObjectCollection_shouldReturnFalseIfFieldIsCollectionOfOtherObjects() throws Exception {
		Assert.assertFalse(RequiredDataAdvice.isOpenmrsObjectCollection(ClassWithOtherFields.class
		        .getDeclaredField("locales")));
		List<String> list = new LinkedList<>();
		list.add("Test");
		Assert.assertFalse(RequiredDataAdvice.isOpenmrsObjectCollection(list));
		
	}
	
	/**
	 * @see RequiredDataAdvice#isOpenmrsObjectCollection(Field)
	 */
	@Test
	public void isOpenmrsObjectCollection_shouldReturnFalseIfFieldIsCollectionOfParameterizedType() throws Exception {
		Assert.assertFalse(RequiredDataAdvice.isOpenmrsObjectCollection(ClassWithOtherFields.class
		        .getDeclaredField("nestedGenericProperty")));
	}
	
	/**
	 * @see RequiredDataAdvice#isOpenmrsObjectCollection(Field)
	 */
	@Test
	public void isOpenmrsObjectCollection_shouldReturnFalseIfFieldIsNotACollection() throws Exception {
		Assert.assertFalse(RequiredDataAdvice.isOpenmrsObjectCollection(ClassWithOtherFields.class.getDeclaredField("id")));
	}
	
	/**
	 * @see RequiredDataAdvice#isOpenmrsObjectCollection(Class<*>,Object)
	 */
	@Test
	public void isOpenmrsObjectCollection_shouldReturnTrueIfClassIsOpenmrsObjectList() throws Exception {
		List<Location> locations = new ArrayList<>();
		Location location = new Location();
		locations.add(location);
		Assert.assertTrue(RequiredDataAdvice.isOpenmrsObjectCollection(locations));
	}
	
	/**
	 * @see RequiredDataAdvice#isOpenmrsObjectCollection(Class<*>,Object)
	 */
	@Test
	public void isOpenmrsObjectCollection_shouldReturnTrueIfClassIsOpenmrsObjectSet() throws Exception {
		Set<Location> locations = new HashSet<>();
		Location location = new Location();
		locations.add(location);
		Assert.assertTrue(RequiredDataAdvice.isOpenmrsObjectCollection(locations));
	}
	
	/**
	 * @see RequiredDataAdvice#isOpenmrsObjectCollection(Class<*>,Object)
	 */
	@Test
	public void isOpenmrsObjectCollection_shouldReturnFalseIfCollectionIsEmptyRegardlessOfTypeHeld() throws Exception {
		Set<Location> locations = new HashSet<>();
		Assert.assertFalse(RequiredDataAdvice.isOpenmrsObjectCollection(locations));
	}
	
	/**
	 * Some OpenmrsData with a collection annotated with @DisableHandlers
	 */
	private class ClassWithDisableHandlersAnnotation extends BaseOpenmrsData {
		
		@DisableHandlers(handlerTypes = { VoidHandler.class, SaveHandler.class })
		private List<Person> persons;
		
		private List<Person> notAnnotatedPersons;
		
		public List<Person> getPersons() {
			return persons;
		}
		
		public void setPersons(List<Person> persons) {
			this.persons = persons;
		}
		
		public List<Person> getNotAnnotatedPersons() {
			return notAnnotatedPersons;
		}
		
		public void setNotAnnotatedPersons(List<Person> notAnnotatedPersons) {
			this.notAnnotatedPersons = notAnnotatedPersons;
		}
		
		@Override
		public Integer getId() {
			return null;
		}
		
		@Override
		public void setId(Integer id) {
		}
	}
	
	/**
	 * @see RequiredDataAdvice#isHandlerMarkedAsDisabled(Class, java.lang.reflect.Field)
	 */
	@Test
	public void isHandlerMarkedAsDisabled_shouldReturnTrueIfHandlerDisabled() {
		
		Field persons = null;
		
		for (Field field : Reflect.getAllFields(ClassWithDisableHandlersAnnotation.class)) {
			if (field.getName().equals("persons")) {
				persons = field;
			}
		}
		
		Assert.assertTrue(RequiredDataAdvice.isHandlerMarkedAsDisabled(SaveHandler.class, persons));
		Assert.assertTrue(RequiredDataAdvice.isHandlerMarkedAsDisabled(VoidHandler.class, persons));
	}
	
	/**
	 * @see RequiredDataAdvice#isHandlerMarkedAsDisabled(Class, java.lang.reflect.Field)
	 */
	@Test
	public void isHandlerMarkedAsDisabled_shouldReturnFalseIfHandlerNotDisabled() {
		
		Field persons = null;
		
		for (Field field : Reflect.getAllFields(ClassWithDisableHandlersAnnotation.class)) {
			if (field.getName().equals("persons")) {
				persons = field;
			}
		}
		
		Assert.assertFalse(RequiredDataAdvice.isHandlerMarkedAsDisabled(RetireHandler.class, persons));
	}
	
	/**
	 * @see RequiredDataAdvice#isHandlerMarkedAsDisabled(Class, java.lang.reflect.Field)
	 */
	@Test
	public void isHandlerMarkedAsDisabled_shouldReturnFalseIfFieldNotAnnotated() {
		
		Field persons = null;
		
		for (Field field : Reflect.getAllFields(ClassWithDisableHandlersAnnotation.class)) {
			if (field.getName().equals("notAnnotatedPersons")) {
				persons = field;
			}
		}
		
		Assert.assertFalse(RequiredDataAdvice.isHandlerMarkedAsDisabled(RetireHandler.class, persons));
	}
	
	/**
	 * Some OpenmrsData with a collection annotated with @DisableHandlers
	 */
	private class ClassWithDisableHandlersAnnotationForSupertype extends BaseOpenmrsData {
		
		// this should disable all handlers
		@DisableHandlers(handlerTypes = { RequiredDataHandler.class })
		private List<Person> persons;
		
		private List<Person> notAnnotatedPersons;
		
		public List<Person> getPersons() {
			return persons;
		}
		
		public void setPersons(List<Person> persons) {
			this.persons = persons;
		}
		
		public List<Person> getNotAnnotatedPersons() {
			return notAnnotatedPersons;
		}
		
		public void setNotAnnotatedPersons(List<Person> notAnnotatedPersons) {
			this.notAnnotatedPersons = notAnnotatedPersons;
		}
		
		@Override
		public Integer getId() {
			return null;
		}
		
		@Override
		public void setId(Integer id) {
		}
	}
	
	/**
	 * @see RequiredDataAdvice#isHandlerMarkedAsDisabled(Class, java.lang.reflect.Field)
	 */
	@Test
	public void isHandlerMarkedAsDisabled_shouldReturnTrueIfSupertypeHandlerDisabled() {
		
		Field persons = null;
		
		for (Field field : Reflect.getAllFields(ClassWithDisableHandlersAnnotationForSupertype.class)) {
			if (field.getName().equals("persons")) {
				persons = field;
			}
		}
		
		// all the handlers should be marked as disabled, since the supertype (RequiredDataHandler) was specified to be ignored
		Assert.assertTrue(RequiredDataAdvice.isHandlerMarkedAsDisabled(SaveHandler.class, persons));
		Assert.assertTrue(RequiredDataAdvice.isHandlerMarkedAsDisabled(VoidHandler.class, persons));
		Assert.assertTrue(RequiredDataAdvice.isHandlerMarkedAsDisabled(UnvoidHandler.class, persons));
		Assert.assertTrue(RequiredDataAdvice.isHandlerMarkedAsDisabled(RetireHandler.class, persons));
		Assert.assertTrue(RequiredDataAdvice.isHandlerMarkedAsDisabled(UnretireHandler.class, persons));
	}
	
	/**
	 * Some OpenmrsMetadata with a collection annotated with @DisableHandlers
	 */
	private class MetadataClassWithDisableHandlersAnnotation extends BaseOpenmrsMetadata {
		
		@DisableHandlers(handlerTypes = { UnretireHandler.class })
		private List<Concept> concepts;
		
		public List<Concept> getConcepts() {
			return concepts;
		}
		
		public void setConcepts(List<Concept> concepts) {
			this.concepts = concepts;
		}
		
		@Override
		public Integer getId() {
			return null;
		}
		
		@Override
		public void setId(Integer id) {
		}
	}
	
	/**
	 * @see RequiredDataAdvice#isHandlerMarkedAsDisabled(Class, java.lang.reflect.Field)
	 */
	@Test
	public void isHandlerMarkedAsDisabled_shouldReturnTrueIfHandlerDisabledOnMetadata() {
		
		Field persons = null;
		
		for (Field field : Reflect.getAllFields(MetadataClassWithDisableHandlersAnnotation.class)) {
			if (field.getName().equals("concepts")) {
				persons = field;
			}
		}
		
		Assert.assertTrue(RequiredDataAdvice.isHandlerMarkedAsDisabled(UnretireHandler.class, persons));
	}
	
	/**
	 * @see RequiredDataAdvice#isHandlerMarkedAsDisabled(Class, java.lang.reflect.Field)
	 */
	@Test
	public void isHandlerMarkedAsDisabled_shouldReturnFalseIfHandlerNotDisabledOnMetatdata() {
		
		Field persons = null;
		
		for (Field field : Reflect.getAllFields(MetadataClassWithDisableHandlersAnnotation.class)) {
			if (field.getName().equals("concepts")) {
				persons = field;
			}
		}
		
		Assert.assertFalse(RequiredDataAdvice.isHandlerMarkedAsDisabled(RetireHandler.class, persons));
	}
	
	/**
	 * @see RequiredDataAdvice#before(Method, null, Object)
	 */
	@Test
	public void before_shouldNotFailOnUpdateMethodWithNoArguments() throws Throwable {
		Method method = ConceptServiceImpl.class.getMethod("updateConceptIndexes", (Class[]) null);
		requiredDataAdvice.before(method, null, new ConceptServiceImpl());
		requiredDataAdvice.before(method, new Object[] {}, new ConceptServiceImpl());
	}
	
	@Test
	public void before_shouldNotCallHandlerOnSaveWithNullOrNoArguments() throws Throwable {
		
		Method m = WithAppropriatelyNamedMethod.class.getMethod("saveSomeOpenmrsData", SomeOpenmrsData.class);
		SomeOpenmrsData openmrsObject = new SomeOpenmrsData();
		requiredDataAdvice.before(m, null, new WithAppropriatelyNamedMethod());
		requiredDataAdvice.before(m, new Object[] {}, new WithAppropriatelyNamedMethod());
		verify(saveHandler, never()).handle(eq(openmrsObject), Matchers.anyObject(), Matchers.anyObject(),
		    anyString());
	}
	
	@Test
	public void before_shouldCallHandlerOnSaveWithOpenmrsObjectArgument() throws Throwable {
		
		Method m = WithAppropriatelyNamedMethod.class.getMethod("saveSomeOpenmrsData", SomeOpenmrsData.class);
		SomeOpenmrsData openmrsObject = new SomeOpenmrsData();
		requiredDataAdvice.before(m, new Object[] { openmrsObject }, new WithAppropriatelyNamedMethod());
		verify(saveHandler, times(1)).handle(eq(openmrsObject), Matchers.anyObject(), Matchers.anyObject(),
		    anyString());
	}
	
	@Test
	public void before_shouldNotCallHandlerOnSaveMethodNameNotMatchingDomainObject() throws Throwable {
		
		Method m = WithAppropriatelyNamedMethod.class.getMethod("saveSomeOpenmrsDataButNotReally", SomeOpenmrsData.class);
		SomeOpenmrsData openmrsObject = new SomeOpenmrsData();
		requiredDataAdvice.before(m, new Object[] { openmrsObject }, new WithAppropriatelyNamedMethod());
		verify(saveHandler, never()).handle(eq(openmrsObject), Matchers.anyObject(), Matchers.anyObject(),
		    anyString());
	}
	
	@Test
	public void before_shouldCallHandlerOnSaveMethodNameWithCollectionArgument() throws Throwable {
		
		Method m = WithAppropriatelyNamedMethod.class.getMethod("saveSomeOpenmrsDatas", List.class);
		List<SomeOpenmrsData> openmrsObjects = Arrays.asList(new SomeOpenmrsData(), new SomeOpenmrsData());
		requiredDataAdvice.before(m, new Object[] { openmrsObjects }, new WithAppropriatelyNamedMethod());
		verify(saveHandler, times(2)).handle(Matchers.anyObject(), Matchers.anyObject(),
		    Matchers.anyObject(), anyString());
	}
	
	@Test
	public void before_shouldNotCallHandlerOnVoidWithNullOrNoArguments() throws Throwable {
		
		Method m = WithAppropriatelyNamedMethod.class.getMethod("voidSomeOpenmrsData", SomeOpenmrsData.class);
		SomeOpenmrsData openmrsObject = new SomeOpenmrsData();
		requiredDataAdvice.before(m, null, new WithAppropriatelyNamedMethod());
		requiredDataAdvice.before(m, new Object[] {}, new WithAppropriatelyNamedMethod());
		verify(voidHandler, never()).handle(eq(openmrsObject), Matchers.anyObject(), Matchers.anyObject(),
		    anyString());
	}
	
	@Test
	public void before_shouldCallHandlerOnVoidMethodNameMatchingDomainObject() throws Throwable {
		
		Method m = WithAppropriatelyNamedMethod.class.getMethod("voidSomeOpenmrsData", SomeOpenmrsData.class);
		SomeOpenmrsData openmrsObject = new SomeOpenmrsData();
		requiredDataAdvice.before(m, new Object[] { openmrsObject, "void reason" }, new WithAppropriatelyNamedMethod());
		verify(voidHandler, times(1)).handle(eq(openmrsObject), Matchers.anyObject(), Matchers.anyObject(),
		    anyString());
	}
	
	@Test
	public void before_shouldCallHandlerOnVoidMethodWhenDomainObjectIsAssignableFromMethodNameObject() throws Throwable {
		
		Method m = WithAppropriatelyNamedMethod.class.getMethod("voidSomeOpenmrsData", SomeOpenmrsData.class);
		SomeOpenmrsData openmrsObjectSubClass = new SomeOpenmrsDataSubClass();
		requiredDataAdvice.before(m, new Object[] { openmrsObjectSubClass, "void reason" },
		    new WithAppropriatelyNamedMethod());
		verify(voidHandler, times(1)).handle(eq(openmrsObjectSubClass), Matchers.anyObject(),
		    Matchers.anyObject(), anyString());
	}
	
	@Test
	public void before_shouldNotCallHandlerOnVoidMethodNameNotMatchingDomainObject() throws Throwable {
		
		Method m = WithAppropriatelyNamedMethod.class.getMethod("voidSomeOpenmrsDataButNotReally", SomeOpenmrsData.class);
		SomeOpenmrsData openmrsObject = new SomeOpenmrsData();
		requiredDataAdvice.before(m, new Object[] { openmrsObject }, new WithAppropriatelyNamedMethod());
		verify(voidHandler, never()).handle(eq(openmrsObject), Matchers.anyObject(), Matchers.anyObject(),
		    anyString());
	}
	
	@Test
	public void before_shouldNotCallHandlersAnnotatedAsDisabled() throws Throwable {
		
		Method m = WithAppropriatelyNamedMethod.class.getMethod("voidClassWithDisableHandlersAnnotation",
		    ClassWithDisableHandlersAnnotation.class);
		
		ClassWithDisableHandlersAnnotation openmrsObject = new ClassWithDisableHandlersAnnotation();
		
		// create a couple locations and associate them with this openmrsObject
		List<Person> persons = new ArrayList<>();
		Person person = new Person();
		persons.add(person);
		openmrsObject.setPersons(persons);
		
		requiredDataAdvice.before(m, new Object[] { openmrsObject, "void reason" }, new WithAppropriatelyNamedMethod());
		
		// verify that the handle method was never called on this object
		verify(voidHandler, never()).handle(eq(person), Matchers.anyObject(), Matchers.anyObject(),
		    anyString());
		
	}
	
	@Test
	public void before_shouldCallHandlersNotAnnotatedAsDisabled() throws Throwable {
		
		Method m = WithAppropriatelyNamedMethod.class.getMethod("voidClassWithDisableHandlersAnnotation",
		    ClassWithDisableHandlersAnnotation.class);
		
		ClassWithDisableHandlersAnnotation openmrsObject = new ClassWithDisableHandlersAnnotation();
		
		// create a couple locations and associate them with this openmrsObject
		List<Person> persons = new ArrayList<>();
		Person person = new Person();
		persons.add(person);
		openmrsObject.setNotAnnotatedPersons(persons);
		
		requiredDataAdvice.before(m, new Object[] { openmrsObject, "void reason" }, new WithAppropriatelyNamedMethod());
		
		// verify that the handle method was called on this object
		verify(voidHandler, times(1)).handle(eq(person), Matchers.anyObject(), Matchers.anyObject(),
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
		
		public void voidClassWithDisableHandlersAnnotation(ClassWithDisableHandlersAnnotation oo) {
		}
	}
	
}
