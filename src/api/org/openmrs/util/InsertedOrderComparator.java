package org.openmrs.util;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * This class is intended to be used with either SortedSets or SortedMaps.  The keys will be 
 * sorted on the order which they are inserted (and hence compared).
 * 
 * Caveat: If an item is removed and readded, it will retain the old order
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
