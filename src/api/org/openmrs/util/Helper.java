package org.openmrs.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class Helper {

	// TODO put civilStatus in database ?
	private static HashMap<String, String> civilStatus = new HashMap<String, String>();
	private static List<String> stopWords = new Vector<String>();
	
	static {
		civilStatus.put("1", "Single");
		civilStatus.put("2", "Married");
		civilStatus.put("3", "Divorced");
		civilStatus.put("4", "Widowed");
		
		stopWords.add("A");
		stopWords.add("AND");
		stopWords.add("AT");
		stopWords.add("BUT");
		stopWords.add("BY");
		stopWords.add("FOR");
		stopWords.add("HAS");
		stopWords.add("OF");
		stopWords.add("THE");
		stopWords.add("TO");
	}
	
	public static final Map<String, String> OPENMRS_CIVIL_STATUS = civilStatus;
	
	public static final List<String> OPENMRS_STOP_WORDS = stopWords;
	
	public static final String OPENMRS_REGEX_LARGE = "[!\"#\\$%&'\\(\\)\\*,+-\\./:;<=>\\?@\\[\\\\\\\\\\]^_`{\\|}~]";
	public static final String OPENMRS_REGEX_SMALL = "[!\"#\\$%&'\\(\\)\\*,\\./:;<=>\\?@\\[\\\\\\\\\\]^_`{\\|}~]";
	
	public static int getCheckdigit(String idWithoutCheckdigit) throws Exception {

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
	
}
