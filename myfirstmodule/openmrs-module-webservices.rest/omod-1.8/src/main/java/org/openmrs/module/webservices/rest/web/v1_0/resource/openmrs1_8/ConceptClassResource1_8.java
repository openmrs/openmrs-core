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
import org.openmrs.ConceptClass;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.resource.impl.MetadataDelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.response.ResponseException;

/**
 * {@link Resource} for {@link ConceptClass}, supporting standard CRUD operations
 */
@Resource(name = RestConstants.VERSION_1 + "/conceptclass", supportedClass = ConceptClass.class, supportedOpenmrsVersions = {
        "1.8.* - 9.*" })
public class ConceptClassResource1_8 extends MetadataDelegatingCrudResource<ConceptClass> {
	
	/**
	 * @see DelegatingCrudResource#newDelegate()
	 */
	@Override
	public ConceptClass newDelegate() {
		return new ConceptClass();
	}
	
	/**
	 * @see DelegatingCrudResource#save(java.lang.Object)
	 */
	@Override
	public ConceptClass save(ConceptClass conceptClass) {
		return Context.getConceptService().saveConceptClass(conceptClass);
	}
	
	/**
	 * @see DelegatingCrudResource#getByUniqueId(java.lang.String)
	 */
	@Override
	public ConceptClass getByUniqueId(String uuid) {
		ConceptClass conceptClass = Context.getConceptService().getConceptClassByUuid(uuid);
		if (conceptClass == null)
			conceptClass = Context.getConceptService().getConceptClassByName(uuid);
		
		return conceptClass;
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#purge(java.lang.Object,
	 *      org.openmrs.module.webservices.rest.web.RequestContext)
	 */
	@Override
	public void purge(ConceptClass conceptClass, RequestContext context) throws ResponseException {
		if (conceptClass == null)
			return;
		Context.getConceptService().purgeConceptClass(conceptClass);
	}
	
	@Override
	public Model getUPDATEModel(Representation rep) {
		return getCREATEModel(rep);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#doGetAll(org.openmrs.module.webservices.rest.web.RequestContext)
	 */
	@Override
	protected NeedsPaging<ConceptClass> doGetAll(RequestContext context) {
		return new NeedsPaging<ConceptClass>(Context.getConceptService().getAllConceptClasses(context.getIncludeAll()),
		        context);
	}
	
}
