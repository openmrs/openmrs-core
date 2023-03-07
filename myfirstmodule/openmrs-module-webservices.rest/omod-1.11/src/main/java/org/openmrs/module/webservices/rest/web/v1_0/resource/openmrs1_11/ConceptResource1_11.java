/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_11;

import org.openmrs.Concept;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_9.ConceptResource1_9;

@Resource(name = RestConstants.VERSION_1 + "/concept", supportedClass = Concept.class, supportedOpenmrsVersions = {
        "1.11.* - 1.12.*" })
public class ConceptResource1_11 extends ConceptResource1_9 {
	
	/**
	 * @see DelegatingCrudResource#fullRepresentationDescription(Concept)
	 */
	@Override
	protected DelegatingResourceDescription fullRepresentationDescription(Concept delegate) {
		DelegatingResourceDescription description = super.fullRepresentationDescription(delegate);
		if (delegate.isNumeric()) {
			description.removeProperty("precise");
			description.addProperty("allowDecimal");
			description.addProperty("displayPrecision");
		}
		return description;
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#getResourceVersion()
	 */
	@Override
	public String getResourceVersion() {
		return RestConstants1_11.RESOURCE_VERSION;
	}
}
