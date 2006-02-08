package org.openmrs.api;

import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifierType;
import org.openmrs.Tribe;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.DAOContext;
import org.openmrs.util.OpenmrsConstants;

/**
 * Patient-related services
 * 
 * @author Ben Wolfe
 * @author Burke Mamlin
 * @vesrion 1.0
 */
public class FormEntryService {

	private Log log = LogFactory.getLog(this.getClass());
	
	private Context context;
	private DAOContext daoContext;
	
	public FormEntryService(Context c, DAOContext d) {
		this.context = c;
		this.daoContext = d;
	}
	
	private PatientService getPatientService() {
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_FORM_ENTRY))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_FORM_ENTRY);
		return context.getPatientService();
	}
	
	/**
	 * @see org.openmrs.api.PatientService.createPatient(org.openmrs.Patient)
	 */
	public void createPatient(Patient patient) throws APIException {
		context.addProxyPrivilege(OpenmrsConstants.PRIV_ADD_PATIENTS);
		try {
			getPatientService().createPatient(patient);
		}
		finally {
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
		}
		finally {
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
		}
		finally {
			context.removeProxyPrivilege(OpenmrsConstants.PRIV_EDIT_PATIENTS);
		}
	}

	/**
	 * @see org.openmrs.api.PatientService.getPatientsByIdentifier(java.lang.String,boolean)
	 */
	public Set<Patient> getPatientsByIdentifier(String identifier, boolean includeVoided) throws APIException {
		context.addProxyPrivilege(OpenmrsConstants.PRIV_VIEW_PATIENTS);
		Set<Patient> p;
		try {
			p = getPatientService().getPatientsByIdentifier(identifier, includeVoided);
		}
		finally {
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
		}
		finally {
			context.removeProxyPrivilege(OpenmrsConstants.PRIV_VIEW_PATIENTS);
		}
		return p;
	}

	/**
	 * @see org.openmrs.api.PatientService.getPatientsByName(java.lang.String,boolean)
	 */
	public Set<Patient> getPatientsByName(String name, boolean includeVoided) throws APIException {
		context.addProxyPrivilege(OpenmrsConstants.PRIV_VIEW_PATIENTS);
		Set<Patient> p;
		try {
			p = getPatientService().getPatientsByName(name, includeVoided);
		}
		finally {
			context.removeProxyPrivilege(OpenmrsConstants.PRIV_VIEW_PATIENTS);
		}
		return p;
	}
	
	/**
	 * @see org.openmrs.api.PatientService.getSimilarPatients(java.lang.String,java.lang.Integer,java.lang.String)
	 */
	public Set<Patient> getSimilarPatients(String name, Integer birthyear, String gender) throws APIException {
		context.addProxyPrivilege(OpenmrsConstants.PRIV_VIEW_PATIENTS);
		Set<Patient> p;
		try {
			p = getPatientService().getSimilarPatients(name, birthyear, gender);
		}
		finally {
			context.removeProxyPrivilege(OpenmrsConstants.PRIV_VIEW_PATIENTS);
		}
		return p;
	}
	
	/**
	 * @see org.openmrs.api.PatientService.getPatientIdentifierTypes()
	 */
	public List<PatientIdentifierType> getPatientIdentifierTypes() throws APIException {
		context.addProxyPrivilege(OpenmrsConstants.PRIV_VIEW_PATIENTS);
		List<PatientIdentifierType> p;
		try {
			p = getPatientService().getPatientIdentifierTypes();
		}
		finally {
			context.removeProxyPrivilege(OpenmrsConstants.PRIV_VIEW_PATIENTS);
		}
		return p;
	}

	/**
	 * @see org.openmrs.api.PatientService.getPatientIdentifierType(java.lang.Integer)
	 */
	public PatientIdentifierType getPatientIdentifierType(Integer patientIdentifierTypeId) throws APIException {
		context.addProxyPrivilege(OpenmrsConstants.PRIV_VIEW_PATIENTS);
		PatientIdentifierType p;
		try {
			p = getPatientService().getPatientIdentifierType(patientIdentifierTypeId);
		}
		finally {
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
		}
		finally {
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
		}
		finally {
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
		}
		finally {
			context.removeProxyPrivilege(OpenmrsConstants.PRIV_VIEW_PATIENTS);
		}
		return t; 
	}

	/**
	 * @see org.openmrs.api.PatientService.getLocation(java.lang.Integer)
	 */
	public Location getLocation(Integer locationId) throws APIException {
		context.addProxyPrivilege(OpenmrsConstants.PRIV_VIEW_PATIENTS);
		Location t;
		try {
			t = getPatientService().getLocation(locationId);
		}
		finally {
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
		}
		finally {
			context.removeProxyPrivilege(OpenmrsConstants.PRIV_VIEW_PATIENTS);
		}
		return patients;
	}
}
