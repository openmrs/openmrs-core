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

import org.junit.Before;
import org.openmrs.Encounter;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResourceTest;
import org.openmrs.module.webservices.rest.web.v1_0.RestTestConstants2_2;

public class EncounterResource2_2Test extends BaseDelegatingResourceTest<EncounterResource2_2, Encounter> {
	
	@Before
	public void before() throws Exception {
		executeDataSet(RestTestConstants2_2.DIAGNOSIS_TEST_DATA_XML);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResourceTest#newObject()
	 */
	@Override
	public Encounter newObject() {
		return Context.getEncounterService().getEncounterByUuid(getUuidProperty());
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResourceTest#validateDefaultRepresentation()
	 */
	@Override
	public void validateDefaultRepresentation() throws Exception {
		super.validateDefaultRepresentation();
		assertPropEquals("encounterDatetime", getObject().getEncounterDatetime());
		assertPropPresent("patient");
		assertPropPresent("location");
		assertPropPresent("form");
		assertPropPresent("encounterType");
		assertPropPresent("diagnoses");
		assertPropPresent("obs");
		assertPropPresent("orders");
		assertPropPresent("encounterProviders");
		assertPropEquals("voided", getObject().getVoided());
		assertPropPresent("visit");
		assertPropEquals("resourceVersion", "2.2");
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResourceTest#validateFullRepresentation()
	 */
	@Override
	public void validateFullRepresentation() throws Exception {
		super.validateFullRepresentation();
		assertPropEquals("encounterDatetime", getObject().getEncounterDatetime());
		assertPropPresent("patient");
		assertPropPresent("location");
		assertPropPresent("form");
		assertPropPresent("encounterType");
		assertPropPresent("encounterProviders");
		assertPropPresent("diagnoses");
		assertPropPresent("obs");
		assertPropPresent("orders");
		assertPropEquals("voided", getObject().getVoided());
		assertPropPresent("auditInfo");
		assertPropPresent("visit");
		assertPropEquals("resourceVersion", "2.2");
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResourceTest#getDisplayProperty()
	 */
	@Override
	public String getDisplayProperty() {
		return "Scheduled 12/08/2017";
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResourceTest#getUuidProperty()
	 */
	@Override
	public String getUuidProperty() {
		return "44444-fcdb-4a5b-97ea-0d5c4b4315a1";
	}
}
