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

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;

public class RoleEditorTest extends BaseContextSensitiveTest {
	
	/**
	 * @see RoleEditor#setAsText(String)
	 */
	@Test
	public void setAsText_shouldSetUsingName() {
		RoleEditor editor = new RoleEditor();
		editor.setAsText("Provider");
		assertNotNull(editor.getValue());
	}
	
	/**
	 * @see RoleEditor#setAsText(String)
	 */
	@Test
	public void setAsText_shouldSetUsingUuid() {
		RoleEditor editor = new RoleEditor();
		editor.setAsText("3480cb6d-c291-46c8-8d3a-96dc33d199fb");
		assertNotNull(editor.getValue());
	}
}
