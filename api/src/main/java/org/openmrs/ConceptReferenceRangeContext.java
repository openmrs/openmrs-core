package org.openmrs;

import java.util.Date;
import java.util.Objects;

public class ConceptReferenceRangeContext {
	
	private final Person person;
	private final Concept concept;
	private final Date date;
	private final Encounter encounter;
	private final Obs obs;
	
	public ConceptReferenceRangeContext(Person person, Concept concept, Date date) {
		
		this.person = Objects.requireNonNull(person, "person is required");
		this.concept = Objects.requireNonNull(concept, "concept is required");
		this.date = date;
		this.encounter = null;
		this.obs = null;
	}
	
	public ConceptReferenceRangeContext(Obs obs) {
		
		Obs safeObs = Objects.requireNonNull(obs, "obs is required");
		
		this.person = Objects.requireNonNull(safeObs.getPerson(), "person is required");
		this.concept = Objects.requireNonNull(safeObs.getConcept(), "concept is required");
		this.date = safeObs.getObsDatetime();
		this.encounter = safeObs.getEncounter();
		this.obs = safeObs;
	}
	
	public ConceptReferenceRangeContext(Encounter encounter, Concept concept) {
		
		Encounter safeEncounter = Objects.requireNonNull(encounter, "encounter is required");
		
		this.person = Objects.requireNonNull(safeEncounter.getPatient(), "person is required");
		this.concept = Objects.requireNonNull(concept, "concept is required");
		this.date = safeEncounter.getEncounterDatetime();
		this.encounter = safeEncounter;
		this.obs = null;
	}
	
	public Person getPerson() {
		return person;
	}
	
	public Concept getConcept() {
		return concept;
	}
	
	public Date getDate() {
		return date;
	}
	
	public Encounter getEncounter() {
		return encounter;
	}
	
	public Obs getObs() {
		return obs;
	}
}

