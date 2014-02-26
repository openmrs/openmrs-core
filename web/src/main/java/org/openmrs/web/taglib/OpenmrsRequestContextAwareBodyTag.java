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

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.support.JspAwareRequestContext;
import org.springframework.web.servlet.support.RequestContext;
import org.springframework.web.servlet.tags.RequestContextAwareTag;

/**
 * Similar to Spring's {@link RequestContextAwareTag}, but intended to support tag body handling on the basis of functionalities,
 * provided by JSTL {@link BodyTagSupport}. Actually, it inherits additional convenience methods including getter methods for the
 * bodyContent property and methods to get at the previous out {@link JspWriter}. May be considered as base class for all tags that
 * require a {@link RequestContext} and aware on processing of tag body content.
 * <p>
 * With use of a <code>RequestContext</code> instance, you can get easy access to current state like the web application context,
 * locale and so on.
 * </p>
 */
public abstract class OpenmrsRequestContextAwareBodyTag extends BodyTagSupport {
	
	/** */
	private static final long serialVersionUID = 1L;
	
	/** Logger available to subclasses */
	protected final Log logger = LogFactory.getLog(getClass());
	
	private RequestContext requestContext;
	
	/**
	 * @see javax.servlet.jsp.tagext.BodyTagSupport#doStartTag()
	 */
	@Override
	public final int doStartTag() throws JspException {
		// explicitly tell container that it's need to buffer the tag body content
		return EVAL_BODY_BUFFERED;
	}
	
	/**
	 * After tag body has been evaluated and buffered this creates and exposes the current RequestContext. Delegates to
	 * {@link #doEndTagInternal()} for actual work.
	 */
	@Override
	public final int doEndTag() throws JspException {
		try {
			// get request content available from pageContext
			this.requestContext = (RequestContext) this.pageContext
			        .getAttribute(RequestContextAwareTag.REQUEST_CONTEXT_PAGE_ATTRIBUTE);
			// if request context is not specified, create empty request context and set it into pageContext
			if (this.requestContext == null) {
				this.requestContext = new JspAwareRequestContext(this.pageContext);
				this.pageContext.setAttribute(RequestContextAwareTag.REQUEST_CONTEXT_PAGE_ATTRIBUTE, this.requestContext);
			}
			// do actual work of this tag
			return doEndTagInternal();
		}
		catch (JspException ex) {
			logger.error(ex.getMessage(), ex);
			throw ex;
		}
		catch (RuntimeException ex) {
			logger.error(ex.getMessage(), ex);
			throw ex;
		}
		catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			throw new JspTagException(ex.getMessage());
		}
	}
	
	/**
	 * Return the current RequestContext.
	 */
	protected final RequestContext getRequestContext() {
		return this.requestContext;
	}
	
	/**
	 * Called by doEndTag to perform the actual work.
	 * 
	 * @return same as BodyTagSupport.doEndTag
	 * @throws Exception
	 *             any exception, any checked one other than a JspException gets wrapped in a JspException by doEndTag
	 * @see javax.servlet.jsp.tagext.TagSupport#doEndTag
	 */
	protected abstract int doEndTagInternal() throws Exception;
	
}
