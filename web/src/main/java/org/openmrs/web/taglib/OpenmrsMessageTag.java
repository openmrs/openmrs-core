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
import java.util.Collection;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;

import org.openmrs.api.context.Context;
import org.openmrs.util.LocaleUtility;
import org.openmrs.util.OpenmrsUtil;
import org.openmrs.web.taglib.behavior.TagMessageWriterBehavior;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.tags.MessageTag;
import org.springframework.web.util.ExpressionEvaluationUtils;
import org.springframework.web.util.HtmlUtils;
import org.springframework.web.util.JavaScriptUtils;
import org.springframework.web.util.TagUtils;

/**
 * Custom JSP tag to look up a message in the scope of the page. This is based on the Spring message tag. It extends functionality
 * of Spring's message tag by adding the ability to place text inside the tag body and specifying the locale of the text. Any text,
 * placed between start and end tags will be treated as the default message within the locale specified in within the locale
 * attribute (or the system default locale if the locale attribute is not specified).
 * <p>
 * As spring:message tag, this also retrieves the message from bundle with the given code, but (in addition) this tag uses its body
 * text when the code cannot be resolved. If the body value also is not specified, the value of the text attribute is used. If there
 * is no message in the bundle and no text attribute value or tag body text, then the message code is displayed, if it is set;
 * otherwise null. If both text attribute and body text are present (shouldn't happen, but could by mistake), then this tag uses the
 * body text before checking the text attribute. With this tag user can define default text for a locale other than the system
 * default by using locale attribute. If locale attribute is not specified, then the system default locale (e.g., "en") will be
 * used. HTML escaping is also supported by this tag.
 * </p>
 * <p>
 * This tag also supports customization of messages writing behavior. To change its default behavior, set the TagWriterBehavior
 * property with a custom implementation of the {@link TagMessageWriterBehavior} interface.
 * </p>
 * <p>
 * For example, if you put the following:
 * </p>
 * <code>
 * 	 &lt;openmrs:message code="wrong.code" &gt;Some text &lt;/openmrs:message&gt;
 * </code>
 * <p>
 * and there is no message that can be resolved by given code, then output will be "Some text". You may also specify the fallback in
 * spring-ish style using text attribute as follows:
 * </p>
 * <code>
 * &lt;openmrs:message code="wrong.code" text="Some other text" /&gt;
 * </code>
 * <p>
 * Using locale attribute of tag you can specify the locale of fallback text provided by message tag. You could write the tag like:
 * </p>
 * &lt;openmrs:message code="foo.greeting" locale="fr"&gt;Bonjour&lt;/openmrs:message&gt;
 * <p>
 * meaning that the message supplied with the tag is in the "fr" locale. If the user is viewing in English, then the message within
 * the English message bundle with the code foo.greeting would be displayed.
 * </p>
 */
public class OpenmrsMessageTag extends OpenmrsHtmlEscapingAwareTag {
	
	/** */
	private static final long serialVersionUID = 1L;
	
	/** Default separator for splitting an arguments String: a comma (",") */
	public static final String DEFAULT_ARGUMENT_SEPARATOR = ",";
	
	/** Default behavior used for writing resolved messages into output */
	public static final TagMessageWriterBehavior DEFAULT_WRITER_BEHAVIOUR = new DefaultTagWriterBehavior();
	
	/** Tag writing behavior instance used for customization of resolved messages rendering */
	public static TagMessageWriterBehavior tagWriterBehavior = DEFAULT_WRITER_BEHAVIOUR;
	
	private Object message;
	
	private String code;
	
	private Object arguments;
	
	private String argumentSeparator = DEFAULT_ARGUMENT_SEPARATOR;
	
	private String text;
	
	private String var;
	
	private String scope = TagUtils.SCOPE_PAGE;
	
	/** Specifies the locale for the tag's message. Default to system default locale. */
	private String locale = LocaleUtility.getDefaultLocale().getLanguage();
	
	private boolean javaScriptEscape = false;
	
	/**
	 * @see MessageTag#setMessage(Object)
	 */
	public void setMessage(Object message) {
		this.message = message;
	}
	
	/**
	 * Set the message code for this tag.
	 */
	public void setCode(String code) {
		this.code = code;
	}
	
	/**
	 * @see MessageTag#setArguments(Object)
	 */
	public void setArguments(Object arguments) {
		this.arguments = arguments;
	}
	
	/**
	 * @see MessageTag#setArgumentSeparator(String)
	 */
	public void setArgumentSeparator(String argumentSeparator) {
		this.argumentSeparator = argumentSeparator;
	}
	
	/**
	 * Set the message text for this tag.
	 */
	public void setText(String text) {
		this.text = text;
	}
	
	/**
	 * @see MessageTag#setVar(String)
	 */
	public void setVar(String var) {
		this.var = var;
	}
	
	/**
	 * @see MessageTag#setScope(String)
	 */
	public void setScope(String scope) {
		this.scope = scope;
	}
	
	/**
	 * Sets the locale of the supplied message. If no text passed in or given text can not be recognized as locale, then default
	 * value of locale attribute of this tag will not be changed
	 * 
	 * @param locale
	 *            the locale string to set
	 */
	public void setLocale(String locale) {
		if (StringUtils.hasText(locale) && LocaleUtility.fromSpecification(locale) != null) {
			this.locale = locale;
		}
	}
	
	/**
	 * Set JavaScript escaping for this tag, as boolean value. Default is "false".
	 */
	public void setJavaScriptEscape(String javaScriptEscape) throws JspException {
		this.javaScriptEscape = ExpressionEvaluationUtils.evaluateBoolean("javaScriptEscape", javaScriptEscape, pageContext);
	}
	
	/**
	 * @see MessageTag#doStartTagInternal()
	 * @should evaluate specified message resolvable
	 * @should resolve message by code
	 * @should resolve message in locale that different from default
	 * @should return code if no message resolved
	 * @should use body content as fallback if no message resolved
	 * @should use text attribute as fallback if no message resolved
	 * @should use body content in prior to text attribute as fallback if no message resolved
	 * @should ignore fallbacks if tag locale differs from context locale
	 */
	@Override
	protected int doEndTagInternal() throws JspException, IOException {
		try {
			// Resolve the unescaped message.
			String msg = resolveMessage();
			
			// HTML and/or JavaScript escape, if demanded.
			msg = isHtmlEscape() ? HtmlUtils.htmlEscape(msg) : msg;
			msg = this.javaScriptEscape ? JavaScriptUtils.javaScriptEscape(msg) : msg;
			
			// Expose as variable, if demanded, else write to the page.
			String resolvedVar = ExpressionEvaluationUtils.evaluateString("var", this.var, pageContext);
			if (resolvedVar != null) {
				String resolvedScope = ExpressionEvaluationUtils.evaluateString("scope", this.scope, pageContext);
				pageContext.setAttribute(resolvedVar, msg, TagUtils.getScope(resolvedScope));
			} else {
				writeMessage(msg);
			}
			
			return EVAL_PAGE;
		}
		catch (NoSuchMessageException ex) {
			throw new JspTagException(getNoSuchMessageExceptionDescription(ex));
		}
	}
	
	/**
	 * Resolve the specified message or code or text or tag body into a concrete message string. The returned message string should
	 * be unescaped.
	 */
	protected String resolveMessage() throws JspException {
		MessageSource messageSource = getMessageSource();
		if (messageSource == null) {
			throw new JspTagException("No corresponding MessageSource to resolve message with found");
		}
		
		// first, evaluate the specified MessageSourceResolvable, if any
		MessageSourceResolvable resolvedMessage = null;
		if (this.message instanceof MessageSourceResolvable) {
			resolvedMessage = (MessageSourceResolvable) this.message;
		} else if (this.message != null) {
			String expr = this.message.toString();
			resolvedMessage = (MessageSourceResolvable) ExpressionEvaluationUtils.evaluate("message", expr,
			    MessageSourceResolvable.class, pageContext);
		}
		
		if (resolvedMessage != null) {
			// we have a given MessageSourceResolvable.
			return messageSource.getMessage(resolvedMessage, getRequestContext().getLocale());
		}
		
		String resolvedCode = ExpressionEvaluationUtils.evaluateString("code", this.code, pageContext);
		String bodyText = null;
		String resolvedText = null;
		// if locale specified with tag attribute is the same as context locale
		if (OpenmrsUtil.nullSafeEquals(this.locale, Context.getLocale().getLanguage())) {
			// we need to evaluate fallback values in this case
			resolvedText = ExpressionEvaluationUtils.evaluateString("text", this.text, pageContext);
			if (getBodyContent() != null) {
				bodyText = getBodyContent().getString();
			}
		}
		// by default message code is used as fallback
		String message = resolvedCode;
		if (resolvedCode != null || resolvedText != null || bodyText != null) {
			// we have either a code or default text or body that we need to resolve.
			Object[] argumentsArray = resolveArguments(this.arguments);
			if (bodyText != null) {
				// we have a fallback body text to consider.
				message = messageSource.getMessage(resolvedCode, argumentsArray, bodyText, getRequestContext().getLocale());
			} else if (resolvedText != null) {
				// we have a fallback value of text attribute to consider.
				message = messageSource.getMessage(resolvedCode, argumentsArray, resolvedText, getRequestContext()
				        .getLocale());
			} else {
				// we have no fallback text to consider.
				try {
					message = messageSource.getMessage(resolvedCode, argumentsArray, getRequestContext().getLocale());
				}
				catch (NoSuchMessageException e) {
					// do nothing, use resolved code as fallback
				}
			}
		}
		
		// all we have is a specified literal text.
		return message;
	}
	
	/**
	 * @see MessageTag#resolveArguments(Object)
	 */
	protected Object[] resolveArguments(Object arguments) throws JspException {
		if (arguments instanceof String) {
			String[] stringArray = StringUtils.delimitedListToStringArray((String) arguments, this.argumentSeparator);
			if (stringArray.length == 1) {
				Object argument = ExpressionEvaluationUtils.evaluate("argument", stringArray[0], pageContext);
				if (argument != null && argument.getClass().isArray()) {
					return ObjectUtils.toObjectArray(argument);
				} else {
					return new Object[] { argument };
				}
			} else {
				Object[] argumentsArray = new Object[stringArray.length];
				for (int i = 0; i < stringArray.length; i++) {
					argumentsArray[i] = ExpressionEvaluationUtils.evaluate("argument[" + i + "]", stringArray[i],
					    pageContext);
				}
				return argumentsArray;
			}
		} else if (arguments instanceof Object[]) {
			return (Object[]) arguments;
		} else if (arguments instanceof Collection) {
			return ((Collection<?>) arguments).toArray();
		} else if (arguments != null) {
			// Assume a single argument object.
			return new Object[] { arguments };
		} else {
			return null;
		}
	}
	
	/**
	 * Writes the message to the page. Before actual writing occurs, it calls method on static instance of tag writer behavior to
	 * customize the message which will be written.
	 * 
	 * @param message
	 *            the message to write
	 * @throws IOException
	 *             if writing error occurs
	 */
	protected void writeMessage(String message) throws IOException {
		pageContext.getOut().write(tagWriterBehavior.renderMessage(String.valueOf(message), code, locale, text));
	}
	
	/**
	 * Use the current RequestContext's application context as MessageSource.
	 */
	protected MessageSource getMessageSource() {
		return getRequestContext().getMessageSource();
	}
	
	/**
	 * Return default exception message.
	 */
	protected String getNoSuchMessageExceptionDescription(NoSuchMessageException ex) {
		return ex.getMessage();
	}
	
	/**
	 * Sets tag writer behavior to customize rendering of resolved messages
	 * 
	 * @param tagWriterBehavior
	 *            the tagWriterBehavior to set
	 */
	public static void setTagWriterBehavior(TagMessageWriterBehavior tagWriterBehavior) {
		OpenmrsMessageTag.tagWriterBehavior = tagWriterBehavior;
	}
	
	/**
	 * Very simple implementation of {@link TagMessageWriterBehavior} interface that is used by this class by default. It actually
	 * does not customize passed in message during output rendering.
	 */
	static final class DefaultTagWriterBehavior implements TagMessageWriterBehavior {
		
		/**
		 * Hidden constructor.
		 */
		private DefaultTagWriterBehavior() {
		}
		
		/**
		 * Just returns the resolved message message that is originally passed in.
		 * 
		 * @see org.openmrs.web.taglib.behavior.TagMessageWriterBehavior#renderMessage(java.lang.String)
		 */
		@Override
		public String renderMessage(String resolvedText, String code, String locale, String fallbackText) {
			return resolvedText;
		}
		
	}
	
}
