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

import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.User;
import org.openmrs.api.context.Context;

/**
 * Prints out a pretty-formatted versions of an OpenMRS object TODO: add the other openmrs domain
 * objects TODO: allow this to be written to a pageContext variable instead of just the jsp TODO:
 * add a size=compact|NORMAL|full|? option
 */
public class FormatTag extends TagSupport {
	
	private static final long serialVersionUID = 1L;
	
	private final Log log = LogFactory.getLog(getClass());
	
	private Integer conceptId;
	
	private Concept concept;
	
	private Obs obsValue;
	
	private Integer userId;
	
	private User user;
	
	public int doStartTag() {
		try {
			StringBuilder sb = new StringBuilder();
			if (conceptId != null)
				concept = Context.getConceptService().getConcept(conceptId);
			if (concept != null)
				sb.append(concept.getName().getName());
			
			if (obsValue != null)
				sb.append(obsValue.getValueAsString(Context.getLocale()));
			
			if (userId != null)
				user = Context.getUserService().getUser(userId);
			if (user != null)
				sb.append(user.getPersonName());
			
			pageContext.getOut().write(sb.toString());
		}
		catch (IOException e) {
			log.error("Failed to write to pageContext.getOut()", e);
		}
		return SKIP_BODY;
	}
	
	public int doEndTag() {
		reset();
		return EVAL_PAGE;
	}
	
	private void reset() {
		conceptId = null;
		concept = null;
		obsValue = null;
	}
	
	public Integer getConceptId() {
		return conceptId;
	}
	
	public void setConceptId(Integer conceptId) {
		this.conceptId = conceptId;
	}
	
	public Concept getConcept() {
		return concept;
	}
	
	public void setConcept(Concept concept) {
		this.concept = concept;
	}
	
	public Obs getObsValue() {
		return obsValue;
	}
	
	public void setObsValue(Obs obsValue) {
		this.obsValue = obsValue;
	}
	
	public Integer getUserId() {
		return userId;
	}
	
	public void setUserId(Integer userId) {
		this.userId = userId;
	}
	
	public User getUser() {
		return user;
	}
	
	public void setUser(User user) {
		this.user = user;
	}
	
}
