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
