/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
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
