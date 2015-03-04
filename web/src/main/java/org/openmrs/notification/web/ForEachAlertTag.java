/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.notification.web;

import java.util.Iterator;
import java.util.List;

import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.jstl.core.LoopTagSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.notification.Alert;
import org.openmrs.notification.AlertService;

public class ForEachAlertTag extends LoopTagSupport {
	
	public static final long serialVersionUID = 1232300L;
	
	private final Log log = LogFactory.getLog(getClass());
	
	private User user = null;
	
	private Boolean includeRead = false;
	
	private Boolean includeExpired = false;
	
	private Iterator<?> alerts;
	
	public void prepare() throws JspTagException {
		
		alerts = null;
		
		try {
			AlertService as = Context.getAlertService();
			if (user == null) {
				List<Alert> alertList = as.getAlertsByUser(null);
				log.debug("alertList.size: " + alertList.size());
				alerts = alertList.iterator();
			} else {
				List<Alert> alertList = as.getAlerts(user, includeRead, includeExpired);
				log.debug("alertList.size: " + alertList.size());
				alerts = alertList.iterator();
			}
			
			setVar("alert");
			setVarStatus("varStatus");
			
		}
		catch (Exception e) {
			log.error(e);
		}
	}
	
	@Override
	protected boolean hasNext() throws JspTagException {
		if (alerts == null) {
			return false;
		}
		return alerts.hasNext();
	}
	
	@Override
	protected Object next() throws JspTagException {
		if (alerts == null) {
			throw new JspTagException("The alert iterator is null");
		}
		return alerts.next();
	}
	
	@Override
	public void release() {
		// Clean out the variables
		user = null;
		includeRead = false;
		includeExpired = false;
	}
	
	public Boolean getIncludeExpired() {
		return includeExpired;
	}
	
	public void setIncludeExpired(Boolean includeExpired) {
		this.includeExpired = includeExpired;
	}
	
	public Boolean getIncludeRead() {
		return includeRead;
	}
	
	public void setIncludeRead(Boolean includeRead) {
		this.includeRead = includeRead;
	}
	
	public User getUser() {
		return user;
	}
	
	public void setUser(User user) {
		this.user = user;
	}
}
