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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.junit.Test;

/**
 * This class tests all methods that are not getter or setters in the Order java object TODO: finish
 * this test class for Order
 * 
 * @see Order
 */
public class OrderTest {
	
	/**
	 * Tests the {@link Order#isDiscontinuedRightNow()} method TODO this should be split into many
	 * different tests
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldIsDiscontinued() throws Exception {
		DateFormat ymd = new SimpleDateFormat("yyyy-MM-dd");
		
		Order o = new Order();
		assertFalse("order without dates shouldn't be discontinued", o.isDiscontinued(ymd.parse("2007-10-26")));
		
		o.setStartDate(ymd.parse("2007-01-01"));
		assertFalse("shouldn't be discontinued before start date", o.isDiscontinued(ymd.parse("2006-10-26")));
		assertFalse("order without no end dates shouldn't be discontinued", o.isDiscontinued(ymd.parse("2007-10-26")));
		
		o.setAutoExpireDate(ymd.parse("2007-12-31"));
		assertFalse("shouldn't be discontinued before start date", o.isDiscontinued(ymd.parse("2006-10-26")));
		assertFalse("shouldn't be discontinued before autoExpireDate", o.isDiscontinued(ymd.parse("2007-10-26")));
		assertFalse("shouldn't be discontinued after autoExpireDate", o.isDiscontinued(ymd.parse("2008-10-26")));
		
		o.setDiscontinued(true);
		assertFalse("shouldn't be discontinued before start date", o.isDiscontinued(ymd.parse("2006-10-26")));
		assertTrue("should be discontinued since discontinuedDate is missing", o.isDiscontinued(ymd.parse("2007-10-26")));
		
		o.setDiscontinuedDate(ymd.parse("2007-11-01"));
		assertFalse("shouldn't be discontinued before start date", o.isDiscontinued(ymd.parse("2006-10-26")));
		assertFalse("shouldn't be discontinued before discontinuedDate", o.isDiscontinued(ymd.parse("2007-10-26")));
		assertTrue("should be discontinued after discontinuedDate", o.isDiscontinued(ymd.parse("2007-11-26")));
		
	}
	
	/**
	 * Tests the {@link Order#isCurrent()} method TODO this should be split into many different
	 * tests
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldIsCurrent() throws Exception {
		DateFormat ymd = new SimpleDateFormat("yyyy-MM-dd");
		
		Order o = new Order();
		assertTrue("startDate==null && no end date should always be current", o.isCurrent(ymd.parse("2007-10-26")));
		
		o.setStartDate(ymd.parse("2007-01-01"));
		assertFalse("shouldn't be current before startDate", o.isCurrent(ymd.parse("2006-10-26")));
		assertTrue("should be current after startDate", o.isCurrent(ymd.parse("2007-10-26")));
		
		o.setAutoExpireDate(ymd.parse("2007-12-31"));
		assertFalse("shouldn't be current before startDate", o.isCurrent(ymd.parse("2006-10-26")));
		assertTrue("should be current between startDate and autoExpireDate", o.isCurrent(ymd.parse("2007-10-26")));
		assertFalse("shouldn't be current after autoExpireDate", o.isCurrent(ymd.parse("2008-10-26")));
		
		o.setDiscontinued(true);
		assertFalse("shouldn't be current before startDate", o.isCurrent(ymd.parse("2006-10-26")));
		assertFalse("shouldn't be current if discontinued with no date", o.isCurrent(ymd.parse("2007-10-26")));
		assertFalse("shouldn't be current if discontinued with no date", o.isCurrent(ymd.parse("2008-10-26")));
		
		o.setDiscontinuedDate(ymd.parse("2007-11-01"));
		assertFalse("shouldn't be current before startDate", o.isCurrent(ymd.parse("2006-10-26")));
		assertTrue("should be current between startDate and discontinuedDate", o.isCurrent(ymd.parse("2007-10-26")));
		assertFalse("shouldn't be current after discontinuedDate", o.isCurrent(ymd.parse("2007-11-26")));
		
		o.setAutoExpireDate(null);
		o.setDiscontinuedDate(null);
		assertFalse("shouldn't be current before startDate", o.isCurrent(ymd.parse("2006-10-26")));
		assertFalse("shouldn't be current if discontinued with no date", o.isCurrent(ymd.parse("2007-10-26")));
		assertFalse("shouldn't be current if discontinued with no date", o.isCurrent(ymd.parse("2008-10-26")));
		
		o.setDiscontinuedDate(ymd.parse("2007-11-01"));
		assertFalse("shouldn't be current before startDate", o.isCurrent(ymd.parse("2006-10-26")));
		assertTrue("should be current between startDate and discontinuedDate", o.isCurrent(ymd.parse("2007-10-26")));
		assertFalse("shouldn't be current after discontinuedDate", o.isCurrent(ymd.parse("2007-11-26")));
	}
	
}
