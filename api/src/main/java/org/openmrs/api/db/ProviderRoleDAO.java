/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.db;

import org.openmrs.ProviderRole;

import java.util.List;

/**
 *  Database methods for {@link org.openmrs.api.ProviderRoleService}.
 */
public interface ProviderRoleDAO {

	/**
	 * Gets all Provider Roles in the database
	 *
	 * @param includeRetired whether or not to include retired providers
	 * @return list of al provider roles in the system
	 */
	public List<ProviderRole> getAllProviderRoles(boolean includeRetired);

	/**
	 * Gets the provider role referenced by the specified id
	 *
	 * @param id
	 * @return providerRole
	 */
	public ProviderRole getProviderRole(Integer id);

	/**
	 * Gets the provider role referenced by the specified uui
	 *
	 * @param uuid
	 * @return providerRole
	 */
	public ProviderRole getProviderRoleByUuid(String uuid);

	/**
	 * Saves/updates a provider role
	 *
	 * @param role the provider role to save
	 * @return provider role
	 */
	public ProviderRole saveProviderRole(ProviderRole role);

	/**
	 * Deletes a provider role
	 *
	 * @param role the provider role to delete
	 */
	public void deleteProviderRole(ProviderRole role);
}
