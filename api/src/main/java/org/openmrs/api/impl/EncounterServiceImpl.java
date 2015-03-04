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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;
import org.openmrs.Cohort;
import org.openmrs.Encounter;
import org.openmrs.EncounterRole;
import org.openmrs.EncounterType;
import org.openmrs.Form;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.Order;
import org.openmrs.Patient;
import org.openmrs.Privilege;
import org.openmrs.Provider;
import org.openmrs.User;
import org.openmrs.Visit;
import org.openmrs.VisitType;
import org.openmrs.api.APIException;
import org.openmrs.api.EncounterService;
import org.openmrs.api.EncounterTypeLockedException;
import org.openmrs.api.ObsService;
import org.openmrs.api.OrderService;
import org.openmrs.api.ProviderService;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.EncounterDAO;
import org.openmrs.api.handler.EncounterVisitHandler;
import org.openmrs.util.HandlerUtil;
import org.openmrs.util.OpenmrsClassLoader;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.OpenmrsUtil;
import org.openmrs.util.PrivilegeConstants;
import org.springframework.transaction.annotation.Transactional;

/**
 * Default implementation of the {@link EncounterService}
 * <p>
 * This class should not be instantiated alone, get a service class from the Context:
 * Context.getEncounterService();
 * 
 * @see org.openmrs.api.context.Context
 * @see org.openmrs.api.EncounterService
 */
@Transactional
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
	@Transactional(readOnly = true)
	public List<Encounter> getEncountersByPatient(String query, boolean includeVoided) throws APIException {
		if (query == null) {
			throw new IllegalArgumentException("The 'query' parameter is required and cannot be null");
		}
		
		return Context.getEncounterService().filterEncountersByViewPermissions(
		    dao.getEncounters(query, null, null, null, includeVoided), null);
	}
	
	/**
	 * @see org.openmrs.api.EncounterService#saveEncounter(org.openmrs.Encounter)
	 */
	public Encounter saveEncounter(Encounter encounter) throws APIException {
		
		// if authenticated user is not supposed to edit encounter of certain type
		if (!canEditEncounter(encounter, null)) {
			throw new APIException("Encounter.error.privilege.required.edit", new Object[] { encounter.getEncounterType()
			        .getEditPrivilege() });
		}
		
		//If new encounter, try to assign a visit using the registered visit assignment handler.
		if (encounter.getEncounterId() == null) {
			
			//Am using Context.getEncounterService().getActiveEncounterVisitHandler() instead of just
			//getActiveEncounterVisitHandler() for modules which may want to AOP around this call.
			EncounterVisitHandler encounterVisitHandler = Context.getEncounterService().getActiveEncounterVisitHandler();
			if (encounterVisitHandler != null) {
				encounterVisitHandler.beforeCreateEncounter(encounter);
				
				//If we have been assigned a new visit, persist it.
				if (encounter.getVisit() != null && encounter.getVisit().getVisitId() == null) {
					Context.getVisitService().saveVisit(encounter.getVisit());
				}
			}
		}
		
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
		Patient p = encounter.getPatient();
		
		if (!isNewEncounter) {
			// fetch the datetime from the database prior to saving for this
			// encounter
			// to see if it has changed and change all obs after saving if so
			originalDate = dao.getSavedEncounterDatetime(encounter);
			if (encounter.getLocation() != null) {
				originalLocation = dao.getSavedEncounterLocation(encounter);
			}
			// Our data model duplicates the patient column to allow for
			// observations to
			// not have to look up the parent Encounter to find the patient
			// Therefore, encounter.patient must always equal
			// encounter.observations[0-n].patient
			
			// If we are changing encounter.encounterDatetime, then we need to
			// also apply that
			// to Obs that inherited their obsDatetime from the encounter in the
			// first place
			
			for (Obs obs : encounter.getAllObs(true)) {
				// if the date was changed
				if (OpenmrsUtil.compare(originalDate, newDate) != 0
				        && OpenmrsUtil.compare(obs.getObsDatetime(), originalDate) == 0) {
					
					// if the obs datetime is the same as the
					// original encounter datetime, fix it
					obs.setObsDatetime(newDate);
					
				}
				
				if (!OpenmrsUtil.nullSafeEquals(newLocation, originalLocation) && obs.getLocation().equals(originalLocation)) {
					obs.setLocation(newLocation);
				}
				
				// if the Person in the obs doesn't match the Patient in the
				// encounter, fix it
				if (!obs.getPerson().getPersonId().equals(p.getPatientId())) {
					obs.setPerson(p);
				}
			}
		}
		// same goes for Orders
		for (Order o : encounter.getOrders()) {
			if (!p.equals(o.getPatient())) {
				o.setPatient(p);
			}
		}
		
		// do the actual saving to the database
		dao.saveEncounter(encounter);
		
		// save the new orders
		for (Order o : encounter.getOrders()) {
			if (o.getOrderId() == null) {
				Context.getOrderService().saveOrder(o, null);
			}
		}
		return encounter;
	}
	
	/**
	 * @see org.openmrs.api.EncounterService#getEncounter(java.lang.Integer)
	 */
	@Transactional(readOnly = true)
	public Encounter getEncounter(Integer encounterId) throws APIException {
		Encounter encounter = dao.getEncounter(encounterId);
		if (encounter == null) {
			return null;
		} else if (canViewEncounter(encounter, null)) {
			return encounter;
		} else {
			throw new APIException("Encounter.error.privilege.required.view", new Object[] { encounter.getEncounterType()
			        .getViewPrivilege() });
		}
	}
	
	/**
	 * @see org.openmrs.api.EncounterService#getEncountersByPatient(org.openmrs.Patient)
	 */
	@Transactional(readOnly = true)
	public List<Encounter> getEncountersByPatient(Patient patient) throws APIException {
		if (patient == null) {
			throw new IllegalArgumentException("The 'patient' parameter is requred and cannot be null");
		}
		return Context.getEncounterService().getEncounters(patient, null, null, null, null, null, null, false);
	}
	
	/**
	 * @see org.openmrs.api.EncounterService#getEncountersByPatient(String)
	 */
	@Transactional(readOnly = true)
	public List<Encounter> getEncountersByPatient(String query) throws APIException {
		
		return Context.getEncounterService().filterEncountersByViewPermissions(getEncountersByPatient(query, false), null);
	}
	
	/**
	 * @see org.openmrs.api.EncounterService#getEncountersByPatientId(java.lang.Integer)
	 */
	@Transactional(readOnly = true)
	public List<Encounter> getEncountersByPatientId(Integer patientId) throws APIException {
		if (patientId == null) {
			throw new IllegalArgumentException("The 'patientId' parameter is requred and cannot be null");
		}
		return Context.getEncounterService()
		        .filterEncountersByViewPermissions(dao.getEncountersByPatientId(patientId), null);
	}
	
	/**
	 * @see org.openmrs.api.EncounterService#getEncountersByPatientIdentifier(java.lang.String)
	 */
	@Transactional(readOnly = true)
	public List<Encounter> getEncountersByPatientIdentifier(String identifier) throws APIException {
		if (identifier == null) {
			throw new IllegalArgumentException("The 'identifier' parameter is required and cannot be null");
		}
		
		List<Encounter> encs = new Vector<Encounter>();
		for (Patient p : Context.getPatientService().getPatients(null, identifier, null, false)) {
			encs.addAll(Context.getEncounterService().getEncountersByPatientId(p.getPatientId()));
		}
		return Context.getEncounterService().filterEncountersByViewPermissions(encs, null);
	}
	
	/**
	 * @see org.openmrs.api.EncounterService#getEncounters(org.openmrs.Patient,
	 *      org.openmrs.Location, java.util.Date, java.util.Date, java.util.Collection,
	 *      java.util.Collection, boolean)
	 * @deprecated replaced by
	 *             {@link #getEncounters(Patient, Location, Date, Date, Collection, Collection, Collection, boolean)}
	 */
	@Deprecated
	@Transactional(readOnly = true)
	public List<Encounter> getEncounters(Patient who, Location loc, Date fromDate, Date toDate,
	        Collection<Form> enteredViaForms, Collection<EncounterType> encounterTypes, boolean includeVoided) {
		return Context.getEncounterService().getEncounters(who, loc, fromDate, toDate, enteredViaForms, encounterTypes,
		    null, includeVoided);
	}
	
	/**
	 * @see org.openmrs.api.EncounterService#getEncounters(org.openmrs.Patient,
	 *      org.openmrs.Location, java.util.Date, java.util.Date, java.util.Collection,
	 *      java.util.Collection, java.util.Collection, boolean)
	 * @deprecated replaced by
	 *             {@link #getEncounters(Patient, Location, Date, Date, Collection, Collection, Collection, Collection, Collection, boolean)}
	 */
	@Deprecated
	@Transactional(readOnly = true)
	public List<Encounter> getEncounters(Patient who, Location loc, Date fromDate, Date toDate,
	        Collection<Form> enteredViaForms, Collection<EncounterType> encounterTypes, Collection<User> providers,
	        boolean includeVoided) {
		return Context.getEncounterService().filterEncountersByViewPermissions(
		    dao.getEncounters(who, loc, fromDate, toDate, enteredViaForms, encounterTypes, usersToProviders(providers),
		        null, null, includeVoided), null);
	}
	
	/**
	 * Helper method that finds the corresponding providers for a collection of users
	 * 
	 * @param users
	 * @return a collection of providers, with 0-n for each item in users
	 */
	private Collection<Provider> usersToProviders(Collection<User> users) {
		if (users == null) {
			return null;
		}
		ProviderService providerService = Context.getProviderService();
		Collection<Provider> ret = new HashSet<Provider>();
		for (User u : users) {
			ret.addAll(providerService.getProvidersByPerson(u.getPerson()));
		}
		return ret;
	}
	
	/**
	 * @see org.openmrs.api.EncounterService#getEncounters(org.openmrs.Patient,
	 *      org.openmrs.Location, java.util.Date, java.util.Date, java.util.Collection,
	 *      java.util.Collection, java.util.Collection, boolean)
	 */
	@Override
	@Transactional(readOnly = true)
	public List<Encounter> getEncounters(Patient who, Location loc, Date fromDate, Date toDate,
	        Collection<Form> enteredViaForms, Collection<EncounterType> encounterTypes, Collection<Provider> providers,
	        Collection<VisitType> visitTypes, Collection<Visit> visits, boolean includeVoided) {
		return Context.getEncounterService().filterEncountersByViewPermissions(
		    dao.getEncounters(who, loc, fromDate, toDate, enteredViaForms, encounterTypes, providers, visitTypes, visits,
		        includeVoided), null);
	}
	
	/**
	 * @see org.openmrs.api.EncounterService#voidEncounter(org.openmrs.Encounter, java.lang.String)
	 */
	public Encounter voidEncounter(Encounter encounter, String reason) {
		
		// if authenticated user is not supposed to edit encounter of certain type
		if (!canEditEncounter(encounter, null)) {
			throw new APIException("Encounter.error.privilege.required.void", new Object[] { encounter.getEncounterType()
			        .getEditPrivilege() });
		}
		
		if (reason == null) {
			throw new IllegalArgumentException("The argument 'reason' is required and so cannot be null");
		}
		
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
		if (encounter.getDateVoided() == null) {
			encounter.setDateVoided(new Date());
		}
		encounter.setVoidReason(reason);
		Context.getEncounterService().saveEncounter(encounter);
		return encounter;
	}
	
	/**
	 * @see org.openmrs.api.EncounterService#unvoidEncounter(org.openmrs.Encounter)
	 */
	public Encounter unvoidEncounter(Encounter encounter) throws APIException {
		
		// if authenticated user is not supposed to edit encounter of certain type
		if (!canEditEncounter(encounter, null)) {
			throw new APIException("Encounter.error.privilege.required.unvoid", new Object[] { encounter.getEncounterType()
			        .getEditPrivilege() });
		}
		
		String voidReason = encounter.getVoidReason();
		if (voidReason == null) {
			voidReason = "";
		}
		
		ObsService os = Context.getObsService();
		for (Obs o : encounter.getObsAtTopLevel(true)) {
			if (voidReason.equals(o.getVoidReason())) {
				os.unvoidObs(o);
			}
		}
		
		OrderService orderService = Context.getOrderService();
		for (Order o : encounter.getOrders()) {
			if (voidReason.equals(o.getVoidReason())) {
				orderService.unvoidOrder(o);
			}
		}
		
		encounter.setVoided(false);
		encounter.setVoidedBy(null);
		encounter.setDateVoided(null);
		encounter.setVoidReason(null);
		Context.getEncounterService().saveEncounter(encounter);
		return encounter;
	}
	
	/**
	 * @see org.openmrs.api.EncounterService#purgeEncounter(org.openmrs.Encounter)
	 */
	public void purgeEncounter(Encounter encounter) throws APIException {
		// if authenticated user is not supposed to edit encounter of certain type
		if (!canEditEncounter(encounter, null)) {
			throw new APIException("Encounter.error.privilege.required.purge", new Object[] { encounter.getEncounterType()
			        .getEditPrivilege() });
		}
		dao.deleteEncounter(encounter);
	}
	
	/**
	 * @see org.openmrs.api.EncounterService#purgeEncounter(Encounter, boolean)
	 */
	public void purgeEncounter(Encounter encounter, boolean cascade) throws APIException {
		
		// if authenticated user is not supposed to edit encounter of certain type
		if (!canEditEncounter(encounter, null)) {
			throw new APIException("Encounter.error.privilege.required.purge", new Object[] { encounter.getEncounterType()
			        .getEditPrivilege() });
		}
		
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
			Set<Order> orders = encounter.getOrders();
			for (Order o : orders) {
				Context.getOrderService().purgeOrder(o);
			}
		}
		Context.getEncounterService().purgeEncounter(encounter);
	}
	
	/**
	 * @see org.openmrs.api.EncounterService#saveEncounterType(org.openmrs.EncounterType)
	 */
	public EncounterType saveEncounterType(EncounterType encounterType) {
		//make sure the user has not turned off encounter types editing
		Context.getEncounterService().checkIfEncounterTypesAreLocked();
		
		dao.saveEncounterType(encounterType);
		return encounterType;
	}
	
	/**
	 * @see org.openmrs.api.EncounterService#getEncounterType(java.lang.Integer)
	 */
	@Transactional(readOnly = true)
	public EncounterType getEncounterType(Integer encounterTypeId) throws APIException {
		return dao.getEncounterType(encounterTypeId);
	}
	
	/**
	 * @see org.openmrs.api.EncounterService#getEncounterType(java.lang.String)
	 */
	@Transactional(readOnly = true)
	public EncounterType getEncounterType(String name) throws APIException {
		return dao.getEncounterType(name);
	}
	
	/**
	 * @see org.openmrs.api.EncounterService#getAllEncounterTypes()
	 */
	@Transactional(readOnly = true)
	public List<EncounterType> getAllEncounterTypes() throws APIException {
		return dao.getAllEncounterTypes(true);
	}
	
	/**
	 * @see org.openmrs.api.EncounterService#getAllEncounterTypes(boolean)
	 */
	@Transactional(readOnly = true)
	public List<EncounterType> getAllEncounterTypes(boolean includeRetired) throws APIException {
		return dao.getAllEncounterTypes(includeRetired);
	}
	
	/**
	 * @see org.openmrs.api.EncounterService#findEncounterTypes(java.lang.String)
	 */
	@Transactional(readOnly = true)
	public List<EncounterType> findEncounterTypes(String name) throws APIException {
		return dao.findEncounterTypes(name);
	}
	
	/**
	 * @see org.openmrs.api.EncounterService#retireEncounterType(EncounterType, String)
	 */
	public EncounterType retireEncounterType(EncounterType encounterType, String reason) throws APIException {
		if (reason == null) {
			throw new IllegalArgumentException("The 'reason' for retiring is required");
		}
		
		//make sure the user has not turned off encounter types editing
		Context.getEncounterService().checkIfEncounterTypesAreLocked();
		
		encounterType.setRetired(true);
		encounterType.setRetireReason(reason);
		return Context.getEncounterService().saveEncounterType(encounterType);
	}
	
	/**
	 * @see org.openmrs.api.EncounterService#unretireEncounterType(org.openmrs.EncounterType)
	 */
	public EncounterType unretireEncounterType(EncounterType encounterType) throws APIException {
		Context.getEncounterService().checkIfEncounterTypesAreLocked();
		
		encounterType.setRetired(false);
		return Context.getEncounterService().saveEncounterType(encounterType);
	}
	
	/**
	 * @see org.openmrs.api.EncounterService#purgeEncounterType(org.openmrs.EncounterType)
	 */
	public void purgeEncounterType(EncounterType encounterType) throws APIException {
		//make sure the user has not turned off encounter types editing
		Context.getEncounterService().checkIfEncounterTypesAreLocked();
		
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
	@Transactional(readOnly = true)
	public List<Encounter> getEncountersByPatientId(Integer patientId, boolean includeVoided) throws APIException {
		return Context.getEncounterService().getEncountersByPatientId(patientId);
	}
	
	/**
	 * @see org.openmrs.api.EncounterService#getEncountersByPatientIdentifier(java.lang.String,
	 *      boolean)
	 * @deprecated replaced by {@link #getEncountersByPatientIdentifier(String)}
	 */
	@Deprecated
	@Transactional(readOnly = true)
	public List<Encounter> getEncountersByPatientIdentifier(String identifier, boolean includeVoided) throws APIException {
		return Context.getEncounterService().getEncountersByPatientIdentifier(identifier);
	}
	
	/**
	 * @see org.openmrs.api.EncounterService#getEncounters(org.openmrs.Patient)
	 * @deprecated replaced by {@link #getEncountersByPatient(Patient patient)}
	 */
	@Deprecated
	@Transactional(readOnly = true)
	public List<Encounter> getEncounters(Patient who) {
		return Context.getEncounterService().filterEncountersByViewPermissions(getEncountersByPatient(who), null);
	}
	
	/**
	 * @see org.openmrs.api.EncounterService#getEncounters(org.openmrs.Patient, boolean)
	 * @deprecated replaced by
	 *             {@link #getEncounters(Patient, Location, Date, Date, Collection, Collection, Collection, boolean)}
	 */
	@Deprecated
	@Transactional(readOnly = true)
	public List<Encounter> getEncounters(Patient who, boolean includeVoided) {
		return Context.getEncounterService().getEncounters(who, null, null, null, null, null, null, includeVoided);
	}
	
	/**
	 * @see org.openmrs.api.EncounterService#getEncounters(org.openmrs.Patient,
	 *      org.openmrs.Location)
	 * @deprecated replaced by
	 *             {@link #getEncounters(Patient, Location, Date, Date, Collection, Collection, Collection, boolean)}
	 */
	@Deprecated
	@Transactional(readOnly = true)
	public List<Encounter> getEncounters(Patient who, Location where) {
		return Context.getEncounterService().getEncounters(who, where, null, null, null, null, null, false);
	}
	
	/**
	 * @see org.openmrs.api.EncounterService#getEncounters(org.openmrs.Patient, java.util.Date,
	 *      java.util.Date)
	 * @deprecated replaced by
	 *             {@link #getEncounters(Patient, Location, Date, Date, Collection, Collection, Collection, boolean)}
	 */
	@Deprecated
	@Transactional(readOnly = true)
	public List<Encounter> getEncounters(Patient who, Date fromDate, Date toDate) {
		return Context.getEncounterService().getEncounters(who, null, fromDate, toDate, null, null, null, false);
	}
	
	/**
	 * @see org.openmrs.api.EncounterService#getEncounters(java.util.Date, java.util.Date)
	 * @deprecated replaced by
	 *             {@link #getEncounters(Patient, Location, Date, Date, Collection, Collection, Collection, boolean)}
	 */
	@Deprecated
	@Transactional(readOnly = true)
	public Collection<Encounter> getEncounters(Date fromDate, Date toDate) {
		return Context.getEncounterService().getEncounters(null, null, fromDate, toDate, null, null, null, false);
	}
	
	/**
	 * @see org.openmrs.api.EncounterService#getEncounters(org.openmrs.Location, java.util.Date,
	 *      java.util.Date)
	 * @deprecated replaced by
	 *             {@link #getEncounters(Patient, Location, Date, Date, Collection, Collection, Collection, boolean)}
	 */
	@Deprecated
	@Transactional(readOnly = true)
	public List<Encounter> getEncounters(Location loc, Date fromDate, Date toDate) {
		return Context.getEncounterService().getEncounters(null, loc, fromDate, toDate, null, null, null, false);
	}
	
	/**
	 * @see org.openmrs.api.EncounterService#getEncounterTypes()
	 * @deprecated replaced by {@link #getAllEncounterTypes()}
	 */
	@Deprecated
	@Transactional(readOnly = true)
	public List<EncounterType> getEncounterTypes() {
		return Context.getEncounterService().getAllEncounterTypes();
	}
	
	/**
	 * @see org.openmrs.api.EncounterService#getLocations()
	 * @deprecated use LocationService.getAllLocations()
	 */
	@Deprecated
	@Transactional(readOnly = true)
	public List<Location> getLocations() throws APIException {
		return Context.getLocationService().getAllLocations();
	}
	
	/**
	 * @see org.openmrs.api.EncounterService#getLocation(java.lang.Integer)
	 * @deprecated use LocationService.getLocation(locationId)
	 */
	@Deprecated
	@Transactional(readOnly = true)
	public Location getLocation(Integer locationId) throws APIException {
		return Context.getLocationService().getLocation(locationId);
	}
	
	/**
	 * @see org.openmrs.api.EncounterService#getLocationByName(java.lang.String)
	 * @deprecated use LocationService.getLocation(name)
	 */
	@Deprecated
	@Transactional(readOnly = true)
	public Location getLocationByName(String name) throws APIException {
		return Context.getLocationService().getLocation(name);
	}
	
	/**
	 * @see org.openmrs.api.EncounterService#findLocations(java.lang.String)
	 * @deprecated use LocationService.getLocations(name)
	 */
	@Deprecated
	@Transactional(readOnly = true)
	public List<Location> findLocations(String name) throws APIException {
		return Context.getLocationService().getLocations(name);
	}
	
	/**
	 * @see org.openmrs.api.EncounterService#getEncounterByUuid(java.lang.String)
	 */
	@Transactional(readOnly = true)
	public Encounter getEncounterByUuid(String uuid) throws APIException {
		return dao.getEncounterByUuid(uuid);
	}
	
	/**
	 * @see org.openmrs.api.EncounterService#getEncounterTypeByUuid(java.lang.String)
	 */
	@Transactional(readOnly = true)
	public EncounterType getEncounterTypeByUuid(String uuid) throws APIException {
		return dao.getEncounterTypeByUuid(uuid);
	}
	
	/**
	 * @see org.openmrs.api.EncounterService#getAllEncounters(org.openmrs.Cohort)
	 */
	@Override
	@Transactional(readOnly = true)
	public Map<Integer, List<Encounter>> getAllEncounters(Cohort patients) {
		return dao.getAllEncounters(patients);
	}
	
	/**
	 * @see org.openmrs.api.EncounterService#getEncounters(java.lang.String, java.lang.Integer,
	 *      java.lang.Integer, boolean)
	 */
	@Override
	@Transactional(readOnly = true)
	public List<Encounter> getEncounters(String query, Integer start, Integer length, boolean includeVoided)
	        throws APIException {
		return Context.getEncounterService().filterEncountersByViewPermissions(
		    dao.getEncounters(query, null, start, length, includeVoided), null);
	}
	
	/**
	 * @see org.openmrs.api.EncounterService#getEncounters(java.lang.String, java.lang.Integer,
	 *      java.lang.Integer, java.lang.Integer, boolean)
	 */
	@Override
	@Transactional(readOnly = true)
	public List<Encounter> getEncounters(String query, Integer patientId, Integer start, Integer length,
	        boolean includeVoided) throws APIException {
		return Context.getEncounterService().filterEncountersByViewPermissions(
		    dao.getEncounters(query, patientId, start, length, includeVoided), null);
	}
	
	/**
	 * @see org.openmrs.api.EncounterService#getCountOfEncounters(java.lang.String, boolean)
	 */
	@Override
	@Transactional(readOnly = true)
	public Integer getCountOfEncounters(String query, boolean includeVoided) {
		return OpenmrsUtil.convertToInteger(dao.getCountOfEncounters(query, null, includeVoided));
	}
	
	/**
	 * @see EncounterService#getEncountersByVisit(Visit, boolean)
	 */
	@Override
	@Transactional(readOnly = true)
	public List<Encounter> getEncountersByVisit(Visit visit, boolean includeVoided) {
		return Context.getEncounterService().filterEncountersByViewPermissions(
		    dao.getEncountersByVisit(visit, includeVoided), null);
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<EncounterVisitHandler> getEncounterVisitHandlers() {
		List<EncounterVisitHandler> handlers = HandlerUtil.getHandlersForType(EncounterVisitHandler.class, null);
		
		return handlers;
	}
	
	/**
	 * @see org.openmrs.api.EncounterService#getActiveEncounterVisitHandler()
	 */
	@Override
	@Transactional(readOnly = true)
	public EncounterVisitHandler getActiveEncounterVisitHandler() throws APIException {
		
		String handlerGlobalValue = Context.getAdministrationService().getGlobalProperty(
		    OpenmrsConstants.GP_VISIT_ASSIGNMENT_HANDLER, null);
		
		if (StringUtils.isBlank(handlerGlobalValue)) {
			return null;
		}
		
		EncounterVisitHandler handler = null;
		
		// convention = [NamePrefix:beanName] or [className]
		String namePrefix = OpenmrsConstants.REGISTERED_COMPONENT_NAME_PREFIX;
		
		if (handlerGlobalValue.startsWith(namePrefix)) {
			String beanName = handlerGlobalValue.substring(namePrefix.length());
			
			handler = Context.getRegisteredComponent(beanName, EncounterVisitHandler.class);
		} else {
			Object instance;
			
			try {
				instance = OpenmrsClassLoader.getInstance().loadClass(handlerGlobalValue).newInstance();
			}
			catch (Exception ex) {
				throw new APIException("failed.instantiate.assignment.handler", new Object[] { handlerGlobalValue }, ex);
			}
			
			if (instance instanceof EncounterVisitHandler) {
				handler = (EncounterVisitHandler) instance;
			} else {
				throw new APIException("assignment.handler.should.implement.EncounterVisitHandler", (Object[]) null);
			}
		}
		
		return handler;
	}
	
	/**
	 * @see org.openmrs.api.EncounterService#saveEncounterRole(org.openmrs.EncounterRole)
	 */
	@Override
	public EncounterRole saveEncounterRole(EncounterRole encounterRole) throws APIException {
		dao.saveEncounterRole(encounterRole);
		return encounterRole;
	}
	
	/**
	 * @see org.openmrs.api.EncounterService#getEncounterRole(Integer)
	 */
	@Override
	@Transactional(readOnly = true)
	public EncounterRole getEncounterRole(Integer encounterRoleId) throws APIException {
		return dao.getEncounterRole(encounterRoleId);
	}
	
	/**
	 * @see org.openmrs.api.EncounterService#purgeEncounterRole(org.openmrs.EncounterRole)
	 */
	@Override
	public void purgeEncounterRole(EncounterRole encounterRole) throws APIException {
		dao.deleteEncounterRole(encounterRole);
	}
	
	/**
	 * @see org.openmrs.api.EncounterService#getAllEncounterRoles(boolean)
	 */
	@Override
	@Transactional(readOnly = true)
	public List<EncounterRole> getAllEncounterRoles(boolean includeRetired) {
		return dao.getAllEncounterRoles(includeRetired);
	}
	
	/**
	 * @see org.openmrs.api.EncounterService#getEncounterRoleByUuid(String)
	 */
	@Override
	@Transactional(readOnly = true)
	public EncounterRole getEncounterRoleByUuid(String uuid) throws APIException {
		return dao.getEncounterRoleByUuid(uuid);
	}
	
	/**
	 * @see org.openmrs.api.EncounterService#getEncounterRoleByName(String)
	 */
	@Override
	public EncounterRole getEncounterRoleByName(String name) {
		return dao.getEncounterRoleByName(name);
	}
	
	/**
	 * @see org.openmrs.api.EncounterService#retireEncounterRole(org.openmrs.EncounterRole, String)
	 */
	@Override
	public EncounterRole retireEncounterRole(EncounterRole encounterRole, String reason) throws APIException {
		if (reason == null) {
			throw new IllegalArgumentException("The 'reason' for retiring is required");
		}
		return Context.getEncounterService().saveEncounterRole(encounterRole);
	}
	
	/**
	 * @see org.openmrs.api.EncounterService#unretireEncounterRole(org.openmrs.EncounterRole)
	 */
	@Override
	public EncounterRole unretireEncounterRole(EncounterRole encounterRole) throws APIException {
		return Context.getEncounterService().saveEncounterRole(encounterRole);
	}
	
	/**
	 * @see org.openmrs.api.EncounterService#getEncountersNotAssignedToAnyVisit(org.openmrs.Patient)
	 */
	@Override
	@Transactional(readOnly = true)
	public List<Encounter> getEncountersNotAssignedToAnyVisit(Patient patient) throws APIException {
		return Context.getEncounterService().filterEncountersByViewPermissions(
		    dao.getEncountersNotAssignedToAnyVisit(patient), null);
	}
	
	/**
	 * @see org.openmrs.api.EncounterService#getEncountersByVisitsAndPatient(org.openmrs.Patient,
	 *      boolean, java.lang.String, java.lang.Integer, java.lang.Integer)
	 */
	@Override
	@Transactional(readOnly = true)
	public List<Encounter> getEncountersByVisitsAndPatient(Patient patient, boolean includeVoided, String query,
	        Integer start, Integer length) throws APIException {
		return Context.getEncounterService().filterEncountersByViewPermissions(
		    dao.getEncountersByVisitsAndPatient(patient, includeVoided, query, start, length), null);
	}
	
	/**
	 * @see org.openmrs.api.EncounterService#getEncountersByVisitsAndPatientCount(org.openmrs.Patient,
	 *      boolean, java.lang.String)
	 */
	@Override
	@Transactional(readOnly = true)
	public Integer getEncountersByVisitsAndPatientCount(Patient patient, boolean includeVoided, String query)
	        throws APIException {
		return dao.getEncountersByVisitsAndPatientCount(patient, includeVoided, query);
	}
	
	/**
	 * @see org.openmrs.api.EncounterService#filterEncountersByViewPermissions(java.util.List,
	 *      org.openmrs.User)
	 */
	@Override
	@Transactional(readOnly = true)
	public List<Encounter> filterEncountersByViewPermissions(List<Encounter> encounters, User user) {
		if (encounters != null) {
			// if user is not specified then use authenticated user from context by default
			if (user == null) {
				user = Context.getAuthenticatedUser();
			}
			for (Iterator<Encounter> iterator = encounters.iterator(); iterator.hasNext();) {
				Encounter encounter = iterator.next();
				// determine whether it's need to include this encounter into result or not
				// as it can be not accessed by current user due to permissions lack
				EncounterType et = encounter.getEncounterType();
				if (et != null && !userHasEncounterPrivilege(et.getViewPrivilege(), user)) {
					// exclude this encounter from result
					iterator.remove();
				}
			}
		}
		return encounters;
	}
	
	/**
	 * @see org.openmrs.api.EncounterService#canViewAllEncounterTypes(org.openmrs.User)
	 */
	@Override
	@Transactional(readOnly = true)
	public boolean canViewAllEncounterTypes(User subject) {
		boolean canView = Boolean.TRUE;
		for (EncounterType et : Context.getEncounterService().getAllEncounterTypes()) {
			if (!userHasEncounterPrivilege(et.getViewPrivilege(), subject)) {
				canView = Boolean.FALSE;
				break;
			}
		}
		return canView;
	}
	
	/**
	 * @see org.openmrs.api.EncounterService#canEditAllEncounterTypes(org.openmrs.User)
	 */
	@Override
	@Transactional(readOnly = true)
	public boolean canEditAllEncounterTypes(User subject) {
		boolean canEdit = Boolean.TRUE;
		for (EncounterType et : Context.getEncounterService().getAllEncounterTypes()) {
			if (!userHasEncounterPrivilege(et.getEditPrivilege(), subject)) {
				canEdit = Boolean.FALSE;
				break;
			}
		}
		return canEdit;
	}
	
	/**
	 * @see org.openmrs.api.EncounterService#canEditEncounter(org.openmrs.Encounter,
	 *      org.openmrs.User)
	 */
	@Override
	public boolean canEditEncounter(Encounter encounter, User user) {
		// if passed in encounter is null raise an exception
		if (encounter == null) {
			throw new IllegalArgumentException("The encounter argument can not be null");
		}
		// since we restrict by encounter type, if it does not exist, then anyone is allowed to edit the encounter
		if (encounter.getEncounterType() == null) {
			return Boolean.TRUE;
		}
		// if user is not specified, then use authenticated user from context by default
		if (user == null) {
			user = Context.getAuthenticatedUser();
		}
		
		return userHasEncounterPrivilege(encounter.getEncounterType().getEditPrivilege(), user);
	}
	
	/**
	 * @see org.openmrs.api.EncounterService#canViewEncounter(org.openmrs.Encounter,
	 *      org.openmrs.User)
	 */
	@Override
	public boolean canViewEncounter(Encounter encounter, User user) {
		// if passed in encounter is null raise an exception
		if (encounter == null) {
			throw new IllegalArgumentException("The encounter argument can not be null");
		}
		// since we restrict by encounter type, if it does not exist, then anyone is allowed to view the encounter
		if (encounter.getEncounterType() == null) {
			return Boolean.TRUE;
		}
		// if user is not specified, then use authenticated user from context by default
		if (user == null) {
			user = Context.getAuthenticatedUser();
		}
		
		return userHasEncounterPrivilege(encounter.getEncounterType().getViewPrivilege(), user);
	}
	
	/**
	 * Convenient method that safely checks if user has given encounter privilege
	 * 
	 * @param privilege the privilege to test
	 * @param user the user instance to check if it has given privilege
	 * @return true if given user has specified privilege
	 */
	private boolean userHasEncounterPrivilege(Privilege privilege, User user) {
		//If the encounter privilege is null, everyone can see and edit the encounter.
		if (privilege == null) {
			return true;
		}
		
		return user.hasPrivilege(privilege.getPrivilege());
	}
	
	/**
	 * @see org.openmrs.api.EncounterService#checkIfEncounterTypesAreLocked()
	 */
	@Transactional(readOnly = true)
	public void checkIfEncounterTypesAreLocked() {
		String locked = Context.getAdministrationService().getGlobalProperty(
		    OpenmrsConstants.GLOBAL_PROPERTY_ENCOUNTER_TYPES_LOCKED, "false");
		if (locked.toLowerCase().equals("true")) {
			throw new EncounterTypeLockedException();
		}
	}
	
	/**
	 * @see org.openmrs.api.EncounterService#getEncounterRolesByName(String)
	 */
	
	@Override
	public List<EncounterRole> getEncounterRolesByName(String name) {
		return dao.getEncounterRolesByName(name);
	}
	
	@Override
	public Encounter transferEncounter(Encounter encounter, Patient patient) {
		Encounter encounterCopy = encounter.copyAndAssignToAnotherPatient(patient);
		
		voidEncounter(encounter, "transfer to patient: id = " + patient.getId());
		
		//void visit if voided encounter is the only one
		Visit visit = encounter.getVisit();
		if (visit != null && visit.getEncounters().size() == 1) {
			Context.getVisitService().voidVisit(visit, "Visit does not contain non-voided encounters");
		}
		
		return saveEncounter(encounterCopy);
	}
}
