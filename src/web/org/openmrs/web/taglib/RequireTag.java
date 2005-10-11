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

public class RequireTag extends TagSupport {

	public static final long serialVersionUID = 1L;
	
	private final Log log = LogFactory.getLog(getClass());

	private String privilege;
	private String otherwise;
	private String msg = "";
	public int doStartTag() {
		
		HttpServletResponse httpResponse = (HttpServletResponse)pageContext.getResponse();
		HttpSession httpSession = pageContext.getSession();
		HttpServletRequest request = (HttpServletRequest)pageContext.getRequest();
		
		Context context = (Context)httpSession.getAttribute(Constants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		if (context == null && privilege != null) {
			log.error("context is unavailable");
			httpSession.removeAttribute(Constants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
			//TODO find correct error to throw 
			throw new APIException("The Context is currently unavailable (null)");
		}
		
		if (!context.isAuthenticated())
			msg = "You must log in to continue";
		else if (!context.hasPrivilege(privilege))
			msg = "You are not authorized to view this page";
		
		if (msg != "") {
			httpSession.setAttribute("openmrs_msg", msg);
			httpSession.setAttribute("login_redirect", request.getContextPath() + request.getServletPath());
			try {
				httpResponse.sendRedirect(request.getContextPath() + otherwise);
			}
			catch (IOException e) {
				// cannot redirect
				throw new APIException(e.getMessage());
			}
		}
		
		return SKIP_BODY;
	}

	public int doEndTag() {
		if ( msg == "")
			return EVAL_PAGE;
		else
			return SKIP_PAGE;
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
