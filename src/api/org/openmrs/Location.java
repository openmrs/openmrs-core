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
package org.openmrs;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;

/**
 * A Location is a physical place, such as a hospital, a room, a clinic, or a district.
 * 
 * Locations support a single hierarchy, such that each location may have one parent location.
 * 
 * A non-geographical grouping of locations, such as "All Community Health Centers" is not a location, and
 * should be modeled using {@link LocationTag}s. 
 */
public class Location extends BaseOpenmrsMetadata implements java.io.Serializable, Attributable<Location> {
	
	public static final long serialVersionUID = 455634L;
	
	public static final int LOCATION_UNKNOWN = 1;
	
	// Fields
	
	private Integer locationId;
	
	private String address1;
	
	private String address2;
	
	private String cityVillage;
	
	private String stateProvince;
	
	private String country;
	
	private String postalCode;
	
	private String latitude;
	
	private String longitude;
	
	private String countyDistrict;
	
	private String neighborhoodCell;
	
	private String townshipDivision;
	
	private String region;
	
	private String subregion;
	
	private Location parentLocation;
	
	private Set<Location> childLocations;
	
	private Set<LocationTag> tags;
	
	// Constructors
	
	/** default constructor */
	public Location() {
	}
	
	/** constructor with id */
	public Location(Integer locationId) {
		this.locationId = locationId;
	}
	
	/**
	 * Compares two objects for similarity
	 * 
	 * @param obj
	 * @return boolean true/false whether or not they are the same objects
	 */
	public boolean equals(Object obj) {
		if (obj instanceof Location) {
			Location loc = (Location) obj;
			if (this.getLocationId() != null && loc.getLocationId() != null)
				return (this.getLocationId().equals(loc.getLocationId()));
			/*
			 * return (this.getName().equals(loc.getName()) &&
			 * this.getDescription().equals(loc.getDescription()) &&
			 * this.getAddress1().equals(loc.getAddress1()) &&
			 * this.getAddress2().equals(loc.getAddress2()) &&
			 * this.getCityVillage().equals(loc.getCityVillage()) &&
			 * this.getStateProvince().equals(loc.getStateProvince()) &&
			 * this.getPostalCode().equals(loc.getPostalCode()) &&
			 * this.getCountry().equals(loc.getCountry()) &&
			 * this.getLatitude().equals(loc.getLatitude()) &&
			 * this.getLongitude().equals(loc.getLongitude()));
			 */
		}
		return obj == this;
	}
	
	public int hashCode() {
		if (this.getLocationId() == null)
			return super.hashCode();
		return this.getLocationId().hashCode();
	}
	
	// Property accessors
	
	/**
	 * @return Returns the address1.
	 */
	public String getAddress1() {
		return address1;
	}
	
	/**
	 * @param address1 The address1 to set.
	 */
	public void setAddress1(String address1) {
		this.address1 = address1;
	}
	
	/**
	 * @return Returns the address2.
	 */
	public String getAddress2() {
		return address2;
	}
	
	/**
	 * @param address2 The address2 to set.
	 */
	public void setAddress2(String address2) {
		this.address2 = address2;
	}
	
	/**
	 * @return Returns the cityVillage.
	 */
	public String getCityVillage() {
		return cityVillage;
	}
	
	/**
	 * @param cityVillage The cityVillage to set.
	 */
	public void setCityVillage(String cityVillage) {
		this.cityVillage = cityVillage;
	}
	
	/**
	 * @return Returns the country.
	 */
	public String getCountry() {
		return country;
	}
	
	/**
	 * @param country The country to set.
	 */
	public void setCountry(String country) {
		this.country = country;
	}
	
	/**
	 * @return Returns the latitude.
	 */
	public String getLatitude() {
		return latitude;
	}
	
	/**
	 * @param latitude The latitude to set.
	 */
	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}
	
	/**
	 * @return Returns the locationId.
	 */
	public Integer getLocationId() {
		return locationId;
	}
	
	/**
	 * @param locationId The locationId to set.
	 */
	public void setLocationId(Integer locationId) {
		this.locationId = locationId;
	}
	
	/**
	 * @return Returns the longitude.
	 */
	public String getLongitude() {
		return longitude;
	}
	
	/**
	 * @param longitude The longitude to set.
	 */
	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}
	
	/**
	 * @return Returns the postalCode.
	 */
	public String getPostalCode() {
		return postalCode;
	}
	
	/**
	 * @param postalCode The postalCode to set.
	 */
	public void setPostalCode(String postalCode) {
		this.postalCode = postalCode;
	}
	
	/**
	 * @return Returns the stateProvince.
	 */
	public String getStateProvince() {
		return stateProvince;
	}
	
	/**
	 * @param stateProvince The stateProvince to set.
	 */
	public void setStateProvince(String stateProvince) {
		this.stateProvince = stateProvince;
	}
	
	public String toString() {
		return getName();
	}
	
	/**
	 * @return Returns the countyDistrict.
	 */
	public String getCountyDistrict() {
		return countyDistrict;
	}
	
	/**
	 * @param countyDistrict The countyDistrict to set.
	 */
	public void setCountyDistrict(String countyDistrict) {
		this.countyDistrict = countyDistrict;
	}
	
	/**
	 * @return Returns the neighborhoodCell.
	 */
	public String getNeighborhoodCell() {
		return neighborhoodCell;
	}
	
	/**
	 * @param neighborhoodCell The neighborhoodCell to set.
	 */
	public void setNeighborhoodCell(String neighborhoodCell) {
		this.neighborhoodCell = neighborhoodCell;
	}
	
	/**
	 * @see org.openmrs.Attributable#findPossibleValues(java.lang.String)
	 */
	public List<Location> findPossibleValues(String searchText) {
		try {
			return Context.getLocationService().getLocations(searchText);
		}
		catch (Exception e) {
			return Collections.emptyList();
		}
	}
	
	/**
	 * @see org.openmrs.Attributable#getPossibleValues()
	 */
	public List<Location> getPossibleValues() {
		try {
			return Context.getLocationService().getAllLocations();
		}
		catch (Exception e) {
			return Collections.emptyList();
		}
	}
	
	/**
	 * @see org.openmrs.Attributable#hydrate(java.lang.String)
	 */
	public Location hydrate(String locationId) {
		try {
			return Context.getLocationService().getLocation(Integer.valueOf(locationId));
		}
		catch (Exception e) {
			return new Location();
		}
	}
	
	/**
	 * @see org.openmrs.Attributable#serialize()
	 */
	public String serialize() {
		if (getLocationId() != null)
			return "" + getLocationId();
		else
			return "";
	}
	
	/**
	 * @see org.openmrs.Attributable#getDisplayString()
	 */
	public String getDisplayString() {
		return getName();
	}
	
	/**
	 * @return the region
	 */
	public String getRegion() {
		return region;
	}
	
	/**
	 * @param region the region to set
	 */
	public void setRegion(String region) {
		this.region = region;
	}
	
	/**
	 * @return the subregion
	 */
	public String getSubregion() {
		return subregion;
	}
	
	/**
	 * @param subregion the subregion to set
	 */
	public void setSubregion(String subregion) {
		this.subregion = subregion;
	}
	
	/**
	 * @return the townshipDivision
	 */
	public String getTownshipDivision() {
		return townshipDivision;
	}
	
	/**
	 * @param townshipDivision the townshipDivision to set
	 */
	public void setTownshipDivision(String townshipDivision) {
		this.townshipDivision = townshipDivision;
	}
	
	/**
	 * @return Returns the parentLocation.
	 * @since 1.5
	 */
	public Location getParentLocation() {
		return parentLocation;
	}
	
	/**
	 * @param parentLocationId The parentLocation to set.
	 * @since 1.5
	 */
	public void setParentLocation(Location parentLocationId) {
		this.parentLocation = parentLocationId;
	}
	
	/**
	 * @return Returns the childLocations.
	 * @since 1.5
	 */
	public Set<Location> getChildLocations() {
		return childLocations;
	}
	
	/**
	 * Returns all childLocations where child.locationId = this.locationId.
	 * 
	 * @param includeRetired specifies whether or not to include voided childLocations
	 * @return Returns a Set<Location> of all the childLocations.
	 * @since 1.5
	 * @should return a set of locations
	 */
	public Set<Location> getChildLocations(boolean includeRetired) {
		Set<Location> ret = new HashSet<Location>();
		if (includeRetired)
			ret = getChildLocations();
		else if (getChildLocations() != null) {
			for (Location l : getChildLocations()) {
				if (!l.isRetired())
					ret.add(l);
			}
		}
		return ret;
	}
	
	/**
	 * @param childLocations The childLocations to set.
	 * @since 1.5
	 */
	public void setChildLocations(Set<Location> childLocations) {
		this.childLocations = childLocations;
	}
	
	/**
	 * @param child The child location to add.
	 * @since 1.5
	 * @should return null given null parameter
	 * @should throw APIException given same object as child
	 * @should throw APIException if child already in hierarchy
	 */
	public void addChildLocation(Location child) {
		if (child == null)
			return;
		
		if (getChildLocations() == null)
			childLocations = new HashSet<Location>();
		
		if (child.equals(this))
			throw new APIException("A location cannot be its own child!");
		
		// Traverse all the way up (down?) to the root, then check whether the child is already
		// anywhere in the tree
		Location root = this;
		while (root.getParentLocation() != null)
			root = root.getParentLocation();
		
		if (isInHierarchy(child, root))
			throw new APIException("Location hierarchy loop detected! You cannot add: '" + child + "' to the parent: '"
			        + this
			        + "' because it is in the parent hierarchy somewhere already and a location cannot be its own parent.");
		
		child.setParentLocation(this);
		childLocations.add(child);
	}
	
	/**
	 * Checks whether 'location' is a member of the tree starting at 'root'.
	 * 
	 * @param location The location to be tested.
	 * @param root Location node from which to start the testing (down in the hierarchy).
	 * @since 1.5
	 * @should return false given any null parameter
	 * @should return true given same object in both parameters
	 * @should return true given location that is already somewhere in hierarchy
	 * @should return false given location that is not in hierarchy
	 * @should should find location in hierarchy
	 */
	public static Boolean isInHierarchy(Location location, Location root) {
		if (location == null || root == null)
			return false;
		if (root.equals(location))
			return true;
		if (root.getChildLocations() != null) {
			for (Location l : root.getChildLocations())
				return isInHierarchy(location, l);
		}
		
		return false;
	}
	
	/**
	 * @param child The child location to remove.
	 * @since 1.5
	 */
	public void removeChildLocation(Location child) {
		if (getChildLocations() != null)
			childLocations.remove(child);
	}
	
	/**
	 * @return Returns the tags which have been attached to this Location.
	 * @since 1.5
	 */
	public Set<LocationTag> getTags() {
		return tags;
	}
	
	/**
	 * Set the tags which are attached to this Location.
	 * 
	 * @param tags The tags to set.
	 * @since 1.5
	 */
	public void setTags(Set<LocationTag> tags) {
		this.tags = tags;
	}
	
	/**
	 * Attaches a tag to the Location.
	 * 
	 * @param tag The tag to add.
	 * @since 1.5
	 */
	public void addTag(LocationTag tag) {
		if (getTags() == null)
			tags = new HashSet<LocationTag>();
		if (tag != null && !tags.contains(tag))
			tags.add(tag);
	}
	
	/**
	 * Remove the tag from the Location.
	 * 
	 * @param tag The tag to remove.
	 * @since 1.5
	 */
	public void removeTag(LocationTag tag) {
		if (getTags() != null)
			tags.remove(tag);
	}
	
	/**
	 * Checks whether the Location has a particular tag.
	 * 
	 * @param tagToFind the string of the tag for which to check
	 * @return true if the tags include the specified tag, false otherwise
	 * @since 1.5
	 * @should not fail given null parameter
	 * @should return false given empty string parameter
	 */
	public Boolean hasTag(String tagToFind) {
		if (tagToFind != null && getTags() != null) {
			for (LocationTag locTag : getTags()) {
				if (locTag.getName().equals(tagToFind)) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#getId()
	 */
	public Integer getId() {
		
		return getLocationId();
	}
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 */
	public void setId(Integer id) {
		setLocationId(id);
		
	}
	
}
