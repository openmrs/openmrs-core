/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.migration;

import org.junit.Test;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Created by freddy on 20.05.18.
 */
public class MigrationHelperTest {
	private Calendar calendar;
	
	@Test
	public void parseDate_shouldReturnNullGivenNull() throws ParseException {
		String input = null;
		
		Date result = MigrationHelper.parseDate(input);
		
		assertNull(result);
	}
	
	@Test
	public void parseDate_shouldReturnNullEmptyString() throws ParseException {
		String input = "";
		
		Date result = MigrationHelper.parseDate(input);
		
		assertNull(result);
	}
	
	@Test
	public void parseDate_shouldReturnSimpleDateGivenYYYMMDD() throws ParseException {
		String input = "2001-07-04";
		calendar = new GregorianCalendar();
		
		Date result = MigrationHelper.parseDate(input);
		
		calendar.setTime(result);
		
		System.out.print(result);
		int dd = calendar.get(Calendar.DAY_OF_MONTH);
		int mm = calendar.get(Calendar.MONTH);
		int yy = calendar.get(Calendar.YEAR);
		
		int minute = calendar.get(Calendar.MINUTE);
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		
		assertEquals(dd, 4);
		assertEquals(mm, Calendar.JULY);
		assertEquals(yy, 2001);
		
		assertEquals(hour, 0);
		assertEquals(minute, 0);
		
	}
	
	@Test
	public void parseDate_shouldReturnSimpleDateGivenYYYMMDDHHMMSS() throws ParseException {
		String input = "2001-07-04 16:20:12";
		calendar = new GregorianCalendar();
		
		Date result = MigrationHelper.parseDate(input);
		
		calendar.setTime(result);
		
		System.out.print(result);
		int dd = calendar.get(Calendar.DAY_OF_MONTH);
		int mm = calendar.get(Calendar.MONTH);
		int yyyy = calendar.get(Calendar.YEAR);
		
		int second = calendar.get(Calendar.SECOND);
		int minute = calendar.get(Calendar.MINUTE);
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		
		assertEquals(dd, 4);
		assertEquals(mm, Calendar.JULY);
		assertEquals(yyyy, 2001);
		
		assertEquals(hour, 16);
		assertEquals(minute, 20);
		assertEquals(second, 12);
	}
	
	@Test(expected = ParseException.class)
	public void parseDate_shouldFailGivenYYYMMDDHHMM() throws ParseException {
		String input = "2001-07-04 16:20";
		
		Date result = MigrationHelper.parseDate(input);
		
		assertNull(result);		
		
	}
}
