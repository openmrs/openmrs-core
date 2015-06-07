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
