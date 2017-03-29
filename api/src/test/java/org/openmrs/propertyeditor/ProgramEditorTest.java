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

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.test.BaseContextSensitiveTest;

public class ProgramEditorTest extends BaseContextSensitiveTest {
	
	/**
	 * @see ProgramEditor#setAsText(String)
	 */
	@Test
	public void setAsText_shouldSetUsingConceptId() {
		ProgramEditor editor = new ProgramEditor();
		editor.setAsText("concept.9");
		Assert.assertNotNull(editor.getValue());
	}
	
	/**
	 * @see ProgramEditor#setAsText(String)
	 */
	@Test
	public void setAsText_shouldSetUsingConceptUuid() {
		ProgramEditor editor = new ProgramEditor();
		editor.setAsText("concept.0a9afe04-088b-44ca-9291-0a8c3b5c96fa");
		Assert.assertNotNull(editor.getValue());
	}
	
	/**
	 * @see ProgramEditor#setAsText(String)
	 */
	@Test
	public void setAsText_shouldSetUsingProgramId() {
		ProgramEditor editor = new ProgramEditor();
		editor.setAsText("1");
		Assert.assertNotNull(editor.getValue());
	}
	
	/**
	 * @see ProgramEditor#setAsText(String)
	 */
	@Test
	public void setAsText_shouldSetUsingProgramUuid() {
		ProgramEditor editor = new ProgramEditor();
		editor.setAsText("da4a0391-ba62-4fad-ad66-1e3722d16380");
		Assert.assertNotNull(editor.getValue());
	}
}
