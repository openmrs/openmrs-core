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
import java.util.Date;
import java.util.List;
import java.util.Vector;

import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Form;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.Order;
import org.openmrs.Patient;
import org.openmrs.User;
import org.openmrs.api.APIException;
import org.openmrs.api.EncounterService;
import org.openmrs.api.ObsService;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.EncounterDAO;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.OpenmrsUtil;

/**
 * Default implementation of the {@link EncounterService}
 * 
 * This class should not be instantiated alone, get a service class
 * from the Context: Context.getEncounterService();
 * 
 * @see org.openmrs.api.context.Context
 * @see org.openmrs.api.EncounterService
 */
public class EncounterServiceImpl extends BaseOpenmrsService implements EncounterService {

	//private Log log = LogFactory.getLog(this.getClass());

	private EncounterDAO dao;

	/**
	 * @see org.openmrs.api.EncounterService#setEncounterDAO(org.openmrs.api.db.EncounterDAO)
	 */
	public void setEncounterDAO(EncounterDAO dao) {
		this.dao = dao;
	}

	/**
	 * @see org.openmrs.api.EncounterService#saveEncounter(org.openmrs.Encounter)
	 */
	public void saveEncounter(Encounter encounter) throws APIException {
		Date now = new Date();
		User me = Context.getAuthenticatedUser();
		
		boolean isNewEncounter = false;
		Date newDate = encounter.getEncounterDatetime();
		Date originalDate = null;
		
		// check permissions
		if (encounter.getEncounterId() == null) {
			isNewEncounter = true;
			Context.requirePrivilege(OpenmrsConstants.PRIV_ADD_ENCOUNTERS);
		} else { 
			Context.requirePrivilege(OpenmrsConstants.PRIV_EDIT_ENCOUNTERS);
		}
		
		// set up child object lists 
		if (encounter.getDateCreated() == null)
			encounter.setDateCreated(now);
		if (encounter.getCreator() == null)
			encounter.setCreator(me);
		for (Obs o : encounter.getAllObs(true)) {
			if (o.getDateCreated() == null)
				o.setDateCreated(now);
			if (o.getCreator() == null)
				o.setCreator(me);
		}
		if (encounter.getOrders() != null) {
			for (Order o : encounter.getOrders()) {
				if (o.getDateCreated() == null)
					o.setDateCreated(now);
				if (o.getCreator() == null)
					o.setCreator(me);
			}
		}
		
		// This must be done after setting dateCreated etc on the obs because
		// of the way the ORM tools flush things and check for nullity
		// This also must be done before the save encounter so we can use the orig date
		// after the save
		if (isNewEncounter == false)
			// fetch the datetime from the database prior to saving for this encounter
			// to see if it has changed and change all obs after saving if so
			originalDate = dao.getSavedEncounterDatetime(encounter);
		
		// do the actual saving to the database
		dao.saveEncounter(encounter);
		
		// (only check for changed dates or persons if updating this encounter 
		if (isNewEncounter == false) {
			// Our data model duplicates the patient column to allow for observations to 
			//   not have to look up the parent Encounter to find the patient
			// Therefore, encounter.patient must always equal encounter.observations[0-n].patient
			
			// If we are changing encounter.encounterDatetime, then we need to also apply that
			// to Obs that inherited their obsDatetime from the encounter in the first place
			
			Patient p = encounter.getPatient();
			for (Obs obs : encounter.getAllObs()) {
				boolean obsWasChanged = false;
				
				// if the date was changed
				if (OpenmrsUtil.compare(originalDate, newDate) != 0 ) {
					
					// if the obs datetime is the same as the 
					// original encounter datetime, fix it
					if (OpenmrsUtil.compare(obs.getObsDatetime(), originalDate) == 0) {
						obs.setObsDatetime(newDate);
						obsWasChanged = true;
					}
					
					if (!obs.getPerson().getPersonId().equals(p.getPatientId())) {
						obs.setPerson(p);
						obsWasChanged = true;
					}
					
					if (obsWasChanged)
						Context.getObsService().saveObs(obs, "Encounter datetime or person was changed");
				}
				
			}
			
			// same goes for Orders
			for (Order o : Context.getOrderService().getOrdersByEncounter(encounter)) {
				if (!p.equals(o.getPatient())) {
					o.setPatient(p);
					Context.getOrderService().saveOrder(o);
				}
			}
		}
	}

	/**
	 * @see org.openmrs.api.EncounterService#getEncounter(java.lang.Integer)
	 */
	public Encounter getEncounter(Integer encounterId) throws APIException {
		return dao.getEncounter(encounterId);
	}

	/**
	 * @see org.openmrs.api.EncounterService#getEncountersByPatient(org.openmrs.Patient)
	 */
	public List<Encounter> getEncountersByPatient(Patient patient)
	        throws APIException {
		return getEncounters(patient, null, null, null, null, null, false);
	}

	/**
	 * @see org.openmrs.api.EncounterService#getEncountersByPatientId(java.lang.Integer)
	 */
	public List<Encounter> getEncountersByPatientId(Integer patientId)
	        throws APIException {
		return dao.getEncountersByPatientId(patientId);
	}

	/**
	 * @see org.openmrs.api.EncounterService#getEncountersByPatientIdentifier(java.lang.String)
	 */
	public List<Encounter> getEncountersByPatientIdentifier(String identifier)
	        throws APIException {
		List<Encounter> encs = new Vector<Encounter>();
		for (Patient p : Context.getPatientService().getPatients(null,
		                                                         identifier,
		                                                         null)) {
			encs.addAll(getEncountersByPatientId(p.getPatientId()));
		}
		return encs;
	}

	/**
	 * @see org.openmrs.api.EncounterService#getEncounters(org.openmrs.Patient,
	 *      org.openmrs.Location, java.util.Date, java.util.Date,
	 *      java.util.Collection, java.util.Collection, boolean)
	 */
	public List<Encounter> getEncounters(Patient who, Location loc,
	        Date fromDate, Date toDate, Collection<Form> enteredViaForms,
	        Collection<EncounterType> encounterTypes, boolean includeVoided) {
		return dao.getEncounters(who,
		                                       loc,
		                                       fromDate,
		                                       toDate,
		                                       enteredViaForms,
		                                       encounterTypes,
		                                       includeVoided);
	}

	/**
	 * @see org.openmrs.api.EncounterService#voidEncounter(org.openmrs.Encounter,
	 *      java.lang.String)
	 */
	public void voidEncounter(Encounter encounter, String reason) {
		if (reason == null)
			reason = "";

		ObsService os = Context.getObsService();
		for (Obs o : encounter.getObsAtTopLevel(false)) {
			if (!o.isVoided()) {
				os.voidObs(o, reason);
			}
		}

		encounter.setVoided(true);
		encounter.setVoidedBy(Context.getAuthenticatedUser());
		encounter.setDateVoided(new Date());
		encounter.setVoidReason(reason);
		saveEncounter(encounter);
	}

	/**
	 * @see org.openmrs.api.EncounterService#unvoidEncounter(org.openmrs.Encounter)
	 */
	public void unvoidEncounter(Encounter encounter) throws APIException {
		String voidReason = encounter.getVoidReason();
		if (voidReason == null)
			voidReason = "";

		ObsService os = Context.getObsService();
		for (Obs o : encounter.getObsAtTopLevel(false)) {
			if (voidReason.equals(o.getVoidReason()))
				os.unvoidObs(o);
		}

		encounter.setVoided(false);
		encounter.setVoidedBy(null);
		encounter.setDateVoided(null);
		encounter.setVoidReason(null);
		saveEncounter(encounter);
	}

	/**
	 * @see org.openmrs.api.EncounterService#purgeEncounter(org.openmrs.Encounter)
	 */
	public void purgeEncounter(Encounter encounter) throws APIException {
		dao.deleteEncounter(encounter);
	}

	/**
	 * @see org.openmrs.api.EncounterService#purgeEncounter(org.openmrs.Encounter,
	 *      java.lang.boolean)
	 */
	public void purgeEncounter(Encounter encounter, boolean cascade)
	        throws APIException {
		if (cascade) {
			ObsService obsService = Context.getObsService();
			List<Encounter> justThisEncounter = new ArrayList<Encounter>();
			justThisEncounter.add(encounter);
			List<Obs> observations = new Vector<Obs>();
			observations.addAll(obsService.getObservations(null,
			                                               justThisEncounter,
			                                               null,
			                                               null,
			                                               null,
			                                               null,
			                                               null,
			                                               null,
			                                               null,
			                                               null,
			                                               null,
			                                               cascade));
			for (Obs o : observations) {
				obsService.purgeObs(o, cascade);
			}
		}
		purgeEncounter(encounter);
	}

	/**
	 * @see org.openmrs.api.EncounterService#saveEncounterType(org.openmrs.EncounterType)
	 */
	public void saveEncounterType(EncounterType encounterType) {
		if (encounterType.getCreator() == null) {
			encounterType.setCreator(Context.getAuthenticatedUser());
			encounterType.setDateCreated(new Date());
		}
		
		dao.saveEncounterType(encounterType);
	}

	/**
	 * @see org.openmrs.api.EncounterService#getEncounterType(java.lang.Integer)
	 */
	public EncounterType getEncounterType(Integer encounterTypeId)
	        throws APIException {
		return dao.getEncounterType(encounterTypeId);
	}

	/**
	 * @see org.openmrs.api.EncounterService#getEncounterType(java.lang.String)
	 */
	public EncounterType getEncounterType(String name) throws APIException {
		return dao.getEncounterType(name);
	}

	/**
	 * @see org.openmrs.api.EncounterService#getAllEncounterTypes()
	 */
	public List<EncounterType> getAllEncounterTypes() throws APIException {
		return dao.getAllEncounterTypes(false);
	}

	/**
	 * @see org.openmrs.api.EncounterService#getAllEncounterTypes(boolean)
	 */
	public List<EncounterType> getAllEncounterTypes(boolean includeVoided)
	        throws APIException {
		return dao.getAllEncounterTypes(includeVoided);
	}

	/**
	 * @see org.openmrs.api.EncounterService#findEncounterTypes(java.lang.String)
	 */
	public List<EncounterType> findEncounterTypes(String name)
	        throws APIException {
		return dao.findEncounterTypes(name);
	}

	/**
	 * @see org.openmrs.api.EncounterService#retireEncounterType(org.openmrs.EncounterType)
	 */
	public void retireEncounterType(EncounterType encounterType, String reason)
	        throws APIException {
		encounterType.setRetired(true);
		encounterType.setRetiredBy(Context.getAuthenticatedUser());
		encounterType.setDateRetired(new Date());
		encounterType.setRetireReason(reason);
		dao.saveEncounterType(encounterType);
	}

	/**
	 * @see org.openmrs.api.EncounterService#unretireEncounterType(org.openmrs.EncounterType)
	 */
	public void unretireEncounterType(EncounterType encounterType)
	        throws APIException {
		encounterType.setRetired(false);
		encounterType.setRetiredBy(null);
		encounterType.setDateRetired(null);
		encounterType.setRetireReason(null);
		dao.saveEncounterType(encounterType);
	}

	/**
	 * @see org.openmrs.api.EncounterService#purgeEncounterType(org.openmrs.EncounterType)
	 */
	public void purgeEncounterType(EncounterType encounterType)
	        throws APIException {
		dao.deleteEncounterType(encounterType);
	}

	/**
	 * @see org.openmrs.api.EncounterService#createEncounter(org.openmrs.Encounter)
	 * @deprecated replaced by {@link #saveEncounter(Encounter)}
	 */
	public void createEncounter(Encounter encounter) throws APIException {
		saveEncounter(encounter);
	}

	/**
	 * @see org.openmrs.api.EncounterService#updateEncounter(org.openmrs.Encounter)
	 * @deprecated replaced by {@link #saveEncounter(Encounter)}
	 */
	public void updateEncounter(Encounter encounter) throws APIException {
		saveEncounter(encounter);
	}

	/**
	 * @see org.openmrs.api.EncounterService#deleteEncounter(org.openmrs.Encounter)
	 * @deprecated Replaced by {@link #purgeEncounter(Encounter)}
	 */
	public void deleteEncounter(Encounter encounter) throws APIException {
		purgeEncounter(encounter);
	}

	/**
	 * @see org.openmrs.api.EncounterService#getEncountersByPatientId(java.lang.Integer,
	 *      boolean)
	 * @deprecated replaced by {@link #getEncountersByPatientId(Integer)}
	 */
	public List<Encounter> getEncountersByPatientId(Integer patientId,
	        boolean includeVoided) throws APIException {
		return getEncountersByPatientId(patientId);
	}

	/**
	 * @see org.openmrs.api.EncounterService#getEncountersByPatientIdentifier(java.lang.String,
	 *      boolean)
	 * @deprecated replaced by {@link #getEncountersByPatientIdentifier(String)}
	 */
	public List<Encounter> getEncountersByPatientIdentifier(String identifier,
	        boolean includeVoided) throws APIException {
		return getEncountersByPatientIdentifier(identifier);
	}

	/**
	 * @see org.openmrs.api.EncounterService#getEncounters(org.openmrs.Patient)
	 * @deprecated replaced by {@link #getEncountersByPatient(Patient patient)}
	 */
	public List<Encounter> getEncounters(Patient who) {
		return getEncountersByPatient(who);
	}

	/**
	 * @see org.openmrs.api.EncounterService#getEncounters(org.openmrs.Patient,
	 *      boolean)
	 * @deprecated replaced by
	 *             {@link #getEncounters(Patient, Location, Date, Date, Collection, Collection, boolean)}
	 */
	public List<Encounter> getEncounters(Patient who, boolean includeVoided) {
		return getEncounters(who, null, null, null, null, null, includeVoided);
	}

	/**
	 * @see org.openmrs.api.EncounterService#getEncounters(org.openmrs.Patient,
	 *      org.openmrs.Location)
	 * @deprecated replaced by
	 *             {@link #getEncounters(Patient, Location, Date, Date, Collection, Collection, boolean);
	 */
	public List<Encounter> getEncounters(Patient who, Location where) {
		return getEncounters(who, where, null, null, null, null, false);
	}

	/**
	 * @see org.openmrs.api.EncounterService#getEncounters(org.openmrs.Patient,
	 *      java.util.Date, java.util.Date)
	 * @deprecated replaced by
	 *             {@link #getEncounters(Patient, Location, Date, Date, Collection, Collection, boolean);
	 */
	public List<Encounter> getEncounters(Patient who, Date fromDate, Date toDate) {
		return getEncounters(who, null, fromDate, toDate, null, null, false);
	}

	/**
	 * @see org.openmrs.api.EncounterService#getEncounters(java.util.Date,
	 *      java.util.Date)
	 * @deprecated replaced by
	 *             {@link #getEncounters(Patient, Location, Date, Date, Collection, Collection, boolean);
	 */
	public Collection<Encounter> getEncounters(Date fromDate, Date toDate) {
		return getEncounters(null, null, fromDate, toDate, null, null, false);
	}

	/**
	 * @see org.openmrs.api.EncounterService#getEncounters(org.openmrs.Location,
	 *      java.util.Date, java.util.Date)
	 * @deprecated replaced by
	 *             {@link #getEncounters(Patient, Location, Date, Date, Collection, Collection, boolean);
	 */
	public List<Encounter> getEncounters(Location loc, Date fromDate,
	        Date toDate) {
		return getEncounters(null, loc, fromDate, toDate, null, null, false);
	}

	/**
	 * @see org.openmrs.api.EncounterService#getEncounterTypes()
	 * @deprecated replaced by {@link# #getAllEncounterTypes()}
	 */
	public List<EncounterType> getEncounterTypes() {
		return getAllEncounterTypes();
	}
	
	/**
	 * @see org.openmrs.api.EncounterService#getLocations()
	 * @deprecated use LocationService.getAllLocations()
	 */
	public List<Location> getLocations() throws APIException {
		return Context.getLocationService().getAllLocations();
	}

	/**
	 * @see org.openmrs.api.EncounterService#getLocation(java.lang.Integer)
	 * @deprecated use LocationService.getLocation(locationId)
	 */
	public Location getLocation(Integer locationId) throws APIException {
		return Context.getLocationService().getLocation(locationId);
	}

	/**
	 * @see org.openmrs.api.EncounterService#getLocationByName(java.lang.String)
	 * @deprecated use LocationService.getLocation(name)
	 */
	public Location getLocationByName(String name) throws APIException {
		return Context.getLocationService().getLocation(name);
	}

	/**
	 * @see org.openmrs.api.EncounterService#findLocations(java.lang.String)
	 * @deprecated use LocationService.getLocations(name)
	 */
	public List<Location> findLocations(String name) throws APIException {
		return Context.getLocationService().getLocations(name);
	}

}