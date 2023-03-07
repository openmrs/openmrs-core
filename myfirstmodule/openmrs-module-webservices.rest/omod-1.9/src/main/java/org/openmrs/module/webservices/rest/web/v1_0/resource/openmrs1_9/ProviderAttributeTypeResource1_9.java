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

import org.openmrs.ProviderAttributeType;
import org.openmrs.api.ProviderService;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.response.ResponseException;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Allows standard CRUD for the {@link ProviderAttributeType} domain object
 */
@Resource(name = RestConstants.VERSION_1 + "/providerattributetype", supportedClass = ProviderAttributeType.class, supportedOpenmrsVersions = {
        "1.9.* - 9.*" })
public class ProviderAttributeTypeResource1_9 extends BaseAttributeTypeCrudResource1_9<ProviderAttributeType> {
	
	public ProviderAttributeTypeResource1_9() {
	}
	
	private ProviderService service() {
		return Context.getProviderService();
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#getByUniqueId(java.lang.String)
	 */
	@Override
	public ProviderAttributeType getByUniqueId(String uniqueId) {
		return service().getProviderAttributeTypeByUuid(uniqueId);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#doGetAll(org.openmrs.module.webservices.rest.web.RequestContext)
	 */
	@Override
	protected NeedsPaging<ProviderAttributeType> doGetAll(RequestContext context) throws ResponseException {
		return new NeedsPaging<ProviderAttributeType>(service().getAllProviderAttributeTypes(), context);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#newDelegate()
	 */
	@Override
	public ProviderAttributeType newDelegate() {
		return new ProviderAttributeType();
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceHandler#save(java.lang.Object)
	 */
	@Override
	public ProviderAttributeType save(ProviderAttributeType delegate) {
		return service().saveProviderAttributeType(delegate);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#purge(java.lang.Object,
	 *      org.openmrs.module.webservices.rest.web.RequestContext)
	 */
	@Override
	public void purge(ProviderAttributeType delegate, RequestContext context) throws ResponseException {
		service().purgeProviderAttributeType(delegate);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#doSearch(org.openmrs.module.webservices.rest.web.RequestContext)
	 */
	@Override
	protected NeedsPaging<ProviderAttributeType> doSearch(RequestContext context) {
		// TODO: Should be a ProviderAttributeType search method in ProviderService
		List<ProviderAttributeType> allAttrs = service().getAllProviderAttributeTypes();
		List<ProviderAttributeType> queryResult = new ArrayList<ProviderAttributeType>();
		for (ProviderAttributeType pAttr : allAttrs) {
			if (Pattern.compile(Pattern.quote(context.getParameter("q")), Pattern.CASE_INSENSITIVE).matcher(pAttr.getName())
			        .find()) {
				queryResult.add(pAttr);
			}
		}
		return new NeedsPaging<ProviderAttributeType>(queryResult, context);
	}
}
