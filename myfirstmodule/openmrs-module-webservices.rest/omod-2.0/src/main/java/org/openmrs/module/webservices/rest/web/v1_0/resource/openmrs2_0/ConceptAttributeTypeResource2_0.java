/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs2_0;

import org.openmrs.ConceptAttributeType;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.response.ResponseException;
import org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_9.BaseAttributeTypeCrudResource1_9;

/**
 * Allows standard CRUD for the {@link ConceptAttributeType} domain object
 */
@Resource(name = RestConstants.VERSION_1 + "/conceptattributetype", supportedClass = ConceptAttributeType.class, supportedOpenmrsVersions = {
        "2.0.* - 9.*" })
public class ConceptAttributeTypeResource2_0 extends BaseAttributeTypeCrudResource1_9<ConceptAttributeType> {
	
	public ConceptAttributeTypeResource2_0() {
	}
	
	private ConceptService service() {
		return Context.getConceptService();
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#getByUniqueId(String)
	 */
	@Override
	public ConceptAttributeType getByUniqueId(String uniqueId) {
		return service().getConceptAttributeTypeByUuid(uniqueId);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#doGetAll(RequestContext)
	 */
	@Override
	protected NeedsPaging<ConceptAttributeType> doGetAll(RequestContext context) throws ResponseException {
		return new NeedsPaging<ConceptAttributeType>(service().getAllConceptAttributeTypes(), context);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#newDelegate()
	 */
	@Override
	public ConceptAttributeType newDelegate() {
		return new ConceptAttributeType();
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceHandler#save(Object)
	 */
	@Override
	public ConceptAttributeType save(ConceptAttributeType delegate) {
		return service().saveConceptAttributeType(delegate);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#purge(Object,
	 *      RequestContext)
	 */
	@Override
	public void purge(ConceptAttributeType delegate, RequestContext context) throws ResponseException {
		service().purgeConceptAttributeType(delegate);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#doSearch(RequestContext)
	 */
	@Override
	protected NeedsPaging<ConceptAttributeType> doSearch(RequestContext context) {
		return new NeedsPaging<ConceptAttributeType>(service().getConceptAttributeTypes(context.getParameter("q")), context);
	}
	
	@Override
	public String getResourceVersion() {
		return "2.0";
	}
}
