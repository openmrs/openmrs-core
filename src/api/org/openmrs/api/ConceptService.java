package org.openmrs.api;

import java.util.List;

import org.openmrs.Concept;

public interface ConceptService {

	public void createConcept(Concept concept);
	
	public Concept getConcept(Integer conceptId);
	
	public void updateConcept(Concept concept);
	
	public void voidConcept(Concept concept, String reason);
	
	public List<Concept> getConceptByName(String name);
}
