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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
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
import org.openmrs.PersonAttribute;
import org.openmrs.PersonName;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.DAOException;
import org.openmrs.api.db.PatientDAO;
import org.openmrs.api.db.hibernate.search.LuceneQuery;
import org.openmrs.collection.ListPart;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.OpenmrsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Hibernate specific database methods for the PatientService
 *
 * @see org.openmrs.api.context.Context
 * @see org.openmrs.api.db.PatientDAO
 * @see org.openmrs.api.PatientService
 */
public class HibernatePatientDAO implements PatientDAO {
	
	private static final Logger log = LoggerFactory.getLogger(HibernatePatientDAO.class);
	
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
     * @param patientId  internal patient identifier
     * @return           patient with given internal identifier
	 * @see org.openmrs.api.PatientService#getPatient(java.lang.Integer)
	 */
        @Override
	public Patient getPatient(Integer patientId) {
		return (Patient) sessionFactory.getCurrentSession().get(Patient.class, patientId);
	}
	
	/**
     * @param patient  patient to be created or updated
     * @return         patient who was created or updated
	 * @see org.openmrs.api.db.PatientDAO#savePatient(org.openmrs.Patient)
	 */
        @Override
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
			//If not yet persisted
			if (patient.getCreator() == null) {
				patient.setCreator(Context.getAuthenticatedUser());
			}
			//If not yet persisted
			if (patient.getDateCreated() == null) {
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
	public List<Patient> getPatients(String query, List<PatientIdentifierType> identifierTypes,
		boolean matchIdentifierExactly, Integer start, Integer length) throws DAOException{
		
		if (StringUtils.isBlank(query) || (length != null && length < 1) || identifierTypes == null || identifierTypes.size() < 1)  {
			return Collections.emptyList();
		}
		
		Integer tmpStart = start;
		if (tmpStart == null || tmpStart < 0) {
			tmpStart = 0;
		}
		
		Integer tmpLength = length;
		if (tmpLength == null) {
			tmpLength = HibernatePersonDAO.getMaximumSearchResults();
		}
		
		List<Patient> patients = findPatients(query, identifierTypes, matchIdentifierExactly, tmpStart, tmpLength);
		
		return patients;
		
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

		Integer tmpStart = start;
		if (tmpStart == null || tmpStart < 0) {
			tmpStart = 0;
		}

		Integer tmpLength = length;
		if (tmpLength == null) {
			tmpLength = HibernatePersonDAO.getMaximumSearchResults();
		}

		List<Patient> patients = findPatients(query, includeVoided, tmpStart, tmpLength);

		return new ArrayList<>(patients);
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
        @Override
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
        @Override
	public void deletePatientIdentifierType(PatientIdentifierType patientIdentifierType) throws DAOException {
		sessionFactory.getCurrentSession().delete(patientIdentifierType);
	}
	
	/**
	 * @see org.openmrs.api.PatientService#getPatientIdentifiers(java.lang.String, java.util.List, java.util.List, java.util.List, java.lang.Boolean)
	 */
	@SuppressWarnings("unchecked")
        @Override
	public List<PatientIdentifier> getPatientIdentifiers(String identifier,
	        List<PatientIdentifierType> patientIdentifierTypes, List<Location> locations, List<Patient> patients,
	        Boolean isPreferred) throws DAOException {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(PatientIdentifier.class);
		
		// join with the patient table to prevent patient identifiers from patients
		// that already voided getting returned
		criteria.createAlias("patient", "patient");

		criteria.add(Restrictions.eq("patient.voided", false));
		
		criteria.add(Restrictions.eq("voided", false));
		
		if (identifier != null) {
			criteria.add(Restrictions.eq("identifier", identifier));
		}
		
		if (!patientIdentifierTypes.isEmpty()) {
			criteria.add(Restrictions.in("identifierType", patientIdentifierTypes));
		}
		
		if (!locations.isEmpty()) {
			criteria.add(Restrictions.in("location", locations));
		}
		
		if (!patients.isEmpty()) {
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
        @Override
	public PatientIdentifierType savePatientIdentifierType(PatientIdentifierType patientIdentifierType) throws DAOException {
		sessionFactory.getCurrentSession().saveOrUpdate(patientIdentifierType);
		return patientIdentifierType;
	}
	
	/**
	 * @see org.openmrs.api.PatientDAO#deletePatient(org.openmrs.Patient)
	 */
        @Override
	public void deletePatient(Patient patient) throws DAOException {
		HibernatePersonDAO.deletePersonAndAttributes(sessionFactory, patient);
	}
	
	/**
	 * @see org.openmrs.api.PatientService#getPatientIdentifierType(java.lang.Integer)
	 */
        @Override
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
        @Override
	public List<PatientIdentifierType> getAllPatientIdentifierTypes(boolean includeRetired) throws DAOException {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(PatientIdentifierType.class);
		
		if (!includeRetired) {
			criteria.add(Restrictions.eq("retired", false));
		} else {
			//retired last
			criteria.addOrder(Order.asc("retired"));
		}
		
		//required first
		criteria.addOrder(Order.desc("required"));
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
        @Override
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
		
		//required first
		criteria.addOrder(Order.desc("required"));
		criteria.addOrder(Order.asc("name"));
		criteria.addOrder(Order.asc("patientIdentifierTypeId"));
		
		return criteria.list();
	}
	
	/**
     * @param attributes attributes on a Person or Patient object. similar to: [gender, givenName,
     *                   middleName, familyName]
     * @return           list of patients that match other patients
	 * @see org.openmrs.api.db.PatientDAO#getDuplicatePatientsByAttributes(java.util.List)
	 */
	@SuppressWarnings("unchecked")
        @Override
	public List<Patient> getDuplicatePatientsByAttributes(List<String> attributes) {
		List<Patient> patients = new ArrayList<>();
		List<Integer> patientIds = new ArrayList<>();

		if (!attributes.isEmpty()) {

			String sqlString = getDuplicatePatientsSQLString(attributes);
			if(sqlString != null) {

				SQLQuery sqlquery = sessionFactory.getCurrentSession().createSQLQuery(sqlString);
				patientIds = sqlquery.list();
				if (!patientIds.isEmpty()) {
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
		StringBuilder outerSelect = new StringBuilder("select distinct t1.patient_id from patient t1 ");
		final String t5 = " = t5.";
		
		Set<String> patientFieldNames = OpenmrsUtil.getDeclaredFields(Patient.class);
		Set<String> personFieldNames = OpenmrsUtil.getDeclaredFields(Person.class);
		Set<String> personNameFieldNames = OpenmrsUtil.getDeclaredFields(PersonName.class);
		Set<String> identifierFieldNames = OpenmrsUtil.getDeclaredFields(PatientIdentifier.class);

		List<String> whereConditions = new ArrayList<>();


		List<String> innerFields = new ArrayList<>();
		StringBuilder innerSelect = new StringBuilder(" from patient p1 ");

		for (String attribute : attributes) {
			if (attribute != null) {
				attribute = attribute.trim();
			}
			if (patientFieldNames.contains(attribute)) {

				AbstractEntityPersister aep = (AbstractEntityPersister) sessionFactory.getClassMetadata(Patient.class);
				String[] properties = aep.getPropertyColumnNames(attribute);
				if (properties.length >= 1) {
					attribute = properties[0];
				}
				whereConditions.add(" t1." + attribute + t5 + attribute);
				innerFields.add("p1." + attribute);
			} else if (personFieldNames.contains(attribute)) {
				if (!outerSelect.toString().contains("person")) {
					outerSelect.append("inner join person t2 on t1.patient_id = t2.person_id ");
					innerSelect.append("inner join person person1 on p1.patient_id = person1.person_id ");
				}

				AbstractEntityPersister aep = (AbstractEntityPersister) sessionFactory.getClassMetadata(Person.class);
				if (aep != null) {
					String[] properties = aep.getPropertyColumnNames(attribute);
					if (properties != null && properties.length >= 1) {
						attribute = properties[0];
					}
				}

				whereConditions.add(" t2." + attribute + t5 + attribute);
				innerFields.add("person1." + attribute);
			} else if (personNameFieldNames.contains(attribute)) {
				if (!outerSelect.toString().contains("person_name")) {
					outerSelect.append("inner join person_name t3 on t2.person_id = t3.person_id ");
					innerSelect.append("inner join person_name pn1 on person1.person_id = pn1.person_id ");
				}

				//Since we are firing a native query get the actual table column name from the field name of the entity
				AbstractEntityPersister aep = (AbstractEntityPersister) sessionFactory
						.getClassMetadata(PersonName.class);
				if (aep != null) {
					String[] properties = aep.getPropertyColumnNames(attribute);

					if (properties != null && properties.length >= 1) {
						attribute = properties[0];
					}
				}

				whereConditions.add(" t3." + attribute + t5 + attribute);
				innerFields.add("pn1." + attribute);
			} else if (identifierFieldNames.contains(attribute)) {
				if (!outerSelect.toString().contains("patient_identifier")) {
					outerSelect.append("inner join patient_identifier t4 on t1.patient_id = t4.patient_id ");
					innerSelect.append("inner join patient_identifier pi1 on p1.patient_id = pi1.patient_id ");
				}

				AbstractEntityPersister aep = (AbstractEntityPersister) sessionFactory
						.getClassMetadata(PatientIdentifier.class);
				if (aep != null) {

					String[] properties = aep.getPropertyColumnNames(attribute);
					if (properties != null && properties.length >= 1) {
						attribute = properties[0];
					}
				}

				whereConditions.add(" t4." + attribute + t5 + attribute);
				innerFields.add("pi1." + attribute);
			} else {
				log.warn("Unidentified attribute: " + attribute);
			}
		}
		if(CollectionUtils.isNotEmpty(innerFields) || CollectionUtils.isNotEmpty(whereConditions)) {
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

		Map<Integer, Integer> patientIdOrder = new HashMap<>();
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
		patients.sort(new PatientIdComparator(patientIdOrder));
	}
	
	/**
	 * @see org.openmrs.api.db.PatientDAO#getPatientByUuid(java.lang.String)
	 */
        @Override
	public Patient getPatientByUuid(String uuid) {
		Patient p;
		
		p = (Patient) sessionFactory.getCurrentSession().createQuery("from Patient p where p.uuid = :uuid").setString(
		    "uuid", uuid).uniqueResult();
		
		return p;
	}
	
        @Override
	public PatientIdentifier getPatientIdentifierByUuid(String uuid) {
		return (PatientIdentifier) sessionFactory.getCurrentSession().createQuery(
		    "from PatientIdentifier p where p.uuid = :uuid").setString("uuid", uuid).uniqueResult();
	}
	
	/**
	 * @see org.openmrs.api.db.PatientDAO#getPatientIdentifierTypeByUuid(java.lang.String)
	 */
        @Override
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
        @Override
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
		return !"0".equals(query.uniqueResult().toString());
	}
	
	/**
     * @param patientIdentifierId  the patientIdentifier id
     * @return                     the patientIdentifier matching the Id
	 * @see org.openmrs.api.db.PatientDAO#getPatientIdentifier(java.lang.Integer)
	 */
        @Override
	public PatientIdentifier getPatientIdentifier(Integer patientIdentifierId) throws DAOException {
		
		return (PatientIdentifier) sessionFactory.getCurrentSession().get(PatientIdentifier.class, patientIdentifierId);
		
	}
	
	/**
     * @param patientIdentifier patientIndentifier to be created or updated
     * @return                  patientIndentifier that was created or updated
	 * @see org.openmrs.api.db.PatientDAO#savePatientIdentifier(org.openmrs.PatientIdentifier)
	 */
        @Override
	public PatientIdentifier savePatientIdentifier(PatientIdentifier patientIdentifier) {
		
		sessionFactory.getCurrentSession().saveOrUpdate(patientIdentifier);
		return patientIdentifier;
		
	}
	
	/**
	 * @see org.openmrs.api.PatientService#purgePatientIdentifier(org.openmrs.PatientIdentifier)
	 * @see org.openmrs.api.db.PatientDAO#deletePatientIdentifier(org.openmrs.PatientIdentifier)
	 */
        @Override
	public void deletePatientIdentifier(PatientIdentifier patientIdentifier) throws DAOException {
		
		sessionFactory.getCurrentSession().delete(patientIdentifier);
		
	}
	
	/**
         * @param query  the string to search on
         * @return       the number of patients matching the given search phrase
	 * @see org.openmrs.api.db.PatientDAO#getCountOfPatients(String)
	 */
        @Override
	public Long getCountOfPatients(String query) {
		return getCountOfPatients(query, false);
	}
	
	/**
         * @param query          the string to search on
         * @param includeVoided  true/false whether or not to included voided patients
         * @return               the number of patients matching the given search phrase
	 * @see org.openmrs.api.db.PatientDAO#getCountOfPatients(String, boolean)
	 */
	@Override
	public Long getCountOfPatients(String query, boolean includeVoided) {
		if (StringUtils.isBlank(query)) {
			return 0L;
		}
		String tmpQuery = LuceneQuery.escapeQuery(query);

		LuceneQuery<PatientIdentifier> identifierQuery = getPatientIdentifierLuceneQuery(tmpQuery, includeVoided, false);

		PersonLuceneQuery personLuceneQuery = new PersonLuceneQuery(sessionFactory);

		LuceneQuery<PersonName> nameQuery = personLuceneQuery.getPatientNameQuery(tmpQuery, includeVoided, identifierQuery);
		LuceneQuery<PersonAttribute> attributeQuery = personLuceneQuery.getPatientAttributeQuery(tmpQuery, includeVoided, nameQuery);

		return identifierQuery.resultSize() + nameQuery.resultSize() + attributeQuery.resultSize();
	}

    private List<Patient> findPatients(String query, boolean includeVoided) {
		return findPatients(query, includeVoided, null, null);
	}
	
	private List<Patient> findPatients(String query, List<PatientIdentifierType> identifierTypes, boolean matchExactly, Integer start, Integer length) {
		String tmpQuery = query;
		Integer tmpStart = start;
		
		if (tmpStart == null) {
			tmpStart = 0;
		}
		Integer maxLength = HibernatePersonDAO.getMaximumSearchResults();
		Integer tmpLength = length;
		if (tmpLength == null || tmpLength > maxLength) {
			tmpLength = maxLength;
		}
		tmpQuery = LuceneQuery.escapeQuery(tmpQuery);
		
		List<Patient> patients = new LinkedList<>();
		
		String minChars = Context.getAdministrationService().getGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_MIN_SEARCH_CHARACTERS);
		
		if (minChars == null || !StringUtils.isNumeric(minChars)) {
			minChars = "" + OpenmrsConstants.GLOBAL_PROPERTY_DEFAULT_MIN_SEARCH_CHARACTERS;
		}
		if (tmpQuery.length() < Integer.valueOf(minChars)) {
			return patients;
		}
		LuceneQuery<PatientIdentifier> identifierQuery = getPatientIdentifierLuceneQuery(tmpQuery, identifierTypes, matchExactly);
		
		long identifiersSize = identifierQuery.resultSize();
		if (identifiersSize > tmpStart) {
			ListPart<Object[]> patientIdentifiers = identifierQuery.listPartProjection(tmpStart, tmpLength, "patient.personId");
			patientIdentifiers.getList().forEach(patientIdentifier -> patients.add(getPatient((Integer) patientIdentifier[0])));
			
			tmpLength -= patientIdentifiers.getList().size();
			tmpStart = 0;
		} else {
			tmpStart -= (int) identifiersSize;
		}
		
		if (tmpLength == 0) {
			return patients;
		}
		return patients;
	}
	
	public List<Patient> findPatients(String query, boolean includeVoided, Integer start, Integer length){
		Integer tmpStart = start;
		if (tmpStart == null) {
			tmpStart = 0;
		}
		Integer maxLength = HibernatePersonDAO.getMaximumSearchResults();
		Integer tmpLength = length;
		if (tmpLength == null || tmpLength > maxLength) {
			tmpLength = maxLength;
		}
		query = LuceneQuery.escapeQuery(query);

		List<Patient> patients = new LinkedList<>();

		String minChars = Context.getAdministrationService().getGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_MIN_SEARCH_CHARACTERS);

		if (minChars == null || !StringUtils.isNumeric(minChars)) {
			minChars = "" + OpenmrsConstants.GLOBAL_PROPERTY_DEFAULT_MIN_SEARCH_CHARACTERS;
		}
		if (query.length() < Integer.valueOf(minChars)) {
			return patients;
		}

		LuceneQuery<PatientIdentifier> identifierQuery = getPatientIdentifierLuceneQuery(query, includeVoided, false);

		long identifiersSize = identifierQuery.resultSize();
		if (identifiersSize > tmpStart) {
			ListPart<Object[]> patientIdentifiers = identifierQuery.listPartProjection(tmpStart, tmpLength, "patient.personId");
			patientIdentifiers.getList().forEach(patientIdentifier -> patients.add(getPatient((Integer) patientIdentifier[0])));

			tmpLength -= patientIdentifiers.getList().size();
			tmpStart = 0;
		} else {
			tmpStart -= (int) identifiersSize;
		}

		if (tmpLength == 0) {
			return patients;
		}

		PersonLuceneQuery personLuceneQuery = new PersonLuceneQuery(sessionFactory);

		LuceneQuery<PersonName> nameQuery = personLuceneQuery.getPatientNameQuery(query, includeVoided, identifierQuery);
		long namesSize = nameQuery.resultSize();
		if (namesSize > tmpStart) {
			ListPart<Object[]> personNames = nameQuery.listPartProjection(tmpStart, tmpLength, "person.personId");
			personNames.getList().forEach(personName -> patients.add(getPatient((Integer) personName[0])));

			tmpLength -= personNames.getList().size();
			tmpStart = 0;
		} else {
			tmpStart -= (int) namesSize;
		}

		if (tmpLength == 0) {
			return patients;
		}

		LuceneQuery<PersonAttribute> attributeQuery = personLuceneQuery.getPatientAttributeQuery(query, includeVoided, nameQuery);
		long attributesSize = attributeQuery.resultSize();
		if (attributesSize > tmpStart) {
			ListPart<Object[]> personAttributes = attributeQuery.listPartProjection(tmpStart, tmpLength, "person.personId");
			personAttributes.getList().forEach(personAttribute -> patients.add(getPatient((Integer) personAttribute[0])));
		}

		return patients;
	}
	private LuceneQuery<PatientIdentifier> getPatientIdentifierLuceneQuery(String query, List<PatientIdentifierType> identifierTypes, boolean matchExactly) {
		LuceneQuery<PatientIdentifier> patientIdentifierLuceneQuery = getPatientIdentifierLuceneQuery(query, matchExactly);
		for(PatientIdentifierType identifierType : identifierTypes) {
			patientIdentifierLuceneQuery.include("identifierType.patientIdentifierTypeId", identifierType.getId());
		}
		patientIdentifierLuceneQuery.include("patient.isPatient", true);
		patientIdentifierLuceneQuery.skipSame("patient.personId");
		
		return patientIdentifierLuceneQuery;
	}
	
	private LuceneQuery<PatientIdentifier> getPatientIdentifierLuceneQuery(String paramQuery, boolean matchExactly) {
		String query = removeIdentifierPadding(paramQuery);
		List<String> tokens = tokenizeIdentifierQuery(query);
		query = StringUtils.join(tokens, " OR ");
		List<String> fields = new ArrayList<>();
		fields.add("identifierPhrase");
		fields.add("identifierType");
		String matchMode = Context.getAdministrationService()
			.getGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_IDENTIFIER_SEARCH_MATCH_MODE);
		if (matchExactly) {
			fields.add("identifierExact");
		}
		else if (OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_SEARCH_MATCH_START.equals(matchMode)) {
			fields.add("identifierStart");
		} 
		else  {
			fields.add("identifierAnywhere");
		}
		return LuceneQuery.newQuery(PatientIdentifier.class, sessionFactory.getCurrentSession(), query, fields);
	
	}		
		
	private LuceneQuery<PatientIdentifier> getPatientIdentifierLuceneQuery(String query, boolean includeVoided, boolean matchExactly) {
	    LuceneQuery<PatientIdentifier> luceneQuery = getPatientIdentifierLuceneQuery(query, matchExactly);
		if(!includeVoided){
        	luceneQuery.include("voided", false);
			luceneQuery.include("patient.voided", false);
        }

        luceneQuery.include("patient.isPatient", true);
		luceneQuery.skipSame("patient.personId");

        return luceneQuery;
    }

	private String removeIdentifierPadding(String query) {
		String regex = Context.getAdministrationService().getGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_IDENTIFIER_REGEX, "");
		if (Pattern.matches("^\\^.{1}\\*.*$", regex)) {
			String padding = regex.substring(regex.indexOf("^") + 1, regex.indexOf("*"));
			Pattern pattern = Pattern.compile("^" + padding + "+");
			query = pattern.matcher(query).replaceFirst("");
		}
		return query;
	}

	/**
	 * Copied over from PatientSearchCriteria...
	 *
	 * I have no idea how it is supposed to work, but tests pass...
	 *
	 * @param query
	 * @return
	 * @see PatientSearchCriteria
	 */
	private List<String> tokenizeIdentifierQuery(String query) {
		List<String> searchPatterns = new ArrayList<>();

		String patternSearch = Context.getAdministrationService().getGlobalProperty(
				OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_IDENTIFIER_SEARCH_PATTERN, "");

		if (StringUtils.isBlank(patternSearch)) {
			searchPatterns.add(query);
		} else {
			// split the pattern before replacing in case the user searched on a comma
			// replace the @SEARCH@, etc in all elements
			for (String pattern : patternSearch.split(",")) {
				searchPatterns.add(replaceSearchString(pattern, query));
			}
		}
		return searchPatterns;
	}

	/**
	 * Copied over from PatientSearchCriteria...
	 *
	 * I have no idea how it is supposed to work, but tests pass...
	 *
	 * Puts @SEARCH@, @SEARCH-1@, and @CHECKDIGIT@ into the search string
	 *
	 * @param regex the admin-defined search string containing the @..@'s to be replaced
	 * @param identifierSearched the user entered search string
	 * @return substituted search strings.
	 *
	 * @see PatientSearchCriteria#replaceSearchString(String, String)
	 */
	private String replaceSearchString(String regex, String identifierSearched) {
		String returnString = regex.replaceAll("@SEARCH@", identifierSearched);
		if (identifierSearched.length() > 1) {
			// for 2 or more character searches, we allow regex to use last character as check digit
			returnString = returnString.replaceAll("@SEARCH-1@", identifierSearched.substring(0,
					identifierSearched.length() - 1));
			returnString = returnString.replaceAll("@CHECKDIGIT@", identifierSearched
					.substring(identifierSearched.length() - 1));
		} else {
			returnString = returnString.replaceAll("@SEARCH-1@", "");
			returnString = returnString.replaceAll("@CHECKDIGIT@", "");
		}
		return returnString;
	}

    /**
	 * @see org.openmrs..api.db.PatientDAO#getAllergies(org.openmrs.Patient)
	 */
	//@Override
        @Override
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
        @Override
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
        @Override
	public Allergy getAllergy(Integer allergyId) {
		return (Allergy) sessionFactory.getCurrentSession().createQuery("from Allergy a where a.allergyId = :allergyId")
				.setInteger("allergyId", allergyId).uniqueResult();
	}
	
	/**
	 * @see org.openmrs.PatientDAO#getAllergyByUuid(String)
	 */
        @Override
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
