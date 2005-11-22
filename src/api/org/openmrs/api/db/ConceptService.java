package org.openmrs.api.db;

import java.util.List;

import org.openmrs.Concept;
import org.openmrs.ConceptClass;
import org.openmrs.ConceptDatatype;
import org.openmrs.ConceptNumeric;
import org.openmrs.ConceptSet;
import org.openmrs.Drug;

public interface ConceptService {

	/**
	 * 
	 * @param concept to be created
	 */
	public void createConcept(Concept concept);
	
	/**
	 * Gets the concept with the given internal identifier
	 * @param conceptId
	 * @return Concept
	 */
	public Concept getConcept(Integer conceptId);
	
	/**
	 * Update the given concept
	 * @param concept to be updated
	 */
	public void updateConcept(Concept concept);
	
	/**
	 * Voiding a concept essentially removes it from circulation
	 * @param Concept concept
	 * @param String reason
	 */
	public void voidConcept(Concept concept, String reason);
	
	/**
	 * Return a list of concepts matching "name" anywhere in the name
	 * @param name
	 * @return List of concepts
	 */
	public List<Concept> getConceptByName(String name);
	
	/**
	 * Return a list of drugs currently in the database
	 * @return List of Drugs
	 */
	public List<Drug> getDrugs();
	
	/**
	 * Return a list of concept classes currently in the database
	 * @return List of Concept class objects
	 */
	public List<ConceptClass> getConceptClasses();
	
	/**
	 * Return a Concept class matching the given identifier
	 * @return ConceptClass
	 */
	public ConceptClass getConceptClass(Integer i);
	
	/**
	 * Return a list of concept datatypes currently in the database
	 * @return List of ConceptDatatypes
	 */
	public List<ConceptDatatype> getConceptDatatypes();
	
	/**
	 * Return a ConceptDatatype matching the given identifier
	 * @return ConceptDatatype
	 */
	public ConceptDatatype getConceptDatatype(Integer i);
	
	/**
	 * Return a list of the concept sets with concept_set matching concept 
	 * @return List
	 */
	public List<ConceptSet> getConceptSets(Concept c);
	
	/**
	 * Return a concept numeric object given the concept id
	 * @return ConceptNumeric
	 */
	public ConceptNumeric getConceptNumeric(Integer conceptId);
	
}
