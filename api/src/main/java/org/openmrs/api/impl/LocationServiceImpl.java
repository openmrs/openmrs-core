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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.openmrs.Address;
import org.openmrs.Location;
import org.openmrs.LocationAttribute;
import org.openmrs.LocationAttributeType;
import org.openmrs.LocationTag;
import org.openmrs.api.APIException;
import org.openmrs.api.LocationService;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.LocationDAO;
import org.openmrs.customdatatype.CustomDatatypeUtil;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.OpenmrsUtil;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * Default implementation of the {@link LocationService}
 * <p>
 * This class should not be instantiated alone, get a service class from the Context:
 * Context.getLocationService();
 *
 * @see org.openmrs.api.context.Context
 * @see org.openmrs.api.LocationService
 * @see org.openmrs.Location
 */
@Transactional
public class LocationServiceImpl extends BaseOpenmrsService implements LocationService {
	
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
		
		// Check for transient tags. If found, try to match by name and overwrite, otherwise throw exception.
		if (location.getTags() != null) {
			for (LocationTag tag : location.getTags()) {
				
				// only check transient (aka non-precreated) location tags
				if (tag.getLocationTagId() == null) {
					if (!StringUtils.hasLength(tag.getName())) {
						throw new APIException("A tag name is required");
					}
					
					LocationTag existing = Context.getLocationService().getLocationTagByName(tag.getName());
					if (existing != null) {
						location.removeTag(tag);
						location.addTag(existing);
					} else {
						throw new APIException("Cannot add transient tags! "
						        + "Save all location tags to the database before saving this location");
					}
				}
			}
		}
		//Check for XSS
		location.validateXSS();
		CustomDatatypeUtil.saveAttributesIfNecessary(location);
		
		return dao.saveLocation(location);
	}
	
	/**
	 * @see org.openmrs.api.LocationService#getLocation(java.lang.Integer)
	 */
	@Transactional(readOnly = true)
	public Location getLocation(Integer locationId) throws APIException {
		return dao.getLocation(locationId);
	}
	
	/**
	 * @see org.openmrs.api.LocationService#getLocation(java.lang.String)
	 */
	@Transactional(readOnly = true)
	public Location getLocation(String name) throws APIException {
		return dao.getLocation(name);
	}
	
	/**
	 * @see org.openmrs.api.LocationService#getDefaultLocation()
	 */
	@Transactional(readOnly = true)
	public Location getDefaultLocation() throws APIException {
		Location location = null;
		String locationGP = Context.getAdministrationService().getGlobalProperty(
		    OpenmrsConstants.GLOBAL_PROPERTY_DEFAULT_LOCATION_NAME);
		
		if (StringUtils.hasText(locationGP)) {
			location = Context.getLocationService().getLocation(locationGP);
		}
		
		//Try to look up 'Unknown Location' in case the global property is something else
		if (location == null && (!StringUtils.hasText(locationGP) || !locationGP.equalsIgnoreCase("Unknown Location"))) {
			location = Context.getLocationService().getLocation("Unknown Location");
		}
		
		// If Unknown Location does not exist, try Unknown if the global property was different
		if (location == null && (!StringUtils.hasText(locationGP) || !locationGP.equalsIgnoreCase("Unknown"))) {
			location = Context.getLocationService().getLocation("Unknown");
		}
		
		// If neither exist, get the first available location
		if (location == null) {
			location = Context.getLocationService().getLocation(Integer.valueOf(1));
		}
		
		// TODO Figure out if we should/could throw an exception if there's  
		// no location to fall back on.
		//if (location == null) { 
		//	throw new APIException("Default location does not exist");
		//}
		
		return location;
	}
	
	/**
	 * @see org.openmrs.api.LocationService#getLocationByUuid(java.lang.String)
	 */
	@Transactional(readOnly = true)
	public Location getLocationByUuid(String uuid) throws APIException {
		return dao.getLocationByUuid(uuid);
	}
	
	/**
	 * @see org.openmrs.api.LocationService#getLocationTagByUuid(java.lang.String)
	 */
	@Transactional(readOnly = true)
	public LocationTag getLocationTagByUuid(String uuid) throws APIException {
		return dao.getLocationTagByUuid(uuid);
	}
	
	/**
	 * @see org.openmrs.api.LocationService#getAllLocations()
	 */
	@Transactional(readOnly = true)
	public List<Location> getAllLocations() throws APIException {
		return dao.getAllLocations(true);
	}
	
	/**
	 * @see org.openmrs.api.LocationService#getAllLocations(boolean)
	 */
	@Transactional(readOnly = true)
	public List<Location> getAllLocations(boolean includeRetired) throws APIException {
		return dao.getAllLocations(includeRetired);
	}
	
	/**
	 * @see org.openmrs.api.LocationService#getLocations(java.lang.String)
	 */
	@Transactional(readOnly = true)
	public List<Location> getLocations(String nameFragment) throws APIException {
		return Context.getLocationService().getLocations(nameFragment, null, null, false, null, null);
	}
	
	/**
	 * @see org.openmrs.api.LocationService#getLocationsByTag(LocationTag)
	 */
	@Transactional(readOnly = true)
	public List<Location> getLocationsByTag(LocationTag tag) throws APIException {
		List<Location> locations = new ArrayList<Location>();
		
		for (Location l : dao.getAllLocations(false)) {
			if (l.getTags().contains(tag)) {
				locations.add(l);
			}
		}
		
		return locations;
	}
	
	/**
	 * @see org.openmrs.api.LocationService#getLocationsHavingAllTags(List)
	 */
	@Transactional(readOnly = true)
	public List<Location> getLocationsHavingAllTags(List<LocationTag> tags) throws APIException {
		List<Location> locations = new ArrayList<Location>();
		
		for (Location loc : dao.getAllLocations(false)) {
			if (loc.getTags().containsAll(tags)) {
				locations.add(loc);
			}
		}
		
		return locations;
	}
	
	/**
	 * @see org.openmrs.api.LocationService#getLocationsHavingAnyTag(List)
	 */
	@Transactional(readOnly = true)
	public List<Location> getLocationsHavingAnyTag(List<LocationTag> tags) throws APIException {
		List<Location> locations = new ArrayList<Location>();
		
		for (Location loc : dao.getAllLocations(false)) {
			for (LocationTag t : tags) {
				if (loc.getTags().contains(t) && !locations.contains(loc)) {
					locations.add(loc);
				}
			}
		}
		
		return locations;
	}
	
	/**
	 * @see org.openmrs.api.LocationService#retireLocation(Location, String)
	 */
	public Location retireLocation(Location location, String reason) throws APIException {
		location.setRetired(true);
		location.setRetireReason(reason);
		return Context.getLocationService().saveLocation(location);
	}
	
	/**
	 * @see org.openmrs.api.LocationService#unretireLocation(org.openmrs.Location)
	 */
	public Location unretireLocation(Location location) throws APIException {
		location.setRetired(false);
		return Context.getLocationService().saveLocation(location);
	}
	
	/**
	 * @see org.openmrs.api.LocationService#purgeLocation(org.openmrs.Location)
	 */
	public void purgeLocation(Location location) throws APIException {
		dao.deleteLocation(location);
	}
	
	/**
	 * @see org.openmrs.api.LocationService#saveLocationTag(org.openmrs.LocationTag)
	 */
	public LocationTag saveLocationTag(LocationTag tag) throws APIException {
		return dao.saveLocationTag(tag);
	}
	
	/**
	 * @see org.openmrs.api.LocationService#getLocationTag(java.lang.Integer)
	 */
	@Transactional(readOnly = true)
	public LocationTag getLocationTag(Integer locationTagId) throws APIException {
		return dao.getLocationTag(locationTagId);
	}
	
	/**
	 * @see org.openmrs.api.LocationService#getLocationTagByName(java.lang.String)
	 */
	@Transactional(readOnly = true)
	public LocationTag getLocationTagByName(String tag) throws APIException {
		return dao.getLocationTagByName(tag);
	}
	
	/**
	 * @see org.openmrs.api.LocationService#getAllLocationTags()
	 */
	@Transactional(readOnly = true)
	public List<LocationTag> getAllLocationTags() throws APIException {
		return dao.getAllLocationTags(true);
	}
	
	/**
	 * @see org.openmrs.api.LocationService#getAllLocationTags(boolean)
	 */
	@Transactional(readOnly = true)
	public List<LocationTag> getAllLocationTags(boolean includeRetired) throws APIException {
		return dao.getAllLocationTags(includeRetired);
	}
	
	/**
	 * @see org.openmrs.api.LocationService#getLocationTags(java.lang.String)
	 */
	@Transactional(readOnly = true)
	public List<LocationTag> getLocationTags(String search) throws APIException {
		if (search == null || search.equals("")) {
			return Context.getLocationService().getAllLocationTags(true);
		}
		
		return dao.getLocationTags(search);
	}
	
	/**
	 * @see org.openmrs.api.LocationService#retireLocationTag(LocationTag, String)
	 */
	public LocationTag retireLocationTag(LocationTag tag, String reason) throws APIException {
		if (tag.isRetired()) {
			return tag;
		} else {
			if (reason == null) {
				throw new APIException("Reason is required");
			}
			tag.setRetired(true);
			tag.setRetireReason(reason);
			tag.setRetiredBy(Context.getAuthenticatedUser());
			tag.setDateRetired(new Date());
			return Context.getLocationService().saveLocationTag(tag);
		}
	}
	
	/**
	 * @see org.openmrs.api.LocationService#unretireLocationTag(org.openmrs.LocationTag)
	 */
	public LocationTag unretireLocationTag(LocationTag tag) throws APIException {
		tag.setRetired(false);
		tag.setRetireReason(null);
		tag.setRetiredBy(null);
		tag.setDateRetired(null);
		return Context.getLocationService().saveLocationTag(tag);
	}
	
	/**
	 * @see org.openmrs.api.LocationService#purgeLocationTag(org.openmrs.LocationTag)
	 */
	public void purgeLocationTag(LocationTag tag) throws APIException {
		dao.deleteLocationTag(tag);
	}
	
	/**
	 * @see org.openmrs.api.LocationService#getCountOfLocations(String, Boolean)
	 */
	@Override
	@Transactional(readOnly = true)
	public Integer getCountOfLocations(String nameFragment, Boolean includeRetired) {
		return OpenmrsUtil.convertToInteger(dao.getCountOfLocations(nameFragment, includeRetired));
	}
	
	/**
	 * @see LocationService#getLocations(String, boolean, Integer, Integer)
	 */
	@Override
	@Deprecated
	@Transactional(readOnly = true)
	public List<Location> getLocations(String nameFragment, boolean includeRetired, Integer start, Integer length)
	        throws APIException {
		return dao.getLocations(nameFragment, null, null, includeRetired, start, length);
	}
	
	/**
	 * @see LocationService#getLocations(String, org.openmrs.Location, java.util.Map, boolean,
	 *      Integer, Integer)
	 */
	@Override
	public List<Location> getLocations(String nameFragment, Location parent,
	        Map<LocationAttributeType, Object> attributeValues, boolean includeRetired, Integer start, Integer length) {
		
		Map<LocationAttributeType, String> serializedAttributeValues = CustomDatatypeUtil
		        .getValueReferences(attributeValues);
		
		return dao.getLocations(nameFragment, parent, serializedAttributeValues, includeRetired, start, length);
	}
	
	/**
	 * @see LocationService#getRootLocations(boolean)
	 */
	@Override
	@Transactional(readOnly = true)
	public List<Location> getRootLocations(boolean includeRetired) throws APIException {
		return dao.getRootLocations(includeRetired);
	}
	
	/**
	 * @see org.openmrs.api.LocationService#getPossibleAddressValues(org.openmrs.Address,
	 *      org.openmrs.AddressField)
	 */
	public List<String> getPossibleAddressValues(Address incomplete, String fieldName) throws APIException {
		// not implemented by default
		return null;
	}
	
	/**
	 * @see org.openmrs.api.LocationService#getAllLocationAttributeTypes()
	 */
	@Override
	@Transactional(readOnly = true)
	public List<LocationAttributeType> getAllLocationAttributeTypes() {
		return dao.getAllLocationAttributeTypes();
	}
	
	/**
	 * @see org.openmrs.api.LocationService#getLocationAttributeType(java.lang.Integer)
	 */
	@Override
	@Transactional(readOnly = true)
	public LocationAttributeType getLocationAttributeType(Integer id) {
		return dao.getLocationAttributeType(id);
	}
	
	/**
	 * @see org.openmrs.api.LocationService#getLocationAttributeTypeByUuid(java.lang.String)
	 */
	@Override
	@Transactional(readOnly = true)
	public LocationAttributeType getLocationAttributeTypeByUuid(String uuid) {
		return dao.getLocationAttributeTypeByUuid(uuid);
	}
	
	/**
	 * @see org.openmrs.api.LocationService#saveLocationAttributeType(org.openmrs.LocationAttributeType)
	 */
	@Override
	public LocationAttributeType saveLocationAttributeType(LocationAttributeType locationAttributeType) {
		return dao.saveLocationAttributeType(locationAttributeType);
	}
	
	/**
	 * @see org.openmrs.api.LocationService#retireLocationAttributeType(org.openmrs.LocationAttributeType,
	 *      java.lang.String)
	 */
	@Override
	public LocationAttributeType retireLocationAttributeType(LocationAttributeType locationAttributeType, String reason) {
		return dao.saveLocationAttributeType(locationAttributeType);
	}
	
	/**
	 * @see org.openmrs.api.LocationService#unretireLocationAttributeType(org.openmrs.LocationAttributeType)
	 */
	@Override
	public LocationAttributeType unretireLocationAttributeType(LocationAttributeType locationAttributeType) {
		return dao.saveLocationAttributeType(locationAttributeType);
	}
	
	/**
	 * @see org.openmrs.api.LocationService#purgeLocationAttributeType(org.openmrs.LocationAttributeType)
	 */
	@Override
	public void purgeLocationAttributeType(LocationAttributeType locationAttributeType) {
		dao.deleteLocationAttributeType(locationAttributeType);
	}
	
	/**
	 * @see org.openmrs.api.LocationService#getLocationAttributeByUuid(java.lang.String)
	 */
	@Override
	@Transactional(readOnly = true)
	public LocationAttribute getLocationAttributeByUuid(String uuid) {
		return dao.getLocationAttributeByUuid(uuid);
	}
	
	/**
	 * @see org.openmrs.api.LocationService#getAddressTemplate()
	 */
	@Override
	@Transactional(readOnly = true)
	public String getAddressTemplate() throws APIException {
		String addressTemplate = Context.getAdministrationService().getGlobalProperty(
		    OpenmrsConstants.GLOBAL_PROPERTY_ADDRESS_TEMPLATE);
		if (!StringUtils.hasLength(addressTemplate)) {
			addressTemplate = OpenmrsConstants.DEFAULT_ADDRESS_TEMPLATE;
		}
		
		return addressTemplate;
	}
	
	/**
	 * @see org.openmrs.api.LocationService#saveAddressTemplate(String)
	 */
	@Override
	public void saveAddressTemplate(String xml) throws APIException {
		Context.getAdministrationService().setGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_ADDRESS_TEMPLATE, xml);
		
	}
	
	/**
	 * @see org.openmrs.api.LocationService#getLocationAttributeTypeByName(java.lang.String)
	 */
	@Override
	@Transactional(readOnly = true)
	public LocationAttributeType getLocationAttributeTypeByName(String name) {
		return dao.getLocationAttributeTypeByName(name);
	}
}
