/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.converter.openmrs2_0;

import org.openmrs.Allergen;
import org.openmrs.Concept;
import org.openmrs.annotation.Handler;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.api.RestService;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingConverter;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.response.ConversionException;
import org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_11.ConceptResource1_11;

/**
 * An implementation of Converter to be able to create a representation from a Allergen when
 * Allergen is used in another resource.
 */
@Handler(supports = Allergen.class, order = 0)
public class AllergenConverter2_0 extends BaseDelegatingConverter<Allergen> {
	
	/**
	 * Gets the {@link DelegatingResourceDescription} for the given representation for this
	 * resource, if it exists
	 * 
	 * @param rep
	 * @return
	 */
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		if (rep instanceof DefaultRepresentation) {
			description.addProperty("allergenType", Representation.REF);
			description.addProperty("codedAllergen", Representation.REF);
			description.addProperty("nonCodedAllergen");
		} else if (rep instanceof FullRepresentation) {
			description.addProperty("allergenType", Representation.DEFAULT);
			description.addProperty("codedAllergen", Representation.DEFAULT);
			description.addProperty("nonCodedAllergen");
		}
		return description;
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.api.Converter#getByUniqueId(java.lang.String)
	 */
	@Override
	public Allergen getByUniqueId(String string) {
		return null;
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.api.Converter#newInstance(java.lang.String)
	 */
	@Override
	public Allergen newInstance(String type) {
		return new Allergen();
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.api.Converter#asRepresentation(T,
	 *      org.openmrs.module.webservices.rest.web.representation.Representation)
	 */
	@Override
	public SimpleObject asRepresentation(Allergen instance, Representation rep) throws ConversionException {
		SimpleObject allergenObject = new SimpleObject();
		allergenObject.add("allergenType", instance.getAllergenType());
		ConceptResource1_11 conceptResource = (ConceptResource1_11) Context.getService(RestService.class)
		        .getResourceBySupportedClass(Concept.class);
		allergenObject.add("codedAllergen", conceptResource.asRepresentation(instance.getCodedAllergen(), rep));
		allergenObject.add("nonCodedAllergen", instance.getNonCodedAllergen());
		return allergenObject;
	}
}
