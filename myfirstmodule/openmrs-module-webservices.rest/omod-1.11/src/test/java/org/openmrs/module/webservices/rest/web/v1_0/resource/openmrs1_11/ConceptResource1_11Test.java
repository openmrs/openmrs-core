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
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.RestTestConstants1_8;
import org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResourceTest;

public class ConceptResource1_11Test extends BaseDelegatingResourceTest<ConceptResource1_11, Concept> {
	
	@Override
	public Concept newObject() {
		return Context.getConceptService().getConceptByUuid(getUuidProperty());
	}
	
	@Override
	public void validateFullRepresentation() throws Exception {
		super.validateFullRepresentation();
		assertPropNotPresent("precise");
		assertPropPresent("allowDecimal");
		assertPropPresent("displayPrecision");
	}
	
	@Override
	public String getDisplayProperty() {
		return "CD4 COUNT";
	}
	
	@Override
	public String getUuidProperty() {
		return RestTestConstants1_8.CONCEPT_NUMERIC_UUID;
	}
}
