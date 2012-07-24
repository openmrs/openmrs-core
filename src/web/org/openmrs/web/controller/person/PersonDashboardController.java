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
package org.openmrs.web.controller.person;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Person;
import org.openmrs.api.context.Context;
import org.springframework.web.servlet.mvc.SimpleFormController;

/**
 *
 */
public class PersonDashboardController extends SimpleFormController {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	protected Object formBackingObject(HttpServletRequest request) throws ServletException {
		if (!Context.isAuthenticated())
			return new Person();
		else
			return Context.getPersonService().getPerson(Integer.valueOf(request.getParameter("personId")));
	}
}
