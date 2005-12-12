package org.openmrs.api;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.ConceptClass;
import org.openmrs.ConceptDatatype;
import org.openmrs.ConceptNumeric;
import org.openmrs.ConceptSet;
import org.openmrs.ConceptWord;
import org.openmrs.Drug;
import org.openmrs.api.context.Context;

/**
 * Concept-related services
 * 
 * @author Ben Wolfe
 * @author Burke Mamlin
 * @version 1.0
 */
public class ConceptService {
	
	private final Log log = LogFactory.getLog(getClass());

	Context context;
	
	public ConceptService(Context c) {
		this.context = c;
	}

	/**
	 * 
	 * @param concept to be created
	 */
	public void createConcept(Concept concept) {
		context.getDAOContext().getConceptDAO().createConcept(concept);
	}
	
	/**
	 * Gets the concept with the given internal identifier
	 * @param conceptId
	 * @return Concept
	 */
	public Concept getConcept(Integer conceptId) {
		return context.getDAOContext().getConceptDAO().getConcept(conceptId);
	}
	
	/**
	 * Update the given concept
	 * @param concept to be updated
	 */
	public void updateConcept(Concept concept) {
		context.getDAOContext().getConceptDAO().updateConcept(concept);
	}
	
	/**
	 * Voiding a concept essentially removes it from circulation
	 * @param Concept concept
	 * @param String reason
	 */
	public void voidConcept(Concept concept, String reason) {
		context.getDAOContext().getConceptDAO().voidConcept(concept, reason);
	}
	
	/**
	 * Return a list of concepts matching "name" anywhere in the name
	 * @param name
	 * @return List of concepts
	 */
	public List<Concept> getConceptByName(String name) {
		return context.getDAOContext().getConceptDAO().getConceptByName(name);
	}
	
	/**
	 * Return a list of drugs currently in the database
	 * @return List of Drugs
	 */
	public List<Drug> getDrugs() {
		return context.getDAOContext().getConceptDAO().getDrugs();
	}
	
	/**
	 * Return a list of concept classes currently in the database
	 * @return List of Concept class objects
	 */
	public List<ConceptClass> getConceptClasses() {
		return context.getDAOContext().getConceptDAO().getConceptClasses();
	}
	
	/**
	 * Return a Concept class matching the given identifier
	 * @return ConceptClass
	 */
	public ConceptClass getConceptClass(Integer i) {
		return context.getDAOContext().getConceptDAO().getConceptClass(i);
	}
	
	/**
	 * Return a list of concept datatypes currently in the database
	 * @return List of ConceptDatatypes
	 */
	public List<ConceptDatatype> getConceptDatatypes() {
		return context.getDAOContext().getConceptDAO().getConceptDatatypes();
	}
	
	/**
	 * Return a ConceptDatatype matching the given identifier
	 * @return ConceptDatatype
	 */
	public ConceptDatatype getConceptDatatype(Integer i) {
		return context.getDAOContext().getConceptDAO().getConceptDatatype(i);
	}
	
	/**
	 * Return a list of the concept sets with concept_set matching concept 
	 * @return List
	 */
	public List<ConceptSet> getConceptSets(Concept c) {
		return context.getDAOContext().getConceptDAO().getConceptSets(c);
	}
	
	/**
	 * Return a concept numeric object given the concept id
	 * @return ConceptNumeric
	 */
	public ConceptNumeric getConceptNumeric(Integer conceptId) {
		return context.getDAOContext().getConceptDAO().getConceptNumeric(conceptId);
	}
	
	/**
	 * Searches on given phrase via the concept word table
	 * @param phrase/search/words String
	 * @param locale Locale
	 * @param includeRetired boolean
	 * @return
	 */
	public List<ConceptWord> findConcepts(String phrase, Locale locale, boolean includeRetired) {
		List<ConceptWord> conceptWords = context.getDAOContext().getConceptDAO().findConcepts(phrase, locale, includeRetired);
		
		//this will store the unique concept hits to the concept word table
		//we are assuming the hits are sorted with synonym matches at the bottom
		//Map<ConceptId, ConceptWord>
		Map<Integer, ConceptWord> uniqueConcepts = new HashMap<Integer, ConceptWord>();
		
		Integer id = null;
		Concept concept = null;
		for (ConceptWord tmpWord : conceptWords) {
			concept = tmpWord.getConcept(); 
			id = concept.getConceptId();
			if (uniqueConcepts.containsKey(id)) {
				//if the concept is already in the list, strengthen the hit
				uniqueConcepts.get(id).increaseWeight(1);
			}
			else {
				//if its not in the list, add it
				uniqueConcepts.put(id, tmpWord);
			}
			
			// if there isn't a synonym, it is matching on the name, increase the weight
			if (tmpWord.getSynonym().length() == 0) {
				uniqueConcepts.get(id).increaseWeight(2);
			}
			else {
				uniqueConcepts.get(id).increaseWeight(5 * (1 / tmpWord.getSynonym().split(" ").length));
			}
		}
		
		conceptWords = new Vector<ConceptWord>(); 
		conceptWords.addAll(uniqueConcepts.values());
		Collections.sort(conceptWords);
		
		return conceptWords;
	}
	
	public List<ConceptWord> findConcepts(String phrase, Locale locale, boolean includeRetired, int start, int size) {
		
		List<ConceptWord> conceptWords = findConcepts(phrase, locale, includeRetired);
		
		List<ConceptWord> subList = conceptWords.subList(start, start + size);
		
		return subList;
	}
	
	/**
	 * Finds the previous available concept via concept id  
	 * @param c
	 * @param offset
	 * @return
	 */
	public Concept getPrevConcept(Concept c) {
		return context.getDAOContext().getConceptDAO().getPrevConcept(c);
	}

	/**
	 * Finds the next available concept via concept id  
	 * @param c
	 * @param offset
	 * @return
	 */
	public Concept getNextConcept(Concept c) {
		return context.getDAOContext().getConceptDAO().getNextConcept(c);
	}

}
