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
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.openmrs.Obs;
import org.openmrs.api.ObsService;
import org.openmrs.api.context.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
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
	 * @param obs observation object containing variables to be used in the criteria
	 * @return true if the criteria evaluates to true, false otherwise
	 */
	public static boolean evaluateCriteria(String criteria, Obs obs) {
		if (criteria.isEmpty() || obs == null) {
			return false;
		}
		
		VelocityContext velocityContext = new VelocityContext();
		velocityContext.put("fn", new ConceptReferenceRangeUtility());
		
		if (obs.getPerson() != null) {
			velocityContext.put("patient", obs.getPerson());
			velocityContext.put("person", obs.getPerson());
		}
		
		VelocityEngine velocityEngine = new VelocityEngine();
		
		StringWriter writer = new StringWriter();
		velocityEngine.evaluate(velocityContext, writer, ConceptReferenceRangeUtility.class.getName(), criteria);
		
		String evaluatedCriteria = writer.toString();
		
		try {
			// Evaluate the resulting criteria using a simple boolean evaluation
			return Boolean.parseBoolean(evaluatedCriteria);
		}
		catch (Exception e) {
			System.err.println("Error evaluating criteria: " + e.getMessage());
			return false;
		}
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
