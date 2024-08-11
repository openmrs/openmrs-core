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
import org.joda.time.LocalTime;
import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.Person;
import org.openmrs.api.context.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringWriter;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * A utility class that evaluates the concept ranges 
 * 
 * @since 2.7.0
 */
public class ConceptReferenceRangeUtility {
	
	private static final Logger logger = LoggerFactory.getLogger(ConceptReferenceRangeUtility.class);
	
	public ConceptReferenceRangeUtility() {
	}
	
	/**
	 * This method evaluates the given criteria against the provided person/patient.
	 *
	 * @param criteria the criteria string to evaluate e.g. "$fn.getAge($patient, 'YEARS') > 1 && $fn.getAge($patient, 'YEARS') < 10"
	 * @param person person object containing attributes to be used in the criteria
	 *                  
	 * @return true if the criteria evaluates to true, false otherwise
	 * 
	 * @since 2.7.0
	 */
	public boolean evaluateCriteria(String criteria, Person person) {
		if (person == null) {
			throw new IllegalArgumentException("Failed to validate with reason: patient is null");
		}
		
		if (StringUtils.isEmpty(criteria)) {
			throw new IllegalArgumentException("Failed to validate with reason: criteria required");
		}
		
		criteria = improveCriteria(criteria);
		
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
	 * Method is used to improve/build criteria so that a user won't have to add "patient" arg to the get age methods
	 *
	 * @return criteria
	 *
	 * @since 2.7.0
	 */
	private String improveCriteria(String criteria) {
		if (criteria.contains("$fn.getAge()")) {
			criteria = criteria.replace("$fn.getAge()", "$fn.getAge($patient)");
		} else if (criteria.contains("$fn.getAgeInMonths()")) {
			criteria = criteria.replace("$fn.getAgeInMonths()", "$fn.getAgeInMonths($patient)");
		} else if (criteria.contains("$fn.getAgeInWeeks()")) {
			criteria = criteria.replace("$fn.getAgeInWeeks()", "$fn.getAgeInWeeks($patient)");
		} else if (criteria.contains("$fn.getAgeInDays()")) {
			criteria = criteria.replace("$fn.getAgeInDays()", "$fn.getAgeInDays($patient)");
		}
		return criteria;
	}
	
	/**
	 * Method to get the age of a person in months.
	 *
	 * @param person patient in subject
	 * @return the age in months as an Integer
	 * 
	 * @since 2.7.0
	 */
	public Integer getAgeInMonths(Person person) {
		return getAge(ChronoUnit.MONTHS, person);
	}
	
	/**
	 * Method to get the age of a person in weeks.
	 *
	 * @param person patient in subject
	 * @return the age in weeks as an Integer
	 *
	 * @since 2.7.0
	 */
	public Integer getAgeInWeeks(Person person) {
		return getAge(ChronoUnit.WEEKS, person);
	}
	
	/**
	 * Method to get the age of a person in days.
	 *
	 * @param person patient in subject
	 * @return the age in days as an Integer
	 * 
	 * @since 2.7.0
	 */
	public Integer getAgeInDays(Person person) {
		return getAge(ChronoUnit.DAYS, person);
	}
	
	/**
	 * Method to get the age of a person in years.
	 *
	 * @param person patient in subject
	 * @return the age in years as an Integer
	 * 
	 * @since 2.7.0
	 */
	public Integer getAge(Person person) {
		return getAge(ChronoUnit.YEARS, person);
	}
	
	/**
	 * Gets the age of a person with a specified ChronoUnit.
	 *
	 * @param chronoUnit the unit of precision for the age calculation (e.g. WEEKS, MONTHS, YEARS)
	 * @param person the person/patient to get age from   
	 * @return the age in the specified unit as an Integer
	 * 
	 * @since 2.7.0
	 */
	public Integer getAge(ChronoUnit chronoUnit, Person person) {
		if (person == null || person.getBirthdate() == null) {
			return null;
		}
		
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
			case DAYS:
				age = ChronoUnit.DAYS.between(birthDate, endDate);
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
	
	/**
	 * Gets the latest Obs by concept.
	 *
	 * @param conceptRef can be either concept uuid or conceptMap's code and sourceName 
	 *                   e.g "bac25fd5-c143-4e43-bffe-4eb1e7efb6ce" or "CIEL:1434"
	 *                   
	 * @return Obs
	 * 
	 * @since 2.7.0
	 */
	public Obs getLatestObsByConcept(String conceptRef) {
		Concept concept = Context.getConceptService().getConceptByReference(conceptRef);

		if (concept != null) {
			List<Obs> observations = Context.getObsService().getObservations(
				null, 
				null, 
				Collections.singletonList(concept), 
				null, 
				null, 
				null, 
				null, 
				null, 
				null,
				null, 
				null, 
				false
			);

			// Return the latest Obs by sorting the list by dateCreated
			return observations.stream()
				.max(Comparator.comparing(Obs::getDateCreated))
				.orElse(null);
		}

		return null;
	}
	
	/**
	 * Gets the time of the day in hours.
	 *
	 * @return the hour of the day (e.g. 14 to mean 2pm)
	 */
	public int getTimeOfTheDay() {
		return LocalTime.now().getHourOfDay();
	}
}
