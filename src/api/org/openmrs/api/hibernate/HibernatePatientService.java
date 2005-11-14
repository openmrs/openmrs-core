package org.openmrs.api.hibernate;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
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

public class HibernatePatientService implements PatientService {

	protected final Log log = LogFactory.getLog(getClass());
	
	private Context context;
	
	public HibernatePatientService(Context c) {
		this.context = c;
	}
	
	/**
	 * @see org.openmrs.api.PatientService#getPatient(java.lang.Long)
	 */
	public Patient getPatient(Integer patientId) {
		Session session = HibernateUtil.currentSession();
		HibernateUtil.beginTransaction();
		Patient patient = (Patient) session.get(Patient.class, patientId);
		HibernateUtil.commitTransaction();
		return patient;
	}
	

	public void createPatient(Patient patient) throws APIException {
		Session session = HibernateUtil.currentSession();
		try {
			HibernateUtil.beginTransaction();

			setCollectionProperties(patient);
			
			session.saveOrUpdate(patient);
			HibernateUtil.commitTransaction();
		}
		catch (Exception e) {
			HibernateUtil.rollbackTransaction();
			throw new APIException(e.getMessage());
		}
	}


	public void updatePatient(Patient patient) throws APIException {
		if (patient.getPatientId() == null)
			createPatient(patient);
		else {
			Session session = HibernateUtil.currentSession();
			try {
				HibernateUtil.beginTransaction();
				setCollectionProperties(patient);
				session.saveOrUpdate(patient);
				HibernateUtil.commitTransaction();
				
			}
			catch (Exception e) {
				HibernateUtil.rollbackTransaction();
				throw new APIException(e.getMessage());
			}
		}
	}


	public List<Patient> getPatientsByIdentifier(String identifier) throws APIException {
		Session session = HibernateUtil.currentSession();
		
		//TODO this will return 3 patient #1's if 3 identifiers are matched. fix?
		List<Patient> patients = session.createCriteria(Patient.class)
						.createCriteria("identifiers")
						.add(Expression.like("identifier", identifier, MatchMode.ANYWHERE))
						.list();
		
		return patients;
	}

	public List<Patient> getPatientsByName(String name) throws APIException {
		Session session = HibernateUtil.currentSession();
		
		//TODO simple name search to start testing, will need to make "real" name search
		//		i.e. split on whitespace, guess at first/last name, etc
		
		// TODO return the matched name instead of the primary name
		//   possible solution: create org.openmrs.PatientListItem and return a list of those 
		List<Patient> patients = session.createCriteria(Patient.class)
						.createAlias("names", "name")
						.add(Expression.or(
								Expression.like("name.familyName", name, MatchMode.ANYWHERE),
								Expression.like("name.givenName", name, MatchMode.ANYWHERE)
							)	)
						.list();
	
		return patients;
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
	
	/**
	 * 
	 * @see org.openmrs.api.PatientService#unvoidPatient(org.openmrs.Patient)
	 */
	public void unvoidPatient(Patient patient) {
		patient.setVoided(false);
		patient.setVoidedBy(null);
		patient.setDateVoided(null);
		patient.setVoidReason(null);
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
		try {
			HibernateUtil.beginTransaction();
			session.delete(patient);
			HibernateUtil.commitTransaction();
		}
		catch (Exception e) {
			HibernateUtil.rollbackTransaction();
			throw new APIException(e.getMessage());
		}
			
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
		
		List<PatientIdentifierType> patientIdentifierTypes = session.createQuery("from PatientIdentifierType p order by p.name").list();
		
		return patientIdentifierTypes;
	}

	/**
	 * @see org.openmrs.api.PatientService#getTribe()
	 */
	public Tribe getTribe(Integer tribeId) throws APIException {
		Session session = HibernateUtil.currentSession();
		
		Tribe tribe = (Tribe)session.get(Tribe.class, tribeId);
		
		return tribe;
	}
	
	/**
	 * @see org.openmrs.api.PatientService#getTribes()
	 */
	public List<Tribe> getTribes() throws APIException {
		Session session = HibernateUtil.currentSession();
		
		List<Tribe> tribes = session.createQuery("from Tribe t order by t.name").list();
		
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
		locations = session.createQuery("from Location l order by l.name").list();
		
		return locations;

	}
	
	/**
	 * Iterates over Names/Addresses/Identifiers to set dateCreated and creator properties if needed
	 * @param patient
	 */
	private void setCollectionProperties(Patient patient) {
		if (patient.getCreator() == null) {
			patient.setCreator(context.getAuthenticatedUser());
			patient.setDateCreated(new Date());
		}
		patient.setChangedBy(context.getAuthenticatedUser());
		patient.setDateChanged(new Date());
		if (patient.getAddresses() != null)
			for (Iterator<PatientAddress> i = patient.getAddresses().iterator(); i.hasNext();) {
				PatientAddress pAddress = i.next();
				if (pAddress.getDateCreated() == null) {
					pAddress.setDateCreated(new Date());
					pAddress.setCreator(context.getAuthenticatedUser());
					pAddress.setPatient(patient);
				}
			}
		if (patient.getNames() != null)
			for (Iterator<PatientName> i = patient.getNames().iterator(); i.hasNext();) {
				PatientName pName = i.next();
				if (pName.getDateCreated() == null) {
					pName.setDateCreated(new Date());
					pName.setCreator(context.getAuthenticatedUser());
					pName.setPatient(patient);
				}
			}
		if (patient.getIdentifiers() != null)
			for (Iterator<PatientIdentifier> i = patient.getIdentifiers().iterator(); i.hasNext();) {
				PatientIdentifier pIdentifier = i.next();
				if (pIdentifier.getDateCreated() == null) {
					pIdentifier.setDateCreated(new Date());
					pIdentifier.setCreator(context.getAuthenticatedUser());
					pIdentifier.setPatient(patient);
				}
			}
	}
	
}
