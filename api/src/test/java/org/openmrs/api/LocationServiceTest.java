/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.GlobalProperty;
import org.openmrs.Location;
import org.openmrs.LocationAttribute;
import org.openmrs.LocationAttributeType;
import org.openmrs.LocationTag;
import org.openmrs.api.context.Context;
import org.openmrs.customdatatype.datatype.FreeTextDatatype;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.Verifies;
import org.openmrs.util.OpenmrsConstants;

/**
 * Tests all methods in the {@link LocationService}
 */
public class LocationServiceTest extends BaseContextSensitiveTest {
	
	protected static final String LOC_INITIAL_DATA_XML = "org/openmrs/api/include/LocationServiceTest-initialData.xml";
	
	protected static final String LOC_ATTRIBUTE_DATA_XML = "org/openmrs/api/include/LocationServiceTest-attributes.xml";
	
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
		location.setDescription("is the parent");
		ls.saveLocation(location);
		
		// Now add a child location to it
		Location childA = new Location();
		childA.setName("level A child");
		childA.setDescription("is a child");
		location.addChildLocation(childA);
		
		// Add a new child location to the first child location
		Location childB = new Location();
		childB.setName("level B child");
		childB.setDescription("is a child");
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
	 * @see {@link LocationService#getRootLocations(boolean)}
	 */
	@Test
	@Verifies(value = "should return all root locations including retired", method = "getRootLocations(boolean)")
	public void getRootLocations_shouldReturnRootLocationsIncludingRetired() throws Exception {
		List<Location> locations = Context.getLocationService().getRootLocations(true);
		
		Assert.assertEquals(3, locations.size());
	}
	
	/**
	 * @see {@link LocationService#getRootLocations(boolean)}
	 */
	@Test
	@Verifies(value = "should return only unretired root locations when includeRetires is false", method = "getRootLocations(boolean)")
	public void getRootLocations_shouldReturnOnlyUnretiredRootLocations() throws Exception {
		List<Location> locations = Context.getLocationService().getRootLocations(false);
		
		Assert.assertEquals(2, locations.size());
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
	 * @see LocationService#getLocations(String, org.openmrs.Location, java.util.Map, boolean, Integer, Integer)
	 */
	@Test
	@Verifies(value = "should return empty list when no location has matching attribute values", method = "getLocations(String,Location,Map,boolean,Integer,Integer)")
	public void getLocations_shouldNotFindAnyLocationsIfNoneHaveGivenAttributeValues() {
		// Save new phone number attribute type
		LocationAttributeType phoneAttrType = new LocationAttributeType();
		phoneAttrType.setName("Facility Phone");
		phoneAttrType.setMinOccurs(0);
		phoneAttrType.setMaxOccurs(1);
		phoneAttrType.setDatatypeClassname("org.openmrs.customdatatype.datatype.FreeTextDatatype");
		Context.getLocationService().saveLocationAttributeType(phoneAttrType);
		
		Map<LocationAttributeType, Object> attrValues = new HashMap<LocationAttributeType, Object>();
		attrValues.put(phoneAttrType, "xxxxxx");
		Assert.assertEquals(0, Context.getLocationService().getLocations(null, null, attrValues, true, null, null).size());
	}
	
	/**
	 * @see LocationService#getLocations(String, org.openmrs.Location, java.util.Map, boolean, Integer, Integer)
	 */
	@Test
	@Verifies(value = "should get locations having all matching attribute values", method = "getLocations(String,Location,Map,boolean,Integer,Integer)")
	public void getLocations_shouldGetLocationsHavingAllMatchingAttributeValues() {
		// Save new phone number attribute type
		LocationAttributeType phoneAttrType = new LocationAttributeType();
		phoneAttrType.setName("Facility Phone");
		phoneAttrType.setMinOccurs(0);
		phoneAttrType.setMaxOccurs(1);
		phoneAttrType.setDatatypeClassname("org.openmrs.customdatatype.datatype.FreeTextDatatype");
		Context.getLocationService().saveLocationAttributeType(phoneAttrType);
		
		// Save new email address attribute type
		LocationAttributeType emailAttrType = new LocationAttributeType();
		emailAttrType.setName("Facility Email");
		emailAttrType.setMinOccurs(0);
		emailAttrType.setMaxOccurs(1);
		emailAttrType.setDatatypeClassname("org.openmrs.customdatatype.datatype.FreeTextDatatype");
		Context.getLocationService().saveLocationAttributeType(emailAttrType);
		
		// Assign phone number 0123456789 and email address admin@facility.com to location #1
		Location location1 = Context.getLocationService().getLocation(1);
		LocationAttribute la1a = new LocationAttribute();
		la1a.setAttributeType(phoneAttrType);
		la1a.setValue("0123456789");
		location1.addAttribute(la1a);
		LocationAttribute la1b = new LocationAttribute();
		la1b.setAttributeType(emailAttrType);
		la1b.setValue("admin@facility.com");
		location1.addAttribute(la1b);
		Context.getLocationService().saveLocation(location1);
		
		// Assign same phone number 0123456789 to location #2
		Location location2 = Context.getLocationService().getLocation(2);
		LocationAttribute la2 = new LocationAttribute();
		la2.setAttributeType(phoneAttrType);
		la2.setValue("0123456789");
		location2.addAttribute(la2);
		Context.getLocationService().saveLocation(location2);
		
		// Search for location #1 by phone number AND email address
		Map<LocationAttributeType, Object> attrValues = new HashMap<LocationAttributeType, Object>();
		attrValues.put(phoneAttrType, "0123456789");
		attrValues.put(emailAttrType, "admin@facility.com");
		
		// Check that only location #1 is returned
		List<Location> locations = Context.getLocationService().getLocations(null, null, attrValues, false, null, null);
		Assert.assertEquals(1, locations.size());
		Assert.assertEquals(location1, locations.get(0));
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
	
	@Test
	@Verifies(value = "get locations having all tags", method = "getLocationsHavingAllTags(List<LocationTag>)")
	public void getLocationsHavingAllTags_shouldGetLocationsHavingAllTags() throws Exception {
		LocationService ls = Context.getLocationService();
		
		List<LocationTag> list1 = new ArrayList<LocationTag>();
		list1.add(ls.getLocationTag(1));
		list1.add(ls.getLocationTag(2));
		
		assertEquals(1, ls.getLocationsHavingAllTags(list1).size());
	}
	
	/**
	 * @see {@link LocationService#getLocationsHavingAllTags(List<LocationTag;>)}
	 */
	@Test
	@Verifies(value = "return all unretired locations given an empty tag list", method = "getLocationsHavingAllTags(List<LocationTag>)")
	public void getLocationsHavingAllTags_shouldReturnAllUnretiredLocationsGivenAnEmptyTagList() throws Exception {
		LocationService ls = Context.getLocationService();
		Assert.assertEquals(5, ls.getLocationsHavingAllTags(Collections.EMPTY_LIST).size());
	}
	
	/**
	 * Get locations that have any of specified set of tags among its child tags.
	 * 
	 * @see {@link LocationService#getLocationsHavingAnyTag(List<LocationTag;>)}
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
		Location locationToDelete = ls.getLocation(4);
		
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
	 * @see {@link LocationService#saveLocationTag(LocationTag)}
	 */
	@Test(expected = APIException.class)
	@Verifies(value = "should throw exception if tag name is null", method = "saveLocationTag(LocationTag)")
	public void saveLocationTag_shouldThrowExceptionIfTagNameIsNull() throws Exception {
		LocationTag tag = new LocationTag();
		
		tag.setName(null);
		tag.setDescription("desc");
		
		Context.getLocationService().saveLocationTag(tag);
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
		location.setDescription("is a location");
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
		location.setDescription("is a location");
		
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
		
		Assert.assertEquals(6, tags.size());
	}
	
	/**
	 * @see {@link LocationService#getAllLocationTags(null)}
	 */
	@Test
	@Verifies(value = "should return all location tags if includeRetired is true", method = "getAllLocationTags(null)")
	public void getAllLocationTags_shouldReturnAllLocationTagsIfIncludeRetiredIsTrue() throws Exception {
		List<LocationTag> tags = Context.getLocationService().getAllLocationTags(true);
		
		Assert.assertEquals(6, tags.size());
	}
	
	/**
	 * @see {@link LocationService#getAllLocationTags(null)}
	 */
	@Test
	@Verifies(value = "should return only unretired location tags if includeRetired is false", method = "getAllLocationTags(null)")
	public void getAllLocationTags_shouldReturnOnlyUnretiredLocationTagsIfIncludeRetiredIsFalse() throws Exception {
		List<LocationTag> tags = Context.getLocationService().getAllLocationTags(false);
		
		Assert.assertEquals(5, tags.size());
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
		LocationTag tag = ls.getLocationTag(5);
		
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
		Assert.assertEquals(Integer.valueOf(3), Context.getLocationService().getLocationTagByUuid(
		    "0d0eaea2-47ed-11df-bc8b-001e378eb67e").getLocationTagId());
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
	
	/**
	 * @see LocationService#getAllLocationAttributeTypes()
	 * @verifies return all location attribute types including retired ones
	 */
	@Test
	public void getAllLocationAttributeTypes_shouldReturnAllLocationAttributeTypesIncludingRetiredOnes() throws Exception {
		executeDataSet(LOC_ATTRIBUTE_DATA_XML);
		Assert.assertEquals(2, Context.getLocationService().getAllLocationAttributeTypes().size());
	}
	
	/**
	 * @see LocationService#getLocationAttributeType(Integer)
	 * @verifies return the location attribute type with the given id
	 */
	@Test
	public void getLocationAttributeType_shouldReturnTheLocationAttributeTypeWithTheGivenId() throws Exception {
		executeDataSet(LOC_ATTRIBUTE_DATA_XML);
		Assert.assertEquals("Audit Date", Context.getLocationService().getLocationAttributeType(1).getName());
	}
	
	/**
	 * @see LocationService#getLocationAttributeType(Integer)
	 * @verifies return null if no location attribute type exists with the given id
	 */
	@Test
	public void getLocationAttributeType_shouldReturnNullIfNoLocationAttributeTypeExistsWithTheGivenId() throws Exception {
		executeDataSet(LOC_ATTRIBUTE_DATA_XML);
		Assert.assertNull(Context.getLocationService().getLocationAttributeType(999));
	}
	
	/**
	 * @see LocationService#getLocationAttributeTypeByUuid(String)
	 * @verifies return the location attribute type with the given uuid
	 */
	@Test
	public void getLocationAttributeTypeByUuid_shouldReturnTheLocationAttributeTypeWithTheGivenUuid() throws Exception {
		executeDataSet(LOC_ATTRIBUTE_DATA_XML);
		Assert.assertEquals("Audit Date", Context.getLocationService().getLocationAttributeTypeByUuid(
		    "9516cc50-6f9f-11e0-8414-001e378eb67e").getName());
	}
	
	/**
	 * @see LocationService#getLocationAttributeTypeByUuid(String)
	 * @verifies return null if no location attribute type exists with the given uuid
	 */
	@Test
	public void getLocationAttributeTypeByUuid_shouldReturnNullIfNoLocationAttributeTypeExistsWithTheGivenUuid()
	        throws Exception {
		executeDataSet(LOC_ATTRIBUTE_DATA_XML);
		Assert.assertNull(Context.getLocationService().getLocationAttributeTypeByUuid("not-a-uuid"));
	}
	
	/**
	 * @see LocationService#purgeLocationAttributeType(LocationAttributeType)
	 * @verifies completely remove a location attribute type
	 */
	@Test
	public void purgeLocationAttributeType_shouldCompletelyRemoveALocationAttributeType() throws Exception {
		executeDataSet(LOC_ATTRIBUTE_DATA_XML);
		Assert.assertEquals(2, Context.getLocationService().getAllLocationAttributeTypes().size());
		Context.getLocationService().purgeLocationAttributeType(Context.getLocationService().getLocationAttributeType(2));
		Assert.assertEquals(1, Context.getLocationService().getAllLocationAttributeTypes().size());
	}
	
	/**
	 * @see LocationService#retireLocationAttributeType(LocationAttributeType,String)
	 * @verifies retire a location attribute type
	 */
	@Test
	public void retireLocationAttributeType_shouldRetireALocationAttributeType() throws Exception {
		executeDataSet(LOC_ATTRIBUTE_DATA_XML);
		LocationAttributeType vat = Context.getLocationService().getLocationAttributeType(1);
		Assert.assertFalse(vat.isRetired());
		Assert.assertNull(vat.getRetiredBy());
		Assert.assertNull(vat.getDateRetired());
		Assert.assertNull(vat.getRetireReason());
		Context.getLocationService().retireLocationAttributeType(vat, "for testing");
		vat = Context.getLocationService().getLocationAttributeType(1);
		Assert.assertTrue(vat.isRetired());
		Assert.assertNotNull(vat.getRetiredBy());
		Assert.assertNotNull(vat.getDateRetired());
		Assert.assertEquals("for testing", vat.getRetireReason());
	}
	
	/**
	 * @see LocationService#saveLocationAttributeType(LocationAttributeType)
	 * @verifies create a new location attribute type
	 */
	@Test
	public void saveLocationAttributeType_shouldCreateANewLocationAttributeType() throws Exception {
		executeDataSet(LOC_ATTRIBUTE_DATA_XML);
		Assert.assertEquals(2, Context.getLocationService().getAllLocationAttributeTypes().size());
		LocationAttributeType lat = new LocationAttributeType();
		lat.setName("Another one");
		lat.setDatatypeClassname(FreeTextDatatype.class.getName());
		Context.getLocationService().saveLocationAttributeType(lat);
		Assert.assertNotNull(lat.getId());
		Assert.assertEquals(3, Context.getLocationService().getAllLocationAttributeTypes().size());
	}
	
	/**
	 * @see LocationService#saveLocationAttributeType(LocationAttributeType)
	 * @verifies edit an existing location attribute type
	 */
	@Test
	public void saveLocationAttributeType_shouldEditAnExistingLocationAttributeType() throws Exception {
		executeDataSet(LOC_ATTRIBUTE_DATA_XML);
		LocationService service = Context.getLocationService();
		Assert.assertEquals(2, service.getAllLocationAttributeTypes().size());
		LocationAttributeType lat = service.getLocationAttributeType(1);
		lat.setName("A new name");
		service.saveLocationAttributeType(lat);
		Assert.assertEquals(2, service.getAllLocationAttributeTypes().size());
		Assert.assertEquals("A new name", service.getLocationAttributeType(1).getName());
	}
	
	/**
	 * @see LocationService#unretireLocationAttributeType(LocationAttributeType)
	 * @verifies unretire a retired location attribute type
	 */
	@Test
	public void unretireLocationAttributeType_shouldUnretireARetiredLocationAttributeType() throws Exception {
		executeDataSet(LOC_ATTRIBUTE_DATA_XML);
		LocationService service = Context.getLocationService();
		LocationAttributeType lat = service.getLocationAttributeType(2);
		Assert.assertTrue(lat.isRetired());
		Assert.assertNotNull(lat.getDateRetired());
		Assert.assertNotNull(lat.getRetiredBy());
		Assert.assertNotNull(lat.getRetireReason());
		service.unretireLocationAttributeType(lat);
		Assert.assertFalse(lat.isRetired());
		Assert.assertNull(lat.getDateRetired());
		Assert.assertNull(lat.getRetiredBy());
		Assert.assertNull(lat.getRetireReason());
	}
	
	/**
	 * @see LocationService#getLocationAttributeByUuid(String)
	 * @verifies get the location attribute with the given uuid
	 */
	@Test
	public void getLocationAttributeByUuid_shouldGetTheLocationAttributeWithTheGivenUuid() throws Exception {
		executeDataSet(LOC_ATTRIBUTE_DATA_XML);
		LocationService service = Context.getLocationService();
		Assert.assertEquals("2011-04-25", service.getLocationAttributeByUuid("3a2bdb18-6faa-11e0-8414-001e378eb67e")
		        .getValueReference());
	}
	
	/**
	 * @see LocationService#getLocationAttributeByUuid(String)
	 * @verifies return null if no location attribute has the given uuid
	 */
	@Test
	public void getLocationAttributeByUuid_shouldReturnNullIfNoLocationAttributeHasTheGivenUuid() throws Exception {
		executeDataSet(LOC_ATTRIBUTE_DATA_XML);
		LocationService service = Context.getLocationService();
		Assert.assertNull(service.getLocationAttributeByUuid("not-a-uuid"));
	}
	
	/**
	 * should set audit info if any item in the location is edited
	 * 
	 * @see {@link LocationService#saveLocation(Location)}
	 */
	@Test
	@Verifies(value = "should set audit info if any item in the location is edited", method = "saveLocation(Location)")
	public void saveLocation_shouldSetAuditInfoIfAnyItemInTheLocationIsEdited() throws Exception {
		LocationService ls = Context.getLocationService();
		
		Location location = ls.getLocation(1);
		Assert.assertNotNull(location);
		Assert.assertNull(location.getDateChanged());
		Assert.assertNull(location.getChangedBy());
		
		location.setName("edited name");
		ls.saveLocation(location);
		
		Location editedLocation = Context.getLocationService().saveLocation(location);
		Context.flushSession();
		
		Assert.assertNotNull(editedLocation.getDateChanged());
		Assert.assertNotNull(editedLocation.getChangedBy());
		
	}
	
	/**
	 * should set audit info if any item in the location tag is edited
	 * 
	 * @see {@link LocationService#saveLocationTag(LocationTag)}
	 */
	@Test
	@Verifies(value = "should set audit info if any item in the location tag is edited", method = "saveLocationTag(LocationTag)")
	public void saveLocationTag_shouldSetAuditInfoIfAnyItemInTheLocationTagIsEdited() throws Exception {
		LocationService ls = Context.getLocationService();
		LocationTag tag = ls.getLocationTag(1);
		
		Assert.assertNull(tag.getDateChanged());
		Assert.assertNull(tag.getChangedBy());
		
		tag.setName("testing");
		tag.setDescription("desc");
		
		LocationTag editedTag = Context.getLocationService().saveLocationTag(tag);
		Context.flushSession();
		
		Assert.assertNotNull(editedTag.getDateChanged());
		Assert.assertNotNull(editedTag.getChangedBy());
	}
	
	/**
	 * @see LocationService#getLocationAttributeTypeByName(String)
	 * @verifies return the location attribute type with the specified name
	 */
	@Test
	public void getLocationAttributeTypeByName_shouldReturnTheLocationAttributeTypeWithTheSpecifiedName() throws Exception {
		executeDataSet(LOC_ATTRIBUTE_DATA_XML);
		LocationAttributeType locationAttributeType = Context.getLocationService().getLocationAttributeTypeByName(
		    "Audit Date");
		Assert.assertNotNull(locationAttributeType);
		Assert.assertEquals("Audit Date", locationAttributeType.getName());
	}
	
	/**
	 * @see LocationService#getLocationAttributeTypeByName(String)
	 * @verifies return null if no location attribute type exists with the specified name
	+	 */
	@Test
	public void getLocationAttributeTypeByName_shouldReturnNullIfNoLocationAttributeTypeExistsWithTheSpecifiedName()
	        throws Exception {
		executeDataSet(LOC_ATTRIBUTE_DATA_XML);
		Assert.assertNull(Context.getLocationService().getLocationAttributeTypeByName("not-a-name"));
	}
	
	/**
	 * @see LocationService#retireLocation(Location location, String reason)
	 */
	@Test
	@Verifies(value = "should not retire location tag", method = "retireLocation(Location location, String reason)")
	public void retireLocation_shouldNotRetireIndependentField() throws Exception {
		LocationService locationService = Context.getLocationService();
		Location location = new Location(1);
		location.setName("location to retire");
		LocationTag tag = new LocationTag(1);
		location.addTag(tag);
		locationService.retireLocation(location, "test retire reason");
		Assert.assertFalse(tag.isRetired());
	}
	
}
