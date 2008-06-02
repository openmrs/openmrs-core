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
	 * @param location
	 * @return
	 */
	public Location saveLocation(Location location);

	/**
	 * Get a location by locationId
	 * 
	 * @param locationId of the location to get
	 * @return
	 */
	public Location getLocation(Integer locationId);

	/**
	 * Get a location by name
	 * 
	 * @param name of the location to get
	 * @return
	 */
	public Location getLocation(String name);

	/**
	 * Get all locations
	 * 
	 * @param includeRetired if <code>true</code> then return retired
	 *        locations as well.
	 * @return
	 */
	public List<Location> getAllLocations(boolean includeRetired);

	/**
	 * Find all locations with matching names. 
	 * 
	 * @param search name to search
	 * @return
	 */
	public List<Location> getLocations(String search);

	/**
	 * Completely remove the location from the database.
	 * 
	 * @param location
	 */
	public void deleteLocation(Location location);

}
