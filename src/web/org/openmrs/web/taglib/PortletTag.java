package org.openmrs.web.taglib;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.taglibs.standard.tag.common.core.ImportSupport;
import org.openmrs.util.Helper;

public class PortletTag extends ImportSupport {

	public static final long serialVersionUID = 21L;
	
	private final Log log = LogFactory.getLog(getClass());

	private String size = "";
	private String id = "";
	private String parameters = "";
	
	public PageContext getPageContext() {
		return this.pageContext;
	}
	
	public int doStartTag() throws JspException {
		if (url == null) url = "";
		if (id == null) id = "";
		
		try {
			if (url.equals(""))
				pageContext.getOut().print("Every portlet must be defined with a URI");
			else {
				// all portlets are contained in the /WEB-INF/view/portlets/ folder and end with .portlet
				if (!url.endsWith("portlet"))
					url += ".portlet";
				url = "/portlets/" + url;
				
				// opening portlet tag
				pageContext.getOut().print("<div class='portlet' id='" + id + "'>");
				
				// add attrs to request so that the controller (and portlet) can see/use them
				pageContext.getRequest().setAttribute("org.openmrs.portlet.size", size);
				pageContext.getRequest().setAttribute("org.openmrs.portlet.parameters", Helper.parseParameterList(parameters));
				
			}
		}
		catch (IOException e) {
			log.error("Error while starting portlet tag", e);
		}
		
		return super.doStartTag();
	}

	public int doEndTag() throws JspException {
		
		try {
			// closing portlet tag
			pageContext.getOut().print("</div>");
		}
		catch (IOException e) {
			log.error("Error while closing portlet tag", e);
		}
		
		resetValues();
		
		return super.doEndTag();
	}
	
	private void resetValues() {
		id = parameters = "";
	}

	public void setUrl(String url) throws JspTagException {
		this.url = url;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getParameters() {
		return parameters;
	}

	public void setParameters(String parameters) {
		this.parameters = parameters;
	}

	public String getSize() {
		return size;
	}

	public void setSize(String size) {
		this.size = size;
	}
	
}