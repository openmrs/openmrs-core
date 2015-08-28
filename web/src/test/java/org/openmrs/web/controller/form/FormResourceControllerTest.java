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

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.openmrs.Form;
import org.openmrs.FormResource;
import org.openmrs.customdatatype.datatype.LongFreeTextDatatype;
import org.openmrs.web.attribute.handler.LongFreeTextFileUploadHandler;
import org.openmrs.web.test.BaseWebContextSensitiveTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockMultipartHttpServletRequest;
import org.springframework.util.FileCopyUtils;
import org.springframework.validation.Errors;

public class FormResourceControllerTest extends BaseWebContextSensitiveTest {
// 
//    @Test
//    public void handleFormResource_shouldStayOnSamePageWhenUnableToSaveFormResource() throws Exception {
//    	
//		final String EXPECTED_FORM_ID = "1";
//		final String EXPECTED_DATATYPE_CLASS_NAME = (new LongFreeTextDatatype()).getClass().getName();
//		final String EXPECTED_HANDLER_CLASS_NAME = (new LongFreeTextFileUploadHandler()).getClass().getName();
//		final String EXPECTED_URL = "redirect:addFormResource.form?formId="
//			+ EXPECTED_FORM_ID + "&datatype=" 
//			+ EXPECTED_DATATYPE_CLASS_NAME + "&handler="
//			+ EXPECTED_HANDLER_CLASS_NAME;
//	    
//		Errors errors = null;
//		
//		Form form = null;
//		
//		FormResource resource = null;
//		
//		MockMultipartHttpServletRequest request = new MockMultipartHttpServletRequest();
//		request.setMethod("POST");
//		request.setParameter("formId", "1");
//		request.setParameter("datatype", EXPECTED_DATATYPE_CLASS_NAME);
//		request.setParameter("handler", EXPECTED_HANDLER_CLASS_NAME);
//	
//		FormResourceController controller = new FormResourceController();
//		
//		String actualUrl = controller.handleAddFormResource(resource, errors, request);
//		
//		Assert.assertEquals(EXPECTED_URL, actualUrl);
//    }
//    
    @Test
    public void handleFormResource_shouldRedirectAfterSavingFormResource() throws Exception {
    	Form form = new Form();
    	form.setId(1);
    	form.setName("test form");
    	form.setFormId(1);
    	form.setVersion("1.0.0");
    	
    	FormResource resource = new FormResource();
    	resource.setName("test resource");
    	resource.setForm(form);
    	resource.setFormResourceId(1);
    	resource.setId(1);
    	
    	byte[] fileContent = "Hello World".getBytes();
    	String fileName = "/resources/testfile.ico";
    	MockMultipartHttpServletRequest request = new MockMultipartHttpServletRequest();
    	MockMultipartFile file = new MockMultipartFile("resourceValue", fileName, "text/plain", fileContent);
    	
    	request.setMethod("POST");
    	request.setParameter("datatype", (new LongFreeTextDatatype()).getClass().getName());
    	request.setParameter("handler", (new LongFreeTextFileUploadHandler()).getClass().getName());
    	request.addFile(file);
    	
    	Errors errors = Mockito.mock(Errors.class);
    	Mockito.when(errors.hasErrors()).thenReturn(false);
    	
    	FormResourceController controller = new FormResourceController();
    	
    	String expectedUrl = "redirect:formResources.form?formId=1";
    	String actualUrl = controller.handleAddFormResource(resource, errors, request);
    
    	Assert.assertEquals(expectedUrl, actualUrl);
    }

}
