/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.util;

import org.apache.commons.lang.StringUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.ParseErrorException;
import org.joda.time.LocalTime;
import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.Person;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;

import java.io.StringWriter;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * A utility class that evaluates the concept ranges 
 * 
 * @since 2.7.0
 */
public class ConceptReferenceRangeUtility {
	
	public ConceptReferenceRangeUtility() {
	}
	
	/**
	 * This method evaluates the given criteria against the provided person/patient.
	 *
	 * @param criteria the criteria string to evaluate e.g. "$patient.getAge() > 1"
	 * @param obs The observation (Obs) object containing the values to be used in the criteria.
	 *                  
	 * @return true if the criteria evaluates to true, false otherwise
	 */
	public boolean evaluateCriteria(String criteria, Obs obs) {
		if (obs == null) {
			throw new IllegalArgumentException("Failed to evaluate criteria with reason: Obs is null");
		}
		
		if (obs.getPerson() == null) {
			throw new IllegalArgumentException("Failed to evaluate criteria with reason: patient is null");
		}
		
		if (StringUtils.isBlank(criteria)) {
			throw new IllegalArgumentException("Failed to evaluate criteria with reason: criteria is empty");
		}
		
		VelocityContext velocityContext = new VelocityContext();
		velocityContext.put("fn", this);
		velocityContext.put("obs", obs);
		
		velocityContext.put("patient", obs.getPerson());
		
		VelocityEngine velocityEngine = new VelocityEngine();
		
		StringWriter writer = new StringWriter();
		String wrappedCriteria = "#set( $criteria = " + criteria + " )$criteria";
		
		try {
			velocityEngine.evaluate(velocityContext, writer, ConceptReferenceRangeUtility.class.getName(), wrappedCriteria);
			return Boolean.parseBoolean(writer.toString());
		}
		catch (ParseErrorException e) {
			throw new APIException("An error occurred while evaluating criteria: Invalid criteria: " + criteria, e);
		}
		catch (Exception e) {
			throw new APIException("An error occurred while evaluating criteria: ", e);
		}
	}
	
	/**
	 * Gets the latest Obs by concept.
	 *
	 * @param conceptRef can be either concept uuid or conceptMap's code and sourceName 
	 *                   e.g "bac25fd5-c143-4e43-bffe-4eb1e7efb6ce" or "CIEL:1434"
	 * @param person person to get obs for
	 *                   
	 * @return Obs
	 */
	public Obs getLatestObs(String conceptRef, Person person) {
		Concept concept = Context.getConceptService().getConceptByReference(conceptRef);

		if (concept != null) {
			List<Obs> observations = Context.getObsService().getObservations(
				Collections.singletonList(person), 
				null, 
				Collections.singletonList(concept), 
				null, 
				null, 
				null,
				Collections.singletonList("dateCreated"), 
				1, 
				null,
				null, 
				null, 
				false
			);

			return observations.isEmpty() ? null : observations.get(0);
		}

		return null;
	}
	
	/**
	 * Gets the time of the day in hours.
	 *
	 * @return the hour of the day in 24hr format (e.g. 14 to mean 2pm)
	 */
	public int getCurrentHour() {
		return LocalTime.now().getHourOfDay();
	}
	
	/**
	 * Retrieves the most relevant Obs for the given current Obs and conceptRef. If the current Obs contains a valid value 
	 * (coded, numeric, date, text e.t.c) and the concept in Obs is the same as the supplied concept,
	 * the method returns the current Obs. Otherwise, it fetches the latest Obs for the supplied concept and patient.
	 *
	 * @param currentObs the current Obs being evaluated
	 * @return the most relevant Obs based on the current Obs, or the latest Obs if the current one has no valid value
	 */
	public Obs getCurrentObs(String conceptRef, Obs currentObs) {
		Concept concept = Context.getConceptService().getConceptByReference(conceptRef);
		
		if (currentObs.getValueAsString(Locale.ENGLISH).isEmpty() 
			&& (concept != null && concept == currentObs.getConcept())) {
			return currentObs;
		} else {
			return getLatestObs(conceptRef, currentObs.getPerson());
		}
	}
}
