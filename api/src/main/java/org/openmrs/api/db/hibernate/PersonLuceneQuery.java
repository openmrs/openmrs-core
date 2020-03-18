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
	
	public LuceneQuery<PersonName> getSoundexPersonNameAtLeastThreeMatches(String name1, String name2,  Integer birthyear, boolean includeVoided, String gender) {
		String n1ThreeMatches = "(givenNameSoundex:" + name1 + " AND (middleNameSoundex:" + name1 + " OR familyName2Soundex:" + name1 + " OR  familyNameSoundex:" + name1 + ")) " 
			+ "OR (middleNameSoundex:"+ name1 + " AND  familyName2Soundex:"+ name1 + " AND  familyNameSoundex:"+name1+")";
		
		String n2TwoMatches = " OR (givenNameSoundex:query AND (middleNameSoundex:query OR familyNameSoundex:query OR familyName2Soundex:query))\n"
			+ "               OR (middleNameSoundex:query AND (familyName2Soundex:query OR familyNameSoundex:query))  \n"
			+ "               OR (familyNameSoundex:query AND familyName2Soundex:query)";
		n2TwoMatches =  n2TwoMatches.replace("query", name2);
		
		String n1OnlyFirstMatch = "OR (givenNameSoundex:query AND -middleNameSoundex:[* TO *] AND -familyNameSoundex:[* TO *] AND -familyName2Soundex:[* TO *])";
		n1OnlyFirstMatch = n1OnlyFirstMatch.replace("query", name1);
		
		String n2MatchesOneNonFirstName = " OR (middleNameSoundex:query AND  -familyNameSoundex:[* TO *] AND -familyName2Soundex:[* TO *] AND -givenNameSoundex:[* TO *])" 
			+ " OR (familyNameSoundex:query AND  -middleNameSoundex:[* TO *] AND -familyName2Soundex:[* TO *] AND -givenNameSoundex:[* TO *])    " 
			+ " OR  (familyName2Soundex:query AND  -middleNameSoundex:[* TO *] AND -familyNameSoundex:[* TO *] AND -givenNameSoundex:[* TO *])";
		
		n2MatchesOneNonFirstName = n2MatchesOneNonFirstName.replace("query", name2);
		
		return getSoundexPersonNameQuery(n1ThreeMatches + n2TwoMatches + n1OnlyFirstMatch + n2MatchesOneNonFirstName, birthyear, includeVoided, gender);
	}
	
	public LuceneQuery<PersonName> getSoundexPersonFirstNameQuery(String firstName, Integer birthyear, boolean includeVoided, String gender) {
		List<String> fields = new ArrayList<>();
		fields.addAll(Arrays.asList("givenNameSoundex"));
		
		return buildSoundexLuceneQuery(firstName, fields, birthyear, includeVoided, gender);
	}
	
	public LuceneQuery<PersonName> getSoundexPersonSecondNameQuery(String secondName, Integer birthyear, boolean includeVoided, String gender) {
		List<String> fields = new ArrayList<>();
		fields.addAll(Arrays.asList("familyNameSoundex", "familyName2Soundex", "middleNameSoundex"));
		String query = secondName;
		if(secondName == "") {
			 query = "*:* AND -(middleNameSoundex:[* TO *] AND  familyNameSoundex:[* TO *] AND familyName2Soundex:[* TO *])" ;
				// + "(*:* AND -middleNameSoundex:[* TO *] OR -familyNameSoundex:[* TO *])";
			
		}
		
		return buildSoundexLuceneQuery(query, fields, birthyear, includeVoided, gender);
	}
		
		
	public LuceneQuery<PersonName> getSoundexPersonNameQuery(String query, Integer birthyear, boolean includeVoided, String gender) {
		List<String> fields = new ArrayList<>();
		fields.addAll(Arrays.asList("familyNameSoundex", "familyName2Soundex", "middleNameSoundex", "givenNameSoundex"));
		
		return buildSoundexLuceneQuery(query, fields, birthyear, includeVoided, gender);
	}

	private LuceneQuery<PersonName> buildSoundexLuceneQuery(String query, List<String> fields, Integer birthyear, boolean includeVoided, String gender) {
		String completeQuery = query;
		if(birthyear != 0) {
			// https://stackoverflow.com/questions/17221736/whats-wrong-with-this-solr-range-filter-query/17225534#17225534
			String dateQuery = " AND (person.birthdate: [" + (birthyear - 1) + " TO " + (birthyear + 1) + "] OR ( -person.birthdate:([* TO *])  AND *:*))";
			completeQuery+= dateQuery;
		}
		
		LuceneQuery<PersonName> luceneQuery = LuceneQuery
			.newQuery(PersonName.class, sessionFactory.getCurrentSession(), completeQuery, fields).useOrQueryParser();
		
		if (!includeVoided) {
			luceneQuery.include("voided", false);
			luceneQuery.include("person.voided", false);
		}
		
		if(gender != null) {
			String[] searchedGenders = new String[] {gender.toLowerCase()};
			luceneQuery.include("person.gender", searchedGenders);
		}		
		return luceneQuery;
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
