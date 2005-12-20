package org.openmrs.api;

import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifierType;
import org.openmrs.Tribe;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.PatientDAO;
import org.openmrs.util.Helper;

/**
 * Patient-related services
 * 
 * @author Ben Wolfe
 * @author Burke Mamlin
 * @vesrion 1.0
 */
public class PatientService {

	private Log log = LogFactory.getLog(this.getClass());
	
	private Context context;
	
	public PatientService(Context c) {
		this.context = c;
	}
	
	/**
	 * Creates a new patient record
	 * 
	 * @param patient to be created
	 * @throws APIException
	 */
	public void createPatient(Patient patient) throws APIException {
		context.getDAOContext().getPatientDAO().createPatient(patient);
	}

	/**
	 * Get patient by internal identifier
	 * 
	 * @param patientId internal patient identifier
	 * @return patient with given internal identifier
	 * @throws APIException
	 */
	public Patient getPatient(Integer patientId) throws APIException {
		return context.getDAOContext().getPatientDAO().getPatient(patientId);
	}

	/**
	 * Update patient 
	 * 
	 * @param patient to be updated
	 * @throws APIException
	 */
	public void updatePatient(Patient patient) throws APIException {
		context.getDAOContext().getPatientDAO().updatePatient(patient);
	}

	/**
	 * Find all patients with a given identifier
	 * 
	 * @param identifier
	 * @return set of patients matching identifier
	 * @throws APIException
	 */
	public Set<Patient> getPatientsByIdentifier(String identifier, boolean includeVoided) throws APIException {
		return context.getDAOContext().getPatientDAO().getPatientsByIdentifier(identifier, includeVoided);
	}
	
	/**
	 * Find patients by name
	 * 
	 * @param name
	 * @return set of patients matching name
	 * @throws APIException
	 */
	public Set<Patient> getPatientsByName(String name, boolean includeVoided) throws APIException {
		return context.getDAOContext().getPatientDAO().getPatientsByName(name, includeVoided);
	}
	
	/**
	 * Void patient record (functionally delete patient from system)
	 * 
	 * @param patient patient to be voided
	 * @param reason reason for voiding patient
	 */
	public void voidPatient(Patient patient, String reason) throws APIException {
		context.getDAOContext().getPatientDAO().voidPatient(patient, reason);
	}

	/**
	 * Unvoid patient record 
	 * 
	 * @param patient patient to be revived
	 */
	public void unvoidPatient(Patient patient) throws APIException {
		context.getDAOContext().getPatientDAO().unvoidPatient(patient);
	}
	
	/**
	 * Delete patient from database. This <b>should not be called</b>
	 * except for testing and administration purposes.  Use the void
	 * method instead.
	 * 
	 * @param patient patient to be deleted
	 * 
	 * @see #voidPatient(Patient, String) 
	 */
	public void deletePatient(Patient patient) throws APIException {
		context.getDAOContext().getPatientDAO().deletePatient(patient);
	}
	
	/**
	 * Get all patientIdentifier types
	 * 
	 * @return patientIdentifier types list
	 * @throws APIException
	 */
	public List<PatientIdentifierType> getPatientIdentifierTypes() throws APIException {
		return context.getDAOContext().getPatientDAO().getPatientIdentifierTypes();
	}

	/**
	 * Get patientIdentifierType by internal identifier
	 * 
	 * @param patientIdentifierType id
	 * @return patientIdentifierType with given internal identifier
	 * @throws APIException
	 */
	public PatientIdentifierType getPatientIdentifierType(Integer patientIdentifierTypeId) throws APIException {
		return context.getDAOContext().getPatientDAO().getPatientIdentifierType(patientIdentifierTypeId);
	}

	/**
	 * Get tribe by internal tribe identifier
	 * 
	 * @return Tribe
	 * @param tribeId 
	 * @throws APIException
	 */
	public Tribe getTribe(Integer tribeId) throws APIException {
		return context.getDAOContext().getPatientDAO().getTribe(tribeId);
	}
	
	/**
	 * Get list of tribes that are not retired
	 * 
	 * @return non-retired Tribe list
	 * @throws APIException
	 */
	public List<Tribe> getTribes() throws APIException {
		return context.getDAOContext().getPatientDAO().getTribes();
	}
	
	/**
	 * Get all locations
	 * 
	 * @return location list
	 * @throws APIException
	 */
	public List<Location> getLocations() throws APIException {
		return context.getDAOContext().getPatientDAO().getLocations();
	}

	/**
	 * Get location by internal identifier
	 * 
	 * @param location id
	 * @return location with given internal identifier
	 * @throws APIException
	 */
	public Location getLocation(Integer locationId) throws APIException {
		return context.getDAOContext().getPatientDAO().getLocation(locationId);
	}
	
	public List<Patient> findPatients(String query, boolean includeVoided) {
		
		List<Patient> patients = new Vector<Patient>();
		PatientDAO dao = context.getDAOContext().getPatientDAO();
		
		//query must be more than 2 characters
		if (query.length() < 3)
			return patients;
		
		// if there is a number in the query string
		if (query.matches(".*\\d+.*")) {
			log.debug("Query: " + query);
			//if there is no hyphen:
			if (query.lastIndexOf('-') != query.length() - 2) {
				// append checkdigit and search
				try {
					log.debug("appended checkdigit: " + query + "-" + Helper.getCheckdigit(query));
					patients.addAll(dao.getPatientsByIdentifier(query + "-" + Helper.getCheckdigit(query), includeVoided));
				} catch (Exception e){}
				
				int ch = query.charAt(query.length()-1);
				
				if (ch > 48 && ch > 57) {
					//if the last character is a number
					try {
						String q = query.substring(0, query.length() - 1);
						int cd = Helper.getCheckdigit(q);
						if (cd == ch) {
							//if the last number is the checkdigit, do the search with that as the checkdigit
							patients.addAll(dao.getPatientsByIdentifier(q + "-" + cd, includeVoided));
						}
						else {
							// the last number is not the check digit, get the checkdigit for the entire string
							cd = Helper.getCheckdigit(query);
							patients.addAll(dao.getPatientsByIdentifier(query + "-" + cd, includeVoided));
						}
					} catch(Exception e) {}
				}
			}
			else { //there is a hyphen
				patients.addAll(dao.getPatientsByIdentifier(query, includeVoided));
			}
				
		}
		else {
			//there is no number in the string, search on name
			patients.addAll(dao.getPatientsByName(query, includeVoided));
		}
		return patients;
	}
}
