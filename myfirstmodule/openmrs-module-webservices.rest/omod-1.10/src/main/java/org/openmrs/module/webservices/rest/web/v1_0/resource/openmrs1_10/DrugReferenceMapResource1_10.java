/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_10;

import org.openmrs.DrugReferenceMap;
import org.openmrs.api.context.Context;
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
import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;
import org.openmrs.module.webservices.rest.web.response.ResponseException;

import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.properties.RefProperty;
import io.swagger.models.properties.StringProperty;

import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;

@Resource(name = RestConstants.VERSION_1
        + "/drugreferencemap", supportedClass = DrugReferenceMap.class, supportedOpenmrsVersions = { "1.10.* - 9.*" })
public class DrugReferenceMapResource1_10 extends DelegatingCrudResource<DrugReferenceMap> {
	
	@Override
	public DrugReferenceMap newDelegate() {
		return new DrugReferenceMap();
	}
	
	@Override
	public DrugReferenceMap save(DrugReferenceMap delegate) {
		delegate.getDrug().addDrugReferenceMap(delegate);
		Context.getConceptService().saveDrug(delegate.getDrug());
		return delegate;
	}
	
	@Override
	public DrugReferenceMap getByUniqueId(String uniqueId) {
		return Context.getService(RestHelperService.class).getObjectByUuid(DrugReferenceMap.class, uniqueId);
	}
	
	@Override
	protected void delete(DrugReferenceMap delegate, String reason, RequestContext context) throws ResponseException {
		throw new ResourceDoesNotSupportOperationException();
	}
	
	@Override
	public void purge(DrugReferenceMap delegate, RequestContext context) throws ResponseException {
		throw new ResourceDoesNotSupportOperationException();
	}
	
	@Override
	public PageableResult doGetAll(RequestContext context) throws ResponseException {
		throw new ResourceDoesNotSupportOperationException();
	}
	
	@PropertyGetter("display")
	public String getDisplayString(DrugReferenceMap map) {
		if (map.getDrug().getDisplayName() == null) {
			return "";
		}
		return map.getDrug().getDisplayName() + " - " + map.getConceptMapType().getName();
	}
	
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
			description.addProperty("display");
			description.addProperty("uuid");
			description.addProperty("drug", Representation.REF);
			description.addProperty("conceptReferenceTerm", Representation.REF);
			description.addProperty("conceptMapType", Representation.REF);
			description.addSelfLink();
			description.addLink("full", ".?v=" + RestConstants.REPRESENTATION_FULL);
			return description;
		} else if (rep instanceof FullRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("display");
			description.addProperty("uuid");
			description.addProperty("auditInfo");
			description.addProperty("drug", Representation.DEFAULT);
			description.addProperty("conceptReferenceTerm", Representation.DEFAULT);
			description.addProperty("conceptMapType", Representation.DEFAULT);
			description.addSelfLink();
			return description;
		}
		return null;
	}
	
	@Override
	public DelegatingResourceDescription getCreatableProperties() {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		description.addRequiredProperty("conceptReferenceTerm");
		description.addRequiredProperty("conceptMapType");
		description.addProperty("drug");
		return description;
	}
	
	public Model getGETModel(Representation rep) {
		ModelImpl modelImpl = (ModelImpl) super.getGETModel(rep);
		if (rep instanceof DefaultRepresentation) {
			modelImpl.property("display", new StringProperty()).property("uuid", new StringProperty())
			        .property("drug", new RefProperty("#/definitions/DrugGetRef"))
			        .property("conceptReferenceTerm", new RefProperty("#/definitions/ConceptreferencetermGetRef"))
			        .property("conceptMapType", new RefProperty("#/definitions/ConceptmaptypeGetRef"));
		} else if (rep instanceof FullRepresentation) {
			modelImpl.property("display", new StringProperty()).property("uuid", new StringProperty())
			        .property("auditInfo", new StringProperty()).property("drug", new RefProperty("#/definitions/DrugGet"))
			        .property("conceptReferenceTerm", new RefProperty("#/definitions/ConceptreferencetermGet"))
			        .property("conceptMapType", new RefProperty("#/definitions/ConceptmaptypeGet"));
		}
		return modelImpl;
	}
	
	@Override
	public Model getCREATEModel(Representation rep) {
		return new ModelImpl().property("conceptReferenceTerm", new StringProperty().example("uuid"))
		        .property("conceptMapType", new StringProperty().example("uuid"))
		        .property("drug", new StringProperty().example("uuid"));
	}
}
