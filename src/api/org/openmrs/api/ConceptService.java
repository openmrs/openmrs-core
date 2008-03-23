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
package org.openmrs.api;

import java.util.List;
import java.util.Locale;

import org.openmrs.Concept;
import org.openmrs.ConceptAnswer;
import org.openmrs.ConceptClass;
import org.openmrs.ConceptDatatype;
import org.openmrs.ConceptNumeric;
import org.openmrs.ConceptProposal;
import org.openmrs.ConceptSet;
import org.openmrs.ConceptWord;
import org.openmrs.Drug;
import org.openmrs.annotation.Authorized;
import org.openmrs.api.db.ConceptDAO;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface ConceptService {

	public void setConceptDAO(ConceptDAO dao);

	/**
	 * @param concept
	 *            to be created
	 */
	@Authorized({"Add Concepts"})
	public void createConcept(Concept concept);

	/**
	 * @param numeric
	 *            concept to be created
	 */
	@Authorized({"Add Concepts"})
	public void createConcept(ConceptNumeric concept);

	/**
	 * Gets the concept with the given internal identifier
	 * 
	 * @param conceptId
	 * @return Concept
	 */
	@Transactional(readOnly=true)
	@Authorized({"View Concepts"})
	public Concept getConcept(Integer conceptId);

	/**
	 * Gets the conceptAnswer with the given internal identifier
	 * 
	 * @param conceptAnswerId
	 * @return ConceptAnswer
	 */
	@Transactional(readOnly=true)
	@Authorized({"View Concepts"})
	public ConceptAnswer getConceptAnswer(Integer conceptAnswerId);

	/**
	 * Return a list of concepts sorted on sortBy in dir direction (asc/desc)
	 * 
	 * @param sortBy
	 * @param dir
	 * @return List of concepts
	 */
	@Transactional(readOnly=true)
	@Authorized({"View Concepts"})
	public List<Concept> getConcepts(String sortBy, String dir);

	/**
	 * Update the given concept
	 * 
	 * @param concept
	 *            to be updated
	 */
	@Authorized({"Edit Concepts"})
	public void updateConcept(Concept concept);

	/**
	 * Update the given numeric concept
	 * 
	 * @param numeric
	 *            concept to be updated
	 */
	@Authorized({"Edit Concepts"})
	public void updateConcept(ConceptNumeric concept);

	/**
	 * Delete the given concept
	 * 
	 * For super users only. If dereferencing concepts, use
	 * <code>voidConcept(org.openmrs.Concept)</code>
	 * 
	 * @param Concept
	 *            to be deleted
	 */
	@Authorized({"Delete Concepts"})
	public void deleteConcept(Concept concept);

	/**
	 * Voiding a concept essentially removes it from circulation
	 * 
	 * @param Concept
	 *            concept
	 * @param String
	 *            reason
	 */
	@Authorized({"Edit Concepts"})
	public void voidConcept(Concept concept, String reason);

	/**
	 * @param drug
	 *            to be created
	 */
	@Authorized({"Add Concepts"})
	public void createDrug(Drug drug);

	/**
	 * Update the given drug
	 * 
	 * @param drug
	 *            to be updated
	 */
	@Authorized({"Edit Concepts"})
	public void updateDrug(Drug drug);

	/**
	 * Return a list of concepts matching "name" anywhere in the name
	 * 
	 * @param name
	 * @return List of concepts
	 */
	@Transactional(readOnly=true)
	@Authorized({"View Concepts"})
	public List<Concept> getConceptsByName(String name);

	/**
	 * Return a Concept that matches the name exactly
	 * 
	 * @param name
	 * @return Concept with matching name
	 */
	@Transactional(readOnly=true)
	@Authorized({"View Concepts"})
	public Concept getConceptByName(String name);

	/**
	 * Return the drug object corresponding to the given id
	 * 
	 * @return Drug
	 */
	@Transactional(readOnly=true)
	@Authorized({"View Concepts"})
	public Drug getDrug(Integer drugId);

	/**
	 * Return the drug object corresponding to the given name
	 * 
	 * @return Drug
	 */
	@Transactional(readOnly=true)
	@Authorized({"View Concepts"})
	public Drug getDrug(String drugName);

	@Transactional(readOnly=true)
	@Authorized({"View Concepts"})
	public Drug getDrugByNameOrId(String drugId);


	/**
	 * Return a list of drugs currently in the database
	 * 
	 * @return List of Drugs
	 */
	@Transactional(readOnly=true)
	@Authorized({"View Concepts"})
	public List<Drug> getDrugs();

	/**
	 * Find drugs in the system. The string search can match either drug.name or
	 * drug.concept.name
	 * 
	 * @param phrase
	 * @param includeRetired
	 * @return List of Drugs
	 */
	@Transactional(readOnly=true)
	@Authorized({"View Concepts"})
	public List<Drug> findDrugs(String phrase, boolean includeRetired);

	/**
	 * Return a list of drugs associated with the given concept
	 * 
	 * @param Concept
	 * @return List of Drugs
	 */
	@Transactional(readOnly=true)
	@Authorized({"View Concepts"})
	public List<Drug> getDrugs(Concept concept);

	/**
	 * Return a list of concept classes currently in the database
	 * 
	 * @return List of Concept class objects
	 */
	@Transactional(readOnly=true)
	@Authorized({"View Concepts"})
	public List<ConceptClass> getConceptClasses();

	/**
	 * Return a Concept class matching the given identifier
	 * 
	 * @param i Integer
	 * @return ConceptClass
	 */
	@Transactional(readOnly=true)
	@Authorized({"View Concepts"})
	public ConceptClass getConceptClass(Integer i);

	/**
	 * Return a Concept class matching the given name
	 * 
	 * @param name String
	 * @return ConceptClass
	 */
	@Transactional(readOnly=true)
	@Authorized({"View Concepts"})
	public ConceptClass getConceptClassByName(String name);

	/**
	 * Return a list of concept datatypes currently in the database
	 * 
	 * @return List of ConceptDatatypes
	 */
	@Transactional(readOnly=true)
	@Authorized({"View Concepts"})
	public List<ConceptDatatype> getConceptDatatypes();

	/**
	 * Return a ConceptDatatype matching the given identifier
	 * 
	 * @return ConceptDatatype
	 */
	@Transactional(readOnly=true)
	@Authorized({"View Concepts"})
	public ConceptDatatype getConceptDatatype(Integer i);
	
	/**
	 * Return a Concept datatype matching the given name
	 * 
	 * @param name String
	 * @return ConceptDatatype
	 */
	@Transactional(readOnly=true)
	@Authorized({"View Concepts"})
	public ConceptDatatype getConceptDatatypeByName(String name);
	
	/**
	 * Return a list of the concept sets with concept_set matching concept
	 * For example to find all concepts for ARVs, you would do
	 *    getConceptSets(getConcept("ANTIRETROVIRAL MEDICATIONS"))
	 * and then take the conceptIds from the resulting list.
	 * 
	 * @return List
	 */
	@Transactional(readOnly=true)
	@Authorized({"View Concepts"})
	public List<ConceptSet> getConceptSets(Concept c);

	@Transactional(readOnly=true)
	@Authorized({"View Concepts"})
	public List<Concept> getConceptsInSet(Concept c);

	/**
	 * Find all sets that the given concept is a member of 
	 * @param concept
	 */
	@Transactional(readOnly=true)
	@Authorized({"View Concepts"})
	public List<ConceptSet> getSetsContainingConcept(Concept concept);

	/**
	 * @return Returns all concepts in a given class 
	 */
	@Transactional(readOnly=true)
	@Authorized({"View Concepts"})
	public List<Concept> getConceptsByClass(ConceptClass cc);
	
	/**
	 * Return a concept numeric object given the concept id
	 * 
	 * @return ConceptNumeric
	 */
	@Transactional(readOnly=true)
	@Authorized({"View Concepts"})
	public ConceptNumeric getConceptNumeric(Integer conceptId);

	/**
	 * Searches on given phrase via the concept word table
	 * 
	 * @param phrase/search/words
	 *            String
	 * @param locale
	 *            Locale
	 * @param includeRetired
	 *            boolean
	 * @return
	 */
	@Transactional(readOnly=true)
	@Authorized({"View Concepts"})
	public List<ConceptWord> findConcepts(String phrase, Locale locale,
			boolean includeRetired);

	/**
	 * Searches on given phrase via the concept word table
	 * 
	 * @param phrase/search/words
	 *            String
	 * @param locale
	 *            Locale
	 * @param includeRetired
	 *            boolean
	 * @param requireClasses
	 *            List<ConceptClass>
	 * @param excludeClasses
	 *            List<ConceptClass>
	 * @param requireDatatypes
	 *            List<ConceptDatatype>
	 * @param excludeDatatypes
	 *            List<ConceptDatatype>
	 * @return
	 * 
	 * @see ConceptService.findConcepts(String,Locale,boolean)
	 */
	@Transactional(readOnly=true)
	@Authorized({"View Concepts"})
	public List<ConceptWord> findConcepts(String phrase, Locale locale, boolean includeRetired, 
			List<ConceptClass> requireClasses, List<ConceptClass> excludeClasses,
			List<ConceptDatatype> requireDatatypes,List<ConceptDatatype> excludeDatatypes);

	/**
	 * 
	 * Finds concepts but only returns the given range
	 * 
	 * @param phrase
	 * @param locale
	 * @param includeRetired
	 * @param start
	 * @param size
	 * @return ConceptWord list
	 */
	@Transactional(readOnly=true)
	@Authorized({"View Concepts"})
	public List<ConceptWord> findConcepts(String phrase, Locale locale,
			boolean includeRetired, int start, int size);

	@Transactional(readOnly=true)
	@Authorized({"View Concepts"})
	public List<ConceptWord> findConceptAnswers(String phrase, Locale locale,
			Concept concept, boolean includeRetired);

	/**
	 * Get the questions that have this concept as a possible answer
	 * 
	 * @param concept
	 *            Concept to get
	 * @return list of concepts
	 */
	@Transactional(readOnly=true)
	@Authorized({"View Concepts"})
	public List<Concept> getQuestionsForAnswer(Concept concept);

	/**
	 * Finds the previous available concept via concept id
	 * 
	 * @param c
	 * @param offset
	 * @return
	 */
	@Transactional(readOnly=true)
	@Authorized({"View Concepts"})
	public Concept getPrevConcept(Concept c);

	/**
	 * Finds the next available concept via concept id
	 * 
	 * @param c
	 * @param offset
	 * @return
	 */
	@Transactional(readOnly=true)
	@Authorized({"View Concepts"})
	public Concept getNextConcept(Concept c);

	@Transactional(readOnly=true)
	@Authorized({"View Concepts"})
	public List<ConceptProposal> getConceptProposals(boolean includeCompleted);

	@Transactional(readOnly=true)
	@Authorized({"View Concepts"})
	public ConceptProposal getConceptProposal(Integer conceptProposalId);

	@Transactional(readOnly=true)
	@Authorized({"View Concepts"})
	public List<ConceptProposal> findMatchingConceptProposals(String text);

	@Transactional(readOnly=true)
	@Authorized({"View Concepts"})
	public List<Concept> findProposedConcepts(String text);

	@Authorized({"View Concepts"})
	public void proposeConcept(ConceptProposal conceptProposal);

	@Transactional(readOnly=true)
	@Authorized({"View Concepts"})
	public Integer getNextAvailableId();

	@Transactional(readOnly=true)
	@Authorized({"View Concepts"})
	public Concept getConceptByIdOrName(String idOrName);

	@Transactional(readOnly=true)
	public void checkIfLocked() throws ConceptsLockedException;
	
	/**
	 * TODO: think about renaming this method
	 * @return All concepts that occur as a Drug.concept.
	 */
	@Transactional(readOnly=true)
	@Authorized({"View Concepts"})
	public List<Concept> getConceptsWithDrugsInFormulary();
}