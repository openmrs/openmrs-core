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

import java.util.List;

import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.openmrs.PersonAttribute;

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
			return personAttribute.getVoided();
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
