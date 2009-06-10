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
import org.openmrs.LocationTag;
import org.openmrs.annotation.Authorized;
import org.openmrs.api.db.LocationDAO;
import org.openmrs.util.OpenmrsConstants;
import org.springframework.transaction.annotation.Transactional;

/**
 * API methods for managing Locations <br/>
 * <br/>
 * Example Usage: <br/>
 * <code>
 *   List<Location> locations = Context.getLocationService().getAllLocations();
 * </code>
 * 
 * @see org.openmrs.api.context.Context
 * @see org.openmrs.Location
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
	 * @should throw APIException if location has no name
	 * @should overwrite transient tag if tag with same name exists
	 * @should throw APIException if transient tag is not found
	 * @should return saved object
	 * @should remove location tag from location
	 * @should add location tag to location
	 * @should remove child location from location
	 * @should cascade save to child location from location
	 * @should update location successfully
	 * @should create location successfully
	 */
	@Authorized( { OpenmrsConstants.PRIV_MANAGE_LOCATIONS })
	public Location saveLocation(Location location) throws APIException;
	
	/**
	 * Returns a location given that locations primary key <code>locationId</code> A null value is
	 * returned if no location exists with this location.
	 * 
	 * @param locationId integer primary key of the location to find
	 * @return Location object that has location.locationId = <code>locationId</code> passed in.
	 */
	@Transactional(readOnly = true)
	@Authorized( { OpenmrsConstants.PRIV_VIEW_LOCATIONS })
	public Location getLocation(Integer locationId) throws APIException;
	
	/**
	 * Returns a location given the location's exact <code>name</code> A null value is returned if
	 * there is no location with this name
	 * 
	 * @param name the exact name of the location to match on
	 * @return Location matching the <code>name</code> to Location.name
	 */
	@Transactional(readOnly = true)
	@Authorized( { OpenmrsConstants.PRIV_VIEW_LOCATIONS })
	public Location getLocation(String name) throws APIException;
	
	/**
	 * Returns the default location for this implementation.
	 * 
	 * @return The default location for this implementation.
	 */
	@Transactional(readOnly = true)
	@Authorized( { OpenmrsConstants.PRIV_VIEW_LOCATIONS })
	public Location getDefaultLocation() throws APIException;
	
	/**
	 * Returns a location by uuid
	 * 
	 * @param uuid is the uuid of the desired location
	 * @return location with the given uuid
	 * @should find object given valid uuid
	 * @should return null if no object found with given uuid
	 */
	@Transactional(readOnly = true)
	@Authorized( { OpenmrsConstants.PRIV_VIEW_LOCATIONS })
	public Location getLocationByUuid(String uuid) throws APIException;
	
	/**
	 * Returns all locations, includes retired locations. This method delegates to the
	 * #getAllLocations(boolean) method
	 * 
	 * @return locations that are in the database
	 */
	@Transactional(readOnly = true)
	@Authorized( { OpenmrsConstants.PRIV_VIEW_LOCATIONS })
	public List<Location> getAllLocations() throws APIException;
	
	/**
	 * Returns all locations.
	 * 
	 * @param includeRetired whether or not to include retired locations
	 * @should return locations when includeRetired is false
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
	 * Returns locations that contain the given tag.
	 * 
	 * @param tag LocationTag criterion
	 * @since 1.5
	 * @should get locations by tag
	 */
	@Transactional(readOnly = true)
	@Authorized( { OpenmrsConstants.PRIV_VIEW_LOCATIONS })
	public List<Location> getLocationsByTag(LocationTag tag) throws APIException;
	
	/**
	 * Returns locations that are mapped to all given tags.
	 * 
	 * @param tags Set of LocationTag criteria
	 * @since 1.5
	 * @should get locations having all tags
	 */
	@Transactional(readOnly = true)
	@Authorized( { OpenmrsConstants.PRIV_VIEW_LOCATIONS })
	public List<Location> getLocationsHavingAllTags(List<LocationTag> tags) throws APIException;
	
	/**
	 * Returns locations that are mapped to any of the given tags.
	 * 
	 * @param tags Set of LocationTag criteria
	 * @since 1.5
	 * @should get locations having any tag
	 */
	@Transactional(readOnly = true)
	@Authorized( { OpenmrsConstants.PRIV_VIEW_LOCATIONS })
	public List<Location> getLocationsHavingAnyTag(List<LocationTag> tags) throws APIException;
	
	/**
	 * Retires the given location. This effectively removes the location from circulation or use.
	 * 
	 * @param location location to be retired
	 * @param reason is the reason why the location is being retired
	 * @should retire location successfully
	 */
	@Authorized( { OpenmrsConstants.PRIV_MANAGE_LOCATIONS })
	public Location retireLocation(Location location, String reason) throws APIException;
	
	/**
	 * Unretire the given location. This restores a previously retired location back into
	 * circulation and use.
	 * 
	 * @param location
	 * @return the newly unretired location
	 * @throws APIException
	 */
	@Authorized( { OpenmrsConstants.PRIV_MANAGE_LOCATIONS })
	public Location unretireLocation(Location location) throws APIException;
	
	/**
	 * Completely remove a location from the database (not reversible) This method delegates to
	 * #purgeLocation(location, boolean) method
	 * 
	 * @param location the Location to clean out of the database.
	 * @should delete location successfully
	 */
	@Authorized( { OpenmrsConstants.PRIV_PURGE_LOCATIONS })
	public void purgeLocation(Location location) throws APIException;
	
	/**
	 * Save location tag to database (create if new or update if changed)
	 * 
	 * @param tag is the tag to be saved to the database
	 * @since 1.5
	 * @should throw APIException if tag has no name
	 * @should return saved object
	 * @should update location tag successfully
	 * @should create location tag successfully
	 */
	@Authorized( { OpenmrsConstants.PRIV_MANAGE_LOCATION_TAGS })
	public LocationTag saveLocationTag(LocationTag tag) throws APIException;
	
	/**
	 * Returns a location tag given that locations primary key <code>locationTagId</code>. A null
	 * value is returned if no tag exists with this ID.
	 * 
	 * @param locationTagId integer primary key of the location tag to find
	 * @return LocationTag object that has LocationTag.locationTagId = <code>locationTagId</code>
	 *         passed in.
	 * @since 1.5
	 */
	@Transactional(readOnly = true)
	@Authorized( { OpenmrsConstants.PRIV_VIEW_LOCATIONS })
	public LocationTag getLocationTag(Integer locationTagId) throws APIException;
	
	/**
	 * Returns a location tag given the location's exact name (tag). A null value is returned if
	 * there is no tag with this name.
	 * 
	 * @param tag the exact name of the tag to match on
	 * @return LocationTag matching the name to LocationTag.tag
	 * @since 1.5
	 * @should get location tag by name
	 */
	@Transactional(readOnly = true)
	@Authorized( { OpenmrsConstants.PRIV_VIEW_LOCATIONS })
	public LocationTag getLocationTagByName(String tag) throws APIException;
	
	/**
	 * Returns all location tags, includes retired location tags. This method delegates to the
	 * #getAllLocationTags(boolean) method.
	 * 
	 * @return location tags that are in the database
	 * @since 1.5
	 */
	@Transactional(readOnly = true)
	@Authorized( { OpenmrsConstants.PRIV_VIEW_LOCATIONS })
	public List<LocationTag> getAllLocationTags() throws APIException;
	
	/**
	 * Returns all location tags.
	 * 
	 * @param includeRetired whether or not to include retired location tags
	 * @since 1.5
	 */
	@Transactional(readOnly = true)
	@Authorized( { OpenmrsConstants.PRIV_VIEW_LOCATIONS })
	public List<LocationTag> getAllLocationTags(boolean includeRetired) throws APIException;
	
	/**
	 * Returns location tags that match the beginning of the given string. A null list will never be
	 * returned. An empty list will be returned if there are no tags. Search is case insensitive.
	 * matching this <code>search</code>
	 * 
	 * @param search is the string used to search for tags
	 * @since 1.5
	 */
	@Transactional(readOnly = true)
	@Authorized( { OpenmrsConstants.PRIV_VIEW_LOCATIONS })
	public List<LocationTag> getLocationTags(String search) throws APIException;
	
	/**
	 * Retire the given location tag. This effectively removes the tag from circulation or use.
	 * 
	 * @param tag location tag to be retired
	 * @param reason is the reason why the location tag is being retired
	 * @should retire location tag successfully
	 * @since 1.5
	 */
	@Authorized( { OpenmrsConstants.PRIV_MANAGE_LOCATION_TAGS })
	public LocationTag retireLocationTag(LocationTag tag, String reason) throws APIException;
	
	/**
	 * Unretire the given location tag. This restores a previously retired tag back into circulation
	 * and use.
	 * 
	 * @param tag
	 * @return the newly unretired location tag
	 * @throws APIException
	 * @since 1.5
	 */
	@Authorized( { OpenmrsConstants.PRIV_MANAGE_LOCATION_TAGS })
	public LocationTag unretireLocationTag(LocationTag tag) throws APIException;
	
	/**
	 * Completely remove a location tag from the database (not reversible).
	 * 
	 * @param tag the LocationTag to clean out of the database.
	 * @since 1.5
	 * @should delete location tag
	 */
	@Authorized( { OpenmrsConstants.PRIV_PURGE_LOCATION_TAGS })
	public void purgeLocationTag(LocationTag tag) throws APIException;
}
