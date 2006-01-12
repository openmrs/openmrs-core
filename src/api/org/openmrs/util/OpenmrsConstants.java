package org.openmrs.util;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class OpenmrsConstants {

	public static final String OPENMRS_REGEX_LARGE = "[!\"#\\$%&'\\(\\)\\*,+-\\./:;<=>\\?@\\[\\\\\\\\\\]^_`{\\|}~]";
	public static final String OPENMRS_REGEX_SMALL = "[!\"#\\$%&'\\(\\)\\*,\\./:;<=>\\?@\\[\\\\\\\\\\]^_`{\\|}~]";
	
	public static final Map<String, String> OPENMRS_CIVIL_STATUS() {
		HashMap<String, String> civilStatus = new HashMap<String, String>();
		civilStatus.put("1", "Single");
		civilStatus.put("2", "Married");
		civilStatus.put("3", "Divorced");
		civilStatus.put("4", "Widowed");
		
		return civilStatus;
	}
	
	// TODO put civilStatus in database ?
	public static final List<String> OPENMRS_STOP_WORDS() {
		List<String> stopWords = new Vector<String>();
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
		
		return stopWords;
	}
	
	// TODO issues with localization
	public static final Map<String, String> OPENMRS_GENDER() {
		Map<String, String> genders = new LinkedHashMap<String, String>();
		genders.put("", "Choose");
		genders.put("M", "Male");
		genders.put("F", "Female");
		return genders;
	}
	
	
	// Baked in Privileges:
	
	public static final String PRIV_VIEW_CONCEPTS   = "View Concepts";
	public static final String PRIV_EDIT_CONCEPTS   = "Edit Concepts";
	public static final String PRIV_DELETE_CONCEPTS = "Delete Concepts";
	
	public static final String PRIV_MANAGE_USERS    = "Manage Users";
	public static final String PRIV_MANAGE_PATIENTS = "Manage Patients";
	public static final String PRIV_MANAGE_REPORTS  = "Manage Reports";
	public static final String PRIV_FORM_ENTRY      = "Form Entry";
	public static final String PRIV_MANAGE_ORDERS   = "Manage Orders";
	public static final String PRIV_MANAGE_OBS      = "Manage Observations";
	public static final String PRIV_MANAGE_FORMS    = "Manage Forms";
	public static final String PRIV_MANAGE_ENC      = "Manage Encounters";
	
	public static List<String> OPENMRS_CORE_PRIVILEGES() {
		List<String> privs = new Vector<String>();
		
		privs.add(PRIV_VIEW_CONCEPTS);
		privs.add(PRIV_EDIT_CONCEPTS);
		privs.add(PRIV_DELETE_CONCEPTS);
		privs.add(PRIV_MANAGE_USERS);
		privs.add(PRIV_MANAGE_PATIENTS);
		privs.add(PRIV_MANAGE_REPORTS);
		privs.add(PRIV_FORM_ENTRY);
		privs.add(PRIV_MANAGE_ORDERS);
		privs.add(PRIV_MANAGE_OBS);
		privs.add(PRIV_MANAGE_FORMS);
		privs.add(PRIV_MANAGE_ENC);
		
		return privs;
	}
	
}