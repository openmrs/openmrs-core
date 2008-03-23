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

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

/**
 * Redirects the request to the given <code>formView</code>
 * 
 * @author bwolfe
 *
 */
public class RedirectController implements Controller {

	private String redirectView = "";
	
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
    	
    	// default to the current path
    	if (redirectView == null)
    		redirectView = request.getServletPath();
    	
    	return new ModelAndView(redirectView);
    }
    
	public void setRedirectView(String view) {
		this.redirectView = view;
	}
	
	public String getRedirectView() {
		return this.redirectView;
	}
}