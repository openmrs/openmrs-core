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
