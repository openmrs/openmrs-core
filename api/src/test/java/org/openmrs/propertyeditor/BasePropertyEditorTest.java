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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.beans.PropertyEditor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.OpenmrsObject;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;

/**
 * Serves as a base test that covers the common implementation of OpenMRS {@code PropertyEditor's}.
 * <p>
 * OpenMRS {@code PropertyEditor's} usually when translating from text to a {@code OpenmrsObject}:
 * <ul>
 * <li>first try to find an {@code OpenmrsObject} by its {@code id}</li>
 * <li>if no object was found, try to find it by its {@code uuid}</li>
 * <li>and if no object was found, throw an {@code IllegalArgumentException}.</li>
 * </ul>
 * </p>
 * 
 * @param <T> the {@link OpenmrsObject} to be translated to or from text
 * @param <E> the {@link PropertyEditor} under test
 */
abstract class BasePropertyEditorTest<T extends OpenmrsObject, E extends PropertyEditor> extends BaseContextSensitiveTest {
	
	private static final String NON_EXISTING_ID = "999999";
	
	private static final String NON_EXISTING_UUID = "9999xxxx-e131-11de-babe-001e378eb67e";
	
	protected PropertyEditor editor;
	
	/**
	 * @return a new property editor instance used in the tests
	 */
	protected abstract E getNewEditor();
	
	/**
	 * @return an existing object for testing set as text by id and uuid
	 */
	protected abstract T getExistingObject();
	
	/**
	 * @return a non existing object uuid for testing set as text
	 */
	protected String getNonExistingObjectId() {
		return NON_EXISTING_ID;
	}
	
	/**
	 * @return a non existing object id for testing set as text
	 */
	protected String getNonExistingObjectUuid() {
		return NON_EXISTING_UUID;
	}
	
	@BeforeEach
	public void setUp() {
		editor = getNewEditor();
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
	public void shouldSetTheEditorValueToNullIfGivenIdDoesNotExist() {
		
		editor.setAsText(getNonExistingObjectId());
		
		assertNull(editor.getValue());
	}
	
	@Test
	public void shouldFailToSetTheEditorValueIfGivenUuidDoesNotExist() {
		
		assertThrows(IllegalArgumentException.class, () -> editor.setAsText(getNonExistingObjectUuid()));
	}
	
	@Test
	public void shouldSetTheEditorValueToTheObjectAssociatedWithGivenId() {
		
		editor.setAsText(getExistingObject().getId().toString());
		
		assertThat(editor.getValue(), is(getExistingObject()));
	}
	
	@Test
	public void shouldSetTheEditorValueToObjectAssociatedWithGivenUuid() {
		
		editor.setAsText(getExistingObject().getUuid());
		
		assertThat(editor.getValue(), is(getExistingObject()));
	}
	
	@Test
	public void shouldReturnEmptyStringIfValueIsNull() {
		
		assertThat(editor.getAsText(), is(""));
	}
	
	@Test
	public void shouldReturnTheObjectIdIfValueIsNotNull() {
		
		editor.setValue(getExistingObject());
		
		assertThat(editor.getAsText(), is(getExistingObject().getId().toString()));
	}
}
