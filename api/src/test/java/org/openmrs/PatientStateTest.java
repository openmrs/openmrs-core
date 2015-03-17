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

import java.util.Date;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.test.Verifies;

public class PatientStateTest {
	
	private Date leftRange;
	
	private Date inRange;
	
	private Date rightRange;
	
	private Date rightOutOfRange;
	
	private Date leftOutOfRange;
	
	private String uuid2;
	
	private String uuid1;
	
	@Before
	public void before() {
		inRange = new Date();
		leftRange = new Date(inRange.getTime() - 10000);
		rightRange = new Date(inRange.getTime() + 10000);
		rightOutOfRange = new Date(rightRange.getTime() + 10000);
		leftOutOfRange = new Date(leftRange.getTime() - 10000);
	}
	
	/**
	 * @see PatientState#getActive(Date)
	 * @verifies return false if voided and date in range
	 */
	@Test
	public void getActive_shouldReturnFalseIfVoidedAndDateInRange() throws Exception {
		//given
		PatientState patientState = new PatientState();
		patientState.setStartDate(leftRange);
		patientState.setEndDate(rightRange);
		patientState.setVoided(true);
		
		//when
		boolean active = patientState.getActive(inRange);
		
		//then
		Assert.assertFalse(active);
	}
	
	/**
	 * @see PatientState#getActive(Date)
	 * @verifies return false if voided and date not in range
	 */
	@Test
	public void getActive_shouldReturnFalseIfVoidedAndDateNotInRange() throws Exception {
		//given
		PatientState patientState = new PatientState();
		patientState.setStartDate(leftRange);
		patientState.setEndDate(rightRange);
		patientState.setVoided(true);
		
		//when
		boolean active = patientState.getActive(rightOutOfRange);
		
		//then
		Assert.assertFalse(active);
	}
	
	/**
	 * @see PatientState#getActive(Date)
	 * @verifies return true if not voided and date in range
	 */
	@Test
	public void getActive_shouldReturnTrueIfNotVoidedAndDateInRange() throws Exception {
		//given
		PatientState patientState = new PatientState();
		patientState.setStartDate(leftRange);
		patientState.setEndDate(rightRange);
		patientState.setVoided(false);
		
		//when
		boolean active = patientState.getActive(inRange);
		
		//then
		Assert.assertTrue(active);
	}
	
	/**
	 * @see PatientState#getActive(Date)
	 * @verifies return true if not voided and date in range with null startDate
	 */
	@Test
	public void getActive_shouldReturnTrueIfNotVoidedAndDateInRangeWithNullStartDate() throws Exception {
		//given
		PatientState patientState = new PatientState();
		patientState.setStartDate(null);
		patientState.setEndDate(rightRange);
		patientState.setVoided(false);
		
		//when
		boolean active = patientState.getActive(inRange);
		
		//then
		Assert.assertTrue(active);
	}
	
	/**
	 * @see PatientState#getActive(Date)
	 * @verifies return true if not voided and date in range with null endDate
	 */
	@Test
	public void getActive_shouldReturnTrueIfNotVoidedAndDateInRangeWithNullEndDate() throws Exception {
		//given
		PatientState patientState = new PatientState();
		patientState.setStartDate(leftRange);
		patientState.setEndDate(null);
		patientState.setVoided(false);
		
		//when
		boolean active = patientState.getActive(inRange);
		
		//then
		Assert.assertTrue(active);
	}
	
	/**
	 * @see PatientState#getActive(Date)
	 * @verifies return true if not voided and both startDate and endDate nulled
	 */
	@Test
	public void getActive_shouldReturnTrueIfNotVoidedAndBothStartDateAndEndDateNulled() throws Exception {
		//given
		PatientState patientState = new PatientState();
		patientState.setStartDate(null);
		patientState.setEndDate(null);
		patientState.setVoided(false);
		
		//when
		boolean active = patientState.getActive(inRange);
		
		//then
		Assert.assertTrue(active);
	}
	
	/**
	 * @see PatientState#getActive(Date)
	 * @verifies compare with current date if date null
	 */
	@Test
	public void getActive_shouldCompareWithCurrentDateIfDateNull() throws Exception {
		//given
		PatientState patientState = new PatientState();
		patientState.setStartDate(leftRange);
		patientState.setEndDate(leftRange);
		patientState.setVoided(false);
		
		//when
		boolean active = patientState.getActive(null);
		
		//then
		Assert.assertFalse(active);
	}
	
	/**
	 * @see PatientState#getActive(Date)
	 * @verifies return false if not voided and date earlier than startDate
	 */
	@Test
	public void getActive_shouldReturnFalseIfNotVoidedAndDateEarlierThanStartDate() throws Exception {
		//given
		PatientState patientState = new PatientState();
		patientState.setStartDate(leftRange);
		patientState.setEndDate(rightRange);
		patientState.setVoided(false);
		
		//when
		boolean active = patientState.getActive(leftOutOfRange);
		
		//then
		Assert.assertFalse(active);
	}
	
	/**
	 * @see PatientState#getActive(Date)
	 * @verifies return false if not voided and date later than endDate
	 */
	@Test
	public void getActive_shouldReturnFalseIfNotVoidedAndDateLaterThanEndDate() throws Exception {
		//given
		PatientState patientState = new PatientState();
		patientState.setStartDate(leftRange);
		patientState.setEndDate(rightRange);
		patientState.setVoided(false);
		
		//when
		boolean active = patientState.getActive(rightOutOfRange);
		
		//then
		Assert.assertFalse(active);
	}
	
	/**
	 * @see PatientState#compareTo(PatientState)
	 * @verifies return positive if startDates equal and this endDate null
	 */
	@Test
	public void compareTo_shouldReturnPositiveIfStartDatesEqualAndThisEndDateNull() throws Exception {
		//given
		PatientState patientState = new PatientState();
		patientState.setStartDate(leftRange);
		patientState.setEndDate(null);
		patientState.setVoided(false);
		
		PatientState patientState2 = new PatientState();
		patientState2.setStartDate(leftRange);
		patientState2.setEndDate(rightRange);
		patientState2.setVoided(false);
		
		//when
		int result = patientState.compareTo(patientState2);
		
		//then
		Assert.assertTrue(result > 0);
	}
	
	/**
	 * @see PatientState#compareTo(PatientState)
	 * @verifies return negative if this startDate null
	 */
	@Test
	public void compareTo_shouldReturnNegativeIfThisStartDateNull() throws Exception {
		//given
		PatientState patientState = new PatientState();
		patientState.setStartDate(null);
		patientState.setEndDate(rightRange);
		patientState.setVoided(false);
		
		PatientState patientState2 = new PatientState();
		patientState2.setStartDate(leftRange);
		patientState2.setEndDate(rightRange);
		patientState2.setVoided(false);
		
		//when
		int result = patientState.compareTo(patientState2);
		
		//then
		Assert.assertTrue(result < 0);
	}
	
	/**
	 * @see PatientState#compareTo(PatientState)
	 */
	@Test
	@Verifies(value = "pass if two states have the same start date, end date and uuid", method = "compareTo(PatientState)")
	public void compareTo_shouldPassIfTwoStatesHaveTheSameStartDateEndDateAndUuid() throws Exception {
		
		PatientState patientState = new PatientState();
		patientState.setStartDate(leftRange);
		patientState.setEndDate(rightRange);
		patientState.setUuid(uuid1);
		patientState.setVoided(false);
		
		PatientState patientState2 = new PatientState();
		patientState2.setStartDate(leftRange);
		patientState2.setEndDate(rightRange);
		patientState2.setUuid(uuid1);
		patientState2.setVoided(false);
		
		Assert.assertTrue(patientState.compareTo(patientState2) == 0);
	}
	
	/**
	 * @see PatientState#compareTo(PatientState)
	 */
	@Test
	@Verifies(value = "return positive or negative if two states have the same start date and end date but different uuids", method = "compareTo(PatientState)")
	public void compareTo_shouldReturnPositiveOrNegativeIfTwoStatesHaveTheSameStartDatesEndDatesAndUuids() throws Exception {
		uuid1 = "some uuid 1";
		uuid2 = "some uuid 2";
		
		PatientState patientState = new PatientState();
		patientState.setStartDate(leftRange);
		patientState.setEndDate(rightRange);
		patientState.setUuid(uuid1);
		patientState.setVoided(false);
		
		PatientState patientState2 = new PatientState();
		patientState2.setStartDate(leftRange);
		patientState2.setEndDate(rightRange);
		patientState2.setUuid(uuid2);
		patientState2.setVoided(false);
		
		int result = (patientState.compareTo(patientState2));
		
		Assert.assertTrue(result <= -1 || result >= 1);
	}
}
