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
package org.openmrs.api.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
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
import org.openmrs.api.APIException;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.PersonService;
import org.openmrs.api.UserService;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.PersonDAO;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.OpenmrsConstants.PERSON_TYPE;
import org.springframework.util.Assert;

/**
 * Default implementation of the PersonService
 * <p>
 * Which implementation to use is determined by Spring. See the spring application context file in
 * /metadata/api/spring/applicatContext-service.xml
 * 
 * @see PersonService
 * @see org.openmrs.api.context.Context
 */
public class PersonServiceImpl implements PersonService {
	
	private Log log = LogFactory.getLog(this.getClass());
	
	private PersonDAO dao;
	
	/**
	 * @see org.openmrs.api.PersonService#setPersonDAO(org.openmrs.api.db.PersonDAO)
	 */
	public void setPersonDAO(PersonDAO dao) {
		this.dao = dao;
	}
	
	/**
	 * @see org.openmrs.api.PersonService#getSimilarPeople(java.lang.String, java.lang.Integer,
	 *      java.lang.String)
	 */
	public Set<Person> getSimilarPeople(String name, Integer birthyear, String gender) throws APIException {
		return dao.getSimilarPeople(name, birthyear, gender);
	}
	
	/**
	 * @see org.openmrs.api.PersonService#getPeople(String, Boolean)
	 */
	public List<Person> getPeople(String searchPhrase, Boolean dead) throws APIException {
		
		return dao.getPeople(searchPhrase, dead);
	}
	
	/**
	 * @deprecated use {@link #getPeople(String, Boolean)}
	 */
	public Set<Person> findPeople(String searchPhrase, boolean includeVoided) {
		if (includeVoided)
			throw new APIException("You should consider voided people as if they are deleted and they cannot be searched");
		
		// convert the list to a set
		Set<Person> matchingPersons = new LinkedHashSet<Person>();
		matchingPersons.addAll(getPeople(searchPhrase, null));
		
		return matchingPersons;
	}
	
	/**
	 * @deprecated use {@link #getPeople(String, Boolean)}
	 */
	public Set<Person> findPeople(String searchPhrase, boolean includeVoided, String roles) {
		List<String> roleList = null;
		
		if (roles != null)
			if (roles.length() > 0) {
				String[] splitRoles = roles.split(",");
				for (String role : splitRoles) {
					if (roleList == null)
						roleList = new ArrayList<String>();
					roleList.add(role);
				}
			}
		
		return findPeople(searchPhrase, includeVoided, roleList);
	}
	
	/**
	 * @deprecated use {@link #getPeople(String, Boolean)}
	 */
	public Set<Person> findPeople(String searchPhrase, boolean includeVoided, List<String> roles) {
		Set<Person> people = new HashSet<Person>();
		
		// If no rules *are not* defined then find all matching persons (users and patients).
		if (roles == null) {
			people.addAll(getPeople(searchPhrase, includeVoided));
		}
		// If roles *are* defined then find matching users who have the given roles.
		else {
			people.addAll(Context.getUserService().findUsers(searchPhrase, roles, includeVoided));
		}
		
		return people;
	}
	
	/**
	 * @see org.openmrs.api.PersonService#getAllPersonAttributeTypes()
	 */
	public List<PersonAttributeType> getAllPersonAttributeTypes() throws APIException {
		return getAllPersonAttributeTypes(true);
	}
	
	/**
	 * @see org.openmrs.api.PersonService#getAllPersonAttributeTypes(boolean)
	 */
	public List<PersonAttributeType> getAllPersonAttributeTypes(boolean includeRetired) throws APIException {
		return dao.getAllPersonAttributeTypes(includeRetired);
	}
	
	/**
	 * @see org.openmrs.api.PersonService#getPersonAttributeTypeByName(java.lang.String)
	 */
	public PersonAttributeType getPersonAttributeTypeByName(String typeName) throws APIException {
		List<PersonAttributeType> types = getPersonAttributeTypes(typeName, null, null, null);
		
		if (types.size() < 1)
			return null;
		else
			return types.get(0);
	}
	
	/**
	 * @see org.openmrs.api.PersonService#purgePersonAttributeType(org.openmrs.PersonAttributeType)
	 */
	public void purgePersonAttributeType(PersonAttributeType type) throws APIException {
		dao.deletePersonAttributeType(type);
	}
	
	/**
	 * @see org.openmrs.api.PersonService#savePersonAttributeType(org.openmrs.PersonAttributeType)
	 */
	public PersonAttributeType savePersonAttributeType(PersonAttributeType type) throws APIException {
		User user = Context.getAuthenticatedUser();
		Date date = new Date();
		
		if (type.getCreator() == null)
			type.setCreator(user);
		
		if (type.getDateCreated() == null)
			type.setDateCreated(date);
		
		if (type.getPersonAttributeTypeId() != null) {
			type.setChangedBy(user);
			type.setDateChanged(date);
		}
		
		return dao.savePersonAttributeType(type);
	}
	
	/**
	 * @deprecated use {@link #savePersonAttributeType(PersonAttributeType)}
	 */
	public void createPersonAttributeType(PersonAttributeType type) throws APIException {
		savePersonAttributeType(type);
	}
	
	/**
	 * @deprecated use {@link #savePersonAttributeType(PersonAttributeType)}
	 */
	public void updatePersonAttributeType(PersonAttributeType type) throws APIException {
		savePersonAttributeType(type);
	}
	
	/**
	 * @see org.openmrs.api.PersonService#getPersonAttributeTypes(java.lang.String,
	 *      java.lang.String, java.lang.Integer, java.lang.Boolean)
	 */
	public List<PersonAttributeType> getPersonAttributeTypes(String exactName, String format, Integer foreignKey,
	                                                         Boolean searchable) throws APIException {
		return dao.getPersonAttributeTypes(exactName, format, foreignKey, searchable);
	}
	
	/**
	 * @deprecated use {@link #purgePersonAttributeType(PersonAttributeType)}
	 */
	public void deletePersonAttributeType(Integer attrTypeId) {
		deletePersonAttributeType(getPersonAttributeType(attrTypeId));
	}
	
	/**
	 * @deprecated use {@link #purgePersonAttributeType(PersonAttributeType)}
	 */
	public void deletePersonAttributeType(PersonAttributeType type) {
		dao.deletePersonAttributeType(type);
	}
	
	/**
	 * @deprecated use {@link #getAllPersonAttributeTypes()}
	 */
	public List<PersonAttributeType> getPersonAttributeTypes() {
		return getAllPersonAttributeTypes();
	}
	
	/**
	 * @see org.openmrs.api.PersonService#getPersonAttributeType(java.lang.Integer)
	 */
	public PersonAttributeType getPersonAttributeType(Integer typeId) {
		return dao.getPersonAttributeType(typeId);
	}
	
	/**
	 * @see org.openmrs.api.PersonService#getPersonAttribute(java.lang.Integer)
	 */
	public PersonAttribute getPersonAttribute(Integer id) {
		return dao.getPersonAttribute(id);
	}
	
	/**
	 * @deprecated use {@link #getPersonAttributeTypeByName(String)}
	 */
	public PersonAttributeType getPersonAttributeType(String s) {
		return getPersonAttributeTypeByName(s);
	}
	
	/**
	 * @see org.openmrs.api.PersonService#getRelationship(java.lang.Integer)
	 */
	public Relationship getRelationship(Integer relationshipId) throws APIException {
		return dao.getRelationship(relationshipId);
	}
	
	/**
	 * @deprecated use {@link #getAllRelationships()}
	 */
	public List<Relationship> getRelationships() throws APIException {
		return getAllRelationships();
	}
	
	/**
	 * @deprecated use {@link #getRelationshipsByPerson(Person)}
	 */
	public List<Relationship> getRelationships(Person p, boolean showVoided) throws APIException {
		if (showVoided)
			throw new APIException(
			        "Voided relationships should be considered gone and unusable.  Don't search for or show them");
		
		return getRelationshipsByPerson(p);
	}
	
	/**
	 * @deprecated use {@link #getRelationshipsByPerson(Person)}
	 */
	public List<Relationship> getRelationships(Person p) throws APIException {
		return getRelationshipsByPerson(p);
	}
	
	/**
	 * @deprecated use {@link #getRelationships(Person, Person, RelationshipType)}
	 */
	public List<Relationship> getRelationshipsTo(Person toPerson, RelationshipType relType) throws APIException {
		return getRelationships(null, toPerson, relType);
	}
	
	/**
	 * @deprecated use {@link #getAllRelationshipTypes()}
	 */
	public List<RelationshipType> getRelationshipTypes() throws APIException {
		return getAllRelationshipTypes();
	}
	
	/**
	 * @see org.openmrs.api.PersonService#getRelationshipType(java.lang.Integer)
	 */
	public RelationshipType getRelationshipType(Integer relationshipTypeId) throws APIException {
		return dao.getRelationshipType(relationshipTypeId);
	}
	
	/**
	 * @deprecated use {@link #getRelationshipTypeByName(String)}
	 */
	public RelationshipType findRelationshipType(String relationshipTypeName) throws APIException {
		return getRelationshipTypeByName(relationshipTypeName);
	}
	
	/**
	 * @see org.openmrs.api.PersonService#getRelationshipTypeByName(java.lang.String)
	 */
	public RelationshipType getRelationshipTypeByName(String relationshipTypeName) throws APIException {
		List<RelationshipType> types = dao.getRelationshipTypes(relationshipTypeName, null);
		
		if (types.size() < 1)
			return null;
		else
			return types.get(0);
	}
	
	/**
	 * @see org.openmrs.api.PersonService#purgePerson(org.openmrs.Person)
	 */
	public void purgePerson(Person person) throws APIException {
		dao.deletePerson(person);
	}
	
	/**
	 * @see org.openmrs.api.PersonService#savePerson(org.openmrs.Person)
	 */
	public Person savePerson(Person person) throws APIException {
		return dao.savePerson(person);
	}
	
	/**
	 * @deprecated use {@link #savePerson(Person)}
	 */
	public Person createPerson(Person person) throws APIException {
		return savePerson(person);
	}
	
	/**
	 * @deprecated use {@link #savePerson(Person)}
	 */
	public void updatePerson(Person person) throws APIException {
		savePerson(person);
	}
	
	/**
	 * @deprecated use {@link #purgePerson(Person)}
	 */
	public void deletePerson(Person person) throws APIException {
		purgePerson(person);
	}
	
	/**
	 * @see org.openmrs.api.PersonService#voidPerson(org.openmrs.Person, java.lang.String)
	 */
	public Person voidPerson(Person person, String reason) throws APIException {
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
		
		person.setPersonVoided(true);
		person.setPersonVoidedBy(Context.getAuthenticatedUser());
		person.setPersonDateVoided(new Date());
		person.setPersonVoidReason(reason);
		savePerson(person);
		Context.getPatientService().voidPatient(Context.getPatientService().getPatient(person.getPersonId()), reason);
		UserService us = Context.getUserService();
		User user = us.getUser(person.getPersonId());
		if (user != null)
			us.voidUser(user, reason);
		
		return person;
	}
	
	/**
	 * @see org.openmrs.api.PersonService#unvoidPerson(org.openmrs.Person)
	 */
	public Person unvoidPerson(Person person) throws APIException {
		String voidReason = person.getPersonVoidReason();
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
		
		person.setPersonVoided(false);
		person.setPersonVoidedBy(null);
		person.setPersonDateVoided(null);
		person.setPersonVoidReason(null);
		savePerson(person);
		
		Context.getPatientService().unvoidPatient(Context.getPatientService().getPatient(person.getPersonId()));
		Context.getUserService().unvoidUser(Context.getUserService().getUser(person.getPersonId()));
		
		return person;
	}
	
	/**
	 * @see org.openmrs.api.PersonService#getPerson(java.lang.Integer)
	 */
	public Person getPerson(Integer personId) throws APIException {
		if (personId == null)
			return null;
		return dao.getPerson(personId);
	}
	
	/**
	 * @deprecated use {@link #getPerson(Integer)}
	 */
	public Person getPerson(Patient pat) throws APIException {
		if (pat == null)
			return null;
		return getPerson(pat.getPatientId());
	}
	
	/**
	 * @deprecated use {@link #getPerson(Integer)}
	 */
	public Person getPerson(User user) throws APIException {
		if (user == null)
			return null;
		return getPerson(user.getUserId());
	}
	
	/**
	 * @see org.openmrs.api.PersonService#getAllRelationships()
	 */
	public List<Relationship> getAllRelationships() throws APIException {
		return getAllRelationships(false);
	}
	
	/**
	 * @see org.openmrs.api.PersonService#getAllRelationships(boolean)
	 */
	public List<Relationship> getAllRelationships(boolean includeVoided) throws APIException {
		return dao.getAllRelationships(includeVoided);
	}
	
	/**
	 * @see org.openmrs.api.PersonService#getRelationships(org.openmrs.Person, org.openmrs.Person,
	 *      org.openmrs.RelationshipType)
	 */
	public List<Relationship> getRelationships(Person fromPerson, Person toPerson, RelationshipType relType)
	                                                                                                        throws APIException {
		return dao.getRelationships(fromPerson, toPerson, relType);
	}
	
	/**
	 * @see org.openmrs.api.PersonService#getRelationshipsByPerson(org.openmrs.Person)
	 */
	public List<Relationship> getRelationshipsByPerson(Person p) throws APIException {
		
		// search both the left side and the right side of the relationship
		// for this person
		List<Relationship> rels = getRelationships(p, null, null);
		rels.addAll(getRelationships(null, p, null));
		
		return rels;
	}
	
	/**
	 * @see org.openmrs.api.PersonService#purgeRelationship(org.openmrs.Relationship)
	 */
	public void purgeRelationship(Relationship relationship) throws APIException {
		dao.deleteRelationship(relationship);
	}
	
	/**
	 * @see org.openmrs.api.PersonService#saveRelationship(org.openmrs.Relationship)
	 */
	public Relationship saveRelationship(Relationship relationship) throws APIException {
		if (relationship.getPersonA().equals(relationship.getPersonB()))
			throw new APIException("Person A and Person B can't be the same");
		
		User user = Context.getAuthenticatedUser();
		Date date = new Date();
		
		relationship.setCreator(user);
		relationship.setDateCreated(date);
		
		return dao.saveRelationship(relationship);
	}
	
	/**
	 * @deprecated use {@link #saveRelationship(Relationship)}
	 */
	public void createRelationship(Relationship relationship) throws APIException {
		saveRelationship(relationship);
	}
	
	/**
	 * @deprecated use {@link #saveRelationship(Relationship)}
	 */
	public void updateRelationship(Relationship relationship) throws APIException {
		saveRelationship(relationship);
	}
	
	/**
	 * @deprecated use {@link #purgeRelationship(Relationship)}
	 */
	public void deleteRelationship(Relationship relationship) throws APIException {
		purgeRelationship(relationship);
	}
	
	/**
	 * @see org.openmrs.api.PersonService#voidRelationship(org.openmrs.Relationship,
	 *      java.lang.String)
	 */
	public Relationship voidRelationship(Relationship relationship, String voidReason) throws APIException {
		if (relationship.isVoided())
			return relationship;
		
		relationship.setVoided(true);
		if (relationship.getVoidedBy() == null)
			relationship.setVoidedBy(Context.getAuthenticatedUser());
		if (voidReason != null)
			relationship.setVoidReason(voidReason);
		relationship.setDateVoided(new Date());
		
		return saveRelationship(relationship);
	}
	
	/**
	 * @see org.openmrs.api.PersonService#unvoidRelationship(org.openmrs.Relationship)
	 */
	public Relationship unvoidRelationship(Relationship relationship) throws APIException {
		relationship.setVoided(false);
		relationship.setVoidedBy(null);
		relationship.setDateVoided(null);
		relationship.setVoidReason(null);
		
		return saveRelationship(relationship);
	}
	
	/**
	 * @deprecated use {@link #saveRelationshipType(RelationshipType)}
	 */
	public void createRelationshipType(RelationshipType relationshipType) throws APIException {
		saveRelationshipType(relationshipType);
	}
	
	/**
	 * @deprecated use {@link #saveRelationshipType(RelationshipType)}
	 */
	public void updateRelationshipType(RelationshipType relationshipType) throws APIException {
		saveRelationshipType(relationshipType);
	}
	
	/**
	 * @deprecated use {@link #purgeRelationshipType(RelationshipType)}
	 */
	public void deleteRelationshipType(RelationshipType relationshipType) throws APIException {
		dao.deleteRelationshipType(relationshipType);
	}
	
	/**
	 * @see org.openmrs.api.PersonService#getAllRelationshipTypes()
	 */
	public List<RelationshipType> getAllRelationshipTypes() throws APIException {
		return dao.getAllRelationshipTypes();
	}
	
	/**
	 * @see org.openmrs.api.PersonService#getRelationshipTypes(java.lang.String)
	 */
	public List<RelationshipType> getRelationshipTypes(String searchString) throws APIException {
		
		return getRelationshipTypes(searchString, null);
	}
	
	/**
	 * @see org.openmrs.api.PersonService#getRelationshipTypes(java.lang.String, java.lang.Boolean)
	 */
	public List<RelationshipType> getRelationshipTypes(String relationshipTypeName, Boolean preferred) throws APIException {
		Assert.hasText(relationshipTypeName, "The search string cannot be empty");
		
		return dao.getRelationshipTypes(relationshipTypeName, preferred);
	}
	
	/**
	 * @see org.openmrs.api.PersonService#purgeRelationshipType(org.openmrs.RelationshipType)
	 */
	public void purgeRelationshipType(RelationshipType relationshipType) throws APIException {
		dao.deleteRelationshipType(relationshipType);
	}
	
	/**
	 * @see org.openmrs.api.PersonService#saveRelationshipType(org.openmrs.RelationshipType)
	 */
	public RelationshipType saveRelationshipType(RelationshipType relationshipType) throws APIException {
		
		User user = Context.getAuthenticatedUser();
		Date date = new Date();
		
		relationshipType.setCreator(user);
		relationshipType.setDateCreated(date);
		
		return dao.saveRelationshipType(relationshipType);
	}
	
	/**
	 * @see org.openmrs.api.PersonService#getPersonAttributeTypes(org.openmrs.util.OpenmrsConstants.PERSON_TYPE,
	 *      org.openmrs.api.PersonService.ATTR_VIEW_TYPE)
	 */
	public List<PersonAttributeType> getPersonAttributeTypes(PERSON_TYPE personType, ATTR_VIEW_TYPE viewType)
	                                                                                                         throws APIException {
		AdministrationService as = Context.getAdministrationService();
		
		String attrString = "";
		
		// TODO cache the global properties to speed this up??
		// Is hibernate taking care of caching and not hitting the db every time? (hopefully it is)
		if (viewType == null) {
			return getAllPersonAttributeTypes();
		} else if (viewType == ATTR_VIEW_TYPE.LISTING) {
			String patientListing = as.getGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_LISTING_ATTRIBUTES, "");
			String userListing = as.getGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_USER_LISTING_ATTRIBUTES, "");
			if (personType == null || personType == PERSON_TYPE.PERSON)
				attrString = patientListing + "," + userListing;
			else if (personType == PERSON_TYPE.PATIENT)
				attrString = patientListing;
			else if (personType == PERSON_TYPE.USER)
				attrString = userListing;
			else
				log.fatal("Should not be here.");
		} else if (viewType == ATTR_VIEW_TYPE.VIEWING) {
			String patientViewing = as.getGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_VIEWING_ATTRIBUTES, "");
			String userViewing = as.getGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_USER_VIEWING_ATTRIBUTES, "");
			if (personType == null || personType == PERSON_TYPE.PERSON)
				attrString = patientViewing + "," + userViewing;
			else if (personType == PERSON_TYPE.PATIENT)
				attrString = patientViewing;
			else if (personType == PERSON_TYPE.USER)
				attrString = userViewing;
			else
				log.fatal("Should not be here");
		} else if (viewType == ATTR_VIEW_TYPE.HEADER) {
			String patientHeader = as.getGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_HEADER_ATTRIBUTES, "");
			String userHeader = as.getGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_USER_HEADER_ATTRIBUTES, "");
			if (personType == null || personType == PERSON_TYPE.PERSON)
				attrString = patientHeader + "," + userHeader;
			else if (personType == PERSON_TYPE.PATIENT)
				attrString = patientHeader;
			else if (personType == PERSON_TYPE.USER)
				attrString = userHeader;
			else
				log.fatal("Should not be here");
			
		} else
			log.fatal("Should not be here");
		
		// the java list object to hold the values from the global properties
		List<String> attrNames = new Vector<String>();
		
		// split the comma delimited string into a java list object
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
		
		// get the PersonAttribute objects for each name/id
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
	 * @deprecated @see
	 *             {@link org.openmrs.api.PersonService#getPersonAttributeTypes(java.lang.String, java.lang.String)}
	 */
	public List<PersonAttributeType> getPersonAttributeTypes(String personTypeStr, String displayTypeStr)
	                                                                                                     throws APIException {
		
		PERSON_TYPE personType = null;
		if ("patient".equals(personTypeStr))
			personType = PERSON_TYPE.PATIENT;
		else if ("user".equals(personTypeStr))
			personType = PERSON_TYPE.USER;
		else if (personTypeStr == null || personTypeStr.equals(""))
			personType = null;
		else
			throw new APIException(personTypeStr + " is an invalid value for 'personType' attribute");
		
		ATTR_VIEW_TYPE attrDisplayType = null;
		if ("listing".equals(displayTypeStr))
			attrDisplayType = ATTR_VIEW_TYPE.LISTING;
		else if ("viewing".equals(displayTypeStr))
			attrDisplayType = ATTR_VIEW_TYPE.VIEWING;
		else if ("header".equals(displayTypeStr))
			attrDisplayType = ATTR_VIEW_TYPE.HEADER;
		else if ("all".equals(displayTypeStr))
			attrDisplayType = null;
		else
			throw new APIException(displayTypeStr + " is an invalid value for 'displayType' attribute");
		
		return getPersonAttributeTypes(personType, attrDisplayType);
	}
	
	/**
	 * @see org.openmrs.api.PersonService#setCollectionProperties(org.openmrs.Person)
	 */
	public void setCollectionProperties(Person person) {
		// set it person creator/changer
		if (person.getPersonCreator() == null) {
			person.setPersonCreator(Context.getAuthenticatedUser());
			person.setPersonDateCreated(new Date());
		} else {
			person.setPersonChangedBy(Context.getAuthenticatedUser());
			person.setPersonDateChanged(new Date());
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
	 * @see org.openmrs.api.PersonService#parsePersonName(java.lang.String)
	 */
	public PersonName parsePersonName(String name) throws APIException {
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
			} else {
				firstName = names[1];
				lastName = names[0];
			}
		} else if (name.contains(" ")) {
			String[] names = name.split(" ");
			if (names.length == 3) {
				firstName = names[0];
				middleName = names[1];
				lastName = names[2];
			} else {
				firstName = names[0];
				lastName = names[1];
			}
		}
		
		return new PersonName(firstName, middleName, lastName);
	}
	
	/**
	 * @deprecated see #parsePersonName(String)
	 */
	public PersonName splitPersonName(String name) {
		return parsePersonName(name);
	}
	
	/**
	 * @see org.openmrs.api.PersonService#getRelationshipMap(org.openmrs.RelationshipType)
	 */
	public Map<Person, List<Person>> getRelationshipMap(RelationshipType relType) throws APIException {
		
		// get all relationships with this type
		List<Relationship> relationships = getRelationships(null, null, relType);
		
		// the map to return
		Map<Person, List<Person>> ret = new HashMap<Person, List<Person>>();
		
		if (relationships != null) {
			for (Relationship rel : relationships) {
				Person from = rel.getPersonA();
				Person to = rel.getPersonB();
				
				List<Person> relList = ret.get(from);
				if (relList == null)
					relList = new ArrayList<Person>();
				relList.add(to);
				
				ret.put(from, relList);
			}
		}
		
		return ret;
	}
	
	/**
	 * @deprecated use {@link #getRelationshipMap(RelationshipType)}
	 */
	public Map<Person, List<Person>> getRelationships(RelationshipType relType) throws APIException {
		return getRelationshipMap(relType);
	}
	
}
