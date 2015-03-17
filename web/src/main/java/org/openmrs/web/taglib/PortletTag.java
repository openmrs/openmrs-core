/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.taglib;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.taglibs.standard.tag.common.core.ImportSupport;
import org.openmrs.module.Module;
import org.openmrs.module.ModuleFactory;
import org.openmrs.util.OpenmrsUtil;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.PageContext;
import java.io.IOException;
import java.util.Map;

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
		
		if (url == null) {
			log.warn("URL came through as NULL to PortletTag - this is a big problem");
			url = "";
		}
		if (id == null) {
			id = "";
		}
		
		try {
			if ("".equals(url)) {
				pageContext.getOut().print("Every portlet must be defined with a URI");
			} else {
				url = generatePortletUrl(url, moduleId);
				
				// opening portlet tag
				if (moduleId != null && moduleId.length() > 0) {
					pageContext.getOut().print("<div class='portlet' id='" + moduleId + "." + id + "'>");
				} else {
					pageContext.getOut().print("<div class='portlet' id='" + id + "'>");
				}
				
				// add attrs to request so that the controller (and portlet) can see/use them
				pageContext.getRequest().setAttribute("org.openmrs.portlet.id", id);
				pageContext.getRequest().setAttribute("org.openmrs.portlet.size", size);
				pageContext.getRequest().setAttribute("org.openmrs.portlet.parameters",
				    OpenmrsUtil.parseParameterList(parameters));
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
		id = "";
		parameters = "";
		patientIds = "";
		moduleId = "";
		personId = null;
		patientId = null;
		encounterId = null;
		userId = null;
		parameterMap = null;
	}
	
	/**
	 * Generates the portlet url.
	 * <ul><li>Core portlets are expected to be in the /WEB-INF/view/portlets/ folder.</li>
	 * <li>Module portlets are expected to be in the /WEB-INF/view/module/{@code moduleId}/portlets/ folder.</li></ul>
	 * @param portletUrl The portlet url.
	 * @param moduleId The optional portlet module id.
	 * @return The url for the portlet.
	 * @should return the correct url for a core portlet
	 * @should return the correct url for a module portlet
	 * @should replace period in a module id with a forward slash when building a module portlet url
	 * @should not update the moduleId field for a module portlet
	 * @should return a core portlet url when the specified module cannot be found
	 * @should append .portlet to the url if not specified
	 * @should treat both an empty and null module id as core portlets
	 */
	protected String generatePortletUrl(String portletUrl, String moduleId) {
		String result = null;
		
		// all portlets must end with .portlet
		if (!portletUrl.endsWith(".portlet")) {
			portletUrl += ".portlet";
		}
		
		// module specific portlets are in /WEB-INF/view/module/*/portlets/
		if (moduleId != null && moduleId.length() > 0) {
			Module mod = ModuleFactory.getModuleById(moduleId);
			if (mod == null) {
				// Could not find the module, the standard portlet url will be used
				log.warn("no module found with id: " + moduleId);
			} else {
				if (moduleId.contains(".")) {
					// Module Id's that contain a '.' result in the module being in a sub-folder
					moduleId = moduleId.replace(".", "/");
				}
				
				result = "/module/" + moduleId + "/portlets/" + portletUrl;
			}
		}
		
		// If the resulting url has not already been defined then use the standard portlet url scheme
		if (result == null) {
			//Core portlets are contained in the /WEB-INF/view/portlets/ folder
			result = "/portlets/" + portletUrl;
		}
		
		return result;
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
