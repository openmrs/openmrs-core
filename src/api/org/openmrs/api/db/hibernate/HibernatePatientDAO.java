package org.openmrs.api.db.hibernate;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientAddress;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PatientName;
import org.openmrs.Person;
import org.openmrs.Relationship;
import org.openmrs.RelationshipType;
import org.openmrs.Tribe;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.DAOException;
import org.openmrs.api.db.PatientDAO;
import org.openmrs.util.OpenmrsConstants;

public class HibernatePatientDAO implements PatientDAO {

	protected final Log log = LogFactory.getLog(getClass());
	
	private Context context;
	
	public HibernatePatientDAO() { }

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
			throw new DAOException(e);
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
				patient = (Patient)session.merge(patient);
				session.saveOrUpdate(patient);
				HibernateUtil.commitTransaction();
				
			}
			catch (Exception e) {
				HibernateUtil.rollbackTransaction();
				throw new DAOException(e);
			}
		}
	}

	@SuppressWarnings("unchecked")
	public Set<Patient> getPatientsByIdentifier(String identifier, boolean includeVoided) throws DAOException {
		Session session = HibernateUtil.currentSession();
		
		Query query;
		
		String sql = "select patient from Patient patient, PatientName name where patient.identifiers.identifier = :id and patient = name.patient";
		String order = " order by name.givenName asc, name.middleName asc, name.familyName asc";
		
		if (includeVoided) {
			query = session.createQuery(sql + order);
			query.setString("id", identifier);
		}
		else {
			query = session.createQuery(sql + " and patient.voided = :void" + order);
			query.setString("id", identifier);
			query.setBoolean("void", includeVoided);
		}
		
		List<Patient> patients = query.list();
		
		Set<Patient> returnSet = new LinkedHashSet<Patient>();
		returnSet.addAll(patients);
		
		return returnSet;
	}
	
	

	/**
	 * @see org.openmrs.api.db.PatientDAO#getPatientsByIdentifierPattern(java.lang.String, boolean)
	 */
	@SuppressWarnings("unchecked")
	public Set<Patient> getPatientsByIdentifierPattern(String identifier, boolean includeVoided) throws DAOException {
		Session session = HibernateUtil.currentSession();
		
		SQLQuery query;
		
		String regex = OpenmrsConstants.PATIENT_IDENTIFIER_REGEX;
		String order = ""; //" order by patient.name.givenName asc";
		
		regex = regex.replace("@SEARCH@", identifier);
		
		String sql = "select {pat.*} from patient {pat}, patient_identifier ident where ident.identifier regexp :regex and ident.patient_id = {pat}.patient_id";
		//String sql = "select {ident.*} from patient_identifier ident where {ident.identifier} regexp :regex";
		
		if (includeVoided) {
			query = session.createSQLQuery(sql + order);
			query.setString("regex", regex);
		}
		else {
			query = session.createSQLQuery(sql + " and {pat}.voided = :void" + order);
			query.setString("regex", regex);
			query.setBoolean("void", includeVoided);
		}
		
		//query.addEntity("ident", PatientIdentifier.class);
		query.addEntity("pat", Patient.class);
		List<Patient> patients = query.list();
		
		Set<Patient> returnSet = new LinkedHashSet<Patient>();
		returnSet.addAll(patients);
		
		return returnSet;
	}

	@SuppressWarnings("unchecked")
	public Set<Patient> getPatientsByName(String name, boolean includeVoided) throws DAOException {
		Session session = HibernateUtil.currentSession();
		
		//TODO simple name search to start testing, will need to make "real" name search
		//		i.e. split on whitespace, guess at first/last name, etc
		// TODO return the matched name instead of the primary name
		//   possible solution: "select new" org.openmrs.PatientListItem and return a list of those
		
		Set<Patient> patients = new LinkedHashSet<Patient>();
		
		name = name.replaceAll("  ", " ");
		name = name.replace(", ", " ");
		String[] names = name.split(" ");
		
		log.debug("name: " + name);
		
		Criteria criteria = session.createCriteria(Patient.class).createAlias("names", "name");
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
		
		if (includeVoided == false) {
			criteria.add(Expression.eq("voided", new Boolean(false)));
		}

		criteria.addOrder(Order.asc("name.givenName"));
		criteria.addOrder(Order.asc("name.middleName"));
		criteria.addOrder(Order.asc("name.familyName"));
		patients.addAll(criteria.list());
		
		return patients;
	}

	@SuppressWarnings("unchecked")
	public Set<Patient> getSimilarPatients(String name, Integer birthyear, String gender) throws DAOException {
		Session session = HibernateUtil.currentSession();
		
		if (birthyear == null)
			birthyear = 0;
		
		// TODO return the matched name instead of the primary name
		//   possible solution: "select new" org.openmrs.PatientListItem and return a list of those
		
		Set<Patient> patients = new LinkedHashSet<Patient>();
		
		name = name.replaceAll("  ", " ");
		name.replace(", ", " ");
		String[] names = name.split(" ");
		
		
		String q = "select p from Patient p where";
		
		if (names.length == 1) {
			q += " soundex(p.names.givenName) = soundex(:n1)";
			q += " or soundex(p.names.middleName) = soundex(:n2)";
			q += " or soundex(p.names.familyName) = soundex(:n3)";
		}
		else if (names.length == 2) {
			q += "(";
			q += " case";
			q += "  when p.names.givenName is null then 1";
			q += "  when soundex(p.names.givenName) = soundex(:n1) then 2";
			q += "  when soundex(p.names.givenName) = soundex(:n2) then 4";
			q += "  else 0 ";
			q += " end";
			q += " + ";
			q += " case";
			q += "  when p.names.middleName is null then 1";
			q += "  when soundex(p.names.middleName) = soundex(:n3) then 2";
			q += "  when soundex(p.names.middleName) = soundex(:n4) then 4";
			q += "  else 0 ";
			q += " end";
			q += " +";
			q += " case";
			q += "  when p.names.familyName is null then 1";
			q += "  when soundex(p.names.familyName) = soundex(:n5) then 2";
			q += "  when soundex(p.names.familyName) = soundex(:n6) then 4";
			q += "  else 0 ";
			q += " end";
			q += ") between 6 and 7";
		}
		else if (names.length == 3) {
			q += "(";
			q += " case";
			q += "  when p.names.givenName is null then 0";
			q += "  when soundex(p.names.givenName) = soundex(:n1) then 3";
			q += "  when soundex(p.names.givenName) = soundex(:n2) then 2";
			q += "  when soundex(p.names.givenName) = soundex(:n3) then 1";
			q += "  else 0 ";
			q += " end";
			q += " + ";
			q += " case";
			q += "  when p.names.middleName is null then 0";
			q += "  when soundex(p.names.middleName) = soundex(:n4) then 2";
			q += "  when soundex(p.names.middleName) = soundex(:n5) then 3";
			q += "  when soundex(p.names.middleName) = soundex(:n6) then 1";
			q += "  else 0";
			q += " end";
			q += " +";
			q += " case";
			q += "  when p.names.familyName is null then 0";
			q += "  when soundex(p.names.familyName) = soundex(:n7) then 1";
			q += "  when soundex(p.names.familyName) = soundex(:n8) then 2";
			q += "  when soundex(p.names.familyName) = soundex(:n9) then 3";
			q += "  else 0";
			q += " end";
			q += ") >= 5";
		}
		else
			throw new DAOException("Too many names");
		
		String birthdayMatch = "(year(p.birthdate) between " + (birthyear - 1) + " and " + (birthyear + 1) +
								" or p.birthdate is null)";
		
		String genderMatch = "p.gender = :gender";
		
		if (birthyear != 0 && gender != null) {
			q += " and (" + birthdayMatch + "and " + genderMatch + ")"; 
		}
		else if (birthyear != 0) {
			q += " and " + birthdayMatch;
		}
		else if (gender != null) {
			q += " and " + genderMatch;
		}
		
		q += " order by p.names.givenName asc, ";
		q += "p.names.middleName asc, ";
		q += "p.names.familyName asc";
		
		Query query = session.createQuery(q);
		
		int count = 1;
		for (int i = 0; i < 3; i++) {
			for (int nameIndex = 0; nameIndex < names.length; nameIndex++) {
				query.setString("n" + count, names[nameIndex]);
				count++;
			}
		}
		
		if (q.contains(":gender"))
			query.setString("gender", gender);
		
		patients.addAll(query.list());
		
		return patients;
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
			throw new DAOException(e);
		}
	}

	/**
	 * @see org.openmrs.api.db.PatientService#getPatientIdentifiers(org.openmrs.PatientIdentifierType)
	 */
	@SuppressWarnings("unchecked")
	public List<PatientIdentifier> getPatientIdentifiers(PatientIdentifierType pit) throws DAOException {
		Session session = HibernateUtil.currentSession();
		List<PatientIdentifier> patientIdentifiers = session.createQuery("from PatientIdentifier p where p.identifierType = :pit and p.voided = false")
				.setParameter("pit", pit)
				.list();
		
		return patientIdentifiers;
	}
	
	/**
	 * @see org.openmrs.api.db.PatientService#getPatientIdentifiers(java.lang.String,org.openmrs.PatientIdentifierType)
	 */
	@SuppressWarnings("unchecked")
	public List<PatientIdentifier> getPatientIdentifiers(String identifier, PatientIdentifierType pit) throws DAOException {
		Session session = HibernateUtil.currentSession();
		List<PatientIdentifier> ids;
		ids = session.createQuery("from PatientIdentifier p where p.identifierType = :pit and p.identifier = :id and p.voided = false")
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
		Session session = HibernateUtil.currentSession();
		try {
			HibernateUtil.beginTransaction();
			log.debug("type: " + pi.getIdentifierType().getName());
			session.createQuery("update PatientIdentifier p set p.identifierType = :pit where p.patient = :pat and p.identifier = :id and p.location = :loc")
				.setParameter("pit", pi.getIdentifierType())
				.setParameter("pat", pi.getPatient())
				.setParameter("id", pi.getIdentifier())
				.setParameter("loc", pi.getLocation())
				.executeUpdate();
			HibernateUtil.commitTransaction();
		}
		catch (Exception e) {
			HibernateUtil.rollbackTransaction();
			throw new DAOException(e);
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
	 * @see org.openmrs.api.db.PatientService#getPatientIdentifierType(java.lang.String)
	 */
	public PatientIdentifierType getPatientIdentifierType(String name) throws DAOException {
		Session session = HibernateUtil.currentSession();

		PatientIdentifierType ret = (PatientIdentifierType) session.createQuery("from PatientIdentifierType t where t.name = :name")
		.setString("name", name)
		.uniqueResult();

		return ret;
	}	

	/**
	 * @see org.openmrs.api.db.PatientService#getPatientIdentifierTypes()
	 */
	@SuppressWarnings("unchecked")
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
	@SuppressWarnings("unchecked")
	public List<Tribe> getTribes() throws DAOException {
		Session session = HibernateUtil.currentSession();
		
		List<Tribe> tribes = session.createQuery("from Tribe t order by t.name asc").list();
		
		return tribes;
	}
	
	/**
	 * @see org.openmrs.api.db.PatientService#findTribes()
	 */
	@SuppressWarnings("unchecked")
	public List<Tribe> findTribes(String s) throws DAOException {
		Session session = HibernateUtil.currentSession();
		
		Criteria crit = session.createCriteria(Tribe.class);
		crit.add(Expression.like("name", s, MatchMode.START));
		crit.addOrder(Order.asc("name"));
		
		return crit.list();
	}

	/**
	 * @see org.openmrs.api.db.PatientService#getRelationship()
	 */
	public Relationship getRelationship(Integer relationshipId) throws DAOException {
		Session session = HibernateUtil.currentSession();
		
		Relationship relationship = (Relationship)session.get(Relationship.class, relationshipId);
		
		return relationship;
	}
	
	/**
	 * @see org.openmrs.api.db.PatientService#getRelationships()
	 */
	@SuppressWarnings("unchecked")
	public List<Relationship> getRelationships() throws DAOException {
		Session session = HibernateUtil.currentSession();
		
		List<Relationship> relationships = session.createQuery("from Relationship r order by r.relationshipId asc").list();
		
		return relationships;
	}
	
	/**
	 * @see org.openmrs.api.db.PatientService#getRelationships(org.openmrs.Person)
	 */
	@SuppressWarnings("unchecked")
	public List<Relationship> getRelationships(Person person) throws DAOException {
		Session session = HibernateUtil.currentSession();
		
		Query query = null;
		List<Relationship> relationships = new Vector<Relationship>();
		
		if (person.getPatient() != null) {
			query = session.createQuery(
				"from Relationship r where r.person.patient = :p1 or r.relative.patient = :p2 order by r.relationshipId asc "
			)
			.setParameter("p1", person.getPatient())
			.setParameter("p2", person.getPatient());
		}
		else if (person.getUser() != null) {
			query = session.createQuery(
					"from Relationship r where r.person.user = :p1 or r.relative.user = :p2 order by r.relationshipId asc "
				)
				.setParameter("p1", person.getUser())
				.setParameter("p2", person.getUser());
		}
		
		if (query != null)
			relationships = query.list(); 
			
		return relationships;
	}

	/**
	 * @see org.openmrs.api.db.PatientService#getRelationshipType(java.lang.Integer)
	 */
	public RelationshipType getRelationshipType(Integer relationshipTypeId) throws DAOException {

		Session session = HibernateUtil.currentSession();
		
		RelationshipType relationshipType = new RelationshipType();
		relationshipType = (RelationshipType)session.get(RelationshipType.class, relationshipTypeId);
		
		return relationshipType;

	}

	/**
	 * @see org.openmrs.api.db.PatientService#getRelationshipTypes()
	 */
	@SuppressWarnings("unchecked")
	public List<RelationshipType> getRelationshipTypes() throws DAOException {

		Session session = HibernateUtil.currentSession();
		
		List<RelationshipType> relationshipTypes;
		relationshipTypes = session.createQuery("from RelationshipType r order by r.name").list();
		
		return relationshipTypes;

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
	 * @see org.openmrs.api.db.PatientService#getLocationByName(java.lang.String)
	 */
	public Location getLocationByName(String name) throws DAOException {
		Session session = HibernateUtil.currentSession();
		List result = session.createQuery("from Location l where l.name = :name").setString("name", name).list();
		if (result.size() == 0) {
			return null;
		} else {
			return (Location) result.get(0);
		}
	}

	/**
	 * @see org.openmrs.api.db.PatientService#getLocations()
	 */
	@SuppressWarnings("unchecked")
	public List<Location> getLocations() throws DAOException {

		Session session = HibernateUtil.currentSession();
		
		List<Location> locations;
		locations = session.createQuery("from Location l order by l.name").list();
		
		return locations;

	}
	
	/** @see org.openmrs.api.db.PatientService#findDuplicatePatients(java.util.Set<String>) */
	@SuppressWarnings("unchecked")
	public List<Patient> findDuplicatePatients(Set<String> attributes) {
		Session session = HibernateUtil.currentSession();
		
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
			
			Class patientName = PatientName.class;
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
			
			if (!attributes.contains("includeVoided"))
				where += "and p1.voided = false and p2.voided = false ";
			
			for (String s : attributes) {
				if (patientFieldNames.contains(s)) {
					where += " and p1." + s + " = p2." + s;
					orderBy += "p1." + s + ", ";
				}
				else if (patientNameFieldNames.contains(s)) {
					if (!select.contains("PatientName")) {
						select += ", PatientName pn1, PatientName pn2";
						where += " and p1 = pn1.patient and p2 = pn2.patient ";
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
			
			Query query = session.createQuery(select);
		
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
			
			Class patientName = PatientName.class;
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
					if (!select.contains("PatientName")) {
						select += ", PatientName pn";
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
