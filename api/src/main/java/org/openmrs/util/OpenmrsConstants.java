/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.openmrs.GlobalProperty;
import org.openmrs.api.handler.ExistingVisitAssignmentHandler;
import org.openmrs.customdatatype.datatype.BooleanDatatype;
import org.openmrs.customdatatype.datatype.FreeTextDatatype;
import org.openmrs.hl7.HL7Constants;
import org.openmrs.module.ModuleConstants;
import org.openmrs.module.ModuleFactory;
import org.openmrs.patient.impl.LuhnIdentifierValidator;
import org.openmrs.scheduler.SchedulerConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Arrays.asList;

/**
 * Constants used in OpenMRS. Contents built from build properties (version, version_short, and
 * expected_database). Some are set at runtime (database, database version). This file should
 * contain all privilege names and global property names. Those strings added to the static CORE_*
 * methods will be written to the database at startup if they don't exist yet.
 */
public final class OpenmrsConstants {
	
	private static final Logger log = LoggerFactory.getLogger(OpenmrsConstants.class);
	
	public static String KEY_OPENMRS_APPLICATION_DATA_DIRECTORY = "OPENMRS_APPLICATION_DATA_DIRECTORY";
	
	/**
	 * This is the hard coded primary key of the concept class for DRUG. This has to be done because
	 * some logic in the API acts on this concept class
	 */
	public static final int CONCEPT_CLASS_DRUG = 3;
	
	/**
	 * hack alert: During an ant build, the openmrs api jar manifest file is loaded with these
	 * values. When constructing the OpenmrsConstants class file, the api jar is read and the values
	 * are copied in as constants
	 */
	private static final Package THIS_PACKAGE = OpenmrsConstants.class.getPackage();
	
	/**
	 * This holds the current openmrs code version. This version is a string containing spaces and
	 * words.<br>
	 * The format is:<br>
	 * <i>major</i>.<i>minor</i>.<i>maintenance</i> <i>suffix</i> Build <i>buildNumber</i>
	 */
	public static final String OPENMRS_VERSION = THIS_PACKAGE.getSpecificationVendor() != null ? THIS_PACKAGE
	        .getSpecificationVendor() : (getBuildVersion() != null ? getBuildVersion() : getVersion());
	
	/**
	 * This holds the current openmrs code version in a short space-less string.<br>
	 * The format is:<br>
	 * <i>major</i>.<i>minor</i>.<i>maintenance</i>.<i>revision</i>-<i>suffix</i>
	 */
	public static final String OPENMRS_VERSION_SHORT = THIS_PACKAGE.getSpecificationVersion() != null ? THIS_PACKAGE
	        .getSpecificationVersion() : (getBuildVersionShort() != null ? getBuildVersionShort() : getVersion());
	
	/**
	 * @return build version with alpha characters (eg:1.10.0 SNAPSHOT Build 24858)
	 * defined in MANIFEST.MF(specification-Vendor)
	 *
	 * @see #OPENMRS_VERSION_SHORT
	 * @see #OPENMRS_VERSION
	 */
	private static String getBuildVersion() {
		return getOpenmrsProperty("openmrs.version.long");
	}
	
	/**
	 * @return build version without alpha characters (eg: 1.10.0.24858)
	 * defined in MANIFEST.MF (specification-Version)
	 *
	 * @see #OPENMRS_VERSION_SHORT
	 * @see #OPENMRS_VERSION
	 */
	private static String getBuildVersionShort() {
		return getOpenmrsProperty("openmrs.version.short");
	}
	
	private static String getVersion() {
		return getOpenmrsProperty("openmrs.version");
	}
	
	public static String getOpenmrsProperty(String property) {
		InputStream file = OpenmrsConstants.class.getClassLoader().getResourceAsStream("org/openmrs/api/openmrs.properties");
		if (file == null) {
			log.error("Unable to find the openmrs.properties file");
			return null;
		}
		
		try {
			Properties props = new Properties();
			props.load(file);
			
			file.close();
			
			return props.getProperty(property);
		}
		catch (IOException e) {
			log.error("Unable to parse the openmrs.properties file", e);
		}
		finally {
			IOUtils.closeQuietly(file);
		}
		
		return null;
	}
		
	public static String DATABASE_NAME = "openmrs";
	
	public static String DATABASE_BUSINESS_NAME = "openmrs";
	
	/**
	 * Set true from runtime configuration to obscure patients for system demonstrations
	 */
	public static boolean OBSCURE_PATIENTS = false;
	
	public static String OBSCURE_PATIENTS_GIVEN_NAME = "Demo";
	
	public static String OBSCURE_PATIENTS_MIDDLE_NAME = null;
	
	public static String OBSCURE_PATIENTS_FAMILY_NAME = "Person";
	
	public static final String REGEX_LARGE = "[!\"#\\$%&'\\(\\)\\*,+-\\./:;<=>\\?@\\[\\\\\\\\\\]^_`{\\|}~]";
	
	public static final String REGEX_SMALL = "[!\"#\\$%&'\\(\\)\\*,\\./:;<=>\\?@\\[\\\\\\\\\\]^_`{\\|}~]";
	
	public static final Integer CIVIL_STATUS_CONCEPT_ID = 1054;
	
	/**
	 * The directory which OpenMRS should attempt to use as its application data directory
	 * in case the current users home dir is not writeable (e.g. when using application servers
	 * like tomcat to deploy OpenMRS).
	 *
	 * @see #APPLICATION_DATA_DIRECTORY_RUNTIME_PROPERTY
	 * @see OpenmrsUtil#getApplicationDataDirectory()
	 */
	public static final String APPLICATION_DATA_DIRECTORY_FALLBACK_UNIX = "/var/lib";
	
	public static final String APPLICATION_DATA_DIRECTORY_FALLBACK_WIN = System.getenv("appdata");
	
	/**
	 * The name of the runtime property that a user can set that will specify where openmrs's
	 * application directory is
	 * 
	 * @see OpenmrsUtil#getApplicationDataDirectory()
	 * @see OpenmrsUtil#startup(java.util.Properties)
	 */
	public static final String APPLICATION_DATA_DIRECTORY_RUNTIME_PROPERTY = "application_data_directory";
	
	/**
	 * The name of the runtime property that a user can set that will specify whether the database
	 * is automatically updated on startup
	 */
	public static final String AUTO_UPDATE_DATABASE_RUNTIME_PROPERTY = "auto_update_database";
	
	/**
	 * These words are ignored in concept and patient searches
	 * 
	 * @return Collection&lt;String&gt; of words that are ignored
	 */
	public static final Collection<String> STOP_WORDS() {
		List<String> stopWords = new ArrayList<>();
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
	
	/**
	 * A gender character to gender name map<br>
	 * TODO issues with localization. How should this be handled?
	 * @deprecated As of 2.2, replaced by {@link #GENDERS}
	 * 
	 * @return Map&lt;String, String&gt; of gender character to gender name
	 */
	@Deprecated
	@SuppressWarnings("squid:S00100")
	public static final Map<String, String> GENDER() {
		Map<String, String> genders = new LinkedHashMap<>();
		genders.put("M", "Male");
		genders.put("F", "Female");
		return genders;
	}
	
	/**
	 * A list of 1-letter strings representing genders
	 */
	public static final List<String> GENDERS = Collections.unmodifiableList(asList("M", "F"));
		
	/**
	 * These roles are given to a user automatically and cannot be assigned
	 * 
	 * @return <code>Collection&lt;String&gt;</code> of the auto-assigned roles
	 */
	public static final Collection<String> AUTO_ROLES() {
		List<String> roles = new ArrayList<>();
		
		roles.add(RoleConstants.ANONYMOUS);
		roles.add(RoleConstants.AUTHENTICATED);
		
		return roles;
	}
	
	public static final String GLOBAL_PROPERTY_DRUG_FREQUENCIES = "dashboard.regimen.displayFrequencies";
	
	public static final String GLOBAL_PROPERTY_CONCEPTS_LOCKED = "concepts.locked";
	
	public static final String GLOBAL_PROPERTY_PATIENT_LISTING_ATTRIBUTES = "patient.listingAttributeTypes";
	
	public static final String GLOBAL_PROPERTY_PATIENT_VIEWING_ATTRIBUTES = "patient.viewingAttributeTypes";
	
	public static final String GLOBAL_PROPERTY_PATIENT_HEADER_ATTRIBUTES = "patient.headerAttributeTypes";
	
	public static final String GLOBAL_PROPERTY_USER_LISTING_ATTRIBUTES = "user.listingAttributeTypes";
	
	public static final String GLOBAL_PROPERTY_USER_VIEWING_ATTRIBUTES = "user.viewingAttributeTypes";
	
	public static final String GLOBAL_PROPERTY_USER_HEADER_ATTRIBUTES = "user.headerAttributeTypes";
	
	public static final String GLOBAL_PROPERTY_USER_REQUIRE_EMAIL_AS_USERNAME = "user.requireEmailAsUsername";
	
	public static final String GLOBAL_PROPERTY_HL7_ARCHIVE_DIRECTORY = "hl7_archive.dir";
	
	public static final String GLOBAL_PROPERTY_DEFAULT_THEME = "default_theme";
	
	public static final String GLOBAL_PROPERTY_APPLICATION_NAME = "application.name";
	
	/**
	 * Array of all core global property names that represent comma-separated lists of
	 * PersonAttributeTypes. (If you rename a PersonAttributeType then these global properties are
	 * potentially modified.)
	 */
	public static final String[] GLOBAL_PROPERTIES_OF_PERSON_ATTRIBUTES = { GLOBAL_PROPERTY_PATIENT_LISTING_ATTRIBUTES,
	        GLOBAL_PROPERTY_PATIENT_VIEWING_ATTRIBUTES, GLOBAL_PROPERTY_PATIENT_HEADER_ATTRIBUTES,
	        GLOBAL_PROPERTY_USER_LISTING_ATTRIBUTES, GLOBAL_PROPERTY_USER_VIEWING_ATTRIBUTES,
	        GLOBAL_PROPERTY_USER_HEADER_ATTRIBUTES, GLOBAL_PROPERTY_USER_REQUIRE_EMAIL_AS_USERNAME };
	
	public static final String GLOBAL_PROPERTY_PATIENT_IDENTIFIER_REGEX = "patient.identifierRegex";
	
	public static final String GLOBAL_PROPERTY_PATIENT_IDENTIFIER_PREFIX = "patient.identifierPrefix";
	
	public static final String GLOBAL_PROPERTY_PATIENT_IDENTIFIER_SUFFIX = "patient.identifierSuffix";
	
	public static final String GLOBAL_PROPERTY_PATIENT_IDENTIFIER_SEARCH_PATTERN = "patient.identifierSearchPattern";
	
	public static final String GLOBAL_PROPERTY_PATIENT_NAME_REGEX = "patient.nameValidationRegex";
	
	public static final String GLOBAL_PROPERTY_PERSON_SEARCH_MAX_RESULTS = "person.searchMaxResults";
	
	public static final int GLOBAL_PROPERTY_PERSON_SEARCH_MAX_RESULTS_DEFAULT_VALUE = 1000;
	
	public static final String GLOBAL_PROPERTY_PERSON_ATTRIBUTE_SEARCH_MATCH_MODE = "person.attributeSearchMatchMode";
	
	public static final String GLOBAL_PROPERTY_PERSON_ATTRIBUTE_SEARCH_MATCH_EXACT = "EXACT";
	
	public static final String GLOBAL_PROPERTY_PERSON_ATTRIBUTE_SEARCH_MATCH_ANYWHERE = "ANYWHERE";
	
	public static final String GLOBAL_PROPERTY_GZIP_ENABLED = "gzip.enabled";
	
	public static final String GLOBAL_PROPERTY_GZIP_ACCEPT_COMPRESSED_REQUESTS_FOR_PATHS = "gzip.acceptCompressedRequestsForPaths";
	
	public static final String GLOBAL_PROPERTY_MEDICAL_RECORD_OBSERVATIONS = "concept.medicalRecordObservations";
	
	public static final String GLOBAL_PROPERTY_PROBLEM_LIST = "concept.problemList";
	
	public static final String GLOBAL_PROPERTY_SHOW_PATIENT_NAME = "dashboard.showPatientName";
	
	public static final String GLOBAL_PROPERTY_ENABLE_VISITS = "visits.enabled";
	
	public static final String GLOBAL_PROPERTY_ALLOW_OVERLAPPING_VISITS = "visits.allowOverlappingVisits";
	
	public static final String GLOBAL_PROPERTY_DEFAULT_PATIENT_IDENTIFIER_VALIDATOR = "patient.defaultPatientIdentifierValidator";
	
	public static final String GLOBAL_PROPERTY_PATIENT_IDENTIFIER_IMPORTANT_TYPES = "patient_identifier.importantTypes";
	
	public static final String GLOBAL_PROPERTY_ENCOUNTER_FORM_OBS_SORT_ORDER = "encounterForm.obsSortOrder";
	
	public static final String GLOBAL_PROPERTY_LOCALE_ALLOWED_LIST = "locale.allowed.list";
	
	public static final String GLOBAL_PROPERTY_IMPLEMENTATION_ID = "implementation_id";
	
	public static final String GLOBAL_PROPERTY_NEWPATIENTFORM_SHOW_RELATIONSHIPS = "new_patient_form.showRelationships";
	
	public static final String GLOBAL_PROPERTY_NEWPATIENTFORM_RELATIONSHIPS = "newPatientForm.relationships";
	
	public static final String GLOBAL_PROPERTY_COMPLEX_OBS_DIR = "obs.complex_obs_dir";
	
	public static final String GLOBAL_PROPERTY_MIN_SEARCH_CHARACTERS = "minSearchCharacters";
	
	public static final int GLOBAL_PROPERTY_DEFAULT_MIN_SEARCH_CHARACTERS = 2;
	
	public static final String GLOBAL_PROPERTY_DEFAULT_LOCALE = "default_locale";
	
	public static final String GLOBAL_PROPERTY_DEFAULT_LOCATION_NAME = "default_location";
	
	public static final String GLOBAL_PROPERTY_DEFAULT_LOCALE_DEFAULT_VALUE = "en_GB";
	
	public static final String GLOBAL_PROPERTY_DEFAULT_WEEK_START_DAY = "datePicker.weekStart";
	
	public static final String GLOBAL_PROPERTY_DEFAULT_WEEK_START_DAY_DEFAULT_VALUE = "0";
	
	public static final String GLOBAL_PROPERTY_PATIENT_IDENTIFIER_SEARCH_MATCH_MODE = "patientIdentifierSearch.matchMode";
	
	public static final String GLOBAL_PROPERTY_PATIENT_SEARCH_MATCH_MODE = "patientSearch.matchMode";
	
	public static final String GLOBAL_PROPERTY_PATIENT_SEARCH_MATCH_EXACT = "EXACT";
	
	public static final String GLOBAL_PROPERTY_PATIENT_SEARCH_MATCH_ANYWHERE = "ANYWHERE";
	
	public static final String GLOBAL_PROPERTY_PATIENT_SEARCH_MATCH_START = "START";
	
	public static final String GLOBAL_PROPERTY_PATIENT_SEARCH_MATCH_SOUNDEX = "SOUNDEX";
	
	public static final String GLOBAL_PROPERTY_PROVIDER_SEARCH_MATCH_MODE = "providerSearch.matchMode";
	
	public static final String GLOBAL_PROPERTY_DEFAULT_SERIALIZER = "serialization.defaultSerializer";
	
	public static final String GLOBAL_PROPERTY_IGNORE_MISSING_NONLOCAL_PATIENTS = "hl7_processor.ignore_missing_patient_non_local";
	
	public static final String GLOBAL_PROPERTY_TRUE_CONCEPT = "concept.true";
	
	public static final String GLOBAL_PROPERTY_FALSE_CONCEPT = "concept.false";
	
	public static final String GLOBAL_PROPERTY_UNKNOWN_CONCEPT = "concept.unknown";
	
	public static final String GLOBAL_PROPERTY_LOCATION_WIDGET_TYPE = "location.field.style";
	
	public static final String GLOBAL_PROPERTY_REPORT_BUG_URL = "reportProblem.url";
	
	public static final String GLOBAL_PROPERTY_ADDRESS_TEMPLATE = "layout.address.format";
	
	public static final String GLOBAL_PROPERTY_LAYOUT_NAME_FORMAT = "layout.name.format";
	
	public static final String GLOBAL_PROPERTY_ENCOUNTER_TYPES_LOCKED = "EncounterType.encounterTypes.locked";
	
	public static final String GLOBAL_PROPERTY_FORMS_LOCKED = "forms.locked";
	
	public static final String GLOBAL_PROPERTY_PERSON_ATRIBUTE_TYPES_LOCKED = "personAttributeTypes.locked";
	
	public static final String GLOBAL_PROPERTY_PATIENT_IDENTIFIER_TYPES_LOCKED = "patientIdentifierTypes.locked";
	
	public static final String GLOBAL_PROPERTY_DRUG_ORDER_REQUIRE_DRUG = "drugOrder.requireDrug";

	public static final String GLOBAL_PROPERTY_DRUG_ORDER_REQUIRE_OUTPATIENT_QUANTITY = "drugOrder.requireOutpatientQuantity";
	
	public static final String DEFAULT_ADDRESS_TEMPLATE = "<org.openmrs.layout.address.AddressTemplate>\n"
	        + "    <nameMappings class=\"properties\">\n"
	        + "      <property name=\"postalCode\" value=\"Location.postalCode\"/>\n"
	        + "      <property name=\"address2\" value=\"Location.address2\"/>\n"
	        + "      <property name=\"address1\" value=\"Location.address1\"/>\n"
	        + "      <property name=\"country\" value=\"Location.country\"/>\n"
	        + "      <property name=\"stateProvince\" value=\"Location.stateProvince\"/>\n"
	        + "      <property name=\"cityVillage\" value=\"Location.cityVillage\"/>\n" + "    </nameMappings>\n"
	        + "    <sizeMappings class=\"properties\">\n" + "      <property name=\"postalCode\" value=\"10\"/>\n"
	        + "      <property name=\"address2\" value=\"40\"/>\n" + "      <property name=\"address1\" value=\"40\"/>\n"
	        + "      <property name=\"country\" value=\"10\"/>\n"
	        + "      <property name=\"stateProvince\" value=\"10\"/>\n"
	        + "      <property name=\"cityVillage\" value=\"10\"/>\n" + "    </sizeMappings>\n" + "    <lineByLineFormat>\n"
	        + "      <string>address1</string>\n" + "      <string>address2</string>\n"
	        + "      <string>cityVillage stateProvince country postalCode</string>\n" + "    </lineByLineFormat>\n"
	        + "   <requiredElements>\\n\" + \" </requiredElements>\\n\" + \" </org.openmrs.layout.address.AddressTemplate>";
	
	/**
	 * Global property name that allows specification of whether user passwords must contain both
	 * upper and lower case characters. Allowable values are "true", "false", and null
	 */
	public static final String GP_PASSWORD_REQUIRES_UPPER_AND_LOWER_CASE = "security.passwordRequiresUpperAndLowerCase";
	
	/**
	 * Global property name that allows specification of whether user passwords require non-digits.
	 * Allowable values are "true", "false", and null
	 */
	public static final String GP_PASSWORD_REQUIRES_NON_DIGIT = "security.passwordRequiresNonDigit";
	
	/**
	 * Global property name that allows specification of whether user passwords must contain digits.
	 * Allowable values are "true", "false", and null
	 */
	public static final String GP_PASSWORD_REQUIRES_DIGIT = "security.passwordRequiresDigit";
	
	/**
	 * Global property name that allows specification of whether user passwords can match username
	 * or system id. Allowable values are "true", "false", and null
	 */
	public static final String GP_PASSWORD_CANNOT_MATCH_USERNAME_OR_SYSTEMID = "security.passwordCannotMatchUsername";
	
	/**
	 * Global property name that allows specification of whether user passwords have a minimum
	 * length requirement Allowable values are any integer
	 */
	public static final String GP_PASSWORD_MINIMUM_LENGTH = "security.passwordMinimumLength";
	
	/**
	 * Global property that stores the duration for which the password reset token is valid
	 */
	public static final String GP_PASSWORD_RESET_VALIDTIME = "security.validTime";
	
	/**
	 * Global property name that allows specification of a regular expression that passwords must
	 * adhere to
	 */
	public static final String GP_PASSWORD_CUSTOM_REGEX = "security.passwordCustomRegex";
	
	/**
	 * Global property name for absolute color for patient graphs.
	 */
	public static final String GP_GRAPH_COLOR_ABSOLUTE = "graph.color.absolute";
	
	/**
	 * Global property name for normal color for patient graphs.
	 */
	public static final String GP_GRAPH_COLOR_NORMAL = "graph.color.normal";
	
	/**
	 * Global property name for critical color for patient graphs.
	 */
	public static final String GP_GRAPH_COLOR_CRITICAL = "graph.color.critical";
	
	/**
	 * Global property name for the maximum number of search results that are returned by a single
	 * ajax call
	 */
	public static final String GP_SEARCH_WIDGET_BATCH_SIZE = "searchWidget.batchSize";
	
	/**
	 * Global property name for the mode the search widgets should run in, this depends on the speed
	 * of the network's connection
	 */
	public static final String GP_SEARCH_WIDGET_IN_SERIAL_MODE = "searchWidget.runInSerialMode";
	
	/**
	 * Global property name for the time interval in milliseconds between key up and triggering the
	 * search off
	 */
	public static final String GP_SEARCH_WIDGET_DELAY_INTERVAL = "searchWidget.searchDelayInterval";
	
	/**
	 * Global property name for the maximum number of results to return from a single search in the
	 * search widgets
	 */
	public static final String GP_SEARCH_WIDGET_MAXIMUM_RESULTS = "searchWidget.maximumResults";
	
	/**
	 * Global property for the Date format to be used to display date under search widgets and auto-completes
	 */
	public static final String GP_SEARCH_DATE_DISPLAY_FORMAT = "searchWidget.dateDisplayFormat";
	
	/**
	 * Global property name for enabling/disabling concept map type management
	 */
	public static final String GP_ENABLE_CONCEPT_MAP_TYPE_MANAGEMENT = "concept_map_type_management.enable";
	
	/**
	 * Global property name for the handler that assigns visits to encounters
	 */
	public static final String GP_VISIT_ASSIGNMENT_HANDLER = "visits.assignmentHandler";
	
	/**
	 * Global property name for mapping encounter types to visit types.
	 */
	public static final String GP_ENCOUNTER_TYPE_TO_VISIT_TYPE_MAPPING = "visits.encounterTypeToVisitTypeMapping";
	
	/**
	 * Global property name for the encounter roles to display on the provider column of the patient
	 * dashboard under the encounters tab.
	 */
	public static final String GP_DASHBOARD_PROVIDER_DISPLAY_ENCOUNTER_ROLES = "dashboard.encounters.providerDisplayRoles";
	
	/**
	 * Global property name for optional configuration of the maximum number of encounters to
	 * display on the encounter tab of the patient dashboard
	 */
	public static final String GP_DASHBOARD_MAX_NUMBER_OF_ENCOUNTERS_TO_SHOW = "dashboard.encounters.maximumNumberToShow";
	
	/**
	 * Global property name to display program, workflow and states in a specific case
	 */
	public static final String GP_DASHBOARD_METADATA_CASE_CONVERSION = "dashboard.metadata.caseConversion";
	
	/**
	 * Global property name for the default ConceptMapType which is set automatically when no other is set manually.
	 */
	public static final String GP_DEFAULT_CONCEPT_MAP_TYPE = "concept.defaultConceptMapType";
	
	/**
	 * Global property name of the allowed concept classes for the dosage form field of the concept drug management form.
	 */
	public static final String GP_CONCEPT_DRUG_DOSAGE_FORM_CONCEPT_CLASSES = "conceptDrug.dosageForm.conceptClasses";
	
	/**
	 * Global property name of the allowed concept classes for the route field of the concept drug management form.
	 */
	public static final String GP_CONCEPT_DRUG_ROUTE_CONCEPT_CLASSES = "conceptDrug.route.conceptClasses";
	
	/**
	 * Global property name of the allowed concept classes for the allergen field of the allergy
	 * management form.
	 */
	public static final String GP_ALLERGY_ALLERGEN_CONCEPT_CLASSES = "allergy.allergen.ConceptClasses";
	
	/**
	 * Global property name of the allowed concept classes for the reaction field of the allergy
	 * management form.
	 */
	public static final String GP_ALLERGY_REACTION_CONCEPT_CLASSES = "allergy.reaction.ConceptClasses";
	
	/**
	 * Global property name of other non coded allergen, stored in allergen coded allergen
	 * when other non coded allergen is represented
	 */
	public static final String GP_ALLERGEN_OTHER_NON_CODED_UUID = "allergy.concept.otherNonCoded";
	
	/**
	 * Encryption properties; both vector and key are required to utilize a two-way encryption
	 */
	public static final String ENCRYPTION_CIPHER_CONFIGURATION = "AES/CBC/PKCS5Padding";
	
	public static final String ENCRYPTION_KEY_SPEC = "AES";
	
	public static final String ENCRYPTION_VECTOR_RUNTIME_PROPERTY = "encryption.vector";
	
	public static final String ENCRYPTION_VECTOR_DEFAULT = "9wyBUNglFCRVSUhMfsTa3Q==";
	
	public static final String ENCRYPTION_KEY_RUNTIME_PROPERTY = "encryption.key";
	
	public static final String ENCRYPTION_KEY_DEFAULT = "dTfyELRrAICGDwzjHDjuhw==";
	
	/**
	 * Global property name for the visit type(s) to automatically close
	 */
	public static final String GP_VISIT_TYPES_TO_AUTO_CLOSE = "visits.autoCloseVisitType";
	
	/**
	 * The name of the scheduled task that automatically stops the active visits
	 */
	public static final String AUTO_CLOSE_VISITS_TASK_NAME = "Auto Close Visits Task";
	
	public static final String GP_ALLOWED_FAILED_LOGINS_BEFORE_LOCKOUT = "security.allowedFailedLoginsBeforeLockout";
	
	/**
	 * @since 1.9.9, 1.10.2, 1.11
	 */
	public static final String GP_CASE_SENSITIVE_DATABASE_STRING_COMPARISON = "search.caseSensitiveDatabaseStringComparison";
	
	public static final String GP_DASHBOARD_CONCEPTS = "dashboard.header.showConcept";
	
	public static final String GP_MAIL_SMTP_STARTTLS_ENABLE = "mail.smtp.starttls.enable";
	
	public static final String GP_NEXT_ORDER_NUMBER_SEED = "order.nextOrderNumberSeed";
	
	public static final String GP_ORDER_NUMBER_GENERATOR_BEAN_ID = "order.orderNumberGeneratorBeanId";
	
	/**
	 * Specifies the uuid of the concept set where its members represent the possible drug routes
	 */
	public static final String GP_DRUG_ROUTES_CONCEPT_UUID = "order.drugRoutesConceptUuid";
	
	public static final String GP_DRUG_DOSING_UNITS_CONCEPT_UUID = "order.drugDosingUnitsConceptUuid";
	
	public static final String GP_DRUG_DISPENSING_UNITS_CONCEPT_UUID = "order.drugDispensingUnitsConceptUuid";
	
	public static final String GP_DURATION_UNITS_CONCEPT_UUID = "order.durationUnitsConceptUuid";
	
	public static final String GP_TEST_SPECIMEN_SOURCES_CONCEPT_UUID = "order.testSpecimenSourcesConceptUuid";
	
	public static final String GP_UNKNOWN_PROVIDER_UUID = "provider.unknownProviderUuid";
	
	/**
	 * @since 1.11
	 */
	public static final String GP_SEARCH_INDEX_VERSION = "search.indexVersion";
	
	/**
	 * Indicates the version of the search index. The index will be rebuilt, if the version changes.
	 * 
	 * @since 1.11
	 */
	public static final Integer SEARCH_INDEX_VERSION = 7;

	/**
	 * @since 1.12
	 */
	public static final String GP_DISABLE_VALIDATION = "validation.disable";

    /**
     * @since 1.12
	 * Specifies the uuid of the concept which represents drug non coded
	 */
	public static final String GP_DRUG_ORDER_DRUG_OTHER = "drugOrder.drugOther";

	/**
	 * Global property that stores the base url for the application.
	 */
	public static final String GP_HOST_URL = "host.url";
	
	
	
	// ConceptProposal proposed concept identifier keyword
	public static final String PROPOSED_CONCEPT_IDENTIFIER = "PROPOSED";
	
	// ConceptProposal states
	public static final String CONCEPT_PROPOSAL_UNMAPPED = "UNMAPPED";
	
	public static final String CONCEPT_PROPOSAL_CONCEPT = "CONCEPT";
	
	public static final String CONCEPT_PROPOSAL_SYNONYM = "SYNONYM";
	
	public static final String CONCEPT_PROPOSAL_REJECT = "REJECT";
	
	public static final Collection<String> CONCEPT_PROPOSAL_STATES() {
		Collection<String> states = new ArrayList<>();
		
		states.add(CONCEPT_PROPOSAL_UNMAPPED);
		states.add(CONCEPT_PROPOSAL_CONCEPT);
		states.add(CONCEPT_PROPOSAL_SYNONYM);
		states.add(CONCEPT_PROPOSAL_REJECT);
		
		return states;
	}
	
	public static final Locale SPANISH_LANGUAGE = new Locale("es");
	
	public static final Locale PORTUGUESE_LANGUAGE = new Locale("pt");
	
	public static final Locale ITALIAN_LANGUAGE = new Locale("it");
	
	/*
	 * User property names
	 */
	public static final String USER_PROPERTY_CHANGE_PASSWORD = "forcePassword";
	
	public static final String USER_PROPERTY_DEFAULT_LOCALE = "defaultLocale";
	
	public static final String USER_PROPERTY_DEFAULT_LOCATION = "defaultLocation";
	
	public static final String USER_PROPERTY_SHOW_RETIRED = "showRetired";
	
	public static final String USER_PROPERTY_SHOW_VERBOSE = "showVerbose";
	
	public static final String USER_PROPERTY_NOTIFICATION = "notification";
	
	public static final String USER_PROPERTY_NOTIFICATION_ADDRESS = "notificationAddress";
	
	public static final String USER_PROPERTY_NOTIFICATION_FORMAT = "notificationFormat"; // text/plain, text/html
	
	/**
	 * Name of the user_property that stores the number of unsuccessful login attempts this user has
	 * made
	 */
	public static final String USER_PROPERTY_LOGIN_ATTEMPTS = "loginAttempts";
	
	/**
	 * Name of the user_property that stores the time the user was locked out due to too many login
	 * attempts
	 */
	public static final String USER_PROPERTY_LOCKOUT_TIMESTAMP = "lockoutTimestamp";
	
	/**
	 * A user property name. The value should be a comma-separated ordered list of fully qualified
	 * locales within which the user is a proficient speaker. The list should be ordered from the
	 * most to the least proficiency. Example:
	 * <code>proficientLocales = en_US, en_GB, en, fr_RW</code>
	 */
	public static final String USER_PROPERTY_PROFICIENT_LOCALES = "proficientLocales";
	
	// Used for differences between windows/linux upload capabilities)
	// Used for determining where to find runtime properties
	public static final String OPERATING_SYSTEM_KEY = "os.name";
	
	public static final String OPERATING_SYSTEM = System.getProperty(OPERATING_SYSTEM_KEY);
	
	public static final String OPERATING_SYSTEM_WINDOWS_XP = "Windows XP";
	
	public static final String OPERATING_SYSTEM_WINDOWS_VISTA = "Windows Vista";
	
	public static final String OPERATING_SYSTEM_LINUX = "Linux";
	
	public static final String OPERATING_SYSTEM_SUNOS = "SunOS";
	
	public static final String OPERATING_SYSTEM_FREEBSD = "FreeBSD";
	
	public static final String OPERATING_SYSTEM_OSX = "Mac OS X";
	
	/**
	 * URL to the concept source id verification server
	 */
	public static final String IMPLEMENTATION_ID_REMOTE_CONNECTION_URL = "https://implementation.openmrs.org";
	
	/**
	 * Shortcut booleans used to make some OS specific checks more generic; note the *nix flavored
	 * check is missing some less obvious choices
	 */
	public static final boolean UNIX_BASED_OPERATING_SYSTEM = (OPERATING_SYSTEM.contains(OPERATING_SYSTEM_LINUX)
	        || OPERATING_SYSTEM.contains(OPERATING_SYSTEM_SUNOS)
	        || OPERATING_SYSTEM.contains(OPERATING_SYSTEM_FREEBSD) || OPERATING_SYSTEM.contains(OPERATING_SYSTEM_OSX));
	
	public static final boolean WINDOWS_BASED_OPERATING_SYSTEM = OPERATING_SYSTEM.contains("Windows");
	
	public static final boolean WINDOWS_VISTA_OPERATING_SYSTEM = OPERATING_SYSTEM.equals(OPERATING_SYSTEM_WINDOWS_VISTA);
	
	/**
	 * Marker put into the serialization session map to tell @Replace methods whether or not to do
	 * just the very basic serialization
	 */
	public static final String SHORT_SERIALIZATION = "isShortSerialization";
	
	// Global property key for global logger level
	public static final String GLOBAL_PROPERTY_LOG_LEVEL = "log.level";
	
	/**
	 * It points to a directory where 'openmrs.log' is stored.
	 * 
	 * @since 1.9.2
	 */
	public static final String GP_LOG_LOCATION = "log.location";
	
	/**
	 * It specifies a log layout pattern used by the OpenMRS file appender.
	 * 
	 * @since 1.9.2
	 */
	public static final String GP_LOG_LAYOUT = "log.layout";
	
	/**
	 * It specifies a default name of the OpenMRS file appender.
	 * .
	 * @since 1.9.2
	 */
	public static final String LOG_OPENMRS_FILE_APPENDER = "OPENMRS FILE APPENDER";
	
	// Global logger category
	public static final String LOG_CLASS_DEFAULT = "org.openmrs.api";
	
	// Log levels
	public static final String LOG_LEVEL_TRACE = "trace";
	
	public static final String LOG_LEVEL_DEBUG = "debug";
	
	public static final String LOG_LEVEL_INFO = "info";
	
	public static final String LOG_LEVEL_WARN = "warn";
	
	public static final String LOG_LEVEL_ERROR = "error";
	
	public static final String LOG_LEVEL_FATAL = "fatal";

	/**
	 * Default url responsible for authentication if a user is not logged in.
	 * 
	 * @see  #GP_LOGIN_URL
	 */
	public static final String LOGIN_URL = "login.htm";
	
	/**
	 * Global property name that defines the default url 
	 * responsible for authentication if user is not logged in.
	 *
	 *  @see #LOGIN_URL
	 */
	public static final String GP_LOGIN_URL = "login.url";
	
	/**
	 * These enumerations should be used in ObsService and PersonService getters to help determine
	 * which type of object to restrict on
	 * 
	 * @see org.openmrs.api.ObsService
	 * @see org.openmrs.api.PersonService
	 */
	public static enum PERSON_TYPE {
		PERSON,
		PATIENT,
		USER
	}
	
	//Patient Identifier Validators
	public static final String LUHN_IDENTIFIER_VALIDATOR = LuhnIdentifierValidator.class.getName();
	
	/** The data type to return on failing to load a custom data type. */
	public static final String DEFAULT_CUSTOM_DATATYPE = FreeTextDatatype.class.getName();
	
	/**Prefix followed by registered component name.*/
	public static final String REGISTERED_COMPONENT_NAME_PREFIX = "bean:";
	
	/** Value for the short person name format */
	public static final String PERSON_NAME_FORMAT_SHORT = "short";
	
	/** Value for the long person name format */
	public static final String PERSON_NAME_FORMAT_LONG = "long";
	
	private OpenmrsConstants() {
	}
	
}
