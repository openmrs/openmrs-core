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

import org.openmrs.ConceptReferenceTermMap;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.RestTestConstants1_9;
import org.openmrs.module.webservices.rest.web.api.RestHelperService;
import org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResourceTest;

public class ConceptReferenceTermMapResource1_9Test extends BaseDelegatingResourceTest<ConceptReferenceTermMapResource1_9, ConceptReferenceTermMap> {
	
	@Override
	public ConceptReferenceTermMap newObject() {
		return Context.getService(RestHelperService.class).getObjectByUuid(ConceptReferenceTermMap.class, getUuidProperty());
	}
	
	@Override
	public String getDisplayProperty() {
		return "Some Standardized Terminology: WGT234 - Some Standardized Terminology: CD41003";
	}
	
	@Override
	public String getUuidProperty() {
		return RestTestConstants1_9.CONCEPT_REFERENCE_TERM_MAP_UUID;
	}
	
}
