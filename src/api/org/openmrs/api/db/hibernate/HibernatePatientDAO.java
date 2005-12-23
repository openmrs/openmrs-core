package org.openmrs.api.db.hibernate;

import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.LogicalExpression;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.SimpleExpression;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientAddress;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PatientName;
import org.openmrs.Tribe;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.DAOException;
import org.openmrs.api.db.PatientDAO;

public class HibernatePatientDAO implements PatientDAO {

	protected final Log log = LogFactory.getLog(getClass());
	
	private Context context;
	
	public HibernatePatientDAO(Context c) {
		this.context = c;
	}
	
	/**
	 * @see org.openmrs.api.db.PatientService#getPatient(java.lang.Long)
	 */
	public Patient getPatient(Integer patientId) {
		Session session = HibernateUtil.currentSession();
		HibernateUtil.beginTransaction();
		Patient patient = (Patient) session.get(Patient.class, patientId);
		HibernateUtil.commitTransaction();
		return patient;
	}
	

	public void createPatient(Patient patient) throws DAOException {
		Session session = HibernateUtil.currentSession();
		try {
			HibernateUtil.beginTransaction();

			setCollectionProperties(patient);
			
			session.saveOrUpdate(patient);
			HibernateUtil.commitTransaction();
		}
		catch (Exception e) {
			HibernateUtil.rollbackTransaction();
			throw new DAOException(e.getMessage());
		}
	}


	public void updatePatient(Patient patient) throws DAOException {
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
				throw new DAOException(e.getMessage());
			}
		}
	}

	public Set<Patient> getPatientsByIdentifier(String identifier, boolean includeVoided) throws DAOException {
		Session session = HibernateUtil.currentSession();
		
		Query query;
		
		if (includeVoided) {
			query = session.createQuery("select patient from Patient patient where patient.identifiers.identifier = :id");
			query.setString("id", identifier);
		}
		else {
			query = session.createQuery("select patient from Patient patient where patient.identifiers.identifier = :id and patient.voided = :void");
			query.setString("id", identifier);
			query.setBoolean("void", includeVoided);
		}
		
		List<Patient> patients = query.list();
		
		Set<Patient> returnSet = new HashSet<Patient>();
		returnSet.addAll(patients);
		
		return returnSet;
	}

	public Set<Patient> getPatientsByName(String name, boolean includeVoided) throws DAOException {
		Session session = HibernateUtil.currentSession();
		
		//TODO simple name search to start testing, will need to make "real" name search
		//		i.e. split on whitespace, guess at first/last name, etc
		// TODO return the matched name instead of the primary name
		//   possible solution: "select new" org.openmrs.PatientListItem and return a list of those
		
		Set<Patient> patients = new HashSet<Patient>();
		
		name.replace(", ", " ");
		String[] names = name.split(" ");
		
		Criteria criteria = session.createCriteria(Patient.class).createAlias("names", "name");
		for (String n : names) {
					criteria.add(Expression.or(
							Expression.like("name.familyName", n, MatchMode.START),
							Expression.like("name.givenName", n, MatchMode.START)
						));
		}
		
		if (includeVoided == false) {
			criteria.add(Expression.eq("voided", new Boolean(false)));
		}

		patients.addAll(criteria.list());
		
		return patients;
	}

	public Set<Patient> getSimilarPatients(String name, Date birthdate, String gender) throws DAOException {
		Session session = HibernateUtil.currentSession();
		
		//TODO simple name search to start testing, will need to make "real" name search
		//		i.e. split on whitespace, guess at first/last name, etc
		// TODO return the matched name instead of the primary name
		//   possible solution: "select new" org.openmrs.PatientListItem and return a list of those
		
		Set<Patient> patients = new HashSet<Patient>();
		
		name.replace(", ", " ");
		String[] names = name.split(" ");
		
		Criteria criteria = session.createCriteria(Patient.class).createAlias("names", "name");
		for (String n : names) {
					criteria.add(Expression.or(
							Expression.like("name.familyName", n, MatchMode.START),
							Expression.like("name.givenName", n, MatchMode.START)
						));
		}
		
		LogicalExpression birthdayMatch = Expression.or(
				Expression.eq("birthdate", birthdate),
				Expression.eq("birthdateEstimated", Boolean.TRUE)
				);
		SimpleExpression genderMatch = Expression.eq("gender", gender);
		
		if (birthdate != null && gender != null) {
			criteria.add(Expression.and(birthdayMatch, genderMatch));
		}
		else if (birthdate != null) {
			criteria.add(birthdayMatch);
		}
		else if (gender != null) {
			criteria.add(genderMatch);
		}
		
		patients.addAll(criteria.list());
		
		return patients;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.api.db.PatientService#voidPatient(org.openmrs.Patient,
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
	 * @see org.openmrs.api.db.PatientService#unvoidPatient(org.openmrs.Patient)
	 */
	public void unvoidPatient(Patient patient) {
		patient.setVoided(false);
		patient.setVoidedBy(null);
		patient.setDateVoided(null);
		patient.setVoidReason(null);
		updatePatient(patient);
	}

	/**
	 * @see org.openmrs.api.db.PatientService#deletePatient(org.openmrs.Patient)
	 */
	public void deletePatient(Patient patient) throws DAOException {
		Session session = HibernateUtil.currentSession();
		try {
			HibernateUtil.beginTransaction();
			session.delete(patient);
			HibernateUtil.commitTransaction();
		}
		catch (Exception e) {
			HibernateUtil.rollbackTransaction();
			throw new DAOException(e.getMessage());
		}
	}

	/**
	 * @see org.openmrs.api.db.PatientService#getPatientIdentifierType(java.lang.Integer)
	 */
	public PatientIdentifierType getPatientIdentifierType(Integer patientIdentifierTypeId) throws DAOException {
		Session session = HibernateUtil.currentSession();
		PatientIdentifierType patientIdentifierType = (PatientIdentifierType) session.get(PatientIdentifierType.class, patientIdentifierTypeId);
		
		return patientIdentifierType;
	}

	/**
	 * @see org.openmrs.api.db.PatientService#getPatientIdentifierTypes()
	 */
	public List<PatientIdentifierType> getPatientIdentifierTypes() throws DAOException {
		Session session = HibernateUtil.currentSession();
		
		List<PatientIdentifierType> patientIdentifierTypes = session.createQuery("from PatientIdentifierType p order by p.name").list();
		
		return patientIdentifierTypes;
	}

	/**
	 * @see org.openmrs.api.db.PatientService#getTribe()
	 */
	public Tribe getTribe(Integer tribeId) throws DAOException {
		Session session = HibernateUtil.currentSession();
		
		Tribe tribe = (Tribe)session.get(Tribe.class, tribeId);
		
		return tribe;
	}
	
	/**
	 * @see org.openmrs.api.db.PatientService#getTribes()
	 */
	public List<Tribe> getTribes() throws DAOException {
		Session session = HibernateUtil.currentSession();
		
		List<Tribe> tribes = session.createQuery("from Tribe t order by t.name").list();
		
		return tribes;
	}

	/**
	 * @see org.openmrs.api.db.PatientService#getLocation(java.lang.Integer)
	 */
	public Location getLocation(Integer locationId) throws DAOException {

		Session session = HibernateUtil.currentSession();
		
		Location location = new Location();
		location = (Location)session.get(Location.class, locationId);
		
		return location;

	}

	/**
	 * @see org.openmrs.api.db.PatientService#getLocations()
	 */
	public List<Location> getLocations() throws DAOException {

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
