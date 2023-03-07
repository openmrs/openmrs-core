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
import io.swagger.models.properties.IntegerProperty;
import io.swagger.models.properties.RefProperty;
import io.swagger.models.properties.StringProperty;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.PropertyGetter;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.impl.DataDelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.response.ConversionException;
import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;
import org.openmrs.module.webservices.rest.web.response.ResponseException;
import org.openmrs.module.webservices.rest.web.v1_0.controller.openmrs1_8.HL7MessageController1_8;
import org.openmrs.module.webservices.rest.web.v1_0.wrapper.openmrs1_8.IncomingHl7Message1_8;

import java.util.Map;

/**
 * {@link Resource} for {@link IncomingHl7Message1_8}, supporting standard CRUD operations
 */
@Resource(name = RestConstants.VERSION_1 + "/hl7", supportedClass = IncomingHl7Message1_8.class, supportedOpenmrsVersions = {
        "1.8.* - 9.*" })
public class HL7MessageResource1_8 extends DataDelegatingCrudResource<IncomingHl7Message1_8> {
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#delete(java.lang.Object,
	 *      java.lang.String, org.openmrs.module.webservices.rest.web.RequestContext)
	 */
	@Override
	protected void delete(IncomingHl7Message1_8 delegate, String reason, RequestContext context) throws ResponseException {
		throw new ResourceDoesNotSupportOperationException();
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#getByUniqueId(java.lang.String)
	 */
	@Override
	public IncomingHl7Message1_8 getByUniqueId(String uniqueId) {
		// Currently it's not supported because we don't have methods within HL7 service, which are returning hl7 message
		// by its uuid. It will be fixed when such methods will be implemented 
		throw new ResourceDoesNotSupportOperationException();
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#getRepresentationDescription(org.openmrs.module.webservices.rest.web.representation.Representation)
	 */
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		if (rep instanceof DefaultRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("uuid");
			description.addProperty("display");
			description.addProperty("messageState");
			description.addSelfLink();
			description.addLink("full", ".?v=" + RestConstants.REPRESENTATION_FULL);
			return description;
		} else if (rep instanceof FullRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("uuid");
			description.addProperty("display");
			description.addProperty("source", Representation.DEFAULT);
			description.addProperty("sourceKey");
			description.addProperty("data");
			description.addProperty("messageState");
			description.addSelfLink();
			return description;
		}
		return null;
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#getCreatableProperties()
	 */
	@Override
	public DelegatingResourceDescription getCreatableProperties() {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		description.addRequiredProperty("hl7");
		return description;
	}
	
	@Override
	public Model getGETModel(Representation rep) {
		ModelImpl modelImpl = (ModelImpl) super.getGETModel(rep);
		if (rep instanceof DefaultRepresentation) {
			modelImpl
			        .property("uuid", new StringProperty())
			        .property("display", new StringProperty())
			        .property("messageState", new IntegerProperty());
		} else if (rep instanceof FullRepresentation) {
			modelImpl
			        .property("uuid", new StringProperty())
			        .property("display", new StringProperty())
			        .property("source", new RefProperty("#/definitions/Hl7sourceGet"))
			        .property("sourceKey", new StringProperty())
			        .property("data", new StringProperty())
			        .property("messageState", new IntegerProperty());
		}
		return modelImpl;
	}
	
	@Override
	public Model getCREATEModel(Representation rep) {
		return new ModelImpl()
		        .property("hl7", new StringProperty()) //FIXME TYPE
		        .required("hl7");
	}
	
	/**
	 * It needs to be overwritten to allow for hidden properties: source, sourceKey and data. They
	 * are automatically extracted from the hl7 property and populated in
	 * {@link HL7MessageController1_8#create(String, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)}
	 * . They should not be POSTed by the user.
	 * 
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#setConvertedProperties(java.lang.Object,
	 *      java.util.Map,
	 *      org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription,
	 *      boolean)
	 */
	@Override
	public void setConvertedProperties(IncomingHl7Message1_8 delegate, Map<String, Object> propertyMap,
	        DelegatingResourceDescription description, boolean mustIncludeRequiredProperties) throws ConversionException {
		for (Map.Entry<String, Object> prop : propertyMap.entrySet()) {
			setProperty(delegate, prop.getKey(), prop.getValue());
		}
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#newDelegate()
	 */
	@Override
	public IncomingHl7Message1_8 newDelegate() {
		return new IncomingHl7Message1_8();
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#purge(java.lang.Object,
	 *      org.openmrs.module.webservices.rest.web.RequestContext)
	 */
	@Override
	public void purge(IncomingHl7Message1_8 delegate, RequestContext context) throws ResponseException {
		throw new ResourceDoesNotSupportOperationException();
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceHandler#save(java.lang.Object)
	 */
	@Override
	public IncomingHl7Message1_8 save(IncomingHl7Message1_8 delegate) {
		return new IncomingHl7Message1_8(Context.getHL7Service().saveHL7InQueue(delegate.toHL7InQueue()));
	}
	
	/**
	 * Gets the display string for an incoming hl7 message resource.
	 * 
	 * @param message the hl7 message.
	 * @return the display string.
	 */
	@PropertyGetter("display")
	public String getDisplayString(IncomingHl7Message1_8 message) {
		return message.getSourceKey();
	}
}
