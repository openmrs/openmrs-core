/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.util;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * This class is intended to be used with either SortedSets or SortedMaps. The keys will be sorted
 * on the order which they are inserted (and hence compared). Caveat: If an item is removed and
 * readded, it will retain the old order
 * 
 * @deprecated this is not needed. Use a LinkedHashSet object instead of this class.
 */
@Deprecated
public class InsertedOrderComparator implements Comparator<String> {
	
	Map<String, Integer> insertedOrder = new HashMap<String, Integer>();
	
	public int compare(String a1, String a2) {
		Integer a1Order = insertedOrder.get(a1);
		Integer a2Order = insertedOrder.get(a2);
		
		if (a2Order == null) {
			a2Order = insertedOrder.size();
			insertedOrder.put(a2, a2Order);
		}
		if (a1Order == null) {
			a1Order = insertedOrder.size();
			insertedOrder.put(a1, a1Order);
		}
		
		return a1Order.compareTo(a2Order);
	}
}
