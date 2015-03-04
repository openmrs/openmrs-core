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
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.web.WebConstants;

/**
 * Lets you conveniently include js and css resources in your jsp pages and fragments. If this tag
 * is used to include the same file more than once in different page fragments (e.g. header,
 * portlets) then it will silently include the file just once. Also, this tag will silently replace
 * certain resources with others (e.g. jquery-1.3.2.min.js maps to jquery.min.js). See
 * openmrs_static_content-servlet.xml for example usage and to see what core resources are remapped.
 */
public class HtmlIncludeTag extends TagSupport {
	
	public static final long serialVersionUID = 13472382823L;
	
	private final Log log = LogFactory.getLog(getClass());
	
	private static final String POSSIBLE_TYPES_JS = ".js,javascript,jscript";
	
	private static final String POSSIBLE_TYPES_CSS = ".css,style,stylesheet";
	
	public static final String OPENMRS_HTML_INCLUDE_REQUEST_ID_KEY = "org.openmrs.htmlInclude.pageName";
	
	public static final String OPENMRS_HTML_INCLUDE_MAP_KEY = "org.openmrs.htmlInclude.includeMap";
	
	public static final Map<String, String> rewrites = new HashMap<String, String>();
	
	private String type;
	
	private String file;
	
	/**
	 * If true, will append &locale=en_US to the url for browser caching purposes Should be used on
	 * files that contain spring message calls and should not be cached across locales
	 *
	 * @since 1.8
	 */
	private boolean appendLocale;
	
	public synchronized void setRewrites(Map<String, String> rules) {
		rewrites.putAll(rules);
	}
	
	@Override
	public synchronized int doStartTag() throws JspException {
		log.debug("\n\n");
		
		if (rewrites.containsKey(file)) {
			file = rewrites.get(file);
		}
		
		// see if this is a JS or CSS file
		boolean isJs = false;
		boolean isCss = false;
		
		String fileExt = file.substring(file.lastIndexOf("."));
		
		if (this.type != null && this.type.length() > 0) {
			if (HtmlIncludeTag.POSSIBLE_TYPES_CSS.indexOf(type) >= 0) {
				isCss = true;
			} else if (HtmlIncludeTag.POSSIBLE_TYPES_JS.indexOf(type) >= 0) {
				isJs = true;
			}
		}
		
		if (!isCss && !isJs && fileExt.length() > 0) {
			if (HtmlIncludeTag.POSSIBLE_TYPES_CSS.indexOf(fileExt) >= 0) {
				isCss = true;
			} else if (HtmlIncludeTag.POSSIBLE_TYPES_JS.indexOf(fileExt) >= 0) {
				isJs = true;
			}
		}
		
		if (isJs || isCss) {
			String initialRequestId = getInitialRequestUniqueId();
			HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
			if (log.isDebugEnabled()) {
				log.debug("initialRequest id: [" + initialRequestId + "]");
				log.debug("Object at pageContext." + HtmlIncludeTag.OPENMRS_HTML_INCLUDE_MAP_KEY + " is "
				        + pageContext.getAttribute(HtmlIncludeTag.OPENMRS_HTML_INCLUDE_MAP_KEY, PageContext.SESSION_SCOPE)
				        + "");
			}
			
			if (!isAlreadyUsed(file, initialRequestId)) {
				StringBuilder output = new StringBuilder();
				String prefix = "";
				try {
					prefix = request.getContextPath();
					if (file.startsWith(prefix + "/")) {
						prefix = "";
					}
				}
				catch (ClassCastException cce) {
					log.debug("Could not cast request to HttpServletRequest in HtmlIncludeTag");
				}
				
				// the openmrs version is inserted into the file src so that js and css files are cached across version releases
				if (isJs) {
					output.append("<script src=\"").append(prefix).append(file);
					output.append("?v=").append(OpenmrsConstants.OPENMRS_VERSION_SHORT);
					if (appendLocale) {
						output.append("&locale=").append(Context.getLocale());
					}
					output.append("\" type=\"text/javascript\" ></script>");
				} else if (isCss) {
					output.append("<link href=\"").append(prefix).append(file);
					output.append("?v=").append(OpenmrsConstants.OPENMRS_VERSION_SHORT);
					if (appendLocale) {
						output.append("&locale=").append(Context.getLocale());
					}
					output.append("\" type=\"text/css\" rel=\"stylesheet\" />");
				}
				
				if (log.isDebugEnabled()) {
					log.debug("isAlreadyUsed() is FALSE - printing " + this.file + " to output.");
				}
				
				try {
					pageContext.getOut().print(output.toString());
				}
				catch (IOException e) {
					log.debug("Could not produce output in HtmlIncludeTag.java");
				}
			} else {
				if (log.isDebugEnabled()) {
					log.debug("isAlreadyUsed() is TRUE - suppressing file print for " + this.file + "");
				}
			}
		}
		
		resetValues();
		
		return SKIP_BODY;
	}
	
	private String getInitialRequestUniqueId() {
		HttpServletRequest pageRequest = (HttpServletRequest) this.pageContext.getRequest();
		Object attr = pageRequest.getAttribute(WebConstants.INIT_REQ_UNIQUE_ID);
		if (attr != null) {
			String uniqueId = (String) attr;
			if (log.isDebugEnabled()) {
				log.debug("Returning initial request: " + uniqueId);
			}
			return uniqueId;
		} else {
			log.error("Could not find value for " + WebConstants.INIT_REQ_UNIQUE_ID + " in pageContext");
			return "";
		}
	}
	
	@SuppressWarnings("unchecked")
	private boolean isAlreadyUsed(String fileName, String initialRequestId) {
		boolean isUsed = false;
		
		if (fileName != null) {
			log.debug("initialRequestId: " + initialRequestId);
			
			// retrieve the request id that the last mapping was added for
			String lastRequestId = (String) pageContext.getAttribute(HtmlIncludeTag.OPENMRS_HTML_INCLUDE_REQUEST_ID_KEY,
			    PageContext.SESSION_SCOPE);
			
			// retrieve the htmlinclude map from the page request
			//HashMap<String,String> hmIncludeMap = (HashMap<String, String>) initialRequest.getAttribute(HtmlIncludeTag.OPENMRS_HTML_INCLUDE_KEY);
			HashMap<String, String> hmIncludeMap = (HashMap<String, String>) pageContext.getAttribute(
			    HtmlIncludeTag.OPENMRS_HTML_INCLUDE_MAP_KEY, PageContext.SESSION_SCOPE);
			
			// reset the hmIncludeMap if not found or if not on the initial request anymore
			if (hmIncludeMap == null || !initialRequestId.equals(lastRequestId)) {
				log.debug("Creating new hmIncludeMap");
				hmIncludeMap = new HashMap<String, String>();
			} else {
				log.debug("Using hmIncludeMap from object");
			}
			
			if (hmIncludeMap.containsKey(fileName)) {
				log.debug("HTMLINCLUDETAG HAS ALREADY INCLUDED FILE " + fileName);
				isUsed = true;
			} else {
				log.debug("HTMLINCLUDETAG IS WRITING HTML TO INCLUDE FILE " + fileName);
				log.debug("HashCode for file is " + fileName.hashCode());
				
				hmIncludeMap.put(fileName, "true");
				
				// save the hmIncludeMap to the
				pageContext.setAttribute(HtmlIncludeTag.OPENMRS_HTML_INCLUDE_MAP_KEY, hmIncludeMap,
				    PageContext.SESSION_SCOPE);
				
				// save the name of the initial page
				pageContext.setAttribute(HtmlIncludeTag.OPENMRS_HTML_INCLUDE_REQUEST_ID_KEY, initialRequestId,
				    PageContext.SESSION_SCOPE);
			}
		}
		
		return isUsed;
	}
	
	private void resetValues() {
		log.debug("resetting values");
		this.type = null;
		this.file = null;
		this.appendLocale = false;
	}
	
	public synchronized String getType() {
		return type;
	}
	
	public synchronized void setType(String type) {
		this.type = type;
	}
	
	/**
	 * @return Returns the file.
	 */
	public synchronized String getFile() {
		return file;
	}
	
	/**
	 * @param file The file to set.
	 */
	public synchronized void setFile(String file) {
		this.file = file;
		if (file != null) {
			this.file = file.trim();
		}
	}
	
	public synchronized boolean getAppendLocale() {
		return appendLocale;
	}
	
	public synchronized void setAppendLocale(boolean appendLocale) {
		this.appendLocale = appendLocale;
	}
}
