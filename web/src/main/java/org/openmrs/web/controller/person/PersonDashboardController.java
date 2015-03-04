/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
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
		if (!Context.isAuthenticated()) {
			return new Person();
		} else {
			return Context.getPersonService().getPerson(Integer.valueOf(request.getParameter("personId")));
		}
	}
}
