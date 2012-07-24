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

import org.openmrs.Encounter;
import org.openmrs.User;
import org.openmrs.Visit;
import org.openmrs.annotation.Handler;
import org.openmrs.aop.RequiredDataAdvice;
import org.openmrs.api.context.Context;

/**
 * This class sets the void attributes on the given {@link Visit} object when an unvoid* method is
 * called with this class. This differs from the {@link BaseUnvoidHandler} because unvoiding the
 * Visit object implies unvoiding encounters voided with that visit.
 * 
 * @see RequiredDataAdvice
 * @see UnvoidHandler
 * @since 1.9
 */
@Handler(supports = Visit.class)
public class VisitUnvoidHandler implements UnvoidHandler<Visit> {
	
	@Override
	public void handle(Visit visit, User voidingUser, Date origParentVoidedDate, String unused) {
		List<Encounter> encountersByVisit = Context.getEncounterService().getEncountersByVisit(visit, true);
		for (Encounter encounter : encountersByVisit) {
			if (encounter.isVoided() && encounter.getDateVoided().equals(visit.getDateVoided())
			        && encounter.getVoidReason().equals(visit.getVoidReason())) {
				Context.getEncounterService().unvoidEncounter(encounter);
			}
		}
	}
	
}
