package org.openmrs.api.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.PersonAddress;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonAttributeType;
import org.openmrs.PersonName;
import org.openmrs.Relationship;
import org.openmrs.RelationshipType;
import org.openmrs.User;
import org.openmrs.api.APIAuthenticationException;
import org.openmrs.api.APIException;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.PersonService;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.PersonDAO;
import org.openmrs.util.OpenmrsConstants;

/**
 * Person-related services
 * @vesrion 1.0
 */
public class PersonServiceImpl implements PersonService {

	private Log log = LogFactory.getLog(this.getClass());
	
	private PersonDAO dao;
	
	public PersonServiceImpl() {	}
	
	private PersonDAO getPersonDAO() {
		return dao;
	}
	
	/**
	 * @see org.openmrs.api.PersonService#getSimilarPeople(java.lang.String, java.lang.Integer, java.lang.String)
	 */
	public Set<Person> getSimilarPeople(String name, Integer birthyear, String gender) throws APIException {
		return getPersonDAO().getSimilarPeople(name, birthyear, gender);
	}

	/**
	 * @see org.openmrs.api.PersonService#findPeople(java.lang.String,boolean)
	 */
	public Set<Person> findPeople(String searchPhrase, boolean includeVoided) {
		log.debug("starting method");
		return this.findPeople(searchPhrase, includeVoided, (List)null);
	}

	public Set<Person> findPeople(String searchPhrase, boolean includeVoided, String roles) {
		log.debug("starting method, roles is " + roles);
		List<String> roleList = null;
		
		if ( roles != null ) if ( roles.length() > 0 ) {
			String[] splitRoles = roles.split(",");
			for ( String role : splitRoles ) {
				if ( roleList == null ) roleList = new ArrayList<String>();
				roleList.add(role);
			}
		}
		
		return this.findPeople(searchPhrase, includeVoided, roleList);
	}

	/**
	 * @see org.openmrs.api.PersonService#findPeople(java.lang.String, boolean, java.util.List)
	 */
	public Set<Person> findPeople(String searchPhrase, boolean includeVoided, List<String> roles) {
		log.debug("starting method, roles are " + roles);
		Set<Person> people = new HashSet<Person>();
		
		// If no rules *are not* defined then find all matching persons (users and patients).
		if ( roles == null ) {
			people.addAll(getPersonDAO().findPeople(searchPhrase, includeVoided));
		}
		// If roles *are* defined then find matching users who have the given roles.
		else {
			people.addAll(Context.getUserService().findUsers(searchPhrase, roles, includeVoided));
		}
			
		return people;
	}

	public void setPersonDAO(PersonDAO dao) {
		this.dao = dao;
	}

	/**
	 * @see org.openmrs.api.PersonService#createPersonAttributeType(org.openmrs.PersonAttributeType)
	 */
	public void createPersonAttributeType(PersonAttributeType type) {
		log.info("Creating person attribute type: " + type);
		User user = Context.getAuthenticatedUser();
		if (type.getCreator() == null) {
			type.setCreator(user);
			type.setDateCreated(new Date());
		}
		else {
			type.setChangedBy(user);
			type.setDateChanged(new Date());
		}
		
		getPersonDAO().createPersonAttributeType(type);
	}

	/**
	 * @see org.openmrs.api.PersonService#deletePersonAttributeType(java.lang.Integer)
	 */
	public void deletePersonAttributeType(Integer attrTypeId) {
		log.info("Creating person attribute type id: " + attrTypeId);
		deletePersonAttributeType(getPersonAttributeType(attrTypeId));
	}

	/**
	 * @see org.openmrs.api.PersonService#deletePersonAttributeType(org.openmrs.PersonAttributeType)
	 */
	public void deletePersonAttributeType(PersonAttributeType type) {
		getPersonDAO().deletePersonAttributeType(type);
	}

	/**
	 * @see org.openmrs.api.PersonService#getPersonAttributeTypes()
	 */
	public List<PersonAttributeType> getPersonAttributeTypes() {
		log.info("Getting person attribute types");
		return getPersonDAO().getPersonAttributeTypes();
	}

	/**
	 * @see org.openmrs.api.PersonService#updatePersonAttributeType(org.openmrs.PersonAttributeType)
	 */
	public void updatePersonAttributeType(PersonAttributeType type) {
		log.info("Updating person attribute type: " + type);
		if (type.getPersonAttributeTypeId() == null)
			createPersonAttributeType(type);
		else {
			type.setChangedBy(Context.getAuthenticatedUser());
			type.setDateChanged(new Date());
			getPersonDAO().updatePersonAttributeType(type);
		}
	}

	public PersonAttributeType getPersonAttributeType(Integer typeId) {
		return getPersonDAO().getPersonAttributeType(typeId);
	}

	public PersonAttribute getPersonAttribute(Integer id) {
		return getPersonDAO().getPersonAttribute(id);
	}

	public PersonAttributeType getPersonAttributeType(String s) {
		return getPersonDAO().getPersonAttributeType(s);
	}
	
	/**
	 * Get relationship by internal relationship identifier
	 * 
	 * @return Relationship
	 * @param relationshipId 
	 * @throws APIException
	 */
	public Relationship getRelationship(Integer relationshipId) throws APIException {
		if (!Context.isAuthenticated())
			throw new APIAuthenticationException("Authentication required");
		
		return getPersonDAO().getRelationship(relationshipId);
	}
	
	/**
	 * Get list of relationships that are not retired
	 * 
	 * @return non-voided Relationship list
	 * @throws APIException
	 */
	public List<Relationship> getRelationships() throws APIException {
		return getPersonDAO().getRelationships();
	}

	/**
	 * Get list of relationships that include Person in person_id or relative_id
	 * 
	 * @return Relationship list
	 * @throws APIException
	 */
	public List<Relationship> getRelationships(Person p, boolean showVoided) throws APIException {
		return getPersonDAO().getRelationships(p, showVoided);
	}
	
	public List<Relationship> getRelationships(Person p) throws APIException {
		return getRelationships(p, true);
	}

	/**
	 * Get list of relationships that have Person as relative_id, and the given type (which can be null)
	 * @return Relationship list
	 */
	public List<Relationship> getRelationshipsTo(Person toPerson, RelationshipType relType) throws APIException {
		List<Relationship> temp = getRelationships(toPerson);
		List<Relationship> ret = new ArrayList<Relationship>();
		for (Relationship rel : temp) {
			if (rel.getPersonB().equals(toPerson) &&
					(relType == null || relType.equals(rel.getRelationshipType()))) {
				ret.add(rel);
			}
		}
		return ret;
	}
	
	/**
	 * Get all relationshipTypes
	 * 
	 * @return relationshipType list
	 * @throws APIException
	 */
	public List<RelationshipType> getRelationshipTypes() throws APIException {
		if (!Context.isAuthenticated())
			throw new APIAuthenticationException("Authentication required");
		
		return getPersonDAO().getRelationshipTypes();
	}
	

	/**
	 * Get relationshipType by internal identifier
	 * 
	 * @param relationshipType id
	 * @return relationshipType with given internal identifier
	 * @throws APIException
	 */
	public RelationshipType getRelationshipType(Integer relationshipTypeId) throws APIException {
		// TODO use 'Authenticated User' option
		if (!Context.isAuthenticated())
			throw new APIAuthenticationException("Authentication required");
		
		return getPersonDAO().getRelationshipType(relationshipTypeId);
	}
	
	/**
	 * Find relationshipType by name
	 * @throws APIException
	 */
	public RelationshipType findRelationshipType(String relationshipTypeName) throws APIException {
		// TODO use 'Authenticated User' option
		if (!Context.isAuthenticated())
			throw new APIAuthenticationException("Authentication required");
		
		return getPersonDAO().findRelationshipType(relationshipTypeName);
	}
	
	/**
	 * Create a new Person
	 * @param Person to create
	 * @throws APIException
	 * @return Person created
	 */
	public Person createPerson(Person person) throws APIException {
		return getPersonDAO().createPerson(person);
	}
	
	/**
	 * Update an encounter type
	 * @param Person to update
	 * @throws APIException
	 */
	public void updatePerson(Person person) throws APIException {
		getPersonDAO().updatePerson(person);
	}
	
	/**
	 * Delete an encounter type
	 * @param Person to delete
	 * @throws APIException
	 */
	public void deletePerson(Person person) throws APIException {
		getPersonDAO().deletePerson(person);
	}
	
	/**
	 * Effectively removes this person from the system.  UserService.voidUser(person) and
	 * PatientService.voidPatient(person) are also called
	 * 
	 * @param person
	 * @param reason
	 * @throws APIException
	 */
	public void voidPerson(Person person, String reason) throws APIException {
		for (PersonName pn : person.getNames()) {
			if (!pn.isVoided()) {
				pn.setVoided(true);
				pn.setVoidReason(reason);
			}
		}
		for (PersonAddress pa : person.getAddresses()) {
			if (!pa.isVoided()) {
				pa.setVoided(true);
				pa.setVoidReason(reason);
			}
		}
		
		person.setVoided(true);
		person.setVoidedBy(Context.getAuthenticatedUser());
		person.setDateVoided(new Date());
		person.setVoidReason(reason);
		updatePerson(person);
		
		Context.getPatientService().voidPatient(Context.getPatientService().getPatient(person.getPersonId()), reason);
		Context.getUserService().voidUser(Context.getUserService().getUser(person.getPersonId()), reason);
	}
	
	/**
	 * Effectively resurrects this person in the db.  Unvoids the associated Patient and User as well
	 * @param person
	 * @throws APIException
	 */
	public void unvoidPerson(Person person) throws APIException {
		String voidReason = person.getVoidReason();
		if (voidReason == null)
			voidReason = "";
		
		for (PersonName pn : person.getNames()) {
			if (voidReason.equals(pn.getVoidReason())) {
				pn.setVoided(false);
				pn.setVoidReason(null);
			}
		}
		for (PersonAddress pa : person.getAddresses()) {
			if (voidReason.equals(pa.getVoidReason())) {
				pa.setVoided(false);
				pa.setVoidReason(null);
			}
		}
		
		person.setVoided(false);
		person.setVoidedBy(null);
		person.setDateVoided(null);
		person.setVoidReason(null);
		updatePerson(person);
		
		Context.getPatientService().unvoidPatient(Context.getPatientService().getPatient(person.getPersonId()));
		Context.getUserService().unvoidUser(Context.getUserService().getUser(person.getPersonId()));
	}
	
	/**
	 * 
	 * @param personId to get
	 * @return Person
	 * @throws APIException
	 */
	public Person getPerson(Integer personId) throws APIException {
		if (personId == null) return null;
		return getPersonDAO().getPerson(personId);
	}
	
	public Person getPerson(Patient pat) throws APIException {
		if (pat == null) return null;
		return getPerson(pat.getPatientId());
	}
	
	public Person getPerson(User user) throws APIException {
		if (user == null) return null;
		return getPerson(user.getUserId());
	}
	
	/**
	 * Create a new Relationship
	 * @param Relationship to create
	 * @throws APIException
	 */
	public void createRelationship(Relationship relationship) throws APIException {
		if (relationship.getPersonA().equals(relationship.getPersonB()))
			throw new APIException("Person A and Person B can't be the same");
		getPersonDAO().createRelationship(relationship);
	}

	/**
	 * Update Relationship
	 * @param Relationship to update
	 * @throws APIException
	 */
	public void updateRelationship(Relationship relationship) throws APIException {
		getPersonDAO().updateRelationship(relationship);
	}

	/**
	 * Delete Relationship
	 * @param Relationship to delete
	 * @throws APIException
	 */
	public void deleteRelationship(Relationship relationship) throws APIException {
		getPersonDAO().deleteRelationship(relationship);
	}
	
	/**
	 * Retire Relationship
	 * @param Relationship to void
	 * @throws APIException
	 */
	public void voidRelationship(Relationship relationship, String voidReason) throws APIException {
		if (relationship.isVoided())
			return;
		
		if (relationship.getVoidedBy() == null)
			relationship.setVoidedBy(Context.getAuthenticatedUser());
		if (voidReason != null)
			relationship.setVoidReason(voidReason);
		
		getPersonDAO().voidRelationship(relationship);
	}

	/**
	 * Unretire Relationship
	 * @param Relationship to unvoid
	 * @throws APIException
	 */
	public void unvoidRelationship(Relationship relationship) throws APIException {
		getPersonDAO().unvoidRelationship(relationship);
	}
		
	/**
	 * Create a new RelationshipType
	 * @param RelationshipType to create
	 * @throws APIException
	 */
	public void createRelationshipType(RelationshipType relationshipType) throws APIException {
		getPersonDAO().createRelationshipType(relationshipType);
	}

	/**
	 * Update RelationshipType
	 * @param RelationshipType to update
	 * @throws APIException
	 */
	public void updateRelationshipType(RelationshipType relationshipType) throws APIException {
		getPersonDAO().updateRelationshipType(relationshipType);
	}

	/**
	 * Delete RelationshipType
	 * @param RelationshipType to delete
	 * @throws APIException
	 */
	public void deleteRelationshipType(RelationshipType relationshipType) throws APIException {
		getPersonDAO().deleteRelationshipType(relationshipType);
	}

	/**
	 * @see org.openmrs.api.PersonService#getPersonAttributeTypes(java.lang.String, java.lang.String)
	 */
	public List<PersonAttributeType> getPersonAttributeTypes(String personType, String displayType) throws APIException {
		List<String> VALID_PERSON_TYPES = new Vector<String>(Arrays.asList(new String[] {"", "patient", "user"}));
		List<String> VALID_DISPLAY_TYPES = new Vector<String>(Arrays.asList(new String[] {"listing", "viewing", "all"}));
		AdministrationService as = Context.getAdministrationService();
		
		if (!VALID_PERSON_TYPES.contains(personType)) {
			throw new APIException(personType + " is an invalid value for 'personType' attribute");
		}
		
		if (!VALID_DISPLAY_TYPES.contains(displayType)) {
			throw new APIException(displayType + " is an invalid value for 'displayType' attribute");
		}
		
		String attrString = "";
		List<String> attrNames = new Vector<String>();
		
		// TODO cache the global properties to speed this up??
		// Is hibernate taking care of caching and not hitting the db every time? (hopefully it is)
		if ("listing".equals(displayType)) {
			if ("patient".equals(personType))
				attrString = as.getGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_LISTING_ATTRIBUTES, "");
			else if ("user".equals(personType))
				attrString = as.getGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_USER_LISTING_ATTRIBUTES, "");
			else if ("".equals(personType)) {
				attrString = as.getGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_LISTING_ATTRIBUTES, "");
				attrString += "," + as.getGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_USER_LISTING_ATTRIBUTES, "");
			}
			else
				log.fatal("Should not be here. 'personType' should be added to VALID_PERSON_TYPES");
		}
		else if ("viewing".equals(displayType)) {
			if ("patient".equals(personType))
				attrString = as.getGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_VIEWING_ATTRIBUTES, "");
			else if ("user".equals(personType))
				attrString = as.getGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_USER_VIEWING_ATTRIBUTES, "");
			else if ("".equals(personType)) {
				attrString = as.getGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_VIEWING_ATTRIBUTES, "");
				attrString += "," + as.getGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_USER_VIEWING_ATTRIBUTES, "");
			}
			else
				log.fatal("Should not be here. 'personType' should be added to VALID_PERSON_TYPES");
		}
		else if ("all".equals(displayType)) {
			return getPersonAttributeTypes();
		}
		else
			log.fatal("Should not be here. `displayType` parameter value of '" + displayType + "' needs to be added to VALID_DISPLAY_TYPES");
		
		// split the comma delimited string into a list
		if (attrString != null)
			for (String s : attrString.split(",")) {
				if (s != null) {
					s = s.trim();
					if (s.length() > 0)
						attrNames.add(s);
				}
			}
		
		// the actual list we'll be returning
		List<PersonAttributeType> attrObjects = new Vector<PersonAttributeType>();
		
		// get the PersonAttribute objects
		if (attrNames.size() > 0) {
			for (String nameOrId : attrNames) {
				if (nameOrId.matches("\\d"))
					attrObjects.add(getPersonAttributeType(Integer.valueOf(nameOrId)));
				else
					attrObjects.add(getPersonAttributeType(nameOrId));
			}
		}
		
		return attrObjects;
	}

	/**
	 * @see org.openmrs.api.PersonService#setCollectionProperties(org.openmrs.Person)
	 */
	public void setCollectionProperties(Person person) {
		// set it person creator/changer
		if (person.getCreator() == null) {
			person.setCreator(Context.getAuthenticatedUser());
			person.setDateCreated(new Date());
		}
		else {
			person.setChangedBy(Context.getAuthenticatedUser());
			person.setDateChanged(new Date());
		}
		
		// address collection
		if (person.getAddresses() != null && person.getAddresses().size() > 0)
			for (PersonAddress pAddress : person.getAddresses()) {
				if (pAddress.getDateCreated() == null) {
					pAddress.setDateCreated(new Date());
					pAddress.setCreator(Context.getAuthenticatedUser());
					pAddress.setPerson(person);
				}
			}
		
		// name collection
		if (person.getNames() != null && person.getNames().size() > 0)
			for (PersonName pName : person.getNames()) {
				if (pName.getDateCreated() == null) {
					pName.setDateCreated(new Date());
					pName.setCreator(Context.getAuthenticatedUser());
					pName.setPerson(person);
				}
			}
		
		// attribute collection
		if (person.getAttributes() != null && person.getAttributes().size() > 0)
			for (PersonAttribute pAttr : person.getAttributes()) {
				if (pAttr.getDateCreated() == null) {
					pAttr.setDateCreated(new Date());
					pAttr.setCreator(Context.getAuthenticatedUser());
					pAttr.setPerson(person);
				}
			}
	}
	
	/**
	 * @see org.openmrs.api.PersonService#splitPersonName(java.lang.String)
	 */
	public PersonName splitPersonName(String name) {
		String firstName = name;
		String middleName = "";
		String lastName = "";
		
		if (name.contains(",")) {
			String[] names = name.split(", ");
			String[] firstNames = names[1].split(" ");
			if (firstNames.length == 2) {
				lastName = names[0];
				firstName = firstNames[0];
				middleName = firstNames[1];
			}
			else {
				firstName = names[1];
				lastName = names[2];
			}
		}
		else if (name.contains(" ")) {
			String[] names = name.split(" ");
			if (names.length == 3) {
				firstName = names[0];
				middleName = names[1];
				lastName = names[2];
			}
			else {
				firstName = names[0];
				lastName = names[1];
			}
		}
		
		return new PersonName(firstName, middleName, lastName);
	}
	

	/**
	 * @see org.openmrs.api.PersonService#getRelationships(org.openmrs.RelationshipType)
	 */
	public Map<Person, List<Person>> getRelationships(RelationshipType relType) throws APIException {
		List<Relationship> all = this.getRelationships();
		Map<Person, List<Person>> ret = new HashMap<Person, List<Person>>();
		
		if ( all !=  null ) {
			for ( Relationship rel : all ) {
				if ( relType == null || (relType != null && relType.equals(rel.getRelationshipType())) ) {
					Person from = rel.getPersonA();
					Person to = rel.getPersonB();
					
					List<Person> relList = ret.get(from);
					if ( relList == null ) relList = new ArrayList<Person>();
					relList.add(to);
					
					ret.put(from, relList);
				}
			}
		}
		
		return ret;
	}

}
