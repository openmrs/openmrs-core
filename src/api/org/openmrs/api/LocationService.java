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

import java.util.List;

import org.openmrs.Location;
import org.openmrs.annotation.Authorized;
import org.openmrs.api.db.LocationDAO;
import org.openmrs.util.OpenmrsConstants;
import org.springframework.transaction.annotation.Transactional;

/**
 * API methods for managing Locations Use: <code>
 *   List<Location> locations = Context.getLocationService().getAllLocations();
 * </code>
 * 
 * @see org.openmrs.api.context.Context
 */
@Transactional()
public interface LocationService extends OpenmrsService {
	
	/**
	 * Set the data access object that the service will use to interact with the database. This is
	 * set by spring in the applicationContext-service.xml file
	 * 
	 * @param dao
	 */
	public void setLocationDAO(LocationDAO dao);
	
	/**
	 * Save location to database (create if new or update if changed)
	 * 
	 * @param location is the location to be saved to the database
	 */
	@Authorized( { OpenmrsConstants.PRIV_MANAGE_LOCATIONS })
	public Location saveLocation(Location location) throws APIException;
	
	/**
	 * Returns a location given that locations primary key <code>locationId</code> A null value is
	 * returned if no location exists with this location.
	 * 
	 * @param locationId integer primary key of the location to find
	 * @returns Location object that has location.locationId = <code>locationId</code> passed in.
	 */
	@Transactional(readOnly = true)
	@Authorized( { OpenmrsConstants.PRIV_VIEW_LOCATIONS })
	public Location getLocation(Integer locationId) throws APIException;
	
	/**
	 * Returns a location given the location's exact <code>name</code> A null value is returned if
	 * there is no location with this name
	 * 
	 * @param name the exact name of the location to match on
	 * @returns Location matching the <code>name</code> to Location.name
	 */
	@Transactional(readOnly = true)
	@Authorized( { OpenmrsConstants.PRIV_VIEW_LOCATIONS })
	public Location getLocation(String name) throws APIException;
	
	/**
	 * Returns the default location for this implementation.
	 * 
	 * @returns The default location for this implementation.
	 */
	@Transactional(readOnly = true)
	@Authorized( { OpenmrsConstants.PRIV_VIEW_LOCATIONS })
	public Location getDefaultLocation() throws APIException;
	
	/**
	 * Returns a location by guid TODO: Not yet implemented.
	 * 
	 * @param guid is the guid of the desired location
	 * @returns location with the given guid
	 */
	@Transactional(readOnly = true)
	@Authorized( { OpenmrsConstants.PRIV_VIEW_LOCATIONS })
	public Location getLocationByGuid(String guid) throws APIException;
	
	/**
	 * Returns all locations, includes retired locations. This method delegates to the
	 * #getAllLocations(boolean) method
	 * 
	 * @returns locations that are in the database
	 */
	@Transactional(readOnly = true)
	@Authorized( { OpenmrsConstants.PRIV_VIEW_LOCATIONS })
	public List<Location> getAllLocations() throws APIException;
	
	/**
	 * Returns all locations.
	 * 
	 * @param includeRetired whether or not to include retired locations
	 */
	@Transactional(readOnly = true)
	@Authorized( { OpenmrsConstants.PRIV_VIEW_LOCATIONS })
	public List<Location> getAllLocations(boolean includeRetired) throws APIException;
	
	/**
	 * Returns locations that match the beginning of the given string. A null list will never be
	 * returned. An empty list will be returned if there are no locations. Search is case
	 * insensitive. matching this <code>nameFragment</code>
	 * 
	 * @param nameFragment is the string used to search for locations
	 */
	@Transactional(readOnly = true)
	@Authorized( { OpenmrsConstants.PRIV_VIEW_LOCATIONS })
	public List<Location> getLocations(String nameFragment) throws APIException;
	
	/**
	 * Retires the given location. This effectively removes the location from circulation or use.
	 * 
	 * @param location location to be retired
	 * @param reason is the reason why the location is being retired
	 */
	@Authorized( { OpenmrsConstants.PRIV_MANAGE_LOCATIONS })
	public Location retireLocation(Location location, String reason) throws APIException;
	
	/**
	 * Unretire the given location. This restores a previously retired location back into
	 * circulation and use.
	 * 
	 * @param location
	 * @return
	 * @throws APIException
	 */
	@Authorized( { OpenmrsConstants.PRIV_MANAGE_LOCATIONS })
	public Location unretireLocation(Location location) throws APIException;
	
	/**
	 * Completely remove a location from the database (not reversible) This method delegates to
	 * #purgeLocation(location, boolean) method
	 * 
	 * @param location the Location to clean out of the database.
	 */
	@Authorized( { OpenmrsConstants.PRIV_PURGE_LOCATIONS })
	public void purgeLocation(Location location) throws APIException;
	
}
