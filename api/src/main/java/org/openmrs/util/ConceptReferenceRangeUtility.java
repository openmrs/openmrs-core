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

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.openmrs.Obs;
import org.openmrs.Person;
import org.openmrs.api.ObsService;
import org.openmrs.api.context.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringWriter;
import java.time.LocalTime;

/**
 * A utility class that evaluates the concept ranges 
 */
public class ConceptReferenceRangeUtility {
	
	private static final Logger logger = LoggerFactory.getLogger(ConceptReferenceRangeUtility.class);
	
	public ConceptReferenceRangeUtility() {
	}
	
	/**
	 * This method evaluates the given criteria against the provided context.
	 *
	 * @param criteria the criteria string to evaluate
	 * @param person person object containing variables to be used in the criteria
	 * @return true if the criteria evaluates to true, false otherwise
	 */
	public static boolean evaluateCriteria(String criteria, Person person) {
		if (criteria == null || criteria.isEmpty() || person == null) {
			return true;
		}
		
		VelocityContext velocityContext = new VelocityContext();
		velocityContext.put("fn", new ConceptReferenceRangeUtility());
		
		velocityContext.put("patient", person);
		
		VelocityEngine velocityEngine = new VelocityEngine();
		
		StringWriter writer = new StringWriter();
		velocityEngine.evaluate(velocityContext, writer, ConceptReferenceRangeUtility.class.getName(), criteria);

		String evaluatedCriteria = writer.toString();
		
		return Boolean.parseBoolean(evaluatedCriteria);
	}
	
	/**
	 * Gets the latest Obs by concept.
	 *
	 * @param conceptId the concept
	 * @return Obs
	 */
	public Obs getLatestObsByConceptId(String conceptId) {
		ObsService obsService = Context.getObsService();
		
		return obsService.getLatestObsByConceptId(conceptId);
	}
	
	/**
	 * Gets the time of the day in hours.
	 *
	 * @return the hour of the day (e.g. 14 to mean 2pm)
	 */
	public static int getTimeOfTheDay() {
		return LocalTime.now().getHour();
	}
}
