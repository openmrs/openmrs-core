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

/**
 * A builder for constructing {@link LocationSearchCriteria} instances.
 *
 * @since 2.9.0
 */
public class LocationSearchCriteriaBuilder {

	private String descendantOfLocationUuid;

	private Collection<String> locationTagUuids = new ArrayList<>();

	private LocationSearchCriteria.TagMatchMode tagMatchMode = LocationSearchCriteria.TagMatchMode.ALL;

	private String nameFragment;

	private boolean includeRetired = false;

	/**
	 * @param uuid UUID of the ancestor location; only its descendants will be returned (root excluded)
	 * @return this builder
	 */
	public LocationSearchCriteriaBuilder setDescendantOfLocation(String uuid) {
		this.descendantOfLocationUuid = uuid;
		return this;
	}

	/**
	 * @param uuid UUID of a tag to filter by
	 * @return this builder
	 */
	public LocationSearchCriteriaBuilder addTag(String uuid) {
		this.locationTagUuids.add(uuid);
		return this;
	}

	/**
	 * @param uuids UUIDs of tags to filter by
	 * @return this builder
	 */
	public LocationSearchCriteriaBuilder addTags(Collection<String> uuids) {
		this.locationTagUuids.addAll(uuids);
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
		return new LocationSearchCriteria(descendantOfLocationUuid, locationTagUuids, tagMatchMode, nameFragment,
		        includeRetired);
	}
}
