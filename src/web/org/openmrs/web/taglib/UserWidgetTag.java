package org.openmrs.web.taglib;

import java.io.IOException;
import java.util.Locale;

import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.web.WebConstants;

public class UserWidgetTag extends TagSupport {

	public static final long serialVersionUID = 1L;
	private final Log log = LogFactory.getLog(getClass());
	
	private Integer userId;
	private String size = "normal";
	
	public UserWidgetTag() { }

	public void setSize(String size) {
		this.size = size;
	}
	
	public void setUserId(Integer userId) {
		this.userId = userId;
	}
	
	public int doStartTag() {
		Context context = (Context) pageContext.getSession().getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		User user = context.getUserService().getUser(userId);
		
		try {
			JspWriter w = pageContext.getOut();
			w.print(user.getFirstName() + " " + user.getMiddleName() + " " + user.getLastName());
			if ("full".equals(size)) {
				w.print(" <i>(" + user.getUsername() + ")</i>");
			}
		} catch (IOException ex) {
			log.error("Error while starting userWidget tag", ex);
		}
		return SKIP_BODY;
	}
	
	public int doEndTag() {
		userId = null;
		size = "normal";
		return EVAL_PAGE;
	}
	
}
