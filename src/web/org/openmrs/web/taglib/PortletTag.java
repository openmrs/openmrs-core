package org.openmrs.web.taglib;

import java.io.IOException;
import java.util.Map;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.taglibs.standard.tag.common.core.ImportSupport;
import org.openmrs.module.Module;
import org.openmrs.module.ModuleFactory;
import org.openmrs.util.OpenmrsUtil;

public class PortletTag extends ImportSupport {
	
	public static final long serialVersionUID = 21L;
	
	private final Log log = LogFactory.getLog(getClass());

	private String size = "";
	private String id = "";
	private String parameters = "";
	private Map<String, Object> parameterMap = null;
	private Integer patientId = null;
	private Integer personId = null;
	private Integer encounterId = null;
	private Integer userId = null;
	private String patientIds = "";
	private String moduleId = "";

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
				
				// module specific portlets are in /WEB-INF/view/module/*/portlets/
				if (moduleId != null && moduleId.length() > 0) {
					Module mod = ModuleFactory.getModuleById(moduleId);
					if (mod == null)
						log.warn("no module found with id: " + moduleId);
					else
						url = "/module/" + moduleId + "/portlets/" + url;	
				}
				else
					url = "/portlets/" + url;
				
				// opening portlet tag
				if (moduleId != null && moduleId.length() > 0)
					pageContext.getOut().print("<div class='portlet' id='" + moduleId + "." + id + "'>");
				else
					pageContext.getOut().print("<div class='portlet' id='" + id + "'>");
				
				// add attrs to request so that the controller (and portlet) can see/use them
				pageContext.getRequest().setAttribute("org.openmrs.portlet.id", id);
				pageContext.getRequest().setAttribute("org.openmrs.portlet.size", size);
				pageContext.getRequest().setAttribute("org.openmrs.portlet.parameters", OpenmrsUtil.parseParameterList(parameters));
				pageContext.getRequest().setAttribute("org.openmrs.portlet.patientId", patientId);
				pageContext.getRequest().setAttribute("org.openmrs.portlet.personId", personId);
				pageContext.getRequest().setAttribute("org.openmrs.portlet.encounterId", encounterId);
				pageContext.getRequest().setAttribute("org.openmrs.portlet.userId", userId);
				pageContext.getRequest().setAttribute("org.openmrs.portlet.patientIds", patientIds);
				pageContext.getRequest().setAttribute("org.openmrs.portlet.parameterMap", parameterMap);
			}
		}
		catch (IOException e) {
			log.error("Error while starting portlet tag", e);
		}

		return super.doStartTag();
	}

	public int doEndTag() throws JspException {
		
		int i = super.doEndTag();
		
		try {
			// closing portlet tag
			pageContext.getOut().print("</div>");
		}
		catch (IOException e) {
			log.error("Error while closing portlet tag", e);
		}
		
		resetValues();
		
		return i;
	}
	
	private void resetValues() {
		id = parameters = patientIds = moduleId = "";
		personId = patientId = encounterId = userId = null;
		parameterMap = null;
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
	
	public Integer getEncounterId() {
		return encounterId;
	}

	public void setEncounterId(Integer encounterId) {
		this.encounterId = encounterId;
	}

	public Integer getPatientId() {
		return patientId;
	}

	public void setPatientId(Integer patientId) {
		this.patientId = patientId;
	}

	public Integer getPersonId() {
    	return personId;
    }

	public void setPersonId(Integer personId) {
    	this.personId = personId;
    }

	public String getPatientIds() {
		return patientIds;
	}

	public void setPatientIds(String patientIds) {
		this.patientIds = patientIds;
	}

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public Map<String, Object> getParameterMap() {
		return parameterMap;
	}

	public void setParameterMap(Map<String, Object> parameterMap) {
		this.parameterMap = parameterMap;
	}
	
	public String getModuleId() {
		return moduleId;
	}

	public void setModuleId(String moduleId) {
		this.moduleId = moduleId;
	}
	
}