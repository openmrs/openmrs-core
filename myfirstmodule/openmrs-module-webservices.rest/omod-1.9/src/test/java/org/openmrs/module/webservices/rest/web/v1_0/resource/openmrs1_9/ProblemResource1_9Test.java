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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.activelist.Problem;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.RestTestConstants1_8;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResourceTest;

public class ProblemResource1_9Test extends BaseDelegatingResourceTest<ProblemResource1_9, Problem> {
	
	private static final String ACTIVE_LIST_INITIAL_XML = "customActiveListTest.xml";
	
	@Before
	public void init() throws Exception {
		executeDataSet(ACTIVE_LIST_INITIAL_XML);
	}
	
	@Override
	public Problem newObject() {
		Problem problem = Context.getPatientService().getProblem(2);
		problem.setUuid(getUuidProperty());
		return problem;
	}
	
	@Override
	public void validateDefaultRepresentation() throws Exception {
		super.validateDefaultRepresentation();
		assertPropPresent("person");
		assertPropPresent("problem");
		assertPropPresent("activeListType");
		assertPropEquals("modifier", getObject().getModifier());
		assertPropEquals("comments", getObject().getComments());
		assertPropEquals("sortWeight", getObject().getSortWeight());
		assertPropPresent("startDate");
		assertPropPresent("endDate");
		assertPropPresent("startObs");
		assertPropPresent("stopObs");
		assertPropEquals("voided", getObject().getVoided());
	}
	
	@Override
	public void validateFullRepresentation() throws Exception {
		super.validateFullRepresentation();
		assertPropPresent("person");
		assertPropPresent("problem");
		assertPropPresent("activeListType");
		assertPropEquals("modifier", getObject().getModifier());
		assertPropEquals("comments", getObject().getComments());
		assertPropEquals("sortWeight", getObject().getSortWeight());
		assertPropPresent("startDate");
		assertPropPresent("endDate");
		assertPropPresent("startObs");
		assertPropPresent("stopObs");
		assertPropEquals("voided", getObject().getVoided());
		assertPropPresent("auditInfo");
	}
	
	@Override
	public String getDisplayProperty() {
		return "FOOD ASSISTANCE";
	}
	
	@Override
	public String getUuidProperty() {
		return RestTestConstants1_8.PROBLEM_UUID;
	}
	
	@Test
	public void asRepresentation_shouldReturnProperlyEncodedValues() throws Exception {
		
		Problem problem = getObject();
		
		Concept concept = Context.getConceptService().getConceptByUuid("a09ab2c5-878e-4905-b25d-5784167d0216");
		problem.setProblem(concept);
		
		SimpleObject rep = getResource().asRepresentation(getObject(), Representation.DEFAULT);
		Assert.assertTrue(rep.keySet().contains("problem"));
		rep = (SimpleObject) rep.get("problem");
		Assert.assertEquals("problem", concept.getUuid(), rep.get("uuid"));
	}
}
