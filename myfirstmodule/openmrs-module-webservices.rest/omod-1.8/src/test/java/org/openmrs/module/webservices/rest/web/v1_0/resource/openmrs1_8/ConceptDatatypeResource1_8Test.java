/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_8;

import org.openmrs.module.webservices.rest.web.RestTestConstants1_8;
import org.openmrs.ConceptDatatype;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResourceTest;

public class ConceptDatatypeResource1_8Test extends BaseDelegatingResourceTest<ConceptDatatypeResource1_8, ConceptDatatype> {
	
	@Override
	public ConceptDatatype newObject() {
		return Context.getConceptService().getConceptDatatypeByUuid(getUuidProperty());
	}
	
	@Override
	public void validateDefaultRepresentation() throws Exception {
		super.validateDefaultRepresentation();
		assertPropEquals("name", getObject().getName());
		assertPropEquals("description", getObject().getDescription());
		assertPropEquals("hl7Abbreviation", getObject().getHl7Abbreviation());
		assertPropEquals("retired", getObject().getRetired());
	}
	
	@Override
	public void validateFullRepresentation() throws Exception {
		super.validateFullRepresentation();
		assertPropEquals("name", getObject().getName());
		assertPropEquals("description", getObject().getDescription());
		assertPropEquals("hl7Abbreviation", getObject().getHl7Abbreviation());
		assertPropEquals("retired", getObject().getRetired());
		assertPropPresent("auditInfo");
	}
	
	@Override
	public String getDisplayProperty() {
		return "N/A";
	}
	
	@Override
	public String getUuidProperty() {
		return RestTestConstants1_8.CONCEPT_DATATYPE_UUID;
	}
	
}
