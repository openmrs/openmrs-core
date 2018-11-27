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
 * @see ConceptService
 */
public interface ConceptDAO {
	
	/**
	 * @see org.openmrs.api.ConceptService#saveConcept(org.openmrs.Concept)
	 */
	public Concept saveConcept(Concept concept) throws DAOException;
	
	/**
	 * @see org.openmrs.api.ConceptService#purgeConcept(org.openmrs.Concept)
	 * @should purge concept
	 */
	public void purgeConcept(Concept concept) throws DAOException;
	
	/**
	 * @see org.openmrs.api.ConceptService#getConceptComplex(java.lang.Integer)
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
	 * @see org.openmrs.api.ConceptService#getConceptName(java.lang.Integer)
	 */
	public ConceptName getConceptName(Integer conceptNameId) throws DAOException;
	
	/**
	 * @see org.openmrs.api.ConceptService#getAllConcepts(java.lang.String, boolean, boolean)
	 */
	public List<Concept> getAllConcepts(String sortBy, boolean asc, boolean includeRetired) throws DAOException;
	
	/**
	 * Returns a list of concepts based on the search criteria
	 * 
	 * @param name
	 * @param loc
	 * @param searchOnPhrase This puts wildcard characters around the concept name search criteria
	 * @return List&lt;Concept&gt;
	 * @throws DAOException
	 * @should not return concepts with matching names that are voided
	 */
	public List<Concept> getConcepts(String name, Locale loc, boolean searchOnPhrase,
	       List<ConceptClass> classes, List<ConceptDatatype> datatypes) throws DAOException;

	/**
	 * @should return correct results for concept with names that contains words with more weight
	 * @should return correct results if a concept name contains same word more than once
	 * @see org.openmrs.api.ConceptService#getConcepts(String, List, boolean, List, List, List, List, Concept,
	 *      Integer, Integer)
	 */
	public List<ConceptSearchResult> getConcepts(String phrase, List<Locale> locales, boolean includeRetired,
	        List<ConceptClass> requireClasses, List<ConceptClass> excludeClasses, List<ConceptDatatype> requireDatatypes,
	        List<ConceptDatatype> excludeDatatypes, Concept answersToConcept, Integer start, Integer size)
	        throws DAOException;
	
	/**
	 * @see org.openmrs.api.ConceptService#getCountOfConcepts(String, List, boolean, List, List, List, List, Concept)  
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
	 * @see org.openmrs.api.ConceptService#getDrugs(String, Concept, boolean, boolean, boolean, Integer, Integer)
	 */
	public List<Drug> getDrugs(String drugName, Concept concept, boolean searchKeywords, boolean searchDrugConceptNames,
	        boolean includeRetired, Integer start, Integer length) throws APIException;
	
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
	 * @see org.openmrs.api.ConceptService#getConceptDatatypeByName(java.lang.String)
	 */
	public ConceptDatatype getConceptDatatypeByName(String name) throws DAOException;
	
	/**
	 * @see org.openmrs.api.ConceptService#getConceptDatatype(java.lang.Integer)
	 */
	public ConceptDatatype getConceptDatatype(Integer i) throws DAOException;
	
	/**
	 * @see org.openmrs.api.ConceptService#saveConceptDatatype(org.openmrs.ConceptDatatype)
	 */
	public ConceptDatatype saveConceptDatatype(ConceptDatatype cd) throws DAOException;
	
	/**
	 * @see org.openmrs.api.ConceptService#purgeConceptDatatype(org.openmrs.ConceptDatatype)
	 */
	public void purgeConceptDatatype(ConceptDatatype cd) throws DAOException;
	
	/**
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
	 * @see org.openmrs.api.ConceptService#getConceptsByAnswer(org.openmrs.Concept)
	 * @should return concepts for the given answer concept
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
	 * @see org.openmrs.api.ConceptService#saveConceptNameTag(org.openmrs.ConceptNameTag)
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
	 */
	public ConceptSource getConceptSource(Integer conceptSourceId) throws DAOException;
	
	/**
	 * @see org.openmrs.api.ConceptService#getAllConceptSources(boolean)
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
	 * @see org.openmrs.api.ConceptService#getConceptsByMapping(String, String, boolean)
	 */
	public List<Concept> getConceptsByMapping(String code, String sourceName, boolean includeRetired);
	
	/**
	 * @see org.openmrs.api.ConceptService#getConceptByUuid(java.lang.String)
	 */
	public Concept getConceptByUuid(String uuid);
	
	/**
	 * @see org.openmrs.api.ConceptService#getConceptClassByUuid(java.lang.String)
	 */
	public ConceptClass getConceptClassByUuid(String uuid);
	
	/**
	 * @see org.openmrs.api.ConceptService#getConceptAnswerByUuid(java.lang.String)
	 */
	public ConceptAnswer getConceptAnswerByUuid(String uuid);
	
	/**
	 * @see org.openmrs.api.ConceptService#getConceptNameByUuid(java.lang.String)
	 */
	public ConceptName getConceptNameByUuid(String uuid);
	
	/**
	 * @see org.openmrs.api.ConceptService#getConceptSetByUuid(java.lang.String)
	 */
	public ConceptSet getConceptSetByUuid(String uuid);
	
	/**
	 * @see org.openmrs.api.ConceptService#getConceptSourceByUuid(java.lang.String)
	 */
	public ConceptSource getConceptSourceByUuid(String uuid);
	
	/**
	 * @see org.openmrs.api.ConceptService#getConceptDatatypeByUuid(java.lang.String)
	 */
	public ConceptDatatype getConceptDatatypeByUuid(String uuid);
	
	/**
	 * @see org.openmrs.api.ConceptService#getConceptNumericByUuid(java.lang.String)
	 */
	public ConceptNumeric getConceptNumericByUuid(String uuid);
	
	/**
	 * @see org.openmrs.api.ConceptService#getConceptProposalByUuid(java.lang.String)
	 */
	public ConceptProposal getConceptProposalByUuid(String uuid);
	
	/**
	 * @see org.openmrs.api.ConceptService#getDrugByUuid(java.lang.String)
	 */
	public Drug getDrugByUuid(String uuid);
	
	/**
	 * @see org.openmrs.api.ConceptService#getDrugIngredientByUuid(java.lang.String)
	 */
	public DrugIngredient getDrugIngredientByUuid(String uuid);
	
	/**
	 * Returns a map of all concepts, wherein the ConceptID is mapped to the uuid of 
	 * the corresponding Concept
	 * 
	 * @return Map that has all the uuids of all concepts
	 * @throws APIException
	 * @should return a map of all the concept uuids
	 */
	public Map<Integer, String> getConceptUuids();
	
	/**
	 * @see org.openmrs.api.ConceptService#getConceptDescriptionByUuid(java.lang.String)
	 */
	public ConceptDescription getConceptDescriptionByUuid(String uuid);
	
	/**
	 * @see org.openmrs.api.ConceptService#getConceptNameTagByUuid(java.lang.String)
	 */
	public ConceptNameTag getConceptNameTagByUuid(String uuid);
	
	/**
	 * @see org.openmrs.api.ConceptService#getConceptMappingsToSource(org.openmrs.ConceptSource)
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
	 * @should get saved conceptDatatype from database
	 * @see org.openmrs.api.ConceptService#getSavedConceptDatatype(org.openmrs.Concept)
	 */
	public ConceptDatatype getSavedConceptDatatype(Concept concept);
	
	/**
	 * @see org.openmrs.api.ConceptService#getSavedConceptName(org.openmrs.ConceptName)
	 */
	public ConceptName getSavedConceptName(ConceptName conceptName);
	
	/**
	 * @see org.openmrs.api.ConceptService#saveConceptStopWord(org.openmrs.ConceptStopWord)
	 */
	public ConceptStopWord saveConceptStopWord(ConceptStopWord conceptStopWord) throws DAOException;
	
	/**
	 * @see org.openmrs.api.ConceptService#deleteConceptStopWord(java.lang.Integer)
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
	 */
	public Long getCountOfDrugs(String drugName, Concept concept, boolean searchOnPhrase, boolean searchDrugConceptNames,
	        boolean includeRetired) throws DAOException;
	
	/**
	 * @see org.openmrs.api.ConceptService#getDrugs(String, Concept, boolean, boolean, boolean, Integer, Integer)
	 */
	public List<Drug> getDrugs(String drugName, Concept concept, boolean searchOnPhrase, boolean searchDrugConceptNames,
	        boolean includeRetired, Integer start, Integer length) throws DAOException;
	
	/**
	 * @see org.openmrs.api.ConceptService#getDrugsByIngredient(Concept)
	 */
	public List<Drug> getDrugsByIngredient(Concept ingredient);
	
	/**
	 * @see org.openmrs.api.ConceptService#getConceptMapTypes(boolean, boolean)
	 */
	public List<ConceptMapType> getConceptMapTypes(boolean includeRetired, boolean includeHidden) throws DAOException;
	
	/**
	 * @see org.openmrs.api.ConceptService#getConceptMapType(java.lang.Integer)
	 */
	public ConceptMapType getConceptMapType(Integer conceptMapTypeId) throws DAOException;
	
	/**
	 * @see org.openmrs.api.ConceptService#getConceptMapTypeByUuid(java.lang.String)
	 */
	public ConceptMapType getConceptMapTypeByUuid(String uuid) throws DAOException;
	
	/**
	 * @see org.openmrs.api.ConceptService#getConceptMapTypeByName(java.lang.String)
	 */
	public ConceptMapType getConceptMapTypeByName(String name) throws DAOException;
	
	/**
	 * @see org.openmrs.api.ConceptService#saveConceptMapType(org.openmrs.ConceptMapType)
	 */
	public ConceptMapType saveConceptMapType(ConceptMapType conceptMapType) throws DAOException;
	
	/**
	 * @see org.openmrs.api.ConceptService#purgeConceptMapType(org.openmrs.ConceptMapType)
	 */
	public void deleteConceptMapType(ConceptMapType conceptMapType) throws DAOException;
	
	/**
	 * @see ConceptService#getConceptReferenceTerms(java.lang.boolean)
	 */
	public List<ConceptReferenceTerm> getConceptReferenceTerms(boolean includeRetired) throws DAOException;
	
	/**
	 * @see ConceptService#getConceptReferenceTerm(java.lang.Integer)
	 */
	public ConceptReferenceTerm getConceptReferenceTerm(Integer conceptReferenceTermId) throws DAOException;
	
	/**
	 * @see ConceptService#getConceptReferenceTermByUuid(java.lang.String)
	 */
	public ConceptReferenceTerm getConceptReferenceTermByUuid(String uuid) throws DAOException;
	
	/**
	 * Gets a list of concept reference terms with a specific ConceptSource
	 * 
	 * @param the ConceptSource to match against
	 * @return ConceptReferenceTerm list
	 * @throws APIException
	 * @should return a concept reference term that matches the given source
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
	 * @see org.openmrs.api.ConceptService#saveConceptReferenceTerm(org.openmrs.ConceptReferenceTerm)
	 */
	public ConceptReferenceTerm saveConceptReferenceTerm(ConceptReferenceTerm conceptReferenceTerm) throws DAOException;
	
	/**
	 * @see org.openmrs.api.ConceptService#purgeConceptReferenceTerm(org.openmrs.ConceptReferenceTerm)
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
	 * @see org.openmrs.api.ConceptService#getReferenceTermMappingsTo(org.openmrs.ConceptReferenceTerm)
	 */
	public List<ConceptReferenceTermMap> getReferenceTermMappingsTo(ConceptReferenceTerm term) throws DAOException;
	
	/**
	 * @should return true if a term has a conceptMap or more using it
	 * @should return true if a term has a conceptReferenceTermMap or more using it
	 * @should return false if a term has no maps using it
	 * @see ConceptService#isConceptReferenceTermInUse(org.openmrs.ConceptReferenceTerm)
	 */
	public boolean isConceptReferenceTermInUse(ConceptReferenceTerm term) throws DAOException;
	
	/**
	 * @should return true if a mapType has a conceptMap or more using it
	 * @should return true if a mapType has a conceptReferenceTermMap or more using it
	 * @should return false if a mapType has no maps using it
	 * @see ConceptService#isConceptMapTypeInUse(org.openmrs.ConceptMapType)
	 */
	public boolean isConceptMapTypeInUse(ConceptMapType mapType) throws DAOException;
	
	/**
	 * @see org.openmrs.api.ConceptService#getConceptsByName(String, Locale, Boolean)
	 */
	public List<Concept> getConceptsByName(String name, Locale locale, Boolean exactLocal);
	
	/**
	 * @see org.openmrs.api.ConceptService#getConceptByName(java.lang.String)
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
	 * @see org.openmrs.api.ConceptService#isConceptNameDuplicate(org.openmrs.ConceptName)
	 */
	public boolean isConceptNameDuplicate(ConceptName name);
	
	/**
	 * @see org.openmrs.api.ConceptService#getDrugs(String, Locale, boolean, boolean)
	 */
	public List<Drug> getDrugs(String searchPhrase, Locale locale, boolean exactLocale, boolean includeRetired);
	
	/**
	 * @see org.openmrs.api.ConceptService#getDrugsByMapping(String, ConceptSource, Collection,
	 *      boolean)
	 */
	public List<Drug> getDrugsByMapping(String code, ConceptSource conceptSource,
	        Collection<ConceptMapType> withAnyOfTheseTypes, boolean includeRetired) throws DAOException;
	
	/**
	 * @see org.openmrs.api.ConceptService#getDrugByMapping(String, ConceptSource, Collection)
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
	 * @see org.openmrs.api.ConceptService#getConceptAttributeType(Integer)
	 */
	ConceptAttributeType getConceptAttributeType(Integer id);

	/**
	 * @see org.openmrs.api.ConceptService#getConceptAttributeTypeByUuid(String)
	 */
	ConceptAttributeType getConceptAttributeTypeByUuid(String uuid);

	/**
	 * @see org.openmrs.api.ConceptService#purgeConceptAttributeType(ConceptAttributeType)
	 */
	public void deleteConceptAttributeType(ConceptAttributeType conceptAttributeType);

	/**
	 * @see org.openmrs.api.ConceptService#getConceptAttributeTypes(String)
	 */
	public List<ConceptAttributeType> getConceptAttributeTypes(String name);

	/**
	 * @see org.openmrs.api.ConceptService#getConceptAttributeTypeByName(String)
	 */
	public ConceptAttributeType getConceptAttributeTypeByName(String exactName);

	/**
	 * @see org.openmrs.api.ConceptService#getConceptAttributeByUuid(String)
	 */
	public ConceptAttribute getConceptAttributeByUuid(String uuid);

	/**
	 * @see ConceptService#hasAnyConceptAttribute(ConceptAttributeType)
	 */
	public long getConceptAttributeCount(ConceptAttributeType conceptAttributeType);
}
