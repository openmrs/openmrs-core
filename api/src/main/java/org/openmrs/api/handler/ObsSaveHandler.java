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

import org.openmrs.Concept;
import org.openmrs.ConceptDatatype;
import org.openmrs.ConceptNumeric;
import org.openmrs.ConceptReferenceRange;
import org.openmrs.ConceptReferenceRangeContext;
import org.openmrs.Obs;
import org.openmrs.ObsReferenceRange;
import org.openmrs.User;
import org.openmrs.annotation.Handler;
import org.openmrs.aop.RequiredDataAdvice;
import org.openmrs.api.context.Context;

/**
 * This class deals with {@link Obs} objects when they are saved via a save* method in an Openmrs
 * Service. This handler is automatically called by the {@link RequiredDataAdvice} AOP class. <br>
 * <br>
 * It derives the {@link ObsReferenceRange} and the {@link Obs.Interpretation} of a numeric
 * observation. This used to be done by the Obs validator, which meant that no reference range was
 * recorded at all on implementations that switch validation off through the
 * <code>validation.disable</code> global property. Deriving them here keeps the two concerns apart:
 * the validator only reports errors, while this handler populates the observation. <br>
 * <br>
 * A reference range is only derived for a new, non-voided, numeric observation that has a value and
 * that does not already carry one, so a reference range supplied by the caller is never
 * overwritten. Group members are reached by {@link RequiredDataAdvice#recursivelyHandle} walking
 * the {@link Obs#getGroupMembers()} collection, so this handler never recurses itself.
 *
 * @see RequiredDataHandler
 * @see SaveHandler
 * @see Obs
 * @since 3.0.0
 */
@Handler(supports = Obs.class, order = 50)
public class ObsSaveHandler implements SaveHandler<Obs> {

	/**
	 * @see org.openmrs.api.handler.SaveHandler#handle(org.openmrs.OpenmrsObject, org.openmrs.User,
	 *      java.util.Date, java.lang.String)
	 */
	@Override
	public void handle(Obs obs, User creator, Date dateCreated, String other) {
		if (obs == null || obs.getVoided()) {
			return;
		}

		// An observation that has already been persisted, and the amended copy that replaces it, both
		// keep the reference range the observation was created with. Editing an observation must not
		// hit the database from here either: the copy is handled while the observation it amends is
		// still dirty in the session, and a query would auto-flush it past ImmutableObsInterceptor.
		if (obs.getId() != null || obs.hasPreviousVersion()) {
			return;
		}

		// an obs group (i.e. a parent) holds no value of its own, so it gets no reference range
		if (obs.hasGroupMembers(true)) {
			return;
		}

		Concept concept = obs.getConcept();
		if (concept == null) {
			return;
		}

		ConceptDatatype datatype = concept.getDatatype();
		if (datatype == null || !datatype.isNumeric() || obs.getValueNumeric() == null) {
			return;
		}

		// a reference range supplied by the caller is never overwritten
		if (obs.getReferenceRange() == null) {
			ConceptReferenceRange conceptReferenceRange = getConceptReferenceRange(obs);

			if (conceptReferenceRange != null) {
				setObsReferenceRange(obs, conceptReferenceRange);
			} else {
				setObsReferenceRange(obs);
			}
		}

		if (obs.getInterpretation() == null) {
			setObsInterpretation(obs);
		}
	}

	/**
	 * Evaluates the criteria and returns the most strict {@link ConceptReferenceRange} for the concept
	 * and patient contained in the given observation. It considers all valid ranges that match the
	 * criteria for the person.
	 *
	 * @param obs containing the concept and patient for whom the range is being evaluated
	 * @return the strictest {@link ConceptReferenceRange}, or null if no valid range is found
	 */
	private ConceptReferenceRange getConceptReferenceRange(Obs obs) {
		if (obs.getPerson() == null || obs.getConcept() == null) {
			return null;
		}
		return Context.getConceptService().getConceptReferenceRange(new ConceptReferenceRangeContext(obs));
	}

	/**
	 * Builds and sets the ObsReferenceRange for the given Obs.
	 *
	 * @param obs Observation to set the reference range
	 * @param conceptReferenceRange ConceptReferenceRange used to build the ObsReferenceRange
	 */
	private void setObsReferenceRange(Obs obs, ConceptReferenceRange conceptReferenceRange) {
		ObsReferenceRange obsRefRange = new ObsReferenceRange();

		obsRefRange.setHiAbsolute(conceptReferenceRange.getHiAbsolute());
		obsRefRange.setHiCritical(conceptReferenceRange.getHiCritical());
		obsRefRange.setHiNormal(conceptReferenceRange.getHiNormal());
		obsRefRange.setLowAbsolute(conceptReferenceRange.getLowAbsolute());
		obsRefRange.setLowCritical(conceptReferenceRange.getLowCritical());
		obsRefRange.setLowNormal(conceptReferenceRange.getLowNormal());
		obsRefRange.setObs(obs);

		obs.setReferenceRange(obsRefRange);
	}

	/**
	 * Builds and sets the ObsReferenceRange from concept numeric values.
	 *
	 * @param obs Observation to set the reference range
	 */
	private void setObsReferenceRange(Obs obs) {
		if (obs.getConcept() == null) {
			return;
		}

		ConceptNumeric conceptNumeric = Context.getConceptService().getConceptNumeric(obs.getConcept().getId());

		if (conceptNumeric != null) {
			ObsReferenceRange obsRefRange = new ObsReferenceRange();

			obsRefRange.setHiAbsolute(conceptNumeric.getHiAbsolute());
			obsRefRange.setHiCritical(conceptNumeric.getHiCritical());
			obsRefRange.setHiNormal(conceptNumeric.getHiNormal());
			obsRefRange.setLowAbsolute(conceptNumeric.getLowAbsolute());
			obsRefRange.setLowCritical(conceptNumeric.getLowCritical());
			obsRefRange.setLowNormal(conceptNumeric.getLowNormal());
			obsRefRange.setObs(obs);

			obs.setReferenceRange(obsRefRange);
		}
	}

	/**
	 * This method sets Obs interpretation based on the current obs' numeric value.
	 *
	 * @param obs Observation to set the interpretation
	 */
	private void setObsInterpretation(Obs obs) {
		ObsReferenceRange referenceRange = obs.getReferenceRange();
		if (referenceRange == null || obs.getValueNumeric() == null) {
			return;
		}

		Double obsValue = obs.getValueNumeric();
		Double hiCritical = referenceRange.getHiCritical();
		Double lowCritical = referenceRange.getLowCritical();
		Double lowNormal = referenceRange.getLowNormal();
		Double hiNormal = referenceRange.getHiNormal();

		if (hiCritical != null && obsValue >= hiCritical) {
			obs.setInterpretation(Obs.Interpretation.CRITICALLY_HIGH);
		} else if (hiNormal != null && obsValue > hiNormal) {
			obs.setInterpretation(Obs.Interpretation.HIGH);
		} else if (lowCritical != null && obsValue <= lowCritical) {
			obs.setInterpretation(Obs.Interpretation.CRITICALLY_LOW);
		} else if (lowNormal != null && obsValue < lowNormal) {
			obs.setInterpretation(Obs.Interpretation.LOW);
		} else {
			obs.setInterpretation(Obs.Interpretation.NORMAL);
		}
	}
}
