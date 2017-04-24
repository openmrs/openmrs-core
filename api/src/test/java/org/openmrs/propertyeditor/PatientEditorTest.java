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
import org.openmrs.Patient;
import org.openmrs.api.PatientService;
import org.openmrs.test.BaseContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Tests the {@link PatientEditor}
 */
public class PatientEditorTest extends BaseContextSensitiveTest {
	
	private PatientEditor editor;
	
	private static final String EXISTING_ID = "2";
	
	private static final String NON_EXISTING_ID = "999999";
	
	private static final String EXISTING_UUID = "da7f524f-27ce-4bb2-86d6-6d1d05312bd5";
	
	private static final String NON_EXISTING_UUID = "9999xxxx-e131-11de-babe-001e378eb67e";
	
	@Autowired
	private PatientService patientService;
	
	@Before
	public void setUp() {
		editor = new PatientEditor();
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
		
		Patient obj = patientService.getPatient(Integer.valueOf(EXISTING_ID));
		
		editor.setAsText(EXISTING_ID);
		
		assertThat(editor.getValue(), is(obj));
	}
	
	@Test
	public void shouldSetTheEditorValueToNullIfGivenIdDoesNotExist() {
		
		Patient obj = patientService.getPatient(Integer.valueOf(NON_EXISTING_ID));
		assertNull("object must not exist", obj);
		
		editor.setAsText(NON_EXISTING_ID);
		
		assertNull(editor.getValue());
	}
	
	@Test
	public void shouldSetTheEditorValueToObjectAssociatedWithGivenUuid() {
		
		Patient obj = patientService.getPatientByUuid(EXISTING_UUID);
		
		editor.setAsText(EXISTING_UUID);
		
		assertThat(editor.getValue(), is(obj));
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void shouldFailToSetTheEditorValueIfGivenUuidDoesNotExist() {
		
		Patient objUuid = patientService.getPatientByUuid(NON_EXISTING_UUID);
		assertNull("object must not exist", objUuid);
		
		editor.setAsText(NON_EXISTING_UUID);
	}
	
	@Test
	public void shouldReturnEmptyStringIfValueIsNull() {
		
		assertThat(editor.getAsText(), is(""));
	}
	
	@Test
	public void shouldReturnTheObjectIdIfValueIsNotNull() {
		
		editor.setValue(patientService.getPatient(Integer.valueOf(EXISTING_ID)));
		
		assertThat(editor.getAsText(), is(EXISTING_ID));
	}
}
