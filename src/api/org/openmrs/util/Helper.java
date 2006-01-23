package org.openmrs.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Helper {

	public static int getCheckDigit(String idWithoutCheckdigit) throws Exception {

		// allowable characters within identifier
		String validChars = "0123456789ABCDEFGHIJKLMNOPQRSTUVYWXZ_";
		
		// remove leading or trailing whitespace, convert to uppercase
		idWithoutCheckdigit = idWithoutCheckdigit.trim().toUpperCase();
		
		// this will be a running total
		int sum = 0;

	    // loop through digits from right to left
		for (int i = 0; i < idWithoutCheckdigit.length(); i++) {

			//set ch to "current" character to be processed
			char ch = idWithoutCheckdigit.charAt(idWithoutCheckdigit.length()
					- i - 1);

			// throw exception for invalid characters
			if (validChars.indexOf(ch) == -1)
				throw new Exception("\"" + ch + "\" is an invalid character");

			// our "digit" is calculated using ASCII value - 48
			int digit = (int) ch - 48;

			// weight will be the current digit's contribution to
			// the running total
			int weight;
			if (i % 2 == 0) {

				// for alternating digits starting with the rightmost, we
				// use our formula this is the same as multiplying x 2 and
				// adding digits together for values 0 to 9.  Using the 
				// following formula allows us to gracefully calculate a
				// weight for non-numeric "digits" as well (from their 
				// ASCII value - 48).
				weight = (2 * digit) - (int) (digit / 5) * 9;

			} else {

				// even-positioned digits just contribute their ascii
				// value minus 48
				weight = digit;

			}

			// keep a running total of weights
			sum += weight;

		}

		// avoid sum less than 10 (if characters below "0" allowed,
		// this could happen)
		sum = Math.abs(sum) + 10;

		// check digit is amount needed to reach next number
		// divisible by ten
		return (10 - (sum % 10)) % 10;

	}
	
	/**
	 * 
	 * @param id
	 * @return true/false whether id has a valid check digit
	 * @throws Exception on invalid characters and invalid id formation
	 */
	public static boolean isValidCheckDigit(String id) throws Exception {
		
		if (!id.matches("^[A-Za-z0-9_]+-[0-9]$")) {
			throw new Exception("Invalid characters and/or id formation");
		}
		
		String idWithoutCheckDigit = id.substring(0, id.indexOf("-"));
		
		int computedCheckDigit = getCheckDigit(idWithoutCheckDigit);
		
		int givenCheckDigit = Integer.valueOf(id.substring(id.indexOf("-")+1, id.length()));

		return (computedCheckDigit == givenCheckDigit);
	}
	
	/**
	 * Compares origList to newList returning map of differences 
	 * @param origList
	 * @param newList
	 * @return Map (List toAdd, List toDelete) with respect to origList
	 */
	public static Map compareLists(List origList, List newList) {
		//TODO finish function
		
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
