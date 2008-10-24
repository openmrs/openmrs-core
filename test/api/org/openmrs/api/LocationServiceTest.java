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
package org.openmrs.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.Location;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;

/**
 * TODO test all methods in LocationService
 */
public class LocationServiceTest extends BaseContextSensitiveTest {

	protected static final String ENC_INITIAL_DATA_XML = "org/openmrs/api/include/EncounterServiceTest-initialData.xml";

	/**
	 * Run this before each unit test in this class.  This adds a bit 
	 * more data to the base data that is done in the "@Before" 
	 * method in {@link BaseContextSensitiveTest} (which is run
	 * right before this method).
	 * 
	 * @throws Exception
	 */
	@Before
	public void runBeforeEachTest() throws Exception {
		executeDataSet(ENC_INITIAL_DATA_XML);
	}

	/**
	 * Test create/update/delete of location
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldLocation() throws Exception {
		LocationService locationService = Context.getLocationService();

		// testing creation

		Location location = new Location();

		location.setName("testing");
		location.setDescription("desc");
		location.setAddress1("123");
		location.setAddress1("456");
		location.setCityVillage("city");
		location.setStateProvince("state");
		location.setCountry("country");
		location.setPostalCode("post");
		location.setLatitude("lat");
		location.setLongitude("lon");

		locationService.saveLocation(location);

		Location newLocation = locationService.getLocation(location.getLocationId());
		assertNotNull(newLocation);

		List<Location> locations = locationService.getAllLocations();

		// make sure we get a list
		assertNotNull(locations);

		boolean found = false;
		for (Iterator<Location> i = locations.iterator(); i.hasNext();) {
			Location location2 = i.next();
			assertNotNull(location);
			// check .equals function
			assertTrue(location.equals(location2) == (location.getLocationId().equals(location2.getLocationId())));
			// mark found flag
			if (location.equals(location2))
				found = true;
		}

		// assert that the new location was returned in the list
		assertTrue(found);

		// check update
		newLocation.setName("another test");
		locationService.saveLocation(newLocation);

		Location newerLocation = locationService.getLocation(newLocation.getLocationId());
		assertTrue(newerLocation.getName().equals(newLocation.getName()));

		// check deletion
		locationService.purgeLocation(newLocation);
		assertNull(locationService.getLocation(newLocation.getLocationId()));

	}

	/**
	 * Tests retiring a location. First, create a location. Then get all
	 * locations (including/not including) retired. Then retire the location.
	 * Then get all locations (including/not including) retired. The two lists
	 * of all locations including retired should be the same. The two lists not
	 * including retired locations should be different.
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldRetireLocation() throws Exception {
		LocationService locationService = Context.getLocationService();
		
		// Create a location.
		Location location = new Location();
		location.setName("Junitlandia");
		location.setDescription("JUnit Test Location");
		location.setAddress1("JUnit Boulevard");
		location.setStateProvince("The Great JUnit State");
		location.setCountry("Niceland");
		locationService.saveLocation(location);

		// Get all locations.
		List<Location> locationsBeforeRetired = locationService.getAllLocations(true);
		List<Location> locationsNotRetiredBefore = locationService.getAllLocations(false);

		// Retire the location.
		Location oldLocation = locationService.getLocation("Junitlandia");
		locationService.retireLocation(oldLocation,
		                               "Spend more time with its family.");

		// Get all locations again.
		List<Location> locationsAfterRetired = locationService.getAllLocations(true);
		List<Location> locationsNotRetiredAfter = locationService.getAllLocations(false);

		// Both location lists that include retired should be equal.
		assertEquals(locationsBeforeRetired, locationsAfterRetired);

		// Both location lists that do not include retired should not be the same.
		assertNotSame(locationsNotRetiredBefore, locationsNotRetiredAfter);

	}

}
