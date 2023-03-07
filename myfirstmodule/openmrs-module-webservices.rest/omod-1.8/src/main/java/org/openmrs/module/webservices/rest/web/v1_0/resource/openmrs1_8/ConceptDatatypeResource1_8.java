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
import io.swagger.models.properties.BooleanProperty;
import io.swagger.models.properties.StringProperty;
import org.openmrs.ConceptDatatype;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.resource.impl.MetadataDelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;
import org.openmrs.module.webservices.rest.web.response.ResponseException;

/**
 * {@link Resource} for {@link ConceptDatatype}, supporting standard CRUD operations
 */
@Resource(name = RestConstants.VERSION_1 + "/conceptdatatype", supportedClass = ConceptDatatype.class, supportedOpenmrsVersions = {
        "1.8.* - 1.12.*" })
public class ConceptDatatypeResource1_8 extends MetadataDelegatingCrudResource<ConceptDatatype> {
	
	/**
	 * @see DelegatingCrudResource#getRepresentationDescription(Representation)
	 */
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		if (rep instanceof DefaultRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("uuid");
			description.addProperty("display");
			description.addProperty("name");
			description.addProperty("description");
			description.addProperty("hl7Abbreviation");
			description.addProperty("retired");
			description.addSelfLink();
			description.addLink("full", ".?v=" + RestConstants.REPRESENTATION_FULL);
			return description;
		} else if (rep instanceof FullRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("uuid");
			description.addProperty("display");
			description.addProperty("name");
			description.addProperty("description");
			description.addProperty("hl7Abbreviation");
			description.addProperty("retired");
			description.addProperty("auditInfo");
			description.addSelfLink();
			return description;
		}
		return null;
	}
	
	public Model getGETModel(Representation rep) {
		ModelImpl model = ((ModelImpl) super.getGETModel(rep));
		if (rep instanceof DefaultRepresentation || rep instanceof FullRepresentation) {
			model
			        .property("uuid", new StringProperty())
			        .property("display", new StringProperty())
			        .property("name", new StringProperty())
			        .property("description", new StringProperty())
			        .property("hl7Abbreviation", new StringProperty())
			        .property("retired", new BooleanProperty());
		}
		return model;
	}
	
	/**
	 * @see DelegatingCrudResource#newDelegate()
	 */
	@Override
	public ConceptDatatype newDelegate() {
		return new ConceptDatatype();
	}
	
	/**
	 * @see DelegatingCrudResource#save(java.lang.Object)
	 */
	@Override
	public ConceptDatatype save(ConceptDatatype conceptDatatype) {
		throw new ResourceDoesNotSupportOperationException();
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#update(java.lang.String,
	 *      org.openmrs.module.webservices.rest.SimpleObject,
	 *      org.openmrs.module.webservices.rest.web.RequestContext)
	 */
	@Override
	public Object update(String uuid, SimpleObject propertiesToUpdate, RequestContext context) throws ResponseException {
		throw new ResourceDoesNotSupportOperationException();
	}
	
	/**
	 * @see DelegatingCrudResource#getByUniqueId(java.lang.String)
	 */
	@Override
	public ConceptDatatype getByUniqueId(String uuid) {
		ConceptDatatype datatype = Context.getConceptService().getConceptDatatypeByUuid(uuid);
		if (datatype == null)
			datatype = Context.getConceptService().getConceptDatatypeByName(uuid);
		
		return datatype;
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#purge(java.lang.Object,
	 *      org.openmrs.module.webservices.rest.web.RequestContext)
	 */
	@Override
	public void purge(ConceptDatatype conceptDatatype, RequestContext context) throws ResponseException {
		throw new ResourceDoesNotSupportOperationException();
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.MetadataDelegatingCrudResource#delete(org.openmrs.OpenmrsMetadata,
	 *      java.lang.String, org.openmrs.module.webservices.rest.web.RequestContext)
	 */
	@Override
	public void delete(ConceptDatatype delegate, String reason, RequestContext context) throws ResponseException {
		throw new ResourceDoesNotSupportOperationException();
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#doGetAll(org.openmrs.module.webservices.rest.web.RequestContext)
	 */
	@Override
	protected NeedsPaging<ConceptDatatype> doGetAll(RequestContext context) {
		return new NeedsPaging<ConceptDatatype>(Context.getConceptService().getAllConceptDatatypes(context.getIncludeAll()),
		        context);
	}
	
}
