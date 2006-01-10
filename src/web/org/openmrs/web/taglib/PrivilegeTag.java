package org.openmrs.web.taglib;

import javax.servlet.http.HttpSession;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.web.WebConstants;

public class PrivilegeTag extends TagSupport {

	public static final long serialVersionUID = 11233L;
	
	private final Log log = LogFactory.getLog(getClass());

	private String privilege;
	private boolean converse;

	public int doStartTag() {

		HttpSession httpSession = pageContext.getSession();
		
		Context context = (Context)httpSession.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		
		// must have an active context to be able to authenticate
		if (context == null) {
			return SKIP_BODY;
		}
		
		// must have authenticated to have a privilege
		if (!context.isAuthenticated()) {
			return SKIP_BODY;
		}
		
		if (converse ^= context.getAuthenticatedUser().hasPrivilege(privilege)) {
			pageContext.setAttribute("authenticatedUser", context.getAuthenticatedUser());
			return EVAL_BODY_INCLUDE;
		}
		else {
			return SKIP_BODY;
		}
	}
	
	/**
	 * @return Returns the converse.
	 */
	public String getPrivilege() {
		return privilege;
	}

	/**
	 * @param converse The converse to set.
	 */
	public void setPrivilege(String privilege) {
		this.privilege = privilege;
	}

	/**
	 * @return Returns the converse.
	 */
	public boolean isConverse() {
		return converse;
	}

	/**
	 * @param converse The converse to set.
	 */
	public void setConverse(boolean converse) {
		this.converse = converse;
	}
}
