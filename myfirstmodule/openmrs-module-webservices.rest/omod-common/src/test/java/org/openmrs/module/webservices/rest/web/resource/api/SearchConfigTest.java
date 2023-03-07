/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.resource.api;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Tests {@link SearchConfig}.
 */
public class SearchConfigTest {
	
	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	
	/**
	 * @verifies create an instance of search config
	 * @see SearchConfig#SearchConfig(String, String, java.util.Collection, java.util.Collection)
	 */
	@Test
	public void SearchConfig_shouldCreateAnInstanceOfSearchConfig() throws Exception {
		
		SearchQuery searchQuery1 = new SearchQuery.Builder("Enables to search by patient").withRequiredParameters("patient")
		        .build();
		SearchQuery searchQuery2 = new SearchQuery.Builder("Enables to search by encounter").withRequiredParameters(
		    "encounter").build();
		SearchConfig searchConfig = new SearchConfig("default", "v1/order", asList("1.8.*", "1.9.*"), asList(searchQuery1,
		    searchQuery2));
		
		assertThat(searchConfig.getId(), is("default"));
		assertThat(searchConfig.getSupportedResource(), is("v1/order"));
		assertThat(searchConfig.getSupportedOpenmrsVersions().size(), is(2));
		assertThat(searchConfig.getSupportedOpenmrsVersions(), hasItem("1.8.*"));
		assertThat(searchConfig.getSupportedOpenmrsVersions(), hasItem("1.9.*"));
		assertThat(searchConfig.getSearchQueries().size(), is(2));
		assertThat(searchConfig.getSearchQueries(), hasItem(searchQuery1));
		assertThat(searchConfig.getSearchQueries(), hasItem(searchQuery2));
	}
	
	/**
	 * @verifies fail if given id is null
	 * @see SearchConfig#SearchConfig(String, String, java.util.Collection, java.util.Collection)
	 */
	@Test
	public void SearchConfig_shouldFailIfGivenIdIsNull() throws Exception {
		
		expectedException.expect(IllegalArgumentException.class);
		expectedException.expectMessage("id must not be empty");
		String id = null;
		new SearchConfig(id, "v1/order", asList("1.8.*"), asList(new SearchQuery.Builder("Enables to search")
		        .withOptionalParameters("id").build()));
	}
	
	/**
	 * @verifies fail if given id is empty
	 * @see SearchConfig#SearchConfig(String, String, java.util.Collection, java.util.Collection)
	 */
	@Test
	public void SearchConfig_shouldFailIfGivenIdIsEmpty() throws Exception {
		
		expectedException.expect(IllegalArgumentException.class);
		expectedException.expectMessage("id must not be empty");
		new SearchConfig("", "v1/order", asList("1.8.*"), asList(new SearchQuery.Builder("Enables to search")
		        .withOptionalParameters("id").build()));
	}
	
	/**
	 * @verifies fail if given supported resource is null
	 * @see SearchConfig#SearchConfig(String, String, java.util.Collection, java.util.Collection)
	 */
	@Test
	public void SearchConfig_shouldFailIfGivenSupportedResourceIsNull() throws Exception {
		
		expectedException.expect(IllegalArgumentException.class);
		expectedException.expectMessage("supportedResource must not be empty");
		String supportedResource = null;
		new SearchConfig("default", supportedResource, asList("1.8.*"), asList(new SearchQuery.Builder("Enables to search")
		        .withOptionalParameters("id").build()));
	}
	
	/**
	 * @verifies fail if given supported resource is empty
	 * @see SearchConfig#SearchConfig(String, String, java.util.Collection, java.util.Collection)
	 */
	@Test
	public void SearchConfig_shouldFailIfGivenSupportedResourceIsEmpty() throws Exception {
		
		expectedException.expect(IllegalArgumentException.class);
		expectedException.expectMessage("supportedResource must not be empty");
		new SearchConfig("default", "", asList("1.8.*"), asList(new SearchQuery.Builder("Enables to search")
		        .withOptionalParameters("id").build()));
	}
	
	/**
	 * @verifies fail if given supported openmrs versions is null
	 * @see SearchConfig#SearchConfig(String, String, java.util.Collection, java.util.Collection)
	 */
	@Test
	public void SearchConfig_shouldFailIfGivenSupportedOpenmrsVersionsIsNull() throws Exception {
		
		expectedException.expect(IllegalArgumentException.class);
		expectedException.expectMessage("supportedOpenmrsVersions must not be empty");
		List<String> supportedOpenmrsVersions = null;
		new SearchConfig("default", "v1/order", supportedOpenmrsVersions,
		        asList(new SearchQuery.Builder("Enables to search").withOptionalParameters("id").build()));
	}
	
	/**
	 * @verifies fail if given supported openmrs versions is empty
	 * @see SearchConfig#SearchConfig(String, String, java.util.Collection, java.util.Collection)
	 */
	@Test
	public void SearchConfig_shouldFailIfGivenSupportedOpenmrsVersionsIsEmpty() throws Exception {
		
		expectedException.expect(IllegalArgumentException.class);
		expectedException.expectMessage("supportedOpenmrsVersions must not be empty");
		List<String> supportedOpenmrsVersions = Collections.emptyList();
		new SearchConfig("default", "v1/order", supportedOpenmrsVersions,
		        asList(new SearchQuery.Builder("Enables to search").withOptionalParameters("id").build()));
	}
	
	/**
	 * @verifies fail if given search queries is null
	 * @see SearchConfig#SearchConfig(String, String, java.util.Collection, java.util.Collection)
	 */
	@Test
	public void SearchConfig_shouldFailIfGivenSearchQueriesIsNull() throws Exception {
		
		expectedException.expect(IllegalArgumentException.class);
		expectedException.expectMessage("searchQueries must not be empty");
		List<SearchQuery> searchQueries = null;
		new SearchConfig("default", "v1/order", "1.8.*", searchQueries);
	}
	
	/**
	 * @verifies fail if given search queries is empty
	 * @see SearchConfig#SearchConfig(String, String, java.util.Collection, java.util.Collection)
	 */
	@Test
	public void SearchConfig_shouldFailIfGivenSearchQueriesIsEmpty() throws Exception {
		
		expectedException.expect(IllegalArgumentException.class);
		expectedException.expectMessage("searchQueries must not be empty");
		List<SearchQuery> searchQueries = Collections.emptyList();
		new SearchConfig("default", "v1/order", "1.8.*", searchQueries);
	}
	
	/**
	 * @verifies create an instance of search config
	 * @see SearchConfig#SearchConfig(String, String, String, SearchQuery)
	 */
	@Test
	public void SearchConfig_shouldCreateAnInstanceOfSearchConfig_ConstructorStringStringStringSearchQuery()
	        throws Exception {
		
		SearchQuery searchQuery = new SearchQuery.Builder("Enables to search by patient").withRequiredParameters("patient")
		        .build();
		SearchConfig searchConfig = new SearchConfig("default", "v1/order", "1.8.*", searchQuery);
		
		assertThat(searchConfig.getId(), is("default"));
		assertThat(searchConfig.getSupportedResource(), is("v1/order"));
		assertThat(searchConfig.getSupportedOpenmrsVersions().size(), is(1));
		assertThat(searchConfig.getSupportedOpenmrsVersions(), hasItem("1.8.*"));
		assertThat(searchConfig.getSearchQueries().size(), is(1));
		assertThat(searchConfig.getSearchQueries(), hasItem(searchQuery));
	}
	
	/**
	 * @verifies create an instance of search config
	 * @see SearchConfig#SearchConfig(String, String, java.util.Collection, SearchQuery)
	 */
	@Test
	public void SearchConfig_shouldCreateAnInstanceOfSearchConfig_ConstructorString() throws Exception {
		
		SearchQuery searchQuery = new SearchQuery.Builder("Enables to search by patient").withRequiredParameters("patient")
		        .build();
		SearchConfig searchConfig = new SearchConfig("default", "v1/order", asList("1.8.*", "1.9.*"), searchQuery);
		
		assertThat(searchConfig.getId(), is("default"));
		assertThat(searchConfig.getSupportedResource(), is("v1/order"));
		assertThat(searchConfig.getSupportedOpenmrsVersions().size(), is(2));
		assertThat(searchConfig.getSupportedOpenmrsVersions(), hasItem("1.8.*"));
		assertThat(searchConfig.getSupportedOpenmrsVersions(), hasItem("1.9.*"));
		assertThat(searchConfig.getSearchQueries().size(), is(1));
		assertThat(searchConfig.getSearchQueries(), hasItem(searchQuery));
	}
	
	/**
	 * @verifies create an instance of search config
	 * @see SearchConfig#SearchConfig(String, String, String, java.util.Collection)
	 */
	@Test
	public void SearchConfig_shouldCreateAnInstanceOfSearchConfig_ConstructorStringStringStringCollection() throws Exception {
		
		SearchQuery searchQuery1 = new SearchQuery.Builder("Enables to search by patient").withRequiredParameters("patient")
		        .build();
		SearchQuery searchQuery2 = new SearchQuery.Builder("Enables to search by encounter").withRequiredParameters(
		    "encounter").build();
		SearchConfig searchConfig = new SearchConfig("default", "v1/order", "1.8.*", Arrays.asList(searchQuery1,
		    searchQuery2));
		
		assertThat(searchConfig.getId(), is("default"));
		assertThat(searchConfig.getSupportedResource(), is("v1/order"));
		assertThat(searchConfig.getSupportedOpenmrsVersions().size(), is(1));
		assertThat(searchConfig.getSupportedOpenmrsVersions(), hasItem("1.8.*"));
		assertThat(searchConfig.getSearchQueries().size(), is(2));
		assertThat(searchConfig.getSearchQueries(), hasItem(searchQuery1));
		assertThat(searchConfig.getSearchQueries(), hasItem(searchQuery2));
	}
	
	/**
	 * @verifies return same hashcode for equal search configs
	 * @see SearchConfig#hashCode()
	 */
	@Test
	public void hashCode_shouldReturnSameHashcodeForEqualSearchConfigs() throws Exception {
		
		SearchQuery searchQuery1 = new SearchQuery.Builder("Enables to search by patient").withRequiredParameters("patient")
		        .build();
		SearchConfig searchConfig1 = new SearchConfig("default", "v1/order", "1.8.*", searchQuery1);
		
		SearchQuery searchQuery2 = new SearchQuery.Builder("Enables to search by patient").withRequiredParameters("patient")
		        .build();
		SearchConfig searchConfig2 = new SearchConfig("default", "v1/order", "1.8.*", searchQuery2);
		
		assertTrue(searchConfig1.equals(searchConfig2));
		
		assertThat(searchConfig1.hashCode(), is(searchConfig2.hashCode()));
	}
	
	/**
	 * @verifies return true if given this
	 * @see SearchConfig#equals(Object)
	 */
	@Test
	public void equals_shouldReturnTrueIfGivenThis() throws Exception {
		
		SearchQuery searchQuery1 = new SearchQuery.Builder("Enables to search by patient").withRequiredParameters("patient")
		        .build();
		SearchConfig searchConfig1 = new SearchConfig("default", "v1/order", "1.8.*", searchQuery1);
		
		assertTrue(searchConfig1.equals(searchConfig1));
	}
	
	/**
	 * @verifies return true if this id and supported openmrs version and supported resource are
	 *           equal to given search configs
	 * @see SearchConfig#equals(Object)
	 */
	@Test
	public void equals_shouldReturnTrueIfThisIdAndSupportedOpenmrsVersionAndSupportedResourceAreEqualToGivenSearchConfigs()
	        throws Exception {
		
		SearchQuery searchQuery1 = new SearchQuery.Builder("Enables to search by patient").withRequiredParameters("patient")
		        .build();
		SearchConfig searchConfig1 = new SearchConfig("default", "v1/order", "1.8.*", searchQuery1);
		
		SearchQuery searchQuery2 = new SearchQuery.Builder("Enables to search by patient").withRequiredParameters("patient")
		        .build();
		SearchConfig searchConfig2 = new SearchConfig("default", "v1/order", "1.8.*", searchQuery2);
		
		assertTrue(searchConfig1.equals(searchConfig2));
	}
	
	/**
	 * @verifies be symmetric
	 * @see SearchConfig#equals(Object)
	 */
	@Test
	public void equals_shouldBeSymmetric() throws Exception {
		
		SearchQuery searchQuery1 = new SearchQuery.Builder("Enables to search by patient").withRequiredParameters("patient")
		        .build();
		SearchConfig searchConfig1 = new SearchConfig("default", "v1/order", "1.8.*", searchQuery1);
		
		SearchQuery searchQuery2 = new SearchQuery.Builder("Enables to search by patient").withRequiredParameters("patient")
		        .build();
		SearchConfig searchConfig2 = new SearchConfig("default", "v1/order", "1.8.*", searchQuery2);
		
		assertTrue(searchConfig1.equals(searchConfig2));
		assertTrue(searchConfig2.equals(searchConfig1));
	}
	
	/**
	 * @verifies be transitive
	 * @see SearchConfig#equals(Object)
	 */
	@Test
	public void equals_shouldBeTransitive() throws Exception {
		
		SearchQuery searchQuery1 = new SearchQuery.Builder("Enables to search by patient").withRequiredParameters("patient")
		        .build();
		SearchConfig searchConfig1 = new SearchConfig("default", "v1/order", "1.8.*", searchQuery1);
		
		SearchQuery searchQuery2 = new SearchQuery.Builder("Enables to search by patient").withRequiredParameters("patient")
		        .build();
		SearchConfig searchConfig2 = new SearchConfig("default", "v1/order", "1.8.*", searchQuery2);
		
		SearchQuery searchQuery3 = new SearchQuery.Builder("Enables to search by patient").withRequiredParameters("patient")
		        .build();
		SearchConfig searchConfig3 = new SearchConfig("default", "v1/order", "1.8.*", searchQuery3);
		
		assertTrue(searchConfig1.equals(searchConfig2));
		assertTrue(searchConfig1.equals(searchConfig3));
		assertTrue(searchConfig2.equals(searchConfig3));
	}
	
	/**
	 * @verifies return false if given null
	 * @see SearchConfig#equals(Object)
	 */
	@Test
	public void equals_shouldReturnFalseIfGivenNull() throws Exception {
		
		SearchQuery searchQuery1 = new SearchQuery.Builder("Enables to search by patient").withRequiredParameters("patient")
		        .build();
		SearchConfig searchConfig1 = new SearchConfig("default", "v1/order", "1.8.*", searchQuery1);
		
		assertFalse(searchConfig1.equals(null));
	}
	
	/**
	 * @verifies return false if given an object which is not an instanceof this class
	 * @see SearchConfig#equals(Object)
	 */
	@Test
	public void equals_shouldReturnFalseIfGivenAnObjectWhichIsNotAnInstanceofThisClass() throws Exception {
		
		SearchQuery searchQuery1 = new SearchQuery.Builder("Enables to search by patient").withRequiredParameters("patient")
		        .build();
		SearchConfig searchConfig1 = new SearchConfig("default", "v1/order", "1.8.*", searchQuery1);
		
		assertFalse(searchConfig1.equals("String"));
	}
	
	/**
	 * @verifies return false if this id is not equal to the given search configs id
	 * @see SearchConfig#equals(Object)
	 */
	@Test
	public void equals_shouldReturnFalseIfThisIdIsNotEqualToTheGivenSearchConfigsId() throws Exception {
		
		SearchQuery searchQuery1 = new SearchQuery.Builder("Enables to search by patient").withRequiredParameters("patient")
		        .build();
		SearchConfig searchConfig1 = new SearchConfig("default", "v1/order", "1.8.*", searchQuery1);
		
		SearchQuery searchQuery2 = new SearchQuery.Builder("Enables to search by patient").withRequiredParameters("patient")
		        .build();
		SearchConfig searchConfig2 = new SearchConfig("other", "v1/order", "1.8.*", searchQuery2);
		
		assertFalse(searchConfig1.equals(searchConfig2));
	}
	
	/**
	 * @verifies return false if this supported openmrs version is not equal to given search configs
	 *           supported openmrs version
	 * @see SearchConfig#equals(Object)
	 */
	@Test
	public void equals_shouldReturnFalseIfThisSupportedOpenmrsVersionIsNotEqualToGivenSearchConfigsSupportedOpenmrsVersion()
	        throws Exception {
		
		SearchQuery searchQuery1 = new SearchQuery.Builder("Enables to search by patient").withRequiredParameters("patient")
		        .build();
		SearchConfig searchConfig1 = new SearchConfig("default", "v1/order", "1.8.*", searchQuery1);
		
		SearchQuery searchQuery2 = new SearchQuery.Builder("Enables to search by patient").withRequiredParameters("patient")
		        .build();
		SearchConfig searchConfig2 = new SearchConfig("default", "v1/order", "1.9.*", searchQuery2);
		
		assertFalse(searchConfig1.equals(searchConfig2));
	}
	
	/**
	 * @verifies return false if this supported resource is not equal to given search configs
	 *           supported resource
	 * @see SearchConfig#equals(Object)
	 */
	@Test
	public void equals_shouldReturnFalseIfThisSupportedResourceIsNotEqualToGivenSearchConfigsSupportedResource()
	        throws Exception {
		
		SearchQuery searchQuery1 = new SearchQuery.Builder("Enables to search by patient").withRequiredParameters("patient")
		        .build();
		SearchConfig searchConfig1 = new SearchConfig("default", "v1/order", "1.8.*", searchQuery1);
		
		SearchQuery searchQuery2 = new SearchQuery.Builder("Enables to search by patient").withRequiredParameters("patient")
		        .build();
		SearchConfig searchConfig2 = new SearchConfig("default", "v2/order", "1.8.*", searchQuery2);
		
		assertFalse(searchConfig1.equals(searchConfig2));
	}
}
