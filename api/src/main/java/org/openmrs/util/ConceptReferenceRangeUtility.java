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
import org.openmrs.api.ValidationException;
import org.openmrs.api.context.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringWriter;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

/**
 * A utility class that evaluates the concept ranges 
 */
public class ConceptReferenceRangeUtility {
	
	private static final Logger logger = LoggerFactory.getLogger(ConceptReferenceRangeUtility.class);
	
	private static final String YEARS_STRING = "YEARS";
	
	public ConceptReferenceRangeUtility() {
	}
	
	/**
	 * This method evaluates the given criteria against the provided context.
	 *
	 * @param criteria the criteria string to evaluate e.g. "$fn.getAge($patient, 'YEARS') > 1 && $fn.getAge($patient, 'YEARS') < 10"
	 * @param person person object containing variables to be used in the criteria
	 * @return true if the criteria evaluates to true, false otherwise
	 */
	public static boolean evaluateCriteria(String criteria, Person person) {
		if (person == null) {
			throw new ValidationException("Failed to validate with reason: patient is null");
		}
		
		if (criteria == null || criteria.isEmpty()) {
			throw new IllegalArgumentException("Failed to validate with reason: criteria required");
		}
		
		VelocityContext velocityContext = new VelocityContext();
		velocityContext.put("fn", new ConceptReferenceRangeUtility());
		
		velocityContext.put("patient", person);
		
		VelocityEngine velocityEngine = new VelocityEngine();
		
		StringWriter writer = new StringWriter();
		String wrappedCriteria = "#set( $criteria = " + criteria + " )$criteria";
		velocityEngine.evaluate(velocityContext, writer, ConceptReferenceRangeUtility.class.getName(), wrappedCriteria);
		
		String evaluatedCriteria = writer.toString();
		
		return Boolean.parseBoolean(evaluatedCriteria);
	}
	
	/**
	 * Default method to get the age of a person in years.
	 *
	 * @param person the person
	 * @return the age in years as an Integer
	 */
	public static Integer getAge(Person person) {
		return getAge(person, YEARS_STRING);
	}
	
	/**
	 * Gets the age of a person with a specified precision.
	 *
	 * @param person     the person
	 * @param chronoUnitString the unit of precision for the age calculation (WEEKS, MONTHS, YEARS)
	 * @return the age in the specified unit as an Integer
	 */
	public static Integer getAge(Person person, String chronoUnitString) {
		if (person == null || person.getBirthdate() == null) {
			return null;
		}
		
		ChronoUnit chronoUnit = getChronoUnitFromString(chronoUnitString);
		
		LocalDate birthDate = new java.sql.Date(person.getBirthdate().getTime()).toLocalDate();
		LocalDate endDate = LocalDate.now();
		
		// If date given is after date of death then use date of death as end date
		if (person.getDeathDate() != null) {
			LocalDate deathDate = new java.sql.Date(person.getDeathDate().getTime()).toLocalDate();
			if (endDate.isAfter(deathDate)) {
				endDate = deathDate;
			}
		}
		
		long age;
		
		switch (chronoUnit) {
			case WEEKS:
				age = ChronoUnit.WEEKS.between(birthDate, endDate);
				break;
			case MONTHS:
				age = ChronoUnit.MONTHS.between(birthDate, endDate);
				break;
			case YEARS:
				age = ChronoUnit.YEARS.between(birthDate, endDate);
				break;
			default:
				throw new IllegalArgumentException("Unsupported ChronoUnit: " + chronoUnit);
		}
		
		return (int) age;
	}
	
	private static ChronoUnit getChronoUnitFromString(String chronoUnitString) {
		if (chronoUnitString == null) {
			logger.error("Validation failed with reason: ChronoUnit string is null");
			throw new ValidationException("Failed to validate with reason: Criteria is not valid.");
		}
		
		switch (chronoUnitString.toUpperCase()) {
			case "YEARS":
				return ChronoUnit.YEARS;
			case "MONTHS":
				return ChronoUnit.MONTHS;
			case "WEEKS":
				return ChronoUnit.WEEKS;
			case "DAYS":
				return ChronoUnit.DAYS;
			default:
				throw new ValidationException("Unsupported ChronoUnit string");
		}
	}
	
	/**
	 * Gets the latest Obs by concept.
	 *
	 * @param conceptId the concept
	 * @return Obs
	 */
	public Obs getLatestObsByConceptId(String conceptId) {
		return Context.getObsService().getLatestObsByConceptId(conceptId);
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
