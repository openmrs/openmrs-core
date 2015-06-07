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
import org.junit.Before;
import org.junit.Test;
import org.openmrs.test.BaseContextSensitiveTest;

public class CohortEditorTest extends BaseContextSensitiveTest {
	
	protected static final String COHORT_XML = "org/openmrs/api/include/CohortServiceTest-cohort.xml";
	
	@Before
	public void prepareData() throws Exception {
		executeDataSet(COHORT_XML);
	}
	
	/**
	 * @see CohortEditor#setAsText(String)
	 * @verifies set using id
	 */
	@Test
	public void setAsText_shouldSetUsingId() throws Exception {
		CohortEditor editor = new CohortEditor();
		editor.setAsText("1");
		Assert.assertNotNull(editor.getValue());
	}
	
	/**
	 * @see CohortEditor#setAsText(String)
	 * @verifies set using uuid
	 */
	@Test
	public void setAsText_shouldSetUsingUuid() throws Exception {
		CohortEditor editor = new CohortEditor();
		editor.setAsText("h9a9m0i6-15e6-467c-9d4b-mbi7teu9lf0f");
		Assert.assertNotNull(editor.getValue());
	}
}
