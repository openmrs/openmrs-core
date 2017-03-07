/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.db;

import java.util.List;
import java.util.Map;

import org.hibernate.SessionFactory;
import org.openmrs.Location;
import org.openmrs.LocationAttribute;
import org.openmrs.LocationAttributeType;
import org.openmrs.LocationTag;
import org.openmrs.api.LocationService;

/**
 * Location-related database functions
 */
public interface LocationDAO {
	
	/**
	 * Set the Hibernate SessionFactory to connect to the database.
	 * 
	 * @param sessionFactory
	 */
	public void setSessionFactory(SessionFactory sessionFactory);
	
	/**
	 * Create or update a location.
	 * 
	 * @param location <code>Location</code> to save
	 * @return the saved <code>Location</code>
	 */
	public Location saveLocation(Location location);
	
	/**
	 * Get a location by locationId
	 * 
	 * @param locationId Internal <code>Integer</code> identifier of the <code>Location</code> to get
	 * @return the requested <code>Location</code>
	 */
	public Location getLocation(Integer locationId);
	
	/**
	 * Get a location by name
	 * 
	 * @param name String name of the <code>Location</code> to get
	 * @return the requested <code>Location</code>
	 */
	public Location getLocation(String name);
	
	/**
	 * Get all locations
	 * 
	 * @param includeRetired boolean - include retired locations as well?
	 * @return <code>List&lt;Location&gt;</code> object of all <code>Location</code>s, possibly including
	 *         retired locations
	 */
	public List<Location> getAllLocations(boolean includeRetired);
	
	/**
	 * Gets the locations matching the specified arguments
	 *
	 * @param nameFragment is the string used to search for locations
	 * @param parent only return children of this parent
	 * @param serializedAttributeValues the serialized attribute values
	 * @param includeRetired specifies if retired locations should also be returned
	 * @param start the beginning index
	 * @param length the number of matching locations to return
	 * @return the list of locations
	 */
	public List<Location> getLocations(String nameFragment, Location parent,
	        Map<LocationAttributeType, String> serializedAttributeValues, boolean includeRetired, Integer start,
	        Integer length) throws DAOException;
	
	/**
	 * Completely remove the location from the database.
	 * 
	 * @param location <code>Location</code> object to delete
	 */
	public void deleteLocation(Location location);
	
	/**
	 * Create or update a location tag.
	 * 
	 * @param tag
	 * @return the saved <code>LocationTag</code>
	 */
	public LocationTag saveLocationTag(LocationTag tag);
	
	/**
	 * Get a location tag by <code>locationTagId</code>
	 * 
	 * @param locationTagId Internal <code>Integer</code> identifier of the tag to get
	 * @return the requested <code>LocationTag</code>
	 */
	public LocationTag getLocationTag(Integer locationTagId);
	
	/**
	 * Get a location tag by name
	 * 
	 * @param tag String representation of the <code>LocationTag</code> to get
	 * @return the requested <code>LocationTag</code>
	 */
	public LocationTag getLocationTagByName(String tag);
	
	/**
	 * Get all location tags
	 * 
	 * @param includeRetired boolean - include retired tags as well?
	 * @return List&lt;LocationTag&gt; object with all <code>LocationTag</code>s, possibly included
	 *         retired ones
	 */
	public List<LocationTag> getAllLocationTags(boolean includeRetired);
	
	/**
	 * Find all location tags with matching names.
	 * 
	 * @param search name to search
	 * @return List&lt;LocationTag&gt; with all matching <code>LocationTags</code>
	 */
	public List<LocationTag> getLocationTags(String search);
	
	/**
	 * Completely remove the location tag from the database.
	 * 
	 * @param tag The <code>LocationTag</code> to delete
	 */
	public void deleteLocationTag(LocationTag tag);
	
	/**
	 * @param uuid the uuid to look for
	 * @return location matching uuid
	 */
	public Location getLocationByUuid(String uuid);
	
	/**
	 * @param uuid
	 * @return location tag or null
	 */
	public LocationTag getLocationTagByUuid(String uuid);
	
	/**
	 * @see org.openmrs.api.LocationService#getCountOfLocations(String, Boolean)
	 */
	public Long getCountOfLocations(String nameFragment, Boolean includeRetired);
	
	/**
	 * @see LocationService#getRootLocations(boolean)
	 */
	public List<Location> getRootLocations(boolean includeRetired);
	
	/**
	 * @see LocationService#getAllLocationAttributeTypes()
	 */
	public List<LocationAttributeType> getAllLocationAttributeTypes();
	
	/**
	 * @see LocationService#getLocationAttributeType(Integer)
	 */
	public LocationAttributeType getLocationAttributeType(Integer id);
	
	/**
	 * @see LocationService#getLocationAttributeTypeByUuid(String)
	 */
	public LocationAttributeType getLocationAttributeTypeByUuid(String uuid);
	
	/**
	 * @see LocationService#saveLocationAttributeType(LocationAttributeType)
	 */
	public LocationAttributeType saveLocationAttributeType(LocationAttributeType locationAttributeType);
	
	/**
	 * @see LocationService#purgeLocationAttributeType(LocationAttributeType)
	 */
	public void deleteLocationAttributeType(LocationAttributeType locationAttributeType);
	
	/**
	 * @see LocationService#getLocationAttributeByUuid(String)
	 */
	public LocationAttribute getLocationAttributeByUuid(String uuid);
	
	/**
	 * @see LocationService#getLocationAttributeTypeByName(String)
	 */
	public LocationAttributeType getLocationAttributeTypeByName(String name);
	
	/**
	 * Get locations that have all the location tags specified.
	 *
	 * @param locationTagIdList
	 * @return list of locations
	 * @should get locations having all tags
	 * @should return empty list when no location has the given tags
	 * @should ignore null values in location tag list
	 */
	List<Location> getLocationsHavingAllTags(List<LocationTag> locationTagIdList);
}
