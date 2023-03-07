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
import org.openmrs.Obs;
import org.openmrs.module.webservices.docs.swagger.core.property.EnumProperty;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_11.ObsResource1_11;

/**
 * Resource for `obs`, supporting the new properties added in openmrs-core 2.1 (status and
 * interpretation)
 */
@Resource(name = RestConstants.VERSION_1 + "/obs", supportedClass = Obs.class, supportedOpenmrsVersions = { "2.1.* - 9.*" })
public class ObsResource2_1 extends ObsResource1_11 {
	
	@Override
	public Model getGETModel(Representation rep) {
		return ((ModelImpl) super.getGETModel(rep))
		        .property("status", new EnumProperty(Obs.Status.class))
		        .property("interpretation", new EnumProperty(Obs.Interpretation.class));
	}
	
	@Override
	public Model getCREATEModel(Representation rep) {
		return ((ModelImpl) super.getCREATEModel(rep))
		        .property("status", new EnumProperty(Obs.Status.class))
		        .property("interpretation", new EnumProperty(Obs.Interpretation.class));
	}
	
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		DelegatingResourceDescription description = super.getRepresentationDescription(rep);
		if (description != null) {
			description.addProperty("status");
			description.addProperty("interpretation");
		}
		return description;
	}
	
	@Override
	public DelegatingResourceDescription getCreatableProperties() {
		DelegatingResourceDescription description = super.getCreatableProperties();
		description.addProperty("status");
		description.addProperty("interpretation");
		return description;
	}
	
	@Override
	public String getResourceVersion() {
		return RestConstants2_1.RESOURCE_VERSION;
	}
}
