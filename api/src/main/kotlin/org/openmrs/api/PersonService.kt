/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api

import org.openmrs.Person
import org.openmrs.PersonAddress
import org.openmrs.PersonAttribute
import org.openmrs.PersonAttributeType
import org.openmrs.PersonName
import org.openmrs.Relationship
import org.openmrs.RelationshipType
import org.openmrs.annotation.Authorized
import org.openmrs.api.db.PersonDAO
import org.openmrs.person.PersonMergeLog
import org.openmrs.serialization.SerializationException
import org.openmrs.util.OpenmrsConstants.PERSON_TYPE
import org.openmrs.util.PrivilegeConstants
import java.util.Date

/**
 * Contains methods pertaining to Persons in the system.
 *
 * Use:
 * ```
 * val personObjects = Context.getPersonService().getAllPersons()
 * ```
 *
 * @see org.openmrs.api.context.Context
 * @see org.openmrs.Patient
 */
interface PersonService : OpenmrsService {

    /**
     * These enumerations are used when determining which person attr types to display.
     */
    enum class ATTR_VIEW_TYPE {
        /** Attributes to be shown when listing off multiple patients or users */
        LISTING,
        /** Attributes to be shown when only showing one patient or user */
        VIEWING,
        /** Attributes to be shown in the header */
        HEADER
    }

    /**
     * Sets the DAO for this service.
     *
     * @param dao DAO for this service
     */
    fun setPersonDAO(dao: PersonDAO)

    /**
     * Find a similar person given the attributes.
     *
     * @param nameSearch string to search the person's name for
     * @param birthyear the year of birth to restrict
     * @param gender The gender field to search on
     * @return Set of Person objects with all people matching criteria
     * @throws APIException if retrieval fails
     */
    @Authorized(PrivilegeConstants.GET_PERSONS)
    @Throws(APIException::class)
    fun getSimilarPeople(nameSearch: String, birthyear: Int?, gender: String?): Set<Person>

    /**
     * Find a person matching the searchPhrase search string.
     *
     * @param searchPhrase person name to match on
     * @param dead if true will return only dead patients, if false will return only alive patients
     * @return list of person objects matches the parameters
     * @throws APIException if retrieval fails
     */
    @Authorized(PrivilegeConstants.GET_PERSONS)
    @Throws(APIException::class)
    fun getPeople(searchPhrase: String, dead: Boolean?): List<Person>

    /**
     * Find a person matching the searchPhrase search string.
     *
     * @param searchPhrase person name to match on
     * @param dead if true will return only dead patients
     * @param voided if true will include voided persons
     * @return list of person objects matches the parameters
     * @throws APIException if retrieval fails
     */
    @Authorized(PrivilegeConstants.GET_PERSONS)
    @Throws(APIException::class)
    fun getPeople(searchPhrase: String, dead: Boolean?, voided: Boolean?): List<Person>

    /**
     * Save the given person attribute type in the database.
     *
     * @param type the type to save
     * @return the saved person attribute type
     * @throws APIException if saving fails
     */
    @Authorized(PrivilegeConstants.MANAGE_PERSON_ATTRIBUTE_TYPES)
    @Throws(APIException::class)
    fun savePersonAttributeType(type: PersonAttributeType): PersonAttributeType

    /**
     * Retire a Person Attribute Type.
     *
     * @param type the type to retire
     * @param retiredReason the reason for retiring
     * @return the retired type
     * @throws APIException if retiring fails
     */
    @Authorized(PrivilegeConstants.MANAGE_PERSON_ATTRIBUTE_TYPES)
    @Throws(APIException::class)
    fun retirePersonAttributeType(type: PersonAttributeType, retiredReason: String): PersonAttributeType

    /**
     * Retire a Person Relationship Type.
     *
     * @param type the type to retire
     * @param retiredReason the reason for retiring
     * @return the retired type
     * @throws APIException if retiring fails
     */
    @Authorized(PrivilegeConstants.MANAGE_RELATIONSHIP_TYPES)
    @Throws(APIException::class)
    fun retireRelationshipType(type: RelationshipType, retiredReason: String): RelationshipType

    /**
     * Unretire a Person Relationship Type.
     *
     * @param relationshipType the type to unretire
     * @return the unretired type
     * @since 1.9
     */
    @Authorized(PrivilegeConstants.MANAGE_RELATIONSHIP_TYPES)
    fun unretireRelationshipType(relationshipType: RelationshipType): RelationshipType

    /**
     * Purges a PersonAttribute type from the database (cannot be undone).
     *
     * @param type type to be purged from the database
     * @throws APIException if purging fails
     */
    @Authorized(PrivilegeConstants.PURGE_PERSON_ATTRIBUTE_TYPES)
    @Throws(APIException::class)
    fun purgePersonAttributeType(type: PersonAttributeType)

    /**
     * Unretires a PersonAttribute type from the database.
     *
     * @param type type to be restored from the database
     * @throws APIException if unretiring fails
     */
    @Authorized(PrivilegeConstants.MANAGE_PERSON_ATTRIBUTE_TYPES)
    @Throws(APIException::class)
    fun unretirePersonAttributeType(type: PersonAttributeType)

    /**
     * Effectively removes this person from the system.
     *
     * @param person person to be voided
     * @param reason reason for voiding person
     * @return the person that was voided
     * @throws APIException if voiding fails
     */
    @Authorized(PrivilegeConstants.EDIT_PERSONS)
    @Throws(APIException::class)
    fun voidPerson(person: Person, reason: String): Person

    /**
     * Effectively resurrects this person in the db.
     *
     * @param person person to be revived
     * @return the person that was unvoided
     * @throws APIException if unvoiding fails
     */
    @Authorized(PrivilegeConstants.EDIT_PERSONS)
    @Throws(APIException::class)
    fun unvoidPerson(person: Person): Person

    /**
     * Get all PersonAttributeTypes in the database.
     *
     * @return All person attribute types including the retired ones
     * @throws APIException if retrieval fails
     */
    @Authorized(PrivilegeConstants.GET_PERSON_ATTRIBUTE_TYPES)
    @Throws(APIException::class)
    fun getAllPersonAttributeTypes(): List<PersonAttributeType>

    /**
     * Get all PersonAttributeTypes in the database with the option of including the retired types.
     *
     * @param includeRetired include retired attribute types?
     * @return List of all PersonAttributeTypes
     * @throws APIException if retrieval fails
     */
    @Authorized(PrivilegeConstants.GET_PERSON_ATTRIBUTE_TYPES)
    @Throws(APIException::class)
    fun getAllPersonAttributeTypes(includeRetired: Boolean): List<PersonAttributeType>

    /**
     * Find person attribute types matching the given parameters.
     *
     * @param exactName the name of type
     * @param format the format for this type
     * @param foreignKey the foreign key
     * @param searchable if true only returns searchable types
     * @return list of PersonAttributeTypes matching the given parameters
     * @throws APIException if retrieval fails
     */
    @Authorized(PrivilegeConstants.GET_PERSON_ATTRIBUTE_TYPES)
    @Throws(APIException::class)
    fun getPersonAttributeTypes(
        exactName: String?,
        format: String?,
        foreignKey: Int?,
        searchable: Boolean?
    ): List<PersonAttributeType>

    /**
     * Get the PersonAttributeType given the type's PersonAttributeTypeId.
     *
     * @param typeId PersonAttributeType.personAttributeTypeId to match on
     * @return the type matching this id or null if none was found
     * @throws APIException if retrieval fails
     */
    @Authorized(PrivilegeConstants.GET_PERSON_ATTRIBUTE_TYPES)
    @Throws(APIException::class)
    fun getPersonAttributeType(typeId: Int?): PersonAttributeType?

    /**
     * Gets a person attribute type with the given uuid.
     *
     * @param uuid the universally unique identifier to lookup
     * @return a person attribute type with the given uuid
     */
    @Authorized(PrivilegeConstants.GET_PERSON_ATTRIBUTE_TYPES)
    fun getPersonAttributeTypeByUuid(uuid: String): PersonAttributeType?

    /**
     * Get a PersonAttribute from the database with the given PersonAttributeId.
     *
     * @param id the PersonAttribute.personAttributeId to match on
     * @return the matching PersonAttribute or null if none was found
     * @throws APIException if retrieval fails
     */
    @Authorized(PrivilegeConstants.GET_PERSON_ATTRIBUTE_TYPES)
    @Throws(APIException::class)
    fun getPersonAttribute(id: Int?): PersonAttribute?

    /**
     * Get the PersonAttributeType given the type's name.
     *
     * @param typeName the name
     * @return the PersonAttributeType that has the given name or null if none found
     * @throws APIException if retrieval fails
     */
    @Authorized(PrivilegeConstants.GET_PERSON_ATTRIBUTE_TYPES)
    @Throws(APIException::class)
    fun getPersonAttributeTypeByName(typeName: String): PersonAttributeType?

    /**
     * Get relationship by internal relationship identifier.
     *
     * @param relationshipId the id
     * @return Relationship the relationship to match on or null if none found
     * @throws APIException if retrieval fails
     */
    @Authorized(PrivilegeConstants.GET_RELATIONSHIPS)
    @Throws(APIException::class)
    fun getRelationship(relationshipId: Int?): Relationship?

    /**
     * Get Relationship by its UUID.
     *
     * @param uuid the uuid
     * @return relationship or null
     * @throws APIException if retrieval fails
     */
    @Authorized(PrivilegeConstants.GET_RELATIONSHIPS)
    @Throws(APIException::class)
    fun getRelationshipByUuid(uuid: String): Relationship?

    /**
     * Get list of relationships that are not voided.
     *
     * @return non-voided Relationship list
     * @throws APIException if retrieval fails
     */
    @Authorized(PrivilegeConstants.GET_RELATIONSHIPS)
    @Throws(APIException::class)
    fun getAllRelationships(): List<Relationship>

    /**
     * Get list of relationships optionally including the voided ones or not.
     *
     * @param includeVoided true/false whether to include the voided relationships
     * @return Relationship list
     * @throws APIException if retrieval fails
     */
    @Authorized(PrivilegeConstants.GET_RELATIONSHIPS)
    @Throws(APIException::class)
    fun getAllRelationships(includeVoided: Boolean): List<Relationship>

    /**
     * Get list of relationships that include Person in person_id or relative_id.
     *
     * @param p person object listed on either side of the relationship
     * @return Relationship list
     * @throws APIException if retrieval fails
     */
    @Authorized(PrivilegeConstants.GET_RELATIONSHIPS)
    @Throws(APIException::class)
    fun getRelationshipsByPerson(p: Person): List<Relationship>

    /**
     * Get list of relationships that include Person in person_id or relative_id.
     *
     * @param p person object listed on either side of the relationship
     * @param effectiveDate effective date of relationship
     * @return Relationship list
     * @throws APIException if retrieval fails
     */
    @Authorized(PrivilegeConstants.GET_RELATIONSHIPS)
    @Throws(APIException::class)
    fun getRelationshipsByPerson(p: Person, effectiveDate: Date?): List<Relationship>

    /**
     * Get relationships stored in the database.
     *
     * @param fromPerson Person in the person_id column
     * @param toPerson Person in the relative_id column
     * @param relType The RelationshipType to match
     * @return relationships matching the given parameters
     * @throws APIException if retrieval fails
     */
    @Authorized(PrivilegeConstants.GET_RELATIONSHIPS)
    @Throws(APIException::class)
    fun getRelationships(fromPerson: Person?, toPerson: Person?, relType: RelationshipType?): List<Relationship>

    /**
     * Get relationships stored in the database that are active on the passed date.
     *
     * @param fromPerson Person in the person_id column
     * @param toPerson Person in the relative_id column
     * @param relType The RelationshipType to match
     * @param effectiveDate The date during which the relationship was effective
     * @return relationships matching the given parameters
     * @throws APIException if retrieval fails
     */
    @Authorized(PrivilegeConstants.GET_RELATIONSHIPS)
    @Throws(APIException::class)
    fun getRelationships(
        fromPerson: Person?,
        toPerson: Person?,
        relType: RelationshipType?,
        effectiveDate: Date?
    ): List<Relationship>

    /**
     * Get relationships stored in the database that were active during the specified date range.
     *
     * @param fromPerson Person in the person_id column
     * @param toPerson Person in the relative_id column
     * @param relType The RelationshipType to match
     * @param startEffectiveDate The date during which the relationship was effective (lower bound)
     * @param endEffectiveDate The date during which the relationship was effective (upper bound)
     * @return relationships matching the given parameters
     * @throws APIException if retrieval fails
     */
    @Authorized(PrivilegeConstants.GET_RELATIONSHIPS)
    @Throws(APIException::class)
    fun getRelationships(
        fromPerson: Person?,
        toPerson: Person?,
        relType: RelationshipType?,
        startEffectiveDate: Date?,
        endEffectiveDate: Date?
    ): List<Relationship>

    /**
     * Get all relationshipTypes. Includes retired relationship types.
     *
     * @return relationshipType list
     * @throws APIException if retrieval fails
     */
    @Authorized(PrivilegeConstants.GET_RELATIONSHIP_TYPES)
    @Throws(APIException::class)
    fun getAllRelationshipTypes(): List<RelationshipType>

    /**
     * Get all relationshipTypes with the option of including the retired types.
     *
     * @param includeRetired include retired relationshipTypes?
     * @return relationshipType list
     * @throws APIException if retrieval fails
     */
    @Authorized(PrivilegeConstants.GET_RELATIONSHIP_TYPES)
    @Throws(APIException::class)
    fun getAllRelationshipTypes(includeRetired: Boolean): List<RelationshipType>

    /**
     * Get relationshipType by internal identifier.
     *
     * @param relationshipTypeId the id
     * @return relationshipType with given internal identifier or null if none found
     * @throws APIException if retrieval fails
     */
    @Authorized(PrivilegeConstants.GET_RELATIONSHIP_TYPES)
    @Throws(APIException::class)
    fun getRelationshipType(relationshipTypeId: Int?): RelationshipType?

    /**
     * Gets the relationship type with the given uuid.
     *
     * @param uuid the uuid
     * @return relationship type or null
     * @throws APIException if retrieval fails
     */
    @Authorized(PrivilegeConstants.GET_RELATIONSHIP_TYPES)
    @Throws(APIException::class)
    fun getRelationshipTypeByUuid(uuid: String): RelationshipType?

    /**
     * Find relationshipType by exact name match.
     *
     * @param relationshipTypeName name to match on
     * @return RelationshipType with given name or null if none found
     * @throws APIException if retrieval fails
     */
    @Authorized(PrivilegeConstants.GET_RELATIONSHIP_TYPES)
    @Throws(APIException::class)
    fun getRelationshipTypeByName(relationshipTypeName: String): RelationshipType?

    /**
     * Find relationshipTypes by exact name match and/or preferred status.
     *
     * @param relationshipTypeName name to match on
     * @param preferred if true, returns on preferred types
     * @return RelationshipTypes with given name and preferred status
     * @throws APIException if retrieval fails
     */
    @Authorized(PrivilegeConstants.GET_RELATIONSHIP_TYPES)
    @Throws(APIException::class)
    fun getRelationshipTypes(relationshipTypeName: String?, preferred: Boolean?): List<RelationshipType>

    /**
     * Get relationshipTypes by searching through the names.
     *
     * @param searchString string to match to a relationship type name
     * @return list of relationship types or empty list if none found
     * @throws APIException if retrieval fails
     */
    @Authorized(PrivilegeConstants.GET_RELATIONSHIP_TYPES)
    @Throws(APIException::class)
    fun getRelationshipTypes(searchString: String?): List<RelationshipType>

    /**
     * Create or update a relationship between people.
     *
     * @param relationship relationship to be created or updated
     * @return relationship that was created or updated
     * @throws APIException if saving fails
     */
    @Authorized(PrivilegeConstants.ADD_RELATIONSHIPS, PrivilegeConstants.EDIT_RELATIONSHIPS)
    @Throws(APIException::class)
    fun saveRelationship(relationship: Relationship): Relationship

    /**
     * Purges a relationship from the database (cannot be undone).
     *
     * @param relationship relationship to be purged from the database
     * @throws APIException if purging fails
     */
    @Authorized(PrivilegeConstants.PURGE_RELATIONSHIPS)
    @Throws(APIException::class)
    fun purgeRelationship(relationship: Relationship)

    /**
     * Voids the given Relationship, effectively removing it from openmrs.
     *
     * @param relationship Relationship to void
     * @param voidReason String reason the relationship is being voided.
     * @return the newly saved relationship
     * @throws APIException if voiding fails
     */
    @Authorized(PrivilegeConstants.DELETE_RELATIONSHIPS)
    @Throws(APIException::class)
    fun voidRelationship(relationship: Relationship, voidReason: String): Relationship

    /**
     * Unvoid Relationship in the database.
     *
     * @param relationship Relationship to unvoid
     * @return the newly unvoided relationship
     * @throws APIException if unvoiding fails
     */
    @Authorized(PrivilegeConstants.EDIT_RELATIONSHIPS)
    @Throws(APIException::class)
    fun unvoidRelationship(relationship: Relationship): Relationship

    /**
     * Creates or updates a Person in the database.
     *
     * @param person person to be created or updated
     * @return person who was created or updated
     * @throws APIException if saving fails
     */
    @Authorized(PrivilegeConstants.ADD_PERSONS, PrivilegeConstants.EDIT_PERSONS)
    @Throws(APIException::class)
    fun savePerson(person: Person): Person

    /**
     * Purges a person from the database (cannot be undone).
     *
     * @param person person to be purged from the database
     * @throws APIException if purging fails
     */
    @Authorized(PrivilegeConstants.PURGE_PERSONS)
    @Throws(APIException::class)
    fun purgePerson(person: Person)

    /**
     * Get Person by its UUID.
     *
     * @param uuid the uuid
     * @return person or null
     * @throws APIException if retrieval fails
     */
    @Authorized(PrivilegeConstants.GET_PERSONS)
    @Throws(APIException::class)
    fun getPersonByUuid(uuid: String): Person?

    /**
     * Get PersonAddress by its UUID.
     *
     * @param uuid the uuid
     * @return person address or null
     * @throws APIException if retrieval fails
     */
    @Authorized(PrivilegeConstants.GET_PERSONS)
    @Throws(APIException::class)
    fun getPersonAddressByUuid(uuid: String): PersonAddress?

    /**
     * Get PersonAttribute by its UUID.
     *
     * @param uuid the uuid
     * @return person attribute or null
     * @throws APIException if retrieval fails
     */
    @Authorized(PrivilegeConstants.GET_PERSONS)
    @Throws(APIException::class)
    fun getPersonAttributeByUuid(uuid: String): PersonAttribute?

    /**
     * Get PersonName by its personNameId.
     *
     * @param personNameId the id
     * @return person name or null
     */
    @Authorized(PrivilegeConstants.GET_PERSONS)
    fun getPersonName(personNameId: Int?): PersonName?

    /**
     * Get PersonName by its UUID.
     *
     * @param uuid the uuid
     * @return person name or null
     * @throws APIException if retrieval fails
     */
    @Authorized(PrivilegeConstants.GET_PERSONS)
    @Throws(APIException::class)
    fun getPersonNameByUuid(uuid: String): PersonName?

    /**
     * Gets a person by internal id.
     *
     * @param personId internal identifier of person to get
     * @return Person person with given internal identifier
     * @throws APIException if retrieval fails
     */
    @Authorized(PrivilegeConstants.GET_PERSONS)
    @Throws(APIException::class)
    fun getPerson(personId: Int?): Person?

    /**
     * Inserts or updates the given relationship type object in the database.
     *
     * @param relationshipType type to be created or updated
     * @return relationship type that was created or updated
     * @throws APIException if saving fails
     */
    @Authorized(PrivilegeConstants.MANAGE_RELATIONSHIP_TYPES)
    @Throws(APIException::class)
    fun saveRelationshipType(relationshipType: RelationshipType): RelationshipType

    /**
     * Purge relationship type from the database (cannot be undone).
     *
     * @param relationshipType relationship type to be purged
     * @throws APIException if purging fails
     */
    @Authorized(PrivilegeConstants.PURGE_RELATIONSHIP_TYPES)
    @Throws(APIException::class)
    fun purgeRelationshipType(relationshipType: RelationshipType)

    /**
     * Gets the types defined for the given person type and the given type of view.
     *
     * @param personType PERSON, USER, PATIENT, or null
     * @param viewType whether this is a listing or viewing or null for both
     * @return list of PersonAttributeTypes that should be displayed
     * @throws APIException if retrieval fails
     */
    @Throws(APIException::class)
    fun getPersonAttributeTypes(personType: PERSON_TYPE?, viewType: ATTR_VIEW_TYPE?): List<PersonAttributeType>

    /**
     * Voids the given PersonName.
     *
     * @param personName PersonName to void
     * @param voidReason String reason the personName is being voided.
     * @return the newly saved personName
     */
    @Authorized(PrivilegeConstants.EDIT_PERSONS)
    fun voidPersonName(personName: PersonName, voidReason: String): PersonName

    /**
     * Unvoid PersonName in the database.
     *
     * @param personName PersonName to unvoid
     * @return the newly unvoided personName
     * @throws APIException if unvoiding fails
     */
    @Authorized(PrivilegeConstants.EDIT_PERSONS)
    @Throws(APIException::class)
    fun unvoidPersonName(personName: PersonName): PersonName

    /**
     * Inserts or updates the given personName object in the database.
     *
     * @param personName to be created or updated
     * @return personName that was created or updated
     */
    @Authorized(PrivilegeConstants.EDIT_PERSONS)
    fun savePersonName(personName: PersonName): PersonName

    /**
     * Parses a name into a PersonName.
     *
     * @param name person name to be parsed
     * @return parsed person name
     * @throws APIException if parsing fails
     */
    @Throws(APIException::class)
    fun parsePersonName(name: String): PersonName

    /**
     * Get all relationships for a given type mapped from the personA to all of the personB's.
     *
     * @param relationshipType type of relationship for which to retrieve all relationships
     * @return all relationships for the given type of relationship
     * @throws APIException if retrieval fails
     */
    @Authorized(PrivilegeConstants.GET_RELATIONSHIPS)
    @Throws(APIException::class)
    fun getRelationshipMap(relationshipType: RelationshipType): Map<Person, List<Person>>

    /**
     * Saves the PersonMergeLog object to the model.
     *
     * @param personMergeLog the PersonMergeLog object to save.
     * @return the persisted PersonMergeLog object
     * @throws SerializationException if serialization fails
     * @throws APIException if saving fails
     */
    @Throws(SerializationException::class, APIException::class)
    fun savePersonMergeLog(personMergeLog: PersonMergeLog): PersonMergeLog

    /**
     * Gets a PersonMergeLog object from the model using the UUID identifier.
     *
     * @param uuid the uuid
     * @param deserialize whether to deserialize
     * @return person merge log object
     * @throws SerializationException if deserialization fails
     * @throws APIException if retrieval fails
     */
    @Throws(SerializationException::class, APIException::class)
    fun getPersonMergeLogByUuid(uuid: String, deserialize: Boolean): PersonMergeLog?

    /**
     * Gets all the PersonMergeLog objects from the model.
     *
     * @param deserialize whether to deserialize
     * @return list of PersonMergeLog objects
     * @throws SerializationException if deserialization fails
     */
    @Throws(SerializationException::class)
    fun getAllPersonMergeLogs(deserialize: Boolean): List<PersonMergeLog>

    /**
     * Gets PersonMergeLog objects by winning person p.
     *
     * @param person the winning person
     * @param deserialize whether to deserialize
     * @return List of PersonMergeLog objects
     * @throws SerializationException if deserialization fails
     */
    @Throws(SerializationException::class)
    fun getWinningPersonMergeLogs(person: Person, deserialize: Boolean): List<PersonMergeLog>

    /**
     * Gets the PersonMergeLog where person p is the loser.
     *
     * @param person the losing person
     * @param deserialize whether to deserialize
     * @return The PersonMergeLog object
     * @throws SerializationException if deserialization fails
     */
    @Throws(SerializationException::class)
    fun getLosingPersonMergeLog(person: Person, deserialize: Boolean): PersonMergeLog?

    /**
     * Voids the given PersonAddress.
     *
     * @param personAddress PersonAddress to void
     * @param voidReason String reason the personAddress is being voided.
     * @return the newly saved personAddress
     */
    @Authorized(PrivilegeConstants.EDIT_PERSONS)
    fun voidPersonAddress(personAddress: PersonAddress, voidReason: String): PersonAddress

    /**
     * Unvoid PersonAddress in the database.
     *
     * @param personAddress PersonAddress to unvoid
     * @return the newly unvoided personAddress
     * @throws APIException if unvoiding fails
     */
    @Authorized(PrivilegeConstants.EDIT_PERSONS)
    @Throws(APIException::class)
    fun unvoidPersonAddress(personAddress: PersonAddress): PersonAddress

    /**
     * Inserts or updates the given personAddress object in the database.
     *
     * @param personAddress PersonAddress to be created or updated
     * @return personAddress that was created or updated
     * @since 1.9
     */
    @Authorized(PrivilegeConstants.EDIT_PERSONS)
    fun savePersonAddress(personAddress: PersonAddress): PersonAddress

    /**
     * Check if the person attribute types are locked.
     *
     * @throws PersonAttributeTypeLockedException if types are locked
     */
    @Throws(PersonAttributeTypeLockedException::class)
    fun checkIfPersonAttributeTypesAreLocked()
}
