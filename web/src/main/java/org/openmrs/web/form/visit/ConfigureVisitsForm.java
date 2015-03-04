/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.form.visit;

import java.util.ArrayList;
import java.util.List;

import org.openmrs.VisitType;
import org.openmrs.web.controller.visit.ConfigureVisitsFormController;

/**
 * Form used by {@link ConfigureVisitsFormController}.
 */
public class ConfigureVisitsForm {
	
	private String visitEncounterHandler;
	
	private boolean enableVisits;
	
	private boolean closeVisitsTaskStarted;
	
	private List<VisitType> visitTypesToClose;
	
	/**
	 * @return the visitEncounterHandler
	 */
	public String getVisitEncounterHandler() {
		return visitEncounterHandler;
	}
	
	/**
	 * @param visitEncounterHandler the visitEncounterHandler to set
	 */
	public void setVisitEncounterHandler(String visitEncounterHandler) {
		this.visitEncounterHandler = visitEncounterHandler;
	}
	
	/**
	 * @return the enableVisits
	 */
	public boolean isEnableVisits() {
		return enableVisits;
	}
	
	/**
	 * @param enableVisits the enableVisits to set
	 */
	public void setEnableVisits(boolean enableVisits) {
		this.enableVisits = enableVisits;
	}
	
	/**
	 * @return the closeVisitsTaskStarted
	 */
	public boolean getCloseVisitsTaskStarted() {
		return closeVisitsTaskStarted;
	}
	
	/**
	 * @param closeVisitsTaskStarted the closeVisitsTaskStarted to set
	 */
	public void setCloseVisitsTaskStarted(boolean closeVisitsTaskStarted) {
		this.closeVisitsTaskStarted = closeVisitsTaskStarted;
	}
	
	/**
	 * @return the visitTypesToClose
	 */
	public List<VisitType> getVisitTypesToClose() {
		if (visitTypesToClose == null) {
			visitTypesToClose = new ArrayList<VisitType>();
		}
		return visitTypesToClose;
	}
	
	/**
	 * @param visitTypesToClose the visitTypesToClose to set
	 */
	public void setVisitTypesToClose(List<VisitType> visitTypesToClose) {
		this.visitTypesToClose = visitTypesToClose;
	}
}
