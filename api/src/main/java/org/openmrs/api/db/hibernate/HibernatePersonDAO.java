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

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.SessionFactory;
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
import org.openmrs.api.db.hibernate.search.LuceneQuery;
import org.openmrs.collection.ListPart;
import org.openmrs.person.PersonMergeLog;
import org.openmrs.util.OpenmrsConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Hibernate specific Person database methods. <br>
 * <br>
 * This class should not be used directly. All database calls should go through the Service layer. <br>
 * <br>
 * Proper use: <code>
 *   PersonService ps = Context.getPersonService();
 *   ps.getPeople("name", false);
 * </code>
 * @see org.openmrs.api.db.PersonDAO
 * @see org.openmrs.api.PersonService
 * @see org.openmrs.api.context.Context
 */
public class HibernatePersonDAO implements PersonDAO {
	
	private static final Logger log = LoggerFactory.getLogger(HibernatePersonDAO.class);
	
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
	 * This method executes a Lucene search on persons based on the soundex filter with one search name given
	 * 
	 * @param name a person should match using soundex representation
	 * @param birthyear the birthyear the searched person should have 
	 * @param includeVoided true if voided person should be included 
	 * @param gender of the person to search for 
	 * @return the set of Persons that match the search criteria 
	 */
	private Set<Person> executeSoundexOnePersonNameQuery(String name, Integer birthyear, boolean includeVoided , String gender) {
		PersonLuceneQuery personLuceneQuery = new PersonLuceneQuery(sessionFactory);
		String query = LuceneQuery.escapeQuery(name);
		int maxResults = HibernatePersonDAO.getMaximumSearchResults();
		LinkedHashSet<Person> people = new LinkedHashSet<>();
		
		LuceneQuery<PersonName> luceneQuery = personLuceneQuery.getSoundexPersonNameQuery(query, birthyear, false, gender);
		ListPart<Object[]> names = luceneQuery.listPartProjection(0, maxResults, "person.personId");
		names.getList().forEach(x -> people.add(getPerson((Integer) x[0])));
		
		return people;
	}
	
	
	/**
	 * This method executes a Lucene search on persons based on the soundex filter with three name elements given
	 *
	 * @param name1 basically represents the first name to be searched for in a person name
	 * @param name2 basically represents the middle name to be searched for in a person name	 
	 * @param name3 basically represents the family name to be searched for in a person name
	 * @param birthyear the birthyear the searched person should have 
	 * @param includeVoided true if voided person should be included 
	 * @param gender of the person to search for 
	 * @return the set of Persons that match the search criteria 
	 */
	private Set<Person> executeSoundexThreePersonNamesQuery(String name1, String name2, String name3, Integer birthyear, boolean includeVoided , String gender) {
		PersonLuceneQuery personLuceneQuery = new PersonLuceneQuery(sessionFactory);
		int maxResults = HibernatePersonDAO.getMaximumSearchResults();
		LinkedHashSet<Person> people = new LinkedHashSet<>();
		
		LuceneQuery<PersonName> luceneQuery = personLuceneQuery.getSoundexPersonNameSearchOnThreeNames(name1, name2, name3, birthyear, false, gender);;
		ListPart<Object[]> names = luceneQuery.listPartProjection(0, maxResults, "person.personId");
		names.getList().forEach(x -> people.add(getPerson((Integer) x[0])));
		
		return people;
	}
	
	/**
	 * This method executes a Lucene search on persons based on the soundex filter with two search names given
	 *
	 * @param searchName1 the first entered name by the user to be searched for
	 * @param searchName2 the second entered name by the user to be searched  for
	 * @param birthyear the birthyear the searched person should have 
	 * @param includeVoided true if voided person should be included 
	 * @param gender of the person to search for 
	 * @return the set of Persons that match the search criteria
	 */
	private Set<Person> executeSoundexTwoPersonNamesQuery(String searchName1, String searchName2, Integer birthyear, boolean includeVoided , String gender) {
		PersonLuceneQuery personLuceneQuery = new PersonLuceneQuery(sessionFactory);
		int maxResults = HibernatePersonDAO.getMaximumSearchResults();
		LinkedHashSet<Person> people = new LinkedHashSet<>();
		
		LuceneQuery<PersonName> luceneQuery = personLuceneQuery.getSoundexPersonNameSearchOnTwoNames(searchName1, searchName2, birthyear, false, gender);;
		ListPart<Object[]> names = luceneQuery.listPartProjection(0, maxResults, "person.personId");
		names.getList().forEach(x -> people.add(getPerson((Integer) x[0])));
		
		return people;
	}
	
	/**
	 * This method executes a Lucence search based on the soundex filter with more than three search names given 
	 * 
	 * @param searchNames the names seperated by space that should be searched for 
	 * @param birthyear the birthyear the searched person should have 
	 * @param includeVoided true if voided person should be included 
	 * @param gender of the person to search for 
	 * @return the set of Persons that match the search criteria
	 */
	private Set<Person> executeSoundexNPersonNamesQuery(String[] searchNames, Integer birthyear, boolean includeVoided , String gender) {
		PersonLuceneQuery personLuceneQuery = new PersonLuceneQuery(sessionFactory);
		int maxResults = HibernatePersonDAO.getMaximumSearchResults();
		LinkedHashSet<Person> people = new LinkedHashSet<>();
		
		LuceneQuery<PersonName> luceneQuery = personLuceneQuery.getSoundexPersonNameSearchOnNNames(searchNames, birthyear, includeVoided, gender);
		ListPart<Object[]> names = luceneQuery.listPartProjection(0, maxResults, "person.personId");
		names.getList().forEach(x -> people.add(getPerson((Integer) x[0])));
		
		return people;
		
	}
	
	/**
	 * @see org.openmrs.api.PersonService#getSimilarPeople(String name, Integer birthyear, String gender)
	 * @see org.openmrs.api.db.PersonDAO#getSimilarPeople(String name, Integer birthyear, String gender)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public Set<Person> getSimilarPeople(String name, Integer birthyear, String gender) throws DAOException {
		if (birthyear == null) {
			birthyear = 0;
		}

		name = name.replaceAll("  ", " ");
		name = name.replace(", ", " ");
		String[] names = name.split(" ");
		
		if (names.length == 1) {
			return  executeSoundexOnePersonNameQuery(name, birthyear, false, gender);
		} else if (names.length == 2) {
			return executeSoundexTwoPersonNamesQuery(names[0], names[1], birthyear, false, gender);
		} else if (names.length == 3) {
			return executeSoundexThreePersonNamesQuery(names[0], names[1], names[2], birthyear, false, gender);
		}
		else if (names.length > 3) {
			return executeSoundexNPersonNamesQuery(names, birthyear, false, gender);
		}
		return new LinkedHashSet<>();
	}
	
	/**
	 * @see org.openmrs.api.db.PersonDAO#getPeople(java.lang.String, java.lang.Boolean)
	 * <strong>Should</strong> get no one by null
	 * <strong>Should</strong> get every one by empty string
	 * <strong>Should</strong> get no one by non-existing attribute
	 * <strong>Should</strong> get no one by non-searchable attribute
	 * <strong>Should</strong> get no one by voided attribute
	 * <strong>Should</strong> get one person by attribute
	 * <strong>Should</strong> get one person by random case attribute
	 * <strong>Should</strong> get one person by searching for a mix of attribute and voided attribute
	 * <strong>Should</strong> get multiple people by single attribute
	 * <strong>Should</strong> get multiple people by multiple attributes
	 * <strong>Should</strong> get no one by non-existing name
	 * <strong>Should</strong> get one person by name
	 * <strong>Should</strong> get one person by random case name
	 * <strong>Should</strong> get multiple people by single name
	 * <strong>Should</strong> get multiple people by multiple names
	 * <strong>Should</strong> get no one by non-existing name and non-existing attribute
	 * <strong>Should</strong> get no one by non-existing name and non-searchable attribute
	 * <strong>Should</strong> get no one by non-existing name and voided attribute
	 * <strong>Should</strong> get one person by name and attribute
	 * <strong>Should</strong> get one person by name and voided attribute
	 * <strong>Should</strong> get multiple people by name and attribute
	 * <strong>Should</strong> get one person by given name
	 * <strong>Should</strong> get multiple people by given name
	 * <strong>Should</strong> get one person by middle name
	 * <strong>Should</strong> get multiple people by middle name
	 * <strong>Should</strong> get one person by family name
	 * <strong>Should</strong> get multiple people by family name
	 * <strong>Should</strong> get one person by family name2
	 * <strong>Should</strong> get multiple people by family name2
	 * <strong>Should</strong> get one person by multiple name parts
	 * <strong>Should</strong> get multiple people by multiple name parts
	 * <strong>Should</strong> get no one by voided name
	 * <strong>Should</strong> not get voided person
	 * <strong>Should</strong> not get dead person
	 * <strong>Should</strong> get single dead person
	 * <strong>Should</strong> get multiple dead people
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<Person> getPeople(String searchString, Boolean dead, Boolean voided) {
		if (searchString == null) {
			return new ArrayList<>();
		}

		int maxResults = HibernatePersonDAO.getMaximumSearchResults();

		boolean includeVoided = (voided != null) ? voided : false;

		if (StringUtils.isBlank(searchString)) {
			Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Person.class);
			if (dead != null) {
				criteria.add(Restrictions.eq("dead", dead));
			}

			if (!includeVoided) {
				criteria.add(Restrictions.eq("personVoided", false));
			}

			criteria.setMaxResults(maxResults);
			return criteria.list();
		}

		String query = LuceneQuery.escapeQuery(searchString);

		PersonLuceneQuery personLuceneQuery = new PersonLuceneQuery(sessionFactory);

		LuceneQuery<PersonName> nameQuery = personLuceneQuery.getPersonNameQueryWithOrParser(query, includeVoided);
		if (dead != null) {
			nameQuery.include("person.dead", dead);
		}
		List<Person> people = new ArrayList<>();

		ListPart<Object[]> names = nameQuery.listPartProjection(0, maxResults, "person.personId");
		names.getList().forEach(name -> people.add(getPerson((Integer) name[0])));

		LuceneQuery<PersonAttribute> attributeQuery = personLuceneQuery.getPersonAttributeQueryWithOrParser(query, includeVoided, nameQuery);
		ListPart<Object[]> attributes = attributeQuery.listPartProjection(0, maxResults, "person.personId");
		attributes.getList().forEach(attribute -> people.add(getPerson((Integer) attribute[0])));

		return people;
	}
	
	@Override
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
	@Override
	public Person getPerson(Integer personId) {
		return (Person) sessionFactory.getCurrentSession().get(Person.class, personId);
	}
	
	/**
	 * @see org.openmrs.api.PersonService#purgePersonAttributeType(org.openmrs.PersonAttributeType)
	 * @see org.openmrs.api.db.PersonDAO#deletePersonAttributeType(org.openmrs.PersonAttributeType)
	 */
	@Override
	public void deletePersonAttributeType(PersonAttributeType type) {
		sessionFactory.getCurrentSession().delete(type);
	}
	
	/**
	 * @see org.openmrs.api.PersonService#savePersonAttributeType(org.openmrs.PersonAttributeType)
	 * @see org.openmrs.api.db.PersonDAO#savePersonAttributeType(org.openmrs.PersonAttributeType)
	 */
	@Override
	public PersonAttributeType savePersonAttributeType(PersonAttributeType type) {
		sessionFactory.getCurrentSession().saveOrUpdate(type);
		return type;
	}
	
	/**
	 * @see org.openmrs.api.PersonService#getPersonAttributeType(java.lang.Integer)
	 * @see org.openmrs.api.db.PersonDAO#getPersonAttributeType(java.lang.Integer)
	 */
	@Override
	public PersonAttributeType getPersonAttributeType(Integer typeId) {
		return (PersonAttributeType) sessionFactory.getCurrentSession().get(PersonAttributeType.class, typeId);
	}
	
	/**
	 * @see org.openmrs.api.PersonService#getPersonAttribute(java.lang.Integer)
	 * @see org.openmrs.api.db.PersonDAO#getPersonAttribute(java.lang.Integer)
	 */
	@Override
	public PersonAttribute getPersonAttribute(Integer id) {
		return (PersonAttribute) sessionFactory.getCurrentSession().get(PersonAttribute.class, id);
	}
	
	/**
	 * @see org.openmrs.api.PersonService#getAllPersonAttributeTypes(boolean)
	 * @see org.openmrs.api.db.PersonDAO#getAllPersonAttributeTypes(boolean)
	 */
	@Override
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
	@Override
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
	@Override
	public Relationship getRelationship(Integer relationshipId) throws DAOException {

		return (Relationship) sessionFactory.getCurrentSession()
		        .get(Relationship.class, relationshipId);
	}
	
	/**
	 * @see org.openmrs.api.PersonService#getAllRelationships(boolean)
	 * @see org.openmrs.api.db.PersonDAO#getAllRelationships(boolean)
	 */
	@Override
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
	@Override
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
	@Override
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
	@Override
	public RelationshipType getRelationshipType(Integer relationshipTypeId) throws DAOException {

		return (RelationshipType) sessionFactory.getCurrentSession().get(
		    RelationshipType.class, relationshipTypeId);
	}
	
	/**
	 * @see org.openmrs.api.PersonService#getRelationshipTypes(java.lang.String, java.lang.Boolean)
	 * @see org.openmrs.api.db.PersonDAO#getRelationshipTypes(java.lang.String, java.lang.Boolean)
	 */
	@Override
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
	@Override
	public RelationshipType saveRelationshipType(RelationshipType relationshipType) throws DAOException {
		sessionFactory.getCurrentSession().saveOrUpdate(relationshipType);
		return relationshipType;
	}
	
	/**
	 * @see org.openmrs.api.PersonService#purgeRelationshipType(org.openmrs.RelationshipType)
	 * @see org.openmrs.api.db.PersonDAO#deleteRelationshipType(org.openmrs.RelationshipType)
	 */
	@Override
	public void deleteRelationshipType(RelationshipType relationshipType) throws DAOException {
		sessionFactory.getCurrentSession().delete(relationshipType);
	}
	
	/**
	 * @see org.openmrs.api.PersonService#purgePerson(org.openmrs.Person)
	 * @see org.openmrs.api.db.PersonDAO#deletePerson(org.openmrs.Person)
	 */
	@Override
	public void deletePerson(Person person) throws DAOException {
		HibernatePersonDAO.deletePersonAndAttributes(sessionFactory, person);
	}
	
	/**
	 * @see org.openmrs.api.PersonService#savePerson(org.openmrs.Person)
	 * @see org.openmrs.api.db.PersonDAO#savePerson(org.openmrs.Person)
	 */
	@Override
	public Person savePerson(Person person) throws DAOException {
		sessionFactory.getCurrentSession().saveOrUpdate(person);
		return person;
	}
	
	/**
	 * @see org.openmrs.api.PersonService#saveRelationship(org.openmrs.Relationship)
	 * @see org.openmrs.api.db.PersonDAO#saveRelationship(org.openmrs.Relationship)
	 */
	@Override
	public Relationship saveRelationship(Relationship relationship) throws DAOException {
		sessionFactory.getCurrentSession().saveOrUpdate(relationship);
		return relationship;
	}
	
	/**
	 * @see org.openmrs.api.PersonService#purgeRelationship(org.openmrs.Relationship)
	 * @see org.openmrs.api.db.PersonDAO#deleteRelationship(org.openmrs.Relationship)
	 */
	@Override
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
	@Override
	public PersonAttributeType getPersonAttributeTypeByUuid(String uuid) {
		return (PersonAttributeType) sessionFactory.getCurrentSession().createQuery(
		    "from PersonAttributeType pat where pat.uuid = :uuid").setString("uuid", uuid).uniqueResult();
	}
	
	/**
	 * @see org.openmrs.api.db.PersonDAO#getSavedPersonAttributeTypeName(org.openmrs.PersonAttributeType)
	 */
	@Override
	public String getSavedPersonAttributeTypeName(PersonAttributeType personAttributeType) {
		SQLQuery sql = sessionFactory.getCurrentSession().createSQLQuery(
		    "select name from person_attribute_type where person_attribute_type_id = :personAttributeTypeId");
		sql.setInteger("personAttributeTypeId", personAttributeType.getId());
		return (String) sql.uniqueResult();
	}

	@Override
	public Boolean getSavedPersonAttributeTypeSearchable(PersonAttributeType personAttributeType) {
		SQLQuery sql = sessionFactory.getCurrentSession().createSQLQuery(
			"select searchable from person_attribute_type where person_attribute_type_id = :personAttributeTypeId");
		sql.setInteger("personAttributeTypeId", personAttributeType.getId());
		return (Boolean) sql.uniqueResult();
	}

	/**
	 * @see org.openmrs.api.db.PersonDAO#getPersonByUuid(java.lang.String)
	 */
	@Override
	public Person getPersonByUuid(String uuid) {
		return (Person) sessionFactory.getCurrentSession().createQuery("from Person p where p.uuid = :uuid").setString(
		    "uuid", uuid).uniqueResult();
	}
	
	@Override
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
	
	@Override
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
	@Override
	public PersonName getPersonNameByUuid(String uuid) {
		return (PersonName) sessionFactory.getCurrentSession().createQuery("from PersonName p where p.uuid = :uuid")
		        .setString("uuid", uuid).uniqueResult();
	}
	
	/**
	 * @see org.openmrs.api.db.PersonDAO#getRelationshipByUuid(java.lang.String)
	 */
	@Override
	public Relationship getRelationshipByUuid(String uuid) {
		return (Relationship) sessionFactory.getCurrentSession().createQuery("from Relationship r where r.uuid = :uuid")
		        .setString("uuid", uuid).uniqueResult();
	}
	
	/**
	 * @see org.openmrs.api.db.PersonDAO#getRelationshipTypeByUuid(java.lang.String)
	 */
	@Override
	public RelationshipType getRelationshipTypeByUuid(String uuid) {
		return (RelationshipType) sessionFactory.getCurrentSession().createQuery(
		    "from RelationshipType rt where rt.uuid = :uuid").setString("uuid", uuid).uniqueResult();
	}
	
	/**
	 * @see org.openmrs.api.db.PersonDAO#getAllRelationshipTypes(boolean)
	 */
	@Override
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
	@Override
	public PersonName savePersonName(PersonName personName) {
		sessionFactory.getCurrentSession().saveOrUpdate(personName);
		return personName;
	}
	
	/**
	 * @see org.openmrs.api.PersonService#savePersonAddress(org.openmrs.PersonAddress)
	 * @see org.openmrs.api.db.PersonDAO#savePersonAddress(org.openmrs.PersonAddress)
	 */
	@Override
	public PersonAddress savePersonAddress(PersonAddress personAddress) {
		sessionFactory.getCurrentSession().saveOrUpdate(personAddress);
		return personAddress;
	}
	
}
