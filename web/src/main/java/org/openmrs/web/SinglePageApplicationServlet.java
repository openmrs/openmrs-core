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

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SinglePageApplicationServlet extends HttpServlet {
	
  private static final Logger log = LoggerFactory.getLogger(SinglePageApplicationServlet.class);

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) {
    try {
      RequestDispatcher dispatcher = request.getRequestDispatcher("/master-single-page-application.jsp");
      dispatcher.forward(request, response);
    } catch (Exception e) {
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      this.log.error("Failed to render GSP for master-single-page-application", e);
    }
  }
}