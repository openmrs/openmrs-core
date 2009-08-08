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

import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.User;
import org.openmrs.api.context.Context;

public class UserWidgetTag extends TagSupport {
	
	public static final long serialVersionUID = 1L;
	
	private final Log log = LogFactory.getLog(getClass());
	
	private Integer userId;
	
	private String size = "normal";
	
	public UserWidgetTag() {
	}
	
	public void setSize(String size) {
		this.size = size;
	}
	
	public void setUserId(Integer userId) {
		this.userId = userId;
	}
	
	public int doStartTag() {
		User user = Context.getUserService().getUser(userId);
		
		try {
			JspWriter w = pageContext.getOut();
			w.print(user.getPersonName());
			if ("full".equals(size)) {
				w.print(" <i>(" + user.getUsername() + ")</i>");
			}
		}
		catch (IOException ex) {
			log.error("Error while starting userWidget tag", ex);
		}
		return SKIP_BODY;
	}
	
	public int doEndTag() {
		userId = null;
		size = "normal";
		return EVAL_PAGE;
	}
	
}
