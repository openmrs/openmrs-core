package org.openmrs.web.taglib.fieldgen;

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.openmrs.api.context.Context;
import org.openmrs.web.WebConstants;
import org.openmrs.web.taglib.FieldGenTag;

public abstract class AbstractFieldGenHandler {
	protected FieldGenTag fieldGenTag;
	
	public void setFieldGenTag(FieldGenTag fieldGenTag) {
		this.fieldGenTag = fieldGenTag;
	}
	
	protected void setParameter(String s, Object o) {
		if ( this.fieldGenTag != null ) {
			//HashMap<String,Object> hmParams = (HashMap<String,Object>)getRequest().getAttribute("org.openmrs.fieldGen.parameterMap");
			HashMap<String,Object> hmParams = (HashMap<String, Object>) this.fieldGenTag.getParameterMap();
			if ( hmParams == null ) hmParams = new HashMap<String,Object>();
			hmParams.put(s, o);
			this.fieldGenTag.setParameterMap(hmParams);
		}
	}
	
	protected void setUrl(String s) {
		if ( this.fieldGenTag != null ) {
			this.fieldGenTag.setUrl(s);
		}
	}
	
	protected void setVal(Object o) {
		if ( this.fieldGenTag != null ) {
			this.fieldGenTag.setVal(o);
		}
	}

	protected void checkEmptyVal(Object o) {
		if ( this.fieldGenTag.getVal() != null ) {
			if ( this.fieldGenTag.getVal() instanceof String && !(o instanceof String)) {
				if ( "".equals(this.fieldGenTag.getVal()) ) {
					setVal(o);
				}
			}
		}
	}
	
	protected Context getContext() {
		if ( this.fieldGenTag != null ) {
			HttpSession session = this.fieldGenTag.getPageContext().getSession();
			//HttpServletRequest request = (HttpServletRequest)this.fieldGenTag.getPageContext().getRequest();
			return (Context)session.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		} else return null;
	}

	protected HttpServletRequest getRequest() {
		if ( this.fieldGenTag != null ) {
			//HttpSession session = this.fieldGenTag.getPageContext().getSession();
			return (HttpServletRequest)this.fieldGenTag.getPageContext().getRequest();
			//return (Context)session.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		} else return null;
	}
}
