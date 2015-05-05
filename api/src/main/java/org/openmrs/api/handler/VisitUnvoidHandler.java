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
