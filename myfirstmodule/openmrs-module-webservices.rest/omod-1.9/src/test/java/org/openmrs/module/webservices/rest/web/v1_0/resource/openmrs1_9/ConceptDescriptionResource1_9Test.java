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

import org.openmrs.ConceptDescription;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.RestTestConstants1_8;
import org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResourceTest;

public class ConceptDescriptionResource1_9Test extends BaseDelegatingResourceTest<ConceptDescriptionResource1_9, ConceptDescription> {
	
	@Override
	public ConceptDescription newObject() {
		return Context.getConceptService().getConceptDescriptionByUuid(getUuidProperty());
	}
	
	@Override
	public void validateDefaultRepresentation() throws Exception {
		super.validateDefaultRepresentation();
		assertPropEquals("description", getObject().getDescription());
		assertPropEquals("locale", getObject().getLocale());
	}
	
	@Override
	public void validateFullRepresentation() throws Exception {
		super.validateFullRepresentation();
		assertPropEquals("description", getObject().getDescription());
		assertPropEquals("locale", getObject().getLocale());
		assertPropPresent("auditInfo");
	}
	
	@Override
	public String getDisplayProperty() {
		return "Drug used for pain relief, fever, and antiplatelet actions.";
	}
	
	@Override
	public String getUuidProperty() {
		return RestTestConstants1_8.CONCEPT_DESCRIPTION_UUID;
	}
	
}
