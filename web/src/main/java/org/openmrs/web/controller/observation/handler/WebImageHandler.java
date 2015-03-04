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

import java.util.Locale;

import org.openmrs.api.context.Context;
import org.openmrs.Obs;
import org.openmrs.obs.ComplexData;
import org.openmrs.obs.handler.ImageHandler;
import org.openmrs.web.WebConstants;

/**
 * Extends functionality of {@link ImageHandler} for web specific views.
 * 
 * @since 1.5
 */
public class WebImageHandler extends ImageHandler {
	
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
			Locale locale = Context.getLocale();
			ComplexData cd = new ComplexData(obs.getValueAsString(locale), getHyperlink(obs));
			obs.setComplexData(cd);
			return obs;
		} else if (WebConstants.HTML_VIEW.equals(view)) {
			String imgtag = "<img src='" + getHyperlink(obs) + "'/>";
			Locale locale = Context.getLocale();
			ComplexData cd = new ComplexData(obs.getValueAsString(locale), imgtag);
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
	
}
