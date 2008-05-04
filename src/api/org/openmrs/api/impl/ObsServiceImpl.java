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
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Location;
import org.openmrs.MimeType;
import org.openmrs.Obs;
import org.openmrs.Person;
import org.openmrs.api.APIAuthenticationException;
import org.openmrs.api.APIException;
import org.openmrs.api.ObsService;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.ObsDAO;
import org.openmrs.logic.Aggregation;
import org.openmrs.logic.Constraint;
import org.openmrs.reporting.PatientSet;
import org.openmrs.util.OpenmrsConstants;

/**
 * Observation-related services
 * 
 * @version 1.0
 */
public class ObsServiceImpl implements ObsService {

	private Log log = LogFactory.getLog(this.getClass());

	private ObsDAO dao;

	public ObsServiceImpl() {
	}

	/**
	 * Returns the injected dao object for this class
	 * 
	 * @return
	 */
	private ObsDAO getObsDAO() {
		if (!Context.hasPrivilege(OpenmrsConstants.PRIV_VIEW_OBS))
			throw new APIAuthenticationException("Privilege required: "
					+ OpenmrsConstants.PRIV_VIEW_OBS);

		return dao;
	}

	/**
	 * @see org.openmrs.api.ObsService#setObsDAO(org.openmrs.api.db.ObsDAO)
	 */
	public void setObsDAO(ObsDAO dao) {
		this.dao = dao;
	}

	/**
	 * @see org.openmrs.api.ObsService#createObs(org.openmrs.Obs)
	 */
	public void createObs(Obs obs) throws APIException {
		setRequiredObsProperties(obs);
		
		getObsDAO().createObs(obs);
	}
	
	/**
	 * Sets the creator and dateCreated properties on the Obs object
	 * 
	 * @param obs
	 */
	private void setRequiredObsProperties(Obs obs) {
		if (obs.getCreator() == null)
			obs.setCreator(Context.getAuthenticatedUser());

		if (obs.getDateCreated() == null)
			obs.setDateCreated(new Date());
		
		if (obs.getGroupMembers() != null) {
			for (Obs member : obs.getGroupMembers()) {
				// if statement does a quick sanity check to
				// avoid the simplest of infinite loops
				if (member.getCreator() == null || 
					member.getDateCreated() == null)
						setRequiredObsProperties(member);
			}
		}
	}

	/**
	 * 
	 * Correct use case:
	 * <pre>
	 * Obs parent = new Obs();
	 * Obs child1 = new Obs();
	 * Obs child2 = new Obs();
	 * 
	 * parent.addGroupMember(child1);
	 * parent.addGroupMember(child2);
	 * </pre>
	 * 
	 * @deprecated This method should no longer need to be called on the api. This
	 * 			  was meant as temporary until we created a true ObsGroup pojo.
	 * 
	 * @see org.openmrs.api.ObsService#createObsGroup(org.openmrs.Obs[])
	 */
	public void createObsGroup(Obs[] obs) throws APIException {
		if (obs == null || obs.length < 1)
			return; // silently tolerate calls with missing/empty parameter
		
		String conceptIdStr = Context.getAdministrationService().
		getGlobalProperty(
			  OpenmrsConstants.GLOBAL_PROPERTY_MEDICAL_RECORD_OBSERVATIONS, 
          "1238");
		// fail silently if a default obs group is not defined
		if (conceptIdStr == null || conceptIdStr.length() == 0)
			return;
		
		Integer conceptId = Integer.valueOf(conceptIdStr);
		Concept defaultObsGroupConcept = Context.getConceptService().getConcept(conceptId);
		
		// if they defined a bad concept, bail
		if (defaultObsGroupConcept == null)
		throw new APIException("There is no concept defined with concept id: " + conceptIdStr +
		               "You should correctly define the default obs group concept id with the global propery" + 
		               OpenmrsConstants.GLOBAL_PROPERTY_MEDICAL_RECORD_OBSERVATIONS);
		
		Obs obsGroup = new Obs();
		obsGroup.setConcept(defaultObsGroupConcept);
		
		for (Obs member : obs) {
    		obsGroup.addGroupMember(member);
    	}
    	
    	updateObs(obsGroup);
	}

	/**
	 * @see org.openmrs.api.ObsService#getObs(java.lang.Integer)
	 */
	public Obs getObs(Integer obsId) throws APIException {
		return getObsDAO().getObs(obsId);
	}

	/**
	 * @see org.openmrs.api.ObsService#updateObs(org.openmrs.Obs)
	 */
	public void updateObs(Obs obs) throws APIException {
		if (obs.isVoided() && obs.getVoidedBy() == null)
			voidObs(obs, obs.getVoidReason());
		else if (obs.isVoided() == false && obs.getVoidedBy() != null)
			unvoidObs(obs);
		else {
			setRequiredObsProperties(obs);
			log.debug("Date voided: " + obs.getDateVoided());
			getObsDAO().updateObs(obs);
		}
	}

	/**
	 * Voids an Obs
	 * 
	 * If the Obs argument is an obsGroup, all group members will be voided.
	 * 
	 * @see org.openmrs.api.ObsService#voidObs(org.openmrs.Obs, java.lang.String)
	 * @param Obs obs the Obs to void
	 * @param String reason the void reason
	 * @throws APIException
	 */
	
	public void voidObs(Obs obs, String reason) throws APIException {
		Set<Obs> obsToVoid = new HashSet<Obs>();
		obsToVoid.add(obs);
		Obs testObs = obs;
		List<Obs> childGroups = new ArrayList<Obs>();
		while (testObs.isObsGrouping()) {
			for (Obs oInner : testObs.getGroupMembers()) {
				if (oInner.isObsGrouping())
					childGroups.add(oInner);
				obsToVoid.add(oInner);
			}
			if (childGroups.size() > 0)
				testObs = childGroups.remove(childGroups.size() - 1);
			else
				testObs = new Obs();
		}
		for (Obs o : obsToVoid) {
			o.setVoided(true);
			o.setVoidReason(reason);
			o.setVoidedBy(Context.getAuthenticatedUser());
			o.setDateVoided(new Date());
		}
		getObsDAO().updateObs(obs);
	}

	/**
	 * Unvoids an Obs
	 * 
	 * If the Obs argument is an obsGroup, all group members
	 * with the same dateVoided will also be unvoided.
	 * 
	 * @see org.openmrs.api.ObsService#unvoidObs(org.openmrs.Obs)
	 * @param Obs obs the Obs to unvoid
	 * @throw APIException
	 */

	public void unvoidObs(Obs obs) throws APIException {
		Set<Obs> obsToUnVoid = new HashSet<Obs>();
		obsToUnVoid.add(obs);
		Obs testObs = obs;
		List<Obs> childGroups = new ArrayList<Obs>();
		while (testObs.isObsGrouping()) {
			for (Obs oInner : testObs.getGroupMembers()) {
				if (oInner.isObsGrouping())
					childGroups.add(oInner);
				if (oInner.getDateVoided().equals(obs.getDateVoided()))
					obsToUnVoid.add(oInner);
			}
			if (childGroups.size() > 0)
				testObs = childGroups.remove(childGroups.size() - 1);
			else
				testObs = new Obs();
		}
		for (Obs o : obsToUnVoid) {
			o.setVoided(false);
			o.setVoidReason(null);
			o.setVoidedBy(null);
			o.setDateVoided(null);
		}
		getObsDAO().updateObs(obs);
	}

	/**
	 * @see org.openmrs.api.ObsService#deleteObs(org.openmrs.Obs)
	 */
	public void deleteObs(Obs obs) throws APIException {
		getObsDAO().deleteObs(obs);
	}
	
	/**
	 * @see org.openmrs.api.ObsService#getMimeTypes()
	 */
	public List<MimeType> getMimeTypes() throws APIException {
		return getObsDAO().getMimeTypes();
	}

	/**
	 * @see org.openmrs.api.ObsService#getMimeType(java.lang.Integer)
	 */
	public MimeType getMimeType(Integer mimeTypeId) throws APIException {
		return getObsDAO().getMimeType(mimeTypeId);
	}

	/**
	 * @see org.openmrs.api.ObsService#getObservations(org.openmrs.Person, boolean includeVoided)
	 */
	public Set<Obs> getObservations(Person who, boolean includeVoided) {
		return getObsDAO().getObservations(who, includeVoided);
	}

	/**
	 * @see org.openmrs.api.ObsService#getObservations(org.openmrs.Concept, org.openmrs.Location, java.lang.String, java.lang.Integer, boolean includeVoided)
	 */
	public List<Obs> getObservations(Concept c, Location loc, String sort, Integer personType, boolean includeVoided) {
		return getObsDAO().getObservations(c, loc, sort, personType);
	}

	/**
	 * @see org.openmrs.api.ObsService#getObservations(org.openmrs.Person, org.openmrs.Concept, boolean includeVoided)
	 */
	public Set<Obs> getObservations(Person who, Concept question, boolean includeVoided) {
		return getObsDAO().getObservations(who, question, includeVoided);
	}

	/**
	 * @see org.openmrs.api.ObsService#getLastNObservations(java.lang.Integer, org.openmrs.Person, org.openmrs.Concept, boolean includeVoided)
	 */
	public List<Obs> getLastNObservations(Integer n, Person who,
			Concept question, boolean includeVoided) {
		return getObsDAO().getLastNObservations(n, who, question);
	}

	/**
	 * @see org.openmrs.api.ObsService#getObservations(org.openmrs.Concept, java.lang.String, java.lang.Integer, boolean includeVoided)
	 */
	public List<Obs> getObservations(Concept question, String sort, Integer personType, boolean includeVoided) {
		return getObsDAO().getObservations(question, sort, personType);
	}
	
	/**
	 * @see org.openmrs.api.ObsService#getObservationsAnsweredByConcept(org.openmrs.Concept, java.lang.Integer, boolean includeVoided)
	 */
	public List<Obs> getObservationsAnsweredByConcept(Concept answer, Integer personType, boolean includeVoided) {
		return getObsDAO().getObservationsAnsweredByConcept(answer, personType);
	}
	
	/**
	 * @see org.openmrs.api.ObsService#getNumericAnswersForConcept(org.openmrs.Concept, java.lang.Boolean, java.lang.Integer, boolean includeVoided)
	 */
	public List<Object[]> getNumericAnswersForConcept(Concept answer, Boolean sortByValue, Integer personType, boolean includeVoided) {
		return getObsDAO().getNumericAnswersForConcept(answer, sortByValue, personType);
	}
	

	/**
	 * @see org.openmrs.api.ObsService#getObservations(org.openmrs.Encounter)
	 */
	public Set<Obs> getObservations(Encounter whichEncounter) {
		return getObsDAO().getObservations(whichEncounter);
	}

	/**
	 * @see org.openmrs.api.ObsService#getVoidedObservations()
	 */
	public List<Obs> getVoidedObservations() {
		return getObsDAO().getVoidedObservations();
	}

	/**
	 * @see org.openmrs.api.ObsService#findObservations(java.lang.String, boolean, java.lang.Integer)
	 */
	public List<Obs> findObservations(String search, boolean includeVoided, Integer personType) {
		List<Obs> obs = new Vector<Obs>();
		for (Person p : Context.getPatientService().getPatientsByIdentifier(
				search, includeVoided)) {
			obs.addAll(getObsDAO().findObservations(p.getPersonId(),
					includeVoided, personType));
		}
		try {
			Integer i = Integer.valueOf(search);
			if (i != null)
				obs.addAll(getObsDAO().findObservations(i, includeVoided, personType));
		} catch (Exception e) {
		}

		return obs;
	}

	/**
	 * @see org.openmrs.api.ObsService#getDistinctObservationValues(org.openmrs.Concept, java.lang.Integer)
	 */
	public List<String> getDistinctObservationValues(Concept question, Integer personType) {
		// todo: make this efficient, and add a sort option

		Locale l = Context.getLocale();
		List<Obs> obs = getObservations(question, null, personType, false);
		SortedSet<String> set = new TreeSet<String>();
		for (Obs o : obs) {
			set.add(o.getValueAsString(l));
		}
		return new ArrayList<String>(set);
	}

	/**
	 * @see org.openmrs.api.ObsService#findObsByGroupId(java.lang.Integer)
	 */
	public List<Obs> findObsByGroupId(Integer obsGroupId) {
		return getObsDAO().findObsByGroupId(obsGroupId);
	}

	/**
	 * @see org.openmrs.api.ObsService#getObservations(org.openmrs.Person, org.openmrs.logic.Aggregation, org.openmrs.Concept, org.openmrs.logic.Constraint)
	 */
	public List<Obs> getObservations(Person who, Aggregation aggregation,
			Concept question, Constraint constraint) {
		return getObsDAO().getObservations(who, aggregation, question, constraint);
	}

	/**
	 * @see org.openmrs.api.ObsService#getObservations(java.util.List<org.openmrs.Concept>, java.util.Date, java.util.Data, boolean)
	 */
	public List<Obs> getObservations(List<Concept> concepts, Date fromDate, Date toDate, boolean includeVoided) {
		return getObsDAO().getObservations(concepts, fromDate, toDate, includeVoided);
	}
	
	/**
	 * @see org.openmrs.api.ObsService#getObservations(java.util.List<org.openmrs.Concept>, java.util.Date, java.util.Data)
	 */
	public List<Obs> getObservations(List<Concept> concepts, Date fromDate, Date toDate) {
		return this.getObservations(concepts, fromDate, toDate, false);
	}
	
	/**
	 * @see org.openmrs.api.ObsService#getObservations(PatientSet patients, List<Concept> concepts, Date fromDate, Date toDate)
	 */
	public List<Obs> getObservations(PatientSet patients, List<Concept> concepts, Date fromDate, Date toDate) {
		return getObsDAO().getObservations(patients, concepts, fromDate, toDate);
	}

}
