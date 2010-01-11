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
package org.openmrs.web.controller.user;

import org.junit.Test;
import org.openmrs.PersonName;
import org.openmrs.User;
import org.openmrs.test.Verifies;
import org.openmrs.web.test.BaseWebContextSensitiveTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindException;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

/**
 * Tests the {@link oldUserFormController} class.
 */
public class UserFormControllerTest extends BaseWebContextSensitiveTest {

	/**
     * @see {@link UserFormController#handleSubmission(WebRequest,HttpSession,String,String,String,null,User,BindingResult)}
     * 
     */
    @Test
    @Verifies(value = "should work for an example", method = "handleSubmission(WebRequest,HttpSession,String,String,String,null,User,BindingResult)")
    public void handleSubmission_shouldWorkForAnExample() throws Exception {
	    UserFormController controller = new UserFormController();
	    WebRequest request = new ServletWebRequest(new MockHttpServletRequest());
	    User user = controller.formBackingObject(request, null);
	    user.addName(new PersonName("This", "is", "Test"));
	    user.getPerson().setGender("F");
	    controller.handleSubmission(request, new MockHttpSession(), new ModelMap(), "Save User", "pass123", "pass123", new String[0], user, new BindException(user, "user"));
    }
	
}
