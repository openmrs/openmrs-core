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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;

import javax.servlet.jsp.tagext.TagSupport;
import java.text.SimpleDateFormat;

/**
 * Returns a time pattern sting (E.g hh:mm ) suitable for jquery or Java in required locale
 *
 * @since 1.9
 */
public class TimePatternTag extends TagSupport {
	
	private static final long serialVersionUID = 1L;
	
	private final Log log = LogFactory.getLog(getClass());
	
	/**
	 * Defines whether time pattern sting to be localised or not,
	 */
	private String localize = null;//TRUE by default
	
	/**
	 * Defines the format, "jquery" for time pattern to be in jquery format
	 * else time pattern to be in Java format
	 */
	private String format = null;
	
	/**
	 * Does the actual working of printing the time pattern
	 *
	 * @see javax.servlet.jsp.tagext.TagSupport#doStartTag()
	 */
	@Override
	public int doStartTag() {
		
		SimpleDateFormat dateFormat = Context.getTimeFormat();
		
		try {
			String pattern;
			if ((localize != null) && "false".equals(localize)) {
				pattern = dateFormat.toPattern().toLowerCase();
			} else {
				pattern = dateFormat.toLocalizedPattern();
			}
			
			if (null != format && format.equals("jquery")) {
				pattern = pattern.toLowerCase().replaceAll("a", "TT");
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
	
	public String getFormat() {
		return format;
	}
	
	public void setFormat(String format) {
		this.format = format;
	}
}
