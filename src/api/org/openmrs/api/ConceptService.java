package org.openmrs.api;

import org.openmrs.Concept;

public interface ConceptService {

	public void createConcept(Concept concept);
	
	public Concept getConcept(long conceptId);
	
	public void saveOrUpdate(Concept concept);
	
	public void voidConcept(Concept concept, String reason);
}
