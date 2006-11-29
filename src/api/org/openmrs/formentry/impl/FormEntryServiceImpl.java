package org.openmrs.formentry.impl;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Form;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifierType;
import org.openmrs.Role;
import org.openmrs.Tribe;
import org.openmrs.User;
import org.openmrs.api.APIAuthenticationException;
import org.openmrs.api.APIException;
import org.openmrs.api.EncounterService;
import org.openmrs.api.PatientService;
import org.openmrs.api.UserService;
import org.openmrs.api.context.Context;
import org.openmrs.formentry.FormEntryArchive;
import org.openmrs.formentry.FormEntryConstants;
import org.openmrs.formentry.FormEntryError;
import org.openmrs.formentry.FormEntryQueue;
import org.openmrs.formentry.FormEntryService;
import org.openmrs.formentry.db.FormEntryDAO;
import org.openmrs.util.OpenmrsConstants;

/**
 * Data entry-related services
 * 
 * @author Ben Wolfe
 * @author Burke Mamlin
 * @version 1.0
 */
public class FormEntryServiceImpl implements FormEntryService {

	private Log log = LogFactory.getLog(this.getClass());
	
	private FormEntryDAO dao;
	
	public FormEntryServiceImpl() { }
	
	private FormEntryDAO getFormEntryDAO() {
		return dao;
	}
	
	public void setFormEntryDAO(FormEntryDAO dao) {
		this.dao = dao;
	}

	private PatientService getPatientService() {
		checkPrivilege(OpenmrsConstants.PRIV_FORM_ENTRY);
		return Context.getPatientService();
	}
	
	private EncounterService getEncounterService() {
		checkPrivilege(OpenmrsConstants.PRIV_FORM_ENTRY);
		return Context.getEncounterService();
	}

	/**
	 * @see org.openmrs.api.PatientService.createPatient(org.openmrs.Patient)
	 */
	public void createPatient(Patient patient) throws APIException {
		Context.addProxyPrivilege(OpenmrsConstants.PRIV_ADD_PATIENTS);
		try {
			getPatientService().createPatient(patient);
		} finally {
			Context.removeProxyPrivilege(OpenmrsConstants.PRIV_ADD_PATIENTS);
		}
	}

	/**
	 * @see org.openmrs.api.PatientService.getPatient(java.lang.Integer)
	 */
	public Patient getPatient(Integer patientId) throws APIException {
		Context.addProxyPrivilege(OpenmrsConstants.PRIV_VIEW_PATIENTS);
		Patient p;
		try {
			p = getPatientService().getPatient(patientId);
		} finally {
			Context.removeProxyPrivilege(OpenmrsConstants.PRIV_VIEW_PATIENTS);
		}
		return p;
	}

	/**
	 * @see org.openmrs.api.PatientService.updatePatient(org.openmrs.Patient)
	 */
	public void updatePatient(Patient patient) throws APIException {
		Context.addProxyPrivilege(OpenmrsConstants.PRIV_EDIT_PATIENTS);
		try {
			getPatientService().updatePatient(patient);
		} finally {
			Context.removeProxyPrivilege(OpenmrsConstants.PRIV_EDIT_PATIENTS);
		}
	}

	/**
	 * @see org.openmrs.api.PatientService.getPatientsByIdentifier(java.lang.String,boolean)
	 */
	public Set<Patient> getPatientsByIdentifier(String identifier,
			boolean includeVoided) throws APIException {
		Context.addProxyPrivilege(OpenmrsConstants.PRIV_VIEW_PATIENTS);
		Set<Patient> p;
		try {
			p = getPatientService().getPatientsByIdentifier(identifier,
					includeVoided);
		} finally {
			Context.removeProxyPrivilege(OpenmrsConstants.PRIV_VIEW_PATIENTS);
		}
		return p;
	}

	/**
	 * @see org.openmrs.api.PatientService.getPatientsByName(java.lang.String)
	 */
	public Set<Patient> getPatientsByName(String name) throws APIException {
		Context.addProxyPrivilege(OpenmrsConstants.PRIV_VIEW_PATIENTS);
		Set<Patient> p;
		try {
			p = getPatientsByName(name, false);
		} finally {
			Context.removeProxyPrivilege(OpenmrsConstants.PRIV_VIEW_PATIENTS);
		}
		return p;
	}

	/**
	 * @see org.openmrs.api.PatientService.getPatientsByName(java.lang.String,boolean)
	 */
	public Set<Patient> getPatientsByName(String name, boolean includeVoided)
			throws APIException {
		Context.addProxyPrivilege(OpenmrsConstants.PRIV_VIEW_PATIENTS);
		Set<Patient> p;
		try {
			p = getPatientService().getPatientsByName(name, includeVoided);
		} finally {
			Context.removeProxyPrivilege(OpenmrsConstants.PRIV_VIEW_PATIENTS);
		}
		return p;
	}

	/**
	 * @see org.openmrs.api.PatientService.getSimilarPatients(java.lang.String,java.lang.Integer,java.lang.String)
	 */
	public Set<Patient> getSimilarPatients(String name, Integer birthyear,
			String gender) throws APIException {
		Context.addProxyPrivilege(OpenmrsConstants.PRIV_VIEW_PATIENTS);
		Set<Patient> p;
		try {
			p = getPatientService().getSimilarPatients(name, birthyear, gender);
		} finally {
			Context.removeProxyPrivilege(OpenmrsConstants.PRIV_VIEW_PATIENTS);
		}
		return p;
	}

	/**
	 * @see org.openmrs.api.PatientService.getPatientIdentifierTypes()
	 */
	public List<PatientIdentifierType> getPatientIdentifierTypes()
			throws APIException {
		Context.addProxyPrivilege(OpenmrsConstants.PRIV_VIEW_PATIENTS);
		List<PatientIdentifierType> p;
		try {
			p = getPatientService().getPatientIdentifierTypes();
		} finally {
			Context.removeProxyPrivilege(OpenmrsConstants.PRIV_VIEW_PATIENTS);
		}
		return p;
	}

	/**
	 * @see org.openmrs.api.PatientService.getPatientIdentifierType(java.lang.Integer)
	 */
	public PatientIdentifierType getPatientIdentifierType(
			Integer patientIdentifierTypeId) throws APIException {
		Context.addProxyPrivilege(OpenmrsConstants.PRIV_VIEW_PATIENTS);
		PatientIdentifierType p;
		try {
			p = getPatientService().getPatientIdentifierType(
					patientIdentifierTypeId);
		} finally {
			Context.removeProxyPrivilege(OpenmrsConstants.PRIV_VIEW_PATIENTS);
		}
		return p;
	}

	/**
	 * @see org.openmrs.api.PatientService.getTribe(java.lang.Integer)
	 */
	public Tribe getTribe(Integer tribeId) throws APIException {
		Context.addProxyPrivilege(OpenmrsConstants.PRIV_VIEW_PATIENTS);
		Tribe t;
		try {
			t = getPatientService().getTribe(tribeId);
		} finally {
			Context.removeProxyPrivilege(OpenmrsConstants.PRIV_VIEW_PATIENTS);
		}
		return t;
	}

	/**
	 * @see org.openmrs.api.PatientService.getTribes()
	 */
	public List<Tribe> getTribes() throws APIException {
		Context.addProxyPrivilege(OpenmrsConstants.PRIV_VIEW_PATIENTS);
		List<Tribe> t;
		try {
			t = getPatientService().getTribes();
		} finally {
			Context.removeProxyPrivilege(OpenmrsConstants.PRIV_VIEW_PATIENTS);
		}
		return t;
	}
	
	/**
	 * @see org.openmrs.api.PatientService.findTribes(java.lang.String)
	 */
	public List<Tribe> findTribes(String s) throws APIException {
		Context.addProxyPrivilege(OpenmrsConstants.PRIV_VIEW_PATIENTS);
		List<Tribe> t;
		try {
			t = getPatientService().findTribes(s);
		} finally {
			Context.removeProxyPrivilege(OpenmrsConstants.PRIV_VIEW_PATIENTS);
		}
		return t;
	}

	/**
	 * @see org.openmrs.api.PatientService.getLocations()
	 */
	public List<Location> getLocations() throws APIException {
		Context.addProxyPrivilege(OpenmrsConstants.PRIV_VIEW_PATIENTS);
		List<Location> t;
		try {
			t = getPatientService().getLocations();
		} finally {
			Context.removeProxyPrivilege(OpenmrsConstants.PRIV_VIEW_PATIENTS);
		}
		return t;
	}
	
	/**
	 * @see org.openmrs.api.EncounterService.findLocations()
	 */
	public List<Location> findLocations(String txt) throws APIException {
		Context.addProxyPrivilege(OpenmrsConstants.PRIV_VIEW_ENCOUNTERS);
		List<Location> locs;
		try {
			locs = getEncounterService().findLocations(txt);
		} finally {
			Context.removeProxyPrivilege(OpenmrsConstants.PRIV_VIEW_ENCOUNTERS);
		}
		return locs;
	}

	/**
	 * @see org.openmrs.api.PatientService.getLocation(java.lang.Integer)
	 */
	public Location getLocation(Integer locationId) throws APIException {
		Context.addProxyPrivilege(OpenmrsConstants.PRIV_VIEW_PATIENTS);
		Location t;
		try {
			t = getPatientService().getLocation(locationId);
		} finally {
			Context.removeProxyPrivilege(OpenmrsConstants.PRIV_VIEW_PATIENTS);
		}
		return t;
	}

	/**
	 * @see org.openmrs.api.PatientService.findPatients(java.lang.String,boolean)
	 */
	public List<Patient> findPatients(String query, boolean includeVoided) {

		List<Patient> patients;
		Context.addProxyPrivilege(OpenmrsConstants.PRIV_VIEW_PATIENTS);
		try {
			patients = getPatientService().findPatients(query, includeVoided);
		} finally {
			Context.removeProxyPrivilege(OpenmrsConstants.PRIV_VIEW_PATIENTS);
		}
		return patients;
	}

	/**
	 * @see org.openmrs.api.FormService.getForm(java.lang.Integer)
	 */
	public Form getForm(Integer formId) {

		Form form;
		Context.addProxyPrivilege(OpenmrsConstants.PRIV_VIEW_FORMS);
		try {
			form = Context.getFormService().getForm(formId);
		} finally {
			Context.removeProxyPrivilege(OpenmrsConstants.PRIV_VIEW_FORMS);
		}
		return form;
	}
	
	public Collection<Form> getForms() {
		return getForms(true);
	}
	
	public Collection<Form> getForms(boolean onlyPublished) {
		if (!Context.hasPrivilege(OpenmrsConstants.PRIV_FORM_ENTRY))
			throw new APIAuthenticationException("Privilege required: "
					+ OpenmrsConstants.PRIV_FORM_ENTRY);
		
		List<Form> forms = new Vector<Form>();
		Context.addProxyPrivilege(OpenmrsConstants.PRIV_VIEW_FORMS);
		try {
			forms = Context.getFormService().getForms(onlyPublished);
		} catch (Exception e) {
			log.error(e);
		} finally {
			Context.removeProxyPrivilege(OpenmrsConstants.PRIV_VIEW_FORMS);
		}
		return forms;
	}
	
	public User getUserByUsername(String username) {
		if (!Context.hasPrivilege(OpenmrsConstants.PRIV_FORM_ENTRY))
			throw new APIAuthenticationException("Privilege required: "
					+ OpenmrsConstants.PRIV_FORM_ENTRY);
		
		Context.addProxyPrivilege(OpenmrsConstants.PRIV_VIEW_USERS);
		User ret = null;
		try {
			ret = Context.getUserService().getUserByUsername(username);
		} catch (Exception e) {
			log.error(e);
		} finally {
			Context.removeProxyPrivilege(OpenmrsConstants.PRIV_VIEW_USERS);
		}
		return ret;
	}

	/**
	 * @see org.openmrs.api.UserService.findUsers(String, List<String>, boolean)
	 */
	public Collection<User> findUsers(String searchValue, List<String> roles,
			boolean includeVoided) {
		if (!Context.hasPrivilege(OpenmrsConstants.PRIV_FORM_ENTRY))
			throw new APIAuthenticationException("Privilege required: "
					+ OpenmrsConstants.PRIV_FORM_ENTRY);
		
		List<User> users = new Vector<User>();
		Context.addProxyPrivilege(OpenmrsConstants.PRIV_VIEW_USERS);
		try {
			users = Context.getUserService().findUsers(searchValue, roles, includeVoided);
		} catch (Exception e) {
			log.error(e);
		} finally {
			Context.removeProxyPrivilege(OpenmrsConstants.PRIV_VIEW_USERS);
		}
		return users;
	}

	/**
	 * @see org.openmrs.api.UserService.getAllUsers(List<String>, boolean)
	 */
	public Collection<User> getAllUsers(List<String> strRoles,
			boolean includeVoided) {
		if (!Context.hasPrivilege(OpenmrsConstants.PRIV_FORM_ENTRY))
			throw new APIAuthenticationException("Privilege required: "
					+ OpenmrsConstants.PRIV_FORM_ENTRY);
		
		// default return list
		List<User> users = new Vector<User>();
		
		// all formentry users need this priv to read in users
		Context.addProxyPrivilege(OpenmrsConstants.PRIV_VIEW_USERS);
		
		UserService us = Context.getUserService();
		try {
			List<Role> roles = new Vector<Role>();
			for (String r : strRoles) {
				Role role = us.getRole(r);
				if (role != null)
					roles.add(role);
			}
			
			users = us.getAllUsers(roles, includeVoided);
		} catch (Exception e) {
			log.error("Error while getting users by role", e);
		} finally {
			Context.removeProxyPrivilege(OpenmrsConstants.PRIV_VIEW_USERS);
		}
		
		return users;
	}
	
	public SortedMap<String,String> getSystemVariables() {
		if (!Context.hasPrivilege(OpenmrsConstants.PRIV_VIEW_ADMIN_FUNCTIONS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_VIEW_ADMIN_FUNCTIONS);
		
		TreeMap<String,String> systemVariables = new TreeMap<String,String>();
		systemVariables.put("FORMENTRY_INFOPATH_PUBLISH_PATH", String.valueOf(FormEntryConstants.FORMENTRY_INFOPATH_PUBLISH_PATH));
		systemVariables.put("FORMENTRY_INFOPATH_TASKPANE_INITIAL_PATH", String.valueOf(FormEntryConstants.FORMENTRY_INFOPATH_TASKPANE_INITIAL_PATH));
		systemVariables.put("FORMENTRY_INFOPATH_SUBMIT_PATH", String.valueOf(FormEntryConstants.FORMENTRY_INFOPATH_SUBMIT_PATH));
		
		// the other formentry system variables (the editable ones) are located in global properties
		
		return systemVariables;
	}
	
	private void checkPrivilege(String privilege) {
		if (!Context.hasPrivilege(privilege))
			throw new APIAuthenticationException("Privilege required: " + privilege);		
	}
	
	private void checkPrivileges(String privilegeA, String privilegeB) {
		boolean hasA = Context.hasPrivilege(privilegeA);
		boolean hasB = Context.hasPrivilege(privilegeB);
		if (!hasA && !hasB) {
			if (!hasA)
				throw new APIAuthenticationException("Privilege required: " + privilegeA);
			else
				throw new APIAuthenticationException("Privilege required: " + privilegeB);
		}
	}

	/***************************************************************************
	 * FormEntryQueue Service Methods
	 **************************************************************************/
	
	public void createFormEntryQueue(FormEntryQueue formEntryQueue) {
		checkPrivileges(FormEntryConstants.PRIV_ADD_FORMENTRY_QUEUE,
				OpenmrsConstants.PRIV_FORM_ENTRY);
		formEntryQueue.setCreator(Context.getAuthenticatedUser());
		formEntryQueue.setDateCreated(new Date());
		getFormEntryDAO().createFormEntryQueue(formEntryQueue);
	}

	public void updateFormEntryQueue(FormEntryQueue formEntryQueue) {
		checkPrivilege(FormEntryConstants.PRIV_EDIT_FORMENTRY_QUEUE);
		getFormEntryDAO().updateFormEntryQueue(formEntryQueue);
	}

	public FormEntryQueue getFormEntryQueue(int formEntryQueueId) {
		checkPrivilege(FormEntryConstants.PRIV_VIEW_FORMENTRY_QUEUE);
		return getFormEntryDAO().getFormEntryQueue(formEntryQueueId);
	}

	public Collection<FormEntryQueue> getFormEntryQueues() {
		checkPrivilege(FormEntryConstants.PRIV_VIEW_FORMENTRY_QUEUE);
		return getFormEntryDAO().getFormEntryQueues();
	}
	
	public FormEntryQueue getNextFormEntryQueue() {
		checkPrivilege(FormEntryConstants.PRIV_VIEW_FORMENTRY_QUEUE);
		return getFormEntryDAO().getNextFormEntryQueue();
	}

	public void deleteFormEntryQueue(FormEntryQueue formEntryQueue) {
		checkPrivilege(FormEntryConstants.PRIV_DELETE_FORMENTRY_QUEUE);
		getFormEntryDAO().deleteFormEntryQueue(formEntryQueue);
	}
	
	public Integer getFormEntryQueueSize() {
		checkPrivilege(FormEntryConstants.PRIV_VIEW_FORMENTRY_ERROR);
		return getFormEntryDAO().getFormEntryQueueSize();
	}
	
	
	public void createFormEntryArchive(FormEntryArchive formEntryArchive) {
		checkPrivilege(FormEntryConstants.PRIV_ADD_FORMENTRY_ARCHIVE);
		formEntryArchive.setCreator(Context.getAuthenticatedUser());
		formEntryArchive.setDateCreated(new Date());
		getFormEntryDAO().createFormEntryArchive(formEntryArchive);
	}
	
	public FormEntryArchive getFormEntryArchive(Integer formEntryArchive) {
		checkPrivilege(FormEntryConstants.PRIV_VIEW_FORMENTRY_ARCHIVE);
		return getFormEntryDAO().getFormEntryArchive(formEntryArchive);
	}
	
	public Collection<FormEntryArchive> getFormEntryArchives() {
		checkPrivilege(FormEntryConstants.PRIV_VIEW_FORMENTRY_ARCHIVE);
		return getFormEntryDAO().getFormEntryArchives();
	}
	
	public void deleteFormEntryArchive(FormEntryArchive formEntryArchive) {
		checkPrivilege(FormEntryConstants.PRIV_DELETE_FORMENTRY_ARCHIVE);
		getFormEntryDAO().deleteFormEntryArchive(formEntryArchive);
	}
	
	public Integer getFormEntryArchiveSize() {
		checkPrivilege(FormEntryConstants.PRIV_VIEW_FORMENTRY_ERROR);
		return getFormEntryDAO().getFormEntryArchiveSize();
	}
	
	
	public void createFormEntryError(FormEntryError formEntryError) {
		checkPrivilege(FormEntryConstants.PRIV_ADD_FORMENTRY_ERROR);
		formEntryError.setCreator(Context.getAuthenticatedUser());
		formEntryError.setDateCreated(new Date());
		getFormEntryDAO().createFormEntryError(formEntryError);
	}
	
	public FormEntryError getFormEntryError(Integer formEntryErrorId) {
		checkPrivilege(FormEntryConstants.PRIV_VIEW_FORMENTRY_ERROR);
		return getFormEntryDAO().getFormEntryError(formEntryErrorId);
	}
	
	public Collection<FormEntryError> getFormEntryErrors() {
		checkPrivilege(FormEntryConstants.PRIV_VIEW_FORMENTRY_ERROR);
		return getFormEntryDAO().getFormEntryErrors();
	}
	
	public void updateFormEntryError(FormEntryError formEntryError) {
		checkPrivilege(FormEntryConstants.PRIV_EDIT_FORMENTRY_ERROR);
		getFormEntryDAO().updateFormEntryError(formEntryError);
	}
	
	public void deleteFormEntryError(FormEntryError formEntryError) {
		checkPrivilege(FormEntryConstants.PRIV_DELETE_FORMENTRY_ERROR);
		getFormEntryDAO().deleteFormEntryError(formEntryError);
	}
	
	public Integer getFormEntryErrorSize() {
		checkPrivilege(FormEntryConstants.PRIV_VIEW_FORMENTRY_ERROR);
		return getFormEntryDAO().getFormEntryErrorSize();
	}
	
	public void garbageCollect() {
		getFormEntryDAO().garbageCollect();
	}

}
