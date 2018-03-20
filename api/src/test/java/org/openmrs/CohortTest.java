/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertTrue;

/**
 * Behavior-driven tests of the Cohort class.
 */
public class CohortTest {
	
	private Integer[] ids = { 1, 2, 3 };
	
	@Test
	public void constructorWithIntegers_shouldAddMembersToCohort() {
		
		Cohort cohort = new Cohort("name", "description", ids);
		Arrays.stream(ids).forEach(id -> assertTrue(cohort.contains(id)));
		
	}
	
	@Test
	public void constructorWithPatients_shouldAddMembersToCohort() {
		
		List<Patient> patients = new ArrayList<>();
		Arrays.stream(ids).forEach(id -> patients.add(new Patient(id)));
		
		Cohort cohort = new Cohort("name", "description", patients);
		Arrays.stream(ids).forEach(id -> assertTrue(cohort.contains(id)));
		
	}
	
	@Test
	public void constructorWithCommaSeparatedIntegers_shouldAddMembersToCohort() {
		
		Cohort cohort = new Cohort("1,2,3");
		Arrays.stream(ids).forEach(id -> assertTrue(cohort.contains(id)));
		
	}

	@Test
	public void getCommaSeparatedPatientIds_shouldReturnCommaSeparatedListOfPatients() {
		
		List<Patient> patients = new ArrayList<>();
		Arrays.stream(ids).forEach(id -> patients.add(new Patient(id)));
		
		Cohort cohort = new Cohort("name", "description", patients);
		
		String[] ids = StringUtils.split(cohort.getCommaSeparatedPatientIds(), ',');
		Arrays.stream(ids).forEach(id -> patients.contains(new Patient(Integer.valueOf(id))));
		
	}

	@Test
	public void union_shouldContainVoidedAndExpiredMemberships() throws Exception {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date startDate = dateFormat.parse("2017-01-01 00:00:00");
		Date endDate = dateFormat.parse("2017-02-01 00:00:00");

		Cohort cohortOne = new Cohort(3);
		CohortMembership membershipOne = new CohortMembership(7, startDate);
		membershipOne.setVoided(true);
		membershipOne.setEndDate(endDate);
		cohortOne.addMembership(membershipOne);

		Cohort cohortTwo = new Cohort(4);
		CohortMembership membershipTwo = new CohortMembership(8, startDate);
		membershipTwo.setVoided(true);
		membershipTwo.setEndDate(endDate);
		cohortTwo.addMembership(membershipTwo);

		Cohort cohortUnion = Cohort.union(cohortOne, cohortTwo);
		Collection<CohortMembership> unionOfMemberships = cohortUnion.getMemberships();
		unionOfMemberships.forEach(m -> {
			assertTrue(m.getPatientId().equals(7) || m.getPatientId().equals(8));
			assertTrue(m.getVoided() && m.getEndDate() != null);
		});
	}

	@Test
	public void subtract_shouldContainVoidedAndExpiredMemberships() throws Exception {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date startDate = dateFormat.parse("2017-01-01 00:00:00");
		Date endDate = dateFormat.parse("2017-02-01 00:00:00");

		Cohort cohortOne = new Cohort(3);
		CohortMembership membershipOne = new CohortMembership(7, startDate);
		membershipOne.setVoided(true);
		membershipOne.setEndDate(endDate);
		cohortOne.addMembership(membershipOne);

		Cohort cohortTwo = new Cohort(4);
		CohortMembership membershipTwo = new CohortMembership(8, startDate);
		membershipTwo.setVoided(true);
		membershipTwo.setEndDate(endDate);
		cohortTwo.addMembership(membershipTwo);

		Cohort cohortSubtract = Cohort.subtract(cohortOne, cohortTwo);
		Collection<CohortMembership> subtractOfMemberships = cohortSubtract.getMemberships();
		subtractOfMemberships.forEach(m -> {
			assertTrue(m.getPatientId().equals(7));
			assertTrue(m.getVoided() && m.getEndDate() != null);
		});
	}

	@Test
	public void intersect_shouldContainVoidedAndExpiredMemberships() throws Exception {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date startDate = dateFormat.parse("2017-01-01 00:00:00");
		Date endDate = dateFormat.parse("2017-02-01 00:00:00");

		Cohort cohortOne = new Cohort(3);
		CohortMembership membershipOne = new CohortMembership(7, startDate);
		membershipOne.setVoided(true);
		membershipOne.setEndDate(endDate);
		cohortOne.addMembership(membershipOne);

		Cohort cohortTwo = new Cohort(4);
		CohortMembership membershipTwo = new CohortMembership(8, startDate);
		membershipTwo.setVoided(true);
		membershipTwo.setEndDate(endDate);
		cohortTwo.addMembership(membershipOne);
		cohortTwo.addMembership(membershipTwo);

		Cohort cohortIntersect = Cohort.intersect(cohortOne, cohortTwo);
		Collection<CohortMembership> intersectOfMemberships = cohortIntersect.getMemberships();
		assertTrue(intersectOfMemberships.stream().anyMatch(m -> m.getVoided() || m.getEndDate() != null));
		intersectOfMemberships.forEach(m -> {
			assertTrue(m.getPatientId().equals(7));
			assertTrue(m.getVoided() && m.getEndDate() != null);
		});
	}

    @Test
    public void setMemberIds_shouldSupportLargeCohorts() {
	    int cohortSize = 100000;
        Cohort c = new Cohort();
        Set<Integer> ids = new HashSet<Integer>();
        for (int i=0; i<cohortSize; i++) {
            ids.add(i);
        }
        long startTime = System.currentTimeMillis();
        c.setMemberIds(ids);
        long endTime = System.currentTimeMillis();
        double secondsToSet = (endTime - startTime)/1000;
        Assert.assertTrue("Setting cohort of size " + cohortSize + " took " + secondsToSet + " seconds", secondsToSet < 5);
    }
}
