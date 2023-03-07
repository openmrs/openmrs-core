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
import io.swagger.models.properties.IntegerProperty;
import org.openmrs.Cohort;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_8.CohortResource1_8;

/**
 * In openmrs-core 2.1 we did a complete rewrite of cohort membership, so although the Cohort class
 * itself didn't change we update the resource here.
 */
@Resource(name = RestConstants.VERSION_1 + "/cohort", supportedClass = Cohort.class, supportedOpenmrsVersions = { "2.1.* - 9.*" })
public class CohortResource2_1 extends CohortResource1_8 {
	
	@Override
	public String getResourceVersion() {
		return RestConstants2_1.RESOURCE_VERSION;
	}
	
	@Override
	public Model getGETModel(Representation rep) {
		ModelImpl model = (ModelImpl) super.getGETModel(rep);
		model.getProperties().remove("memberIds");
		model.property("size", new IntegerProperty());
		return model;
	}
	
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		// We do not override the CohortResource1_8 representation, because we want to basically do a clean-slate
		// representation, rather than make minor tweaks (and require a dev to look at a superclass)
		if (rep instanceof DefaultRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("uuid");
			description.addProperty("display");
			description.addProperty("name");
			description.addProperty("description");
			description.addProperty("voided");
			description.addProperty("size");
			description.addSelfLink();
			description.addLink("full", ".?v=" + RestConstants.REPRESENTATION_FULL);
			return description;
		} else if (rep instanceof FullRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("uuid");
			description.addProperty("display");
			description.addProperty("name");
			description.addProperty("description");
			description.addProperty("voided");
			description.addProperty("size");
			description.addProperty("auditInfo");
			description.addSelfLink();
			return description;
		}
		return null;
	}
}
