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
import org.openmrs.web.Constants;

public class GetContextTag extends TagSupport {

	public static final long serialVersionUID = 1L;
	
	private final Log log = LogFactory.getLog(getClass());

	private String var = "context";

	public int doStartTag() {
		
		HttpServletResponse httpResponse = (HttpServletResponse)pageContext.getResponse();
		HttpSession httpSession = pageContext.getSession();
		HttpServletRequest request = (HttpServletRequest)pageContext.getRequest();
		
		Context context = (Context)httpSession.getAttribute(Constants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		if (context == null) {
			log.error("context is unavailable");
			httpSession.removeAttribute(Constants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
			//TODO find correct error to throw 
			throw new APIException("The Context is currently unavailable (null)");
		}
		
		if (!context.isAuthenticated()) {
			httpSession.setAttribute(Constants.OPENMRS_MSG_ATTR, "You must log in to continue");
			httpSession.setAttribute("login_redirect", request.getContextPath() + request.getServletPath());
			try {
				httpResponse.sendRedirect(request.getContextPath() + "/logout");
			}
			catch (IOException e) {
				// cannot redirect
				throw new APIException(e.getMessage());
			}
		}

		pageContext.setAttribute(getVar(), context);
		
		return SKIP_BODY;
	}
	
	public String getVar() {
		return var;
	}

	public void setVar(String var) {
		this.var = var;
	}

}
