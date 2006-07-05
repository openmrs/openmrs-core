package org.openmrs.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

public class OpenmrsConstants {
	
	public static final String OPENMRS_VERSION = "@OPENMRS.VERSION@";
	public static final String DATABASE_VERSION_EXPECTED = "@DATABASE.VERSION.EXPECTED@";
	public static String DATABASE_VERSION = "";	// loaded from (Hibernate)Util.checkDatabaseVersion
	public static String DATABASE_NAME = "openmrs";
	public static String DATABASE_BUSINESS_NAME = "openmrs";

	// Set true from runtime configuration to obscure patients for system demonstrations 
	public static boolean OBSCURE_PATIENTS = false;
	public static String OBSCURE_PATIENTS_GIVEN_NAME = "Demo";
	public static String OBSCURE_PATIENTS_MIDDLE_NAME = null;
	public static String OBSCURE_PATIENTS_FAMILY_NAME = "Patient";

	public static final String REGEX_LARGE = "[!\"#\\$%&'\\(\\)\\*,+-\\./:;<=>\\?@\\[\\\\\\\\\\]^_`{\\|}~]";
	public static final String REGEX_SMALL = "[!\"#\\$%&'\\(\\)\\*,\\./:;<=>\\?@\\[\\\\\\\\\\]^_`{\\|}~]";
	
	public static final Integer CIVIL_STATUS_CONCEPT_ID = 1054;
	
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
	
	public static final String PRIV_VIEW_REPORT_OBJECTS		= "View Report Objects";
	public static final String PRIV_ADD_REPORT_OBJECTS		= "Add Report Objects";
	public static final String PRIV_EDIT_REPORT_OBJECTS		= "Edit Report Objects";
	public static final String PRIV_DELETE_REPORT_OBJECTS	= "Delete Report Objects";

	public static final String PRIV_MANAGE_TRIBES			= "Manage Tribes";
	public static final String PRIV_MANAGE_RELATIONSHIPS	= "Manage Relationships";
	public static final String PRIV_MANAGE_IDENTIFIER_TYPES	= "Manage Identifier Types";
	public static final String PRIV_MANAGE_LOCATIONS		= "Manage Locations";
	public static final String PRIV_MANAGE_MIME_TYPES		= "Manage Mime Types";
	public static final String PRIV_MANAGE_CONCEPT_CLASSES	= "Manage Concept Classes";
	public static final String PRIV_MANAGE_CONCEPT_DATATYPES= "Manage Concept Datatypes";
	public static final String PRIV_MANAGE_ENCOUNTER_TYPES	= "Manage Encounter Types";
	public static final String PRIV_MANAGE_PRIVILEGES		= "Manage Privileges";
	public static final String PRIV_MANAGE_ROLES			= "Manage Roles";
	public static final String PRIV_MANAGE_FIELD_TYPES		= "Manage Field Types";
	public static final String PRIV_MANAGE_ORDER_TYPES		= "Manage Order Types";
	public static final String PRIV_MANAGE_RELATIONSHIP_TYPES	= "Manage Relationship Types";
	
	public static final String PRIV_VIEW_NAVIGATION_MENU	= "View Navigation Menu";
	public static final String PRIV_VIEW_ADMIN_FUNCTIONS	= "View Administration Functions";
	
	public static final String PRIV_FORM_ENTRY      = "Form Entry";
	
	public static final Collection<String> CORE_PRIVILEGES() {
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
		privs.add(PRIV_MANAGE_RELATIONSHIPS);
		privs.add(PRIV_MANAGE_IDENTIFIER_TYPES);
		privs.add(PRIV_MANAGE_LOCATIONS);
		privs.add(PRIV_MANAGE_MIME_TYPES);
		privs.add(PRIV_MANAGE_CONCEPT_CLASSES);
		privs.add(PRIV_MANAGE_CONCEPT_DATATYPES);
		privs.add(PRIV_MANAGE_ENCOUNTER_TYPES);
		privs.add(PRIV_MANAGE_PRIVILEGES);
		privs.add(PRIV_MANAGE_FIELD_TYPES);
		privs.add(PRIV_MANAGE_ORDER_TYPES);
		privs.add(PRIV_MANAGE_RELATIONSHIP_TYPES);
		
		privs.add(PRIV_VIEW_NAVIGATION_MENU);
		privs.add(PRIV_VIEW_ADMIN_FUNCTIONS);
		
		return privs;
	}
	
	// Baked in Roles:
	public static final String SUPERUSER_ROLE = "System Developer";
	public static final String ANONYMOUS_ROLE = "Anonymous";
	public static final String AUTHENTICATED_ROLE = "Authenticated";
	
	public static final Collection<String> CORE_ROLES() {
		List<String> roles = new Vector<String>();
		
		roles.add(SUPERUSER_ROLE);
		roles.add(ANONYMOUS_ROLE);
		roles.add(AUTHENTICATED_ROLE);
		
		return roles;
	}
	
	// These roles are given to a user automatically and cannot be assigned
	public static final Collection<String> AUTO_ROLES() {
		List<String> roles = new Vector<String>();
		
		roles.add(ANONYMOUS_ROLE);
		roles.add(AUTHENTICATED_ROLE);
		
		return roles;
	}
	
	// ConceptProposal proposed concept identifier keyword
	public static final String PROPOSED_CONCEPT_IDENTIFIER = "PROPOSED";
	
	// ConceptProposal states
	public static final String CONCEPT_PROPOSAL_UNMAPPED = "UNMAPPED";
	public static final String CONCEPT_PROPOSAL_CONCEPT  = "CONCEPT";
	public static final String CONCEPT_PROPOSAL_SYNONYM  = "SYNONYM";
	public static final String CONCEPT_PROPOSAL_REJECT   = "REJECT";
	
	public static final Collection<String> CONCEPT_PROPOSAL_STATES() {
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

	
	/**
	 * @return Collection of locales available to openmrs
	 */
	public static final Collection<Locale> OPENMRS_LOCALES() {
		List<Locale> languages = new Vector<Locale>();
		
		languages.add(Locale.US);
		languages.add(Locale.UK);
		languages.add(Locale.FRENCH);
		
		return languages;
	}
	
	public static final Locale GLOBAL_DEFAULT_LOCALE = Locale.US;

	/**
	 * @return Collection of locales that the concept dictionary should be aware of
	 */
	public static final Collection<Locale> OPENMRS_CONCEPT_LOCALES() {
		List<Locale> languages = new Vector<Locale>();
		
		languages.add(Locale.ENGLISH);
		languages.add(Locale.FRENCH);
		
		return languages;
	}
	
	/**
	 * This method is necessary until SimpleDateFormat(java.util.locale) returns a 
	 *   pattern with a four digit year
	 *   <locale.toString().toLowerCase(), pattern>
	 *   
	 * @return Mapping of Locales to locale specific date pattern
	 */
	public static final Map<String, String> OPENMRS_LOCALE_DATE_PATTERNS() {
		Map<String, String> patterns = new HashMap<String, String>();
		
		patterns.put(Locale.US.toString().toLowerCase(), "MM/dd/yyyy");
		patterns.put(Locale.UK.toString().toLowerCase(), "dd/MM/yyyy");
		patterns.put(Locale.FRENCH.toString().toLowerCase(), "dd/MM/yyyy");
		patterns.put(Locale.GERMAN.toString().toLowerCase(), "MM.dd.yyyy");
		
		return patterns;
	}
	
	/*
	 * User properties 
	 */
	public static final String USER_PROPERTY_CHANGE_PASSWORD  		= "forcePassword";
	public static final String USER_PROPERTY_DEFAULT_LOCALE   		= "defaultLocale";
	public static final String USER_PROPERTY_DEFAULT_LOCATION 		= "defaultLocation";
	public static final String USER_PROPERTY_SHOW_RETIRED 			= "showRetired";
	public static final String USER_PROPERTY_SHOW_VERBOSE 			= "showVerbose";
	public static final String USER_PROPERTY_NOTIFICATION 			= "notification";
	public static final String USER_PROPERTY_NOTIFICATION_ADDRESS 	= "notificationAddress";
	public static final String USER_PROPERTY_NOTIFICATION_FORMAT 	= "notificationFormat";		// text/plain, text/html
	
	/*
	 * Report object properties
	 */
	public static final String REPORT_OBJECT_TYPE_PATIENTFILTER 		= "Patient Filter";
	public static final String REPORT_OBJECT_TYPE_PATIENTDATAPRODUCER 	= "Patient Data Producer";


	/*
	 * Mail session properties -- moved from context.xml because we want to avoid using JNDI.   
	 */

	// Mail property names
	public static final String MAIL_TRANSPORT_PROTOCOL_PROPERTY = "mail.transport.protocol";
	public static final String MAIL_SMTP_HOST_PROPERTY 			= "mail.smtp.host";
	public static final String MAIL_SMTP_PORT_PROPERTY 			= "mail.smtp.port";
	public static final String MAIL_FROM_PROPERTY 				= "mail.from";
	public static final String MAIL_DEBUG_PROPERTY 				= "mail.debug";
	public static final String MAIL_SMTP_AUTH_PROPERTY 			= "mail.smtp.auth";
	// Mail property values
	public static final String MAIL_TRANSPORT_PROTOCOL 	= "@MAIL.TRANSPORT.PROTOCOL@";
	public static final String MAIL_SMTP_HOST			= "@MAIL.SMTP.HOST@";
	public static final String MAIL_SMTP_PORT 			= "@MAIL.SMTP.PORT@";
	public static final String MAIL_FROM 				= "@MAIL.FROM@";
	public static final String MAIL_DEBUG 				= "@MAIL.DEBUG@";
	public static final String MAIL_SMTP_AUTH 			= "@MAIL.SMTP.AUTH@";
	public static final String MAIL_USER 				= "@MAIL.USER@";
	public static final String MAIL_PASSWORD	 		= "@MAIL.PASSWORD@";
	public static final String DEFAULT_CONTENT_TYPE 	= "@MAIL.FORMAT@";
	
	
	public static Properties MAIL_PROPERTIES = new Properties();
	
	static { 
		MAIL_PROPERTIES.setProperty(OpenmrsConstants.MAIL_TRANSPORT_PROTOCOL_PROPERTY, OpenmrsConstants.MAIL_TRANSPORT_PROTOCOL);
		MAIL_PROPERTIES.setProperty(OpenmrsConstants.MAIL_SMTP_HOST_PROPERTY, OpenmrsConstants.MAIL_SMTP_HOST);
		MAIL_PROPERTIES.setProperty(OpenmrsConstants.MAIL_SMTP_PORT_PROPERTY, OpenmrsConstants.MAIL_SMTP_PORT);
		MAIL_PROPERTIES.setProperty(OpenmrsConstants.MAIL_FROM_PROPERTY, OpenmrsConstants.MAIL_FROM);
		MAIL_PROPERTIES.setProperty(OpenmrsConstants.MAIL_DEBUG_PROPERTY, OpenmrsConstants.MAIL_DEBUG);
		MAIL_PROPERTIES.setProperty(OpenmrsConstants.MAIL_SMTP_AUTH_PROPERTY, OpenmrsConstants.MAIL_SMTP_AUTH);
	}	

	
}