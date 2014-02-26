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
