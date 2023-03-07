/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs2_2;

import org.openmrs.ProgramAttributeType;
import org.openmrs.api.ProgramWorkflowService;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResourceTest;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;

import static org.junit.Assert.assertEquals;
import org.openmrs.module.webservices.rest.web.v1_0.RestTestConstants2_2;

public class ProgramAttributeTypeResource2_2Test extends BaseDelegatingResourceTest<ProgramAttributeTypeResource2_2, ProgramAttributeType> {

	@Before
	public void before() throws Exception {
		executeDataSet("programEnrollmentDataSet.xml");
	}

	@Override
	public ProgramAttributeType newObject() {
		return Context.getService(ProgramWorkflowService.class).getProgramAttributeTypeByUuid(getUuidProperty());
	}

	@Override
	public void validateDefaultRepresentation() throws Exception {
		super.validateDefaultRepresentation();

		assertPropEquals("name", getObject().getName());
		assertPropEquals("description", getObject().getDescription());
		assertPropEquals("datatypeClassname", getObject().getDatatypeClassname());
		assertPropEquals("preferredHandlerClassname", getObject().getPreferredHandlerClassname());
		assertPropEquals("retired", getObject().getRetired());
	}

	@Override
	public void validateFullRepresentation() throws Exception {
		super.validateFullRepresentation();
		assertPropEquals("name", getObject().getName());
		assertPropEquals("description", getObject().getDescription());
		assertPropEquals("minOccurs", getObject().getMinOccurs());
		assertPropEquals("maxOccurs", getObject().getMaxOccurs());
		assertPropEquals("datatypeClassname", getObject().getDatatypeClassname());
		assertPropEquals("datatypeConfig", getObject().getDatatypeConfig());
		assertPropEquals("preferredHandlerClassname", getObject().getPreferredHandlerClassname());
		assertPropEquals("handlerConfig", getObject().getHandlerConfig());
		assertPropEquals("retired", getObject().getRetired());
		assertPropPresent("auditInfo");
	}

	@Override
	public void validateRefRepresentation() throws Exception {
		assertPropEquals("uuid", getObject().getUuid());
		assertPropEquals("description", getObject().getDescription());
		assertPropEquals("display", getObject().getName());
		assertPropEquals("retired", getObject().getRetired());
		assertPropNotPresent("datatypeClassname");
	}

	@Test
	public void ensureGetAllReturnsAllTheAttributes() {
		RequestContext context = new RequestContext();
		context.setLimit(100);
		context.setStartIndex(0);
		NeedsPaging<ProgramAttributeType> programAttributeTypes = getResource().doGetAll(context);
		assertEquals(3, programAttributeTypes.getPageOfResults().size());
		assertEquals("d7477c21-bfc3-4922-9591-e89d8b9c8efb", programAttributeTypes.getPageOfResults().get(0).getUuid());
		assertEquals("d7477c21-bfc3-4922-9591-e89d8b9c8efe", programAttributeTypes.getPageOfResults().get(1).getUuid());
		assertEquals("d7477c21-bfc3-4922-9591-e89d8b9c8efh", programAttributeTypes.getPageOfResults().get(2).getUuid());
	}

	@Override
	public String getDisplayProperty() {
		return "stage";
	}

	@Override
	public String getUuidProperty() {
		return RestTestConstants2_2.PROGRAM_ATTRIBUTE_TYPE_UUID;
	}

	@Test
	public void ensureGetConceptReturnsTheConceptAttribute() throws Exception {
		ProgramAttributeType programAttributeType = Context.getService(ProgramWorkflowService.class).getProgramAttributeTypeByUuid("d7477c21-bfc3-4922-9591-e89d8b9c8efh");;
		SimpleObject concept = (SimpleObject) getResource().getConcept(programAttributeType);
		assertEquals("d102c80f-1yz9-4da3-bb88-8122ce8868dd", concept.get("uuid"));
	}
}
