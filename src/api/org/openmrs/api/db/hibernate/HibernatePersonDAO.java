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

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.type.StringType;
import org.openmrs.Person;
import org.openmrs.PersonAddress;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonAttributeType;
import org.openmrs.PersonName;
import org.openmrs.Relationship;
import org.openmrs.RelationshipType;
import org.openmrs.api.db.DAOException;
import org.openmrs.api.db.PersonDAO;

/**
 * Hibernate specific Person database methods. <br/>
 * <br/>
 * This class should not be used directly. All database calls should go through the Service layer. <br/>
 * <br/>
 * Proper use: <code>
 *   PersonService ps = Context.getPersonService();
 *   ps.getPeople("name", false);
 * </code>
 * 
 * @see org.openmrs.api.db.PersonDAO
 * @see org.openmrs.api.PersonService
 * @see org.openmrs.api.context.Context
 */
public class HibernatePersonDAO implements PersonDAO {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	/**
	 * Hibernate session factory
	 */
	private SessionFactory sessionFactory;
	
	/**
	 * Set session factory
	 * 
	 * @param sessionFactory
	 */
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	/**
	 * @see org.openmrs.api.PersonService#getSimilarPeople(java.lang.String,java.lang.Integer,java.lang.String,java.lang.String)
	 * @see org.openmrs.api.db.PersonDAO#getSimilarPeople(java.lang.String,java.lang.Integer,java.lang.String,java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	public Set<Person> getSimilarPeople(String name, Integer birthyear, String gender, String personType) throws DAOException {
		if (birthyear == null)
			birthyear = 0;
		
		// TODO return the matched name instead of the primary name
		// possible solution: "select new" org.openmrs.PersonListItem and return
		// a list of those
		
		Set<Person> people = new LinkedHashSet<Person>();
		
		name = name.replaceAll("  ", " ");
		name = name.replace(", ", " ");
		String[] names = name.split(" ");
		
		String q = "select p from Person p left join p.names as pname where p.personVoided = false and pname.voided = false and ";
		
		if (names.length == 1) {
			q += "(";
			q += " soundex(pname.givenName) = soundex(:n1)";
			q += " or soundex(pname.middleName) = soundex(:n1)";
			q += " or soundex(pname.familyName) = soundex(:n1) ";
			q += " or soundex(pname.familyName2) = soundex(:n1) ";
			q += ")";
		} else if (names.length == 2) {
			q += "(";
			q += " case";
			q += "  when pname.givenName is null then 1";
			q += "  when pname.givenName = '' then 1";
			q += "  when soundex(pname.givenName) = soundex(:n1) then 4";
			q += "  when soundex(pname.givenName) = soundex(:n2) then 3";
			q += "  else 0 ";
			q += " end";
			q += " + ";
			q += " case";
			q += "  when pname.middleName is null then 1";
			q += "  when pname.middleName = '' then 1";
			q += "  when soundex(pname.middleName) = soundex(:n1) then 3";
			q += "  when soundex(pname.middleName) = soundex(:n2) then 4";
			q += "  else 0 ";
			q += " end";
			q += " + ";
			q += " case";
			q += "  when pname.familyName is null then 1";
			q += "  when pname.familyName = '' then 1";
			q += "  when soundex(pname.familyName) = soundex(:n1) then 3";
			q += "  when soundex(pname.familyName) = soundex(:n2) then 4";
			q += "  else 0 ";
			q += " end";
			q += " +";
			q += " case";
			q += "  when pname.familyName2 is null then 1";
			q += "  when pname.familyName2 = '' then 1";
			q += "  when soundex(pname.familyName2) = soundex(:n1) then 3";
			q += "  when soundex(pname.familyName2) = soundex(:n2) then 4";
			q += "  else 0 ";
			q += " end";
			q += ") > 6";
		} else if (names.length == 3) {
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
			q += "  when soundex(pname.middleName) = soundex(:n1) then 2";
			q += "  when soundex(pname.middleName) = soundex(:n2) then 3";
			q += "  when soundex(pname.middleName) = soundex(:n3) then 1";
			q += "  else 0";
			q += " end";
			q += " + ";
			q += " case";
			q += "  when pname.familyName is null then 0";
			q += "  when soundex(pname.familyName) = soundex(:n1) then 1";
			q += "  when soundex(pname.familyName) = soundex(:n2) then 2";
			q += "  when soundex(pname.familyName) = soundex(:n3) then 3";
			q += "  else 0";
			q += " end";
			q += " +";
			q += " case";
			q += "  when pname.familyName2 is null then 0";
			q += "  when soundex(pname.familyName2) = soundex(:n1) then 1";
			q += "  when soundex(pname.familyName2) = soundex(:n2) then 2";
			q += "  when soundex(pname.familyName2) = soundex(:n3) then 3";
			q += "  else 0";
			q += " end";
			q += ") >= 5";
		} else {
			
			// This is simply an alternative method of name matching which scales better
			// for large names, although it is hard to imagine getting names with more than 
			// six or so tokens.  This can be easily updated to attain more desirable 
			// results; it is just a working alternative to throwing an exception.
			
			q += "(";
			q += " case";
			q += "  when pname.givenName is null then 0";
			for (int i = 0; i < names.length; i++) {
				q += "  when soundex(pname.givenName) = soundex(:n" + (i + 1) + ") then 1";
			}
			q += "  else 0";
			q += " end";
			q += ")";
			q += "+";
			q += "(";
			q += " case";
			q += "  when pname.middleName is null then 0";
			for (int i = 0; i < names.length; i++) {
				q += "  when soundex(pname.middleName) = soundex(:n" + (i + 1) + ") then 1";
			}
			q += "  else 0";
			q += " end";
			q += ")";
			q += "+";
			q += "(";
			q += " case";
			q += "  when pname.familyName is null then 0";
			for (int i = 0; i < names.length; i++) {
				q += "  when soundex(pname.familyName) = soundex(:n" + (i + 1) + ") then 1";
			}
			q += "  else 0";
			q += " end";
			q += ")";
			q += "+";
			q += "(";
			q += " case";
			q += "  when pname.familyName2 is null then 0";
			for (int i = 0; i < names.length; i++) {
				q += "  when soundex(pname.familyName2) = soundex(:n" + (i + 1) + ") then 1";
			}
			q += "  else 0";
			q += " end";
			q += ") >= " + (int) (names.length * .75); // if most of the names have at least a hit somewhere
		}
		
		String birthdayMatch = " (year(p.birthdate) between " + (birthyear - 1) + " and " + (birthyear + 1)
		        + " or p.birthdate is null) ";
		
		String genderMatch = " (p.gender = :gender or p.gender = '') ";
		
		if (birthyear != 0 && gender != null) {
			q += " and (" + birthdayMatch + "and " + genderMatch + ") ";
		} else if (birthyear != 0) {
			q += " and " + birthdayMatch;
		} else if (gender != null) {
			q += " and " + genderMatch;
		}
		
		if (personType.equals("patient"))
			q += " and p.user = 0";
		
		if (personType.equals("user"))
			q += " and p.patient = 0";
		
		q += " order by pname.givenName asc,";
		q += " pname.middleName asc,";
		q += " pname.familyName asc";
		q += " pname.familyName2 asc";
		
		Query query = sessionFactory.getCurrentSession().createQuery(q);
		
		for (int nameIndex = 0; nameIndex < names.length; nameIndex++) {
			query.setString("n" + (nameIndex + 1), names[nameIndex]);
		}
		
		if (q.contains(":gender"))
			query.setString("gender", gender);
		
		people.addAll(query.list());
		
		return people;
	}
	
	/**
	 * @see org.openmrs.api.db.PersonDAO#getPeople(java.lang.String, java.lang.Boolean)
	 */
	@SuppressWarnings("unchecked")
	public List<Person> getPeople(String name, Boolean dead) {
		name = name.replace(", ", " ");
		String[] names = name.split(" ");
		
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Person.class);
		criteria.createAlias("names", "name");
		for (String n : names) {
			if (n != null && n.length() > 0) {
				criteria.add(Expression.or(Expression.like("name.givenName", n, MatchMode.START), Expression.or(Expression
				        .like("name.familyName", n, MatchMode.START), Expression.or(Expression.like("name.middleName", n,
				    MatchMode.START), Expression.like("name.familyName2", n, MatchMode.START)))));
			}
		}
		
		criteria.add(Expression.eq("personVoided", false));
		if (dead != null)
			criteria.add(Expression.eq("dead", dead));
		criteria.addOrder(Order.asc("personId"));
		criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
		return criteria.list();
	}
	
	/**
	 * @see org.openmrs.api.PersonService#getPerson(java.lang.Integer)
	 * @see org.openmrs.api.db.PersonDAO#getPerson(java.lang.Integer)
	 */
	public Person getPerson(Integer personId) {
		return (Person) sessionFactory.getCurrentSession().get(Person.class, personId);
	}
	
	/**
	 * @see org.openmrs.api.PersonService#deletePersonAttributeType(org.openmrs.PersonAttributeType)
	 * @see org.openmrs.api.db.PersonDAO#deletePersonAttributeType(org.openmrs.PersonAttributeType)
	 */
	public void deletePersonAttributeType(PersonAttributeType type) {
		sessionFactory.getCurrentSession().delete(type);
	}
	
	/**
	 * @see org.openmrs.api.PersonService#savePersonAttributeType(org.openmrs.PersonAttributeType)
	 * @see org.openmrs.api.db.PersonDAO#savePersonAttributeType(org.openmrs.PersonAttributeType)
	 */
	public PersonAttributeType savePersonAttributeType(PersonAttributeType type) {
		sessionFactory.getCurrentSession().saveOrUpdate(type);
		return type;
	}
	
	/**
	 * @see org.openmrs.api.PersonService#getPersonAttributeType(java.lang.Integer)
	 * @see org.openmrs.api.db.PersonDAO#getPersonAttributeType(java.lang.Integer)
	 */
	public PersonAttributeType getPersonAttributeType(Integer typeId) {
		return (PersonAttributeType) sessionFactory.getCurrentSession().get(PersonAttributeType.class, typeId);
	}
	
	/**
	 * @see org.openmrs.api.PersonService#getPersonAttribute(java.lang.Integer)
	 * @see org.openmrs.api.db.PersonDAO#getPersonAttribute(java.lang.Integer)
	 */
	public PersonAttribute getPersonAttribute(Integer id) {
		return (PersonAttribute) sessionFactory.getCurrentSession().get(PersonAttribute.class, id);
	}
	
	/**
	 * @see org.openmrs.api.PersonService#getAllPersonAttributeTypes(boolean)
	 * @see org.openmrs.api.db.PersonDAO#getAllPersonAttributeTypes(boolean)
	 */
	@SuppressWarnings("unchecked")
	public List<PersonAttributeType> getAllPersonAttributeTypes(boolean includeRetired) throws DAOException {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(PersonAttributeType.class, "r");
		
		if (!includeRetired) {
			criteria.add(Expression.eq("retired", false));
		}
		
		criteria.addOrder(Order.asc("name"));
		
		return criteria.list();
	}
	
	/**
	 * @see org.openmrs.api.db.PersonDAO#getPersonAttributeTypes(java.lang.String, java.lang.String,
	 *      java.lang.Integer, java.lang.Boolean)
	 */
	@SuppressWarnings("unchecked")
	public List<PersonAttributeType> getPersonAttributeTypes(String exactName, String format, Integer foreignKey,
	                                                         Boolean searchable) throws DAOException {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(PersonAttributeType.class, "r");
		criteria.add(Expression.eq("retired", false));
		
		if (exactName != null)
			criteria.add(Expression.eq("name", exactName));
		
		if (format != null)
			criteria.add(Expression.eq("format", format));
		
		if (foreignKey != null)
			criteria.add(Expression.eq("foreignKey", foreignKey));
		
		if (searchable != null)
			criteria.add(Expression.eq("searchable", format));
		
		return criteria.list();
	}
	
	/**
	 * @see org.openmrs.api.PersonService#getRelationship(java.lang.Integer)
	 * @see org.openmrs.api.db.PersonDAO#getRelationship(java.lang.Integer)
	 */
	public Relationship getRelationship(Integer relationshipId) throws DAOException {
		Relationship relationship = (Relationship) sessionFactory.getCurrentSession()
		        .get(Relationship.class, relationshipId);
		
		return relationship;
	}
	
	/**
	 * @see org.openmrs.api.PersonService#getAllRelationships(boolean)
	 * @see org.openmrs.api.db.PersonDAO#getAllRelationships(boolean)
	 */
	@SuppressWarnings("unchecked")
	public List<Relationship> getAllRelationships(boolean includeVoided) throws DAOException {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Relationship.class, "r");
		
		if (!includeVoided) {
			criteria.add(Expression.eq("voided", false));
		}
		
		return criteria.list();
	}
	
	/**
	 * @see org.openmrs.api.PersonService#getRelationships(org.openmrs.Person, org.openmrs.Person,
	 *      org.openmrs.RelationshipType)
	 * @see org.openmrs.api.db.PersonDAO#getRelationships(org.openmrs.Person, org.openmrs.Person,
	 *      org.openmrs.RelationshipType)
	 */
	@SuppressWarnings("unchecked")
	public List<Relationship> getRelationships(Person fromPerson, Person toPerson, RelationshipType relType) {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Relationship.class, "r");
		
		if (fromPerson != null)
			criteria.add(Expression.eq("personA", fromPerson));
		if (toPerson != null)
			criteria.add(Expression.eq("personB", toPerson));
		if (relType != null)
			criteria.add(Expression.eq("relationshipType", relType));
		
		criteria.add(Expression.eq("voided", false));
		
		return criteria.list();
	}
	
	/**
	 * @see org.openmrs.api.PersonService#getRelationshipType(java.lang.Integer)
	 * @see org.openmrs.api.db.PersonDAO#getRelationshipType(java.lang.Integer)
	 */
	public RelationshipType getRelationshipType(Integer relationshipTypeId) throws DAOException {
		RelationshipType relationshipType = new RelationshipType();
		relationshipType = (RelationshipType) sessionFactory.getCurrentSession().get(RelationshipType.class,
		    relationshipTypeId);
		
		return relationshipType;
	}
	
	/**
	 * @see org.openmrs.api.PersonService#getRelationshipTypes(java.lang.String, java.lang.Boolean)
	 * @see org.openmrs.api.db.PersonDAO#getRelationshipTypes(java.lang.String, java.lang.Boolean)
	 */
	@SuppressWarnings("unchecked")
	public List<RelationshipType> getRelationshipTypes(String relationshipTypeName, Boolean preferred) throws DAOException {
		
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(RelationshipType.class);
		criteria.add(Restrictions.sqlRestriction("CONCAT(a_Is_To_B, CONCAT('/', b_Is_To_A)) like (?)", relationshipTypeName,
		    new StringType()));
		
		if (preferred != null)
			criteria.add(Expression.eq("preferred", preferred));
		
		return criteria.list();
	}
	
	/**
	 * @see org.openmrs.api.PersonService#getAllRelationshipTypes()
	 * @see org.openmrs.api.db.PersonDAO#getAllRelationshipTypes()
	 */
	@SuppressWarnings("unchecked")
	public List<RelationshipType> getAllRelationshipTypes() throws DAOException {
		
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(RelationshipType.class, "t");
		
		/**
		 * if (!includeRetired) criteria.add(Expression.eq("retired", false));
		 */
		
		criteria.addOrder(Order.asc("weight"));
		
		return criteria.list();
	}
	
	/**
	 * @see org.openmrs.api.PersonService#saveRelationshipType(org.openmrs.RelationshipType)
	 * @see org.openmrs.api.db.PersonDAO#saveRelationshipType(org.openmrs.RelationshipType)
	 */
	public RelationshipType saveRelationshipType(RelationshipType relationshipType) throws DAOException {
		sessionFactory.getCurrentSession().saveOrUpdate(relationshipType);
		return relationshipType;
	}
	
	/**
	 * @see org.openmrs.api.PersonService#deleteRelationshipType(org.openmrs.RelationshipType)
	 * @see org.openmrs.api.db.PersonDAO#deleteRelationshipType(org.openmrs.RelationshipType)
	 */
	public void deleteRelationshipType(RelationshipType relationshipType) throws DAOException {
		sessionFactory.getCurrentSession().delete(relationshipType);
	}
	
	/**
	 * @see org.openmrs.api.PersonService#purgePerson(org.openmrs.Person)
	 * @see org.openmrs.api.db.PersonDAO#deletePerson(org.openmrs.Person)
	 */
	public void deletePerson(Person person) throws DAOException {
		HibernatePersonDAO.deletePersonAndAttributes(sessionFactory, person);
	}
	
	/**
	 * @see org.openmrs.api.PersonService#savePerson(org.openmrs.Person)
	 * @see org.openmrs.api.db.PersonDAO#savePerson(org.openmrs.Person)
	 */
	public Person savePerson(Person person) throws DAOException {
		sessionFactory.getCurrentSession().saveOrUpdate(person);
		return person;
	}
	
	/**
	 * @see org.openmrs.api.PersonService#saveRelationship(org.openmrs.Relationship)
	 * @see org.openmrs.api.db.PersonDAO#saveRelationship(org.openmrs.Relationship)
	 */
	public Relationship saveRelationship(Relationship relationship) throws DAOException {
		sessionFactory.getCurrentSession().saveOrUpdate(relationship);
		return relationship;
	}
	
	/**
	 * @see org.openmrs.api.PersonService#purgeRelationship(org.openmrs.Relationship)
	 * @see org.openmrs.api.db.PersonDAO#deleteRelationship(org.openmrs.Relationship)
	 */
	public void deleteRelationship(Relationship relationship) throws DAOException {
		sessionFactory.getCurrentSession().delete(relationship);
	}
	
	/**
	 * Used by deletePerson, deletePatient, and deleteUser to remove all properties of a person
	 * before deleting them.
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
			} else
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
	
	/**
	 * @see org.openmrs.api.db.PersonDAO#getPersonAttributeTypeByUuid(java.lang.String)
	 */
	public PersonAttributeType getPersonAttributeTypeByUuid(String uuid) {
		return (PersonAttributeType) sessionFactory.getCurrentSession().createQuery(
		    "from PersonAttributeType pat where pat.uuid = :uuid").setString("uuid", uuid).uniqueResult();
	}
	
	/**
	 * @see org.openmrs.api.db.PersonDAO#getPersonByUuid(java.lang.String)
	 */
	public Person getPersonByUuid(String uuid) {
		return (Person) sessionFactory.getCurrentSession().createQuery("from Person p where p.uuid = :uuid").setString(
		    "uuid", uuid).uniqueResult();
	}
	
	public PersonAddress getPersonAddressByUuid(String uuid) {
		return (PersonAddress) sessionFactory.getCurrentSession().createQuery("from PersonAddress p where p.uuid = :uuid")
		        .setString("uuid", uuid).uniqueResult();
	}
	
	public PersonAttribute getPersonAttributeByUuid(String uuid) {
		return (PersonAttribute) sessionFactory.getCurrentSession().createQuery(
		    "from PersonAttribute p where p.uuid = :uuid").setString("uuid", uuid).uniqueResult();
	}
	
	public PersonName getPersonNameByUuid(String uuid) {
		return (PersonName) sessionFactory.getCurrentSession().createQuery("from PersonName p where p.uuid = :uuid")
		        .setString("uuid", uuid).uniqueResult();
	}
	
	/**
	 * @see org.openmrs.api.db.PersonDAO#getRelationshipByUuid(java.lang.String)
	 */
	public Relationship getRelationshipByUuid(String uuid) {
		return (Relationship) sessionFactory.getCurrentSession().createQuery("from Relationship r where r.uuid = :uuid")
		        .setString("uuid", uuid).uniqueResult();
	}
	
	/**
	 * @see org.openmrs.api.db.PersonDAO#getRelationshipTypeByUuid(java.lang.String)
	 */
	public RelationshipType getRelationshipTypeByUuid(String uuid) {
		return (RelationshipType) sessionFactory.getCurrentSession().createQuery(
		    "from RelationshipType rt where rt.uuid = :uuid").setString("uuid", uuid).uniqueResult();
	}
}
