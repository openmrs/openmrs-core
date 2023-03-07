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

import java.util.Date;

import org.apache.commons.lang3.time.DateUtils;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Cohort;
import org.openmrs.CohortMembership;
import org.openmrs.api.CohortService;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.test.SameDatetimeMatcher;
import org.openmrs.module.webservices.rest.web.v1_0.RestTestConstants2_1;
import org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;

public class CohortMembershipResource2_1WebTest extends MainResourceControllerTest {
	
	public static final String COHORT_NAME = "A cohort";
	
	public Date startDate;
	
	public Date endDate;
	
	@Autowired
	private CohortService cohortService;
	
	@Before
	public void setUp() throws Exception {
		startDate = DateUtils.parseDate("2017-01-01", "yyyy-MM-dd");
		endDate = DateUtils.parseDate("2017-02-28 23:59:59", "yyyy-MM-dd HH:mm:ss");
		
		CohortMembership membership = new CohortMembership(6);
		membership.setUuid(RestTestConstants2_1.COHORT_MEMBERSHIP_UUID);
		membership.setStartDate(startDate);
		membership.setEndDate(endDate);
		
		Cohort cohort = new Cohort();
		cohort.addMembership(membership);
		cohort.addMembership(new CohortMembership(7));
		cohort.setName(COHORT_NAME);
		cohort.setDescription("description");
		cohort.setUuid(RestTestConstants2_1.COHORT_UUID);
		cohortService.saveCohort(cohort);
	}
	
	// there's a test for GET ALL in the superclass
	
	@Test
	public void shouldCreateNew() throws Exception {
		String uuidForPatient8 = "8adf539e-4b5a-47aa-80c0-ba1025c957fa";
		SimpleObject post = new SimpleObject().add("patientUuid", uuidForPatient8).add("startDate", "2017-02-01");
		MockHttpServletRequest request = newPostRequest(getURI(), post);
		SimpleObject result = deserialize(handle(request));
		assertThat((String) result.get("patientUuid"), is(uuidForPatient8));
		assertThat((String) result.get("startDate"), new SameDatetimeMatcher("2017-02-01"));
		assertNull(result.get("endDate"));
	}
	
	@Test
	public void shouldUpdate() throws Exception {
		SimpleObject post = new SimpleObject().add("endDate", "2017-02-01");
		MockHttpServletRequest request = newPostRequest(getURI() + "/" + getUuid(), post);
		SimpleObject result = deserialize(handle(request));
		assertThat((String) result.get("uuid"), is(RestTestConstants2_1.COHORT_MEMBERSHIP_UUID));
		assertThat((String) result.get("endDate"), new SameDatetimeMatcher("2017-02-01"));
	}
	
	@Test
	public void shouldVoid() throws Exception {
		MockHttpServletRequest request = newDeleteRequest(getURI() + "/" + getUuid());
		handle(request);
		assertTrue(cohortService.getCohortMembershipByUuid(RestTestConstants2_1.COHORT_MEMBERSHIP_UUID).getVoided());
	}
	
	@Test
	public void shouldPurge() throws Exception {
		MockHttpServletRequest request = newDeleteRequest(getURI() + "/" + getUuid());
		request.setParameter("purge", "true");
		handle(request);
		assertNull(cohortService.getCohortMembershipByUuid(RestTestConstants2_1.COHORT_MEMBERSHIP_UUID));
	}
	
	@Override
	public String getURI() {
		return "cohort/" + RestTestConstants2_1.COHORT_UUID + "/membership";
	}
	
	@Override
	public String getUuid() {
		return RestTestConstants2_1.COHORT_MEMBERSHIP_UUID;
	}
	
	@Override
	public long getAllCount() {
		return 2;
	}
}
