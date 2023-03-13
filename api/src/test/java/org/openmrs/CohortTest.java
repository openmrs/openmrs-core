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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

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
        Set<Integer> ids = new HashSet<>();
        for (int i=0; i<cohortSize; i++) {
            ids.add(i);
        }
        long startTime = System.currentTimeMillis();
        c.setMemberIds(ids);
        long endTime = System.currentTimeMillis();
        double secondsToSet = (endTime - startTime)/1000;
        assertTrue(secondsToSet < 5, "Setting cohort of size " + cohortSize + " took " + secondsToSet + " seconds");
    }
	
	
	@Test
	public void contains_shouldOnlyContainNonVoidedMembersAndIgnoreActiveStatus() {
		
		Cohort cohort = new Cohort("name", "description", ids);
		
		// create date in past to verify also non active patains are counted
		CohortMembership cohortMembershipOne = new CohortMembership(12, new Date());
		Date dateInPast = new GregorianCalendar(1992, Calendar.SEPTEMBER, 30).getTime();
		cohortMembershipOne.setEndDate(dateInPast);
		cohort.addMembership(cohortMembershipOne);
		
		Object[] allIds = ArrayUtils.add(ids, 12);
		
		Arrays.stream(allIds).forEach(id -> assertTrue(cohort.contains((Integer)id)));
		assertEquals(cohort.size(), allIds.length);
	}
	
	@Test
	public void contains_shouldOnlyContainNonVoidedMembers() {
		
		Cohort cohort = new Cohort("name", "description", ids);
		
		// create date in past to verify also non active patains are counted
		CohortMembership cohortMembershipOne = new CohortMembership(12, new Date());
		cohortMembershipOne.setVoided(true);
		cohort.addMembership(cohortMembershipOne);
		
		Arrays.stream(ids).forEach(id -> assertTrue(cohort.contains(id)));
		assertFalse(cohort.contains(12));
	}
	
	@Test
	public void size_shouldOnlyCountNonVoidedMembers() {
		
		Cohort cohort = new Cohort("name", "description", ids);
		
		// create a cohort that should not be counted
		CohortMembership cohortMembershipOne = new CohortMembership(12, new Date());
		cohortMembershipOne.setVoided(true);
		cohort.addMembership(cohortMembershipOne);
		
		assertEquals(cohort.size(),  ids.length);
	}
    
    @Test
	public void hasActiveMembership_shouldFindAnActiveMemberCorrectly() throws Exception{
	    
	    Calendar calendar = Calendar.getInstance();
	    calendar.add(Calendar.DAY_OF_YEAR, 1);
	    // endDateLater will be tomorrow
	    Date endDateLater =  calendar.getTime();
	    calendar.add(Calendar.DAY_OF_YEAR, -2);
	    // endDateEarlier will be yesterday
	    Date endDateEarlier = calendar.getTime();
	
	
	    Cohort cohort = new Cohort(3);
	    CohortMembership membershipOne = new CohortMembership(7);
	    membershipOne.setVoided(false);
	    membershipOne.setEndDate(endDateLater);
	    cohort.addMembership(membershipOne);
	
	    CohortMembership membershipTwo = new CohortMembership(8);
	    membershipTwo.setVoided(true);
	    cohort.addMembership(membershipTwo);
	
	    CohortMembership cohortMembershipThree = new CohortMembership(9);
	    cohortMembershipThree.setEndDate(endDateEarlier);
	    cohort.addMembership(cohortMembershipThree);
	
	    CohortMembership membershipFour = new CohortMembership(10);
	    cohort.addMembership(membershipFour);
	    
	    assertTrue(cohort.hasActiveMembership(7));
	    assertFalse(cohort.hasActiveMembership(9));
	    assertFalse(cohort.hasActiveMembership(8));
	    assertTrue(cohort.hasActiveMembership(10));
	
    }
	
	@Test
	public void activeMembershipSize_shouldDetermineSizeCorrectly() throws Exception{

		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DAY_OF_YEAR, 1);
		// endDateLater will be tomorrow		
		Date endDateLater =  calendar.getTime();
		calendar.add(Calendar.DAY_OF_YEAR, -2);
		// endDateEarlier will be yesterday
		Date endDateEarlier = calendar.getTime();
		
		Cohort cohort = new Cohort(3);
		CohortMembership temp = new CohortMembership(7);
		temp.setVoided(false);
		temp.setEndDate(endDateLater);
		cohort.addMembership(temp);
		
		temp = new CohortMembership(8);
		temp.setVoided(true);
		cohort.addMembership(temp);
		
		temp = new CohortMembership(9);
		temp.setEndDate(endDateEarlier);
		cohort.addMembership(temp);
		
		temp = new CohortMembership(10);
		temp.setVoided(false);
		cohort.addMembership(temp);
		
		assertEquals(2, cohort.activeMembershipSize() );
		
	}
	
	
	@Test
	public void hasNoActiveMemberships_shouldReturnTrueIfNoneExists() throws Exception{
		
		
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DAY_OF_YEAR, 1);
		// endDateLater will be tomorrow		
		Date endDateLater =  calendar.getTime();
		calendar.add(Calendar.DAY_OF_YEAR, -2);
		// endDateEarlier will be yesterday
		Date endDateEarlier = calendar.getTime();
		
		Cohort cohort = new Cohort(3);
		CohortMembership temp = new CohortMembership(7);
		temp.setVoided(true);
		temp.setEndDate(endDateLater);
		cohort.addMembership(temp);
		
		temp = new CohortMembership(8);
		temp.setVoided(true);
		cohort.addMembership(temp);
		
		temp = new CohortMembership(9);
		temp.setEndDate(endDateEarlier);
		cohort.addMembership(temp);
		
		temp = new CohortMembership(10);
		temp.setVoided(true);
		cohort.addMembership(temp);
		
		assertTrue(cohort.hasNoActiveMemberships());
	}
	
	@Test
	public void hasNoActiveMemberships_shouldReturnFalseIfOneExists() throws Exception{
		
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DAY_OF_YEAR, 1);
		// endDateLater will be tomorrow		
		Date endDateLater =  calendar.getTime();
		
		Cohort cohort = new Cohort(3);
		CohortMembership temp = new CohortMembership(7);
		temp.setVoided(false);
		temp.setEndDate(endDateLater);
		cohort.addMembership(temp);

		
		assertFalse(cohort.hasNoActiveMemberships());
		
	}
}
