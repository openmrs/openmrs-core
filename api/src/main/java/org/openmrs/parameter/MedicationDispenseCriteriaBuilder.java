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
 * A convenience builder for {@link MedicationDispenseCriteria}. Create a builder, set
 * its properties to desired values and finally call {@link #build()}
 * to create the actual search criteria instance.
 * @since 2.6.0
 * @see MedicationDispenseCriteria
 */
public class MedicationDispenseCriteriaBuilder {
	
	private final MedicationDispenseCriteria criteria;
	
	public MedicationDispenseCriteriaBuilder() {
		criteria = new MedicationDispenseCriteria();
	}
	
	public MedicationDispenseCriteriaBuilder setPatient(Patient patient) {
		criteria.setPatient(patient);
		return this;
	}
	
	public MedicationDispenseCriteriaBuilder setEncounter(Encounter encounter) {
		criteria.setEncounter(encounter);
		return this;
	}
	
	public MedicationDispenseCriteriaBuilder setDrugOrder(DrugOrder drugOrder) {
		criteria.setDrugOrder(drugOrder);
		return this;
	}
	
	public MedicationDispenseCriteriaBuilder setIncludeVoided(boolean includeVoided) {
		criteria.setIncludeVoided(includeVoided);
		return this;
	}
	
	public MedicationDispenseCriteria build() {
		return criteria;
	}
}
