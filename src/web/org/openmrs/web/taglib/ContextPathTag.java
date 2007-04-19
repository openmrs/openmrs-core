package org.openmrs.web.taglib;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * Prints the contextPath for the current webapp.
 * Typically you can get this via ${pageContext.request.contextPath}
 * 
 * @author Ben Wolfe
 */
public class ContextPathTag extends TagSupport {
	
	private static final long serialVersionUID = 112734331123332322L;
	
	//private final Log log = LogFactory.getLog(getClass());
	
	public int doStartTag() {
		HttpServletRequest request = (HttpServletRequest)pageContext.getRequest();

		try {
			pageContext.getOut().write(request.getContextPath());
		} catch (IOException e) {
			// pass
		}
		return SKIP_BODY;
	}
	
	public int doEndTag() {
		return EVAL_PAGE;
	}

}
