/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openmrs.Person;
import org.openmrs.PersonAddress;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonAttributeType;
import org.openmrs.PersonName;
import org.openmrs.Relationship;
import org.openmrs.RelationshipType;
import org.openmrs.annotation.Authorized;
import org.openmrs.api.db.PersonDAO;
import org.openmrs.person.PersonMergeLog;
import org.openmrs.serialization.SerializationException;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.OpenmrsConstants.PERSON_TYPE;
import org.openmrs.util.PrivilegeConstants;

/**
 * Contains methods pertaining to Persons in the system Use:<br>
 * 
 * <pre>
 * 
 * 
 * 
 * 
 * List&lt;Person&gt; personObjects = Context.getPersonService().getAllPersons();
 * </pre>
 * 
 * @see org.openmrs.api.context.Context
 * @see org.openmrs.Patient
 */
public interface PersonService extends OpenmrsService {
	
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
	 * @return Set&lt;Person&gt; object with all people matching criteria
	 * @throws APIException
	 * @should accept greater than three names
	 * @should match single search to any name part
	 * @should match two word search to any name part
	 * @should match three word search to any name part
	 * @should match search to familyName2
	 */
	// TODO: make gender a (definable?) constant
	@Authorized( { PrivilegeConstants.GET_PERSONS })
	public Set<Person> getSimilarPeople(String nameSearch, Integer birthyear, String gender) throws APIException;
		
	/**
	 * Find a person matching the <tt>searchPhrase</tt> search string
	 * 
	 * @param searchPhrase person name to match on
	 * @param dead if true will return only dead patients, if false will return only alive patients,
	 *            if null will return both
	 * @return list of person objects matches the parameters
	 * @should match search to familyName2
	 */
	@Authorized( { PrivilegeConstants.GET_PERSONS })
	public List<Person> getPeople(String searchPhrase, Boolean dead) throws APIException;
	
	@Authorized( { PrivilegeConstants.GET_PERSONS })
	public List<Person> getPeople(String searchPhrase, Boolean dead, Boolean voided) throws APIException;
		
	/**
	 * Save the given person attribute type in the database. <br>
	 * If the given type's Id is not empty, then also need to change any global property which is in
	 * {@link OpenmrsConstants#GLOBAL_PROPERTIES_OF_PERSON_ATTRIBUTES} and reference this given
	 * type, prior to saving this given type. <br>
	 * 
	 * @param type
	 * @return the saved person attribute type
	 * @throws APIException
	 * @should set the date created and creator on new
	 * @should set the date changed and changed by on update
	 * @should update any global property which reference this type
	 * @should throw an error when trying to save person attribute type while person attribute types are locked
	 */
	@Authorized( { PrivilegeConstants.MANAGE_PERSON_ATTRIBUTE_TYPES })
	public PersonAttributeType savePersonAttributeType(PersonAttributeType type) throws APIException;
	
	/**
	 * Retire a Person Attribute Type
	 * 
	 * @param type
	 * @param retiredReason
	 * @should throw an error when trying to retire person attribute type while person attribute types are locked
	 */
	@Authorized( { PrivilegeConstants.MANAGE_PERSON_ATTRIBUTE_TYPES })
	public PersonAttributeType retirePersonAttributeType(PersonAttributeType type, String retiredReason) throws APIException;
	
	/**
	 * Retire a Person Relationship Type
	 * 
	 * @param type
	 * @param retiredReason
	 */
	@Authorized( { PrivilegeConstants.MANAGE_RELATIONSHIP_TYPES })
	public RelationshipType retireRelationshipType(RelationshipType type, String retiredReason) throws APIException;
	
	/**
	 * Unretire a Person Relationship Type
	 * 
	 * @param relationshipType retiredReason
	 * @since 1.9
	 */
	@Authorized( { PrivilegeConstants.MANAGE_RELATIONSHIP_TYPES })
	public RelationshipType unretireRelationshipType(RelationshipType relationshipType);
	
	/**
	 * Purges a PersonAttribute type from the database (cannot be undone)
	 * 
	 * @param type type to be purged from the database
	 * @throws APIException
	 * @should delete person attribute type from database
	 * @should throw an error when trying to delete person attribute type while person attribute types are locked
	 */
	@Authorized( { PrivilegeConstants.PURGE_PERSON_ATTRIBUTE_TYPES })
	public void purgePersonAttributeType(PersonAttributeType type) throws APIException;
	
	/**
	 * Unretires a PersonAttribute type from the database (can be undone)
	 * 
	 * @param type type to be restored from the database
	 * @throws APIException
	 * @should restore person attribute type from database
	 * @should throw an error when trying to unretire person attribute type while person attribute types are locked
	 */
	
	@Authorized( { PrivilegeConstants.MANAGE_PERSON_ATTRIBUTE_TYPES })
	public void unretirePersonAttributeType(PersonAttributeType type) throws APIException;
	
	/**
	 * Effectively removes this person from the system. Voids Patient and retires Users as well.
	 * 
	 * @param person person to be voided
	 * @param reason reason for voiding person
	 * @return the person that was voided
	 * @should return voided person with given reason
	 * @should void patient
	 * @should retire users
	 */
	@Authorized( { PrivilegeConstants.EDIT_PERSONS })
	public Person voidPerson(Person person, String reason) throws APIException;
	
	/**
	 * Effectively resurrects this person in the db. Unvoids Patient as well.
	 * 
	 * @param person person to be revived
	 * @return the person that was unvoided
	 * @should unvoid the given person
	 * @should unvoid patient
	 * @should not unretire users
	 */
	@Authorized( { PrivilegeConstants.EDIT_PERSONS })
	public Person unvoidPerson(Person person) throws APIException;
		
	/**
	 * Get all PersonAttributeTypes in the database
	 * 
	 * @see #getAllPersonAttributeTypes(boolean)
	 * @return All person attribute types including the retired ones
	 * @should return all person attribute types including retired
	 */
	@Authorized( { PrivilegeConstants.GET_PERSON_ATTRIBUTE_TYPES })
	public List<PersonAttributeType> getAllPersonAttributeTypes() throws APIException;
	
	/**
	 * Get all PersonAttributeTypes in the database with the option of including the retired types
	 * 
	 * @param includeRetired boolean - include retired attribute types as well?
	 * @return List&lt;PersonAttributeType&gt; object of all PersonAttributeTypes, possibly including
	 *         retired ones
	 * @should return all person attribute types including retired when include retired is true
	 * @should return all person attribute types excluding retired when include retired is false
	 */
	@Authorized( { PrivilegeConstants.GET_PERSON_ATTRIBUTE_TYPES })
	public List<PersonAttributeType> getAllPersonAttributeTypes(boolean includeRetired) throws APIException;
	
	/**
	 * Find person attribute types matching the given parameters. Retired types are included in the
	 * results
	 * 
	 * @param exactName (optional) The name of type
	 * @param format (optional) The format for this type
	 * @param foreignKey (optional) The foreign key
	 * @param searchable (optional) if true only returns searchable types, if false returns only
	 *            nonsearchable and if null returns all
	 * @return list of PersonAttributeTypes matching the given parameters
	 * @throws APIException
	 * @should return person attribute types matching given parameters
	 * @should return empty list when no person attribute types match given parameters
	 */
	@Authorized( { PrivilegeConstants.GET_PERSON_ATTRIBUTE_TYPES })
	public List<PersonAttributeType> getPersonAttributeTypes(String exactName, String format, Integer foreignKey,
	        Boolean searchable) throws APIException;
	
	/**
	 * Get the PersonAttributeType given the type's PersonAttributeTypeId
	 * 
	 * @param typeId PersonAttributeType.personAttributeTypeId to match on
	 * @return the type matching this id or null if none was found
	 * @should return null when no person attribute with the given id exist
	 */
	@Authorized( { PrivilegeConstants.GET_PERSON_ATTRIBUTE_TYPES })
	public PersonAttributeType getPersonAttributeType(Integer typeId) throws APIException;
	
	/**
	 * Gets a person attribute type with the given uuid.
	 * 
	 * @param uuid the universally unique identifier to lookup
	 * @return a person attribute type with the given uuid
	 * @should find object given valid uuid
	 * @should return null if no object found with given uuid
	 */
	@Authorized( { PrivilegeConstants.GET_PERSON_ATTRIBUTE_TYPES })
	public PersonAttributeType getPersonAttributeTypeByUuid(String uuid);
	
	/**
	 * Get a PersonAttribute from the database with the given PersonAttributeid
	 * 
	 * @param id the PersonAttribute.personAttributeId to match on
	 * @return the matching PersonAttribute or null if none was found
	 * @throws APIException
	 * @should return null when PersonAttribute with given id does not exist
	 * @should return person attribute when PersonAttribute with given id does exist
	 */
	@Authorized( { PrivilegeConstants.GET_PERSON_ATTRIBUTE_TYPES })
	public PersonAttribute getPersonAttribute(Integer id) throws APIException;
	
	/**
	 * Get the PersonAttributeType given the type's name
	 * 
	 * @param typeName
	 * @return the PersonAttributeType that has the given name or null if none found
	 * @should return person attribute type when name matches given typeName
	 * @should return null when no person attribute type match given typeName
	 */
	@Authorized( { PrivilegeConstants.GET_PERSON_ATTRIBUTE_TYPES })
	public PersonAttributeType getPersonAttributeTypeByName(String typeName) throws APIException;
	
	/**
	 * Get relationship by internal relationship identifier
	 * 
	 * @param relationshipId
	 * @return Relationship the relationship to match on or null if none found
	 * @throws APIException
	 * @should return relationship with given id
	 * @should return null when relationship with given id does not exist
	 */
	@Authorized( { PrivilegeConstants.GET_RELATIONSHIPS })
	public Relationship getRelationship(Integer relationshipId) throws APIException;
	
	/**
	 * Get Relationship by its UUID
	 * 
	 * @param uuid
	 * @return relationship or null
	 * @should find object given valid uuid
	 * @should return null if no object found with given uuid
	 */
	@Authorized( { PrivilegeConstants.GET_RELATIONSHIPS })
	public Relationship getRelationshipByUuid(String uuid) throws APIException;
	
	/**
	 * Get list of relationships that are not voided
	 * 
	 * @return non-voided Relationship list
	 * @throws APIException
	 * @return list of all unvoided relationship
	 * @should return all unvoided relationships
	 */
	@Authorized( { PrivilegeConstants.GET_RELATIONSHIPS })
	public List<Relationship> getAllRelationships() throws APIException;
	
	/**
	 * Get list of relationships optionally including the voided ones or not
	 * 
	 * @param includeVoided true/false whether to include the voided relationships
	 * @return non-voided Relationship list
	 * @throws APIException
	 * @should return all relationship including voided when include voided equals true
	 * @should return all relationship excluding voided when include voided equals false
	 */
	@Authorized( { PrivilegeConstants.GET_RELATIONSHIPS })
	public List<Relationship> getAllRelationships(boolean includeVoided) throws APIException;
		
	/**
	 * Get list of relationships that include Person in person_id or relative_id Does not include
	 * voided relationships
	 * 
	 * @param p person object listed on either side of the relationship
	 * @return Relationship list
	 * @throws APIException
	 * @should only get unvoided relationships
	 * @should fetch relationships associated with the given person
	 * @should fetch unvoided relationships only
	 */
	@Authorized( { PrivilegeConstants.GET_RELATIONSHIPS })
	public List<Relationship> getRelationshipsByPerson(Person p) throws APIException;
	
	/**
	 * Get list of relationships that include Person in person_id or relative_id. Does not include
	 * voided relationships. Accepts an effectiveDate parameter which, if supplied, will limit the
	 * returned relationships to those that were active on the given date. Such active relationships
	 * include those that have a startDate that is null or less than or equal to the effectiveDate,
	 * and that have an endDate that is null or greater than or equal to the effectiveDate.
	 * 
	 * @param p person object listed on either side of the relationship
	 * @param effectiveDate effective date of relationship
	 * @return Relationship list
	 * @throws APIException
	 * @should only get unvoided relationships
	 * @should only get unvoided relationships regardless of effective date
	 * @should fetch relationships associated with the given person
	 * @should fetch relationships that were active during effectiveDate
	 */
	@Authorized( { PrivilegeConstants.GET_RELATIONSHIPS })
	public List<Relationship> getRelationshipsByPerson(Person p, Date effectiveDate) throws APIException;
		
	/**
	 * Get relationships stored in the database that
	 * 
	 * @param fromPerson (optional) Person to in the person_id column
	 * @param toPerson (optional) Person in the relative_id column
	 * @param relType (optional) The RelationshipType to match
	 * @return relationships matching the given parameters
	 * @throws APIException
	 * @should fetch relationships matching the given from person
	 * @should fetch relationships matching the given to person
	 * @should fetch relationships matching the given rel type
	 * @should return empty list when no relationship matching given parameters exist
	 */
	@Authorized( { PrivilegeConstants.GET_RELATIONSHIPS })
	public List<Relationship> getRelationships(Person fromPerson, Person toPerson, RelationshipType relType)
	        throws APIException;
	
	/**
	 * Get relationships stored in the database that are active on the passed date
	 * 
	 * @param fromPerson (optional) Person to in the person_id column
	 * @param toPerson (optional) Person in the relative_id column
	 * @param relType (optional) The RelationshipType to match
	 * @param effectiveDate (optional) The date during which the relationship was effective
	 * @return relationships matching the given parameters
	 * @throws APIException
	 * @should fetch relationships matching the given from person
	 * @should fetch relationships matching the given to person
	 * @should fetch relationships matching the given rel type
	 * @should return empty list when no relationship matching given parameters exist
	 * @should fetch relationships that were active during effectiveDate
	 */
	@Authorized( { PrivilegeConstants.GET_RELATIONSHIPS })
	public List<Relationship> getRelationships(Person fromPerson, Person toPerson, RelationshipType relType,
	        Date effectiveDate) throws APIException;
	
	/**
	 * Get relationships stored in the database that were active during the specified date range
	 * 
	 * @param fromPerson (optional) Person to in the person_id column
	 * @param toPerson (optional) Person in the relative_id column
	 * @param relType (optional) The RelationshipType to match
	 * @param startEffectiveDate (optional) The date during which the relationship was effective
	 *            (lower bound)
	 * @param endEffectiveDate (optional) The date during which the relationship was effective
	 *            (upper bound)
	 * @return relationships matching the given parameters
	 * @throws APIException
	 * @should fetch relationships matching the given from person
	 * @should fetch relationships matching the given to person
	 * @should fetch relationships matching the given rel type
	 * @should return empty list when no relationship matching given parameters exist
	 * @should fetch relationships that were active during the specified date range
	 */
	@Authorized( { PrivilegeConstants.GET_RELATIONSHIPS })
	public List<Relationship> getRelationships(Person fromPerson, Person toPerson, RelationshipType relType,
	        Date startEffectiveDate, Date endEffectiveDate) throws APIException;
	
	/**
	 * Get all relationshipTypes Includes retired relationship types
	 * 
	 * @return relationshipType list
	 * @throws APIException
	 * @should return all relationship types
	 */
	@Authorized( { PrivilegeConstants.GET_RELATIONSHIP_TYPES })
	public List<RelationshipType> getAllRelationshipTypes() throws APIException;
	
	/**
	 * Get all relationshipTypes with the option of including the retired types
	 * 
	 * @param includeRetired boolean - include retired relationshipTypes as well?
	 * @return relationshipType list
	 * @throws APIException
	 */
	@Authorized( { PrivilegeConstants.GET_RELATIONSHIP_TYPES })
	public List<RelationshipType> getAllRelationshipTypes(boolean includeRetired) throws APIException;
	
	/**
	 * Get relationshipType by internal identifier
	 * 
	 * @param relationshipTypeId
	 * @return relationshipType with given internal identifier or null if none found
	 * @throws APIException
	 * @should return relationship type with the given relationship type id
	 * @should return null when no relationship type matches given relationship type id
	 */
	@Authorized( { PrivilegeConstants.GET_RELATIONSHIP_TYPES })
	public RelationshipType getRelationshipType(Integer relationshipTypeId) throws APIException;
	
	/**
	 * Gets the relationship type with the given uuid.
	 * 
	 * @param uuid
	 * @return relationship type or null
	 * @throws APIException
	 * @should find object given valid uuid
	 * @should return null if no object found with given uuid
	 */
	@Authorized( { PrivilegeConstants.GET_RELATIONSHIP_TYPES })
	public RelationshipType getRelationshipTypeByUuid(String uuid) throws APIException;
	
	/**
	 * Find relationshipType by exact name match
	 * 
	 * @param relationshipTypeName name to match on
	 * @return RelationshipType with given name or null if none found
	 * @throws APIException
	 * @should return null when no relationship type match the given name
	 */
	@Authorized( { PrivilegeConstants.GET_RELATIONSHIP_TYPES })
	public RelationshipType getRelationshipTypeByName(String relationshipTypeName) throws APIException;
	
	/**
	 * Find relationshipTypes by exact name match and/or preferred status
	 * 
	 * @param relationshipTypeName name to match on
	 * @param preferred if true, returns on preferred types, if false returns only the nonpreferred
	 *            types. if null returns both
	 * @return RelationshipTypes with given name and preferred status
	 * @throws APIException
	 * @should return list of preferred relationship type matching given name
	 * @should return empty list when no preferred relationship type match the given name
	 */
	@Authorized( { PrivilegeConstants.GET_RELATIONSHIP_TYPES })
	public List<RelationshipType> getRelationshipTypes(String relationshipTypeName, Boolean preferred) throws APIException;
	
	/**
	 * Get relationshipTypes by searching through the names and loosely matching to the given
	 * searchString
	 * 
	 * @param searchString string to match to a relationship type name
	 * @return list of relationship types or empty list if none found
	 * @throws APIException
	 * @should return empty list when no relationship type match the search string
	 */
	@Authorized( { PrivilegeConstants.GET_RELATIONSHIP_TYPES })
	public List<RelationshipType> getRelationshipTypes(String searchString) throws APIException;
	
	/**
	 * Create or update a relationship between people. Saves the given <code>relationship</code> to
	 * the database
	 * 
	 * @param relationship relationship to be created or updated
	 * @return relationship that was created or updated
	 * @throws APIException
	 * @should create new object when relationship id is null
	 * @should update existing object when relationship id is not null
	 */
	@Authorized( { PrivilegeConstants.ADD_RELATIONSHIPS, PrivilegeConstants.EDIT_RELATIONSHIPS })
	public Relationship saveRelationship(Relationship relationship) throws APIException;
	
	/**
	 * Purges a relationship from the database (cannot be undone)
	 * 
	 * @param relationship relationship to be purged from the database
	 * @throws APIException
	 * @should delete relationship from the database
	 */
	@Authorized( { PrivilegeConstants.PURGE_RELATIONSHIPS })
	public void purgeRelationship(Relationship relationship) throws APIException;
	
	/**
	 * Voids the given Relationship, effectively removing it from openmrs.
	 * 
	 * @param relationship Relationship to void
	 * @param voidReason String reason the relationship is being voided.
	 * @return the newly saved relationship
	 * @throws APIException
	 * @should void relationship with the given reason
	 */
	@Authorized( { PrivilegeConstants.DELETE_RELATIONSHIPS })
	public Relationship voidRelationship(Relationship relationship, String voidReason) throws APIException;
	
	/**
	 * Unvoid Relationship in the database, effectively marking this as a valid relationship again
	 * 
	 * @param relationship Relationship to unvoid
	 * @return the newly unvoided relationship
	 * @throws APIException
	 * @should unvoid voided relationship
	 */
	@Authorized( { PrivilegeConstants.EDIT_RELATIONSHIPS })
	public Relationship unvoidRelationship(Relationship relationship) throws APIException;
	
	/**
	 * Creates or updates a Person in the database
	 * 
	 * @param person person to be created or updated
	 * @return person who was created or updated
	 * @throws APIException
	 * @should create new object when person id is null
	 * @should update existing object when person id is not null
	 * @should set the preferred name and address if none is specified
	 * @should not set the preferred name and address if they already exist
	 * @should not set a voided name or address as preferred
	 */
	@Authorized( { PrivilegeConstants.ADD_PERSONS, PrivilegeConstants.EDIT_PERSONS })
	public Person savePerson(Person person) throws APIException;
	
	/**
	 * Purges a person from the database (cannot be undone)
	 * 
	 * @param person person to be purged from the database
	 * @throws APIException
	 * @should delete person from the database
	 */
	@Authorized( { PrivilegeConstants.PURGE_PERSONS })
	public void purgePerson(Person person) throws APIException;
	
	/**
	 * Get Person by its UUID
	 * 
	 * @param uuid
	 * @return person or null
	 * @should find object given valid uuid
	 * @should return null if no object found with given uuid
	 */
	@Authorized( { PrivilegeConstants.GET_PERSONS })
	public Person getPersonByUuid(String uuid) throws APIException;
	
	/**
	 * Get PersonAddress by its UUID
	 * 
	 * @param uuid
	 * @return person address or null
	 * @should find object given valid uuid
	 * @should return null if no object found with given uuid
	 */
	@Authorized( { PrivilegeConstants.GET_PERSONS })
	public PersonAddress getPersonAddressByUuid(String uuid) throws APIException;
	
	/**
	 * Get PersonAttribute by its UUID
	 * 
	 * @param uuid
	 * @return person attribute or null
	 * @should find object given valid uuid
	 * @should return null if no object found with given uuid
	 */
	@Authorized( { PrivilegeConstants.GET_PERSONS })
	public PersonAttribute getPersonAttributeByUuid(String uuid) throws APIException;
	
	/**
	 * Get PersonName by its personNameId
	 * 
	 * @param personNameId
	 * @return person name or null
	 * @should find PersonName given valid personNameId
	 * @should return null if no object found with given personNameId
	 */
	@Authorized( { PrivilegeConstants.GET_PERSONS })
	PersonName getPersonName(Integer personNameId);
	
	/**
	 * Get PersonName by its UUID
	 * 
	 * @param uuid
	 * @return person name or null
	 * @should find object given valid uuid
	 * @should return null if no object found with given uuid
	 */
	@Authorized( { PrivilegeConstants.GET_PERSONS })
	public PersonName getPersonNameByUuid(String uuid) throws APIException;
	
	/**
	 * Gets a person by internal id
	 * 
	 * @param personId internal identifier of person to get
	 * @return Person person with given internal identifier
	 * @throws APIException
	 * @should return null when no person has the given id
	 */
	@Authorized( { PrivilegeConstants.GET_PERSONS })
	public Person getPerson(Integer personId) throws APIException;
	
	/**
	 * Inserts or updates the given relationship type object in the database
	 * 
	 * @param relationshipType type to be created or updated
	 * @return relationship type that was created or updated
	 * @throws APIException
	 * @should create new object when relationship type id is null
	 * @should update existing object when relationship type id is not null
	 * @should fail if the description is not specified
	 */
	@Authorized( { PrivilegeConstants.MANAGE_RELATIONSHIP_TYPES })
	public RelationshipType saveRelationshipType(RelationshipType relationshipType) throws APIException;
	
	/**
	 * Purge relationship type from the database (cannot be undone)
	 * 
	 * @param relationshipType relationship type to be purged
	 * @throws APIException
	 * @should delete relationship type from the database
	 */
	@Authorized( { PrivilegeConstants.PURGE_RELATIONSHIP_TYPES })
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
	// this has anonymous access because its cached into generic js files
	public List<PersonAttributeType> getPersonAttributeTypes(PERSON_TYPE personType, ATTR_VIEW_TYPE viewType)
	        throws APIException;
	
	/**
	 * Voids the given PersonName, effectively deleting the name, from the end-user's point of view.
	 * 
	 * @param personName PersonName to void
	 * @param voidReason String reason the personName is being voided.
	 * @return the newly saved personName
	 * @throws APIException
	 * @should void personName with the given reason
	 */
	@Authorized( { PrivilegeConstants.EDIT_PERSONS })
	public PersonName voidPersonName(PersonName personName, String voidReason);
	
	/**
	 * Unvoid PersonName in the database, effectively marking this as a valid personName again
	 * 
	 * @param personName PersonName to unvoid
	 * @return the newly unvoided personName
	 * @throws APIException
	 * @should unvoid voided personName
	 */
	@Authorized( { PrivilegeConstants.EDIT_PERSONS })
	public PersonName unvoidPersonName(PersonName personName) throws APIException;
	
	/**
	 * Inserts or updates the given personName object in the database
	 * 
	 * @param personName to be created or updated
	 * @return personName that was created or updated
	 * @throws APIException
	 * @should fail if you try to void the last non voided name
	 */
	@Authorized( { PrivilegeConstants.EDIT_PERSONS })
	public PersonName savePersonName(PersonName personName);
	
	/**
	 * Parses a name into a PersonName (separate Given, Middle, and Family names)
	 * 
	 * @param name person name to be parsed
	 * @return parsed person name
	 * @should parse two person name with comma
	 * @should parse two person name without comma
	 * @should not fail when ending with whitespace
	 * @should not fail when ending with a comma
	 * @should parse four person name
	 */
	public PersonName parsePersonName(String name) throws APIException;
	
	/**
	 * Get all relationships for a given type of relationship mapped from the personA to all of the
	 * personB's
	 * 
	 * @param relationshipType type of relationship for which to retrieve all relationships
	 * @return all relationships for the given type of relationship
	 * @throws APIException
	 * @should return empty map when no relationship has the matching relationship type
	 */
	@Authorized( { PrivilegeConstants.GET_RELATIONSHIPS })
	public Map<Person, List<Person>> getRelationshipMap(RelationshipType relationshipType) throws APIException;
	
	/**
	 * Builds the serialized data from
	 * {@link org.openmrs.person.PersonMergeLog#getPersonMergeLogData}, sets the mergedData String,
	 * and the creator and date if null. It then saves the <code>PersonMergeLog</code> object to the
	 * model.
	 * 
	 * @param personMergeLog the <code>PersonMergeLog</code> object to save.
	 * @return the persisted <code>PersonMergeLog</code> object
	 * @see org.openmrs.person.PersonMergeLog
	 * @see org.openmrs.api.handler.OpenmrsObjectSaveHandler
	 * @should require PersonMergeLogData
	 * @should require winner
	 * @should require loser
	 * @should set date created if null
	 * @should set creator if null
	 * @should serialize PersonMergeLogData
	 * @should save PersonMergeLog
	 */
	public PersonMergeLog savePersonMergeLog(PersonMergeLog personMergeLog) throws SerializationException, APIException;
	
	/**
	 * Gets a PersonMergeLog object from the model using the UUID identifier. Deserializes the
	 * 
	 * @param uuid
	 * @param deserialize
	 * @return person merge log object
	 * @throws SerializationException
	 * @throws APIException
	 * @should require uuid
	 * @should retrieve personMergeLog without deserializing data
	 * @should retrieve personMergeLog and deserialize data
	 */
	public PersonMergeLog getPersonMergeLogByUuid(String uuid, boolean deserialize) throws SerializationException,
	        APIException;
	
	/**
	 * Gets all the <code>PersonMergeLog</code> objects from the model
	 * 
	 * @return list of PersonMergeLog objects
	 * @throws SerializationException
	 * @should retrieve all PersonMergeLogs from the model
	 * @should retrieve all PersonMergeLogs and deserialize them
	 */
	public List<PersonMergeLog> getAllPersonMergeLogs(boolean deserialize) throws SerializationException;
	
	/**
	 * Gets <code>PersonMergeLog</code> objects by winning person p. Useful for to getting all persons merged into p.
	 * @param person the winning person
	 * @return List of <code>PersonMergeLog</code> objects
	 * @throws SerializationException
	 * @should retrieve PersonMergeLogs by winner
	 */
	public List<PersonMergeLog> getWinningPersonMergeLogs(Person person, boolean deserialize) throws SerializationException;
	
	/**
	 * Gets the <code>PersonMergeLog</code> where person p is the loser. Useful for getting the person that p was merged into.
	 * @param person the losing person
	 * @return The <code>PersonMergeLog</code> object
	 * @throws SerializationException
	 * @should find PersonMergeLog by loser
	 */
	public PersonMergeLog getLosingPersonMergeLog(Person person, boolean deserialize) throws SerializationException;
	
	/**
	 * Voids the given PersonAddress, effectively deleting the personAddress, from the end-user's
	 * point of view.
	 * 
	 * @param personAddress PersonAddress to void
	 * @param voidReason String reason the personAddress is being voided.
	 * @return the newly saved personAddress
	 * @throws APIException
	 * @should void personAddress with the given reason
	 */
	@Authorized( { PrivilegeConstants.EDIT_PERSONS })
	public PersonAddress voidPersonAddress(PersonAddress personAddress, String voidReason);
	
	/**
	 * Unvoid PersonAddress in the database, effectively marking this as a valid PersonAddress again
	 * 
	 * @param personAddress PersonAddress to unvoid
	 * @return the newly unvoided personAddress
	 * @throws APIException
	 * @should unvoid voided personAddress
	 */
	@Authorized( { PrivilegeConstants.EDIT_PERSONS })
	public PersonAddress unvoidPersonAddress(PersonAddress personAddress) throws APIException;
	
	/**
	 * Inserts or updates the given personAddress object in the database
	 * 
	 * @param personAddress PersonAddress to be created or updated
	 * @return personAddress that was created or updated
	 * @since 1.9
	 */
	@Authorized( { PrivilegeConstants.EDIT_PERSONS })
	public PersonAddress savePersonAddress(PersonAddress personAddress);
	
	/**
	 * Check if the person attribute types are locked, and if they are throws an exception during manipulation of a person attribute type
	 * 
	 * @throws PersonAttributeTypeLockedException
	 */
	public void checkIfPersonAttributeTypesAreLocked() throws PersonAttributeTypeLockedException;
}
