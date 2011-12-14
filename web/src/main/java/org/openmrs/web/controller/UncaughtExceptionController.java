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
package org.openmrs.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Controller to display the uncaughtException page and ensure that it always displays
 * via the spring dispatcherServlet.
 */
@Controller
public class UncaughtExceptionController {
	
	private static final long serialVersionUID = 1L;
	
	@RequestMapping(value = "/uncaughtException", method = RequestMethod.POST)
	public void displayUncaughtException() {
		
	}
}
