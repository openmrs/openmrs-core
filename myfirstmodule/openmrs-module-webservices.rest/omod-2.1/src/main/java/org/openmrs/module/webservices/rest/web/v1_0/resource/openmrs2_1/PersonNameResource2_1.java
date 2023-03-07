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
import org.openmrs.PersonName;
import org.openmrs.module.webservices.rest.web.annotation.SubResource;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_11.PersonResource1_11;
import org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs2_0.PersonNameResource2_0;

@SubResource(parent = PersonResource1_11.class, path = "name", supportedClass = PersonName.class, supportedOpenmrsVersions = {
        "2.1.* - 9.*" })
public class PersonNameResource2_1 extends PersonNameResource2_0 {
	
	@Override
	public DelegatingResourceDescription getCreatableProperties() {
		DelegatingResourceDescription resourceDescription = super.getCreatableProperties();
		resourceDescription.getProperties().get("familyName").setRequired(false);
		return resourceDescription;
	}
	
	@Override
	public Model getCREATEModel(Representation rep) {
		Model model = super.getCREATEModel(rep);
		model.getProperties().get("familyName").setRequired(false);
		return model;
	}
}
