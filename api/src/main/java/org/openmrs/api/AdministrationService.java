/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import org.openmrs.GlobalProperty;
import org.openmrs.ImplementationId;
import org.openmrs.OpenmrsObject;
import org.openmrs.User;
import org.openmrs.annotation.Authorized;
import org.openmrs.api.db.AdministrationDAO;
import org.openmrs.util.HttpClient;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.PrivilegeConstants;
import org.openmrs.validator.ValidateUtil;
import org.springframework.validation.Errors;

/**
 * Contains methods pertaining to doing some administrative tasks in OpenMRS
 * <p>
 * Use:<br>
 * 
 * <pre>
 * 
 * List&lt;GlobalProperty&gt; globalProperties = Context.getAdministrationService().getGlobalProperties();
 * </pre>
 * 
 * @see org.openmrs.api.context.Context
 */
public interface AdministrationService extends OpenmrsService {
	
	/**
	 * Used by Spring to set the specific/chosen database access implementation
	 * 
	 * @param dao The dao implementation to use
	 */
	public void setAdministrationDAO(AdministrationDAO dao);
										
	/**
	 * Get a global property by its uuid. There should be only one of these in the database (well,
	 * in the world actually). If multiple are found, an error is thrown.
	 * 
	 * @return the global property matching the given uuid
	 * @should find object given valid uuid
	 * @should return null if no object found with given uuid
	 */
	public GlobalProperty getGlobalPropertyByUuid(String uuid) throws APIException;
	
	/**
	 * Get a listing or important variables used in openmrs
	 * 
	 * @return a map from variable name to variable value
	 * @should return all registered system variables
	 */
	
	@Authorized(PrivilegeConstants.VIEW_ADMIN_FUNCTIONS)
	public SortedMap<String, String> getSystemVariables() throws APIException;
	
	/**
	 * Get a map of all the System Information. Java, user, time, runtime properties, etc
	 * 
	 * @return a map from variable name to a map of the information
	 * @should return all system information
	 */
	@Authorized(PrivilegeConstants.VIEW_ADMIN_FUNCTIONS)
	public Map<String, Map<String, String>> getSystemInformation() throws APIException;
	
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
	 * @should get property in case insensitive way
	 */
	public String getGlobalProperty(String propertyName) throws APIException;
	
	/**
	 * Gets the global property that has the given <code>propertyName</code>
	 * <p>
	 * If propertyName is not found in the list of Global Properties currently in the database, a
	 * <code>defaultValue</code> is returned
	 * <p>
	 * This method should not have any authorization check
	 * 
	 * @param propertyName property key to look for
	 * @param defaultValue value to return if propertyName is not found
	 * @return value of propertyName property or defaultValue if none
	 * @should return default value if property name does not exist
	 * @should not fail with null default value
	 */
	public String getGlobalProperty(String propertyName, String defaultValue) throws APIException;
	
	/**
	 * Gets the global property that has the given <code>propertyName</code>
	 * 
	 * @param propertyName property key to look for
	 * @return the global property that matches the given <code>propertyName</code>
	 * @should return null when no global property match given property name
	 */
	public GlobalProperty getGlobalPropertyObject(String propertyName);
	
	/**
	 * Gets all global properties that begin with <code>prefix</code>.
	 * 
	 * @param prefix The beginning of the property name to match.
	 * @return a <code>List</code> of <code>GlobalProperty</code>s that match <code>prefix</code>
	 * @since 1.5
	 * @should return all relevant global properties in the database
	 */
	public List<GlobalProperty> getGlobalPropertiesByPrefix(String prefix);
	
	/**
	 * Gets all global properties that end with <code>suffix</code>.
	 * 
	 * @param suffix The end of the property name to match.
	 * @return a <code>List</code> of <code>GlobalProperty</code>s that match <code>.*suffix</code>
	 * @since 1.6
	 * @should return all relevant global properties in the database
	 */
	public List<GlobalProperty> getGlobalPropertiesBySuffix(String suffix);
	
	/**
	 * Get a list of all global properties in the system
	 * 
	 * @return list of global properties
	 * @should return all global properties in the database
	 */
	@Authorized(PrivilegeConstants.GET_GLOBAL_PROPERTIES)
	public List<GlobalProperty> getAllGlobalProperties() throws APIException;
	
	/**
	 * Save the given list of global properties to the database.
	 * 
	 * @param props list of GlobalProperty objects to save
	 * @return the saved global properties
	 * @should save all global properties to the database
	 * @should not fail with empty list
	 * @should assign uuid to all new properties
	 * @should save properties with case difference only
	 */
	@Authorized(PrivilegeConstants.MANAGE_GLOBAL_PROPERTIES)
	public List<GlobalProperty> saveGlobalProperties(List<GlobalProperty> props) throws APIException;
	
	/**
	 * Completely remove the given global property from the database
	 * 
	 * @param globalProperty the global property to delete/remove from the database
	 * @throws APIException
	 * @should delete global property from database
	 */
	@Authorized(PrivilegeConstants.PURGE_GLOBAL_PROPERTIES)
	public void purgeGlobalProperty(GlobalProperty globalProperty) throws APIException;
	
	/**
	 * Completely remove the given global properties from the database
	 * 
	 * @param globalProperties the global properties to delete/remove from the database
	 * @throws APIException
	 * @should delete global properties from database
	 */
	@Authorized(PrivilegeConstants.PURGE_GLOBAL_PROPERTIES)
	public void purgeGlobalProperties(List<GlobalProperty> globalProperties) throws APIException;
	
	/**
	 * Save the given global property to the database. If the global property already exists,
	 * then it will be overwritten
	 * 
	 * @param propertyName the name of the global property to save
	 * @param propertyValue the value of the global property to save
	 * @should create global property in database
	 * @should overwrite global property if exists
	 * @should save a global property whose typed value is handled by a custom datatype
	 */
	public void setGlobalProperty(String propertyName, String propertyValue);
	
	/**
	 * Overwrites the value of the global property if it already exists. If the global property does
	 * not exist, an exception will be thrown
	 * @since 1.10
	 * @param propertyName  the name of the global property to overwrite
	 * @param propertyValue  the value of the global property to overwrite
	 * @throws IllegalStateException
	 * @should update global property in database
	 * @should fail if global property being updated does not already exist
	 * @should update a global property whose typed value is handled by a custom datatype
	 */
	public void updateGlobalProperty(String propertyName, String propertyValue) throws IllegalStateException;
	
	/**
	 * Save the given global property to the database
	 * 
	 * @param gp global property to save
	 * @return the saved global property
	 * @throws APIException
	 * @should create global property in database
	 * @should overwrite global property if exists
	 * @should not allow different properties to have the same string with different case
	 * @should save a global property whose typed value is handled by a custom datatype
	 * @should evict all entries of search locale cache
	 */
	@Authorized(PrivilegeConstants.MANAGE_GLOBAL_PROPERTIES)
	public GlobalProperty saveGlobalProperty(GlobalProperty gp) throws APIException;
	
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
	@Authorized(PrivilegeConstants.SQL_LEVEL_ACCESS)
	public List<List<Object>> executeSQL(String sql, boolean selectOnly) throws APIException;
	
	/**
	 * Get the implementation id stored for this server Returns null if no implementation id has
	 * been successfully set yet
	 * 
	 * @return ImplementationId object that is this implementation's unique id
	 * @should return null if no implementation id is defined yet
	 */
	@Authorized(PrivilegeConstants.MANAGE_IMPLEMENTATION_ID)
	public ImplementationId getImplementationId() throws APIException;
	
	/**
	 * Set the given <code>implementationId</code> as this implementation's unique id
	 * 
	 * @param implementationId the ImplementationId to save
	 * @throws APIException if implementationId is empty or is invalid according to central id
	 *             server
	 * @should create implementation id in database
	 * @should overwrite implementation id in database if exists
	 * @should not fail if given implementationId is null
	 * @should throw APIException if given empty implementationId object
	 * @should throw APIException if given a caret in the implementationId code
	 * @should throw APIException if given a pipe in the implementationId code
	 * @should set uuid on implementation id global property
	 */
	@Authorized(PrivilegeConstants.MANAGE_IMPLEMENTATION_ID)
	public void setImplementationId(ImplementationId implementationId) throws APIException;
	
	/**
	 * Gets the list of locales which the administrator has allowed for use on the system. This is
	 * specified with a global property named
	 * {@link OpenmrsConstants#GLOBAL_PROPERTY_LOCALE_ALLOWED_LIST}.
	 * 
	 * @return list of allowed locales
	 * @should return at least one locale if no locales defined in database yet
	 * @should not fail if not global property for locales allowed defined yet
	 * @should not return duplicates even if the global property has them
	 */
	public List<Locale> getAllowedLocales();
	
	/**
	 * Gets the list of locales for which localized messages are available for the user interface
	 * (presentation layer). This set includes all the available locales (as indicated by the
	 * MessageSourceService) filtered by the allowed locales (as indicated by this
	 * AdministrationService).
	 * 
	 * @return list of allowed presentation locales
	 * @should return at least one locale if no locales defined in database yet
	 * @should not return more locales than message source service locales
	 * @should return only country locale if both country locale and language locale are specified in allowed list
	 * @should return all country locales if language locale and no country locales are specified in allowed list
	 * @should return language locale if country locale is specified in allowed list but country locale message file is missing
	 * @should return language locale if it is specified in allowed list and there are no country locale message files available
	 */
	public Set<Locale> getPresentationLocales();
	
	/**
	 * Returns a global property according to the type specified
	 * 
	 * @param <T>
	 * @param propertyName
	 * @should get property value in the proper type specified
	 * @should return default value if property name does not exist
	 * @return property value in the type of the default value
	 * @since 1.7
	 */
	public <T> T getGlobalPropertyValue(String propertyName, T defaultValue) throws APIException;
	
	/**
	 * @param aClass class of object getting length for
	 * @param fieldName name of the field to get the length for
	 * @return the max field length of a property
	 */
	public int getMaximumPropertyLength(Class<? extends OpenmrsObject> aClass, String fieldName);
	
	/**
	 * Performs validation in the manual flush mode to prevent any premature flushes.
	 * <p>
	 * Used by {@link ValidateUtil#validate(Object)}.
	 * 
	 * @since 1.9
	 * @param object
	 * @param errors
	 * @should pass for a valid object
	 * @should fail for an invalid object
	 * @should throw throw APIException if the input is null
	 */
	public void validate(Object object, Errors errors) throws APIException;

	/**
	 * Returns a list of locales used by the user when searching.
	 *
	 * @param currentLocale currently selected locale
	 * @param user authenticated user
	 * @return
	 * @throws APIException
     */
	public List<Locale> getSearchLocales(Locale currentLocale, User user) throws APIException;

	/**
	 * Returns a list of locales used by the user when searching.
	 * <p>
	 * The list is constructed from a currently selected locale and allowed user proficient locales.
	 * 
	 * @return locales
	 * @throws APIException
	 * @since 1.8.4, 1.9.1, 1.10
	 * @should include currently selected full locale and language
	 * @should include users proficient locales
	 * @should exclude not allowed locales
	 * @should cache results for a user
	 */
	public List<Locale> getSearchLocales() throws APIException;
	
	/**
	 * Used by Spring to set the http client for accessing the openmrs implementation service
	 *
	 * @param implementationHttpClient The implementation http client
	 */
	public void setImplementationIdHttpClient(HttpClient implementationHttpClient);
	
	/**
	 * Reads a GP which specifies if database string comparison is case sensitive.
	 * <p>
	 * It is an optimisation parameter for MySQL, which can speed up searching if set to <b>false</b>.
	 * See http://dev.mysql.com/doc/refman/5.7/en/case-sensitivity.html
	 * <p>
	 * It is set to <b>true</b> by default.
	 * 
	 * @return true if database string comparison is case sensitive
	 * @since 1.9.9, 1.10.2, 1.11
	 */
	public boolean isDatabaseStringComparisonCaseSensitive();
}
