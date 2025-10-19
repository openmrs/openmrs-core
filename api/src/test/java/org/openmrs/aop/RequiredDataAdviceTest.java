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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Spy;
import org.openmrs.BaseOpenmrsData;
import org.openmrs.BaseOpenmrsMetadata;
import org.openmrs.BaseOpenmrsObject;
import org.openmrs.Concept;
import org.openmrs.Location;
import org.openmrs.OpenmrsObject;
import org.openmrs.Person;
import org.openmrs.annotation.AllowDirectAccess;
import org.openmrs.api.db.ConceptDAO;
import org.openmrs.annotation.DisableHandlers;
import org.openmrs.api.APIException;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.ConceptService;
import org.openmrs.api.ObsService;
import org.openmrs.api.handler.BaseVoidHandler;
import org.openmrs.messagesource.MessageSourceService;
import org.openmrs.api.handler.OpenmrsObjectSaveHandler;
import org.openmrs.api.handler.RequiredDataHandler;
import org.openmrs.api.handler.RetireHandler;
import org.openmrs.api.handler.SaveHandler;
import org.openmrs.api.handler.UnretireHandler;
import org.openmrs.api.handler.UnvoidHandler;
import org.openmrs.api.handler.VoidHandler;
import org.openmrs.api.impl.ConceptServiceImpl;
import org.openmrs.test.jupiter.BaseContextMockTest;
import org.openmrs.util.HandlerUtil;
import org.openmrs.util.Reflect;
import org.springframework.context.ApplicationContext;

/**
 * Tests the {@link RequiredDataAdvice} class.
 */
public class RequiredDataAdviceTest extends BaseContextMockTest {

	@Mock
	AdministrationService administrationService;

	@Mock
	ApplicationContext applicationContext;
	
	@Spy
	OpenmrsObjectSaveHandler saveHandler;
	
	@Spy
	BaseVoidHandler voidHandler;
	
	RequiredDataAdvice requiredDataAdvice = new RequiredDataAdvice();
	
	@BeforeEach
	public void setUp() {
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
		assertTrue(fetchedLocations.contains(location));
	}
	
	/**
	 * @see RequiredDataAdvice#getChildCollection(OpenmrsObject,Field)
	 */
	@Test
	public void getChildCollection_shouldShouldBeAbleToGetAnnotatedPrivateFields() throws Exception {
		MiniOpenmrsObject oo = new MiniOpenmrsObject();
		oo.setLocations(new ArrayList<>());
		assertNotNull(RequiredDataAdvice
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
	@Test
	public void getChildCollection_shouldThrowAPIExceptionIfGetterMethodNotFound() throws Exception {
		ClassWithBadGetter oo = new ClassWithBadGetter();
		oo.setMyLocations(new HashSet<>());
		assertThrows(APIException.class, () -> RequiredDataAdvice.getChildCollection(oo, ClassWithBadGetter.class.getDeclaredField("locations")));
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
		assertFalse(RequiredDataAdvice.isOpenmrsObjectCollection(ClassWithOtherFields.class.getDeclaredField("locales")));
		List<String> list = new LinkedList<>();
		list.add("Test");
		assertFalse(RequiredDataAdvice.isOpenmrsObjectCollection(list));
		
	}
	
	/**
	 * @see RequiredDataAdvice#isOpenmrsObjectCollection(Field)
	 */
	@Test
	public void isOpenmrsObjectCollection_shouldReturnFalseIfFieldIsCollectionOfParameterizedType() throws Exception {
		assertFalse(RequiredDataAdvice.isOpenmrsObjectCollection(ClassWithOtherFields.class.getDeclaredField("nestedGenericProperty")));
	}
	
	/**
	 * @see RequiredDataAdvice#isOpenmrsObjectCollection(Field)
	 */
	@Test
	public void isOpenmrsObjectCollection_shouldReturnFalseIfFieldIsNotACollection() throws Exception {
		assertFalse(RequiredDataAdvice.isOpenmrsObjectCollection(ClassWithOtherFields.class.getDeclaredField("id")));
	}
	
	/**
	 * @see RequiredDataAdvice#isOpenmrsObjectCollection(Class<*>,Object)
	 */
	@Test
	public void isOpenmrsObjectCollection_shouldReturnTrueIfClassIsOpenmrsObjectList() throws Exception {
		List<Location> locations = new ArrayList<>();
		Location location = new Location();
		locations.add(location);
		assertTrue(RequiredDataAdvice.isOpenmrsObjectCollection(locations));
	}
	
	/**
	 * @see RequiredDataAdvice#isOpenmrsObjectCollection(Class<*>,Object)
	 */
	@Test
	public void isOpenmrsObjectCollection_shouldReturnTrueIfClassIsOpenmrsObjectSet() throws Exception {
		Set<Location> locations = new HashSet<>();
		Location location = new Location();
		locations.add(location);
		assertTrue(RequiredDataAdvice.isOpenmrsObjectCollection(locations));
	}
	
	/**
	 * @see RequiredDataAdvice#isOpenmrsObjectCollection(Class<*>,Object)
	 */
	@Test
	public void isOpenmrsObjectCollection_shouldReturnFalseIfCollectionIsEmptyRegardlessOfTypeHeld() throws Exception {
		Set<Location> locations = new HashSet<>();
		assertFalse(RequiredDataAdvice.isOpenmrsObjectCollection(locations));
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
		
		assertTrue(RequiredDataAdvice.isHandlerMarkedAsDisabled(SaveHandler.class, persons));
		assertTrue(RequiredDataAdvice.isHandlerMarkedAsDisabled(VoidHandler.class, persons));
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
		
		assertFalse(RequiredDataAdvice.isHandlerMarkedAsDisabled(RetireHandler.class, persons));
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
		
		assertFalse(RequiredDataAdvice.isHandlerMarkedAsDisabled(RetireHandler.class, persons));
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
		assertTrue(RequiredDataAdvice.isHandlerMarkedAsDisabled(SaveHandler.class, persons));
		assertTrue(RequiredDataAdvice.isHandlerMarkedAsDisabled(VoidHandler.class, persons));
		assertTrue(RequiredDataAdvice.isHandlerMarkedAsDisabled(UnvoidHandler.class, persons));
		assertTrue(RequiredDataAdvice.isHandlerMarkedAsDisabled(RetireHandler.class, persons));
		assertTrue(RequiredDataAdvice.isHandlerMarkedAsDisabled(UnretireHandler.class, persons));
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
		
		assertTrue(RequiredDataAdvice.isHandlerMarkedAsDisabled(UnretireHandler.class, persons));
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
		
		assertFalse(RequiredDataAdvice.isHandlerMarkedAsDisabled(RetireHandler.class, persons));
	}
	
	/**
	 * @see RequiredDataAdvice#before(Method, null, Object)
	 */
	@Test
	public void before_shouldNotFailOnUpdateMethodWithNoArguments() throws Throwable {
		Method method = ConceptServiceImpl.class.getMethod("updateConceptIndexes", (Class[]) null);
		requiredDataAdvice.before(method, null, new ConceptServiceImpl(mock(ConceptDAO.class), mock(MessageSourceService.class), mock(ObsService.class), mock(AdministrationService.class), mock(ConceptService.class)));
		requiredDataAdvice.before(method, new Object[] {}, new ConceptServiceImpl(mock(ConceptDAO.class), mock(MessageSourceService.class), mock(ObsService.class), mock(AdministrationService.class), mock(ConceptService.class)));
	}
	
	@Test
	public void before_shouldNotCallHandlerOnSaveWithNullOrNoArguments() throws Throwable {
		
		Method m = WithAppropriatelyNamedMethod.class.getMethod("saveSomeOpenmrsData", SomeOpenmrsData.class);
		SomeOpenmrsData openmrsObject = new SomeOpenmrsData();
		requiredDataAdvice.before(m, null, new WithAppropriatelyNamedMethod());
		requiredDataAdvice.before(m, new Object[] {}, new WithAppropriatelyNamedMethod());
		verify(saveHandler, never()).handle(eq(openmrsObject), any(), any(),
		    anyString());
	}
	
	@Test
	public void before_shouldCallHandlerOnSaveWithOpenmrsObjectArgument() throws Throwable {
		
		Map<String, SaveHandler> saveHandlers = new HashMap<>();
		saveHandlers.put("saveHandler", saveHandler);
		when(applicationContext.getBeansOfType(SaveHandler.class)).thenReturn(saveHandlers);
		
		Method m = WithAppropriatelyNamedMethod.class.getMethod("saveSomeOpenmrsData", SomeOpenmrsData.class);
		SomeOpenmrsData openmrsObject = new SomeOpenmrsData();
		requiredDataAdvice.before(m, new Object[] { openmrsObject }, new WithAppropriatelyNamedMethod());
		verify(saveHandler, times(1)).handle(eq(openmrsObject), any(), any(),
				any());
	}
	
	@Test
	public void before_shouldNotCallHandlerOnSaveMethodNameNotMatchingDomainObject() throws Throwable {
		
		Method m = WithAppropriatelyNamedMethod.class.getMethod("saveSomeOpenmrsDataButNotReally", SomeOpenmrsData.class);
		SomeOpenmrsData openmrsObject = new SomeOpenmrsData();
		requiredDataAdvice.before(m, new Object[] { openmrsObject }, new WithAppropriatelyNamedMethod());
		verify(saveHandler, never()).handle(eq(openmrsObject), any(), any(),
		    anyString());
	}
	
	@Test
	public void before_shouldCallHandlerOnSaveMethodNameWithCollectionArgument() throws Throwable {
		
		Map<String, SaveHandler> saveHandlers = new HashMap<>();
		saveHandlers.put("saveHandler", saveHandler);
		when(applicationContext.getBeansOfType(SaveHandler.class)).thenReturn(saveHandlers);
		
		Method m = WithAppropriatelyNamedMethod.class.getMethod("saveSomeOpenmrsDatas", List.class);
		List<SomeOpenmrsData> openmrsObjects = Arrays.asList(new SomeOpenmrsData(), new SomeOpenmrsData());
		requiredDataAdvice.before(m, new Object[] { openmrsObjects }, new WithAppropriatelyNamedMethod());
		verify(saveHandler, times(2)).handle(any(), any(),
		    any(), any());
	}
	
	@Test
	public void before_shouldNotCallHandlerOnVoidWithNullOrNoArguments() throws Throwable {
		
		Method m = WithAppropriatelyNamedMethod.class.getMethod("voidSomeOpenmrsData", SomeOpenmrsData.class);
		SomeOpenmrsData openmrsObject = new SomeOpenmrsData();
		requiredDataAdvice.before(m, null, new WithAppropriatelyNamedMethod());
		requiredDataAdvice.before(m, new Object[] {}, new WithAppropriatelyNamedMethod());
		verify(voidHandler, never()).handle(eq(openmrsObject), any(), any(),
		    anyString());
	}
	
	@Test
	public void before_shouldCallHandlerOnVoidMethodNameMatchingDomainObject() throws Throwable {
		
		Map<String, VoidHandler> voidHandlers = new HashMap<>();
		voidHandlers.put("voidHandler", voidHandler);
		when(applicationContext.getBeansOfType(VoidHandler.class)).thenReturn(voidHandlers);
		
		Method m = WithAppropriatelyNamedMethod.class.getMethod("voidSomeOpenmrsData", SomeOpenmrsData.class);
		SomeOpenmrsData openmrsObject = new SomeOpenmrsData();
		requiredDataAdvice.before(m, new Object[] { openmrsObject, "void reason" }, new WithAppropriatelyNamedMethod());
		verify(voidHandler, times(1)).handle(eq(openmrsObject), any(), any(),
		    anyString());
	}
	
	@Test
	public void before_shouldCallHandlerOnVoidMethodWhenDomainObjectIsAssignableFromMethodNameObject() throws Throwable {
		
		Map<String, VoidHandler> voidHandlers = new HashMap<>();
		voidHandlers.put("voidHandler", voidHandler);
		when(applicationContext.getBeansOfType(VoidHandler.class)).thenReturn(voidHandlers);
		
		Method m = WithAppropriatelyNamedMethod.class.getMethod("voidSomeOpenmrsData", SomeOpenmrsData.class);
		SomeOpenmrsData openmrsObjectSubClass = new SomeOpenmrsDataSubClass();
		requiredDataAdvice.before(m, new Object[] { openmrsObjectSubClass, "void reason" },
		    new WithAppropriatelyNamedMethod());
		verify(voidHandler, times(1)).handle(eq(openmrsObjectSubClass), any(),
		    any(), anyString());
	}
	
	@Test
	public void before_shouldNotCallHandlerOnVoidMethodNameNotMatchingDomainObject() throws Throwable {
		
		Method m = WithAppropriatelyNamedMethod.class.getMethod("voidSomeOpenmrsDataButNotReally", SomeOpenmrsData.class);
		SomeOpenmrsData openmrsObject = new SomeOpenmrsData();
		requiredDataAdvice.before(m, new Object[] { openmrsObject }, new WithAppropriatelyNamedMethod());
		verify(voidHandler, never()).handle(eq(openmrsObject), any(), any(),
		    anyString());
	}
	
	@Test
	public void before_shouldNotCallHandlersAnnotatedAsDisabled() throws Throwable {
		
		Map<String, VoidHandler> voidHandlers = new HashMap<>();
		voidHandlers.put("voidHandler", voidHandler);
		when(applicationContext.getBeansOfType(VoidHandler.class)).thenReturn(voidHandlers);
		
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
		verify(voidHandler, never()).handle(eq(person), any(), any(),
		    anyString());
		
	}
	
	@Test
	public void before_shouldCallHandlersNotAnnotatedAsDisabled() throws Throwable {
		
		Map<String, VoidHandler> voidHandlers = new HashMap<>();
		voidHandlers.put("voidHandler", voidHandler);
		when(applicationContext.getBeansOfType(VoidHandler.class)).thenReturn(voidHandlers);
		
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
		verify(voidHandler, times(1)).handle(eq(person), any(), any(),
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
