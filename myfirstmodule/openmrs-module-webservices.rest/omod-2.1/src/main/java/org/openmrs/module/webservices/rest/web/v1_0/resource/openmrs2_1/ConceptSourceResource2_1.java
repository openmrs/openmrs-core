/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs2_1;

import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.properties.StringProperty;
import org.openmrs.ConceptSource;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs2_0.ConceptSourceResource2_0;

/**
 * {@link Resource} for {@link ConceptSource}, supporting standard CRUD operations
 */
@Resource(name = RestConstants.VERSION_1 + "/conceptsource", supportedClass = ConceptSource.class, supportedOpenmrsVersions = {
        "2.1.* - 9.*" })
public class ConceptSourceResource2_1 extends ConceptSourceResource2_0 {
	
	@Override
	public Model getGETModel(Representation rep) {
		ModelImpl model = (ModelImpl) super.getGETModel(rep);
		if (rep instanceof DefaultRepresentation || rep instanceof FullRepresentation) {
			model
			        .property("uniqueId", new StringProperty());
		}
		return model;
	}
	
	@Override
	public Model getCREATEModel(Representation representation) {
		return ((ModelImpl) super.getCREATEModel(representation))
		        .property("uniqueId", new StringProperty());
	}
	
	/**
	 * @see DelegatingCrudResource#getRepresentationDescription(Representation)
	 */
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		if (rep instanceof DefaultRepresentation || rep instanceof FullRepresentation) {
			DelegatingResourceDescription description = super.getRepresentationDescription(rep);
			description.addProperty("uniqueId");
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
		description.addProperty("uniqueId");
		return description;
	}
	
	@Override
	public String getResourceVersion() {
		return RestConstants2_1.RESOURCE_VERSION;
	}
}
