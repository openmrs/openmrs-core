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

import org.junit.Test;
import org.openmrs.Person;
import org.openmrs.Relationship;
import org.openmrs.RelationshipType;
import org.openmrs.api.PersonService;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestTestConstants1_8;
import org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResourceTest;

import java.util.List;

import static org.junit.Assert.assertFalse;

/**
 * Contains tests for the
 * {@link org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_9.RelationshipResource1_9}
 */
public class RelationshipResource1_9Test extends BaseDelegatingResourceTest<RelationshipResource1_9, Relationship> {
	
	@Override
	public Relationship newObject() {
		return Context.getPersonService().getRelationshipByUuid(getUuidProperty());
	}
	
	@Override
	public void validateDefaultRepresentation() throws Exception {
		super.validateDefaultRepresentation();
		assertPropPresent("personA");
		assertPropPresent("relationshipType");
		assertPropPresent("personB");
		assertPropPresent("startDate");
		assertPropPresent("endDate");
		assertPropEquals("voided", getObject().isVoided());
		assertFalse("Should not expose the Patient subclass",
		    findSelfLink((SimpleObject) getRepresentation().get("personA")).contains("/patient/"));
		assertFalse("Should not expose the Patient subclass",
		    findSelfLink((SimpleObject) getRepresentation().get("personB")).contains("/patient/"));
	}
	
	@Override
	public void validateFullRepresentation() throws Exception {
		super.validateFullRepresentation();
		assertPropPresent("personA");
		assertPropPresent("relationshipType");
		assertPropPresent("personB");
		assertPropPresent("startDate");
		assertPropPresent("endDate");
		assertPropEquals("voided", getObject().isVoided());
		assertPropPresent("auditInfo");
		assertFalse("Should not expose the Patient subclass",
		    findSelfLink((SimpleObject) getRepresentation().get("personA")).contains("/patient/"));
		assertFalse("Should not expose the Patient subclass",
		    findSelfLink((SimpleObject) getRepresentation().get("personB")).contains("/patient/"));
	}
	
	@Override
	public String getDisplayProperty() {
		return "Hippocrates is the Doctor of Horatio";
	}
	
	@Override
	public String getUuidProperty() {
		return RestTestConstants1_8.RELATIONSHIP_UUID;
	}
	
	@Test
	public void createShouldIgnoreRelationshipTypeSpecificDisplayPropertiesWhenCreating() {
		// Ensuring that when the payload comes with displayAIsToB/displayBIsToA the resource should create resource without any issue.
		final String RELATIONSHIP_NAME = "Sibling";
		final String DESCRIPTION = "Relationship between brother/sister, brother/brother, and sister/sister";
		RelationshipType relationshipType = new RelationshipType();
		relationshipType.setaIsToB(RELATIONSHIP_NAME);
		relationshipType.setbIsToA(RELATIONSHIP_NAME);
		relationshipType.setDescription(DESCRIPTION);
		relationshipType.setWeight(1);
		
		PersonService personService = Context.getPersonService();
		relationshipType = personService.saveRelationshipType(relationshipType);
		
		// Convert the previously saved relationship_type into simple object.
		SimpleObject relationshipTypeSimpleObject = new SimpleObject()
		        .add("uuid", relationshipType.getUuid())
		        .add("display", "Sibling/Sibling")
		        .add("description", DESCRIPTION)
		        .add("aIsToB", RELATIONSHIP_NAME)
		        .add("bIsToA", RELATIONSHIP_NAME)
		        .add("displayAIsToB", RELATIONSHIP_NAME)
		        .add("displayBIsToA", RELATIONSHIP_NAME)
		        .add("weight", 1);
		List<Person> people = Context.getPersonService().getPeople("", false);
		assert people != null && people.size() >= 2;
		
		SimpleObject relationship = new SimpleObject()
		        .add("personA", people.get(0).getUuid())
		        .add("relationshipType", relationshipTypeSimpleObject)
		        .add("personB", people.get(1).getUuid());
		
		getResource().create(relationship, new RequestContext());
		
	}
}
