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
package org.openmrs.web.controller.observation.handler;

import java.lang.String;

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
	 * @return String url that will render the image file object (in this case, this rendering is
	 *         done by the ComplexObsServlet)
	 */
	public static String getHyperlink(Obs obs, String view) {
		return "/" + WebConstants.WEBAPP_NAME + "/complexObsServlet?obsId=" + obs.getObsId() + "&view=" + view;
	}
	
}
