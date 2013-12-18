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
package org.openmrs.util;

public class Version implements Comparable<Version> {
	
	private int major;
	
	private int minor;
	
	private int maintenance;
	
	public Version(String version) {
		String[] split = version.split("\\.");
		if (split.length > 2) {
			maintenance = parse(split[2]);
		}
		if (split.length > 1) {
			minor = parse(split[1]);
		}
		if (split.length > 0) {
			major = parse(split[0]);
		}
	}
	
	private Integer parse(String s) {
		try {
			return Integer.parseInt(s);
		}
		catch (NumberFormatException e) {
			return 0;
		}
	}
	
	public Integer getMajor() {
		return major;
	}
	
	public void setMajor(Integer major) {
		this.major = major;
	}
	
	public Integer getMinor() {
		return minor;
	}
	
	public void setMinor(Integer minor) {
		this.minor = minor;
	}
	
	public Integer getMaintenance() {
		return maintenance;
	}
	
	public void setMaintenance(Integer maintenance) {
		this.maintenance = maintenance;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + maintenance;
		result = prime * result + major;
		result = prime * result + minor;
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Version other = (Version) obj;
		if (maintenance != other.maintenance)
			return false;
		if (major != other.major)
			return false;
		if (minor != other.minor)
			return false;
		return true;
	}
	
	/**
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 * @should compare versions correctly
	 */
	@Override
	public int compareTo(Version o) {
		//perfect matches
		if (major == o.major && minor == o.minor && maintenance == o.maintenance) {
			return 0;
		}
		//different maintenance versions
		else if (major == o.major && minor == o.minor) {
			return compareInts(maintenance, o.maintenance);
		}
		//different minor versions
		else if (major == o.major) {
			return compareInts(minor, o.minor);
		}
		//different majors versions
		return compareInts(major, o.major);
	}
	
	/**
	 * Compares two integers
	 * 
	 * @param int1
	 * @param int2
	 * @return 0 if they are equal, -1 if int1 is less or 1 if int1 is greater
	 */
	private int compareInts(int int1, int int2) {
		return (int1 < int2 ? -1 : (int1 == int2 ? 0 : 1));
	}
}
