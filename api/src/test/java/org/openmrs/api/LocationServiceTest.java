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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openmrs.GlobalProperty;
import org.openmrs.Location;
import org.openmrs.LocationTag;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.Verifies;
import org.openmrs.util.OpenmrsConstants;

/**
 * Tests all methods in the {@link LocationService}
 */
public class LocationServiceTest extends BaseContextSensitiveTest {
	
	protected static final String LOC_INITIAL_DATA_XML = "org/openmrs/api/include/LocationServiceTest-initialData.xml";
	
	/**
	 * Run this before each unit test in this class. This adds a bit more data to the base data that
	 * is done in the "@Before" method in {@link BaseContextSensitiveTest} (which is run right
	 * before this method).
	 * 
	 * @throws Exception
	 */
	@Before
	public void runBeforeEachTest() throws Exception {
		executeDataSet(LOC_INITIAL_DATA_XML);
	}
	
	/**
	 * Test to make sure that a simple save to a new location gets persisted to the database
	 * 
	 * @see {@link LocationService#saveLocation(Location)}
	 */
	@Test
	@Verifies(value = "should create location successfully", method = "saveLocation(Location)")
	public void saveLocation_shouldCreateLocationSuccessfully() throws Exception {
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
		
		LocationService ls = Context.getLocationService();
		ls.saveLocation(location);
		
		Location newSavedLocation = ls.getLocation(location.getLocationId());
		
		assertNotNull("The saved location should have an id now", location.getLocationId());
		assertNotNull("We should get back a location", newSavedLocation);
		assertTrue("The created location needs to equal the pojo location", location.equals(newSavedLocation));
	}
	
	/**
	 * Test a simple update to a location that is already in the database.
	 * 
	 * @see {@link LocationService#saveLocation(Location)}
	 */
	@Test
	@Verifies(value = "should update location successfully", method = "saveLocation(Location)")
	public void saveLocation_shouldUpdateLocationSuccessfully() throws Exception {
		LocationService ls = Context.getLocationService();
		
		// get the location from the database
		Location location = ls.getLocation(1);
		
		// save the current values for comparison later
		String origName = location.getName();
		String origDesc = location.getDescription();
		
		// add values that are different than the ones in the initialData.xml file
		String newName = "new name";
		String newDesc = "new desc";
		
		location.setName(newName);
		location.setDescription(newDesc);
		
		// save to the db
		ls.saveLocation(location);
		
		// fetch that encounter from the db
		Location newestLoc = ls.getLocation(location.getLocationId());
		
		assertFalse("The name should be different", origName.equals(newName));
		assertTrue("The name should be the same", newestLoc.getName().equals(newName));
		assertFalse("The name should be different", origDesc.equals(newDesc));
		assertTrue("The name should be the same", newestLoc.getDescription().equals(newDesc));
	}
	
	/**
	 * You should be able to add child locations to a location (multi-level), save the location, and
	 * have the child location automatically persisted.
	 * 
	 * @see {@link LocationService#saveLocation(Location)}
	 */
	@Test
	@Verifies(value = "should cascade save to child location from location", method = "saveLocation(Location)")
	public void saveLocation_shouldCascadeSaveToChildLocationFromLocation() throws Exception {
		LocationService ls = Context.getLocationService();
		
		// First, create a new Location
		Location location = new Location();
		location.setName("parent");
		ls.saveLocation(location);
		
		// Now add a child location to it
		Location childA = new Location();
		childA.setName("level A child");
		location.addChildLocation(childA);
		
		// Add a new child location to the first child location
		Location childB = new Location();
		childB.setName("level B child");
		childA.addChildLocation(childB);
		
		ls.saveLocation(location);
		
		Location newSavedLocation = ls.getLocation(location.getLocationId());
		
		// The id should have been populated during the save
		assertNotNull(childA.getLocationId());
		assertNotNull(childB.getLocationId());
		
		// Saved parent location should have child locations
		assertNotNull("newSavedLocation.childLocations must be not null", newSavedLocation.getChildLocations());
		
		// Saved parent location should have exactly 1 child location
		assertEquals(1, newSavedLocation.getChildLocations().size());
		
		Location newChildA = newSavedLocation.getChildLocations().iterator().next();
		
		// Child location must be the previously added object
		assertTrue("Child location must be the previously created childA", newChildA.equals(childA));
		
		// Saved level A child location should have child locations
		assertNotNull("newSavedLocation.childLocations must be not null", newChildA.getChildLocations());
		
		// Saved level A child location should have exactly 1 child location
		assertEquals(1, newSavedLocation.getChildLocations().size());
		
		// Level B child location must be the previously added object
		assertTrue("Level B child location must be the previously created childB", newChildA.getChildLocations().iterator()
		        .next().equals(childB));
	}
	
	/**
	 * You should be able to remove a child location from a location.
	 * 
	 * @see {@link LocationService#saveLocation(Location)}
	 */
	@Test
	@Verifies(value = "should remove child location from location", method = "saveLocation(Location)")
	public void saveLocation_shouldRemoveChildLocationFromLocation() throws Exception {
		LocationService ls = Context.getLocationService();
		
		// Retrieving a location with initially 2 child locations
		Location location = ls.getLocation(1);
		
		// Removing a child
		location.removeChildLocation(location.getChildLocations().iterator().next());
		ls.saveLocation(location);
		
		Location newSavedLocation = ls.getLocation(location.getLocationId());
		
		// Saved location should have 1 child locations now
		assertEquals(1, newSavedLocation.getChildLocations().size());
	}
	
	/**
	 * @see {@link LocationService#getDefaultLocation()}
	 */
	@Test
	@Verifies(value = "should return default location for the implementation", method = "getDefaultLocation()")
	public void getDefaultLocation_shouldReturnDefaultLocationForTheImplementation() throws Exception {
		//set the global property for default location to something other than Unknown Location
		GlobalProperty gp = new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_DEFAULT_LOCATION_NAME,
		        "Test Parent Location", "Testing default Location");
		Context.getAdministrationService().saveGlobalProperty(gp);
		Assert.assertEquals("Test Parent Location", Context.getLocationService().getDefaultLocation().getName());
	}
	
	/**
	 * @see {@link LocationService#getLocation(Integer)}
	 */
	@Test
	@Verifies(value = "should return null when no location match given location id", method = "getLocation(Integer)")
	public void getLocation_shouldReturnNullWhenNoLocationMatchGivenLocationId() throws Exception {
		Assert.assertNull(Context.getLocationService().getLocation(1337));
	}
	
	/**
	 * @see {@link LocationService#getLocation(String)}
	 */
	@Test
	@Verifies(value = "should return null when no location match given location name", method = "getLocation(String)")
	public void getLocation_shouldReturnNullWhenNoLocationMatchGivenLocationName() throws Exception {
		Assert.assertNull(Context.getLocationService().getLocation("Princeton Plainsboro"));
	}
	
	/**
	 * @see {@link LocationService#getAllLocations()}
	 */
	@Test
	@Verifies(value = "should return all locations including retired", method = "getAllLocations()")
	public void getAllLocations_shouldReturnAllLocationsIncludingRetired() throws Exception {
		List<Location> locations = Context.getLocationService().getAllLocations();
		
		Assert.assertEquals(6, locations.size());
	}
	
	/**
	 * @see {@link LocationService#getAllLocations(null)}
	 */
	@Test
	@Verifies(value = "should return all locations when includeRetired is true", method = "getAllLocations(null)")
	public void getAllLocations_shouldReturnAllLocationsWhenIncludeRetiredIsTrue() throws Exception {
		List<Location> locations = Context.getLocationService().getAllLocations(true);
		
		Assert.assertEquals(6, locations.size());
	}
	
	/**
	 * @see {@link LocationService#getAllLocations(null)}
	 */
	@Test
	@Verifies(value = "should return only unretired locations when includeRetires is false", method = "getAllLocations(null)")
	public void getAllLocations_shouldReturnOnlyUnretiredLocationsWhenIncludeRetiresIsFalse() throws Exception {
		List<Location> locations = Context.getLocationService().getAllLocations(false);
		
		Assert.assertEquals(5, locations.size());
	}
	
	/**
	 * @see {@link LocationService#getLocations(String)}
	 */
	@Test
	@Verifies(value = "should return empty list when no location match the name fragment", method = "getLocations(String)")
	public void getLocations_shouldReturnEmptyListWhenNoLocationMatchTheNameFragment() throws Exception {
		Assert.assertEquals(0, Context.getLocationService().getLocations("Mansion").size());
	}
	
	/**
	 * Get locations that have a specified tag among its child tags.
	 * 
	 * @see {@link LocationService#getLocationsByTag(LocationTag)}
	 */
	@Test
	@Verifies(value = "should get locations by tag", method = "getLocationsByTag(LocationTag)")
	public void getLocationsByTag_shouldGetLocationsByTag() throws Exception {
		LocationService ls = Context.getLocationService();
		
		assertEquals(1, ls.getLocationsByTag(ls.getLocationTag(1)).size());
		assertEquals(2, ls.getLocationsByTag(ls.getLocationTag(3)).size());
		assertEquals(4, ls.getLocationsByTag(ls.getLocationTag(4)).size());
	}
	
	/**
	 * @see {@link LocationService#getLocationsByTag(LocationTag)}
	 */
	@Test
	@Verifies(value = "should return empty list when no locations has the given tag", method = "getLocationsByTag(LocationTag)")
	public void getLocationsByTag_shouldReturnEmptyListWhenNoLocationsHasTheGivenTag() throws Exception {
		LocationService ls = Context.getLocationService();
		Assert.assertEquals(0, ls.getLocationsByTag(ls.getLocationTagByName("Retired")).size());
	}
	
	/**
	 * Get locations that have a specified set of tags among its child tags.
	 * 
	 * @see {@link LocationService#getLocationsHavingAllTags(List<QLocationTag;>)}
	 */
	@Test
	@Verifies(value = "should get locations having all tags", method = "getLocationsHavingAllTags(List<QLocationTag;>)")
	public void getLocationsHavingAllTags_shouldGetLocationsHavingAllTags() throws Exception {
		LocationService ls = Context.getLocationService();
		
		List<LocationTag> list1 = new ArrayList<LocationTag>();
		list1.add(ls.getLocationTag(1));
		list1.add(ls.getLocationTag(2));
		
		List<LocationTag> list2 = new ArrayList<LocationTag>();
		list2.add(ls.getLocationTag(3));
		list2.add(ls.getLocationTag(4));
		
		List<LocationTag> list3 = new ArrayList<LocationTag>();
		list3.add(ls.getLocationTag(1));
		list3.add(ls.getLocationTag(2));
		list3.add(ls.getLocationTag(3));
		list3.add(ls.getLocationTag(4));
		
		List<LocationTag> list4 = new ArrayList<LocationTag>();
		list4.add(ls.getLocationTag(4));
		
		assertEquals(1, ls.getLocationsHavingAllTags(list1).size());
		assertEquals(2, ls.getLocationsHavingAllTags(list2).size());
		assertEquals(0, ls.getLocationsHavingAllTags(list3).size());
		assertEquals(4, ls.getLocationsHavingAllTags(list4).size());
	}
	
	/**
	 * @see {@link LocationService#getLocationsHavingAllTags(List<QLocationTag;>)}
	 */
	@Test
	@Verifies(value = "should return empty list when no location has the given tags", method = "getLocationsHavingAllTags(List<QLocationTag;>)")
	public void getLocationsHavingAllTags_shouldReturnEmptyListWhenNoLocationHasTheGivenTags() throws Exception {
		LocationService ls = Context.getLocationService();
		Assert.assertEquals(0, ls.getLocationsHavingAllTags(Collections.singletonList(ls.getLocationTagByName("Retired")))
		        .size());
	}
	
	/**
	 * @see {@link LocationService#getLocationsHavingAllTags(List<QLocationTag;>)}
	 */
	@Test
	@Verifies(value = "return all unretired locations given an empty tag list", method = "getLocationsHavingAllTags(List<QLocationTag;>)")
	public void getLocationsHavingAllTags_shouldReturnAllUnretiredLocationsGivenAnEmptyTagList() throws Exception {
		LocationService ls = Context.getLocationService();
		Assert.assertEquals(5, ls.getLocationsHavingAllTags(new ArrayList<LocationTag>()).size());
	}
	
	/**
	 * Get locations that have any of specified set of tags among its child tags.
	 * 
	 * @see {@link LocationService#getLocationsHavingAnyTag(List<QLocationTag;>)}
	 */
	@Test
	@Verifies(value = "should get locations having any tag", method = "getLocationsHavingAnyTag(List<QLocationTag;>)")
	public void getLocationsHavingAnyTag_shouldGetLocationsHavingAnyTag() throws Exception {
		LocationService ls = Context.getLocationService();
		
		List<LocationTag> list1 = new ArrayList<LocationTag>();
		list1.add(ls.getLocationTag(1));
		list1.add(ls.getLocationTag(2));
		
		List<LocationTag> list2 = new ArrayList<LocationTag>();
		list2.add(ls.getLocationTag(3));
		list2.add(ls.getLocationTag(4));
		
		List<LocationTag> list3 = new ArrayList<LocationTag>();
		list3.add(ls.getLocationTag(1));
		list3.add(ls.getLocationTag(2));
		list3.add(ls.getLocationTag(3));
		
		assertEquals(1, ls.getLocationsHavingAnyTag(list1).size());
		assertEquals(4, ls.getLocationsHavingAnyTag(list2).size());
		assertEquals(3, ls.getLocationsHavingAnyTag(list3).size());
	}
	
	/**
	 * @see {@link LocationService#getLocationsHavingAnyTag(List<QLocationTag;>)}
	 */
	@Test
	@Verifies(value = "should return empty list when no location has the given tags", method = "getLocationsHavingAnyTag(List<QLocationTag;>)")
	public void getLocationsHavingAnyTag_shouldReturnEmptyListWhenNoLocationHasTheGivenTags() throws Exception {
		LocationService ls = Context.getLocationService();
		Assert.assertEquals(0, ls.getLocationsHavingAnyTag(Collections.singletonList(ls.getLocationTagByName("Retired")))
		        .size());
	}
	
	/**
	 * @see {@link LocationService#getLocationsHavingAnyTag(List<QLocationTag;>)}
	 */
	@Test
	@Verifies(value = "should return empty list when given an empty tag list", method = "getLocationsHavingAnyTag(List<QLocationTag;>)")
	public void getLocationsHavingAnyTag_shouldReturnEmptyListWhenGivenAnEmptyTagList() throws Exception {
		LocationService ls = Context.getLocationService();
		Assert.assertEquals(0, ls.getLocationsHavingAnyTag(new ArrayList<LocationTag>()).size());
	}
	
	/**
	 * @see {@link LocationService#retireLocation(Location,String)}
	 */
	@Test
	@Verifies(value = "should retire location successfully", method = "retireLocation(Location,String)")
	public void retireLocation_shouldRetireLocationSuccessfully() throws Exception {
		LocationService ls = Context.getLocationService();
		
		// Get all locations.
		List<Location> locationsBeforeRetired = ls.getAllLocations(true);
		List<Location> locationsNotRetiredBefore = ls.getAllLocations(false);
		
		// Get a non-retired location
		Location location = ls.getLocation(1);
		
		Location retiredLoc = ls.retireLocation(location, "Just Testing");
		
		// Get all locations again.
		List<Location> locationsAfterRetired = ls.getAllLocations(true);
		List<Location> locationsNotRetiredAfter = ls.getAllLocations(false);
		
		// make sure that all the values were filled in
		assertTrue(retiredLoc.isRetired());
		assertNotNull(retiredLoc.getDateRetired());
		assertEquals(Context.getAuthenticatedUser(), retiredLoc.getRetiredBy());
		assertEquals("Just Testing", retiredLoc.getRetireReason());
		
		// Both location lists that include retired should be equal in size and not order of elements.
		assertEquals(locationsBeforeRetired.size(), locationsAfterRetired.size());
		
		// Both location lists that do not include retired should not be the same.
		assertNotSame(locationsNotRetiredBefore, locationsNotRetiredAfter);
	}
	
	/**
	 * @see {@link LocationService#retireLocation(Location,String)}
	 */
	@Ignore
	// TODO Determine whether or not RetireHandler should throw IllegalArgumentException under these conditions 
	@Test(expected = IllegalArgumentException.class)
	@Verifies(value = "should throw IllegalArgumentException when no reason is given", method = "retireLocation(Location,String)")
	public void retireLocation_shouldThrowIllegalArgumentExceptionWhenNoReasonIsGiven() throws Exception {
		LocationService ls = Context.getLocationService();
		Location loc = ls.getLocation("Test Parent Location");
		ls.retireLocation(loc, "");
	}
	
	/**
	 * @see {@link LocationService#unretireLocation(Location)}
	 */
	@Test
	@Verifies(value = "should unretire retired location", method = "unretireLocation(Location)")
	public void unretireLocation_shouldUnretireRetiredLocation() throws Exception {
		LocationService ls = Context.getLocationService();
		
		Location loc = ls.getLocation("Test Retired Location");
		Assert.assertTrue(loc.isRetired());
		
		Location newLoc = ls.unretireLocation(loc);
		Assert.assertEquals(loc, newLoc);
		Assert.assertFalse(loc.isRetired());
		Assert.assertNull(loc.getRetiredBy());
		Assert.assertNull(loc.getRetireReason());
	}
	
	/**
	 * Make sure that purging a location removes the row from the database
	 * 
	 * @see {@link LocationService#purgeLocation(Location)}
	 */
	@Test
	@Verifies(value = "should delete location successfully", method = "purgeLocation(Location)")
	public void purgeLocation_shouldDeleteLocationSuccessfully() throws Exception {
		
		LocationService ls = Context.getLocationService();
		
		// fetch the encounter to delete from the db
		Location locationToDelete = ls.getLocation(1);
		
		ls.purgeLocation(locationToDelete);
		
		// try to refetch the location. should get a null object
		Location l = ls.getLocation(locationToDelete.getLocationId());
		
		assertNull("We shouldn't find the location after deletion", l);
	}
	
	/**
	 * Test to make sure that a simple save to a new location tag gets persisted to the database
	 * 
	 * @see {@link LocationService#saveLocationTag(LocationTag)}
	 */
	@Test
	@Verifies(value = "should create location tag successfully", method = "saveLocationTag(LocationTag)")
	public void saveLocationTag_shouldCreateLocationTagSuccessfully() throws Exception {
		LocationTag tag = new LocationTag();
		
		tag.setName("testing");
		tag.setDescription("desc");
		
		LocationService ls = Context.getLocationService();
		ls.saveLocationTag(tag);
		
		LocationTag newSavedTag = ls.getLocationTag(tag.getLocationTagId());
		
		assertNotNull("The saved tag should have an id now", tag.getLocationTagId());
		assertNotNull("We should get back a tag", newSavedTag);
		assertTrue("The created tag needs to equal the pojo location", tag.equals(newSavedTag));
	}
	
	/**
	 * Test a simple update to a location tag that is already in the database.
	 * 
	 * @see {@link LocationService#saveLocationTag(LocationTag)}
	 */
	@Test
	@Verifies(value = "should update location tag successfully", method = "saveLocationTag(LocationTag)")
	public void saveLocationTag_shouldUpdateLocationTagSuccessfully() throws Exception {
		LocationService ls = Context.getLocationService();
		
		// get the location tag from the database
		LocationTag tag = ls.getLocationTag(1);
		
		// save the current values for comparison later
		String origName = tag.getName();
		String origDesc = tag.getDescription();
		
		// add values that are different than the ones in the initialData.xml file
		String newName = "new name";
		String newDesc = "new desc";
		
		tag.setName(newName);
		tag.setDescription(newDesc);
		
		// save to the db
		ls.saveLocationTag(tag);
		
		// fetch that encounter from the db
		LocationTag newestTag = ls.getLocationTag(tag.getLocationTagId());
		
		assertFalse("The name should be different", origName.equals(newName));
		assertTrue("The name should NOT be different", newestTag.getName().equals(newName));
		assertFalse("The name should be different", origDesc.equals(newDesc));
		assertTrue("The name should NOT be different", newestTag.getDescription().equals(newDesc));
	}
	
	/**
	 * You should be able to add a tag to a location.
	 * 
	 * @see {@link LocationService#saveLocation(Location)}
	 */
	@Test
	@Verifies(value = "should add location tag to location", method = "saveLocation(Location)")
	public void saveLocation_shouldAddLocationTagToLocation() throws Exception {
		LocationService ls = Context.getLocationService();
		
		// First, create a new Location
		Location location = new Location();
		location.setName("name");
		ls.saveLocation(location);
		
		// Create a tag
		LocationTag tag = new LocationTag();
		tag.setName("tag name");
		ls.saveLocationTag(tag);
		
		// Add tag to location
		location.addTag(tag);
		ls.saveLocation(location);
		
		Location newSavedLocation = ls.getLocation(location.getLocationId());
		
		// Saved parent location should have tags
		assertNotNull("newSavedLocation.tags must be not null", newSavedLocation.getTags());
		
		// Saved parent location should have exactly 1 tag
		assertEquals(1, newSavedLocation.getTags().size());
		
		// Tag must be the previously added object
		assertTrue("Tag must be the previously added tag", newSavedLocation.getTags().iterator().next().equals(tag));
	}
	
	/**
	 * You should be able to add a transient tag with an existing tag name.
	 * 
	 * @see {@link LocationService#saveLocation(Location)}
	 */
	@Test
	@Verifies(value = "should overwrite transient tag if tag with same name exists", method = "saveLocation(Location)")
	public void saveLocation_shouldOverwriteTransientTagIfTagWithSameNameExists() throws Exception {
		LocationService ls = Context.getLocationService();
		
		// First, create a new Location
		Location location = new Location();
		location.setName("name");
		
		// Add a transient tag with an existing name
		location.addTag(new LocationTag("General Hospital", null));
		ls.saveLocation(location);
		
		Location newSavedLocation = ls.getLocation(location.getLocationId());
		
		// Saved parent location should have exactly 1 tag
		assertEquals(1, newSavedLocation.getTags().size());
		
		// Tag must be overwritten with tag with locationTagId == 1
		assertNotNull("Location tag should have an ID now", newSavedLocation.getTags().iterator().next().getLocationTagId());
		assertEquals(1, newSavedLocation.getTags().iterator().next().getLocationTagId().intValue());
	}
	
	/**
	 * You should be able to remove a tag from a location.
	 * 
	 * @see {@link LocationService#saveLocation(Location)}
	 */
	@Test
	@Verifies(value = "should remove location tag from location", method = "saveLocation(Location)")
	public void saveLocation_shouldRemoveLocationTagFromLocation() throws Exception {
		LocationService ls = Context.getLocationService();
		
		// Loading location with exactly 3 tags from the initialData.xml file
		Location location = ls.getLocation(1);
		
		// Removing a tag
		location.removeTag(location.getTags().iterator().next());
		ls.saveLocation(location);
		
		Location newSavedLocation = ls.getLocation(location.getLocationId());
		
		// Saved location should have 2 tag now
		assertEquals(2, newSavedLocation.getTags().size());
	}
	
	/**
	 * @see {@link LocationService#getLocationTag(Integer)}
	 */
	@Test
	@Verifies(value = "should return null when no location tag match given id", method = "getLocationTag(Integer)")
	public void getLocationTag_shouldReturnNullWhenNoLocationTagMatchGivenId() throws Exception {
		Assert.assertNull(Context.getLocationService().getLocationTag(9999));
	}
	
	/**
	 * Should be able to retrieve a single LocationTag by its name.
	 * 
	 * @see {@link LocationService#getLocationTagByName(String)}
	 */
	@Test
	@Verifies(value = "should get location tag by name", method = "getLocationTagByName(String)")
	public void getLocationTagByName_shouldGetLocationTagByName() throws Exception {
		LocationService ls = Context.getLocationService();
		
		// Existing tag
		assertEquals(ls.getLocationTag(1), ls.getLocationTagByName("General Hospital"));
		
		// Nonexistant tag
		assertEquals(null, ls.getLocationTagByName("random"));
	}
	
	/**
	 * @see {@link LocationService#getAllLocationTags()}
	 */
	@Test
	@Verifies(value = "should return all location tags including retired", method = "getAllLocationTags()")
	public void getAllLocationTags_shouldReturnAllLocationTagsIncludingRetired() throws Exception {
		List<LocationTag> tags = Context.getLocationService().getAllLocationTags();
		
		Assert.assertEquals(5, tags.size());
	}
	
	/**
	 * @see {@link LocationService#getAllLocationTags(null)}
	 */
	@Test
	@Verifies(value = "should return all location tags if includeRetired is true", method = "getAllLocationTags(null)")
	public void getAllLocationTags_shouldReturnAllLocationTagsIfIncludeRetiredIsTrue() throws Exception {
		List<LocationTag> tags = Context.getLocationService().getAllLocationTags(true);
		
		Assert.assertEquals(5, tags.size());
	}
	
	/**
	 * @see {@link LocationService#getAllLocationTags(null)}
	 */
	@Test
	@Verifies(value = "should return only unretired location tags if includeRetired is false", method = "getAllLocationTags(null)")
	public void getAllLocationTags_shouldReturnOnlyUnretiredLocationTagsIfIncludeRetiredIsFalse() throws Exception {
		List<LocationTag> tags = Context.getLocationService().getAllLocationTags(false);
		
		Assert.assertEquals(4, tags.size());
	}
	
	/**
	 * @see {@link LocationService#getLocationTagByName(String)}
	 */
	@Test
	@Verifies(value = "should return null when no location tag match given name", method = "getLocationTagByName(String)")
	public void getLocationTagByName_shouldReturnNullWhenNoLocationTagMatchGivenName() throws Exception {
		Assert.assertNull(Context.getLocationService().getLocationTagByName("Hospital of the year 2222"));
	}
	
	/**
	 * @see {@link LocationService#getLocationTags(String)}
	 */
	@Test
	@Verifies(value = "should return empty list when no location tag match given search string", method = "getLocationTags(String)")
	public void getLocationTags_shouldReturnEmptyListWhenNoLocationTagMatchGivenSearchString() throws Exception {
		Assert.assertEquals(0, Context.getLocationService().getLocationTags("!!!").size());
	}
	
	/**
	 * @see {@link LocationService#retireLocationTag(LocationTag,String)}
	 */
	@Test
	@Verifies(value = "should retire location tag successfully", method = "retireLocationTag(LocationTag,String)")
	public void retireLocationTag_shouldRetireLocationTagSuccessfully() throws Exception {
		LocationService ls = Context.getLocationService();
		
		// Get all tags.
		List<LocationTag> tagsBeforeRetired = ls.getAllLocationTags(true);
		List<LocationTag> tagsNotRetiredBefore = ls.getAllLocationTags(false);
		
		// Get a non-retired tag
		LocationTag tag = ls.getLocationTag(1);
		
		LocationTag retiredTag = ls.retireLocationTag(tag, "Just Testing");
		
		// make sure its still the same object
		assertEquals(retiredTag, tag);
		
		// Get all tags again.
		List<LocationTag> tagsAfterRetired = ls.getAllLocationTags(true);
		List<LocationTag> tagsNotRetiredAfter = ls.getAllLocationTags(false);
		
		// Make sure that all the values were filled in
		assertTrue(retiredTag.isRetired());
		assertNotNull(retiredTag.getDateRetired());
		assertEquals(Context.getAuthenticatedUser(), retiredTag.getRetiredBy());
		assertEquals("Just Testing", retiredTag.getRetireReason());
		
		// Both tag lists that include retired should be equal.
		assertEquals(tagsBeforeRetired, tagsAfterRetired);
		
		// Both tag lists that do not include retired should not be the same.
		assertNotSame(tagsNotRetiredBefore, tagsNotRetiredAfter);
	}
	
	/**
	 * @see {@link LocationService#retireLocationTag(LocationTag,String)}
	 */
	@Test
	@Verifies(value = "should retire location tag with given reason", method = "retireLocationTag(LocationTag,String)")
	public void retireLocationTag_shouldRetireLocationTagWithGivenReason() throws Exception {
		LocationService ls = Context.getLocationService();
		
		LocationTag tag = ls.getLocationTag(1);
		Assert.assertFalse(tag.isRetired());
		
		String reason = "because i can";
		LocationTag newTag = ls.retireLocationTag(tag, reason);
		
		Assert.assertEquals(tag, newTag);
		Assert.assertTrue(tag.isRetired());
		Assert.assertEquals(reason, tag.getRetireReason());
	}
	
	/**
	 * @see {@link LocationService#retireLocationTag(LocationTag,String)}
	 */
	@Test(expected = IllegalArgumentException.class)
	@Ignore
	// TODO Determine whether or not RetireHandler should throw IllegalArgumentException under these conditions 
	@Verifies(value = "should throw IllegalArgumentException when no reason is given", method = "retireLocationTag(LocationTag,String)")
	public void retireLocationTag_shouldThrowIllegalArgumentExceptionWhenNoReasonIsGiven() throws Exception {
		LocationService ls = Context.getLocationService();
		LocationTag tag = ls.getLocationTag(1);
		ls.retireLocationTag(tag, "");
	}
	
	/**
	 * @see {@link LocationService#unretireLocationTag(LocationTag)}
	 */
	@Test
	@Verifies(value = "should unretire retired location tag", method = "unretireLocationTag(LocationTag)")
	public void unretireLocationTag_shouldUnretireRetiredLocationTag() throws Exception {
		LocationService ls = Context.getLocationService();
		LocationTag tag = ls.getLocationTagByName("Test Retired Tag");
		
		Assert.assertTrue(tag.isRetired());
		
		LocationTag newTag = ls.unretireLocationTag(tag);
		
		Assert.assertEquals(tag, newTag);
		Assert.assertFalse(tag.isRetired());
		Assert.assertNull(tag.getRetiredBy());
		Assert.assertNull(tag.getRetireReason());
	}
	
	/**
	 * Make sure that purging a location tag removes the row from the database
	 * 
	 * @see {@link LocationService#purgeLocationTag(LocationTag)}
	 */
	@Test
	@Verifies(value = "should delete location tag", method = "purgeLocationTag(LocationTag)")
	public void purgeLocationTag_shouldDeleteLocationTag() throws Exception {
		LocationService ls = Context.getLocationService();
		
		// Fetch the encounter to delete from the db
		LocationTag tag = ls.getLocationTag(1);
		
		ls.purgeLocationTag(tag);
		
		// Try to refetch the location. should get a null object
		LocationTag t = ls.getLocationTag(tag.getLocationTagId());
		
		assertNull("We shouldn't find the tag after deletion", t);
	}
	
	/**
	 * @see {@link LocationService#saveLocation(Location)}
	 */
	@Test
	@Verifies(value = "should return saved object", method = "saveLocation(Location)")
	public void saveLocation_shouldReturnSavedObject() throws Exception {
		Location location = new Location();
		location.setName("Some name");
		location.setDescription("Some description");
		Location savedLocation = Context.getLocationService().saveLocation(location);
		Assert.assertEquals(location, savedLocation);
	}
	
	/**
	 * @see {@link LocationService#saveLocation(Location)}
	 */
	@Test(expected = APIException.class)
	@Verifies(value = "should throw APIException if location has no name", method = "saveLocation(Location)")
	public void saveLocation_shouldThrowAPIExceptionIfLocationHasNoName() throws Exception {
		Location location = new Location();
		Context.getLocationService().saveLocation(location);
	}
	
	/**
	 * @see {@link LocationService#saveLocation(Location)}
	 */
	@Test(expected = APIException.class)
	@Verifies(value = "should throw APIException if transient tag is not found", method = "saveLocation(Location)")
	public void saveLocation_shouldThrowAPIExceptionIfTransientTagIsNotFound() throws Exception {
		LocationTag tagWithoutName = new LocationTag("some random tag name", "a nonexistant tag");
		Location location = new Location();
		location.setName("Some name");
		location.setDescription("Some description");
		location.addTag(tagWithoutName);
		Context.getLocationService().saveLocation(location);
	}
	
	/**
	 * @see {@link LocationService#saveLocationTag(LocationTag)}
	 */
	@Test
	@Verifies(value = "should return saved object", method = "saveLocationTag(LocationTag)")
	public void saveLocationTag_shouldReturnSavedObject() throws Exception {
		LocationTag locationTag = new LocationTag("Some tag name", "Some description");
		LocationTag savedLocationTag = Context.getLocationService().saveLocationTag(locationTag);
		Assert.assertEquals(locationTag, savedLocationTag);
	}
	
	/**
	 * @see {@link LocationService#saveLocationTag(LocationTag)}
	 */
	@Test(expected = APIException.class)
	@Verifies(value = "should throw APIException if tag has no name", method = "saveLocationTag(LocationTag)")
	public void saveLocationTag_shouldThrowAPIExceptionIfTagHasNoName() throws Exception {
		LocationTag tagWithoutName = new LocationTag();
		Location location = new Location();
		location.setName("Some name");
		location.setDescription("Some description");
		location.addTag(tagWithoutName);
		Context.getLocationService().saveLocation(location);
	}
	
	/**
	 * @see {@link LocationService#getLocationByUuid(String)}
	 */
	@Test
	@Verifies(value = "should find object given valid uuid", method = "getLocationByUuid(String)")
	public void getLocationByUuid_shouldFindObjectGivenValidUuid() throws Exception {
		String uuid = "f08ba64b-ea57-4a41-b33c-9dfc59b0c60a";
		Location location = Context.getLocationService().getLocationByUuid(uuid);
		Assert.assertEquals(1, (int) location.getLocationId());
	}
	
	/**
	 * @see {@link LocationService#getLocationByUuid(String)}
	 */
	@Test
	@Verifies(value = "should return null if no object found with given uuid", method = "getLocationByUuid(String)")
	public void getLocationByUuid_shouldReturnNullIfNoObjectFoundWithGivenUuid() throws Exception {
		Assert.assertNull(Context.getLocationService().getLocationByUuid("some invalid uuid"));
	}
	
	/**
	 * @see {@link LocationService#getLocationTagByUuid(String)}
	 */
	@Test
	@Verifies(value = "should find object given valid uuid", method = "getLocationTagByUuid(String)")
	public void getLocationTagByUuid_shouldFindObjectGivenValidUuid() throws Exception {
		Assert.assertEquals(Integer.valueOf(3),
		    Context.getLocationService().getLocationTagByUuid("0d0eaea2-47ed-11df-bc8b-001e378eb67e").getLocationTagId());
	}
	
	/**
	 * @see {@link LocationService#getLocationTagByUuid(String)}
	 */
	@Test
	@Verifies(value = "should return null if no object found with given uuid", method = "getLocationTagByUuid(String)")
	public void getLocationTagByUuid_shouldReturnNullIfNoObjectFoundWithGivenUuid() throws Exception {
		Assert.assertNull(Context.getLocationService().getLocationTagByUuid("ffffffff-47ed-11df-bc8b-001e378eb67e"));
	}
	
	/**
	 * @see {@link LocationService#getDefaultLocation()}
	 */
	@Test
	@Verifies(value = "should return Unknown Location if the global property is something else that doesnot exist", method = "getDefaultLocation()")
	public void getDefaultLocation_shouldReturnUnknownLocationIfTheGlobalPropertyIsSomethingElseThatDoesnotExist()
	        throws Exception {
		//set the global property to something that has no match in the location table
		GlobalProperty gp = new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_DEFAULT_LOCATION_NAME,
		        "None existent Location", "Testing");
		Context.getAdministrationService().saveGlobalProperty(gp);
		Assert.assertEquals("Unknown Location", Context.getLocationService().getDefaultLocation().getName());
	}
	
	/**
	 * @see {@link LocationService#getAllLocations(null)}
	 */
	@Test
	@Verifies(value = "should push retired locations to the end of the list when includeRetired is true", method = "getAllLocations(null)")
	public void getAllLocations_shouldPushRetiredLocationsToTheEndOfTheListWhenIncludeRetiredIsTrue() throws Exception {
		LocationService ls = Context.getLocationService();
		//retire the first location
		ls.retireLocation(ls.getAllLocations().get(0), "Just Testing");
		// Get all locations.
		List<Location> locations = ls.getAllLocations();
		//The 2 retired locations should be always be at the end
		Assert.assertTrue("Retired locations should be at the end of the list", locations.get(locations.size() - 1)
		        .isRetired());
		Assert.assertTrue("Retired locations should be at the end of the list", locations.get(locations.size() - 2)
		        .isRetired());
	}
	
}
