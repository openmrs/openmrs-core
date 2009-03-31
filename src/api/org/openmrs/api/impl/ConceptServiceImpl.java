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
import org.openmrs.User;
import org.openmrs.api.APIException;
import org.openmrs.api.ConceptService;
import org.openmrs.api.ConceptsLockedException;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.ConceptDAO;
import org.openmrs.scheduler.SchedulerException;
import org.openmrs.scheduler.SchedulerService;
import org.openmrs.scheduler.Task;
import org.openmrs.scheduler.TaskDefinition;
import org.openmrs.util.OpenmrsConstants;
import org.springframework.util.StringUtils;

/**
 * Default Implementation of ConceptService service layer classes
 * 
 * @see org.openmrs.api.ConceptService to access these methods
 */
public class ConceptServiceImpl extends BaseOpenmrsService implements ConceptService {
	
	private final Log log = LogFactory.getLog(getClass());
	
	private ConceptDAO dao;
	
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
	public void createConcept(Concept concept) {
		saveConcept(concept);
	}
	
	/**
	 * @deprecated use {@link #saveConcept(Concept)}
	 */
	public void createConcept(ConceptNumeric concept) {
		saveConcept(concept);
	}
	
	/**
	 * @deprecated use {@link #saveConcept(Concept)}
	 */
	public void updateConcept(Concept concept) {
		saveConcept(concept);
	}
	
	/**
	 * @deprecated use {@link #saveConcept(Concept)}
	 */
	public void updateConcept(ConceptNumeric concept) {
		saveConcept(concept);
	}
	
	/**
	 * @deprecated use #saveDrug(Drug)
	 */
	public void createDrug(Drug drug) {
		saveDrug(drug);
	}
	
	/**
	 * @deprecated Use #saveDrug(Drug)
	 */
	public void updateDrug(Drug drug) {
		saveDrug(drug);
	}
	
	/**
	 * @deprecated use #purgeConcept(Concept concept)
	 */
	public void deleteConcept(Concept concept) {
		purgeConcept(concept);
	}
	
	/**
	 * @deprecated use {@link #retireConcept(Concept, String)}etireConcept
	 */
	public void voidConcept(Concept concept, String reason) {
		retireConcept(concept, reason);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#saveConcept(org.openmrs.Concept)
	 */
	public Concept saveConcept(Concept concept) throws APIException {
		
		// make sure the administrator hasn't turned off concept editing
		checkIfLocked();
		
		// set that creator/dateCreated properties on the concept and child objects
		this.modifyCollections(concept);
		
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
		
		if (drug.getCreator() == null)
			drug.setCreator(Context.getAuthenticatedUser());
		if (drug.getDateCreated() == null)
			drug.setDateCreated(new Date());
		
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
			concept.setRetiredBy(Context.getAuthenticatedUser());
			concept.setDateRetired(new Date());
			return dao.saveConcept(concept);
		}
		
		return concept;
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#retireDrug(org.openmrs.Drug, java.lang.String)
	 * @throws APIException
	 */
	public Drug retireDrug(Drug drug, String reason) throws APIException {
		
		if (drug.isRetired() == false) {
			drug.setRetired(true);
			drug.setRetiredBy(Context.getAuthenticatedUser());
			drug.setRetireReason(reason);
			drug.setDateRetired(new Date());
			return dao.saveDrug(drug);
		}
		
		return drug;
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#unretireDrug(org.openmrs.Drug)
	 */
	public Drug unretireDrug(Drug drug) throws APIException {
		if (drug.isRetired() == true) {
			drug.setRetired(false);
			drug.setRetiredBy(null);
			drug.setRetireReason(null);
			drug.setDateRetired(null);
			return dao.saveDrug(drug);
		}
		
		return drug;
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
	public List<ConceptWord> findConcepts(String phrase, Locale locale, boolean includeRetired) {
		
		List<Locale> locales = new Vector<Locale>();
		locales.add(locale);
		
		return getConceptWords(phrase, locales, includeRetired, null, null, null, null, null, null, null);
	}
	
	/**
	 * @deprecated use
	 *             {@link #getConceptWords(String, List, boolean, List, List, List, List, Concept, Integer, Integer)}
	 */
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
	public Drug getDrugByNameOrId(String drugNameOrId) {
		return getDrug(drugNameOrId);
	}
	
	/**
	 * @deprecated use {@link #getAllDrugs()}
	 */
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
	public List<Drug> getDrugs(Concept concept, boolean includeRetired) {
		if (includeRetired == true)
			throw new APIException("Getting retired drugs is no longer an options.  Use the getAllDrugs() method for that");
		
		return getDrugsByConcept(concept);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getAllDrugs(boolean)
	 * @deprecated Use {@link #getAllDrugs(boolean)}
	 */
	public List<Drug> getDrugs(boolean includeVoided) {
		return getAllDrugs(includeVoided);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#findDrugs(java.lang.String, boolean)
	 * @deprecated Use {@link #getDrugs(String)}
	 */
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
		if (cc.getDateCreated() == null)
			cc.setDateCreated(new Date());
		if (cc.getCreator() == null)
			cc.setCreator(Context.getAuthenticatedUser());
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
		if (cd.getCreator() == null)
			cd.setCreator(Context.getAuthenticatedUser());
		if (cd.getDateCreated() == null)
			cd.setDateCreated(new Date());
		return dao.saveConceptDatatype(cd);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getAllConceptDatatypes()
	 * @deprecated use {@link #getAllConceptDatatypes() }
	 */
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
		List<ConceptDatatype> lcd = new ArrayList<ConceptDatatype>();
		lcd = getConceptDatatypes(name);
		if (lcd.size() > 1)
			log.warn("More than one ConceptDatatype found with name " + name);
		if (lcd.size() == 0)
			return null;
		return lcd.get(0);
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
	public void proposeConcept(ConceptProposal conceptProposal) {
		saveConceptProposal(conceptProposal);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#saveConceptProposal(org.openmrs.ConceptProposal)
	 */
	public ConceptProposal saveConceptProposal(ConceptProposal conceptProposal) throws APIException {
		// set the state of the proposal
		if (conceptProposal.getState() == null)
			conceptProposal.setState(OpenmrsConstants.CONCEPT_PROPOSAL_UNMAPPED);
		
		// set the creator and date created
		if (conceptProposal.getCreator() == null && conceptProposal.getEncounter() != null)
			conceptProposal.setCreator(conceptProposal.getEncounter().getCreator());
		else
			conceptProposal.setCreator(Context.getAuthenticatedUser());
		
		if (conceptProposal.getDateCreated() == null && conceptProposal.getEncounter() != null)
			conceptProposal.setDateCreated(conceptProposal.getEncounter().getDateCreated());
		else
			conceptProposal.setDateCreated(new Date());
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
	public void rejectConceptProposal(ConceptProposal cp) {
		cp.rejectConceptProposal();
		saveConceptProposal(cp);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#findMatchingConceptProposals(String text)
	 * @deprecated use {@link #getConceptProposals(String)}
	 */
	public List<ConceptProposal> findMatchingConceptProposals(String text) {
		return getConceptProposals(text);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#findConceptAnswers(String phrase, Locale locale,Concept
	 *      concept, boolean includeRetired)
	 * @deprecated use {@link #getConceptAnswers(String, Locale, Concept)}
	 */
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
	 * Sets required fields for a concept before saving it.
	 * 
	 * @param concept
	 */
	protected void modifyCollections(Concept concept) {
		User authUser = Context.getAuthenticatedUser();
		Date timestamp = new Date();
		
		if (concept.getCreator() == null)
			concept.setCreator(authUser);
		if (concept.getDateCreated() == null)
			concept.setDateCreated(timestamp);
		
		// if updating a concept (instead of creating a new one)
		if (concept.getConceptId() != null) {
			concept.setChangedBy(authUser);
			concept.setDateChanged(timestamp);
		}
		
		if (concept.getNames() != null) {
			for (ConceptName cn : concept.getNames()) {
				if (cn.getCreator() == null)
					cn.setCreator(authUser);
				if (cn.getDateCreated() == null)
					cn.setDateCreated(timestamp);
				
				cn.setConcept(concept);
				
				if (cn.getTags() != null) {
					for (ConceptNameTag tag : cn.getTags()) {
						if (tag.getConceptNameTagId() == null) {
							ConceptNameTag possibleReplacementTag = getConceptNameTagByName(tag.getTag());
							if (possibleReplacementTag != null) {
								cn.removeTag(tag);
								cn.addTag(possibleReplacementTag);
							}
						}
						if (tag.getCreator() == null)
							tag.setCreator(authUser);
						if (tag.getDateCreated() == null)
							tag.setDateCreated(timestamp);
					}
				}
				
				// alter the other voided info
				if (cn.isVoided()) {
					if (cn.getVoidedBy() == null)
						cn.setVoidedBy(authUser);
					if (cn.getDateVoided() == null)
						cn.setDateVoided(timestamp);
				} else {
					cn.setVoidReason(null);
					cn.setDateVoided(null);
					cn.setVoidedBy(null);
				}
			}
		}
		
		if (concept.getConceptSets() != null) {
			for (ConceptSet set : concept.getConceptSets()) {
				if (set.getCreator() == null)
					set.setCreator(authUser);
				if (set.getDateCreated() == null)
					set.setDateCreated(timestamp);
				
				set.setConceptSet(concept);
			}
		}
		if (concept.getAnswers(true) != null) {
			for (ConceptAnswer ca : concept.getAnswers(true)) {
				if (ca.getCreator() == null)
					ca.setCreator(authUser);
				if (ca.getDateCreated() == null)
					ca.setDateCreated(timestamp);
				
				ca.setConcept(concept);
			}
		}
		if (concept.getDescriptions() != null) {
			for (ConceptDescription cd : concept.getDescriptions()) {
				if (cd.getCreator() == null)
					cd.setCreator(authUser);
				if (cd.getDateCreated() == null)
					cd.setDateCreated(timestamp);
				
				cd.setConcept(concept);
			}
		}
		if (concept.getConceptMappings() != null) {
			for (ConceptMap map : concept.getConceptMappings()) {
				if (map.getCreator() == null)
					map.setCreator(authUser);
				if (map.getDateCreated() == null)
					map.setDateCreated(timestamp);
				
				map.setConcept(concept);
			}
		}
		
	}
	
	/**
	 * This will weight and sort the concepts we are assuming the hits are sorted with synonym
	 * matches at the bottom
	 * 
	 * @param phrase that was used to get this search
	 * @param locales List<Locale> that were used to get this search
	 * @param conceptWords
	 * @return List<ConceptWord> object containing sorted <code>ConceptWord</code>s
	 */
	protected List<ConceptWord> weightWords(String phrase, List<Locale> locales, List<ConceptWord> conceptWords) {
		
		// Map<ConceptId, ConceptWord>
		Map<Integer, ConceptWord> uniqueConcepts = new HashMap<Integer, ConceptWord>();
		
		// phrase words
		if (phrase == null)
			phrase = "";
		List<String> searchedWords = ConceptWord.getUniqueWords(phrase);
		
		Integer id = null;
		Concept concept = null;
		for (ConceptWord tmpWord : conceptWords) {
			concept = tmpWord.getConcept();
			id = concept.getConceptId();
			
			if (uniqueConcepts.containsKey(id)) {
				ConceptWord initialWord = uniqueConcepts.get(id);
				
				// this concept is already in the list
				// because we're sort synonyms at the bottom, the initial
				// concept must be a match on the conceptName
				// check synonym in case we have multiple synonym hits
				String toSplit = initialWord.getSynonym();
				if (toSplit == null || toSplit.equals("")) {
					ConceptName cn = null;
					// find which locale provided the concept name
					for (Locale locale : locales) {
						cn = initialWord.getConcept().getName(locale);
						if (cn != null) {
							toSplit = cn.getName();
							break;
						}
					}
				}
				List<String> nameWords = ConceptWord.getUniqueWords(toSplit);
				
				// if the conceptName doesn't contain all of the search words,
				// replace the initial word with this synonym based word
				if (!containsAll(nameWords, searchedWords)) {
					tmpWord.setWeight(initialWord.getWeight());
					uniqueConcepts.put(id, tmpWord);
				} else
					tmpWord = null;
				
			} else {
				// normalize the weighting
				tmpWord.setWeight(0.0);
				// its not in the list, add it
				uniqueConcepts.put(id, tmpWord);
			}
			
			// don't increase weight with second/third/... synonym
			if (tmpWord != null) {
				// default matched string
				String matchedString = tmpWord.getSynonym();
				
				// if there isn't a synonym, it is matching on the name,
				if (matchedString.length() == 0) {
					// We weight name matches higher
					tmpWord.increaseWeight(2.0);
					for (Locale locale : locales) {
						ConceptName cn = tmpWord.getConcept().getName(locale);
						if (cn != null) {
							matchedString = cn.getName();
							break;
						}
					}
				}
				
				// increase the weight by a factor of the % of words matched
				Double percentMatched = getPercentMatched(searchedWords, matchedString);
				tmpWord.increaseWeight(5.0 * percentMatched);
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
	 * @see org.openmrs.api.ConceptService#saveConceptSource(org.openmrs.ConceptSource)
	 */
	public ConceptSource saveConceptSource(ConceptSource conceptSource) throws APIException {
		
		if (conceptSource.getCreator() == null)
			conceptSource.setCreator(Context.getAuthenticatedUser());
		if (conceptSource.getDateCreated() == null)
			conceptSource.setDateCreated(new Date());
		
		return dao.saveConceptSource(conceptSource);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#saveConceptNameTag(org.openmrs.ConceptNameTag)
	 */
	public ConceptNameTag saveConceptNameTag(ConceptNameTag nameTag) {
		checkIfLocked();
		
		if (nameTag.getDateCreated() == null) {
			nameTag.setDateCreated(new Date());
		}
		if (nameTag.getCreator() == null) {
			nameTag.setCreator(Context.getAuthenticatedUser());
		}
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
		conceptWordUpdateTaskDef.setRepeatInterval(0l); // zero interval means do not repeat
		conceptWordUpdateTaskDef.setStartOnStartup(false);
		conceptWordUpdateTaskDef.setStartTime(null); // to induce immediate execution
		conceptWordUpdateTaskDef.setName(CONCEPT_WORD_UPDATE_TASK_NAME);
		conceptWordUpdateTaskDef
		        .setDescription("Iterates through the concept dictionary, re-creating concept words (which are used for searcing). This task is started when using the \"Update Concept Word Storage\" page and no range is given.  This task stops itself when one iteration has completed.");
		return conceptWordUpdateTaskDef;
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
	
}
