/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.taglib.fieldgen;

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.web.taglib.FieldGenTag;
import org.openmrs.web.taglib.HtmlIncludeTag;

public abstract class AbstractFieldGenHandler implements FieldGenHandler {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	protected FieldGenTag fieldGenTag;
	
	public void setFieldGenTag(FieldGenTag fieldGenTag) {
		this.fieldGenTag = fieldGenTag;
	}
	
	protected void setParameter(String s, Object o) {
		if (this.fieldGenTag != null) {
			//HashMap<String,Object> hmParams = (HashMap<String,Object>)getRequest().getAttribute("org.openmrs.fieldGen.parameterMap");
			HashMap<String, Object> hmParams = (HashMap<String, Object>) this.fieldGenTag.getParameterMap();
			if (hmParams == null) {
				hmParams = new HashMap<String, Object>();
			}
			hmParams.put(s, o);
			this.fieldGenTag.setParameterMap(hmParams);
		}
	}
	
	protected void setUrl(String s) {
		if (this.fieldGenTag != null) {
			this.fieldGenTag.setUrl(s);
		}
	}
	
	protected void setVal(Object o) {
		if (this.fieldGenTag != null) {
			this.fieldGenTag.setVal(o);
		}
	}
	
	protected void checkEmptyVal(Object o) {
		if (this.fieldGenTag.getVal() != null && this.fieldGenTag.getVal() instanceof String && !(o instanceof String)
		        && "".equals(this.fieldGenTag.getVal())) {
			setVal(o);
		}
	}
	
	protected void htmlInclude(String fileToInclude) {
		HtmlIncludeTag hit = new HtmlIncludeTag();
		hit.setPageContext(this.fieldGenTag.getPageContext());
		hit.setFile(fileToInclude);
		try {
			hit.doStartTag();
		}
		catch (JspException e) {
			log.error("Unable to execute doStartTag() method of HtmlIncludeTag from FieldGenHandler");
		}
	}
	
	protected HttpServletRequest getRequest() {
		if (this.fieldGenTag != null) {
			//HttpSession session = this.fieldGenTag.getPageContext().getSession();
			return (HttpServletRequest) this.fieldGenTag.getPageContext().getRequest();
			//return (Context)session.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		} else {
			return null;
		}
	}
}
