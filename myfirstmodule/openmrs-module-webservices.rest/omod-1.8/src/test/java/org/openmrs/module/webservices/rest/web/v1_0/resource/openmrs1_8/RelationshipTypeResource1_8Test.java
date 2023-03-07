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

import org.junit.Test;
import org.openmrs.RelationshipType;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestTestConstants1_8;
import org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResourceTest;

/**
 * Contains tests for the
 * {@link org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_8.RelationShipTypeResource1_8}
 */
public class RelationshipTypeResource1_8Test extends BaseDelegatingResourceTest<RelationShipTypeResource1_8, RelationshipType> {
	
	@Override
	public RelationshipType newObject() {
		return Context.getPersonService().getRelationshipTypeByUuid(getUuidProperty());
	}
	
	@Override
	public void validateDefaultRepresentation() throws Exception {
		super.validateDefaultRepresentation();
		assertPropEquals("display", getObject().toString());
		assertPropEquals("description", getObject().getDescription());
		assertPropEquals("aIsToB", getObject().getaIsToB());
		assertPropEquals("bIsToA", getObject().getbIsToA());
		assertPropEquals("displayAIsToB", getObject().getaIsToB());
		assertPropEquals("displayBIsToA", getObject().getbIsToA());
		assertPropEquals("retired", getObject().isRetired());
	}
	
	@Override
	public void validateFullRepresentation() throws Exception {
		super.validateFullRepresentation();
		assertPropEquals("display", getObject().toString());
		assertPropEquals("description", getObject().getDescription());
		assertPropEquals("aIsToB", getObject().getaIsToB());
		assertPropEquals("bIsToA", getObject().getbIsToA());
		assertPropEquals("displayAIsToB", getObject().getaIsToB());
		assertPropEquals("displayBIsToA", getObject().getbIsToA());
		assertPropEquals("retired", getObject().isRetired());
		assertPropEquals("weight", getObject().getWeight());
		assertPropPresent("auditInfo");
	}
	
	@Override
	public String getDisplayProperty() {
		return getObject().toString();
	}
	
	@Override
	public String getUuidProperty() {
		return RestTestConstants1_8.RELATIONSHIP_TYPE_UUID;
	}
	
	@Test
	public void shouldIgnoreRelationshipSpecificPropertiesWhenCreating() {
		SimpleObject relationshipTypeSimpleObject = new SimpleObject()
		        .add("display", "Sibling/Sibling")
		        .add("description", "Relationship between brother/sister, brother/brother, and sister/sister")
		        .add("aIsToB", "Sibling")
		        .add("bIsToA", "Sibling")
		        .add("displayAIsToB", "Sibling side")
		        .add("displayBIsToA", "Sibling another")
		        .add("weight", 1);
		
		// This line should not throw exception.
		getResource().create(relationshipTypeSimpleObject, new RequestContext());
	}
	
}
