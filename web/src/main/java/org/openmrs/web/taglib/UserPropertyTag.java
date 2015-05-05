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

import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;

public class UserPropertyTag extends TagSupport {
	
	private static final long serialVersionUID = 121L;
	
	private final Log log = LogFactory.getLog(getClass());
	
	private String key = "";
	
	private String defaultValue = "";
	
	private String var = null;
	
	public int doStartTag() {
		
		String value = defaultValue;
		// If user is logged in
		if (Context.isAuthenticated()) {
			String prop = Context.getAuthenticatedUser().getUserProperty(key);
			if (prop != null && !"".equals(prop)) {
				value = prop;
			}
		}
		
		try {
			if (var != null) {
				pageContext.setAttribute(var, value);
			} else {
				pageContext.getOut().write(value);
			}
			
		}
		catch (Exception e) {
			log.error("error getting global property", e);
		}
		return SKIP_BODY;
	}
	
	public String getKey() {
		return this.key;
	}
	
	public void setKey(String key) {
		this.key = key;
	}
	
	public String getDefaultValue() {
		return this.defaultValue;
	}
	
	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}
	
	public String getVar() {
		return var;
	}
	
	public void setVar(String var) {
		this.var = var;
	}
	
}
