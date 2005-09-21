package org.openmrs.api.hibernate;

import java.util.List;

import org.openmrs.Patient;
import org.openmrs.api.APIException;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

public class HibernatePatientService extends HibernateDaoSupport { // implementsPatientService {

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

}
