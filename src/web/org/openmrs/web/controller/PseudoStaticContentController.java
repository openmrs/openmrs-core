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

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

public class PseudoStaticContentController implements Controller {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	private Boolean interpretJstl = false;
	
	public Boolean getInterpretJstl() {
		return interpretJstl;
	}
	
	public void setInterpretJstl(Boolean interpretJstl) {
		this.interpretJstl = interpretJstl;
	}
	
	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException,
	                                                                                           IOException {
		String path = request.getServletPath() + request.getPathInfo();
		if (interpretJstl)
			path += ".withjstl";
		return new ModelAndView(path);
	}
}
