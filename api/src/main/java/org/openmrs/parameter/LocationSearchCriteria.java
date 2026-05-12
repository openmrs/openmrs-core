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

import java.util.Collection;
import java.util.Collections;

/**
 * Encapsulates the criteria used for searching locations. All provided criteria are ANDed together.
 * <p>
 * Instead of calling the constructor directly, use {@link LocationSearchCriteriaBuilder}.
 *
 * @since 2.9.0
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

	private final String descendantOfLocationUuid;

	private final Collection<String> locationTagUuids;

	private final TagMatchMode tagMatchMode;

	private final String nameFragment;

	private final boolean includeRetired;

	/**
	 * Constructs a LocationSearchCriteria. Use {@link LocationSearchCriteriaBuilder} instead.
	 *
	 * @param descendantOfLocationUuid UUID of the ancestor location; only its descendants are returned
	 *            (root excluded)
	 * @param locationTagUuids UUIDs of tags to filter by
	 * @param tagMatchMode whether all or any of the tags must match
	 * @param nameFragment only return locations whose name starts with this fragment
	 * @param includeRetired whether to include retired locations
	 */
	public LocationSearchCriteria(String descendantOfLocationUuid, Collection<String> locationTagUuids,
	    TagMatchMode tagMatchMode, String nameFragment, boolean includeRetired) {
		this.descendantOfLocationUuid = descendantOfLocationUuid;
		this.locationTagUuids = locationTagUuids == null ? Collections.emptyList()
		        : Collections.unmodifiableCollection(locationTagUuids);
		this.tagMatchMode = tagMatchMode;
		this.nameFragment = nameFragment;
		this.includeRetired = includeRetired;
	}

	/**
	 * @return UUID of the ancestor location; only descendants of that location are returned (root
	 *         excluded)
	 */
	public String getDescendantOfLocationUuid() {
		return descendantOfLocationUuid;
	}

	/**
	 * @return UUIDs of the location tags to filter by
	 */
	public Collection<String> getLocationTagUuids() {
		return locationTagUuids;
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
}
