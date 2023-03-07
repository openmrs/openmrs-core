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

import org.openmrs.VisitAttributeType;
import org.openmrs.api.VisitService;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.response.ResponseException;

import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Allows standard CRUD for the {@link VisitAttributeType} domain object
 */
@Resource(name = RestConstants.VERSION_1 + "/visitattributetype", supportedClass = VisitAttributeType.class, supportedOpenmrsVersions = {
        "1.9.* - 9.*" })
public class VisitAttributeTypeResource1_9 extends BaseAttributeTypeCrudResource1_9<VisitAttributeType> {
	
	private VisitService getService() {
		return Context.getVisitService();
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#newDelegate()
	 */
	@Override
	public VisitAttributeType newDelegate() {
		return new VisitAttributeType();
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#getByUniqueId(java.lang.String)
	 */
	@Override
	public VisitAttributeType getByUniqueId(String uniqueId) {
		return getService().getVisitAttributeTypeByUuid(uniqueId);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#doGetAll(org.openmrs.module.webservices.rest.web.RequestContext)
	 */
	@Override
	protected NeedsPaging<VisitAttributeType> doGetAll(RequestContext context) throws ResponseException {
		if (context.getIncludeAll())
			return new NeedsPaging<VisitAttributeType>(getService().getAllVisitAttributeTypes(), context);
		
		List<VisitAttributeType> vats = getService().getAllVisitAttributeTypes();
		for (Iterator<VisitAttributeType> iterator = vats.iterator(); iterator.hasNext();) {
			VisitAttributeType visitAttributeType = iterator.next();
			if (visitAttributeType.isRetired())
				iterator.remove();
		}
		return new NeedsPaging<VisitAttributeType>(vats, context);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceHandler#save(java.lang.Object)
	 */
	@Override
	public VisitAttributeType save(VisitAttributeType delegate) {
		return getService().saveVisitAttributeType(delegate);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#purge(java.lang.Object,
	 *      org.openmrs.module.webservices.rest.web.RequestContext)
	 */
	@Override
	public void purge(VisitAttributeType delegate, RequestContext context) throws ResponseException {
		getService().purgeVisitAttributeType(delegate);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#doSearch(org.openmrs.module.webservices.rest.web.RequestContext)
	 */
	@Override
	protected NeedsPaging<VisitAttributeType> doSearch(RequestContext context) {
		// TODO: Should be a VisitAttributeType search method in VisitService
		List<VisitAttributeType> vats = getService().getAllVisitAttributeTypes();
		for (Iterator<VisitAttributeType> iterator = vats.iterator(); iterator.hasNext();) {
			VisitAttributeType visitAttributeType = iterator.next();
			//find matches excluding retired ones if necessary
			if (!Pattern.compile(Pattern.quote(context.getParameter("q")), Pattern.CASE_INSENSITIVE)
			        .matcher(visitAttributeType.getName()).find()
			        || (!context.getIncludeAll() && visitAttributeType.isRetired())) {
				iterator.remove();
			}
		}
		return new NeedsPaging<VisitAttributeType>(vats, context);
	}
}
