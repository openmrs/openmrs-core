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
package org.openmrs.api;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonAttributeType;
import org.openmrs.PersonName;
import org.openmrs.Relationship;
import org.openmrs.RelationshipType;
import org.openmrs.User;
import org.openmrs.annotation.Authorized;
import org.openmrs.api.db.PersonDAO;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface PersonService {
	
	public void setPersonDAO(PersonDAO dao);

	/**
	 * Find a similar person given the attributes
	 * 
	 * @param name
	 * @param birthyear
	 * @param gender
	 * @return
	 * @throws APIException
	 */
	@Transactional(readOnly=true)
	@Authorized({"View People"})
	public Set<Person> getSimilarPeople(String name, Integer birthyear,
			String gender) throws APIException;
	
	/**
	 * Find a person matching the <tt>searchPhrase</tt> search string
	 * 
	 * @param searchPhrase
	 * @param includeVoided
	 * @return list of possible person matches
	 */
	@Transactional(readOnly=true)
	@Authorized({"View People"})
	public Set<Person> findPeople(String searchPhrase, boolean includeVoided);

	@Transactional(readOnly=true)
	@Authorized({"View People"})
	public Set<Person> findPeople(String searchPhrase, boolean includeVoided, String roles);

	@Transactional(readOnly=true)
	@Authorized({"View People"})
	public Set<Person> findPeople(String searchPhrase, boolean includeVoided, List<String> roles);

	/**
	 * Create the person attribute type in the database
	 * @param type
	 */
	@Authorized({"Manage Person Attribute Types"})
	public void createPersonAttributeType(PersonAttributeType type);
	
	/**
	 * Dleete a PersonAttribute type
	 * @param type
	 */
	@Authorized({"Manage Person Attribute Types"})
	public void deletePersonAttributeType(PersonAttributeType type);
	
	/**
	 * Effectively removes this person from the system.  UserService.voidUser(person) and
	 * PatientService.voidPatient(person) are also called
	 * 
	 * @param person person to be voided
	 * @param reason reason for voiding person
	 */
	@Authorized({"Edit Person"})
	public void voidPerson(Person person, String reason) throws APIException;

	/**
	 * Effectively resurrects this person in the db.  Unvoids the associated Patient and User as well
	 * 
	 * @param person person to be revived
	 */
	@Authorized({"Edit Person"})
	public void unvoidPerson(Person person) throws APIException;

	
	/**
	 * Delete a Person Attribute Type
	 * @param attrTypeId
	 */
	@Authorized({"Manage Person Attribute Types"})
	public void deletePersonAttributeType(Integer attrTypeId);
	
	/**
	 * Save changes to a PersonAttributeType
	 * @param type
	 */
	@Authorized({"Manage Person Attribute Types"})
	public void updatePersonAttributeType(PersonAttributeType type);
	
	/**
	 * Get all PersonAttributeTypes in the database
	 * @return
	 */
	@Transactional(readOnly=true)
	public List<PersonAttributeType> getPersonAttributeTypes();
	
	/**
	 * Get the PersonAttributeType given the type's PersonAttributeTypeId
	 * @param typeId
	 * @return
	 */
	@Transactional(readOnly=true)
	public PersonAttributeType getPersonAttributeType(Integer typeId);
	
	@Transactional(readOnly=true)
	public PersonAttribute getPersonAttribute(Integer id);

	/**
	 * Get the PersonAttributeType given the type's name
	 * @param typeName
	 * @return
	 */
	@Transactional(readOnly=true)
	public PersonAttributeType getPersonAttributeType(String typeName);
	
	/**
	 * Get relationship by internal relationship identifier
	 * 
	 * @return Relationship
	 * @param relationshipId 
	 * @throws APIException
	 */
	@Transactional(readOnly=true)
	public Relationship getRelationship(Integer relationshipId)
			throws APIException;

	/**
	 * Get list of relationships that are not retired
	 * 
	 * @return non-voided Relationship list
	 * @throws APIException
	 */
	@Transactional(readOnly=true)
	public List<Relationship> getRelationships() throws APIException;

	/**
	 * Get list of relationships that include Person in person_id or relative_id
	 * 
	 * @return Relationship list
	 * @throws APIException
	 */
	@Transactional(readOnly=true)
	@Authorized({"Manage Relationships"})
	public List<Relationship> getRelationships(Person p, boolean showVoided)
			throws APIException;
	
	/**
	 * Get all relationships for the given person
	 * @param p
	 * @return
	 * @throws APIException
	 */
	@Transactional(readOnly=true)
	@Authorized({"Manage Relationships"})
	public List<Relationship> getRelationships(Person p) throws APIException;

	/**
	 * Get list of relationships that have Person as relative_id, and the given type (which can be null)
	 * @return Relationship list
	 */
	@Transactional(readOnly=true)
	public List<Relationship> getRelationshipsTo(Person toPerson,
			RelationshipType relType) throws APIException;

	/**
	 * Get all relationshipTypes
	 * 
	 * @return relationshipType list
	 * @throws APIException
	 */
	@Transactional(readOnly=true)
	public List<RelationshipType> getRelationshipTypes() throws APIException;

	/**
	 * Get relationshipType by internal identifier
	 * 
	 * @param relationshipType id
	 * @return relationshipType with given internal identifier
	 * @throws APIException
	 */
	@Transactional(readOnly=true)
	public RelationshipType getRelationshipType(Integer relationshipTypeId)
			throws APIException;

	/**
	 * Find relationshipType by name
	 * @throws APIException
	 */
	@Transactional(readOnly=true)
	public RelationshipType findRelationshipType(String relationshipTypeName)
			throws APIException;

	/**
	 * Create a new Relationship
	 * @param Relationship to create
	 * @throws APIException
	 */
	@Authorized({"Manage Relationships"})
	public void createRelationship(Relationship relationship)
			throws APIException;

	/**
	 * Update Relationship
	 * @param Relationship to update
	 * @throws APIException
	 */
	@Authorized({"Manage Relationships"})
	public void updateRelationship(Relationship relationship)
			throws APIException;

	/**
	 * Delete Relationship
	 * @param Relationship to delete
	 * @throws APIException
	 */
	@Authorized({"Manage Relationships"})
	public void deleteRelationship(Relationship relationship)
			throws APIException;

	/**
	 * Retire Relationship
	 * @param Relationship to void
	 * @throws APIException
	 */
	@Authorized({"Manage Relationships"})
	public void voidRelationship(Relationship relationship, String voidReason) throws APIException;

	/**
	 * Unretire Relationship
	 * @param Relationship to unvoid
	 * @throws APIException
	 */
	public void unvoidRelationship(Relationship relationship)
			throws APIException;

	/**
	 * Create a new Person
	 * @param Person to create
	 * @throws APIException
	 * @return Person created
	 */
	@Authorized({"Add People"})
	public Person createPerson(Person person) throws APIException;
	
	/**
	 * Update an encounter type
	 * @param Person to update
	 * @throws APIException
	 */
	@Authorized({"Edit People"})
	public void updatePerson(Person person) throws APIException;

	/**
	 * Delete an encounter type
	 * @param Person to delete
	 * @throws APIException
	 */
	@Authorized({"Delete People"})
	public void deletePerson(Person person) throws APIException;

	/**
	 * 
	 * @param personId to get
	 * @return Person
	 * @throws APIException
	 */
	@Transactional(readOnly=true)
	@Authorized({"View People"})
	public Person getPerson(Integer personId) throws APIException;
	
	/**
	 * Get the person object given the patient object
	 * @param pat
	 * @return
	 * @throws APIException
	 */
	@Transactional(readOnly=true)
	@Authorized({"View People"})
	public Person getPerson(Patient pat) throws APIException;
	
	/**
	 * Get the person object given the user object
	 * @param user
	 * @return
	 * @throws APIException
	 */
	@Transactional(readOnly=true)
	@Authorized({"View People"})
	public Person getPerson(User user) throws APIException;
	
	/**
	 * Create a new RelationshipType
	 * @param RelationshipType to create
	 * @throws APIException
	 */
	@Authorized({"Manage Relationship Types"})
	public void createRelationshipType(RelationshipType relationshipType)
			throws APIException;

	/**
	 * Update RelationshipType
	 * @param RelationshipType to update
	 * @throws APIException
	 */
	@Authorized({"Manage Relationship Types"})
	public void updateRelationshipType(RelationshipType relationshipType)
			throws APIException;

	/**
	 * Delete RelationshipType
	 * @param RelationshipType to delete
	 * @throws APIException
	 */
	@Authorized({"Manage Relationship Types"})
	public void deleteRelationshipType(RelationshipType relationshipType)
			throws APIException;
	
	/**
	 * 
	 * @param personType one of patient, user, or null/empty string
	 * @param viewType one of viewing, listing, all
	 * @return
	 */
	@Transactional(readOnly=true)
	public List<PersonAttributeType> getPersonAttributeTypes(String personType, String viewType);
	
	/**
	 * Iterates over Names/Addresses/Attributes to set dateCreated and creator properties if needed
	 * @param person
	 */
	public void setCollectionProperties(Person person);
	
	/**
	 * Splits the <code>name</code> string into Given, Middle, and Family parts of a PersonName
	 *  
	 * @param name
	 * @return fully formed PersonName object
	 */
	public PersonName splitPersonName(String name);
	
	/**
	 * Get all relationships for this relationship type
	 * @param accompLeaderType
	 * @return Map from person object to list of people they are related to (through <tt>relType</tt>)
	 * @throws APIException
	 */
	@Transactional(readOnly=true)
	public Map<Person, List<Person>> getRelationships(RelationshipType relType) throws APIException;
}
