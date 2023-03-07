/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs2_1;

import org.junit.Before;
import org.openmrs.Cohort;
import org.openmrs.CohortMembership;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResourceTest;
import org.openmrs.module.webservices.rest.web.v1_0.RestTestConstants2_1;

public class CohortResource2_1Test extends BaseDelegatingResourceTest<CohortResource2_1, Cohort> {
	
	public static final String COHORT_NAME = "A cohort";
	
	@Before
	public void setUp() throws Exception {
		Cohort cohort = new Cohort();
		cohort.addMembership(new CohortMembership(6));
		cohort.addMembership(new CohortMembership(7));
		cohort.setName(COHORT_NAME);
		cohort.setDescription("description");
		cohort.setUuid(getUuidProperty());
		Context.getCohortService().saveCohort(cohort);
	}
	
	@Override
	public void validateDefaultRepresentation() throws Exception {
		super.validateDefaultRepresentation();
		assertPropEquals("name", getObject().getName());
		assertPropEquals("description", getObject().getDescription());
		assertPropEquals("voided", getObject().getVoided());
		assertPropEquals("size", getObject().size());
		assertPropNotPresent("memberIds");
	}
	
	@Override
	public Cohort newObject() {
		return Context.getCohortService().getCohortByUuid(getUuidProperty());
	}
	
	@Override
	public String getDisplayProperty() {
		return COHORT_NAME;
	}
	
	@Override
	public String getUuidProperty() {
		return RestTestConstants2_1.COHORT_UUID;
	}
	
}
