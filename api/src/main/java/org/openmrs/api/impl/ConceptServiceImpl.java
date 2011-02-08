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

import static org.apache.commons.lang.StringUtils.contains;

import java.lang.reflect.InvocationTargetException;
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
import java.util.UUID;
import java.util.Vector;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections.CollectionUtils;
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
import org.openmrs.ConceptSearchResult;
import org.openmrs.ConceptSet;
import org.openmrs.ConceptSource;
import org.openmrs.ConceptStopWord;
import org.openmrs.ConceptWord;
import org.openmrs.Drug;
import org.openmrs.Obs;
import org.openmrs.api.APIException;
import org.openmrs.api.ConceptInUseException;
import org.openmrs.api.ConceptNameInUseException;
import org.openmrs.api.ConceptService;
import org.openmrs.api.ConceptStopWordException;
import org.openmrs.api.ConceptsLockedException;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.ConceptDAO;
import org.openmrs.api.db.DAOException;
import org.openmrs.scheduler.SchedulerException;
import org.openmrs.scheduler.SchedulerService;
import org.openmrs.scheduler.Task;
import org.openmrs.scheduler.TaskDefinition;
import org.openmrs.util.LocaleUtility;
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
	 * Name of the concept word update task. A constant, because we only manage
	 * a single task with this name.
	 */
	public static final String CONCEPT_WORD_UPDATE_TASK_NAME = "Update Concept Index";
	
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
		
		List<ConceptName> changedConceptNames = null;
		Map<String, ConceptName> uuidClonedConceptNameMap = null;
		
		if (concept.getConceptId() != null) {
			uuidClonedConceptNameMap = new HashMap<String, ConceptName>();
			for (ConceptName conceptName : concept.getNames()) {
				// ignore newly added names
				if (conceptName.getConceptNameId() != null) {
					ConceptName clone = cloneConceptName(conceptName);
					clone.setConceptNameId(null);
					uuidClonedConceptNameMap.put(conceptName.getUuid(), clone);
					
					if (hasNameChanged(conceptName)) {
						if (changedConceptNames == null)
							changedConceptNames = new ArrayList<ConceptName>();
						changedConceptNames.add(conceptName);
					} else {
						// put back the concept name id
						clone.setConceptNameId(conceptName.getConceptNameId());
						// Use the cloned version
						try {
							BeanUtils.copyProperties(conceptName, clone);
						}
						catch (IllegalAccessException e) {
							log.error("Error generated", e);
						}
						catch (InvocationTargetException e) {
							log.error("Error generated", e);
						}
					}
				}
			}
		}
		
		Errors errors = new BindException(concept, "concept");
		new ConceptValidator().validate(concept, errors);
		if (errors.hasErrors())
			throw new APIException("Validation errors found");
		
		if (CollectionUtils.isNotEmpty(changedConceptNames)) {
			for (ConceptName changedName : changedConceptNames) {
				// void old concept name
				ConceptName nameInDB = changedName;
				nameInDB.setVoided(true);
				nameInDB.setDateVoided(new Date());
				nameInDB.setVoidedBy(Context.getAuthenticatedUser());
				nameInDB.setVoidReason(Context.getMessageSourceService().getMessage("Concept.name.voidReason.nameChanged"));
				
				// Make the voided name a synonym, this would help to avoid
				// having multiple fully specified or preferred
				// names in a locale incase the name is unvoided
				if (!nameInDB.isSynonym())
					nameInDB.setConceptNameType(null);
				if (nameInDB.isLocalePreferred())
					nameInDB.setLocalePreferred(false);
				
				// create a new concept name from the matching cloned
				// conceptName
				ConceptName clone = uuidClonedConceptNameMap.get(nameInDB.getUuid());
				clone.setUuid(UUID.randomUUID().toString());
				concept.addName(clone);
			}
		}
		
		//Set a preferred name for each locale for those where it isn't yet specified
		for (Locale locale : LocaleUtility.getLocalesInOrder()) {
			ConceptName possiblePreferredName = concept.getPreferredName(locale);
			if (possiblePreferredName == null || !possiblePreferredName.isLocalePreferred()) {
				if (possiblePreferredName != null)
					possiblePreferredName.setLocalePreferred(true);
				//set the first synonym as the preferred name if it has any
				else if (!CollectionUtils.isEmpty(concept.getSynonyms(locale)))
					concept.getSynonyms(locale).iterator().next().setLocalePreferred(true);
			}
		}
		
		Concept conceptToReturn = dao.saveConcept(concept);
		
		// add/remove entries in the concept_word table (used for searching)
		this.updateConceptIndex(conceptToReturn);
		
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
		
		if (concept.getConceptId() != null) {
			for (ConceptName conceptName : concept.getNames()) {
				if (hasAnyObservation(conceptName))
					throw new ConceptNameInUseException("Can't delete concept with id : " + concept.getConceptId()
					        + " because it has a name '" + conceptName.getName()
					        + "' which is being used by some observation(s)");
			}
		}
		
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
					for (ConceptName indexTerm : c.getIndexTermsForLocale(Context.getLocale())) {
						if (indexTerm.getName().compareTo(name) == 0)
							return c;
					}
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
	 * @see ConceptService#getConcepts(String, List, boolean, List, List, List, List, Concept,
	 *      Integer, Integer)
	 */
	@Deprecated
	public List<ConceptWord> getConceptWords(String phrase, List<Locale> locales, boolean includeRetired,
	        List<ConceptClass> requireClasses, List<ConceptClass> excludeClasses, List<ConceptDatatype> requireDatatypes,
	        List<ConceptDatatype> excludeDatatypes, Concept answerToConcept, Integer start, Integer size) {
		
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
	 * @see ConceptService#getConceptWords(String, Locale)
	 */
	@Deprecated
	public List<ConceptWord> getConceptWords(String phrase, Locale locale) throws APIException {
		List<Locale> locales = new Vector<Locale>();
		locales.add(locale);
		
		return getConceptWords(phrase, locales, false, null, null, null, null, null, null, null);
	}
	
	/**
	 * @see ConceptService#findConcepts(String, Locale, boolean, int, int)
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
	 * @see ConceptService#findConcepts(String, Locale, boolean)
	 */
	@Deprecated
	public List<ConceptWord> findConcepts(String phrase, Locale locale, boolean includeRetired) {
		
		List<Locale> locales = new Vector<Locale>();
		locales.add(locale);
		
		return getConceptWords(phrase, locales, includeRetired, null, null, null, null, null, null, null);
	}
	
	/**
	 * @see ConceptService#findConcepts(String, Locale, boolean, List, List, List, List)
	 */
	@Deprecated
	public List<ConceptWord> findConcepts(String phrase, Locale locale, boolean includeRetired,
	        List<ConceptClass> requireClasses, List<ConceptClass> excludeClasses, List<ConceptDatatype> requireDatatypes,
	        List<ConceptDatatype> excludeDatatypes) {
		
		List<Locale> locales = new Vector<Locale>();
		locales.add(locale);
		
		return getConceptWords(phrase, locales, includeRetired, requireClasses, excludeClasses, requireDatatypes,
		    excludeDatatypes, null, null, null);
	}
	
	/**
	 * @see ConceptService#findConcepts(String, List, boolean, List, List, List, List)
	 */
	@Deprecated
	public List<ConceptWord> findConcepts(String phrase, List<Locale> locales, boolean includeRetired,
	        List<ConceptClass> requireClasses, List<ConceptClass> excludeClasses, List<ConceptDatatype> requireDatatypes,
	        List<ConceptDatatype> excludeDatatypes) {
		
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
			return drugs.get(0);
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
			return null;
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
	 * @see ConceptService#findConceptAnswers(String, Locale, Concept, boolean)
	 */
	@Deprecated
	public List<ConceptWord> findConceptAnswers(String phrase, Locale locale, Concept concept, boolean includeRetired) {
		
		return getConceptAnswers(phrase, locale, concept);
	}
	
	/**
	 * @see ConceptService#findConceptAnswers(String, Locale, Concept)
	 */
	@Deprecated
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
	@Deprecated
	public void updateConceptWords() throws APIException {
		updateConceptIndexes();
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#updateConceptWord(org.openmrs.Concept)
	 */
	@Deprecated
	public void updateConceptWord(Concept concept) throws APIException {
		updateConceptIndex(concept);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#updateConceptWords(java.lang.Integer, java.lang.Integer)
	 */
	@Deprecated
	public void updateConceptWords(Integer conceptIdStart, Integer conceptIdEnd) throws APIException {
		updateConceptIndexes(conceptIdStart, conceptIdEnd);
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
	 * @should weigh preferred names higher than other names in the locale
	 * @should weigh a fully specified name higher than an indexTerm in the locale
	 * @should weigh a fully specified name higher than a synonym in the locale
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
			currentWord.setWeight(0.0);
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
			
			// if the conceptName doesn't contain all of the search words, lower
			// the weighting
			if (!containsAll(nameWords, searchedWords)) {
				currentWord.increaseWeight(-2.0);
			}
			
			log.debug("Weight for: " + conceptName.getName() + " is: " + currentWord.getWeight());
			
			if (uniqueConcepts.containsKey(conceptId)) {
				// if we've seen another name for this concept already, check
				// the name weightings
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
				ret.add(c);
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
	
	private TaskDefinition createConceptIndexUpdateTask() {
		TaskDefinition conceptIndexUpdateTaskDef = new TaskDefinition();
		conceptIndexUpdateTaskDef.setTaskClass("org.openmrs.scheduler.tasks.ConceptIndexUpdateTask");
		conceptIndexUpdateTaskDef.setRepeatInterval(0L); // zero interval means
		// do not repeat
		conceptIndexUpdateTaskDef.setStartOnStartup(false);
		conceptIndexUpdateTaskDef.setStartTime(null); // to induce immediate
		// execution
		conceptIndexUpdateTaskDef.setName(CONCEPT_WORD_UPDATE_TASK_NAME);
		conceptIndexUpdateTaskDef
		        .setDescription("Iterates through the concept dictionary, re-creating concept index (which are used for searcing). This task is started when using the \"Update Concept Index Storage\" page and no range is given.  This task stops itself when one iteration has completed.");
		return conceptIndexUpdateTaskDef;
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
	public Concept getConceptByMapping(String code, String sourceName) throws APIException {
		return getConceptByMapping(code, sourceName, true);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getConceptByMapping(java.lang.String, java.lang.String,
	 *      java.lang.Boolean)
	 */
	public Concept getConceptByMapping(String code, String sourceName, Boolean includeRetired) throws APIException {
		List<Concept> concepts = getConceptsByMapping(code, sourceName, includeRetired);		
		
		if (concepts.size() == 0) {
			return null;
		} 
		// we want to throw an exception if there is more than one non-retired concept; 
		// since the getConceptByMapping DAO method returns a list with all non-retired concept
		// sorted to the front of the list, we can test if there is more than one retired concept
		// by testing if the second concept in the list is retired or not
		else if (concepts.size() > 1 && !concepts.get(1).isRetired()) { 
			 throw new APIException("Multiple non-retired concepts found for mapping " + code + " from source " + sourceName);
		}
		else {
			return concepts.get(0);
		}
	}
	
	/**
	 * @see org.openmrs.api.ConceptService
	 * @getConceptsByMapping(java.lang.String, java.lang.String)
	 */
	public List<Concept> getConceptsByMapping(String code, String sourceName) throws APIException {
		return getConceptsByMapping(code, sourceName, true);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService
	 * @getConceptsByMapping(java.lang.String, java.lang.String, boolean)
	 */
	public List<Concept> getConceptsByMapping(String code, String sourceName, boolean includeRetired) throws APIException {
		return dao.getConceptsByMapping(code, sourceName, includeRetired);
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
	 * @throws ConceptInUseException
	 */
	private void checkIfDatatypeCanBeChanged(Concept concept) {
		if (concept.getId() != null) {
			if (hasAnyObservation(concept) && hasDatatypeChanged(concept)) {
				// allow boolean concepts to be converted to coded
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
	
	/**
	 * @see org.openmrs.api.ConceptService#hasAnyObservation(org.openmrs.ConceptName)
	 */
	@Override
	public boolean hasAnyObservation(ConceptName conceptName) throws APIException {
		List<ConceptName> conceptNames = new Vector<ConceptName>();
		conceptNames.add(conceptName);
		Integer count = Context.getObsService().getObservationCount(conceptNames, true);
		return count > 0;
	}
	
	/**
	 * Utility method which loads the previous version of a conceptName to check if the name
	 * property of the given conceptName has changed.
	 * 
	 * @param conceptName to be modified
	 * @return boolean indicating change in the name property
	 */
	private boolean hasNameChanged(ConceptName conceptName) {
		String newName = conceptName.getName();
		String oldName = dao.getSavedConceptName(conceptName).getName();
		return !oldName.equalsIgnoreCase(newName);
	}
	
	/**
	 * Creates a copy of a conceptName
	 * 
	 * @param conceptName the conceptName to be cloned
	 * @return the cloned conceptName
	 */
	private ConceptName cloneConceptName(ConceptName conceptName) {
		ConceptName copy = new ConceptName();
		try {
			copy = (ConceptName) BeanUtils.cloneBean(conceptName);
		}
		catch (IllegalAccessException e) {
			
			log.warn("Error generated", e);
		}
		catch (InstantiationException e) {
			
			log.warn("Error generated", e);
		}
		catch (InvocationTargetException e) {
			
			log.warn("Error generated", e);
		}
		catch (NoSuchMethodException e) {
			
			log.warn("Error generated", e);
		}
		return copy;
	}
	
	/**
	 * @see ConceptService#findConceptAnswers(String, Locale, Concept)
	 */
	@Override
	public List<ConceptSearchResult> findConceptAnswers(String phrase, Locale locale, Concept concept) throws APIException {
		
		List<ConceptWord> conceptWords = getConceptAnswers(phrase, locale, concept);
		return createSearchResultsList(conceptWords);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getConceptStopWords(java.util.Locale)
	 */
	public List<String> getConceptStopWords(Locale locale) {
		return dao.getConceptStopWords(locale);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#saveConceptStopWord(org.openmrs.ConceptStopWord)
	 */
	public ConceptStopWord saveConceptStopWord(ConceptStopWord conceptStopWord) throws APIException {
		try {
			return dao.saveConceptStopWord(conceptStopWord);
		}
		catch (DAOException e) {
			if (e.getMessage().equalsIgnoreCase("Duplicate ConceptStopWord Entry")) {
				throw new ConceptStopWordException("ConceptStopWord.duplicated", e);
			}
			throw new ConceptStopWordException("ConceptStopWord.notSaved", e);
		}
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#deleteConceptStopWord(Integer)
	 */
	public void deleteConceptStopWord(Integer conceptStopWordId) throws APIException {
		try {
			dao.deleteConceptStopWord(conceptStopWordId);
		}
		catch (DAOException e) {
			if (contains(e.getMessage(), "Concept Stop Word not found or already deleted")) {
				throw new ConceptStopWordException("ConceptStopWord.error.notfound", e);
			}
			throw new ConceptStopWordException("general.cannot.delete", e);
		}
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getAllConceptStopWords()
	 */
	public List<ConceptStopWord> getAllConceptStopWords() {
		return dao.getAllConceptStopWords();
	}
	
	/**
	 * @see ConceptService#getConcepts(String, List, boolean, List, List, List, List, Concept,
	 *      Integer, Integer)
	 */
	@Override
	public List<ConceptSearchResult> getConcepts(String phrase, List<Locale> locales, boolean includeRetired,
	        List<ConceptClass> requireClasses, List<ConceptClass> excludeClasses, List<ConceptDatatype> requireDatatypes,
	        List<ConceptDatatype> excludeDatatypes, Concept answersToConcept, Integer start, Integer size)
	        throws APIException {
		
		if (requireClasses == null)
			requireClasses = new Vector<ConceptClass>();
		if (excludeClasses == null)
			excludeClasses = new Vector<ConceptClass>();
		if (requireDatatypes == null)
			requireDatatypes = new Vector<ConceptDatatype>();
		if (excludeDatatypes == null)
			excludeDatatypes = new Vector<ConceptDatatype>();
		
		return dao.getConcepts(phrase, locales, includeRetired, requireClasses, excludeClasses, requireDatatypes,
		    excludeDatatypes, answersToConcept, start, size);
		
	}
	
	/**
	 * @see ConceptService#updateConceptIndexes(Integer, Integer)
	 */
	@Override
	public void updateConceptIndexes(Integer conceptIdStart, Integer conceptIdEnd) throws APIException {
		checkIfLocked();
		Integer i = conceptIdStart;
		ConceptService cs = Context.getConceptService();
		while (i++ <= conceptIdEnd) {
			updateConceptWord(cs.getConcept(i));
		}
	}
	
	/**
	 * @see ConceptService#updateConceptIndex(Concept)
	 */
	@Override
	public void updateConceptIndex(Concept concept) throws APIException {
		checkIfLocked();
		dao.updateConceptWord(concept);
	}
	
	/**
	 * @see ConceptService#updateConceptIndexes()
	 */
	@Override
	public void updateConceptIndexes() throws APIException {
		checkIfLocked();
		SchedulerService ss = Context.getSchedulerService();
		
		// ABKTODO: this whole pattern should be moved into the scheduler,
		// providing a call like scheduleThisIfNotRunning()
		TaskDefinition conceptIndexUpdateTaskDef = ss.getTaskByName(CONCEPT_WORD_UPDATE_TASK_NAME);
		if (conceptIndexUpdateTaskDef == null) {
			conceptIndexUpdateTaskDef = createConceptIndexUpdateTask();
			try {
				ss.saveTask(conceptIndexUpdateTaskDef);
				conceptWordUpdateTask = ss.scheduleTask(conceptIndexUpdateTaskDef);
			}
			catch (SchedulerException e) {
				log.error("Failed to schedule concept-word update task, because:", e);
			}
		} else {
			// task definition exists. get the task itself
			conceptWordUpdateTask = conceptIndexUpdateTaskDef.getTaskInstance();
			if (conceptWordUpdateTask == null) {
				try {
					ss.rescheduleTask(conceptIndexUpdateTaskDef);
				}
				catch (SchedulerException e) {
					log.error("Failed to schedule concept-word update task, because:", e);
				}
			} else if (!conceptWordUpdateTask.isExecuting()) {
				try {
					ss.rescheduleTask(conceptIndexUpdateTaskDef);
				}
				catch (SchedulerException e) {
					log.error("Failed to re-schedule concept-word update task, because:", e);
				}
			}
		}
		
	}
	
	/**
	 * Convenience method that creates a list of ConceptSearchResults from the specified list of
	 * ConceptWords
	 * 
	 * @param conceptWords
	 * @return
	 */
	private List<ConceptSearchResult> createSearchResultsList(List<ConceptWord> conceptWords) {
		
		if (CollectionUtils.isNotEmpty(conceptWords)) {
			ArrayList<ConceptSearchResult> conceptSearchResults = new ArrayList<ConceptSearchResult>();
			for (ConceptWord conceptWord : conceptWords) {
				if (conceptWord != null) {
					// constructor ConceptSearchResult(ConceptWord) is not
					// visible here
					conceptSearchResults.add(new ConceptSearchResult(conceptWord.getWord(), conceptWord.getConcept(),
					        conceptWord.getConceptName(), conceptWord.getWeight()));
				}
			}
			
			return conceptSearchResults;
		}
		
		return Collections.emptyList();
		
	}
	
	/**
	 * @see ConceptService#getCountOfConcepts(String, List, boolean, List, List, List, List,
	 *      Concept)
	 */
	@Override
	public Integer getCountOfConcepts(String phrase, List<Locale> locales, boolean includeRetired,
	        List<ConceptClass> requireClasses, List<ConceptClass> excludeClasses, List<ConceptDatatype> requireDatatypes,
	        List<ConceptDatatype> excludeDatatypes, Concept answersToConcept) {
		
		return dao.getCountOfConceptWords(phrase, locales, includeRetired, requireClasses, excludeClasses, requireDatatypes,
		    excludeDatatypes, answersToConcept, true);
	}
	
	/**
	 * @see ConceptService#getCountOfDrugs(String, Concept, boolean, boolean, boolean)
	 */
	public Integer getCountOfDrugs(String drugName, Concept concept, boolean searchOnPhrase, boolean searchDrugConceptNames,
	        boolean includeRetired) throws APIException {
		return dao.getCountOfDrugs(drugName, concept, searchOnPhrase, searchDrugConceptNames, includeRetired);
	}
	
	/**
	 * @see ConceptService#getDrugs(String, Concept, boolean, boolean, boolean, Integer, Integer)
	 */
	public List<Drug> getDrugs(String drugName, Concept concept, boolean searchOnPhrase, boolean searchDrugConceptNames,
	        boolean includeRetired, Integer start, Integer length) throws APIException {
		return dao.getDrugs(drugName, concept, searchOnPhrase, searchDrugConceptNames, includeRetired, start, length);
	}
	
	/**
	 * @see ConceptService#getConcepts(String, Locale, boolean)
	 */
	@Override
	public List<ConceptSearchResult> getConcepts(String phrase, Locale locale, boolean includeRetired) throws APIException {
		List<Locale> locales = new Vector<Locale>();
		if (locale != null)
			locales.add(locale);
		
		return getConcepts(phrase, locales, includeRetired, null, null, null, null, null, null, null);
	}
}
