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

import java.util.Locale;

import org.openmrs.Encounter;
import org.openmrs.Visit;
import org.openmrs.api.EncounterService;
import org.openmrs.api.context.Context;

/**
 * This is the contract for different ways to determine if an Encounter should be part of an Active
 * visit. An admin can decide how they want their Encounters automatically assigned to Visits by
 * choosing different handlers.
 * 
 * @see EncounterService#getVisitAssignmentHandlers()
 * @since 1.9
 */
public interface EncounterVisitHandler {
	
	/**
	 * @return a displayable string so that users can pick between different assignment handlers
	 */
	public String getDisplayName();
	
	/**
	 * @param locale optional locale to specify. If none is passed, {@link Context#getLocale()}
	 *            should be used
	 * @return a displayable string so that users can pick between different assignment handlers
	 */
	public String getDisplayName(Locale locale);
	
	/**
	 * Implementations of this method should look at the given <code>encounter</code> and choose
	 * whether or not it should be assigned to a {@link Visit} that is already open or if it should
	 * be part of a new visit. <br/>
	 * <br/>
	 * The decision of what to do is up to the handler, but it should call
	 * {@link Encounter#setVisit(Visit)} with the outcome. The visit assigned to the encounter will
	 * be persisted to the database after this method is returned, so the handler is not required to
	 * save it.
	 * 
	 * @param encounter the new unsaved encounter in question of whether to assign to a visit
	 */
	public void beforeCreateEncounter(Encounter encounter);
	
}
