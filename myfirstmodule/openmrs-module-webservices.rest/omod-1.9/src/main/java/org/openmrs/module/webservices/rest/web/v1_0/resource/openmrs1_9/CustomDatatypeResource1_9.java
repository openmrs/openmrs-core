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
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.RefProperty;
import io.swagger.models.properties.StringProperty;
import org.openmrs.api.context.Context;
import org.openmrs.customdatatype.CustomDatatype;
import org.openmrs.customdatatype.CustomDatatypeHandler;
import org.openmrs.customdatatype.CustomDatatypeUtil;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.RefRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.response.ConversionException;
import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;
import org.openmrs.module.webservices.rest.web.response.ResponseException;

import java.util.ArrayList;
import java.util.List;

@Resource(name = RestConstants.VERSION_1 + "/customdatatype", supportedClass = CustomDatatypeRepresentation.class, supportedOpenmrsVersions = {
        "1.9.* - 9.*" })
public class CustomDatatypeResource1_9 extends DelegatingCrudResource<CustomDatatypeRepresentation> {
	
	@Override
	public CustomDatatypeRepresentation newDelegate() {
		return new CustomDatatypeRepresentation();
	}
	
	@Override
	public CustomDatatypeRepresentation save(CustomDatatypeRepresentation delegate) {
		throw new ResourceDoesNotSupportOperationException();
	}
	
	@Override
	public CustomDatatypeRepresentation getByUniqueId(String uniqueId) {
		List<CustomDatatypeRepresentation> datatypes = getAllCustomDatatypes();
		for (CustomDatatypeRepresentation datatype : datatypes) {
			if (datatype.getUuid().equals(uniqueId)) {
				return datatype;
			}
		}
		return null;
	}
	
	@Override
	protected void delete(CustomDatatypeRepresentation delegate, String reason, RequestContext context)
	        throws ResponseException {
		throw new ResourceDoesNotSupportOperationException();
	}
	
	@Override
	public void purge(CustomDatatypeRepresentation delegate, RequestContext context) throws ResponseException {
		throw new ResourceDoesNotSupportOperationException();
	}
	
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		if (rep instanceof RefRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("uuid");
			description.addProperty("display", "textToDisplay");
			description.addSelfLink();
			description.addLink("full", ".?v=" + RestConstants.REPRESENTATION_FULL);
			return description;
		} else if (rep instanceof DefaultRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("uuid");
			description.addProperty("display", "textToDisplay");
			description.addProperty("datatypeClassname");
			description.addProperty("handlers", Representation.FULL);
			description.addSelfLink();
			description.addLink("full", ".?v=" + RestConstants.REPRESENTATION_FULL);
			return description;
		} else if (rep instanceof FullRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("uuid");
			description.addProperty("display", "textToDisplay");
			description.addProperty("datatypeClassname");
			description.addProperty("handlers", Representation.FULL);
			description.addSelfLink();
			return description;
		}
		return null;
	}
	
	@Override
	public Model getGETModel(Representation rep) {
		ModelImpl model = (ModelImpl) super.getGETModel(rep);
		if (rep instanceof DefaultRepresentation || rep instanceof FullRepresentation) {
			model
			        .property("uuid", new StringProperty())
			        .property("display", new StringProperty())
			        .property("datatypeClassname", new StringProperty());
		}
		if (rep instanceof DefaultRepresentation) {
			model
			        .property("handlers", new ArrayProperty(new RefProperty("#/definitions/CustomdatatypeHandlersGetRef")));
		} else if (rep instanceof FullRepresentation) {
			model
			        .property("handlers", new ArrayProperty(new RefProperty("#/definitions/CustomdatatypeHandlersGet")));
		}
		return model;
	}
	
	@Override
	protected PageableResult doGetAll(RequestContext context) throws ResponseException {
		List<CustomDatatypeRepresentation> datatypes = getAllCustomDatatypes();
		
		return new NeedsPaging<CustomDatatypeRepresentation>(datatypes, context);
	}
	
	private List<CustomDatatypeRepresentation> getAllCustomDatatypes() {
		List<String> datatypeClassnames = CustomDatatypeUtil.getDatatypeClassnames();
		
		List<CustomDatatypeRepresentation> datatypes = new ArrayList<CustomDatatypeRepresentation>();
		
		for (String datatypeClassname : datatypeClassnames) {
			CustomDatatypeRepresentation datatype = new CustomDatatypeRepresentation();
			datatypes.add(datatype);
			datatype.setDatatypeClassname(datatypeClassname);
			
			Class<CustomDatatype<?>> datatypeClass;
			try {
				datatypeClass = (Class<CustomDatatype<?>>) Context.loadClass(datatypeClassname);
			}
			catch (ClassNotFoundException e) {
				throw new ConversionException("Failed to load datatype class", e);
			}
			
			List<Class<? extends CustomDatatypeHandler>> handlerClasses = Context.getDatatypeService().getHandlerClasses(
			    datatypeClass);
			for (Class<? extends CustomDatatypeHandler> handlerClass : handlerClasses) {
				datatype.getHandlers().add(new CustomDatatypeHandlerRepresentation(datatype, handlerClass.getName()));
			}
		}
		return datatypes;
	}
	
	@Override
	public String getResourceVersion() {
		return "1.9";
	}
	
}
