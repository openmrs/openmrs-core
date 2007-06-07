package org.openmrs.api.db;

import java.util.List;
import java.util.Set;

import org.openmrs.Person;
import org.openmrs.PersonAttributeType;
import org.openmrs.Relationship;
import org.openmrs.RelationshipType;

/**
 * Person-related database functions
 * 
 * @author Ben Wolfe
 * @version 1.0
 */
public interface PersonDAO {
	
	/**
	 * @see org.openmrs.api.PersonService#getSimilarPeople(java.lang.String, java.lang.Integer, java.lang.String)
	 */
	public Set<Person> getSimilarPeople(String name, Integer birthyear, String gender) throws DAOException;
	
	public void createPersonAttributeType(PersonAttributeType type);

	public void deletePersonAttributeType(PersonAttributeType type);

	public void updatePersonAttributeType(PersonAttributeType type);

	public List<PersonAttributeType> getPersonAttributeTypes();

	public PersonAttributeType getPersonAttributeType(Integer typeId);
	
	public PersonAttributeType getPersonAttributeType(String s);
	
	/**
	 * Get relationship by internal relationship identifier
	 * 
	 * @return Relationship
	 * @param relationshipId 
	 * @throws DAOException
	 */
	public Relationship getRelationship(Integer relationshipId) throws DAOException;
	
	/**
	 * Get list of relationships that are not retired
	 * 
	 * @return non-voided Relationship list
	 * @throws DAOException
	 */
	public List<Relationship> getRelationships() throws DAOException;
	
	/**
	 * Get list of relationships containing Person 
	 * 
	 * @return Relationship list
	 * @throws DAOException
	 */
	public List<Relationship> getRelationships(Person p, boolean showVoided) throws DAOException;
	
	/**
	 * Get all relationshipTypes
	 * 
	 * @return relationshipType list
	 * @throws DAOException
	 */
	public List<RelationshipType> getRelationshipTypes() throws DAOException;

	/**
	 * Get relationshipType by internal identifier
	 * 
	 * @param relationshipType id
	 * @return relationshipType with given internal identifier
	 * @throws DAOException
	 */
	public RelationshipType getRelationshipType(Integer relationshipTypeId) throws DAOException;
	
	/**
	 * Get relationshipType by name
	 * 
	 * @throws DAOException
	 */
	public RelationshipType findRelationshipType(String relationshipTypeName) throws DAOException;

	/**
	 * Create a new Person
	 * @param Person to create
	 * @throws DAOException
	 */
	public void createPerson(Person person) throws DAOException;

	/**
	 * Update a person
	 * @param Person to update
	 * @throws DAOException
	 */
	public void updatePerson(Person person) throws DAOException;

	/**
	 * Delete a person
	 * @param Person to delete
	 * @throws DAOException
	 */
	public void deletePerson(Person person) throws DAOException;
	
	/**
	 * 
	 * @param personId of the Person to retrieve
	 * @return Person
	 * @throws DAOException
	 */
	public Person getPerson(Integer personId) throws DAOException;

	/**
	 * Create a new Relationship
	 * @param Relationship to create
	 * @throws DAOException
	 */
	public void createRelationship(Relationship relationship) throws DAOException;

	/**
	 * Update Relationship
	 * @param Relationship to update
	 * @throws DAOException
	 */
	public void updateRelationship(Relationship relationship) throws DAOException;

	/**
	 * Delete Relationship
	 * @param Relationship to delete
	 * @throws DAOException
	 */
	public void deleteRelationship(Relationship relationship) throws DAOException;	
	
	/**
	 * Retire Relationship
	 * @param Relationship to void
	 * @throws DAOException
	 */
	public void voidRelationship(Relationship relationship) throws DAOException;	

	/**
	 * Unretire Relationship
	 * @param Relationship to unvoid
	 * @throws DAOException
	 */
	public void unvoidRelationship(Relationship relationship) throws DAOException;		

	/**
	 * Create a new RelationshipType
	 * @param RelationshipType to create
	 * @throws DAOException
	 */
	public void createRelationshipType(RelationshipType relationshipType) throws DAOException;

	/**
	 * Update RelationshipType
	 * @param RelationshipType to update
	 * @throws DAOException
	 */
	public void updateRelationshipType(RelationshipType relationshipType) throws DAOException;

	/**
	 * Delete RelationshipType
	 * @param RelationshipType to delete
	 * @throws DAOException
	 */
	public void deleteRelationshipType(RelationshipType relationshipType) throws DAOException;	

	
}
