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

import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.openmrs.Person;
import org.openmrs.PersonAddress;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonAttributeType;
import org.openmrs.PersonName;
import org.openmrs.Relationship;
import org.openmrs.RelationshipType;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.DAOException;
import org.openmrs.api.db.PersonDAO;

public class HibernatePersonDAO implements PersonDAO {

	protected final Log log = LogFactory.getLog(getClass());

	/**
	 * Hibernate session factory
	 */
	private SessionFactory sessionFactory;
	
	public HibernatePersonDAO() { }
	
	/**
	 * Set session factory
	 * 
	 * @param sessionFactory
	 */
	public void setSessionFactory(SessionFactory sessionFactory) { 
		this.sessionFactory = sessionFactory;
	}
	
	/**
	 * @see org.openmrs.api.dao.PersonDAO#getSimilarPeople(java.lang.String,java.lang.Integer,java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	public Set<Person> getSimilarPeople(String name, Integer birthyear, String gender) throws DAOException {
		if (birthyear == null)
			birthyear = 0;
		
		// TODO return the matched name instead of the primary name
		//   possible solution: "select new" org.openmrs.PersonListItem and return a list of those
		
		Set<Person> people = new LinkedHashSet<Person>();
		
		name = name.replaceAll("  ", " ");
		name = name.replace(", ", " ");
		String[] names = name.split(" ");
		
		
		String q = "select p from Person p left join p.names as pname where";
		
		if (names.length == 1) {
			q += "(";
			q += " soundex(pname.givenName) = soundex(:n1)";
			q += " or soundex(pname.middleName) = soundex(:n2)";
			q += " or soundex(pname.familyName) = soundex(:n3) ";
			q += ")";
		}
		else if (names.length == 2) {
			q += "(";
			q += " case";
			q += "  when pname.givenName is null then 1";
			q += "  when soundex(pname.givenName) = soundex(:n1) then 2";
			q += "  when soundex(pname.givenName) = soundex(:n2) then 4";
			q += "  else 0 ";
			q += " end";
			q += " + ";
			q += " case";
			q += "  when pname.middleName is null then 1";
			q += "  when soundex(pname.middleName) = soundex(:n3) then 2";
			q += "  when soundex(pname.middleName) = soundex(:n4) then 4";
			q += "  else 0 ";
			q += " end";
			q += " +";
			q += " case";
			q += "  when pname.familyName is null then 1";
			q += "  when soundex(pname.familyName) = soundex(:n5) then 2";
			q += "  when soundex(pname.familyName) = soundex(:n6) then 4";
			q += "  else 0 ";
			q += " end";
			q += ") between 6 and 7";
		}
		else if (names.length == 3) {
			q += "(";
			q += " case";
			q += "  when pname.givenName is null then 0";
			q += "  when soundex(pname.givenName) = soundex(:n1) then 3";
			q += "  when soundex(pname.givenName) = soundex(:n2) then 2";
			q += "  when soundex(pname.givenName) = soundex(:n3) then 1";
			q += "  else 0 ";
			q += " end";
			q += " + ";
			q += " case";
			q += "  when pname.middleName is null then 0";
			q += "  when soundex(pname.middleName) = soundex(:n4) then 2";
			q += "  when soundex(pname.middleName) = soundex(:n5) then 3";
			q += "  when soundex(pname.middleName) = soundex(:n6) then 1";
			q += "  else 0";
			q += " end";
			q += " +";
			q += " case";
			q += "  when pname.familyName is null then 0";
			q += "  when soundex(pname.familyName) = soundex(:n7) then 1";
			q += "  when soundex(pname.familyName) = soundex(:n8) then 2";
			q += "  when soundex(pname.familyName) = soundex(:n9) then 3";
			q += "  else 0";
			q += " end";
			q += ") >= 5";
		}
		else
			throw new DAOException("Too many names to compare effectively.");
		
		String birthdayMatch = " (year(p.birthdate) between " + (birthyear - 1) + " and " + (birthyear + 1) +
								" or p.birthdate is null) ";
		
		String genderMatch = " (p.gender = :gender or p.gender = '') ";
		
		if (birthyear != 0 && gender != null) {
			q += " and (" + birthdayMatch + "and " + genderMatch + ") "; 
		}
		else if (birthyear != 0) {
			q += " and " + birthdayMatch;
		}
		else if (gender != null) {
			q += " and " + genderMatch;
		}
		
		q += " order by pname.givenName asc, ";
		q += "pname.middleName asc, ";
		q += "pname.familyName asc";
		
		Query query = sessionFactory.getCurrentSession().createQuery(q);
		
		int count = 1;
		for (int i = 0; i < 3; i++) {
			for (int nameIndex = 0; nameIndex < names.length; nameIndex++) {
				query.setString("n" + count, names[nameIndex]);
				count++;
			}
		}
		
		if (q.contains(":gender"))
			query.setString("gender", gender);
		
		people.addAll(query.list());
		
		return people;
	}
	
	@SuppressWarnings("unchecked")
	public List<Person> findPeople(String name, boolean includeVoided) {
		name = name.replace(", ", " ");
		String[] names = name.split(" ");
		
		log.debug("name: " + name);
		
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Person.class);
		criteria.createAlias("names", "name");
		for (String n : names) {
			if (n != null && n.length() > 0) {
				criteria.add(Expression.or(
						Expression.like("name.givenName", n, MatchMode.START),
						Expression.or(
							Expression.like("name.familyName", n, MatchMode.START),
								Expression.or(
										Expression.like("name.middleName", n, MatchMode.START),
										Expression.like("systemId", n, MatchMode.START)
										)
							)
						)
					);
			}
		}
				
		if (includeVoided == false)
			criteria.add(Expression.eq("voided", false));
		
		criteria.addOrder(Order.asc("personId"));
		
		List returnList = new Vector();
		returnList = criteria.list();
		
		return returnList;
	}
		
		
	/**
	 * @see org.openmrs.api.db.PersonService#getPerson(java.lang.Long)
	 */
	public Person getPerson(Integer personId) {
		return (Person) sessionFactory.getCurrentSession().get(Person.class, personId);
	}
	
	/**
	 * @see org.openmrs.api.db.PersonDAO#createPersonAttributeType(org.openmrs.PersonAttributeType)
	 */
	public void createPersonAttributeType(PersonAttributeType type) {
		sessionFactory.getCurrentSession().save(type);
	}

	/**
	 * @see org.openmrs.api.db.PersonDAO#deletePersonAttributeType(org.openmrs.PersonAttributeType)
	 */
	public void deletePersonAttributeType(PersonAttributeType type) {
		sessionFactory.getCurrentSession().delete(type);
	}

	/**
	 * @see org.openmrs.api.db.PersonDAO#getPersonAttributeTypes()
	 */
	@SuppressWarnings("unchecked")
	public List<PersonAttributeType> getPersonAttributeTypes() {
		return sessionFactory.getCurrentSession().createQuery(
		"from PersonAttributeType type order by type.name").list();
	}

	/**
	 * @see org.openmrs.api.db.PersonDAO#updatePersonAttributeType(org.openmrs.PersonAttributeType)
	 */
	public void updatePersonAttributeType(PersonAttributeType type) {
		sessionFactory.getCurrentSession().merge(type);
	}

	/**
	 * @see org.openmrs.api.db.PersonDAO#getPersonAttributeType(java.lang.Integer)
	 */
	public PersonAttributeType getPersonAttributeType(Integer typeId) {
		return (PersonAttributeType) sessionFactory.getCurrentSession().get(PersonAttributeType.class, typeId);
	}

	public PersonAttribute getPersonAttribute(Integer id) {
		return (PersonAttribute)sessionFactory.getCurrentSession().get(PersonAttribute.class, id);
	}

	/**
	 * @see org.openmrs.api.db.PersonDAO#getPersonAttributeType(java.lang.String)
	 */
	public PersonAttributeType getPersonAttributeType(String typeName) {
		Session session = sessionFactory.getCurrentSession();
		Query q = session.createQuery("from PersonAttributeType t where t.name = :name")
						.setString("name", typeName);
		return (PersonAttributeType)q.uniqueResult();
	}
	
	/**
	 * @see org.openmrs.api.db.PatientService#getRelationship()
	 */
	public Relationship getRelationship(Integer relationshipId) throws DAOException {
		Relationship relationship = (Relationship)sessionFactory.getCurrentSession().get(Relationship.class, relationshipId);
		
		return relationship;
	}
	
	/**
	 * @see org.openmrs.api.db.PatientService#getRelationships()
	 */
	@SuppressWarnings("unchecked")
	public List<Relationship> getRelationships() throws DAOException {
		List<Relationship> relationships = sessionFactory.getCurrentSession().createQuery("from Relationship r order by r.relationshipId asc").list();
		
		return relationships;
	}
	
	/**
	 * @see org.openmrs.api.db.PatientService#getRelationships(org.openmrs.Person)
	 */
	@SuppressWarnings("unchecked")
	public List<Relationship> getRelationships(Person person, boolean showVoided) throws DAOException {

		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Relationship.class, "r")
		.add(Expression.or(Expression.eq("personA", person), Expression.eq("personB", person)));
		
		if (!showVoided) {
			criteria.add(Expression.eq("voided", showVoided));
		}
		
		return criteria.list();
	}

	/**
	 * @see org.openmrs.api.db.PatientService#getRelationshipType(java.lang.Integer)
	 */
	public RelationshipType getRelationshipType(Integer relationshipTypeId) throws DAOException {
		RelationshipType relationshipType = new RelationshipType();
		relationshipType = (RelationshipType)sessionFactory.getCurrentSession().get(RelationshipType.class, relationshipTypeId);
		
		return relationshipType;

	}
	
	public RelationshipType findRelationshipType(String relationshipTypeName) throws DAOException {
		RelationshipType ret = (RelationshipType) sessionFactory.getCurrentSession().createQuery("from RelationshipType t where CONCAT(t.aIsToB, CONCAT('/', t.bIsToA)) = :toString order by weight")
					.setString("toString", relationshipTypeName)
					.uniqueResult();
		
		return ret;
	}

	/**
	 * @see org.openmrs.api.db.PatientService#getRelationshipTypes()
	 */
	@SuppressWarnings("unchecked")
	public List<RelationshipType> getRelationshipTypes() throws DAOException {
		List<RelationshipType> relationshipTypes;
		relationshipTypes = sessionFactory.getCurrentSession().createQuery("from RelationshipType t order by t.weight").list();
		
		return relationshipTypes;
	}
	
	/**
	 * @see org.openmrs.api.db.PersonService#createRelationshipType(org.openmrs.RelationshipType)
	 */
	public void createRelationshipType(RelationshipType relationshipType) throws DAOException {
		relationshipType.setCreator(Context.getAuthenticatedUser());
		relationshipType.setDateCreated(new Date());
		sessionFactory.getCurrentSession().save(relationshipType);
	}
	
	/**
	 * @see org.openmrs.api.db.PersonService#updateRelationshipType(org.openmrs.RelationshipType)
	 */
	public void updateRelationshipType(RelationshipType relationshipType) throws DAOException {
		if (relationshipType.getRelationshipTypeId() == null)
			createRelationshipType(relationshipType);
		else
			sessionFactory.getCurrentSession().saveOrUpdate(relationshipType);
	}

	/**
	 * @see org.openmrs.api.db.PersonService#deleteRelationshipType(org.openmrs.RelationshipType)
	 */
	public void deleteRelationshipType(RelationshipType relationshipType) throws DAOException {
		sessionFactory.getCurrentSession().delete(relationshipType);
	}
	
	/**
	 * @see org.openmrs.api.db.PersonDAO#createPerson(org.openmrs.Person)
	 *
	public void createPerson(Person person) throws DAOException {
		sessionFactory.getCurrentSession().save(person);
	}
	 */
	
	/**
	 * @see org.openmrs.api.db.PersonDAO#createPerson(org.openmrs.Person)
	 */
	public Person createPerson(Person person) throws DAOException {
		return (Person)sessionFactory.getCurrentSession().merge(person);
	}

	/**
	 * @see org.openmrs.api.db.PersonDAO#deletePerson(org.openmrs.Person)
	 */
	public void deletePerson(Person person) throws DAOException {
		HibernatePersonDAO.deletePersonAndAttributes(sessionFactory, person);
	}

	/**
	 * @see org.openmrs.api.db.PersonDAO#updatePerson(org.openmrs.Person)
	 */
	public void updatePerson(Person person) throws DAOException {
		if (person.getPersonId() == null)
			createPerson(person);
		else {
			sessionFactory.getCurrentSession().saveOrUpdate(person);
		}
	}

	/**
	 * @see org.openmrs.api.db.PersonService#createRelationship(org.openmrs.Relationship)
	 */
	public void createRelationship(Relationship relationship) throws DAOException {
		relationship.setCreator(Context.getAuthenticatedUser());
		relationship.setDateCreated(new Date());
		sessionFactory.getCurrentSession().save(relationship);
	}
	
	/**
	 * @see org.openmrs.api.db.PersonService#updateRelationship(org.openmrs.Relationship)
	 */
	public void updateRelationship(Relationship relationship) throws DAOException {
		if (relationship.getRelationshipId() == null)
			createRelationship(relationship);
		else
			sessionFactory.getCurrentSession().saveOrUpdate(relationship);
	}	
	
	/**
	 * @see org.openmrs.api.db.PersonService#deleteRelationship(org.openmrs.Relationship)
	 */
	public void deleteRelationship(Relationship relationship) throws DAOException {
		sessionFactory.getCurrentSession().delete(relationship);
	}
	
	/**
	 * @see org.openmrs.api.db.PersonService#voidRelationship(org.openmrs.Relationship)
	 */
	public void voidRelationship(Relationship relationship) throws DAOException {
		relationship.setVoided(true);
		updateRelationship(relationship);
	}

	/**
	 * @see org.openmrs.api.db.PersonService#unvoidRelationship(org.openmrs.Relationship)
	 */
	public void unvoidRelationship(Relationship relationship) throws DAOException {
		relationship.setVoided(false);
		relationship.setVoidedBy(null);
		relationship.setDateVoided(null);
		relationship.setVoidReason(null);
		updateRelationship(relationship);
	}
	
	/**
	 * Used by deletePerson, deletePatient, and deleteUser to remove all 
	 * properties of a person before deleting them.
	 * 
	 * @param sessionFactory the session factory from which to pull the current session
	 * @param person the person to delete
	 */
	public static void deletePersonAndAttributes(SessionFactory sessionFactory, Person person) {
		// delete properties and fields so hibernate can't complain
		for (PersonAddress address : person.getAddresses()) {
			if (address.getDateCreated() == null) {
				sessionFactory.getCurrentSession().evict(address);
				address = null;
			}
			else
				sessionFactory.getCurrentSession().delete(address);
		}
		sessionFactory.getCurrentSession().evict(person.getAddresses());
		person.setAddresses(null);
		
		for (PersonAttribute attribute : person.getAttributes()) {
			if (attribute.getDateCreated() == null)
				sessionFactory.getCurrentSession().evict(attribute);
			else
				sessionFactory.getCurrentSession().delete(attribute);
		}
		sessionFactory.getCurrentSession().evict(person.getAttributes());
		person.setAttributes(null);
		
		for (PersonName name : person.getNames()) {
			if (name.getDateCreated() == null)
				sessionFactory.getCurrentSession().evict(name);
			else
				sessionFactory.getCurrentSession().delete(name);
		}
		sessionFactory.getCurrentSession().evict(person.getNames());
		person.setNames(null);
		
		// finally, just tell hibernate to delete our object
		sessionFactory.getCurrentSession().delete(person);
	}
}
