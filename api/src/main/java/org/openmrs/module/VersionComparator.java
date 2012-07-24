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
package org.openmrs.module;

import java.util.Comparator;

/**
 * A comparator which takes version numbers stored as string. It assumes the versions
 * are separated by periods and does not contain letters. This prevents the version 
 * number 10 from coming before version number 9.
 *
 */
public class VersionComparator implements Comparator<String> {
	
	String TOKEN = ".";
	
	/**
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 * @should compare via numeric value not string value
	 */
	@Override
	public int compare(String o1, String o2) {
		return ModuleUtil.compareVersion(o1, o2);
	}
	
}
