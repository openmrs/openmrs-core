/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.test;

import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.properties.StringProperty;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.response.ResponseException;

@Resource(name = RestConstants.VERSION_1 + "/genericChild", supportedClass = GenericChild.class, supportedOpenmrsVersions = {
        "1.9.* - 9.*" })
public class GenericChildResource extends DelegatingCrudResource<GenericChild> {
	
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		description.addProperty("uuid");
		description.addProperty("value");
		description.addSelfLink();
		description.addLink("full", ".?v=" + RestConstants.REPRESENTATION_FULL);
		
		return description;
	}
	
	@Override
	public DelegatingResourceDescription getCreatableProperties() {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		description.addProperty("value");
		
		return description;
	}
	
	@Override
	public Model getGETModel(Representation rep) {
		return ((ModelImpl) super.getGETModel(rep))
		        .property("uuid", new StringProperty())
		        .property("value", new StringProperty());
	}
	
	@Override
	public Model getCREATEModel(Representation rep) {
		return new ModelImpl()
		        .property("value", new StringProperty());
	}
	
	@Override
	public Model getUPDATEModel(Representation rep) {
		return new ModelImpl();
	}
	
	@Override
	public GenericChild newDelegate() {
		return new GenericChild();
	}
	
	@Override
	public GenericChild save(GenericChild child) {
		return child;
	}
	
	@Override
	public GenericChild getByUniqueId(String uuid) {
		return null;
	}
	
	@Override
	public void delete(GenericChild visit, String reason, RequestContext context) throws ResponseException {
	}
	
	@Override
	public void purge(GenericChild visit, RequestContext context) throws ResponseException {
	}
	
	@Override
	public String getResourceVersion() {
		return "1.9";
	}
}
