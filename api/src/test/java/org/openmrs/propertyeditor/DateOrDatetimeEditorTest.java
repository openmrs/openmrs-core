/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.propertyeditor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DateOrDatetimeEditorTest {
	
	DateOrDatetimeEditor ed;
	
	DateFormat ymdhm = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	
	@BeforeEach
	public void before() {
		ed = new DateOrDatetimeEditor();
	}
	
	/**
	 * @throws ParseException
	 * @see DateOrDatetimeEditor#getAsText()
	 */
	@Test
	public void getAsText_shouldPrintDateWithoutTime() throws ParseException {
		ed.setValue(ymdhm.parse("2011-10-27 00:00"));
		assertEquals("27/10/2011", ed.getAsText());
	}
	
	/**
	 * @throws ParseException
	 * @see DateOrDatetimeEditor#getAsText()
	 */
	@Test
	public void getAsText_shouldPrintDateAndTimeWithTime() throws ParseException {
		ed.setValue(ymdhm.parse("2011-10-27 17:59"));
		assertEquals("27/10/2011 17:59", ed.getAsText());
	}
	
	/**
	 * @throws ParseException
	 * @see DateOrDatetimeEditor#setAsText(String)
	 */
	@Test
	public void setAsText_shouldHandleDate() throws ParseException {
		ed.setAsText("27/10/2011");
		assertEquals(ymdhm.parse("2011-10-27 00:00"), ed.getValue());
	}
	
	/**
	 * @throws ParseException
	 * @see DateOrDatetimeEditor#setAsText(String)
	 */
	@Test
	public void setAsText_shouldHandleDateAndTime() throws ParseException {
		ed.setAsText("27/10/2011 17:59");
		assertEquals(ymdhm.parse("2011-10-27 17:59"), ed.getValue());
	}
	
	/**
	 * @see DateOrDatetimeEditor#setAsText(String)
	 */
	@Test
	public void setAsText_shouldFailOnPartialDate() {
		assertThrows(IllegalArgumentException.class, () -> ed.setAsText("27/10"));
	}
	
	/**
	 * @see DateOrDatetimeEditor#setAsText(String)
	 */
	@Test
	public void setAsText_shouldFailOnPartialDateAndTime() {
		assertThrows(IllegalArgumentException.class, () -> ed.setAsText("27/10/2011 17"));
	}
}
