/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.api.impl;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockingbird.test.Animal;
import org.mockingbird.test.Cat;
import org.mockingbird.test.HibernateProxyAnimal;
import org.mockingbird.test.MockingBird;
import org.mockingbird.test.rest.resource.AnimalClassResource_1_9;
import org.mockingbird.test.rest.resource.AnimalResource_1_11;
import org.mockingbird.test.rest.resource.AnimalResource_1_9;
import org.mockingbird.test.rest.resource.BirdResource_1_9;
import org.mockingbird.test.rest.resource.CatSubclassHandler_1_11;
import org.mockingbird.test.rest.resource.CatSubclassHandler_1_9;
import org.mockingbird.test.rest.resource.CountryResource_1_9;
import org.mockingbird.test.rest.resource.DuplicateNameAndOrderAnimalResource_1_9;
import org.mockingbird.test.rest.resource.DuplicateNameAnimalResource_1_9;
import org.mockingbird.test.rest.resource.InstantiateExceptionAnimalResource_1_9;
import org.mockingbird.test.rest.resource.UnannotatedAnimalResource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.openmrs.api.APIException;
import org.openmrs.module.webservices.rest.web.OpenmrsClassScanner;
import org.openmrs.module.webservices.rest.web.RestUtil;
import org.openmrs.module.webservices.rest.web.api.RestHelperService;
import org.openmrs.module.webservices.rest.web.api.RestService;
import org.openmrs.module.webservices.rest.web.representation.CustomRepresentation;
import org.openmrs.module.webservices.rest.web.representation.NamedRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.api.Resource;
import org.openmrs.module.webservices.rest.web.resource.api.SearchConfig;
import org.openmrs.module.webservices.rest.web.resource.api.SearchHandler;
import org.openmrs.module.webservices.rest.web.resource.api.SearchQuery;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceHandler;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingSubclassHandler;
import org.openmrs.module.webservices.rest.web.response.InvalidSearchException;
import org.openmrs.module.webservices.rest.web.response.UnknownResourceException;
import org.openmrs.test.BaseContextMockTest;
import org.openmrs.util.OpenmrsConstants;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests {@link RestServiceImpl}.
 */
public class RestServiceImplTest extends BaseContextMockTest {
	
	@Mock
	RestHelperService restHelperService;
	
	@Mock
	OpenmrsClassScanner openmrsClassScanner;
	
	@InjectMocks
	RestService restService = new RestServiceImpl();
	
	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	
	/**
	 * @verifies return default representation if given null
	 * @see RestServiceImpl#getRepresentation(String)
	 */
	@Test
	public void getRepresentation_shouldReturnDefaultRepresentationIfGivenNull() throws Exception {
		
		assertThat(restService.getRepresentation(null), is(Representation.DEFAULT));
	}
	
	/**
	 * @verifies return default representation if given string is empty
	 * @see RestServiceImpl#getRepresentation(String)
	 */
	@Test
	public void getRepresentation_shouldReturnDefaultRepresentationIfGivenStringIsEmpty() throws Exception {
		
		assertThat(restService.getRepresentation(""), is(Representation.DEFAULT));
	}
	
	/**
	 * @verifies return reference representation if given string matches the ref representation
	 *           constant
	 * @see RestServiceImpl#getRepresentation(String)
	 */
	@Test
	public void getRepresentation_shouldReturnReferenceRepresentationIfGivenStringMatchesTheRefRepresentationConstant()
	        throws Exception {
		
		RestUtil.disableContext(); //to avoid a Context call
		assertThat(restService.getRepresentation("ref"), is(Representation.REF));
	}
	
	/**
	 * @verifies return default representation if given string matches the default representation
	 *           constant
	 * @see RestServiceImpl#getRepresentation(String)
	 */
	@Test
	public void getRepresentation_shouldReturnDefaultRepresentationIfGivenStringMatchesTheDefaultRepresentationConstant()
	        throws Exception {
		
		RestUtil.disableContext(); //to avoid a Context call
		assertThat(restService.getRepresentation("default"), is(Representation.DEFAULT));
	}
	
	/**
	 * @verifies return full representation if given string matches the full representation constant
	 * @see RestServiceImpl#getRepresentation(String)
	 */
	@Test
	public void getRepresentation_shouldReturnFullRepresentationIfGivenStringMatchesTheFullRepresentationConstant()
	        throws Exception {
		
		RestUtil.disableContext(); //to avoid a Context call
		assertThat(restService.getRepresentation("full"), is(Representation.FULL));
	}
	
	/**
	 * @verifies return an instance of custom representation if given string starts with the custom
	 *           representation prefix
	 * @see RestServiceImpl#getRepresentation(String)
	 */
	@Test
	public void getRepresentation_shouldReturnAnInstanceOfCustomRepresentationIfGivenStringStartsWithTheCustomRepresentationPrefix()
	        throws Exception {
		
		RestUtil.disableContext(); //to avoid a Context call
		Representation representation = restService.getRepresentation("custom:datatableslist");
		assertThat(representation, instanceOf(CustomRepresentation.class));
		assertThat(representation.getRepresentation(), is("datatableslist"));
	}
	
	/**
	 * @verifies return an instance of named representation for given string if it is not empty and
	 *           does not match any other case
	 * @see RestServiceImpl#getRepresentation(String)
	 */
	@Test
	public void getRepresentation_shouldReturnAnInstanceOfNamedRepresentationForGivenStringIfItIsNotEmptyAndDoesNotMatchAnyOtherCase()
	        throws Exception {
		
		RestUtil.disableContext(); //to avoid a Context call
		Representation representation = restService.getRepresentation("UNKNOWNREPRESENTATION");
		assertThat(representation, instanceOf(NamedRepresentation.class));
		assertThat(representation.getRepresentation(), is("UNKNOWNREPRESENTATION"));
	}
	
	/**
	 * @verifies return resource for given name
	 * @see RestServiceImpl#getResourceByName(String)
	 */
	@Test
	public void getResourceByName_shouldReturnResourceForGivenName() throws Exception {
		
		List<Class<? extends Resource>> resources = new ArrayList<Class<? extends Resource>>();
		resources.add(AnimalResource_1_9.class);
		resources.add(AnimalResource_1_11.class);
		
		when(openmrsClassScanner.getClasses(Resource.class, true)).thenReturn(resources);
		setCurrentOpenmrsVersion("1.9.10");
		
		assertThat(restService.getResourceByName("v1/animal"), instanceOf(AnimalResource_1_9.class));
	}
	
	/**
	 * @verifies return resource for given name and ignore unannotated resources
	 * @see RestServiceImpl#getResourceByName(String)
	 */
	@Test
	public void getResourceByName_shouldReturnResourceForGivenNameAndIgnoreUnannotatedResources() throws Exception {
		
		List<Class<? extends Resource>> resources = new ArrayList<Class<? extends Resource>>();
		resources.add(AnimalResource_1_9.class);
		resources.add(AnimalResource_1_11.class);
		resources.add(UnannotatedAnimalResource.class);
		
		when(openmrsClassScanner.getClasses(Resource.class, true)).thenReturn(resources);
		setCurrentOpenmrsVersion("1.9.10");
		
		assertThat(restService.getResourceByName("v1/animal"), instanceOf(AnimalResource_1_9.class));
	}
	
	/**
	 * Helper method to set the current OpenMRS version for tests.
	 * 
	 * @param currentOpenmrsVersion the openmrs version to set the current version to
	 * @throws NoSuchFieldException
	 * @throws IllegalAccessException
	 */
	private void setCurrentOpenmrsVersion(final String currentOpenmrsVersion) throws NoSuchFieldException,
	        IllegalAccessException {
		
		Field versionField = OpenmrsConstants.class.getDeclaredField("OPENMRS_VERSION_SHORT");
		Field modifiersField = Field.class.getDeclaredField("modifiers");
		modifiersField.setAccessible(true);
		modifiersField.setInt(versionField, versionField.getModifiers() & ~Modifier.FINAL);
		versionField.set(null, currentOpenmrsVersion);
	}
	
	/**
	 * @verifies fail if failed to get resource classes
	 * @see RestServiceImpl#getResourceByName(String)
	 */
	@Test
	public void getResourceByName_shouldFailIfFailedToGetResourceClasses() throws Exception {
		
		IOException ioException = new IOException("some");
		
		when(openmrsClassScanner.getClasses(Resource.class, true)).thenThrow(ioException);
		
		expectedException.expect(APIException.class);
		expectedException.expectMessage("Cannot access REST resources");
		expectedException.expectCause(is(ioException));
		restService.getResourceByName("v1/animal");
	}
	
	/**
	 * @verifies fail if resource for given name cannot be found
	 * @see RestServiceImpl#getResourceByName(String)
	 */
	@Test
	public void getResourceByName_shouldFailIfResourceForGivenNameCannotBeFound() throws Exception {
		
		expectedException.expect(UnknownResourceException.class);
		expectedException.expectMessage("Unknown resource: UNKNOWNRESOURCENAME");
		restService.getResourceByName("UNKNOWNRESOURCENAME");
	}
	
	/**
	 * @verifies fail if resource for given name does not support the current openmrs version
	 * @see RestServiceImpl#getResourceByName(String)
	 */
	@Test
	public void getResourceByName_shouldFailIfResourceForGivenNameDoesNotSupportTheCurrentOpenmrsVersion() throws Exception {
		
		List<Class<? extends Resource>> resources = new ArrayList<Class<? extends Resource>>();
		resources.add(AnimalResource_1_9.class);
		resources.add(AnimalResource_1_11.class);
		
		when(openmrsClassScanner.getClasses(Resource.class, true)).thenReturn(resources);
		setCurrentOpenmrsVersion("1.12.0");
		
		expectedException.expect(UnknownResourceException.class);
		expectedException.expectMessage("Unknown resource: v1/animal");
		restService.getResourceByName("v1/animal");
	}
	
	/**
	 * @verifies return subresource for given name
	 * @see RestServiceImpl#getResourceByName(String)
	 */
	@Test
	public void getResourceByName_shouldReturnSubresourceForGivenName() throws Exception {
		
		List<Class<? extends Resource>> resources = new ArrayList<Class<? extends Resource>>();
		resources.add(AnimalResource_1_9.class);
		resources.add(AnimalResource_1_11.class);
		resources.add(AnimalClassResource_1_9.class);
		
		when(openmrsClassScanner.getClasses(Resource.class, true)).thenReturn(resources);
		setCurrentOpenmrsVersion("1.9.10");
		
		assertThat(restService.getResourceByName("v1/animal/class"), instanceOf(AnimalClassResource_1_9.class));
	}
	
	/**
	 * @verifies fail if subresource for given name does not support the current openmrs version
	 * @see RestServiceImpl#getResourceByName(String)
	 */
	@Test
	public void getResourceByName_shouldFailIfSubresourceForGivenNameDoesNotSupportTheCurrentOpenmrsVersion()
	        throws Exception {
		
		List<Class<? extends Resource>> resources = new ArrayList<Class<? extends Resource>>();
		resources.add(AnimalResource_1_9.class);
		resources.add(AnimalResource_1_11.class);
		resources.add(AnimalClassResource_1_9.class);
		
		when(openmrsClassScanner.getClasses(Resource.class, true)).thenReturn(resources);
		setCurrentOpenmrsVersion("1.12.0");
		
		expectedException.expect(UnknownResourceException.class);
		expectedException.expectMessage("Unknown resource: v1/animal/class");
		restService.getResourceByName("v1/animal/class");
	}
	
	/**
	 * @verifies fail if two resources with same name and order are found for given name
	 * @see RestServiceImpl#getResourceByName(String)
	 */
	@Test
	public void getResourceByName_shouldFailIfTwoResourcesWithSameNameAndOrderAreFoundForGivenName() throws Exception {
		
		List<Class<? extends Resource>> resources = new ArrayList<Class<? extends Resource>>();
		resources.add(AnimalResource_1_9.class);
		resources.add(DuplicateNameAndOrderAnimalResource_1_9.class);
		
		when(openmrsClassScanner.getClasses(Resource.class, true)).thenReturn(resources);
		setCurrentOpenmrsVersion("1.9.10");
		
		expectedException.expect(IllegalStateException.class);
		expectedException.expectMessage("Two resources with the same name (v1/animal) must not have the same order");
		restService.getResourceByName("v1/animal");
	}
	
	/**
	 * @verifies return resource with lower order value if two resources with the same name are
	 *           found for given name
	 * @see RestServiceImpl#getResourceByName(String)
	 */
	@Test
	public void getResourceByName_shouldReturnResourceWithLowerOrderValueIfTwoResourcesWithTheSameNameAreFoundForGivenName()
	        throws Exception {
		
		List<Class<? extends Resource>> resources = new ArrayList<Class<? extends Resource>>();
		resources.add(AnimalResource_1_9.class);
		resources.add(DuplicateNameAnimalResource_1_9.class);
		
		when(openmrsClassScanner.getClasses(Resource.class, true)).thenReturn(resources);
		setCurrentOpenmrsVersion("1.9.10");
		
		assertThat(restService.getResourceByName("v1/animal"), instanceOf(AnimalResource_1_9.class));
	}
	
	/**
	 * @verifies return resource supporting given class and current openmrs version
	 * @see RestServiceImpl#getResourceBySupportedClass(Class)
	 */
	@Test
	public void getResourceBySupportedClass_shouldReturnResourceSupportingGivenClassAndCurrentOpenmrsVersion()
	        throws Exception {
		
		List<Class<? extends Resource>> resources = new ArrayList<Class<? extends Resource>>();
		resources.add(AnimalResource_1_9.class);
		
		when(openmrsClassScanner.getClasses(Resource.class, true)).thenReturn(resources);
		setCurrentOpenmrsVersion("1.9.10");
		
		assertThat(restService.getResourceBySupportedClass(Animal.class), instanceOf(AnimalResource_1_9.class));
	}
	
	/**
	 * @verifies fail if no resource supporting given class and current openmrs version was found
	 * @see RestServiceImpl#getResourceBySupportedClass(Class)
	 */
	@Test
	public void getResourceBySupportedClass_shouldFailIfNoResourceSupportingGivenClassAndCurrentOpenmrsVersionWasFound()
	        throws Exception {
		
		List<Class<? extends Resource>> resources = new ArrayList<Class<? extends Resource>>();
		resources.add(AnimalResource_1_9.class);
		resources.add(AnimalResource_1_11.class);
		
		when(openmrsClassScanner.getClasses(Resource.class, true)).thenReturn(resources);
		setCurrentOpenmrsVersion("1.12.0");
		
		expectedException.expect(APIException.class);
		expectedException.expectMessage("Unknown resource: class org.mockingbird.test.Animal");
		restService.getResourceBySupportedClass(Animal.class);
	}
	
	/**
	 * @verifies fail if no resource supporting given class was found
	 * @see RestServiceImpl#getResourceBySupportedClass(Class)
	 */
	@Test
	public void getResourceBySupportedClass_shouldFailIfNoResourceSupportingGivenClassWasFound() throws Exception {
		
		List<Class<? extends Resource>> resources = new ArrayList<Class<? extends Resource>>();
		resources.add(AnimalResource_1_9.class);
		resources.add(AnimalResource_1_11.class);
		
		when(openmrsClassScanner.getClasses(Resource.class, true)).thenReturn(resources);
		setCurrentOpenmrsVersion("1.9.10");
		
		expectedException.expect(APIException.class);
		expectedException.expectMessage("Unknown resource: class java.lang.String");
		restService.getResourceBySupportedClass(String.class);
	}
	
	/**
	 * @verifies return resource supporting superclass of given class if given class is a hibernate
	 *           proxy
	 * @see RestServiceImpl#getResourceBySupportedClass(Class)
	 */
	@Test
	public void getResourceBySupportedClass_shouldReturnResourceSupportingSuperclassOfGivenClassIfGivenClassIsAHibernateProxy()
	        throws Exception {
		
		List<Class<? extends Resource>> resources = new ArrayList<Class<? extends Resource>>();
		resources.add(AnimalResource_1_9.class);
		resources.add(AnimalResource_1_11.class);
		
		when(openmrsClassScanner.getClasses(Resource.class, true)).thenReturn(resources);
		setCurrentOpenmrsVersion("1.9.10");
		
		assertThat(restService.getResourceBySupportedClass(HibernateProxyAnimal.class), instanceOf(AnimalResource_1_9.class));
	}
	
	/**
	 * @verifies return resource supporting superclass of given class if no resource supporting
	 *           given class was found
	 * @see RestServiceImpl#getResourceBySupportedClass(Class)
	 */
	@Test
	public void getResourceBySupportedClass_shouldReturnResourceSupportingSuperclassOfGivenClassIfNoResourceSupportingGivenClassWasFound()
	        throws Exception {
		
		List<Class<? extends Resource>> resources = new ArrayList<Class<? extends Resource>>();
		resources.add(AnimalResource_1_9.class);
		resources.add(AnimalResource_1_11.class);
		
		when(openmrsClassScanner.getClasses(Resource.class, true)).thenReturn(resources);
		setCurrentOpenmrsVersion("1.9.10");
		
		assertThat(restService.getResourceBySupportedClass(Cat.class), instanceOf(AnimalResource_1_9.class));
	}
	
	/**
	 * @verifies return resource supporting direct superclass of given class if no resource
	 *           supporting given class was found but multiple resources supporting multiple
	 *           superclasses exist
	 * @see RestServiceImpl#getResourceBySupportedClass(Class) <p>
	 */
	@Test
	public void getResourceBySupportedClass_shouldReturnResourceSupportingDirectSuperclassOfGivenClassIfNoResourceSupportingGivenClassWasFoundButMultipleResourcesSupportingMultipleSuperclassesExist()
	        throws Exception {
		
		List<Class<? extends Resource>> resources = new ArrayList<Class<? extends Resource>>();
		resources.add(AnimalResource_1_9.class);
		resources.add(AnimalResource_1_11.class);
		resources.add(BirdResource_1_9.class);
		
		when(openmrsClassScanner.getClasses(Resource.class, true)).thenReturn(resources);
		setCurrentOpenmrsVersion("1.9.10");
		
		assertThat(restService.getResourceBySupportedClass(MockingBird.class), instanceOf(BirdResource_1_9.class));
	}
	
	/**
	 * @verifies fail if failed to get resource classes
	 * @see RestServiceImpl#getResourceBySupportedClass(Class)
	 */
	@Test
	public void getResourceBySupportedClass_shouldFailIfFailedToGetResourceClasses() throws Exception {
		
		IOException ioException = new IOException("some");
		
		when(openmrsClassScanner.getClasses(Resource.class, true)).thenThrow(ioException);
		
		expectedException.expect(APIException.class);
		expectedException.expectMessage("Cannot access REST resources");
		expectedException.expectCause(is(ioException));
		restService.getResourceByName("v1/animal");
	}
	
	/**
	 * @verifies fail if two resources with same name and order are found for given class
	 * @see RestServiceImpl#getResourceBySupportedClass(Class)
	 */
	@Test
	public void getResourceBySupportedClass_shouldFailIfTwoResourcesWithSameNameAndOrderAreFoundForGivenClass()
	        throws Exception {
		
		List<Class<? extends Resource>> resources = new ArrayList<Class<? extends Resource>>();
		resources.add(AnimalResource_1_9.class);
		resources.add(DuplicateNameAndOrderAnimalResource_1_9.class);
		
		when(openmrsClassScanner.getClasses(Resource.class, true)).thenReturn(resources);
		setCurrentOpenmrsVersion("1.9.10");
		
		expectedException.expect(IllegalStateException.class);
		expectedException.expectMessage("Two resources with the same name (v1/animal) must not have the same order");
		restService.getResourceBySupportedClass(Animal.class);
	}
	
	/**
	 * @verifies return resource with lower order value if two resources with the same name are
	 *           found for given class
	 * @see RestServiceImpl#getResourceBySupportedClass(Class)
	 */
	@Test
	public void getResourceBySupportedClass_shouldReturnResourceWithLowerOrderValueIfTwoResourcesWithTheSameNameAreFoundForGivenClass()
	        throws Exception {
		
		List<Class<? extends Resource>> resources = new ArrayList<Class<? extends Resource>>();
		resources.add(AnimalResource_1_9.class);
		resources.add(DuplicateNameAnimalResource_1_9.class);
		
		when(openmrsClassScanner.getClasses(Resource.class, true)).thenReturn(resources);
		setCurrentOpenmrsVersion("1.9.10");
		
		assertThat(restService.getResourceBySupportedClass(Animal.class), instanceOf(AnimalResource_1_9.class));
	}
	
	/**
	 * @verifies return search handler matching id set in given parameters
	 * @see RestServiceImpl#getSearchHandler(String, Map)
	 */
	@Test
	public void getSearchHandler_shouldReturnSearchHandlerMatchingIdSetInGivenParameters() throws Exception {
		
		SearchHandler searchHandler = mock(SearchHandler.class);
		SearchConfig searchConfig = new SearchConfig("conceptByMapping", "v1/concept", "1.8.*", new SearchQuery.Builder(
		        "Fuzzy search").withRequiredParameters("q").build());
		when(searchHandler.getSearchConfig()).thenReturn(searchConfig);
		when(restHelperService.getRegisteredSearchHandlers()).thenReturn(asList(searchHandler));
		
		setCurrentOpenmrsVersion("1.8.10");
		
		Map<String, String[]> parameters = new HashMap<String, String[]>();
		parameters.put("s", new String[] { "conceptByMapping" });
		assertThat(restService.getSearchHandler("v1/concept", parameters), is(searchHandler));
	}
	
	/**
	 * @verifies fail if parameters contain a search handler id which cannot be found
	 * @see RestServiceImpl#getSearchHandler(String, Map)
	 */
	@Test
	public void getSearchHandler_shouldFailIfParametersContainASearchHandlerIdWhichCannotBeFound() throws Exception {
		
		SearchHandler searchHandler = mock(SearchHandler.class);
		SearchConfig searchConfig = new SearchConfig("default", "v1/concept", "1.8.*", new SearchQuery.Builder(
		        "Fuzzy search").withRequiredParameters("q").build());
		when(searchHandler.getSearchConfig()).thenReturn(searchConfig);
		when(restHelperService.getRegisteredSearchHandlers()).thenReturn(asList(searchHandler));
		
		setCurrentOpenmrsVersion("1.8.10");
		
		String searchHandlerId = "conceptByMapping";
		Map<String, String[]> parameters = new HashMap<String, String[]>();
		parameters.put("s", new String[] { searchHandlerId });
		
		expectedException.expect(InvalidSearchException.class);
		expectedException.expectMessage("The search with id '" + searchHandlerId + "' for '"
		        + searchConfig.getSupportedResource() + "' resource is not recognized");
		restService.getSearchHandler("v1/concept", parameters);
	}
	
	/**
	 * @verifies fail if two search handlers for the same resource have the same id
	 * @see RestServiceImpl#getSearchHandler(String, Map)
	 */
	@Test
	public void getSearchHandler_shouldFailIfTwoSearchHandlersForTheSameResourceHaveTheSameId() throws Exception {
		
		SearchHandler searchHandler1 = mock(SearchHandler.class);
		SearchConfig searchConfig1 = new SearchConfig("conceptByMapping", "v1/concept", "1.8.*", new SearchQuery.Builder(
		        "description").withRequiredParameters("source").withOptionalParameters("code").build());
		when(searchHandler1.getSearchConfig()).thenReturn(searchConfig1);
		
		SearchHandler searchHandler2 = mock(SearchHandler.class);
		SearchConfig searchConfig2 = new SearchConfig("conceptByMapping", "v1/concept", "1.8.*", new SearchQuery.Builder(
		        "description").withRequiredParameters("source").withOptionalParameters("code").build());
		when(searchHandler2.getSearchConfig()).thenReturn(searchConfig2);
		
		setCurrentOpenmrsVersion("1.8.10");
		
		when(restHelperService.getRegisteredSearchHandlers()).thenReturn(asList(searchHandler1, searchHandler2));
		
		RestUtil.disableContext(); //to avoid a Context call
		
		Map<String, String[]> parameters = new HashMap<String, String[]>();
		parameters.put("q", new String[] { "some name" });
		
		expectedException.expect(IllegalStateException.class);
		expectedException.expectMessage(startsWith("Two search handlers ("));
		expectedException
		        .expectMessage(endsWith("for the same resource (v1/concept) must not have the same ID (conceptByMapping)"));
		restService.getSearchHandlers("v1/concept");
	}
	
	/**
	 * @verifies return null if parameters do not contain a search handler id and no other non
	 *           special request parameters
	 * @see RestServiceImpl#getSearchHandler(String, Map)
	 */
	@Test
	public void getSearchHandler_shouldReturnNullIfParametersDoNotContainASearchHandlerIdAndNoOtherNonSpecialRequestParameters()
	        throws Exception {
		
		SearchHandler searchHandler1 = mock(SearchHandler.class);
		SearchConfig searchConfig = new SearchConfig("default", "v1/concept", "1.8.*",
		        new SearchQuery.Builder("description").withOptionalParameters("code").build());
		when(searchHandler1.getSearchConfig()).thenReturn(searchConfig);
		
		setCurrentOpenmrsVersion("1.8.10");
		
		when(restHelperService.getRegisteredSearchHandlers()).thenReturn(asList(searchHandler1));
		
		RestUtil.disableContext(); //to avoid a Context call
		
		Map<String, String[]> parameters = new HashMap<String, String[]>();
		
		assertThat(restService.getSearchHandler("v1/concept", parameters), is(nullValue()));
	}
	
	/**
	 * @verifies return search handler providing all request parameters and parameters satisfying
	 *           its required parameters
	 * @see RestServiceImpl#getSearchHandler(String, Map)
	 */
	@Test
	public void getSearchHandler_shouldReturnSearchHandlerProvidingAllRequestParametersAndParametersSatisfyingItsRequiredParameters()
	        throws Exception {
		
		SearchHandler searchHandler1 = mock(SearchHandler.class);
		SearchConfig searchConfig1 = new SearchConfig("default", "v1/concept", "1.8.*", new SearchQuery.Builder(
		        "description").withRequiredParameters("source").withOptionalParameters("code").build());
		when(searchHandler1.getSearchConfig()).thenReturn(searchConfig1);
		
		SearchHandler searchHandler2 = mock(SearchHandler.class);
		SearchConfig searchConfig2 = new SearchConfig("conceptByMapping", "v1/concept", "1.8.*", new SearchQuery.Builder(
		        "description").withRequiredParameters("source").build());
		when(searchHandler2.getSearchConfig()).thenReturn(searchConfig2);
		
		setCurrentOpenmrsVersion("1.8.10");
		
		when(restHelperService.getRegisteredSearchHandlers()).thenReturn(asList(searchHandler1, searchHandler2));
		
		RestUtil.disableContext(); //to avoid a Context call
		
		Map<String, String[]> parameters = new HashMap<String, String[]>();
		parameters.put("source", new String[] { "some source" });
		parameters.put("code", new String[] { "some code" });
		
		assertThat(restService.getSearchHandler("v1/concept", parameters), is(searchHandler1));
	}
	
	/**
	 * @verifies return null if given parameters are missing a parameter required by search handlers
	 *           eligible for given resource name and parameters
	 * @see RestServiceImpl#getSearchHandler(String, Map)
	 */
	@Test
	public void getSearchHandler_shouldReturnNullIfGivenParametersAreMissingAParameterRequiredBySearchHandlersEligibleForGivenResourceNameAndParameters()
	        throws Exception {
		
		SearchHandler searchHandler1 = mock(SearchHandler.class);
		SearchConfig searchConfig1 = new SearchConfig("default", "v1/concept", "1.8.*", new SearchQuery.Builder(
		        "description").withRequiredParameters("source").withOptionalParameters("code").build());
		when(searchHandler1.getSearchConfig()).thenReturn(searchConfig1);
		
		SearchHandler searchHandler2 = mock(SearchHandler.class);
		SearchConfig searchConfig2 = new SearchConfig("conceptByMapping", "v1/concept", "1.8.*", new SearchQuery.Builder(
		        "description").withRequiredParameters("source").build());
		when(searchHandler2.getSearchConfig()).thenReturn(searchConfig2);
		
		setCurrentOpenmrsVersion("1.8.10");
		
		when(restHelperService.getRegisteredSearchHandlers()).thenReturn(asList(searchHandler1, searchHandler2));
		
		RestUtil.disableContext(); //to avoid a Context call
		
		Map<String, String[]> parameters = new HashMap<String, String[]>();
		parameters.put("code", new String[] { "some name" });
		
		assertThat(restService.getSearchHandler("v1/concept", parameters), is(nullValue()));
	}
	
	/**
	 * @verifies fail if two search handlers match given resource and parameters and no search
	 *           handler id is specified
	 * @see RestServiceImpl#getSearchHandler(String, Map)
	 */
	@Test
	public void getSearchHandler_shouldFailIfTwoSearchHandlersMatchGivenResourceAndParametersAndNoSearchHandlerIdIsSpecified()
	        throws Exception {
		
		SearchHandler searchHandler1 = mock(SearchHandler.class);
		SearchConfig searchConfig1 = new SearchConfig("default", "v1/concept", "1.8.*", new SearchQuery.Builder(
		        "description").withRequiredParameters("source").withOptionalParameters("code").build());
		when(searchHandler1.getSearchConfig()).thenReturn(searchConfig1);
		
		SearchHandler searchHandler2 = mock(SearchHandler.class);
		SearchConfig searchConfig2 = new SearchConfig("conceptByMapping", "v1/concept", "1.8.*", new SearchQuery.Builder(
		        "description").withRequiredParameters("source").withOptionalParameters("code").build());
		when(searchHandler2.getSearchConfig()).thenReturn(searchConfig2);
		
		setCurrentOpenmrsVersion("1.8.10");
		
		when(restHelperService.getRegisteredSearchHandlers()).thenReturn(asList(searchHandler1, searchHandler2));
		
		RestUtil.disableContext(); //to avoid a Context call
		
		Map<String, String[]> parameters = new HashMap<String, String[]>();
		parameters.put("source", new String[] { "some name" });
		parameters.put("code", new String[] { "some code" });
		
		expectedException.expect(InvalidSearchException.class);
		expectedException.expectMessage("The search is ambiguous. Please specify s=");
		restService.getSearchHandler("v1/concept", parameters);
	}
	
	/**
	 * @verifies return null if a non special request parameter in given parameters cannot be found
	 *           in any search handler
	 * @see RestServiceImpl#getSearchHandler(String, Map)
	 */
	@Test
	public void getSearchHandler_shouldReturnNullIfANonSpecialRequestParameterInGivenParametersCannotBeFoundInAnySearchHandler()
	        throws Exception {
		
		SearchHandler searchHandler1 = mock(SearchHandler.class);
		SearchConfig searchConfig1 = new SearchConfig("default", "v1/concept", "1.8.*", new SearchQuery.Builder(
		        "description").withRequiredParameters("source").withOptionalParameters("code").build());
		when(searchHandler1.getSearchConfig()).thenReturn(searchConfig1);
		
		SearchHandler searchHandler2 = mock(SearchHandler.class);
		SearchConfig searchConfig2 = new SearchConfig("conceptByMapping", "v1/concept", "1.8.*", new SearchQuery.Builder(
		        "description").withRequiredParameters("source").build());
		when(searchHandler2.getSearchConfig()).thenReturn(searchConfig2);
		
		setCurrentOpenmrsVersion("1.8.10");
		
		when(restHelperService.getRegisteredSearchHandlers()).thenReturn(asList(searchHandler1, searchHandler2));
		
		RestUtil.disableContext(); //to avoid a Context call
		
		Map<String, String[]> parameters = new HashMap<String, String[]>();
		parameters.put("date", new String[] { "some date" });
		
		assertThat(restService.getSearchHandler("v1/concept", parameters), is(nullValue()));
	}
	
	/**
	 * @verifies return null if no search handler is found for given resource name
	 * @see RestServiceImpl#getSearchHandler(String, Map)
	 */
	@Test
	public void getSearchHandler_shouldReturnNullIfNoSearchHandlerIsFoundForGivenResourceName() throws Exception {
		
		SearchHandler searchHandler1 = mock(SearchHandler.class);
		SearchConfig searchConfig1 = new SearchConfig("default", "v1/concept", "1.8.*", new SearchQuery.Builder(
		        "description").withRequiredParameters("source").withOptionalParameters("code").build());
		when(searchHandler1.getSearchConfig()).thenReturn(searchConfig1);
		
		setCurrentOpenmrsVersion("1.8.10");
		
		when(restHelperService.getRegisteredSearchHandlers()).thenReturn(asList(searchHandler1));
		
		RestUtil.disableContext(); //to avoid a Context call
		
		Map<String, String[]> parameters = new HashMap<String, String[]>();
		parameters.put("source", new String[] { "some source" });
		
		assertThat(restService.getSearchHandler("v1/order", parameters), is(nullValue()));
	}
	
	/**
	 * @verifies return null if no search handler is found for current openmrs version
	 * @see RestServiceImpl#getSearchHandler(String, Map)
	 */
	@Test
	public void getSearchHandler_shouldReturnNullIfNoSearchHandlerIsFoundForCurrentOpenmrsVersion() throws Exception {
		
		SearchHandler searchHandler1 = mock(SearchHandler.class);
		SearchConfig searchConfig1 = new SearchConfig("default", "v1/concept", "1.8.*", new SearchQuery.Builder(
		        "description").withRequiredParameters("source").withOptionalParameters("code").build());
		when(searchHandler1.getSearchConfig()).thenReturn(searchConfig1);
		
		setCurrentOpenmrsVersion("1.12.0");
		
		when(restHelperService.getRegisteredSearchHandlers()).thenReturn(asList(searchHandler1));
		
		RestUtil.disableContext(); //to avoid a Context call
		
		Map<String, String[]> parameters = new HashMap<String, String[]>();
		parameters.put("source", new String[] { "some source" });
		
		assertThat(restService.getSearchHandler("v1/concept", parameters), is(nullValue()));
	}
	
	/**
	 * @verifies return list of delegating resource handlers including subclass handlers
	 * @see RestServiceImpl#getResourceHandlers()
	 */
	@Test
	public void getResourceHandlers_shouldReturnListOfDelegatingResourceHandlersIncludingSubclassHandlers() throws Exception {
		
		List<Class<? extends Resource>> resources = new ArrayList<Class<? extends Resource>>();
		resources.add(AnimalResource_1_9.class);
		resources.add(AnimalResource_1_11.class);
		resources.add(CountryResource_1_9.class);
		
		CatSubclassHandler_1_9 catSubclassHandler_1_9 = mock(CatSubclassHandler_1_9.class);
		CatSubclassHandler_1_11 catSubclassHandler_1_11 = mock(CatSubclassHandler_1_11.class);
		when(restHelperService.getRegisteredRegisteredSubclassHandlers()).thenReturn(
		    Arrays.<DelegatingSubclassHandler> asList(catSubclassHandler_1_9, catSubclassHandler_1_11));
		
		when(openmrsClassScanner.getClasses(Resource.class, true)).thenReturn(resources);
		setCurrentOpenmrsVersion("1.9.10");
		
		assertThat(restService.getResourceHandlers().size(), is(3));
		List<DelegatingResourceHandler<?>> handlers = restService.getResourceHandlers();
		assertThat(handlers, hasItem(catSubclassHandler_1_9));
		assertThat(handlers, hasItem(catSubclassHandler_1_11));
		for (DelegatingResourceHandler handler : handlers) {
			assertThat(handler, is(not(instanceOf(CountryResource_1_9.class))));
			assertThat(handler, is(not(instanceOf(AnimalResource_1_11.class))));
		}
	}
	
	/**
	 * @verifies return list with delegating resource with lower order value if two resources with
	 *           the same name are found for given name
	 * @see RestServiceImpl#getResourceHandlers()
	 */
	@Test
	public void getResourceHandlers_shouldReturnListWithDelegatingResourceWithLowerOrderValueIfTwoResourcesWithTheSameNameAreFoundForGivenName()
	        throws Exception {
		
		List<Class<? extends Resource>> resources = new ArrayList<Class<? extends Resource>>();
		resources.add(AnimalResource_1_9.class);
		resources.add(DuplicateNameAnimalResource_1_9.class);
		
		when(openmrsClassScanner.getClasses(Resource.class, true)).thenReturn(resources);
		setCurrentOpenmrsVersion("1.9.10");
		
		assertThat(restService.getResourceHandlers().size(), is(1));
		assertThat(restService.getResourceHandlers().get(0), instanceOf(AnimalResource_1_9.class));
	}
	
	/**
	 * @verifies fail if failed to get resource classes
	 * @see RestServiceImpl#getResourceHandlers()
	 */
	@Test
	public void getResourceHandlers_shouldFailIfFailedToGetResourceClasses() throws Exception {
		
		IOException ioException = new IOException("some");
		
		when(openmrsClassScanner.getClasses(Resource.class, true)).thenThrow(ioException);
		
		expectedException.expect(APIException.class);
		expectedException.expectMessage("Cannot access REST resources");
		expectedException.expectCause(is(ioException));
		restService.getResourceHandlers();
	}
	
	/**
	 * @verifies fail if two resources with same name and order are found for a class
	 * @see RestServiceImpl#getResourceHandlers()
	 */
	@Test
	public void getResourceHandlers_shouldFailIfTwoResourcesWithSameNameAndOrderAreFoundForAClass() throws Exception {
		
		List<Class<? extends Resource>> resources = new ArrayList<Class<? extends Resource>>();
		resources.add(AnimalResource_1_9.class);
		resources.add(DuplicateNameAndOrderAnimalResource_1_9.class);
		
		when(openmrsClassScanner.getClasses(Resource.class, true)).thenReturn(resources);
		setCurrentOpenmrsVersion("1.9.10");
		
		expectedException.expect(IllegalStateException.class);
		expectedException.expectMessage("Two resources with the same name (v1/animal) must not have the same order");
		restService.getResourceHandlers();
	}
	
	/**
	 * @verifies return all search handlers if search handlers have been initialized
	 * @see RestServiceImpl#getAllSearchHandlers()
	 */
	@Test
	public void getAllSearchHandlers_shouldReturnAllSearchHandlersIfSearchHandlersHaveBeenInitialized() throws Exception {
		
		SearchHandler searchHandler1 = mock(SearchHandler.class);
		SearchConfig searchConfig1 = new SearchConfig("default", "v1/concept", "1.8.*", new SearchQuery.Builder(
		        "description").withRequiredParameters("source").withOptionalParameters("code").build());
		when(searchHandler1.getSearchConfig()).thenReturn(searchConfig1);
		
		SearchHandler searchHandler2 = mock(SearchHandler.class);
		SearchConfig searchConfig2 = new SearchConfig("conceptByMapping", "v1/concept", "1.8.*", new SearchQuery.Builder(
		        "description").withRequiredParameters("source").build());
		when(searchHandler2.getSearchConfig()).thenReturn(searchConfig2);
		
		SearchHandler searchHandler3 = mock(SearchHandler.class);
		SearchConfig searchConfig3 = new SearchConfig("default", "v1/order", "1.11.*",
		        new SearchQuery.Builder("description").withRequiredParameters("patient").build());
		when(searchHandler3.getSearchConfig()).thenReturn(searchConfig3);
		
		SearchHandler searchHandler4 = mock(SearchHandler.class);
		SearchConfig searchConfig4 = new SearchConfig("conceptByMapping", "v1/concept", "1.9.*", new SearchQuery.Builder(
		        "description").withRequiredParameters("source").build());
		when(searchHandler4.getSearchConfig()).thenReturn(searchConfig4);
		
		setCurrentOpenmrsVersion("1.8.10");
		
		when(restHelperService.getRegisteredSearchHandlers()).thenReturn(
		    asList(searchHandler1, searchHandler2, searchHandler3, searchHandler4));
		
		RestUtil.disableContext(); //to avoid a Context call
		
		restService.initialize();
		List<SearchHandler> searchHandlers = restService.getAllSearchHandlers();
		assertThat(searchHandlers.size(), is(4));
		assertThat(searchHandlers, hasItem(searchHandler1));
		assertThat(searchHandlers, hasItem(searchHandler2));
		assertThat(searchHandlers, hasItem(searchHandler3));
		assertThat(searchHandlers, hasItem(searchHandler4));
	}
	
	/**
	 * @verifies return null if search handlers have not been initialized
	 * @see RestServiceImpl#getAllSearchHandlers()
	 */
	@Test
	public void getAllSearchHandlers_shouldReturnNullIfSearchHandlersHaveNotBeenInitialized() throws Exception {
		
		assertThat(restService.getAllSearchHandlers(), is(nullValue()));
	}
	
	/**
	 * @verifies return search handlers for given resource name
	 * @see RestServiceImpl#getSearchHandlers(String)
	 */
	@Test
	public void getSearchHandlers_shouldReturnSearchHandlersForGivenResourceName() throws Exception {
		
		SearchHandler searchHandler1 = mock(SearchHandler.class);
		SearchConfig searchConfig1 = new SearchConfig("default", "v1/concept", "1.8.*", new SearchQuery.Builder(
		        "description").withRequiredParameters("source").withOptionalParameters("code").build());
		when(searchHandler1.getSearchConfig()).thenReturn(searchConfig1);
		
		SearchHandler searchHandler2 = mock(SearchHandler.class);
		SearchConfig searchConfig2 = new SearchConfig("conceptByMapping", "v1/concept", "1.8.*", new SearchQuery.Builder(
		        "description").withRequiredParameters("source").build());
		when(searchHandler2.getSearchConfig()).thenReturn(searchConfig2);
		
		SearchHandler searchHandler3 = mock(SearchHandler.class);
		SearchConfig searchConfig3 = new SearchConfig("default", "v1/order", "1.8.*", new SearchQuery.Builder("description")
		        .withRequiredParameters("patient").build());
		when(searchHandler3.getSearchConfig()).thenReturn(searchConfig3);
		
		SearchHandler searchHandler4 = mock(SearchHandler.class);
		SearchConfig searchConfig4 = new SearchConfig("conceptByMapping", "v1/concept", "1.9.*", new SearchQuery.Builder(
		        "description").withRequiredParameters("source").build());
		when(searchHandler4.getSearchConfig()).thenReturn(searchConfig4);
		
		setCurrentOpenmrsVersion("1.8.10");
		
		when(restHelperService.getRegisteredSearchHandlers()).thenReturn(
		    asList(searchHandler1, searchHandler2, searchHandler3, searchHandler4));
		
		RestUtil.disableContext(); //to avoid a Context call
		
		Set<SearchHandler> searchHandlers = restService.getSearchHandlers("v1/concept");
		assertThat(searchHandlers.size(), is(2));
		assertThat(searchHandlers, hasItem(searchHandler1));
		assertThat(searchHandlers, hasItem(searchHandler2));
	}
	
	/**
	 * @verifies return null if no search handler is found for given resource name
	 * @see RestServiceImpl#getSearchHandlers(String)
	 */
	@Test
	public void getSearchHandlers_shouldReturnNullIfNoSearchHandlerIsFoundForGivenResourceName() throws Exception {
		
		SearchHandler searchHandler1 = mock(SearchHandler.class);
		SearchConfig searchConfig1 = new SearchConfig("default", "v1/concept", "1.8.*", new SearchQuery.Builder(
		        "description").withRequiredParameters("source").withOptionalParameters("code").build());
		when(searchHandler1.getSearchConfig()).thenReturn(searchConfig1);
		
		setCurrentOpenmrsVersion("1.8.10");
		
		when(restHelperService.getRegisteredSearchHandlers()).thenReturn(asList(searchHandler1));
		
		RestUtil.disableContext(); //to avoid a Context call
		
		assertThat(restService.getSearchHandlers("v1/order"), is(nullValue()));
	}
	
	/**
	 * @verifies return null if no search handler is found for current openmrs version
	 * @see RestServiceImpl#getSearchHandlers(String)
	 */
	@Test
	public void getSearchHandlers_shouldReturnNullIfNoSearchHandlerIsFoundForCurrentOpenmrsVersion() throws Exception {
		
		SearchHandler searchHandler1 = mock(SearchHandler.class);
		SearchConfig searchConfig1 = new SearchConfig("default", "v1/concept", "1.8.*", new SearchQuery.Builder(
		        "description").withRequiredParameters("source").withOptionalParameters("code").build());
		when(searchHandler1.getSearchConfig()).thenReturn(searchConfig1);
		
		setCurrentOpenmrsVersion("1.12.0");
		
		when(restHelperService.getRegisteredSearchHandlers()).thenReturn(asList(searchHandler1));
		
		RestUtil.disableContext(); //to avoid a Context call
		
		assertThat(restService.getSearchHandlers("v1/concept"), is(nullValue()));
	}
	
	/**
	 * @verifies return null given null
	 * @see RestServiceImpl#getSearchHandlers(String)
	 */
	@Test
	public void getSearchHandlers_shouldReturnNullGivenNull() throws Exception {
		
		assertThat(restService.getSearchHandlers(null), is(nullValue()));
	}
	
	/**
	 * @verifies fail if two search handlers for the same resource have the same id
	 * @see RestServiceImpl#getSearchHandlers(String)
	 */
	@Test
	public void getSearchHandlers_shouldFailIfTwoSearchHandlersForTheSameResourceHaveTheSameId() throws Exception {
		
		SearchHandler searchHandler1 = mock(SearchHandler.class);
		SearchConfig searchConfig1 = new SearchConfig("conceptByMapping", "v1/concept", "1.8.*", new SearchQuery.Builder(
		        "description").withRequiredParameters("source").withOptionalParameters("code").build());
		when(searchHandler1.getSearchConfig()).thenReturn(searchConfig1);
		
		SearchHandler searchHandler2 = mock(SearchHandler.class);
		SearchConfig searchConfig2 = new SearchConfig("conceptByMapping", "v1/concept", "1.8.*", new SearchQuery.Builder(
		        "description").withRequiredParameters("source").withOptionalParameters("code").build());
		when(searchHandler2.getSearchConfig()).thenReturn(searchConfig2);
		
		setCurrentOpenmrsVersion("1.8.10");
		
		when(restHelperService.getRegisteredSearchHandlers()).thenReturn(asList(searchHandler1, searchHandler2));
		
		RestUtil.disableContext(); //to avoid a Context call
		
		expectedException.expect(IllegalStateException.class);
		expectedException.expectMessage(startsWith("Two search handlers ("));
		expectedException
		        .expectMessage(endsWith("for the same resource (v1/concept) must not have the same ID (conceptByMapping)"));
		restService.getSearchHandlers("v1/concept");
	}
	
	/**
	 * @verifies initialize resources and search handlers
	 * @see RestServiceImpl#initialize()
	 */
	@Test
	public void initialize_shouldInitializeResourcesAndSearchHandlers() throws Exception {
		
		List<Class<? extends Resource>> resources = new ArrayList<Class<? extends Resource>>();
		resources.add(AnimalResource_1_9.class);
		when(openmrsClassScanner.getClasses(Resource.class, true)).thenReturn(resources);
		
		SearchHandler searchHandler1 = mock(SearchHandler.class);
		SearchConfig searchConfig1 = new SearchConfig("default", "v1/concept", "1.9.*", new SearchQuery.Builder(
		        "Search for concepts").withRequiredParameters("source").withOptionalParameters("code").build());
		when(searchHandler1.getSearchConfig()).thenReturn(searchConfig1);
		when(restHelperService.getRegisteredSearchHandlers()).thenReturn(asList(searchHandler1));
		
		setCurrentOpenmrsVersion("1.9.10");
		
		RestUtil.disableContext(); //to avoid a Context call
		
		restService.initialize();
		assertThat(restService.getSearchHandlers("v1/concept").size(), is(1));
		assertThat(restService.getSearchHandlers("v1/concept").iterator().next(), is(searchHandler1));
		assertThat(restService.getResourceByName("v1/animal"), instanceOf(AnimalResource_1_9.class));
	}
	
	/**
	 * @verifies clear cached resources and search handlers and reinitialize them
	 * @see RestServiceImpl#initialize()
	 */
	@Test
	public void initialize_shouldClearCachedResourcesAndSearchHandlersAndReinitializeThem() throws Exception {
		
		List<Class<? extends Resource>> resources = new ArrayList<Class<? extends Resource>>();
		resources.add(AnimalResource_1_9.class);
		when(openmrsClassScanner.getClasses(Resource.class, true)).thenReturn(resources);
		
		SearchHandler searchHandler1 = mock(SearchHandler.class);
		SearchConfig searchConfig1 = new SearchConfig("default", "v1/concept", "1.9.*", new SearchQuery.Builder(
		        "Search for concepts").withRequiredParameters("source").withOptionalParameters("code").build());
		when(searchHandler1.getSearchConfig()).thenReturn(searchConfig1);
		when(restHelperService.getRegisteredSearchHandlers()).thenReturn(asList(searchHandler1));
		
		setCurrentOpenmrsVersion("1.9.10");
		
		RestUtil.disableContext(); //to avoid a Context call
		
		restService.initialize();
		assertThat(restService.getAllSearchHandlers().size(), is(1));
		assertThat(restService.getAllSearchHandlers(), hasItem(searchHandler1));
		assertThat(restService.getResourceByName("v1/animal"), instanceOf(AnimalResource_1_9.class));
		
		// add new resources and search handlers to show cache was cleared and updated
		resources.add(CountryResource_1_9.class);
		
		SearchHandler searchHandler2 = mock(SearchHandler.class);
		SearchConfig searchConfig2 = new SearchConfig("default", "v1/order", "1.9.*", new SearchQuery.Builder(
		        "Search for orders by patient").withRequiredParameters("patient").build());
		when(searchHandler2.getSearchConfig()).thenReturn(searchConfig2);
		when(restHelperService.getRegisteredSearchHandlers()).thenReturn(asList(searchHandler1, searchHandler2));
		
		restService.initialize();
		assertThat(restService.getAllSearchHandlers().size(), is(2));
		assertThat(restService.getAllSearchHandlers(), hasItem(searchHandler1));
		assertThat(restService.getAllSearchHandlers(), hasItem(searchHandler2));
		assertThat(restService.getResourceByName("v1/animal"), instanceOf(AnimalResource_1_9.class));
		assertThat(restService.getResourceByName("v1/country"), instanceOf(CountryResource_1_9.class));
	}
	
	/**
	 * @verifies fail if failed to get resource classes
	 * @see RestServiceImpl#initialize()
	 */
	@Test
	public void initialize_shouldFailIfFailedToGetResourceClasses() throws Exception {
		
		IOException ioException = new IOException("some");
		
		when(openmrsClassScanner.getClasses(Resource.class, true)).thenThrow(ioException);
		
		expectedException.expect(APIException.class);
		expectedException.expectMessage("Cannot access REST resources");
		expectedException.expectCause(is(ioException));
		restService.initialize();
	}
	
	/**
	 * @verifies fail if failed to instantiate a resource
	 * @see RestServiceImpl#initialize()
	 */
	@Test
	public void initialize_shouldFailIfFailedToInstantiateAResource() throws Exception {
		
		List<Class<? extends Resource>> resources = new ArrayList<Class<? extends Resource>>();
		resources.add(InstantiateExceptionAnimalResource_1_9.class);
		when(openmrsClassScanner.getClasses(Resource.class, true)).thenReturn(resources);
		
		setCurrentOpenmrsVersion("1.9.10");
		
		expectedException.expect(APIException.class);
		expectedException.expectMessage("Failed to instantiate " + InstantiateExceptionAnimalResource_1_9.class.toString());
		restService.initialize();
	}
	
	/**
	 * @verifies fail if two resources with same name and order are found
	 * @see RestServiceImpl#initialize()
	 */
	@Test
	public void initialize_shouldFailIfTwoResourcesWithSameNameAndOrderAreFound() throws Exception {
		
		List<Class<? extends Resource>> resources = new ArrayList<Class<? extends Resource>>();
		resources.add(AnimalResource_1_9.class);
		resources.add(DuplicateNameAndOrderAnimalResource_1_9.class);
		
		when(openmrsClassScanner.getClasses(Resource.class, true)).thenReturn(resources);
		setCurrentOpenmrsVersion("1.9.10");
		
		expectedException.expect(IllegalStateException.class);
		expectedException.expectMessage("Two resources with the same name (v1/animal) must not have the same order");
		restService.initialize();
	}
	
	/**
	 * @verifies fail if two search handlers for the same resource have the same id
	 * @see RestServiceImpl#initialize()
	 */
	@Test
	public void initialize_shouldFailIfTwoSearchHandlersForTheSameResourceHaveTheSameId() throws Exception {
		
		SearchHandler searchHandler1 = mock(SearchHandler.class);
		SearchConfig searchConfig1 = new SearchConfig("conceptByMapping", "v1/concept", "1.8.*", new SearchQuery.Builder(
		        "description").withRequiredParameters("source").withOptionalParameters("code").build());
		when(searchHandler1.getSearchConfig()).thenReturn(searchConfig1);
		
		SearchHandler searchHandler2 = mock(SearchHandler.class);
		SearchConfig searchConfig2 = new SearchConfig("conceptByMapping", "v1/concept", "1.8.*", new SearchQuery.Builder(
		        "description").withRequiredParameters("source").withOptionalParameters("code").build());
		when(searchHandler2.getSearchConfig()).thenReturn(searchConfig2);
		
		setCurrentOpenmrsVersion("1.8.10");
		
		when(restHelperService.getRegisteredSearchHandlers()).thenReturn(asList(searchHandler1, searchHandler2));
		
		RestUtil.disableContext(); //to avoid a Context call
		
		expectedException.expect(IllegalStateException.class);
		expectedException.expectMessage(startsWith("Two search handlers ("));
		expectedException
		        .expectMessage(endsWith("for the same resource (v1/concept) must not have the same ID (conceptByMapping)"));
		restService.initialize();
	}
}
