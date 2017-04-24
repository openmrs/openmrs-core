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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.Visit;
import org.openmrs.api.VisitService;
import org.openmrs.test.BaseContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

public class VisitEditorTest extends BaseContextSensitiveTest {
	
	private VisitEditor editor;
	
	private static final String EXISTING_ID = "1";
	
	private static final String NON_EXISTING_ID = "999999";
	
	private static final String EXISTING_UUID = "1e5d5d48-6b78-11e0-93c3-18a905e044dc";
	
	private static final String NON_EXISTING_UUID = "9999xxxx-e131-11de-babe-001e378eb67e";
	
	@Autowired
	private VisitService visitService;
	
	@Before
	public void setUp() {
		editor = new VisitEditor();
	}
	
	@Test
	public void shouldSetTheEditorValueToNullIfGivenNull() {
		
		editor.setAsText(null);
		
		assertNull(editor.getValue());
	}
	
	@Test
	public void shouldSetTheEditorValueToNullIfGivenAnEmptyString() {
		
		editor.setAsText("  ");
		
		assertNull(editor.getValue());
	}
	
	@Test
	public void shouldSetTheEditorValueToObjectAssociatedWithGivenId() {
		
		Visit obj = visitService.getVisit(Integer.valueOf(EXISTING_ID));
		
		editor.setAsText(EXISTING_ID);
		
		assertThat(editor.getValue(), is(obj));
	}
	
	@Test
	public void shouldSetTheEditorValueToNullIfGivenIdDoesNotExist() {
		
		Visit obj = visitService.getVisit(Integer.valueOf(NON_EXISTING_ID));
		assertNull("object must not exist", obj);
		
		editor.setAsText(NON_EXISTING_ID);
		
		assertNull(editor.getValue());
	}
	
	@Test
	public void shouldSetTheEditorValueToObjectAssociatedWithGivenUuid() {
		
		Visit obj = visitService.getVisitByUuid(EXISTING_UUID);
		
		editor.setAsText(EXISTING_UUID);
		
		assertThat(editor.getValue(), is(obj));
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void shouldFailToSetTheEditorValueIfGivenUuidDoesNotExist() {
		
		Visit objUuid = visitService.getVisitByUuid(NON_EXISTING_UUID);
		assertNull("object must not exist", objUuid);
		
		editor.setAsText(NON_EXISTING_UUID);
	}
	
	@Test
	public void shouldReturnEmptyStringIfValueIsNull() {
		
		assertThat(editor.getAsText(), is(""));
	}
	
	@Test
	public void shouldReturnTheObjectIdIfValueIsNotNull() {
		
		editor.setValue(visitService.getVisit(Integer.valueOf(EXISTING_ID)));
		
		assertThat(editor.getAsText(), is(EXISTING_ID));
	}
}
