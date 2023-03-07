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

import org.openmrs.AllergyReaction;
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
 * An implementation of Converter to be able to create a representation from a AllergyReaction when
 * AllergyReaction is used in another resource.
 */
@Handler(supports = AllergyReaction.class, order = 0)
public class AllergyReactionConverter2_0 extends BaseDelegatingConverter<AllergyReaction> {
	
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
			description.addProperty("reaction", Representation.REF);
			description.addProperty("reactionNonCoded");
		} else if (rep instanceof FullRepresentation) {
			description.addProperty("reaction", Representation.DEFAULT);
			description.addProperty("reactionNonCoded");
		}
		return description;
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.api.Converter#getByUniqueId(java.lang.String)
	 */
	@Override
	public AllergyReaction getByUniqueId(String string) {
		return null;
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.api.Converter#newInstance(java.lang.String)
	 */
	@Override
	public AllergyReaction newInstance(String type) {
		return new AllergyReaction();
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.api.Converter#asRepresentation(T,
	 *      org.openmrs.module.webservices.rest.web.representation.Representation)
	 */
	@Override
	public SimpleObject asRepresentation(AllergyReaction instance, Representation rep) throws ConversionException {
		SimpleObject allergenReactionObject = new SimpleObject();
		Concept reaction = instance.getReaction();
		ConceptResource1_11 conceptResource = (ConceptResource1_11) Context.getService(RestService.class)
		        .getResourceBySupportedClass(Concept.class);
		allergenReactionObject.add("reaction", conceptResource.asRepresentation(reaction, rep));
		allergenReactionObject.add("reactionNonCoded", instance.getReactionNonCoded());
		return allergenReactionObject;
	}
}
