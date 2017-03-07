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

import org.openmrs.Order;
import org.openmrs.OrderGroup;
import org.openmrs.api.ConceptService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.OrderService;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;

public class OrderBuilder {
	
	protected Order order;
	
	private final PatientService patientService;
	
	private final OrderService orderService;
	
	private final EncounterService encounterService;
	
	public ConceptService conceptService;
	
	public OrderBuilder() {
		patientService = Context.getPatientService();
		conceptService = Context.getConceptService();
		orderService = Context.getOrderService();
		encounterService = Context.getEncounterService();
		order = new Order();
	}
	
	public Order build() {
		return order;
	}
	
	public OrderBuilder withAction(Order.Action action) {
		order.setAction(action);
		return this;
	}
	
	public OrderBuilder withPatient(Integer patientID) {
		order.setPatient(patientService.getPatient(patientID));
		return this;
	}
	
	public OrderBuilder withConcept(Integer conceptID) {
		order.setConcept(conceptService.getConcept(conceptID));
		return this;
	}
	
	public OrderBuilder withCareSetting(Integer careSettingID) {
		order.setCareSetting(orderService.getCareSetting(careSettingID));
		return this;
	}
	
	public OrderBuilder withOrderer(Integer orderID) {
		order.setOrderer(orderService.getOrder(orderID).getOrderer());
		return this;
	}
	
	public OrderBuilder withEncounter(Integer encounterID) {
		order.setEncounter(encounterService.getEncounter(encounterID));
		return this;
	}
	
	public OrderBuilder withDateActivated(Date date) {
		order.setDateActivated(date);
		return this;
	}
	
	public OrderBuilder withOrderType(Integer orderTypeID) {
		order.setOrderType(orderService.getOrderType(orderTypeID));
		return this;
	}
	
	public OrderBuilder withUrgency(Order.Urgency urgency) {
		order.setUrgency(urgency);
		return this;
	}
	
	public OrderBuilder withScheduledDate(Date date) {
		order.setScheduledDate(date);
		return this;
	}
	
	public OrderBuilder withOrderGroup(OrderGroup orderGroup) {
		order.setOrderGroup(orderGroup);
		return this;
	}
}
