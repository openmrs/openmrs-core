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

public class ConceptSourceEditorTest extends BaseContextSensitiveTest {
	
	/**
	 * @see ConceptSourceEditor#setAsText(String)
	 * @verifies set using id
	 */
	@Test
	public void setAsText_shouldSetUsingId() throws Exception {
		ConceptSourceEditor editor = new ConceptSourceEditor();
		editor.setAsText("3");
		Assert.assertNotNull(editor.getValue());
	}
	
	/**
	 * @see ConceptSourceEditor#setAsText(String)
	 * @verifies set using uuid
	 */
	@Test
	public void setAsText_shouldSetUsingUuid() throws Exception {
		ConceptSourceEditor editor = new ConceptSourceEditor();
		editor.setAsText("75f5b378-5065-11de-80cb-001e378eb67e");
		Assert.assertNotNull(editor.getValue());
	}
}
