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
import org.openmrs.module.webservices.rest.web.RestTestConstants1_8;
import org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResourceTest;

public class ConceptNumericResource1_9Test extends BaseDelegatingResourceTest<ConceptResource1_9, Concept> {
	
	@Override
	public Concept newObject() {
		return Context.getConceptService().getConceptByUuid(getUuidProperty());
	}
	
	@Override
	public void validateDefaultRepresentation() throws Exception {
		super.validateDefaultRepresentation();
		assertPropPresent("name");
		assertPropPresent("datatype");
		assertPropPresent("conceptClass");
		assertPropPresent("set");
		assertPropEquals("version", getObject().getVersion());
		assertPropEquals("retired", getObject().isRetired());
		assertPropPresent("names");
		assertPropPresent("descriptions");
		assertPropEquals("display", getDisplayProperty());
		assertPropPresent("answers");
		assertPropPresent("setMembers");
	}
	
	@Override
	public void validateFullRepresentation() throws Exception {
		super.validateFullRepresentation();
		assertPropPresent("name");
		assertPropPresent("datatype");
		assertPropPresent("conceptClass");
		assertPropPresent("set");
		assertPropEquals("version", getObject().getVersion());
		assertPropEquals("retired", getObject().isRetired());
		assertPropPresent("names");
		assertPropPresent("descriptions");
		assertPropPresent("auditInfo");
		assertPropEquals("display", getDisplayProperty());
		assertPropPresent("answers");
		assertPropPresent("setMembers");
		assertPropPresent("hiNormal");
		assertPropPresent("hiAbsolute");
		assertPropPresent("hiCritical");
		assertPropPresent("lowNormal");
		assertPropPresent("lowAbsolute");
		assertPropPresent("lowCritical");
		assertPropPresent("units");
		assertPropPresent("precise");
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
