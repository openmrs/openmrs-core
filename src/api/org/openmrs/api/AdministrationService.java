/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.api;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.SortedMap;

import org.openmrs.Concept;
import org.openmrs.ConceptClass;
import org.openmrs.ConceptDatatype;
import org.openmrs.ConceptProposal;
import org.openmrs.EncounterType;
import org.openmrs.FieldType;
import org.openmrs.GlobalProperty;
import org.openmrs.ImplementationId;
import org.openmrs.Location;
import org.openmrs.MimeType;
import org.openmrs.PatientIdentifierType;
import org.openmrs.Privilege;
import org.openmrs.Role;
import org.openmrs.Tribe;
import org.openmrs.annotation.Authorized;
import org.openmrs.api.db.AdministrationDAO;
import org.openmrs.reporting.AbstractReportObject;
import org.openmrs.reporting.Report;
import org.openmrs.util.OpenmrsConstants;
import org.springframework.transaction.annotation.Transactional;

/**
 * Contains methods pertaining to doing some administrative tasks in OpenMRS
 * <p>
 * Use:<br/>
 * 
 * <pre>
 * List&lt;GlobalProperty&gt; globalProperties = Context.getAdministrationService().getGlobalProperties();
 * </pre>
 * 
 * @see org.openmrs.api.context.Context
 */
@Transactional
public interface AdministrationService extends OpenmrsService {
	
	/**
	 * Used by Spring to set the specific/chosen database access implementation
	 * 
	 * @param dao The dao implementation to use
	 */
	public void setAdministrationDAO(AdministrationDAO dao);
	
	/**
	 * @deprecated use {@link org.openmrs.api.EncounterService#saveEncounterType(EncounterType)}
	 */
	public void createEncounterType(EncounterType encounterType) throws APIException;
	
	/**
	 * @deprecated use {@link org.openmrs.api.EncounterService#saveEncounterType(EncounterType)}
	 */
	public void updateEncounterType(EncounterType encounterType) throws APIException;
	
	/**
	 * @deprecated use {@link org.openmrs.api.EncounterService#purgeEncounterType(EncounterType)}
	 */
	public void deleteEncounterType(EncounterType encounterType) throws APIException;
	
	/**
	 * @see org.openmrs.api.PatientService#savePatientIdentifierType(PatientIdentifierType)
	 * @deprecated replaced by
	 *             {@link org.openmrs.api.PatientService#savePatientIdentifierType(PatientIdentifierType)}
	 */
	public void createPatientIdentifierType(PatientIdentifierType patientIdentifierType) throws APIException;
	
	/**
	 * @see org.openmrs.api.PatientService#savePatientIdentifierType(PatientIdentifierType)
	 * @deprecated replaced by
	 *             {@link org.openmrs.api.PatientService#savePatientIdentifierType(PatientIdentifierType)}
	 */
	public void updatePatientIdentifierType(PatientIdentifierType patientIdentifierType) throws APIException;
	
	/**
	 * @see org.openmrs.api.PatientService#purgePatientIdentifierType(PatientIdentifierType)
	 * @deprecated replaced by
	 *             {@link org.openmrs.api.PatientService#purgePatientIdentifierType(PatientIdentifierType)}
	 */
	public void deletePatientIdentifierType(PatientIdentifierType patientIdentifierType) throws APIException;
	
	/**
	 * @deprecated The Tribe object is no longer supported. Install the Tribe module
	 */
	public void createTribe(Tribe tribe) throws APIException;
	
	/**
	 * @deprecated The Tribe object is no longer supported. Install the Tribe module
	 */
	public void updateTribe(Tribe tribe) throws APIException;
	
	/**
	 * @deprecated The Tribe object is no longer supported. Install the Tribe module
	 */
	public void deleteTribe(Tribe tribe) throws APIException;
	
	/**
	 * @deprecated The Tribe object is no longer supported. Install the Tribe module
	 */
	public void retireTribe(Tribe tribe) throws APIException;
	
	/**
	 * @deprecated The Tribe object is no longer supported. Install the Tribe module
	 */
	public void unretireTribe(Tribe tribe) throws APIException;
	
	/**
	 * @deprecated use {@link FormService#saveFieldType(FieldType)}
	 */
	public void createFieldType(FieldType fieldType) throws APIException;
	
	/**
	 * @deprecated use {@link FormService#saveFieldType(FieldType)}
	 */
	public void updateFieldType(FieldType fieldType) throws APIException;
	
	/**
	 * @deprecated use {@link FormService#purgeFieldType(FieldType)}
	 */
	public void deleteFieldType(FieldType fieldType) throws APIException;
	
	/**
	 * @deprecated see {@link org.openmrs.api.ObsService#saveMimeType(MimeType)}
	 **/
	@Authorized(OpenmrsConstants.PRIV_MANAGE_MIME_TYPES)
	public void createMimeType(MimeType mimeType) throws APIException;
	
	/**
	 * @deprecated see {@link org.openmrs.api.ObsService#saveMimeType(MimeType)}
	 */
	@Authorized(OpenmrsConstants.PRIV_MANAGE_MIME_TYPES)
	public void updateMimeType(MimeType mimeType) throws APIException;
	
	/**
	 * @deprecated see {@link org.openmrs.api.ObsService#purgeMimeType(MimeType)}
	 */
	@Authorized(OpenmrsConstants.PRIV_MANAGE_MIME_TYPES)
	public void deleteMimeType(MimeType mimeType) throws APIException;
	
	/**
	 * @deprecated see {@link org.openmrs.api.LocationService#saveLocation(Location)}
	 */
	public void createLocation(Location location) throws APIException;
	
	/**
	 * @deprecated see {@link org.openmrs.api.LocationService#saveLocation(Location)}
	 */
	public void updateLocation(Location location) throws APIException;
	
	/**
	 * @deprecated see {@link org.openmrs.api.LocationService#purgeLocation(Location)}
	 */
	public void deleteLocation(Location location) throws APIException;
	
	/**
	 * @deprecated see {@link org.openmrs.api.UserService#saveRole(Role)}
	 */
	public void createRole(Role role) throws APIException;
	
	/**
	 * @deprecated see {@link org.openmrs.api.UserService#saveRole(Role)}
	 */
	public void updateRole(Role role) throws APIException;
	
	/**
	 * @deprecated see {@link org.openmrs.api.UserService#purgeRole(Role)}
	 */
	public void deleteRole(Role role) throws APIException;
	
	/**
	 * @deprecated see {@link org.openmrs.api.UserService#savePrivilege(Privilege)}
	 */
	public void createPrivilege(Privilege privilege) throws APIException;
	
	/**
	 * @deprecated see {@link org.openmrs.api.UserService#savePrivilege(Privilege)}
	 */
	public void updatePrivilege(Privilege privilege) throws APIException;
	
	/**
	 * @deprecated see {@link org.openmrs.api.UserService#purgePrivilege(Privilege)}
	 */
	public void deletePrivilege(Privilege privilege) throws APIException;
	
	/**
	 * Create a new ConceptClass
	 * 
	 * @param cc ConceptClass to create
	 * @throws APIException
	 * @deprecated use {@link org.openmrs.api.ConceptService#saveConceptClass(ConceptClass)}
	 */
	public void createConceptClass(ConceptClass cc) throws APIException;
	
	/**
	 * Update ConceptClass
	 * 
	 * @param cc ConceptClass to update
	 * @throws APIException
	 * @deprecated use {@link org.openmrs.api.ConceptService#saveConceptClass(ConceptClass)}
	 */
	public void updateConceptClass(ConceptClass cc) throws APIException;
	
	/**
	 * Delete ConceptClass
	 * 
	 * @param cc ConceptClass to delete
	 * @throws APIException
	 * @deprecated use {@link org.openmrs.api.ConceptService#purgeConceptClass(ConceptClass)}
	 */
	public void deleteConceptClass(ConceptClass cc) throws APIException;
	
	/**
	 * Create a new ConceptDatatype
	 * 
	 * @param cd ConceptDatatype to create
	 * @throws APIException
	 * @deprecated use {@link org.openmrs.api.ConceptService#saveConceptDatatype(ConceptDatatype)}
	 */
	public void createConceptDatatype(ConceptDatatype cd) throws APIException;
	
	/**
	 * Update ConceptDatatype
	 * 
	 * @param cd ConceptDatatype to update
	 * @throws APIException
	 * @deprecated use {@link org.openmrs.api.ConceptService#saveConceptDatatype(ConceptDatatype)}
	 */
	public void updateConceptDatatype(ConceptDatatype cd) throws APIException;
	
	/**
	 * Delete ConceptDatatype
	 * 
	 * @param cd ConceptDatatype to delete
	 * @throws APIException
	 * @deprecated use {@link org.openmrs.api#deleteConceptDatatype(ConceptDatatype)}
	 */
	public void deleteConceptDatatype(ConceptDatatype cd) throws APIException;
	
	/**
	 * Create a new Report
	 * 
	 * @param report Report to create
	 * @throws APIException
	 * @deprecated see reportingcompatibility module
	 */
	@Deprecated
	public void createReport(Report report) throws APIException;
	
	/**
	 * Update Report
	 * 
	 * @param report Report to update
	 * @deprecated see reportingcompatibility module
	 * @throws APIException
	 */
	@Deprecated
	public void updateReport(Report report) throws APIException;
	
	/**
	 * Delete Report
	 * 
	 * @param report Report to delete
	 * @throws APIException
	 * @deprecated see reportingcompatibility module
	 */
	@Deprecated
	public void deleteReport(Report report) throws APIException;
	
	/**
	 * Create a new Report Object
	 * 
	 * @param reportObject Report Object to create
	 * @deprecated see reportingcompatibility module
	 * @throws APIException
	 */
	@Deprecated
	public void createReportObject(AbstractReportObject reportObject) throws APIException;
	
	/**
	 * Update Report Object
	 * 
	 * @param reportObject the Report Object to update
	 * @deprecated see reportingcompatibility module
	 * @throws APIException
	 */
	@Deprecated
	public void updateReportObject(AbstractReportObject reportObject) throws APIException;
	
	/**
	 * Delete Report Object
	 * 
	 * @param reportObjectId Internal identifier for the Report Object to delete
	 * @deprecated see reportingcompatibility module
	 * @throws APIException
	 */
	@Deprecated
	public void deleteReportObject(Integer reportObjectId) throws APIException;
	
	/**
	 * Iterates over the words in names and synonyms (for each locale) and updates the concept word
	 * business table
	 * 
	 * @param concept
	 * @throws APIException
	 * @deprecated moved to {@link org.openmrs.api.ConceptService#updateConceptWord(Concept)}
	 */
	public void updateConceptWord(Concept concept) throws APIException;
	
	/**
	 * Iterates over all concepts calling updateConceptWord(concept)
	 * 
	 * @throws APIException
	 * @deprecated moved to {@link org.openmrs.api.ConceptService#updateConceptWords()}
	 */
	public void updateConceptWords() throws APIException;
	
	/**
	 * Iterates over all concepts with conceptIds between <code>conceptIdStart</code> and
	 * <code>conceptIdEnd</code> (inclusive) calling updateConceptWord(concept)
	 * 
	 * @throws APIException
	 * @deprecated moved to
	 *             {@link org.openmrs.api.ConceptService#updateConceptWords(Integer, Integer)}
	 */
	public void updateConceptWords(Integer conceptIdStart, Integer conceptIdEnd) throws APIException;
	
	/**
	 * Updates the concept set derived business table for this concept (bursting the concept sets)
	 * 
	 * @param concept
	 * @throws APIException
	 * @deprecated moved to {@link org.openmrs.api.ConceptService#updateConceptSetDerived(Concept)};
	 */
	public void updateConceptSetDerived(Concept concept) throws APIException;
	
	/**
	 * Iterates over all concepts calling updateConceptSetDerived(concept)
	 * 
	 * @throws APIException
	 * @deprecated moved to {@link org.openmrs.api.ConceptService#updateConceptSetDerived()}
	 */
	public void updateConceptSetDerived() throws APIException;
	
	/**
	 * Create a concept proposal
	 * 
	 * @param cp
	 * @throws APIException
	 * @deprecated use {@link org.openmrs.api.ConceptService#saveConceptProposal(ConceptProposal)}
	 */
	public void createConceptProposal(ConceptProposal cp) throws APIException;
	
	/**
	 * Update a concept proposal
	 * 
	 * @param cp
	 * @throws APIException
	 * @deprecated use {@link org.openmrs.api.ConceptService#saveConceptProposal(ConceptProposal)}
	 */
	public void updateConceptProposal(ConceptProposal cp) throws APIException;
	
	/**
	 * maps a concept proposal to a concept
	 * 
	 * @param cp
	 * @param mappedConcept
	 * @throws APIException
	 * @deprecated moved to
	 *             {@link org.openmrs.api.ConceptService#mapConceptProposalToConcept(ConceptProposal, Concept)}
	 */
	public void mapConceptProposalToConcept(ConceptProposal cp, Concept mappedConcept) throws APIException;
	
	/**
	 * rejects a concept proposal
	 * 
	 * @param cp
	 * @deprecated moved to
	 *             {@link org.openmrs.api#ConceptServicerejectConceptProposal(ConceptProposal)}
	 */
	public void rejectConceptProposal(ConceptProposal cp);
	
	/**
	 * @param site
	 * @param start
	 * @param count
	 * @deprecated use the mrngen module instead
	 */
	public void mrnGeneratorLog(String site, Integer start, Integer count);
	
	/**
	 * @deprecated use the mrngen module instead
	 */
	@Transactional(readOnly = true)
	public Collection<?> getMRNGeneratorLog();
	
	/**
	 * Get a global property by its uuid. There should be only one of these in the database (well,
	 * in the world actually). If multiple are found, an error is thrown.
	 * 
	 * @return the global property matching the given uuid
	 * @should find object given valid uuid
	 * @should return null if no object found with given uuid
	 */
	@Transactional(readOnly = true)
	public GlobalProperty getGlobalPropertyByUuid(String uuid) throws APIException;
	
	/**
	 * Get a listing or important variables used in openmrs
	 * 
	 * @return a map from variable name to variable value
	 */
	@Transactional(readOnly = true)
	@Authorized(OpenmrsConstants.PRIV_VIEW_ADMIN_FUNCTIONS)
	public SortedMap<String, String> getSystemVariables() throws APIException;
	
	/**
	 * Gets the global property that has the given <code>propertyName</code>.
	 * <p>
	 * If <code>propertyName</code> is not found in the list of Global Properties currently in the
	 * database, a null value is returned. This method should not have any authorization check.
	 * 
	 * @param propertyName property key to look for
	 * @return value of property returned or null if none
	 * @see #getGlobalProperty(String, String)
	 * @should not fail with null propertyName
	 * @should get property value given valid property name
	 */
	@Transactional(readOnly = true)
	public String getGlobalProperty(String propertyName) throws APIException;
	
	/**
	 * Gets the global property that has the given <code>propertyName</code> If propertyName is not
	 * found in the list of Global Properties currently in the database, a <code>defaultValue</code>
	 * is returned This method should not have any authorization check
	 * 
	 * @param propertyName property key to look for
	 * @param defaultValue value to return if propertyName is not found
	 * @return value of propertyName property or defaultValue if none
	 * @should return default value if property name does not exist
	 * @should not fail with null default value
	 */
	@Transactional(readOnly = true)
	public String getGlobalProperty(String propertyName, String defaultValue) throws APIException;
	
	/**
	 * Gets the global property that has the given <code>propertyName</code>
	 * 
	 * @param propertyName property key to look for
	 * @return the global property that matches the given <code>propertyName</code>
	 */
	@Transactional(readOnly = true)
	public GlobalProperty getGlobalPropertyObject(String propertyName);
	
	/**
	 * Gets all global properties that begin with <code>prefix</code>.
	 * 
	 * @param prefix The beginning of the property name to match.
	 * @return a <code>List</code> of <code>GlobalProperty</code>s that match <code>prefix</code>
	 * @since 1.5
	 * @should return all relevant global properties in the database
	 */
	@Transactional(readOnly = true)
	public List<GlobalProperty> getGlobalPropertiesByPrefix(String prefix);
	
	/**
	 * Gets all global properties that end with <code>suffix</code>.
	 * 
	 * @param prefix The end of the property name to match.
	 * @return a <code>List</code> of <code>GlobalProperty</code>s that match <code>.*suffix</code>
	 * @since 1.6
	 * @should return all relevant global properties in the database
	 */
	@Transactional(readOnly = true)
	public List<GlobalProperty> getGlobalPropertiesBySuffix(String suffix);
	
	/**
	 * Get a list of all global properties in the system
	 * 
	 * @return list of global properties
	 * @should return all global properties in the database
	 */
	@Transactional(readOnly = true)
	@Authorized(OpenmrsConstants.PRIV_VIEW_GLOBAL_PROPERTIES)
	public List<GlobalProperty> getAllGlobalProperties() throws APIException;
	
	/**
	 * @deprecated use {@link #getAllGlobalProperties()}
	 */
	@Transactional(readOnly = true)
	public List<GlobalProperty> getGlobalProperties();
	
	/**
	 * Save the given list of global properties to the database overwriting all values with the
	 * given values. If a value exists in the database that does not exist in the given list, that
	 * property is deleted from the database.
	 * 
	 * @param props list of GlobalProperty objects to save
	 * @return the saved global properties
	 * @should save all global properties to the database
	 * @should not fail with empty list
	 * @should delete property from database if not in list
	 * @should assign uuid to all new properties
	 */
	@Authorized(OpenmrsConstants.PRIV_MANAGE_GLOBAL_PROPERTIES)
	public List<GlobalProperty> saveGlobalProperties(List<GlobalProperty> props) throws APIException;
	
	/**
	 * @deprecated use {@link #saveGlobalProperties(List)}
	 */
	public void setGlobalProperties(List<GlobalProperty> props);
	
	/**
	 * Completely remove the given global property from the database
	 * 
	 * @param globalProperty the global property to delete/remove from the database
	 * @throws APIException
	 * @should delete global property from database
	 */
	@Authorized(OpenmrsConstants.PRIV_PURGE_GLOBAL_PROPERTIES)
	public void purgeGlobalProperty(GlobalProperty globalProperty) throws APIException;
	
	/**
	 * Use
	 * 
	 * <pre>
	 * purgeGlobalProperty(new GlobalProperty(propertyName));
	 * </pre>
	 * 
	 * @deprecated use {@link #purgeGlobalProperty(GlobalProperty)}
	 */
	public void deleteGlobalProperty(String propertyName);
	
	/**
	 * Use
	 * 
	 * <pre>
	 * purgeGlobalProperty(new GlobalProperty(propertyName, propertyValue));
	 * </pre>
	 * 
	 * @deprecated use #saveGlobalProperty(GlobalProperty)
	 */
	public void setGlobalProperty(String propertyName, String propertyValue);
	
	/**
	 * Save the given global property to the database
	 * 
	 * @param gp global property to save
	 * @return the saved global property
	 * @throws APIException
	 * @should create global property in database
	 * @should overwrite global property if exists
	 */
	@Authorized(OpenmrsConstants.PRIV_MANAGE_GLOBAL_PROPERTIES)
	public GlobalProperty saveGlobalProperty(GlobalProperty gp) throws APIException;
	
	/**
	 * @deprecated use {@link #saveGlobalProperty(GlobalProperty)}
	 */
	public void setGlobalProperty(GlobalProperty gp);
	
	/**
	 * @deprecated use {@link #saveGlobalProperty(GlobalProperty)}
	 */
	public void addGlobalProperty(String propertyName, String propertyValue);
	
	/**
	 * @deprecated use {@link #saveGlobalProperty(GlobalProperty)}
	 */
	public void addGlobalProperty(GlobalProperty gp);
	
	/**
	 * Allows code to be notified when a global property is created/edited/deleted.
	 * 
	 * @see GlobalPropertyListener
	 * @param listener The listener to register
	 */
	public void addGlobalPropertyListener(GlobalPropertyListener listener);
	
	/**
	 * Removes a GlobalPropertyListener previously registered by
	 * {@link #addGlobalPropertyListener(GlobalPropertyListener)}
	 * 
	 * @param listener
	 */
	public void removeGlobalPropertyListener(GlobalPropertyListener listener);
	
	/**
	 * Runs the <code>sql</code> on the database. If <code>selectOnly</code> is flagged then any
	 * non-select sql statements will be rejected.
	 * 
	 * @param sql
	 * @param selectOnly
	 * @return ResultSet
	 * @throws APIException
	 * @should execute sql containing group by
	 */
	@Authorized(OpenmrsConstants.PRIV_SQL_LEVEL_ACCESS)
	public List<List<Object>> executeSQL(String sql, boolean selectOnly) throws APIException;
	
	/**
	 * Get the implementation id stored for this server Returns null if no implementation id has
	 * been successfully set yet
	 * 
	 * @return ImplementationId object that is this implementation's unique id
	 * @should return null if no implementation id is defined yet
	 */
	@Transactional(readOnly = true)
	@Authorized(OpenmrsConstants.PRIV_MANAGE_IMPLEMENTATION_ID)
	public ImplementationId getImplementationId() throws APIException;
	
	/**
	 * Set the given <code>implementationId</code> as this implementation's unique id
	 * 
	 * @param implementationId the ImplementationId to save
	 * @throws APIException if implementationId is empty or is invalid according to central id server
	 * @should create implementation id in database
	 * @should overwrite implementation id in database if exists
	 * @should not fail if given implementationId is null
	 * @should throw APIException if given empty implementationId object
	 * @should throw APIException if given a caret in the implementationId code
	 * @should throw APIException if given a pipe in the implementationId code
	 * @should set uuid on implementation id global property
	 */
	@Authorized(OpenmrsConstants.PRIV_MANAGE_IMPLEMENTATION_ID)
	public void setImplementationId(ImplementationId implementationId) throws APIException;
	
	/**
	 * Gets the list of locales which the administrator has allowed for use on the system. This is
	 * specified with a global property named
	 * {@link OpenmrsConstants#GLOBAL_PROPERTY_LOCALE_ALLOWED_LIST}.
	 * 
	 * @return list of allowed locales
	 * @should return at least one locale if no locales defined in database yet
	 * @should not fail if not global property for locales allowed defined yet
	 */
	@Transactional(readOnly = true)
	public List<Locale> getAllowedLocales();
	
	/**
	 * Gets the list of locales for which localized messages are available for the user interface
	 * (presentation layer). This set includes all the available locales (as indicated by the
	 * MessageSourceService) filtered by the allowed locales (as indicated by this
	 * AdministrationService).
	 * 
	 * @return list of allowed presentation locales TODO change this return type to list?
	 * @should return at least one locale if no locales defined in database yet
	 * @should not return more locales than message source service locales
	 */
	@Transactional(readOnly = true)
	public Set<Locale> getPresentationLocales();
	
}
