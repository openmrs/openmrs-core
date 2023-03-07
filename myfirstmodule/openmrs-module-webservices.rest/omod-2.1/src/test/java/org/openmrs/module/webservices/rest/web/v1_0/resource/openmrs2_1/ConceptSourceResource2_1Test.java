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

import org.openmrs.ConceptSource;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResourceTest;
import org.openmrs.module.webservices.rest.web.v1_0.RestTestConstants2_1;

public class ConceptSourceResource2_1Test extends BaseDelegatingResourceTest<ConceptSourceResource2_1, ConceptSource> {
	
	@Override
	public void validateFullRepresentation() throws Exception {
		super.validateFullRepresentation();
		assertPropPresent("uniqueId");
	}
	
	@Override
	public void validateDefaultRepresentation() throws Exception {
		super.validateDefaultRepresentation();
		assertPropPresent("uniqueId");
	}
	
	@Override
	public ConceptSource newObject() {
		return Context.getConceptService().getConceptSourceByUuid(getUuidProperty());
	}
	
	@Override
	public String getDisplayProperty() {
		return "ICD-10";
	}
	
	@Override
	public String getUuidProperty() {
		return RestTestConstants2_1.CONCEPT_SOURCE_UUID;
	}
}
