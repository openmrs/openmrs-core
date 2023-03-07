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

import java.util.ArrayList;
import java.util.List;

import org.openmrs.VisitType;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.resource.impl.MetadataDelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.response.ResponseException;

/**
 * {@link Resource} for {@link VisitType}, supporting standard CRUD operations
 */
@Resource(name = RestConstants.VERSION_1 + "/visittype", supportedClass = VisitType.class, supportedOpenmrsVersions = {
        "1.9.* - 9.*" })
public class VisitTypeResource1_9 extends MetadataDelegatingCrudResource<VisitType> {
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#getCreatableProperties()
	 */
	@Override
	public DelegatingResourceDescription getCreatableProperties() {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		
		description.addRequiredProperty("name");
		description.addProperty("description");
		
		return description;
	}
	
	/**
	 * @see DelegatingCrudResource#newDelegate()
	 */
	@Override
	public VisitType newDelegate() {
		return new VisitType();
	}
	
	/**
	 * @see DelegatingCrudResource#save(java.lang.Object)
	 */
	@Override
	public VisitType save(VisitType visitType) {
		return Context.getVisitService().saveVisitType(visitType);
	}
	
	/**
	 * Fetches a visitType by uuid, if no match is found, it tries to look up one with a matching
	 * name with the assumption that the passed parameter is a visitType name
	 * 
	 * @see DelegatingCrudResource#getByUniqueId(java.lang.String)
	 */
	@Override
	public VisitType getByUniqueId(String uuid) {
		VisitType visitType = Context.getVisitService().getVisitTypeByUuid(uuid);
		//We assume the caller was fetching by name, 1.9.0 has no method to fetch by name
		if (visitType == null) {
			List<VisitType> visitTypes = Context.getVisitService().getAllVisitTypes();
			for (VisitType possibleVisitType : visitTypes) {
				if (possibleVisitType.getName().equalsIgnoreCase(uuid))
					return possibleVisitType;
			}
		}
		
		return visitType;
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#purge(java.lang.Object,
	 *      org.openmrs.module.webservices.rest.web.RequestContext)
	 */
	@Override
	public void purge(VisitType visitType, RequestContext context) throws ResponseException {
		if (visitType == null)
			return;
		Context.getVisitService().purgeVisitType(visitType);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#doGetAll(org.openmrs.module.webservices.rest.web.RequestContext)
	 */
	@Override
	protected NeedsPaging<VisitType> doGetAll(RequestContext context) {
		//Apparently, in 1.9.0 this method returns all and has no argument for excluding retired ones
		List<VisitType> visitTypes = Context.getVisitService().getAllVisitTypes();
		List<VisitType> filteredVisitTypes;
		if (context.getIncludeAll()) {
			filteredVisitTypes = visitTypes;
		} else {
			filteredVisitTypes = new ArrayList<VisitType>();
			for (VisitType visitType : visitTypes) {
				if (!visitType.isRetired())
					filteredVisitTypes.add(visitType);
			}
		}
		return new NeedsPaging<VisitType>(filteredVisitTypes, context);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#doSearch(org.openmrs.module.webservices.rest.web.RequestContext)
	 */
	@Override
	protected NeedsPaging<VisitType> doSearch(RequestContext context) {
		List<VisitType> visitTypes = Context.getVisitService().getVisitTypes(context.getParameter("q"));
		List<VisitType> filteredVisitTypes;
		if (context.getIncludeAll()) {
			filteredVisitTypes = visitTypes;
		} else {
			filteredVisitTypes = new ArrayList<VisitType>();
			for (VisitType visitType : visitTypes) {
				if (!visitType.isRetired())
					filteredVisitTypes.add(visitType);
			}
		}
		return new NeedsPaging<VisitType>(filteredVisitTypes, context);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#getResourceVersion()
	 */
	@Override
	public String getResourceVersion() {
		return "1.9";
	}
}
