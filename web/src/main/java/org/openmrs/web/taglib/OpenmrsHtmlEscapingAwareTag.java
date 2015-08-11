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

import javax.servlet.jsp.JspException;

import org.springframework.web.servlet.tags.HtmlEscapingAwareTag;

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
	 * @see HtmlEscapingAwareTag#setHtmlEscape(boolean)
	 */
	public void setHtmlEscape(String htmlEscape) throws JspException {
		this.htmlEscape = Boolean.valueOf(htmlEscape);
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
