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

import org.openmrs.ConceptReferenceTerm;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.RestTestConstants1_9;
import org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResourceTest;

public class ConceptReferenceTermResource1_9Test extends BaseDelegatingResourceTest<ConceptReferenceTermResource1_9, ConceptReferenceTerm> {
	
	@Override
	public ConceptReferenceTerm newObject() {
		return Context.getConceptService().getConceptReferenceTermByUuid(getUuidProperty());
	}
	
	@Override
	public void validateDefaultRepresentation() throws Exception {
		super.validateDefaultRepresentation();
		assertPropEquals("name", getObject().getName());
		assertPropEquals("description", getObject().getDescription());
		assertPropEquals("code", getObject().getCode());
		assertPropEquals("version", getObject().getVersion());
		assertPropEquals("retired", getObject().isRetired());
		assertPropPresent("conceptSource");
	}
	
	@Override
	public void validateFullRepresentation() throws Exception {
		super.validateFullRepresentation();
		assertPropEquals("name", getObject().getName());
		assertPropEquals("description", getObject().getDescription());
		assertPropEquals("code", getObject().getCode());
		assertPropEquals("version", getObject().getVersion());
		assertPropEquals("retired", getObject().isRetired());
		assertPropPresent("conceptSource");
		assertPropPresent("auditInfo");
	}
	
	@Override
	public String getDisplayProperty() {
		return "Some Standardized Terminology: 127cd4689 (cd4died term2)";
	}
	
	@Override
	public String getUuidProperty() {
		return RestTestConstants1_9.CONCEPT_REFERENCE_TERM_UUID;
	}
	
}
