/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.controller.observation.handler;

import org.openmrs.Obs;
import org.openmrs.web.WebConstants;

/**
 * Common utilities for handling complex obs by the web application.
 * <br/>
 * 
 * @since 1.12
 */
public class WebHandlerUtils {
	
	/**
	 * Return the link to the complex obs servlet that will write out the contents of the complex
	 * obs to the response
	 * 
	 * @param obs
	 * @param view
	 * @return String url that will render the complex observation object (in this case, this rendering is
	 *         done by the ComplexObsServlet)
	 */
	public static String getHyperlink(Obs obs, String view) {
		return "/" + WebConstants.WEBAPP_NAME + "/complexObsServlet?obsId=" + obs.getObsId() + "&view=" + view;
	}
	
}
