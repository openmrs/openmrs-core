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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import ca.uhn.hl7v2.HL7Exception;
import org.junit.jupiter.api.Test;

/**
 * Tests methods on the {@link HL7Util} class
 */
public class HL7UtilTest {
	
	/**
	 * @throws HL7Exception
	 * @see HL7Util#parseHL7Timestamp(String)
	 */
	@Test
	@SuppressWarnings("deprecation")
	public void parseHL7Timestamp_shouldNotFlubDstWith20091225123000() throws HL7Exception {
		// set tz to be US/Indianapolis so this junit test works everywhere and always
		TimeZone originalTimeZone = TimeZone.getDefault();
		TimeZone.setDefault(TimeZone.getTimeZone("EST"));
		
		Date d = HL7Util.parseHL7Date("20091225003000");
		assertEquals(25, d.getDate());
		
		// reset the timezone
		TimeZone.setDefault(originalTimeZone);
	}
	
	/**
	 * @throws HL7Exception
	 * @see HL7Util#parseHL7Timestamp(String)
	 */
	@Test
	public void parseHL7Timestamp_shouldHandle197804110615dash0200() throws HL7Exception {
		Date d = HL7Util.parseHL7Date("197804110615-0200");
		assertEquals(new Long("261130500000"), (Long) d.getTime());
	}
	
	/**
	 * @throws ParseException
	 * @see HL7Util#getTimeZoneOffset(String,Date)
	 */
	@Test
	public void getTimeZoneOffset_shouldReturnTimezoneForGivenDateAndNotTheCurrentDate() throws ParseException {
		// set tz to be US/Indianapolis so this junit test works everywhere and always
		TimeZone originalTimeZone = TimeZone.getDefault();
		TimeZone.setDefault(TimeZone.getTimeZone("GMT-05:00"));
		
		assertEquals("-0500", HL7Util.getTimeZoneOffset("197804110615", new SimpleDateFormat("yyyyMMdd")
		        .parse("20091225")));
		
		// reset the timezone
		TimeZone.setDefault(originalTimeZone);
	}
	
	/**
	 * @see HL7Util#getTimeZoneOffset(String,Date)
	 */
	@Test
	public void getTimeZoneOffset_shouldReturnTimezoneStringIfExistsInGivenString() {
		assertEquals("+1100", HL7Util.getTimeZoneOffset("348934934934+1100", new Date()));
	}
	
	/**
	 * @throws HL7Exception
	 * @see HL7Util#parseHL7Time(String)
	 */
	@Test
	@SuppressWarnings("deprecation")
	public void parseHL7Time_shouldHandle0615() throws HL7Exception {
		// set tz to be a __non DST__ timezone so this junit test works everywhere and always
		TimeZone originalTimeZone = TimeZone.getDefault();
		TimeZone.setDefault(TimeZone.getTimeZone("EAT"));
		
		Date parsedDate = HL7Util.parseHL7Time("0615");
		assertEquals(6, parsedDate.getHours());
		assertEquals(15, parsedDate.getMinutes());
		
		// reset the timezone
		TimeZone.setDefault(originalTimeZone);
	}
	
}
