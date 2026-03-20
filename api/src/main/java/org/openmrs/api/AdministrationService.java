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
import org.openmrs.module.Module;
import org.openmrs.util.DatabaseUpdateException;
import org.openmrs.util.HttpClient;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.PrivilegeConstants;
import org.openmrs.validator.ValidateUtil;
import org.springframework.validation.Errors;

/**
 * Contains methods pertaining to doing some administrative tasks in OpenMRS
 * <p>
 * Use:<br>
 * <pre>
 *
 * List&lt;GlobalProperty&gt; globalProperties = Context.getAdministrationService().getGlobalProperties();
 * </pre>
 *
 * @see org.openmrs.api.context.Context
 */
public interface AdministrationService extends OpenmrsService {

	public static final String GP_SUFFIX_SERIALIZER_WHITELIST_TYPES = ".serializer.whitelist.types";

	public static final String GP_SERIALIZER_WHITELIST_HIERARCHY_TYPES_PREFIX = "hierarchyOf:";

	/**
	 * Used by Spring to set the specific/chosen database access implementation
	 *
	 * @param dao The dao implementation to use
	 */
	public void setAdministrationDAO(AdministrationDAO dao);

	/**
	 * Get a global property by its uuid. There should be only one of these in the database (well, in
	 * the world actually). If multiple are found, an error is thrown.
	 * <p>
	 * <strong>Should</strong> find object given valid uuid<br/>
	 * <strong>Should</strong> return null if no object found with given uuid
	 *
	 * @return the global property matching the given uuid
	 */
	@Authorized(PrivilegeConstants.GET_GLOBAL_PROPERTIES)
	public GlobalProperty getGlobalPropertyByUuid(String uuid);

	/**
	 * Get a listing or important variables used in openmrs
	 * <p>
	 * <strong>Should</strong> return all registered system variables
	 *
	 * @return a map from variable name to variable value
	 */

	@Authorized(PrivilegeConstants.VIEW_ADMIN_FUNCTIONS)
	public SortedMap<String, String> getSystemVariables();

	/**
	 * Get a map of all the System Information. Java, user, time, runtime properties, etc
	 * <p>
	 * <strong>Should</strong> return all system information
	 *
	 * @return a map from variable name to a map of the information
	 */
	@Authorized(PrivilegeConstants.VIEW_ADMIN_FUNCTIONS)
	public Map<String, Map<String, String>> getSystemInformation();

	/**
	 * Gets the global property that has the given <code>propertyName</code>.
	 * <p>
	 * If <code>propertyName</code> is not found in the list of Global Properties currently in the
	 * database, a null value is returned. This method should not have any authorization check.
	 * <p>
	 * <strong>Should</strong> not fail with null propertyName<br/>
	 * <strong>Should</strong> get property value given valid property name<br/>
	 * <strong>Should</strong> get property in case insensitive way
	 *
	 * @param propertyName property key to look for
	 * @return value of property returned or null if none
	 * @see #getGlobalProperty(String, String)
	 */
	@Authorized(PrivilegeConstants.GET_GLOBAL_PROPERTIES)
	public String getGlobalProperty(String propertyName);

	/**
	 * Gets the global property that has the given <code>propertyName</code>
	 * <p>
	 * If propertyName is not found in the list of Global Properties currently in the database, a
	 * <code>defaultValue</code> is returned
	 * <p>
	 * This method should not have any authorization check
	 * <p>
	 * <strong>Should</strong> return default value if property name does not exist<br/>
	 * <strong>Should</strong> not fail with null default value
	 *
	 * @param propertyName property key to look for
	 * @param defaultValue value to return if propertyName is not found
	 * @return value of propertyName property or defaultValue if none
	 */
	@Authorized(PrivilegeConstants.GET_GLOBAL_PROPERTIES)
	public String getGlobalProperty(String propertyName, String defaultValue);

	/**
	 * Gets the global property that has the given <code>propertyName</code>
	 * <p>
	 * <strong>Should</strong> return null when no global property match given property name
	 *
	 * @param propertyName property key to look for
	 * @return the global property that matches the given <code>propertyName</code>
	 */
	@Authorized(PrivilegeConstants.GET_GLOBAL_PROPERTIES)
	public GlobalProperty getGlobalPropertyObject(String propertyName);

	/**
	 * Gets all global properties that begin with <code>prefix</code>.
	 * <p>
	 * <strong>Should</strong> return all relevant global properties in the database
	 *
	 * @param prefix The beginning of the property name to match.
	 * @return a <code>List</code> of <code>GlobalProperty</code>s that match <code>prefix</code>
	 * @since 1.5
	 */
	@Authorized(PrivilegeConstants.GET_GLOBAL_PROPERTIES)
	public List<GlobalProperty> getGlobalPropertiesByPrefix(String prefix);

	/**
	 * Gets all global properties that end with <code>suffix</code>.
	 * <p>
	 * <strong>Should</strong> return all relevant global properties in the database
	 *
	 * @param suffix The end of the property name to match.
	 * @return a <code>List</code> of <code>GlobalProperty</code>s that match <code>.*suffix</code>
	 * @since 1.6
	 */
	@Authorized(PrivilegeConstants.GET_GLOBAL_PROPERTIES)
	public List<GlobalProperty> getGlobalPropertiesBySuffix(String suffix);

	/**
	 * Get a list of all global properties in the system
	 * <p>
	 * <strong>Should</strong> return all global properties in the database
	 *
	 * @return list of global properties
	 */
	@Authorized(PrivilegeConstants.GET_GLOBAL_PROPERTIES)
	public List<GlobalProperty> getAllGlobalProperties();

	/**
	 * Save the given list of global properties to the database.
	 * <p>
	 * <strong>Should</strong> save all global properties to the database<br/>
	 * <strong>Should</strong> not fail with empty list<br/>
	 * <strong>Should</strong> assign uuid to all new properties<br/>
	 * <strong>Should</strong> save properties with case difference only
	 *
	 * @param props list of GlobalProperty objects to save
	 * @return the saved global properties
	 */
	@Authorized(PrivilegeConstants.MANAGE_GLOBAL_PROPERTIES)
	public List<GlobalProperty> saveGlobalProperties(List<GlobalProperty> props);

	/**
	 * Completely remove the given global property from the database
	 * <p>
	 * <strong>Should</strong> delete global property from database
	 *
	 * @param globalProperty the global property to delete/remove from the database
	 * @throws APIException
	 */
	@Authorized(PrivilegeConstants.PURGE_GLOBAL_PROPERTIES)
	public void purgeGlobalProperty(GlobalProperty globalProperty);

	/**
	 * Completely remove the given global properties from the database
	 * <p>
	 * <strong>Should</strong> delete global properties from database
	 *
	 * @param globalProperties the global properties to delete/remove from the database
	 * @throws APIException
	 */
	@Authorized(PrivilegeConstants.PURGE_GLOBAL_PROPERTIES)
	public void purgeGlobalProperties(List<GlobalProperty> globalProperties);

	/**
	 * Save the given global property to the database. If the global property already exists, then it
	 * will be overwritten
	 * <p>
	 * <strong>Should</strong> create global property in database<br/>
	 * <strong>Should</strong> overwrite global property if exists<br/>
	 * <strong>Should</strong> save a global property whose typed value is handled by a custom datatype
	 *
	 * @param propertyName the name of the global property to save
	 * @param propertyValue the value of the global property to save
	 */
	@Authorized(PrivilegeConstants.MANAGE_GLOBAL_PROPERTIES)
	public void setGlobalProperty(String propertyName, String propertyValue);

	/**
	 * Overwrites the value of the global property if it already exists. If the global property does not
	 * exist, an exception will be thrown
	 * <p>
	 * <strong>Should</strong> update global property in database<br/>
	 * <strong>Should</strong> fail if global property being updated does not already exist<br/>
	 * <strong>Should</strong> update a global property whose typed value is handled by a custom
	 * datatype
	 *
	 * @since 1.10
	 * @param propertyName the name of the global property to overwrite
	 * @param propertyValue the value of the global property to overwrite
	 * @throws IllegalStateException
	 */
	@Authorized(PrivilegeConstants.MANAGE_GLOBAL_PROPERTIES)
	public void updateGlobalProperty(String propertyName, String propertyValue);

	/**
	 * Save the given global property to the database
	 * <p>
	 * <strong>Should</strong> create global property in database<br/>
	 * <strong>Should</strong> overwrite global property if exists<br/>
	 * <strong>Should</strong> not allow different properties to have the same string with different
	 * case<br/>
	 * <strong>Should</strong> save a global property whose typed value is handled by a custom
	 * datatype<br/>
	 * <strong>Should</strong> evict all entries of search locale cache
	 *
	 * @param gp global property to save
	 * @return the saved global property
	 * @throws APIException
	 */
	@Authorized(PrivilegeConstants.MANAGE_GLOBAL_PROPERTIES)
	public GlobalProperty saveGlobalProperty(GlobalProperty gp);

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
	 * <p>
	 * <strong>Should</strong> execute sql containing group by
	 *
	 * @param sql
	 * @param selectOnly
	 * @return ResultSet
	 * @throws APIException
	 */
	@Authorized(PrivilegeConstants.SQL_LEVEL_ACCESS)
	public List<List<Object>> executeSQL(String sql, boolean selectOnly);

	/**
	 * Get the implementation id stored for this server Returns null if no implementation id has been
	 * successfully set yet
	 * <p>
	 * <strong>Should</strong> return null if no implementation id is defined yet
	 *
	 * @return ImplementationId object that is this implementation's unique id
	 */
	@Authorized(PrivilegeConstants.MANAGE_IMPLEMENTATION_ID)
	public ImplementationId getImplementationId();

	/**
	 * Set the given <code>implementationId</code> as this implementation's unique id
	 * <p>
	 * <strong>Should</strong> create implementation id in database<br/>
	 * <strong>Should</strong> overwrite implementation id in database if exists<br/>
	 * <strong>Should</strong> not fail if given implementationId is null<br/>
	 * <strong>Should</strong> throw APIException if given empty implementationId object<br/>
	 * <strong>Should</strong> throw APIException if given a caret in the implementationId code<br/>
	 * <strong>Should</strong> throw APIException if given a pipe in the implementationId code<br/>
	 * <strong>Should</strong> set uuid on implementation id global property
	 *
	 * @param implementationId the ImplementationId to save
	 * @throws APIException if implementationId is empty or is invalid according to central id server
	 */
	@Authorized(PrivilegeConstants.MANAGE_IMPLEMENTATION_ID)
	public void setImplementationId(ImplementationId implementationId);

	/**
	 * Gets the list of locales which the administrator has allowed for use on the system. This is
	 * specified with a global property named
	 * {@link OpenmrsConstants#GLOBAL_PROPERTY_LOCALE_ALLOWED_LIST}.
	 * <p>
	 * <strong>Should</strong> return at least one locale if no locales defined in database yet<br/>
	 * <strong>Should</strong> not fail if not global property for locales allowed defined yet<br/>
	 * <strong>Should</strong> not return duplicates even if the global property has them
	 *
	 * @return list of allowed locales
	 */
	public List<Locale> getAllowedLocales();

	/**
	 * Gets the list of locales for which localized messages are available for the user interface
	 * (presentation layer). This set includes all the available locales (as indicated by the
	 * MessageSourceService) filtered by the allowed locales (as indicated by this
	 * AdministrationService).
	 * <p>
	 * <strong>Should</strong> return at least one locale if no locales defined in database yet<br/>
	 * <strong>Should</strong> not return more locales than message source service locales<br/>
	 * <strong>Should</strong> return only country locale if both country locale and language locale are
	 * specified in allowed list<br/>
	 * <strong>Should</strong> return all country locales if language locale and no country locales are
	 * specified in allowed list<br/>
	 * <strong>Should</strong> return language locale if country locale is specified in allowed list but
	 * country locale message file is missing<br/>
	 * <strong>Should</strong> return language locale if it is specified in allowed list and there are
	 * no country locale message files available
	 *
	 * @return list of allowed presentation locales
	 */
	public Set<Locale> getPresentationLocales();

	/**
	 * Returns a global property according to the type specified
	 * <p>
	 * <strong>Should</strong> get property value in the proper type specified<br/>
	 * <strong>Should</strong> return default value if property name does not exist
	 *
	 * @param <T>
	 * @param propertyName
	 * @return property value in the type of the default value
	 * @since 1.7
	 */
	@Authorized(PrivilegeConstants.GET_GLOBAL_PROPERTIES)
	public <T> T getGlobalPropertyValue(String propertyName, T defaultValue);

	/**
	 * @param aClass class of object getting length for
	 * @param fieldName name of the field to get the length for
	 * @return the max field length of a property
	 */
	public long getMaximumPropertyLength(Class<? extends OpenmrsObject> aClass, String fieldName);

	/**
	 * Performs validation in the manual flush mode to prevent any premature flushes.
	 * <p>
	 * Used by {@link ValidateUtil#validate(Object)}.
	 * <p>
	 * <strong>Should</strong> pass for a valid object<br/>
	 * <strong>Should</strong> fail for an invalid object<br/>
	 * <strong>Should</strong> throw throw APIException if the input is null
	 *
	 * @since 1.9
	 * @param object
	 * @param errors
	 */
	public void validate(Object object, Errors errors);

	/**
	 * Returns a list of locales used by the user when searching.
	 *
	 * @param currentLocale currently selected locale
	 * @param user authenticated user
	 * @return
	 * @throws APIException
	 */
	public List<Locale> getSearchLocales(Locale currentLocale, User user);

	/**
	 * Returns a list of locales used by the user when searching.
	 * <p>
	 * The list is constructed from a currently selected locale and allowed user proficient locales.
	 * <p>
	 * <strong>Should</strong> include currently selected full locale and language<br/>
	 * <strong>Should</strong> include users proficient locales<br/>
	 * <strong>Should</strong> exclude not allowed locales<br/>
	 * <strong>Should</strong> cache results for a user
	 *
	 * @return locales
	 * @throws APIException
	 * @since 1.8.4, 1.9.1, 1.10
	 */
	public List<Locale> getSearchLocales();

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

	/**
	 * <ul>
	 * <li>Unlike MySQL which uses identifier strategy, PostgreSQL follows sequence strategy</li>
	 * <li>So as to bridge the gap between these two strategies, this method has been created.</li>
	 * <li>It will perform task of updating the sequence values after insertions are done from core data
	 * or concepts are inserted (present in Reference Metadata Module)</li>
	 * </ul>
	 *
	 * @since 2.4
	 */
	public void updatePostgresSequence();

	/**
	 * Returns a list of packages and/or individual classes including hierarchy of OpenmrsObject,
	 * OpenmmrsMetadata, OpenmrsData and other common OpenMRS classes as well as any whitelists defined
	 * through GPs with the '.serializer.whitelist.types' suffix that are considered to be safe for
	 * deserializing. It is the responsibility of the serializer to block any unlisted classes from
	 * being deserialized and posing security risk. It is especially important for serializers using
	 * XStream.
	 * <p>
	 * <strong>Should</strong> return packages and individual classes defined in GPs<br/>
	 * <strong>Should</strong> return default common classes if no GPs defined
	 *
	 * @since 2.7.0, 2.6.2, 2.5.13
	 * @return a list of packages and/or classes
	 */
	List<String> getSerializerWhitelistTypes();

	/**
	 * Checks whether a core setup needs to be run due to a version change.
	 *
	 * @since 2.9.0
	 * @return true if core setup should be executed because of a version change, false otherwise
	 */
	boolean isCoreSetupOnVersionChangeNeeded();

	/**
	 * Checks whether a module setup needs to be run due to a version change.
	 *
	 * @since 2.9.0
	 * @param moduleId the identifier of the module to check
	 * @return true if the module setup should be executed because of a version change, false otherwise
	 */
	boolean isModuleSetupOnVersionChangeNeeded(String moduleId);

	/**
	 * Executes the core setup procedures required after a core version change.
	 *
	 * @since 2.9.0
	 * @throws DatabaseUpdateException if the core setup fails
	 */
	void runCoreSetupOnVersionChange() throws DatabaseUpdateException;

	/**
	 * Executes the setup procedures required for a module after a module version change.
	 *
	 * @since 2.9.0
	 * @param module the module for which the setup should be executed
	 */
	void runModuleSetupOnVersionChange(Module module);
}
