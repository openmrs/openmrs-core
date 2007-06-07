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