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

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.openmrs.Person;
import org.openmrs.Provider;
import org.openmrs.ProviderAttribute;
import org.openmrs.ProviderAttributeType;
import org.openmrs.ProviderRole;
import org.openmrs.annotation.Authorized;
import org.openmrs.annotation.Handler;
import org.openmrs.util.PrivilegeConstants;

/**
 * This service contains methods relating to providers.
 *
 * @since 1.9
 */
@Handler(supports = Provider.class)
public interface ProviderService extends OpenmrsService {

	/**
	 * Gets all providers. includes retired Provider.This method delegates to the
	 * #getAllProviders(boolean) method
	 * <p>
	 * <strong>Should</strong> get all providers
	 *
	 * @return a list of provider objects.
	 */
	@Authorized({ PrivilegeConstants.GET_PROVIDERS })
	public List<Provider> getAllProviders();

	/**
	 * Gets all providers
	 * <p>
	 * <strong>Should</strong> get all providers that are unretired
	 *
	 * @param includeRetired - if true, retired providers are also included
	 */
	@Authorized({ PrivilegeConstants.GET_PROVIDERS })
	public List<Provider> getAllProviders(boolean includeRetired);

	/**
	 * Retires a given Provider
	 * <p>
	 * <strong>Should</strong> retire a provider
	 *
	 * @param provider provider to retire
	 * @param reason reason why the provider is retired
	 */
	@Authorized({ PrivilegeConstants.MANAGE_PROVIDERS })
	public void retireProvider(Provider provider, String reason);

	/**
	 * Unretire a given Provider
	 * <p>
	 * <strong>Should</strong> unretire a provider
	 *
	 * @param provider provider to unretire
	 */
	@Authorized({ PrivilegeConstants.MANAGE_PROVIDERS })
	public Provider unretireProvider(Provider provider);

	/**
	 * Deletes a given Provider
	 * <p>
	 * <strong>Should</strong> delete a provider
	 *
	 * @param provider provider to be deleted
	 */
	@Authorized({ PrivilegeConstants.PURGE_PROVIDERS })
	public void purgeProvider(Provider provider);

	/**
	 * Gets a provider by its provider id
	 * <p>
	 * <strong>Should</strong> get provider given ID
	 *
	 * @param providerId the provider id
	 * @return the provider by it's id
	 */
	@Authorized({ PrivilegeConstants.GET_PROVIDERS })
	public Provider getProvider(Integer providerId);

	/**
	 * <p>
	 * <strong>Should</strong> save a Provider with Person alone<br/>
	 * <strong>Should</strong> not save a Provider person being null
	 *
	 * @param provider
	 * @return the Provider object after saving it in the database
	 */
	@Authorized({ PrivilegeConstants.MANAGE_PROVIDERS })
	public Provider saveProvider(Provider provider);

	/**
	 * <p>
	 * <strong>Should</strong> get provider given Uuid
	 *
	 * @param uuid
	 * @return the Provider object having the given uuid
	 */
	@Authorized({ PrivilegeConstants.GET_PROVIDERS })
	public Provider getProviderByUuid(String uuid);

	/**
	 * Gets the Providers for the given person.
	 * <p>
	 * <strong>Should</strong> return providers for given person<br/>
	 * <strong>Should</strong> fail if person is null
	 *
	 * @param person
	 * @return providers or empty collection
	 */
	@Authorized({ PrivilegeConstants.GET_PROVIDERS })
	public Collection<Provider> getProvidersByPerson(Person person);

	/**
	 * Gets the Providers for the given person including or excluding retired.
	 * <p>
	 * <strong>Should</strong> return all providers by person including retired if includeRetired is
	 * true<br/>
	 * <strong>Should</strong> return all providers by person and exclude retired if includeRetired is
	 * false<br/>
	 * <strong>Should</strong> fail if person is null
	 *
	 * @param person
	 * @param includeRetired
	 * @return providers or empty collection
	 * @since 1.10, 1.9.1
	 */
	@Authorized({ PrivilegeConstants.GET_PROVIDERS })
	public Collection<Provider> getProvidersByPerson(Person person, boolean includeRetired);

	/**
	 * <p>
	 * <strong>Should</strong> fetch provider with given identifier with case in sensitive<br/>
	 * <strong>Should</strong> fetch provider with given name with case in sensitive<br/>
	 * <strong>Should</strong> fetch provider by matching query string with any unVoided PersonName's
	 * Given Name<br/>
	 * <strong>Should</strong> fetch provider by matching query string with any unVoided PersonName's
	 * middleName<br/>
	 * <strong>Should</strong> fetch provider by matching query string with any unVoided Person's
	 * familyName<br/>
	 * <strong>Should</strong> not fetch provider if the query string matches with any voided Person
	 * name for that<br/>
	 * <strong>Should</strong> get all visits with given attribute values<br/>
	 * <strong>Should</strong> not find any visits if none have given attribute values<br/>
	 * <strong>Should</strong> return all providers if query is empty<br/>
	 * <strong>Should</strong> find provider by identifier
	 *
	 * @param query
	 * @param start
	 * @param length
	 * @param attributes
	 * @param includeRetired
	 * @return the list of Providers given the query , current page and page length Provider
	 */
	@Authorized({ PrivilegeConstants.GET_PROVIDERS })
	public List<Provider> getProviders(String query, Integer start, Integer length,
	        Map<ProviderAttributeType, Object> attributes, boolean includeRetired);

	/**
	 * <p>
	 * <strong>Should</strong> fetch provider with given identifier with case in sensitive<br/>
	 * <strong>Should</strong> fetch provider with given name with case in sensitive<br/>
	 * <strong>Should</strong> fetch provider by matching query string with any unVoided PersonName's
	 * Given Name<br/>
	 * <strong>Should</strong> fetch provider by matching query string with any unVoided PersonName's
	 * middleName<br/>
	 * <strong>Should</strong> fetch provider by matching query string with any unVoided Person's
	 * familyName<br/>
	 * <strong>Should</strong> not fetch provider if the query string matches with any voided Person
	 * name for that<br/>
	 * <strong>Should</strong> get all visits with given attribute values<br/>
	 * <strong>Should</strong> not find any visits if none have given attribute values<br/>
	 * <strong>Should</strong> return all providers if query is empty<br/>
	 * <strong>Should</strong> return retired providers
	 *
	 * @param query
	 * @param start
	 * @param length
	 * @param attributes
	 * @return the list of Providers given the query , current page and page length Provider
	 */
	@Authorized({ PrivilegeConstants.GET_PROVIDERS })
	public List<Provider> getProviders(String query, Integer start, Integer length,
	        Map<ProviderAttributeType, Object> attributes);

	/**
	 * <p>
	 * <strong>Should</strong> exclude retired providers
	 *
	 * @param query
	 * @return Count-Integer
	 */
	@Authorized({ PrivilegeConstants.GET_PROVIDERS })
	public Integer getCountOfProviders(String query);

	/**
	 * Gets the count of providers with a person name or identifier or name that matches the specified
	 * query
	 * <p>
	 * <strong>Should</strong> fetch number of provider matching given query<br/>
	 * <strong>Should</strong> include retired providers if includeRetired is set to true
	 *
	 * @param query the text to match
	 * @param includeRetired specifies whether retired providers should be include or not
	 * @return Count-Integer
	 * @since 1.9.4
	 */
	@Authorized({ PrivilegeConstants.GET_PROVIDERS })
	public Integer getCountOfProviders(String query, boolean includeRetired);

	/**
	 * Gets all provider attribute types including retired provider attribute types. This method
	 * delegates to the #getAllProviderAttributeTypes(boolean) method
	 * <p>
	 * <strong>Should</strong> get all provider attribute types including retired by default
	 *
	 * @return a list of provider attribute type objects.
	 */
	public List<ProviderAttributeType> getAllProviderAttributeTypes();

	/**
	 * Gets all provider attribute types optionally including retired provider attribute types.
	 * <p>
	 * <strong>Should</strong> get all provider attribute types excluding retired<br/>
	 * <strong>Should</strong> get all provider attribute types including retired
	 *
	 * @param includeRetired boolean value to indicate whether to include retired records or not
	 * @return a list of provider attribute type objects.
	 */
	public List<ProviderAttributeType> getAllProviderAttributeTypes(boolean includeRetired);

	/**
	 * Gets a provider attribute type by it's id
	 * <p>
	 * <strong>Should</strong> get provider attribute type for the given id
	 *
	 * @param providerAttributeTypeId the provider attribute type id
	 * @return the provider type attribute by it's id
	 */
	public ProviderAttributeType getProviderAttributeType(Integer providerAttributeTypeId);

	/**
	 * Get a provider attribute type by its uuid
	 * <p>
	 * <strong>Should</strong> get the provider attribute type by its uuid
	 *
	 * @param uuid the uuid of the provider attribute type
	 * @return the provider attribute type for the given uuid
	 */
	public ProviderAttributeType getProviderAttributeTypeByUuid(String uuid);

	/**
	 * Get a provider attribute type by its name
	 * <p>
	 * <strong>Should</strong> get the provider attribute type by its name
	 *
	 * @param name the name of the provider attribute type
	 * @return the provider attribute type for the given name
	 * @since 2.7.0, 2.6.3
	 */
	@Authorized({ PrivilegeConstants.GET_PROVIDER_ATTRIBUTE_TYPES })
	public ProviderAttributeType getProviderAttributeTypeByName(String name);

	/**
	 * Get a provider attribute by it's providerAttributeID
	 * <p>
	 * <strong>Should</strong> get the provider attribute by it's providerAttributeID
	 *
	 * @param providerAttributeID the provider attribute ID of the providerAttribute
	 * @return the provider attribute for the given providerAttributeID
	 */
	public ProviderAttribute getProviderAttribute(Integer providerAttributeID);

	/**
	 * Get a provider attribute by its providerAttributeUuid
	 * <p>
	 * <strong>Should</strong> get the provider attribute by its providerAttributeUuid
	 *
	 * @param uuid the provider attribute uuid of the providerAttribute
	 * @return the provider attribute for the given providerAttributeUuid
	 */
	public ProviderAttribute getProviderAttributeByUuid(String uuid);

	/**
	 * Save the provider attribute type
	 * <p>
	 * <strong>Should</strong> save the provider attribute type
	 *
	 * @param providerAttributeType the provider attribute type to be saved
	 * @return the saved provider attribute type
	 */
	public ProviderAttributeType saveProviderAttributeType(ProviderAttributeType providerAttributeType);

	/**
	 * Retire a provider attribute type
	 * <p>
	 * <strong>Should</strong> retire provider type attribute
	 *
	 * @param providerAttributeType the provider attribute type to be retired
	 * @param reason for retiring the provider attribute type
	 * @return the retired provider attribute type
	 */
	public ProviderAttributeType retireProviderAttributeType(ProviderAttributeType providerAttributeType, String reason);

	/**
	 * Un-Retire a provider attribute type
	 * <p>
	 * <strong>Should</strong> unretire a provider attribute type
	 *
	 * @param providerAttributeType the provider type attribute to unretire
	 * @return the unretire provider attribute type
	 */
	public ProviderAttributeType unretireProviderAttributeType(ProviderAttributeType providerAttributeType);

	/**
	 * Deletes a provider attribute type
	 * <p>
	 * <strong>Should</strong> delete a provider attribute type
	 *
	 * @param providerAttributeType provider attribute type to be deleted
	 */
	public void purgeProviderAttributeType(ProviderAttributeType providerAttributeType);

	/**
	 * Checks if the identifier for the specified provider is unique
	 * <p>
	 * <strong>Should</strong> return false if the identifier is a duplicate<br/>
	 * <strong>Should</strong> return true if the identifier is null<br/>
	 * <strong>Should</strong> return true if the identifier is a blank string
	 *
	 * @param provider the provider whose identifier to check
	 * @return true if the identifier is unique otherwise false
	 * @throws APIException
	 */
	@Authorized({ PrivilegeConstants.GET_PROVIDERS })
	public boolean isProviderIdentifierUnique(Provider provider) throws APIException;

	/**
	 * Gets a provider with a matching identifier, this method performs a case insensitive search
	 * <p>
	 * <strong>Should</strong> get a provider matching the specified identifier ignoring case
	 *
	 * @param identifier the identifier to match against
	 * @return a {@link Provider}
	 */
	@Authorized({ PrivilegeConstants.GET_PROVIDERS })
	public Provider getProviderByIdentifier(String identifier);

	/**
	 * Gets the unknown provider account, i.e. the provider account that matches the uuid specified as
	 * the value for the global property
	 * {@link org.openmrs.util.OpenmrsConstants#GP_UNKNOWN_PROVIDER_UUID}
	 * <p>
	 * <strong>Should</strong> get the unknown provider account
	 *
	 * @return a {@link Provider}
	 * @since 1.10
	 */
	@Authorized({ PrivilegeConstants.GET_PROVIDERS })
	public Provider getUnknownProvider();

	/**
	 * Get a {@link ProviderRole} by its ID
	 *
	 * @param providerRoleId
	 * @return {@link ProviderRole}
	 * @since 2.8.0
	 */
	@Authorized({ PrivilegeConstants.GET_PROVIDER_ROLES })
	ProviderRole getProviderRole(Integer providerRoleId);

	/**
	 * Gets all Provider Roles in the database
	 *
	 * @param includeRetired whether to include retired provider roles or not
	 * @return list of all provider roles in the system
	 * @since 2.8.1
	 */
	@Authorized({ PrivilegeConstants.GET_PROVIDER_ROLES })
	public List<ProviderRole> getAllProviderRoles(boolean includeRetired);

	/**
	 * Get a {@link ProviderRole} by its UUID
	 *
	 * @param uuid The ProviderRole UUID
	 * @return {@link ProviderRole}
	 * @since 2.8.0
	 */
	@Authorized({ PrivilegeConstants.GET_PROVIDER_ROLES })
	ProviderRole getProviderRoleByUuid(String uuid);

	/**
	 * Get providers for given roles
	 *
	 * @param roles The list of {@link ProviderRole}
	 * @return a list of matching {@link Provider} instances
	 * @since 2.8.0
	 */
	@Authorized({ PrivilegeConstants.GET_PROVIDERS })
	List<Provider> getProvidersByRoles(List<ProviderRole> roles);

	/**
	 * Saves/updates a provider role
	 *
	 * @param providerRole the provider role to save
	 * @return the saved provider role
	 * @since 2.8.2
	 */
	@Authorized({ PrivilegeConstants.MANAGE_PROVIDER_ROLES })
	ProviderRole saveProviderRole(ProviderRole providerRole);

	/**
	 * Retires a provider role
	 *
	 * @param providerRole the role to retire
	 * @param reason the reason the role is being retired
	 * @since 2.8.2
	 */
	@Authorized({ PrivilegeConstants.MANAGE_PROVIDER_ROLES })
	ProviderRole retireProviderRole(ProviderRole providerRole, String reason);

	/**
	 * Unretires a provider role
	 *
	 * @param providerRole the role to unretire
	 * @since 2.8.2
	 */
	@Authorized({ PrivilegeConstants.MANAGE_PROVIDER_ROLES })
	ProviderRole unretireProviderRole(ProviderRole providerRole);

	/**
	 * Deletes a provider role entirely from the database
	 *
	 * @param providerRole the provider role to delete
	 * @since 2.8.2
	 */
	@Authorized({ PrivilegeConstants.PURGE_PROVIDER_ROLES })
	void purgeProviderRole(ProviderRole providerRole);
}
