/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs;

import java.util.Date;

/**
 * Holds the context needed to resolve a concept reference range for a given person and concept. The
 * optional date field supports retrospective entry scenarios where date-relative criteria (e.g. age
 * at encounter time) should be evaluated at a point in time other than today.
 * 
 * @since 3.0.0, 2.9.0, 2.8.5, 2.7.9
 */
public class ConceptReferenceRangeContext {
	
	private final Person person;
	
	private final Concept concept;
	
	private final Date date;
	
	private final Encounter encounter;
	
	private final Obs obs;
	
	/**
	 * @param person the person to evaluate criteria against (required)
	 * @param concept the concept whose reference ranges to resolve (required)
	 * @param date the date at which to evaluate criteria, or null for today
	 */
	public ConceptReferenceRangeContext(Person person, Concept concept, Date date) {
		if (person == null) {
			throw new IllegalArgumentException("person is required");
		}
		if (concept == null) {
			throw new IllegalArgumentException("concept is required");
		}
		this.person = person;
		this.concept = concept;
		this.date = date;
		this.encounter = null;
		this.obs = null;
	}
	
	/**
	 * Convenience constructor that extracts person, concept, and obsDatetime from an existing Obs.
	 * The Obs is retained so that criteria expressions referencing {@code $obs} (e.g.
	 * {@code $obs.obsDatetime}) continue to work.
	 * 
	 * @param obs the observation to extract context from
	 */
	public ConceptReferenceRangeContext(Obs obs) {
		if (obs == null) {
			throw new IllegalArgumentException("obs is required");
		}
		if (obs.getPerson() == null) {
			throw new IllegalArgumentException("person is required");
		}
		if (obs.getConcept() == null) {
			throw new IllegalArgumentException("concept is required");
		}
		this.person = obs.getPerson();
		this.concept = obs.getConcept();
		this.date = obs.getObsDatetime();
		this.encounter = obs.getEncounter();
		this.obs = obs;
	}
	
	/**
	 * Construct a context from an encounter and concept. The patient and encounter datetime are
	 * extracted from the encounter.
	 * 
	 * @param encounter the encounter to extract context from (required)
	 * @param concept the concept whose reference ranges to resolve (required)
	 * @since 3.0.0, 2.9.0, 2.8.5, 2.7.9
	 */
	public ConceptReferenceRangeContext(Encounter encounter, Concept concept) {
		if (encounter == null) {
			throw new IllegalArgumentException("encounter is required");
		}
		if (encounter.getPatient() == null) {
			throw new IllegalArgumentException("person is required");
		}
		if (concept == null) {
			throw new IllegalArgumentException("concept is required");
		}
		this.person = encounter.getPatient();
		this.concept = concept;
		this.date = encounter.getEncounterDatetime();
		this.encounter = encounter;
		this.obs = null;
	}
	
	public Person getPerson() {
		return person;
	}
	
	public Concept getConcept() {
		return concept;
	}
	
	/**
	 * @return the date at which to evaluate criteria, or null meaning "today"
	 */
	public Date getDate() {
		return date;
	}
	
	/**
	 * @return the encounter if this context was constructed from one or from an Obs with an
	 *         encounter, or null
	 * @since 3.0.0, 2.9.0, 2.8.5, 2.7.9
	 */
	public Encounter getEncounter() {
		return encounter;
	}
	
	/**
	 * @return the original Obs if this context was constructed from one, or null
	 */
	public Obs getObs() {
		return obs;
	}
}
