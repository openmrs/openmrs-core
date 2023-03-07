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

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Tests {@link SearchQuery}.
 */
public class SearchQueryTest {
	
	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	
	/**
	 * @verifies fail if required parameters are already set
	 * @see SearchQuery.Builder#withRequiredParameters(String...)
	 */
	@Test
	public void withRequiredParameters_shouldFailIfRequiredParametersAreAlreadySet() throws Exception {
		
		SearchQuery.Builder builder = new SearchQuery.Builder("Enables search").withRequiredParameters("patient");
		
		expectedException.expect(IllegalStateException.class);
		expectedException.expectMessage("withRequiredParameters() must not be called twice");
		builder.withRequiredParameters("encounter");
	}
	
	/**
	 * @verifies fail if optional parameters are already set
	 * @see SearchQuery.Builder#withOptionalParameters(String...)
	 */
	@Test
	public void withOptionalParameters_shouldFailIfOptionalParametersAreAlreadySet() throws Exception {
		
		SearchQuery.Builder builder = new SearchQuery.Builder("Enables search").withOptionalParameters("patient");
		
		expectedException.expect(IllegalStateException.class);
		expectedException.expectMessage("withOptionalParameters() must not be called twice");
		builder.withOptionalParameters("encounter");
	}
	
	/**
	 * @verifies return a search query instance with properties set through the builder
	 * @see SearchQuery.Builder#build()
	 */
	@Test
	public void build_shouldReturnASearchQueryInstanceWithPropertiesSetThroughTheBuilder() throws Exception {
		
		SearchQuery searchQuery = new SearchQuery.Builder("Enables search for patient")
		        .withRequiredParameters("patient", "visit").withOptionalParameters("encounter", "date").build();
		
		assertThat(searchQuery.getDescription(), is("Enables search for patient"));
		assertThat(searchQuery.getRequiredParameters().size(), is(2));
		assertThat(searchQuery.getRequiredParameters(), hasItem(new SearchParameter("patient")));
		assertThat(searchQuery.getRequiredParameters(), hasItem(new SearchParameter("visit")));
		assertThat(searchQuery.getOptionalParameters().size(), is(2));
		assertThat(searchQuery.getOptionalParameters(), hasItem(new SearchParameter("encounter")));
		assertThat(searchQuery.getOptionalParameters(), hasItem(new SearchParameter("date")));
	}
	
	/**
	 * @verifies assign an empty set to required parameters if not set by the builder
	 * @see SearchQuery.Builder#build()
	 */
	@Test
	public void build_shouldAssignAnEmptySetToRequiredParametersIfNotSetByTheBuilder() throws Exception {
		
		SearchQuery searchQuery = new SearchQuery.Builder("Enables search for patient").withOptionalParameters("encounter",
		    "date").build();
		
		assertThat(searchQuery.getDescription(), is("Enables search for patient"));
		assertThat(searchQuery.getRequiredParameters(), is(empty()));
		assertThat(searchQuery.getOptionalParameters().size(), is(2));
		assertThat(searchQuery.getOptionalParameters(), hasItem(new SearchParameter("encounter")));
		assertThat(searchQuery.getOptionalParameters(), hasItem(new SearchParameter("date")));
	}
	
	/**
	 * @verifies assign an empty set to optional parameters if not set by the builder
	 * @see SearchQuery.Builder#build()
	 */
	@Test
	public void build_shouldAssignAnEmptySetToOptionalParametersIfNotSetByTheBuilder() throws Exception {
		
		SearchQuery searchQuery = new SearchQuery.Builder("Enables search for patient").withRequiredParameters("patient",
		    "visit").build();
		
		assertThat(searchQuery.getDescription(), is("Enables search for patient"));
		assertThat(searchQuery.getRequiredParameters().size(), is(2));
		assertThat(searchQuery.getRequiredParameters(), hasItem(new SearchParameter("patient")));
		assertThat(searchQuery.getRequiredParameters(), hasItem(new SearchParameter("visit")));
		assertThat(searchQuery.getOptionalParameters(), is(empty()));
	}
	
	/**
	 * @verifies fail if the description is null
	 * @see SearchQuery.Builder#build()
	 */
	@Test
	public void build_shouldFailIfTheDescriptionIsNull() throws Exception {
		
		expectedException.expect(IllegalArgumentException.class);
		expectedException.expectMessage("Description must not be empty");
		new SearchQuery.Builder(null).withRequiredParameters("patient", "visit").withOptionalParameters("encounter", "date")
		        .build();
	}
	
	/**
	 * @verifies fail if the description is empty
	 * @see SearchQuery.Builder#build()
	 */
	@Test
	public void build_shouldFailIfTheDescriptionIsEmpty() throws Exception {
		
		expectedException.expect(IllegalArgumentException.class);
		expectedException.expectMessage("Description must not be empty");
		new SearchQuery.Builder("").withRequiredParameters("patient", "visit").withOptionalParameters("encounter", "date")
		        .build();
	}
	
	/**
	 * @verifies fail if both required and optional parameters are empty
	 * @see SearchQuery.Builder#build()
	 */
	@Test
	public void build_shouldFailIfBothRequiredAndOptionalParametersAreEmpty() throws Exception {
		
		expectedException.expect(IllegalArgumentException.class);
		expectedException.expectMessage("Either required or optional parameters must not be empty");
		new SearchQuery.Builder("Enable search").build();
	}
	
	/**
	 * @verifies return same hashcode for equal search configs
	 * @see SearchQuery#hashCode()
	 */
	@Test
	public void hashCode_shouldReturnSameHashcodeForEqualSearchConfigs() throws Exception {
		
		SearchQuery searchQuery1 = new SearchQuery.Builder("Enables to search by patient").withRequiredParameters("patient")
		        .build();
		SearchQuery searchQuery2 = new SearchQuery.Builder("Enables to search by patient").withRequiredParameters("patient")
		        .build();
		
		assertTrue(searchQuery1.equals(searchQuery2));
		
		assertThat(searchQuery1.hashCode(), is(searchQuery2.hashCode()));
	}
	
	/**
	 * @verifies return true if given this
	 * @see SearchQuery#equals(Object)
	 */
	@Test
	public void equals_shouldReturnTrueIfGivenThis() throws Exception {
		
		SearchQuery searchQuery1 = new SearchQuery.Builder("Enables to search by patient").withRequiredParameters("patient")
		        .build();
		
		assertTrue(searchQuery1.equals(searchQuery1));
	}
	
	/**
	 * @verifies return true if this optional parameters and required parameters are equal to given
	 *           search query
	 * @see SearchQuery#equals(Object)
	 */
	@Test
	public void equals_shouldReturnTrueIfThisOptionalParametersAndRequiredParametersAreEqualToGivenSearchQuery()
	        throws Exception {
		
		SearchQuery searchQuery1 = new SearchQuery.Builder("Enables to search by patient").withRequiredParameters("patient")
		        .withOptionalParameters("encounter").build();
		SearchQuery searchQuery2 = new SearchQuery.Builder("Enables to search by patient").withRequiredParameters("patient")
		        .withOptionalParameters("encounter").build();
		
		assertTrue(searchQuery1.equals(searchQuery2));
	}
	
	/**
	 * @verifies be symmetric
	 * @see SearchQuery#equals(Object)
	 */
	@Test
	public void equals_shouldBeSymmetric() throws Exception {
		
		SearchQuery searchQuery1 = new SearchQuery.Builder("Enables to search by patient").withRequiredParameters("patient")
		        .withOptionalParameters("encounter").build();
		SearchQuery searchQuery2 = new SearchQuery.Builder("Enables to search by patient").withRequiredParameters("patient")
		        .withOptionalParameters("encounter").build();
		
		assertTrue(searchQuery1.equals(searchQuery2));
		assertTrue(searchQuery2.equals(searchQuery1));
	}
	
	/**
	 * @verifies be transitive
	 * @see SearchQuery#equals(Object)
	 */
	@Test
	public void equals_shouldBeTransitive() throws Exception {
		
		SearchQuery searchQuery1 = new SearchQuery.Builder("Enables to search by patient").withRequiredParameters("patient")
		        .withOptionalParameters("encounter").build();
		SearchQuery searchQuery2 = new SearchQuery.Builder("Enables to search by patient").withRequiredParameters("patient")
		        .withOptionalParameters("encounter").build();
		SearchQuery searchQuery3 = new SearchQuery.Builder("Enables to search by patient").withRequiredParameters("patient")
		        .withOptionalParameters("encounter").build();
		
		assertTrue(searchQuery1.equals(searchQuery2));
		assertTrue(searchQuery2.equals(searchQuery3));
		assertTrue(searchQuery1.equals(searchQuery3));
	}
	
	/**
	 * @verifies return false if given null
	 * @see SearchQuery#equals(Object)
	 */
	@Test
	public void equals_shouldReturnFalseIfGivenNull() throws Exception {
		
		SearchQuery searchQuery1 = new SearchQuery.Builder("Enables to search by patient").withRequiredParameters("patient")
		        .withOptionalParameters("encounter").build();
		
		assertFalse(searchQuery1.equals(null));
	}
	
	/**
	 * @verifies return false if given an object which is not an instanceof this class
	 * @see SearchQuery#equals(Object)
	 */
	@Test
	public void equals_shouldReturnFalseIfGivenAnObjectWhichIsNotAnInstanceofThisClass() throws Exception {
		
		SearchQuery searchQuery1 = new SearchQuery.Builder("Enables to search by patient").withRequiredParameters("patient")
		        .withOptionalParameters("encounter").build();
		
		assertFalse(searchQuery1.equals("some string"));
	}
	
	/**
	 * @verifies return false if this optional parameters is not equal to the given search queries
	 *           optional parameters
	 * @see SearchQuery#equals(Object)
	 */
	@Test
	public void equals_shouldReturnFalseIfThisOptionalParametersIsNotEqualToTheGivenSearchQueriesOptionalParameters()
	        throws Exception {
		
		SearchQuery searchQuery1 = new SearchQuery.Builder("Enables to search by patient").withRequiredParameters("patient")
		        .withOptionalParameters("encounter").build();
		SearchQuery searchQuery2 = new SearchQuery.Builder("Enables to search by patient").withRequiredParameters("patient")
		        .withOptionalParameters("visit").build();
		
		assertFalse(searchQuery1.equals(searchQuery2));
	}
	
	/**
	 * @verifies return false if this required parameters is not equal to the given search queries
	 *           required parameters
	 * @see SearchQuery#equals(Object)
	 */
	@Test
	public void equals_shouldReturnFalseIfThisRequiredParametersIsNotEqualToTheGivenSearchQueriesRequiredParameters()
	        throws Exception {
		
		SearchQuery searchQuery1 = new SearchQuery.Builder("Enables to search by patient").withRequiredParameters("patient")
		        .withOptionalParameters("encounter").build();
		SearchQuery searchQuery2 = new SearchQuery.Builder("Enables to search by patient").withRequiredParameters("visit")
		        .withOptionalParameters("encounter").build();
		
		assertFalse(searchQuery1.equals(searchQuery2));
	}
}
