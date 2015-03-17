/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.web.taglib;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.Extension;
import org.openmrs.module.ModuleFactory;
import org.openmrs.module.web.extension.LinkProviderExtension;
import org.openmrs.module.web.extension.provider.Link;
import org.openmrs.util.OpenmrsUtil;
import org.springframework.web.servlet.support.RequestContext;

/**
 *
 */
public class ExtensionPopupMenuTag extends TagSupport {
	
	private static final long serialVersionUID = 1L;
	
	private final Log log = LogFactory.getLog(getClass());
	
	private String label;
	
	private String pointId;
	
	private String popupDivId;
	
	private String position;
	
	private String parameters;
	
	private Boolean showLabelIfNoExtensions;
	
	public ExtensionPopupMenuTag() {
	}
	
	private String randomString() {
		Random gen = new Random();
		return "" + (1000000 * gen.nextInt());
	}
	
	public int doStartTag() throws JspException {
		if (showLabelIfNoExtensions == null) {
			showLabelIfNoExtensions = true;
		}
		// using this as we'd use MessageSourceAccessor
		RequestContext context = new RequestContext((HttpServletRequest) this.pageContext.getRequest());
		
		boolean below = !"above".equals(position);
		Map<String, String> parameters = new HashMap<String, String>();
		if (this.parameters != null) {
			parameters.putAll(OpenmrsUtil.parseParameterList(this.parameters));
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append("<span style=\"position: relative\">");
		if (below) {
			sb
			        .append("<div id=\""
			                + popupDivId
			                + "\" style=\"width: 35em; border: 1px solid black; background-color: #f0f0a0; position: absolute; top: 0px; padding-right: 1.2em; z-index: 1; display: none\">");
		} else {
			sb
			        .append("<div id=\""
			                + popupDivId
			                + "\" style=\"width: 35em; border: 1px solid black; background-color: #f0f0a0; position: absolute; bottom: 0px; padding-right: 1.2em; z-index: 1; display: none\">");
		}
		
		sb.append("<div style=\"float: right\"><a href=\"javascript:hideLayer('" + popupDivId + "');\" >["
		        + context.getMessage("general.close") + "]</a></div>");
		sb.append("<ul>");
		boolean anyExtensionsFound = false;
		List<Extension> extensions = ModuleFactory.getExtensions(pointId, Extension.MEDIA_TYPE.html);
		for (Extension e : extensions) {
			if (e instanceof LinkProviderExtension) {
				anyExtensionsFound = true;
				LinkProviderExtension extension = (LinkProviderExtension) e;
				List<Link> links = extension.getLinks();
				log.debug("extension of class " + e.getClass() + " provides " + links.size() + " links");
				for (Link link : links) {
					String url = link.getUrl();
					log.debug("url = " + url);
					StringBuilder hiddenVars = new StringBuilder();
					Map<String, String> javascriptSubstitutions = new HashMap<String, String>();
					for (Map.Entry<String, String> entry : link.getQueryParameters().entrySet()) {
						hiddenVars.append("<input type=\"hidden\" name=\"" + entry.getKey() + "\" value=\""
						        + entry.getValue() + "\"/>\n");
					}
					for (Map.Entry<String, String> entry : parameters.entrySet()) {
						hiddenVars.append("<input type=\"hidden\" name=\"" + entry.getKey() + "\" ");
						if (entry.getValue().startsWith("javascript:")) {
							String function = entry.getValue();
							function = function.substring(function.indexOf(":") + 1);
							String random = randomString();
							javascriptSubstitutions.put(random, function);
							hiddenVars.append("id=\"" + random + "\" value=\"\"");
						} else {
							hiddenVars.append("value=\"" + entry.getValue() + "\"");
						}
						hiddenVars.append("/>\n");
					}
					String formId = randomString();
					
					StringBuilder onClick = new StringBuilder();
					if (javascriptSubstitutions.size() > 0) {
						onClick.append(" var _popup_tmp = ''; ");
					}
					for (Map.Entry<String, String> entry : javascriptSubstitutions.entrySet()) {
						String id = entry.getKey();
						String function = entry.getValue();
						onClick.append(" _popup_tmp = " + function
						        + "; if (_popup_tmp == null) return; document.getElementById('" + id
						        + "').value = _popup_tmp; ");
					}
					onClick.append("document.getElementById('" + formId + "').submit();");
					
					sb.append("<li>");
					sb.append("<form id=\"" + formId + "\" method=\"post\" action=\"" + url + "\">\n");
					sb.append(hiddenVars);
					sb.append("\n<a href=\"#\" onClick=\"javascript:" + onClick + "\">"
					        + context.getMessage(link.getLabel(), link.getLabel()) + "</a>");
					if (link.getDescription() != null) {
						sb.append("<br/><small>" + context.getMessage(link.getDescription(), link.getDescription())
						        + "</small>");
					}
					sb.append("</form>");
					sb.append("</li>");
				}
			}
		}
		if (!anyExtensionsFound) {
			sb.append("<li>" + context.getMessage("general.none") + "</li>");
		}
		
		sb.append("</ul>");
		sb.append("</div>");
		sb.append("</span>");
		sb.append("<a href=\"#\" onClick=\"toggleLayer('" + popupDivId + "')\" style=\"border: 1px black solid\">"
		        + context.getMessage(label, label) + "</a>");
		
		try {
			if (anyExtensionsFound || showLabelIfNoExtensions) {
				pageContext.getOut().print(sb);
			}
		}
		catch (IOException ex) {
			throw new JspException(ex);
		}
		
		resetValues();
		return SKIP_BODY;
	}
	
	private void resetValues() {
		pointId = null;
		popupDivId = null;
		label = null;
		position = null;
		parameters = null;
		showLabelIfNoExtensions = null;
	}
	
	public String getPointId() {
		return pointId;
	}
	
	public void setPointId(String pointId) {
		this.pointId = pointId;
	}
	
	public String getPopupDivId() {
		return popupDivId;
	}
	
	public void setPopupDivId(String divId) {
		this.popupDivId = divId;
	}
	
	public String getLabel() {
		return label;
	}
	
	public void setLabel(String label) {
		this.label = label;
	}
	
	public String getPosition() {
		return position;
	}
	
	public void setPosition(String position) {
		this.position = position;
	}
	
	public String getParameters() {
		return parameters;
	}
	
	public void setParameters(String parameters) {
		this.parameters = parameters;
	}
	
	public Boolean getShowLabelIfNoExtensions() {
		return showLabelIfNoExtensions;
	}
	
	public void setShowLabelIfNoExtensions(Boolean showLabelIfNoExtensions) {
		this.showLabelIfNoExtensions = showLabelIfNoExtensions;
	}
	
}
