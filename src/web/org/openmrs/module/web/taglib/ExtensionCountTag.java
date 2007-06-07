package org.openmrs.module.web.taglib;

import java.io.IOException;
import java.util.List;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.Extension;
import org.openmrs.module.ModuleFactory;

public class ExtensionCountTag extends TagSupport {

	private static final long serialVersionUID = 1L;
	private final Log log = LogFactory.getLog(getClass());
	private String pointId;
	private String var;

	public int doStartTag() throws JspException {
		log.debug("Starting tag for extension count: " + pointId);
		log.debug("Getting extensions: " + pointId);
		List<Extension> extensionList = ModuleFactory.getExtensions(pointId, Extension.MEDIA_TYPE.html);
		log.debug("extensionList = " + extensionList);
		int count = extensionList == null ? 0 : extensionList.size();
		if (var == null)
			try {
				pageContext.getOut().print(count);
			} catch (IOException ex) {
				log.error("IOException printing count to pageContext.getOut()", ex);
			}
		else
			pageContext.setAttribute(var, count);
		return SKIP_BODY;
	}
	
	public int doEndTag() {
		this.pointId = null;
		this.var = null;
		return SKIP_BODY;
	}	
	
	public ExtensionCountTag() { }

	public String getPointId() {
		return pointId;
	}

	public void setPointId(String pointId) {
		this.pointId = pointId;
	}

	public String getVar() {
		return var;
	}

	public void setVar(String var) {
		this.var = var;
	}
	
}
