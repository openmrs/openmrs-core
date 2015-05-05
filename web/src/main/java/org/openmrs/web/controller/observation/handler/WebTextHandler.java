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
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

import org.springframework.util.Assert;

import org.openmrs.api.context.Context;
import org.openmrs.Obs;
import org.openmrs.obs.ComplexData;
import org.openmrs.obs.ComplexObsHandler;
import org.openmrs.obs.handler.TextHandler;

import org.openmrs.web.controller.observation.handler.WebHandlerUtils;

/**
 * Extends functionality of {@link TextHandler} for web specific views.
 * 
 * @since 1.12
 */
public class WebTextHandler extends TextHandler {
	
	/** Views supported by this handler */
	private static final String[] supportedViews = { ComplexObsHandler.URI_VIEW, ComplexObsHandler.HTML_VIEW };
	
	/**
	 * Default Constructor
	 */
	public WebTextHandler() {
		super();
	}
	
	/**
	 * Returns the ComplexData for an Obs depending on the view.
	 * Currently, the views implemented are those supported by ancestor plus the following:
	 * <ul>
	 * <li>{@link WebConstants#URI_VIEW}: a lightweight alternative to returning the
	 * ComplexData from the parent class since this does not require access to the service layer.
	 * Gives a link to the ComplexServlet for this obs
	 * </ul>
	 * 
	 * @see org.openmrs.obs.handler.TextHandler#getComplexData(org.openmrs.Obs, java.lang.String)
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
			obs = super.getObs(obs, ComplexObsHandler.TEXT_VIEW);
			
			ComplexData cd = obs.getComplexData();
			Assert.notNull(cd, "TextHandler failed to provide text complex data");
			Assert.isInstanceOf(String.class, cd.getData(), "TextHandler doesn't provide text as string");
			
			Locale locale = Context.getLocale();
			cd = new ComplexData(obs.getValueAsString(locale), "<pre>" + cd.getData() + "</pre>");
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
		List viewList = new ArrayList(Arrays.asList(supportedViews));
		viewList.addAll(Arrays.asList(super.getSupportedViews()));
		String[] views = new String[viewList.size()];
		viewList.toArray(views);
		return views;
	}
	
}
