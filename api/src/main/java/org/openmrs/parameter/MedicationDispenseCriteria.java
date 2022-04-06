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

import org.openmrs.Concept;
import org.openmrs.Drug;
import org.openmrs.DrugOrder;
import org.openmrs.Encounter;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.Provider;

import java.util.Date;
import java.util.List;

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
	
	// TODO: Consider what else we might want to add here to start out with, eg:
	private List<Concept> concept;
	private List<Drug> drug;
	private List<Location> location;
	private List<Provider> provider;
	private List<Concept> status;
	private List<Concept> statusReason;
	private List<Concept> type;
	private Date orderDateActivatedFrom;
	private Date orderDateActivatedTo;
	private Date encounterDateFrom;
	private Date encounterDateTo;
	private Date datePreparedFrom;
	private Date datePreparedTo;
	private Date dateHandedOverFrom;
	private Date dateHandedOverTo;
	private Boolean substituted;
	private List<Concept> substitutionType;
	private List<Concept> substitutionReason;
	
	public MedicationDispenseCriteria() {}

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
