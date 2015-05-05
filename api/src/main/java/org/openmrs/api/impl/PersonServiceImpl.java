/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.GlobalProperty;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.PersonAddress;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonAttributeType;
import org.openmrs.PersonName;
import org.openmrs.Relationship;
import org.openmrs.RelationshipType;
import org.openmrs.User;
import org.openmrs.api.APIException;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.PersonAttributeTypeLockedException;
import org.openmrs.api.PersonService;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.PersonDAO;
import org.openmrs.person.PersonMergeLog;
import org.openmrs.person.PersonMergeLogData;
import org.openmrs.serialization.SerializationException;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.OpenmrsConstants.PERSON_TYPE;
import org.openmrs.validator.ValidateUtil;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * Default implementation of the PersonService
 * <p>
 * Which implementation to use is determined by Spring. See the spring application context file in
 * /metadata/api/spring/applicatContext-service.xml
 *
 * @see PersonService
 * @see org.openmrs.api.context.Context
 */
@Transactional
public class PersonServiceImpl extends BaseOpenmrsService implements PersonService {
	
	private Log log = LogFactory.getLog(this.getClass());
	
	private PersonDAO dao;
	
	/**
	 * @see org.openmrs.api.PersonService#setPersonDAO(org.openmrs.api.db.PersonDAO)
	 */
	public void setPersonDAO(PersonDAO dao) {
		this.dao = dao;
	}
	
	/**
	 * @see org.openmrs.api.PersonService#getSimilarPeople(java.lang.String, java.lang.Integer,
	 *      java.lang.String)
	 */
	@Transactional(readOnly = true)
	public Set<Person> getSimilarPeople(String name, Integer birthyear, String gender) throws APIException {
		return dao.getSimilarPeople(name, birthyear, gender);
	}
	
	/**
	 * @see org.openmrs.api.PersonService#getSimilarPeople(java.lang.String, java.lang.Integer,
	 *      java.lang.String, java.lang.String)
	 * @deprecated @see {@link #getSimilarPeople(String, Integer, String)}
	 */
	@Transactional(readOnly = true)
	public Set<Person> getSimilarPeople(String nameSearch, Integer birthyear, String gender, String personType)
	        throws APIException {
		return Context.getPersonService().getSimilarPeople(nameSearch, birthyear, gender);
	}
	
	/**
	 * @see org.openmrs.api.PersonService#getPeople(String, Boolean)
	 */
	@Transactional(readOnly = true)
	public List<Person> getPeople(String searchPhrase, Boolean dead) throws APIException {
		
		return dao.getPeople(searchPhrase, dead);
	}
	
	public List<Person> getPeople(String searchPhrase, Boolean dead, Boolean voided) throws APIException {
		
		return dao.getPeople(searchPhrase, dead, voided);
	}
	
	/**
	 * @deprecated use {@link #getPeople(String, Boolean)}
	 */
	@Transactional(readOnly = true)
	public Set<Person> findPeople(String searchPhrase, boolean includeVoided) {
		if (includeVoided) {
			throw new APIException("Person.should.consider.voided", (Object[]) null);
		}
		
		// convert the list to a set
		Set<Person> matchingPersons = new LinkedHashSet<Person>();
		matchingPersons.addAll(Context.getPersonService().getPeople(searchPhrase, null));
		
		return matchingPersons;
	}
	
	/**
	 * @deprecated use {@link #getPeople(String, Boolean)}
	 */
	@Transactional(readOnly = true)
	public Set<Person> findPeople(String searchPhrase, boolean includeVoided, String roles) {
		List<String> roleList = null;
		
		if (roles != null && roles.length() > 0) {
			String[] splitRoles = roles.split(",");
			for (String role : splitRoles) {
				if (roleList == null) {
					roleList = new ArrayList<String>();
				}
				roleList.add(role);
			}
		}
		
		return Context.getPersonService().findPeople(searchPhrase, includeVoided, roleList);
	}
	
	/**
	 * @deprecated use {@link #getPeople(String, Boolean)}
	 */
	@Transactional(readOnly = true)
	public Set<Person> findPeople(String searchPhrase, boolean includeVoided, List<String> roles) {
		Set<Person> people = new HashSet<Person>();
		
		// If no rules *are not* defined then find all matching persons (users and patients).
		if (roles == null) {
			people.addAll(Context.getPersonService().getPeople(searchPhrase, includeVoided));
		}
		// If roles *are* defined then find matching users who have the given roles.
		else {
			for (User u : Context.getUserService().findUsers(searchPhrase, roles, includeVoided)) {
				people.add(u.getPerson());
			}
		}
		
		return people;
	}
	
	/**
	 * @see org.openmrs.api.PersonService#getAllPersonAttributeTypes()
	 */
	@Transactional(readOnly = true)
	public List<PersonAttributeType> getAllPersonAttributeTypes() throws APIException {
		return Context.getPersonService().getAllPersonAttributeTypes(true);
	}
	
	/**
	 * @see org.openmrs.api.PersonService#getAllPersonAttributeTypes(boolean)
	 */
	@Transactional(readOnly = true)
	public List<PersonAttributeType> getAllPersonAttributeTypes(boolean includeRetired) throws APIException {
		return dao.getAllPersonAttributeTypes(includeRetired);
	}
	
	/**
	 * @see org.openmrs.api.PersonService#getPersonAttributeTypeByName(java.lang.String)
	 */
	@Transactional(readOnly = true)
	public PersonAttributeType getPersonAttributeTypeByName(String typeName) throws APIException {
		List<PersonAttributeType> types = Context.getPersonService().getPersonAttributeTypes(typeName, null, null, null);
		
		if (types.size() < 1) {
			return null;
		} else {
			return types.get(0);
		}
	}
	
	/**
	 * @see org.openmrs.api.PersonService#purgePersonAttributeType(org.openmrs.PersonAttributeType)
	 */
	public void purgePersonAttributeType(PersonAttributeType type) throws APIException {
		checkIfPersonAttributeTypesAreLocked();
		dao.deletePersonAttributeType(type);
	}
	
	/**
	 * @see org.openmrs.api.PersonService#savePersonAttributeType(org.openmrs.PersonAttributeType)
	 */
	public PersonAttributeType savePersonAttributeType(PersonAttributeType type) throws APIException {
		checkIfPersonAttributeTypesAreLocked();
		if (type.getSortWeight() == null) {
			List<PersonAttributeType> allTypes = Context.getPersonService().getAllPersonAttributeTypes();
			if (allTypes.size() > 0) {
				type.setSortWeight(allTypes.get(allTypes.size() - 1).getSortWeight() + 1);
			} else {
				type.setSortWeight(1.0);
			}
		}
		
		if (type.getId() != null) {
			String oldTypeName = dao.getSavedPersonAttributeTypeName(type);
			String newTypeName = type.getName();
			
			if (!oldTypeName.equals(newTypeName)) {
				List<GlobalProperty> props = new ArrayList<GlobalProperty>();
				
				AdministrationService as = Context.getAdministrationService();
				
				for (String propName : OpenmrsConstants.GLOBAL_PROPERTIES_OF_PERSON_ATTRIBUTES) {
					props.add(as.getGlobalPropertyObject(propName));
				}
				
				for (GlobalProperty prop : props) {
					if (prop != null) {
						String propVal = prop.getPropertyValue();
						if (propVal != null && propVal.indexOf(oldTypeName) != -1) {
							prop.setPropertyValue(propVal.replaceFirst(oldTypeName, newTypeName));
							as.saveGlobalProperty(prop);
						}
					}
				}
			}
		}
		
		return dao.savePersonAttributeType(type);
	}
	
	/**
	 * @see org.openmrs.api.PersonService#retirePersonAttributeType(org.openmrs.PersonAttributeType)
	 */
	public PersonAttributeType retirePersonAttributeType(PersonAttributeType type, String retiredReason) throws APIException {
		checkIfPersonAttributeTypesAreLocked();
		if (retiredReason == null || retiredReason.length() < 1) {
			throw new APIException("Person.retiring.reason.required", (Object[]) null);
		}
		
		type.setRetired(true);
		type.setRetiredBy(Context.getAuthenticatedUser());
		type.setRetireReason(retiredReason);
		type.setDateRetired(new Date());
		
		return dao.savePersonAttributeType(type);
	}
	
	/**
	 * @deprecated use {@link #savePersonAttributeType(PersonAttributeType)}
	 */
	public void createPersonAttributeType(PersonAttributeType type) throws APIException {
		Context.getPersonService().savePersonAttributeType(type);
	}
	
	/**
	 * @deprecated use {@link #savePersonAttributeType(PersonAttributeType)}
	 */
	public void updatePersonAttributeType(PersonAttributeType type) throws APIException {
		Context.getPersonService().savePersonAttributeType(type);
	}
	
	/**
	 * @see org.openmrs.api.PersonService#getPersonAttributeTypes(java.lang.String,
	 *      java.lang.String, java.lang.Integer, java.lang.Boolean)
	 */
	@Transactional(readOnly = true)
	public List<PersonAttributeType> getPersonAttributeTypes(String exactName, String format, Integer foreignKey,
	        Boolean searchable) throws APIException {
		return dao.getPersonAttributeTypes(exactName, format, foreignKey, searchable);
	}
	
	/**
	 * @deprecated use {@link #purgePersonAttributeType(PersonAttributeType)}
	 */
	public void deletePersonAttributeType(Integer attrTypeId) {
		Context.getPersonService().deletePersonAttributeType(getPersonAttributeType(attrTypeId));
	}
	
	/**
	 * @deprecated use {@link #purgePersonAttributeType(PersonAttributeType)}
	 */
	public void deletePersonAttributeType(PersonAttributeType type) {
		dao.deletePersonAttributeType(type);
	}
	
	public void unretirePersonAttributeType(PersonAttributeType type) throws APIException {
		checkIfPersonAttributeTypesAreLocked();
		type.setRetired(false);
		type.setDateRetired(null);
		type.setRetiredBy(null);
		type.setRetireReason(null);
		dao.savePersonAttributeType(type);
		
	}
	
	/**
	 * @deprecated use {@link #getAllPersonAttributeTypes()}
	 */
	@Transactional(readOnly = true)
	public List<PersonAttributeType> getPersonAttributeTypes() {
		return Context.getPersonService().getAllPersonAttributeTypes();
	}
	
	/**
	 * @see org.openmrs.api.PersonService#getPersonAttributeType(java.lang.Integer)
	 */
	@Transactional(readOnly = true)
	public PersonAttributeType getPersonAttributeType(Integer typeId) {
		return dao.getPersonAttributeType(typeId);
	}
	
	/**
	 * @see org.openmrs.api.PersonService#getPersonAttribute(java.lang.Integer)
	 */
	@Transactional(readOnly = true)
	public PersonAttribute getPersonAttribute(Integer id) {
		return dao.getPersonAttribute(id);
	}
	
	/**
	 * @deprecated use {@link #getPersonAttributeTypeByName(String)}
	 */
	@Transactional(readOnly = true)
	public PersonAttributeType getPersonAttributeType(String s) {
		return Context.getPersonService().getPersonAttributeTypeByName(s);
	}
	
	/**
	 * @see org.openmrs.api.PersonService#getRelationship(java.lang.Integer)
	 */
	@Transactional(readOnly = true)
	public Relationship getRelationship(Integer relationshipId) throws APIException {
		return dao.getRelationship(relationshipId);
	}
	
	/**
	 * @deprecated use {@link #getAllRelationships()}
	 */
	@Transactional(readOnly = true)
	public List<Relationship> getRelationships() throws APIException {
		return Context.getPersonService().getAllRelationships();
	}
	
	/**
	 * @deprecated use {@link #getRelationshipsByPerson(Person)}
	 */
	@Transactional(readOnly = true)
	public List<Relationship> getRelationships(Person p, boolean showVoided) throws APIException {
		if (showVoided) {
			throw new APIException("Relationship.voided.should.be.considered", (Object[]) null);
		}
		
		return Context.getPersonService().getRelationshipsByPerson(p);
	}
	
	/**
	 * @deprecated use {@link #getRelationshipsByPerson(Person)}
	 */
	@Transactional(readOnly = true)
	public List<Relationship> getRelationships(Person p) throws APIException {
		return Context.getPersonService().getRelationshipsByPerson(p);
	}
	
	/**
	 * @deprecated use {@link #getRelationships(Person, Person, RelationshipType)}
	 */
	@Transactional(readOnly = true)
	public List<Relationship> getRelationshipsTo(Person toPerson, RelationshipType relType) throws APIException {
		return Context.getPersonService().getRelationships(null, toPerson, relType);
	}
	
	/**
	 * @deprecated use {@link #getAllRelationshipTypes()}
	 */
	@Transactional(readOnly = true)
	public List<RelationshipType> getRelationshipTypes() throws APIException {
		return Context.getPersonService().getAllRelationshipTypes();
	}
	
	/**
	 * @see org.openmrs.api.PersonService#getRelationshipType(java.lang.Integer)
	 */
	@Transactional(readOnly = true)
	public RelationshipType getRelationshipType(Integer relationshipTypeId) throws APIException {
		return dao.getRelationshipType(relationshipTypeId);
	}
	
	/**
	 * @deprecated use {@link #getRelationshipTypeByName(String)}
	 */
	@Transactional(readOnly = true)
	public RelationshipType findRelationshipType(String relationshipTypeName) throws APIException {
		return Context.getPersonService().getRelationshipTypeByName(relationshipTypeName);
	}
	
	/**
	 * @see org.openmrs.api.PersonService#getRelationshipTypeByName(java.lang.String)
	 */
	@Transactional(readOnly = true)
	public RelationshipType getRelationshipTypeByName(String relationshipTypeName) throws APIException {
		List<RelationshipType> types = dao.getRelationshipTypes(relationshipTypeName, null);
		
		if (types.size() < 1) {
			return null;
		} else {
			return types.get(0);
		}
	}
	
	/**
	 * @see org.openmrs.api.PersonService#purgePerson(org.openmrs.Person)
	 */
	public void purgePerson(Person person) throws APIException {
		dao.deletePerson(person);
	}
	
	/**
	 * @see org.openmrs.api.PersonService#savePerson(org.openmrs.Person)
	 */
	public Person savePerson(Person person) throws APIException {
		PersonName preferredName = null;
		PersonName possiblePreferredName = person.getPersonName();
		if (possiblePreferredName != null && possiblePreferredName.isPreferred() && !possiblePreferredName.isVoided()) {
			preferredName = possiblePreferredName;
		}
		
		for (PersonName name : person.getNames()) {
			if (preferredName == null && !name.isVoided()) {
				name.setPreferred(true);
				preferredName = name;
				continue;
			}
			
			if (!name.equals(preferredName)) {
				name.setPreferred(false);
			}
		}
		
		PersonAddress preferredAddress = null;
		PersonAddress possiblePreferredAddress = person.getPersonAddress();
		if (possiblePreferredAddress != null && possiblePreferredAddress.isPreferred()
		        && !possiblePreferredAddress.isVoided()) {
			preferredAddress = possiblePreferredAddress;
		}
		
		for (PersonAddress address : person.getAddresses()) {
			if (preferredAddress == null && !address.isVoided()) {
				address.setPreferred(true);
				preferredAddress = address;
				continue;
			}
			
			if (!address.equals(preferredAddress)) {
				address.setPreferred(false);
			}
		}
		
		return dao.savePerson(person);
	}
	
	/**
	 * @deprecated use {@link #savePerson(Person)}
	 */
	public Person createPerson(Person person) throws APIException {
		return Context.getPersonService().savePerson(person);
	}
	
	/**
	 * @deprecated use {@link #savePerson(Person)}
	 */
	public void updatePerson(Person person) throws APIException {
		Context.getPersonService().savePerson(person);
	}
	
	/**
	 * @deprecated use {@link #purgePerson(Person)}
	 */
	public void deletePerson(Person person) throws APIException {
		Context.getPersonService().purgePerson(person);
	}
	
	/**
	 * @see org.openmrs.api.PersonService#voidPerson(org.openmrs.Person, java.lang.String)
	 */
	public Person voidPerson(Person person, String reason) throws APIException {
		if (person == null) {
			return null;
		}
		
		return dao.savePerson(person);
	}
	
	/**
	 * @see org.openmrs.api.PersonService#unvoidPerson(org.openmrs.Person)
	 */
	public Person unvoidPerson(Person person) throws APIException {
		if (person == null) {
			return null;
		}
		
		return dao.savePerson(person);
	}
	
	/**
	 * @see org.openmrs.api.PersonService#getPerson(java.lang.Integer)
	 */
	@Transactional(readOnly = true)
	public Person getPerson(Integer personId) throws APIException {
		if (personId == null) {
			return null;
		}
		return dao.getPerson(personId);
	}
	
	/**
	 * @deprecated use {@link #getPerson(Integer)}
	 */
	@Transactional(readOnly = true)
	public Person getPerson(Patient pat) throws APIException {
		if (pat == null) {
			return null;
		}
		return Context.getPersonService().getPerson(pat.getPatientId());
	}
	
	/**
	 * @deprecated use {@link #getPerson(Integer)}
	 */
	@Transactional(readOnly = true)
	public Person getPerson(User user) throws APIException {
		if (user == null) {
			return null;
		}
		return Context.getPersonService().getPerson(user.getUserId());
	}
	
	/**
	 * @see org.openmrs.api.PersonService#getAllRelationships()
	 */
	@Transactional(readOnly = true)
	public List<Relationship> getAllRelationships() throws APIException {
		return Context.getPersonService().getAllRelationships(false);
	}
	
	/**
	 * @see org.openmrs.api.PersonService#getAllRelationships(boolean)
	 */
	@Transactional(readOnly = true)
	public List<Relationship> getAllRelationships(boolean includeVoided) throws APIException {
		return dao.getAllRelationships(includeVoided);
	}
	
	/**
	 * @see org.openmrs.api.PersonService#getRelationships(org.openmrs.Person, org.openmrs.Person,
	 *      org.openmrs.RelationshipType)
	 */
	@Transactional(readOnly = true)
	public List<Relationship> getRelationships(Person fromPerson, Person toPerson, RelationshipType relType)
	        throws APIException {
		return dao.getRelationships(fromPerson, toPerson, relType);
	}
	
	/**
	 * @see org.openmrs.api.PersonService#getRelationships(org.openmrs.Person, org.openmrs.Person,
	 *      org.openmrs.RelationshipType, java.util.Date)
	 */
	@Transactional(readOnly = true)
	public List<Relationship> getRelationships(Person fromPerson, Person toPerson, RelationshipType relType,
	        Date effectiveDate) throws APIException {
		return dao.getRelationships(fromPerson, toPerson, relType, effectiveDate, null);
	}
	
	/**
	 * @see org.openmrs.api.PersonService#getRelationships(org.openmrs.Person, org.openmrs.Person,
	 *      org.openmrs.RelationshipType, java.util.Date, java.util.Date)
	 */
	@Transactional(readOnly = true)
	public List<Relationship> getRelationships(Person fromPerson, Person toPerson, RelationshipType relType,
	        Date startEffectiveDate, Date endEffectiveDate) throws APIException {
		return dao.getRelationships(fromPerson, toPerson, relType, startEffectiveDate, endEffectiveDate);
	}
	
	/**
	 * @see org.openmrs.api.PersonService#getRelationshipsByPerson(org.openmrs.Person)
	 */
	@Transactional(readOnly = true)
	public List<Relationship> getRelationshipsByPerson(Person p) throws APIException {
		
		// search both the left side and the right side of the relationship
		// for this person
		List<Relationship> rels = Context.getPersonService().getRelationships(p, null, null);
		rels.addAll(Context.getPersonService().getRelationships(null, p, null));
		
		return rels;
	}
	
	/**
	 * @see org.openmrs.api.PersonService#getRelationshipsByPerson(org.openmrs.Person,
	 *      java.util.Date)
	 */
	@Transactional(readOnly = true)
	public List<Relationship> getRelationshipsByPerson(Person p, Date effectiveDate) throws APIException {
		
		// search both the left side and the right side of the relationship
		// for this person
		List<Relationship> rels = Context.getPersonService().getRelationships(p, null, null, effectiveDate);
		rels.addAll(Context.getPersonService().getRelationships(null, p, null, effectiveDate));
		
		return rels;
	}
	
	/**
	 * @see org.openmrs.api.PersonService#purgeRelationship(org.openmrs.Relationship)
	 */
	public void purgeRelationship(Relationship relationship) throws APIException {
		dao.deleteRelationship(relationship);
	}
	
	/**
	 * @see org.openmrs.api.PersonService#saveRelationship(org.openmrs.Relationship)
	 */
	public Relationship saveRelationship(Relationship relationship) throws APIException {
		if (relationship.getPersonA().equals(relationship.getPersonB())) {
			throw new APIException("Person.cannot.same", (Object[]) null);
		}
		
		return dao.saveRelationship(relationship);
	}
	
	/**
	 * @deprecated use {@link #saveRelationship(Relationship)}
	 */
	public void createRelationship(Relationship relationship) throws APIException {
		Context.getPersonService().saveRelationship(relationship);
	}
	
	/**
	 * @deprecated use {@link #saveRelationship(Relationship)}
	 */
	public void updateRelationship(Relationship relationship) throws APIException {
		Context.getPersonService().saveRelationship(relationship);
	}
	
	/**
	 * @deprecated use {@link #purgeRelationship(Relationship)}
	 */
	public void deleteRelationship(Relationship relationship) throws APIException {
		Context.getPersonService().purgeRelationship(relationship);
	}
	
	/**
	 * @see org.openmrs.api.PersonService#voidRelationship(org.openmrs.Relationship,
	 *      java.lang.String)
	 */
	public Relationship voidRelationship(Relationship relationship, String voidReason) throws APIException {
		if (relationship.isVoided()) {
			return relationship;
		}
		
		relationship.setVoided(true);
		if (relationship.getVoidedBy() == null) {
			relationship.setVoidedBy(Context.getAuthenticatedUser());
		}
		if (voidReason != null) {
			relationship.setVoidReason(voidReason);
		}
		relationship.setDateVoided(new Date());
		
		return Context.getPersonService().saveRelationship(relationship);
	}
	
	/**
	 * @see org.openmrs.api.PersonService#unvoidRelationship(org.openmrs.Relationship)
	 */
	public Relationship unvoidRelationship(Relationship relationship) throws APIException {
		relationship.setVoided(false);
		relationship.setVoidedBy(null);
		relationship.setDateVoided(null);
		relationship.setVoidReason(null);
		
		return Context.getPersonService().saveRelationship(relationship);
	}
	
	/**
	 * @deprecated use {@link #saveRelationshipType(RelationshipType)}
	 */
	public void createRelationshipType(RelationshipType relationshipType) throws APIException {
		Context.getPersonService().saveRelationshipType(relationshipType);
	}
	
	/**
	 * @deprecated use {@link #saveRelationshipType(RelationshipType)}
	 */
	public void updateRelationshipType(RelationshipType relationshipType) throws APIException {
		Context.getPersonService().saveRelationshipType(relationshipType);
	}
	
	/**
	 * @deprecated use {@link #purgeRelationshipType(RelationshipType)}
	 */
	public void deleteRelationshipType(RelationshipType relationshipType) throws APIException {
		Context.getPersonService().purgeRelationshipType(relationshipType);
	}
	
	/**
	 * @see org.openmrs.api.PersonService#getAllRelationshipTypes()
	 */
	@Transactional(readOnly = true)
	public List<RelationshipType> getAllRelationshipTypes() throws APIException {
		return Context.getPersonService().getAllRelationshipTypes(false);
	}
	
	/**
	 * @see org.openmrs.api.PersonService#getRelationshipTypes(java.lang.String)
	 */
	@Transactional(readOnly = true)
	public List<RelationshipType> getRelationshipTypes(String searchString) throws APIException {
		
		return Context.getPersonService().getRelationshipTypes(searchString, null);
	}
	
	/**
	 * @see org.openmrs.api.PersonService#getRelationshipTypes(java.lang.String, java.lang.Boolean)
	 */
	@Transactional(readOnly = true)
	public List<RelationshipType> getRelationshipTypes(String relationshipTypeName, Boolean preferred) throws APIException {
		Assert.hasText(relationshipTypeName, "The search string cannot be empty");
		
		return dao.getRelationshipTypes(relationshipTypeName, preferred);
	}
	
	/**
	 * @see org.openmrs.api.PersonService#purgeRelationshipType(org.openmrs.RelationshipType)
	 */
	public void purgeRelationshipType(RelationshipType relationshipType) throws APIException {
		dao.deleteRelationshipType(relationshipType);
	}
	
	/**
	 * @see org.openmrs.api.PersonService#saveRelationshipType(org.openmrs.RelationshipType)
	 */
	public RelationshipType saveRelationshipType(RelationshipType relationshipType) throws APIException {
		if (StringUtils.isBlank(relationshipType.getDescription())) {
			throw new APIException("error.required", new Object[] { Context.getMessageSourceService().getMessage(
			    "general.description") });
		}
		
		return dao.saveRelationshipType(relationshipType);
	}
	
	/**
	 * @see org.openmrs.api.PersonService#getPersonAttributeTypes(org.openmrs.util.OpenmrsConstants.PERSON_TYPE,
	 *      org.openmrs.api.PersonService.ATTR_VIEW_TYPE)
	 */
	@Transactional(readOnly = true)
	public List<PersonAttributeType> getPersonAttributeTypes(PERSON_TYPE personType, ATTR_VIEW_TYPE viewType)
	        throws APIException {
		AdministrationService as = Context.getAdministrationService();
		
		String attrString = "";
		
		// TODO cache the global properties to speed this up??
		// Is hibernate taking care of caching and not hitting the db every time? (hopefully it is)
		if (viewType == null) {
			return Context.getPersonService().getAllPersonAttributeTypes();
		} else if (viewType == ATTR_VIEW_TYPE.LISTING) {
			String patientListing = as.getGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_LISTING_ATTRIBUTES, "");
			String userListing = as.getGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_USER_LISTING_ATTRIBUTES, "");
			if (personType == null || personType == PERSON_TYPE.PERSON) {
				attrString = patientListing + "," + userListing;
			} else if (personType == PERSON_TYPE.PATIENT) {
				attrString = patientListing;
			} else if (personType == PERSON_TYPE.USER) {
				attrString = userListing;
			} else {
				log.fatal("Should not be here.");
			}
		} else if (viewType == ATTR_VIEW_TYPE.VIEWING) {
			String patientViewing = as.getGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_VIEWING_ATTRIBUTES, "");
			String userViewing = as.getGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_USER_VIEWING_ATTRIBUTES, "");
			if (personType == null || personType == PERSON_TYPE.PERSON) {
				attrString = patientViewing + "," + userViewing;
			} else if (personType == PERSON_TYPE.PATIENT) {
				attrString = patientViewing;
			} else if (personType == PERSON_TYPE.USER) {
				attrString = userViewing;
			} else {
				log.fatal("Should not be here");
			}
		} else if (viewType == ATTR_VIEW_TYPE.HEADER) {
			String patientHeader = as.getGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_HEADER_ATTRIBUTES, "");
			String userHeader = as.getGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_USER_HEADER_ATTRIBUTES, "");
			if (personType == null || personType == PERSON_TYPE.PERSON) {
				attrString = patientHeader + "," + userHeader;
			} else if (personType == PERSON_TYPE.PATIENT) {
				attrString = patientHeader;
			} else if (personType == PERSON_TYPE.USER) {
				attrString = userHeader;
			} else {
				log.fatal("Should not be here");
			}
			
		} else {
			log.fatal("Should not be here");
		}
		
		// the java list object to hold the values from the global properties
		List<String> attrNames = new Vector<String>();
		
		// split the comma delimited string into a java list object
		if (attrString != null) {
			for (String s : attrString.split(",")) {
				if (s != null) {
					s = s.trim();
					if (s.length() > 0) {
						attrNames.add(s);
					}
				}
			}
		}
		
		// the actual list we'll be returning
		List<PersonAttributeType> attrObjects = new Vector<PersonAttributeType>();
		
		// get the PersonAttribute objects for each name/id
		if (attrNames.size() > 0) {
			for (String nameOrId : attrNames) {
				if (nameOrId.matches("\\d")) {
					attrObjects.add(Context.getPersonService().getPersonAttributeType(Integer.valueOf(nameOrId)));
				} else {
					attrObjects.add(getPersonAttributeType(nameOrId));
				}
			}
		}
		
		return attrObjects;
	}
	
	/**
	 * @deprecated @see
	 *             {@link org.openmrs.api.PersonService#getPersonAttributeTypes(java.lang.String, java.lang.String)}
	 */
	@Transactional(readOnly = true)
	public List<PersonAttributeType> getPersonAttributeTypes(String personTypeStr, String displayTypeStr)
	        throws APIException {
		
		PERSON_TYPE personType = null;
		if ("patient".equals(personTypeStr)) {
			personType = PERSON_TYPE.PATIENT;
		} else if ("user".equals(personTypeStr)) {
			personType = PERSON_TYPE.USER;
		} else if (StringUtils.isEmpty(personTypeStr)) {
			personType = null;
		} else {
			throw new APIException("Person.invalid.personType.attribute", new Object[] { personTypeStr });
		}
		
		ATTR_VIEW_TYPE attrDisplayType = null;
		if ("listing".equals(displayTypeStr)) {
			attrDisplayType = ATTR_VIEW_TYPE.LISTING;
		} else if ("viewing".equals(displayTypeStr)) {
			attrDisplayType = ATTR_VIEW_TYPE.VIEWING;
		} else if ("header".equals(displayTypeStr)) {
			attrDisplayType = ATTR_VIEW_TYPE.HEADER;
		} else if ("all".equals(displayTypeStr)) {
			attrDisplayType = null;
		} else {
			throw new APIException("Person.invalid.displayType.attribute", new Object[] { displayTypeStr });
		}
		
		return Context.getPersonService().getPersonAttributeTypes(personType, attrDisplayType);
	}
	
	/**
	 * @see org.openmrs.api.PersonService#parsePersonName(java.lang.String)
	 */
	public PersonName parsePersonName(String name) throws APIException {
		// strip beginning/ending whitespace
		name = name.trim();
		
		// trim off all trailing commas
		while (name.endsWith(",")) {
			name = name.substring(0, name.length() - 1);
		}
		
		String firstName = name;
		String middleName = "";
		String lastName = "";
		String lastName2 = null;
		
		if (name.contains(",")) {
			
			String[] names = name.split(",");
			
			// trim whitespace on each part of the name
			for (int x = 0; x < names.length; x++) {
				names[x] = names[x].trim();
			}
			
			String[] firstNames = names[1].split(" ");
			if (firstNames.length == 2) {
				// user entered "Smith, John Adam"
				lastName = names[0];
				firstName = firstNames[0];
				middleName = firstNames[1];
			} else {
				// user entered "Smith, John"
				firstName = names[1];
				lastName = names[0];
			}
		} else if (name.contains(" ")) {
			String[] names = name.split(" ");
			if (names.length == 4) {
				// user entered "John Adam Smith"
				firstName = names[0];
				middleName = names[1];
				lastName = names[2];
				lastName2 = names[3];
			} else if (names.length == 3) {
				// user entered "John Adam Smith"
				firstName = names[0];
				middleName = names[1];
				lastName = names[2];
			} else {
				// user entered "John Smith"
				firstName = names[0];
				lastName = names[1];
			}
		}
		
		PersonName pn = new PersonName(firstName, middleName, lastName);
		pn.setFamilyName2(lastName2);
		
		return pn;
	}
	
	/**
	 * @deprecated see #parsePersonName(String)
	 */
	public PersonName splitPersonName(String name) {
		return Context.getPersonService().parsePersonName(name);
	}
	
	/**
	 * @see org.openmrs.api.PersonService#voidPersonName(org.openmrs.PersonName, String)
	 */
	public PersonName voidPersonName(PersonName personName, String voidReason) throws APIException {
		return Context.getPersonService().savePersonName(personName);
	}
	
	/**
	 * @see org.openmrs.api.PersonService#unvoidPersonName(org.openmrs.PersonName)
	 */
	public PersonName unvoidPersonName(PersonName personName) throws APIException {
		return Context.getPersonService().savePersonName(personName);
		
	}
	
	/**
	 * @see org.openmrs.api.PersonService#savePersonName(org.openmrs.PersonName)
	 */
	public PersonName savePersonName(PersonName personName) throws APIException {
		ValidateUtil.validate(personName.getPerson());
		return dao.savePersonName(personName);
	}
	
	/**
	 * @see org.openmrs.api.PersonService#getRelationshipMap(org.openmrs.RelationshipType)
	 */
	@Transactional(readOnly = true)
	public Map<Person, List<Person>> getRelationshipMap(RelationshipType relType) throws APIException {
		
		// get all relationships with this type
		List<Relationship> relationships = Context.getPersonService().getRelationships(null, null, relType);
		
		// the map to return
		Map<Person, List<Person>> ret = new HashMap<Person, List<Person>>();
		
		if (relationships != null) {
			for (Relationship rel : relationships) {
				Person from = rel.getPersonA();
				Person to = rel.getPersonB();
				
				List<Person> relList = ret.get(from);
				if (relList == null) {
					relList = new ArrayList<Person>();
				}
				relList.add(to);
				
				ret.put(from, relList);
			}
		}
		
		return ret;
	}
	
	/**
	 * @deprecated use {@link #getRelationshipMap(RelationshipType)}
	 */
	@Transactional(readOnly = true)
	public Map<Person, List<Person>> getRelationships(RelationshipType relType) throws APIException {
		return Context.getPersonService().getRelationshipMap(relType);
	}
	
	/**
	 * @see org.openmrs.api.PersonService#getPersonAttributeTypeByUuid(java.lang.String)
	 */
	@Transactional(readOnly = true)
	public PersonAttributeType getPersonAttributeTypeByUuid(String uuid) {
		return dao.getPersonAttributeTypeByUuid(uuid);
	}
	
	/**
	 * @see org.openmrs.api.PersonService#getPersonByUuid(java.lang.String)
	 */
	@Transactional(readOnly = true)
	public Person getPersonByUuid(String uuid) throws APIException {
		return dao.getPersonByUuid(uuid);
	}
	
	@Transactional(readOnly = true)
	public PersonAddress getPersonAddressByUuid(String uuid) throws APIException {
		return dao.getPersonAddressByUuid(uuid);
	}
	
	@Transactional(readOnly = true)
	public PersonAttribute getPersonAttributeByUuid(String uuid) throws APIException {
		return dao.getPersonAttributeByUuid(uuid);
	}
	
	/**
	 * @see org.openmrs.api.PersonService#getPersonName(java.lang.Integer)
	 */
	@Override
	@Transactional(readOnly = true)
	public PersonName getPersonName(Integer personNameId) {
		return dao.getPersonName(personNameId);
	}
	
	/**
	 * @see org.openmrs.api.PersonService#getPersonNameByUuid(java.lang.String)
	 */
	@Transactional(readOnly = true)
	public PersonName getPersonNameByUuid(String uuid) throws APIException {
		return dao.getPersonNameByUuid(uuid);
	}
	
	/**
	 * @see org.openmrs.api.PersonService#savePersonMergeLog(PersonMergeLog)
	 */
	@Override
	public PersonMergeLog savePersonMergeLog(PersonMergeLog personMergeLog) throws SerializationException, APIException {
		//verify required fields
		if (Context.getSerializationService().getDefaultSerializer() == null) {
			throw new APIException("serializer.default.not.found", (Object[]) null);
		}
		log.debug("Auditing merging of non-preferred person " + personMergeLog.getLoser().getUuid()
		        + " with preferred person " + personMergeLog.getWinner().getId());
		//populate the mergedData XML from the PersonMergeLogData object
		String serialized = Context.getSerializationService().getDefaultSerializer().serialize(
		    personMergeLog.getPersonMergeLogData());
		personMergeLog.setSerializedMergedData(serialized);
		log.debug(serialized);
		//save the bean to the database
		return dao.savePersonMergeLog(personMergeLog);
	}
	
	/**
	 * @see org.openmrs.api.PersonService#getPersonMergeLogByUuid(String, boolean)
	 */
	@Override
	@Transactional(readOnly = true)
	public PersonMergeLog getPersonMergeLogByUuid(String uuid, boolean deserialize) throws SerializationException,
	        APIException {
		if (uuid == null) {
			throw new APIException("uuid.cannot.null", (Object[]) null);
		}
		PersonMergeLog personMergeLog = dao.getPersonMergeLogByUuid(uuid);
		//deserialize if requested
		if (deserialize) {
			deserialize(personMergeLog);
		}
		return personMergeLog;
	}
	
	/**
	 * Deserializes a List of <code>PersonMErgeLog</code> objects
	 *
	 * @param lst the List of <code> PersonMergeLog</code> objects to deserialize
	 * @throws SerializationException
	 */
	private void deserializeList(List<PersonMergeLog> lst) throws SerializationException {
		for (PersonMergeLog personMergeLog : lst) {
			deserialize(personMergeLog);
		}
	}
	
	/**
	 * Deserializes a <code>PersonMErgeLog</code> object
	 *
	 * @param personMergeLog the <code> PersonMergeLog</code> object to deserialize
	 * @throws SerializationException
	 */
	private void deserialize(PersonMergeLog personMergeLog) throws SerializationException {
		PersonMergeLogData data = Context.getSerializationService().getDefaultSerializer().deserialize(
		    personMergeLog.getSerializedMergedData(), PersonMergeLogData.class);
		personMergeLog.setPersonMergeLogData(data);
	}
	
	/**
	 * @see org.openmrs.api.PersonService#getAllPersonMergeLogs(boolean)
	 */
	@Override
	@Transactional(readOnly = true)
	public List<PersonMergeLog> getAllPersonMergeLogs(boolean deserialize) throws SerializationException {
		List<PersonMergeLog> lst = dao.getAllPersonMergeLogs();
		//deserialize if requested
		if (deserialize) {
			deserializeList(lst);
		}
		return lst;
	}
	
	/**
	 * @see org.openmrs.api.PersonService#getWinningPersonMergeLogs(org.openmrs.Person)
	 */
	@Override
	@Transactional(readOnly = true)
	public List<PersonMergeLog> getWinningPersonMergeLogs(Person person, boolean deserialize) throws SerializationException {
		List<PersonMergeLog> lst = dao.getWinningPersonMergeLogs(person);
		if (deserialize) {
			deserializeList(lst);
		}
		return lst;
	}
	
	/**
	 * @see org.openmrs.api.PersonService#getLosingPersonMergeLog(org.openmrs.Person)
	 */
	@Override
	@Transactional(readOnly = true)
	public PersonMergeLog getLosingPersonMergeLog(Person person, boolean deserialize) throws SerializationException {
		PersonMergeLog personMergeLog = dao.getLosingPersonMergeLogs(person);
		if (deserialize) {
			deserialize(personMergeLog);
		}
		return personMergeLog;
	}
	
	/**
	 * @see org.openmrs.api.PersonService#getRelationshipByUuid(java.lang.String)
	 */
	@Transactional(readOnly = true)
	public Relationship getRelationshipByUuid(String uuid) throws APIException {
		return dao.getRelationshipByUuid(uuid);
	}
	
	/**
	 * @see org.openmrs.api.PersonService#getRelationshipTypeByUuid(java.lang.String)
	 */
	@Transactional(readOnly = true)
	public RelationshipType getRelationshipTypeByUuid(String uuid) throws APIException {
		return dao.getRelationshipTypeByUuid(uuid);
	}
	
	/**
	 * @see org.openmrs.api.PersonService#getAllRelationshipTypes(boolean)
	 */
	@Transactional(readOnly = true)
	public List<RelationshipType> getAllRelationshipTypes(boolean includeRetired) throws APIException {
		return dao.getAllRelationshipTypes(includeRetired);
	}
	
	/**
	 * @see org.openmrs.api.PersonService#retireRelationshipType(org.openmrs.RelationshipType,
	 *      java.lang.String)
	 */
	public RelationshipType retireRelationshipType(RelationshipType type, String retiredReason) throws APIException {
		if (retiredReason == null || retiredReason.length() < 1) {
			throw new APIException("Relationship.retiring.reason.required", (Object[]) null);
		}
		
		type.setRetired(true);
		type.setRetiredBy(Context.getAuthenticatedUser());
		type.setDateRetired(new Date());
		type.setRetireReason(retiredReason);
		return Context.getPersonService().saveRelationshipType(type);
	}
	
	/**
	 * @see org.openmrs.api.PersonService#unretireRelationshipType(org.openmrs.RelationshipType)
	 */
	public RelationshipType unretireRelationshipType(RelationshipType relationshipType) {
		relationshipType.setRetired(false);
		relationshipType.setRetiredBy(null);
		relationshipType.setDateRetired(null);
		relationshipType.setRetireReason(null);
		return Context.getPersonService().saveRelationshipType(relationshipType);
	}
	
	/**
	 * @see org.openmrs.api.PersonService#voidPersonAddress(org.openmrs.PersonAddress, String)
	 */
	public PersonAddress voidPersonAddress(PersonAddress personAddress, String voidReason) {
		return Context.getPersonService().savePersonAddress(personAddress);
	}
	
	/**
	 * @see org.openmrs.api.PersonService#unvoidPersonAddress(org.openmrs.PersonAddress)
	 */
	public PersonAddress unvoidPersonAddress(PersonAddress personAddress) throws APIException {
		return Context.getPersonService().savePersonAddress(personAddress);
	}
	
	/**
	 * @see org.openmrs.api.PersonService#savePersonAddress(org.openmrs.PersonAddress)
	 */
	public PersonAddress savePersonAddress(PersonAddress personAddress) {
		return dao.savePersonAddress(personAddress);
	}
	
	public void checkIfPersonAttributeTypesAreLocked() {
		String locked = Context.getAdministrationService().getGlobalProperty(
		    OpenmrsConstants.GLOBAL_PROPERTY_PERSON_ATRIBUTE_TYPES_LOCKED, "false");
		if (locked.toLowerCase().equals("true")) {
			throw new PersonAttributeTypeLockedException();
		}
	}
}
