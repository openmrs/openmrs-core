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

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * Prints the contextPath for the current webapp. Typically you can get this via
 * ${pageContext.request.contextPath}
 */
public class ContextPathTag extends TagSupport {
	
	private static final long serialVersionUID = 112734331123332322L;
	
	//private final Log log = LogFactory.getLog(getClass());
	
	public int doStartTag() {
		HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
		
		try {
			pageContext.getOut().write(request.getContextPath());
		}
		catch (IOException e) {
			// pass
		}
		return SKIP_BODY;
	}
	
	public int doEndTag() {
		return EVAL_PAGE;
	}
	
}
