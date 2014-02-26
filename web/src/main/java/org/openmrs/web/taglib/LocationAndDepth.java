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
