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
import org.openmrs.Drug;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.Verifies;

/**
 * 
 */
public class DrugEditorTest extends BaseContextSensitiveTest {
	
	/**
	 * @see {@link DrugEditor#setAsText(String)}
	 */
	@Test
	@Verifies(value = "should set value to the drug with the specified identifier", method = "setAsText(String)")
	public void xsetAsText_shouldSetValueToTheDrugWithTheSpecifiedIdentifier() throws Exception {
		DrugEditor drugEditor = new DrugEditor();
		drugEditor.setAsText("2");
		Drug drug = (Drug) drugEditor.getValue();
		
		Assert.assertNotNull(drug);
		Assert.assertEquals("", Integer.valueOf(2), drug.getDrugId());
	}
	
	/**
	 * @see DrugEditor#setAsText(String)
	 * @verifies set using uuid
	 */
	@Test
	public void setAsText_shouldSetUsingUuid() throws Exception {
		DrugEditor drugEditor = new DrugEditor();
		drugEditor.setAsText("3cfcf118-931c-46f7-8ff6-7b876f0d4202");
		Assert.assertNotNull(drugEditor.getValue());
	}
}
