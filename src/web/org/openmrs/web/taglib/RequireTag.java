package org.openmrs.web.taglib;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.APIException;
import org.openmrs.context.Context;

public class RequireTag extends TagSupport {

	public static final long serialVersionUID = 1L;
	
	private final Log log = LogFactory.getLog(getClass());

	private String privilege;
	private String otherwise;

	public int doStartTag() {

		// TODO implement true authorization
//		HttpServletResponse httpResponse = (HttpServletResponse)pageContext.getResponse();
		HttpSession httpSession = pageContext.getSession();
		HttpServletRequest request = (HttpServletRequest)pageContext.getRequest();
		
		Context context = (Context)httpSession.getAttribute("__openmrs_context");
		if (context == null && privilege != null) {
			log.error("context is unavailable");
			//TODO find correct error to throw 
			throw new APIException("The Context is currently unavailable");
		}
		if (privilege == null || !context.isAuthenticated() || !context.hasPrivilege(privilege)) {
			try {
				String redirect = request.getContextPath() + request.getServletPath();
				httpSession.setAttribute("login_redirect", redirect);
				((HttpServletResponse) pageContext.getResponse())
						.sendRedirect(otherwise);
			} catch (IOException e) {
				// Failed to redirect, not much we can do about it here
			}
		}

		return SKIP_BODY;
	}

	public String getPrivilege() {
		return privilege;
	}

	public void setPrivilege(String privilege) {
		this.privilege = privilege;
	}

	public String getOtherwise() {
		return otherwise;
	}

	public void setOtherwise(String otherwise) {
		this.otherwise = otherwise;
	}

}
