/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.api.builder;

import org.openmrs.DrugOrder;
import org.openmrs.Encounter;
import org.openmrs.MedicationDispense;
import org.openmrs.api.ConceptService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.OrderService;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;

import java.util.Date;

public class MedicationDispenseBuilder {
	
	private final MedicationDispense medicationDispense;
	
	private final PatientService patientService;
	
	private final OrderService orderService;
	
	private final EncounterService encounterService;
	
	private final ConceptService conceptService;
	
	public MedicationDispenseBuilder() {
		patientService = Context.getPatientService();
		conceptService = Context.getConceptService();
		orderService = Context.getOrderService();
		encounterService = Context.getEncounterService();
		medicationDispense = new MedicationDispense();
	}
	
	public MedicationDispense build() {
		return medicationDispense;
	}
	
	public MedicationDispenseBuilder withUuid(String uuid) {
		medicationDispense.setUuid(uuid);
		return this;
	}
	
	public MedicationDispenseBuilder withPatient(Integer patientID) {
		medicationDispense.setPatient(patientService.getPatient(patientID));
		return this;
	}
	
	public MedicationDispenseBuilder withConcept(Integer conceptID) {
		medicationDispense.setConcept(conceptService.getConcept(conceptID));
		return this;
	}
	
	public MedicationDispenseBuilder withStatus(Integer statusConceptId, Integer statusReasonConceptId) {
		medicationDispense.setStatus(conceptService.getConcept(statusConceptId));
		if (statusReasonConceptId != null) {
			medicationDispense.setStatusReason(conceptService.getConcept(statusReasonConceptId));
		} else {
			medicationDispense.setStatusReason(null);
		}
		return this;
	}
	
	public MedicationDispenseBuilder withType(Integer conceptID) {
		medicationDispense.setType(conceptService.getConcept(conceptID));
		return this;
	}
	
	public MedicationDispenseBuilder withEncounter(Integer encounterID) {
		Encounter e = encounterService.getEncounter(encounterID);
		medicationDispense.setEncounter(e);
		medicationDispense.setLocation(e.getLocation());
		medicationDispense.setDispenser(e.getActiveEncounterProviders().iterator().next().getProvider());
		return this;
	}
	
	public MedicationDispenseBuilder withLocation(Integer locationId) {
		medicationDispense.setLocation(Context.getLocationService().getLocation(locationId));
		return this;
	}
	
	public MedicationDispenseBuilder withDispenser(Integer providerId) {
		medicationDispense.setDispenser(Context.getProviderService().getProvider(providerId));
		return this;
	}
	
	public MedicationDispenseBuilder withDrugOrder(Integer drugOrderId) {
		DrugOrder drugOrder = (DrugOrder) orderService.getOrder(drugOrderId);
		medicationDispense.setDrugOrder(drugOrder);
		medicationDispense.setDrug(drugOrder.getDrug());
		medicationDispense.setQuantity(drugOrder.getQuantity());
		medicationDispense.setQuantityUnits(drugOrder.getQuantityUnits());
		medicationDispense.setDose(drugOrder.getDose());
		medicationDispense.setDoseUnits(drugOrder.getDoseUnits());
		medicationDispense.setRoute(drugOrder.getRoute());
		medicationDispense.setFrequency(drugOrder.getFrequency());
		medicationDispense.setAsNeeded(drugOrder.getAsNeeded());
		medicationDispense.setDosingInstructions(drugOrder.getDosingInstructions());
		return this;
	}
	
	public MedicationDispenseBuilder withDrug(Integer drugID, Integer substitutionTypeConceptId,
	        Integer substitutionReasonConceptId) {
		medicationDispense.setDrug(conceptService.getDrug(drugID));
		if (substitutionTypeConceptId != null) {
			medicationDispense.setSubstitutionType(conceptService.getConcept(substitutionTypeConceptId));
		} else {
			medicationDispense.setSubstitutionType(null);
		}
		if (substitutionReasonConceptId != null) {
			medicationDispense.setSubstitutionReason(conceptService.getConcept(substitutionReasonConceptId));
		} else {
			medicationDispense.setSubstitutionReason(null);
		}
		return this;
	}
	
	public MedicationDispenseBuilder withDatePrepared(Date datePrepared) {
		medicationDispense.setDatePrepared(datePrepared);
		return this;
	}
	
	public MedicationDispenseBuilder withDateHandedOver(Date dateHandedOver) {
		medicationDispense.setDateHandedOver(dateHandedOver);
		return this;
	}
	
	public MedicationDispenseBuilder withFormNamespaceAndPath(String formNamespaceAndPath) {
		medicationDispense.setFormNamespaceAndPath(formNamespaceAndPath);
		return this;
	}
}
