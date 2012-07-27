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
		Assert.assertEquals("", new Integer(2), drug.getDrugId());
	}
}
