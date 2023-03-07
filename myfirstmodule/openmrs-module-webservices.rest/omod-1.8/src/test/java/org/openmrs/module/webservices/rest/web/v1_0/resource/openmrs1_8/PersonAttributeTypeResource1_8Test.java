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

import org.junit.Before;
import org.openmrs.PersonAttributeType;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.RestTestConstants1_8;
import org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResourceTest;

public class PersonAttributeTypeResource1_8Test extends BaseDelegatingResourceTest<PersonAttributeTypeResource1_8, PersonAttributeType> {
	
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
	public void validateDefaultRepresentation() throws Exception {
		super.validateDefaultRepresentation();
		assertPropEquals("name", getObject().getName());
		assertPropEquals("description", getObject().getDescription());
		assertPropEquals("format", getObject().getFormat());
		assertPropEquals("foreignKey", getObject().getForeignKey());
		assertPropEquals("sortWeight", getObject().getSortWeight());
		assertPropEquals("searchable", getObject().getSearchable());
		assertPropEquals("editPrivilege", getObject().getEditPrivilege());
		assertPropEquals("retired", getObject().getRetired());
	}
	
	@Override
	public void validateFullRepresentation() throws Exception {
		super.validateFullRepresentation();
		assertPropEquals("name", getObject().getName());
		assertPropEquals("description", getObject().getDescription());
		assertPropEquals("format", getObject().getFormat());
		assertPropEquals("foreignKey", getObject().getForeignKey());
		assertPropEquals("sortWeight", getObject().getSortWeight());
		assertPropEquals("searchable", getObject().getSearchable());
		assertPropEquals("editPrivilege", getObject().getEditPrivilege());
		assertPropEquals("retired", getObject().getRetired());
		assertPropPresent("auditInfo");
	}
	
	@Override
	public String getDisplayProperty() {
		return "Race";
	}
	
	@Override
	public String getUuidProperty() {
		return RestTestConstants1_8.PERSON_ATTRIBUTE_TYPE_UUID;
	}
}
