package org.openmrs.formentry;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Form;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifierType;
import org.openmrs.Tribe;
import org.openmrs.User;
import org.openmrs.api.APIAuthenticationException;
import org.openmrs.api.APIException;
import org.openmrs.api.EncounterService;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.DAOContext;
import org.openmrs.formentry.db.FormEntryDAO;
import org.openmrs.util.OpenmrsConstants;

/**
 * Data entry-related services
 * 
 * @author Ben Wolfe
 * @author Burke Mamlin
 * @version 1.0
 */
public class FormEntryService {

	private Log log = LogFactory.getLog(this.getClass());

	private Context context;
	private DAOContext daoContext;

	public FormEntryService(Context c, DAOContext d) {
		this.context = c;
		this.daoContext = d;
	}
	
	private FormEntryDAO dao() {
		return daoContext.getFormEntryDAO();
	}

	private PatientService getPatientService() {
		checkPrivilege(OpenmrsConstants.PRIV_FORM_ENTRY);
		return context.getPatientService();
	}
	
	private EncounterService getEncounterService() {
		checkPrivilege(OpenmrsConstants.PRIV_FORM_ENTRY);
		return context.getEncounterService();
	}

	/**
	 * @see org.openmrs.api.PatientService.createPatient(org.openmrs.Patient)
	 */
	public void createPatient(Patient patient) throws APIException {
		context.addProxyPrivilege(OpenmrsConstants.PRIV_ADD_PATIENTS);
		try {
			getPatientService().createPatient(patient);
		} finally {
			context.removeProxyPrivilege(OpenmrsConstants.PRIV_ADD_PATIENTS);
		}
	}

	/**
	 * @see org.openmrs.api.PatientService.getPatient(java.lang.Integer)
	 */
	public Patient getPatient(Integer patientId) throws APIException {
		context.addProxyPrivilege(OpenmrsConstants.PRIV_VIEW_PATIENTS);
		Patient p;
		try {
			p = getPatientService().getPatient(patientId);
		} finally {
			context.removeProxyPrivilege(OpenmrsConstants.PRIV_VIEW_PATIENTS);
		}
		return p;
	}

	/**
	 * @see org.openmrs.api.PatientService.updatePatient(org.openmrs.Patient)
	 */
	public void updatePatient(Patient patient) throws APIException {
		context.addProxyPrivilege(OpenmrsConstants.PRIV_EDIT_PATIENTS);
		try {
			getPatientService().updatePatient(patient);
		} finally {
			context.removeProxyPrivilege(OpenmrsConstants.PRIV_EDIT_PATIENTS);
		}
	}

	/**
	 * @see org.openmrs.api.PatientService.getPatientsByIdentifier(java.lang.String,boolean)
	 */
	public Set<Patient> getPatientsByIdentifier(String identifier,
			boolean includeVoided) throws APIException {
		context.addProxyPrivilege(OpenmrsConstants.PRIV_VIEW_PATIENTS);
		Set<Patient> p;
		try {
			p = getPatientService().getPatientsByIdentifier(identifier,
					includeVoided);
		} finally {
			context.removeProxyPrivilege(OpenmrsConstants.PRIV_VIEW_PATIENTS);
		}
		return p;
	}

	/**
	 * @see org.openmrs.api.PatientService.getPatientsByName(java.lang.String)
	 */
	public Set<Patient> getPatientsByName(String name) throws APIException {
		context.addProxyPrivilege(OpenmrsConstants.PRIV_VIEW_PATIENTS);
		Set<Patient> p;
		try {
			p = getPatientsByName(name, false);
		} finally {
			context.removeProxyPrivilege(OpenmrsConstants.PRIV_VIEW_PATIENTS);
		}
		return p;
	}

	/**
	 * @see org.openmrs.api.PatientService.getPatientsByName(java.lang.String,boolean)
	 */
	public Set<Patient> getPatientsByName(String name, boolean includeVoided)
			throws APIException {
		context.addProxyPrivilege(OpenmrsConstants.PRIV_VIEW_PATIENTS);
		Set<Patient> p;
		try {
			p = getPatientService().getPatientsByName(name, includeVoided);
		} finally {
			context.removeProxyPrivilege(OpenmrsConstants.PRIV_VIEW_PATIENTS);
		}
		return p;
	}

	/**
	 * @see org.openmrs.api.PatientService.getSimilarPatients(java.lang.String,java.lang.Integer,java.lang.String)
	 */
	public Set<Patient> getSimilarPatients(String name, Integer birthyear,
			String gender) throws APIException {
		context.addProxyPrivilege(OpenmrsConstants.PRIV_VIEW_PATIENTS);
		Set<Patient> p;
		try {
			p = getPatientService().getSimilarPatients(name, birthyear, gender);
		} finally {
			context.removeProxyPrivilege(OpenmrsConstants.PRIV_VIEW_PATIENTS);
		}
		return p;
	}

	/**
	 * @see org.openmrs.api.PatientService.getPatientIdentifierTypes()
	 */
	public List<PatientIdentifierType> getPatientIdentifierTypes()
			throws APIException {
		context.addProxyPrivilege(OpenmrsConstants.PRIV_VIEW_PATIENTS);
		List<PatientIdentifierType> p;
		try {
			p = getPatientService().getPatientIdentifierTypes();
		} finally {
			context.removeProxyPrivilege(OpenmrsConstants.PRIV_VIEW_PATIENTS);
		}
		return p;
	}

	/**
	 * @see org.openmrs.api.PatientService.getPatientIdentifierType(java.lang.Integer)
	 */
	public PatientIdentifierType getPatientIdentifierType(
			Integer patientIdentifierTypeId) throws APIException {
		context.addProxyPrivilege(OpenmrsConstants.PRIV_VIEW_PATIENTS);
		PatientIdentifierType p;
		try {
			p = getPatientService().getPatientIdentifierType(
					patientIdentifierTypeId);
		} finally {
			context.removeProxyPrivilege(OpenmrsConstants.PRIV_VIEW_PATIENTS);
		}
		return p;
	}

	/**
	 * @see org.openmrs.api.PatientService.getTribe(java.lang.Integer)
	 */
	public Tribe getTribe(Integer tribeId) throws APIException {
		context.addProxyPrivilege(OpenmrsConstants.PRIV_VIEW_PATIENTS);
		Tribe t;
		try {
			t = getPatientService().getTribe(tribeId);
		} finally {
			context.removeProxyPrivilege(OpenmrsConstants.PRIV_VIEW_PATIENTS);
		}
		return t;
	}

	/**
	 * @see org.openmrs.api.PatientService.getTribes()
	 */
	public List<Tribe> getTribes() throws APIException {
		context.addProxyPrivilege(OpenmrsConstants.PRIV_VIEW_PATIENTS);
		List<Tribe> t;
		try {
			t = getPatientService().getTribes();
		} finally {
			context.removeProxyPrivilege(OpenmrsConstants.PRIV_VIEW_PATIENTS);
		}
		return t;
	}
	
	/**
	 * @see org.openmrs.api.PatientService.findTribes(java.lang.String)
	 */
	public List<Tribe> findTribes(String s) throws APIException {
		context.addProxyPrivilege(OpenmrsConstants.PRIV_VIEW_PATIENTS);
		List<Tribe> t;
		try {
			t = getPatientService().findTribes(s);
		} finally {
			context.removeProxyPrivilege(OpenmrsConstants.PRIV_VIEW_PATIENTS);
		}
		return t;
	}

	/**
	 * @see org.openmrs.api.PatientService.getLocations()
	 */
	public List<Location> getLocations() throws APIException {
		context.addProxyPrivilege(OpenmrsConstants.PRIV_VIEW_PATIENTS);
		List<Location> t;
		try {
			t = getPatientService().getLocations();
		} finally {
			context.removeProxyPrivilege(OpenmrsConstants.PRIV_VIEW_PATIENTS);
		}
		return t;
	}
	
	/**
	 * @see org.openmrs.api.EncounterService.findLocations()
	 */
	public List<Location> findLocations(String txt) throws APIException {
		context.addProxyPrivilege(OpenmrsConstants.PRIV_VIEW_ENCOUNTERS);
		List<Location> locs;
		try {
			locs = getEncounterService().findLocations(txt);
		} finally {
			context.removeProxyPrivilege(OpenmrsConstants.PRIV_VIEW_ENCOUNTERS);
		}
		return locs;
	}

	/**
	 * @see org.openmrs.api.PatientService.getLocation(java.lang.Integer)
	 */
	public Location getLocation(Integer locationId) throws APIException {
		context.addProxyPrivilege(OpenmrsConstants.PRIV_VIEW_PATIENTS);
		Location t;
		try {
			t = getPatientService().getLocation(locationId);
		} finally {
			context.removeProxyPrivilege(OpenmrsConstants.PRIV_VIEW_PATIENTS);
		}
		return t;
	}

	/**
	 * @see org.openmrs.api.PatientService.findPatients(java.lang.String,boolean)
	 */
	public List<Patient> findPatients(String query, boolean includeVoided) {

		List<Patient> patients;
		context.addProxyPrivilege(OpenmrsConstants.PRIV_VIEW_PATIENTS);
		try {
			patients = getPatientService().findPatients(query, includeVoided);
		} finally {
			context.removeProxyPrivilege(OpenmrsConstants.PRIV_VIEW_PATIENTS);
		}
		return patients;
	}

	/**
	 * @see org.openmrs.api.FormService.getForm(java.lang.Integer)
	 */
	public Form getForm(Integer formId) {

		Form form;
		context.addProxyPrivilege(OpenmrsConstants.PRIV_VIEW_FORMS);
		try {
			form = context.getFormService().getForm(formId);
		} finally {
			context.removeProxyPrivilege(OpenmrsConstants.PRIV_VIEW_FORMS);
		}
		return form;
	}
	
	public Collection<Form> getForms() {
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_FORM_ENTRY))
			throw new APIAuthenticationException("Privilege required: "
					+ OpenmrsConstants.PRIV_FORM_ENTRY);
		
		List<Form> forms = new Vector<Form>();
		context.addProxyPrivilege(OpenmrsConstants.PRIV_VIEW_FORMS);
		try {
			forms = context.getFormService().getForms(true);
		} catch (Exception e) {
			log.error(e);
		} finally {
			context.removeProxyPrivilege(OpenmrsConstants.PRIV_VIEW_FORMS);
		}
		return forms;
	}

	/**
	 * @see org.openmrs.api.UserService.findUsers(String, List<String>, boolean)
	 */
	public Collection<User> findUsers(String searchValue, List<String> roles,
			boolean includeVoided) {
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_FORM_ENTRY))
			throw new APIAuthenticationException("Privilege required: "
					+ OpenmrsConstants.PRIV_FORM_ENTRY);
		
		List<User> users = new Vector<User>();
		context.addProxyPrivilege(OpenmrsConstants.PRIV_VIEW_USERS);
		try {
			users = context.getUserService().findUsers(searchValue, roles, includeVoided);
		} catch (Exception e) {
			log.error(e);
		} finally {
			context.removeProxyPrivilege(OpenmrsConstants.PRIV_VIEW_USERS);
		}
		return users;
	}

	/**
	 * @see org.openmrs.api.UserService.getAllUsers(List<String>, boolean)
	 */
	public Collection<User> getAllUsers(List<String> roles,
			boolean includeVoided) {
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_FORM_ENTRY))
			throw new APIAuthenticationException("Privilege required: "
					+ OpenmrsConstants.PRIV_FORM_ENTRY);
		
		List<User> users = new Vector<User>();
		context.addProxyPrivilege(OpenmrsConstants.PRIV_VIEW_USERS);
		try {
			users = context.getUserService().getAllUsers(roles, includeVoided);
		} catch (Exception e) {
			log.error(e);
		} finally {
			context.removeProxyPrivilege(OpenmrsConstants.PRIV_VIEW_USERS);
		}
		return users;
	}
	
	private void checkPrivilege(String privilege) {
		if (!context.hasPrivilege(privilege))
			throw new APIAuthenticationException("Privilege required: " + privilege);		
	}
	
	private void checkPrivileges(String privilegeA, String privilegeB) {
		boolean hasA = context.hasPrivilege(privilegeA);
		boolean hasB = context.hasPrivilege(privilegeB);
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
		dao().createFormEntryQueue(formEntryQueue);
	}

	public void updateFormEntryQueue(FormEntryQueue formEntryQueue) {
		checkPrivilege(FormEntryConstants.PRIV_EDIT_FORMENTRY_QUEUE);
		dao().updateFormEntryQueue(formEntryQueue);
	}

	public FormEntryQueue getFormEntryQueue(int formEntryQueueId) {
		checkPrivilege(FormEntryConstants.PRIV_VIEW_FORMENTRY_QUEUE);
		return dao().getFormEntryQueue(formEntryQueueId);
	}

	public Collection<FormEntryQueue> getFormEntryQueues() {
		checkPrivilege(FormEntryConstants.PRIV_VIEW_FORMENTRY_QUEUE);
		return dao().getFormEntryQueues();
	}
	
	public FormEntryQueue getNextFormEntryQueue() {
		checkPrivilege(FormEntryConstants.PRIV_VIEW_FORMENTRY_QUEUE);
		return dao().getNextFormEntryQueue();
	}

	public void deleteFormEntryQueue(FormEntryQueue formEntryQueue) {
		checkPrivilege(FormEntryConstants.PRIV_DELETE_FORMENTRY_QUEUE);
		dao().deleteFormEntryQueue(formEntryQueue);
	}
	
	public Integer getFormEntryQueueSize() {
		checkPrivilege(FormEntryConstants.PRIV_VIEW_FORMENTRY_ERROR);
		return dao().getFormEntryQueueSize();
	}
	
	
	public void createFormEntryArchive(FormEntryArchive formEntryArchive) {
		checkPrivilege(FormEntryConstants.PRIV_ADD_FORMENTRY_ARCHIVE);
		dao().createFormEntryArchive(formEntryArchive);
	}
	
	public FormEntryArchive getFormEntryArchive(Integer formEntryArchive) {
		checkPrivilege(FormEntryConstants.PRIV_VIEW_FORMENTRY_ARCHIVE);
		return dao().getFormEntryArchive(formEntryArchive);
	}
	
	public Collection<FormEntryArchive> getFormEntryArchives() {
		checkPrivilege(FormEntryConstants.PRIV_VIEW_FORMENTRY_ARCHIVE);
		return dao().getFormEntryArchives();
	}
	
	public void deleteFormEntryArchive(FormEntryArchive formEntryArchive) {
		checkPrivilege(FormEntryConstants.PRIV_DELETE_FORMENTRY_ARCHIVE);
		dao().deleteFormEntryArchive(formEntryArchive);
	}
	
	public Integer getFormEntryArchiveSize() {
		checkPrivilege(FormEntryConstants.PRIV_VIEW_FORMENTRY_ERROR);
		return dao().getFormEntryArchiveSize();
	}
	
	
	public void createFormEntryError(FormEntryError formEntryError) {
		checkPrivilege(FormEntryConstants.PRIV_ADD_FORMENTRY_ERROR);
		dao().createFormEntryError(formEntryError);
	}
	
	public FormEntryError getFormEntryError(Integer formEntryErrorId) {
		checkPrivilege(FormEntryConstants.PRIV_VIEW_FORMENTRY_ERROR);
		return dao().getFormEntryError(formEntryErrorId);
	}
	
	public Collection<FormEntryError> getFormEntryErrors() {
		checkPrivilege(FormEntryConstants.PRIV_VIEW_FORMENTRY_ERROR);
		return dao().getFormEntryErrors();
	}
	
	public void updateFormEntryError(FormEntryError formEntryError) {
		checkPrivilege(FormEntryConstants.PRIV_EDIT_FORMENTRY_ERROR);
		dao().updateFormEntryError(formEntryError);
	}
	
	public void deleteFormEntryError(FormEntryError formEntryError) {
		checkPrivilege(FormEntryConstants.PRIV_DELETE_FORMENTRY_ERROR);
		dao().deleteFormEntryError(formEntryError);
	}
	
	public Integer getFormEntryErrorSize() {
		checkPrivilege(FormEntryConstants.PRIV_VIEW_FORMENTRY_ERROR);
		return dao().getFormEntryErrorSize();
	}
	
	public void garbageCollect() {
		dao().garbageCollect();
	}

}
