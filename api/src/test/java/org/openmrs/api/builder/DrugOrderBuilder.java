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

import java.util.Date;

import org.openmrs.DosingInstructions;
import org.openmrs.DrugOrder;
import org.openmrs.Order;
import org.openmrs.api.ConceptService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.OrderService;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;

public class DrugOrderBuilder {
	
	private final DrugOrder drugOrder;
	
	private final PatientService patientService;
	
	private final OrderService orderService;
	
	private final EncounterService encounterService;
	
	private final ConceptService conceptService;
	
	public DrugOrderBuilder() {
		patientService = Context.getPatientService();
		conceptService = Context.getConceptService();
		orderService = Context.getOrderService();
		encounterService = Context.getEncounterService();
		drugOrder = new DrugOrder();
	}
	
	public DrugOrder build() {
		return drugOrder;
	}
	
	public DrugOrderBuilder withDrug(Integer drugID) {
		drugOrder.setDrug(conceptService.getDrug(drugID));
		return this;
	}
	
	public DrugOrderBuilder withAutoExpireDate(Date autoExpireDate) {
		drugOrder.setAutoExpireDate(autoExpireDate);
		return this;
	}
	
	public DrugOrderBuilder withDosingInstructions(String dosingInstructions) {
		drugOrder.setDosingInstructions(dosingInstructions);
		return this;
	}
	
	public DrugOrderBuilder withDosingType(Class<? extends DosingInstructions> dosingType) {
		drugOrder.setDosingType(dosingType);
		return this;
	}
	
	public DrugOrderBuilder withQuantity(Double quantity) {
		drugOrder.setQuantity(quantity);
		return this;
	}
	
	public DrugOrderBuilder withQuantityUnits(Integer quantityUnitsID) {
		drugOrder.setQuantityUnits(conceptService.getConcept(quantityUnitsID));
		return this;
	}
	
	public DrugOrderBuilder withNumRefills(Integer numRefills) {
		drugOrder.setNumRefills(numRefills);
		return this;
	}
	
	public DrugOrderBuilder withPatient(Integer patientID) {
		drugOrder.setPatient(patientService.getPatient(patientID));
		return this;
	}
	
	public DrugOrderBuilder withConcept(Integer conceptID) {
		drugOrder.setConcept(conceptService.getConcept(conceptID));
		return this;
	}
	
	public DrugOrderBuilder withCareSetting(Integer careSettingID) {
		drugOrder.setCareSetting(orderService.getCareSetting(careSettingID));
		return this;
	}
	
	public DrugOrderBuilder withOrderer(Integer orderID) {
		drugOrder.setOrderer(orderService.getOrder(orderID).getOrderer());
		return this;
	}
	
	public DrugOrderBuilder withEncounter(Integer encounterID) {
		drugOrder.setEncounter(encounterService.getEncounter(encounterID));
		return this;
	}
	
	public DrugOrderBuilder withDateActivated(Date date) {
		drugOrder.setDateActivated(date);
		return this;
	}
	
	public DrugOrderBuilder withOrderType(Integer orderTypeID) {
		drugOrder.setOrderType(orderService.getOrderType(orderTypeID));
		return this;
	}
	
	public DrugOrderBuilder withUrgency(Order.Urgency urgency) {
		drugOrder.setUrgency(urgency);
		return this;
	}
	
	public DrugOrderBuilder withScheduledDate(Date date) {
		drugOrder.setScheduledDate(date);
		return this;
	}
}
