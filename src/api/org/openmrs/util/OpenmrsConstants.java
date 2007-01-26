package org.openmrs.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import org.openmrs.GlobalProperty;
import org.openmrs.Privilege;
import org.openmrs.module.ModuleFactory;
import org.openmrs.scheduler.SchedulerConstants;

public class OpenmrsConstants {
	
	public static final int ORDERTYPE_DRUG = 2;
	public static final int CONCEPT_CLASS_DRUG = 3;
	
	public static final String OPENMRS_VERSION = "@OPENMRS.VERSION.LONG@";
	public static final String OPENMRS_VERSION_SHORT = "@OPENMRS.VERSION.SHORT@";
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
	
	public static final String PRIV_ADD_CONCEPT_PROPOSAL  = "Add Concept Proposal";
	public static final String PRIV_EDIT_CONCEPT_PROPOSAL = "Edit Concept Proposal";
	
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
	public static final String PRIV_MANAGE_ORDERS			= "Manage Orders";
	public static final String PRIV_MANAGE_ORDER_TYPES		= "Manage Order Types";
	public static final String PRIV_MANAGE_RELATIONSHIP_TYPES	= "Manage Relationship Types";
	public static final String PRIV_MANAGE_ALERTS 				= "Manage Alerts";
	
	public static final String PRIV_VIEW_NAVIGATION_MENU	= "View Navigation Menu";
	public static final String PRIV_VIEW_ADMIN_FUNCTIONS	= "View Administration Functions";
	
	public static final String PRIV_VIEW_UNPUBLISHED_FORMS = "View Unpublished Forms";
	
	public static final String PRIV_VIEW_PROGRAMS = "View Programs";
	public static final String PRIV_MANAGE_PROGRAMS = "Manage Programs";
	public static final String PRIV_EDIT_PATIENT_PROGRAMS = "Edit Patient Programs";
	
	public static final String PRIV_DASHBOARD_OVERVIEW = "Patient Dashboard - View Overview Section";
	public static final String PRIV_DASHBOARD_REGIMEN = "Patient Dashboard - View Regimen Section";
	public static final String PRIV_DASHBOARD_ENCOUNTERS = "Patient Dashboard - View Encounters Section";
	public static final String PRIV_DASHBOARD_DEMOGRAPHICS = "Patient Dashboard - View Demographics Section";
	public static final String PRIV_DASHBOARD_GRAPHS = "Patient Dashboard - View Graphs Section";
	public static final String PRIV_DASHBOARD_FORMS = "Patient Dashboard - View Forms Section";
	public static final String PRIV_DASHBOARD_SUMMARY = "Patient Dashboard - View Patient Summary";
	
	public static final String PRIV_MANAGE_GLOBAL_PROPERTIES = "Manage Global Properties";
	public static final String PRIV_MANAGE_MODULES = "Manage Modules";
	
	public static final Map<String, String> CORE_PRIVILEGES() {
		Map<String, String> privs = new HashMap<String, String>();
		
		privs.put(PRIV_VIEW_PROGRAMS, "Able to view patient programs");
		privs.put(PRIV_MANAGE_PROGRAMS, "Able to add/view/delete patient programs");
		privs.put(PRIV_EDIT_PATIENT_PROGRAMS, "Able to edit patient programs");
		
		privs.put(PRIV_VIEW_UNPUBLISHED_FORMS, "Able to view and fill out unpublished forms");
		
		privs.put(PRIV_VIEW_CONCEPTS, "Able to view concept entries");
		privs.put(PRIV_ADD_CONCEPTS, "Able to add concepts to the dictionary");
		privs.put(PRIV_EDIT_CONCEPTS, "Able to edit concepts in the dictionary");
		privs.put(PRIV_DELETE_CONCEPTS, "Able to delete concepts from the dictionary");
		
		privs.put(PRIV_ADD_CONCEPT_PROPOSAL, "Able to add concept proposals to the system");
		privs.put(PRIV_EDIT_CONCEPT_PROPOSAL, "Able to edit concept proposals in the system");
		
		privs.put(PRIV_VIEW_USERS, "Able to view users in OpenMRS");
		privs.put(PRIV_ADD_USERS, "Able to add users to OpenMRS");
		privs.put(PRIV_EDIT_USERS, "Able to edit users in OpenMRS");
		privs.put(PRIV_DELETE_USERS, "Able to delete users in OpenMRS");
		
		privs.put(PRIV_VIEW_ENCOUNTERS, "Able to view patient encounters");
		privs.put(PRIV_ADD_ENCOUNTERS, "Able to add patient encounters");
		privs.put(PRIV_EDIT_ENCOUNTERS, "Able to edit patient encounters");
		privs.put(PRIV_DELETE_ENCOUNTERS, "Able to delete patient encounters");
		
		privs.put(PRIV_VIEW_OBS, "Able to view patient observations");
		privs.put(PRIV_ADD_OBS, "Able to add patient observations");
		privs.put(PRIV_EDIT_OBS, "Able to edit patient observations");
		privs.put(PRIV_DELETE_OBS, "Able to delete patient observations");
		
		privs.put(PRIV_VIEW_PATIENTS, "Able to view patients");
		privs.put(PRIV_ADD_PATIENTS, "Able to add patients");
		privs.put(PRIV_EDIT_PATIENTS, "Able to edit patients");
		privs.put(PRIV_DELETE_PATIENTS, "Able to delete patients");
		
		privs.put(PRIV_VIEW_PATIENT_SETS, "Able to view patient sets");
		
		privs.put(PRIV_VIEW_ORDERS, "Able to view orders");
		privs.put(PRIV_ADD_ORDERS, "Able to add orders");
		privs.put(PRIV_EDIT_ORDERS, "Able to edit orders");
		privs.put(PRIV_DELETE_ORDERS, "Able to delete orders");
		
		privs.put(PRIV_VIEW_FORMS, "Able to view forms");
		privs.put(PRIV_ADD_FORMS, "Able to add forms");
		privs.put(PRIV_EDIT_FORMS, "Able to edit forms");
		privs.put(PRIV_DELETE_FORMS, "Able to delete forms");
		
		privs.put(PRIV_VIEW_REPORTS, "Able to view reports");
		privs.put(PRIV_ADD_REPORTS, "Able to add reports");
		privs.put(PRIV_EDIT_REPORTS, "Able to edit reports");
		privs.put(PRIV_DELETE_REPORTS, "Able to delete reports");
		
		privs.put(PRIV_MANAGE_TRIBES, "Able to add/edit/delete tribes");
		privs.put(PRIV_MANAGE_RELATIONSHIPS, "Able to add/edit/delete relationships");
		privs.put(PRIV_MANAGE_IDENTIFIER_TYPES, "Able to add/edit/delete patient identifier types");
		privs.put(PRIV_MANAGE_LOCATIONS, "Able to add/edit/delete locations");
		privs.put(PRIV_MANAGE_MIME_TYPES, "Able to add/edit/delete obs mime types");
		privs.put(PRIV_MANAGE_CONCEPT_CLASSES, "Able to add/edit/delete concept classes");
		privs.put(PRIV_MANAGE_CONCEPT_DATATYPES, "Able to add/edit/delete concept datatypes");
		privs.put(PRIV_MANAGE_ENCOUNTER_TYPES, "Able to add/edit/delete encounter types");
		privs.put(PRIV_MANAGE_PRIVILEGES, "Able to add/edit/delete privileges");
		privs.put(PRIV_MANAGE_FIELD_TYPES, "Able to add/edit/delete field types");
		privs.put(PRIV_MANAGE_ORDER_TYPES, "Able to add/edit/delete order types");
		privs.put(PRIV_MANAGE_RELATIONSHIP_TYPES, "Able to add/edit/delete relationship types");
		privs.put(PRIV_MANAGE_ALERTS, "Able to add/edit/delete user alerts");
		
		privs.put(PRIV_VIEW_NAVIGATION_MENU, "Able to view the navigations menu (Home, View Patients, Dictionary, Administration, My Profile)");
		privs.put(PRIV_VIEW_ADMIN_FUNCTIONS, "Able to view the 'Administration' link in the navigation bar");
		
		privs.put(PRIV_DASHBOARD_OVERVIEW, "Able to view the 'Overview' tab on the patient dashboard");
		privs.put(PRIV_DASHBOARD_REGIMEN, "Able to view the 'Regimen' tab on the patient dashboard");
		privs.put(PRIV_DASHBOARD_ENCOUNTERS, "Able to view the 'Encounters' tab on the patient dashboard");
		privs.put(PRIV_DASHBOARD_DEMOGRAPHICS, "Able to view the 'Demographics' tab on the patient dashboard");
		privs.put(PRIV_DASHBOARD_GRAPHS, "Able to view the 'Graphs' tab on the patient dashboard");
		privs.put(PRIV_DASHBOARD_FORMS, "Able to view the 'Forms' tab on the patient dashboard");
		privs.put(PRIV_DASHBOARD_SUMMARY, "Able to view the 'Summary' tab on the patient dashboard");
		
		privs.put(PRIV_MANAGE_GLOBAL_PROPERTIES, "Able to add/edit/delete global properties");
		privs.put(PRIV_MANAGE_MODULES, "Able to add/remove modules to the system");
		
		for (Privilege privilege : ModuleFactory.getPrivileges()) {
			privs.put(privilege.getPrivilege(), privilege.getDescription());
		}
		
		return privs;
	}
	
	// Baked in Roles:
	public static final String SUPERUSER_ROLE = "System Developer";
	public static final String ANONYMOUS_ROLE = "Anonymous";
	public static final String AUTHENTICATED_ROLE = "Authenticated";
	
	public static final Map<String, String> CORE_ROLES() {
		Map<String, String> roles = new HashMap<String, String>();
		
		roles.put(SUPERUSER_ROLE, "Assigned to developers of OpenMRS. Gives additional access to change fundamental structure of the database model.");
		roles.put(ANONYMOUS_ROLE, "Privileges for non-authenticated users.");
		roles.put(AUTHENTICATED_ROLE, "Privileges gained once authentication has been established.");
		
		return roles;
	}
	
	// These roles are given to a user automatically and cannot be assigned
	public static final Collection<String> AUTO_ROLES() {
		List<String> roles = new Vector<String>();
		
		roles.add(ANONYMOUS_ROLE);
		roles.add(AUTHENTICATED_ROLE);
		
		return roles;
	}
	
	// These properties (and default values) are set if not found in the database on startup
	public static final Map<String, String> CORE_GLOBAL_PROPERTIES() {
		Map<String, String> props = new HashMap<String, String>();
		
		props.put("use_patient_attribute.tribe", "true");
		props.put("use_patient_attribute.healthCenter", "false");
		props.put("use_patient_attribute.mothersName", "false");
		props.put("new_patient_form.showRelationships", "false");
		props.put("dashboard.overview.showConcepts", "");
		props.put("dashboard.encounters.viewWhere", "newWindow"); // known values: 'sameWindow', 'newWindow', 'oneNewWindow'
		props.put("dashboard.encounters.showEmptyFields", "true");
		props.put("dashboard.encounters.usePages", "smart"); // known values: 'true', 'false', 'smart'
		props.put("dashboard.encounters.showViewLink", "true");
		props.put("dashboard.encounters.showEditLink", "true");
		
		props.put("concept.weight", "5089");
		props.put("concept.cd4_count", "5497");
		
		props.put("mail.transport_protocol", "smtp");
		props.put("mail.smtp_host", "localhost");
		props.put("mail.smtp_port", "25");
		props.put("mail.from", "info@openmrs.org");
		props.put("mail.debug", "false");
		props.put("mail.smtp_auth", "false");
		props.put("mail.user", "test");
		props.put("mail.password", "test");
		props.put("mail.default_content_type", "text/plain");
		
		props.put("module_repository_folder", "modules");
		
		props.put("address.format", "kenya");
		
		// TODO should be changed to text defaults and constants should be removed
		props.put("scheduler.username", SchedulerConstants.SCHEDULER_USERNAME);
		props.put("scheduler.password", SchedulerConstants.SCHEDULER_PASSWORD);
		
		for (GlobalProperty gp : ModuleFactory.getGlobalProperties()) {
			props.put(gp.getProperty(), gp.getPropertyValue());
		}
		
		return props;
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
	
	// Used for differences between windows/linux upload capabilities)
	// Used for determining where to find runtime properties
	public static String OPERATING_SYSTEM_KEY = "os.name";
	public static String OPERATING_SYSTEM = System.getProperty(OPERATING_SYSTEM_KEY);
	public static String OPERATING_SYSTEM_WINDOWS_XP = "Windows XP";
	public static String OPERATING_SYSTEM_LINUX = "Linux";

	
}