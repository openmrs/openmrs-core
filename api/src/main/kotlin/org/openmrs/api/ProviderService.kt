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

import org.openmrs.Person
import org.openmrs.Provider
import org.openmrs.ProviderAttribute
import org.openmrs.ProviderAttributeType
import org.openmrs.ProviderRole
import org.openmrs.annotation.Authorized
import org.openmrs.annotation.Handler
import org.openmrs.util.PrivilegeConstants

/**
 * This service contains methods relating to providers.
 *
 * @since 1.9
 */
@Handler(supports = [Provider::class])
interface ProviderService : OpenmrsService {

    /**
     * Gets all providers. Includes retired Provider. This method delegates to the
     * [getAllProviders] method.
     *
     * @return a list of provider objects.
     */
    @Authorized(PrivilegeConstants.GET_PROVIDERS)
    fun getAllProviders(): List<Provider>

    /**
     * Gets all providers.
     *
     * @param includeRetired if true, retired providers are also included
     * @return a list of provider objects
     */
    @Authorized(PrivilegeConstants.GET_PROVIDERS)
    fun getAllProviders(includeRetired: Boolean): List<Provider>

    /**
     * Retires a given Provider.
     *
     * @param provider provider to retire
     * @param reason reason why the provider is retired
     */
    @Authorized(PrivilegeConstants.MANAGE_PROVIDERS)
    fun retireProvider(provider: Provider, reason: String)

    /**
     * Unretire a given Provider.
     *
     * @param provider provider to unretire
     * @return the unretired provider
     */
    @Authorized(PrivilegeConstants.MANAGE_PROVIDERS)
    fun unretireProvider(provider: Provider): Provider

    /**
     * Deletes a given Provider.
     *
     * @param provider provider to be deleted
     */
    @Authorized(PrivilegeConstants.PURGE_PROVIDERS)
    fun purgeProvider(provider: Provider)

    /**
     * Gets a provider by its provider id.
     *
     * @param providerId the provider id
     * @return the provider by its id
     */
    @Authorized(PrivilegeConstants.GET_PROVIDERS)
    fun getProvider(providerId: Int?): Provider?

    /**
     * Saves a provider.
     *
     * @param provider the provider to save
     * @return the Provider object after saving it in the database
     */
    @Authorized(PrivilegeConstants.MANAGE_PROVIDERS)
    fun saveProvider(provider: Provider): Provider

    /**
     * Gets a provider by its uuid.
     *
     * @param uuid the provider uuid
     * @return the Provider object having the given uuid
     */
    @Authorized(PrivilegeConstants.GET_PROVIDERS)
    fun getProviderByUuid(uuid: String): Provider?

    /**
     * Gets the Providers for the given person.
     *
     * @param person the person
     * @return providers or empty collection
     */
    @Authorized(PrivilegeConstants.GET_PROVIDERS)
    fun getProvidersByPerson(person: Person): Collection<Provider>

    /**
     * Gets the Providers for the given person including or excluding retired.
     *
     * @param person the person
     * @param includeRetired whether to include retired providers
     * @return providers or empty collection
     * @since 1.10, 1.9.1
     */
    @Authorized(PrivilegeConstants.GET_PROVIDERS)
    fun getProvidersByPerson(person: Person, includeRetired: Boolean): Collection<Provider>

    /**
     * Gets providers matching the query.
     *
     * @param query the search query
     * @param start the starting index
     * @param length the page length
     * @param attributes attributes to match
     * @param includeRetired whether to include retired providers
     * @return the list of Providers given the query, current page and page length
     */
    @Authorized(PrivilegeConstants.GET_PROVIDERS)
    fun getProviders(
        query: String?,
        start: Int?,
        length: Int?,
        attributes: @JvmSuppressWildcards Map<ProviderAttributeType, Any>?,
        includeRetired: Boolean
    ): List<Provider>

    /**
     * Gets providers matching the query (includes retired).
     *
     * @param query the search query
     * @param start the starting index
     * @param length the page length
     * @param attributes attributes to match
     * @return the list of Providers given the query, current page and page length
     */
    @Authorized(PrivilegeConstants.GET_PROVIDERS)
    fun getProviders(
        query: String?,
        start: Int?,
        length: Int?,
        attributes: @JvmSuppressWildcards Map<ProviderAttributeType, Any>?
    ): List<Provider>

    /**
     * Gets the count of providers matching the query (excludes retired).
     *
     * @param query the search query
     * @return Count-Integer
     */
    @Authorized(PrivilegeConstants.GET_PROVIDERS)
    fun getCountOfProviders(query: String?): Integer

    /**
     * Gets the count of providers with a person name or identifier or name that matches the
     * specified query.
     *
     * @param query the text to match
     * @param includeRetired specifies whether retired providers should be included or not
     * @return Count-Integer
     * @since 1.9.4
     */
    @Authorized(PrivilegeConstants.GET_PROVIDERS)
    fun getCountOfProviders(query: String?, includeRetired: Boolean): Integer

    /**
     * Gets all provider attribute types including retired provider attribute types. This method
     * delegates to the [getAllProviderAttributeTypes] method.
     *
     * @return a list of provider attribute type objects.
     */
    fun getAllProviderAttributeTypes(): List<ProviderAttributeType>

    /**
     * Gets all provider attribute types optionally including retired provider attribute types.
     *
     * @param includeRetired boolean value to indicate whether to include retired records or not
     * @return a list of provider attribute type objects.
     */
    fun getAllProviderAttributeTypes(includeRetired: Boolean): List<ProviderAttributeType>

    /**
     * Gets a provider attribute type by its id.
     *
     * @param providerAttributeTypeId the provider attribute type id
     * @return the provider type attribute by its id
     */
    fun getProviderAttributeType(providerAttributeTypeId: Int?): ProviderAttributeType?

    /**
     * Get a provider attribute type by its uuid.
     *
     * @param uuid the uuid of the provider attribute type
     * @return the provider attribute type for the given uuid
     */
    fun getProviderAttributeTypeByUuid(uuid: String): ProviderAttributeType?

    /**
     * Get a provider attribute type by its name.
     *
     * @param name the name of the provider attribute type
     * @return the provider attribute type for the given name
     * @since 2.7.0, 2.6.3
     */
    @Authorized(PrivilegeConstants.GET_PROVIDER_ATTRIBUTE_TYPES)
    fun getProviderAttributeTypeByName(name: String): ProviderAttributeType?

    /**
     * Get a provider attribute by its providerAttributeID.
     *
     * @param providerAttributeID the provider attribute ID of the providerAttribute
     * @return the provider attribute for the given providerAttributeID
     */
    fun getProviderAttribute(providerAttributeID: Int?): ProviderAttribute?

    /**
     * Get a provider attribute by its providerAttributeUuid.
     *
     * @param uuid the provider attribute uuid of the providerAttribute
     * @return the provider attribute for the given providerAttributeUuid
     */
    fun getProviderAttributeByUuid(uuid: String): ProviderAttribute?

    /**
     * Save the provider attribute type.
     *
     * @param providerAttributeType the provider attribute type to be saved
     * @return the saved provider attribute type
     */
    fun saveProviderAttributeType(providerAttributeType: ProviderAttributeType): ProviderAttributeType

    /**
     * Retire a provider attribute type.
     *
     * @param providerAttributeType the provider attribute type to be retired
     * @param reason for retiring the provider attribute type
     * @return the retired provider attribute type
     */
    fun retireProviderAttributeType(providerAttributeType: ProviderAttributeType, reason: String): ProviderAttributeType

    /**
     * Un-Retire a provider attribute type.
     *
     * @param providerAttributeType the provider type attribute to unretire
     * @return the unretired provider attribute type
     */
    fun unretireProviderAttributeType(providerAttributeType: ProviderAttributeType): ProviderAttributeType

    /**
     * Deletes a provider attribute type.
     *
     * @param providerAttributeType provider attribute type to be deleted
     */
    fun purgeProviderAttributeType(providerAttributeType: ProviderAttributeType)

    /**
     * Checks if the identifier for the specified provider is unique.
     *
     * @param provider the provider whose identifier to check
     * @return true if the identifier is unique otherwise false
     * @throws APIException if checking fails
     */
    @Authorized(PrivilegeConstants.GET_PROVIDERS)
    @Throws(APIException::class)
    fun isProviderIdentifierUnique(provider: Provider): Boolean

    /**
     * Gets a provider with a matching identifier, this method performs a case insensitive search.
     *
     * @param identifier the identifier to match against
     * @return a Provider
     */
    @Authorized(PrivilegeConstants.GET_PROVIDERS)
    fun getProviderByIdentifier(identifier: String): Provider?

    /**
     * Gets the unknown provider account, i.e. the provider account that matches the uuid specified
     * as the value for the global property [org.openmrs.util.OpenmrsConstants.GP_UNKNOWN_PROVIDER_UUID].
     *
     * @return a Provider
     * @since 1.10
     */
    @Authorized(PrivilegeConstants.GET_PROVIDERS)
    fun getUnknownProvider(): Provider?

    /**
     * Get a ProviderRole by its ID.
     *
     * @param providerRoleId the provider role id
     * @return ProviderRole
     * @since 2.8.0
     */
    @Authorized(PrivilegeConstants.GET_PROVIDER_ROLES)
    fun getProviderRole(providerRoleId: Int?): ProviderRole?

    /**
     * Gets all Provider Roles in the database.
     *
     * @param includeRetired whether to include retired provider roles or not
     * @return list of all provider roles in the system
     * @since 2.8.1
     */
    @Authorized(PrivilegeConstants.GET_PROVIDER_ROLES)
    fun getAllProviderRoles(includeRetired: Boolean): List<ProviderRole>

    /**
     * Get a ProviderRole by its UUID.
     *
     * @param uuid The ProviderRole UUID
     * @return ProviderRole
     * @since 2.8.0
     */
    @Authorized(PrivilegeConstants.GET_PROVIDER_ROLES)
    fun getProviderRoleByUuid(uuid: String): ProviderRole?

    /**
     * Get providers for given roles.
     *
     * @param roles The list of ProviderRole
     * @return a list of matching Provider instances
     * @since 2.8.0
     */
    @Authorized(PrivilegeConstants.GET_PROVIDERS)
    fun getProvidersByRoles(roles: @JvmSuppressWildcards List<ProviderRole>): List<Provider>

    /**
     * Saves/updates a provider role.
     *
     * @param providerRole the provider role to save
     * @return the saved provider role
     * @since 2.8.2
     */
    @Authorized(PrivilegeConstants.MANAGE_PROVIDER_ROLES)
    fun saveProviderRole(providerRole: ProviderRole): ProviderRole

    /**
     * Retires a provider role.
     *
     * @param providerRole the role to retire
     * @param reason the reason the role is being retired
     * @return the retired provider role
     * @since 2.8.2
     */
    @Authorized(PrivilegeConstants.MANAGE_PROVIDER_ROLES)
    fun retireProviderRole(providerRole: ProviderRole, reason: String): ProviderRole

    /**
     * Unretires a provider role.
     *
     * @param providerRole the role to unretire
     * @return the unretired provider role
     * @since 2.8.2
     */
    @Authorized(PrivilegeConstants.MANAGE_PROVIDER_ROLES)
    fun unretireProviderRole(providerRole: ProviderRole): ProviderRole

    /**
     * Deletes a provider role entirely from the database.
     *
     * @param providerRole the provider role to delete
     * @since 2.8.2
     */
    @Authorized(PrivilegeConstants.PURGE_PROVIDER_ROLES)
    fun purgeProviderRole(providerRole: ProviderRole)
}
