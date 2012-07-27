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

import java.util.Arrays;
import java.util.Collections;

import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.springframework.util.StringUtils;

public class GlobalPropertyTag extends TagSupport {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private final Log log = LogFactory.getLog(getClass());
	
	private String key = "";
	
	private String defaultValue = "";
	
	private String var = null;
	
	private String listSeparator = null;
	
	public int doStartTag() {
		
		Object value;
		if (StringUtils.hasText(listSeparator))
			value = Collections.singletonList(defaultValue);
		else
			value = defaultValue;
		
		// If user is logged in
		if (Context.isAuthenticated()) {
			if (StringUtils.hasText(listSeparator)) {
				String stringVal = (String) Context.getAdministrationService().getGlobalProperty(key, defaultValue);
				if (stringVal.trim().length() == 0)
					value = Collections.emptyList();
				else
					value = Arrays.asList(stringVal.split(listSeparator));
			} else
				value = (String) Context.getAdministrationService().getGlobalProperty(key, defaultValue);
		}
		
		try {
			if (var != null)
				pageContext.setAttribute(var, value);
			else
				pageContext.getOut().write(value.toString());
			
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
	
	public String getListSeparator() {
		return listSeparator;
	}
	
	public void setListSeparator(String listSeparator) {
		this.listSeparator = listSeparator;
	}
	
}
