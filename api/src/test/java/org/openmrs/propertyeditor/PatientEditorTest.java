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

/**
 * Tests the {@link ProviderEditor}
 *
 */
public class PatientEditorTest extends BaseContextSensitiveTest {
	
	/**
	 * @see PatientEditor#setAsText(String)
	 * @verifies set using id
	 */
	@Test
	public void setAsText_shouldSetUsingId() throws Exception {
		PatientEditor editor = new PatientEditor();
		editor.setAsText("2");
		Assert.assertNotNull(editor.getValue());
	}
	
	/**
	 * @see PatientEditor#setAsText(String)
	 * @verifies set using uuid
	 */
	@Test
	public void setAsText_shouldSetUsingUuid() throws Exception {
		PatientEditor editor = new PatientEditor();
		editor.setAsText("da7f524f-27ce-4bb2-86d6-6d1d05312bd5");
		Assert.assertNotNull(editor.getValue());
	}
}
