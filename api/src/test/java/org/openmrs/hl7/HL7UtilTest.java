/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
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
