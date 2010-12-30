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
package org.openmrs.api.db;

import java.util.List;

import org.hibernate.SessionFactory;
import org.openmrs.Location;
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
	 * @param locationId Internal <code>Integer</code> identifier of the <code>Location<code> to get
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
	 * @return <code>List<Location></code> object of all <code>Location</code>s, possibly including
	 *         retired locations
	 */
	public List<Location> getAllLocations(boolean includeRetired);
	
	/**
	 * Returns a specified number of locations starting with a given string from the specified index
	 * 
	 * @see LocationService#getLocations(String, boolean, Integer, Integer)
	 */
	public List<Location> getLocations(String nameFragment, boolean includeRetired, Integer start, Integer length)
	        throws DAOException;
	
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
	 * @return List<LocationTag> object with all <code>LocationTag</code>s, possibly included
	 *         retired ones
	 */
	public List<LocationTag> getAllLocationTags(boolean includeRetired);
	
	/**
	 * Find all location tags with matching names.
	 * 
	 * @param search name to search
	 * @return List<LocationTag> with all matching <code>LocationTags</code>
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
	 * @return
	 */
	public LocationTag getLocationTagByUuid(String uuid);
	
	/**
	 * @see org.openmrs.api.LocationService#getCountOfLocations(String, Boolean)
	 */
	public Integer getCountOfLocations(String nameFragment, Boolean includeRetired);
	
}
