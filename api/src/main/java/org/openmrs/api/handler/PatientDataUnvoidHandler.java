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
package org.openmrs.api.handler;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.openmrs.Encounter;
import org.openmrs.Order;
import org.openmrs.Patient;
import org.openmrs.User;
import org.openmrs.annotation.Handler;
import org.openmrs.aop.RequiredDataAdvice;
import org.openmrs.api.EncounterService;
import org.openmrs.api.OrderService;
import org.openmrs.api.OrderService.ORDER_STATUS;
import org.openmrs.api.context.Context;

/**
 * This class deals with {@link Patient} objects when they are unvoided via the unvoid* method in an
 * Openmrs Service. This handler is automatically called by the {@link RequiredDataAdvice} AOP
 * class. <br/>
 * The handler unvoids all the encounters(including their associated observations) and orders
 * associated to the specified patient object that got voided because the patient was getting voided
 * 
 * @see RequiredDataHandler
 * @see UnvoidHandler
 * @see Patient
 * @since 1.9
 */
@Handler(supports = Patient.class)
public class PatientDataUnvoidHandler implements UnvoidHandler<Patient> {
	
	@SuppressWarnings("unchecked")
	@Override
	public void handle(Patient patient, User originalVoidingUser, Date origParentVoidedDate, String unused) {
		//can't be unvoiding a patient that doesn't exist in the database
		if (patient.getId() != null) {
			//unvoid all the encounter that got voided as a result of the patient getting voided
			EncounterService es = Context.getEncounterService();
			List<Encounter> encounters = es.getEncounters(patient, null, null, null, null, null, null, true);
			if (CollectionUtils.isNotEmpty(encounters)) {
				for (Encounter encounter : encounters) {
					if (encounter.isVoided() && encounter.getDateVoided().equals(origParentVoidedDate)
					        && encounter.getVoidedBy().equals(originalVoidingUser)) {
						es.unvoidEncounter(encounter);
					}
				}
			}
			
			//unvoid all the orders that got voided as a result of the patient getting voided
			OrderService os = Context.getOrderService();
			List<Patient> patients = new ArrayList<Patient>();
			patients.add(patient);
			List<Order> orders = os.getOrders(Order.class, patients, null, ORDER_STATUS.ANY, null, null, null);
			if (CollectionUtils.isNotEmpty(orders)) {
				for (Order order : orders) {
					if (order.isVoided() && order.getDateVoided().equals(origParentVoidedDate)
					        && order.getVoidedBy().equals(originalVoidingUser))
						os.unvoidOrder(order);
				}
			}
		}
	}
}
