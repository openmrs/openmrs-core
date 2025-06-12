/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.impl;

import org.openmrs.ProviderRole;
import org.openmrs.api.ProviderRoleInUseException;
import org.openmrs.api.ProviderRoleService;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.ProviderRoleDAO;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.PersistenceException;
import java.util.List;

/**
 * It is a default implementation of {@link ProviderRoleService}.
 * 
 * @since 2.8.0
 */
@Transactional
public class ProviderRoleServiceImpl extends BaseOpenmrsService implements ProviderRoleService {

	private ProviderRoleDAO providerRoleDAO;

	public ProviderRoleDAO getProviderRoleDAO() {
		return providerRoleDAO;
	}

	public void setProviderRoleDAO(ProviderRoleDAO providerRoleDAO) {
		this.providerRoleDAO = providerRoleDAO;
	}

	@Override
	@Transactional(readOnly = true)
	public List<ProviderRole> getAllProviderRoles(boolean includeRetired) {
		return providerRoleDAO.getAllProviderRoles(includeRetired);
	}

	@Override
	@Transactional(readOnly = true)
	public ProviderRole getProviderRole(Integer id) {
		return providerRoleDAO.getProviderRole(id);
	}

	@Override
	@Transactional(readOnly = true)
	public ProviderRole getProviderRoleByUuid(String uuid) {
		return providerRoleDAO.getProviderRoleByUuid(uuid);
	}

	@Override
	@Transactional
	public ProviderRole saveProviderRole(ProviderRole role) {
		return providerRoleDAO.saveProviderRole(role);
	}

	@Override
	@Transactional
	public void retireProviderRole(ProviderRole role, String reason) {
		// BaseRetireHandler handles retiring the object
		providerRoleDAO.saveProviderRole(role);
	}

	@Override
	@Transactional
	public void unretireProviderRole(ProviderRole role) {
		// BaseUnretireHandler handles unretiring the object
		providerRoleDAO.saveProviderRole(role);
	}

	@Override
	@Transactional
	public void purgeProviderRole(ProviderRole role) throws ProviderRoleInUseException {
		try {
			providerRoleDAO.deleteProviderRole(role);
			Context.flushSession();  // shouldn't really have to do this, but we do to force a commit so that the exception will be thrown if necessary
		}
		catch (PersistenceException e) {
			throw new ProviderRoleInUseException("Cannot purge provider role. Most likely it is currently linked to an existing provider ", e);
		}

	}
}
