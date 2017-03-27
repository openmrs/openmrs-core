/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Contains methods to test behavior of DoubleRange methods
 */
public class DoubleRangeTest {
	
	/**
	 * @see DoubleRange#equals(Object)
	 */
	@Test
	public void equals_shouldReturnTrueForTheSameObjectTypeAndFalseForDifferentTypeObjects() {
		Double value1 = 0.0;
		Double value2 = 5.0;
		Double value3 = 6.0;
		DoubleRange obj = new DoubleRange(value1, value2);
		DoubleRange objTest1 = new DoubleRange(value1, value2);
		DoubleRange objTest2 = new DoubleRange(value1, value3);
		Object objTest3 = new Object();
		
		assertTrue(obj.equals(objTest1));
		assertFalse(obj.equals(objTest2));
		assertFalse(obj.equals(objTest3));
	}
	
	@Test
	public void DoubleRange_shouldReturnNullLowAndHighIfAccessorsAreNotCalled() {
		DoubleRange dr = new DoubleRange();
		assertNull(dr.getHigh());
		assertNull(dr.getLow());
	}
	
	@Test
	public void DoubleRange_shouldReturnInfiniteLowAndHighIfCalledWithNullParameters() {
		DoubleRange dr = new DoubleRange(null, null);
		assertEquals(Double.POSITIVE_INFINITY, dr.getHigh().doubleValue(), 0);
		assertEquals(Double.NEGATIVE_INFINITY, dr.getLow().doubleValue(), 0);
	}
	
	@Test
	public void getHigh_shouldReturnCorrectValueOfHighIfHighWasSetPreviously() {
		DoubleRange dr = new DoubleRange(0.0, 4.0);
		assertEquals(4.0, dr.getHigh().doubleValue(), 0);
	}
	
	@Test
	public void getHigh_shouldReturnPositiveInfinityIfHighWasNotSetPreviously() {
		DoubleRange dr = new DoubleRange(0.0, null);
		assertEquals(Double.POSITIVE_INFINITY, dr.getHigh().doubleValue(), 0);
	}
	
	@Test
	public void setHigh_shouldSetHighToPositiveInfinityOnNullParameter() {
		DoubleRange dr = new DoubleRange(0.0, 4.0);
		dr.setHigh(null);
		assertEquals(Double.POSITIVE_INFINITY, dr.getHigh().doubleValue(), 0);
	}
	
	@Test
	public void setHigh_shouldCauseHighToHaveTheSetValue() {
		DoubleRange dr = new DoubleRange(null, null);
		dr.setHigh(8.0);
		assertEquals(8.0, dr.getHigh().doubleValue(), 0);
	}
	
	@Test
	public void getLow_shouldReturnCorrectValueOfLowIfLowWasSetPreviously() {
		DoubleRange dr = new DoubleRange(0.0, 4.0);
		assertEquals(0.0, dr.getLow().doubleValue(), 0);
	}
	
	@Test
	public void getLow_shouldReturnNegativeInfinityIfLowWasNotSetPreviously() {
		DoubleRange dr = new DoubleRange(null, 0.0);
		assertEquals(Double.NEGATIVE_INFINITY, dr.getLow().doubleValue(), 0);
	}
	
	@Test
	public void setLow_shouldSetLowToNegativeInfinityOnNullParameter() {
		DoubleRange dr = new DoubleRange(0.0, 4.0);
		dr.setLow(null);
		assertEquals(Double.NEGATIVE_INFINITY, dr.getLow().doubleValue(), 0);
	}
	
	@Test
	public void setLow_shouldCauseLowToHaveTheSetValue() {
		DoubleRange dr = new DoubleRange(null, null);
		dr.setLow(8.0);
		assertEquals(8.0, dr.getLow().doubleValue(), 0);
	}
	
	/**
	 * @see DoubleRange#compareTo(DoubleRange)
	 */
	@Test
	public void compareTo_shouldReturnPlus1IfThisLowIsGreaterThanOtherLow() throws Exception {
		DoubleRange r1 = new DoubleRange(1.0, 2.0);
		DoubleRange r2 = new DoubleRange(0.0, 2.0);
		assertEquals(1, r1.compareTo(r2));
	}
	
	/**
	 * @see DoubleRange#compareTo(DoubleRange)
	 */
	@Test
	public void compareTo_shouldReturnMinusOneIfThisLowIsLowerThanOtherLow() throws Exception {
		DoubleRange r1 = new DoubleRange(1.0, 2.0);
		DoubleRange r2 = new DoubleRange(0.0, 2.0);
		assertEquals(-1, r2.compareTo(r1));
	}
	
	/**
	 * @see DoubleRange#compareTo(DoubleRange)
	 */
	@Test
	public void compareTo_shouldReturnZeroIfBothLowsAndBothHighsAreEqual() throws Exception {
		DoubleRange r1 = new DoubleRange(1.0, 2.0);
		DoubleRange r2 = new DoubleRange(1.0, 2.0);
		assertEquals(0, r1.compareTo(r2));
	}
	
	/**
	 * @see DoubleRange#compareTo(DoubleRange)
	 */
	@Test
	public void compareTo_shouldReturnPlusOneIfBothLowsAreEqualButOtherHighIsGreaterThanThisHigh() throws Exception {
		DoubleRange r1 = new DoubleRange(1.0, 1.0);
		DoubleRange r2 = new DoubleRange(1.0, 2.0);
		assertEquals(1, r1.compareTo(r2));
	}
	
	/**
	 * @see DoubleRange#compareTo(DoubleRange)
	 */
	@Test
	public void compareTo_shouldReturnMinusOneIfBothLowsAreEqualButOtherHighIsLessThanThisHigh() throws Exception {
		DoubleRange r1 = new DoubleRange(1.0, 2.0);
		DoubleRange r2 = new DoubleRange(1.0, 1.0);
		assertEquals(-1, r1.compareTo(r2));
	}
	
	/**
	 * @see DoubleRange#compareTo(DoubleRange)
	 */
	@Test
	public void compareTo_shouldReturn1IfThisRangeIsWiderThanOtherRange() throws Exception {
		DoubleRange r1 = new DoubleRange(0.0, 1.0);
		DoubleRange r2 = new DoubleRange(1.0, 3.0);
		assertEquals(1, r2.compareTo(r1));
	}
	
	/**
	 * @see DoubleRange#contains(double)
	 */
	@Test
	public void contains_shouldReturnTrueIfParameterIsInRange() throws Exception {
		DoubleRange r1 = new DoubleRange(0.0, 1.0);
		Double d = 0.5;
		assertTrue(r1.contains(d));
	}
	
	/**
	 * @see DoubleRange#contains(double)
	 */
	@Test
	public void contains_shouldReturnFalseIfParameterIsNotInRange() throws Exception {
		DoubleRange r1 = new DoubleRange(0.0, 1.0);
		Double d = 1.1;
		assertFalse(r1.contains(d));
	}
	
	/**
	 * @see DoubleRange#contains(double)
	 */
	@Test
	public void contains_shouldReturnFalseIfParameterIsEqualToHigh() throws Exception {
		DoubleRange r1 = new DoubleRange(0.0, 1.0);
		Double d = 1.0;
		assertFalse(r1.contains(d));
	}
	
	/**
	 * @see DoubleRange#contains(double)
	 */
	@Test
	public void contains_shouldReturnTrueIfParameterIsEqualToLow() throws Exception {
		DoubleRange r1 = new DoubleRange(0.0, 1.0);
		Double d = 0.0;
		assertTrue(r1.contains(d));
	}
	
	/**
	 * @see DoubleRange#contains(double)
	 */
	@Test
	public void contains_shouldReturnFalseIfParameterIsLowerThanLow() throws Exception {
		DoubleRange r1 = new DoubleRange(1.0, 1.0);
		Double d = 0.0;
		assertFalse(r1.contains(d));
	}
	
	/**
	 * @see DoubleRange#toString()
	 */
	@Test
	public void toString_shouldPrintTheRangeIfHighAndLowAreNotNullAndNotInfinite() throws Exception {
		DoubleRange r1 = new DoubleRange(1.0, 1.0);
		assertEquals(">= 1.0 and < 1.0", r1.toString());
	}
	
	/**
	 * @see DoubleRange#toString()
	 */
	@Test
	public void toString_shouldPrintEmptyLowIfLowIsInfinite() throws Exception {
		DoubleRange r1 = new DoubleRange(Double.NEGATIVE_INFINITY, 1.0);
		assertEquals("< 1.0", r1.toString());
	}
	
	/**
	 * @see DoubleRange#toString()
	 */
	@Test
	public void toString_shouldPrintEmptyStringIfLowAndHighAreInfinite() throws Exception {
		DoubleRange r1 = new DoubleRange(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
		assertEquals("", r1.toString());
	}
	
	/**
	 * @see DoubleRange#hashCode()
	 */
	@Test
	public void hashCode_shouldReturnTheSameHashCodeForObjectsRepresentingTheSameInterval() throws Exception {
		DoubleRange r1 = new DoubleRange(0.0, 1.0);
		DoubleRange r2 = new DoubleRange(0.0, 1.0);
		assertEquals(r1.hashCode(), r2.hashCode());
	}
	
	/**
	 * @see DoubleRange#toString()
	 */
	@Test
	public void toString_shouldPrintEmptyStringIfLowAndHighAreNull() throws Exception {
		DoubleRange r = new DoubleRange(null, null);
		assertEquals("", r.toString());
	}
	
	/**
	 * @see DoubleRange#toString()
	 */
	@Test
	public void toString_shouldPrintEmptyLowIfLowIsNull() throws Exception {
		DoubleRange r = new DoubleRange(null, 1.0);
		assertEquals("< 1.0", r.toString());
	}
}
