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
import org.openmrs.api.context.Context;
import org.openmrs.hl7.HL7InQueue;
import org.openmrs.module.webservices.rest.web.RestTestConstants1_8;
import org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResourceTest;
import org.openmrs.module.webservices.rest.web.v1_0.wrapper.openmrs1_8.IncomingHl7Message1_8;

public class HL7MessageResource1_8Test extends BaseDelegatingResourceTest<HL7MessageResource1_8, IncomingHl7Message1_8> {
	
	@Before
	public void before() throws Exception {
		executeDataSet(RestTestConstants1_8.RESOURCE_TEST_DATASET);
	}
	
	@Override
	public IncomingHl7Message1_8 newObject() {
		HL7InQueue msg = new HL7InQueue();
		msg.setHL7Data("hl7Data");
		msg.setHL7SourceKey("sourceKey");
		msg.setHL7Source(Context.getHL7Service().getHL7SourceByName(RestTestConstants1_8.HL7_SOURCE_NAME));
		Context.getHL7Service().saveHL7InQueue(msg);
		return new IncomingHl7Message1_8(msg);
	}
	
	@Override
	public void validateDefaultRepresentation() throws Exception {
		super.validateDefaultRepresentation();
		assertPropEquals("messageState", getObject().getMessageState());
	}
	
	@Override
	public void validateFullRepresentation() throws Exception {
		super.validateFullRepresentation();
		assertPropPresent("source");
		assertPropEquals("sourceKey", getObject().getSourceKey());
		assertPropEquals("data", getObject().getData());
		assertPropEquals("messageState", getObject().getMessageState());
	}
	
	@Override
	public String getDisplayProperty() {
		return "sourceKey";
	}
	
	@Override
	public String getUuidProperty() {
		return getObject().getUuid();
	}
	
}
