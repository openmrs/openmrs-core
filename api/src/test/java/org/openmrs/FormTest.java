/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
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
