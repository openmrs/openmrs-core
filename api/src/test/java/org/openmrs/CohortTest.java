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

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.openmrs.test.BaseContextSensitiveTest;

/**
 * Behavior-driven tests of the Cohort class.
 */
public class CohortTest extends BaseContextSensitiveTest {
	
	private Integer[] ids = { 1, 2, 3 };
	
	@Test
	public void constructorWithIntegers_shouldAddMembersToCohort() {
		
		Cohort cohort = new Cohort("name", "description", ids);
		Arrays.stream(ids).forEach(id -> assertTrue(cohort.contains(id)));
		
	}
	
	@Test
	public void constructorWithPatients_shouldAddMembersToCohort() {
		
		List<Patient> patients = new ArrayList<Patient>();
		Arrays.stream(ids).forEach(id -> patients.add(new Patient(id)));
		
		Cohort cohort = new Cohort("name", "description", patients);
		Arrays.stream(ids).forEach(id -> assertTrue(cohort.contains(new Patient(id))));
		
	}
	
	@Test
	public void constructorWithCommaSeparatedIntegers_shouldAddMembersToCohort() {
		
		Cohort cohort = new Cohort("1,2,3");
		Arrays.stream(ids).forEach(id -> assertTrue(cohort.contains(id)));
		
	}

	@Test
	public void getCommaSeparatedPatientIds_shouldReturnCommaSeparatedListOfPatients() {
		
		List<Patient> patients = new ArrayList<Patient>();
		Arrays.stream(ids).forEach(id -> patients.add(new Patient(id)));
		
		Cohort cohort = new Cohort("name", "description", patients);
		
		String[] ids = StringUtils.split(cohort.getCommaSeparatedPatientIds(), ',');
		Arrays.stream(ids).forEach(id -> patients.contains(new Patient(Integer.valueOf(id))));
		
	}
	
}
