/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.db.hibernate;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.persister.entity.AbstractEntityPersister;
import org.openmrs.Allergies;
import org.openmrs.Allergy;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PatientIdentifierType.UniquenessBehavior;
import org.openmrs.Person;
import org.openmrs.PersonName;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.DAOException;
import org.openmrs.api.db.PatientDAO;

/**
 * Hibernate specific database methods for the PatientService
 *
 * @see org.openmrs.api.context.Context
 * @see org.openmrs.api.db.PatientDAO
 * @see org.openmrs.api.PatientService
 */
public class HibernatePatientDAO implements PatientDAO {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	/**
	 * Hibernate session factory
	 */
	private SessionFactory sessionFactory;
	
	/**
	 * Set session factory
	 *
	 * @param sessionFactory
	 */
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	/**
	 * @see org.openmrs.api.PatientService#getPatient(java.lang.Integer)
	 */
	public Patient getPatient(Integer patientId) {
		return (Patient) sessionFactory.getCurrentSession().get(Patient.class, patientId);
	}
	
	/**
	 * @see org.openmrs.api.db.PatientDAO#savePatient(org.openmrs.Patient)
	 */
	public Patient savePatient(Patient patient) throws DAOException {
		if (patient.getPatientId() == null) {
			// if we're saving a new patient, just do the normal thing
			// and rows in the person and patient table will be created by
			// hibernate
			sessionFactory.getCurrentSession().saveOrUpdate(patient);
			return patient;
		} else {
			// if we're updating a patient, its possible that a person
			// row exists but a patient row does not. hibernate does not deal
			// with this correctly right now, so we must create a dummy row
			// in the patient table before saving
			
			// Check to make sure we have a row in the patient table already.
			// If we don't have a row, create it so Hibernate doesn't bung
			// things up
			insertPatientStubIfNeeded(patient);
			
			// Note: A merge might be necessary here because hibernate thinks that Patients
			// and Persons are the same objects.  So it sees a Person object in the
			// cache and claims it is a duplicate of this Patient object.
			//patient = (Patient) sessionFactory.getCurrentSession().merge(patient);
			sessionFactory.getCurrentSession().saveOrUpdate(patient);
			
			return patient;
		}
	}
	
	/**
	 * Inserts a row into the patient table This avoids hibernate's bunging of our
	 * person/patient/user inheritance
	 *
	 * @param patient
	 */
	private void insertPatientStubIfNeeded(Patient patient) {
		
		boolean stubInsertNeeded = false;
		
		if (patient.getPatientId() != null) {
			// check if there is a row with a matching patient.patient_id
			String sql = "SELECT 1 FROM patient WHERE patient_id = :patientId";
			Query query = sessionFactory.getCurrentSession().createSQLQuery(sql);
			query.setInteger("patientId", patient.getPatientId());
			
			stubInsertNeeded = (query.uniqueResult() == null);
		}
		
		if (stubInsertNeeded) {
			if (patient.getCreator() == null) { //If not yet persisted
				patient.setCreator(Context.getAuthenticatedUser());
			}
			if (patient.getDateCreated() == null) { //If not yet persisted
				patient.setDateCreated(new Date());
			}
			
			String insert = "INSERT INTO patient (patient_id, creator, voided, date_created) VALUES (:patientId, :creator, 0, :dateCreated)";
			Query query = sessionFactory.getCurrentSession().createSQLQuery(insert);
			query.setInteger("patientId", patient.getPatientId());
			query.setInteger("creator", patient.getCreator().getUserId());
			query.setDate("dateCreated", patient.getDateCreated());
			
			query.executeUpdate();
			
			//Without evicting person, you will get this error when promoting person to patient
			//org.hibernate.NonUniqueObjectException: a different object with the same identifier
			//value was already associated with the session: [org.openmrs.Patient#]
			//see TRUNK-3728
			Person person = (Person) sessionFactory.getCurrentSession().get(Person.class, patient.getPersonId());
			sessionFactory.getCurrentSession().evict(person);
		}
		
	}
	
	/**
	 * @see org.openmrs.api.db.PatientDAO#getPatients(String, boolean, Integer, Integer)
	 * @should return exact match first
	 */
	@Override
	public List<Patient> getPatients(String query, boolean includeVoided, Integer start, Integer length) throws DAOException {
		if (StringUtils.isBlank(query) || (length != null && length < 1)) {
			return Collections.emptyList();
		}
		
		if (start == null || start < 0) {
			start = 0;
		}
		if (length == null) {
			length = HibernatePersonDAO.getMaximumSearchResults();
		}
		
		Criteria criteriaExactMatch = sessionFactory.getCurrentSession().createCriteria(Patient.class);
		criteriaExactMatch = new PatientSearchCriteria(sessionFactory, criteriaExactMatch).prepareCriteria(query, true,
		    false, includeVoided);
		
		criteriaExactMatch.setProjection(Projections.rowCount());
		Integer listSize = ((Number) criteriaExactMatch.uniqueResult()).intValue();
		
		criteriaExactMatch = sessionFactory.getCurrentSession().createCriteria(Patient.class);
		criteriaExactMatch = new PatientSearchCriteria(sessionFactory, criteriaExactMatch).prepareCriteria(query, true,
		    true, includeVoided);
		
		Set<Patient> patients = new LinkedHashSet<Patient>();
		
		if (start < listSize) {
			setFirstAndMaxResult(criteriaExactMatch, start, length);
			patients.addAll(criteriaExactMatch.list());
			
			length -= patients.size();
		}
		
		if (length > 0) {
			Criteria criteriaAllMatch = sessionFactory.getCurrentSession().createCriteria(Patient.class);
			criteriaAllMatch = new PatientSearchCriteria(sessionFactory, criteriaAllMatch).prepareCriteria(query, null,
			    false, includeVoided);
			criteriaAllMatch.setProjection(Projections.rowCount());
			
			start -= listSize;
			listSize = ((Number) criteriaAllMatch.uniqueResult()).intValue();
			
			criteriaAllMatch = sessionFactory.getCurrentSession().createCriteria(Patient.class);
			criteriaAllMatch = new PatientSearchCriteria(sessionFactory, criteriaAllMatch).prepareCriteria(query, null,
			    true, includeVoided);
			
			if (start < listSize) {
				setFirstAndMaxResult(criteriaAllMatch, start, length);
				
				List<Patient> patientsList = criteriaAllMatch.list();
				
				patients.addAll(patientsList);
				
				length -= patientsList.size();
			}
		}
		
		if (length > 0) {
			Criteria criteriaNoExactMatch = sessionFactory.getCurrentSession().createCriteria(Patient.class);
			criteriaNoExactMatch = new PatientSearchCriteria(sessionFactory, criteriaNoExactMatch).prepareCriteria(query,
			    false, false, includeVoided);
			criteriaNoExactMatch.setProjection(Projections.rowCount());
			
			start -= listSize;
			listSize = ((Number) criteriaNoExactMatch.uniqueResult()).intValue();
			
			criteriaNoExactMatch = sessionFactory.getCurrentSession().createCriteria(Patient.class);
			criteriaNoExactMatch = new PatientSearchCriteria(sessionFactory, criteriaNoExactMatch).prepareCriteria(query,
			    false, true, includeVoided);
			
			if (start < listSize) {
				setFirstAndMaxResult(criteriaNoExactMatch, start, length);
				patients.addAll(criteriaNoExactMatch.list());
			}
		}
		return new ArrayList<Patient>(patients);
	}
	
	/**
	 * @see org.openmrs.api.db.PatientDAO#getPatients(String, Integer, Integer)
	 */
	@Override
	public List<Patient> getPatients(String query, Integer start, Integer length) throws DAOException {
		return getPatients(query, false, start, length);
	}
	
	private void setFirstAndMaxResult(Criteria criteria, Integer start, Integer length) {
		if (start != null) {
			criteria.setFirstResult(start);
		}
		
		int maximumSearchResults = HibernatePersonDAO.getMaximumSearchResults();
		if (length != null && length < maximumSearchResults) {
			criteria.setMaxResults(length);
		} else {
			if (log.isDebugEnabled()) {
				log.debug("Limiting the size of the number of matching patients to " + maximumSearchResults);
			}
			criteria.setMaxResults(maximumSearchResults);
		}
	}
	
	/**
	 * @see org.openmrs.api.db.PatientDAO#getAllPatients(boolean)
	 */
	@SuppressWarnings("unchecked")
	public List<Patient> getAllPatients(boolean includeVoided) throws DAOException {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Patient.class);
		
		if (!includeVoided) {
			criteria.add(Restrictions.eq("voided", false));
		}
		
		return criteria.list();
	}
	
	/**
	 * @see org.openmrs.api.PatientService#purgePatientIdentifierType(org.openmrs.PatientIdentifierType)
	 * @see org.openmrs.api.db.PatientDAO#deletePatientIdentifierType(org.openmrs.PatientIdentifierType)
	 */
	public void deletePatientIdentifierType(PatientIdentifierType patientIdentifierType) throws DAOException {
		sessionFactory.getCurrentSession().delete(patientIdentifierType);
	}
	
	/**
	 * @see org.openmrs.api.PatientService#getPatientIdentifiers(java.lang.String, java.util.List, java.util.List, java.util.List, java.lang.Boolean)
	 */
	@SuppressWarnings("unchecked")
	public List<PatientIdentifier> getPatientIdentifiers(String identifier,
	        List<PatientIdentifierType> patientIdentifierTypes, List<Location> locations, List<Patient> patients,
	        Boolean isPreferred) throws DAOException {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(PatientIdentifier.class);
		
		// join with the patient table to prevent patient identifiers from patients
		// that already voided getting returned
		criteria.createAlias("patient", "patient");
		criteria.add(Restrictions.eq("patient.voided", false));
		
		// make sure the patient object isn't voided
		criteria.add(Restrictions.eq("voided", false));
		
		if (identifier != null) {
			criteria.add(Restrictions.eq("identifier", identifier));
		}
		
		// TODO add junit test for getting by identifier type
		if (patientIdentifierTypes.size() > 0) {
			criteria.add(Restrictions.in("identifierType", patientIdentifierTypes));
		}
		
		if (locations.size() > 0) {
			criteria.add(Restrictions.in("location", locations));
		}
		
		// TODO add junit test for getting by patients
		if (patients.size() > 0) {
			criteria.add(Restrictions.in("patient", patients));
		}
		
		if (isPreferred != null) {
			criteria.add(Restrictions.eq("preferred", isPreferred));
		}
		
		return criteria.list();
	}
	
	/**
	 * @see org.openmrs.api.db.PatientDAO#savePatientIdentifierType(org.openmrs.PatientIdentifierType)
	 */
	public PatientIdentifierType savePatientIdentifierType(PatientIdentifierType patientIdentifierType) throws DAOException {
		sessionFactory.getCurrentSession().saveOrUpdate(patientIdentifierType);
		return patientIdentifierType;
	}
	
	/**
	 * @see org.openmrs.api.PatientService#deletePatient(org.openmrs.Patient)
	 */
	public void deletePatient(Patient patient) throws DAOException {
		HibernatePersonDAO.deletePersonAndAttributes(sessionFactory, patient);
	}
	
	/**
	 * @see org.openmrs.api.PatientService#getPatientIdentifierType(java.lang.Integer)
	 */
	public PatientIdentifierType getPatientIdentifierType(Integer patientIdentifierTypeId) throws DAOException {
		return (PatientIdentifierType) sessionFactory.getCurrentSession().get(PatientIdentifierType.class,
		    patientIdentifierTypeId);
	}
	
	/**
	 * @should not return null when includeRetired is false
	 * @should not return retired when includeRetired is false
	 * @should not return null when includeRetired is true
	 * @should return all when includeRetired is true
	 * @should return ordered
	 * @see org.openmrs.api.db.PatientDAO#getAllPatientIdentifierTypes(boolean)
	 */
	@SuppressWarnings("unchecked")
	public List<PatientIdentifierType> getAllPatientIdentifierTypes(boolean includeRetired) throws DAOException {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(PatientIdentifierType.class);
		
		if (!includeRetired) {
			criteria.add(Restrictions.eq("retired", false));
		} else {
			criteria.addOrder(Order.asc("retired")); //retired last
		}
		
		criteria.addOrder(Order.desc("required")); //required first
		criteria.addOrder(Order.asc("name"));
		criteria.addOrder(Order.asc("patientIdentifierTypeId"));
		
		return criteria.list();
	}
	
	/**
	 * @see org.openmrs.api.db.PatientDAO#getPatientIdentifierTypes(java.lang.String,
	 *      java.lang.String, java.lang.Boolean, java.lang.Boolean)
	 *
	 * @should return non retired patient identifier types with given name
	 * @should return non retired patient identifier types with given format
	 * @should return non retired patient identifier types that are not required
	 * @should return non retired patient identifier types that are required
	 * @should return non retired patient identifier types that has checkDigit
	 * @should return non retired patient identifier types that has not CheckDigit
	 * @should return only non retired patient identifier types
	 * @should return non retired patient identifier types ordered by required first
	 * @should return non retired patient identifier types ordered by required and name
	 * @should return non retired patient identifier types ordered by required name and type id
	 *
	 */
	@SuppressWarnings("unchecked")
	public List<PatientIdentifierType> getPatientIdentifierTypes(String name, String format, Boolean required,
	        Boolean hasCheckDigit) throws DAOException {
		
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(PatientIdentifierType.class);
		
		if (name != null) {
			criteria.add(Restrictions.eq("name", name));
		}
		
		if (format != null) {
			criteria.add(Restrictions.eq("format", format));
		}
		
		if (required != null) {
			criteria.add(Restrictions.eq("required", required));
		}
		
		if (hasCheckDigit != null) {
			criteria.add(Restrictions.eq("checkDigit", hasCheckDigit));
		}
		
		criteria.add(Restrictions.eq("retired", false));
		
		criteria.addOrder(Order.desc("required")); //required first
		criteria.addOrder(Order.asc("name"));
		criteria.addOrder(Order.asc("patientIdentifierTypeId"));
		
		return criteria.list();
	}
	
	/**
	 * @see org.openmrs.api.db.PatientDAO#getDuplicatePatientsByAttributes(java.util.List)
	 */
	@SuppressWarnings("unchecked")
	public List<Patient> getDuplicatePatientsByAttributes(List<String> attributes) {
		List<Patient> patients = new Vector<Patient>();
		List<Integer> patientIds = new Vector<Integer>();

		if (attributes.size() > 0) {

			String sqlString = getDuplicatePatientsSQLString(attributes);
			if(sqlString != null) {

				SQLQuery sqlquery = sessionFactory.getCurrentSession().createSQLQuery(sqlString);
				patientIds = sqlquery.list();
				if (patientIds.size() >= 1) {
					Query query = sessionFactory.getCurrentSession().createQuery(
							"from Patient p1 where p1.patientId in (:ids)");
					query.setParameterList("ids", patientIds);
					patients = query.list();
				}
			}
		
		}

		sortDuplicatePatients(patients, patientIds);
		return patients;
	}

	private String getDuplicatePatientsSQLString(List<String> attributes) {
		String outerSelect = "select distinct t1.patient_id from patient t1 ";

		Class patient = Patient.class;
		Set<String> patientFieldNames = new HashSet<String>(patient.getDeclaredFields().length);
		for (Field field : patient.getDeclaredFields()) {
			patientFieldNames.add(field.getName());
			log.debug(field.getName());
		}

		Class person = Person.class;
		Set<String> personFieldNames = new HashSet<String>(person.getDeclaredFields().length);
		for (Field field : person.getDeclaredFields()) {
			personFieldNames.add(field.getName());
			log.debug(field.getName());
		}

		Class personName = PersonName.class;

		Set<String> personNameFieldNames = new HashSet<String>(personName.getDeclaredFields().length);
		for (Field field : personName.getDeclaredFields()) {
			personNameFieldNames.add(field.getName());
			log.debug(field.getName());
		}

		Class identifier = PatientIdentifier.class;
		Set<String> identifierFieldNames = new HashSet<String>(identifier.getDeclaredFields().length);
		for (Field field : identifier.getDeclaredFields()) {
			identifierFieldNames.add(field.getName());
			log.debug(field.getName());
		}
		List<String> whereConditions = new ArrayList<String>();


		List<String> innerFields = new ArrayList<String>();
		String innerSelect = " from patient p1 ";

		for (String attribute : attributes) {
			if (attribute != null) {
				attribute = attribute.trim();
			}
			if (patientFieldNames.contains(attribute)) {

				AbstractEntityPersister aep = ((AbstractEntityPersister) sessionFactory.getClassMetadata(Patient.class));
				String[] properties = aep.getPropertyColumnNames(attribute);
				if (properties.length >= 1) {
					attribute = properties[0];
				}
				whereConditions.add(" t1." + attribute + " = t5." + attribute);
				innerFields.add("p1." + attribute);
			} else if (personFieldNames.contains(attribute)) {
				if (!outerSelect.contains("person")) {
					outerSelect += "inner join person t2 on t1.patient_id = t2.person_id ";
					innerSelect += "inner join person person1 on p1.patient_id = person1.person_id ";
				}

				AbstractEntityPersister aep = ((AbstractEntityPersister) sessionFactory.getClassMetadata(Person.class));
				if (aep != null) {
					String[] properties = aep.getPropertyColumnNames(attribute);
					if (properties != null && properties.length >= 1) {
						attribute = properties[0];
					}
				}

				whereConditions.add(" t2." + attribute + " = t5." + attribute);
				innerFields.add("person1." + attribute);
			} else if (personNameFieldNames.contains(attribute)) {
				if (!outerSelect.contains("person_name")) {
					outerSelect += "inner join person_name t3 on t2.person_id = t3.person_id ";
					innerSelect += "inner join person_name pn1 on person1.person_id = pn1.person_id ";
				}

				//Since we are firing a native query get the actual table column name from the field name of the entity
				AbstractEntityPersister aep = ((AbstractEntityPersister) sessionFactory
						.getClassMetadata(PersonName.class));
				if (aep != null) {
					String[] properties = aep.getPropertyColumnNames(attribute);

					if (properties != null && properties.length >= 1) {
						attribute = properties[0];
					}
				}

				whereConditions.add(" t3." + attribute + " = t5." + attribute);
				innerFields.add("pn1." + attribute);
			} else if (identifierFieldNames.contains(attribute)) {
				if (!outerSelect.contains("patient_identifier")) {
					outerSelect += "inner join patient_identifier t4 on t1.patient_id = t4.patient_id ";
					innerSelect += "inner join patient_identifier pi1 on p1.patient_id = pi1.patient_id ";
				}

				AbstractEntityPersister aep = ((AbstractEntityPersister) sessionFactory
						.getClassMetadata(PatientIdentifier.class));
				if (aep != null) {

					String[] properties = aep.getPropertyColumnNames(attribute);
					if (properties != null && properties.length >= 1) {
						attribute = properties[0];
					}
				}

				whereConditions.add(" t4." + attribute + " = t5." + attribute);
				innerFields.add("pi1." + attribute);
			} else {
				log.warn("Unidentified attribute: " + attribute);
			}
		}
		if(innerFields.size() > 0 || whereConditions.size() > 0) {
			String innerFieldsJoined = StringUtils.join(innerFields, ", ");
			String whereFieldsJoined = StringUtils.join(whereConditions, " and ");
			String innerWhereCondition = "";
			if (!attributes.contains("includeVoided")) {
				innerWhereCondition = " where p1.voided = false ";
			}
			String innerQuery = "(Select " + innerFieldsJoined + innerSelect + innerWhereCondition + " group by "
					+ innerFieldsJoined + " having count(*) > 1" + " order by " + innerFieldsJoined + ") t5";
			return outerSelect + ", " + innerQuery + " where " + whereFieldsJoined + ";";
		}
		return null;
	}

	private void sortDuplicatePatients(List<Patient> patients, List<Integer> patientIds) {

		Map<Integer, Integer> patientIdOrder = new HashMap<Integer, Integer>();
		int startPos = 0;
		for (Integer id : patientIds) {
			patientIdOrder.put(id, startPos++);
		}
		class PatientIdComparator implements Comparator<Patient> {

			private Map<Integer, Integer> sortOrder;

			public PatientIdComparator(Map<Integer, Integer> sortOrder) {
				this.sortOrder = sortOrder;
			}

			@Override
			public int compare(Patient patient1, Patient patient2) {
				Integer patPos1 = sortOrder.get(patient1.getPatientId());
				if (patPos1 == null) {
					throw new IllegalArgumentException("Bad patient encountered: " + patient1.getPatientId());
				}
				Integer patPos2 = sortOrder.get(patient2.getPatientId());
				if (patPos2 == null) {
					throw new IllegalArgumentException("Bad patient encountered: " + patient2.getPatientId());
				}
				return patPos1.compareTo(patPos2);
			}
		}
		Collections.sort(patients, new PatientIdComparator(patientIdOrder));
	}
	
	/**
	 * @see org.openmrs.api.db.PatientDAO#getPatientByUuid(java.lang.String)
	 */
	public Patient getPatientByUuid(String uuid) {
		Patient p = null;
		
		p = (Patient) sessionFactory.getCurrentSession().createQuery("from Patient p where p.uuid = :uuid").setString(
		    "uuid", uuid).uniqueResult();
		
		return p;
	}
	
	public PatientIdentifier getPatientIdentifierByUuid(String uuid) {
		return (PatientIdentifier) sessionFactory.getCurrentSession().createQuery(
		    "from PatientIdentifier p where p.uuid = :uuid").setString("uuid", uuid).uniqueResult();
	}
	
	/**
	 * @see org.openmrs.api.db.PatientDAO#getPatientIdentifierTypeByUuid(java.lang.String)
	 */
	public PatientIdentifierType getPatientIdentifierTypeByUuid(String uuid) {
		return (PatientIdentifierType) sessionFactory.getCurrentSession().createQuery(
		    "from PatientIdentifierType pit where pit.uuid = :uuid").setString("uuid", uuid).uniqueResult();
	}
	
	/**
	 * This method uses a SQL query and does not load anything into the hibernate session. It exists
	 * because of ticket #1375.
	 *
	 * @see org.openmrs.api.db.PatientDAO#isIdentifierInUseByAnotherPatient(org.openmrs.PatientIdentifier)
	 */
	public boolean isIdentifierInUseByAnotherPatient(PatientIdentifier patientIdentifier) {
		boolean checkPatient = patientIdentifier.getPatient() != null
		        && patientIdentifier.getPatient().getPatientId() != null;
		boolean checkLocation = patientIdentifier.getLocation() != null
		        && patientIdentifier.getIdentifierType().getUniquenessBehavior() == UniquenessBehavior.LOCATION;
		
		// switched this to an hql query so the hibernate cache can be considered as well as the database
		String hql = "select count(*) from PatientIdentifier pi, Patient p where pi.patient.patientId = p.patient.patientId "
		        + "and p.voided = false and pi.voided = false and pi.identifier = :identifier and pi.identifierType = :idType";
		
		if (checkPatient) {
			hql += " and p.patientId != :ptId";
		}
		if (checkLocation) {
			hql += " and pi.location = :locationId";
		}
		
		Query query = sessionFactory.getCurrentSession().createQuery(hql);
		query.setString("identifier", patientIdentifier.getIdentifier());
		query.setInteger("idType", patientIdentifier.getIdentifierType().getPatientIdentifierTypeId());
		if (checkPatient) {
			query.setInteger("ptId", patientIdentifier.getPatient().getPatientId());
		}
		if (checkLocation) {
			query.setInteger("locationId", patientIdentifier.getLocation().getLocationId());
		}
		return !query.uniqueResult().toString().equals("0");
	}
	
	/**
	 * @see org.openmrs.api.db.PatientDAO#getPatientIdentifier(java.lang.Integer)
	 */
	public PatientIdentifier getPatientIdentifier(Integer patientIdentifierId) throws DAOException {
		
		return (PatientIdentifier) sessionFactory.getCurrentSession().get(PatientIdentifier.class, patientIdentifierId);
		
	}
	
	/**
	 * @see org.openmrs.api.db.PatientDAO#savePatientIdentifier(org.openmrs.PatientIdentifier)
	 */
	public PatientIdentifier savePatientIdentifier(PatientIdentifier patientIdentifier) {
		
		sessionFactory.getCurrentSession().saveOrUpdate(patientIdentifier);
		return patientIdentifier;
		
	}
	
	/**
	 * @see org.openmrs.api.PatientService#purgePatientIdentifier(org.openmrs.PatientIdentifier)
	 * @see org.openmrs.api.db.PatientDAO#deletePatientIdentifier(org.openmrs.PatientIdentifier)
	 */
	public void deletePatientIdentifier(PatientIdentifier patientIdentifier) throws DAOException {
		
		sessionFactory.getCurrentSession().delete(patientIdentifier);
		
	}
	
	/**
	 * @see org.openmrs.api.db.PatientDAO#getCountOfPatients(String)
	 */
	public Long getCountOfPatients(String query) {
		if (StringUtils.isBlank(query)) {
			return 0L;
		}
		
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Patient.class);
		criteria = new PatientSearchCriteria(sessionFactory, criteria).prepareCriteria(query);
		
		// Using Hibernate projections did NOT work here, the resulting queries could not be executed due to
		// missing group-by clauses. Hence the poor man's implementation of counting search results.
		//
		return (long) criteria.list().size();
	}
	
	/**
	 * @see org.openmrs.api.db.PatientDAO#getCountOfPatients(String, boolean)
	 */
	public Long getCountOfPatients(String query, boolean includeVoided) {
		if (StringUtils.isBlank(query)) {
			return 0L;
		}
		
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Patient.class);
		criteria = new PatientSearchCriteria(sessionFactory, criteria).prepareCriteria(query, includeVoided);
		
		// Using Hibernate projections did NOT work here, the resulting queries could not be executed due to
		// missing group-by clauses. Hence the poor man's implementation of counting search results.
		//
		return (long) criteria.list().size();
	}
	
	/**
	 * @see org.openmrs..api.db.PatientDAO#getAllergies(org.openmrs.Patient)
	 */
	//@Override
	public List<Allergy> getAllergies(Patient patient) {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Allergy.class);
		criteria.add(Restrictions.eq("patient", patient));
		criteria.add(Restrictions.eq("voided", false));
		return criteria.list();
	}
	
	/**
	 * @see org.openmrs.api.db.PatientDAO#getAllergyStatus(org.openmrs.Patient)
	 */
	//@Override
	public String getAllergyStatus(Patient patient) {

		return (String) sessionFactory.getCurrentSession().createSQLQuery(
			    "select allergy_status from patient where patient_id = :patientId").setInteger("patientId", patient.getPatientId()).uniqueResult();
	}
	
	/**
	 * @see org.openmrs.api.db.PatientDAO#saveAllergies(org.openmrs.Patient,
	 *      org.openmrsallergyapi.Allergies)
	 */
	@Override
	public Allergies saveAllergies(Patient patient, Allergies allergies) {

		sessionFactory.getCurrentSession().createSQLQuery(
			    "update patient set allergy_status = :allergyStatus where patient_id = :patientId")
			    .setInteger("patientId", patient.getPatientId())
			    .setString("allergyStatus", allergies.getAllergyStatus())
			    .executeUpdate();
		
		for (Allergy allergy : allergies) {
			sessionFactory.getCurrentSession().save(allergy);
		}
			
		return allergies;
	}
	
	/**
	 * @see org.openmrs.PatientDAO#getAllergy(Integer)
	 */
	public Allergy getAllergy(Integer allergyId) {
		return (Allergy) sessionFactory.getCurrentSession().createQuery("from Allergy a where a.allergyId = :allergyId")
				.setInteger("allergyId", allergyId).uniqueResult();
	}
	
	/**
	 * @see org.openmrs.PatientDAO#getAllergyByUuid(String)
	 */
	public Allergy getAllergyByUuid(String uuid) {
		return (Allergy) sessionFactory.getCurrentSession().createQuery("from Allergy a where a.uuid = :uuid")
				.setString("uuid", uuid).uniqueResult();
	}

	/**
     * @see org.openmrs.api.db.PatientDAO#saveAllergy(org.openmrs.Allergy)
     */
    @Override
    public Allergy saveAllergy(Allergy allergy) {
    	sessionFactory.getCurrentSession().save(allergy);
    	return allergy;
    }
}
