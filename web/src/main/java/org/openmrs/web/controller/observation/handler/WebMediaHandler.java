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

import java.util.Locale;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

import org.openmrs.api.context.Context;
import org.openmrs.Obs;
import org.openmrs.obs.ComplexData;
import org.openmrs.obs.ComplexObsHandler;

import org.openmrs.obs.handler.MediaHandler;
import org.openmrs.util.OpenmrsUtil;

/**
 * Extends functionality of {@link MediaHandler} for web specific views.
 *
 * @since 1.12
 */
public class WebMediaHandler extends MediaHandler {
	
	/** Views supported by this handler */
	private static final String[] supportedViews = { ComplexObsHandler.URI_VIEW, ComplexObsHandler.HTML_VIEW, };
	
	/**
	 * Default Constructor
	 */
	public WebMediaHandler() {
		super();
	}
	
	/**
	 * Returns the ComplexData for an Obs depending on the view. Currently supported views are
	 * listed in ComplexObsHandler.*_VIEW. <br>
	 * Currently the only implemented views are those implemented by ancestor plus the following:
	 * <ul>
	 * <li>{@link org.openmrs.web.WebConstants#HYPERLINK_VIEW}: a lightweight alternative to returning the
	 * ComplexData from the parent class since this does not require access to the service layer.
	 * Gives a link to the ComplexServlet for this obs
	 * <li>{@link org.openmrs.web.WebConstants#HTML_VIEW}: An html tag that will display this complex data. For this
	 * MediaHandler, its an html5 audio or video tag.
	 * </ul>
	 *
	 * @see org.openmrs.obs.handler.MediaHandler#getObs(org.openmrs.Obs, java.lang.String)
	 */
	@Override
	public Obs getObs(Obs obs, String view) {
		if (ComplexObsHandler.URI_VIEW.equals(view)) {
			Locale locale = Context.getLocale();
			ComplexData cd = new ComplexData(obs.getValueAsString(locale), WebHandlerUtils.getHyperlink(obs,
			    ComplexObsHandler.RAW_VIEW));
			obs.setComplexData(cd);
			return obs;
		}
		
		if (ComplexObsHandler.HTML_VIEW.equals(view)) {
			String mediaTag = "";
			String mimeType = OpenmrsUtil.getFileMimeType(getComplexDataFile(obs));
			if (mimeType.contains("video")) {
				mediaTag = "<video controls>" + "<source src=\""
				        + WebHandlerUtils.getHyperlink(obs, ComplexObsHandler.RAW_VIEW) + "\" type=\"" + mimeType
				        + "\"></video>";
			} else {
				mediaTag = "<audio controls>" + "<source src=\""
				        + WebHandlerUtils.getHyperlink(obs, ComplexObsHandler.RAW_VIEW) + "\" type=\"" + mimeType
				        + "\"></audio>";
			}
			Locale locale = Context.getLocale();
			ComplexData cd = new ComplexData(obs.getValueAsString(locale), mediaTag);
			obs.setComplexData(cd);
			return obs;
		}
		
		return super.getObs(obs, view);
	}
	
	/**
	 * @see org.openmrs.obs.ComplexObsHandler#getSupportedViews()
	 */
	@Override
	public String[] getSupportedViews() {
		List view_list = new ArrayList(Arrays.asList(supportedViews));
		view_list.addAll(Arrays.asList(super.getSupportedViews()));
		String[] views = new String[view_list.size()];
		view_list.toArray(views);
		return views;
	}
	
}
