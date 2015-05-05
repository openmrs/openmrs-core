/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.impl;

import static org.apache.commons.lang.StringUtils.contains;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.openmrs.Obs;
import org.openmrs.api.APIException;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.ConceptInUseException;
import org.openmrs.api.ConceptNameInUseException;
import org.openmrs.api.ConceptService;
import org.openmrs.api.ConceptStopWordException;
import org.openmrs.api.ConceptsLockedException;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.ConceptDAO;
import org.openmrs.api.db.DAOException;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.OpenmrsUtil;
import org.openmrs.validator.ValidateUtil;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * Default Implementation of ConceptService service layer classes
 * 
 * @see org.openmrs.api.ConceptService to access these methods
 */
@Transactional
public class ConceptServiceImpl extends BaseOpenmrsService implements ConceptService {
	
	private final Log log = LogFactory.getLog(getClass());
	
	private ConceptDAO dao;
	
	private static Concept trueConcept;
	
	private static Concept falseConcept;
	
	private static Concept unknownConcept;
	
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
	 * @should return the concept with new conceptID if creating new concept
	 * @should return the concept with same conceptID if updating existing concept
	 * @should leave preferred name preferred if set
	 * @should set default preferred name to fully specified first
	 * @should not set default preferred name to short or index terms
	 */
	public Concept saveConcept(Concept concept) throws APIException {
		ConceptMapType defaultConceptMapType = null;
		for (ConceptMap map : concept.getConceptMappings()) {
			if (map.getConceptMapType() == null) {
				if (defaultConceptMapType == null) {
					defaultConceptMapType = Context.getConceptService().getDefaultConceptMapType();
				}
				map.setConceptMapType(defaultConceptMapType);
			}
		}
		
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
						if (changedConceptNames == null) {
							changedConceptNames = new ArrayList<ConceptName>();
						}
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
				if (!nameInDB.isSynonym()) {
					nameInDB.setConceptNameType(null);
				}
				if (nameInDB.isLocalePreferred()) {
					nameInDB.setLocalePreferred(false);
				}
				
				// create a new concept name from the matching cloned
				// conceptName
				ConceptName clone = uuidClonedConceptNameMap.get(nameInDB.getUuid());
				clone.setUuid(UUID.randomUUID().toString());
				clone.setDateCreated(null);
				clone.setCreator(null);
				concept.addName(clone);
			}
		}
		
		//Ensure if there's a name for a locale that at least one suitable name is marked preferred in that locale
		//Order of preference is:
		// 1) any name that concept.getPreferredName returns
		// 2) fully specified name
		// 3) any synonym
		// short name and index terms are never preferred.
		
		Set<Locale> checkedLocales = new HashSet<Locale>();
		for (ConceptName n : concept.getNames()) {
			Locale locale = n.getLocale();
			if (checkedLocales.contains(locale)) {
				continue; //we've already checked this locale
			}
			
			//getPreferredName(locale) returns any name marked preferred,
			//or the fullySpecifiedName even if not marked preferred
			ConceptName possiblePreferredName = concept.getPreferredName(locale);
			
			if (possiblePreferredName != null) {
				//do nothing yet, but stick around to setLocalePreferred(true)
			} else if (concept.getFullySpecifiedName(locale) != null) {
				possiblePreferredName = concept.getFullySpecifiedName(locale);
			} else if (!CollectionUtils.isEmpty(concept.getSynonyms(locale))) {
				concept.getSynonyms(locale).iterator().next().setLocalePreferred(true);
			}
			//index terms are never used as preferred name
			
			if (possiblePreferredName != null) { //there may have been none
				possiblePreferredName.setLocalePreferred(true);
			}
			checkedLocales.add(locale);
		}
		
		//See TRUNK-3337 for why we set changed by and date changed every time we save a concept.
		concept.setDateChanged(new Date());
		concept.setChangedBy(Context.getAuthenticatedUser());
		
		Concept conceptToReturn = dao.saveConcept(concept);
		
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
				if (hasAnyObservation(conceptName)) {
					throw new ConceptNameInUseException("Can't delete concept with id : " + concept.getConceptId()
					        + " because it has a name '" + conceptName.getName()
					        + "' which is being used by some observation(s)");
				}
			}
		}
		
		dao.purgeConcept(concept);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#retireConcept(org.openmrs.Concept, java.lang.String)
	 */
	public Concept retireConcept(Concept concept, String reason) throws APIException {
		if (!StringUtils.hasText(reason)) {
			throw new IllegalArgumentException(Context.getMessageSourceService().getMessage("general.voidReason.empty"));
		}
		
		// only do this if the concept isn't retired already
		if (!concept.isRetired()) {
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
	@Transactional(readOnly = true)
	public Concept getConcept(Integer conceptId) throws APIException {
		return dao.getConcept(conceptId);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getConceptName(java.lang.Integer)
	 */
	@Transactional(readOnly = true)
	public ConceptName getConceptName(Integer conceptNameId) throws APIException {
		return dao.getConceptName(conceptNameId);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getConceptAnswer(java.lang.Integer)
	 */
	@Transactional(readOnly = true)
	public ConceptAnswer getConceptAnswer(Integer conceptAnswerId) throws APIException {
		return dao.getConceptAnswer(conceptAnswerId);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getDrug(java.lang.Integer)
	 */
	@Transactional(readOnly = true)
	public Drug getDrug(Integer drugId) throws APIException {
		return dao.getDrug(drugId);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getConceptNumeric(java.lang.Integer)
	 */
	@Transactional(readOnly = true)
	public ConceptNumeric getConceptNumeric(Integer conceptId) throws APIException {
		return dao.getConceptNumeric(conceptId);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getConceptComplex(java.lang.Integer)
	 */
	@Transactional(readOnly = true)
	public ConceptComplex getConceptComplex(Integer conceptId) {
		return dao.getConceptComplex(conceptId);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getAllConcepts()
	 */
	@Transactional(readOnly = true)
	public List<Concept> getAllConcepts() throws APIException {
		return Context.getConceptService().getAllConcepts(null, true, true);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getAllConcepts(java.lang.String, boolean, boolean)
	 */
	@Transactional(readOnly = true)
	public List<Concept> getAllConcepts(String sortBy, boolean asc, boolean includeRetired) throws APIException {
		if (sortBy == null) {
			sortBy = "conceptId";
		}
		
		return dao.getAllConcepts(sortBy, asc, includeRetired);
	}
	
	/**
	 * @deprecated use {@link #getAllConcepts(String, boolean, boolean)}
	 */
	@Deprecated
	@Transactional(readOnly = true)
	public List<Concept> getConcepts(String sortBy, String dir) throws APIException {
		boolean asc = true ? "asc".equals(dir) : !"asc".equals(dir);
		return Context.getConceptService().getAllConcepts(sortBy, asc, true);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getConceptsByName(java.lang.String)
	 */
	@Transactional(readOnly = true)
	public List<Concept> getConceptsByName(String name) throws APIException {
		return getConcepts(name, Context.getLocale(), true, null, null);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getConceptByName(java.lang.String)
	 */
	@Transactional(readOnly = true)
	public Concept getConceptByName(String name) {
		if (!StringUtils.hasText(name)) {
			return null;
		}
		return dao.getConceptByName(name);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getConceptByIdOrName(java.lang.String)
	 * @deprecated use {@link #getConcept(String)}
	 */
	@Deprecated
	@Transactional(readOnly = true)
	public Concept getConceptByIdOrName(String idOrName) {
		return Context.getConceptService().getConcept(idOrName);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getConcept(java.lang.String)
	 */
	@Transactional(readOnly = true)
	public Concept getConcept(String conceptIdOrName) {
		Concept c = null;
		Integer conceptId = null;
		try {
			conceptId = Integer.valueOf(conceptIdOrName);
		}
		catch (NumberFormatException nfe) {
			conceptId = null;
		}
		
		if (conceptId != null) {
			c = Context.getConceptService().getConcept(conceptId);
		} else {
			c = Context.getConceptService().getConceptByName(conceptIdOrName);
		}
		return c;
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
		if (classes == null) {
			classes = new Vector<ConceptClass>();
		}
		if (datatypes == null) {
			datatypes = new Vector<ConceptDatatype>();
		}
		
		return dao.getConcepts(name, loc, searchOnPhrase, classes, datatypes);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getDrug(java.lang.String)
	 */
	@Transactional(readOnly = true)
	public Drug getDrug(String drugNameOrId) {
		Integer drugId = null;
		
		try {
			drugId = Integer.valueOf(drugNameOrId);
		}
		catch (NumberFormatException nfe) {
			drugId = null;
		}
		
		if (drugId != null) {
			return Context.getConceptService().getDrug(drugId);
		} else {
			List<Drug> drugs = dao.getDrugs(drugNameOrId, null, false);
			if (drugs.size() > 1) {
				log.warn("more than one drug name returned with name:" + drugNameOrId);
			}
			if (drugs.size() == 0) {
				return null;
			}
			return drugs.get(0);
		}
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getDrugByNameOrId(java.lang.String)
	 * @deprecated use {@link #getDrug(String)}
	 */
	@Deprecated
	@Transactional(readOnly = true)
	public Drug getDrugByNameOrId(String drugNameOrId) {
		return Context.getConceptService().getDrug(drugNameOrId);
	}
	
	/**
	 * @deprecated use {@link #getAllDrugs()}
	 */
	@Deprecated
	@Transactional(readOnly = true)
	public List<Drug> getDrugs() {
		return Context.getConceptService().getAllDrugs();
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getAllDrugs()
	 */
	@Transactional(readOnly = true)
	public List<Drug> getAllDrugs() {
		return Context.getConceptService().getAllDrugs(true);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getAllDrugs(boolean)
	 */
	@Transactional(readOnly = true)
	public List<Drug> getAllDrugs(boolean includeRetired) {
		return dao.getDrugs(null, null, includeRetired);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getDrugs(org.openmrs.Concept)
	 * @deprecated use {@link #getDrugsByConcept(Concept)}
	 */
	@Deprecated
	@Transactional(readOnly = true)
	public List<Drug> getDrugs(Concept concept) {
		return Context.getConceptService().getDrugsByConcept(concept);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getDrugsByConcept(org.openmrs.Concept)
	 */
	@Transactional(readOnly = true)
	public List<Drug> getDrugsByConcept(Concept concept) {
		return dao.getDrugs(null, concept, false);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getDrugs(Concept)
	 * @deprecated Use {@link #getDrugsByConcept(Concept)}
	 */
	@Deprecated
	@Transactional(readOnly = true)
	public List<Drug> getDrugs(Concept concept, boolean includeRetired) {
		if (includeRetired) {
			throw new APIException("ConceptDrug.retired.getting", (Object[]) null);
		}
		
		return Context.getConceptService().getDrugsByConcept(concept);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getAllDrugs(boolean)
	 * @deprecated Use {@link #getAllDrugs(boolean)}
	 */
	@Deprecated
	@Transactional(readOnly = true)
	public List<Drug> getDrugs(boolean includeVoided) {
		return Context.getConceptService().getAllDrugs(includeVoided);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#findDrugs(java.lang.String, boolean)
	 * @deprecated Use {@link #getDrugs(String)}
	 */
	@Deprecated
	@Transactional(readOnly = true)
	public List<Drug> findDrugs(String phrase, boolean includeRetired) {
		if (includeRetired) {
			throw new APIException("ConceptDrug.retired.getting", (Object[]) null);
		}
		
		return Context.getConceptService().getDrugs(phrase);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getDrugs(java.lang.String)
	 */
	@Transactional(readOnly = true)
	public List<Drug> getDrugs(String phrase) {
		List<Drug> drugs = new ArrayList<Drug>();
		// trying to treat search phrase as drug id
		try {
			Integer drugId = Integer.parseInt(phrase);
			Drug targetDrug = Context.getConceptService().getDrug(drugId);
			// if drug was found add it to result
			if (targetDrug != null) {
				drugs.add(targetDrug);
			}
		}
		catch (NumberFormatException e) {
			// do nothing
		}
		
		// also try to treat search phrase as drug concept id
		try {
			Integer conceptId = Integer.parseInt(phrase);
			Concept targetConcept = Context.getConceptService().getConcept(conceptId);
			if (targetConcept != null) {
				drugs.addAll(Context.getConceptService().getDrugsByConcept(targetConcept));
			}
		}
		catch (NumberFormatException e) {
			// do nothing
		}
		
		drugs.addAll(dao.getDrugs(phrase));
		return drugs;
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getConceptsByClass(org.openmrs.ConceptClass)
	 */
	@Transactional(readOnly = true)
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
	@Transactional(readOnly = true)
	public List<ConceptClass> getConceptClasses() {
		return Context.getConceptService().getAllConceptClasses(true);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getAllConceptClasses(boolean)
	 */
	@Transactional(readOnly = true)
	public List<ConceptClass> getAllConceptClasses(boolean includeRetired) {
		return dao.getAllConceptClasses(includeRetired);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getConceptClass(java.lang.Integer)
	 */
	@Transactional(readOnly = true)
	public ConceptClass getConceptClass(Integer i) {
		return dao.getConceptClass(i);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getConceptClassByName(java.lang.String)
	 */
	@Transactional(readOnly = true)
	public ConceptClass getConceptClassByName(String name) {
		List<ConceptClass> ccList = dao.getConceptClasses(name);
		if (ccList.size() > 1) {
			log.warn("More than one ConceptClass found with name: " + name);
		}
		if (ccList.size() == 1) {
			return ccList.get(0);
		}
		return null;
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getAllConceptClasses(boolean)
	 */
	@Transactional(readOnly = true)
	public List<ConceptClass> getAllConceptClasses() throws APIException {
		return Context.getConceptService().getAllConceptClasses(true);
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
	 * @see org.openmrs.api.ConceptService#purgeConceptNameTag(org.openmrs.ConceptNameTag)
	 */
	public void purgeConceptNameTag(ConceptNameTag cnt) {
		dao.deleteConceptNameTag(cnt);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#purgeConceptDatatype(org.openmrs.ConceptDatatype)
	 * @deprecated as of 1.9 because users should never delete datatypes, it could harm data and
	 *             other code expecting them to be here
	 */
	@Deprecated
	public void purgeConceptDatatype(ConceptDatatype cd) {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#saveConceptDatatype(org.openmrs.ConceptDatatype)
	 * @deprecated as of 1.9 because users should never change datatypes, it could harm data and
	 *             other code expecting them to be here
	 */
	@Deprecated
	public ConceptDatatype saveConceptDatatype(ConceptDatatype cd) throws APIException {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getAllConceptDatatypes()
	 * @deprecated use {@link #getAllConceptDatatypes()}
	 */
	@Deprecated
	@Transactional(readOnly = true)
	public List<ConceptDatatype> getConceptDatatypes() {
		return Context.getConceptService().getAllConceptDatatypes();
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getAllConceptDatatypes()
	 */
	@Transactional(readOnly = true)
	public List<ConceptDatatype> getAllConceptDatatypes() {
		return Context.getConceptService().getAllConceptDatatypes(true);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getAllConceptDatatypes(boolean)
	 */
	@Transactional(readOnly = true)
	public List<ConceptDatatype> getAllConceptDatatypes(boolean includeRetired) throws APIException {
		return dao.getAllConceptDatatypes(includeRetired);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getConceptDatatype(java.lang.Integer)
	 */
	@Transactional(readOnly = true)
	public ConceptDatatype getConceptDatatype(Integer i) {
		return dao.getConceptDatatype(i);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getConceptDatatypes(java.lang.String)
	 */
	@Transactional(readOnly = true)
	public List<ConceptDatatype> getConceptDatatypes(String name) {
		return dao.getConceptDatatypes(name);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getConceptDatatypeByName(java.lang.String)
	 */
	@Transactional(readOnly = true)
	public ConceptDatatype getConceptDatatypeByName(String name) {
		return dao.getConceptDatatypeByName(name);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getConceptSets(org.openmrs.Concept)
	 * @deprecated use {@link #getConceptSetsByConcept(Concept)}
	 */
	@Deprecated
	@Transactional(readOnly = true)
	public List<ConceptSet> getConceptSets(Concept c) {
		return Context.getConceptService().getConceptSetsByConcept(c);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getConceptSetsByConcept(org.openmrs.Concept)
	 */
	@Transactional(readOnly = true)
	public List<ConceptSet> getConceptSetsByConcept(Concept concept) throws APIException {
		return dao.getConceptSetsByConcept(concept);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getConceptsInSet(org.openmrs.Concept)
	 * @deprecated use {@link #getConceptsByConceptSet(Concept)}
	 */
	@Deprecated
	@Transactional(readOnly = true)
	public List<Concept> getConceptsInSet(Concept c) {
		return Context.getConceptService().getConceptsByConceptSet(c);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getConceptsInSet(org.openmrs.Concept)
	 */
	@Transactional(readOnly = true)
	public List<Concept> getConceptsByConceptSet(Concept c) {
		Set<Integer> alreadySeen = new HashSet<Integer>();
		List<Concept> ret = new ArrayList<Concept>();
		explodeConceptSetHelper(c, ret, alreadySeen);
		return ret;
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getSetsContainingConcept(org.openmrs.Concept)
	 */
	@Transactional(readOnly = true)
	public List<ConceptSet> getSetsContainingConcept(Concept concept) {
		if (concept.getConceptId() == null) {
			return Collections.emptyList();
		}
		
		return dao.getSetsContainingConcept(concept);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getConceptProposal(java.lang.Integer)
	 */
	@Transactional(readOnly = true)
	public ConceptProposal getConceptProposal(Integer conceptProposalId) {
		return dao.getConceptProposal(conceptProposalId);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getConceptProposals(boolean)
	 * @deprecated use {@link #getAllConceptProposals(boolean)}
	 */
	@Deprecated
	@Transactional(readOnly = true)
	public List<ConceptProposal> getConceptProposals(boolean includeCompleted) {
		return Context.getConceptService().getAllConceptProposals(includeCompleted);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getAllConceptProposals(boolean)
	 */
	@Transactional(readOnly = true)
	public List<ConceptProposal> getAllConceptProposals(boolean includeCompleted) {
		return dao.getAllConceptProposals(includeCompleted);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getConceptProposals(java.lang.String)
	 */
	@Transactional(readOnly = true)
	public List<ConceptProposal> getConceptProposals(String cp) {
		return dao.getConceptProposals(cp);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#findProposedConcepts(java.lang.String)
	 * @deprecated
	 */
	@Deprecated
	@Transactional(readOnly = true)
	public List<Concept> findProposedConcepts(String text) {
		return Context.getConceptService().getProposedConcepts(text);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getProposedConcepts(java.lang.String)
	 */
	@Transactional(readOnly = true)
	public List<Concept> getProposedConcepts(String text) {
		return dao.getProposedConcepts(text);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#proposeConcept(org.openmrs.ConceptProposal)
	 * @deprecated
	 */
	@Deprecated
	public void proposeConcept(ConceptProposal conceptProposal) {
		Context.getConceptService().saveConceptProposal(conceptProposal);
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
	 *      org.openmrs.Concept, java.util.locale)
	 */
	public Concept mapConceptProposalToConcept(ConceptProposal cp, Concept mappedConcept, Locale locale) throws APIException {
		
		if (cp.getState().equals(OpenmrsConstants.CONCEPT_PROPOSAL_REJECT)) {
			cp.rejectConceptProposal();
			Context.getConceptService().saveConceptProposal(cp);
			return null;
		}
		
		if (mappedConcept == null) {
			throw new APIException("Concept.mapped.illegal", (Object[]) null);
		}
		
		ConceptName conceptName = null;
		if (cp.getState().equals(OpenmrsConstants.CONCEPT_PROPOSAL_CONCEPT) || !StringUtils.hasText(cp.getFinalText())) {
			cp.setState(OpenmrsConstants.CONCEPT_PROPOSAL_CONCEPT);
			cp.setFinalText("");
		} else if (cp.getState().equals(OpenmrsConstants.CONCEPT_PROPOSAL_SYNONYM)) {
			
			checkIfLocked();
			
			String finalText = cp.getFinalText();
			conceptName = new ConceptName(finalText, null);
			conceptName.setConcept(mappedConcept);
			conceptName.setLocale(locale == null ? Context.getLocale() : locale);
			conceptName.setDateCreated(new Date());
			conceptName.setCreator(Context.getAuthenticatedUser());
			//If this is pre 1.9
			if (conceptName.getUuid() == null) {
				conceptName.setUuid(UUID.randomUUID().toString());
			}
			mappedConcept.addName(conceptName);
			mappedConcept.setChangedBy(Context.getAuthenticatedUser());
			mappedConcept.setDateChanged(new Date());
			ValidateUtil.validate(mappedConcept);
		}
		
		cp.setMappedConcept(mappedConcept);
		
		if (cp.getObsConcept() != null) {
			Obs ob = new Obs();
			ob.setEncounter(cp.getEncounter());
			ob.setConcept(cp.getObsConcept());
			ob.setValueCoded(cp.getMappedConcept());
			if (cp.getState().equals(OpenmrsConstants.CONCEPT_PROPOSAL_SYNONYM)) {
				ob.setValueCodedName(conceptName);
			}
			ob.setCreator(Context.getAuthenticatedUser());
			ob.setDateCreated(new Date());
			ob.setObsDatetime(cp.getEncounter().getEncounterDatetime());
			ob.setLocation(cp.getEncounter().getLocation());
			ob.setPerson(cp.getEncounter().getPatient());
			if (ob.getUuid() == null) {
				ob.setUuid(UUID.randomUUID().toString());
			}
			cp.setObs(ob);
		}
		
		return mappedConcept;
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#mapConceptProposalToConcept(org.openmrs.ConceptProposal,
	 *      org.openmrs.Concept)
	 */
	public Concept mapConceptProposalToConcept(ConceptProposal cp, Concept mappedConcept) throws APIException {
		return Context.getConceptService().mapConceptProposalToConcept(cp, mappedConcept, null);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#rejectConceptProposal(org.openmrs.ConceptProposal)
	 * @deprecated use {@link ConceptProposal#rejectConceptProposal()}
	 */
	@Deprecated
	public void rejectConceptProposal(ConceptProposal cp) {
		cp.rejectConceptProposal();
		Context.getConceptService().saveConceptProposal(cp);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#findMatchingConceptProposals(String text)
	 * @deprecated use {@link #getConceptProposals(String)}
	 */
	@Deprecated
	@Transactional(readOnly = true)
	public List<ConceptProposal> findMatchingConceptProposals(String text) {
		return Context.getConceptService().getConceptProposals(text);
	}
	
	/**
	 * @deprecated use {@link #getConceptsByAnswer(Concept)}
	 * @see org.openmrs.api.ConceptService#getQuestionsForAnswer(org.openmrs.Concept)
	 */
	@Deprecated
	@Transactional(readOnly = true)
	public List<Concept> getQuestionsForAnswer(Concept concept) {
		return Context.getConceptService().getConceptsByAnswer(concept);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getConceptsByAnswer(org.openmrs.Concept)
	 */
	@Transactional(readOnly = true)
	public List<Concept> getConceptsByAnswer(Concept concept) throws APIException {
		if (concept.getConceptId() == null) {
			return Collections.emptyList();
		}
		
		return dao.getConceptsByAnswer(concept);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getPrevConcept(org.openmrs.Concept)
	 */
	@Transactional(readOnly = true)
	public Concept getPrevConcept(Concept c) {
		return dao.getPrevConcept(c);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getNextConcept(org.openmrs.Concept)
	 */
	@Transactional(readOnly = true)
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
				if (p.startsWith(s)) {
					found = true;
				}
			}
			if (!found) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#checkIfLocked()
	 */
	@Transactional(readOnly = true)
	public void checkIfLocked() throws ConceptsLockedException {
		String locked = Context.getAdministrationService().getGlobalProperty(
		    OpenmrsConstants.GLOBAL_PROPERTY_CONCEPTS_LOCKED, "false");
		if ("true".equalsIgnoreCase(locked)) {
			throw new ConceptsLockedException();
		}
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getConceptsWithDrugsInFormulary()
	 */
	@Transactional(readOnly = true)
	public List<Concept> getConceptsWithDrugsInFormulary() {
		return dao.getConceptsWithDrugsInFormulary();
	}
	
	/**
	 * @see ConceptService#getMaxConceptId()
	 */
	@Transactional(readOnly = true)
	public Integer getMaxConceptId() {
		return dao.getMaxConceptId();
	}
	
	/**
	 * Utility method used by getConceptsInSet(Concept concept)
	 * 
	 * @param concept
	 * @param ret
	 * @param alreadySeen
	 */
	private void explodeConceptSetHelper(Concept concept, Collection<Concept> ret, Collection<Integer> alreadySeen) {
		if (alreadySeen.contains(concept.getConceptId())) {
			return;
		}
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
	@Transactional(readOnly = true)
	public ConceptNameTag getConceptNameTagByName(String tagName) {
		return dao.getConceptNameTagByName(tagName);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getLocalesOfConceptNames()
	 */
	@Transactional(readOnly = true)
	public Set<Locale> getLocalesOfConceptNames() {
		return dao.getLocalesOfConceptNames();
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getConceptSource(java.lang.Integer)
	 */
	@Transactional(readOnly = true)
	public ConceptSource getConceptSource(Integer conceptSourceId) {
		return dao.getConceptSource(conceptSourceId);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getAllConceptSources()
	 * @deprecated
	 */
	@Deprecated
	@Transactional(readOnly = true)
	public List<ConceptSource> getAllConceptSources() {
		// backwards compatible
		return Context.getConceptService().getAllConceptSources(true);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getAllConceptSources(java.lang.boolean)
	 */
	@Transactional(readOnly = true)
	public List<ConceptSource> getAllConceptSources(boolean includeRetired) {
		return dao.getAllConceptSources(includeRetired);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#purgeConceptSource(org.openmrs.ConceptSource)
	 */
	public ConceptSource purgeConceptSource(ConceptSource cs) throws APIException {
		
		return dao.deleteConceptSource(cs);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#retireConceptSource(org.openmrs.ConceptSource, String)
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
	@Transactional(readOnly = true)
	public Iterator<Concept> conceptIterator() {
		return dao.conceptIterator();
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getConceptByUuid(java.lang.String)
	 */
	@Transactional(readOnly = true)
	public Concept getConceptByUuid(String uuid) {
		return dao.getConceptByUuid(uuid);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getConceptClassByUuid(java.lang.String)
	 */
	@Transactional(readOnly = true)
	public ConceptClass getConceptClassByUuid(String uuid) {
		return dao.getConceptClassByUuid(uuid);
	}
	
	@Transactional(readOnly = true)
	public ConceptAnswer getConceptAnswerByUuid(String uuid) {
		return dao.getConceptAnswerByUuid(uuid);
	}
	
	@Transactional(readOnly = true)
	public ConceptName getConceptNameByUuid(String uuid) {
		return dao.getConceptNameByUuid(uuid);
	}
	
	public ConceptSet getConceptSetByUuid(String uuid) {
		return dao.getConceptSetByUuid(uuid);
	}
	
	@Transactional(readOnly = true)
	public ConceptSource getConceptSourceByUuid(String uuid) {
		return dao.getConceptSourceByUuid(uuid);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getConceptDatatypeByUuid(java.lang.String)
	 */
	@Transactional(readOnly = true)
	public ConceptDatatype getConceptDatatypeByUuid(String uuid) {
		return dao.getConceptDatatypeByUuid(uuid);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getConceptNumericByUuid(java.lang.String)
	 */
	@Transactional(readOnly = true)
	public ConceptNumeric getConceptNumericByUuid(String uuid) {
		return dao.getConceptNumericByUuid(uuid);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getConceptProposalByUuid(java.lang.String)
	 */
	@Transactional(readOnly = true)
	public ConceptProposal getConceptProposalByUuid(String uuid) {
		return dao.getConceptProposalByUuid(uuid);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getDrugByUuid(java.lang.String)
	 */
	@Transactional(readOnly = true)
	public Drug getDrugByUuid(String uuid) {
		return dao.getDrugByUuid(uuid);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getConceptDescriptionByUuid(java.lang.String)
	 */
	@Transactional(readOnly = true)
	public ConceptDescription getConceptDescriptionByUuid(String uuid) {
		return dao.getConceptDescriptionByUuid(uuid);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getConceptNameTagByUuid(java.lang.String)
	 */
	@Transactional(readOnly = true)
	public ConceptNameTag getConceptNameTagByUuid(String uuid) {
		return dao.getConceptNameTagByUuid(uuid);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getAllConceptNameTags()
	 */
	@Transactional(readOnly = true)
	public List<ConceptNameTag> getAllConceptNameTags() {
		return dao.getAllConceptNameTags();
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getConceptNameTag(java.lang.Integer)
	 */
	@Transactional(readOnly = true)
	public ConceptNameTag getConceptNameTag(Integer id) {
		return dao.getConceptNameTag(id);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getConceptByMapping(java.lang.String, java.lang.String)
	 */
	@Transactional(readOnly = true)
	public Concept getConceptByMapping(String code, String sourceName) throws APIException {
		return Context.getConceptService().getConceptByMapping(code, sourceName, true);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getConceptByMapping(java.lang.String, java.lang.String,
	 *      java.lang.Boolean)
	 */
	@Transactional(readOnly = true)
	public Concept getConceptByMapping(String code, String sourceName, Boolean includeRetired) throws APIException {
		List<Concept> concepts = Context.getConceptService().getConceptsByMapping(code, sourceName, includeRetired);
		
		if (concepts.size() == 0) {
			return null;
		}
		// we want to throw an exception if there is more than one non-retired concept; 
		// since the getConceptByMapping DAO method returns a list with all non-retired concept
		// sorted to the front of the list, we can test if there is more than one retired concept
		// by testing if the second concept in the list is retired or not
		else if (concepts.size() > 1 && !concepts.get(1).isRetired()) {
			throw new APIException("Concept.error.multiple.non.retired", new Object[] { code, sourceName });
		} else {
			return concepts.get(0);
		}
	}
	
	/**
	 * @see org.openmrs.api.ConceptService
	 * @getConceptsByMapping(java.lang.String, java.lang.String)
	 */
	@Transactional(readOnly = true)
	public List<Concept> getConceptsByMapping(String code, String sourceName) throws APIException {
		return Context.getConceptService().getConceptsByMapping(code, sourceName, true);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService
	 * @getConceptsByMapping(java.lang.String, java.lang.String, boolean)
	 */
	@Transactional(readOnly = true)
	public List<Concept> getConceptsByMapping(String code, String sourceName, boolean includeRetired) throws APIException {
		return dao.getConceptsByMapping(code, sourceName, includeRetired);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getFalseConcept()
	 */
	@Override
	@Transactional(readOnly = true)
	public Concept getFalseConcept() {
		if (falseConcept == null) {
			setBooleanConcepts();
		}
		
		return falseConcept;
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getTrueConcept()
	 */
	@Override
	@Transactional(readOnly = true)
	public Concept getTrueConcept() {
		if (trueConcept == null) {
			setBooleanConcepts();
		}
		
		return trueConcept;
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getUnknownConcept()
	 */
	@Override
	@Transactional(readOnly = true)
	public Concept getUnknownConcept() {
		if (unknownConcept == null) {
			try {
				ConceptServiceImpl.setStaticUnknownConcept(Context.getConceptService().getConcept(
				    Integer.parseInt(Context.getAdministrationService().getGlobalProperty(
				        OpenmrsConstants.GLOBAL_PROPERTY_UNKNOWN_CONCEPT))));
			}
			catch (NumberFormatException e) {
				log.warn("Concept id for unknown concept should be a number");
			}
		}
		
		return unknownConcept;
	}
	
	/**
	 * Sets unknownConcept using static method
	 *
	 * @param currentUnknownConcept
	 */
	private static void setStaticUnknownConcept(Concept currentUnknownConcept) {
		ConceptServiceImpl.unknownConcept = currentUnknownConcept;
	}
	
	/**
	 * Sets the TRUE and FALSE concepts by reading their ids from the global_property table
	 */
	private void setBooleanConcepts() {
		
		try {
			trueConcept = Context.getConceptService().getConcept(
			    Integer.parseInt(Context.getAdministrationService().getGlobalProperty(
			        OpenmrsConstants.GLOBAL_PROPERTY_TRUE_CONCEPT)));
			falseConcept = Context.getConceptService().getConcept(
			    Integer.parseInt(Context.getAdministrationService().getGlobalProperty(
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
	@Transactional(readOnly = true)
	public List<ConceptMap> getConceptsByConceptSource(ConceptSource conceptSource) throws APIException {
		return Context.getConceptService().getConceptMappingsToSource(conceptSource);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getConceptSourceByName(java.lang.String)
	 */
	@Transactional(readOnly = true)
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
		if (concept.getId() != null && hasAnyObservation(concept) && hasDatatypeChanged(concept)) {
			// allow boolean concepts to be converted to coded
			if (!(dao.getSavedConceptDatatype(concept).isBoolean() && concept.getDatatype().isCoded())) {
				throw new ConceptInUseException();
			}
			if (log.isDebugEnabled()) {
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
	@Transactional(readOnly = true)
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
			if (!conceptToChange.getDatatype().isBoolean()) {
				throw new APIException("Concept.datatype.invalid", (Object[]) null);
			}
			
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
	@Transactional(readOnly = true)
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
	@Transactional(readOnly = true)
	public List<ConceptSearchResult> findConceptAnswers(String phrase, Locale locale, Concept concept) throws APIException {
		
		List<ConceptSearchResult> concepts = getConcepts(phrase, Arrays.asList(locale), false, null, null, null, null,
		    concept, null, null);
		
		return concepts;
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getConceptStopWords(java.util.Locale)
	 */
	@Transactional(readOnly = true)
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
			if ("Duplicate ConceptStopWord Entry".equalsIgnoreCase(e.getMessage())) {
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
	@Transactional(readOnly = true)
	public List<ConceptStopWord> getAllConceptStopWords() {
		return dao.getAllConceptStopWords();
	}
	
	/**
	 * @see ConceptService#getConcepts(String, List, boolean, List, List, List, List, Concept,
	 *      Integer, Integer)
	 */
	@Override
	@Transactional(readOnly = true)
	public List<ConceptSearchResult> getConcepts(String phrase, List<Locale> locales, boolean includeRetired,
	        List<ConceptClass> requireClasses, List<ConceptClass> excludeClasses, List<ConceptDatatype> requireDatatypes,
	        List<ConceptDatatype> excludeDatatypes, Concept answersToConcept, Integer start, Integer size)
	        throws APIException {
		
		if (requireClasses == null) {
			requireClasses = new Vector<ConceptClass>();
		}
		if (excludeClasses == null) {
			excludeClasses = new Vector<ConceptClass>();
		}
		if (requireDatatypes == null) {
			requireDatatypes = new Vector<ConceptDatatype>();
		}
		if (excludeDatatypes == null) {
			excludeDatatypes = new Vector<ConceptDatatype>();
		}
		
		return dao.getConcepts(phrase, locales, includeRetired, requireClasses, excludeClasses, requireDatatypes,
		    excludeDatatypes, answersToConcept, start, size);
		
	}
	
	/**
	 * @see ConceptService#updateConceptIndexes(Integer, Integer)
	 */
	@Override
	public void updateConceptIndexes(Integer conceptIdStart, Integer conceptIdEnd) throws APIException {
		Integer i = conceptIdStart;
		ConceptService cs = Context.getConceptService();
		while (i++ <= conceptIdEnd) {
			Concept concept = cs.getConcept(i);
			if (concept != null) {
				Context.updateSearchIndexForObject(concept);
			}
		}
	}
	
	/**
	 * @see ConceptService#updateConceptIndex(Concept)
	 */
	@Override
	public void updateConceptIndex(Concept concept) throws APIException {
		Context.updateSearchIndexForObject(concept);
	}
	
	/**
	 * @see ConceptService#updateConceptIndexes()
	 */
	@Override
	public void updateConceptIndexes() throws APIException {
		Context.updateSearchIndexForType(ConceptName.class);
	}
	
	/**
	 * @see ConceptService#getCountOfConcepts(String, List, boolean, List, List, List, List,
	 *      Concept)
	 */
	@Override
	@Transactional(readOnly = true)
	public Integer getCountOfConcepts(String phrase, List<Locale> locales, boolean includeRetired,
	        List<ConceptClass> requireClasses, List<ConceptClass> excludeClasses, List<ConceptDatatype> requireDatatypes,
	        List<ConceptDatatype> excludeDatatypes, Concept answersToConcept) {
		if (requireClasses == null) {
			requireClasses = new Vector<ConceptClass>();
		}
		if (excludeClasses == null) {
			excludeClasses = new Vector<ConceptClass>();
		}
		if (requireDatatypes == null) {
			requireDatatypes = new Vector<ConceptDatatype>();
		}
		if (excludeDatatypes == null) {
			excludeDatatypes = new Vector<ConceptDatatype>();
		}
		
		return dao.getCountOfConcepts(phrase, locales, includeRetired, requireClasses, excludeClasses, requireDatatypes,
		    excludeDatatypes, answersToConcept);
	}
	
	/**
	 * @see ConceptService#getCountOfDrugs(String, Concept, boolean, boolean, boolean)
	 */
	@Transactional(readOnly = true)
	public Integer getCountOfDrugs(String drugName, Concept concept, boolean searchOnPhrase, boolean searchDrugConceptNames,
	        boolean includeRetired) throws APIException {
		return OpenmrsUtil.convertToInteger(dao.getCountOfDrugs(drugName, concept, searchOnPhrase, searchDrugConceptNames,
		    includeRetired));
	}
	
	/**
	 * @see ConceptService#getDrugs(String, Concept, boolean, boolean, boolean, Integer, Integer)
	 */
	@Transactional(readOnly = true)
	public List<Drug> getDrugs(String drugName, Concept concept, boolean searchOnPhrase, boolean searchDrugConceptNames,
	        boolean includeRetired, Integer start, Integer length) throws APIException {
		return dao.getDrugs(drugName, concept, searchOnPhrase, searchDrugConceptNames, includeRetired, start, length);
	}
	
	/**
	 * @see ConceptService#getConcepts(String, Locale, boolean)
	 */
	@Override
	@Transactional(readOnly = true)
	public List<ConceptSearchResult> getConcepts(String phrase, Locale locale, boolean includeRetired) throws APIException {
		List<Locale> locales = new Vector<Locale>();
		if (locale != null) {
			locales.add(locale);
		}
		
		return Context.getConceptService().getConcepts(phrase, locales, includeRetired, null, null, null, null, null, null,
		    null);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getDrugsByIngredient(org.openmrs.Concept)
	 */
	@Override
	@Transactional(readOnly = true)
	public List<Drug> getDrugsByIngredient(Concept ingredient) throws APIException {
		if (ingredient == null) {
			throw new IllegalArgumentException("ingredient is required");
		}
		
		return dao.getDrugsByIngredient(ingredient);
	}
	
	/**
	 * @see ConceptService#getConceptMappingsToSource(ConceptSource)
	 */
	@Override
	@Transactional(readOnly = true)
	public List<ConceptMap> getConceptMappingsToSource(ConceptSource conceptSource) throws APIException {
		return dao.getConceptMapsBySource(conceptSource);
	}
	
	/**
	 * @see ConceptService#getActiveConceptMapTypes()
	 */
	@Override
	@Transactional(readOnly = true)
	public List<ConceptMapType> getActiveConceptMapTypes() throws APIException {
		return Context.getConceptService().getConceptMapTypes(true, false);
	}
	
	/**
	 * @see ConceptService#getConceptMapTypes(boolean, boolean)
	 */
	@Override
	@Transactional(readOnly = true)
	public List<ConceptMapType> getConceptMapTypes(boolean includeRetired, boolean includeHidden) throws APIException {
		return dao.getConceptMapTypes(includeRetired, includeHidden);
	}
	
	/**
	 * @see ConceptService#getConceptMapType(Integer)
	 */
	@Override
	@Transactional(readOnly = true)
	public ConceptMapType getConceptMapType(Integer conceptMapTypeId) throws APIException {
		return dao.getConceptMapType(conceptMapTypeId);
	}
	
	/**
	 * @see ConceptService#getConceptMapTypeByUuid(String)
	 */
	@Override
	@Transactional(readOnly = true)
	public ConceptMapType getConceptMapTypeByUuid(String uuid) throws APIException {
		return dao.getConceptMapTypeByUuid(uuid);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getConceptMapTypeByName(java.lang.String)
	 */
	@Override
	@Transactional(readOnly = true)
	public ConceptMapType getConceptMapTypeByName(String name) throws APIException {
		return dao.getConceptMapTypeByName(name);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#saveConceptMapType(org.openmrs.ConceptMapType)
	 */
	@Override
	public ConceptMapType saveConceptMapType(ConceptMapType conceptMapType) throws APIException {
		return dao.saveConceptMapType(conceptMapType);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#retireConceptMapType(org.openmrs.ConceptMapType,
	 *      java.lang.String)
	 */
	@Override
	public ConceptMapType retireConceptMapType(ConceptMapType conceptMapType, String retireReason) throws APIException {
		if (!StringUtils.hasText(retireReason)) {
			retireReason = Context.getMessageSourceService().getMessage("general.default.retireReason");
		}
		conceptMapType.setRetireReason(retireReason);
		return dao.saveConceptMapType(conceptMapType);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#unretireConceptMapType(org.openmrs.ConceptMapType)
	 */
	@Override
	public ConceptMapType unretireConceptMapType(ConceptMapType conceptMapType) throws APIException {
		return dao.saveConceptMapType(conceptMapType);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#purgeConceptMapType(org.openmrs.ConceptMapType)
	 */
	@Override
	public void purgeConceptMapType(ConceptMapType conceptMapType) throws APIException {
		if (dao.isConceptMapTypeInUse(conceptMapType)) {
			throw new APIException("ConceptMapType.inUse", (Object[]) null);
		}
		dao.deleteConceptMapType(conceptMapType);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getAllConceptReferenceTerms()
	 */
	@Override
	@Transactional(readOnly = true)
	public List<ConceptReferenceTerm> getAllConceptReferenceTerms() throws APIException {
		return Context.getConceptService().getConceptReferenceTerms(true);
	}
	
	/**
	 * @see ConceptService#getConceptReferenceTerms(boolean)
	 */
	@Override
	@Transactional(readOnly = true)
	public List<ConceptReferenceTerm> getConceptReferenceTerms(boolean includeRetired) throws APIException {
		return dao.getConceptReferenceTerms(includeRetired);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getConceptReferenceTerm(java.lang.Integer)
	 */
	@Override
	@Transactional(readOnly = true)
	public ConceptReferenceTerm getConceptReferenceTerm(Integer conceptReferenceTermId) throws APIException {
		return dao.getConceptReferenceTerm(conceptReferenceTermId);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getConceptReferenceTermByUuid(java.lang.String)
	 */
	@Override
	@Transactional(readOnly = true)
	public ConceptReferenceTerm getConceptReferenceTermByUuid(String uuid) throws APIException {
		return dao.getConceptReferenceTermByUuid(uuid);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getConceptReferenceTermByName(java.lang.String,
	 *      org.openmrs.ConceptSource)
	 */
	@Override
	@Transactional(readOnly = true)
	public ConceptReferenceTerm getConceptReferenceTermByName(String name, ConceptSource conceptSource) throws APIException {
		//On addition of extra attributes to concept maps, terms that were generated from existing maps have 
		//empty string values for the name property, ignore the search when name is an empty string but allow 
		//white space characters
		if (!StringUtils.hasLength(name)) {
			return null;
		}
		return dao.getConceptReferenceTermByName(name, conceptSource);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getConceptReferenceTermByCode(java.lang.String,
	 *      org.openmrs.ConceptSource)
	 */
	@Override
	@Transactional(readOnly = true)
	public ConceptReferenceTerm getConceptReferenceTermByCode(String code, ConceptSource conceptSource) throws APIException {
		return dao.getConceptReferenceTermByCode(code, conceptSource);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#saveConceptReferenceTerm(org.openmrs.ConceptReferenceTerm)
	 */
	@Override
	public ConceptReferenceTerm saveConceptReferenceTerm(ConceptReferenceTerm conceptReferenceTerm) throws APIException {
		return dao.saveConceptReferenceTerm(conceptReferenceTerm);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#retireConceptReferenceTerm(ConceptReferenceTerm, String)
	 */
	@Override
	public ConceptReferenceTerm retireConceptReferenceTerm(ConceptReferenceTerm conceptReferenceTerm, String retireReason)
	        throws APIException {
		if (!StringUtils.hasText(retireReason)) {
			retireReason = Context.getMessageSourceService().getMessage("general.default.retireReason");
		}
		conceptReferenceTerm.setRetireReason(retireReason);
		return dao.saveConceptReferenceTerm(conceptReferenceTerm);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#unretireConceptReferenceTerm(org.openmrs.ConceptReferenceTerm)
	 */
	@Override
	public ConceptReferenceTerm unretireConceptReferenceTerm(ConceptReferenceTerm conceptReferenceTerm) throws APIException {
		return dao.saveConceptReferenceTerm(conceptReferenceTerm);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#purgeConceptReferenceTerm(org.openmrs.ConceptReferenceTerm)
	 */
	@Override
	public void purgeConceptReferenceTerm(ConceptReferenceTerm conceptReferenceTerm) throws APIException {
		if (dao.isConceptReferenceTermInUse(conceptReferenceTerm)) {
			throw new APIException("ConceptRefereceTerm.inUse", (Object[]) null);
		}
		dao.deleteConceptReferenceTerm(conceptReferenceTerm);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getConceptReferenceTerms(java.lang.String,
	 *      org.openmrs.ConceptSource, java.lang.Integer, java.lang.Integer, boolean)
	 */
	@Override
	@Transactional(readOnly = true)
	public List<ConceptReferenceTerm> getConceptReferenceTerms(String query, ConceptSource conceptSource, Integer start,
	        Integer length, boolean includeRetired) throws APIException {
		if (length == null) {
			length = 10000;
		}
		return dao.getConceptReferenceTerms(query, conceptSource, start, length, includeRetired);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getCountOfConceptReferenceTerms(String, ConceptSource,
	 *      boolean)
	 */
	@Override
	@Transactional(readOnly = true)
	public Integer getCountOfConceptReferenceTerms(String query, ConceptSource conceptSource, boolean includeRetired) {
		return OpenmrsUtil.convertToInteger(dao.getCountOfConceptReferenceTerms(query, conceptSource, includeRetired));
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getReferenceTermMappingsTo(ConceptReferenceTerm)
	 */
	@Override
	@Transactional(readOnly = true)
	public List<ConceptReferenceTermMap> getReferenceTermMappingsTo(ConceptReferenceTerm term) throws APIException {
		return dao.getReferenceTermMappingsTo(term);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getConceptsByName(java.lang.String, java.util.Locale,
	 *      java.lang.Boolean)
	 */
	@Override
	@Transactional(readOnly = true)
	public List<Concept> getConceptsByName(String name, Locale locale, Boolean exactLocale) throws APIException {
		return dao.getConceptsByName(name, locale, exactLocale);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getDefaultConceptMapType()
	 */
	@Override
	@Transactional(readOnly = true)
	public ConceptMapType getDefaultConceptMapType() throws APIException {
		//We need to fetch it in DAO since it must be done in the MANUAL fush mode to prevent pre-mature flushes.
		return dao.getDefaultConceptMapType();
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#isConceptNameDuplicate(org.openmrs.ConceptName)
	 */
	@Override
	public boolean isConceptNameDuplicate(ConceptName name) {
		return dao.isConceptNameDuplicate(name);
	}
	
	/**
	 * @see ConceptService#getDrugs(String, java.util.Locale, boolean, boolean)
	 */
	@Override
	@Transactional(readOnly = true)
	public List<Drug> getDrugs(String searchPhrase, Locale locale, boolean exactLocale, boolean includeRetired)
	        throws APIException {
		if (searchPhrase == null) {
			throw new IllegalArgumentException("searchPhrase is required");
		}
		return dao.getDrugs(searchPhrase, locale, exactLocale, includeRetired);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getDrugsByMapping(String, ConceptSource, Collection,
	 *      boolean)
	 */
	@Override
	@Transactional(readOnly = true)
	public List<Drug> getDrugsByMapping(String code, ConceptSource conceptSource,
	        Collection<ConceptMapType> withAnyOfTheseTypes, boolean includeRetired) throws APIException {
		
		if (conceptSource == null) {
			throw new APIException("ConceptSource.is.required", (Object[]) null);
		}
		if (withAnyOfTheseTypes == null) {
			withAnyOfTheseTypes = Collections.emptyList();
		}
		return dao.getDrugsByMapping(code, conceptSource, withAnyOfTheseTypes, includeRetired);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getDrugByMapping(String, org.openmrs.ConceptSource,
	 *      java.util.Collection
	 */
	@Override
	@Transactional(readOnly = true)
	public Drug getDrugByMapping(String code, ConceptSource conceptSource,
	        Collection<ConceptMapType> withAnyOfTheseTypesOrOrderOfPreference) throws APIException {
		if (conceptSource == null) {
			throw new APIException("ConceptSource.is.required", (Object[]) null);
		}
		if (withAnyOfTheseTypesOrOrderOfPreference == null) {
			withAnyOfTheseTypesOrOrderOfPreference = Collections.emptyList();
		}
		return dao.getDrugByMapping(code, conceptSource, withAnyOfTheseTypesOrOrderOfPreference);
	}
	
	/**
	 * @see org.openmrs.api.ConceptService#getOrderableConcepts(String, java.util.List, boolean,
	 *      Integer, Integer)
	 */
	@Override
	@Transactional(readOnly = true)
	public List<ConceptSearchResult> getOrderableConcepts(String phrase, List<Locale> locales, boolean includeRetired,
	        Integer start, Integer length) {
		List<ConceptClass> mappedClasses = getConceptClassesOfOrderTypes();
		if (mappedClasses.isEmpty()) {
			return Collections.emptyList();
		}
		if (locales == null) {
			locales = new ArrayList<Locale>();
			locales.add(Context.getLocale());
		}
		return dao.getConcepts(phrase, locales, false, mappedClasses, (List) Collections.emptyList(), (List) Collections
		        .emptyList(), (List) Collections.emptyList(), null, start, length);
	}
	
	private List<ConceptClass> getConceptClassesOfOrderTypes() {
		List<ConceptClass> mappedClasses = new ArrayList<ConceptClass>();
		AdministrationService administrationService = Context.getAdministrationService();
		List<List<Object>> result = administrationService.executeSQL(
		    "SELECT DISTINCT concept_class_id FROM order_type_class_map", true);
		for (List<Object> temp : result) {
			for (Object value : temp) {
				if (value != null) {
					mappedClasses.add(this.getConceptClass((Integer) value));
				}
			}
		}
		return mappedClasses;
	}
}
