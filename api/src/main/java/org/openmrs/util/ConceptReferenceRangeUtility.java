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

import java.io.StringWriter;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.runtime.log.Log4JLogChute;
import org.joda.time.LocalTime;
import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.Person;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;

/**
 * A utility class that evaluates the concept ranges 
 * 
 * @since 2.7.0
 */
public class ConceptReferenceRangeUtility {
	
	private final long NULL_DATE_RETURN_VALUE = -1;
	
	public ConceptReferenceRangeUtility() {
	}
	
	/**
	 * This method evaluates the given criteria against the provided {@link Obs}.
	 *
	 * @param criteria the criteria string to evaluate e.g. "$patient.getAge() > 1"
	 * @param obs The observation (Obs) object containing the values to be used in the criteria evaluation.
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
		try {
			Properties props = new Properties();
			props.put("runtime.log.logsystem.class", Log4JLogChute.class.getName());
			props.put("runtime.log.logsystem.log4j.category", "velocity");
			props.put("runtime.log.logsystem.log4j.logger", "velocity");
			velocityEngine.init(props);
		}
		catch (Exception e) {
			throw new APIException("Failed to create the velocity engine: " + e.getMessage(), e);
		}
		
		StringWriter writer = new StringWriter();
		String wrappedCriteria = "#set( $criteria = " + criteria + " )$criteria";
		
		try {
			velocityEngine.evaluate(velocityContext, writer, ConceptReferenceRangeUtility.class.getName(), wrappedCriteria);
			return Boolean.parseBoolean(writer.toString());
		}
		catch (ParseErrorException e) {
			throw new APIException("An error occurred while evaluating criteria. Invalid criteria: " + criteria, e);
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
	 * @return Obs latest Obs
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
		
		if (currentObs.getValueAsString(Locale.ENGLISH).isEmpty() && (concept != null && concept == currentObs.getConcept())) {
			return currentObs;
		} else {
			return getLatestObs(conceptRef, currentObs.getPerson());
		}
	}
	
	/**
	 * Gets the person's latest observation date for a given concept
	 * 
	 * @param conceptRef can be either concept uuid or conceptMap's code and sourceName 
	 *                   e.g "bac25fd5-c143-4e43-bffe-4eb1e7efb6ce" or "CIEL:1434"
	 * @param person the person
	 * 
	 * @return the observation date
	 * 
	 * @since 2.7.8
	 */
	public Date getLatestObsDate(String conceptRef, Person person) {
		Obs obs = getLatestObs(conceptRef, person);
		if (obs == null) {
			return null;
		}
		
		Date date = obs.getValueDate();
		if (date == null) {
			date = obs.getValueDatetime();
		}
		
		return date;
	}
	
	/**
	 * Gets the number of days from the person's latest observation date value for a given concept to the current date
	 * 
	 * @param conceptRef can be either concept uuid or conceptMap's code and sourceName 
	 *                   e.g "bac25fd5-c143-4e43-bffe-4eb1e7efb6ce" or "CIEL:1434"
	 * @param person the person
	 * 
	 * @return the number of days
	 * 
	 * @since 2.7.8
	 */
	public long getObsDays(String conceptRef, Person person) {
		Date date = getLatestObsDate(conceptRef, person);
		if (date == null) {
			return NULL_DATE_RETURN_VALUE;
		}
		return this.getDays(date);
	}
	
	/**
	 * Gets the number of weeks from the person's latest observation date value for a given concept to the current date
	 * 
	 * @param conceptRef can be either concept uuid or conceptMap's code and sourceName 
	 *                   e.g "bac25fd5-c143-4e43-bffe-4eb1e7efb6ce" or "CIEL:1434"
	 * @param person the person
	 * 
	 * @return the number of weeks
	 * 
	 * @since 2.7.8
	 */
	public long getObsWeeks(String conceptRef, Person person) {
		Date date = getLatestObsDate(conceptRef, person);
		if (date == null) {
			return NULL_DATE_RETURN_VALUE;
		}
		return this.getWeeks(date);
	}
	
	/**
	 * Gets the number of months from the person's latest observation date value for a given concept to the current date
	 * 
	 * @param conceptRef can be either concept uuid or conceptMap's code and sourceName 
	 *                   e.g "bac25fd5-c143-4e43-bffe-4eb1e7efb6ce" or "CIEL:1434"
	 * @param person the person
	 * 
	 * @return the number of months
	 * 
	 * @since 2.7.8
	 */
	public long getObsMonths(String conceptRef, Person person) {
		Date date = getLatestObsDate(conceptRef, person);
		if (date == null) {
			return NULL_DATE_RETURN_VALUE;
		}
		return this.getMonths(date);
	}
	
	/**
	 * Gets the number of years from the person's latest observation date value for a given concept to the current date
	 * 
	 * @param conceptRef can be either concept uuid or conceptMap's code and sourceName 
	 *                   e.g "bac25fd5-c143-4e43-bffe-4eb1e7efb6ce" or "CIEL:1434"
	 * @param person the person
	 * 
	 * @return the number of years
	 * 
	 * @since 2.7.8
	 */
	public long getObsYears(String conceptRef, Person person) {
		Date date = getLatestObsDate(conceptRef, person);
		if (date == null) {
			return NULL_DATE_RETURN_VALUE;
		}
		return this.getYears(date);
	}
	
	/**
	 * Gets the number of days between two given dates
	 * 
	 * @param fromDate the date from which to start counting
	 * @param dateDate the date up to which to stop counting
	 * 
	 * @return the number of days between
	 * 
	 * @since 2.7.8
	 */
	public long getDaysBetween(Date fromDate, Date toDate) {
		if (fromDate == null || toDate == null) {
			return NULL_DATE_RETURN_VALUE;
		}
		return ChronoUnit.DAYS.between(toLocalDate(fromDate), toLocalDate(toDate));
	}
	
	/**
	 * Gets the number of weeks between two given dates
	 * 
	 * @param fromDate the date from which to start counting
	 * @param dateDate the date up to which to stop counting
	 * 
	 * @return the number of weeks between
	 * 
	 * @since 2.7.8
	 */
	public long getWeeksBetween(Date fromDate, Date toDate) {
		if (fromDate == null || toDate == null) {
			return NULL_DATE_RETURN_VALUE;
		}
		return ChronoUnit.WEEKS.between(toLocalDate(fromDate), toLocalDate(toDate));
	}
	
	/**
	 * Gets the number of months between two given dates
	 * 
	 * @param fromDate the date from which to start counting
	 * @param dateDate the date up to which to stop counting
	 * 
	 * @return the number of months between
	 * 
	 * @since 2.7.8
	 */
	public long getMonthsBetween(Date fromDate, Date toDate) {
		if (fromDate == null || toDate == null) {
			return NULL_DATE_RETURN_VALUE;
		}
		return ChronoUnit.MONTHS.between(toLocalDate(fromDate), toLocalDate(toDate));
	}
	
	/**
	 * Gets the number of years between two given dates
	 * 
	 * @param fromDate the date from which to start counting
	 * @param dateDate the date up to which to stop counting
	 * 
	 * @return the number of years between
	 * 
	 * @since 2.7.8
	 */
	public long getYearsBetween(Date fromDate, Date toDate) {
		if (fromDate == null || toDate == null) {
			return NULL_DATE_RETURN_VALUE;
		}
		return ChronoUnit.YEARS.between(toLocalDate(fromDate), toLocalDate(toDate));
	}
	
	/**
	 * Gets the number of days from a given date up to the current date.
	 * 
	 * @param fromDate the date from which to start counting
	 * @return the number of days
	 * 
	 * @since 2.7.8
	 */
	public long getDays(Date fromDate) {
		return getDaysBetween(fromDate, new Date());
	}
	
	/**
	 * Gets the number of weeks from a given date up to the current date.
	 * 
	 * @param fromDate the date from which to start counting
	 * @return the number of weeks
	 * 
	 * @since 2.7.8
	 */
	public long getWeeks(Date fromDate) {
		return getWeeksBetween(fromDate, new Date());
	}
	
	/**
	 * Gets the number of months from a given date up to the current date.
	 * 
	 * @param fromDate the date from which to start counting
	 * @return the number of months
	 * 
	 * @since 2.7.8
	 */
	public long getMonths(Date fromDate) {
		return getMonthsBetween(fromDate, new Date());
	}
	
	/**
	 * Gets the number of years from a given date up to the current date.
	 * 
	 * @param fromDate the date from which to start counting
	 * @return the number of years
	 * 
	 * @since 2.7.8
	 */
	public long getYears(Date fromDate) {
		return getYearsBetween(fromDate, new Date());
	}
	
	/**
	 * Converts a java.util.Date to java.time.LocalDate
	 * 
	 * @param date the java.util.Date
	 * @return the java.time.LocalDate
	 */
	private LocalDate toLocalDate(Date date) {
		return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
	}
}
