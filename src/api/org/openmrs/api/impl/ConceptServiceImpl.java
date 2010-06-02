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
package org.openmrs.api.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.ConceptAnswer;
import org.openmrs.ConceptClass;
import org.openmrs.ConceptComplex;
import org.openmrs.ConceptDatatype;
import org.openmrs.ConceptDescription;
import org.openmrs.ConceptMap;
import org.openmrs.ConceptName;
import org.openmrs.ConceptNameTag;
import org.openmrs.ConceptNumeric;
import org.openmrs.ConceptProposal;
import org.openmrs.ConceptSet;
import org.openmrs.ConceptSource;
import org.openmrs.ConceptWord;
import org.openmrs.Drug;
import org.openmrs.Obs;
import org.openmrs.api.APIException;
import org.openmrs.api.ConceptInUseException;
import org.openmrs.api.ConceptService;
import org.openmrs.api.ConceptsLockedException;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.ConceptDAO;
import org.openmrs.scheduler.SchedulerException;
import org.openmrs.scheduler.SchedulerService;
import org.openmrs.scheduler.Task;
import org.openmrs.scheduler.TaskDefinition;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.validator.ConceptValidator;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

/**
 * Default Implementation of ConceptService service layer classes
 * 
 * @see org.openmrs.api.ConceptService to access these methods
 */
public class ConceptServiceImpl extends BaseOpenmrsService implements ConceptService {
	
	private final Log log = LogFactory.getLog(getClass());
	
	private ConceptDAO dao;
	
	private static Concept trueConcept;
	
	private static Concept falseConcept;
	
	/*
	 * Name of the concept word update task. A constant, because we only
	 * manage a single task with this name.
	 */
	public static final String CONCEPT_WORD_UPDATE_TASK_NAME = "Update Concept Words";
	
	/**
	 * Task managed by the scheduler to update concept words. May be null.
	 */
	private Task conceptWordUpdateTask;
	
	/**
	 * @see org.openmrs.api.ConceptService#setConceptDAO(org.openmrs.api.db.ConceptDAO)
	 */
	public void setConceptDAO(ConceptDAO dao) {
		this.dao = dao;
	}
	
	/**
	 * @deprecated use {@link #saveConcept(Concept)}
	 */
	@Deprecated
    public void createConcept(Concept concept) {
		Context.getConceptService().saveConcept(concept);
	}
	
	/**
	 * @deprecated use {@link #saveConcept(Concept)}
	 */
	@Deprecated
    public void createConcept(ConceptNumeric concept) {
		Context.getConceptService().saveConcept(concept);
	}
	
	/**
	 * @deprecated use {@link #saveConcept(Concept)}
	 */
	@Deprecated
    public void updateConcept(Concept concept) {
		Context.getConceptService().saveConcept(concept);
	}
	
	/**
	 * @deprecated use {@link #saveConcept(Concept)}
	 */
	@Deprecated
    public void updateConcept(ConceptNumeric concept) {
		Context.getConceptService().saveConcept(concept);
	}
	
	/**
	 * @deprecated use #saveDrug(Drug)
	 */
	@Deprecated
    public void createDrug(Drug drug) {
		Context.getConceptService().saveDrug(drug);
	}
	
	/**
	 * @deprecated Use #saveDrug(Drug)
	 */
	@Deprecated
    public void updateDrug(Drug drug) {
		Context.getConceptService().saveDrug(drug);
	}
	
	/**
	 * @deprecated use #purgeConcept(Concept concept)
	 */
	@Deprecated
    public void deleteConcept(Concept concept) {
		Context.getConceptService().purgeConcept(concept);
	}
	
	/**
	 * @deprecated use {@link #retireConcept(Concept, String)}etireConcept
	 */
	@Deprecated
    public void voidConcept(Concept concept, String reason) {
		Context.getConceptService().retireConcept(concept, reason);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#saveConcept(org.openmrs.Concept)
	 */
	public Concept saveConcept(Concept concept) throws APIException {
		
		// make sure the administrator hasn't turned off concept editing
		checkIfLocked();
		checkIfDatatypeCanBeChanged(concept);
		
		//check that there is no concept already using the preferred concept name in the locale
		Errors errors = new BindException(concept, "concept");
		new ConceptValidator().validate(concept, errors);
		if (errors.hasErrors())
			throw new APIException("Validation errors found");
		
		Concept conceptToReturn = dao.saveConcept(concept);
		
		// add/remove entries in the concept_word table (used for searching)
		this.updateConceptWord(conceptToReturn);
		
		return conceptToReturn;
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#saveDrug(org.openmrs.Drug)
	 */
	public Drug saveDrug(Drug drug) throws APIException {
		checkIfLocked();
		
		return dao.saveDrug(drug);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#purgeConcept(Concept)
	 */
	public void purgeConcept(Concept concept) throws APIException {
		checkIfLocked();
		dao.purgeConcept(concept);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#retireConcept(org.openmrs.Concept, java.lang.String)
	 */
	public Concept retireConcept(Concept concept, String reason) throws APIException {
		
		// only do this if the concept isn't retired already
		if (concept.isRetired() == false) {
			checkIfLocked();
			
			concept.setRetired(true);
			concept.setRetireReason(reason);
			return dao.saveConcept(concept);
		}
		
		return concept;
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#retireDrug(org.openmrs.Drug, java.lang.String)
	 * @throws APIException
	 */
	public Drug retireDrug(Drug drug, String reason) throws APIException {
		return dao.saveDrug(drug);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#unretireDrug(org.openmrs.Drug)
	 */
	public Drug unretireDrug(Drug drug) throws APIException {
		return dao.saveDrug(drug);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#purgeDrug(org.openmrs.Drug)
	 * @throws APIException
	 */
	public void purgeDrug(Drug drug) throws APIException {
		dao.purgeDrug(drug);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getConcept(java.lang.Integer)
	 */
	public Concept getConcept(Integer conceptId) throws APIException {
		return dao.getConcept(conceptId);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getConceptName(java.lang.Integer)
	 */
	public ConceptName getConceptName(Integer conceptNameId) throws APIException {
		return dao.getConceptName(conceptNameId);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getConceptAnswer(java.lang.Integer)
	 */
	public ConceptAnswer getConceptAnswer(Integer conceptAnswerId) throws APIException {
		return dao.getConceptAnswer(conceptAnswerId);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getDrug(java.lang.Integer)
	 */
	public Drug getDrug(Integer drugId) throws APIException {
		return dao.getDrug(drugId);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getConceptNumeric(java.lang.Integer)
	 */
	public ConceptNumeric getConceptNumeric(Integer conceptId) throws APIException {
		return dao.getConceptNumeric(conceptId);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getConceptComplex(java.lang.Integer)
	 */
	public ConceptComplex getConceptComplex(Integer conceptId) {
		return dao.getConceptComplex(conceptId);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getAllConcepts()
	 */
	public List<Concept> getAllConcepts() throws APIException {
		return getAllConcepts(null, true, true);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getAllConcepts(java.lang.String, boolean, boolean)
	 */
	public List<Concept> getAllConcepts(String sortBy, boolean asc, boolean includeRetired) throws APIException {
		if (sortBy == null)
			sortBy = "conceptId";
		
		return dao.getAllConcepts(sortBy, asc, includeRetired);
	}
	
	/**
	 * @deprecated use {@link #getAllConcepts(String, boolean, boolean)}
	 */
	@Deprecated
    public List<Concept> getConcepts(String sortBy, String dir) throws APIException {
		boolean asc = true ? dir.equals("asc") : !dir.equals("asc");
		return getAllConcepts(sortBy, asc, true);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getConceptsByName(java.lang.String)
	 */
	public List<Concept> getConceptsByName(String name) throws APIException {
		return getConcepts(name, Context.getLocale(), true, null, null);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getConceptByName(java.lang.String)
	 */
	public Concept getConceptByName(String name) {
		if (name == null)
			return null;
		
		List<Concept> concepts = getConcepts(name, Context.getLocale(), false, null, null);
		int size = concepts.size();
		if (size > 0) {
			if (size > 1) {
				log.warn("Multiple concepts found for '" + name + "'");
				for (Concept c : concepts) {
					if (c.getName(Context.getLocale()).getName().compareTo(name) == 0)
						return c;
				}
			}
			return concepts.get(0);
		}
		return null;
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getConceptByIdOrName(java.lang.String)
	 * @deprecated use {@link #getConcept(String)}
	 */
	@Deprecated
    public Concept getConceptByIdOrName(String idOrName) {
		return getConcept(idOrName);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getConcept(java.lang.String)
	 */
	public Concept getConcept(String conceptIdOrName) {
		Concept c = null;
		Integer conceptId = null;
		try {
			conceptId = new Integer(conceptIdOrName);
		}
		catch (NumberFormatException nfe) {
			conceptId = null;
		}
		
		if (conceptId != null) {
			c = getConcept(conceptId);
		} else {
			c = getConceptByName(conceptIdOrName);
		}
		return c;
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getConceptWords(String, List, boolean, List, List, List,
	 *      List, Concept, Integer, Integer)
	 */
	public List<ConceptWord> getConceptWords(String phrase, List<Locale> locales, boolean includeRetired,
	                                         List<ConceptClass> requireClasses, List<ConceptClass> excludeClasses,
	                                         List<ConceptDatatype> requireDatatypes, List<ConceptDatatype> excludeDatatypes,
	                                         Concept answerToConcept, Integer start, Integer size) {
		
		if (requireClasses == null)
			requireClasses = new Vector<ConceptClass>();
		if (excludeClasses == null)
			excludeClasses = new Vector<ConceptClass>();
		if (requireDatatypes == null)
			requireDatatypes = new Vector<ConceptDatatype>();
		if (excludeDatatypes == null)
			excludeDatatypes = new Vector<ConceptDatatype>();
		
		List<ConceptWord> conceptWords = dao.getConceptWords(phrase, locales, includeRetired, requireClasses,
		    excludeClasses, requireDatatypes, excludeDatatypes, answerToConcept, start, size);
		
		return weightWords(phrase, locales, conceptWords);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getConceptWords(java.lang.String, java.util.Locale)
	 */
	public List<ConceptWord> getConceptWords(String phrase, Locale locale) throws APIException {
		List<Locale> locales = new Vector<Locale>();
		locales.add(locale);
		
		return getConceptWords(phrase, locales, false, null, null, null, null, null, null, null);
	}
	
	/**
	 * @deprecated use
	 *             {@link #getConceptWords(String, List, boolean, List, List, List, List, Concept, Integer, Integer)}
	 */
	@Deprecated
    public List<ConceptWord> findConcepts(String phrase, Locale locale, boolean includeRetired, int start, int size) {
		List<Locale> locales = new Vector<Locale>();
		locales.add(locale);
		
		// delegate to the non-deprecated method
		List<ConceptWord> conceptWords = getConceptWords(phrase, locales, includeRetired, null, null, null, null, null,
		    start, size);
		
		List<ConceptWord> subList = conceptWords.subList(start, start + size);
		
		return subList;
	}
	
	/**
	 * @deprecated use
	 *             {@link #getConceptWords(String, List, boolean, List, List, List, List, Concept, Integer, Integer)}
	 */
	@Deprecated
    public List<ConceptWord> findConcepts(String phrase, Locale locale, boolean includeRetired) {
		
		List<Locale> locales = new Vector<Locale>();
		locales.add(locale);
		
		return getConceptWords(phrase, locales, includeRetired, null, null, null, null, null, null, null);
	}
	
	/**
	 * @deprecated use
	 *             {@link #getConceptWords(String, List, boolean, List, List, List, List, Concept, Integer, Integer)}
	 */
	@Deprecated
    public List<ConceptWord> findConcepts(String phrase, Locale locale, boolean includeRetired,
	                                      List<ConceptClass> requireClasses, List<ConceptClass> excludeClasses,
	                                      List<ConceptDatatype> requireDatatypes, List<ConceptDatatype> excludeDatatypes) {
		
		List<Locale> locales = new Vector<Locale>();
		locales.add(locale);
		
		return getConceptWords(phrase, locales, includeRetired, requireClasses, excludeClasses, requireDatatypes,
		    excludeDatatypes, null, null, null);
	}
	
	/**
	 * @deprecated use
	 *             {@link #getConceptWords(String, List, boolean, List, List, List, List, Concept, Integer, Integer)}
	 */
	@Deprecated
    public List<ConceptWord> findConcepts(String phrase, List<Locale> locales, boolean includeRetired,
	                                      List<ConceptClass> requireClasses, List<ConceptClass> excludeClasses,
	                                      List<ConceptDatatype> requireDatatypes, List<ConceptDatatype> excludeDatatypes) {
		
		return getConceptWords(phrase, locales, includeRetired, requireClasses, excludeClasses, requireDatatypes,
		    excludeDatatypes, null, null, null);
	}
	
	/**
	 * Generic getConcepts method (used internally) to get concepts matching a on name
	 * 
	 * @param name
	 * @param loc
	 * @param searchOnPhrase
	 * @return
	 */
	private List<Concept> getConcepts(String name, Locale loc, boolean searchOnPhrase, List<ConceptClass> classes,
	                                  List<ConceptDatatype> datatypes) {
		if (classes == null)
			classes = new Vector<ConceptClass>();
		if (datatypes == null)
			datatypes = new Vector<ConceptDatatype>();
		
		return dao.getConcepts(name, loc, searchOnPhrase, classes, datatypes);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getDrug(java.lang.String)
	 */
	public Drug getDrug(String drugNameOrId) {
		Integer drugId = null;
		
		try {
			drugId = new Integer(drugNameOrId);
		}
		catch (NumberFormatException nfe) {
			drugId = null;
		}
		
		if (drugId != null) {
			return getDrug(drugId);
		} else {
			List<Drug> drugs = new ArrayList<Drug>();
			drugs = dao.getDrugs(drugNameOrId, null, false);
			if (drugs.size() > 1)
				log.warn("more than one drug name returned with name:" + drugNameOrId);
			if (drugs.size() == 0)
				return null;
			return (Drug) drugs.get(0);
		}
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getDrugByNameOrId(java.lang.String)
	 * @deprecated use {@link #getDrug(String)}
	 */
	@Deprecated
    public Drug getDrugByNameOrId(String drugNameOrId) {
		return getDrug(drugNameOrId);
	}
	
	/**
	 * @deprecated use {@link #getAllDrugs()}
	 */
	@Deprecated
    public List<Drug> getDrugs() {
		return getAllDrugs();
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getAllDrugs()
	 */
	public List<Drug> getAllDrugs() {
		return getAllDrugs(true);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getAllDrugs(boolean)
	 */
	public List<Drug> getAllDrugs(boolean includeRetired) {
		return dao.getDrugs(null, null, includeRetired);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getDrugs(org.openmrs.Concept)
	 * @deprecated use {@link #getDrugsByConcept(Concept)}
	 */
	@Deprecated
    public List<Drug> getDrugs(Concept concept) {
		return getDrugsByConcept(concept);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getDrugsByConcept(org.openmrs.Concept)
	 */
	public List<Drug> getDrugsByConcept(Concept concept) {
		return dao.getDrugs(null, concept, false);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getDrugs(Concept)
	 * @deprecated Use {@link #getDrugsByConcept(Concept)}
	 */
	@Deprecated
    public List<Drug> getDrugs(Concept concept, boolean includeRetired) {
		if (includeRetired == true)
			throw new APIException("Getting retired drugs is no longer an options.  Use the getAllDrugs() method for that");
		
		return getDrugsByConcept(concept);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getAllDrugs(boolean)
	 * @deprecated Use {@link #getAllDrugs(boolean)}
	 */
	@Deprecated
    public List<Drug> getDrugs(boolean includeVoided) {
		return getAllDrugs(includeVoided);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#findDrugs(java.lang.String, boolean)
	 * @deprecated Use {@link #getDrugs(String)}
	 */
	@Deprecated
    public List<Drug> findDrugs(String phrase, boolean includeRetired) {
		if (includeRetired == true)
			throw new APIException("Getting retired drugs is no longer an options.  Use the getAllDrugs() method for that");
		
		return getDrugs(phrase);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getDrugs(java.lang.String)
	 */
	public List<Drug> getDrugs(String phrase) {
		return dao.getDrugs(phrase);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getConceptsByClass(org.openmrs.ConceptClass)
	 */
	public List<Concept> getConceptsByClass(ConceptClass cc) {
		List<ConceptClass> classes = new Vector<ConceptClass>();
		classes.add(cc);
		
		return getConcepts(null, null, false, classes, null);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getConceptClasses()
	 * @deprecated
	 */
	@Deprecated
    public List<ConceptClass> getConceptClasses() {
		return getAllConceptClasses(true);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getAllConceptClasses(boolean)
	 */
	public List<ConceptClass> getAllConceptClasses(boolean includeRetired) {
		return dao.getAllConceptClasses(includeRetired);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getConceptClass(java.lang.Integer)
	 */
	public ConceptClass getConceptClass(Integer i) {
		return dao.getConceptClass(i);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getConceptClassByName(java.lang.String)
	 */
	public ConceptClass getConceptClassByName(String name) {
		List<ConceptClass> ccList = dao.getConceptClasses(name);
		if (ccList.size() > 1)
			log.warn("More than one ConceptClass found with name: " + name);
		if (ccList.size() == 1)
			return ccList.get(0);
		return null;
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getAllConceptClasses(boolean)
	 */
	public List<ConceptClass> getAllConceptClasses() throws APIException {
		return getAllConceptClasses(true);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#saveConceptClass(org.openmrs.ConceptClass)
	 */
	public ConceptClass saveConceptClass(ConceptClass cc) throws APIException {
		return dao.saveConceptClass(cc);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#purgeConceptClass(org.openmrs.ConceptClass)
	 */
	public void purgeConceptClass(ConceptClass cc) {
		dao.purgeConceptClass(cc);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#purgeConceptDatatype(org.openmrs.ConceptDatatype)
	 */
	public void purgeConceptDatatype(ConceptDatatype cd) throws APIException {
		dao.purgeConceptDatatype(cd);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#saveConceptDatatype(org.openmrs.ConceptDatatype)
	 */
	public ConceptDatatype saveConceptDatatype(ConceptDatatype cd) throws APIException {
		return dao.saveConceptDatatype(cd);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getAllConceptDatatypes()
	 * @deprecated use {@link #getAllConceptDatatypes()}
	 */
	@Deprecated
    public List<ConceptDatatype> getConceptDatatypes() {
		return getAllConceptDatatypes();
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getAllConceptDatatypes()
	 */
	public List<ConceptDatatype> getAllConceptDatatypes() {
		return getAllConceptDatatypes(true);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getAllConceptDatatypes(boolean)
	 */
	public List<ConceptDatatype> getAllConceptDatatypes(boolean includeRetired) throws APIException {
		return dao.getAllConceptDatatypes(includeRetired);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getConceptDatatype(java.lang.Integer)
	 */
	public ConceptDatatype getConceptDatatype(Integer i) {
		return dao.getConceptDatatype(i);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getConceptDatatypes(java.lang.String)
	 */
	public List<ConceptDatatype> getConceptDatatypes(String name) {
		return dao.getConceptDatatypes(name);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getConceptDatatypeByName(java.lang.String)
	 */
	public ConceptDatatype getConceptDatatypeByName(String name) {
		return dao.getConceptDatatypeByName(name);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#updateConceptSetDerived(org.openmrs.Concept)
	 */
	public void updateConceptSetDerived(Concept concept) throws APIException {
		checkIfLocked();
		dao.updateConceptSetDerived(concept);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#updateConceptSetDerived()
	 */
	public void updateConceptSetDerived() throws APIException {
		checkIfLocked();
		dao.updateConceptSetDerived();
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getConceptSets(org.openmrs.Concept)
	 * @deprecated use {@link #getConceptSetsByConcept(Concept)}
	 */
	@Deprecated
    public List<ConceptSet> getConceptSets(Concept c) {
		return getConceptSetsByConcept(c);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getConceptSetsByConcept(org.openmrs.Concept)
	 */
	public List<ConceptSet> getConceptSetsByConcept(Concept concept) throws APIException {
		return dao.getConceptSetsByConcept(concept);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getConceptsInSet(org.openmrs.Concept)
	 * @deprecated use {@link #getConceptsByConceptSet(Concept)}
	 */
	@Deprecated
    public List<Concept> getConceptsInSet(Concept c) {
		return getConceptsByConceptSet(c);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getConceptsInSet(org.openmrs.Concept)
	 */
	public List<Concept> getConceptsByConceptSet(Concept c) {
		Set<Integer> alreadySeen = new HashSet<Integer>();
		List<Concept> ret = new ArrayList<Concept>();
		explodeConceptSetHelper(c, ret, alreadySeen);
		return ret;
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getSetsContainingConcept(org.openmrs.Concept)
	 */
	public List<ConceptSet> getSetsContainingConcept(Concept concept) {
		if (concept.getConceptId() == null)
			return Collections.emptyList();
		
		return dao.getSetsContainingConcept(concept);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getConceptProposal(java.lang.Integer)
	 */
	public ConceptProposal getConceptProposal(Integer conceptProposalId) {
		return dao.getConceptProposal(conceptProposalId);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getConceptProposals(boolean)
	 * @deprecated use {@link #getAllConceptProposals(boolean)}
	 */
	@Deprecated
    public List<ConceptProposal> getConceptProposals(boolean includeCompleted) {
		return getAllConceptProposals(includeCompleted);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getAllConceptProposals(boolean)
	 */
	public List<ConceptProposal> getAllConceptProposals(boolean includeCompleted) {
		return dao.getAllConceptProposals(includeCompleted);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getConceptProposals(java.lang.String)
	 */
	public List<ConceptProposal> getConceptProposals(String cp) {
		return dao.getConceptProposals(cp);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#findProposedConcepts(java.lang.String)
	 * @deprecated
	 */
	@Deprecated
    public List<Concept> findProposedConcepts(String text) {
		return getProposedConcepts(text);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getProposedConcepts(java.lang.String)
	 */
	public List<Concept> getProposedConcepts(String text) {
		return dao.getProposedConcepts(text);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#proposeConcept(org.openmrs.ConceptProposal)
	 * @deprecated
	 */
	@Deprecated
    public void proposeConcept(ConceptProposal conceptProposal) {
		saveConceptProposal(conceptProposal);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#saveConceptProposal(org.openmrs.ConceptProposal)
	 */
	public ConceptProposal saveConceptProposal(ConceptProposal conceptProposal) throws APIException {
		return dao.saveConceptProposal(conceptProposal);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#purgeConceptProposal(org.openmrs.ConceptProposal)
	 */
	public void purgeConceptProposal(ConceptProposal cp) throws APIException {
		dao.purgeConceptProposal(cp);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#mapConceptProposalToConcept(org.openmrs.ConceptProposal,
	 *      org.openmrs.Concept)
	 */
	public Concept mapConceptProposalToConcept(ConceptProposal cp, Concept mappedConcept) throws APIException {
		
		if (cp.getState().equals(OpenmrsConstants.CONCEPT_PROPOSAL_REJECT)) {
			cp.rejectConceptProposal();
			saveConceptProposal(cp);
		}
		
		if (mappedConcept == null)
			throw new APIException("Illegal Mapped Concept");
		
		if (cp.getState().equals(OpenmrsConstants.CONCEPT_PROPOSAL_CONCEPT) || !StringUtils.hasText(cp.getFinalText())) {
			cp.setState(OpenmrsConstants.CONCEPT_PROPOSAL_CONCEPT);
			cp.setFinalText("");
		} else if (cp.getState().equals(OpenmrsConstants.CONCEPT_PROPOSAL_SYNONYM)) {
			
			checkIfLocked();
			
			String finalText = cp.getFinalText();
			ConceptName conceptName = new ConceptName(finalText, null);
			conceptName.setConcept(mappedConcept);
			
			conceptName.setDateCreated(new Date());
			conceptName.setCreator(Context.getAuthenticatedUser());
			
			mappedConcept.addName(conceptName);
			mappedConcept.setChangedBy(Context.getAuthenticatedUser());
			mappedConcept.setDateChanged(new Date());
			updateConceptWord(mappedConcept);
		}
		
		cp.setMappedConcept(mappedConcept);
		
		if (cp.getObsConcept() != null) {
			Obs ob = new Obs();
			ob.setEncounter(cp.getEncounter());
			ob.setConcept(cp.getObsConcept());
			ob.setValueCoded(cp.getMappedConcept());
			ob.setCreator(Context.getAuthenticatedUser());
			ob.setDateCreated(new Date());
			ob.setObsDatetime(cp.getEncounter().getEncounterDatetime());
			ob.setLocation(cp.getEncounter().getLocation());
			ob.setPerson(cp.getEncounter().getPatient());
			cp.setObs(ob);
		}
		
		return mappedConcept;
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#rejectConceptProposal(org.openmrs.ConceptProposal)
	 * @deprecated use {@link ConceptProposal#rejectConceptProposal()}
	 */
	@Deprecated
    public void rejectConceptProposal(ConceptProposal cp) {
		cp.rejectConceptProposal();
		saveConceptProposal(cp);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#findMatchingConceptProposals(String text)
	 * @deprecated use {@link #getConceptProposals(String)}
	 */
	@Deprecated
    public List<ConceptProposal> findMatchingConceptProposals(String text) {
		return getConceptProposals(text);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#findConceptAnswers(String phrase, Locale locale,Concept
	 *      concept, boolean includeRetired)
	 * @deprecated use {@link #getConceptAnswers(String, Locale, Concept)}
	 */
	@Deprecated
    public List<ConceptWord> findConceptAnswers(String phrase, Locale locale, Concept concept, boolean includeRetired) {
		
		return getConceptAnswers(phrase, locale, concept);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getConceptAnswers(java.lang.String, java.util.Locale,
	 *      org.openmrs.Concept)
	 */
	public List<ConceptWord> getConceptAnswers(String phrase, Locale locale, Concept concept) throws APIException {
		
		List<Locale> locales = new Vector<Locale>();
		locales.add(locale);
		
		List<ConceptWord> conceptWords = getConceptWords(phrase, locales, false, null, null, null, null, concept, null, null);
		
		return weightWords(phrase, locales, conceptWords);
	}
	
	/**
	 * @deprecated use {@link #getConceptsByAnswer(Concept)}
	 * @see org.openmrs.api.ConceptService#getQuestionsForAnswer(org.openmrs.Concept)
	 */
	@Deprecated
    public List<Concept> getQuestionsForAnswer(Concept concept) {
		return getConceptsByAnswer(concept);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getConceptsByAnswer(org.openmrs.Concept)
	 */
	public List<Concept> getConceptsByAnswer(Concept concept) throws APIException {
		if (concept.getConceptId() == null)
			return Collections.emptyList();
		
		return dao.getConceptsByAnswer(concept);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getPrevConcept(org.openmrs.Concept)
	 */
	public Concept getPrevConcept(Concept c) {
		return dao.getPrevConcept(c);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getNextConcept(org.openmrs.Concept)
	 */
	public Concept getNextConcept(Concept c) {
		return dao.getNextConcept(c);
	}
	
	/**
	 * Convenience method
	 * 
	 * @param parent
	 * @param subList
	 * @return
	 */
	private Boolean containsAll(Collection<String> parent, Collection<String> subList) {
		
		for (String s : subList) {
			s = s.toUpperCase();
			boolean found = false;
			for (String p : parent) {
				p = p.toUpperCase();
				if (p.startsWith(s))
					found = true;
			}
			if (!found)
				return false;
		}
		return true;
	}
	
	/**
	 * Convenience method
	 * 
	 * @param searchedWords
	 * @param matchedString
	 * @return
	 */
	private double getPercentMatched(Collection<String> searchedWords, String matchedString) {
		
		List<String> subList = ConceptWord.getUniqueWords(matchedString);
		double size = ConceptWord.splitPhrase(matchedString).length; // total
		// # of
		// words
		
		double matches = 0.0;
		for (String s : subList) {
			s = s.toUpperCase();
			for (String p : searchedWords) {
				p = p.toUpperCase();
				if (p.startsWith(s))
					matches += 1.0;
			}
		}
		
		return matches == 0 ? 0 : (matches / size);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#checkIfLocked()
	 */
	public void checkIfLocked() throws ConceptsLockedException {
		String locked = Context.getAdministrationService().getGlobalProperty(
		    OpenmrsConstants.GLOBAL_PROPERTY_CONCEPTS_LOCKED, "false");
		if (locked.toLowerCase().equals("true"))
			throw new ConceptsLockedException();
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getConceptsWithDrugsInFormulary()
	 */
	public List<Concept> getConceptsWithDrugsInFormulary() {
		return dao.getConceptsWithDrugsInFormulary();
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#updateConceptWords()
	 */
	public void updateConceptWords() throws APIException {
		checkIfLocked();
		
		SchedulerService ss = Context.getSchedulerService();
		
		// ABKTODO: this whole pattern should be moved into the scheduler,
		// providing a call like scheduleThisIfNotRunning()
		TaskDefinition conceptWordUpdateTaskDef = ss.getTaskByName(CONCEPT_WORD_UPDATE_TASK_NAME);
		if (conceptWordUpdateTaskDef == null) {
			conceptWordUpdateTaskDef = createConceptWordUpdateTask();
			try {
				ss.saveTask(conceptWordUpdateTaskDef);
				conceptWordUpdateTask = ss.scheduleTask(conceptWordUpdateTaskDef);
			}
			catch (SchedulerException e) {
				log.error("Failed to schedule concept-word update task, because:", e);
			}
		} else {
			// task definition exists. get the task itself
			conceptWordUpdateTask = conceptWordUpdateTaskDef.getTaskInstance();
			if (conceptWordUpdateTask == null) {
				try {
					ss.rescheduleTask(conceptWordUpdateTaskDef);
				}
				catch (SchedulerException e) {
					log.error("Failed to schedule concept-word update task, because:", e);
				}
			} else if (!conceptWordUpdateTask.isExecuting()) {
				try {
					ss.rescheduleTask(conceptWordUpdateTaskDef);
				}
				catch (SchedulerException e) {
					log.error("Failed to re-schedule concept-word update task, because:", e);
				}
			}
		}
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#updateConceptWord(org.openmrs.Concept)
	 */
	public void updateConceptWord(Concept concept) throws APIException {
		checkIfLocked();
		dao.updateConceptWord(concept);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#updateConceptWords(java.lang.Integer, java.lang.Integer)
	 */
	public void updateConceptWords(Integer conceptIdStart, Integer conceptIdEnd) throws APIException {
		checkIfLocked();
		Integer i = conceptIdStart;
		ConceptService cs = Context.getConceptService();
		while (i++ <= conceptIdEnd) {
			updateConceptWord(cs.getConcept(i));
		}
	}
	
	/**
	 * @see ConceptService#getMaxConceptId()
	 */
	public Integer getMaxConceptId() {
		return dao.getMaxConceptId();
	}
	
	/**
	 * This will weight and sort the concepts according to how many of the words in the name match
	 * the words in the search phrase.
	 * 
	 * @param phrase that was used to get this search
	 * @param locales List<Locale> that were used to get this search
	 * @param conceptWords the words that were found via a db search and now must be weighted before
	 *            being shown to the user
	 * @return List<ConceptWord> object containing sorted <code>ConceptWord</code>s
	 * @should not fail with null phrase
	 * @should weight preferred names in country higher than other preferred names
	 * @should weight preferred names in language higher than just preferred names
	 * @should weight preferred names higher than other names
	 * @should weight names that contain all words in search phrase higher than names that dont
	 * @should weight better matches higher than lower matches
	 */
	protected List<ConceptWord> weightWords(String phrase, List<Locale> locales, List<ConceptWord> conceptWords) {
		
		// Map<ConceptId, ConceptWord>
		Map<Integer, ConceptWord> uniqueConcepts = new HashMap<Integer, ConceptWord>();
		
		// phrase words
		if (phrase == null)
			phrase = "";
		List<String> searchedWords = ConceptWord.getUniqueWords(phrase);
		
		Integer conceptId = null;
		Concept concept = null;
		ConceptName conceptName = null;
		
		for (ConceptWord currentWord : conceptWords) {
			concept = currentWord.getConcept();
			conceptId = concept.getConceptId();
			conceptName = currentWord.getConceptName();
			
			// check each locale the user is searching in for name preference
			for (Locale locale : locales) {
				// We weight matches on preferred names higher
				if (conceptName.isPreferredInCountry(locale.getCountry()))
					currentWord.increaseWeight(5.0);
				else if (conceptName.isPreferredInLanguage(locale.getLanguage()))
					currentWord.increaseWeight(3.0);
				else if (conceptName.isPreferred())
					currentWord.increaseWeight(1.0);
			}
			
			// increase the weight by a factor of the % of words matched
			Double percentMatched = getPercentMatched(searchedWords, conceptName.getName());
			currentWord.increaseWeight(5.0 * percentMatched);
			
			List<String> nameWords = ConceptWord.getUniqueWords(conceptName.getName());
			
			// if the conceptName doesn't contain all of the search words, lower the weighting
			if (!containsAll(nameWords, searchedWords)) {
				currentWord.increaseWeight(-2.0);
			}
			
			log.debug("Weight for: " + conceptName.getName() + " is: " + currentWord.getWeight());
			
			if (uniqueConcepts.containsKey(conceptId)) {
				// if we've seen another name for this concept already, check the name weightings
				ConceptWord previousWord = uniqueConcepts.get(conceptId);
				
				if (currentWord.getWeight() > previousWord.getWeight()) {
					uniqueConcepts.put(conceptId, currentWord);
				}
			} else {
				// its not in the list, add it
				uniqueConcepts.put(conceptId, currentWord);
			}
			
		}
		
		conceptWords = new Vector<ConceptWord>();
		conceptWords.addAll(uniqueConcepts.values());
		Collections.sort(conceptWords);
		
		return conceptWords;
	}
	
	/**
	 * Utility method used by getConceptsInSet(Concept concept)
	 * 
	 * @param concept
	 * @param ret
	 * @param alreadySeen
	 */
	private void explodeConceptSetHelper(Concept concept, Collection<Concept> ret, Collection<Integer> alreadySeen) {
		if (alreadySeen.contains(concept.getConceptId()))
			return;
		alreadySeen.add(concept.getConceptId());
		List<ConceptSet> cs = getConceptSets(concept);
		for (ConceptSet set : cs) {
			Concept c = set.getConcept();
			if (c.isSet()) {
				explodeConceptSetHelper(c, ret, alreadySeen);
			} else {
				ret.add(c);
			}
		}
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getConceptNameTagByName(java.lang.String)
	 */
	public ConceptNameTag getConceptNameTagByName(String tagName) {
		return dao.getConceptNameTagByName(tagName);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getLocalesOfConceptNames()
	 */
	public Set<Locale> getLocalesOfConceptNames() {
		return dao.getLocalesOfConceptNames();
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getConceptSource(java.lang.Integer)
	 */
	public ConceptSource getConceptSource(Integer conceptSourceId) {
		return dao.getConceptSource(conceptSourceId);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getAllConceptSources()
	 */
	public List<ConceptSource> getAllConceptSources() {
		return dao.getAllConceptSources();
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#purgeConceptSource(org.openmrs.ConceptSource)
	 */
	public ConceptSource purgeConceptSource(ConceptSource cs) throws APIException {
		
		return dao.deleteConceptSource(cs);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#retireConceptSource(org.openmrs.ConceptSource,String)
	 */
	public ConceptSource retireConceptSource(ConceptSource cs, String reason) throws APIException {
		// retireReason is automatically set in BaseRetireHandler
		return dao.saveConceptSource(cs);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#saveConceptSource(org.openmrs.ConceptSource)
	 */
	public ConceptSource saveConceptSource(ConceptSource conceptSource) throws APIException {
		return dao.saveConceptSource(conceptSource);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#saveConceptNameTag(org.openmrs.ConceptNameTag)
	 */
	public ConceptNameTag saveConceptNameTag(ConceptNameTag nameTag) {
		checkIfLocked();
		
		return dao.saveConceptNameTag(nameTag);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#conceptIterator()
	 */
	public Iterator<Concept> conceptIterator() {
		return dao.conceptIterator();
	}
	
	private TaskDefinition createConceptWordUpdateTask() {
		TaskDefinition conceptWordUpdateTaskDef = new TaskDefinition();
		conceptWordUpdateTaskDef.setTaskClass("org.openmrs.scheduler.tasks.ConceptWordUpdateTask");
		conceptWordUpdateTaskDef.setRepeatInterval(0L); // zero interval means do not repeat
		conceptWordUpdateTaskDef.setStartOnStartup(false);
		conceptWordUpdateTaskDef.setStartTime(null); // to induce immediate execution
		conceptWordUpdateTaskDef.setName(CONCEPT_WORD_UPDATE_TASK_NAME);
		conceptWordUpdateTaskDef
		        .setDescription("Iterates through the concept dictionary, re-creating concept words (which are used for searcing). This task is started when using the \"Update Concept Word Storage\" page and no range is given.  This task stops itself when one iteration has completed.");
		return conceptWordUpdateTaskDef;
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getConceptByUuid(java.lang.String)
	 */
	public Concept getConceptByUuid(String uuid) {
		return dao.getConceptByUuid(uuid);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getConceptClassByUuid(java.lang.String)
	 */
	public ConceptClass getConceptClassByUuid(String uuid) {
		return dao.getConceptClassByUuid(uuid);
	}
	
	public ConceptAnswer getConceptAnswerByUuid(String uuid) {
		return dao.getConceptAnswerByUuid(uuid);
	}
	
	public ConceptName getConceptNameByUuid(String uuid) {
		return dao.getConceptNameByUuid(uuid);
	}
	
	public ConceptSet getConceptSetByUuid(String uuid) {
		return dao.getConceptSetByUuid(uuid);
	}
	
	public ConceptSource getConceptSourceByUuid(String uuid) {
		return dao.getConceptSourceByUuid(uuid);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getConceptDatatypeByUuid(java.lang.String)
	 */
	public ConceptDatatype getConceptDatatypeByUuid(String uuid) {
		return dao.getConceptDatatypeByUuid(uuid);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getConceptNumericByUuid(java.lang.String)
	 */
	public ConceptNumeric getConceptNumericByUuid(String uuid) {
		return dao.getConceptNumericByUuid(uuid);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getConceptProposalByUuid(java.lang.String)
	 */
	public ConceptProposal getConceptProposalByUuid(String uuid) {
		return dao.getConceptProposalByUuid(uuid);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getDrugByUuid(java.lang.String)
	 */
	public Drug getDrugByUuid(String uuid) {
		return dao.getDrugByUuid(uuid);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getConceptDescriptionByUuid(java.lang.String)
	 */
	public ConceptDescription getConceptDescriptionByUuid(String uuid) {
		return dao.getConceptDescriptionByUuid(uuid);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getConceptNameTagByUuid(java.lang.String)
	 */
	public ConceptNameTag getConceptNameTagByUuid(String uuid) {
		return dao.getConceptNameTagByUuid(uuid);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getAllConceptNameTags()
	 */
	public List<ConceptNameTag> getAllConceptNameTags() {
		return dao.getAllConceptNameTags();
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getConceptNameTag(java.lang.Integer)
	 */
	public ConceptNameTag getConceptNameTag(Integer id) {
		return dao.getConceptNameTag(id);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getConceptByMapping(java.lang.String, java.lang.String)
	 */
	public Concept getConceptByMapping(String conceptCode, String mappingCode) throws APIException {
		return dao.getConceptByMapping(conceptCode, mappingCode);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getFalseConcept()
	 */
	@Override
	public Concept getFalseConcept() {
		if (falseConcept == null)
			setBooleanConcepts();
		
		return falseConcept;
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getTrueConcept()
	 */
	@Override
	public Concept getTrueConcept() {
		if (trueConcept == null)
			setBooleanConcepts();
		
		return trueConcept;
	}
	
	/**
	 * Sets the TRUE and FALSE concepts by reading their ids from the global_property table
	 */
	private void setBooleanConcepts() {
		
		try {
			trueConcept = new Concept(Integer.parseInt(Context.getAdministrationService().getGlobalProperty(
			    OpenmrsConstants.GLOBAL_PROPERTY_TRUE_CONCEPT)));
			falseConcept = new Concept(Integer.parseInt(Context.getAdministrationService().getGlobalProperty(
			    OpenmrsConstants.GLOBAL_PROPERTY_FALSE_CONCEPT)));
		}
		catch (NumberFormatException e) {
			log.warn("Concept ids for boolean concepts should be numbers");
			return;
		}
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getConceptsByConceptSource(org.openmrs.ConceptSource)
	 */
	public List<ConceptMap> getConceptsByConceptSource(ConceptSource conceptSource) throws APIException {
		List<ConceptMap> ret = dao.getConceptsByConceptSource(conceptSource);
		if (ret != null)
			return ret;
		else
			return new ArrayList<ConceptMap>();
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getConceptSourceByName(java.lang.String)
	 */
	public ConceptSource getConceptSourceByName(String conceptSourceName) throws APIException {
		return dao.getConceptSourceByName(conceptSourceName);
	}
	
	/**
	 * Utility method to check if the concept is already attached to an observation (including
	 * voided ones) and if the datatype of the concept has changed, an exception indicating that the
	 * datatype cannot be modified will be reported if the concept is attached to an observation.
	 * This method will only allow changing boolean concepts to coded.
	 * 
	 * @param concept
	 * @throws ConceptsDataTypeCannotBeModifiedException
	 */
	private void checkIfDatatypeCanBeChanged(Concept concept) {
		if (concept.getId() != null) {
			if (hasAnyObservation(concept) && hasDatatypeChanged(concept)) {
				//allow boolean concepts to be converted to coded
				if (!(dao.getSavedConceptDatatype(concept).isBoolean() && concept.getDatatype().isCoded()))
					throw new ConceptInUseException();
				if (log.isDebugEnabled())
					log.debug("Converting datatype of concept with id " + concept.getConceptId() + " from Boolean to Coded");
			}
		}
	}
	
	/**
	 * Utility method which loads the previous version of a concept to check if the datatype has
	 * changed.
	 * 
	 * @param concept to be modified
	 * @return boolean indicating change in the datatype
	 */
	private boolean hasDatatypeChanged(Concept concept) {
		ConceptDatatype oldConceptDatatype = dao.getSavedConceptDatatype(concept);
		return !oldConceptDatatype.equals(concept.getDatatype());
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#hasAnyObservation(org.openmrs.Concept)
	 */
	public boolean hasAnyObservation(Concept concept) {
		List<Concept> concepts = new Vector<Concept>();
		concepts.add(concept);
		Integer count = Context.getObsService().getObservationCount(null, null, concepts, null, null, null, null, null,
		    null, true);
		return count > 0;
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#convertBooleanConceptToCoded(org.openmrs.Concept)
	 */
	@Override
	public void convertBooleanConceptToCoded(Concept conceptToChange) throws APIException {
		if (conceptToChange != null) {
			if (!conceptToChange.getDatatype().isBoolean())
				throw new APIException("Invalid datatype of the concept to convert, should be Boolean");
			
			conceptToChange.setDatatype(getConceptDatatypeByName("Coded"));
			conceptToChange.addAnswer(new ConceptAnswer(getTrueConcept()));
			conceptToChange.addAnswer(new ConceptAnswer(getFalseConcept()));
			Context.getConceptService().saveConcept(conceptToChange);
		}
	}
	
}
