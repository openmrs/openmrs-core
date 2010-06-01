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

import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.User;
import org.openmrs.api.UserService;
import org.openmrs.api.context.Context;
import org.openmrs.messagesource.MessageSourceService;
import org.openmrs.util.OpenmrsConstants;

public class ConfirmDialogWidgetTag extends TagSupport {

	private static final long serialVersionUID = 122321211L;

	private static final Log log = LogFactory.getLog(ConfirmDialogWidgetTag.class);
	
	private final String DEFAULT_AFFIRM = "OK";
	
	private final String DEFAULT_NEGATE = "Cancel";
	
	private final String DEFAULT_TITLE = "Confirmation";
	
	private final String DEFAULT_MESSAGE = "Are you sure?";
	
	private final String DEFAULT_SUPPRESS_MESSAGE = "Do not show this message again";

	private String id;
	
	private String messageCode;
	
	private String titleCode;

	private String button1;
	
	private String button2;
	
	private String suppressMessageCode;
	
	private String suppress;

	@Override
	public int doStartTag() {
		StringBuffer sb = new StringBuffer();
		
		UserService userService = Context.getUserService();
		
		MessageSourceService messageSourceService = Context.getMessageSourceService();
		
		User currentUser = Context.getAuthenticatedUser();

		User user = userService.getUser(currentUser.getUserId());
		
		String suppressKey = (this.suppress != null ? OpenmrsConstants.USER_PROPERTY_SUPPRESS_DIALOG + "." + this.suppress
		        : "NA");

		String suppress = user.getUserProperty(suppressKey, "false");
		
		String message = messageSourceService.getMessage(messageCode);
		
		String affirm = messageSourceService.getMessage(button1);
		
		String negate = messageSourceService.getMessage(button2);
		
		String suppressMessage = messageSourceService.getMessage(this.suppressMessageCode);
		
		sb.append("<div style=\"display:none\" id=\"" + id + "\" class=\"jConfirm_Window\">");
		sb.append("<div id=\"jConfirm_Box\">");
		sb.append("<div class=\"jConfirm_Header\"><span>"
		        + (titleCode == null ? DEFAULT_TITLE : messageSourceService.getMessage(titleCode))
		        + "</span><a href=\"#\" id=\"jConfirm_Close\">x</a></div>");
		
		sb.append("<div id=\"jConfirm_Message\">"+( message == null ? DEFAULT_MESSAGE : message )+"</div>");
		sb.append("<input type=\"hidden\" id=\"suppress\" value=\"" + suppress + "\" />");
		sb.append("<input type=\"hidden\" id=\"suppress_key\" value=\"" + suppressKey + "\" />");
		sb.append("<div id=\"jConfirm_Suppress\">");
		if (!"NA".equals(suppressKey)) { //Don't show suppress message if no suppress key is supplied
			sb.append("<input type=\"checkbox\" name=\"suppress_message\" />"
			        + (suppressMessage == null ? DEFAULT_SUPPRESS_MESSAGE : suppressMessage));
		}else{
			sb.append("<br>"); //Just to have a balanced confirmation dialog
		}
		sb.append("</div>");
		sb.append("<div id=\"jConfirm_Control\">");
		sb.append("<input type=\"button\" class=\"jConfirm_Button\" id=\"jConfirm_Affirm\" value=\""
		        + (affirm == null ? DEFAULT_AFFIRM : affirm) + "\" />");
		sb.append("<input type=\"button\" class=\"jConfirm_Button\" id=\"jConfirm_Negate\" value=\""
		        + (negate == null ? DEFAULT_NEGATE : negate) + "\" />");
		sb.append("</div>");
		sb.append("</div>");
		sb.append("</div>");
		
		try {
			JspWriter out = pageContext.getOut();
			out.write(sb.toString());
		}
		catch (IOException e) {
			log.error("Error while starting Confirm Dialog Widget tag", e);
		}

		return SKIP_BODY;
	}

	/**
	 * @return the id
	 */
	@Override
	public String getId() {
		return id;
	}
	
	/**
	 * @param id the id to set
	 */
	@Override
	public void setId(String id) {
		this.id = id;
	}
	
	/**
	 * @return the button1
	 */
	public String getButton1() {
		return button1;
	}
	
	/**
	 * @param button1 the button1 to set
	 */
	public void setButton1(String button1) {
		this.button1 = button1;
	}
	
	/**
	 * @return the button2
	 */
	public String getButton2() {
		return button2;
	}
	
	/**
	 * @param button2 the button2 to set
	 */
	public void setButton2(String button2) {
		this.button2 = button2;
	}
	
	/**
	 * @return the suppress
	 */
	public String getSuppress() {
		return suppress;
	}
	
	/**
	 * @param suppress the suppress to set
	 */
	public void setSuppress(String suppress) {
		this.suppress = suppress;
	}

	/**
	 * @return the messageCode
	 */
	public String getMessageCode() {
		return messageCode;
	}
	
	/**
	 * @param messageCode the messageCode to set
	 */
	public void setMessageCode(String messageCode) {
		this.messageCode = messageCode;
	}

	/**
	 * @return the titleCode
	 */
	public String getTitleCode() {
		return titleCode;
	}
	
	/**
	 * @param titleCode the titleCode to set
	 */
	public void setTitleCode(String titleCode) {
		this.titleCode = titleCode;
	}
	
	/**
	 * @return the suppressMessageCode
	 */
	public String getSuppressMessageCode() {
		return suppressMessageCode;
	}
	
	/**
	 * @param suppressMessageCode the suppressMessageCode to set
	 */
	public void setSuppressMessageCode(String suppressMessage) {
		this.suppressMessageCode = suppressMessage;
	}

	
}
