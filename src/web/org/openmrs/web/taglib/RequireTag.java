package org.openmrs.web.taglib;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.tagext.TagSupport;

public class RequireTag extends TagSupport {

	public static final long serialVersionUID = 1L;

	private String privilege;
	private String otherwise;

	public int doStartTag() {

		// @to.do implement true authorization
//		HttpServletResponse httpResponse = (HttpServletResponse)pageContext.getResponse();
		HttpSession httpSession = pageContext.getSession();
		
		httpSession.getAttribute("__openmrs_context");
		if (privilege == null || !privilege.equals("FormEntry User")) {
			try {
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
