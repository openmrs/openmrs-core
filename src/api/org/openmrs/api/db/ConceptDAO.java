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
import org.openmrs.ConceptDerived;
import org.openmrs.ConceptDescription;
import org.openmrs.ConceptMap;
import org.openmrs.ConceptName;
import org.openmrs.ConceptNameTag;
import org.openmrs.ConceptNumeric;
import org.openmrs.ConceptProposal;
import org.openmrs.ConceptSet;
import org.openmrs.ConceptSetDerived;
import org.openmrs.ConceptSource;
import org.openmrs.ConceptWord;
import org.openmrs.Drug;
import org.openmrs.DrugIngredient;
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
	 */
	public List<Concept> getConcepts(String name, Locale loc, boolean searchOnPhrase, List<ConceptClass> classes,
	                                 List<ConceptDatatype> datatypes) throws DAOException;
	
	/**
	 * @see org.openmrs.api.ConceptService#getConceptWords(String, List, boolean, List, List, List,
	 *      List, Concept, Integer, Integer)
	 */
	public List<ConceptWord> getConceptWords(String phrase, List<Locale> locales, boolean includeRetired,
	                                         List<ConceptClass> requireClasses, List<ConceptClass> excludeClasses,
	                                         List<ConceptDatatype> requireDatatypes, List<ConceptDatatype> excludeDatatypes,
	                                         Concept answersToConcept, Integer start, Integer size) throws DAOException;
	
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
	 * @see org.openmrs.api.ConceptService#getAllConceptDatatypes(boolean)
	 */
	public List<ConceptDatatype> getAllConceptDatatypes(boolean includeRetired) throws DAOException;
	
	/**
	 * @see org.openmrs.api.ConceptService#getConceptDatatypes(java.lang.String)
	 */
	public List<ConceptDatatype> getConceptDatatypes(String name) throws DAOException;
	
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
	 * @see org.openmrs.api.ConceptService#updateConceptSetDerived(org.openmrs.Concept)
	 */
	public void updateConceptSetDerived(Concept concept) throws DAOException;
	
	/**
	 * @see org.openmrs.api.ConceptService#updateConceptSetDerived()
	 */
	public void updateConceptSetDerived() throws DAOException;
	
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
	 * @see org.openmrs.api.ConceptService#updateConceptWord(org.openmrs.Concept)
	 */
	public void updateConceptWord(Concept concept) throws DAOException;
	
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
	 * @see org.openmrs.api.ConceptService#getAllConceptSources()
	 */
	public List<ConceptSource> getAllConceptSources() throws DAOException;
	
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
	 * @see org.openmrs.api.ConceptService#getConceptByMapping(java.lang.String, java.lang.String)
	 */
	public Concept getConceptByMapping(String conceptCode, String mappingCode);
	
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
	
	public ConceptDerived getConceptDerivedByUuid(String uuid);
	
	public ConceptName getConceptNameByUuid(String uuid);
	
	public ConceptSet getConceptSetByUuid(String uuid);
	
	public ConceptSetDerived getConceptSetDerivedByUuid(String uuid);
	
	public ConceptSource getConceptSourceByUuid(String uuid);
	
	public ConceptWord getConceptWordByUuid(String uuid);
	
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
	 * @see org.openmrs.api.ConceptService#getConceptsByConceptSource(org.openmrs.ConceptSource)
	 */
	public List<ConceptMap> getConceptsByConceptSource(ConceptSource conceptSourceName) throws DAOException;
	
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
	
}
