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
	 * Does the actual working of printing the date pattern
	 * 
	 * @see javax.servlet.jsp.tagext.TagSupport#doStartTag()
	 */
	public int doStartTag() {
		
		SimpleDateFormat dateFormat = Context.getDateFormat();
		
		try {
			pageContext.getOut().write(dateFormat.toLocalizedPattern().toLowerCase());
			
		}
		catch (Exception e) {
			log.error("error getting date pattern", e);
		}
		
		return SKIP_BODY;
	}
	
}
