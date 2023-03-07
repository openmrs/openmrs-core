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
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.RefProperty;
import org.openmrs.Program;
import org.openmrs.ProgramWorkflow;
import org.openmrs.ProgramWorkflowState;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.PropertyGetter;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.RefRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.resource.impl.MetadataDelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;
import org.openmrs.module.webservices.rest.web.response.ResponseException;

import java.util.Set;

@Resource(name = RestConstants.VERSION_1 + "/workflow", supportedClass = ProgramWorkflow.class, supportedOpenmrsVersions = {
        "1.8.* - 9.*" }, order = 1)
public class ProgramWorkflowResource1_8 extends MetadataDelegatingCrudResource<ProgramWorkflow> {
	
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		if (rep instanceof DefaultRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("uuid");
			description.addProperty("concept", Representation.DEFAULT);
			description.addProperty("description");
			description.addProperty("retired");
			description.addProperty("states", Representation.DEFAULT);
			description.addProperty("concept", Representation.REF);
			description.addSelfLink();
			description.addLink("full", ".?v=" + RestConstants.REPRESENTATION_FULL);
			return description;
		} else if (rep instanceof FullRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("uuid");
			description.addProperty("concept", Representation.FULL);
			description.addProperty("description");
			description.addProperty("retired");
			description.addProperty("states", Representation.FULL);
			description.addProperty("concept");
			description.addSelfLink();
			return description;
		} else if (rep instanceof RefRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("uuid");
			description.addProperty("concept", Representation.REF);
			description.addProperty("retired");
			description.addProperty("states", Representation.REF);
			description.addSelfLink();
			return description;
		}
		return null;
	}
	
	@Override
	public Model getGETModel(Representation rep) {
		ModelImpl model = (ModelImpl) super.getGETModel(rep);
		if (rep instanceof DefaultRepresentation) {
			model
			        .property("concept", new RefProperty("#/definitions/ConceptGetRef"))
			        .property("states", new ArrayProperty(new RefProperty("#/definitions/WorkflowStateGetRef")));
		} else if (rep instanceof FullRepresentation) {
			model
			        .property("concept", new RefProperty("#/definitions/ConceptGet"))
			        .property("states", new ArrayProperty(new RefProperty("#/definitions/WorkflowStateGet")));
		} else if (rep instanceof RefRepresentation) {
			model
			        .property("concept", new RefProperty("#/definitions/ConceptGet"))
			        .property("states", new ArrayProperty(new RefProperty("#/definitions/WorkflowStateGet")));
			//FIXME should remove 'description'?
		}
		return model;
	}
	
	@PropertyGetter("states")
	public Set<ProgramWorkflowState> getUnretiredStates(ProgramWorkflow instance) {
		return instance.getStates(false);
	}
	
	@Override
	public ProgramWorkflow getByUniqueId(String uniqueId) {
		return Context.getProgramWorkflowService().getWorkflowByUuid(uniqueId);
	}
	
	@Override
	public ProgramWorkflow newDelegate() {
		return new ProgramWorkflow();
	}

	@Override
	public DelegatingResourceDescription getCreatableProperties() {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		description.addRequiredProperty("program");
		description.addRequiredProperty("concept");
		return description;
	}

	@Override
	public ProgramWorkflow save(ProgramWorkflow delegate) {
		Program parent = delegate.getProgram();
		parent.addWorkflow(delegate);
		Context.getProgramWorkflowService().saveProgram(parent);
		return delegate;
	}
	
	@Override
	public void purge(ProgramWorkflow delegate, RequestContext context) throws ResponseException {
		throw new ResourceDoesNotSupportOperationException();
	}
}
