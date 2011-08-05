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

import org.openmrs.Obs;
import org.openmrs.obs.ComplexData;
import org.openmrs.obs.handler.ImageHandler;
import org.openmrs.web.WebConstants;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.obs.ComplexObsHandler;

/**
 * Extends functionality of {@link ImageHandler} for web specific views.
 * 
 * @since 1.5
 * 
 * This class extends org.openmrs.obs.handler.ImageHandler
 * You may also consider extending ImageHandler class to meet your requirements. 
 * 
 * There may be several classes which extend
 * ImageHandler. Out of these, only one will be loaded by Spring. The class to be loaded will be
 * decided based on the @Order annotation value. 
 * 
 * As default, ImageHandler will have the lowest possible
 * priority, and will be overridden by WebImageHandler
 */

@Component
@Order(0)
public class WebImageHandler extends ImageHandler implements ComplexObsHandler {
	
	private static final Log log = LogFactory.getLog(WebImageHandler.class);
	
	/**
	 * Default Constructor
	 */
	public WebImageHandler() {
		super();
	}
	
	/**
	 * Returns the ComplexData for an Obs depending on the view. Currently supported views are
	 * listed in WebConstants.*_VIEW. <br>
	 * Currently the only implemented views are
	 * <ul>
	 * <li>{@link WebConstants#HYPERLINK_VIEW}: a lightweight alternative to returning the
	 * ComplexData from the parent class since this does not require access to the service layer.
	 * Gives a link to the ComplexServlet for this obs
	 * <li>{@link WebConstants#HTML_VIEW}: An html tag that will display this complex data. For this
	 * ImageHandler, its an html img tag.
	 * </ul>
	 * 
	 * @see org.openmrs.obs.handler.ImageHandler#getComplexData(org.openmrs.Obs, java.lang.String)
	 */
	@Override
	public Obs getObs(Obs obs, String view) {
		
		if (WebConstants.HYPERLINK_VIEW.equals(view)) {
			ComplexData cd = new ComplexData(obs.getValueAsString(null), getHyperlink(obs));
			obs.setComplexData(cd);
			return obs;
		} else if (WebConstants.HTML_VIEW.equals(view)) {
			String imgtag = "<img src='" + getHyperlink(obs) + "'/>";
			ComplexData cd = new ComplexData(obs.getValueAsString(null), imgtag);
			obs.setComplexData(cd);
			return obs;
		} else {
			// fall through to default parent ImageHandler action
		}
		
		return super.getObs(obs, view);
	}
	
	/**
	 * Return the link to the complex obs servlet that will write out the contents of the complex
	 * obs to the response
	 * 
	 * @param obs
	 * @return String url that will render the image file object (in this case, this rendering is
	 *         done by the ComplexObsServlet)
	 */
	private String getHyperlink(Obs obs) {
		return "/" + WebConstants.WEBAPP_NAME + "/complexObsServlet?obsId=" + obs.getObsId();
	}
	
	/**
	 * Gets the handler type for each registered handler.
	 * 
	 * @return the handler type
	 */
	@Override
	public String getHandlerType() {
		return "ImageHandler";
	}
	
}
