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

import java.util.List;
import java.util.Locale;

import org.openmrs.Encounter;
import org.openmrs.Visit;
import org.openmrs.api.EncounterService;
import org.openmrs.api.context.Context;

/**
 * This is the contract for different ways to determine if an Encounter should
 * be part of an Active visit. An admin can decide how they want their
 * Encounters automatically assigned to Visits by choosing different handlers.
 * 
 * @see EncounterService#getVisitAssignmentHandlers()
 * @since 1.9
 */
public interface EncounterToVisitAssignmentHandler {

	/**
	 * @param locale
	 *            optional locale to specify. If none is passed,
	 *            {@link Context#getLocale()} should be used
	 * @return a displayable string so that users can pick between different
	 *         assignment handlers
	 */
	public String getDisplayName(Locale locale);

	/**
	 * Implementations of this method should look at the given
	 * <code>encounter</code> and choose whether or not it should be assigned to
	 * a {@link Visit} that is already open or if it should be part of a new
	 * visit. <br/>
	 * <br/>
	 * If a null value is returned, the encounter will not be associated with
	 * any Visit.<br/>
	 * If the handler wants a new Visit opened, it should instantiate a new
	 * Visit and set the desired properties on it<br/>
	 * If the handler wants to use a current visit, a Visit in the
	 * <code>activeVisits</code> list should be returned (without being
	 * modified)
	 * 
	 * @param activeVisits
	 *            the patient's currently open visits
	 * @param enc
	 *            the new encounter in question of whether to assign to a visit
	 * @return the {@link Visit} that <code>encounter</code> will be assigned to
	 *         or null if none
	 */
	public Visit getVisitForEncounter(List<Visit> activeVisits,
			Encounter encounter);

}
