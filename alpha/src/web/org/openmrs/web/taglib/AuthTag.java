package org.openmrs.web.taglib;

import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;

public class AuthTag extends TagSupport {

	public static final long serialVersionUID = 11233L;
	
	private final Log log = LogFactory.getLog(getClass());
	
	public int doStartTag() {
		log.debug("setting authenticatedUser with value: " + Context.getAuthenticatedUser());
		
		// sets a null value when not authenticated
		pageContext.setAttribute("authenticatedUser", Context.getAuthenticatedUser());
		
		return EVAL_BODY_INCLUDE;
	}
	
}
