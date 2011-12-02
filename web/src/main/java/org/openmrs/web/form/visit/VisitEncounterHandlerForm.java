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
package org.openmrs.web.form.visit;

import org.openmrs.web.controller.visit.VisitEncounterHandlerFormController;

/**
 * Form used by {@link VisitEncounterHandlerFormController}.
 */
public class VisitEncounterHandlerForm {
	
	private String visitEncounterHandler;
	
	private boolean enableVisits;
	
	private boolean closeVisitsTaskStarted;
	
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
}
