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
package org.openmrs.notification.web.dwr;

import java.util.List;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.notification.Alert;
import org.openmrs.notification.AlertService;

public class DWRAlertService {

	protected final Log log = LogFactory.getLog(getClass());

	/**
	 * Calls the corresponding AlertService.getAlerts() method
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
			for (Alert a : as.getAlerts()) {
				alerts.add(new AlertListItem(a));
			}

		} catch (Exception e) {
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
			// Mark the alert as read
			as.markAlertRead(alert);
		} catch (Exception e) {
			log.error("Error while marking alert '" + alertId + "' as read", e);
		}
	}
}