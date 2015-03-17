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
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsUtil;
import org.springframework.web.servlet.support.BindStatus;
import org.springframework.web.servlet.support.RequestContext;
import org.springframework.web.servlet.tags.NestedPathTag;
import org.springframework.web.servlet.tags.RequestContextAwareTag;
import org.springframework.web.util.ExpressionEvaluationUtils;

/**
 * Formats a date object in the desired type:<br/>
 * e.g:
 *
 * <pre>
 *   &lt;openmrs:formatDate date="${dateObj}" type="textbox"/&gt;
 * </pre>
 *
 * becomes a string like: "20/11/2009" in the user's current locale. <br/>
 * <br/>
 * Options for "type" are:
 * <ul>
 * <li>xml - dd-MMM-yyyy
 * <li>long - DateFormat.LONG
 * <li>medium - DateFormat.MEDIUM
 * <li>textbox - Context.getDateFormat()
 * <li>milliseconds - current milliseconds since the epoch
 * </ul>
 */
public class FormatDateTag extends TagSupport {
	
	public static final long serialVersionUID = 121341222L;
	
	private final Log log = LogFactory.getLog(getClass());
	
	private Date date;
	
	private Boolean dateWasSet = false;
	
	private String path;
	
	private String type;
	
	private String format;
	
	private boolean showTodayOrYesterday = false;
	
	public int doStartTag() {
		RequestContext requestContext = (RequestContext) this.pageContext
		        .getAttribute(RequestContextAwareTag.REQUEST_CONTEXT_PAGE_ATTRIBUTE);
		
		if (date == null && getPath() != null) {
			try {
				// get the "path" object from the pageContext
				String resolvedPath = ExpressionEvaluationUtils.evaluateString("path", getPath(), pageContext);
				String nestedPath = (String) pageContext.getAttribute(NestedPathTag.NESTED_PATH_VARIABLE_NAME,
				    PageContext.REQUEST_SCOPE);
				if (nestedPath != null) {
					resolvedPath = nestedPath + resolvedPath;
				}
				
				BindStatus status = new BindStatus(requestContext, resolvedPath, false);
				log.debug("status: " + status);
				
				if (status.getValue() != null) {
					log.debug("status.value: " + status.getValue());
					if (status.getValue().getClass() == Date.class) {
						// if no editor was registered all will go well here
						date = (Date) status.getValue();
					} else {
						// if a "Date" property editor was registerd for the form, the status.getValue()
						// object will be a java.lang.String.  This is useless.  Try getting the original
						// value from the troublesome editor
						log.debug("status.valueType: " + status.getValueType());
						Timestamp timestamp = (Timestamp) status.getEditor().getValue();
						date = new Date(timestamp.getTime());
					}
				}
			}
			catch (Exception e) {
				log.warn("Unable to get a date object from path: " + getPath(), e);
				return SKIP_BODY;
			}
		}
		
		if (!dateWasSet && date == null) {
			log.warn("Both 'date' and 'path' cannot be null.  Page: " + pageContext.getPage() + " localname:"
			        + pageContext.getRequest().getLocalName() + " rd:" + pageContext.getRequest().getRequestDispatcher(""));
			return SKIP_BODY;
		}
		
		if (type == null) {
			type = "";
		}
		
		DateFormat dateFormat = null;
		
		if (format != null && format.length() > 0) {
			dateFormat = new SimpleDateFormat(format, Context.getLocale());
		} else if (type.equals("xml")) {
			dateFormat = new SimpleDateFormat("dd-MMM-yyyy", Context.getLocale());
		} else {
			log.debug("context locale: " + Context.getLocale());
			
			if (type.equals("long")) {
				dateFormat = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG, Context.getLocale());
			} else if (type.equals("medium")) {
				dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM, Context.getLocale());
			} else {
				dateFormat = Context.getDateFormat();
			}
		}
		
		if (dateFormat == null) {
			dateFormat = new SimpleDateFormat("MM-dd-yyyy");
		}
		
		String datestr = "";
		
		try {
			if (date != null) {
				if (type.equals("milliseconds")) {
					datestr = "" + date.getTime();
				} else {
					if (showTodayOrYesterday
					        && (DateUtils.isSameDay(Calendar.getInstance().getTime(), date) || OpenmrsUtil.isYesterday(date))) {
						//print only time of day but maintaining the format(24 Vs 12) if any was specified
						String timeFormatString = (format != null && !format.contains("a")) ? "HH:mm" : "h:mm a";
						dateFormat = new SimpleDateFormat(timeFormatString);
						if (DateUtils.isSameDay(Calendar.getInstance().getTime(), date)) {
							datestr = Context.getMessageSourceService().getMessage("general.today") + " "
							        + dateFormat.format(date);
						} else {
							datestr = Context.getMessageSourceService().getMessage("general.yesterday") + " "
							        + dateFormat.format(date);
						}
					} else {
						datestr = dateFormat.format(date);
					}
				}
			}
		}
		catch (IllegalArgumentException e) {
			//format or date is invalid
			log.error("date: " + date);
			log.error("format: " + format);
			log.error(e);
			datestr = date.toString();
		}
		
		try {
			pageContext.getOut().write(datestr);
		}
		catch (IOException e) {
			log.error(e);
		}
		
		// reset the objects to null because taglibs are reused
		release();
		
		return SKIP_BODY;
	}
	
	/**
	 * Clean up the variables
	 */
	public void release() {
		super.release();
		this.dateWasSet = false;
		this.date = null;
		this.format = null;
		this.path = null;
		this.showTodayOrYesterday = false;
	}
	
	// variable access methods
	
	public Date getDate() {
		return date;
	}
	
	public void setDate(Date date) {
		this.dateWasSet = true;
		this.date = date;
	}
	
	public String getPath() {
		return path;
	}
	
	public void setPath(String path) {
		this.path = path;
	}
	
	public String getFormat() {
		return format;
	}
	
	public void setFormat(String format) {
		this.format = format;
	}
	
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	/**
	 * @return the showTodayOrYesterday
	 * @since 1.9
	 */
	public boolean isShowTodayOrYesterday() {
		return showTodayOrYesterday;
	}
	
	/**
	 * @param showTodayOrYesterday the showTodayOrYesterday to set
	 * @since 1.9
	 */
	public void setShowTodayOrYesterday(boolean showTodayOrYesterday) {
		this.showTodayOrYesterday = showTodayOrYesterday;
	}
}
