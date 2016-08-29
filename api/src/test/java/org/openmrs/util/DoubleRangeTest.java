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

import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Test;
import org.openmrs.test.Verifies;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.TestCase.assertNull;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Contains methods to test behavior of DoubleRange methods
 */
public class DoubleRangeTest {

        /**
         * @see DoubleRange#equals(Object)
         */
        @Test
        @Verifies(value = "should return true for the same object type and false for different objects", method = "equals(Object o)")
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
        @Verifies(value = "return null low and high if accessors are not called", method = "DoubleRange ()")
        public void DoubleRange_shouldReturnNullLowAndHighIfAccessorsAreNotCalled() {
                DoubleRange dr = new DoubleRange();
                assertNull(dr.getHigh());
                assertNull(dr.getLow());
        }

        @Test
        @Verifies(value = "return infinite low and high if called with null parameters", method = "DoubleRange(Double low, Double high)")
        public void DoubleRange_shouldReturnInfiniteLowAndHighIfCalledWithNullParameters() {
                DoubleRange dr = new DoubleRange(null, null);
                assertEquals(Double.POSITIVE_INFINITY, dr.getHigh());
                assertEquals(Double.NEGATIVE_INFINITY, dr.getLow());
        }

        @Test
        @Verifies(value = "return correct value of high if high was set previously", method = "getHigh()")
        public void getHigh_shouldReturnCorrectValueOfHighIfHighWasSetPreviously() {
                DoubleRange dr = new DoubleRange(0.0, 4.0);
                assertEquals(4.0, dr.getHigh());

        }

        @Test
        @Verifies(value = "return positive infinity if high was not set previously", method = "getHigh()")
        public void getHigh_shouldReturnPositiveInfinityIfHighWasNotSetPreviously() {
                DoubleRange dr = new DoubleRange(0.0, null);
                assertEquals(Double.POSITIVE_INFINITY, dr.getHigh());
        }

        @Test
        @Verifies(value = "set high to positive infinity on null parameter", method = "setHigh(Double high)")
        public void setHigh_shouldSetHighToPositiveInfinityOnNullParameter() {
                DoubleRange dr = new DoubleRange(0.0, 4.0);
                dr.setHigh(null);
                assertEquals(Double.POSITIVE_INFINITY, dr.getHigh());
        }

        @Test
        @Verifies(value = "cause high to have the set value", method = "setHigh(Double high)")
        public void setHigh_shouldCauseHighToHaveTheSetValue() {
                DoubleRange dr = new DoubleRange(null, null);
                dr.setHigh(8.0);
                assertEquals(8.0, dr.getHigh());
        }

        @Test
        @Verifies(value = "return correct value of low if low was set previously", method = "getLow()")
        public void getLow_shouldReturnCorrectValueOfLowIfLowWasSetPreviously() {
                DoubleRange dr = new DoubleRange(0.0, 4.0);
                assertEquals(0.0, dr.getLow());

        }

        @Test
        @Verifies(value = "return negative infinity if low was not set previously", method = "getLow()")
        public void getLow_shouldReturnNegativeInfinityIfLowWasNotSetPreviously() {
                DoubleRange dr = new DoubleRange(null, 0.0);
                assertEquals(Double.NEGATIVE_INFINITY, dr.getLow());
        }

        @Test
        @Verifies(value = "set low to negative infinity on null parameter", method = "setLow(Double low)")
        public void setLow_shouldSetLowToNegativeInfinityOnNullParameter() {
                DoubleRange dr = new DoubleRange(0.0, 4.0);
                dr.setLow(null);
                assertEquals(Double.NEGATIVE_INFINITY, dr.getLow());
        }

        @Test
        @Verifies(value = "cause low to have the set value", method = "setLow(Double low)")
        public void setLow_shouldCauseLowToHaveTheSetValue() {
                DoubleRange dr = new DoubleRange(null, null);
                dr.setLow(8.0);
                assertEquals(8.0, dr.getLow());
        }

        /**
         * @verifies return plus 1 if this low is greater than other low
         * @see DoubleRange#compareTo(DoubleRange)
         */
        @Test
        public void compareTo_shouldReturnPlus1IfThisLowIsGreaterThanOtherLow() throws Exception {
                DoubleRange r1 = new DoubleRange(1.0, 2.0);
                DoubleRange r2 = new DoubleRange(0.0, 2.0);
                assertEquals(1, r1.compareTo(r2));
        }

        /**
         * @verifies return minus one if this low is lower than other low
         * @see DoubleRange#compareTo(DoubleRange)
         */
        @Test
        public void compareTo_shouldReturnMinusOneIfThisLowIsLowerThanOtherLow() throws Exception {
                DoubleRange r1 = new DoubleRange(1.0, 2.0);
                DoubleRange r2 = new DoubleRange(0.0, 2.0);
                assertEquals(-1, r2.compareTo(r1));
        }

        /**
         * @verifies return zero if both lows and both highs are equal
         * @see DoubleRange#compareTo(DoubleRange)
         */
        @Test
        public void compareTo_shouldReturnZeroIfBothLowsAndBothHighsAreEqual() throws Exception {
                DoubleRange r1 = new DoubleRange(1.0, 2.0);
                DoubleRange r2 = new DoubleRange(1.0, 2.0);
                assertEquals(0, r1.compareTo(r2));
        }

        /**
         * @verifies return plus one if both lows are equal but other high is greater than this high
         * @see DoubleRange#compareTo(DoubleRange)
         */
        @Test
        public void compareTo_shouldReturnPlusOneIfBothLowsAreEqualButOtherHighIsGreaterThanThisHigh() throws Exception {
                DoubleRange r1 = new DoubleRange(1.0, 1.0);
                DoubleRange r2 = new DoubleRange(1.0, 2.0);
                assertEquals(1, r1.compareTo(r2));
        }

        /**
         * @verifies return minus one if both lows are equal but other high is less than this high
         * @see DoubleRange#compareTo(DoubleRange)
         */
        @Test
        public void compareTo_shouldReturnMinusOneIfBothLowsAreEqualButOtherHighIsLessThanThisHigh() throws Exception {
                DoubleRange r1 = new DoubleRange(1.0, 2.0);
                DoubleRange r2 = new DoubleRange(1.0, 1.0);
                assertEquals(-1, r1.compareTo(r2));
        }

        /**
         * @verifies return 1 if this range is wider than other range
         * @see DoubleRange#compareTo(DoubleRange)
         */
        @Test
        public void compareTo_shouldReturn1IfThisRangeIsWiderThanOtherRange() throws Exception {
                DoubleRange r1 = new DoubleRange(0.0, 1.0);
                DoubleRange r2 = new DoubleRange(1.0, 3.0);
                assertEquals(1, r2.compareTo(r1));
        }

        /**
         * @verifies return true if parameter is in range
         * @see DoubleRange#contains(double)
         */
        @Test
        public void contains_shouldReturnTrueIfParameterIsInRange() throws Exception {
                DoubleRange r1 = new DoubleRange(0.0, 1.0);
                Double d = 0.5;
                assertTrue(r1.contains(d));
        }

        /**
         * @verifies return false if parameter is not in range
         * @see DoubleRange#contains(double)
         */
        @Test
        public void contains_shouldReturnFalseIfParameterIsNotInRange() throws Exception {
                DoubleRange r1 = new DoubleRange(0.0, 1.0);
                Double d = 1.1;
                assertFalse(r1.contains(d));
        }

        /**
         * @verifies return false if parameter is equal to high
         * @see DoubleRange#contains(double)
         */
        @Test
        public void contains_shouldReturnFalseIfParameterIsEqualToHigh() throws Exception {
                DoubleRange r1 = new DoubleRange(0.0, 1.0);
                Double d = 1.0;
                assertFalse(r1.contains(d));
        }

         /**
         * @verifies return true if parameter is equal to low
         * @see DoubleRange#contains(double)
         */
        @Test
        public void contains_shouldReturnTrueIfParameterIsEqualToLow() throws Exception {
                DoubleRange r1 = new DoubleRange(0.0, 1.0);
                Double d = 0.0;
                assertTrue(r1.contains(d));
        }


         /**
         * @verifies return false if parameter is lower than low
         * @see DoubleRange#contains(double)
         */
        @Test
        public void contains_shouldReturnFalseIfParameterIsLowerThanLow() throws Exception {
                DoubleRange r1 = new DoubleRange(1.0, 1.0);
                Double d = 0.0;
                assertFalse(r1.contains(d));
        }

        /**
         * @verifies print the range if high and low are not null and not infinite
         * @see DoubleRange#toString()
         */
        @Test
        public void toString_shouldPrintTheRangeIfHighAndLowAreNotNullAndNotInfinite() throws Exception {
                DoubleRange r1 = new DoubleRange(1.0, 1.0);
                assertEquals(">= 1.0 and < 1.0", r1.toString());
        }

        /**
         * @verifies print empty low if low is infinite
         * @see DoubleRange#toString()
         */
        @Test
        public void toString_shouldPrintEmptyLowIfLowIsInfinite() throws Exception {
                DoubleRange r1 = new DoubleRange(Double.NEGATIVE_INFINITY, 1.0);
                assertEquals("< 1.0", r1.toString());
        }

        /**
         * @verifies print empty string if low and high are infinite
         * @see DoubleRange#toString()
         */
        @Test
        public void toString_shouldPrintEmptyStringIfLowAndHighAreInfinite() throws Exception {
                DoubleRange r1 = new DoubleRange(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
                assertEquals("", r1.toString());
        }

        /**
         * @verifies return the same hashCode for objects representing the same interval
         * @see DoubleRange#hashCode()
         */
        @Test
        public void hashCode_shouldReturnTheSameHashCodeForObjectsRepresentingTheSameInterval() throws Exception {
                DoubleRange r1 = new DoubleRange(0.0, 1.0);
                DoubleRange r2 = new DoubleRange(0.0, 1.0);
                assertEquals(r1.hashCode(), r2.hashCode());
        }

        /**
         * @verifies print empty string if low and high are null
         * @see DoubleRange#toString()
         */
        @Test
        public void toString_shouldPrintEmptyStringIfLowAndHighAreNull() throws Exception {
                DoubleRange r = new DoubleRange(null, null);
                assertEquals("", r.toString());
        }

        /**
         * @verifies print empty low if low is null
         * @see DoubleRange#toString()
         */
        @Test
        public void toString_shouldPrintEmptyLowIfLowIsNull() throws Exception {
                DoubleRange r = new DoubleRange(null, 1.0);
                assertEquals("< 1.0", r.toString());
        }
}
