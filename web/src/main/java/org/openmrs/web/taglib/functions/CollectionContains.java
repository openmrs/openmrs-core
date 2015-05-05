/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.taglib.functions;

import java.util.Collection;

/**
 * Function for openmrs tag library to determine whether a collection contains an object
 */
public class CollectionContains {
	
	/**
	 * Returns true if collection is not null and contains obj
	 *
	 * @param collection
	 * @param obj
	 * @return whether collection contains obj
	 */
	public static Boolean collectionContains(Collection<?> collection, Object obj) {
		if (collection == null) {
			return false;
		} else {
			return collection.contains(obj);
		}
	}
	
}
