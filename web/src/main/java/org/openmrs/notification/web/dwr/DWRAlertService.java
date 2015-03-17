/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.notification.web.dwr;

import java.util.Collection;
import java.util.List;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Role;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.notification.Alert;
import org.openmrs.notification.AlertService;
import org.openmrs.util.PrivilegeConstants;

public class DWRAlertService {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	/**
	 * Calls the corresponding AlertService.getAlertsByUser(null) method to get alerts for the
	 * current user or for the authenticated role
	 *
	 * @return
	 */
	public List<AlertListItem> getAlerts() {
		
		// The list of AlertListItems that we will return
		List<AlertListItem> alerts = new Vector<AlertListItem>();
		
		// get out context
		try {
			AlertService as = Context.getAlertService();
			
			// loop over the Alerts to create AlertListItems
			for (Alert a : as.getAlertsByUser(null)) {
				alerts.add(new AlertListItem(a));
			}
			
		}
		catch (Exception e) {
			log.error("Error getting alerts", e);
		}
		return alerts;
	}
	
	/**
	 * Calls the corresponding AlertService.markAlertRead(Alert) method
	 *
	 * @param alertId
	 */
	public void markAlertRead(Integer alertId) {
		
		try {
			AlertService as = Context.getAlertService();
			
			// Get the alert object
			Alert alert = as.getAlert(alertId);
			
			// fail early and quietly if the current user isn't actually
			// a recipient on this alert.
			if (alert == null || alert.getRecipient(Context.getAuthenticatedUser()) == null) {
				return;
			}
			
			// allow this user to save changes to alerts temporarily
			Context.addProxyPrivilege(PrivilegeConstants.MANAGE_ALERTS);
			
			// Mark the alert as read and save it
			as.saveAlert(alert.markAlertRead());
			
		}
		catch (Exception e) {
			log.error("Error while marking alert '" + alertId + "' as read", e);
		}
		finally {
			Context.removeProxyPrivilege(PrivilegeConstants.MANAGE_ALERTS);
		}
	}
	
	/**
	 * Creates and saves a new {@link Alert}
	 *
	 * @param text the string to set as the alert text
	 * @return true if the alert was successfully created and saved otherwise false
	 */
	public boolean createAlert(String text) {
		if (StringUtils.isNotBlank(text)) {
			try {
				Context.addProxyPrivilege(PrivilegeConstants.MANAGE_ALERTS);
				Context.addProxyPrivilege(PrivilegeConstants.GET_USERS);
				Context.addProxyPrivilege(PrivilegeConstants.GET_ROLES);
				
				Role role = Context.getUserService().getRole("System Developer");
				Collection<User> users = Context.getUserService().getUsersByRole(role);
				Context.getAlertService().saveAlert(new Alert(text, users));
				
				return true;
			}
			catch (Exception e) {
				log.error("Error while creating an alert ", e);
			}
			finally {
				Context.removeProxyPrivilege(PrivilegeConstants.MANAGE_ALERTS);
				Context.removeProxyPrivilege(PrivilegeConstants.GET_USERS);
				Context.removeProxyPrivilege(PrivilegeConstants.GET_ROLES);
			}
		}
		
		return false;
	}
	
	/**
	 * Marks all alert as read
	 */
	public void markAllAlertsRead() {
		AlertService as = Context.getAlertService();
		// Get the alert objects
		List<Alert> alerts = as.getAlertsByUser(Context.getAuthenticatedUser());
		
		for (Alert alert : alerts) {
			markAlertRead(alert.getId());
		}
	}
}
