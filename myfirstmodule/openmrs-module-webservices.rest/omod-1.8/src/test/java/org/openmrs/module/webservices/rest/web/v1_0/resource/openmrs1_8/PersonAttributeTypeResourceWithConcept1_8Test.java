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

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.openmrs.Concept;
import org.openmrs.PersonAttributeType;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResourceTest;

public class PersonAttributeTypeResourceWithConcept1_8Test extends BaseDelegatingResourceTest<PersonAttributeTypeResource1_8, PersonAttributeType> {
	
	private static final String ACTIVE_LIST_INITIAL_XML = "personAttributeTypeWithConcept.xml";
	
	@Before
	public void init() throws Exception {
		executeDataSet(ACTIVE_LIST_INITIAL_XML);
	}
	
	@Override
	public PersonAttributeType newObject() {
		return Context.getPersonService().getPersonAttributeTypeByUuid(getUuidProperty());
	}
	
	@Override
	public void validateFullRepresentation() throws Exception {
		Concept concept = (Concept) getRepresentation().get("concept");
		assertEquals("d102c80f-1yz9-4da3-bb88-8122ce8868dd", concept.getUuid());
	}
	
	@Override
	public String getDisplayProperty() {
		return "Caste";
	}
	
	@Override
	public String getUuidProperty() {
		return "55e6ce9e-25bf-11e3-a013-3c0754156a5d";
	}
}
