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

import javax.servlet.jsp.JspException;

import org.springframework.web.servlet.tags.HtmlEscapingAwareTag;
import org.springframework.web.util.ExpressionEvaluationUtils;

/**
 * This class takes its functionality from {@link HtmlEscapingAwareTag}. Actually, it supplies a "htmlEscape" property for
 * explicitly specifying whether or not to apply HTML escaping. The only difference from spring's analog is that it provides
 * additional access to tag body content, which is not provided in spring implementation
 */
public abstract class OpenmrsHtmlEscapingAwareTag extends OpenmrsRequestContextAwareBodyTag {
	
	/** */
	private static final long serialVersionUID = 1L;
	
	private Boolean htmlEscape;
	
	/**
	 * @see HtmlEscapingAwareTag#setHtmlEscape(String)
	 */
	public void setHtmlEscape(String htmlEscape) throws JspException {
		this.htmlEscape = ExpressionEvaluationUtils.evaluateBoolean("htmlEscape", htmlEscape, pageContext);
	}
	
	/**
	 * @see HtmlEscapingAwareTag#isHtmlEscape()
	 */
	protected boolean isHtmlEscape() {
		if (this.htmlEscape != null) {
			return this.htmlEscape.booleanValue();
		} else {
			return isDefaultHtmlEscape();
		}
	}
	
	/**
	 * @see HtmlEscapingAwareTag#isDefaultHtmlEscape()
	 */
	protected boolean isDefaultHtmlEscape() {
		return getRequestContext().isDefaultHtmlEscape();
	}
	
}
