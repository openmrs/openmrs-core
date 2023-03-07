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
import io.swagger.models.properties.RefProperty;
import org.openmrs.ConceptReferenceTerm;
import org.openmrs.ConceptReferenceTermMap;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.PropertyGetter;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.api.RestHelperService;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.RefRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;
import org.openmrs.module.webservices.rest.web.response.ResponseException;

/**
 * {@link Resource} for {@link ConceptReferenceTermMap}, supporting standard CRUD operations
 */
@Resource(name = RestConstants.VERSION_1 + "/conceptreferencetermmap", supportedClass = ConceptReferenceTermMap.class, supportedOpenmrsVersions = {
        "1.9.* - 9.*" })
public class ConceptReferenceTermMapResource1_9 extends DelegatingCrudResource<ConceptReferenceTermMap> {
	
	/**
	 * @see DelegatingCrudResource#getRepresentationDescription(Representation)
	 */
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		if (rep instanceof RefRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("uuid");
			description.addProperty("display");
			description.addSelfLink();
			description.addLink("full", ".?v=" + RestConstants.REPRESENTATION_FULL);
			return description;
		} else if (rep instanceof DefaultRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("uuid");
			description.addProperty("display");
			description.addProperty("termA", Representation.REF);
			description.addProperty("termB", Representation.REF);
			description.addProperty("conceptMapType", Representation.REF);
			description.addSelfLink();
			description.addLink("full", ".?v=" + RestConstants.REPRESENTATION_FULL);
			return description;
		} else if (rep instanceof FullRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("uuid");
			description.addProperty("display");
			description.addProperty("termA", Representation.DEFAULT);
			description.addProperty("termB", Representation.DEFAULT);
			description.addProperty("conceptMapType", Representation.DEFAULT);
			description.addProperty("auditInfo");
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
		description.addRequiredProperty("termA");
		description.addRequiredProperty("termB");
		description.addRequiredProperty("conceptMapType");
		
		return description;
	}
	
	@Override
	public Model getGETModel(Representation rep) {
		ModelImpl model = (ModelImpl) super.getGETModel(rep);
		if (rep instanceof DefaultRepresentation) {
			model
			        .property("termA", new RefProperty("#/definitions/ConceptreferencetermGetRef"))
			        .property("termB", new RefProperty("#/definitions/ConceptreferencetermGetRef"))
			        .property("conceptMapType", new RefProperty("#/definitions/ConceptmaptypeGetRef"));
		} else if (rep instanceof FullRepresentation) {
			model
			        .property("termA", new RefProperty("#/definitions/ConceptreferencetermGet"))
			        .property("termB", new RefProperty("#/definitions/ConceptreferencetermGet"))
			        .property("conceptMapType", new RefProperty("#/definitions/ConceptmaptypeGet"));
		}
		return model;
	}
	
	@Override
	public Model getCREATEModel(Representation rep) {
		return new ModelImpl()
		        .property("termA", new RefProperty("#/definitions/ConceptreferencetermCreate"))
		        .property("termB", new RefProperty("#/definitions/ConceptreferencetermCreate"))
		        .property("conceptMapType", new RefProperty("#/definitions/ConceptmaptypeCreate"))
		        
		        .required("termA").required("termB").required("conceptMapType");
	}
	
	@Override
	public Model getUPDATEModel(Representation rep) {
		return new ModelImpl(); //FIXME missing props
	}
	
	/**
	 * Gets the display string for a concept map.
	 * 
	 * @param conceptReferenceTermMap the concept map object.
	 * @return the display string.
	 */
	@PropertyGetter("display")
	public String getDisplayString(ConceptReferenceTermMap conceptReferenceTermMap) {
		if (conceptReferenceTermMap.getTermA() == null || conceptReferenceTermMap.getTermB() == null) {
			return "";
		}
		
		return conceptReferenceTermMap.getTermA().getConceptSource().getName() + ": "
		        + conceptReferenceTermMap.getTermA().getCode() + " - "
		        + conceptReferenceTermMap.getTermB().getConceptSource().getName() + ": "
		        + conceptReferenceTermMap.getTermB().getCode();
	}
	
	/**
	 * @see DelegatingCrudResource#newDelegate()
	 */
	@Override
	public ConceptReferenceTermMap newDelegate() {
		return new ConceptReferenceTermMap();
	}
	
	/**
	 * @see DelegatingCrudResource#save(java.lang.Object)
	 */
	@Override
	public ConceptReferenceTermMap save(ConceptReferenceTermMap conceptReferenceTermMap) {
		ConceptReferenceTerm termA = conceptReferenceTermMap.getTermA();
		termA.addConceptReferenceTermMap(conceptReferenceTermMap);
		
		Context.getConceptService().saveConceptReferenceTerm(termA);
		
		return conceptReferenceTermMap;
	}
	
	/**
	 * Fetches a conceptReferenceTerm by uuid
	 * 
	 * @see DelegatingCrudResource#getByUniqueId(java.lang.String)
	 */
	@Override
	public ConceptReferenceTermMap getByUniqueId(String uuid) {
		return Context.getService(RestHelperService.class).getObjectByUuid(ConceptReferenceTermMap.class, uuid);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#purge(java.lang.Object,
	 *      org.openmrs.module.webservices.rest.web.RequestContext)
	 */
	@Override
	public void purge(ConceptReferenceTermMap conceptReferenceTermMap, RequestContext context) throws ResponseException {
		if (conceptReferenceTermMap == null)
			return;
		ConceptReferenceTerm termA = conceptReferenceTermMap.getTermA();
		termA.removeConceptReferenceTermMap(conceptReferenceTermMap);
		
		Context.getConceptService().saveConceptReferenceTerm(termA);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#doGetAll(org.openmrs.module.webservices.rest.web.RequestContext)
	 */
	@Override
	protected NeedsPaging<ConceptReferenceTermMap> doGetAll(RequestContext context) {
		throw new ResourceDoesNotSupportOperationException();
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#doSearch(org.openmrs.module.webservices.rest.web.RequestContext)
	 */
	@Override
	protected PageableResult doSearch(RequestContext context) {
		throw new ResourceDoesNotSupportOperationException();
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#getResourceVersion()
	 */
	@Override
	public String getResourceVersion() {
		return "1.9";
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#delete(java.lang.Object,
	 *      java.lang.String, org.openmrs.module.webservices.rest.web.RequestContext)
	 */
	@Override
	protected void delete(ConceptReferenceTermMap delegate, String reason, RequestContext context) throws ResponseException {
		throw new ResourceDoesNotSupportOperationException();
	}
}
