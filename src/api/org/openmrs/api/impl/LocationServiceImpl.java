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
package org.openmrs.api.impl;

import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Location;
import org.openmrs.api.APIException;
import org.openmrs.api.LocationService;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.LocationDAO;

/**
 * Default implementation of the {@link LocationService}
 * 
 * This class should not be instantiated alone, get a service class
 * from the Context: Context.getLocationService();
 * 
 * @see org.openmrs.api.context.Context
 * @see org.openmrs.api.LocationService
 * @see org.openmrs.Location
*/
public class LocationServiceImpl extends BaseOpenmrsService implements LocationService {

	private Log log = LogFactory.getLog(this.getClass());

	private LocationDAO dao;
	
	/**
	 * @see org.openmrs.api.LocationService#setLocationDAO(org.openmrs.api.db.LocationDAO)
	 */
	public void setLocationDAO(LocationDAO dao) {
		this.dao = dao;
	}

	/**
	 * @see org.openmrs.api.LocationService#saveLocation(org.openmrs.Location)
	 */
	public Location saveLocation(Location location) throws APIException {
		if (location.getName() == null) {
			throw new APIException("Location name is required");
		}
		if (location.getDateCreated() == null) {
			location.setDateCreated(new Date());
		}
		if (location.getCreator() == null) {
			location.setCreator(Context.getAuthenticatedUser());
		}
		return dao.saveLocation(location);
	}

	/**
	 * @see org.openmrs.api.LocationService#getLocation(java.lang.Integer)
	 */
	public Location getLocation(Integer locationId) throws APIException {
		return dao.getLocation(locationId);
	}

	/**
	 * @see org.openmrs.api.LocationService#getLocation(java.lang.String)
	 */
	public Location getLocation(String name) throws APIException {
		return dao.getLocation(name);
	}

	/**
	 * @see org.openmrs.api.LocationService#getDefaultLocation()
	 */
	public Location getDefaultLocation() throws APIException { 
		
		// TODO The name of the default location should be configured using global properties 
		Location location = getLocation("Unknown Location");
		
		// If Unknown Location does not exist, try Unknown
		if (location == null) { 
			location = getLocation("Unknown");
		}
		
		// If neither exist, get the first available location
		if ( location == null ) {
			location = getLocation(Integer.valueOf(1));
		}
		
		// TODO Figure out if we should/could throw an exception if there's  
		// no location to fall back on.
		//if (location == null) { 
		//	throw new APIException("Default location does not exist");
		//}
		
		
		return location;
	}
	
	/**
	 * TODO: Not yet implemented for guid.
	 * @see org.openmrs.api.LocationService#getLocationByGuid(java.lang.String)
	 */
	public Location getLocationByGuid(String guid) throws APIException {
		if (log.isErrorEnabled())
			log.error("Getting a location with guid is not yet implemented. " + guid);
		//return dao.getLocationByGuid(guid);
		return null;
	}

	/**
	 * @see org.openmrs.api.LocationService#getLocations()
	 */
	public List<Location> getAllLocations() throws APIException {
		return dao.getAllLocations(true);
	}

	/**
	 * @see org.openmrs.api.LocationService#getLocations(boolean)
	 */
	public List<Location> getAllLocations(boolean includeRetired) throws APIException {
		return dao.getAllLocations(includeRetired);
	}

	/**
	 * @see org.openmrs.api.LocationService#getLocations(java.lang.String)
	 */
	public List<Location> getLocations(String nameFragment) throws APIException {
		return dao.getLocations(nameFragment);
	}

	/**
	 * @see org.openmrs.api.LocationService#retireLocation(org.openmrs.Location)
	 */
	public Location retireLocation(Location location, String reason) throws APIException {
		if (location.getRetired()) {
			return location;
		} else {
			if (reason == null)
				throw new APIException("Reason is required");
			location.setRetired(true);
			location.setRetireReason(reason);
			location.setRetiredBy(Context.getAuthenticatedUser());
			location.setDateRetired(new Date());
			return saveLocation(location);
		}
	}
	
	/**
	 * @see org.openmrs.api.LocationService#unretireLocation(org.openmrs.Location)
	 */
	public Location unretireLocation(Location location) throws APIException {
		location.setRetired(false);
		location.setRetireReason(null);
		location.setRetiredBy(null);
		location.setDateRetired(null);
		return saveLocation(location);
	}

	/**
	 * @see org.openmrs.api.LocationService#purgeLocation(org.openmrs.Location)
	 */
	public void purgeLocation(Location location) throws APIException {
		dao.deleteLocation(location);
	}

}
