/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.annotation.Independent;

/**
 * A Location is a physical place, such as a hospital, a room, a clinic, or a district. Locations
 * support a single hierarchy, such that each location may have one parent location. A
 * non-geographical grouping of locations, such as "All Community Health Centers" is not a location,
 * and should be modeled using {@link LocationTag}s.
 * Note: Prior to version 1.9 this class extended BaseMetadata
 */
public class Location extends BaseCustomizableMetadata<LocationAttribute> implements java.io.Serializable, Attributable<Location>, Address {
	
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
	
	private String address3;
	
	private String address4;
	
	private String address6;
	
	private String address5;
	
	private Location parentLocation;
	
	private Set<Location> childLocations;
	
	@Independent
	private Set<LocationTag> tags;
	
	// Constructors
	
	/** default constructor */
	public Location() {
	}
	
	/** constructor with id */
	public Location(Integer locationId) {
		this.locationId = locationId;
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
	
	@Override
	public String toString() {
		if (getName() != null) {
			return getName();
		}
		if (getId() != null) {
			return getId().toString();
		}
		return "";
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
	 * @deprecated As of 1.8, replaced by {@link #getAddress3()}
	 * @return Returns the neighborhoodCell.
	 */
	@Deprecated
	public String getNeighborhoodCell() {
		return getAddress3();
	}
	
	/**
	 * @deprecated As of 1.8, replaced by {@link #setAddress3(String)}
	 * @param address3 The neighborhoodCell to set.
	 */
	@Deprecated
	public void setNeighborhoodCell(String address3) {
		this.setAddress3(address3);
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
		if (getLocationId() != null) {
			return "" + getLocationId();
		} else {
			return "";
		}
	}
	
	/**
	 * @see org.openmrs.Attributable#getDisplayString()
	 */
	public String getDisplayString() {
		return getName();
	}
	
	/**
	 * @deprecated As of 1.8, replaced by {@link #getAddress6()}
	 * @return the region
	 */
	@Deprecated
	public String getRegion() {
		return getAddress6();
	}
	
	/**
	 * @deprecated As of 1.8, replaced by {@link #setAddress6(String)}
	 * @param address6 the region to set
	 */
	@Deprecated
	public void setRegion(String address6) {
		this.setAddress6(address6);
	}
	
	/**
	 * @deprecated As of 1.8, replaced by {@link #getAddress5()}
	 * @return the subregion
	 */
	@Deprecated
	public String getSubregion() {
		return getAddress5();
	}
	
	/**
	 * @deprecated As of 1.8, replaced by {@link #setAddress5(String)}
	 * @param address5 the subregion to set
	 */
	@Deprecated
	public void setSubregion(String address5) {
		this.setAddress5(address5);
	}
	
	/**
	 * @deprecated As of 1.8, replaced by {@link #getAddress4()}
	 * @return the townshipDivision
	 */
	@Deprecated
	public String getTownshipDivision() {
		return getAddress4();
	}
	
	/**
	 * @deprecated As of 1.8, replaced by {@link #setAddress4(String)}
	 * @param address4 the townshipDivision to set
	 */
	@Deprecated
	public void setTownshipDivision(String address4) {
		this.setAddress4(address4);
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
		if (includeRetired) {
			ret = getChildLocations();
		} else if (getChildLocations() != null) {
			for (Location l : getChildLocations()) {
				if (!l.isRetired()) {
					ret.add(l);
				}
			}
		}
		return ret;
	}
	
	/**
	 * Returns the descendant locations.
	 *
	 * @param includeRetired specifies whether or not to include voided childLocations
	 * @return Returns a Set<Location> of the descendant location.
	 * @since 1.10
	 */
	public Set<Location> getDescendantLocations(boolean includeRetired) {
		Set<Location> result = new HashSet<Location>();
		
		for (Location childLocation : getChildLocations()) {
			if (!childLocation.isRetired() || includeRetired) {
				result.add(childLocation);
				result.addAll(childLocation.getDescendantLocations(includeRetired));
			}
		}
		return result;
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
		if (child == null) {
			return;
		}
		
		if (getChildLocations() == null) {
			childLocations = new HashSet<Location>();
		}
		
		if (child.equals(this)) {
			throw new APIException("Location.cannot.be.its.own.child", (Object[]) null);
		}
		
		// Traverse all the way up (down?) to the root, then check whether the child is already
		// anywhere in the tree
		Location root = this;
		while (root.getParentLocation() != null) {
			root = root.getParentLocation();
		}
		
		if (isInHierarchy(child, root)) {
			throw new APIException("Location.hierarchy.loop", new Object[] { child, this });
		}
		
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
		if (root == null) {
			return false;
		}
		while (true) {
			if (location == null) {
				return false;
			} else if (root.equals(location)) {
				return true;
			}
			location = location.getParentLocation();
		}
	}
	
	/**
	 * @param child The child location to remove.
	 * @since 1.5
	 */
	public void removeChildLocation(Location child) {
		if (getChildLocations() != null) {
			childLocations.remove(child);
		}
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
		if (getTags() == null) {
			tags = new HashSet<LocationTag>();
		}
		if (tag != null && !tags.contains(tag)) {
			tags.add(tag);
		}
	}
	
	/**
	 * Remove the tag from the Location.
	 *
	 * @param tag The tag to remove.
	 * @since 1.5
	 */
	public void removeTag(LocationTag tag) {
		if (getTags() != null) {
			tags.remove(tag);
		}
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
	 * @since 1.8
	 * @return the address3
	 */
	public String getAddress3() {
		return address3;
	}
	
	/**
	 * @since 1.8
	 * @param address3 the address3 to set
	 */
	public void setAddress3(String address3) {
		this.address3 = address3;
	}
	
	/**
	 * @since 1.8
	 * @return the address4
	 */
	public String getAddress4() {
		return address4;
	}
	
	/**
	 * @since 1.8
	 * @param address4 the address4 to set
	 */
	public void setAddress4(String address4) {
		this.address4 = address4;
	}
	
	/**
	 * @since 1.8
	 * @return the address6
	 */
	public String getAddress6() {
		return address6;
	}
	
	/**
	 * @since 1.8
	 * @param address6 the address6 to set
	 */
	public void setAddress6(String address6) {
		this.address6 = address6;
	}
	
	/**
	 * @since 1.8
	 * @return the address5
	 */
	public String getAddress5() {
		return address5;
	}
	
	/**
	 * @since 1.8
	 * @param address5 the address5 to set
	 */
	public void setAddress5(String address5) {
		this.address5 = address5;
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
