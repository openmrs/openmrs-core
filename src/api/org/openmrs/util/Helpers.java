package org.openmrs.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class Helpers {

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
	
}
