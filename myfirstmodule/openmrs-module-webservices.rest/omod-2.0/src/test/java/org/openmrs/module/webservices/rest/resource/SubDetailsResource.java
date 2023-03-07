/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.resource;

import io.swagger.models.Model;
import org.openmrs.GlobalProperty;
import org.openmrs.module.webservices.rest.doc.SwaggerSpecificationCreatorTest;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.annotation.SubResource;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingSubResource;
import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;
import org.openmrs.module.webservices.rest.web.response.ResponseException;
import org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_9.SystemSettingResource1_9;

/**
 * A test only resource used to determine if the correct definitions are included in the swagger
 * document. Only supports the create option.
 * 
 * @see SwaggerSpecificationCreatorTest#createOnlySubresourceDefinitions()
 */
@SubResource(parent = SystemSettingResource1_9.class, path = "subdetails", supportedClass = SubDetails.class, supportedOpenmrsVersions = {
        "2.0.*" })
public class SubDetailsResource extends DelegatingSubResource<SubDetails, GlobalProperty, SystemSettingResource1_9> {
	
	@Override
	public GlobalProperty getParent(SubDetails instance) {
		return new GlobalProperty();
	}
	
	@Override
	public void setParent(SubDetails instance, GlobalProperty parent) {
	}
	
	@Override
	public PageableResult doGetAll(GlobalProperty parent, RequestContext context) throws ResponseException {
		throw new ResourceDoesNotSupportOperationException();
	}
	
	@Override
	public SubDetails getByUniqueId(String uniqueId) {
		throw new ResourceDoesNotSupportOperationException();
	}
	
	@Override
	protected void delete(SubDetails delegate, String reason, RequestContext context) throws ResponseException {
	}
	
	@Override
	public void purge(SubDetails delegate, RequestContext context) throws ResponseException {
	}
	
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		return new DelegatingResourceDescription();
	}
	
	@Override
	public SubDetails newDelegate() {
		return new SubDetails();
	}
	
	@Override
	public SubDetails save(SubDetails delegate) {
		return delegate;
	}
	
	@Override
	public Model getCREATEModel(Representation rep) {
		return super.getGETModel(rep);
	}
}
