package org.openmrs.web.taglib;

import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.UserContext;

public class PrivilegeTag extends TagSupport {

	public static final long serialVersionUID = 11233L;
	
	private final Log log = LogFactory.getLog(getClass());

	private String privilege;
	private String inverse;

	public int doStartTag() {

		UserContext userContext = Context.getUserContext();
		
		log.debug("Checking user " + userContext.getAuthenticatedUser() + " for privs " + privilege);
		
		boolean hasPrivilege = false;
		if (privilege.contains(",")) {
			String[] privs = privilege.split(",");
			for (String p : privs) {
				if (userContext.hasPrivilege(p)) {
					hasPrivilege = true;
					break;
				}
			}
		}
		else {
			hasPrivilege = userContext.hasPrivilege(privilege);
		}

		// allow inversing
		boolean isInverted = false;
		if ( inverse != null ) isInverted = "true".equals(inverse.toLowerCase());
				
		if ( (hasPrivilege && !isInverted) || (!hasPrivilege && isInverted)) {
			pageContext.setAttribute("authenticatedUser", userContext.getAuthenticatedUser());
			return EVAL_BODY_INCLUDE;
		}
		else {
			return SKIP_BODY;
		}
	}
	
	/**
	 * @return Returns the privilege.
	 */
	public String getPrivilege() {
		return privilege;
	}

	/**
	 * @param converse The privilege to set.
	 */
	public void setPrivilege(String privilege) {
		this.privilege = privilege;
	}

	/**
	 * @return Returns the inverse.
	 */
	public String getInverse() {
		return inverse;
	}

	/**
	 * @param inverse The inverse to set.
	 */
	public void setInverse(String inverse) {
		this.inverse = inverse;
	}
}
