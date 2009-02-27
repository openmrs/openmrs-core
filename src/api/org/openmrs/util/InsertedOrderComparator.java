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

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * This class is intended to be used with either SortedSets or SortedMaps. The keys will be sorted
 * on the order which they are inserted (and hence compared). Caveat: If an item is removed and
 * readded, it will retain the old order
 * 
 * @author bwolfe
 */
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
