/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.db.hibernate;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Disjunction;
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
import org.openmrs.api.context.Context;
import org.openmrs.api.db.DAOException;
import org.openmrs.api.db.PersonDAO;
import org.openmrs.person.PersonMergeLog;
import org.openmrs.util.OpenmrsConstants;

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
	
	protected final static Log log = LogFactory.getLog(HibernatePersonDAO.class);
	
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
	 * @see org.openmrs.api.PersonService#getSimilarPeople(java.lang.String, java.lang.Integer,
	 *      java.lang.String, java.lang.String)
	 * @see org.openmrs.api.db.PersonDAO#getSimilarPeople(String name, Integer birthyear, String
	 *      gender)
	 */
	@SuppressWarnings("unchecked")
	public Set<Person> getSimilarPeople(String name, Integer birthyear, String gender) throws DAOException {
		if (birthyear == null) {
			birthyear = 0;
		}
		
		Set<Person> people = new LinkedHashSet<Person>();
		
		name = name.replaceAll("  ", " ");
		name = name.replace(", ", " ");
		String[] names = name.split(" ");
		
		StringBuilder q = new StringBuilder(
		        "select p from Person p left join p.names as pname where p.personVoided = false and pname.voided = false and ");
		
		if (names.length == 1) {
			q.append("(").append(" soundex(pname.givenName) = soundex(:n1)").append(
			    " or soundex(pname.middleName) = soundex(:n1)").append(" or soundex(pname.familyName) = soundex(:n1) ")
			        .append(" or soundex(pname.familyName2) = soundex(:n1) ").append(")");
		} else if (names.length == 2) {
			q.append("(").append(" case").append("  when pname.givenName is null then 1").append(
			    "  when pname.givenName = '' then 1").append("  when soundex(pname.givenName) = soundex(:n1) then 4")
			        .append("  when soundex(pname.givenName) = soundex(:n2) then 3").append("  else 0 ").append(" end")
			        .append(" + ").append(" case").append("  when pname.middleName is null then 1").append(
			            "  when pname.middleName = '' then 1").append(
			            "  when soundex(pname.middleName) = soundex(:n1) then 3").append(
			            "  when soundex(pname.middleName) = soundex(:n2) then 4").append("  else 0 ").append(" end").append(
			            " + ").append(" case").append("  when pname.familyName is null then 1").append(
			            "  when pname.familyName = '' then 1").append(
			            "  when soundex(pname.familyName) = soundex(:n1) then 3").append(
			            "  when soundex(pname.familyName) = soundex(:n2) then 4").append("  else 0 ").append(" end").append(
			            " +").append(" case").append("  when pname.familyName2 is null then 1").append(
			            "  when pname.familyName2 = '' then 1").append(
			            "  when soundex(pname.familyName2) = soundex(:n1) then 3").append(
			            "  when soundex(pname.familyName2) = soundex(:n2) then 4").append("  else 0 ").append(" end")
			        .append(") > 6");
		} else if (names.length == 3) {
			q.append("(").append(" case").append("  when pname.givenName is null then 0").append(
			    "  when soundex(pname.givenName) = soundex(:n1) then 3").append(
			    "  when soundex(pname.givenName) = soundex(:n2) then 2").append(
			    "  when soundex(pname.givenName) = soundex(:n3) then 1").append("  else 0 ").append(" end").append(" + ")
			        .append(" case").append("  when pname.middleName is null then 0").append(
			            "  when soundex(pname.middleName) = soundex(:n1) then 2").append(
			            "  when soundex(pname.middleName) = soundex(:n2) then 3").append(
			            "  when soundex(pname.middleName) = soundex(:n3) then 1").append("  else 0").append(" end").append(
			            " + ").append(" case").append("  when pname.familyName is null then 0").append(
			            "  when soundex(pname.familyName) = soundex(:n1) then 1").append(
			            "  when soundex(pname.familyName) = soundex(:n2) then 2").append(
			            "  when soundex(pname.familyName) = soundex(:n3) then 3").append("  else 0").append(" end").append(
			            " +").append(" case").append("  when pname.familyName2 is null then 0").append(
			            "  when soundex(pname.familyName2) = soundex(:n1) then 1").append(
			            "  when soundex(pname.familyName2) = soundex(:n2) then 2").append(
			            "  when soundex(pname.familyName2) = soundex(:n3) then 3").append("  else 0").append(" end").append(
			            ") >= 5");
		} else {
			
			// This is simply an alternative method of name matching which scales better
			// for large names, although it is hard to imagine getting names with more than
			// six or so tokens.  This can be easily updated to attain more desirable
			// results; it is just a working alternative to throwing an exception.
			
			q.append("(").append(" case").append("  when pname.givenName is null then 0");
			for (int i = 0; i < names.length; i++) {
				q.append("  when soundex(pname.givenName) = soundex(:n").append(i + 1).append(") then 1");
			}
			q.append("  else 0").append(" end").append(")").append("+").append("(").append(" case").append(
			    "  when pname.middleName is null then 0");
			for (int i = 0; i < names.length; i++) {
				q.append("  when soundex(pname.middleName) = soundex(:n").append(i + 1).append(") then 1");
			}
			q.append("  else 0").append(" end").append(")").append("+").append("(").append(" case").append(
			    "  when pname.familyName is null then 0");
			for (int i = 0; i < names.length; i++) {
				q.append("  when soundex(pname.familyName) = soundex(:n").append(i + 1).append(") then 1");
			}
			q.append("  else 0").append(" end").append(")").append("+").append("(").append(" case").append(
			    "  when pname.familyName2 is null then 0");
			for (int i = 0; i < names.length; i++) {
				q.append("  when soundex(pname.familyName2) = soundex(:n").append(i + 1).append(") then 1");
			}
			q.append("  else 0").append(" end").append(") >= ").append((int) (names.length * .75)); // if most of the names have at least a hit somewhere
		}
		
		String birthdayMatch = " (year(p.birthdate) between " + (birthyear - 1) + " and " + (birthyear + 1)
		        + " or p.birthdate is null) ";
		
		String genderMatch = " (p.gender = :gender or p.gender = '') ";
		
		if (birthyear != 0 && gender != null) {
			q.append(" and (" + birthdayMatch + "and " + genderMatch + ") ");
		} else if (birthyear != 0) {
			q.append(" and " + birthdayMatch);
		} else if (gender != null) {
			q.append(" and " + genderMatch);
		}
		
		q.append(" order by pname.givenName asc,").append(" pname.middleName asc,").append(" pname.familyName asc,").append(
		    " pname.familyName2 asc");
		
		String qStr = q.toString();
		Query query = sessionFactory.getCurrentSession().createQuery(qStr);
		
		for (int nameIndex = 0; nameIndex < names.length; nameIndex++) {
			query.setString("n" + (nameIndex + 1), names[nameIndex]);
		}
		
		if (qStr.contains(":gender")) {
			query.setString("gender", gender);
		}
		
		people.addAll(query.list());
		
		return people;
	}
	
	/**
	 * @see org.openmrs.api.db.PersonDAO#getPeople(java.lang.String, java.lang.Boolean)
	 * @should get no one by null
	 * @should get every one by empty string
	 * @should get no one by non-existing attribute
	 * @should get no one by non-searchable attribute
	 * @should get no one by voided attribute
	 * @should get one person by attribute
	 * @should get one person by random case attribute
	 * @should get one person by searching for a mix of attribute and voided attribute
	 * @should get multiple people by single attribute
	 * @should get multiple people by multiple attributes
	 * @should get no one by non-existing name
	 * @should get one person by name
	 * @should get one person by random case name
	 * @should get multiple people by single name
	 * @should get multiple people by multiple names
	 * @should get no one by non-existing name and non-existing attribute
	 * @should get no one by non-existing name and non-searchable attribute
	 * @should get no one by non-existing name and voided attribute
	 * @should get one person by name and attribute
	 * @should get one person by name and voided attribute
	 * @should get multiple people by name and attribute
	 * @should get one person by given name
	 * @should get multiple people by given name
	 * @should get one person by middle name
	 * @should get multiple people by middle name
	 * @should get one person by family name
	 * @should get multiple people by family name
	 * @should get one person by family name2
	 * @should get multiple people by family name2
	 * @should get one person by multiple name parts
	 * @should get multiple people by multiple name parts
	 * @should get no one by voided name
	 * @should not get voided person
	 * @should not get dead person
	 * @should get single dead person
	 * @should get multiple dead people
	 */
	@SuppressWarnings("unchecked")
	public List<Person> getPeople(String searchString, Boolean dead, Boolean voided) {
		if (searchString == null) {
			return new ArrayList<Person>();
		}
		
		PersonSearchCriteria personSearchCriteria = new PersonSearchCriteria();
		
		searchString = searchString.replace(", ", " ");
		String[] values = searchString.split(" ");
		
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Person.class);
		
		personSearchCriteria.addAliasForName(criteria);
		personSearchCriteria.addAliasForAttribute(criteria);
		if (voided == null || voided == false) {
			criteria.add(Restrictions.eq("personVoided", false));
		}
		if (dead != null) {
			criteria.add(Restrictions.eq("dead", dead));
		}
		
		Disjunction disjunction = Restrictions.disjunction();
		MatchMode matchMode = personSearchCriteria.getAttributeMatchMode();
		
		for (String value : values) {
			if (value != null && value.length() > 0) {
				disjunction.add(personSearchCriteria.prepareCriterionForName(value, voided)).add(
				    personSearchCriteria.prepareCriterionForAttribute(value, voided, matchMode));
			}
		}
		criteria.add(disjunction);
		
		criteria.addOrder(Order.asc("personId"));
		criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
		criteria.setMaxResults(getMaximumSearchResults());
		
		// TODO - remove
		log.debug(criteria.toString());
		
		return criteria.list();
	}
	
	public List<Person> getPeople(String searchString, Boolean dead) {
		return getPeople(searchString, dead, null);
	}
	
	/**
	 * Fetch the max results value from the global properties table
	 * 
	 * @return Integer value for the person search max results global property
	 */
	public static Integer getMaximumSearchResults() {
		try {
			return Integer.valueOf(Context.getAdministrationService().getGlobalProperty(
			    OpenmrsConstants.GLOBAL_PROPERTY_PERSON_SEARCH_MAX_RESULTS,
			    String.valueOf(OpenmrsConstants.GLOBAL_PROPERTY_PERSON_SEARCH_MAX_RESULTS_DEFAULT_VALUE)));
		}
		catch (Exception e) {
			log.warn("Unable to convert the global property " + OpenmrsConstants.GLOBAL_PROPERTY_PERSON_SEARCH_MAX_RESULTS
			        + "to a valid integer. Returning the default "
			        + OpenmrsConstants.GLOBAL_PROPERTY_PERSON_SEARCH_MAX_RESULTS_DEFAULT_VALUE);
		}
		
		return OpenmrsConstants.GLOBAL_PROPERTY_PERSON_SEARCH_MAX_RESULTS_DEFAULT_VALUE;
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
			criteria.add(Restrictions.eq("retired", false));
		}
		
		criteria.addOrder(Order.asc("sortWeight"));
		
		return criteria.list();
	}
	
	/**
	 * @see org.openmrs.api.db.PersonDAO#getPersonAttributeTypes(java.lang.String, java.lang.String,
	 *      java.lang.Integer, java.lang.Boolean)
	 */
	// TODO - PersonServiceTest fails here
	@SuppressWarnings("unchecked")
	public List<PersonAttributeType> getPersonAttributeTypes(String exactName, String format, Integer foreignKey,
	        Boolean searchable) throws DAOException {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(PersonAttributeType.class, "r");
		
		if (exactName != null) {
			criteria.add(Restrictions.eq("name", exactName));
		}
		
		if (format != null) {
			criteria.add(Restrictions.eq("format", format));
		}
		
		if (foreignKey != null) {
			criteria.add(Restrictions.eq("foreignKey", foreignKey));
		}
		
		if (searchable != null) {
			criteria.add(Restrictions.eq("searchable", searchable));
		}
		
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
			criteria.add(Restrictions.eq("voided", false));
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
		
		if (fromPerson != null) {
			criteria.add(Restrictions.eq("personA", fromPerson));
		}
		if (toPerson != null) {
			criteria.add(Restrictions.eq("personB", toPerson));
		}
		if (relType != null) {
			criteria.add(Restrictions.eq("relationshipType", relType));
		}
		
		criteria.add(Restrictions.eq("voided", false));
		
		return criteria.list();
	}
	
	/**
	 * @see org.openmrs.api.PersonService#getRelationships(org.openmrs.Person, org.openmrs.Person,
	 *      org.openmrs.RelationshipType, java.util.Date, java.util.Date)
	 * @see org.openmrs.api.db.PersonDAO#getRelationships(org.openmrs.Person, org.openmrs.Person,
	 *      org.openmrs.RelationshipType, java.util.Date, java.util.Date)
	 */
	@SuppressWarnings("unchecked")
	public List<Relationship> getRelationships(Person fromPerson, Person toPerson, RelationshipType relType,
	        Date startEffectiveDate, Date endEffectiveDate) {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Relationship.class, "r");
		
		if (fromPerson != null) {
			criteria.add(Restrictions.eq("personA", fromPerson));
		}
		if (toPerson != null) {
			criteria.add(Restrictions.eq("personB", toPerson));
		}
		if (relType != null) {
			criteria.add(Restrictions.eq("relationshipType", relType));
		}
		if (startEffectiveDate != null) {
			criteria.add(Restrictions.disjunction().add(
			    Restrictions.and(Restrictions.le("startDate", startEffectiveDate), Restrictions.ge("endDate",
			        startEffectiveDate))).add(
			    Restrictions.and(Restrictions.le("startDate", startEffectiveDate), Restrictions.isNull("endDate"))).add(
			    Restrictions.and(Restrictions.isNull("startDate"), Restrictions.ge("endDate", startEffectiveDate))).add(
			    Restrictions.and(Restrictions.isNull("startDate"), Restrictions.isNull("endDate"))));
		}
		if (endEffectiveDate != null) {
			criteria.add(Restrictions.disjunction().add(
			    Restrictions.and(Restrictions.le("startDate", endEffectiveDate), Restrictions
			            .ge("endDate", endEffectiveDate))).add(
			    Restrictions.and(Restrictions.le("startDate", endEffectiveDate), Restrictions.isNull("endDate"))).add(
			    Restrictions.and(Restrictions.isNull("startDate"), Restrictions.ge("endDate", endEffectiveDate))).add(
			    Restrictions.and(Restrictions.isNull("startDate"), Restrictions.isNull("endDate"))));
		}
		criteria.add(Restrictions.eq("voided", false));
		
		return criteria.list();
	}
	
	/**
	 * @see org.openmrs.api.PersonService#getRelationshipType(java.lang.Integer)
	 * @see org.openmrs.api.db.PersonDAO#getRelationshipType(java.lang.Integer)
	 */
	public RelationshipType getRelationshipType(Integer relationshipTypeId) throws DAOException {
		RelationshipType relationshipType = (RelationshipType) sessionFactory.getCurrentSession().get(
		    RelationshipType.class, relationshipTypeId);
		
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
		
		if (preferred != null) {
			criteria.add(Restrictions.eq("preferred", preferred));
		}
		
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
			} else {
				sessionFactory.getCurrentSession().delete(address);
			}
		}
		person.setAddresses(null);
		
		for (PersonAttribute attribute : person.getAttributes()) {
			if (attribute.getDateCreated() == null) {
				sessionFactory.getCurrentSession().evict(attribute);
			} else {
				sessionFactory.getCurrentSession().delete(attribute);
			}
		}
		person.setAttributes(null);
		
		for (PersonName name : person.getNames()) {
			if (name.getDateCreated() == null) {
				sessionFactory.getCurrentSession().evict(name);
			} else {
				sessionFactory.getCurrentSession().delete(name);
			}
		}
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
	 * @see org.openmrs.api.db.PersonDAO#getSavedPersonAttributeTypeName(org.openmrs.PersonAttributeType)
	 */
	public String getSavedPersonAttributeTypeName(PersonAttributeType personAttributeType) {
		SQLQuery sql = sessionFactory.getCurrentSession().createSQLQuery(
		    "select name from person_attribute_type where person_attribute_type_id = :personAttributeTypeId");
		sql.setInteger("personAttributeTypeId", personAttributeType.getId());
		return (String) sql.uniqueResult();
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
	
	/**
	 * @see org.openmrs.api.db.PersonDAO#savePersonMergeLog(PersonMergeLog)
	 */
	@Override
	public PersonMergeLog savePersonMergeLog(PersonMergeLog personMergeLog) throws DAOException {
		sessionFactory.getCurrentSession().saveOrUpdate(personMergeLog);
		return personMergeLog;
	}
	
	/**
	 * @see org.openmrs.api.db.PersonDAO#getPersonMergeLog(java.lang.Integer)
	 */
	@Override
	public PersonMergeLog getPersonMergeLog(Integer id) throws DAOException {
		return (PersonMergeLog) sessionFactory.getCurrentSession().get(PersonMergeLog.class, id);
	}
	
	/**
	 * @see org.openmrs.api.db.PersonDAO#getPersonMergeLogByUuid(String)
	 */
	@Override
	public PersonMergeLog getPersonMergeLogByUuid(String uuid) throws DAOException {
		return (PersonMergeLog) sessionFactory.getCurrentSession().createQuery("from PersonMergeLog p where p.uuid = :uuid")
		        .setString("uuid", uuid).uniqueResult();
	}
	
	/**
	 * @see org.openmrs.api.db.PersonDAO#getWinningPersonMergeLogs(org.openmrs.Person)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<PersonMergeLog> getWinningPersonMergeLogs(Person person) throws DAOException {
		return (List<PersonMergeLog>) sessionFactory.getCurrentSession().createQuery(
		    "from PersonMergeLog p where p.winner.id = :winnerId").setInteger("winnerId", person.getId()).list();
	}
	
	/**
	 * @see org.openmrs.api.db.PersonDAO#getLosingPersonMergeLogs(org.openmrs.Person)
	 */
	@Override
	public PersonMergeLog getLosingPersonMergeLogs(Person person) throws DAOException {
		return (PersonMergeLog) sessionFactory.getCurrentSession().createQuery(
		    "from PersonMergeLog p where p.loser.id = :loserId").setInteger("loserId", person.getId()).uniqueResult();
	}
	
	/**
	 * @see org.openmrs.api.db.PersonDAO#getAllPersonMergeLogs()
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<PersonMergeLog> getAllPersonMergeLogs() throws DAOException {
		return (List<PersonMergeLog>) sessionFactory.getCurrentSession().createQuery("from PersonMergeLog p").list();
	}
	
	public PersonAttribute getPersonAttributeByUuid(String uuid) {
		return (PersonAttribute) sessionFactory.getCurrentSession().createQuery(
		    "from PersonAttribute p where p.uuid = :uuid").setString("uuid", uuid).uniqueResult();
	}
	
	/**
	 * @see org.openmrs.api.db.PersonDAO#getPersonName(Integer)
	 */
	@Override
	public PersonName getPersonName(Integer personNameId) {
		return (PersonName) sessionFactory.getCurrentSession().get(PersonName.class, personNameId);
	}
	
	/**
	 * @see org.openmrs.api.db.PersonDAO#getPersonNameByUuid(String)
	 */
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
	
	/**
	 * @see org.openmrs.api.db.PersonDAO#getAllRelationshipTypes(boolean)
	 */
	@SuppressWarnings("unchecked")
	public List<RelationshipType> getAllRelationshipTypes(boolean includeRetired) {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(RelationshipType.class);
		criteria.addOrder(Order.asc("weight"));
		
		if (!includeRetired) {
			criteria.add(Restrictions.eq("retired", false));
		}
		
		return criteria.list();
	}
	
	/**
	 * @see org.openmrs.api.PersonService#savePersonName(org.openmrs.PersonName)
	 * @see org.openmrs.api.db.PersonDAO#savePersonName(org.openmrs.PersonName)
	 */
	public PersonName savePersonName(PersonName personName) {
		sessionFactory.getCurrentSession().saveOrUpdate(personName);
		return personName;
	}
	
	/**
	 * @see org.openmrs.api.PersonService#savePersonAddress(org.openmrs.PersonAddress)
	 * @see org.openmrs.api.db.PersonDAO#savePersonAddress(org.openmrs.PersonAddress)
	 */
	public PersonAddress savePersonAddress(PersonAddress personAddress) {
		sessionFactory.getCurrentSession().saveOrUpdate(personAddress);
		return personAddress;
	}
	
}
