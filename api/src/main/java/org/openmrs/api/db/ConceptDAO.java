/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.db;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.openmrs.Concept;
import org.openmrs.ConceptAnswer;
import org.openmrs.ConceptAttribute;
import org.openmrs.ConceptAttributeType;
import org.openmrs.ConceptClass;
import org.openmrs.ConceptComplex;
import org.openmrs.ConceptDatatype;
import org.openmrs.ConceptDescription;
import org.openmrs.ConceptMap;
import org.openmrs.ConceptMapType;
import org.openmrs.ConceptName;
import org.openmrs.ConceptNameTag;
import org.openmrs.ConceptNumeric;
import org.openmrs.ConceptProposal;
import org.openmrs.ConceptReferenceTerm;
import org.openmrs.ConceptReferenceTermMap;
import org.openmrs.ConceptSearchResult;
import org.openmrs.ConceptSet;
import org.openmrs.ConceptSource;
import org.openmrs.ConceptStopWord;
import org.openmrs.Drug;
import org.openmrs.DrugIngredient;
import org.openmrs.api.APIException;
import org.openmrs.api.ConceptService;

/**
 * Concept-related database functions   
 * 
 * @see org.openmrs.api.ConceptService
 */
public interface ConceptDAO {
	
	/**
	 * @see org.openmrs.api.ConceptService#saveConcept(org.openmrs.Concept)
	 */
	public Concept saveConcept(Concept concept) throws DAOException;
	
	/**
	 * @see org.openmrs.api.ConceptService#purgeConcept(org.openmrs.Concept)
	 * <strong>Should</strong> purge concept
	 */
	public void purgeConcept(Concept concept) throws DAOException;
	
	/**
	 * Get the concept complex
	 * 
	 * @param conceptId The id of the concept complex to return
	 * @return the concept complex using the specified conceptId
	 */
	public ConceptComplex getConceptComplex(Integer conceptId);
	
	/**
	 * @see org.openmrs.api.ConceptService#purgeDrug(org.openmrs.Drug)
	 */
	public void purgeDrug(Drug drug) throws DAOException;
	
	/**
	 * @see org.openmrs.api.ConceptService#saveDrug(org.openmrs.Drug)
	 */
	public Drug saveDrug(Drug drug) throws DAOException;
	
	/**
	 * @see org.openmrs.api.ConceptService#getConcept(java.lang.Integer)
	 */
	public Concept getConcept(Integer conceptId) throws DAOException;
	
	/**
	 * Get the concept name
	 * 
	 * @see org.openmrs.api.ConceptService#getConceptName(java.lang.Integer)
	 * @param conceptNameId The concept name id
	 * @return The concept name matching the specified conceptNameId
	 * @throws DAOException
	 */
	public ConceptName getConceptName(Integer conceptNameId) throws DAOException;
	
	/**
	 * Returns all concepts
	 * 
	 * @see org.openmrs.api.ConceptService#getAllConcepts(java.lang.String, boolean, boolean)
	 * @throws DAOException
	 */
	public List<Concept> getAllConcepts(String sortBy, boolean asc, boolean includeRetired) throws DAOException;
	
	/**
	 * Returns a list of concepts based on the search criteria
	 * 
	 * @param name The name of the concept to search
	 * @param loc The locale to look in
	 * @param searchOnPhrase This puts wildcard characters around the concept name search criteria
	 * @return a list of concepts based on search criteria
	 * @throws DAOException
	 * <strong>Should</strong> not return concepts with matching names that are voided
	 */
	public List<Concept> getConcepts(String name, Locale loc, boolean searchOnPhrase, List<ConceptClass> classes,
	        List<ConceptDatatype> datatypes) throws DAOException;
	
	/**
	 * Return a list of concept results based on search criteria
	 * 
	 * @see ConceptService#getConcepts(String, List, boolean, List, List, List, List, Concept,
	 *      Integer, Integer)
	 * @param phrase 
	 * @param locales A list of locales
	 * @param includeRetired 
	 * @param requireClasses 
	 * @param excludeClasses 
	 * @param requireDatatypes 
	 * @param excludeDatatypes 
	 * @param answersToConcept 
	 * @param start 
	 * @param size 
	 * @return a list of concept results
	 * @throws DAOException
	 * <strong>Should</strong> return correct results for concept with names that contain words with more weight
	 * <strong>Should</strong> return correct results if a concept name contains same word more than once
	 */
	public List<ConceptSearchResult> getConcepts(String phrase, List<Locale> locales, boolean includeRetired,
	        List<ConceptClass> requireClasses, List<ConceptClass> excludeClasses, List<ConceptDatatype> requireDatatypes,
	        List<ConceptDatatype> excludeDatatypes, Concept answersToConcept, Integer start, Integer size)
	        throws DAOException;
	/**
	 * Returns count of concepts
	 * 
	 * @param phrase
	 * @param locales 
	 * @param includeRetired 
	 * @param requireClasses 
	 * @param excludeClasses 
	 * @param requireDatatypes 
	 * @param excludeDatatypes 
	 * @param answersToConcept 
	 * @return the concept count
	 * @throws DAOException
	*/
	public Integer getCountOfConcepts(String phrase, List<Locale> locales, boolean includeRetired,
	        List<ConceptClass> requireClasses, List<ConceptClass> excludeClasses, List<ConceptDatatype> requireDatatypes,
	        List<ConceptDatatype> excludeDatatypes, Concept answersToConcept) throws DAOException;
	
	/**
	 * @see org.openmrs.api.ConceptService#getConceptAnswer(java.lang.Integer)
	 */
	public ConceptAnswer getConceptAnswer(Integer conceptAnswerId) throws DAOException;
	
	/**
	 * @see org.openmrs.api.ConceptService#getDrug(java.lang.Integer)
	 */
	public Drug getDrug(Integer drugId) throws DAOException;
	
	/**
	 * Retrieve a list of drugs based on the supplied criteria
	 * 
	 * @param drugName The name of the drug to required
	 * @param concept The concept required
	 * @param includeRetired The include retired flag or option
	 * @return a list of drugs
	 * @throws DAOException
	 */
	public List<Drug> getDrugs(String drugName, Concept concept, boolean includeRetired) throws DAOException;
	
	/**
	 * @see org.openmrs.api.ConceptService#getDrugs(java.lang.String)
	 */
	public List<Drug> getDrugs(String phrase) throws DAOException;
	
	/**
	 * @see org.openmrs.api.ConceptService#getConceptClass(java.lang.Integer)
	 */
	public ConceptClass getConceptClass(Integer i) throws DAOException;
	
	/**
	 * @see org.openmrs.api.ConceptService#getConceptClassByName(java.lang.String)
	 */
	public List<ConceptClass> getConceptClasses(String name) throws DAOException;
	
	/**
	 * @see org.openmrs.api.ConceptService#getAllConceptClasses(boolean)
	 */
	public List<ConceptClass> getAllConceptClasses(boolean includeRetired) throws DAOException;
	
	/**
	 * @see org.openmrs.api.ConceptService#saveConceptClass(org.openmrs.ConceptClass)
	 */
	public ConceptClass saveConceptClass(ConceptClass cc) throws DAOException;
	
	/**
	 * @see org.openmrs.api.ConceptService#purgeConceptClass(org.openmrs.ConceptClass)
	 */
	public void purgeConceptClass(ConceptClass cc) throws DAOException;
	
	/**
	 * @see org.openmrs.api.ConceptService#purgeConceptNameTag(org.openmrs.ConceptNameTag)
	 */
	public void deleteConceptNameTag(ConceptNameTag cnt) throws DAOException;
	
	/**
	 * @see org.openmrs.api.ConceptService#getAllConceptDatatypes(boolean)
	 */
	public List<ConceptDatatype> getAllConceptDatatypes(boolean includeRetired) throws DAOException;
	
	/**
	 * Returns a concept data type which matches the provided name
	 * 
	 * @param name The name of the concept data type
	 * @return the {@link ConceptDatatype} that matches <em>name</em> exactly or null if one does
	 *         not exist.
	 * @throws DAOException
	 */
	public ConceptDatatype getConceptDatatypeByName(String name) throws DAOException;
	
	/**
	 * @see org.openmrs.api.ConceptService#getConceptDatatype(java.lang.Integer)
	 */
	public ConceptDatatype getConceptDatatype(Integer i) throws DAOException;
	
<<<<<<< HEAD
	/**
<<<<<<< HEAD
	 * @see org.openmrs.api.ConceptService#saveConceptDatatype(org.openmrs.ConceptDatatype)
	 */
	public ConceptDatatype saveConceptDatatype(ConceptDatatype cd) throws DAOException;
	
	/**
	 * @see org.openmrs.api.ConceptService#purgeConceptDatatype(org.openmrs.ConceptDatatype)
	 */
	public void purgeConceptDatatype(ConceptDatatype cd) throws DAOException;
	
=======
>>>>>>> bae085a53... TRUNK-1824: Removing  ConceptDAO.saveConceptDatatype() , ConceptDAO.purgeConceptDatatype() , HibernateConceptDAO.saveConceptDatatype() and  HibernateConceptDAO.purgeConceptDatatype()
	/**
=======
>>>>>>> f57b7f645... TRUNK-1824 : Refactoring and adding more javadocs to ConceptDAO
	 * @see org.openmrs.api.ConceptService#getConceptSetsByConcept(org.openmrs.Concept)
	 */
	public List<ConceptSet> getConceptSetsByConcept(Concept c) throws DAOException;
	
	/**
	 * @see org.openmrs.api.ConceptService#getSetsContainingConcept(org.openmrs.Concept)
	 */
	public List<ConceptSet> getSetsContainingConcept(Concept concept) throws DAOException;
	
	/**
	 * @see org.openmrs.api.ConceptService#getConceptNumeric(java.lang.Integer)
	 */
	public ConceptNumeric getConceptNumeric(Integer conceptId) throws DAOException;
	
	/**
	 * Returns all possible Concepts to which this concept is a value-coded answer. 
	 * 
	 * @see org.openmrs.api.ConceptService#getConceptsByAnswer(org.openmrs.Concept)
	 * <strong>Should</strong> return concepts for the given answer concept
	 * @param concept The concept
	 * @return a list of concepts to which this concept is a value-coded answer
	 * @throws DAOException
	 */
	public List<Concept> getConceptsByAnswer(Concept concept) throws DAOException;
	
	/**
	 * @see org.openmrs.api.ConceptService#getPrevConcept(org.openmrs.Concept)
	 */
	public Concept getPrevConcept(Concept c) throws DAOException;
	
	/**
	 * @see org.openmrs.api.ConceptService#getNextConcept(org.openmrs.Concept)
	 */
	public Concept getNextConcept(Concept c) throws DAOException;
	
	/**
	 * @see org.openmrs.api.ConceptService#getAllConceptProposals(boolean)
	 */
	public List<ConceptProposal> getAllConceptProposals(boolean includeComplete) throws DAOException;
	
	/**
	 * @see org.openmrs.api.ConceptService#getConceptProposal(java.lang.Integer)
	 */
	public ConceptProposal getConceptProposal(Integer i) throws DAOException;
	
	/**
	 * @see org.openmrs.api.ConceptService#getConceptProposals(java.lang.String)
	 */
	public List<ConceptProposal> getConceptProposals(String text) throws DAOException;
	
	/**
	 * @see org.openmrs.api.ConceptService#getProposedConcepts(java.lang.String)
	 */
	public List<Concept> getProposedConcepts(String text) throws DAOException;
	
	/**
	 * @see org.openmrs.api.ConceptService#saveConceptProposal(org.openmrs.ConceptProposal)
	 */
	public ConceptProposal saveConceptProposal(ConceptProposal cp) throws DAOException;
	
	/**
	 * @see org.openmrs.api.ConceptService#purgeConceptProposal(org.openmrs.ConceptProposal)
	 */
	public void purgeConceptProposal(ConceptProposal cp) throws DAOException;
	
	/**
	 * @see org.openmrs.api.ConceptService#getConceptsWithDrugsInFormulary()
	 */
	public List<Concept> getConceptsWithDrugsInFormulary() throws DAOException;

	/**
	 * Saves the concept name tag
	 * 
	 * @param nameTag
	 * @return
	 */
	public ConceptNameTag saveConceptNameTag(ConceptNameTag nameTag);

	/**
	 * @see org.openmrs.api.ConceptService#getConceptNameTag(java.lang.Integer)
	 */
	public ConceptNameTag getConceptNameTag(Integer i);

	/**
	 * @see org.openmrs.api.ConceptService#getConceptNameTagByName(java.lang.String)
	 */
	public ConceptNameTag getConceptNameTagByName(String name);
	
	/**
	 * @see org.openmrs.api.ConceptService#getAllConceptNameTags()
	 */
	public List<ConceptNameTag> getAllConceptNameTags();
	
	/**
	 * @see org.openmrs.api.ConceptService#getConceptSource(java.lang.Integer)
	 * @throws DAOException
	 */
	public ConceptSource getConceptSource(Integer conceptSourceId) throws DAOException;
	
	/**
	 * @see org.openmrs.api.ConceptService#getAllConceptSources(boolean)
	 * @throws DAOException
	 */
	public List<ConceptSource> getAllConceptSources(boolean includeRetired) throws DAOException;
	
	/**
	 * @see org.openmrs.api.ConceptService#saveConceptSource(org.openmrs.ConceptSource)
	 */
	public ConceptSource saveConceptSource(ConceptSource conceptSource) throws DAOException;
	
	/**
	 * @see org.openmrs.api.ConceptService#purgeConceptSource(org.openmrs.ConceptSource)
	 */
	public ConceptSource deleteConceptSource(ConceptSource cs) throws DAOException;
	
	/**
	 * @see org.openmrs.api.ConceptService#getLocalesOfConceptNames()
	 */
	public Set<Locale> getLocalesOfConceptNames();
	
	/**
	 * @see ConceptService#getMaxConceptId()
	 */
	public Integer getMaxConceptId();
	
	/**
	 * @see org.openmrs.api.ConceptService#conceptIterator()
	 */
	public Iterator<Concept> conceptIterator();
	
	/**
	 * @see org.openmrs.api.ConceptService#getConceptsByMapping(java.lang.String, java.lang.String)
	 */
	public List<Concept> getConceptsByMapping(String code, String sourceName, boolean includeRetired);
	
	/**
	 * Retrieves a concept using the provided uuid
	 * 
	 * @see org.openmrs.api.ConceptService#getConceptByUuid(java.lang.String) 
	 * @param uuid the uuid provided to retrieve the concept
	 * @return concept or null
	 */
	public Concept getConceptByUuid(String uuid);
	
	/**
	 * Retrieves a concept class using the provided uuid
	 * 
	 * @see org.openmrs.api.ConceptService#getConceptClassByUuid(java.lang.String) 
	 * @param uuid the uuid provided to retrieve the concept class
	 * @return concept class or null
	 */
	public ConceptClass getConceptClassByUuid(String uuid);

	/**
	 * Retrieves a concept answer using the provided uuid
	 *
	 * @see org.openmrs.api.ConceptService#getConceptAnswerByUuid(java.lang.String) 
	 * @param uuid the uuid provided to retrieve the concept answer
	 * @return concept class or null
	 */
	public ConceptAnswer getConceptAnswerByUuid(String uuid);

	/**
	 * Retrieves a concept name using the provided uuid
	 *  
	 * @see org.openmrs.api.ConceptService#getConceptNameByUuid(java.lang.String) 
	 * @param uuid the uuid provided to retrieve the concept name
	 * @return concept name or null
	 */
	public ConceptName getConceptNameByUuid(String uuid);

	/**
	 * Retrieves a concept set using the provided uuid
	 * 
	 * @see org.openmrs.api.ConceptService#getConceptSetByUuid(java.lang.String) 
	 * @param uuid the uuid provided to retrieve the concept set
	 * @return concept set or null
	 */
	public ConceptSet getConceptSetByUuid(String uuid);

	/**
	 * Retrieves a concept source using the provided uuid
	 *
	 * @see org.openmrs.api.ConceptService#getConceptSourceByUuid(java.lang.String) 
	 * @param uuid the uuid provided to retrieve the concept source
	 * @return concept source or null
	 */
	public ConceptSource getConceptSourceByUuid(String uuid);
	
	/**
	 * Retrieves a concept data type using the provided uuid
	 * 
	 * @see org.openmrs.api.ConceptService#getConceptDatatypeByUuid(java.lang.String) 
	 * @param uuid the uuid provided to retrieve the concept data type
	 * @return concept data type or null
	 */
	public ConceptDatatype getConceptDatatypeByUuid(String uuid);
	
	/**
	 * Retrieves a concept numeric using the provided uuid
	 * 
	 * @see org.openmrs.api.ConceptService#getConceptNumericByUuid(java.lang.String) 
	 * @param uuid the uuid provided to retrieve the concept numeric
	 * @return concept numeric or null
	 */
	public ConceptNumeric getConceptNumericByUuid(String uuid);
	
	/**
	 * Retrieves a concept proposal using the provided uuid
	 * 
	 * @see org.openmrs.api.ConceptService#getConceptProposalByUuid(java.lang.String)
	 * @param uuid the uuid provided to retrieve the concept proposal
	 * @return concept proposal or null
	 */
	public ConceptProposal getConceptProposalByUuid(String uuid);
	
	/**
	 * Gets a drug using provided uuid
	 * 
	 *@see org.openmrs.api.ConceptService#getDrugByUuid(java.lang.String) 
	 * @param uuid the uuid of the drug to get
	 * @return drug or null
	 */
	public Drug getDrugByUuid(String uuid);

	/**
	 * Gets a drug ingredient using provided uuid
	 *
	 * @see org.openmrs.api.ConceptService#getDrugIngredientByUuid(java.lang.String) 
	 * @param uuid the uuid of the drug ingredient to get
	 * @return the drug ingredient if found, else null 
	 */
	public DrugIngredient getDrugIngredientByUuid(String uuid);

	/**
	 * Returns a map of concepts uuids
	 * 
	 * @return key value pairs of concept uuids
	 */
	public Map<Integer, String> getConceptUuids();

	/**
	 * Retrieves concept description using the provided uuid
	 * 
	 * @see org.openmrs.api.ConceptService#getConceptDescriptionByUuid(java.lang.String) 
	 * @param uuid The uuid of the concept description
	 * @return concept description or null
	 */
	public ConceptDescription getConceptDescriptionByUuid(String uuid);

	/**
	 * Retrieves concept name tag using the provided uuid
	 * 
	 * @see org.openmrs.api.ConceptService#getConceptNameByUuid(java.lang.String) 
	 * @param uuid The uuid of the concept name tag
	 * @return concept name or null 
	 */
	public ConceptNameTag getConceptNameTagByUuid(String uuid);
	
	/**
	 * @see ConceptService#getConceptMappingsToSource(ConceptSource)
	 */
	public List<ConceptMap> getConceptMapsBySource(ConceptSource conceptSource) throws DAOException;
	
	/**
	 * @see org.openmrs.api.ConceptService#getConceptSourceByName(java.lang.String)
	 */
	public ConceptSource getConceptSourceByName(String conceptSourceName) throws DAOException;

	/**
	 * @see org.openmrs.api.ConceptService#getConceptSourceByUniqueId(java.lang.String)
	 */
	public ConceptSource getConceptSourceByUniqueId(String uniqueId);

	/**
	 * @see org.openmrs.api.ConceptService#getConceptSourceByHL7Code(java.lang.String)
	 */
	public ConceptSource getConceptSourceByHL7Code(String hl7Code);

	/**
	 * Gets the value of conceptDatatype currently saved in the database for the given concept,
	 * bypassing any caches. This is used prior to saving an concept so that we can change the obs
	 * if need be
	 * 
	 * @param concept The concept for which the concept data type should be fetched
	 * @return the concept data type currently in the database for this concept
	 * <strong>Should</strong> get saved conceptDatatype from database
	 */
	public ConceptDatatype getSavedConceptDatatype(Concept concept);
	
	/**
	 * Gets the persisted copy of the conceptName currently saved in the database for the given
	 * conceptName, bypassing any caches. This is used prior to saving an concept so that we can
	 * change the obs if need be or avoid breaking any obs referencing it.
	 * 
	 * @param conceptName The concept name to fetch from the database
	 * @return the persisted copy of the concept name currently saved in the database for this
	 *         conceptName
	 */
	public ConceptName getSavedConceptName(ConceptName conceptName);
	
	/**
	 * @see org.openmrs.api.ConceptService#saveConceptStopWord(org.openmrs.ConceptStopWord)
	 */
	public ConceptStopWord saveConceptStopWord(ConceptStopWord conceptStopWord) throws DAOException;
	
	/**
	 * @see org.openmrs.api.ConceptService#deleteConceptStopWord(Integer)
	 */
	public void deleteConceptStopWord(Integer conceptStopWordId) throws DAOException;
	
	/**
	 * @see org.openmrs.api.ConceptService#getConceptStopWords(java.util.Locale)
	 */
	public List<String> getConceptStopWords(Locale locale) throws DAOException;
	
	/**
	 * @see org.openmrs.api.ConceptService#getAllConceptStopWords()
	 */
	public List<ConceptStopWord> getAllConceptStopWords();
	
	/**
	 * @see org.openmrs.api.ConceptService#getCountOfDrugs(String, Concept, boolean, boolean, boolean)
	 * @throws DAOException
	 */
	public Long getCountOfDrugs(String drugName, Concept concept, boolean searchOnPhrase, boolean searchDrugConceptNames,
	        boolean includeRetired) throws DAOException;
	
	/**
	 * @see ConceptService#getDrugs(String, Concept, boolean, boolean, boolean, Integer, Integer)
	 * @throws DAOException
	 */
	public List<Drug> getDrugs(String drugName, Concept concept, boolean searchOnPhrase, boolean searchDrugConceptNames,
	        boolean includeRetired, Integer start, Integer length) throws DAOException;
	
	/**
	 * @see org.openmrs.api.ConceptService#getDrugsByIngredient(org.openmrs.Concept)
	 */
	public List<Drug> getDrugsByIngredient(Concept ingredient);
	
	/**
	 * @see ConceptService#getConceptMapTypes(boolean, boolean)
	 * @throws DAOException
	 */
	public List<ConceptMapType> getConceptMapTypes(boolean includeRetired, boolean includeHidden) throws DAOException;
	
	/**
	 * @see org.openmrs.api.ConceptService#getConceptMapType(java.lang.Integer)
	 * @throws DAOException
	 */
	public ConceptMapType getConceptMapType(Integer conceptMapTypeId) throws DAOException;
	
	/**
	 * @see org.openmrs.api.ConceptService#getConceptMapTypeByUuid(java.lang.String)
	 * @throws DAOException
	 */
	public ConceptMapType getConceptMapTypeByUuid(String uuid) throws DAOException;
	
	/**
	 * @see org.openmrs.api.ConceptService#getConceptMapTypeByName(java.lang.String)
	 * @throws DAOException
	 */
	public ConceptMapType getConceptMapTypeByName(String name) throws DAOException;
	
	/**
	 * @see ConceptService#saveConceptMapType(ConceptMapType)
	 * @throws DAOException
	 */
	public ConceptMapType saveConceptMapType(ConceptMapType conceptMapType) throws DAOException;
	
	/**
	 * @see ConceptService#purgeConceptMapType(ConceptMapType)
	 * @throws DAOException
	 */
	public void deleteConceptMapType(ConceptMapType conceptMapType) throws DAOException;
	
	/**
	 * @see ConceptService#getConceptReferenceTerms(boolean)
	 * @throws DAOException
	 */
	public List<ConceptReferenceTerm> getConceptReferenceTerms(boolean includeRetired) throws DAOException;
	
	/**
	 * @see org.openmrs.api.ConceptService#getConceptReferenceTerm(java.lang.Integer)
	 * @throws DAOException
	 */
	public ConceptReferenceTerm getConceptReferenceTerm(Integer conceptReferenceTermId) throws DAOException;
	
	/**
	 * @see org.openmrs.api.ConceptService#getConceptReferenceTermByUuid(java.lang.String)
	 * @throws DAOException
	 */
	public ConceptReferenceTerm getConceptReferenceTermByUuid(String uuid) throws DAOException;

	/**
	 * @see org.openmrs.api.ConceptService#getCountOfConceptReferenceTerms(String, ConceptSource, boolean) 
	 * @throws DAOException
	 */
	public List<ConceptReferenceTerm> getConceptReferenceTermsBySource(ConceptSource conceptSource) throws DAOException;
	
	/**
	 * @see org.openmrs.api.ConceptService#getConceptReferenceTermByName(String, ConceptSource)
	 */
	public ConceptReferenceTerm getConceptReferenceTermByName(String name, ConceptSource conceptSource) throws DAOException;
	
	/**
	 * @see org.openmrs.api.ConceptService#getConceptReferenceTermByCode(String, ConceptSource)
	 */
	public ConceptReferenceTerm getConceptReferenceTermByCode(String code, ConceptSource conceptSource) throws DAOException;
	
	/**
	 * @see org.openmrs.api.ConceptService#saveConceptReferenceTerm(ConceptReferenceTerm)
	 */
	public ConceptReferenceTerm saveConceptReferenceTerm(ConceptReferenceTerm conceptReferenceTerm) throws DAOException;
	
	/**
	 * @see org.openmrs.api.ConceptService#purgeConceptReferenceTerm(ConceptReferenceTerm)
	 */
	public void deleteConceptReferenceTerm(ConceptReferenceTerm conceptReferenceTerm) throws DAOException;
	
	/**
	 * @see org.openmrs.api.ConceptService#getCountOfConceptReferenceTerms(String, ConceptSource, boolean)
	 */
	public Long getCountOfConceptReferenceTerms(String query, ConceptSource conceptSource, boolean includeRetired)
	        throws DAOException;
	
	/**
	 * @see org.openmrs.api.ConceptService#getConceptReferenceTerms(String, ConceptSource, Integer, Integer,
	 *      boolean)
	 */
	public List<ConceptReferenceTerm> getConceptReferenceTerms(String query, ConceptSource conceptSource, Integer start,
	        Integer length, boolean includeRetired) throws APIException;
	
	/**
	 * @see org.openmrs.api.ConceptService#getReferenceTermMappingsTo(ConceptReferenceTerm)
	 */
	public List<ConceptReferenceTermMap> getReferenceTermMappingsTo(ConceptReferenceTerm term) throws DAOException;
	
	/**
	 * Checks if there are any {@link ConceptReferenceTermMap}s or {@link ConceptMap}s using the
	 * specified term
	 * 
	 * @param term
	 * @return true if term is in use
	 * @throws DAOException
	 * <strong>Should</strong> return true if a term has a conceptMap or more using it
	 * <strong>Should</strong> return true if a term has a conceptReferenceTermMap or more using it
	 * <strong>Should</strong> return false if a term has no maps using it
	 */
	public boolean isConceptReferenceTermInUse(ConceptReferenceTerm term) throws DAOException;
	
	/**
	 * Checks if there are any {@link ConceptReferenceTermMap}s or {@link ConceptMap}s using the
	 * specified mapType
	 * 
	 * @param mapType
	 * @return true if map type is in use
	 * @throws DAOException
	 * <strong>Should</strong> return true if a mapType has a conceptMap or more using it
	 * <strong>Should</strong> return true if a mapType has a conceptReferenceTermMap or more using it
	 * <strong>Should</strong> return false if a mapType has no maps using it
	 */
	public boolean isConceptMapTypeInUse(ConceptMapType mapType) throws DAOException;
	
	/**
	 * @see org.openmrs.api.ConceptService#getConceptsByName(String, Locale, Boolean)
	 */
	public List<Concept> getConceptsByName(String name, Locale locale, Boolean exactLocal);
	
	/**
	 * @see org.openmrs.api.ConceptService#getConceptByName(String)
	 */
	public Concept getConceptByName(String name);
	
	/**
	 * It is in the DAO, because it must be done in the MANUAL flush mode to prevent premature
	 * flushes in {@link ConceptService#saveConcept(Concept)}. It will be removed in 1.10 when we
	 * have a better way to manage flush modes.
	 * 
	 * @see org.openmrs.api.ConceptService#getDefaultConceptMapType()
	 */
	public ConceptMapType getDefaultConceptMapType() throws DAOException;
	
	/**
	 * @see org.openmrs.api.ConceptService#isConceptNameDuplicate(ConceptName)
	 */
	public boolean isConceptNameDuplicate(ConceptName name);
	
	/**
	 * @see ConceptService#getDrugs(String, java.util.Locale, boolean, boolean)
	 */
	public List<Drug> getDrugs(String searchPhrase, Locale locale, boolean exactLocale, boolean includeRetired);
	
	/**
	 * @see org.openmrs.api.ConceptService#getDrugsByMapping(String, ConceptSource, Collection,
	 *      boolean)
	 */
	public List<Drug> getDrugsByMapping(String code, ConceptSource conceptSource,
	        Collection<ConceptMapType> withAnyOfTheseTypes, boolean includeRetired) throws DAOException;
	
	/**
	 * @see org.openmrs.api.ConceptService#getDrugByMapping(String, org.openmrs.ConceptSource, java.util.Collection)
	 */
	Drug getDrugByMapping(String code, ConceptSource conceptSource,
	        Collection<ConceptMapType> withAnyOfTheseTypesOrOrderOfPreference) throws DAOException;

	/**
	 * @see org.openmrs.api.ConceptService#getAllConceptAttributeTypes()
	 */

	List<ConceptAttributeType> getAllConceptAttributeTypes();

	/**
	 * @see org.openmrs.api.ConceptService#saveConceptAttributeType(ConceptAttributeType)
	 */
	ConceptAttributeType saveConceptAttributeType(ConceptAttributeType conceptAttributeType);

	/**
	 * @see org.openmrs.api.ConceptService#getConceptAttributeType(java.lang.Integer)
	 */
	ConceptAttributeType getConceptAttributeType(Integer id);

	/**
	 * @see org.openmrs.api.ConceptService#getConceptAttributeTypeByUuid(java.lang.String)
	 */
	ConceptAttributeType getConceptAttributeTypeByUuid(String uuid);

	/**
	 * @see org.openmrs.api.ConceptService#purgeConceptAttributeType(ConceptAttributeType)
	 */
	public void deleteConceptAttributeType(ConceptAttributeType conceptAttributeType);

	/**
	 * @see org.openmrs.api.ConceptService#getConceptAttributeTypes(java.lang.String)
	 */
	public List<ConceptAttributeType> getConceptAttributeTypes(String name);

	/**
	 * @see org.openmrs.api.ConceptService#getConceptAttributeTypeByName(java.lang.String)
	 */
	public ConceptAttributeType getConceptAttributeTypeByName(String exactName);

	/**
	 * @see org.openmrs.api.ConceptService#getConceptAttributeByUuid(java.lang.String)
	 */
	public ConceptAttribute getConceptAttributeByUuid(String uuid);

	/**
	 * @see org.openmrs.api.ConceptService#hasAnyConceptAttribute(ConceptAttributeType)
	 */
	public long getConceptAttributeCount(ConceptAttributeType conceptAttributeType);

	/**
	 *@see org.openmrs.api.ConceptService#getConceptsByClass(ConceptClass) 
	 */
	List<Concept> getConceptsByClass(ConceptClass conceptClass);
}
