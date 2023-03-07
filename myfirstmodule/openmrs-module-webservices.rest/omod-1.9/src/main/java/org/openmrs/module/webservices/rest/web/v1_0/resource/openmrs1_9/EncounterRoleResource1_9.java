/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_9;

import io.swagger.models.Model;
import org.openmrs.EncounterRole;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.resource.impl.MetadataDelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.response.ResponseException;

/**
 * {@link Resource} for {@link EncounterRole}, supporting standard CRUD operations
 */
@Resource(name = RestConstants.VERSION_1 + "/encounterrole", supportedClass = EncounterRole.class, supportedOpenmrsVersions = {
        "1.9.* - 1.10.*" })
public class EncounterRoleResource1_9 extends MetadataDelegatingCrudResource<EncounterRole> {
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#getCreatableProperties()
	 */
	@Override
	public DelegatingResourceDescription getCreatableProperties() {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		
		description.addRequiredProperty("name");
		description.addRequiredProperty("description");
		
		return description;
	}
	
	@Override
	public Model getUPDATEModel(Representation rep) {
		return getCREATEModel(rep);
	}
	
	/**
	 * @see DelegatingCrudResource#newDelegate()
	 */
	@Override
	public EncounterRole newDelegate() {
		return new EncounterRole();
	}
	
	/**
	 * @see DelegatingCrudResource#save(java.lang.Object)
	 */
	@Override
	public EncounterRole save(EncounterRole encounterRole) {
		return Context.getEncounterService().saveEncounterRole(encounterRole);
	}
	
	/**
	 * Fetches a encounterRole by uuid, if no match is found, it tries to look up one with a
	 * matching name with the assumption that the passed parameter is a encounterRole name
	 * 
	 * @see DelegatingCrudResource#getByUniqueId(java.lang.String)
	 */
	@Override
	public EncounterRole getByUniqueId(String uuid) {
		return Context.getEncounterService().getEncounterRoleByUuid(uuid);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#purge(java.lang.Object,
	 *      org.openmrs.module.webservices.rest.web.RequestContext)
	 */
	@Override
	public void purge(EncounterRole encounterRole, RequestContext context) throws ResponseException {
		if (encounterRole == null) {
			return;
		}
		
		Context.getEncounterService().purgeEncounterRole(encounterRole);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#doGetAll(org.openmrs.module.webservices.rest.web.RequestContext)
	 */
	@Override
	protected NeedsPaging<EncounterRole> doGetAll(RequestContext context) {
		return new NeedsPaging<EncounterRole>(Context.getEncounterService().getAllEncounterRoles(context.getIncludeAll()),
		        context);
	}
}
