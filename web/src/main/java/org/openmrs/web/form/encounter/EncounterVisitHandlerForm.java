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
package org.openmrs.web.form.encounter;

import org.openmrs.web.controller.encounter.EncounterVisitHandlerFormController;

/**
 * Form used by {@link EncounterVisitHandlerFormController}.
 */
public class EncounterVisitHandlerForm {
	
	private String encounterVisitHandler;
	
	private boolean enableVisits;
	
	/**
	 * @return the encounterVisitHandler
	 */
	public String getEncounterVisitHandler() {
		return encounterVisitHandler;
	}
	
	/**
	 * @param encounterVisitHandler the encounterVisitHandler to set
	 */
	public void setEncounterVisitHandler(String encounterVisitHandler) {
		this.encounterVisitHandler = encounterVisitHandler;
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
	
}
