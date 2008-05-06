/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.api.db;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.openmrs.Concept;
import org.openmrs.ConceptAnswer;
import org.openmrs.ConceptClass;
import org.openmrs.ConceptDatatype;
import org.openmrs.ConceptDerived;
import org.openmrs.ConceptName;
import org.openmrs.ConceptNumeric;
import org.openmrs.ConceptProposal;
import org.openmrs.ConceptSet;
import org.openmrs.ConceptSetDerived;
import org.openmrs.ConceptSource;
import org.openmrs.ConceptSynonym;
import org.openmrs.ConceptWord;
import org.openmrs.Drug;
import org.openmrs.DrugIngredient;
import org.openmrs.api.APIException;

/**
 * Concept-related database functions
 * @version 1.0
 */
public interface ConceptDAO {

	/**
	 * @param Concept to be created
	 */
	public void createConcept(Concept concept);

	public void createConcept(Concept concept, boolean isForced);

	/**
	 * @param Numeric concept to be created
	 */
	public void createConcept(ConceptNumeric concept);

	public void createConcept(ConceptNumeric concept, boolean isForced);

	/**
	 * Gets the concept with the given internal identifier
	 * @param conceptId
	 * @return Concept
	 */
	public Concept getConcept(Integer conceptId);
	
	/**
	 * Gets the conceptAnswer with the given internal identifier
	 * @param conceptAnswerId
	 * @return ConceptAnswer
	 */
	public ConceptAnswer getConceptAnswer(Integer conceptAnswerId);

	/**
	 * Return a list of concepts sorted on sortBy in direction
	 * @param sortBy
	 * @param direction
	 * @return List of concepts
	 */
	public List<Concept> getConcepts(String sortBy, String direction);
	
	/**
	 * Update the given concept
	 * @param concept to be updated
	 */
	public void updateConcept(Concept concept);

	public void updateConcept(Concept concept, boolean isForced);

	/**
	 * Update the given numeric concept
	 * @param numeric concept to be updated
	 */
	public void updateConcept(ConceptNumeric concept);

	public void updateConcept(ConceptNumeric concept, boolean isForced);

	/**
	 * Delete the given concept
	 * 
	 * For super users only.  If dereferencing concepts, use <code>voidConcept(org.openmrs.Concept)</code>
	 * 
	 * @param Concept to be deleted
	 */
	public void deleteConcept(Concept concept);

	/**
	 * Voiding a concept essentially removes it from circulation
	 * @param Concept concept
	 * @param String reason
	 */
	public void voidConcept(Concept concept, String reason);

	public void voidConcept(Concept concept, String reason, boolean isForced);

	/**
	 * @param Drug to be created
	 */
	public void createDrug(Drug drug);

	/**
	 * Update the given drug
	 * @param drug to be updated
	 */
	public void updateDrug(Drug drug);

	/**
	 * Return a list of concepts matching "name" anywhere in the name
	 * @param name
	 * @return List of concepts
	 */
	public List<Concept> getConceptsByName(String name);
	
	/**
	 * Return a Concept that matches the name exactly
	 * 
	 * @param name
	 * @return Concept with matching name
	 */
	public Concept getConceptByName(String name);
	
	/**
	 * Return drug object corresponding to the given id
	 * @return Drug
	 */
	public Drug getDrug(Integer drugId);
	
	/**
	 * Return drug object corresponding to the given name
	 * @return Drug
	 */
	public Drug getDrug(String drugName);
	
	/**
	 * Return a list of drugs currently in the database
	 * @return List of Drugs
	 */
	public List<Drug> getDrugs();
	
	/**
	 * Find drugs in the system.
	 * The string search can match either drug.name or drug.concept.name
	 * @param phrase
	 * @param includeRetired
	 * @return List of Drugs
	 */
	public List<Drug> findDrugs(String phrase, boolean includeRetired);
	
	/**
	 * Return a list of drugs associated with this concept
	 * @param Concept
	 * @return List of Drugs
	 */
	public List<Drug> getDrugs(Concept c);
	
	/**
	 * Return a list of concept classes currently in the database
	 * @return List of Concept class objects
	 */
	public List<ConceptClass> getConceptClasses();
	
	/**
	 * Return a Concept class matching the given identifier
	 * @param i Integer
	 * @return ConceptClass
	 */
	public ConceptClass getConceptClass(Integer i);
	
	/**
	 * Return a Concept class matching the given identifier
	 * @param name String
	 * @return ConceptClass
	 */
	public ConceptClass getConceptClassByName(String name);
	
	/**
	 * Return a list of concept datatypes currently in the database
	 * @return List of ConceptDatatypes
	 */
	public List<ConceptDatatype> getConceptDatatypes();
	
	/**
	 * Return concept datatype with given name
	 * @param name
	 * @return
	 */
	public ConceptDatatype getConceptDatatypeByName(String name);
	
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
	 * @see org.openmrs.api.ConceptService#getSetsContainingConcept(org.openmrs.Concept)
	 */
	public List<ConceptSet> getSetsContainingConcept(Concept concept);
	
	/**
	 * Return a concept numeric object given the concept id
	 * @return ConceptNumeric
	 */
	public ConceptNumeric getConceptNumeric(Integer conceptId);

	/**
	 * Searches on given phrase via the concept word table
	 * @param phrase/search/words String
	 * @param locale Locale
	 * @param includeRetired boolean
	 * @param requireClasses List<ConceptClass>
	 * @param excludeClasses List<ConceptClass>
	 * @param requireDatatypes List<ConceptDatatype>
	 * @param excludeDatatypes List<ConceptDatatype>
	 * @return
	 */
	public List<ConceptWord> findConcepts(String phrase, Locale locale, boolean includeRetired, 
			List<ConceptClass> requireClasses, List<ConceptClass> excludeClasses,
			List<ConceptDatatype> requireDatatypes, List<ConceptDatatype> excludeDatatypes);
	
	/**
	 * Searches on given phrase via the concept word table
	 * Restricts on answers to the given concept
	 * 
	 * @param phrase/search/words String
	 * @param locale Locale
	 * @param Concept
	 * @param includeRetired boolean
	 * @return list of concept words
	 */
	public List<ConceptWord> findConceptAnswers(String phrase, Locale locale, Concept concept, boolean includeRetired);
	
	/**
	 * Get the questions that have this concept as a possible answer
	 * 
	 * @param concept Concept to get 
	 * @return list of concepts
	 */
	public List<Concept> getQuestionsForAnswer(Concept concept);
	
	/**
	 * Finds the previous available concept via concept id  
	 * @param c
	 * @param offset
	 * @return
	 */
	public Concept getPrevConcept(Concept c); 

	/**
	 * Finds the next available concept via concept id  
	 * @param c
	 * @param offset
	 * @return
	 */
	public Concept getNextConcept(Concept c); 

	/**
	 * Returns a list of ConceptProposals currently in the database
	 * @param includeComplete
	 * @return
	 */
	public List<ConceptProposal> getConceptProposals(boolean includeComplete);
	
	public ConceptProposal getConceptProposal(Integer i);
	
	public List<ConceptProposal> findMatchingConceptProposals(String text) throws APIException;
	
	/**
	 * Looks in the proposed concepts table for a completed proposed concept that 
	 * has the same text 
	 * @param text
	 * @return
	 */
	public List<Concept> findProposedConcepts(String text);
	
	public void proposeConcept(ConceptProposal cp);
	
	public Integer getNextAvailableId();

	public List<Concept> getConceptsByClass(ConceptClass cc);
	
	public List<Concept> getConceptsWithDrugsInFormulary();

	/**
     * Auto generated method comment
     * 
     * @param guid
     * @return
     */
    public Concept getConceptByGuid(String guid);

	/**
     * Auto generated method comment
     * 
     * @param guid
     * @return
     */
    public ConceptClass getConceptClassByGuid(String guid);

    public ConceptAnswer getConceptAnswerByGuid(String guid);

    public ConceptDerived getConceptDerivedByGuid(String guid);

    public ConceptName getConceptNameByGuid(String guid);

    public ConceptSet getConceptSetByGuid(String guid);

    public ConceptSetDerived getConceptSetDerivedByGuid(String guid);

    public ConceptSource getConceptSourceByGuid(String guid);

    public ConceptSynonym getConceptSynonymByGuid(String guid);

    public ConceptWord getConceptWordByGuid(String guid);

	/**
     * Auto generated method comment
     * 
     * @param guid
     * @return
     */
    public ConceptDatatype getConceptDatatypeByGuid(String guid);

	/**
     * Auto generated method comment
     * 
     * @param guid
     * @return
     */
    public ConceptNumeric getConceptNumericByGuid(String guid);

	/**
     * Auto generated method comment
     * 
     * @param guid
     * @return
     */
    public ConceptProposal getConceptProposalByGuid(String guid);

	/**
     * Auto generated method comment
     * 
     * @param guid
     * @return
     */
    public Drug getDrugByGuid(String guid);

    public DrugIngredient getDrugIngredientByGuid(String guid);

	/**
	 * @param ConceptAnswer to be created
	 */
	public void createConceptAnswer(ConceptAnswer conceptAnswer);

	/**
	 * Update the given conceptAnswer
	 * @param conceptAnswer to be updated
	 */
	public void updateConceptAnswer(ConceptAnswer conceptAnswer);

	/**
	 * @param ConceptName to be created
	 */
	public void createConceptName(ConceptName conceptName);

	/**
	 * Update the given conceptName
	 * @param conceptName to be updated
	 */
	public void updateConceptName(ConceptName conceptName);

	/**
	 * @param ConceptSet to be created
	 */
	public void createConceptSet(ConceptSet conceptSet);

	/**
	 * Update the given conceptSet
	 * @param conceptSet to be updated
	 */
	public void updateConceptSet(ConceptSet conceptSet);

	/**
	 * @param ConceptSource to be created
	 */
	public void createConceptSource(ConceptSource conceptSource);

	/**
	 * Update the given conceptSource
	 * @param conceptSource to be updated
	 */
	public void updateConceptSource(ConceptSource conceptSource);

	/**
	 * @param ConceptSynonym to be created
	 */
	public void createConceptSynonym(ConceptSynonym conceptSynonym);

	/**
	 * Update the given conceptSynonym
	 * @param conceptSynonym to be updated
	 */
	public void updateConceptSynonym(ConceptSynonym conceptSynonym);

	/**
	 * @param ConceptWord to be created
	 */
	public void createConceptWord(ConceptWord conceptWord);

	/**
	 * Update the given conceptWord
	 * @param conceptWord to be updated
	 */
	public void updateConceptWord(ConceptWord conceptWord);

    /**
     * @see org.openmrs.api.ConceptService#getConceptGuids() 
     */
    public Map<Integer, String> getConceptGuids(); 
}
