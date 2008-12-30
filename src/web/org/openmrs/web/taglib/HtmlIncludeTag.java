/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.web.taglib;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.web.WebConstants;

public class HtmlIncludeTag extends TagSupport {
	
	public static final long serialVersionUID = 13472382822L;
	
	private final Log log = LogFactory.getLog(getClass());
	
	private static final String POSSIBLE_TYPES_JS = ".js,javascript,jscript";
	
	private static final String POSSIBLE_TYPES_CSS = ".css,style,stylesheet";
	
	public static final String OPENMRS_HTML_INCLUDE_REQUEST_ID_KEY = "org.openmrs.htmlInclude.pageName";
	
	public static final String OPENMRS_HTML_INCLUDE_MAP_KEY = "org.openmrs.htmlInclude.includeMap";
	
	private String type;
	
	private String file;
	
	public int doStartTag() throws JspException {
		log.debug("\n\n");
		
		// see if this is a JS or CSS file
		boolean isJs = false;
		boolean isCss = false;
		
		String fileExt = file.substring(file.lastIndexOf("."));
		
		if (this.type != null) {
			if (this.type.length() > 0) {
				if (HtmlIncludeTag.POSSIBLE_TYPES_CSS.indexOf(type) >= 0)
					isCss = true;
				else if (HtmlIncludeTag.POSSIBLE_TYPES_JS.indexOf(type) >= 0)
					isJs = true;
			}
		}
		
		if (!isCss && !isJs && fileExt.length() > 0) {
			if (HtmlIncludeTag.POSSIBLE_TYPES_CSS.indexOf(fileExt) >= 0)
				isCss = true;
			else if (HtmlIncludeTag.POSSIBLE_TYPES_JS.indexOf(fileExt) >= 0)
				isJs = true;
		}
		
		if (isJs || isCss) {
			String initialRequestId = getInitialRequestUniqueId();
			HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
			log.debug("initialRequest id: [" + initialRequestId + "]");
			log.debug("Object at pageContext." + HtmlIncludeTag.OPENMRS_HTML_INCLUDE_MAP_KEY + " is "
			        + pageContext.getAttribute(HtmlIncludeTag.OPENMRS_HTML_INCLUDE_MAP_KEY, PageContext.SESSION_SCOPE) + "");
			
			if (!isAlreadyUsed(file, initialRequestId)) {
				String output = "";
				String prefix = "";
				try {
					prefix = request.getContextPath();
					if (file.startsWith(prefix + "/"))
						prefix = "";
				}
				catch (ClassCastException cce) {
					log.debug("Could not cast request to HttpServletRequest in HtmlIncludeTag");
				}
				
				if (isJs) {
					output = "<script src=\"" + prefix + file + "\" type=\"text/javascript\" ></script>";
				} else if (isCss) {
					output = "<link href=\"" + prefix + file + "\" type=\"text/css\" rel=\"stylesheet\" />";
				}
				
				log.debug("isAlreadyUsed() is FALSE - printing " + this.file + " to output.");
				
				try {
					pageContext.getOut().print(output);
				}
				catch (IOException e) {
					log.debug("Could not produce output in HtmlIncludeTag.java");
				}
			} else {
				log.debug("isAlreadyUsed() is TRUE - suppressing file print for " + this.file + "");
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
			log.debug("Returning initial request: " + uniqueId);
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
			} else
				log.debug("Using hmIncludeMap from object");
			
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
	}
	
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	/**
	 * @return Returns the file.
	 */
	public String getFile() {
		return file;
	}
	
	/**
	 * @param file The file to set.
	 */
	public void setFile(String file) {
		this.file = file;
		if (file != null)
			this.file = file.trim();
	}
	
}
