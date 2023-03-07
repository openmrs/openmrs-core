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
import org.openmrs.ConceptSearchResult;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.RestTestConstants1_8;
import org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResourceTest;

public class ConceptSearchResource1_9Test extends BaseDelegatingResourceTest<ConceptSearchResource1_9, ConceptSearchResult> {
	
	@Override
	public ConceptSearchResult newObject() {
		Concept concept = Context.getConceptService().getConceptByUuid(getUuidProperty());
		return new ConceptSearchResult("Yes", concept, concept.getName(), 12d);
		
	}
	
	@Override
	public void validateRefRepresentation() throws Exception {
		assertPropEquals("display", getDisplayProperty());
		assertPropPresent("concept");
		assertPropPresent("conceptName");
		assertPropNotPresent("resourceVersion");
		assertPropNotPresent("uuid");
		assertPropNotPresent("links");
		assertPropNotPresent("word");
		assertPropNotPresent("transientWeight");
		
	}
	
	@Override
	public void validateDefaultRepresentation() throws Exception {
		assertPropEquals("display", getDisplayProperty());
		assertPropPresent("resourceVersion");
		assertPropPresent("concept");
		assertPropPresent("conceptName");
		assertPropNotPresent("uuid");
		assertPropNotPresent("links");
		assertPropNotPresent("word");
		assertPropNotPresent("transientWeight");
	}
	
	@Override
	public void validateFullRepresentation() throws Exception {
		assertPropEquals("display", getDisplayProperty());
		assertPropPresent("resourceVersion");
		assertPropPresent("concept");
		assertPropPresent("conceptName");
		assertPropEquals("word", getObject().getWord());
		assertPropEquals("transientWeight", getObject().getTransientWeight());
		assertPropNotPresent("uuid");
		assertPropNotPresent("links");
	}
	
	@Override
	public String getDisplayProperty() {
		return "YES";
	}
	
	@Override
	public String getUuidProperty() {
		return RestTestConstants1_8.CONCEPT_UUID;
	}
	
}
