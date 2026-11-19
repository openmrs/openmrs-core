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

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.search.engine.search.common.BooleanOperator;
import org.hibernate.search.engine.search.predicate.SearchPredicate;
import org.hibernate.search.engine.search.predicate.dsl.BooleanPredicateOptionsCollector;
import org.hibernate.search.engine.search.predicate.dsl.SearchPredicateFactory;
import org.hibernate.search.engine.search.predicate.dsl.SimpleQueryStringPredicateFieldMoreStep;
import org.hibernate.search.util.common.data.RangeBoundInclusion;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsConstants;

/**
 * Provides common queries for HibernatePatientDAO and HibernatePersonDAO.
 *
 * @see HibernatePatientDAO
 * @see HibernatePersonDAO
 * @since 2.8.0
 */
public class PersonQuery {

	public SearchPredicate getPatientNameQuery(SearchPredicateFactory predicateFactory, String query,
	        boolean includeVoided) {
		return getPersonNameQuery(predicateFactory, query, false, includeVoided, true, null);
	}

	public SearchPredicate getPersonNameQueryWithOrParser(SearchPredicateFactory predicateFactory, String query,
	        boolean includeVoided, Boolean dead) {
		return getPersonNameQuery(predicateFactory, query, true, includeVoided, false, dead);
	}

	/**
	 * This method creates a Lucene search query for a Person based on a soundex search on the first
	 * name
	 *
	 * @param name1 the first part of the name to be searched for
	 * @param name2 the second part of the name to be searched
	 * @param name3 the third part of the name to be searched
	 * @param birthyear the birthyear the searched person should have
	 * @param includeVoided is true if voided person should be matched
	 * @param gender the gender of the person to search
	 * @return the LuceneQuery that returns Persons with a soundex representation of the firstName and
	 *         other defined search criteria
	 */
	public SearchPredicate getSoundexPersonNameSearchOnThreeNames(SearchPredicateFactory predicateFactory, String name1,
	        String name2, String name3, Integer birthyear, boolean includeVoided, String gender) {
		return predicateFactory.bool().with(b -> {
			b.minimumShouldMatchNumber(1);
			b.should(predicateFactory.bool().with(bb -> {
				bb.should(predicateFactory.match().field("givenNameSoundex").matching(name1).boost(6f));
				bb.should(predicateFactory.match().field("givenNameSoundex").matching(name2).boost(2f));
				bb.should(predicateFactory.match().field("givenNameSoundex").matching(name3));
			}));
			b.should(predicateFactory.bool().with(bb -> {
				bb.should(predicateFactory.match().field("middleNameSoundex").matching(name1).boost(2f));
				bb.should(predicateFactory.match().field("middleNameSoundex").matching(name2).boost(6f));
				bb.should(predicateFactory.match().field("middleNameSoundex").matching(name3));
			}));
			b.should(predicateFactory.bool().with(bb -> {
				bb.should(predicateFactory.match().field("familyNameSoundex").matching(name1));
				bb.should(predicateFactory.match().field("familyNameSoundex").matching(name2).boost(2f));
				bb.should(predicateFactory.match().field("familyNameSoundex").matching(name3).boost(6f));
			}));
			b.should(predicateFactory.bool().with(bb -> {
				bb.should(predicateFactory.match().field("familyName2Soundex").matching(name1));
				bb.should(predicateFactory.match().field("familyName2Soundex").matching(name2).boost(2f));
				bb.should(predicateFactory.match().field("familyName2Soundex").matching(name3).boost(6f));
			}));
			applyPersonFilters(predicateFactory, b, includeVoided, null, null, birthyear, gender);
		}).toPredicate();
	}

	/**
	 * This method creates a Lucene search query for a Person based on a soundex search
	 *
	 * @param name1 the first part of the name to be searched for
	 * @param name2 the second part of the name to be searched
	 * @param birthyear the birthyear the searched person should have
	 * @param includeVoided is true if voided person should be matched
	 * @param gender the gender of the person to search
	 * @return the LuceneQuery that returns Persons with a soundex representation of the defined names
	 *         and the other defined search criteria
	 */
	public SearchPredicate getSoundexPersonNameSearchOnTwoNames(SearchPredicateFactory predicateFactory, String name1,
	        String name2, Integer birthyear, boolean includeVoided, String gender) {

		return predicateFactory.bool().with(b -> {
			b.minimumShouldMatchNumber(1);
			b.should(predicateFactory.bool().with(bb -> {
				bb.should(predicateFactory.match().field("givenNameSoundex").matching(name1).boost(8f));
				bb.should(predicateFactory.match().field("givenNameSoundex").matching(name2).boost(4f));
			}));
			b.should(predicateFactory.bool().with(bb -> {
				bb.should(predicateFactory.match().field("middleNameSoundex").matching(name1).boost(4f));
				bb.should(predicateFactory.match().field("middleNameSoundex").matching(name2).boost(8f));
			}));
			b.should(predicateFactory.bool().with(bb -> {
				bb.should(predicateFactory.match().field("familyNameSoundex").matching(name1).boost(4f));
				bb.should(predicateFactory.match().field("familyNameSoundex").matching(name2).boost(8f));
			}));
			b.should(predicateFactory.bool().with(bb -> {
				bb.should(predicateFactory.match().field("familyName2Soundex").matching(name1).boost(4f));
				bb.should(predicateFactory.match().field("familyName2Soundex").matching(name2).boost(8f));
			}));
			applyPersonFilters(predicateFactory, b, includeVoided, null, null, birthyear, gender);
		}).toPredicate();
	}

	/**
	 * This method creates a Lucene search query for a Person based on a soundex search on n>3 names
	 *
	 * @param searchNames an array of names that should be searched for
	 * @param birthyear the birthyear the searched person should have
	 * @param includeVoided is true if voided person should be matched
	 * @param gender the gender of the person to search
	 * @return the LuceneQuery that returns Persons with a soundex representation of the defined names
	 *         and the other defined search criteria
	 */
	public SearchPredicate getSoundexPersonNameSearchOnNNames(SearchPredicateFactory predicateFactory, String[] searchNames,
	        Integer birthyear, boolean includeVoided, String gender) {
		List<String> queryPart = new ArrayList<>();
		for (String name : searchNames) {
			queryPart.add("\"" + name + "\"");
		}
		String query = "(" + String.join(" | ", queryPart) + " )";
		return newSoundexPersonNameSearchQuery(predicateFactory, query, includeVoided, birthyear, gender);
	}

	/**
	 * The method creates a Lucene search query for a Person based on a soundex search on the givenName,
	 * familyNames and middleName
	 *
	 * @param query the query that should be executed on the names
	 * @param birthyear the birthyear the searched person should have
	 * @param includeVoided is true if voided person should be matched
	 * @param gender the gender of the person to search
	 * @return the LuceneQuery that returns Persons with a soundex representation of the givenName,
	 *         familyNames and middleName
	 */
	public SearchPredicate getSoundexPersonNameQuery(SearchPredicateFactory predicateFactory, String query,
	        Integer birthyear, boolean includeVoided, String gender) {
		return newSoundexPersonNameSearchQuery(predicateFactory, query, includeVoided, birthyear, gender);
	}

	private SearchPredicate getPersonNameQuery(SearchPredicateFactory predicateFactory, String query, boolean orQueryParser,
	        boolean includeVoided, boolean patientsOnly, Boolean dead) {
		String matchMode = Context.getAdministrationService()
		        .getGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_SEARCH_MATCH_MODE);
		boolean includeAnywhere = OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_SEARCH_MATCH_ANYWHERE.equals(matchMode);
		return newPersonNameSearchQuery(predicateFactory, includeAnywhere, query, orQueryParser, includeVoided, patientsOnly,
		    dead);
	}

	private SearchPredicate newPersonNameSearchQuery(SearchPredicateFactory predicateFactory, boolean includeAnywhere,
	        String query, boolean orQueryParser, boolean includeVoided, Boolean patientsOnly, Boolean dead) {
		BooleanOperator operator = orQueryParser ? BooleanOperator.OR : BooleanOperator.AND;
		return predicateFactory.bool().with(b -> {
			SimpleQueryStringPredicateFieldMoreStep<?, ?, ?> names = predicateFactory.simpleQueryString()
			        .fields("givenNameExact", "familyNameExact").boost(8f).fields("middleNameExact", "familyName2Exact")
			        .boost(4f).fields("givenNameStart", "familyNameStart").boost(4f)
			        .fields("middleNameStart", "familyName2Start").boost(2f);

			if (includeAnywhere) {
				names = names.fields("givenNameAnywhere", "familyNameAnywhere").boost(2f)
				        .fields("middleNameAnywhere", "familyName2Anywhere").boost(1f);
			}

			if (orQueryParser || singleCharacterTerms(query) == null) {
				b.must(names.matching(query).defaultOperator(operator));
			} else {
				List<String> otherTerms = new ArrayList<>();
				for (String term : query.trim().split("\\s+")) {
					String initial = initialOf(term);
					if (initial == null) {
						otherTerms.add(term);
						continue;
					}
					// the exact match ranks an actual initial, stored with or without a dot, above the longer
					// names the prefix also matches
					b.must(predicateFactory.bool().with(i -> {
						i.should(predicateFactory.match().field("givenNameExact").boost(8f).field("familyNameExact")
						        .boost(8f).field("middleNameExact").boost(4f).field("familyName2Exact").boost(4f)
						        .matching(initial + " " + initial + "."));
						// a simple query string prefix, unlike a wildcard predicate, is lowercased and folded to
						// ASCII by the field's analyzer on both the Lucene and Elasticsearch backends
						i.should(predicateFactory.simpleQueryString()
						        .fields("givenNameExact", "familyNameExact", "middleNameExact", "familyName2Exact")
						        .matching(initial + "*"));
					}));
				}
				if (!otherTerms.isEmpty()) {
					b.must(names.matching(String.join(" ", otherTerms)).defaultOperator(operator));
				}
			}
			applyPersonFilters(predicateFactory, b, includeVoided, patientsOnly, dead, null, null);
		}).toPredicate();
	}

	/**
	 * Returns the initials of a plain AND name query, i.e. its terms made of a single letter or digit,
	 * optionally followed by a dot ("j" or "j."), without the dot. Returns null when the query has no
	 * initials or uses simple query string syntax, in which case it is matched unchanged.
	 * <p>
	 * The *Start name fields are indexed with edge n-grams of at least two characters, so an initial
	 * never matches them and, since every term must match, would make the whole search return nothing.
	 * Each returned initial is instead matched as the start of a whole name in the *Exact fields.
	 * Hyphenated names such as "jean-claude" are not treated as syntax, since a "-" only negates a term
	 * when it starts one.
	 *
	 * @param query the name search query
	 * @return the initials, or null to match the query unchanged
	 */
	static List<String> singleCharacterTerms(String query) {
		if (query == null || query.matches(".*[\"+|*()~].*")) {
			return null;
		}
		List<String> initials = new ArrayList<>();
		for (String term : query.trim().split("\\s+")) {
			if (term.startsWith("-")) {
				return null;
			}
			String initial = initialOf(term);
			if (initial != null) {
				initials.add(initial);
			}
		}
		return initials.isEmpty() ? null : initials;
	}

	/**
	 * Returns the letter or digit of an initial such as "j" or "j.", or null if the term is not one.
	 */
	private static String initialOf(String term) {
		boolean initial = (term.length() == 1 || term.length() == 2 && term.charAt(1) == '.')
		        && Character.isLetterOrDigit(term.charAt(0));
		return initial ? term.substring(0, 1) : null;
	}

	private SearchPredicate newSoundexPersonNameSearchQuery(SearchPredicateFactory predicateFactory, String query,
	        boolean includeVoided, Integer birthyear, String gender) {
		return predicateFactory.bool().with(b -> {
			b.must(predicateFactory.simpleQueryString()
			        .fields("familyNameSoundex", "familyName2Soundex", "middleNameSoundex", "givenNameSoundex")
			        .matching(query).defaultOperator(BooleanOperator.OR));
			applyPersonFilters(predicateFactory, b, includeVoided, null, null, birthyear, gender);
		}).toPredicate();
	}

	private void applyPersonFilters(SearchPredicateFactory predicateFactory, BooleanPredicateOptionsCollector<?, ?> b,
	        boolean includeVoided, Boolean patientsOnly, Boolean dead, Integer birthyear, String gender) {
		if (!includeVoided) {
			b.filter(predicateFactory.match().field("voided").matching(false));
			b.filter(predicateFactory.match().field("person.voided").matching(false));
		}

		if (patientsOnly != null && patientsOnly) {
			b.filter(predicateFactory.match().field("person.isPatient").matching(true));
		}

		if (dead != null) {
			b.filter(predicateFactory.match().field("person.dead").matching(dead));
		}

		if (gender != null) {
			b.filter(predicateFactory.match().field("person.gender").matching(gender));
		}

		if (birthyear != null && birthyear != 0) {
			ZonedDateTime birthdate = LocalDate.ofYearDay(birthyear, 1).atStartOfDay(ZoneId.systemDefault());
			b.must(predicateFactory.or(
			    predicateFactory.range().field("person.birthdate").between(Date.from(birthdate.toInstant()),
			        RangeBoundInclusion.INCLUDED, Date.from(birthdate.plusDays(1).toInstant()),
			        RangeBoundInclusion.EXCLUDED),
			    predicateFactory.not(predicateFactory.exists().field("person.birthdate"))));
		}
	}

	public SearchPredicate getPatientAttributeQuery(SearchPredicateFactory predicateFactory, String query,
	        boolean includeVoided) {
		return getPersonAttributeQuery(predicateFactory, query, false, includeVoided, true);
	}

	public SearchPredicate getPersonAttributeQueryWithOrParser(SearchPredicateFactory predicateFactory, String query,
	        boolean includeVoided) {
		return getPersonAttributeQuery(predicateFactory, query, true, includeVoided, false);
	}

	private SearchPredicate getPersonAttributeQuery(SearchPredicateFactory predicateFactory, String query,
	        boolean orQueryParser, boolean includeVoided, boolean patientsOnly) {
		BooleanOperator operator = orQueryParser ? BooleanOperator.OR : BooleanOperator.AND;
		String matchMode = Context.getAdministrationService()
		        .getGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_PERSON_ATTRIBUTE_SEARCH_MATCH_MODE);
		boolean includeAnywhere = OpenmrsConstants.GLOBAL_PROPERTY_PERSON_ATTRIBUTE_SEARCH_MATCH_ANYWHERE.equals(matchMode);

		return predicateFactory.bool().with(b -> {
			SimpleQueryStringPredicateFieldMoreStep<?, ?, ?> attribute = predicateFactory.simpleQueryString()
			        .field("valuePhrase").boost(8f).field("valueExact").boost(4f);

			if (includeAnywhere) {
				attribute = attribute.field("valueStart").boost(2f).field("valueAnywhere").boost(1f);
			}
			b.must(attribute.matching(query).defaultOperator(operator));

			if (!includeVoided) {
				b.filter(predicateFactory.match().field("voided").matching(false));
				b.filter(predicateFactory.match().field("person.voided").matching(false));
			}

			b.filter(predicateFactory.match().field("attributeType.searchable").matching(true));

			if (patientsOnly) {
				b.filter(predicateFactory.match().field("person.isPatient").matching(true));
			}
		}).toPredicate();
	}
}
