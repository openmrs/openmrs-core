/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_8;

import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import org.openmrs.EncounterType;
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
 * {@link Resource} for {@link EncounterType}, supporting standard CRUD operations
 */
@Resource(name = RestConstants.VERSION_1 + "/encountertype", supportedClass = EncounterType.class, supportedOpenmrsVersions = {
        "1.8.* - 9.*" })
public class EncounterTypeResource1_8 extends MetadataDelegatingCrudResource<EncounterType> {
	
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
	public Model getCREATEModel(Representation rep) {
		return ((ModelImpl) super.getCREATEModel(rep))
		        .required("description");
	}
	
	/**
	 * @see DelegatingCrudResource#newDelegate()
	 */
	@Override
	public EncounterType newDelegate() {
		return new EncounterType();
	}
	
	/**
	 * @see DelegatingCrudResource#save(java.lang.Object)
	 */
	@Override
	public EncounterType save(EncounterType encounterType) {
		return Context.getEncounterService().saveEncounterType(encounterType);
	}
	
	/**
	 * Fetches a encounterType by uuid, if no match is found, it tries to look up one with a
	 * matching name with the assumption that the passed parameter is a encounterType name
	 * 
	 * @see DelegatingCrudResource#getByUniqueId(java.lang.String)
	 */
	@Override
	public EncounterType getByUniqueId(String uuid) {
		EncounterType encounterType = Context.getEncounterService().getEncounterTypeByUuid(uuid);
		//We assume the caller was fetching by name
		if (encounterType == null)
			encounterType = Context.getEncounterService().getEncounterType(uuid);
		
		return encounterType;
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#purge(java.lang.Object,
	 *      org.openmrs.module.webservices.rest.web.RequestContext)
	 */
	@Override
	public void purge(EncounterType encounterType, RequestContext context) throws ResponseException {
		if (encounterType == null)
			return;
		Context.getEncounterService().purgeEncounterType(encounterType);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#doGetAll(org.openmrs.module.webservices.rest.web.RequestContext)
	 */
	@Override
	protected NeedsPaging<EncounterType> doGetAll(RequestContext context) {
		return new NeedsPaging<EncounterType>(Context.getEncounterService().getAllEncounterTypes(context.getIncludeAll()),
		        context);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#doSearch(org.openmrs.module.webservices.rest.web.RequestContext)
	 */
	@Override
	protected NeedsPaging<EncounterType> doSearch(RequestContext context) {
		return new NeedsPaging<EncounterType>(Context.getEncounterService().findEncounterTypes(context.getParameter("q")),
		        context);
	}
}
