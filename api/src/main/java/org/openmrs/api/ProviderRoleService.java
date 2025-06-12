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

import org.openmrs.ProviderRole;
import org.openmrs.annotation.Authorized;

import java.util.List;

import static org.openmrs.util.PrivilegeConstants.PROVIDER_ROLE_API_PRIVILEGE;
import static org.openmrs.util.PrivilegeConstants.PROVIDER_ROLE_API_READ_ONLY_PRIVILEGE;

/**
 * This service contains methods relating to provider roles.
 * @since 2.8.0
 */
public interface ProviderRoleService extends OpenmrsService{

	/**
	 * Gets all Provider Roles in the database
	 *
	 * @param includeRetired whether to include retired provider roles or not
	 * @return list of all provider roles in the system
	 */
	@Authorized(value = {PROVIDER_ROLE_API_PRIVILEGE, PROVIDER_ROLE_API_READ_ONLY_PRIVILEGE}, requireAll = false)
	public List<ProviderRole> getAllProviderRoles(boolean includeRetired);

	/**
	 * Gets the provider role referenced by the specified id
	 *
	 * @param id
	 * @return providerRole
	 */
	@Authorized(value = {PROVIDER_ROLE_API_PRIVILEGE, PROVIDER_ROLE_API_READ_ONLY_PRIVILEGE}, requireAll = false)
	public ProviderRole getProviderRole(Integer id);

	/**
	 * Gets the provider role referenced by the specified uui
	 *
	 * @param uuid
	 * @return providerRole
	 */
	@Authorized(value = {PROVIDER_ROLE_API_PRIVILEGE, PROVIDER_ROLE_API_READ_ONLY_PRIVILEGE}, requireAll = false)
	public ProviderRole getProviderRoleByUuid(String uuid);

	/**
	 * Saves/updates a provider role
	 *
	 * @param role the provider role to save
	 * @return the saved provider role
	 */
	@Authorized(PROVIDER_ROLE_API_PRIVILEGE)
	public ProviderRole saveProviderRole(ProviderRole role);

	/**
	 * Retires a provider role
	 * @param role the role to retire
	 * @param reason the reason the role is being retired
	 */
	@Authorized(PROVIDER_ROLE_API_PRIVILEGE)
	public void retireProviderRole(ProviderRole role, String reason);

	/**
	 * Unretires a provider role
	 * @param role the role to unretire
	 */
	@Authorized(PROVIDER_ROLE_API_PRIVILEGE)
	public void unretireProviderRole(ProviderRole role);

	/**
	 * Deletes a provider role
	 *
	 * @param role the provider role to delete
	 */
	@Authorized(PROVIDER_ROLE_API_PRIVILEGE)
	public void purgeProviderRole(ProviderRole role) throws ProviderRoleInUseException;
}
