/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_9;

import org.openmrs.Concept;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_8.ConceptResource1_8;

/**
 * Implemented to increase the resource version since the mapping subresource changed.
 */
@Resource(name = RestConstants.VERSION_1 + "/concept", order = 1, supportedClass = Concept.class, supportedOpenmrsVersions = {
        "1.9.* - 1.10.*" })
public class ConceptResource1_9 extends ConceptResource1_8 {
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#getResourceVersion()
	 */
	@Override
	public String getResourceVersion() {
		return RestConstants1_9.RESOURCE_VERSION;
	}
	
	@Override
	public Concept getByUniqueId(String identifier) {
		
		Concept concept = null;
		
		if (identifier.contains(":")) {
			String[] tokens = identifier.split(":");
			String sourceName = tokens[0];
			String termCode = tokens[1];
			concept = Context.getConceptService().getConceptByMapping(termCode, sourceName, true);
		} else {
			concept = Context.getConceptService().getConceptByUuid(identifier);
		}
		
		return concept;
	}
}
