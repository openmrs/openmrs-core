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
import io.swagger.models.properties.ObjectProperty;
import io.swagger.models.properties.RefProperty;
import io.swagger.models.properties.StringProperty;
import org.openmrs.Patient;
import org.openmrs.activelist.Allergy;
import org.openmrs.activelist.AllergySeverity;
import org.openmrs.activelist.AllergyType;
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
 * {@link Resource} for Allergy, supporting standard CRUD operations
 */
@Resource(name = RestConstants.VERSION_1 + "/allergy", supportedClass = Allergy.class, supportedOpenmrsVersions = { "1.8.*" })
public class AllergyResource1_8 extends BaseActiveListItemResource1_8<Allergy> {
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#getRepresentationDescription(org.openmrs.module.webservices.rest.web.representation.Representation)
	 */
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		if (rep instanceof DefaultRepresentation) {
			DelegatingResourceDescription description = super.getRepresentationDescription(rep);
			description.addProperty("allergyType");
			description.addProperty("reaction", Representation.REF);
			description.addProperty("severity");
			description.addProperty("allergen", Representation.REF);
			return description;
		} else if (rep instanceof FullRepresentation) {
			DelegatingResourceDescription description = super.getRepresentationDescription(rep);
			description.addProperty("allergyType");
			description.addProperty("reaction", Representation.DEFAULT);
			description.addProperty("severity");
			description.addProperty("allergen", Representation.DEFAULT);
			return description;
		}
		return null;
	}
	
	@Override
	public Model getGETModel(Representation rep) {
		ModelImpl model = ((ModelImpl) super.getGETModel(rep))
		        .property("allergyType", new EnumProperty(AllergyType.class))
		        .property("reaction", new RefProperty("#/definitions/ConceptGetRef"))
		        .property("severity", new EnumProperty(AllergySeverity.class))
		        .property("allergen", new RefProperty("#/definitions/ConceptGetRef"));
		if (rep instanceof FullRepresentation) {
			model
			        .property("reaction", new RefProperty("#/definitions/ConceptGet"))
			        .property("allergen", new RefProperty("#/definitions/ConceptGet"));
		}
		return model;
	}
	
	@Override
	public Model getCREATEModel(Representation rep) {
		return ((ModelImpl) super.getCREATEModel(rep))
		        .property("allergyType", new EnumProperty(AllergyType.class))
		        .property("reaction", new ObjectProperty()
		                .property("uuid", new StringProperty()))
		        .property("severity", new EnumProperty(AllergySeverity.class))
		        .property("allergen", new StringProperty());
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#newDelegate()
	 */
	@Override
	public Allergy newDelegate() {
		return new Allergy();
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#getCreatableProperties()
	 */
	@Override
	public DelegatingResourceDescription getCreatableProperties() {
		DelegatingResourceDescription description = super.getCreatableProperties();
		description.addRequiredProperty("allergyType");
		description.addRequiredProperty("allergen");
		description.addProperty("reaction");
		description.addProperty("severity");
		
		return description;
	}
	
	/**
	 * Display string for allergy
	 * 
	 * @param allergy
	 * @return String ConceptName
	 */
	@PropertyGetter("display")
	public String getDisplayString(Allergy allergy) {
		if (allergy.getAllergen() == null)
			return "";
		
		return allergy.getAllergen().getName().toString();
	}
	
	/**
	 * Annotated setter for allergen
	 * 
	 * @param allergen
	 * @param value
	 */
	@PropertySetter("allergen")
	public static void setAllergen(Allergy allergy, Object value) {
		allergy.setAllergen(Context.getConceptService().getConceptByUuid((String) value));
	}
	
	/**
	 * Gets allergies for a given patient (paged according to context if necessary) only if a
	 * patient parameter exists in the request set on the {@link RequestContext}
	 * 
	 * @param context
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#doSearch(RequestContext)
	 */
	@Override
	protected PageableResult doSearch(RequestContext context) {
		String patientUuid = context.getRequest().getParameter("patient");
		if (patientUuid != null) {
			Patient patient = ((PatientResource1_8) Context.getService(RestService.class).getResourceBySupportedClass(
			    Patient.class)).getByUniqueId(patientUuid);
			if (patient == null)
				return new EmptySearchResult();
			
			return new NeedsPaging<Allergy>(Context.getPatientService().getAllergies(patient), context);
		}
		
		//currently this is not supported since the superclass throws an exception
		return super.doSearch(context);
	}
	
}
