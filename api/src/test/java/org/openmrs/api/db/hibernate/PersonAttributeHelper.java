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

import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.openmrs.PersonAttribute;

import java.util.List;

public class PersonAttributeHelper {
	
	private static final String QUERY_ALL_PERSON_ATTRIBUTES = "select pa.* from person_attribute pa";
	
	private static final String QUERY_ALL_VOIDED_PERSON_ATTRIBUTES = "select pa.* from person_attribute pa where voided = true";
	
	private static final String QUERY_ALL_NON_SEARCHABLE_PERSON_ATTRIBUTES = "select pa.* from person_attribute pa, person_attribute_type pta "
	        + "where pa.person_attribute_type_id = pta.person_attribute_type_id and pta.searchable = false";
	
	private SessionFactory sessionFactory;
	
	public PersonAttributeHelper(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	/**
	 * @should return true if a person attribute exists
	 */
	public boolean personAttributeExists(String value) {
		return getPersonAttribute(getPersonAttributeList(QUERY_ALL_PERSON_ATTRIBUTES), value) != null;
	}
	
	/**
	 * @should return true if a voided person attribute exists
	 */
	public boolean voidedPersonAttributeExists(String value) {
		PersonAttribute personAttribute = getPersonAttribute(getPersonAttributeList(QUERY_ALL_VOIDED_PERSON_ATTRIBUTES),
		    value);
		if (personAttribute != null) {
			return personAttribute.isVoided();
		}
		return false;
	}
	
	/**
	 * @should return true if a non-voided person attribute exists
	 */
	public boolean nonVoidedPersonAttributeExists(String value) {
		return personAttributeExists(value) && (!voidedPersonAttributeExists(value));
	}
	
	/**
	 * @should return true if a non-searchable person attribute exists
	 */
	public boolean nonSearchablePersonAttributeExists(String value) {
		return getPersonAttribute(getPersonAttributeList(QUERY_ALL_NON_SEARCHABLE_PERSON_ATTRIBUTES), value) != null;
	}
	
	/**
	 * @should return true if a searchable person attribute exists
	 */
	public boolean searchablePersonAttributeExists(String value) {
		return personAttributeExists(value) && (!nonSearchablePersonAttributeExists(value));
	}
	
	private List<PersonAttribute> getPersonAttributeList(String queryString) {
		Query query = sessionFactory.getCurrentSession().createSQLQuery(queryString).addEntity(PersonAttribute.class);
		
		return query.list();
	}
	
	private PersonAttribute getPersonAttribute(List<PersonAttribute> personAttributeList, String personAttributeValue) {
		for (PersonAttribute personAttribute : personAttributeList) {
			if (personAttribute.getValue().equalsIgnoreCase(personAttributeValue)) {
				return personAttribute;
			}
		}
		return null;
	}
	
}
