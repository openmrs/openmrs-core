/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.impl;

import org.hibernate.exception.ConstraintViolationException;
import org.junit.Test;
import org.openmrs.FormResource;
import org.openmrs.api.InvalidFileTypeException;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;

import static org.mockito.Mockito.*;

public class FormServiceImplTest extends BaseContextSensitiveTest{
   
    @Test(expected = InvalidFileTypeException.class)
    public void saveFormResource_shouldThrowAnInvalidFileTypeExceptionWhenUsedWithNonTextFiles() throws Exception {
	FormResource formResource = mock(FormResource.class);
	when(formResource.getName()).thenReturn("some resource");
	when(formResource.isDirty()).thenThrow(new ConstraintViolationException("some message", null, "for testing"));
	
	Context.getFormService().saveFormResource(formResource);
    }
  
}
