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
import org.openmrs.api.db.ConceptDAO;
import org.openmrs.api.db.DAOContext;
import org.openmrs.util.OpenmrsConstants;

/**
 * Concept-related services
 * 
 * @author Ben Wolfe
 * @author Burke Mamlin
 * @version 1.0
 */
public class ConceptService {
	
	private final Log log = LogFactory.getLog(getClass());

	private Context context;
	private DAOContext daoContext;
	
	public ConceptService(Context c, DAOContext d) {
		this.context = c;
		this.daoContext = d;
	}
	
	private ConceptDAO getConceptDAO() {
		// TODO No privilege check for concepts in the openmrs model
		//if (!context.hasPrivilege(OpenmrsConstants.PRIV_VIEW_CONCEPTS))
		//	throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_VIEW_CONCEPTS);
		
		return daoContext.getConceptDAO();
	}

	/**
	 * 
	 * @param concept to be created
	 */
	public void createConcept(Concept concept) {
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_EDIT_CONCEPTS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_EDIT_CONCEPTS);
		
		getConceptDAO().createConcept(concept);
	}
	
	/**
	 * Gets the concept with the given internal identifier
	 * @param conceptId
	 * @return Concept
	 */
	public Concept getConcept(Integer conceptId) {
		return getConceptDAO().getConcept(conceptId);
	}
	
	/**
	 * Return a list of concepts sorted on sortBy in dir direction (asc/desc)
	 * @param sortBy
	 * @param dir
	 * @return List of concepts
	 */
	public List<Concept> getConcepts(String sortBy, String dir) {
		return getConceptDAO().getConcepts(sortBy, dir);
	}
	
	/**
	 * Update the given concept
	 * @param concept to be updated
	 */
	public void updateConcept(Concept concept) {
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_EDIT_CONCEPTS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_EDIT_CONCEPTS);
		
		getConceptDAO().updateConcept(concept);
	}
	
	/**
	 * Voiding a concept essentially removes it from circulation
	 * @param Concept concept
	 * @param String reason
	 */
	public void voidConcept(Concept concept, String reason) {
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_EDIT_CONCEPTS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_EDIT_CONCEPTS);
		
		getConceptDAO().voidConcept(concept, reason);
	}
	
	/**
	 * Return a list of concepts matching "name" anywhere in the name
	 * @param name
	 * @return List of concepts
	 */
	public List<Concept> getConceptByName(String name) {
		return getConceptDAO().getConceptByName(name);
	}
	
	/**
	 * Return a list of drugs currently in the database
	 * @return List of Drugs
	 */
	public List<Drug> getDrugs() {
		return getConceptDAO().getDrugs();
	}
	
	/**
	 * Return a list of concept classes currently in the database
	 * @return List of Concept class objects
	 */
	public List<ConceptClass> getConceptClasses() {
		return getConceptDAO().getConceptClasses();
	}
	
	/**
	 * Return a Concept class matching the given identifier
	 * @return ConceptClass
	 */
	public ConceptClass getConceptClass(Integer i) {
		return getConceptDAO().getConceptClass(i);
	}
	
	/**
	 * Return a list of concept datatypes currently in the database
	 * @return List of ConceptDatatypes
	 */
	public List<ConceptDatatype> getConceptDatatypes() {
		return getConceptDAO().getConceptDatatypes();
	}
	
	/**
	 * Return a ConceptDatatype matching the given identifier
	 * @return ConceptDatatype
	 */
	public ConceptDatatype getConceptDatatype(Integer i) {
		return getConceptDAO().getConceptDatatype(i);
	}
	
	/**
	 * Return a list of the concept sets with concept_set matching concept 
	 * @return List
	 */
	public List<ConceptSet> getConceptSets(Concept c) {
		return getConceptDAO().getConceptSets(c);
	}
	
	/**
	 * Return a concept numeric object given the concept id
	 * @return ConceptNumeric
	 */
	public ConceptNumeric getConceptNumeric(Integer conceptId) {
		return getConceptDAO().getConceptNumeric(conceptId);
	}
	
	/**
	 * Searches on given phrase via the concept word table
	 * @param phrase/search/words String
	 * @param locale Locale
	 * @param includeRetired boolean
	 * @return
	 */
	public List<ConceptWord> findConcepts(String phrase, Locale locale, boolean includeRetired) {
		List<ConceptWord> conceptWords = getConceptDAO().findConcepts(phrase, locale, includeRetired);
		
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
				uniqueConcepts.get(id).increaseWeight(1.0);
			}
			else {
				//normalize the weighting
				tmpWord.setWeight(0.0);
				//if its not in the list, add it
				uniqueConcepts.put(id, tmpWord);
			}
			
			
			if (tmpWord.getSynonym().length() == 0) {
				 //if there isn't a synonym, it is matching on the name, increase the weight
				uniqueConcepts.get(id).increaseWeight(2.0);
			}
			else {
				//increase the weight by a factor of the percentage of words matched
				uniqueConcepts.get(id).increaseWeight(5.0 * (1 / tmpWord.getSynonym().split(" ").length));
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
		return getConceptDAO().getPrevConcept(c);
	}

	/**
	 * Finds the next available concept via concept id  
	 * @param c
	 * @param offset
	 * @return
	 */
	public Concept getNextConcept(Concept c) {
		return getConceptDAO().getNextConcept(c);
	}

}
