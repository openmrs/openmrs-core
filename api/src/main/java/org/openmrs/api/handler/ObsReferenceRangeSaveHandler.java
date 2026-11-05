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
import org.openmrs.ConceptReferenceRange;
import org.openmrs.ConceptReferenceRangeContext;
import org.openmrs.Obs;
import org.openmrs.ObsReferenceRange;
import org.openmrs.User;
import org.openmrs.annotation.Handler;
import org.openmrs.api.context.Context;

/**
 * Derives the {@link ObsReferenceRange} and the {@link Obs.Interpretation} of a numeric {@link Obs}
 * as it is saved. {@link org.openmrs.validator.ObsValidator} used to do this, which meant neither
 * was recorded on implementations that switch validation off through the
 * <code>validation.disable</code> global property.
 * <p>
 * A reference range is derived only for an observation that does not already carry one, so a range
 * supplied by the caller is never overwritten. The interpretation is deliberately asymmetric: a
 * caller-supplied one is kept on a new observation, but is re-derived on a persisted observation
 * the caller has edited, because an amended value has to be re-interpreted and a persisted
 * observation gives no way to tell an interpretation the caller has just set from one derived on an
 * earlier save. A persisted observation that is not dirty is left alone, since
 * <code>interpretation</code> is immutable on an obs the current save will not rewrite.
 *
 * @see RequiredDataHandler
 * @see SaveHandler
 * @since 3.0.0, 2.9.0, 2.8.10
 */
@Handler(supports = Obs.class, order = 50)
public class ObsReferenceRangeSaveHandler implements SaveHandler<Obs> {

	/**
	 * @see org.openmrs.api.handler.SaveHandler#handle(org.openmrs.OpenmrsObject, org.openmrs.User,
	 *      java.util.Date, java.lang.String)
	 */
	@Override
	public void handle(Obs obs, User creator, Date dateCreated, String other) {
		if (obs == null || obs.getVoided()) {
			return;
		}

		// an obs group holds no value of its own, so it gets no reference range
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

		// A persisted obs keeps the range it was created with. Its interpretation is re-derived only
		// when the caller has actually edited it: interpretation is not one of
		// ImmutableObsInterceptor's mutable properties, so writing it on an unedited obs leaves a
		// dirty field that either the flush rejects (RequiredDataAdvice.recursivelyHandle reaches
		// every obs under a save, and VisitService.saveVisit never calls saveObs) or that voids the
		// obs and replaces it with a pointless revision. Gate on the same isDirty() that
		// ObsServiceImpl.saveObs uses to decide the obs is being amended; a genuinely edited obs is
		// already dirty by the time the handler sees it.
		if (obs.getId() != null) {
			if (obs.isDirty()) {
				assignObsInterpretation(obs);
			}
			return;
		}

		// a reference range supplied by the caller is never overwritten
		if (obs.getReferenceRange() == null) {
			ConceptReferenceRange conceptReferenceRange = getConceptReferenceRange(obs);

			if (conceptReferenceRange != null) {
				assignObsReferenceRange(obs, conceptReferenceRange);
			}
		}

		// unlike the amend path above, an interpretation supplied by the caller is kept
		if (obs.getInterpretation() == null) {
			assignObsInterpretation(obs);
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
	 * Builds the {@link ObsReferenceRange} for the given observation from the given
	 * {@link ConceptReferenceRange} and attaches it.
	 *
	 * @param obs observation to attach the reference range to
	 * @param conceptReferenceRange the range to copy the bounds from
	 */
	private void assignObsReferenceRange(Obs obs, ConceptReferenceRange conceptReferenceRange) {
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
	 * Derives the observation's interpretation from its numeric value and the bounds of the reference
	 * range it carries.
	 *
	 * @param obs observation to set the interpretation on
	 */
	private void assignObsInterpretation(Obs obs) {
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
