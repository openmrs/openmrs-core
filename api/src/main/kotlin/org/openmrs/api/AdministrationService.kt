/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api

import org.openmrs.GlobalProperty
import org.openmrs.ImplementationId
import org.openmrs.OpenmrsObject
import org.openmrs.User
import org.openmrs.annotation.Authorized
import org.openmrs.api.db.AdministrationDAO
import org.openmrs.util.HttpClient
import org.openmrs.util.PrivilegeConstants
import org.springframework.validation.Errors
import java.util.Locale
import java.util.SortedMap

/**
 * Contains methods pertaining to doing some administrative tasks in OpenMRS.
 *
 * Use:
 * ```
 * val globalProperties = Context.getAdministrationService().getAllGlobalProperties()
 * ```
 *
 * @see org.openmrs.api.context.Context
 */
interface AdministrationService : OpenmrsService {

    companion object {
        const val GP_SUFFIX_SERIALIZER_WHITELIST_TYPES = ".serializer.whitelist.types"
        const val GP_SERIALIZER_WHITELIST_HIERARCHY_TYPES_PREFIX = "hierarchyOf:"
    }

    /**
     * Used by Spring to set the specific/chosen database access implementation.
     *
     * @param dao The dao implementation to use
     */
    fun setAdministrationDAO(dao: AdministrationDAO)

    /**
     * Get a global property by its uuid. There should be only one of these in the database (well,
     * in the world actually). If multiple are found, an error is thrown.
     *
     * @param uuid the global property uuid
     * @return the global property matching the given uuid
     */
    @Authorized(PrivilegeConstants.GET_GLOBAL_PROPERTIES)
    fun getGlobalPropertyByUuid(uuid: String): GlobalProperty?

    /**
     * Get a listing of important variables used in openmrs.
     *
     * @return a map from variable name to variable value
     */
    @Authorized(PrivilegeConstants.VIEW_ADMIN_FUNCTIONS)
    fun getSystemVariables(): SortedMap<String, String>

    /**
     * Get a map of all the System Information. Java, user, time, runtime properties, etc.
     *
     * @return a map from variable name to a map of the information
     */
    @Authorized(PrivilegeConstants.VIEW_ADMIN_FUNCTIONS)
    fun getSystemInformation(): Map<String, Map<String, String>>

    /**
     * Gets the global property that has the given propertyName.
     *
     * If propertyName is not found in the list of Global Properties currently in the
     * database, a null value is returned. This method should not have any authorization check.
     *
     * @param propertyName property key to look for
     * @return value of property returned or null if none
     * @see getGlobalProperty
     */
    @Authorized(PrivilegeConstants.GET_GLOBAL_PROPERTIES)
    fun getGlobalProperty(propertyName: String): String?

    /**
     * Gets the global property that has the given propertyName.
     *
     * If propertyName is not found in the list of Global Properties currently in the database, a
     * defaultValue is returned.
     *
     * This method should not have any authorization check.
     *
     * @param propertyName property key to look for
     * @param defaultValue value to return if propertyName is not found
     * @return value of propertyName property or defaultValue if none
     */
    @Authorized(PrivilegeConstants.GET_GLOBAL_PROPERTIES)
    fun getGlobalProperty(propertyName: String, defaultValue: String?): String?

    /**
     * Gets the global property that has the given propertyName.
     *
     * @param propertyName property key to look for
     * @return the global property that matches the given propertyName
     */
    @Authorized(PrivilegeConstants.GET_GLOBAL_PROPERTIES)
    fun getGlobalPropertyObject(propertyName: String): GlobalProperty?

    /**
     * Gets all global properties that begin with prefix.
     *
     * @param prefix The beginning of the property name to match.
     * @return a List of GlobalProperty that match prefix
     * @since 1.5
     */
    @Authorized(PrivilegeConstants.GET_GLOBAL_PROPERTIES)
    fun getGlobalPropertiesByPrefix(prefix: String): List<GlobalProperty>

    /**
     * Gets all global properties that end with suffix.
     *
     * @param suffix The end of the property name to match.
     * @return a List of GlobalProperty that match .*suffix
     * @since 1.6
     */
    @Authorized(PrivilegeConstants.GET_GLOBAL_PROPERTIES)
    fun getGlobalPropertiesBySuffix(suffix: String): List<GlobalProperty>

    /**
     * Get a list of all global properties in the system.
     *
     * @return list of global properties
     */
    @Authorized(PrivilegeConstants.GET_GLOBAL_PROPERTIES)
    fun getAllGlobalProperties(): List<GlobalProperty>

    /**
     * Save the given list of global properties to the database.
     *
     * @param props list of GlobalProperty objects to save
     * @return the saved global properties
     */
    @Authorized(PrivilegeConstants.MANAGE_GLOBAL_PROPERTIES)
    fun saveGlobalProperties(props: @JvmSuppressWildcards List<GlobalProperty>): List<GlobalProperty>

    /**
     * Completely remove the given global property from the database.
     *
     * @param globalProperty the global property to delete/remove from the database
     * @throws APIException if deletion fails
     */
    @Authorized(PrivilegeConstants.PURGE_GLOBAL_PROPERTIES)
    @Throws(APIException::class)
    fun purgeGlobalProperty(globalProperty: GlobalProperty)

    /**
     * Completely remove the given global properties from the database.
     *
     * @param globalProperties the global properties to delete/remove from the database
     * @throws APIException if deletion fails
     */
    @Authorized(PrivilegeConstants.PURGE_GLOBAL_PROPERTIES)
    @Throws(APIException::class)
    fun purgeGlobalProperties(globalProperties: @JvmSuppressWildcards List<GlobalProperty>)

    /**
     * Save the given global property to the database. If the global property already exists,
     * then it will be overwritten.
     *
     * @param propertyName the name of the global property to save
     * @param propertyValue the value of the global property to save
     */
    @Authorized(PrivilegeConstants.MANAGE_GLOBAL_PROPERTIES)
    fun setGlobalProperty(propertyName: String, propertyValue: String)

    /**
     * Overwrites the value of the global property if it already exists. If the global property does
     * not exist, an exception will be thrown.
     *
     * @since 1.10
     * @param propertyName the name of the global property to overwrite
     * @param propertyValue the value of the global property to overwrite
     * @throws IllegalStateException if the property does not exist
     */
    @Authorized(PrivilegeConstants.MANAGE_GLOBAL_PROPERTIES)
    @Throws(IllegalStateException::class)
    fun updateGlobalProperty(propertyName: String, propertyValue: String)

    /**
     * Save the given global property to the database.
     *
     * @param gp global property to save
     * @return the saved global property
     * @throws APIException if saving fails
     */
    @Authorized(PrivilegeConstants.MANAGE_GLOBAL_PROPERTIES)
    @Throws(APIException::class)
    fun saveGlobalProperty(gp: GlobalProperty): GlobalProperty

    /**
     * Allows code to be notified when a global property is created/edited/deleted.
     *
     * @see GlobalPropertyListener
     * @param listener The listener to register
     */
    fun addGlobalPropertyListener(listener: GlobalPropertyListener)

    /**
     * Removes a GlobalPropertyListener previously registered by
     * [addGlobalPropertyListener].
     *
     * @param listener the listener to remove
     */
    fun removeGlobalPropertyListener(listener: GlobalPropertyListener)

    /**
     * Runs the sql on the database. If selectOnly is flagged then any
     * non-select sql statements will be rejected.
     *
     * @param sql the SQL to execute
     * @param selectOnly whether to restrict to SELECT statements only
     * @return ResultSet
     * @throws APIException if execution fails
     */
    @Authorized(PrivilegeConstants.SQL_LEVEL_ACCESS)
    @Throws(APIException::class)
    fun executeSQL(sql: String, selectOnly: Boolean): List<List<Any>>

    /**
     * Get the implementation id stored for this server. Returns null if no implementation id has
     * been successfully set yet.
     *
     * @return ImplementationId object that is this implementation's unique id
     */
    @Authorized(PrivilegeConstants.MANAGE_IMPLEMENTATION_ID)
    fun getImplementationId(): ImplementationId?

    /**
     * Set the given implementationId as this implementation's unique id.
     *
     * @param implementationId the ImplementationId to save
     * @throws APIException if implementationId is empty or is invalid according to central id server
     */
    @Authorized(PrivilegeConstants.MANAGE_IMPLEMENTATION_ID)
    @Throws(APIException::class)
    fun setImplementationId(implementationId: ImplementationId?)

    /**
     * Gets the list of locales which the administrator has allowed for use on the system. This is
     * specified with a global property named [org.openmrs.util.OpenmrsConstants.GLOBAL_PROPERTY_LOCALE_ALLOWED_LIST].
     *
     * @return list of allowed locales
     */
    fun getAllowedLocales(): List<Locale>

    /**
     * Gets the list of locales for which localized messages are available for the user interface
     * (presentation layer). This set includes all the available locales (as indicated by the
     * MessageSourceService) filtered by the allowed locales (as indicated by this
     * AdministrationService).
     *
     * @return list of allowed presentation locales
     */
    fun getPresentationLocales(): Set<Locale>

    /**
     * Returns a global property according to the type specified.
     *
     * @param T the type parameter
     * @param propertyName the property name
     * @param defaultValue the default value if property is not found
     * @return property value in the type of the default value
     * @since 1.7
     */
    @Authorized(PrivilegeConstants.GET_GLOBAL_PROPERTIES)
    fun <T> getGlobalPropertyValue(propertyName: String, defaultValue: T): T

    /**
     * Gets the max field length of a property.
     *
     * @param aClass class of object getting length for
     * @param fieldName name of the field to get the length for
     * @return the max field length of a property
     */
    fun getMaximumPropertyLength(aClass: Class<out OpenmrsObject>, fieldName: String): Long

    /**
     * Performs validation in the manual flush mode to prevent any premature flushes.
     *
     * Used by ValidateUtil.validate(Object).
     *
     * @since 1.9
     * @param obj the object to validate
     * @param errors the errors object to populate
     */
    fun validate(obj: Any, errors: Errors)

    /**
     * Returns a list of locales used by the user when searching.
     *
     * @param currentLocale currently selected locale
     * @param user authenticated user
     * @return list of search locales
     * @throws APIException if retrieval fails
     */
    @Throws(APIException::class)
    fun getSearchLocales(currentLocale: Locale, user: User): List<Locale>

    /**
     * Returns a list of locales used by the user when searching.
     *
     * The list is constructed from a currently selected locale and allowed user proficient locales.
     *
     * @return locales
     * @throws APIException if retrieval fails
     * @since 1.8.4, 1.9.1, 1.10
     */
    @Throws(APIException::class)
    fun getSearchLocales(): List<Locale>

    /**
     * Used by Spring to set the http client for accessing the openmrs implementation service.
     *
     * @param implementationHttpClient The implementation http client
     */
    fun setImplementationIdHttpClient(implementationHttpClient: HttpClient)

    /**
     * Reads a GP which specifies if database string comparison is case sensitive.
     *
     * It is an optimization parameter for MySQL, which can speed up searching if set to false.
     *
     * It is set to true by default.
     *
     * @return true if database string comparison is case sensitive
     * @since 1.9.9, 1.10.2, 1.11
     */
    fun isDatabaseStringComparisonCaseSensitive(): Boolean

    /**
     * Updates PostgreSQL sequence values after insertions are done from core data or concepts.
     *
     * Unlike MySQL which uses identifier strategy, PostgreSQL follows sequence strategy.
     * This method bridges the gap between these two strategies.
     *
     * @since 2.4
     */
    fun updatePostgresSequence()

    /**
     * Returns a list of packages and/or individual classes including hierarchy of OpenmrsObject, OpenmrsMetadata,
     * OpenmrsData and other common OpenMRS classes as well as any whitelists defined through GPs with the
     * '.serializer.whitelist.types' suffix that are considered to be safe for deserializing.
     *
     * It is the responsibility of the serializer to block any unlisted classes from being deserialized and posing
     * security risk. It is especially important for serializers using XStream.
     *
     * @since 2.7.0, 2.6.2, 2.5.13
     * @return a list of packages and/or classes
     */
    fun getSerializerWhitelistTypes(): List<String>
}
