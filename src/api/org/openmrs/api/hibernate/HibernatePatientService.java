package org.openmrs.api.hibernate;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifierType;
import org.openmrs.Tribe;
import org.openmrs.api.APIException;
import org.openmrs.api.PatientService;
import org.openmrs.context.Context;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

public class HibernatePatientService extends HibernateDaoSupport implements PatientService {

	protected final Log log = LogFactory.getLog(getClass());
	
	private Context context;
	
	public HibernatePatientService(Context c) {
		this.context = c;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.api.PatientService#getPatient(java.lang.Long)
	 */
	public Patient getPatient(Integer patientId) {
		return (Patient) getHibernateTemplate().get(Patient.class, patientId);
	}
	

	public Patient createPatient(Patient patient) throws APIException {
		// TODO Auto-generated method stub
		return null;
	}


	public void deletePatient(Integer patientId) throws APIException {
		// TODO Auto-generated method stub
		
	}


	public List getPatientByIdentifier(String identifier) throws APIException {
		// TODO Auto-generated method stub
		return null;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.api.PatientService#saveOrUpdate(org.openmrs.Patient)
	 */
	public void savePatient(Patient patient) {
		getHibernateTemplate().save(patient);
		getHibernateTemplate().flush();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.api.PatientService#voidPatient(org.openmrs.Patient,
	 *      java.lang.String)
	 */
	public void voidPatient(Patient patient, String reason) {
		patient.setVoided(true);
		patient.setVoidReason(reason);
		getHibernateTemplate().save(patient);
	}

	public List findPatient(String q) {
		return getHibernateTemplate()
				.find(
						"from Patient as p "
						+ "left join fetch p.patientIdentifiers pid "
						+ "where pid.patientIdentifierId.identifier = ?",
						q);
	}

	/**
	 * @see org.openmrs.api.PatientService#deletePatient(org.openmrs.Patient)
	 */
	public void deletePatient(Patient patient) throws APIException {
		// TODO Auto-generated method stub
		
	}

	/**
	 * @see org.openmrs.api.PatientService#getPatientIdentifierType(java.lang.Integer)
	 */
	public PatientIdentifierType getPatientIdentifierType(Integer patientIdentifierTypeId) throws APIException {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see org.openmrs.api.PatientService#getPatientIdentifierTypes()
	 */
	public List<PatientIdentifierType> getPatientIdentifierTypes() throws APIException {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see org.openmrs.api.PatientService#getPatientTribes()
	 */
	public List<Tribe> getPatientTribes() throws APIException {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see org.openmrs.api.PatientService#updatePatient(org.openmrs.Patient)
	 */
	public void updatePatient(Patient patient) throws APIException {
		// TODO Auto-generated method stub
		
	}

}
