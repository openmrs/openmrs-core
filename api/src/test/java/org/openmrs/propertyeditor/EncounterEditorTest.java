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

public class EncounterEditorTest extends BaseContextSensitiveTest {
	
	/**
	 * @see EncounterEditor#setAsText(String)
	 * @verifies set using id
	 */
	@Test
	public void setAsText_shouldSetUsingId() throws Exception {
		EncounterEditor editor = new EncounterEditor();
		editor.setAsText("3");
		Assert.assertNotNull(editor.getValue());
	}
	
	/**
	 * @see EncounterEditor#setAsText(String)
	 * @verifies set using uuid
	 */
	@Test
	public void setAsText_shouldSetUsingUuid() throws Exception {
		EncounterEditor editor = new EncounterEditor();
		editor.setAsText("6519d653-393b-4118-9c83-a3715b82d4ac");
		Assert.assertNotNull(editor.getValue());
	}
}
