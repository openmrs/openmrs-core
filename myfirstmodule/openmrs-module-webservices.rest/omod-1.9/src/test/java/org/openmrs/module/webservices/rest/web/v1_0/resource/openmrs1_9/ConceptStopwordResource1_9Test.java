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

import org.openmrs.ConceptStopWord;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResourceTest;

import java.util.List;

/**
 * Contains tests for the {@link ConceptStopWordResource1_9}
 */
public class ConceptStopwordResource1_9Test extends BaseDelegatingResourceTest<ConceptStopwordResource1_9, ConceptStopWord> {
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResourceTest#newObject()
	 */
	@Override
	public ConceptStopWord newObject() {
		List<ConceptStopWord> datatypes = Context.getConceptService().getAllConceptStopWords();
		for (ConceptStopWord datatype : datatypes) {
			if (datatype.getUuid().equals(getUuidProperty())) {
				return datatype;
			}
		}
		return null;
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResourceTest#validateDefaultRepresentation()
	 */
	@Override
	public void validateDefaultRepresentation() throws Exception {
		super.validateDefaultRepresentation();
		assertPropPresent("uuid");
		assertPropPresent("display");
		assertPropPresent("value");
		assertPropPresent("locale");
		assertPropEquals("resourceVersion", "1.9");
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResourceTest#validateFullRepresentation()
	 */
	@Override
	public void validateFullRepresentation() throws Exception {
		super.validateFullRepresentation();
		assertPropPresent("uuid");
		assertPropPresent("display");
		assertPropPresent("value");
		assertPropPresent("locale");
		assertPropEquals("resourceVersion", "1.9");
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResourceTest#getDisplayProperty()
	 */
	@Override
	public String getDisplayProperty() {
		return "AN";
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResourceTest#getUuidProperty()
	 */
	@Override
	public String getUuidProperty() {
		return "75af8c00-3ab2-4c1d-8a8d-3c0e5c2972ec";
	}
}
