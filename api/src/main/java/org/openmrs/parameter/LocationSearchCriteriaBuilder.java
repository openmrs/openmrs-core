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

import java.util.ArrayList;
import java.util.Collection;

import org.openmrs.Location;
import org.openmrs.LocationTag;

/**
 * A builder for constructing {@link LocationSearchCriteria} instances.
 *
 * @since 2.8.7
 */
public class LocationSearchCriteriaBuilder {

	private Location descendantOfLocation;

	private Collection<LocationTag> locationTags = new ArrayList<>();

	private LocationSearchCriteria.TagMatchMode tagMatchMode = LocationSearchCriteria.TagMatchMode.ALL;

	private String nameFragment;

	private boolean includeRetired = false;

	/**
	 * @param location the ancestor location; only its descendants will be returned (root excluded)
	 * @return this builder
	 */
	public LocationSearchCriteriaBuilder setDescendantOfLocation(Location location) {
		this.descendantOfLocation = location;
		return this;
	}

	/**
	 * @param tag a tag to filter by
	 * @return this builder
	 */
	public LocationSearchCriteriaBuilder addTag(LocationTag tag) {
		this.locationTags.add(tag);
		return this;
	}

	/**
	 * @param tags tags to filter by
	 * @return this builder
	 */
	public LocationSearchCriteriaBuilder addTags(Collection<LocationTag> tags) {
		this.locationTags.addAll(tags);
		return this;
	}

	/**
	 * @param tagMatchMode whether all or any tags must match; defaults to ALL
	 * @return this builder
	 */
	public LocationSearchCriteriaBuilder setTagMatchMode(LocationSearchCriteria.TagMatchMode tagMatchMode) {
		this.tagMatchMode = tagMatchMode;
		return this;
	}

	/**
	 * @param nameFragment only return locations whose name starts with this value
	 * @return this builder
	 */
	public LocationSearchCriteriaBuilder setNameFragment(String nameFragment) {
		this.nameFragment = nameFragment;
		return this;
	}

	/**
	 * @param includeRetired whether to include retired locations; defaults to false
	 * @return this builder
	 */
	public LocationSearchCriteriaBuilder includeRetired(boolean includeRetired) {
		this.includeRetired = includeRetired;
		return this;
	}

	/**
	 * @return a new {@link LocationSearchCriteria} from the current builder state
	 */
	public LocationSearchCriteria build() {
		return new LocationSearchCriteria(descendantOfLocation, locationTags, tagMatchMode, nameFragment, includeRetired);
	}
}
