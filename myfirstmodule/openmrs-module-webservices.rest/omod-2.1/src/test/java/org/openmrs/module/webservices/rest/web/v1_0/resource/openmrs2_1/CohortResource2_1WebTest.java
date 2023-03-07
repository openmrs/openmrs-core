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

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.Cohort;
import org.openmrs.CohortMembership;
import org.openmrs.api.CohortService;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.v1_0.RestTestConstants2_1;
import org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;

public class CohortResource2_1WebTest extends MainResourceControllerTest {
	
	public static final String COHORT_NAME = "A cohort";
	
	@Autowired
	private CohortService cohortService;
	
	@Before
	public void setUp() throws Exception {
		Cohort cohort = new Cohort();
		cohort.addMembership(new CohortMembership(6));
		cohort.addMembership(new CohortMembership(7));
		cohort.setName(COHORT_NAME);
		cohort.setDescription("description");
		cohort.setUuid(RestTestConstants2_1.COHORT_UUID);
		cohortService.saveCohort(cohort);
	}
	
	@Test
	public void shouldVoid() throws Exception {
		MockHttpServletRequest request = newDeleteRequest(getURI() + "/" + getUuid());
		handle(request);
		assertTrue(cohortService.getCohortByUuid(RestTestConstants2_1.COHORT_UUID).getVoided());
	}
	
	@Test
	public void shouldPurge() throws Exception {
		MockHttpServletRequest request = newDeleteRequest(getURI() + "/" + getUuid());
		request.setParameter("purge", "true");
		handle(request);
		assertNull(cohortService.getCohortByUuid(RestTestConstants2_1.COHORT_UUID));
	}
	
	@Test
	public void shouldEdit() throws Exception {
		String newDescription = "New description";
		MockHttpServletRequest request = newPostRequest(getURI() + "/" + getUuid(),
		    new SimpleObject().add("description", newDescription));
		SimpleObject result = deserialize(handle(request));
		assertThat((String) result.get("description"), is(newDescription));
		assertThat(cohortService.getCohortByUuid(RestTestConstants2_1.COHORT_UUID).getDescription(), is(newDescription));
	}
	
	@Override
	public String getURI() {
		return "cohort";
	}
	
	@Override
	public String getUuid() {
		return RestTestConstants2_1.COHORT_UUID;
	}
	
	@Override
	public long getAllCount() {
		return 1;
	}
	
}
