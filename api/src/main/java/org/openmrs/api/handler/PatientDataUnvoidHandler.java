/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.handler;

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

import java.util.Date;
import java.util.List;

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
			List<Order> orders = os.getAllOrdersByPatient(patient);
			if (CollectionUtils.isNotEmpty(orders)) {
				for (Order order : orders) {
					if (order.isVoided() && order.getDateVoided().equals(origParentVoidedDate)
					        && order.getVoidedBy().equals(originalVoidingUser)) {
						os.unvoidOrder(order);
					}
				}
			}
		}
	}
}
