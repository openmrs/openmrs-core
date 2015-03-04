/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.hl7;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.test.Verifies;

/**
 * Tests methods on the {@link HL7Util} class
 */
public class HL7UtilTest {
	
	/**
	 * @see {@link HL7Util#parseHL7Timestamp(String)}
	 */
	@Test
	@SuppressWarnings("deprecation")
	@Verifies(value = "should not flub dst with 20091225123000", method = "parseHL7Timestamp(String)")
	public void parseHL7Timestamp_shouldNotFlubDstWith20091225123000() throws Exception {
		// set tz to be US/Indianapolis so this junit test works everywhere and always
		TimeZone originalTimeZone = TimeZone.getDefault();
		TimeZone.setDefault(TimeZone.getTimeZone("EST"));
		
		Date d = HL7Util.parseHL7Date("20091225003000");
		//		System.out.println("tz for a date not in dst: "
		//		        + new SimpleDateFormat("Z").format(new SimpleDateFormat("yyyyMMdd").parse("20091225")));
		//		System.out.println("tz for a date in dst: "
		//		        + new SimpleDateFormat("Z").format(new SimpleDateFormat("yyyyMMdd").parse("20090625")));
		Assert.assertEquals(25, d.getDate());
		
		// reset the timezone
		TimeZone.setDefault(originalTimeZone);
	}
	
	/**
	 * @see {@link HL7Util#parseHL7Timestamp(String)}
	 */
	@Test
	@Verifies(value = "should handle 197804110615-0200", method = "parseHL7Timestamp(String)")
	public void parseHL7Timestamp_shouldHandle197804110615dash0200() throws Exception {
		Date d = HL7Util.parseHL7Date("197804110615-0200");
		Assert.assertEquals(new Long("261130500000"), (Long) d.getTime());
	}
	
	/**
	 * @see {@link HL7Util#getTimeZoneOffset(String,Date)}
	 */
	@Test
	@Verifies(value = "should return timezone for givenDate and not the current date", method = "getTimeZoneOffset(String,Date)")
	public void getTimeZoneOffset_shouldReturnTimezoneForGivenDateAndNotTheCurrentDate() throws Exception {
		// set tz to be US/Indianapolis so this junit test works everywhere and always
		TimeZone originalTimeZone = TimeZone.getDefault();
		TimeZone.setDefault(TimeZone.getTimeZone("EST"));
		
		Assert.assertEquals("-0500", HL7Util.getTimeZoneOffset("197804110615", new SimpleDateFormat("yyyyMMdd")
		        .parse("20091225")));
		
		// reset the timezone
		TimeZone.setDefault(originalTimeZone);
	}
	
	/**
	 * @see {@link HL7Util#getTimeZoneOffset(String,Date)}
	 */
	@Test
	@Verifies(value = "should return timezone string if exists in given string", method = "getTimeZoneOffset(String,Date)")
	public void getTimeZoneOffset_shouldReturnTimezoneStringIfExistsInGivenString() throws Exception {
		Assert.assertEquals("+1100", HL7Util.getTimeZoneOffset("348934934934+1100", new Date()));
	}
	
	/**
	 * @see {@link HL7Util#parseHL7Time(String)}
	 */
	@Test
	@SuppressWarnings("deprecation")
	@Verifies(value = "should handle 0615", method = "parseHL7Time(String)")
	public void parseHL7Time_shouldHandle0615() throws Exception {
		// set tz to be a __non DST__ timezone so this junit test works everywhere and always
		TimeZone originalTimeZone = TimeZone.getDefault();
		TimeZone.setDefault(TimeZone.getTimeZone("EAT"));
		
		Date parsedDate = HL7Util.parseHL7Time("0615");
		Assert.assertEquals(6, parsedDate.getHours());
		Assert.assertEquals(15, parsedDate.getMinutes());
		
		// reset the timezone
		TimeZone.setDefault(originalTimeZone);
	}
	
}
