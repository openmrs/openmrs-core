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
package org.openmrs.api.db;

import java.util.List;
import java.util.Set;

import org.openmrs.Person;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonAttributeType;
import org.openmrs.Relationship;
import org.openmrs.RelationshipType;

/**
 * Person-related database functions
 * <p>
 * This is used by the PersonService. This should not be used directly, but rather used through the
 * methods on the PersonService.
 * <p>
 * Use case: <code>
 *   PersonService ps = Context.getPersonService();
 *   ps....
 *   
 * </code>
 * 
 * @see org.openmrs.api.PersonService
 * @see org.openmrs.api.context.Context
 */
public interface PersonDAO {
	
	/**
	 * @see org.openmrs.api.PersonService#getSimilarPeople(java.lang.String, java.lang.Integer,
	 *      java.lang.String)
	 */
	public Set<Person> getSimilarPeople(String name, Integer birthyear, String gender) throws DAOException;
	
	/**
	 * @see org.openmrs.api.PersonService#getPeople(String, Boolean)
	 */
	public List<Person> getPeople(String searchPhrase, Boolean dead) throws DAOException;
	
	/**
	 * @see org.openmrs.api.PersonService#savePersonAttributeType(org.openmrs.PersonAttributeType)
	 */
	public PersonAttributeType savePersonAttributeType(PersonAttributeType type) throws DAOException;
	
	/**
	 * @see org.openmrs.api.PersonService#purgePersonAttributeType(org.openmrs.PersonAttributeType)
	 */
	public void deletePersonAttributeType(PersonAttributeType type) throws DAOException;
	
	/**
	 * @see org.openmrs.api.PersonService#getPersonAttributeTypes(java.lang.String,
	 *      java.lang.String, java.lang.Integer, java.lang.Boolean)
	 */
	public List<PersonAttributeType> getPersonAttributeTypes(String exactName, String format, Integer foreignKey,
	                                                         Boolean searchable) throws DAOException;
	
	/**
	 * @see org.openmrs.api.PersonService#getAllPersonAttributeTypes()
	 * @see org.openmrs.api.PersonService#getAllPersonAttributeTypes(boolean)
	 */
	public List<PersonAttributeType> getAllPersonAttributeTypes(boolean includeRetired) throws DAOException;
	
	/**
	 * @see org.openmrs.api.PersonService#getPersonAttributeType(java.lang.Integer)
	 */
	public PersonAttributeType getPersonAttributeType(Integer typeId) throws DAOException;
	
	/**
	 * @see org.openmrs.api.PersonService#getPersonAttribute(java.lang.Integer)
	 */
	public PersonAttribute getPersonAttribute(Integer id) throws DAOException;
	
	/**
	 * @see org.openmrs.api.PersonService#getRelationship(java.lang.Integer)
	 */
	public Relationship getRelationship(Integer relationshipId) throws DAOException;
	
	/**
	 * @see org.openmrs.api.PersonService#getAllRelationships(boolean)
	 */
	public List<Relationship> getAllRelationships(boolean includeVoided) throws DAOException;
	
	/**
	 * @see org.openmrs.api.PersonService#getAllRelationshipTypes()
	 */
	public List<RelationshipType> getAllRelationshipTypes() throws DAOException;
	
	/**
	 * @see org.openmrs.api.PersonService#getRelationshipType(java.lang.Integer)
	 */
	public RelationshipType getRelationshipType(Integer relationshipTypeId) throws DAOException;
	
	/**
	 * @see org.openmrs.api.PersonService#getRelationshipTypes(java.lang.String, java.lang.Boolean)
	 */
	public List<RelationshipType> getRelationshipTypes(String relationshipTypeName, Boolean preferred) throws DAOException;
	
	/**
	 * @see org.openmrs.api.PersonService#savePerson(org.openmrs.Person)
	 */
	public Person savePerson(Person person) throws DAOException;
	
	/**
	 * @see org.openmrs.api.PersonService#deletePerson(org.openmrs.Person)
	 */
	public void deletePerson(Person person) throws DAOException;
	
	/**
	 * @see org.openmrs.api.PersonService#getPerson(java.lang.Integer)
	 */
	public Person getPerson(Integer personId) throws DAOException;
	
	/**
	 * @see org.openmrs.api.PersonService#saveRelationship(org.openmrs.Relationship)
	 */
	public Relationship saveRelationship(Relationship relationship) throws DAOException;
	
	/**
	 * @see org.openmrs.api.PersonService#purgeRelationship(org.openmrs.Relationship)
	 */
	public void deleteRelationship(Relationship relationship) throws DAOException;
	
	/**
	 * @see org.openmrs.api.PersonService#getRelationships(org.openmrs.Person, org.openmrs.Person,
	 *      org.openmrs.RelationshipType)
	 */
	public List<Relationship> getRelationships(Person fromPerson, Person toPerson, RelationshipType relType)
	                                                                                                        throws DAOException;
	
	/**
	 * @see org.openmrs.api.PersonService#saveRelationshipType(org.openmrs.RelationshipType)
	 */
	public RelationshipType saveRelationshipType(RelationshipType relationshipType) throws DAOException;
	
	/**
	 * @see org.openmrs.api.PersonService#deleteRelationshipType(org.openmrs.RelationshipType)
	 */
	public void deleteRelationshipType(RelationshipType relationshipType) throws DAOException;
}
