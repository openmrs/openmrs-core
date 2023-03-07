/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs2_0;

import org.openmrs.Concept;
import org.openmrs.ConceptAttribute;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.PropertySetter;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_11.ConceptResource1_11;

import java.util.Arrays;
import java.util.List;

@Resource(name = RestConstants.VERSION_1 + "/concept", supportedClass = Concept.class, supportedOpenmrsVersions = { "2.0.* - 9.*" })
public class ConceptResource2_0 extends ConceptResource1_11 {
	
	/**
	 * Sets attributes on the given Concept.
	 * 
	 * @param instance
	 * @param attrs
	 */
	@PropertySetter("attributes")
	public static void setAttributes(Concept instance, List<ConceptAttribute> attrs) {
		for (ConceptAttribute attr : attrs) {
			instance.addAttribute(attr);
		}
	}
	
	/**
	 * @see DelegatingCrudResource#fullRepresentationDescription(Concept)
	 */
	@Override
	protected DelegatingResourceDescription fullRepresentationDescription(Concept delegate) {
		DelegatingResourceDescription description = super.fullRepresentationDescription(delegate);
		description.addProperty("attributes", "activeAttributes", Representation.DEFAULT);
		return description;
	}
	
	/**
	 * @see DelegatingCrudResource#getRepresentationDescription(Representation)
	 */
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		if (rep instanceof DefaultRepresentation) {
			DelegatingResourceDescription description = super.getRepresentationDescription(rep);
			description.addProperty("attributes", "activeAttributes", Representation.REF);
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
		description.addProperty("attributes");
		return description;
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#getUpdatableProperties()
	 */
	@Override
	public DelegatingResourceDescription getUpdatableProperties() {
		DelegatingResourceDescription description = super.getUpdatableProperties();
		description.addProperty("attributes");
		return description;
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#getPropertiesToExposeAsSubResources()
	 */
	@Override
	public List<String> getPropertiesToExposeAsSubResources() {
		return Arrays.asList("attributes");
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#getResourceVersion()
	 */
	@Override
	public String getResourceVersion() {
		return RestConstants2_0.RESOURCE_VERSION;
	}
}
