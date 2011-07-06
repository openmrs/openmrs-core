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
import java.util.Map;
import java.util.Vector;

import org.openmrs.Cohort;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Form;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.Order;
import org.openmrs.Patient;
import org.openmrs.User;
import org.openmrs.Visit;
import org.openmrs.VisitType;
import org.openmrs.api.APIException;
import org.openmrs.api.EncounterService;
import org.openmrs.api.ObsService;
import org.openmrs.api.OrderService;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.EncounterDAO;
import org.openmrs.util.OpenmrsUtil;
import org.openmrs.util.PrivilegeConstants;
import org.openmrs.validator.EncounterValidator;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

/**
 * Default implementation of the {@link EncounterService}
 * <p>
 * This class should not be instantiated alone, get a service class from the Context:
 * Context.getEncounterService();
 * 
 * @see org.openmrs.api.context.Context
 * @see org.openmrs.api.EncounterService
 */
public class EncounterServiceImpl extends BaseOpenmrsService implements EncounterService {
	
	// private Log log = LogFactory.getLog(this.getClass());
	
	private EncounterDAO dao;
	
	/**
	 * @see org.openmrs.api.EncounterService#setEncounterDAO(org.openmrs.api.db.EncounterDAO)
	 */
	public void setEncounterDAO(EncounterDAO dao) {
		this.dao = dao;
	}
	
	/**
	 * @see org.openmrs.api.EncounterService#getEncountersByPatient(java.lang.String, boolean)
	 */
	@Override
	public List<Encounter> getEncountersByPatient(String query, boolean includeVoided) throws APIException {
		if (query == null)
			throw new IllegalArgumentException("The 'query' parameter is required and cannot be null");
		
		return dao.getEncounters(query, null, null, includeVoided);
	}
	
	/**
	 * @see org.openmrs.api.EncounterService#saveEncounter(org.openmrs.Encounter)
	 */
	public Encounter saveEncounter(Encounter encounter) throws APIException {
		Errors errors = new BindException(encounter, "encounter");
		new EncounterValidator().validate(encounter, errors);
		if (errors.hasErrors())
			throw new APIException(Context.getMessageSourceService().getMessage("error.foundValidationErrors"));
		
		boolean isNewEncounter = false;
		Date newDate = encounter.getEncounterDatetime();
		Date originalDate = null;
		Location newLocation = encounter.getLocation();
		Location originalLocation = null;
		// check permissions
		if (encounter.getEncounterId() == null) {
			isNewEncounter = true;
			Context.requirePrivilege(PrivilegeConstants.ADD_ENCOUNTERS);
		} else {
			Context.requirePrivilege(PrivilegeConstants.EDIT_ENCOUNTERS);
		}
		
		// This must be done after setting dateCreated etc on the obs because
		// of the way the ORM tools flush things and check for nullity
		// This also must be done before the save encounter so we can use the
		// orig date
		// after the save
		if (!isNewEncounter) {
			// fetch the datetime from the database prior to saving for this
			// encounter
			// to see if it has changed and change all obs after saving if so
			originalDate = dao.getSavedEncounterDatetime(encounter);
			if (encounter.getLocation() != null)
				originalLocation = dao.getSavedEncounterLocation(encounter);
			// Our data model duplicates the patient column to allow for
			// observations to
			// not have to look up the parent Encounter to find the patient
			// Therefore, encounter.patient must always equal
			// encounter.observations[0-n].patient
			
			// If we are changing encounter.encounterDatetime, then we need to
			// also apply that
			// to Obs that inherited their obsDatetime from the encounter in the
			// first place
			
			Patient p = encounter.getPatient();
			for (Obs obs : encounter.getAllObs(true)) {
				// if the date was changed
				if (OpenmrsUtil.compare(originalDate, newDate) != 0) {
					
					// if the obs datetime is the same as the
					// original encounter datetime, fix it
					if (OpenmrsUtil.compare(obs.getObsDatetime(), originalDate) == 0) {
						obs.setObsDatetime(newDate);
					}
					
				}
				
				if (!OpenmrsUtil.nullSafeEquals(newLocation, originalLocation)) {
					if (obs.getLocation().equals(originalLocation)) {
						obs.setLocation(newLocation);
					}
				}
				
				// if the Person in the obs doesn't match the Patient in the
				// encounter, fix it
				if (!obs.getPerson().getPersonId().equals(p.getPatientId())) {
					obs.setPerson(p);
				}
			}
			
			// same goes for Orders
			for (Order o : encounter.getOrders()) {
				if (!p.equals(o.getPatient())) {
					o.setPatient(p);
				}
			}
		}
		
		// do the actual saving to the database
		dao.saveEncounter(encounter);
		
		return encounter;
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
	public List<Encounter> getEncountersByPatient(Patient patient) throws APIException {
		if (patient == null)
			throw new IllegalArgumentException("The 'patient' parameter is requred and cannot be null");
		return getEncounters(patient, null, null, null, null, null, null, false);
	}
	
	/**
	 * @see org.openmrs.api.EncounterService#getEncountersByPatient(String)
	 */
	public List<Encounter> getEncountersByPatient(String query) throws APIException {
		
		return getEncountersByPatient(query, false);
	}
	
	/**
	 * @see org.openmrs.api.EncounterService#getEncountersByPatientId(java.lang.Integer)
	 */
	public List<Encounter> getEncountersByPatientId(Integer patientId) throws APIException {
		if (patientId == null)
			throw new IllegalArgumentException("The 'patientId' parameter is requred and cannot be null");
		return dao.getEncountersByPatientId(patientId);
	}
	
	/**
	 * @see org.openmrs.api.EncounterService#getEncountersByPatientIdentifier(java.lang.String)
	 */
	public List<Encounter> getEncountersByPatientIdentifier(String identifier) throws APIException {
		if (identifier == null)
			throw new IllegalArgumentException("The 'identifier' parameter is required and cannot be null");
		
		List<Encounter> encs = new Vector<Encounter>();
		for (Patient p : Context.getPatientService().getPatients(null, identifier, null, false)) {
			encs.addAll(getEncountersByPatientId(p.getPatientId()));
		}
		return encs;
	}
	
	/**
	 * @see org.openmrs.api.EncounterService#getEncounters(org.openmrs.Patient,
	 *      org.openmrs.Location, java.util.Date, java.util.Date, java.util.Collection,
	 *      java.util.Collection, boolean)
	 * @deprecated replaced by
	 *             {@link #getEncounters(Patient, Location, Date, Date, Collection, Collection, Collection, boolean)}
	 */
	@Deprecated
	public List<Encounter> getEncounters(Patient who, Location loc, Date fromDate, Date toDate,
	        Collection<Form> enteredViaForms, Collection<EncounterType> encounterTypes, boolean includeVoided) {
		return getEncounters(who, loc, fromDate, toDate, enteredViaForms, encounterTypes, null, includeVoided);
	}
	
	/**
	 * @see org.openmrs.api.EncounterService#getEncounters(org.openmrs.Patient,
	 *      org.openmrs.Location, java.util.Date, java.util.Date, java.util.Collection,
	 *      java.util.Collection, java.util.Collection, boolean)
	 * @deprecated replaced by
	 *             {@link #getEncounters(Patient, Location, Date, Date, Collection, Collection, Collection, Collection, Collection, boolean)}
	 */
	@Deprecated
	public List<Encounter> getEncounters(Patient who, Location loc, Date fromDate, Date toDate,
	        Collection<Form> enteredViaForms, Collection<EncounterType> encounterTypes, Collection<User> providers,
	        boolean includeVoided) {
		return dao.getEncounters(who, loc, fromDate, toDate, enteredViaForms, encounterTypes, providers, null, null,
		    includeVoided);
	}
	
	/**
	 * @see org.openmrs.api.EncounterService#getEncounters(org.openmrs.Patient,
	 *      org.openmrs.Location, java.util.Date, java.util.Date, java.util.Collection,
	 *      java.util.Collection, java.util.Collection, boolean)
	 */
	public List<Encounter> getEncounters(Patient who, Location loc, Date fromDate, Date toDate,
	        Collection<Form> enteredViaForms, Collection<EncounterType> encounterTypes, Collection<User> providers,
	        Collection<VisitType> visitTypes, Collection<Visit> visits, boolean includeVoided) {
		return dao.getEncounters(who, loc, fromDate, toDate, enteredViaForms, encounterTypes, providers, visitTypes, visits,
		    includeVoided);
	}
	
	/**
	 * @see org.openmrs.api.EncounterService#voidEncounter(org.openmrs.Encounter, java.lang.String)
	 */
	public Encounter voidEncounter(Encounter encounter, String reason) {
		if (reason == null)
			throw new IllegalArgumentException("The argument 'reason' is required and so cannot be null");
		
		ObsService os = Context.getObsService();
		for (Obs o : encounter.getObsAtTopLevel(false)) {
			if (!o.isVoided()) {
				os.voidObs(o, reason);
			}
		}
		
		OrderService orderService = Context.getOrderService();
		for (Order o : encounter.getOrders()) {
			if (!o.isVoided()) {
				orderService.voidOrder(o, reason);
			}
		}
		
		encounter.setVoided(true);
		encounter.setVoidedBy(Context.getAuthenticatedUser());
		//we expect the dateVoided to be already set by AOP logic at this point unless this method was called within the API, 
		//this ensures that original ParentVoidedDate and the dateVoided of associated objects will always match for the 
		//unvoid handler to work
		if (encounter.getDateVoided() == null)
			encounter.setDateVoided(new Date());
		encounter.setVoidReason(reason);
		saveEncounter(encounter);
		return encounter;
	}
	
	/**
	 * @see org.openmrs.api.EncounterService#unvoidEncounter(org.openmrs.Encounter)
	 */
	public Encounter unvoidEncounter(Encounter encounter) throws APIException {
		String voidReason = encounter.getVoidReason();
		if (voidReason == null)
			voidReason = "";
		
		ObsService os = Context.getObsService();
		for (Obs o : encounter.getObsAtTopLevel(true)) {
			if (voidReason.equals(o.getVoidReason()))
				os.unvoidObs(o);
		}
		
		OrderService orderService = Context.getOrderService();
		for (Order o : encounter.getOrders()) {
			if (voidReason.equals(o.getVoidReason()))
				orderService.unvoidOrder(o);
		}
		
		encounter.setVoided(false);
		encounter.setVoidedBy(null);
		encounter.setDateVoided(null);
		encounter.setVoidReason(null);
		saveEncounter(encounter);
		return encounter;
	}
	
	/**
	 * @see org.openmrs.api.EncounterService#purgeEncounter(org.openmrs.Encounter)
	 */
	public void purgeEncounter(Encounter encounter) throws APIException {
		dao.deleteEncounter(encounter);
	}
	
	/**
	 * @see org.openmrs.api.EncounterService#purgeEncounter(Encounter, boolean)
	 */
	public void purgeEncounter(Encounter encounter, boolean cascade) throws APIException {
		if (cascade) {
			ObsService obsService = Context.getObsService();
			List<Encounter> justThisEncounter = new ArrayList<Encounter>();
			justThisEncounter.add(encounter);
			List<Obs> observations = new Vector<Obs>();
			observations.addAll(obsService.getObservations(null, justThisEncounter, null, null, null, null, null, null,
			    null, null, null, true));
			for (Obs o : observations) {
				obsService.purgeObs(o);
			}
		}
		purgeEncounter(encounter);
	}
	
	/**
	 * @see org.openmrs.api.EncounterService#saveEncounterType(org.openmrs.EncounterType)
	 */
	public EncounterType saveEncounterType(EncounterType encounterType) {
		dao.saveEncounterType(encounterType);
		return encounterType;
	}
	
	/**
	 * @see org.openmrs.api.EncounterService#getEncounterType(java.lang.Integer)
	 */
	public EncounterType getEncounterType(Integer encounterTypeId) throws APIException {
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
		return dao.getAllEncounterTypes(true);
	}
	
	/**
	 * @see org.openmrs.api.EncounterService#getAllEncounterTypes(boolean)
	 */
	public List<EncounterType> getAllEncounterTypes(boolean includeRetired) throws APIException {
		return dao.getAllEncounterTypes(includeRetired);
	}
	
	/**
	 * @see org.openmrs.api.EncounterService#findEncounterTypes(java.lang.String)
	 */
	public List<EncounterType> findEncounterTypes(String name) throws APIException {
		return dao.findEncounterTypes(name);
	}
	
	/**
	 * @see org.openmrs.api.EncounterService#retireEncounterType(EncounterType, String)
	 */
	public EncounterType retireEncounterType(EncounterType encounterType, String reason) throws APIException {
		if (reason == null)
			throw new IllegalArgumentException("The 'reason' argument is required");
		
		encounterType.setRetired(true);
		encounterType.setRetireReason(reason);
		return saveEncounterType(encounterType);
	}
	
	/**
	 * @see org.openmrs.api.EncounterService#unretireEncounterType(org.openmrs.EncounterType)
	 */
	public EncounterType unretireEncounterType(EncounterType encounterType) throws APIException {
		encounterType.setRetired(false);
		return saveEncounterType(encounterType);
	}
	
	/**
	 * @see org.openmrs.api.EncounterService#purgeEncounterType(org.openmrs.EncounterType)
	 */
	public void purgeEncounterType(EncounterType encounterType) throws APIException {
		dao.deleteEncounterType(encounterType);
	}
	
	/**
	 * @see org.openmrs.api.EncounterService#createEncounter(org.openmrs.Encounter)
	 * @deprecated replaced by {@link #saveEncounter(Encounter)}
	 */
	@Deprecated
	public void createEncounter(Encounter encounter) throws APIException {
		Context.getEncounterService().saveEncounter(encounter);
	}
	
	/**
	 * @see org.openmrs.api.EncounterService#updateEncounter(org.openmrs.Encounter)
	 * @deprecated replaced by {@link #saveEncounter(Encounter)}
	 */
	@Deprecated
	public void updateEncounter(Encounter encounter) throws APIException {
		Context.getEncounterService().saveEncounter(encounter);
	}
	
	/**
	 * @see org.openmrs.api.EncounterService#deleteEncounter(org.openmrs.Encounter)
	 * @deprecated Replaced by {@link #purgeEncounter(Encounter)}
	 */
	@Deprecated
	public void deleteEncounter(Encounter encounter) throws APIException {
		Context.getEncounterService().purgeEncounter(encounter);
	}
	
	/**
	 * @see org.openmrs.api.EncounterService#getEncountersByPatientId(java.lang.Integer, boolean)
	 * @deprecated replaced by {@link #getEncountersByPatientId(Integer)}
	 */
	@Deprecated
	public List<Encounter> getEncountersByPatientId(Integer patientId, boolean includeVoided) throws APIException {
		return getEncountersByPatientId(patientId);
	}
	
	/**
	 * @see org.openmrs.api.EncounterService#getEncountersByPatientIdentifier(java.lang.String,
	 *      boolean)
	 * @deprecated replaced by {@link #getEncountersByPatientIdentifier(String)}
	 */
	@Deprecated
	public List<Encounter> getEncountersByPatientIdentifier(String identifier, boolean includeVoided) throws APIException {
		return getEncountersByPatientIdentifier(identifier);
	}
	
	/**
	 * @see org.openmrs.api.EncounterService#getEncounters(org.openmrs.Patient)
	 * @deprecated replaced by {@link #getEncountersByPatient(Patient patient)}
	 */
	@Deprecated
	public List<Encounter> getEncounters(Patient who) {
		return getEncountersByPatient(who);
	}
	
	/**
	 * @see org.openmrs.api.EncounterService#getEncounters(org.openmrs.Patient, boolean)
	 * @deprecated replaced by
	 *             {@link #getEncounters(Patient, Location, Date, Date, Collection, Collection, Collection, boolean)}
	 */
	@Deprecated
	public List<Encounter> getEncounters(Patient who, boolean includeVoided) {
		return getEncounters(who, null, null, null, null, null, null, includeVoided);
	}
	
	/**
	 * @see org.openmrs.api.EncounterService#getEncounters(org.openmrs.Patient,
	 *      org.openmrs.Location)
	 * @deprecated replaced by
	 *             {@link #getEncounters(Patient, Location, Date, Date, Collection, Collection, Collection, boolean)}
	 */
	@Deprecated
	public List<Encounter> getEncounters(Patient who, Location where) {
		return getEncounters(who, where, null, null, null, null, null, false);
	}
	
	/**
	 * @see org.openmrs.api.EncounterService#getEncounters(org.openmrs.Patient, java.util.Date,
	 *      java.util.Date)
	 * @deprecated replaced by
	 *             {@link #getEncounters(Patient, Location, Date, Date, Collection, Collection, Collection, boolean)}
	 */
	@Deprecated
	public List<Encounter> getEncounters(Patient who, Date fromDate, Date toDate) {
		return getEncounters(who, null, fromDate, toDate, null, null, null, false);
	}
	
	/**
	 * @see org.openmrs.api.EncounterService#getEncounters(java.util.Date, java.util.Date)
	 * @deprecated replaced by
	 *             {@link #getEncounters(Patient, Location, Date, Date, Collection, Collection, Collection, boolean)}
	 */
	@Deprecated
	public Collection<Encounter> getEncounters(Date fromDate, Date toDate) {
		return getEncounters(null, null, fromDate, toDate, null, null, null, false);
	}
	
	/**
	 * @see org.openmrs.api.EncounterService#getEncounters(org.openmrs.Location, java.util.Date,
	 *      java.util.Date)
	 * @deprecated replaced by
	 *             {@link #getEncounters(Patient, Location, Date, Date, Collection, Collection, Collection, boolean)}
	 */
	@Deprecated
	public List<Encounter> getEncounters(Location loc, Date fromDate, Date toDate) {
		return getEncounters(null, loc, fromDate, toDate, null, null, null, false);
	}
	
	/**
	 * @see org.openmrs.api.EncounterService#getEncounterTypes()
	 * @deprecated replaced by {@link #getAllEncounterTypes()}
	 */
	@Deprecated
	public List<EncounterType> getEncounterTypes() {
		return getAllEncounterTypes();
	}
	
	/**
	 * @see org.openmrs.api.EncounterService#getLocations()
	 * @deprecated use LocationService.getAllLocations()
	 */
	@Deprecated
	public List<Location> getLocations() throws APIException {
		return Context.getLocationService().getAllLocations();
	}
	
	/**
	 * @see org.openmrs.api.EncounterService#getLocation(java.lang.Integer)
	 * @deprecated use LocationService.getLocation(locationId)
	 */
	@Deprecated
	public Location getLocation(Integer locationId) throws APIException {
		return Context.getLocationService().getLocation(locationId);
	}
	
	/**
	 * @see org.openmrs.api.EncounterService#getLocationByName(java.lang.String)
	 * @deprecated use LocationService.getLocation(name)
	 */
	@Deprecated
	public Location getLocationByName(String name) throws APIException {
		return Context.getLocationService().getLocation(name);
	}
	
	/**
	 * @see org.openmrs.api.EncounterService#findLocations(java.lang.String)
	 * @deprecated use LocationService.getLocations(name)
	 */
	@Deprecated
	public List<Location> findLocations(String name) throws APIException {
		return Context.getLocationService().getLocations(name);
	}
	
	/**
	 * @see org.openmrs.api.EncounterService#getEncounterByUuid(java.lang.String)
	 */
	public Encounter getEncounterByUuid(String uuid) throws APIException {
		return dao.getEncounterByUuid(uuid);
	}
	
	/**
	 * @see org.openmrs.api.EncounterService#getEncounterTypeByUuid(java.lang.String)
	 */
	public EncounterType getEncounterTypeByUuid(String uuid) throws APIException {
		return dao.getEncounterTypeByUuid(uuid);
	}
	
	/**
	 * @see org.openmrs.api.EncounterService#getAllEncounters(org.openmrs.Cohort)
	 */
	@Override
	public Map<Integer, List<Encounter>> getAllEncounters(Cohort patients) {
		return dao.getAllEncounters(patients);
	}
	
	/**
	 * @see org.openmrs.api.EncounterService#getEncounters(java.lang.String, java.lang.Integer,
	 *      java.lang.Integer, boolean)
	 */
	@Override
	public List<Encounter> getEncounters(String query, Integer start, Integer length, boolean includeVoided)
	        throws APIException {
		return dao.getEncounters(query, start, length, includeVoided);
	}
	
	/**
	 * @see org.openmrs.api.EncounterService#getCountOfEncounters(java.lang.String, boolean)
	 */
	@Override
	public Integer getCountOfEncounters(String query, boolean includeVoided) {
		return dao.getCountOfEncounters(query, includeVoided);
	}
	
	/**
	 * @see EncounterService#getEncountersByVisit(Visit)
	 */
	@Override
	public List<Encounter> getEncountersByVisit(Visit visit) {
		return dao.getEncountersByVisit(visit);
	}
}
