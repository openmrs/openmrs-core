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
import io.swagger.models.ModelImpl;
import io.swagger.models.properties.StringProperty;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.SubResource;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingSubResource;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;
import org.openmrs.module.webservices.rest.web.response.ResponseException;

@SubResource(parent = CustomDatatypeResource1_9.class, path = "handlers", supportedClass = CustomDatatypeHandlerRepresentation.class, supportedOpenmrsVersions = {
        "1.9.* - 9.*" })
public class CustomDatatypeHandlerResource1_9 extends DelegatingSubResource<CustomDatatypeHandlerRepresentation, CustomDatatypeRepresentation, CustomDatatypeResource1_9> {
	
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		description.addProperty("uuid");
		description.addProperty("handlerClassname");
		description.addProperty("display", "textToDisplay");
		description.addSelfLink();
		description.addLink("full", ".?v=" + RestConstants.REPRESENTATION_FULL);
		return description;
	}
	
	@Override
	public Model getGETModel(Representation rep) {
		ModelImpl model = (ModelImpl) super.getGETModel(rep);
		if (rep instanceof DefaultRepresentation || rep instanceof FullRepresentation) {
			model
			        .property("uuid", new StringProperty())
			        .property("handlerClassname", new StringProperty())
			        .property("display", new StringProperty()); //FIXME delegate property name
		}
		return model;
	}
	
	@Override
	public CustomDatatypeHandlerRepresentation newDelegate() {
		return new CustomDatatypeHandlerRepresentation();
	}
	
	@Override
	public CustomDatatypeHandlerRepresentation save(CustomDatatypeHandlerRepresentation delegate) {
		throw new ResourceDoesNotSupportOperationException();
	}
	
	@Override
	public CustomDatatypeRepresentation getParent(CustomDatatypeHandlerRepresentation instance) {
		return instance.getParent();
	}
	
	@Override
	public void setParent(CustomDatatypeHandlerRepresentation instance, CustomDatatypeRepresentation parent) {
		throw new ResourceDoesNotSupportOperationException();
	}
	
	@Override
	public PageableResult doGetAll(CustomDatatypeRepresentation parent, RequestContext context) throws ResponseException {
		return new NeedsPaging<CustomDatatypeHandlerRepresentation>(parent.getHandlers(), context);
	}
	
	@Override
	public CustomDatatypeHandlerRepresentation getByUniqueId(String uniqueId) {
		throw new ResourceDoesNotSupportOperationException();
	}
	
	@Override
	protected void delete(CustomDatatypeHandlerRepresentation delegate, String reason, RequestContext context)
	        throws ResponseException {
		throw new ResourceDoesNotSupportOperationException();
	}
	
	@Override
	public void purge(CustomDatatypeHandlerRepresentation delegate, RequestContext context) throws ResponseException {
		throw new ResourceDoesNotSupportOperationException();
	}
	
}
