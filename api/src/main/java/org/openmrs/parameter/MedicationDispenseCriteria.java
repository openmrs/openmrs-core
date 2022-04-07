/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.parameter;

import org.openmrs.DrugOrder;
import org.openmrs.Encounter;
import org.openmrs.Patient;

/**
 * The search parameter object for MedicationDispense. A convenience interface for building
 * instances is provided by {@link MedicationDispenseCriteriaBuilder}.
 * @since 2.6.0
 * @see MedicationDispenseCriteriaBuilder
 */
public class MedicationDispenseCriteria {
	
	private Patient patient;
	
	private Encounter encounter;
	
	private DrugOrder drugOrder;
	
	private boolean includeVoided = false;
	
	public MedicationDispenseCriteria() {
	}
	
	public Patient getPatient() {
		return patient;
	}
	
	public void setPatient(Patient patient) {
		this.patient = patient;
	}
	
	public Encounter getEncounter() {
		return encounter;
	}
	
	public void setEncounter(Encounter encounter) {
		this.encounter = encounter;
	}
	
	public DrugOrder getDrugOrder() {
		return drugOrder;
	}
	
	public void setDrugOrder(DrugOrder drugOrder) {
		this.drugOrder = drugOrder;
	}
	
	public boolean isIncludeVoided() {
		return includeVoided;
	}
	
	public void setIncludeVoided(boolean includeVoided) {
		this.includeVoided = includeVoided;
	}
}
