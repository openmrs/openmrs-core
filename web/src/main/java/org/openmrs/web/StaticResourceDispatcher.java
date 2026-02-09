/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

public class StaticResourceDispatcher implements Controller {
	
	private Controller jstlContentController;
	
	private Controller staticContentController;

	@Override
	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {

		String fullPath = request.getRequestURI();

		String ctx = request.getContextPath();
		if (ctx != null && !ctx.isEmpty() && fullPath.startsWith(ctx)) {
			fullPath = fullPath.substring(ctx.length());
		}

		String lastSegment;
		int slash = fullPath.lastIndexOf('/');
		if (slash >= 0) {
			lastSegment = fullPath.substring(slash + 1);
		} else {
			lastSegment = fullPath;
		}

		if ("openmrsmessages.js".equals(lastSegment) || "drugOrder.js".equals(lastSegment)) {
			return jstlContentController.handleRequest(request, response);
		}

		return staticContentController.handleRequest(request, response);
	}

	public void setJstlContentController(Controller jstlContentController) {
		this.jstlContentController = jstlContentController;
	}

	public void setStaticContentController(Controller staticContentController) {
		this.staticContentController = staticContentController;
	}
}
