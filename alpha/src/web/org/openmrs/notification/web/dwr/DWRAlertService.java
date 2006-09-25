package org.openmrs.notification.web.dwr;

import java.util.List;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.notification.Alert;
import org.openmrs.notification.AlertService;
import org.openmrs.web.WebConstants;

import uk.ltd.getahead.dwr.WebContextFactory;

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
		Context context = (Context) WebContextFactory.get().getSession()
				.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);

		if (context != null) {
			try {
				AlertService as = context.getAlertService();

				// loop over the Alerts to create AlertListItems
				for (Alert a : as.getAlerts()) {
					alerts.add(new AlertListItem(a));
				}

			} catch (Exception e) {
				log.error(e);
			}
		}
		return alerts;
	}

	/**
	 * Calls the corresponding AlertService.markAlertRead(Alert) method
	 * 
	 * @param alertId
	 */
	public void markAlertRead(Integer alertId) {

		// Get our context
		Context context = (Context) WebContextFactory.get().getSession()
				.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);

		if (context != null) {
			try {
				AlertService as = context.getAlertService();
				// Get the alert object
				Alert alert = as.getAlert(alertId);
				// Mark the alert as read
				as.markAlertRead(alert);
			} catch (Exception e) {
				log.error(e);
			}
		}
	}
}