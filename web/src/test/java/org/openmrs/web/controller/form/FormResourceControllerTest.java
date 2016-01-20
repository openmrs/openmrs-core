/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.controller.form;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.hibernate.exception.ConstraintViolationException;
import org.junit.Test;
import org.openmrs.Form;
import org.openmrs.FormResource;
import org.openmrs.customdatatype.datatype.LongFreeTextDatatype;
import org.openmrs.web.attribute.handler.LongFreeTextFileUploadHandler;
import org.openmrs.web.test.BaseWebContextSensitiveTest;
import org.springframework.mock.web.MockMultipartHttpServletRequest;
import org.springframework.validation.Errors;

public class FormResourceControllerTest extends BaseWebContextSensitiveTest {
	
	@Test
	public void handleFormResource_shouldReturnViewURLOfCurrentPageWhenAnInvalidFileTypeExceptionIsThrown() throws Exception {
		
		final String EXPECTED_FORM_ID = "1";
		final String EXPECTED_DATATYPE_CLASS_NAME = (new LongFreeTextDatatype()).getClass().getName();
		final String EXPECTED_HANDLER_CLASS_NAME = (new LongFreeTextFileUploadHandler()).getClass().getName();
		final String EXPECTED_URL = "redirect:addFormResource.form?formId=" + EXPECTED_FORM_ID + "&datatype="
		        + EXPECTED_DATATYPE_CLASS_NAME + "&handler=" + EXPECTED_HANDLER_CLASS_NAME;
		
		Errors errors = mock(Errors.class);
		when(errors.hasErrors()).thenReturn(false);
		
		Form form = mock(Form.class);
		when(form.getFormId()).thenReturn(1);
		when(form.getId()).thenReturn(1);
		
		FormResource resource = mock(FormResource.class);
		when(resource.getForm()).thenReturn(form);
		when(resource.getDatatypeClassname()).thenReturn(EXPECTED_DATATYPE_CLASS_NAME);
		when(resource.getPreferredHandlerClassname()).thenReturn(EXPECTED_HANDLER_CLASS_NAME);
		when(resource.isDirty()).thenThrow(new ConstraintViolationException("for testing", null, "for testing"));
		
		MockMultipartHttpServletRequest request = new MockMultipartHttpServletRequest();
		request.setMethod("POST");
		request.setParameter("formId", "1");
		request.setParameter("datatype", EXPECTED_DATATYPE_CLASS_NAME);
		request.setParameter("handler", EXPECTED_HANDLER_CLASS_NAME);
		
		FormResourceController controller = new FormResourceController();
		
		String actualUrl = controller.handleAddFormResource(resource, errors, request);
		
		assertEquals(EXPECTED_URL, actualUrl);
	}
	
}
