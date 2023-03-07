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

import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.properties.RefProperty;
import io.swagger.models.properties.StringProperty;
import org.openmrs.Program;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.RefRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_8.ProgramResource1_8;

@Resource(name = RestConstants.VERSION_1 + "/program", supportedClass = Program.class, supportedOpenmrsVersions = {
        "1.10.* - 9.*" })
public class ProgramResource1_10 extends ProgramResource1_8 {
	
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		if (rep instanceof DefaultRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("name");
			description.addProperty("uuid");
			description.addProperty("retired");
			description.addProperty("description");
			description.addProperty("concept", Representation.REF);
			description.addProperty("allWorkflows", Representation.DEFAULT);
			description.addProperty("outcomesConcept", Representation.FULL);
			description.addSelfLink();
			description.addLink("full", ".?v=" + RestConstants.REPRESENTATION_FULL);
			return description;
		} else if (rep instanceof FullRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("uuid");
			description.addProperty("name");
			description.addProperty("description");
			description.addProperty("retired");
			description.addProperty("concept");
			description.addProperty("outcomesConcept");
			description.addProperty("allWorkflows", Representation.FULL);
			description.addSelfLink();
			description.addProperty("auditInfo");
			return description;
		} else if (rep instanceof RefRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("uuid");
			description.addProperty("name");
			description.addProperty("allWorkflows", Representation.REF);
			description.addSelfLink();
			return description;
		}
		return null;
	}
	
	@Override
	public Model getCREATEModel(Representation rep) {
		ModelImpl model = ((ModelImpl) super.getCREATEModel(rep))
		        .property("outcomesConcept", new StringProperty().example("uuid"));
		if (rep instanceof FullRepresentation) {
			model
			        .property("outcomesConcept", new RefProperty("#/definitions/ConceptCreate"));
		}
		return model;
	}
	
	@Override
	public DelegatingResourceDescription getCreatableProperties() {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		description.addRequiredProperty("name");
		description.addRequiredProperty("description");
		description.addRequiredProperty("concept");
		
		description.addProperty("outcomesConcept");
		description.addProperty("retired");
		return description;
	}
	
}
