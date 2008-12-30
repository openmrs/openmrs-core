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
package org.openmrs.module.web.taglib;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.tagext.BodyTag;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.Extension;
import org.openmrs.module.ModuleFactory;
import org.openmrs.util.OpenmrsUtil;

/**
 * Extension point tag. Loops over all extensions defined for point "pointId". Makes the variable
 * "extension" Usage:
 * 
 * <pre>
 *  &lt;openmrs:extensionPoint pointId=&quot;org.openmrs.cohortbuilder.links&quot; type=&quot;html&quot; varStatus=&quot;stat&quot;&gt;
 *     &lt;c:if test=&quot;${stat.first}&quot;&gt;
 *       &lt;br/&gt;
 *       &lt;b&gt;Module Links:&lt;/b&gt;
 *     &lt;/c:if&gt;
 *  &lt;ul>
 *    &lt;openmrs:hasPrivilege privilege=&quot;${extension.requiredPrivilege}&quot;&gt;
 *        &lt;form method=&quot;post&quot; action=&quot;${pageContext.request.contextPath}/${extension.url}&quot;&gt;
 *           &lt;input type=&quot;hidden&quot; name=&quot;patientIds&quot; value=&quot;&quot;/&gt;
 *           &lt;li>
 *              &lt;a href=&quot;#&quot; onClick=&quot;javascript:submitLink(this)&quot;&gt;&lt;spring:message code=&quot;${extension.label}&quot;/&gt;&lt;/a&gt;
 *           &lt;/li>
 *        &lt;/form&gt;
 *    &lt;/openmrs:hasPrivilege&gt;
 *    &lt;c:if test=&quot;${stat.last}&quot;&gt;
 *      &lt;/ul>
 *    &lt;/c:if&gt;
 *  &lt;/openmrs:extensionPoint&gt;
 * </pre>
 * 
 * @see org.openmrs.module.Extension available in the loop.
 */
public class ExtensionPointTag extends TagSupport implements BodyTag {
	
	// general variables
	public static final long serialVersionUID = 12323003L;
	
	private final Log log = LogFactory.getLog(getClass());
	
	// variables for the varStatus map
	private static final String STATUS_FIRST = "first";
	
	private static final String STATUS_LAST = "last";
	
	private static final String STATUS_INDEX = "index";
	
	private Integer index = 0;
	
	// private variables
	private Iterator<Extension> extensions;
	
	private Map<String, String> parameterMap;
	
	private BodyContent bodyContent = null;
	
	// tag attributes
	private String pointId;
	
	private String parameters = "";
	
	private String requiredClass;
	
	/** all tags using this should default to 'html' media type */
	private String type = "html";
	
	/** name of Map containing variables first/last/index */
	private String varStatus = "varStatus";
	
	/** actual map containing the status variables */
	private Map<String, Object> status = new HashMap<String, Object>();
	
	// tag helpers
	private Boolean firstIteration = true;
	
	// methods
	public int doStartTag() {
		log.debug("Starting tag for extension point: " + pointId);
		
		// "zero out" the extension list and other variables
		extensions = null;
		parameterMap = OpenmrsUtil.parseParameterList(parameters);
		status = new HashMap<String, Object>();
		
		List<Extension> extensionList = null;
		
		if (type != null && type.length() > 0) {
			try {
				Extension.MEDIA_TYPE mediaType = Enum.valueOf(Extension.MEDIA_TYPE.class, type);
				log.debug("Getting extensions: " + pointId + " : " + mediaType);
				extensionList = ModuleFactory.getExtensions(pointId, mediaType);
			}
			catch (IllegalArgumentException e) {
				log.warn("extension point type: '" + type + "' is invalid. Must be enum of Extension.MEDIA_TYPE", e);
			}
		} else {
			log.debug("Getting extensions: " + pointId);
			extensionList = ModuleFactory.getExtensions(pointId);
		}
		
		if (extensionList != null) {
			log.debug("Found " + extensionList.size() + " extensions");
			if (requiredClass != null) {
				try {
					Class<?> clazz = Class.forName(requiredClass);
					for (Extension ext : extensionList) {
						if (!clazz.isAssignableFrom(ext.getClass())) {
							throw new ClassCastException("Extensions at this point (" + pointId + ") are "
							        + "required to be of " + clazz + " or a subclass. " + ext.getClass() + " is not.");
						}
					}
				}
				catch (ClassNotFoundException ex) {
					throw new IllegalArgumentException(ex);
				}
			}
			extensions = extensionList.iterator();
		}
		
		if (extensions == null || extensions.hasNext() == false) {
			extensions = null;
			return SKIP_BODY;
		} else {
			firstIteration = true;
			return EVAL_BODY_BUFFERED;
		}
		
	}
	
	/**
	 * @see javax.servlet.jsp.tagext.BodyTag#doInitBody()
	 */
	public void doInitBody() throws JspException {
		getBodyContent().clearBody();
		pageContext.removeAttribute("extension");
		return;
	}
	
	/**
	 * @see javax.servlet.jsp.tagext.IterationTag#doAfterBody()
	 */
	public int doAfterBody() throws JspException {
		if (extensions.hasNext()) {
			if (firstIteration) {
				// for some reason the body is getting evaluated after the
				// doInitBody() call
				// and before this. Instead of hacking in duplicated logic, I
				// use this hack
				bodyContent.clearBody();
				firstIteration = false;
			}
			Extension ext = extensions.next();
			String overrideContent = ext.getOverrideContent(getBodyContentString());
			if (overrideContent == null) {
				iterate(ext);
			} else {
				try {
					bodyContent.getEnclosingWriter().write(overrideContent);
				}
				catch (IOException io) {
					log.warn("Cannot write override content of extension: " + ext.toString(), io);
				}
				if (!extensions.hasNext())
					return SKIP_BODY;
			}
			return EVAL_BODY_BUFFERED;
		}
		
		return SKIP_BODY;
	}
	
	private void iterate(Extension ext) {
		if (ext != null) {
			ext.initialize(parameterMap);
			log.debug("Adding ext: " + ext.getExtensionId() + " to pageContext class: " + ext.getClass());
			pageContext.setAttribute("extension", ext);
			
			// set up and apply the status variable
			status.put(STATUS_FIRST, index == 0);
			status.put(STATUS_LAST, extensions.hasNext() == false);
			status.put(STATUS_INDEX, index++);
			pageContext.setAttribute(varStatus, status);
		} else {
			pageContext.removeAttribute("extension");
		}
	}
	
	/**
	 * @see javax.servlet.jsp.tagext.Tag#doEndTag()
	 */
	public int doEndTag() throws JspException {
		try {
			if (getBodyContent() != null) {
				if (log.isDebugEnabled())
					log.debug("Ending tag: " + bodyContent.getString());
				if (extensions != null)
					getBodyContent().writeOut(bodyContent.getEnclosingWriter());
				bodyContent.clearBody();
			} else {
				// the tag doesn't have a body, so initBody and doAfterBody have
				// not been called. Do iterations now
				while (extensions != null && extensions.hasNext()) {
					Extension ext = extensions.next();
					ext.initialize(parameterMap);
					String overrideContent = ext.getOverrideContent("");
					if (overrideContent != null)
						pageContext.getOut().write(overrideContent);
				}
			}
		}
		catch (java.io.IOException e) {
			throw new JspTagException("IO Error while ending tag for point: " + pointId, e);
		}
		release();
		return EVAL_PAGE;
	}
	
	@Override
	public void release() {
		extensions = null;
		pointId = null;
		requiredClass = null;
		type = null;
		if (bodyContent != null)
			bodyContent.clearBody();
		bodyContent = null;
		super.release();
	}
	
	public String getPointId() {
		return pointId;
	}
	
	public void setPointId(String pointId) {
		this.pointId = pointId;
	}
	
	public String getParameters() {
		return parameters;
	}
	
	public void setParameters(String parameters) {
		this.parameters = parameters;
	}
	
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public void setBodyContent(BodyContent b) {
		this.bodyContent = b;
	}
	
	public BodyContent getBodyContent() {
		return bodyContent;
	}
	
	public String getBodyContentString() {
		if (bodyContent == null)
			return "";
		else
			return bodyContent.getString();
	}
	
	/**
	 * @return the varStatus
	 */
	public String getVarStatus() {
		return varStatus;
	}
	
	/**
	 * @param varStatus the varStatus to set
	 */
	public void setVarStatus(String varStatus) {
		this.varStatus = varStatus;
	}
	
	public String getRequiredClass() {
		return requiredClass;
	}
	
	public void setRequiredClass(String requiredClass) {
		this.requiredClass = requiredClass;
	}
	
}
