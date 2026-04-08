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

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalTime;
import org.openmrs.Concept;
import org.openmrs.ConceptReferenceRangeContext;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.PatientProgram;
import org.openmrs.PatientState;
import org.openmrs.Person;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.hibernate.HibernateUtil;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.SpelEvaluationException;
import org.springframework.expression.spel.SpelMessage;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.DataBindingMethodResolver;
import org.springframework.expression.spel.support.DataBindingPropertyAccessor;
import org.springframework.expression.spel.support.MapAccessor;
import org.springframework.expression.spel.support.SimpleEvaluationContext;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

/**
 * A utility class that evaluates the concept ranges
 *
 * @since 2.7.0
 */
public class ConceptReferenceRangeUtility {

	/**
	 * A local-only cache for expressions, which should alleviate parsing overhead in hot loops, i.e.,
	 * if the same expressions are evaluated multiple times within a relatively short succession.
	 * Expires each element 5 minutes after it's last access.
	 */
	private static final Cache<String, Expression> EXPRESSION_CACHE = Caffeine.newBuilder().maximumSize(20000)
	        .expireAfterAccess(5, TimeUnit.MINUTES).build();

	/**
	 * {@link ExpressionParser} instance used by the {@link ConceptReferenceRangeUtility} to parse
	 * expressions
	 */
	private static final ExpressionParser PARSER = new SpelExpressionParser();

	/**
	 * Static {@link org.springframework.expression.EvaluationContext} which is used to run evaluations.
	 * This class is thread-safe, so shareable.
	 */
	private static final SimpleEvaluationContext EVAL_CONTEXT = SimpleEvaluationContext
	        .forPropertyAccessors(new MapAccessor(), DataBindingPropertyAccessor.forReadOnlyAccess())
	        .withMethodResolvers(DataBindingMethodResolver.forInstanceMethodInvocation()).build();

	private final CriteriaFunctions functions = new CriteriaFunctions();

	public ConceptReferenceRangeUtility() {
	}

	/**
	 * This method evaluates the given criteria against the provided {@link Obs}.
	 *
	 * @param criteria the criteria string to evaluate e.g. "$patient.getAge() > 1"
	 * @param obs The observation (Obs) object containing the values to be used in the criteria
	 *            evaluation.
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

		return evaluateCriteria(criteria, new ConceptReferenceRangeContext(obs));
	}

	/**
	 * Evaluates criteria against a {@link ConceptReferenceRangeContext}. When the context was
	 * constructed from an Obs, {@code $obs} is available in the expression; otherwise only
	 * {@code $patient}, {@code $fn}, {@code $context}, {@code $date}, and {@code $encounter} are
	 * available.
	 *
	 * @param criteria the criteria string to evaluate
	 * @param context the evaluation context
	 * @return true if the criteria evaluates to true, false otherwise
	 * @since 3.0.0, 2.9.0, 2.8.5, 2.7.9
	 */
	public boolean evaluateCriteria(String criteria, ConceptReferenceRangeContext context) {
		if (context == null) {
			throw new IllegalArgumentException("Failed to evaluate criteria with reason: context is null");
		}

		if (context.getPerson() == null) {
			throw new IllegalArgumentException("Failed to evaluate criteria with reason: patient is null");
		}

		if (StringUtils.isBlank(criteria)) {
			throw new IllegalArgumentException("Failed to evaluate criteria with reason: criteria is empty");
		}

		Map<String, Object> root = new HashMap<>();
		root.put("$fn", functions);
		root.put("$patient", HibernateUtil.getRealObjectFromProxy(context.getPerson()));
		root.put("$context", context);
		root.put("$obs", context.getObs());
		root.put("$encounter", context.getEncounter());
		root.put("$date", context.getDate());

		try {
			Expression expression = EXPRESSION_CACHE.get(criteria, PARSER::parseExpression);
			Boolean result = expression.getValue(EVAL_CONTEXT, root, Boolean.class);
			return result != null && result;
		} catch (SpelEvaluationException e) {
			SpelMessage msg = e.getMessageCode();
			if (msg == SpelMessage.METHOD_CALL_ON_NULL_OBJECT_NOT_ALLOWED
			        || msg == SpelMessage.PROPERTY_OR_FIELD_NOT_READABLE_ON_NULL) {
				return false;
			}
			throw new APIException("An error occurred while evaluating criteria: " + criteria, e);
		} catch (Exception e) {
			throw new APIException("An error occurred while evaluating criteria: " + criteria, e);
		}
	}

	/**
	 * Helper functions available as {@code $fn} in concept reference range criteria expressions.
	 * <p>
	 * This class is intentionally separate from the outer class so that {@code evaluateCriteria} is not
	 * callable from within expressions.
	 *
	 * @since 2.7.9, 2.8.6, 2.9.0, 3.0.0
	 */
	static class CriteriaFunctions {

		private final long NULL_DATE_RETURN_VALUE = -1;

		public Obs getLatestObs(String conceptRef, Person person) {
			if (person == null) {
				return null;
			}
			Concept concept = Context.getConceptService().getConceptByReference(conceptRef);

			if (concept != null) {
				List<Obs> observations = Context.getObsService().getObservations(Collections.singletonList(person), null,
				    Collections.singletonList(concept), null, null, null, Collections.singletonList("dateCreated"), 1, null,
				    null, null, false);

				return observations.isEmpty() ? null : observations.get(0);
			}

			return null;
		}

		public int getCurrentHour() {
			return LocalTime.now().getHourOfDay();
		}

		public Obs getCurrentObs(String conceptRef, Obs currentObs) {
			Concept concept = Context.getConceptService().getConceptByReference(conceptRef);

			if (currentObs.getValueAsString(Locale.ENGLISH).isEmpty()
			        && (concept != null && concept == currentObs.getConcept())) {
				return currentObs;
			} else {
				return getLatestObs(conceptRef, currentObs.getPerson());
			}
		}

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

		public boolean isObsValueCodedAnswer(String conceptRef, Person person, String answerConceptRef) {
			Obs obs = getLatestObs(conceptRef, person);
			if (obs == null) {
				return false;
			}

			Concept valudeCoded = obs.getValueCoded();
			if (valudeCoded == null) {
				return false;
			}

			Concept answerConcept = Context.getConceptService().getConceptByReference(answerConceptRef);
			if (answerConcept == null) {
				return false;
			}

			return valudeCoded.equals(answerConcept);
		}

		public long getObsDays(String conceptRef, Person person) {
			Date date = getLatestObsDate(conceptRef, person);
			if (date == null) {
				return NULL_DATE_RETURN_VALUE;
			}
			return getDays(date);
		}

		public long getObsWeeks(String conceptRef, Person person) {
			Date date = getLatestObsDate(conceptRef, person);
			if (date == null) {
				return NULL_DATE_RETURN_VALUE;
			}
			return getWeeks(date);
		}

		public long getObsMonths(String conceptRef, Person person) {
			Date date = getLatestObsDate(conceptRef, person);
			if (date == null) {
				return NULL_DATE_RETURN_VALUE;
			}
			return getMonths(date);
		}

		public long getObsYears(String conceptRef, Person person) {
			Date date = getLatestObsDate(conceptRef, person);
			if (date == null) {
				return NULL_DATE_RETURN_VALUE;
			}
			return getYears(date);
		}

		public long getDaysBetween(Date fromDate, Date toDate) {
			if (fromDate == null || toDate == null) {
				return NULL_DATE_RETURN_VALUE;
			}
			return ChronoUnit.DAYS.between(toLocalDate(fromDate), toLocalDate(toDate));
		}

		public long getWeeksBetween(Date fromDate, Date toDate) {
			if (fromDate == null || toDate == null) {
				return NULL_DATE_RETURN_VALUE;
			}
			return ChronoUnit.WEEKS.between(toLocalDate(fromDate), toLocalDate(toDate));
		}

		public long getMonthsBetween(Date fromDate, Date toDate) {
			if (fromDate == null || toDate == null) {
				return NULL_DATE_RETURN_VALUE;
			}
			return ChronoUnit.MONTHS.between(toLocalDate(fromDate), toLocalDate(toDate));
		}

		public long getYearsBetween(Date fromDate, Date toDate) {
			if (fromDate == null || toDate == null) {
				return NULL_DATE_RETURN_VALUE;
			}
			return ChronoUnit.YEARS.between(toLocalDate(fromDate), toLocalDate(toDate));
		}

		public long getDays(Date fromDate) {
			return getDaysBetween(fromDate, new Date());
		}

		public long getWeeks(Date fromDate) {
			return getWeeksBetween(fromDate, new Date());
		}

		public long getMonths(Date fromDate) {
			return getMonthsBetween(fromDate, new Date());
		}

		public long getYears(Date fromDate) {
			return getYearsBetween(fromDate, new Date());
		}

		public boolean isEnrolledInProgram(String uuid, Person person, Date onDate) {
			if (person == null) {
				return false;
			}
			if (!(person.getIsPatient())) {
				return false;
			}
			return getPatientPrograms((Patient) person, onDate).stream()
			        .anyMatch(pp -> pp.getProgram().getUuid().equals(uuid));
		}

		public boolean isInProgramState(String uuid, Person person, Date onDate) {
			if (person == null) {
				return false;
			}
			if (!(person.getIsPatient())) {
				return false;
			}

			List<PatientProgram> patientPrograms = getPatientPrograms((Patient) person, onDate);
			List<PatientState> patientStates = new ArrayList<>();

			for (PatientProgram pp : patientPrograms) {
				for (PatientState state : pp.getStates()) {
					if (state.getActive(onDate)) {
						patientStates.add(state);
					}
				}
			}

			return patientStates.stream().anyMatch(ps -> ps.getState().getUuid().equals(uuid));
		}

		private LocalDate toLocalDate(Date date) {
			return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		}

		private List<PatientProgram> getPatientPrograms(Patient patient, Date onDate) {
			if (onDate == null) {
				onDate = new Date();
			}
			return Context.getProgramWorkflowService().getPatientPrograms(patient, null, null, onDate, onDate, null, false);
		}
	}
}
