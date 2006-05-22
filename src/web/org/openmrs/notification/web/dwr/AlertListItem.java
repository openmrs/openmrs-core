package org.openmrs.notification.web.dwr;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.notification.Alert;

public class AlertListItem {

	protected final Log log = LogFactory.getLog(getClass());

	private Integer alertId;

	private String text = "";

	private boolean satisfiedByAny = false;
	
	private Date dateToExpire;

	public AlertListItem() {
	}

	public AlertListItem(Alert alert) {
		if (alert != null) {
			this.alertId = alert.getAlertId();
			this.text = alert.getText();
			this.dateToExpire = alert.getDateToExpire();
			this.satisfiedByAny = alert.getSatisfiedByAny();
		}
	}

	public Integer getAlertId() {
		return alertId;
	}

	public void setAlertId(Integer alertId) {
		this.alertId = alertId;
	}

	public Date getDateToExpire() {
		return dateToExpire;
	}

	public void setDateToExpire(Date dateToExpire) {
		this.dateToExpire = dateToExpire;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public boolean isSatisfiedByAny() {
		return satisfiedByAny;
	}

	public void setSatisfiedByAny(boolean satisfiedByAny) {
		this.satisfiedByAny = satisfiedByAny;
	}
}
