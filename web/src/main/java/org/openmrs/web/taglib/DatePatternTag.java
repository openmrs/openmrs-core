/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.taglib;

import java.text.SimpleDateFormat;

import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;

/**
 * Returns a string like mm/dd/yyyy for the current user
 */
public class DatePatternTag extends TagSupport {
	
	private static final long serialVersionUID = 122L;
	
	private final Log log = LogFactory.getLog(getClass());
	
	/**
	 * This is to tell the user whether the string to be returned is the localized pattern or not,
	 * in use as the jquery datepicker widget format
	 * 
	 * @since 1.7
	 */
	private String localize = null;//TRUE by default
	
	/**
	 * Does the actual working of printing the date pattern
	 * 
	 * @see javax.servlet.jsp.tagext.TagSupport#doStartTag()
	 */
	@Override
	public int doStartTag() {
		
		SimpleDateFormat dateFormat = Context.getDateFormat();
		
		try {
			String pattern = dateFormat.toLocalizedPattern().toLowerCase();
			
			if ((localize != null) && "false".equals(localize)) {
				pattern = dateFormat.toPattern().toLowerCase();
			}
			
			pageContext.getOut().write(pattern);
		}
		catch (Exception e) {
			log.error("error getting date pattern", e);
		}
		
		return SKIP_BODY;
	}
	
	public String getLocalize() {
		return localize;
	}
	
	public void setLocalize(String localize) {
		this.localize = localize;
	}
}
