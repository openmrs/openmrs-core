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
package org.openmrs.web.controller.patientset;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.openmrs.Cohort;
import org.openmrs.api.context.Context;
import org.springframework.web.servlet.mvc.SimpleFormController;

public class MultipleSummariesController extends SimpleFormController {
	
	/**
	 * This is called prior to displaying a form for the first time. It tells Spring the
	 * form/command object to load into the request
	 * 
	 * @see org.springframework.web.servlet.mvc.AbstractFormController#formBackingObject(javax.servlet.http.HttpServletRequest)
	 */
	protected Object formBackingObject(HttpServletRequest request) throws ServletException {
		
		Cohort ps = null;
		
		if (Context.isAuthenticated()) {
			String source = request.getParameter("source");
			if ("myPatientSet".equals(source))
				ps = Context.getPatientSetService().getMyPatientSet();
		}
		
		if (ps == null)
			ps = new Cohort();
		
		return ps;
	}
	
}
