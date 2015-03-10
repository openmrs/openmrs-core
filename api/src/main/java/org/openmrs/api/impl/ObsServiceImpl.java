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

import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;
import org.openmrs.Cohort;
import org.openmrs.Concept;
import org.openmrs.ConceptName;
import org.openmrs.Encounter;
import org.openmrs.Location;
import org.openmrs.MimeType;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.aop.RequiredDataAdvice;
import org.openmrs.api.APIException;
import org.openmrs.api.EncounterService;
import org.openmrs.api.ObsService;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.ObsDAO;
import org.openmrs.api.handler.SaveHandler;
import org.openmrs.obs.ComplexObsHandler;
import org.openmrs.util.OpenmrsClassLoader;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.OpenmrsConstants.PERSON_TYPE;
import org.openmrs.util.OpenmrsUtil;
import org.openmrs.util.PrivilegeConstants;
import org.springframework.transaction.annotation.Transactional;

/**
 * Default implementation of the Observation Service
 * 
 * @see org.openmrs.api.ObsService
 */
@Transactional
public class ObsServiceImpl extends BaseOpenmrsService implements ObsService {
	
	/**
	 * The data access object for the obs service
	 */
	protected ObsDAO dao;
	
	/**
	 * Report handlers that have been registered. This is filled via {@link #setHandlers(Map)} and
	 * spring's applicationContext-service.xml object
	 */
	private static Map<String, ComplexObsHandler> handlers = null;
	
	/**
	 * Default empty constructor for this obs service
	 */
	public ObsServiceImpl() {
	}
	
	/**
	 * @see org.openmrs.api.ObsService#setObsDAO(org.openmrs.api.db.ObsDAO)
	 */
	public void setObsDAO(ObsDAO dao) {
		this.dao = dao;
	}
	
	/**
	 * Clean up after this class. Set the static var to null so that the classloader can reclaim the
	 * space.
	 * 
	 * @see org.openmrs.api.impl.BaseOpenmrsService#onShutdown()
	 */
	@Override
	public void onShutdown() {
		setHandlers(null);
	}
	
	/**
	 * @see org.openmrs.api.ObsService#saveObs(org.openmrs.Obs, String)
	 */
	public Obs saveObs(Obs obs, String changeMessage) throws APIException {
		if (null != obs && null != obs.getConcept() && obs.getConcept().isComplex()
		        && null != obs.getComplexData().getData()) {
			// save or update complexData object on this obs
			// this is done before the database save so that the obs.valueComplex
			// can be filled in by the handler.
			ComplexObsHandler handler = getHandler(obs);
			if (null != handler) {
				handler.saveObs(obs);
			} else {
				throw new APIException("unknown.handler", new Object[] { obs.getConcept() });
			}
		}
		
		if (obs != null && obs.getObsId() == null) {
			Context.requirePrivilege(PrivilegeConstants.ADD_OBS);
			return dao.saveObs(obs);
		} else {
			Context.requirePrivilege(PrivilegeConstants.EDIT_OBS);
			
			if (changeMessage == null) {
				throw new APIException("Obs.error.ChangeMessage.required", (Object[]) null);
			}
			
			Encounter encounter = obs.getEncounter();
			if (encounter != null) {
				obs.setPerson(encounter.getPatient());
			}
			
			// get a copy of the passed in obs and save it to the
			// database. This allows us to create a new row and new obs_id
			// this method doesn't copy the obs_id
			Obs newObs = Obs.newInstance(obs);
			
			// unset any voided properties on the new obs
			newObs.setVoided(false);
			newObs.setVoidReason(null);
			newObs.setDateVoided(null);
			newObs.setVoidedBy(null);
			// unset the creation stats
			newObs.setCreator(null);
			newObs.setDateCreated(null);
			newObs.setPreviousVersion(obs);
			
			RequiredDataAdvice.recursivelyHandle(SaveHandler.class, newObs, changeMessage);
			
			// save the new row to the database with the changes that
			// have been made to it
			dao.saveObs(newObs);
			
			// void out the original observation to keep it around for
			// historical purposes
			try {
				Context.addProxyPrivilege(PrivilegeConstants.DELETE_OBS);
				
				// fetch a clean copy of this obs from the database so that
				// we don't write the changes to the database when we save
				// the fact that the obs is now voided
				Context.evictFromSession(obs);
				obs = Context.getObsService().getObs(obs.getObsId());
				
				// calling this via the service so that AOP hooks are called
				Context.getObsService().voidObs(obs, changeMessage);
				
			}
			finally {
				Context.removeProxyPrivilege(PrivilegeConstants.DELETE_OBS);
			}
			
			return newObs;
		}
	}
	
	/**
	 * @see org.openmrs.api.ObsService#getObs(java.lang.Integer)
	 */
	@Transactional(readOnly = true)
	public Obs getObs(Integer obsId) throws APIException {
		return dao.getObs(obsId);
	}
	
	/**
	 * @see org.openmrs.api.ObsService#updateObs(org.openmrs.Obs)
	 * @deprecated
	 */
	@Deprecated
	public void updateObs(Obs obs) throws APIException {
		Context.getObsService().saveObs(obs, obs.getVoidReason());
	}
	
	/**
	 * Voids an Obs If the Obs argument is an obsGroup, all group members will be voided.
	 * 
	 * @see org.openmrs.api.ObsService#voidObs(org.openmrs.Obs, java.lang.String)
	 * @param obs the Obs to void
	 * @param reason the void reason
	 * @throws APIException
	 */
	public Obs voidObs(Obs obs, String reason) throws APIException {
		return dao.saveObs(obs);
	}
	
	/**
	 * Unvoids an Obs
	 * <p>
	 * If the Obs argument is an obsGroup, all group members with the same dateVoided will also be
	 * unvoided.
	 * 
	 * @see org.openmrs.api.ObsService#unvoidObs(org.openmrs.Obs)
	 * @param obs the Obs to unvoid
	 * @return the unvoided Obs
	 * @throws APIException
	 */
	public Obs unvoidObs(Obs obs) throws APIException {
		return dao.saveObs(obs);
	}
	
	/**
	 * @see org.openmrs.api.ObsService#purgeObs(org.openmrs.Obs, boolean)
	 */
	public void purgeObs(Obs obs, boolean cascade) throws APIException {
		if (!purgeComplexData(obs)) {
			throw new APIException("Obs.error.unable.purge.complex.data", new Object[] { obs });
		}
		
		if (cascade) {
			throw new APIException("Obs.error.cascading.purge.not.implemented", (Object[]) null);
			// TODO delete any related objects here before deleting the obs
			// obsGroups objects?
			// orders?
		}
		
		dao.deleteObs(obs);
	}
	
	/**
	 * @see org.openmrs.api.ObsService#purgeObs(org.openmrs.Obs)
	 */
	public void purgeObs(Obs obs) throws APIException {
		Context.getObsService().purgeObs(obs, false);
	}
	
	/**
	 * @see org.openmrs.api.ObsService#getMimeTypes()
	 * @deprecated use {@link #getAllMimeTypes()}
	 */
	@Deprecated
	@Transactional(readOnly = true)
	public List<MimeType> getMimeTypes() throws APIException {
		return Context.getObsService().getAllMimeTypes();
	}
	
	/**
	 * @see org.openmrs.api.ObsService#getAllMimeTypes()
	 * @deprecated
	 */
	@Deprecated
	@Transactional(readOnly = true)
	public List<MimeType> getAllMimeTypes() throws APIException {
		return dao.getAllMimeTypes(true);
	}
	
	/**
	 * @see org.openmrs.api.ObsService#getAllMimeTypes(boolean)
	 * @deprecated
	 */
	@Deprecated
	@Transactional(readOnly = true)
	public List<MimeType> getAllMimeTypes(boolean includeRetired) {
		return dao.getAllMimeTypes(includeRetired);
	}
	
	/**
	 * @see org.openmrs.api.ObsService#saveMimeType(org.openmrs.MimeType)
	 * @deprecated
	 */
	@Deprecated
	public MimeType saveMimeType(MimeType mimeType) throws APIException {
		return dao.saveMimeType(mimeType);
	}
	
	/**
	 * @see org.openmrs.api.ObsService#voidMimeType(org.openmrs.MimeType, java.lang.String)
	 * @deprecated
	 */
	@Deprecated
	public MimeType voidMimeType(MimeType mimeType, String reason) throws APIException {
		throw new APIException("general.not.yet.implemented", (Object[]) null);
	}
	
	/**
	 * @see org.openmrs.api.ObsService#getMimeType(java.lang.Integer)
	 * @deprecated
	 */
	@Deprecated
	@Transactional(readOnly = true)
	public MimeType getMimeType(Integer mimeTypeId) throws APIException {
		return dao.getMimeType(mimeTypeId);
	}
	
	/**
	 * @see org.openmrs.api.ObsService#purgeMimeType(org.openmrs.MimeType)
	 * @deprecated
	 */
	@Deprecated
	public void purgeMimeType(MimeType mimeType) {
		dao.deleteMimeType(mimeType);
	}
	
	/**
	 * @see org.openmrs.api.ObsService#getObservations(java.util.List, java.util.List,
	 *      java.util.List, java.util.List, List, List, java.util.List, java.lang.Integer,
	 *      java.lang.Integer, java.util.Date, java.util.Date, boolean)
	 */
	@Transactional(readOnly = true)
	public List<Obs> getObservations(List<Person> whom, List<Encounter> encounters, List<Concept> questions,
	        List<Concept> answers, List<PERSON_TYPE> personTypes, List<Location> locations, List<String> sort,
	        Integer mostRecentN, Integer obsGroupId, Date fromDate, Date toDate, boolean includeVoidedObs)
	        throws APIException {
		
		if (sort == null) {
			sort = new Vector<String>();
		}
		if (sort.isEmpty()) {
			sort.add("obsDatetime");
		}
		
		return dao.getObservations(whom, encounters, questions, answers, personTypes, locations, sort, mostRecentN,
		    obsGroupId, fromDate, toDate, includeVoidedObs, null);
	}
	
	/**
	 * @see org.openmrs.api.ObsService#getObservations(java.util.List, java.util.List,
	 *      java.util.List, java.util.List, List, List, java.util.List, java.lang.Integer,
	 *      java.lang.Integer, java.util.Date, java.util.Date, boolean, java.lang.String)
	 */
	@Transactional(readOnly = true)
	public List<Obs> getObservations(List<Person> whom, List<Encounter> encounters, List<Concept> questions,
	        List<Concept> answers, List<PERSON_TYPE> personTypes, List<Location> locations, List<String> sort,
	        Integer mostRecentN, Integer obsGroupId, Date fromDate, Date toDate, boolean includeVoidedObs,
	        String accessionNumber) throws APIException {
		
		if (sort == null) {
			sort = new Vector<String>();
		}
		if (sort.isEmpty()) {
			sort.add("obsDatetime");
		}
		
		return dao.getObservations(whom, encounters, questions, answers, personTypes, locations, sort, mostRecentN,
		    obsGroupId, fromDate, toDate, includeVoidedObs, accessionNumber);
	}
	
	/**
	 * @see org.openmrs.api.ObsService#getObservationCount(java.util.List, java.util.List,
	 *      java.util.List, java.util.List, java.util.List, java.util.List, java.lang.Integer,
	 *      java.util.Date, java.util.Date, boolean)
	 */
	@Transactional(readOnly = true)
	public Integer getObservationCount(List<Person> whom, List<Encounter> encounters, List<Concept> questions,
	        List<Concept> answers, List<PERSON_TYPE> personTypes, List<Location> locations, Integer obsGroupId,
	        Date fromDate, Date toDate, boolean includeVoidedObs) throws APIException {
		return OpenmrsUtil.convertToInteger(dao.getObservationCount(whom, encounters, questions, answers, personTypes,
		    locations, obsGroupId, fromDate, toDate, null, includeVoidedObs, null));
	}
	
	/**
	 * @see org.openmrs.api.ObsService#getObservationCount(java.util.List, java.util.List,
	 *      java.util.List, java.util.List, java.util.List, java.util.List, java.lang.Integer,
	 *      java.util.Date, java.util.Date, boolean, java.lang.String)
	 */
	@Transactional(readOnly = true)
	public Integer getObservationCount(List<Person> whom, List<Encounter> encounters, List<Concept> questions,
	        List<Concept> answers, List<PERSON_TYPE> personTypes, List<Location> locations, Integer obsGroupId,
	        Date fromDate, Date toDate, boolean includeVoidedObs, String accessionNumber) throws APIException {
		return OpenmrsUtil.convertToInteger(dao.getObservationCount(whom, encounters, questions, answers, personTypes,
		    locations, obsGroupId, fromDate, toDate, null, includeVoidedObs, accessionNumber));
	}
	
	/**
	 * This implementation queries the obs table comparing the given <code>searchString</code> with
	 * the patient's identifier, encounterId, and obsId
	 * 
	 * @see org.openmrs.api.ObsService#getObservations(java.lang.String)
	 */
	@Transactional(readOnly = true)
	public List<Obs> getObservations(String searchString) {
		
		// search on patient identifier
		PatientService ps = Context.getPatientService();
		List<Patient> patients = ps.getPatients(null, searchString, null, false);
		List<Person> persons = new Vector<Person>();
		persons.addAll(patients);
		
		// try to search on encounterId
		EncounterService es = Context.getEncounterService();
		List<Encounter> encounters = new Vector<Encounter>();
		try {
			Encounter e = es.getEncounter(Integer.valueOf(searchString));
			if (e != null) {
				encounters.add(e);
			}
		}
		catch (NumberFormatException e) {
			// pass
		}
		
		List<Obs> returnList = new Vector<Obs>();
		
		if (encounters.size() > 0 || persons.size() > 0) {
			returnList = Context.getObsService().getObservations(persons, encounters, null, null, null, null, null, null,
			    null, null, null, false);
		}
		
		// try to search on obsId
		try {
			Obs o = getObs(Integer.valueOf(searchString));
			if (o != null) {
				returnList.add(o);
			}
		}
		catch (NumberFormatException e) {
			// pass
		}
		
		return returnList;
	}
	
	/**
	 * @see org.openmrs.api.ObsService#createObs(org.openmrs.Obs)
	 * @deprecated
	 */
	@Deprecated
	public void createObs(Obs obs) throws APIException {
		Context.getObsService().saveObs(obs, null);
	}
	
	/**
	 * Correct use case:
	 * 
	 * <pre>
	 * Obs parent = new Obs();
	 * Obs child1 = new Obs();
	 * Obs child2 = new Obs();
	 * 
	 * parent.addGroupMember(child1);
	 * parent.addGroupMember(child2);
	 * </pre>
	 * 
	 * @deprecated This method should no longer need to be called on the api. This was meant as
	 *             temporary until we created a true ObsGroup pojo.
	 * @see org.openmrs.api.ObsService#createObsGroup(org.openmrs.Obs[])
	 */
	@Deprecated
	public void createObsGroup(Obs[] obs) throws APIException {
		if (obs == null || obs.length < 1) {
			return; // silently tolerate calls with missing/empty parameter
		}
		
		String conceptIdStr = Context.getAdministrationService().getGlobalProperty(
		    OpenmrsConstants.GLOBAL_PROPERTY_MEDICAL_RECORD_OBSERVATIONS, "1238");
		// fail silently if a default obs group is not defined
		if (conceptIdStr == null || conceptIdStr.length() == 0) {
			return;
		}
		
		Integer conceptId = Integer.valueOf(conceptIdStr);
		Concept defaultObsGroupConcept = Context.getConceptService().getConcept(conceptId);
		
		// if they defined a bad concept, bail
		if (defaultObsGroupConcept == null) {
			throw new APIException("no.concept.defined.with.id", new Object[] { conceptIdStr,
			        OpenmrsConstants.GLOBAL_PROPERTY_MEDICAL_RECORD_OBSERVATIONS });
		}
		
		Obs obsGroup = new Obs();
		obsGroup.setConcept(defaultObsGroupConcept);
		
		for (Obs member : obs) {
			obsGroup.addGroupMember(member);
		}
		
		Context.getObsService().updateObs(obsGroup);
	}
	
	/**
	 * @see org.openmrs.api.ObsService#deleteObs(org.openmrs.Obs)
	 * @deprecated use #purgeObs(Obs)
	 */
	@Deprecated
	public void deleteObs(Obs obs) throws APIException {
		Context.getObsService().purgeObs(obs);
	}
	
	/**
	 * @see org.openmrs.api.ObsService#getObservationsByPerson(org.openmrs.Person)
	 */
	@Transactional(readOnly = true)
	public List<Obs> getObservationsByPerson(Person who) {
		List<Person> whom = new Vector<Person>();
		whom.add(who);
		return Context.getObsService().getObservations(whom, null, null, null, null, null, null, null, null, null, null,
		    false);
	}
	
	/**
	 * @see org.openmrs.api.ObsService#getObservations(org.openmrs.Person, boolean includeVoided)
	 * @deprecated use {@link #getObservationsByPerson(Person)}
	 */
	@Deprecated
	@Transactional(readOnly = true)
	public Set<Obs> getObservations(Person who, boolean includeVoided) {
		if (includeVoided) {
			throw new APIException("Obs.error.voided.no.longer.allowed", (Object[]) null);
		}
		
		Set<Obs> obsSet = new HashSet<Obs>();
		obsSet.addAll(Context.getObsService().getObservationsByPerson(who));
		
		return obsSet;
	}
	
	/**
	 * @see org.openmrs.api.ObsService#getObservations(org.openmrs.Concept, org.openmrs.Location,
	 *      java.lang.String, java.lang.Integer, boolean includeVoided)
	 * @deprecated
	 */
	@Deprecated
	@Transactional(readOnly = true)
	public List<Obs> getObservations(Concept c, Location loc, String sort, Integer personType, boolean includeVoided) {
		List<Concept> questions = new Vector<Concept>();
		questions.add(c);
		List<Location> locations = new Vector<Location>();
		locations.add(loc);
		
		// make the sort list from the given sort string
		List<String> sortList = makeSortList(sort);
		
		return Context.getObsService().getObservations(null, null, questions, null, getPersonTypeEnumerations(personType),
		    locations, sortList, null, null, null, null, includeVoided);
	}
	
	/**
	 * Convenience method for turning a string like "location.locationId asc, obs.valueDatetime
	 * desc" into a list of strings to sort on
	 * 
	 * @param sort string
	 * @return simple list of strings to sort on without asc/desc
	 */
	private List<String> makeSortList(String sort) {
		List<String> sortList = new Vector<String>();
		if (StringUtils.isNotEmpty(sort)) {
			for (String sortPart : sort.split(",")) {
				
				sortPart = sortPart.trim();
				
				// split out the asc/desc part if applicable
				if (sortPart.contains(" ")) {
					sortPart = sortPart.substring(0, sortPart.indexOf(" "));
				}
				
				// add the current sort to the list of things to sort on
				if (!"".equals(sort)) {
					sortList.add(sortPart);
				}
			}
		}
		
		return sortList;
	}
	
	/**
	 * This method should be removed when all methods using an Integer personType are removed. This
	 * method does a bitwise compare on <code>personType</code> and returns a list of PERSON_TYPEs
	 * that are comparable
	 * 
	 * @param personType Integer corresponding to {@link ObsService#PERSON}, {@link ObsService#USER}
	 *            , or {@link ObsService#PATIENT},
	 * @return the enumeration that corresponds to the given integer (old way of doing it)
	 */
	@SuppressWarnings("deprecation")
	private List<PERSON_TYPE> getPersonTypeEnumerations(Integer personType) {
		List<PERSON_TYPE> personTypes = new Vector<PERSON_TYPE>();
		if (personType == null) {
			personTypes.add(PERSON_TYPE.PERSON);
			return personTypes;
		} else if ((personType & ObsService.PATIENT) == ObsService.PATIENT) {
			personTypes.add(PERSON_TYPE.PATIENT);
			return personTypes;
		} else if ((personType & ObsService.USER) == ObsService.USER) {
			personTypes.add(PERSON_TYPE.USER);
			return personTypes;
		} else {
			// default to an all-encompassing search
			return personTypes;
		}
	}
	
	/**
	 * @see org.openmrs.api.ObsService#getObservations(org.openmrs.Person, org.openmrs.Concept,
	 *      boolean)
	 * @deprecated
	 */
	@Deprecated
	@Transactional(readOnly = true)
	public Set<Obs> getObservations(Person who, Concept question, boolean includeVoided) {
		List<Obs> obs = Context.getObsService().getObservationsByPersonAndConcept(who, question);
		Set<Obs> obsSet = new HashSet<Obs>();
		obsSet.addAll(obs);
		return obsSet;
	}
	
	/**
	 * @see org.openmrs.api.ObsService#getObservationsByPersonAndConcept(org.openmrs.Person,
	 *      org.openmrs.Concept)
	 */
	@Transactional(readOnly = true)
	public List<Obs> getObservationsByPersonAndConcept(Person who, Concept question) throws APIException {
		List<Person> whom = new Vector<Person>();
		if (who != null && who.getPersonId() != null) {
			whom.add(who);
		}
		List<Concept> questions = new Vector<Concept>();
		questions.add(question);
		
		return Context.getObsService().getObservations(whom, null, questions, null, null, null, null, null, null, null,
		    null, false);
	}
	
	/**
	 * @see org.openmrs.api.ObsService#getLastNObservations(java.lang.Integer, org.openmrs.Person,
	 *      org.openmrs.Concept, boolean includeVoided)
	 * @deprecated
	 */
	@Deprecated
	@Transactional(readOnly = true)
	public List<Obs> getLastNObservations(Integer n, Person who, Concept question, boolean includeVoided) {
		List<Person> whom = new Vector<Person>();
		whom.add(who);
		List<Concept> questions = new Vector<Concept>();
		questions.add(question);
		
		return Context.getObsService().getObservations(whom, null, questions, null, null, null, null, n, null, null, null,
		    includeVoided);
	}
	
	/**
	 * @see org.openmrs.api.ObsService#getObservations(org.openmrs.Concept, java.lang.String,
	 *      java.lang.Integer, boolean includeVoided)
	 * @deprecated
	 */
	@Deprecated
	@Transactional(readOnly = true)
	public List<Obs> getObservations(Concept question, String sort, Integer personType, boolean includeVoided) {
		List<Concept> questions = new Vector<Concept>();
		questions.add(question);
		
		// make the sort list from the given sort string
		List<String> sortList = makeSortList(sort);
		
		return Context.getObsService().getObservations(null, null, questions, null, getPersonTypeEnumerations(personType),
		    null, sortList, null, null, null, null, includeVoided);
	}
	
	/**
	 * @see org.openmrs.api.ObsService#getObservationsAnsweredByConcept(org.openmrs.Concept,
	 *      java.lang.Integer, boolean includeVoided)
	 * @deprecated
	 */
	@Deprecated
	@Transactional(readOnly = true)
	public List<Obs> getObservationsAnsweredByConcept(Concept answer, Integer personType, boolean includeVoided) {
		List<Concept> answers = new Vector<Concept>();
		answers.add(answer);
		
		return Context.getObsService().getObservations(null, null, null, answers, getPersonTypeEnumerations(personType),
		    null, null, null, null, null, null, includeVoided);
	}
	
	/**
	 * @see org.openmrs.api.ObsService#getNumericAnswersForConcept(org.openmrs.Concept,
	 *      java.lang.Boolean, java.lang.Integer, boolean includeVoided)
	 * @deprecated
	 */
	@Deprecated
	@Transactional(readOnly = true)
	public List<Object[]> getNumericAnswersForConcept(Concept question, Boolean sortByValue, Integer personType,
	        boolean includeVoided) {
		List<String> sortList = new Vector<String>();
		if (sortByValue) {
			sortList.add("valueNumeric");
		}
		
		List<Concept> questions = new Vector<Concept>();
		questions.add(question);
		
		List<Obs> obs = Context.getObsService().getObservations(null, null, questions, null,
		    getPersonTypeEnumerations(personType), null, sortList, null, null, null, null, includeVoided);
		
		List<Object[]> returnList = new Vector<Object[]>();
		
		for (Obs o : obs) {
			returnList.add(new Object[] { o.getObsId(), o.getObsDatetime(), o.getValueNumeric() });
		}
		
		return returnList;
	}
	
	/**
	 * @see org.openmrs.api.ObsService#getObservations(org.openmrs.Encounter)
	 * @deprecated use org.openmrs.Encounter#getObs()
	 */
	@Deprecated
	@Transactional(readOnly = true)
	public Set<Obs> getObservations(Encounter whichEncounter) {
		return whichEncounter.getObs();
	}
	
	/**
	 * @see org.openmrs.api.ObsService#getVoidedObservations()
	 * @deprecated
	 */
	@Deprecated
	@Transactional(readOnly = true)
	public List<Obs> getVoidedObservations() {
		return Context.getObsService().getObservations(null, null, null, null, null, null, null, null, null, null, null,
		    true);
	}
	
	/**
	 * @see org.openmrs.api.ObsService#findObservations(java.lang.String, boolean,
	 *      java.lang.Integer)
	 * @deprecated
	 */
	@Deprecated
	@Transactional(readOnly = true)
	public List<Obs> findObservations(String search, boolean includeVoided, Integer personType) {
		// ignoring voided and personTypes now
		return Context.getObsService().getObservations(search);
	}
	
	/**
	 * @see org.openmrs.api.ObsService#findObsByGroupId(java.lang.Integer)
	 * @deprecated -- should use obs.getGroupMembers
	 */
	@Deprecated
	@Transactional(readOnly = true)
	public List<Obs> findObsByGroupId(Integer obsGroupId) {
		return Context.getObsService().getObservations(null, null, null, null, null, null, null, null, obsGroupId, null,
		    null, false);
	}
	
	/**
	 * @see org.openmrs.api.ObsService#getObsByUuid(java.lang.String)
	 */
	@Transactional(readOnly = true)
	public Obs getObsByUuid(String uuid) throws APIException {
		return dao.getObsByUuid(uuid);
	}
	
	/**
	 * @see org.openmrs.api.ObsService#getObservations(List, Date, Date, boolean)
	 * @deprecated
	 */
	@Deprecated
	@Transactional(readOnly = true)
	public List<Obs> getObservations(List<Concept> concepts, Date fromDate, Date toDate, boolean includeVoided) {
		return Context.getObsService().getObservations(null, null, concepts, null, null, null, null, null, null, fromDate,
		    toDate, includeVoided);
	}
	
	/**
	 * @see org.openmrs.api.ObsService#getObservations(List, Date, Date)
	 * @deprecated
	 */
	@Deprecated
	@Transactional(readOnly = true)
	public List<Obs> getObservations(List<Concept> concepts, Date fromDate, Date toDate) {
		return Context.getObsService().getObservations(null, null, concepts, null, null, null, null, null, null, fromDate,
		    toDate, false);
	}
	
	/**
	 * @see org.openmrs.api.ObsService#getObservations(Cohort, List, Date, Date)
	 * @deprecated
	 */
	@Deprecated
	@Transactional(readOnly = true)
	public List<Obs> getObservations(Cohort patients, List<Concept> concepts, Date fromDate, Date toDate) {
		List<Person> persons = new Vector<Person>();
		
		if (patients != null) {
			for (Integer memberId : patients.getMemberIds()) {
				persons.add(new Person(memberId));
			}
		}
		
		return Context.getObsService().getObservations(persons, null, concepts, null, null, null, null, null, null,
		    fromDate, toDate, false);
	}
	
	/**
	 * @see org.openmrs.api.ObsService#getComplexObs(Integer, String)
	 */
	@Transactional(readOnly = true)
	public Obs getComplexObs(Integer obsId, String view) throws APIException {
		Obs obs = dao.getObs(obsId);
		
		if (obs != null && obs.isComplex()) {
			return getHandler(obs).getObs(obs, view);
		}
		
		return obs;
	}
	
	/**
	 * Internal method to remove ComplexData when an Obs is purged.
	 */
	protected boolean purgeComplexData(Obs obs) throws APIException {
		if (obs.isComplex()) {
			ComplexObsHandler handler = getHandler(obs);
			if (null != handler) {
				return handler.purgeComplexData(obs);
			}
		}
		
		return true;
	}
	
	/**
	 * @see org.openmrs.api.ObsService#getHandler(org.openmrs.Obs)
	 */
	@Transactional(readOnly = true)
	public ComplexObsHandler getHandler(Obs obs) throws APIException {
		if (obs.getConcept().isComplex()) {
			// Get the ConceptComplex from the ConceptService then return its
			// handler.
			if (obs.getConcept() == null) {
				throw new APIException("Obs.error.unable.get.handler", new Object[] { obs });
			}
			
			String handlerString = Context.getConceptService().getConceptComplex(obs.getConcept().getConceptId())
			        .getHandler();
			
			if (handlerString == null) {
				throw new APIException("Obs.error.unable.get.handler.and.concept", new Object[] { obs, obs.getConcept() });
			}
			
			return this.getHandler(handlerString);
		}
		
		return null;
	}
	
	/**
	 * @see org.openmrs.api.ObsService#getHandler(java.lang.String)
	 */
	public ComplexObsHandler getHandler(String key) {
		return handlers.get(key);
	}
	
	/**
	 * @see org.openmrs.api.ObsService#setHandlers(Map)
	 * @see #registerHandler(String, ComplexObsHandler)
	 */
	public void setHandlers(Map<String, ComplexObsHandler> newHandlers) throws APIException {
		if (newHandlers == null) {
			ObsServiceImpl.setStaticHandlers(null);
			return;
		}
		for (Map.Entry<String, ComplexObsHandler> entry : newHandlers.entrySet()) {
			registerHandler(entry.getKey(), entry.getValue());
		}
	}
	
	/**
	 * Sets handlers using static method
	 *
	 * @param currentHandlers
	 */
	private static void setStaticHandlers(Map<String, ComplexObsHandler> currentHandlers) {
		ObsServiceImpl.handlers = currentHandlers;
	}
	
	/**
	 * @see org.openmrs.api.ObsService#getHandlers()
	 */
	public Map<String, ComplexObsHandler> getHandlers() throws APIException {
		if (handlers == null) {
			handlers = new LinkedHashMap<String, ComplexObsHandler>();
		}
		
		return handlers;
	}
	
	/**
	 * @see org.openmrs.api.ObsService#registerHandler(String, ComplexObsHandler)
	 */
	public void registerHandler(String key, ComplexObsHandler handler) throws APIException {
		getHandlers().put(key, handler);
	}
	
	/**
	 * @see org.openmrs.api.ObsService#registerHandler(String, String)
	 */
	@SuppressWarnings("unchecked")
	public void registerHandler(String key, String handlerClass) throws APIException {
		try {
			Class loadedClass = OpenmrsClassLoader.getInstance().loadClass(handlerClass);
			registerHandler(key, (ComplexObsHandler) loadedClass.newInstance());
			
		}
		catch (Exception e) {
			throw new APIException("unable.load.and.instantiate.handler", null, e);
		}
	}
	
	/**
	 * @see org.openmrs.api.ObsService#getObservationCount(java.util.List, boolean)
	 */
	@Override
	@Transactional(readOnly = true)
	public Integer getObservationCount(List<ConceptName> conceptNames, boolean includeVoided) {
		return OpenmrsUtil.convertToInteger(dao.getObservationCount(null, null, null, null, null, null, null, null, null,
		    conceptNames, true, null));
	}
	
	/**
	 * @see org.openmrs.api.ObsService#removeHandler(java.lang.String)
	 */
	public void removeHandler(String key) {
		handlers.remove(key);
	}
	
}
