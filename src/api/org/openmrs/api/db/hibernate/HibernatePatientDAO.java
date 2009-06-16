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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.LogicalExpression;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.SimpleExpression;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.Person;
import org.openmrs.PersonName;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.DAOException;
import org.openmrs.api.db.PatientDAO;
import org.openmrs.util.OpenmrsConstants;

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
			
			// TODO: A merge is necessary here because hibernate thinks that Patients and 
			// 		Persons are the same objects.  So it sees a Person object in the 
			//      cache and claims it is a duplicate of this Patient object.
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
		Connection connection = sessionFactory.getCurrentSession().connection();
		
		boolean stubInsertNeeded = false;
		
		PreparedStatement ps = null;
		
		if (patient.getPatientId() != null) {
			// check if there is a row with a matching patient.patient_id 
			try {
				ps = connection.prepareStatement("SELECT * FROM patient WHERE patient_id = ?");
				ps.setInt(1, patient.getPatientId());
				ps.execute();
				
				if (ps.getResultSet().next())
					stubInsertNeeded = false;
				else
					stubInsertNeeded = true;
				
			}
			catch (SQLException e) {
				log.error("Error while trying to see if this person is a patient already", e);
			}
			if (ps != null) {
				try {
					ps.close();
				}
				catch (SQLException e) {
					log.error("Error generated while closing statement", e);
				}
			}
		}
		
		if (stubInsertNeeded) {
			try {
				ps = connection
				        .prepareStatement("INSERT INTO patient (patient_id, creator, voided, date_created) VALUES (?, ?, 0, ?)");
				
				ps.setInt(1, patient.getPatientId());
				ps.setInt(2, patient.getCreator().getUserId());
				ps.setDate(3, new java.sql.Date(patient.getDateCreated().getTime()));
				
				ps.executeUpdate();
			}
			catch (SQLException e) {
				log.warn("SQL Exception while trying to create a patient stub", e);
			}
			finally {
				if (ps != null) {
					try {
						ps.close();
					}
					catch (SQLException e) {
						log.error("Error generated while closing statement", e);
					}
				}
			}
		}
		
		// commenting this out to get the save patient as a user option to work correctly
		//sessionFactory.getCurrentSession().flush();
	}
	
	/**
	 * @see org.openmrs.api.db.PatientDAO#getPatients(java.lang.String, java.lang.String,
	 *      java.util.List, boolean)
	 */
	@SuppressWarnings("unchecked")
	public List<Patient> getPatients(String name, String identifier, List<PatientIdentifierType> identifierTypes,
	                                 boolean matchIdentifierExactly) throws DAOException {
		
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Patient.class);
		
		criteria.createAlias("names", "name");
		criteria.addOrder(Order.asc("name.givenName"));
		criteria.addOrder(Order.asc("name.middleName"));
		criteria.addOrder(Order.asc("name.familyName"));
		
		// get only distinct patients 
		criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
		
		if (name != null) {
			// TODO simple name search to start testing, will need to make "real"
			// name search
			// i.e. split on whitespace, guess at first/last name, etc
			// TODO return the matched name instead of the primary name
			// possible solution: "select new" org.openmrs.PatientListItem and
			// return a list of those
			
			name = name.replaceAll("  ", " ");
			name = name.replace(", ", " ");
			String[] names = name.split(" ");
			
			// TODO add junit test for searching on voided patient names
			
			String nameSoFar = names[0];
			for (int i = 0; i < names.length; i++) {
				String n = names[i];
				if (n != null && n.length() > 0) {
					LogicalExpression oneNameSearch = getNameSearch(n);
					LogicalExpression searchExpression = oneNameSearch;
					if (i > 0) {
						nameSoFar += " " + n;
						LogicalExpression fullNameSearch = getNameSearch(nameSoFar);
						searchExpression = Expression.or(oneNameSearch, fullNameSearch);
					}
					criteria.add(searchExpression);
				}
			}
			
		}
		
		// do the restriction on either identifier string or types
		if (identifier != null || identifierTypes.size() > 0) {
			
			// TODO add junit test for searching on voided identifiers
			
			// add the join on the identifiers table
			criteria.createAlias("identifiers", "ids");
			criteria.add(Expression.eq("ids.voided", false));
			
			// do the identifier restriction
			if (identifier != null) {
				AdministrationService adminService = Context.getAdministrationService();
				String regex = adminService.getGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_IDENTIFIER_REGEX, "");
				
				// if the user wants an exact search, match on that.
				if (matchIdentifierExactly) {
					criteria.add(Expression.eq("ids.identifier", identifier));
				} else {
					// remove padding from identifier search string
					if (Pattern.matches("^\\^.{1}\\*.*$", regex)) {
						String padding = regex.substring(regex.indexOf("^") + 1, regex.indexOf("*"));
						Pattern pattern = Pattern.compile("^" + padding + "+");
						identifier = pattern.matcher(identifier).replaceFirst("");
					}
					// if the regex is empty, default to a simple "like" search or if 
					// we're in hsql world, also only do the simple like search (because
					// hsql doesn't know how to deal with 'regexp'
					if (regex.equals("") || HibernateUtil.isHSQLDialect(sessionFactory)) {
						String prefix = adminService.getGlobalProperty(
						    OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_IDENTIFIER_PREFIX, "");
						String suffix = adminService.getGlobalProperty(
						    OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_IDENTIFIER_SUFFIX, "%");
						StringBuffer likeString = new StringBuffer(prefix).append(identifier).append(suffix);
						criteria.add(Expression.like("ids.identifier", likeString.toString()));
					}
					// if the regex is present, search on that
					else {
						regex = regex.replace("@SEARCH@", identifier);
						criteria.add(Restrictions.sqlRestriction("identifier regexp ?", regex, Hibernate.STRING));
					}
				}
			}
			
			// TODO add a junit test for patientIdentifierType restrictions
			
			// do the type restriction
			if (identifierTypes.size() > 0) {
				criteria.add(Expression.in("ids.identifierType", identifierTypes));
			}
		}
		
		// TODO add junit test for searching on voided patients
		
		// make sure the patient object isn't voided
		criteria.add(Expression.eq("voided", false));
		
		// restricting the search to the max search results value
		criteria.setFirstResult(0);
		criteria.setMaxResults(getMaximumSearchResults());
		
		return criteria.list();
	}
	
	/**
	 * Returns a criteria object comparing the given string to each part of the name. <br/>
	 * <br/>
	 * This criteria is essentially:
	 * 
	 * <pre>
	 * ... where voided = false &amp;&amp; name in (familyName2, familyName, middleName, givenName)
	 * </pre>
	 * 
	 * @param name
	 * @return
	 */
	private LogicalExpression getNameSearch(String name) {
		
		MatchMode mode = MatchMode.START;
		String matchModeConstant = OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_SEARCH_MATCH_MODE;
		String modeGp = Context.getAdministrationService().getGlobalProperty(matchModeConstant);
		if (OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_SEARCH_MATCH_ANYWHERE.equalsIgnoreCase(modeGp)) {
			mode = MatchMode.ANYWHERE;
		}
		SimpleExpression givenName = Expression.like("name.givenName", name, mode);
		SimpleExpression middleName = Expression.like("name.middleName", name, mode);
		SimpleExpression familyName = Expression.like("name.familyName", name, mode);
		SimpleExpression familyName2 = Expression.like("name.familyName2", name, mode);
		
		return Expression.and(Expression.eq("name.voided", false), Expression.or(familyName2, Expression.or(familyName,
		    Expression.or(middleName, givenName))));
	}
	
	/**
	 * @see org.openmrs.api.db.PatientDAO#getAllPatients(boolean)
	 */
	@SuppressWarnings("unchecked")
	public List<Patient> getAllPatients(boolean includeVoided) throws DAOException {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Patient.class);
		
		if (includeVoided == false)
			criteria.add(Expression.eq("voided", false));
		
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
	 * @see org.openmrs.api.db.PatientDAO#getPatientIdentifiers(java.lang.String, java.util.List,
	 *      java.util.List, java.util.List, java.lang.Boolean)
	 * @see org.openmrs.api.PatientService#getPatientIdentifiers(java.lang.String, java.util.List,
	 *      java.util.List, java.util.List, java.lang.Boolean)
	 */
	@SuppressWarnings("unchecked")
	public List<PatientIdentifier> getPatientIdentifiers(String identifier,
	                                                     List<PatientIdentifierType> patientIdentifierTypes,
	                                                     List<Location> locations, List<Patient> patients,
	                                                     Boolean isPreferred) throws DAOException {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(PatientIdentifier.class);
		
		// TODO add junit test for not getting voided
		// make sure the patient object isn't voided
		criteria.add(Expression.eq("voided", false));
		
		// TODO add junit test for getting by identifier (and for not getting by partial here)
		if (identifier != null)
			criteria.add(Expression.eq("identifier", identifier));
		
		// TODO add junit test for getting by identifier type
		if (patientIdentifierTypes.size() > 0)
			criteria.add(Expression.in("identifierType", patientIdentifierTypes));
		
		// TODO add junit test for getting by patients
		if (patients.size() > 0)
			criteria.add(Expression.in("patient", patients));
		
		// TODO add junit test for getting by null/true/false isPreferred
		if (isPreferred != null)
			criteria.add(Expression.eq("preferred", isPreferred));
		
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
	 * @see org.openmrs.api.db.PatientDAO#getAllPatientIdentifierTypes(boolean)
	 */
	@SuppressWarnings("unchecked")
	public List<PatientIdentifierType> getAllPatientIdentifierTypes(boolean includeRetired) throws DAOException {
		
		// TODO test this method
		
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(PatientIdentifierType.class);
		criteria.addOrder(Order.asc("name"));
		
		if (includeRetired == false)
			criteria.add(Expression.eq("retired", false));
		
		return criteria.list();
	}
	
	/**
	 * @see org.openmrs.api.db.PatientDAO#getPatientIdentifierTypes(java.lang.String,
	 *      java.lang.String, java.lang.Boolean, java.lang.Boolean)
	 */
	@SuppressWarnings("unchecked")
	public List<PatientIdentifierType> getPatientIdentifierTypes(String name, String format, Boolean required,
	                                                             Boolean hasCheckDigit) throws DAOException {
		// TODO test this method
		
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(PatientIdentifierType.class);
		criteria.addOrder(Order.asc("name"));
		
		if (name != null)
			criteria.add(Expression.eq("name", name));
		
		if (format != null)
			criteria.add(Expression.eq("format", format));
		
		if (required != null)
			criteria.add(Expression.eq("required", required));
		
		if (hasCheckDigit != null)
			criteria.add(Expression.eq("checkDigit", hasCheckDigit));
		
		criteria.add(Expression.eq("retired", false));
		
		return criteria.list();
	}
	
	/**
	 * @see org.openmrs.api.db.PatientDAO#getDuplicatePatientsByAttributes(java.util.List)
	 */
	@SuppressWarnings("unchecked")
	public List<Patient> getDuplicatePatientsByAttributes(List<String> attributes) {
		List<Patient> patients = new Vector<Patient>();
		
		if (attributes.size() > 0) {
			String select = "select distinct p1 from Patient p1, Patient p2";
			String where = " where p1 <> p2 ";
			String orderBy = " order by ";
			
			Class patient = Patient.class;
			Set<String> patientFieldNames = new HashSet<String>(patient.getDeclaredFields().length);
			for (Field f : patient.getDeclaredFields()) {
				patientFieldNames.add(f.getName());
				log.debug(f.getName());
			}
			
			Class person = Person.class;
			Set<String> personFieldNames = new HashSet<String>(person.getDeclaredFields().length);
			for (Field f : person.getDeclaredFields()) {
				personFieldNames.add(f.getName());
				log.debug(f.getName());
			}
			
			Class personName = PersonName.class;
			Set<String> personNameFieldNames = new HashSet<String>(personName.getDeclaredFields().length);
			for (Field f : personName.getDeclaredFields()) {
				personNameFieldNames.add(f.getName());
				log.debug(f.getName());
			}
			
			Class identifier = PatientIdentifier.class;
			Set<String> identifierFieldNames = new HashSet<String>(identifier.getDeclaredFields().length);
			for (Field f : identifier.getDeclaredFields()) {
				identifierFieldNames.add(f.getName());
				log.debug(f.getName());
			}
			
			if (!attributes.contains("includeVoided"))
				where += "and p1.voided = false and p2.voided = false ";
			
			for (String s : attributes) {
				if (patientFieldNames.contains(s)) {
					where += " and p1." + s + " = p2." + s;
					orderBy += "p1." + s + ", ";
				} else if (personFieldNames.contains(s)) {
					if (!select.contains("Person ")) {
						select += ", Person person1, Person person2";
						where += " and p1.patientId = person1.personId and p2.patientId = person2.personId ";
					}
					where += " and person1." + s + " = person2." + s;
					orderBy += "person1." + s + ", ";
				} else if (personNameFieldNames.contains(s)) {
					if (!select.contains("PersonName")) {
						select += ", PersonName pn1, PersonName pn2";
						where += " and p1 = pn1.person and p2 = pn2.person ";
					}
					where += " and pn1." + s + " = pn2." + s;
					orderBy += "pn1." + s + ", ";
				} else if (identifierFieldNames.contains(s)) {
					if (!select.contains("PatientIdentifier")) {
						select += ", PatientIdentifier pi1, PatientIdentifier pi2";
						where += " and p1 = pi1.patient and p2 = pi2.patient ";
					}
					where += " and pi1." + s + " = pi2." + s;
					orderBy += "pi1." + s + ", ";
				} else
					log.warn("Unidentified attribute: " + s);
			}
			
			int index = orderBy.lastIndexOf(", ");
			orderBy = orderBy.substring(0, index);
			
			select = select + where + orderBy;
			
			Query query = sessionFactory.getCurrentSession().createQuery(select);
			
			patients = query.list();
		}
		
		/*
		 * if (attributes.size() > 0) { String select = "select p from Patient
		 * p"; String where = " where 1=1 "; String groupBy= " group by ";
		 * String having = " having count(p.patientId) > 1";
		 * 
		 * Class patient = Patient.class; Set<String> patientFieldNames = new
		 * HashSet<String>(patient.getDeclaredFields().length); for (Field f :
		 * patient.getDeclaredFields()){ patientFieldNames.add(f.getName());
		 * log.debug(f.getName()); }
		 * 
		 * Class patientName = PersonName.class; Set<String>
		 * patientNameFieldNames = new HashSet<String>(patientName.getDeclaredFields().length);
		 * for (Field f : patientName.getDeclaredFields()){
		 * patientNameFieldNames.add(f.getName()); log.debug(f.getName()); }
		 * 
		 * Class identifier = PatientIdentifier.class; Set<String>
		 * identifierFieldNames = new HashSet<String>(identifier.getDeclaredFields().length);
		 * for (Field f : identifier.getDeclaredFields()){
		 * identifierFieldNames.add(f.getName()); log.debug(f.getName()); }
		 * 
		 * for (String s : attributes) { if (patientFieldNames.contains(s)) {
		 * groupBy += "p." + s + ", "; } else if
		 * (patientNameFieldNames.contains(s)) { if
		 * (!select.contains("PersonName")) { select += ", PersonName pn"; where +=
		 * "and p = pn.patient "; } groupBy += "pn." + s + ", "; } else if
		 * (identifierFieldNames.contains(s)) { if
		 * (!select.contains("PatientIdentifier")) { select += ",
		 * PatientIdentifier pi"; where += "and p = pi.patient "; } groupBy +=
		 * "pi." + s + ", "; } else log.warn("Unidentified attribute: " + s); }
		 * 
		 * int index = groupBy.lastIndexOf(", "); groupBy = groupBy.substring(0,
		 * index);
		 * 
		 * select = select + where + groupBy + having;
		 * 
		 * Query query = session.createQuery(select);
		 * 
		 * patients = query.list(); }
		*/

		return patients;
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
	 * Fetch the max results value from the global properties table
	 * 
	 * @return Integer value for the patient search max results global property
	 */
	private Integer getMaximumSearchResults() {
		try {
			return Integer.valueOf(Context.getAdministrationService().getGlobalProperty(
			    OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_SEARCH_MAX_RESULTS, "1000"));
		}
		catch (Exception e) {
			log.warn("Unable to convert the global property " + OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_SEARCH_MAX_RESULTS
			        + "to a valid integer. Returning the default 1000");
		}
		
		return 1000;
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
		
		// switched this to an hql query so the hibernate cache can be considered as well as the database
		String hql = "select count(*) from PatientIdentifier pi, Patient p where pi.patient.patientId = p.patient.patientId " + 
			"and p.voided = false and pi.voided = false and pi.identifier = :identifier and pi.identifierType = :idType";
		
		if (checkPatient) {
			hql += " and p.patientId != :ptId";
		}
		
		Query query = sessionFactory.getCurrentSession().createQuery(hql);
		query.setString("identifier", patientIdentifier.getIdentifier());
		query.setInteger("idType", patientIdentifier.getIdentifierType().getPatientIdentifierTypeId());
		if (checkPatient) {
			query.setInteger("ptId", patientIdentifier.getPatient().getPatientId());
		}
		return !query.uniqueResult().toString().equals("0");
	}
}
