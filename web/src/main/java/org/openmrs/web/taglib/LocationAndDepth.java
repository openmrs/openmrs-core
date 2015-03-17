/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.taglib;

import org.openmrs.Location;

/**
 * This is helper bean use by {@link ForEachRecordTag} when fetching "locationHierarchy"
 * @since 1.9
 */
public class LocationAndDepth {
	
	private int depth;
	
	private Location location;
	
	public LocationAndDepth() {
	}
	
	/**
	 * @param depth
	 * @param location
	 */
	public LocationAndDepth(int depth, Location location) {
		this.depth = depth;
		this.location = location;
	}
	
	/**
	 * @return the depth
	 */
	public int getDepth() {
		return depth;
	}
	
	/**
	 * @param depth the depth to set
	 */
	public void setDepth(int depth) {
		this.depth = depth;
	}
	
	/**
	 * @return the location
	 */
	public Location getLocation() {
		return location;
	}
	
	/**
	 * @param location the location to set
	 */
	public void setLocation(Location location) {
		this.location = location;
	}
}
