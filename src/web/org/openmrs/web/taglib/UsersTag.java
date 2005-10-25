package org.openmrs.web.taglib;

import java.util.Iterator;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.UserService;
import org.openmrs.User;
import org.openmrs.context.Context;
import org.openmrs.web.Constants;

public class UsersTag extends BodyTagSupport {

	public static final long serialVersionUID = 1L;
	
	private final Log log = LogFactory.getLog(getClass());

	private String var = "user";
	private String role;
	private Iterator users;

	public int doStartTag() {
		
		users = null;
		
		Context context = (Context)pageContext.getSession().getAttribute(Constants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		UserService us = context.getUserService();
		
		if (getRole() == null || getRole().equals("")) {
			users = us.getUsers().iterator();
		}
		else {
			users = us.getUsersByRole(us.getRole(getRole())).iterator();
		}
		
		if (users == null)
			return SKIP_BODY;
		else
			return EVAL_BODY_BUFFERED;
		
	}

	/**
	 * @see javax.servlet.jsp.tagext.BodyTag#doInitBody()
	 */
	public void doInitBody() throws JspException {
		if (users.hasNext()) {
			User user = (User)users.next();
			pageContext.setAttribute(getVar(), user);
		}
	}

	/**
	 * @see javax.servlet.jsp.tagext.IterationTag#doAfterBody()
	 */
	public int doAfterBody() throws JspException {
        if(users.hasNext()) {
			User user = (User)users.next();
			pageContext.setAttribute(getVar(), user);
            return EVAL_BODY_BUFFERED;
        }
        else
            return SKIP_BODY;
	}

	/**
	 * @see javax.servlet.jsp.tagext.Tag#doEndTag()
	 */
	public int doEndTag() throws JspException {
		try
        {
            if(bodyContent != null)
            	bodyContent.writeOut(bodyContent.getEnclosingWriter());
        }
        catch(java.io.IOException e)
        {
            throw new JspTagException("IO Error: " + e.getMessage());
        }
        return EVAL_PAGE;
	}

	public String getVar() {
		return var;
	}

	public void setVar(String var) {
		this.var = var;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}
}
