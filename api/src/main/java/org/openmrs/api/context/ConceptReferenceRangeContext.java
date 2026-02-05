/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.context;

import java.util.Date;

import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Patient;

/**
 * Context used to evaluate concept reference ranges independently of a fully populated Obs. This
 * allows reference range effectiveness at times other than Obs persistence (e.g. retrospective
 * entry), while preserving compatibility with existing criteria expressions.
 */
public class ConceptReferenceRangeContext {
	
	private final Patient patient;
	
	private final Concept concept;
	
	private final Date effectiveDate;
	
	private final Encounter encounter;
	
	private final Obs obs;
	
	/**
	 * Construct a context from an existing Obs.
	 */
	public ConceptReferenceRangeContext(Obs obs) {
		this.obs = obs;
		this.patient = obs.getPerson() instanceof Patient ? (Patient) obs.getPerson() : null;
		this.concept = obs.getConcept();
		this.effectiveDate = obs.getObsDatetime();
		this.encounter = obs.getEncounter();
	}
	
	/**
	 * Construct a context from patient, concept, and date.
	 */
	public ConceptReferenceRangeContext(Patient patient, Concept concept, Date effectiveDate) {
		this.patient = patient;
		this.concept = concept;
		this.effectiveDate = effectiveDate;
		this.encounter = null;
		
		Obs obs = new Obs();
		obs.setPerson(patient);
		obs.setConcept(concept);
		obs.setObsDatetime(effectiveDate);
		
		this.obs = obs;
	}
	
	/**
	 * Construct from encounter and concept.
	 */
	public ConceptReferenceRangeContext(Encounter encounter, Concept concept) {
		this.patient = encounter.getPatient();
		this.concept = concept;
		this.effectiveDate = encounter.getEncounterDatetime();
		this.encounter = encounter;
		
		Obs obs = new Obs();
		obs.setPerson(patient);
		obs.setConcept(concept);
		obs.setObsDatetime(effectiveDate);
		obs.setEncounter(encounter);
		
		this.obs = obs;
	}
	
	public Patient getPatient() {
		return patient;
	}
	
	public Concept getConcept() {
		return concept;
	}
	
	public Encounter getEncounter() {
		return encounter;
	}
	
	/** Required by ConceptServiceImpl */
	public Date getDate() {
		return effectiveDate;
	}
	
	/**
	 * Optional Obs, if this context was constructed from one.
	 */
	public Obs getObs() {
		return obs;
	}
}
