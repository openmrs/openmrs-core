/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.parameter;

import java.util.HashSet;
import java.util.Set;

import org.openmrs.Location;
import org.openmrs.LocationTag;

/**
 * Encapsulates the criteria used for searching locations. All provided criteria are ANDed together.
 *
 * @since 2.8.7
 */
public class LocationSearchCriteria {

	/**
	 * Controls how multiple tags are matched against a location.
	 */
	public enum TagMatchMode {
		/** Location must have every tag in the collection. */
		ALL,
		/** Location must have at least one tag in the collection. */
		ANY
	}

	private Location descendantOfLocation;

	private Set<LocationTag> locationTags = new HashSet<>();

	private TagMatchMode tagMatchMode = TagMatchMode.ALL;

	private String nameFragment;

	private boolean includeRetired = false;

	private Integer startIndex;

	private Integer maxResults;

	/**
	 * @param descendantOfLocation the ancestor location; only its descendants are returned (root
	 *            excluded)
	 */
	public void setDescendantOfLocation(Location descendantOfLocation) {
		this.descendantOfLocation = descendantOfLocation;
	}

	/**
	 * @param locationTags tags to filter by
	 */
	public void setLocationTags(Set<LocationTag> locationTags) {
		this.locationTags = locationTags == null ? new HashSet<>() : locationTags;
	}

	/**
	 * @param tagMatchMode whether all or any of the tags must match
	 */
	public void setTagMatchMode(TagMatchMode tagMatchMode) {
		this.tagMatchMode = tagMatchMode;
	}

	/**
	 * @param nameFragment only return locations whose name starts with this fragment
	 */
	public void setNameFragment(String nameFragment) {
		this.nameFragment = nameFragment;
	}

	/**
	 * @param includeRetired whether to include retired locations
	 */
	public void setIncludeRetired(boolean includeRetired) {
		this.includeRetired = includeRetired;
	}

	/**
	 * @param startIndex zero-based index of the first result to return; null means no offset
	 */
	public void setStartIndex(Integer startIndex) {
		this.startIndex = startIndex;
	}

	/**
	 * @param maxResults maximum number of results to return; null means no limit
	 */
	public void setMaxResults(Integer maxResults) {
		this.maxResults = maxResults;
	}

	/**
	 * @return the ancestor location; only descendants of that location are returned (root excluded)
	 */
	public Location getDescendantOfLocation() {
		return descendantOfLocation;
	}

	/**
	 * @return the location tags to filter by
	 */
	public Set<LocationTag> getLocationTags() {
		return locationTags;
	}

	/**
	 * @return whether all or any of the tags must match
	 */
	public TagMatchMode getTagMatchMode() {
		return tagMatchMode;
	}

	/**
	 * @return the name fragment; locations whose name starts with this value are returned
	 */
	public String getNameFragment() {
		return nameFragment;
	}

	/**
	 * @return true if retired locations are included in the search results
	 */
	public boolean getIncludeRetired() {
		return includeRetired;
	}

	/**
	 * @return zero-based index of the first result to return, or null if not set
	 */
	public Integer getStartIndex() {
		return startIndex;
	}

	/**
	 * @return maximum number of results to return, or null if not set
	 */
	public Integer getMaxResults() {
		return maxResults;
	}
}
