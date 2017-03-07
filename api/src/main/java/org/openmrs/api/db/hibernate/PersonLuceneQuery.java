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
import java.util.Arrays;
import java.util.List;

import org.hibernate.SessionFactory;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonName;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.hibernate.search.LuceneQuery;
import org.openmrs.util.OpenmrsConstants;

/**
 * Provides common queries for HibernatePatientDAO and HibernatePersonDAO.
 *
 * @see HibernatePatientDAO
 * @see HibernatePersonDAO
 *
 * @since 2.1.0
 */
public class PersonLuceneQuery {

	private SessionFactory sessionFactory;

	public PersonLuceneQuery(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	public LuceneQuery<PersonName> getPersonNameQuery(String query, boolean includeVoided) {
		return getPersonNameQuery(query, false, includeVoided, false, null);
	}

	public LuceneQuery<PersonName> getPatientNameQuery(String query, boolean includeVoided) {
		return getPersonNameQuery(query, false, includeVoided, true, null);
	}

	public LuceneQuery<PersonName> getPersonNameQuery(String query, boolean includeVoided, LuceneQuery<?> skipSame) {
		return getPersonNameQuery(query, false, includeVoided, false, skipSame);
	}

	public LuceneQuery<PersonName> getPatientNameQuery(String query, boolean includeVoided, LuceneQuery<?> skipSame) {
		return getPersonNameQuery(query, false, includeVoided, true, skipSame);
	}

	public LuceneQuery<PersonName> getPersonNameQueryWithOrParser(String query, boolean includeVoided) {
		return getPersonNameQuery(query, true, includeVoided, false, null);
	}

	public LuceneQuery<PersonName> getPatientNameQueryWithOrParser(String query, boolean includeVoided) {
		return getPersonNameQuery(query, true, includeVoided, true, null);
	}

	public LuceneQuery<PersonName> getPersonNameQueryWithOrParser(String query, boolean includeVoided, LuceneQuery<?> skipSame) {
		return getPersonNameQuery(query, true, includeVoided, false, skipSame);
	}

	public LuceneQuery<PersonName> getPatientNameQueryWithOrParser(String query, boolean includeVoided, LuceneQuery<?> skipSame) {
		return getPersonNameQuery(query, true, includeVoided, true, skipSame);
	}

	private LuceneQuery<PersonName> getPersonNameQuery(String query, boolean orQueryParser, boolean includeVoided, boolean patientsOnly, LuceneQuery<?> skipSame) {
		List<String> fields = new ArrayList<>();
		fields.addAll(Arrays.asList("givenNameExact", "middleNameExact", "familyNameExact", "familyName2Exact"));
		fields.addAll(Arrays.asList("givenNameStart", "middleNameStart", "familyNameStart", "familyName2Start"));

		String matchMode = Context.getAdministrationService().getGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_SEARCH_MATCH_MODE);
		if (OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_SEARCH_MATCH_ANYWHERE.equals(matchMode)) {
			fields.addAll(Arrays.asList("givenNameAnywhere", "middleNameAnywhere", "familyNameAnywhere", "familyName2Anywhere"));
		}

		LuceneQuery<PersonName> luceneQuery = LuceneQuery
				.newQuery(PersonName.class, sessionFactory.getCurrentSession(), query, fields);

		if (orQueryParser) {
			luceneQuery.useOrQueryParser();
		}

		if (!includeVoided) {
			luceneQuery.include("voided", false);
			luceneQuery.include("person.voided", false);
		}

		if (patientsOnly) {
			luceneQuery.include("person.isPatient", true);
		}

		if (skipSame != null) {
			luceneQuery.skipSame("person.personId", skipSame);
		} else {
			luceneQuery.skipSame("person.personId");
		}

		return luceneQuery;
	}

	public LuceneQuery<PersonAttribute> getPersonAttributeQuery(String query, boolean includeVoided, LuceneQuery<?> skipSame) {
		return getPersonAttributeQuery(query, false, includeVoided, false, skipSame);
	}

	public LuceneQuery<PersonAttribute> getPatientAttributeQuery(String query, boolean includeVoided, LuceneQuery<?> skipSame) {
		return getPersonAttributeQuery(query, false, includeVoided, true, skipSame);
	}

	public LuceneQuery<PersonAttribute> getPersonAttributeQueryWithOrParser(String query, boolean includeVoided, LuceneQuery<?> skipSame) {
		return getPersonAttributeQuery(query, true, includeVoided, false, skipSame);
	}

	public LuceneQuery<PersonAttribute> getPatientAttributeQueryWithOrParser(String query, boolean includeVoided, LuceneQuery<?> skipSame) {
		return getPersonAttributeQuery(query, true, includeVoided, true, skipSame);
	}

	private LuceneQuery<PersonAttribute> getPersonAttributeQuery(String query, boolean orQueryParser, boolean includeVoided, boolean patientsOnly, LuceneQuery<?> skipSame) {
		List<String> fields = new ArrayList<>();
		fields.add("valuePhrase"); //will position whole phrase match higher
		fields.add("valueExact");
		String matchMode = Context.getAdministrationService().getGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_PERSON_ATTRIBUTE_SEARCH_MATCH_MODE);
		if (OpenmrsConstants.GLOBAL_PROPERTY_PERSON_ATTRIBUTE_SEARCH_MATCH_ANYWHERE.equals(matchMode)) {
			fields.add("valueStart"); //will position "starts with" match higher
			fields.add("valueAnywhere");
		}

		LuceneQuery<PersonAttribute> luceneQuery = LuceneQuery
				.newQuery(PersonAttribute.class, sessionFactory.getCurrentSession(), query, fields);

		if (orQueryParser) {
			luceneQuery.useOrQueryParser();
		}

		if (!includeVoided){
			luceneQuery.include("voided", false);
			luceneQuery.include("person.voided", false);
		}

		luceneQuery.include("attributeType.searchable", true);

		if (patientsOnly) {
			luceneQuery.include("person.isPatient", true);
		}

		if (skipSame != null) {
			luceneQuery.skipSame("person.personId", skipSame);
		} else {
			luceneQuery.skipSame("person.personId");
		}

		return luceneQuery;
	}
}
