/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.converter.openmrs2_2;

import org.openmrs.CodedOrFreeText;
import org.openmrs.Concept;
import org.openmrs.ConceptName;
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
import org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_9.ConceptNameResource1_9;
import org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs2_0.ConceptResource2_0;

@Handler(supports = CodedOrFreeText.class, order = 0)
public class CodedOrFreeTextConverter extends BaseDelegatingConverter<CodedOrFreeText> {
	
	@Override
	public SimpleObject asRepresentation(CodedOrFreeText instance, Representation rep) throws ConversionException {
		SimpleObject codedOfFreeText = new SimpleObject();
		
		if (instance.getSpecificName() != null) {
			ConceptNameResource1_9 conceptNameResource = (ConceptNameResource1_9) Context.getService(RestService.class)
			        .getResourceBySupportedClass(ConceptName.class);
			codedOfFreeText.add("specificName", conceptNameResource.asRepresentation(instance.getSpecificName(), rep));
		}
		
		if (instance.getCoded() != null) {
			ConceptResource2_0 conceptResource = (ConceptResource2_0) Context.getService(RestService.class)
			        .getResourceBySupportedClass(Concept.class);
			codedOfFreeText.add("coded", conceptResource.asRepresentation(instance.getCoded(), rep));
		}
		
		if (instance.getNonCoded() != null) {
			codedOfFreeText.add("nonCoded", instance.getNonCoded());
		}
		
		return codedOfFreeText;
	}
	
	@Override
	public CodedOrFreeText getByUniqueId(String uuid) {
		return null;
	}
	
	@Override
	public CodedOrFreeText newInstance(String type) {
		return new CodedOrFreeText();
	}
	
	@Override
	public void setProperty(Object instance, String propertyName, Object value) throws ConversionException {
		if (propertyName.equals("specificName")) {
			((CodedOrFreeText) instance).setSpecificName(Context.getConceptService().getConceptNameByUuid((String) value));
		}
		else if (propertyName.equals("coded")) {
			((CodedOrFreeText) instance).setCoded(Context.getConceptService().getConceptByUuid((String) value));
		}
		else if (propertyName.equals("nonCoded")) {
			((CodedOrFreeText) instance).setNonCoded((String) value);
		}
	}
	
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		if (rep instanceof DefaultRepresentation) {
			description.addProperty("specificName", Representation.REF);
			description.addProperty("coded", Representation.REF);
			description.addProperty("nonCoded");
		} else if (rep instanceof FullRepresentation) {
			description.addProperty("specificName", Representation.DEFAULT);
			description.addProperty("coded", Representation.DEFAULT);
			description.addProperty("nonCoded");
		}
		return description;
	}
}
