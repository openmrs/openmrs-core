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
import org.openmrs.PersonAddress;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonAttributeType;
import org.openmrs.PersonName;
import org.openmrs.Relationship;
import org.openmrs.RelationshipType;
import org.openmrs.User;
import org.openmrs.annotation.Authorized;
import org.openmrs.api.db.PersonDAO;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.OpenmrsConstants.PERSON_TYPE;
import org.springframework.transaction.annotation.Transactional;

/**
 * Contains methods pertaining to Persons in the system Use:<br/>
 * 
 * <pre>
 * List&lt;Person&gt; personObjects = Context.getPersonService().getAllPersons();
 * </pre>
 * 
 * @see org.openmrs.api.context.Context
 * @see org.openmrs.Patient
 */
@Transactional
public interface PersonService {
	
	/**
	 * These enumerations are used when determining which person attr types to display. If listing
	 * off a lot of patients/users, one set of types are shown. When only displaying one
	 * patient/user, another type is shown.
	 */
	public static enum ATTR_VIEW_TYPE {
		/**
		 * Attributes to be shown when listing off multiple patients or users
		 */
		LISTING,

		/**
		 * Attributes to be shown when only showing one patient or user
		 */
		VIEWING,

		/**
		 * Attributes to be shown in the header
		 */
		HEADER,
		
	}
	
	/**
	 * Sets the DAO for this service. This is done through spring injection
	 * 
	 * @param dao DAO for this service
	 */
	public void setPersonDAO(PersonDAO dao);
	
	/**
	 * Find a similar person given the attributes. This does a very loose lookup with the
	 * <code>nameSearch</code> parameter. This does a very loose lookup on <code>birthyear</code> as
	 * well. Any person with a null/missing birthdate is included and anyone with a birthyear
	 * plus/minus one year from the given <code>birthyear</code> is also included
	 * 
	 * @param nameSearch string to search the person's name for
	 * @param birthyear the year of birth to restrict
	 * @param gender The gender field to search on (Typically just "M" or "F")
	 * @return Set<Person> object with all people matching criteria
	 * @throws APIException
	 * @should accept greater than three names
	 * @should match single search to any name part
	 * @should match two word search to any name part
	 * @should match three word search to any name part
	 * @should match search to familyName2
	 */
	// TODO: make gender a (definable?) constant 
	@Transactional(readOnly = true)
	@Authorized( { OpenmrsConstants.PRIV_VIEW_PERSONS })
	public Set<Person> getSimilarPeople(String nameSearch, Integer birthyear, String gender) throws APIException;
	
	/**
	 * This method is needed to limit the ability for Users/Patients to be created that are already
	 * of the other type. This method will not be needed after a Person/Patient/User refactor that
	 * is due in 1.6.
	 * 
	 * @param nameSearch string to search the person's name for
	 * @param birthyear the year of birth to restrict
	 * @param gender The gender field to search on (Typically just "M" or "F")
	 * @param personType one of person, user, or patient. If Person, any Person object is returned,
	 *            if Patient, only Persons that are Patients are returned, if User, only Persons
	 *            that are Users are returned
	 * @return Set<Person> object with all people matching criteria
	 * @throws APIException
	 * @see {@link #getSimilarPeople(String, Integer, String)}
	 * @since 1.5
	 * @should limit personType equals Patient searches to only Patients
	 * @should limit personType equals User searches to only Users
	 * @should limit return all Persons with personType equals Person
	 */
	public Set<Person> getSimilarPeople(String nameSearch, Integer birthyear, String gender, String personType)
	                                                                                                           throws APIException;
	
	/**
	 * Find a person matching the <tt>searchPhrase</tt> search string
	 * 
	 * @param searchPhrase person name to match on
	 * @param dead if true will return only dead patients, if false will return only alive patients,
	 *            if null will return both
	 * @return list of person objects matches the parameters
	 * @should match search to familyName2
	 */
	@Transactional(readOnly = true)
	@Authorized( { OpenmrsConstants.PRIV_VIEW_PERSONS })
	public List<Person> getPeople(String searchPhrase, Boolean dead) throws APIException;
	
	/**
	 * @deprecated @see #getPeople(...)
	 */
	@Transactional(readOnly = true)
	@Authorized( { OpenmrsConstants.PRIV_VIEW_PERSONS })
	public Set<Person> findPeople(String searchPhrase, boolean includeVoided) throws APIException;
	
	/**
	 * @deprecated @see #getPeople(...)
	 */
	@Transactional(readOnly = true)
	@Authorized( { OpenmrsConstants.PRIV_VIEW_PERSONS })
	public Set<Person> findPeople(String searchPhrase, boolean includeVoided, String roles) throws APIException;
	
	/**
	 * @deprecated @see #getPeople(...)
	 */
	@Transactional(readOnly = true)
	@Authorized( { OpenmrsConstants.PRIV_VIEW_PERSONS })
	public Set<Person> findPeople(String searchPhrase, boolean includeVoided, List<String> roles) throws APIException;
	
	/**
	 * Save the given person attribute type in the database
	 * 
	 * @param type
	 * @return the saved person attribute type
	 * @throws APIException
	 * @should set the date created and creator on new
	 * @should set the date changed and changed by on update
	 */
	@Authorized( { OpenmrsConstants.PRIV_MANAGE_PERSON_ATTRIBUTE_TYPES })
	public PersonAttributeType savePersonAttributeType(PersonAttributeType type) throws APIException;
	
	/**
	 * @deprecated {@link #savePersonAttributeType(PersonAttributeType)}
	 */
	@Authorized( { OpenmrsConstants.PRIV_MANAGE_PERSON_ATTRIBUTE_TYPES })
	public void createPersonAttributeType(PersonAttributeType type) throws APIException;
	
	/**
	 * @deprecated replaced by #purgePersonAttributeType(PersonAttributeType)
	 */
	@Authorized( { OpenmrsConstants.PRIV_PURGE_PERSON_ATTRIBUTE_TYPES })
	public void deletePersonAttributeType(PersonAttributeType type) throws APIException;
	
	/**
	 * Purges a PersonAttribute type from the database (cannot be undone)
	 * 
	 * @param type type to be purged from the database
	 * @throws APIException
	 */
	@Authorized( { OpenmrsConstants.PRIV_PURGE_PERSON_ATTRIBUTE_TYPES })
	public void purgePersonAttributeType(PersonAttributeType type) throws APIException;
	
	/**
	 * Effectively removes this person from the system. UserService.voidUser(person) and
	 * PatientService.voidPatient(person) are also called
	 * 
	 * @param person person to be voided
	 * @param reason reason for voiding person
	 * @return the person that was voided
	 */
	@Authorized( { OpenmrsConstants.PRIV_EDIT_PERSONS })
	public Person voidPerson(Person person, String reason) throws APIException;
	
	/**
	 * Effectively resurrects this person in the db. Unvoids the associated Patient and User as well
	 * 
	 * @param person person to be revived
	 * @return the person that was unvoided
	 */
	@Authorized( { OpenmrsConstants.PRIV_EDIT_PERSONS })
	public Person unvoidPerson(Person person) throws APIException;
	
	/**
	 * Delete a Person Attribute Type
	 * 
	 * @param attrTypeId
	 */
	@Authorized( { OpenmrsConstants.PRIV_MANAGE_PERSON_ATTRIBUTE_TYPES })
	public void deletePersonAttributeType(Integer attrTypeId) throws APIException;
	
	/**
	 * @deprecated use {@link #savePersonAttributeType(PersonAttributeType)}
	 */
	@Authorized( { OpenmrsConstants.PRIV_MANAGE_PERSON_ATTRIBUTE_TYPES })
	public void updatePersonAttributeType(PersonAttributeType type) throws APIException;
	
	/**
	 * Get all PersonAttributeTypes in the database
	 * 
	 * @see #getAllPersonAttributeTypes(boolean)
	 * @return All person attribute types including the retired ones
	 */
	@Transactional(readOnly = true)
	@Authorized( { OpenmrsConstants.PRIV_VIEW_PERSON_ATTRIBUTE_TYPES })
	public List<PersonAttributeType> getAllPersonAttributeTypes() throws APIException;
	
	/**
	 * Get all PersonAttributeTypes in the database with the option of including the retired types
	 * 
	 * @param includeRetired boolean - include retired attribute types as well?
	 * @return List<PersonAttributeType> object of all PersonAttributeTypes, possibly including
	 *         retired ones
	 */
	@Transactional(readOnly = true)
	@Authorized( { OpenmrsConstants.PRIV_VIEW_PERSON_ATTRIBUTE_TYPES })
	public List<PersonAttributeType> getAllPersonAttributeTypes(boolean includeRetired) throws APIException;
	
	/**
	 * @deprecated use {@link #getAllPersonAttributeTypes()}
	 */
	@Transactional(readOnly = true)
	@Authorized( { OpenmrsConstants.PRIV_VIEW_PERSON_ATTRIBUTE_TYPES })
	public List<PersonAttributeType> getPersonAttributeTypes() throws APIException;
	
	/**
	 * Find person attribute types matching the given parameters
	 * 
	 * @param exactName (optional) The name of type
	 * @param format (optional) The format for this type
	 * @param foreignKey (optional) The foreign key
	 * @param searchable (optional) if true only returns searchable types, if false returns only
	 *            nonsearchable and if null returns all
	 * @return list of PersonAttributeTypes matching the given parameters
	 * @throws APIException
	 */
	@Transactional(readOnly = true)
	@Authorized( { OpenmrsConstants.PRIV_VIEW_PERSON_ATTRIBUTE_TYPES })
	public List<PersonAttributeType> getPersonAttributeTypes(String exactName, String format, Integer foreignKey,
	                                                         Boolean searchable) throws APIException;
	
	/**
	 * Get the PersonAttributeType given the type's PersonAttributeTypeId
	 * 
	 * @param typeId PersonAttributeType.personAttributeTypeId to match on
	 * @return the type matching this id or null if none was found
	 */
	@Transactional(readOnly = true)
	@Authorized( { OpenmrsConstants.PRIV_VIEW_PERSON_ATTRIBUTE_TYPES })
	public PersonAttributeType getPersonAttributeType(Integer typeId) throws APIException;
	
	/**
	 * Get PersonAttributeType by its UUID
	 * 
	 * @param uuid
	 * @return
	 * @should find object given valid uuid
	 * @should return null if no object found with given uuid 
	 */
	@Transactional(readOnly = true)
	public PersonAttributeType getPersonAttributeTypeByUuid(String uuid);
	
	/**
	 * Get a PersonAttribute from the database with the given PersonAttributeid
	 * 
	 * @param id the PersonAttribute.personAttributeId to match on
	 * @return the matching PersonAttribute or null if none was found
	 * @throws APIException
	 */
	@Transactional(readOnly = true)
	@Authorized( { OpenmrsConstants.PRIV_VIEW_PERSON_ATTRIBUTE_TYPES })
	public PersonAttribute getPersonAttribute(Integer id) throws APIException;
	
	/**
	 * Get the PersonAttributeType given the type's name
	 * 
	 * @param typeName
	 * @return the PersonAttributeType that has the given name or null if none found
	 */
	@Transactional(readOnly = true)
	@Authorized( { OpenmrsConstants.PRIV_VIEW_PERSON_ATTRIBUTE_TYPES })
	public PersonAttributeType getPersonAttributeTypeByName(String typeName) throws APIException;
	
	/**
	 * @deprecated use {@link #getPersonAttributeTypeByName(String)}
	 */
	@Transactional(readOnly = true)
	@Authorized( { OpenmrsConstants.PRIV_VIEW_PERSON_ATTRIBUTE_TYPES })
	public PersonAttributeType getPersonAttributeType(String typeName) throws APIException;
	
	/**
	 * Get relationship by internal relationship identifier
	 * 
	 * @param relationshipId
	 * @return Relationship the relationship to match on or null if none found
	 * @throws APIException
	 */
	@Transactional(readOnly = true)
	@Authorized( { OpenmrsConstants.PRIV_VIEW_RELATIONSHIPS })
	public Relationship getRelationship(Integer relationshipId) throws APIException;
	
	/**
	 * Get Relationship by its UUID
	 * 
	 * @param uuid
	 * @return
	 * @should find object given valid uuid
	 * @should return null if no object found with given uuid 
	 */
	@Transactional(readOnly = true)
	public Relationship getRelationshipByUuid(String uuid) throws APIException;
	
	/**
	 * Get list of relationships that are not voided
	 * 
	 * @return non-voided Relationship list
	 * @throws APIException
	 */
	@Transactional(readOnly = true)
	@Authorized( { OpenmrsConstants.PRIV_VIEW_RELATIONSHIPS })
	public List<Relationship> getAllRelationships() throws APIException;
	
	/**
	 * Get list of relationships optionally including the voided ones or not
	 * 
	 * @param includeVoided true/false whether to include the voided relationships
	 * @return non-voided Relationship list
	 * @throws APIException
	 */
	@Transactional(readOnly = true)
	@Authorized( { OpenmrsConstants.PRIV_VIEW_RELATIONSHIPS })
	public List<Relationship> getAllRelationships(boolean includeVoided) throws APIException;
	
	/**
	 * @deprecated use {@link #getAllRelationships()}
	 */
	@Transactional(readOnly = true)
	@Authorized( { OpenmrsConstants.PRIV_VIEW_RELATIONSHIPS })
	public List<Relationship> getRelationships() throws APIException;
	
	/**
	 * Get list of relationships that include Person in person_id or relative_id Does not include
	 * voided relationships
	 * 
	 * @param p person object listed on either side of the relationship
	 * @return Relationship list
	 * @throws APIException
	 * @should only get unvoided relationships
	 */
	@Transactional(readOnly = true)
	@Authorized( { OpenmrsConstants.PRIV_VIEW_RELATIONSHIPS })
	public List<Relationship> getRelationshipsByPerson(Person p) throws APIException;
	
	/**
	 * @deprecated use {@link #getRelationshipsByPerson(Person)}
	 */
	@Transactional(readOnly = true)
	@Authorized( { OpenmrsConstants.PRIV_VIEW_RELATIONSHIPS })
	public List<Relationship> getRelationships(Person p, boolean showVoided) throws APIException;
	
	/**
	 * @deprecated use {@link #getRelationshipsByPerson(Person)}
	 */
	@Transactional(readOnly = true)
	@Authorized( { OpenmrsConstants.PRIV_VIEW_RELATIONSHIPS })
	public List<Relationship> getRelationships(Person p) throws APIException;
	
	/**
	 * @deprecated use {@link #getRelationships(...)}
	 */
	@Transactional(readOnly = true)
	@Authorized( { OpenmrsConstants.PRIV_VIEW_RELATIONSHIPS })
	public List<Relationship> getRelationshipsTo(Person toPerson, RelationshipType relType) throws APIException;
	
	/**
	 * Get relationships stored in the database that
	 * 
	 * @param fromPerson (optional) Person to in the person_id column
	 * @param toPerson (optional) Person in the relative_id column
	 * @param relType (optional) The RelationshipType to match
	 * @return relationships matching the given parameters
	 * @throws APIException
	 */
	@Transactional(readOnly = true)
	@Authorized( { OpenmrsConstants.PRIV_VIEW_RELATIONSHIPS })
	public List<Relationship> getRelationships(Person fromPerson, Person toPerson, RelationshipType relType)
	                                                                                                        throws APIException;
	
	/**
	 * Get all relationshipTypes Includes retired relationship types
	 * 
	 * @return relationshipType list
	 * @throws APIException
	 */
	@Transactional(readOnly = true)
	@Authorized( { OpenmrsConstants.PRIV_VIEW_RELATIONSHIP_TYPES })
	public List<RelationshipType> getAllRelationshipTypes() throws APIException;
	
	/**
	 * @deprecated use {@link #getAllRelationshipTypes()}
	 */
	@Transactional(readOnly = true)
	public List<RelationshipType> getRelationshipTypes() throws APIException;
	
	/**
	 * Get relationshipType by internal identifier
	 * 
	 * @param relationshipTypeId
	 * @return relationshipType with given internal identifier or null if none found
	 * @throws APIException
	 */
	@Transactional(readOnly = true)
	@Authorized( { OpenmrsConstants.PRIV_VIEW_RELATIONSHIP_TYPES })
	public RelationshipType getRelationshipType(Integer relationshipTypeId) throws APIException;
	
	/**
	 * Get RelationshipType by its UUID
	 * 
	 * @param uuid
	 * @return
	 * @should find object given valid uuid
	 * @should return null if no object found with given uuid 
	 */
	@Transactional(readOnly = true)
	public RelationshipType getRelationshipTypeByUuid(String uuid) throws APIException;
	
	/**
	 * @deprecated use {@link #getRelationshipTypeByName(String)}
	 */
	@Transactional(readOnly = true)
	public RelationshipType findRelationshipType(String relationshipTypeName) throws APIException;
	
	/**
	 * Find relationshipType by exact name match
	 * 
	 * @param relationshipTypeName name to match on
	 * @return RelationshipType with given name or null if none found
	 * @throws APIException
	 */
	@Transactional(readOnly = true)
	@Authorized( { OpenmrsConstants.PRIV_VIEW_RELATIONSHIP_TYPES })
	public RelationshipType getRelationshipTypeByName(String relationshipTypeName) throws APIException;
	
	/**
	 * Find relationshipTypes by exact name match and/or preferred status
	 * 
	 * @param relationshipTypeName name to match on
	 * @param preferred if true, returns on preferred types, if false returns only the nonpreferred
	 *            types. if null returns both
	 * @return RelationshipTypes with given name and preferred status
	 * @throws APIException
	 */
	@Transactional(readOnly = true)
	@Authorized( { OpenmrsConstants.PRIV_VIEW_RELATIONSHIP_TYPES })
	public List<RelationshipType> getRelationshipTypes(String relationshipTypeName, Boolean preferred) throws APIException;
	
	/**
	 * Get relationshipTypes by searching through the names and loosely matching to the given
	 * searchString
	 * 
	 * @param searchString string to match to a relationship type name
	 * @return list of relationship types or empty list if none found
	 * @throws APIException
	 */
	@Transactional(readOnly = true)
	@Authorized( { OpenmrsConstants.PRIV_VIEW_RELATIONSHIP_TYPES })
	public List<RelationshipType> getRelationshipTypes(String searchString) throws APIException;
	
	/**
	 * @deprecated replaced by #saveRelationship(Relationship)
	 */
	@Authorized( { OpenmrsConstants.PRIV_ADD_RELATIONSHIPS })
	public void createRelationship(Relationship relationship) throws APIException;
	
	/**
	 * Create or update a relationship between people. Saves the given <code>relationship</code> to
	 * the database
	 * 
	 * @param relationship relationship to be created or updated
	 * @return relationship that was created or updated
	 * @throws APIException
	 */
	@Authorized( { OpenmrsConstants.PRIV_ADD_RELATIONSHIPS, OpenmrsConstants.PRIV_EDIT_RELATIONSHIPS })
	public Relationship saveRelationship(Relationship relationship) throws APIException;
	
	/**
	 * @deprecated replaced by #saveRelationship(Relationship)
	 */
	@Authorized( { OpenmrsConstants.PRIV_EDIT_RELATIONSHIPS })
	public void updateRelationship(Relationship relationship) throws APIException;
	
	/**
	 * @deprecated replaced by #purgeRelationship(Relationship)
	 */
	@Authorized( { OpenmrsConstants.PRIV_PURGE_RELATIONSHIPS })
	public void deleteRelationship(Relationship relationship) throws APIException;
	
	/**
	 * Purges a relationship from the database (cannot be undone)
	 * 
	 * @param relationship relationship to be purged from the database
	 * @throws APIException
	 */
	@Authorized( { OpenmrsConstants.PRIV_PURGE_RELATIONSHIPS })
	public void purgeRelationship(Relationship relationship) throws APIException;
	
	/**
	 * Voids the given Relationship, effectively removing it from openmrs.
	 * 
	 * @param relationship Relationship to void
	 * @param voidReason String reason the relationship is being voided.
	 * @return the newly saved relationship
	 * @throws APIException
	 */
	@Authorized( { OpenmrsConstants.PRIV_DELETE_RELATIONSHIPS })
	public Relationship voidRelationship(Relationship relationship, String voidReason) throws APIException;
	
	/**
	 * Unvoid Relationship in the database, effectively marking this as a valid relationship again
	 * 
	 * @param relationship Relationship to unvoid
	 * @return the newly unvoided relationship
	 * @throws APIException
	 */
	@Authorized( { OpenmrsConstants.PRIV_EDIT_RELATIONSHIPS })
	public Relationship unvoidRelationship(Relationship relationship) throws APIException;
	
	/**
	 * @deprecated replaced by #savePerson(Person)
	 */
	@Authorized( { OpenmrsConstants.PRIV_ADD_PERSONS })
	public Person createPerson(Person person) throws APIException;
	
	/**
	 * Creates or updates a Person in the database
	 * 
	 * @param person person to be created or updated
	 * @return person who was created or updated
	 * @throws APIException
	 */
	@Authorized( { OpenmrsConstants.PRIV_ADD_PERSONS, OpenmrsConstants.PRIV_EDIT_PERSONS })
	public Person savePerson(Person person) throws APIException;
	
	/**
	 * @deprecated use {@link #savePerson(Person)}
	 */
	@Authorized( { OpenmrsConstants.PRIV_EDIT_PERSONS })
	public void updatePerson(Person person) throws APIException;
	
	/**
	 * @deprecated replaced by #purgePerson(Person)
	 */
	@Authorized( { OpenmrsConstants.PRIV_PURGE_PERSONS })
	public void deletePerson(Person person) throws APIException;
	
	/**
	 * Purges a person from the database (cannot be undone)
	 * 
	 * @param person person to be purged from the database
	 * @throws APIException
	 */
	@Authorized( { OpenmrsConstants.PRIV_PURGE_PERSONS })
	public void purgePerson(Person person) throws APIException;
	
	/**
	 * Get Person by its UUID
	 * 
	 * @param uuid
	 * @return
	 * @should find object given valid uuid
	 * @should return null if no object found with given uuid 
	 */
	@Transactional(readOnly = true)
	public Person getPersonByUuid(String uuid) throws APIException;
	
	/**
	 * Get PersonAddress by its UUID
	 * 
	 * @param uuid
	 * @return
	 * @should find object given valid uuid
	 * @should return null if no object found with given uuid 
	 */
	@Transactional(readOnly = true)
	public PersonAddress getPersonAddressByUuid(String uuid) throws APIException;
	
	/**
	 * Get PersonAttribute by its UUID
	 * 
	 * @param uuid
	 * @return
	 * @should find object given valid uuid
	 * @should return null if no object found with given uuid 
	 */
	@Transactional(readOnly = true)
	public PersonAttribute getPersonAttributeByUuid(String uuid) throws APIException;
	
	/**
	 * Get PersonName by its UUID
	 * 
	 * @param uuid
	 * @return
	 * @should find object given valid uuid
	 * @should return null if no object found with given uuid 
	 */
	@Transactional(readOnly = true)
	public PersonName getPersonNameByUuid(String uuid) throws APIException;
	
	/**
	 * Gets a person by internal id
	 * 
	 * @param personId internal identifier of person to get
	 * @return Person person with given internal identifier
	 * @throws APIException
	 */
	@Transactional(readOnly = true)
	@Authorized( { OpenmrsConstants.PRIV_VIEW_PERSONS })
	public Person getPerson(Integer personId) throws APIException;
	
	/**
	 * Use either {@link #getPerson(Integer)} passing in pat.getPatientId() or just cast the pat
	 * object to a Person object because Patient is a subclass of Person. (Person)pat
	 * 
	 * @deprecated use {@link #getPerson(Integer)}
	 */
	@Transactional(readOnly = true)
	@Authorized( { OpenmrsConstants.PRIV_VIEW_PERSONS })
	public Person getPerson(Patient pat) throws APIException;
	
	/**
	 * Use either {@link #getPerson(Integer)} passing in user.getUserId() or just cast the user
	 * object to a Person object because User is a subclass of Person. (Person)user
	 * 
	 * @deprecated use {@link #getPerson(Integer)}
	 */
	@Transactional(readOnly = true)
	@Authorized( { OpenmrsConstants.PRIV_VIEW_PERSONS })
	public Person getPerson(User user) throws APIException;
	
	/**
	 * @deprecated replaced with #saveRelationshipType(RelationshipType)
	 */
	@Authorized( { OpenmrsConstants.PRIV_MANAGE_RELATIONSHIP_TYPES })
	public void createRelationshipType(RelationshipType relationshipType) throws APIException;
	
	/**
	 * Inserts or updates the given relationship type object in the database
	 * 
	 * @param relationshipType type to be created or updated
	 * @return relationship type that was created or updated
	 * @throws APIException
	 */
	@Authorized( { OpenmrsConstants.PRIV_MANAGE_RELATIONSHIP_TYPES })
	public RelationshipType saveRelationshipType(RelationshipType relationshipType) throws APIException;
	
	/**
	 * @deprecated replaced by #saveRelationshipType(RelationshipType)
	 */
	@Authorized( { OpenmrsConstants.PRIV_MANAGE_RELATIONSHIP_TYPES })
	public void updateRelationshipType(RelationshipType relationshipType) throws APIException;
	
	/**
	 * @deprecated replaced by #purgeRelationshipType(RelationshipType)
	 */
	@Authorized( { OpenmrsConstants.PRIV_PURGE_RELATIONSHIP_TYPES })
	public void deleteRelationshipType(RelationshipType relationshipType) throws APIException;
	
	/**
	 * Purge relationship type from the database (cannot be undone)
	 * 
	 * @param relationshipType relationship type to be purged
	 * @throws APIException
	 */
	@Authorized( { OpenmrsConstants.PRIV_PURGE_RELATIONSHIP_TYPES })
	public void purgeRelationshipType(RelationshipType relationshipType) throws APIException;
	
	/**
	 * Gets the types defined for the given person type (person, user, patient) and the given type
	 * of view (one person vs many person objects)
	 * 
	 * @param personType PERSON, USER, PATIENT, or null. Both PERSON and null mean to return attr
	 *            types for both patients and users
	 * @param viewType whether this is a listing or viewing or null for both listing and viewing
	 * @return list of PersonAttributeTypes that should be displayed
	 */
	@Transactional(readOnly = true)
	@Authorized( { OpenmrsConstants.PRIV_VIEW_PERSON_ATTRIBUTE_TYPES })
	public List<PersonAttributeType> getPersonAttributeTypes(PERSON_TYPE personType, ATTR_VIEW_TYPE viewType)
	                                                                                                         throws APIException;
	
	/**
	 * @deprecated use {@link #getPersonAttributeTypes(PERSON_TYPE, ATTR_VIEW_TYPE)}
	 */
	@Transactional(readOnly = true)
	@Authorized( { OpenmrsConstants.PRIV_VIEW_PERSON_ATTRIBUTE_TYPES })
	public List<PersonAttributeType> getPersonAttributeTypes(String personType, String viewType) throws APIException;
	
	/**
	 * Splits the <code>name</code> string into Given, Middle, and Family parts of a PersonName
	 * 
	 * @param name
	 * @return fully formed PersonName object
	 * @see #parsePersonName(String)
	 * @deprecated replaced by parsePersonName(String)
	 */
	public PersonName splitPersonName(String name);
	
	/**
	 * Parses a name into a PersonName (separate Given, Middle, and Family names)
	 * 
	 * @param name person name to be parsed
	 * @return parsed person name
	 * @should parse two person name with comma
	 * @should parse two person name without comma
	 */
	public PersonName parsePersonName(String name) throws APIException;
	
	/**
	 * Get all relationships for a given type of relationship mapped from the personA to all of the
	 * personB's
	 * 
	 * @param relationshipType type of relationship for which to retrieve all relationships
	 * @return all relationships for the given type of relationship
	 * @throws APIException
	 */
	@Transactional(readOnly = true)
	@Authorized( { OpenmrsConstants.PRIV_VIEW_RELATIONSHIPS })
	public Map<Person, List<Person>> getRelationshipMap(RelationshipType relationshipType) throws APIException;
	
	/**
	 * @deprecated use {@link #getRelationshipMap(RelationshipType)}
	 */
	@Transactional(readOnly = true)
	public Map<Person, List<Person>> getRelationships(RelationshipType relationshipType) throws APIException;
}
