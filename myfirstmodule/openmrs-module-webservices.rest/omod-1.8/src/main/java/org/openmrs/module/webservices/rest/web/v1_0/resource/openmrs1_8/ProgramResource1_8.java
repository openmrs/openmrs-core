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
import io.swagger.models.properties.BooleanProperty;
import io.swagger.models.properties.RefProperty;
import io.swagger.models.properties.StringProperty;
import org.openmrs.Program;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.RefRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.resource.impl.EmptySearchResult;
import org.openmrs.module.webservices.rest.web.resource.impl.MetadataDelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.response.ResponseException;

import java.util.List;

@Resource(name = RestConstants.VERSION_1 + "/program", supportedClass = Program.class, supportedOpenmrsVersions = { "1.8.* - 1.9.*" }, order = 1)
public class ProgramResource1_8 extends MetadataDelegatingCrudResource<Program> {
	
	@Override
	public Program getByUniqueId(String uniqueId) {
		Program programByUuid = Context.getProgramWorkflowService().getProgramByUuid(uniqueId);
		//We assume the caller was fetching by name
		if (programByUuid == null) {
			programByUuid = Context.getProgramWorkflowService().getProgramByName(uniqueId);
		}
		return programByUuid;
	}
	
	@Override
	public Program newDelegate() {
		return new Program();
	}
	
	@Override
	public Program save(Program program) {
		return Context.getProgramWorkflowService().saveProgram(program);
	}
	
	@Override
	public void purge(Program program, RequestContext context) throws ResponseException {
		Context.getProgramWorkflowService().purgeProgram(program);
	}
	
	@Override
	protected NeedsPaging<Program> doGetAll(RequestContext context) {
		return new NeedsPaging<Program>(Context.getProgramWorkflowService().getAllPrograms(false), context);
	}
	
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		if (rep instanceof DefaultRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("uuid");
			description.addProperty("name");
			description.addProperty("description");
			description.addProperty("retired");
			// see https://issues.openmrs.org/browse/RESTWS-723 for why "allWorkflows" is returned
			description.addProperty("allWorkflows", Representation.DEFAULT);
			description.addProperty("workflows", Representation.DEFAULT);
			description.addProperty("concept", Representation.REF);
			description.addSelfLink();
			description.addLink("full", ".?v=" + RestConstants.REPRESENTATION_FULL);
			return description;
		} else if (rep instanceof FullRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("uuid");
			description.addProperty("name");
			description.addProperty("description");
			description.addProperty("retired");
			description.addProperty("allWorkflows", Representation.FULL);
			description.addProperty("workflows", Representation.FULL);
			description.addProperty("concept");
			description.addSelfLink();
			description.addProperty("auditInfo");
			return description;
		} else if (rep instanceof RefRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("uuid");
			description.addProperty("display");
			description.addProperty("retired");
			description.addProperty("allWorkflows", Representation.REF);
			description.addProperty("workflows", Representation.REF);
			description.addSelfLink();
			return description;
		}
		return null;
	}
	
	@Override
	public DelegatingResourceDescription getCreatableProperties() {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		description.addRequiredProperty("name");
		description.addRequiredProperty("description");
		description.addRequiredProperty("concept");
		
		description.addProperty("retired");
		return description;
	}
	
	@Override
	public Model getGETModel(Representation rep) {
		ModelImpl model = (ModelImpl) super.getGETModel(rep);
		if (rep instanceof DefaultRepresentation) {
			model
			        .property("concept", new RefProperty("#/definitions/ConceptGetRef"))
			        .property("allWorkflows", new ArrayProperty(new RefProperty("#/definitions/WorkflowGetRef")));
		} else if (rep instanceof FullRepresentation) {
			model
			        .property("concept", new RefProperty("#/definitions/ConceptGet"))
			        .property("allWorkflows", new ArrayProperty(new RefProperty("#/definitions/WorkflowGet")));
		} else if (rep instanceof RefRepresentation) {
			model
			        .property("allWorkflows", new ArrayProperty(new RefProperty("#/definitions/WorkflowGetRef")));
		}
		return model;
	}
	
	@Override
	public Model getCREATEModel(Representation rep) {
		ModelImpl model = ((ModelImpl) super.getCREATEModel(rep))
		        .property("concept", new StringProperty().example("uuid"))
		        .property("retired", new BooleanProperty())
		        
		        .required("concept").required("description");
		if (rep instanceof FullRepresentation) {
			model
			        .property("concept", new RefProperty("#/definitions/ConceptCreate"));
		}
		return model;
	}
	
	@Override
	public Model getUPDATEModel(Representation rep) {
		return new ModelImpl(); //FIXME missing props
	}
	
	@Override
	protected PageableResult doSearch(RequestContext context) {
		String query = context.getParameter("q");
		
		if (query != null) {
			List<Program> programs = Context.getProgramWorkflowService().getPrograms(query);
			return new NeedsPaging<Program>(programs, context);
		}
		return new EmptySearchResult();
	}
	
}
