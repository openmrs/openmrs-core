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

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.GlobalProperty;
import org.openmrs.api.context.Context;
import org.openmrs.web.WebConstants;

/**
 * Retrieves the matching global properties, and if any are missing, redirects to configuration
 * page. This tag should be placed before the header import tag.
 */
public class RequireConfigurationByPrefixTag extends TagSupport {
	
	private static final long serialVersionUID = 5958840687498314711L;
	
	private final Log log = LogFactory.getLog(getClass());
	
	private String propertyPrefix;
	
	private String ignoreList;
	
	private String configurationPage;
	
	/**
	 * @see javax.servlet.jsp.tagext.TagSupport#doStartTag()
	 */
	public int doStartTag() throws JspException {
		
		Set<String> propsToIgnore = new HashSet<String>();
		if (ignoreList != null) {
			for (String s : ignoreList.split(",")) {
				s = s.trim();
				if (s.length() != 0) {
					propsToIgnore.add(propertyPrefix + s);
				}
			}
		}
		
		HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
		HttpServletResponse response = (HttpServletResponse) pageContext.getResponse();
		
		for (GlobalProperty prop : Context.getAdministrationService().getGlobalPropertiesByPrefix(propertyPrefix)) {
			if (prop != null && StringUtils.isBlank(prop.getPropertyValue()) && !propsToIgnore.contains(prop.getProperty())) {
				
				pageContext.getSession().setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "error.configurationRequired");
				try {
					log.info("Configuration not complete, missing property (" + prop.getProperty()
					        + ").  Redirecting to page: " + request.getContextPath() + configurationPage);
					response.sendRedirect(request.getContextPath() + configurationPage);
					return SKIP_PAGE;
				}
				catch (IllegalStateException ise) {
					log.warn("Unable to forward request.  It is likely that a response was already committed. ", ise);
				}
				catch (IOException e) {
					log.error("An error occurred in tag", e);
					throw new JspException(e);
				}
			}
		}
		return SKIP_BODY;
	}
	
	public String getPropertyPrefix() {
		return propertyPrefix;
	}
	
	public void setPropertyPrefix(String propertyPrefix) {
		this.propertyPrefix = propertyPrefix;
	}
	
	public String getIgnoreList() {
		return ignoreList;
	}
	
	public void setIgnoreList(String ignoreList) {
		this.ignoreList = ignoreList;
	}
	
	public String getConfigurationPage() {
		return configurationPage;
	}
	
	public void setConfigurationPage(String configurationPage) {
		this.configurationPage = configurationPage;
	}
}
