/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.api.db.hibernate;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.ObjectNotFoundException;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.Person;
import org.openmrs.PersonName;
import org.openmrs.Tribe;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.DAOException;
import org.openmrs.api.db.PatientDAO;
import org.openmrs.util.OpenmrsConstants;

/**
 * Patient related database hibernate specific methods
 */
public class HibernatePatientDAO implements PatientDAO {

	protected final Log log = LogFactory.getLog(getClass());

	/**
	 * Hibernate session factory
	 */
	private SessionFactory sessionFactory;
	
	public HibernatePatientDAO() { }
	
	/**
	 * Set session factory
	 * 
	 * @param sessionFactory
	 */
	public void setSessionFactory(SessionFactory sessionFactory) { 
		this.sessionFactory = sessionFactory;
	}
	
	/**
	 * @see org.openmrs.api.db.PatientService#getPatient(java.lang.Long)
	 */
	public Patient getPatient(Integer patientId) {
		try {
			return (Patient) sessionFactory.getCurrentSession().get(Patient.class, patientId);
		} catch (ObjectNotFoundException ex) {
			return null;
		}
	}
	

	/**
	 * @see org.openmrs.api.db.PatientDAO#createPatient(org.openmrs.Patient)
	 */
	public Patient createPatient(Patient patient) throws DAOException {
		sessionFactory.getCurrentSession().saveOrUpdate(patient);
		//sessionFactory.getCurrentSession().refresh(patient);
		
		return patient;
	}


	/**
	 * @see org.openmrs.api.db.PatientDAO#updatePatient(org.openmrs.Patient)
	 */
	public Patient updatePatient(Patient patient) throws DAOException {
		if (patient.getPatientId() == null)
			// TODO this check/call should be moved up from the DB layer to the API layer
			return createPatient(patient);
		else {
			
			// Check to make sure we have a row in the patient table already.
			// If we don't have a row, create it so Hibernate doesn't bung things up
			Object obj = sessionFactory.getCurrentSession().get(Patient.class, patient.getPatientId());
			if (!(obj instanceof Patient)) {
				insertPatientStub(patient);
			}
			
			patient = (Patient)sessionFactory.getCurrentSession().merge(patient);
			//sessionFactory.getCurrentSession().update("org.openmrs.Person", (Object)patient);
			return patient;
		}
	}

	/**
	 * Inserts a row into the patient table
	 * 
	 * This avoids hibernate's bunging of our person/patient/user inheritance
	 * 
	 * @param patient
	 */
	private void insertPatientStub(Patient patient) {
		Connection connection = sessionFactory.getCurrentSession().connection();
		try {
			PreparedStatement ps = connection.prepareStatement("INSERT INTO patient (patient_id, creator, date_created) VALUES (?, ?, ?)");
			
			ps.setInt(1, patient.getPatientId());
			ps.setInt(2, patient.getCreator().getUserId());
			ps.setDate(3, new java.sql.Date(patient.getDateCreated().getTime()));
	
			ps.executeUpdate();
		}
		catch (SQLException e) {
			log.warn("SQL Exception while trying to create a patient stub", e);
		}
		
		sessionFactory.getCurrentSession().flush();
	}

	@SuppressWarnings("unchecked")
	public Set<Patient> getPatientsByIdentifier(String identifier, boolean includeVoided) throws DAOException {
		Query query;
		
		String sql = "select patient from Patient patient, PersonName name join patient.identifiers ids where ids.identifier = :id and patient.patientId = name.person.personId";
		String order = " order by name.givenName asc, name.middleName asc, name.familyName asc";
		
		if (includeVoided) {
			query = sessionFactory.getCurrentSession().createQuery(sql + order);
			query.setString("id", identifier);
		}
		else {
			query = sessionFactory.getCurrentSession().createQuery(sql + " and patient.voided = :void" + order);
			query.setString("id", identifier);
			query.setBoolean("void", includeVoided);
		}
		
		Set<Patient> returnSet = new LinkedHashSet<Patient>();
		returnSet.addAll(query.list());
		
		return returnSet;
	}
	
	

	/**
	 * @see org.openmrs.api.db.PatientDAO#getPatientsByIdentifierPattern(java.lang.String, boolean)
	 */
	@SuppressWarnings("unchecked")
	public Collection<Patient> getPatientsByIdentifierPattern(String identifier, boolean includeVoided) throws DAOException {
		
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(PatientIdentifier.class);
		criteria.setProjection(Projections.property("patient"));
		
		AdministrationService adminService = Context.getAdministrationService();
		String regex = adminService.getGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_IDENTIFIER_REGEX, "");
		
		// if the regex is empty, default to a simple "like" search
		if (regex.equals("")) {
			String prefix = adminService.getGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_IDENTIFIER_PREFIX, "");
			String suffix = adminService.getGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_IDENTIFIER_SUFFIX, "");
			StringBuffer likeString = new StringBuffer(prefix).append(identifier).append(suffix);
			criteria.add(Expression.like("identifier", likeString.toString()));
		}
		// if the regex is present, search on that
		else {
			regex = regex.replace("@SEARCH@", identifier);
			criteria.add(Restrictions.sqlRestriction("identifier regexp '" + regex + "'"));
		}
		
		if (includeVoided == false) {
			criteria.createAlias("patient", "pat");
			criteria.add(Restrictions.eq("pat.voided", false));
		}
		
		criteria.setFirstResult(0);
		criteria.setMaxResults(getMaximumSearchResults());
		
		return criteria.list();
	}

	@SuppressWarnings("unchecked")
	public Collection<Patient> getPatientsByName(String name, boolean includeVoided) throws DAOException {
		//TODO simple name search to start testing, will need to make "real" name search
		//		i.e. split on whitespace, guess at first/last name, etc
		// TODO return the matched name instead of the primary name
		//   possible solution: "select new" org.openmrs.PatientListItem and return a list of those
		
		name = name.replaceAll("  ", " ");
		name = name.replace(", ", " ");
		String[] names = name.split(" ");
		
		if (log.isDebugEnabled())
			log.debug("name: " + name);
		
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Patient.class).createAlias("names", "name");
		for (String n : names) {
			if (n != null && n.length() > 0) {
				criteria.add(Expression.or(
					Expression.like("name.familyName", n, MatchMode.START),
					Expression.or(
						Expression.like("name.middleName", n, MatchMode.START),
						Expression.like("name.givenName", n, MatchMode.START)
						)
					)
				);
			}
		}
		
		if (includeVoided == false)
			criteria.add(Expression.eq("voided", new Boolean(false)));

		criteria.addOrder(Order.asc("name.givenName"));
		criteria.addOrder(Order.asc("name.middleName"));
		criteria.addOrder(Order.asc("name.familyName"));
		
		criteria.setFirstResult(0);
		criteria.setMaxResults(getMaximumSearchResults());
		
		return criteria.list();
	}

	/**
	 * @see org.openmrs.api.db.PatientService#deletePatient(org.openmrs.Patient)
	 */
	public void deletePatient(Patient patient) throws DAOException {
		HibernatePersonDAO.deletePersonAndAttributes(sessionFactory, patient);
	}

	/**
	 * @see org.openmrs.api.db.PatientService#getPatientIdentifiers(org.openmrs.PatientIdentifierType)
	 */
	@SuppressWarnings("unchecked")
	public List<PatientIdentifier> getPatientIdentifiers(PatientIdentifierType pit) throws DAOException {
		List<PatientIdentifier> patientIdentifiers = sessionFactory.getCurrentSession().createQuery("from PatientIdentifier p where p.identifierType = :pit and p.voided = false")
				.setParameter("pit", pit)
				.list();
		
		return patientIdentifiers;
	}
	
	/**
	 * @see org.openmrs.api.db.PatientService#getPatientIdentifiers(java.lang.String,org.openmrs.PatientIdentifierType)
	 */
	@SuppressWarnings("unchecked")
	public List<PatientIdentifier> getPatientIdentifiers(String identifier, PatientIdentifierType pit) throws DAOException {
		List<PatientIdentifier> ids;
		ids = sessionFactory.getCurrentSession().createQuery("from PatientIdentifier p where p.identifierType = :pit and p.identifier = :id and p.voided = false")
				.setParameter("pit", pit)
				.setString("id", identifier)
				.list();
		
		return ids;
	}
	
	/**
	 * Only updates the identifier type at the moment
	 * 
	 * 
	 * @see org.openmrs.api.db.PatientService#updatePatientIdentifier(org.openmrs.PatientIdentifier)
	 */
	public void updatePatientIdentifier(PatientIdentifier pi) throws DAOException {
		log.debug("type: " + pi.getIdentifierType().getName());
		sessionFactory.getCurrentSession().createQuery("update PatientIdentifier p set p.identifierType = :pit where p.patient = :pat and p.identifier = :id and p.location = :loc")
			.setParameter("pit", pi.getIdentifierType())
			.setParameter("pat", pi.getPatient())
			.setParameter("id", pi.getIdentifier())
			.setParameter("loc", pi.getLocation())
			.executeUpdate();
	}
	
	/**
	 * @see org.openmrs.api.db.PatientService#getPatientIdentifierType(java.lang.Integer)
	 */
	public PatientIdentifierType getPatientIdentifierType(Integer patientIdentifierTypeId) throws DAOException {
		PatientIdentifierType patientIdentifierType = (PatientIdentifierType) sessionFactory.getCurrentSession().get(PatientIdentifierType.class, patientIdentifierTypeId);
		
		return patientIdentifierType;
	}
	
	/**
	 * @see org.openmrs.api.db.PatientService#getPatientIdentifierType(java.lang.String)
	 */
	public PatientIdentifierType getPatientIdentifierType(String name) throws DAOException {
		PatientIdentifierType ret = (PatientIdentifierType) sessionFactory.getCurrentSession().createQuery("from PatientIdentifierType t where t.name = :name")
			.setString("name", name)
			.uniqueResult();

		return ret;
	}	

	/**
	 * @see org.openmrs.api.db.PatientService#getPatientIdentifierTypes()
	 */
	@SuppressWarnings("unchecked")
	public List<PatientIdentifierType> getPatientIdentifierTypes() throws DAOException {
		List<PatientIdentifierType> patientIdentifierTypes = sessionFactory.getCurrentSession().createQuery("from PatientIdentifierType p order by p.name").list();
		
		return patientIdentifierTypes;
	}

	/**
	 * @see org.openmrs.api.db.PatientService#getTribe()
	 */
	public Tribe getTribe(Integer tribeId) throws DAOException {
		Tribe tribe = (Tribe)sessionFactory.getCurrentSession().get(Tribe.class, tribeId);
		
		return tribe;
	}
	
	/**
	 * @see org.openmrs.api.db.PatientService#getTribes()
	 */
	@SuppressWarnings("unchecked")
	public List<Tribe> getTribes() throws DAOException {
		List<Tribe> tribes = sessionFactory.getCurrentSession().createQuery("from Tribe t order by t.name asc").list();
		
		return tribes;
	}
	
	/**
	 * @see org.openmrs.api.db.PatientService#findTribes()
	 */
	@SuppressWarnings("unchecked")
	public List<Tribe> findTribes(String s) throws DAOException {
		Criteria crit = sessionFactory.getCurrentSession().createCriteria(Tribe.class);
		crit.add(Expression.like("name", s, MatchMode.START));
		crit.addOrder(Order.asc("name"));
		
		return crit.list();
	}
	
	/** @see org.openmrs.api.db.PatientService#findDuplicatePatients(java.util.Set<String>) */
	@SuppressWarnings("unchecked")
	public List<Patient> findDuplicatePatients(Set<String> attributes) {
		List<Patient> patients = new Vector<Patient>();
		
		if (attributes.size() > 0) {
			String select = "select distinct p1 from Patient p1, Patient p2";
			String where  = " where p1 <> p2 ";
			String orderBy= " order by ";
			
			Class patient = Patient.class;
			Set<String> patientFieldNames = new HashSet<String>(patient.getDeclaredFields().length);
			for (Field f : patient.getDeclaredFields()){
				patientFieldNames.add(f.getName());
				log.debug(f.getName());
			}
			
			Class person = Person.class;
			Set<String> personFieldNames = new HashSet<String>(person.getDeclaredFields().length);
			for (Field f : person.getDeclaredFields()){
				personFieldNames.add(f.getName());
				log.debug(f.getName());
			}
			
			Class personName = PersonName.class;
			Set<String> personNameFieldNames = new HashSet<String>(personName.getDeclaredFields().length);
			for (Field f : personName.getDeclaredFields()){
				personNameFieldNames.add(f.getName());
				log.debug(f.getName());
			}
			
			Class identifier = PatientIdentifier.class;
			Set<String> identifierFieldNames = new HashSet<String>(identifier.getDeclaredFields().length);
			for (Field f : identifier.getDeclaredFields()){
				identifierFieldNames.add(f.getName());
				log.debug(f.getName());
			}
			
			if (!attributes.contains("includeVoided"))
				where += "and p1.voided = false and p2.voided = false ";
			
			for (String s : attributes) {
				if (patientFieldNames.contains(s)) {
					where += " and p1." + s + " = p2." + s;
					orderBy += "p1." + s + ", ";
				}
				else if (personFieldNames.contains(s)) {
					if (!select.contains("Person ")) {
						select += ", Person person1, Person person2";
						where += " and p1.patientId = person1.personId and p2.patientId = person2.personId ";
					}
					where += " and person1." + s + " = person2." + s;
					orderBy += "person1." + s + ", ";
				}
				else if (personNameFieldNames.contains(s)) {
					if (!select.contains("PersonName")) {
						select += ", PersonName pn1, PersonName pn2";
						where += " and p1 = pn1.person and p2 = pn2.person ";
					}
					where += " and pn1." + s + " = pn2." + s;
					orderBy += "pn1." + s + ", ";
				}
				else if (identifierFieldNames.contains(s)) {
					if (!select.contains("PatientIdentifier")) {
						select += ", PatientIdentifier pi1, PatientIdentifier pi2";
						where += " and p1 = pi1.patient and p2 = pi2.patient ";
					}
					where += " and pi1." + s + " = pi2." + s;
					orderBy += "pi1." + s + ", ";
				}
				else
					log.warn("Unidentified attribute: " + s);
			}

			int index = orderBy.lastIndexOf(", ");
			orderBy = orderBy.substring(0, index);
			
			select = select + where + orderBy;
			
			Query query = sessionFactory.getCurrentSession().createQuery(select);
		
			patients = query.list();
		}
		
		/*
		if (attributes.size() > 0) {
			String select = "select p from Patient p";
			String where  = " where 1=1 ";
			String groupBy= " group by ";
			String having = " having count(p.patientId) > 1";
			
			Class patient = Patient.class;
			Set<String> patientFieldNames = new HashSet<String>(patient.getDeclaredFields().length);
			for (Field f : patient.getDeclaredFields()){
				patientFieldNames.add(f.getName());
				log.debug(f.getName());
			}
			
			Class patientName = PersonName.class;
			Set<String> patientNameFieldNames = new HashSet<String>(patientName.getDeclaredFields().length);
			for (Field f : patientName.getDeclaredFields()){
				patientNameFieldNames.add(f.getName());
				log.debug(f.getName());
			}
			
			Class identifier = PatientIdentifier.class;
			Set<String> identifierFieldNames = new HashSet<String>(identifier.getDeclaredFields().length);
			for (Field f : identifier.getDeclaredFields()){
				identifierFieldNames.add(f.getName());
				log.debug(f.getName());
			}
			
			for (String s : attributes) {
				if (patientFieldNames.contains(s)) {
					groupBy += "p." + s + ", ";
				}
				else if (patientNameFieldNames.contains(s)) {
					if (!select.contains("PersonName")) {
						select += ", PersonName pn";
						where += "and p = pn.patient ";
					}
					groupBy += "pn." + s + ", ";
				}
				else if (identifierFieldNames.contains(s)) {
					if (!select.contains("PatientIdentifier")) {
						select += ", PatientIdentifier pi";
						where += "and p = pi.patient ";
					}
					groupBy += "pi." + s + ", ";
				}
				else
					log.warn("Unidentified attribute: " + s);
			}
			
			int index = groupBy.lastIndexOf(", ");
			groupBy = groupBy.substring(0, index);
			
			select = select + where + groupBy + having;
			
			Query query = session.createQuery(select);
		
			patients = query.list();
		}
		*/
		
		return patients;
	}
	
	/**
	 * Fetch the max results value from the global properties table
	 * 
	 * @return Integer value for the patient search max results global property
	 */
	private Integer getMaximumSearchResults() {
		try {
			return Integer.valueOf(
			        Context.getAdministrationService().getGlobalProperty(
						OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_SEARCH_MAX_RESULTS,
						"1000")
					);
		}
		catch (Exception e) {
			log.warn("Unable to convert the global property " + OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_SEARCH_MAX_RESULTS +
			         "to a valid integer. Returning the default 1000");
		}
		
		return 1000;
	}
}
