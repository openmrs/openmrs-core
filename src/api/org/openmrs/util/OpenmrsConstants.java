package org.openmrs.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class OpenmrsConstants {

	public static final String REGEX_LARGE = "[!\"#\\$%&'\\(\\)\\*,+-\\./:;<=>\\?@\\[\\\\\\\\\\]^_`{\\|}~]";
	public static final String REGEX_SMALL = "[!\"#\\$%&'\\(\\)\\*,\\./:;<=>\\?@\\[\\\\\\\\\\]^_`{\\|}~]";
	
	public static final Map<String, String> CIVIL_STATUS() {
		HashMap<String, String> civilStatus = new HashMap<String, String>();
		civilStatus.put("1", "Single");
		civilStatus.put("2", "Married");
		civilStatus.put("3", "Divorced");
		civilStatus.put("4", "Widowed");
		
		return civilStatus;
	}
	
	// TODO put civilStatus in database ?
	public static final Collection<String> STOP_WORDS() {
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
	public static final Map<String, String> GENDER() {
		Map<String, String> genders = new LinkedHashMap<String, String>();
		genders.put("", "Choose");
		genders.put("M", "Male");
		genders.put("F", "Female");
		return genders;
	}
	
	
	// Baked in Privileges:
	
	public static final String PRIV_VIEW_CONCEPTS   = "View Concepts";
	public static final String PRIV_ADD_CONCEPTS	= "Add Concepts";
	public static final String PRIV_EDIT_CONCEPTS   = "Edit Concepts";
	public static final String PRIV_DELETE_CONCEPTS = "Delete Concepts";
	
	public static final String PRIV_VIEW_USERS	= "View Users";
	public static final String PRIV_ADD_USERS	= "Add Users";
	public static final String PRIV_EDIT_USERS	= "Edit Users";
	public static final String PRIV_DELETE_USERS= "Delete Users";

	public static final String PRIV_VIEW_ENCOUNTERS		= "View Encounters";
	public static final String PRIV_ADD_ENCOUNTERS		= "Add Encounters";
	public static final String PRIV_EDIT_ENCOUNTERS		= "Edit Encounters";
	public static final String PRIV_DELETE_ENCOUNTERS	= "Delete Encounters";

	public static final String PRIV_VIEW_OBS	= "View Observations";
	public static final String PRIV_ADD_OBS		= "Add Observations";
	public static final String PRIV_EDIT_OBS	= "Edit Observations";
	public static final String PRIV_DELETE_OBS	= "Delete Observations";

	public static final String PRIV_VIEW_PATIENTS   = "View Patients";
	public static final String PRIV_ADD_PATIENTS	= "Add Patients";
	public static final String PRIV_EDIT_PATIENTS   = "Edit Patients";
	public static final String PRIV_DELETE_PATIENTS = "Delete Patients";

	public static final String PRIV_VIEW_PATIENT_SETS = "View Patient Sets";
	
	public static final String PRIV_VIEW_ORDERS		= "View Orders";
	public static final String PRIV_ADD_ORDERS		= "Add Orders";
	public static final String PRIV_EDIT_ORDERS		= "Edit Orders";
	public static final String PRIV_DELETE_ORDERS	= "Delete Orders";

	public static final String PRIV_VIEW_FORMS		= "View Forms";
	public static final String PRIV_ADD_FORMS		= "Add Forms";
	public static final String PRIV_EDIT_FORMS		= "Edit Forms";
	public static final String PRIV_DELETE_FORMS	= "Delete Forms";
	
	public static final String PRIV_VIEW_REPORTS	= "View Reports";
	public static final String PRIV_ADD_REPORTS		= "Add Reports";
	public static final String PRIV_EDIT_REPORTS	= "Edit Reports";
	public static final String PRIV_DELETE_REPORTS	= "Delete Reports";
	
	public static final String PRIV_MANAGE_TRIBES			= "Manage Tribes";
	public static final String PRIV_MANAGE_IDENTIFIER_TYPES	= "Manage Identifier Types";
	public static final String PRIV_MANAGE_LOCATIONS		= "Manage Locations";
	public static final String PRIV_MANAGE_MIME_TYPES		= "Manage Mime Types";
	public static final String PRIV_MANAGE_CONCEPT_CLASSES	= "Manage Concept Classes";
	public static final String PRIV_MANAGE_CONCEPT_DATATYPES= "Manage Concept Datatypes";
	public static final String PRIV_MANAGE_ENCOUNTER_TYPES	= "Manage Encounter Types";
	public static final String PRIV_MANAGE_GROUPS		= "Manage Groups";
	public static final String PRIV_MANAGE_PRIVILEGES	= "Manage Privileges";
	public static final String PRIV_MANAGE_ROLES		= "Manage Roles";
	public static final String PRIV_MANAGE_FIELD_TYPES	= "Manage Field Types";
	public static final String PRIV_MANAGE_ORDER_TYPES	= "Manage Order Types";
	public static final String PRIV_MANAGE_RELATIONSHIP_TYPES	= "Manage Relationship Types";

	public static final String PRIV_FORM_ENTRY      = "Form Entry";
	
	public static Collection<String> CORE_PRIVILEGES() {
		List<String> privs = new Vector<String>();
		
		privs.add(PRIV_FORM_ENTRY);
		
		privs.add(PRIV_VIEW_CONCEPTS);
		privs.add(PRIV_ADD_CONCEPTS);
		privs.add(PRIV_EDIT_CONCEPTS);
		privs.add(PRIV_DELETE_CONCEPTS);
		
		privs.add(PRIV_VIEW_USERS);
		privs.add(PRIV_ADD_USERS);
		privs.add(PRIV_EDIT_USERS);
		privs.add(PRIV_DELETE_USERS);
		
		privs.add(PRIV_VIEW_ENCOUNTERS);
		privs.add(PRIV_ADD_ENCOUNTERS);
		privs.add(PRIV_EDIT_ENCOUNTERS);
		privs.add(PRIV_DELETE_ENCOUNTERS);
		
		privs.add(PRIV_VIEW_OBS);
		privs.add(PRIV_ADD_OBS);
		privs.add(PRIV_EDIT_OBS);
		privs.add(PRIV_DELETE_OBS);
		
		privs.add(PRIV_VIEW_PATIENTS);
		privs.add(PRIV_ADD_PATIENTS);
		privs.add(PRIV_EDIT_PATIENTS);
		privs.add(PRIV_DELETE_PATIENTS);
		
		privs.add(PRIV_VIEW_PATIENT_SETS);
		
		privs.add(PRIV_VIEW_ORDERS);
		privs.add(PRIV_ADD_ORDERS);
		privs.add(PRIV_EDIT_ORDERS);
		privs.add(PRIV_DELETE_ORDERS);
		
		privs.add(PRIV_VIEW_FORMS);
		privs.add(PRIV_ADD_FORMS);
		privs.add(PRIV_EDIT_FORMS);
		privs.add(PRIV_DELETE_FORMS);
		
		privs.add(PRIV_VIEW_REPORTS);
		privs.add(PRIV_ADD_REPORTS);
		privs.add(PRIV_EDIT_REPORTS);
		privs.add(PRIV_DELETE_REPORTS);
		
		privs.add(PRIV_MANAGE_TRIBES);
		privs.add(PRIV_MANAGE_IDENTIFIER_TYPES);
		privs.add(PRIV_MANAGE_LOCATIONS);
		privs.add(PRIV_MANAGE_MIME_TYPES);
		privs.add(PRIV_MANAGE_CONCEPT_CLASSES);
		privs.add(PRIV_MANAGE_CONCEPT_DATATYPES);
		privs.add(PRIV_MANAGE_ENCOUNTER_TYPES);
		privs.add(PRIV_MANAGE_GROUPS);
		privs.add(PRIV_MANAGE_PRIVILEGES);
		privs.add(PRIV_MANAGE_FIELD_TYPES);
		privs.add(PRIV_MANAGE_ORDER_TYPES);
		privs.add(PRIV_MANAGE_RELATIONSHIP_TYPES);
		
		return privs;
	}
	
	// Baked in Roles:
	public static final String SUPERUSER_ROLE = "System Developer";
	public static final String ANONYMOUS_ROLE = "Anonymous";
	public static final String AUTHENTICATED_ROLE = "Authenticated";
	
	public static Collection<String> CORE_ROLES() {
		List<String> roles = new Vector<String>();
		
		roles.add(SUPERUSER_ROLE);
		roles.add(ANONYMOUS_ROLE);
		roles.add(AUTHENTICATED_ROLE);
		
		return roles;
	}
	
	public static Collection<String> AUTO_ROLES() {
		List<String> roles = new Vector<String>();
		
		roles.add(ANONYMOUS_ROLE);
		roles.add(AUTHENTICATED_ROLE);
		
		return roles;
	}
	
	// ConceptProposal states
	public static final String CONCEPT_PROPOSAL_UNMAPPED = "UNMAPPED";
	public static final String CONCEPT_PROPOSAL_CONCEPT  = "CONCEPT";
	public static final String CONCEPT_PROPOSAL_SYNONYM  = "SYNONYM";
	public static final String CONCEPT_PROPOSAL_REJECT   = "REJECT";
	
	public static Collection<String> CONCEPT_PROPOSAL_STATES() {
		Collection<String> states = new Vector<String>();
		
		states.add(CONCEPT_PROPOSAL_UNMAPPED);
		states.add(CONCEPT_PROPOSAL_CONCEPT);
		states.add(CONCEPT_PROPOSAL_SYNONYM);
		states.add(CONCEPT_PROPOSAL_REJECT);
		
		return states;
	}
	
	// This regex is used in identifier pattern searches.  
	// @SEARCH@ is needs to be replaced with the searched string
	public static final String PATIENT_IDENTIFIER_REGEX = "^0*@SEARCH@([A-Z]+-[0-9])?$";
	
	public static final String USER_PROPERTY_CHANGE_PASSWORD = "forcePassword";

}