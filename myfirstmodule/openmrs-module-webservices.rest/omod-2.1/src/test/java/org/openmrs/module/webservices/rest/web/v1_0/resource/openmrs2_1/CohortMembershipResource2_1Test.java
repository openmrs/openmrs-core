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

import static org.junit.Assert.assertThat;
import static org.openmrs.module.webservices.rest.test.LinkMatcher.hasLink;

import java.util.Date;

import org.apache.commons.lang3.time.DateUtils;
import org.junit.Before;
import org.openmrs.Cohort;
import org.openmrs.CohortMembership;
import org.openmrs.api.CohortService;
import org.openmrs.api.PatientService;
import org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResourceTest;
import org.openmrs.module.webservices.rest.web.v1_0.RestTestConstants2_1;
import org.springframework.beans.factory.annotation.Autowired;

public class CohortMembershipResource2_1Test extends BaseDelegatingResourceTest<CohortMembershipResource2_1, CohortMembership> {
	
	public static final String COHORT_NAME = "A cohort";
	
	public Date startDate;
	
	public Date endDate;
	
	@Autowired
	private CohortService cohortService;
	
	@Autowired
	private PatientService patientService;
	
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
	
	@Override
	public void validateDefaultRepresentation() throws Exception {
		super.validateDefaultRepresentation();
		
		assertPropEquals("startDate", startDate);
		assertPropEquals("endDate", endDate);
		//		assertThat((String) result.get("startDate"), new SameDatetimeMatcher(startDate));
		//		assertThat((String) result.get("endDate"), new SameDatetimeMatcher(endDate));
		
		String patientUuid = patientService.getPatient(6).getUuid();
		assertPropEquals("patientUuid", patientUuid);
		assertThat(getRepresentation(), hasLink("patient", "/v1/patient/" + patientUuid));
	}
	
	@Override
	public CohortMembership newObject() {
		return cohortService.getCohortMembershipByUuid(RestTestConstants2_1.COHORT_MEMBERSHIP_UUID);
	}
	
	@Override
	public String getDisplayProperty() {
		// this is just hardcoded placeholder text like "Patient in cohort (see link with rel=patient)"
		return new CohortMembershipResource2_1().getDisplay(null);
	}
	
	@Override
	public String getUuidProperty() {
		return RestTestConstants2_1.COHORT_MEMBERSHIP_UUID;
	}
}
