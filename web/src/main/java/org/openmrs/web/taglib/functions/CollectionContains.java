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
		if (collection == null)
			return false;
		else
			return collection.contains(obj);
	}
	
}
