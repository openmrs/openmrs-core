package org.openmrs.api.hibernate;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.MatchMode;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientAddress;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PatientName;
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
		
		return patient;
	}
	

	public void createPatient(Patient patient) throws APIException {
		Session session = HibernateUtil.currentSession();
		
		patient.setCreator(context.getAuthenticatedUser());
		patient.setDateCreated(new Date());
		if (patient.getAddresses() != null)
			for (Iterator<PatientAddress> i = patient.getAddresses().iterator(); i.hasNext();) {
				PatientAddress pAddress = i.next();
				pAddress.setDateCreated(new Date());
				pAddress.setCreator(context.getAuthenticatedUser());
				pAddress.setPatient(patient);
			}
		if (patient.getNames() != null)
			for (Iterator<PatientName> i = patient.getNames().iterator(); i.hasNext();) {
				PatientName pName = i.next();
				pName.setDateCreated(new Date());
				pName.setCreator(context.getAuthenticatedUser());
				pName.setPatient(patient);
			}
		if (patient.getIdentifiers() != null)
			for (Iterator<PatientIdentifier> i = patient.getIdentifiers().iterator(); i.hasNext();) {
				PatientIdentifier pIdentifier = i.next();
				pIdentifier.setDateCreated(new Date());
				pIdentifier.setCreator(context.getAuthenticatedUser());
				pIdentifier.setPatient(patient);
			}
		
		session.saveOrUpdate(patient);
	}


	public void updatePatient(Patient patient) throws APIException {
		if (patient.getPatientId() == null)
			createPatient(patient);
		else {
			Session session = HibernateUtil.currentSession();
			
			session.saveOrUpdate(patient);
		}
	}


	public List getPatientByIdentifier(String identifier) throws APIException {
		Session session = HibernateUtil.currentSession();
		
		//TODO this will return 3 patient #1's if 3 identifiers are matched. fix?
		List patients = session.createCriteria(Patient.class)
						.createCriteria("identifiers")
						.add(Expression.like("identifier", identifier, MatchMode.ANYWHERE))
						.list();
		
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
		session.delete(patient);
	}

	/**
	 * @see org.openmrs.api.PatientService#getPatientIdentifierType(java.lang.Integer)
	 */
	public PatientIdentifierType getPatientIdentifierType(Integer patientIdentifierTypeId) throws APIException {
		Session session = HibernateUtil.currentSession();
		PatientIdentifierType patientIdentifierType = (PatientIdentifierType) session.get(PatientIdentifierType.class, patientIdentifierTypeId);
		
		return patientIdentifierType;
	}

	/**
	 * @see org.openmrs.api.PatientService#getPatientIdentifierTypes()
	 */
	public List<PatientIdentifierType> getPatientIdentifierTypes() throws APIException {
		Session session = HibernateUtil.currentSession();
		
		List<PatientIdentifierType> patientIdentifierTypes = session.createQuery("from PatientIdentifierType").list();
		
		return patientIdentifierTypes;
	}

	/**
	 * @see org.openmrs.api.PatientService#getPatientTribes()
	 */
	public List<Tribe> getPatientTribes() throws APIException {
		Session session = HibernateUtil.currentSession();
		
		List<Tribe> tribes = session.createQuery("from Tribe").list();
		
		return tribes;
	}

	/**
	 * @see org.openmrs.api.PatientService#getLocation(java.lang.Integer)
	 */
	public Location getLocation(Integer locationId) throws APIException {

		Session session = HibernateUtil.currentSession();
		
		Location location = new Location();
		location = (Location)session.get(Location.class, locationId);
		
		return location;

	}

	/**
	 * @see org.openmrs.api.PatientService#getLocations()
	 */
	public List<Location> getLocations() throws APIException {

		Session session = HibernateUtil.currentSession();
		
		List<Location> locations;
		locations = session.createQuery("from Location l").list();
		
		return locations;

	}
	
}
