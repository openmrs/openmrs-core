package org.openmrs.formentry;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;

import org.openmrs.Form;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifierType;
import org.openmrs.RelationshipType;
import org.openmrs.Tribe;
import org.openmrs.User;
import org.openmrs.api.APIException;
import org.openmrs.formentry.db.FormEntryDAO;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface FormEntryService {

	public void setFormEntryDAO(FormEntryDAO dao);

	/**
	 * @see org.openmrs.api.PatientService.createPatient(org.openmrs.Patient)
	 */
	public void createPatient(Patient patient) throws APIException;

	/**
	 * @see org.openmrs.api.PatientService.getPatient(java.lang.Integer)
	 */
	@Transactional(readOnly=true)
	public Patient getPatient(Integer patientId) throws APIException;

	/**
	 * @see org.openmrs.api.PatientService.updatePatient(org.openmrs.Patient)
	 */
	public void updatePatient(Patient patient) throws APIException;

	/**
	 * @see org.openmrs.api.PatientService.getPatientsByIdentifier(java.lang.String,boolean)
	 */
	@Transactional(readOnly=true)
	public Set<Patient> getPatientsByIdentifier(String identifier,
			boolean includeVoided) throws APIException;

	/**
	 * @see org.openmrs.api.PatientService.getPatientsByName(java.lang.String)
	 */
	@Transactional(readOnly=true)
	public Set<Patient> getPatientsByName(String name) throws APIException;

	/**
	 * @see org.openmrs.api.PatientService.getPatientsByName(java.lang.String,boolean)
	 */
	@Transactional(readOnly=true)
	public Set<Patient> getPatientsByName(String name, boolean includeVoided)
			throws APIException;

	/**
	 * @see org.openmrs.api.PatientService.getSimilarPatients(java.lang.String,java.lang.Integer,java.lang.String)
	 */
	@Transactional(readOnly=true)
	public Set<Patient> getSimilarPatients(String name, Integer birthyear,
			String gender) throws APIException;

	/**
	 * @see org.openmrs.api.PatientService.getPatientIdentifierTypes()
	 */
	@Transactional(readOnly=true)
	public List<PatientIdentifierType> getPatientIdentifierTypes()
			throws APIException;

	/**
	 * @see org.openmrs.api.PatientService.getPatientIdentifierType(java.lang.Integer)
	 */
	@Transactional(readOnly=true)
	public PatientIdentifierType getPatientIdentifierType(
			Integer patientIdentifierTypeId) throws APIException;

	/**
	 * @see org.openmrs.api.PatientService.getTribe(java.lang.Integer)
	 */
	@Transactional(readOnly=true)
	public Tribe getTribe(Integer tribeId) throws APIException;

	/**
	 * @see org.openmrs.api.PatientService.getTribes()
	 */
	@Transactional(readOnly=true)
	public List<Tribe> getTribes() throws APIException;

	/**
	 * @see org.openmrs.api.PatientService.findTribes(java.lang.String)
	 */
	@Transactional(readOnly=true)
	public List<Tribe> findTribes(String s) throws APIException;

	/**
	 * @see org.openmrs.api.PatientService.getLocations()
	 */
	@Transactional(readOnly=true)
	public List<Location> getLocations() throws APIException;

	/**
	 * @see org.openmrs.api.EncounterService.findLocations()
	 */
	@Transactional(readOnly=true)
	public List<Location> findLocations(String txt) throws APIException;

	/**
	 * @see org.openmrs.api.PatientService.getLocation(java.lang.Integer)
	 */
	@Transactional(readOnly=true)
	public Location getLocation(Integer locationId) throws APIException;

	/**
	 * @see org.openmrs.api.PatientService.findPatients(java.lang.String,boolean)
	 */
	@Transactional(readOnly=true)
	public List<Patient> findPatients(String query, boolean includeVoided);

	/**
	 * @see org.openmrs.api.FormService.getForm(java.lang.Integer)
	 */
	@Transactional(readOnly=true)
	public Form getForm(Integer formId);

	@Transactional(readOnly=true)
	public Collection<Form> getForms();

	@Transactional(readOnly=true)
	public Collection<Form> getForms(boolean onlyPublished);

	/**
	 * @see org.openmrs.api.UserService.getUserByUsername(String)
	 */
	@Transactional(readOnly=true)
	public User getUserByUsername(String username);
	
	/**
	 * @see org.openmrs.api.UserService.findUsers(String, List<String>, boolean)
	 */
	@Transactional(readOnly=true)
	public Collection<User> findUsers(String searchValue, List<String> roles,
			boolean includeVoided);

	/**
	 * @see org.openmrs.api.UserService.getAllUsers(List<String>, boolean)
	 */
	@Transactional(readOnly=true)
	public Collection<User> getAllUsers(List<String> strRoles,
			boolean includeVoided);

	@Transactional(readOnly=true)
	public SortedMap<String, String> getSystemVariables();
	
	@Transactional(readOnly=true)
	public RelationshipType getRelationshipType(Integer id);

	/***************************************************************************
	 * FormEntryQueue Service Methods
	 **************************************************************************/

	public void createFormEntryQueue(FormEntryQueue formEntryQueue);

	public void updateFormEntryQueue(FormEntryQueue formEntryQueue);

	@Transactional(readOnly=true)
	public FormEntryQueue getFormEntryQueue(int formEntryQueueId);

	public Collection<FormEntryQueue> getFormEntryQueues();

	@Transactional(readOnly=true)
	public FormEntryQueue getNextFormEntryQueue();

	public void deleteFormEntryQueue(FormEntryQueue formEntryQueue);

	@Transactional(readOnly=true)
	public Integer getFormEntryQueueSize();

	public void createFormEntryArchive(FormEntryArchive formEntryArchive);

	@Transactional(readOnly=true)
	public FormEntryArchive getFormEntryArchive(Integer formEntryArchive);

	@Transactional(readOnly=true)
	public Collection<FormEntryArchive> getFormEntryArchives();

	public void deleteFormEntryArchive(FormEntryArchive formEntryArchive);

	@Transactional(readOnly=true)
	public Integer getFormEntryArchiveSize();

	public void createFormEntryError(FormEntryError formEntryError);

	@Transactional(readOnly=true)
	public FormEntryError getFormEntryError(Integer formEntryErrorId);

	@Transactional(readOnly=true)
	public Collection<FormEntryError> getFormEntryErrors();

	public void updateFormEntryError(FormEntryError formEntryError);

	public void deleteFormEntryError(FormEntryError formEntryError);

	@Transactional(readOnly=true)
	public Integer getFormEntryErrorSize();

	public void garbageCollect();

}