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

/**
 * 
 */
public class DrugEditorTest extends BaseContextSensitiveTest {
	
	/**
	 * @verifies {@link DrugEditor#setAsText} set value to the drug with the specified identifier
	 * @throws Exception
	 */
	@Test
	public void setAsText_shouldSetValueToTheDrugWithTheSpecifiedIdentifier() throws Exception {
		DrugEditor drugEditor = new DrugEditor();
		drugEditor.setAsText("2");
		Drug drug = (Drug) drugEditor.getValue();
		
		Assert.assertNotNull(drug);
		Assert.assertEquals("", new Integer(2), drug.getDrugId());
	}
	
	/*
	@should set value to null if given empty string 
	@should set value to null if given null value
	@should fail if drug does not exist with non-empty identifier
	@should return drug identifier as string when editor has a value
	@should return empty string when editor has a null value
	*/
}
