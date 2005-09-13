package org.openmrs.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Compare {

	/**
	 * Compares origList to newList returning map of differences 
	 * @param origList
	 * @param newList
	 * @return Map (List toAdd, List toDelete) with respect to origList
	 */
	public static Map compareLists(List origList, List newList) {
		
		HashMap map = new HashMap();
		
		List toAdd = new LinkedList();
		List toDel;
		
		//loop over the new list. 
		for(Iterator newListIter = newList.iterator(); newListIter.hasNext();) {
			Object currentNewListObj = newListIter.next();
			
			//loop over the original list
			boolean foundInList = false;
			for(Iterator origListIter = origList.iterator(); origListIter.hasNext();) {
				Object currentOrigListObj = origListIter.next();
				//checking if the current new list object is in the original list
				if (currentNewListObj.equals(currentOrigListObj)) {
					foundInList = true;
					origList.remove(currentOrigListObj);
					break;
				}
			}
			if (!foundInList)
				toAdd.add(currentNewListObj);
			
			// all found new objects were removed from the orig list, 
			//  leaving only objects needing to be removed
			toDel = origList;
			
		}
		
		
		return map;
	}
	
}
