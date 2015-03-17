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
	 * Get a ConceptComplex. The Concept.getDatatype() is "Complex" and the Concept.getHandler() is
	 * the class name for the ComplexObsHandler key associated with this ConceptComplex.
	 * 
	 * @param conceptId
	 * @return the ConceptComplex
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
	 * @param conceptNameId
	 * @return The ConceptName matching the specified conceptNameId
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
	 * @return List<Concept>
	 * @throws DAOException
	 * @should not return concepts with matching names that are voided
	 */
	public List<Concept> getConcepts(String name, Locale loc, boolean searchOnPhrase, List<ConceptClass> classes,
	        List<ConceptDatatype> datatypes) throws DAOException;
	
	/**
	 * @see ConceptService#getConcepts(String, List, boolean, List, List, List, List, Concept,
	 *      Integer, Integer)
	 * @throws DAOException
	 * @should return correct results for concept with names that contains words with more weight
	 * @should return correct results if a concept name contains same word more than once
	 */
	public List<ConceptSearchResult> getConcepts(String phrase, List<Locale> locales, boolean includeRetired,
	        List<ConceptClass> requireClasses, List<ConceptClass> excludeClasses, List<ConceptDatatype> requireDatatypes,
	        List<ConceptDatatype> excludeDatatypes, Concept answersToConcept, Integer start, Integer size)
	        throws DAOException;
	
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
	 * DAO for retrieving a list of drugs based on the following criteria
	 * 
	 * @param drugName
	 * @param concept
	 * @param includeRetired
	 * @return List<Drug>
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
	 * @deprecated
	 * @see org.openmrs.api.ConceptService#getConceptDatatypes(java.lang.String)
	 */
	@Deprecated
	public List<ConceptDatatype> getConceptDatatypes(String name) throws DAOException;
	
	/**
	 * @param name
	 * @return the {@link ConceptDatatype} that matches <em>name</em> exactly or null if one does
	 *         not exist.
	 * @throws DAOException
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
	
	public ConceptNameTag saveConceptNameTag(ConceptNameTag nameTag);
	
	public ConceptNameTag getConceptNameTag(Integer i);
	
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
	 * @see org.openmrs.api.ConceptService#getAllConceptSources(java.lang.boolean)
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
	 * @see org.openmrs.api.ConceptService@getConceptsByMapping(java.lang.String, java.lang.String)
	 */
	public List<Concept> getConceptsByMapping(String code, String sourceName, boolean includeRetired);
	
	/**
	 * Auto generated method comment
	 * 
	 * @param uuid
	 * @return
	 */
	public Concept getConceptByUuid(String uuid);
	
	/**
	 * Auto generated method comment
	 * 
	 * @param uuid
	 * @return
	 */
	public ConceptClass getConceptClassByUuid(String uuid);
	
	public ConceptAnswer getConceptAnswerByUuid(String uuid);
	
	public ConceptName getConceptNameByUuid(String uuid);
	
	public ConceptSet getConceptSetByUuid(String uuid);
	
	public ConceptSource getConceptSourceByUuid(String uuid);
	
	/**
	 * Auto generated method comment
	 * 
	 * @param uuid
	 * @return
	 */
	public ConceptDatatype getConceptDatatypeByUuid(String uuid);
	
	/**
	 * Auto generated method comment
	 * 
	 * @param uuid
	 * @return
	 */
	public ConceptNumeric getConceptNumericByUuid(String uuid);
	
	/**
	 * Auto generated method comment
	 * 
	 * @param uuid
	 * @return
	 */
	public ConceptProposal getConceptProposalByUuid(String uuid);
	
	/**
	 * Auto generated method comment
	 * 
	 * @param uuid
	 * @return
	 */
	public Drug getDrugByUuid(String uuid);
	
	public DrugIngredient getDrugIngredientByUuid(String uuid);
	
	/**
	 * @see org.openmrs.api.ConceptService#getConceptUuids()
	 */
	public Map<Integer, String> getConceptUuids();
	
	public ConceptDescription getConceptDescriptionByUuid(String uuid);
	
	public ConceptNameTag getConceptNameTagByUuid(String uuid);
	
	/**
	 * @see ConceptService#getConceptMapsBySource(ConceptSource)
	 */
	public List<ConceptMap> getConceptMapsBySource(ConceptSource conceptSource) throws DAOException;
	
	/**
	 * @see org.openmrs.api.ConceptService#getConceptSourceByName(java.lang.String)
	 */
	public ConceptSource getConceptSourceByName(String conceptSourceName) throws DAOException;
	
	/**
	 * Gets the value of conceptDatatype currently saved in the database for the given concept,
	 * bypassing any caches. This is used prior to saving an concept so that we can change the obs
	 * if need be
	 * 
	 * @param concept for which the conceptDatatype should be fetched
	 * @return the conceptDatatype currently in the database for this concept
	 * @should get saved conceptDatatype from database
	 */
	public ConceptDatatype getSavedConceptDatatype(Concept concept);
	
	/**
	 * Gets the persisted copy of the conceptName currently saved in the database for the given
	 * conceptName, bypassing any caches. This is used prior to saving an concept so that we can
	 * change the obs if need be or avoid breaking any obs referencing it.
	 * 
	 * @param conceptName ConceptName to fetch from the database
	 * @return the persisted copy of the conceptName currently saved in the database for this
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
	 * @see ConceptService#getCountOfDrugs(String, Concept, boolean, boolean, boolean)
	 */
	public Long getCountOfDrugs(String drugName, Concept concept, boolean searchOnPhrase, boolean searchDrugConceptNames,
	        boolean includeRetired) throws DAOException;
	
	/**
	 * @see ConceptService#getDrugs(String, Concept, boolean, boolean, boolean, Integer, Integer)
	 */
	public List<Drug> getDrugs(String drugName, Concept concept, boolean searchOnPhrase, boolean searchDrugConceptNames,
	        boolean includeRetired, Integer start, Integer length) throws DAOException;
	
	/**
	 * @see ConceptService#getDrugsByIngredient(Concept)
	 */
	public List<Drug> getDrugsByIngredient(Concept ingredient);
	
	/**
	 * @see ConceptService#getConceptMapTypes(boolean, boolean)
	 */
	public List<ConceptMapType> getConceptMapTypes(boolean includeRetired, boolean includeHidden) throws DAOException;
	
	/**
	 * @see ConceptService#getConceptMapType(Integer)
	 */
	public ConceptMapType getConceptMapType(Integer conceptMapTypeId) throws DAOException;
	
	/**
	 * @see ConceptService#getConceptMapTypeByUuid(String)
	 */
	public ConceptMapType getConceptMapTypeByUuid(String uuid) throws DAOException;
	
	/**
	 * @see ConceptService#getConceptMapTypeByName(String)
	 */
	public ConceptMapType getConceptMapTypeByName(String name) throws DAOException;
	
	/**
	 * @see ConceptService#saveConceptMapType(ConceptMapType)
	 */
	public ConceptMapType saveConceptMapType(ConceptMapType conceptMapType) throws DAOException;
	
	/**
	 * @see ConceptService#purgeConceptMapType(ConceptMapType)
	 */
	public void deleteConceptMapType(ConceptMapType conceptMapType) throws DAOException;
	
	/**
	 * @see ConceptService#getConceptReferenceTerms(boolean)
	 */
	public List<ConceptReferenceTerm> getConceptReferenceTerms(boolean includeRetired) throws DAOException;
	
	/**
	 * @see ConceptService#getConceptReferenceTerm(Integer)
	 */
	public ConceptReferenceTerm getConceptReferenceTerm(Integer conceptReferenceTermId) throws DAOException;
	
	/**
	 * @see ConceptService#getConceptReferenceTermByUuid(String)
	 */
	public ConceptReferenceTerm getConceptReferenceTermByUuid(String uuid) throws DAOException;
	
	/**
	 * @see ConceptService#getConceptReferenceTermsBySource(ConceptSource)
	 */
	public List<ConceptReferenceTerm> getConceptReferenceTermsBySource(ConceptSource conceptSource) throws DAOException;
	
	/**
	 * @see ConceptService#getConceptReferenceTermByName(String, ConceptSource)
	 */
	public ConceptReferenceTerm getConceptReferenceTermByName(String name, ConceptSource conceptSource) throws DAOException;
	
	/**
	 * @see ConceptService#getConceptReferenceTermByCode(String, ConceptSource)
	 */
	public ConceptReferenceTerm getConceptReferenceTermByCode(String code, ConceptSource conceptSource) throws DAOException;
	
	/**
	 * @see ConceptService#saveConceptReferenceTerm(ConceptReferenceTerm)
	 */
	public ConceptReferenceTerm saveConceptReferenceTerm(ConceptReferenceTerm conceptReferenceTerm) throws DAOException;
	
	/**
	 * @see ConceptService#purgeConceptReferenceTerm(ConceptReferenceTerm)
	 */
	public void deleteConceptReferenceTerm(ConceptReferenceTerm conceptReferenceTerm) throws DAOException;
	
	/**
	 * @see ConceptService#getCountOfConceptReferenceTerms(String, ConceptSource, boolean)
	 */
	public Long getCountOfConceptReferenceTerms(String query, ConceptSource conceptSource, boolean includeRetired)
	        throws DAOException;
	
	/**
	 * @see ConceptService#getConceptReferenceTerms(String, ConceptSource, Integer, Integer,
	 *      boolean)
	 */
	public List<ConceptReferenceTerm> getConceptReferenceTerms(String query, ConceptSource conceptSource, Integer start,
	        Integer length, boolean includeRetired) throws APIException;
	
	/**
	 * @see ConceptService#getReferenceTermMappingsTo(ConceptReferenceTerm)
	 */
	public List<ConceptReferenceTermMap> getReferenceTermMappingsTo(ConceptReferenceTerm term) throws DAOException;
	
	/**
	 * Checks if there are any {@link ConceptReferenceTermMap}s or {@link ConceptMap}s using the
	 * specified term
	 * 
	 * @param term
	 * @return
	 * @throws DAOException
	 * @should return true if a term has a conceptMap or more using it
	 * @should return true if a term has a conceptReferenceTermMap or more using it
	 * @should return false if a term has no maps using it
	 */
	public boolean isConceptReferenceTermInUse(ConceptReferenceTerm term) throws DAOException;
	
	/**
	 * Checks if there are any {@link ConceptReferenceTermMap}s or {@link ConceptMap}s using the
	 * specified mapType
	 * 
	 * @param mapType
	 * @return
	 * @throws DAOException
	 * @should return true if a mapType has a conceptMap or more using it
	 * @should return true if a mapType has a conceptReferenceTermMap or more using it
	 * @should return false if a mapType has no maps using it
	 */
	public boolean isConceptMapTypeInUse(ConceptMapType mapType) throws DAOException;
	
	/**
	 * @see ConceptService#getConceptsByName(String, Locale, Boolean)
	 */
	public List<Concept> getConceptsByName(String name, Locale locale, Boolean exactLocal);
	
	/**
	 * @see ConceptService#getConceptByName(String)
	 */
	public Concept getConceptByName(String name);
	
	/**
	 * It is in the DAO, because it must be done in the MANUAL flush mode to prevent premature
	 * flushes in {@link ConceptService#saveConcept(Concept)}. It will be removed in 1.10 when we
	 * have a better way to manage flush modes.
	 * 
	 * @see ConceptService#getDefaultConceptMapType()
	 */
	public ConceptMapType getDefaultConceptMapType() throws DAOException;
	
	/**
	 * @see ConceptService#isConceptNameDuplicate(ConceptName)
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
	 * @see org.openmrs.api.ConceptService#getDrugByMapping(String, org.openmrs.ConceptSource, java.util.Collection
	 */
	Drug getDrugByMapping(String code, ConceptSource conceptSource,
	        Collection<ConceptMapType> withAnyOfTheseTypesOrOrderOfPreference) throws DAOException;
}
