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
import org.openmrs.api.context.Context;

/**
 * This class deals with {@link Patient} objects when they are voided via a void* method in an
 * Openmrs Service. This handler is automatically called by the {@link RequiredDataAdvice} AOP
 * class. <br/>
 * The handler voids all the encounters(including their associated observations) and orders
 * associated with the specified patient object
 * 
 * @see RequiredDataHandler
 * @see VoidHandler
 * @see Patient
 * @since 1.9
 */
@Handler(supports = Patient.class)
public class PatientDataVoidHandler implements VoidHandler<Patient> {
	
	/**
	 * @see org.openmrs.api.handler.VoidHandler#handle(org.openmrs.Voidable, org.openmrs.User,
	 *      java.util.Date, java.lang.String)
	 * @should void the orders encounters and observations associated with the patient
	 */
	@Override
	public void handle(Patient patient, User voidingUser, Date voidedDate, String voidReason) {
		//void all the encounters associated with this patient
		EncounterService es = Context.getEncounterService();
		List<Encounter> encounters = es.getEncountersByPatient(patient);
		if (CollectionUtils.isNotEmpty(encounters)) {
			for (Encounter encounter : encounters) {
				if (!encounter.isVoided()) {
					// EncounterServiceImpl.voidEncounter and the requiredDataAdvice will set dateVoided to current date 
					//if it is null, we need to set it now to match the patient's date voided so that the unvoid 
					//handler's logic doesn't fail when comparing dates while unvoiding encounters that were voided 
					//with the patient
					encounter.setDateVoided(patient.getDateVoided());
					es.voidEncounter(encounter, voidReason);
				}
			}
		}
		//void all the orders associated with this patient
		OrderService os = Context.getOrderService();
		List<Order> orders = os.getOrdersByPatient(patient);
		if (CollectionUtils.isNotEmpty(orders)) {
			for (Order order : orders) {
				if (!order.isVoided()) {
					order.setDateVoided(patient.getDateVoided());
					os.voidOrder(order, voidReason);
				}
			}
		}
	}
}
