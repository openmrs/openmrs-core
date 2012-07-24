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
package org.openmrs;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.test.Verifies;

/**
 * This class tests the all of the {@link Form} non-trivial object methods.
 * 
 * @see Form
 */
public class FormTest {
	
	/**
	 * Make sure the Form(Integer) constructor sets the formId
	 * 
	 * @see {@link Form#Form(Integer)}
	 */
	@Test
	@Verifies(value = "should set formId with given parameter", method = "Form(Integer)")
	public void Form_shouldSetFormIdWithGivenParameter() throws Exception {
		Form form = new Form(123);
		Assert.assertEquals(123, form.getFormId().intValue());
	}
}
