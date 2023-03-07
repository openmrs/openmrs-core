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
import io.swagger.models.properties.DoubleProperty;
import io.swagger.models.properties.RefProperty;
import org.openmrs.Patient;
import org.openmrs.activelist.Problem;
import org.openmrs.activelist.ProblemModifier;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.docs.swagger.core.property.EnumProperty;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.PropertyGetter;
import org.openmrs.module.webservices.rest.web.annotation.PropertySetter;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.api.RestService;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.resource.impl.EmptySearchResult;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;

/**
 * {@link Resource} for Problem, supporting standard CRUD operations
 */
@Resource(name = RestConstants.VERSION_1 + "/problem", supportedClass = Problem.class, supportedOpenmrsVersions = { "1.8.*" })
public class ProblemResource1_8 extends BaseActiveListItemResource1_8<Problem> {
	
	@Override
	public Model getGETModel(Representation rep) {
		ModelImpl model = (ModelImpl) super.getGETModel(rep);
		if (rep instanceof DefaultRepresentation || rep instanceof FullRepresentation) {
			model
			        .property("modifier", new EnumProperty(ProblemModifier.class))
			        .property("sortWeight", new DoubleProperty());
		}
		if (rep instanceof DefaultRepresentation) {
			model
			        .property("problem", new RefProperty("#/definitions/ConceptGetRef"));
			
		} else if (rep instanceof FullRepresentation) {
			model
			        .property("problem", new RefProperty("#/definitions/ConceptGet"));
			
		}
		return model;
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#getRepresentationDescription(org.openmrs.module.webservices.rest.web.representation.Representation)
	 */
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		if (rep instanceof DefaultRepresentation) {
			DelegatingResourceDescription description = super.getRepresentationDescription(rep);
			description.addProperty("modifier");
			description.addProperty("sortWeight");
			description.addProperty("problem", Representation.REF);
			return description;
		} else if (rep instanceof FullRepresentation) {
			DelegatingResourceDescription description = super.getRepresentationDescription(rep);
			description.addProperty("modifier");
			description.addProperty("sortWeight");
			description.addProperty("problem", Representation.DEFAULT);
			return description;
		}
		return null;
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#getCreatableProperties()
	 */
	@Override
	public DelegatingResourceDescription getCreatableProperties() {
		DelegatingResourceDescription description = super.getCreatableProperties();
		description.addRequiredProperty("problem");
		description.addProperty("modifier");
		description.addProperty("sortWeight");
		
		return description;
	}
	
	@Override
	public Model getCREATEModel(Representation rep) {
		return ((ModelImpl) super.getCREATEModel(rep))
		        .property("problem", new RefProperty("#/definitions/ConceptCreate"))
		        .property("modifier", new EnumProperty(ProblemModifier.class))
		        .property("sortWeight", new DoubleProperty())
		        
		        .required("problem");
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#newDelegate()
	 */
	@Override
	public Problem newDelegate() {
		return new Problem();
	}
	
	/**
	 * Display string for Problem
	 * 
	 * @param problem
	 * @return String ConceptName
	 */
	@PropertyGetter("display")
	public String getDisplayString(Problem problem) {
		if (problem.getProblem() == null)
			return "";
		
		return problem.getProblem().getName().toString();
	}
	
	/**
	 * Annotated setter for Problem
	 * 
	 * @param problem
	 * @param value
	 */
	
	@PropertySetter("problem")
	public static void setProblem(Problem problem, Object value) {
		problem.setProblem(Context.getConceptService().getConceptByUuid((String) value));
	}
	
	/**
	 * Gets problems for a given patient (paged according to context if necessary) only if a patient
	 * parameter exists in the request set on the {@link RequestContext}
	 * 
	 * @param context
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#doSearch(org.openmrs.module.webservices.rest.web.RequestContext)
	 */
	@Override
	protected PageableResult doSearch(RequestContext context) {
		String patientUuid = context.getRequest().getParameter("patient");
		if (patientUuid != null) {
			Patient patient = ((PatientResource1_8) Context.getService(RestService.class).getResourceBySupportedClass(
			    Patient.class)).getByUniqueId(patientUuid);
			if (patient == null)
				return new EmptySearchResult();
			return new NeedsPaging<Problem>(Context.getPatientService().getProblems(patient), context);
		}
		
		//currently this is not supported since the superclass throws an exception
		return super.doSearch(context);
	}
	
}
