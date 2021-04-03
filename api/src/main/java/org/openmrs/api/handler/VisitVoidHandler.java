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
 * This class sets the void attributes on the given {@link Visit} object when a void* method is
 * called with this class. This differs from the {@link BaseVoidHandler} because voiding the Visit
 * object implies voiding encounters.
 * 
 * @see RequiredDataAdvice
 * @see VoidHandler
 * @since 1.9
 */
@Handler(supports = Visit.class)
public class VisitVoidHandler implements VoidHandler<Visit> {
	
	@Override
	public void handle(Visit voidableObject, User voidingUser, Date voidedDate, String voidReason) {
		List<Encounter> encountersByVisit = Context.getEncounterService().getEncountersByVisit(voidableObject, false);
		for (Encounter encounter : encountersByVisit) {
			encounter.setDateVoided(voidedDate);
			Context.getEncounterService().voidEncounter(encounter, voidReason);
		}
	}
	
}
