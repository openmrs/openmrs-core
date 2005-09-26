package org.openmrs.api.hibernate;

import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.MatchMode;
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
		Session session = HibernateUtil.currentSession();
		Patient patient = (Patient) session.get(Patient.class, patientId);
		HibernateUtil.disconnectSession();
		
		return patient;
	}
	

	public void createPatient(Patient patient) throws APIException {
		Session session = HibernateUtil.currentSession();
		Transaction tx = session.beginTransaction();
		
		patient.setCreator(context.getAuthenticatedUser());
		patient.setDateCreated(new Date());
		session.save(patient);
		
		tx.commit();
		HibernateUtil.disconnectSession();
	}


	public void updatePatient(Patient patient) throws APIException {
		if (patient.getPatientId() == null)
			createPatient(patient);
		else {
			Session session = HibernateUtil.currentSession();
			Transaction tx = session.beginTransaction();
			
			session.update(patient);
			
			tx.commit();
			HibernateUtil.disconnectSession();
		}
	}


	public List getPatientByIdentifier(String identifier) throws APIException {
		Session session = HibernateUtil.currentSession();
		
		//TODO this will return 3 patient #1's if 3 identifiers are matched. fix?
		List patients = session.createCriteria(Patient.class)
						.createCriteria("identifiers")
						.add(Expression.like("identifier", identifier, MatchMode.ANYWHERE))
						.list();
		
		HibernateUtil.disconnectSession();
		
		return patients;
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
		patient.setVoidedBy(context.getAuthenticatedUser());
		patient.setDateVoided(new Date());
		patient.setVoidReason(reason);
		updatePatient(patient);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.api.PatientService#unvoidPatient(org.openmrs.Patient)
	 */
	public void unvoidPatient(Patient patient, String reason) {
		patient.setVoided(false);
		patient.setVoidedBy(null);
		patient.setDateVoided(null);
		patient.setVoidReason("");
		updatePatient(patient);
	}

	/*  See getPatientByIdentifier(String)
	 * 
	public List findPatient(String q) {
		return getHibernateTemplate()
				.find(
						"from Patient as p "
						+ "left join fetch p.patientIdentifiers pid "
						+ "where pid.patientIdentifierId.identifier = ?",
						q);
	}
	*/

	/**
	 * @see org.openmrs.api.PatientService#deletePatient(org.openmrs.Patient)
	 */
	public void deletePatient(Patient patient) throws APIException {
		Session session = HibernateUtil.currentSession();
		Transaction tx = session.beginTransaction();
		
		session.delete(patient);
		
		tx.commit();
		HibernateUtil.disconnectSession();		
	}

	/**
	 * @see org.openmrs.api.PatientService#getPatientIdentifierType(java.lang.Integer)
	 */
	public PatientIdentifierType getPatientIdentifierType(Integer patientIdentifierTypeId) throws APIException {
		Session session = HibernateUtil.currentSession();
		PatientIdentifierType patientIdentifierType = (PatientIdentifierType) session.get(PatientIdentifierType.class, patientIdentifierTypeId);
		HibernateUtil.disconnectSession();
		
		return patientIdentifierType;
	}

	/**
	 * @see org.openmrs.api.PatientService#getPatientIdentifierTypes()
	 */
	public List<PatientIdentifierType> getPatientIdentifierTypes() throws APIException {
		Session session = HibernateUtil.currentSession();
		
		List<PatientIdentifierType> patientIdentifierTypes = session.createQuery("from PatientIdentifierType").list();
		
		HibernateUtil.disconnectSession();
		
		return patientIdentifierTypes;
	}

	/**
	 * @see org.openmrs.api.PatientService#getPatientTribes()
	 */
	public List<Tribe> getPatientTribes() throws APIException {
		Session session = HibernateUtil.currentSession();
		
		List<Tribe> tribes = session.createQuery("from Tribe").list();
		
		HibernateUtil.disconnectSession();
		
		return tribes;
	}

}
