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

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.SessionFactory;
import org.openmrs.Encounter;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PersonName;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.attribute.Attribute;
import org.openmrs.attribute.AttributeType;
import org.openmrs.util.OpenmrsConstants;

/**
 * The PatientSearchCriteria class. It has API to return a criteria from the Patient Name and
 * identifier.
 *
 * @deprecated since 2.1.0 (in favor of Hibernate Search)
 */
@Deprecated
public class PatientSearchCriteria {

	private final SessionFactory sessionFactory;

	private final PersonSearchCriteria personSearchCriteria;

	/**
	 * @param sessionFactory
	 */
	public PatientSearchCriteria(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
		this.personSearchCriteria = new PersonSearchCriteria();
	}

	/**
	 * Prepare a {@link QueryResult} for searching patients by name and/or identifier. The visibility of
	 * this method remains public in order not to break OpenMRS modules that use this method. Instead of
	 * calling this method consider using {@link org.openmrs.api.PatientService} or
	 * {@link org.openmrs.api.db.PatientDAO}.
	 *
	 * @param cb
	 * @param patientJoin
	 * @param name
	 * @param identifier
	 * @param identifierTypes
	 * @param matchIdentifierExactly
	 * @param searchOnNamesOrIdentifiers specifies if the logic should find patients that match the name
	 *            or identifier otherwise find patients that match both the name and identifier
	 * @return {@link QueryResult}
	 */
	public QueryResult prepareCriteria(CriteriaBuilder cb, Join<Encounter, Patient> patientJoin, String name,
	        String identifier, List<PatientIdentifierType> identifierTypes, boolean matchIdentifierExactly,
	        boolean orderByNames, boolean searchOnNamesOrIdentifiers) {
		
		QueryResult queryResult = new QueryResult();
		PatientSearchMode patientSearchMode = getSearchMode(name, identifier, identifierTypes, searchOnNamesOrIdentifiers);

		List<Predicate> predicates = new ArrayList<>();
		Join<Patient, PersonName> nameJoin;
		switch (patientSearchMode) {
			case PATIENT_SEARCH_BY_NAME:
				nameJoin = addAliasForName(cb, patientJoin, orderByNames, queryResult);
				predicates.add(preparePredicateForName(cb, nameJoin, name));
				break;

			case PATIENT_SEARCH_BY_IDENTIFIER:
				Join<Patient, PatientIdentifier> identifierJoin = addAliasForIdentifiers(patientJoin);
				predicates.add(preparePredicateForIdentifier(cb, identifierJoin, identifier, identifierTypes, matchIdentifierExactly));
				break;

			case PATIENT_SEARCH_BY_NAME_OR_IDENTIFIER:

				// If only name *or* identifier is provided as a search parameter,
				// the respective value is copied to the empty search parameter.
				//
				// As a consequence, the *single* parameter is used to search for both names and identifiers.
				//
				name = copySearchParameter(identifier, name);
				identifier = copySearchParameter(name, identifier);

				nameJoin = addAliasForName(cb, patientJoin, orderByNames, queryResult);
				Join<Patient, PatientIdentifier> idsJoin = addAliasForIdentifiers(patientJoin);
				predicates.add((cb.or(
					preparePredicateForName(cb, nameJoin, name), 
					preparePredicateForIdentifier(cb, idsJoin, identifier, identifierTypes, matchIdentifierExactly))
				));
				break;

			case PATIENT_SEARCH_BY_NAME_AND_IDENTIFIER:
				nameJoin = addAliasForName(cb, patientJoin, orderByNames, queryResult);
				Join<Patient, PatientIdentifier> idJoin = addAliasForIdentifiers(patientJoin);
				predicates.add((cb.and(
					preparePredicateForName(cb, nameJoin, name),
					preparePredicateForIdentifier(cb, idJoin, identifier, identifierTypes, matchIdentifierExactly))
				));
				break;

			default:
				break;
		}

		predicates.add(cb.isFalse(patientJoin.get("voided")));

		queryResult.addPredicates(predicates);

		return queryResult;
	}

	/**
	 * Provides a {@link QueryResult} object for searching patients by name, identifier or searchable attribute.
	 * 
	 * The visibility of this method is "default" as this method should NOT be called directly by classes other
	 * than org.openmrs.api.db.hibernate.HibernatePatientDAO.
	 * 
	 * Instead of calling this method consider using {@link org.openmrs.api.PatientService} or
	 * {@link org.openmrs.api.db.PatientDAO}.
	 *
	 * @param cb            the CriteriaBuilder to build the criteria
	 * @param patientJoin   the join from Encounter to Patient
	 * @param query         defines search parameters
	 * @param includeVoided true/false whether or not to included voided patients
	 * @return QueryResult for searching by name OR identifier OR searchable attributes
	 */
	QueryResult prepareCriteria(CriteriaBuilder cb, Join<Encounter, Patient> patientJoin, String query, boolean includeVoided) {
		QueryResult queryResult = new QueryResult();
		List<Predicate> predicates = new ArrayList<>();
		
		Join<Patient, PersonName> nameJoin = addAliasForName(cb, patientJoin, true, queryResult);
		Join<Patient, Attribute> attributeJoin = personSearchCriteria.addAliasForAttribute(patientJoin);
		Join<Attribute, AttributeType> attributeTypeJoin = personSearchCriteria.addAliasForAttributeType(attributeJoin);
		Join<Patient, PatientIdentifier> idsJoin = addAliasForIdentifiers(patientJoin);
		predicates.add(cb.or(
			preparePredicateForName(cb, nameJoin, query, includeVoided),
			prepareCriterionForAttribute(cb, attributeJoin, attributeTypeJoin, query, includeVoided),
			preparePredicateForIdentifier(cb, idsJoin, query, new ArrayList<>(), false, includeVoided)));
		if (!includeVoided) {
			predicates.add(cb.isFalse(patientJoin.get("voided")));
		}
		
		queryResult.addPredicates(predicates);
		return queryResult;
	}

	/**
	 * Provides a {@link QueryResult} object for searching patients by name, identifier or searchable attribute.
	 *
	 * The visibility of this method is "default" as this method should NOT be called directly by classes other
	 * than org.openmrs.api.db.hibernate.HibernatePatientDAO.
	 *
	 * Instead of calling this method consider using {@link org.openmrs.api.PatientService} or
	 * {@link org.openmrs.api.db.PatientDAO}.
	 *
	 * @param cb            the CriteriaBuilder to build the criteria
	 * @param patientJoin   the join from Encounter to Patient
	 * @param query defines search parameters
	 * @return QueryResult for searching by name OR identifier OR searchable attributes
	 */
	QueryResult prepareCriteria(CriteriaBuilder cb, Join<Encounter, Patient> patientJoin, String query) {
		return prepareCriteria(cb, patientJoin, query, false);
	}

	/**
	 * @param query         defines search parameters
	 *
	 * @param cb            the CriteriaBuilder to build the criteria
	 * @param patientJoin   the join from Encounter to Patient   
	 * @param matchExactly  true/false whether to perform an exact match on names
	 * @param orderByNames  true/false whether to order by names
	 * @param includeVoided true/false whether or not to included voided patients
	 * @return QueryResult for searching by name OR identifier OR searchable attributes
	 */
	QueryResult prepareCriteria(CriteriaBuilder cb, Join<Encounter, Patient> patientJoin, String query, Boolean matchExactly, boolean orderByNames, boolean includeVoided) {
		QueryResult queryResult = new QueryResult();
		List<Predicate> predicates = new ArrayList<>();
		
		Join<Patient, PersonName> nameJoin = addAliasForName(cb, patientJoin, orderByNames, queryResult);

		if (matchExactly == null) {
			predicates.add(cb.and(
				preparePredicateForName(cb, nameJoin, query, null, includeVoided),
				preparePredicateForName(cb, nameJoin, query, true, includeVoided),
				cb.not(preparePredicateForName(cb, nameJoin, query, false, includeVoided))));
		} else if (!matchExactly) {
			predicates.add(preparePredicateForName(cb, nameJoin, query, false, includeVoided));
		} else {
			Join<Patient, Attribute> attributeJoin = personSearchCriteria.addAliasForAttribute(patientJoin);
			Join<Attribute, AttributeType> attributeTypeJoin = personSearchCriteria.addAliasForAttributeType(attributeJoin);
			Join<Patient, PatientIdentifier> idsJoin = addAliasForIdentifiers(patientJoin);

			predicates.add(cb.or(
				preparePredicateForName(cb, nameJoin, query, true, includeVoided),
				prepareCriterionForAttribute(cb, attributeJoin, attributeTypeJoin, query, includeVoided),
				preparePredicateForIdentifier(cb, idsJoin, query, new ArrayList<>(), false, includeVoided))
			);
		}

		if (!includeVoided) {
			predicates.add(cb.isFalse(patientJoin.get("voided")));
		}
		
		queryResult.addPredicates(predicates);
		return queryResult;
	}

	/**
	 * <strong>Should</strong> return source value when target is blank
	 * <strong>Should</strong> return target value when target is non-blank
	 */
	String copySearchParameter(String source, String target) {
		if (!StringUtils.isBlank(source) && StringUtils.isBlank(target)) {
			return source;
		}
		return target;
	}

	/**
	 * <strong>Should</strong> identify search by name
	 * <strong>Should</strong> identify search by identifier
	 * <strong>Should</strong> identify search by identifier type list
	 * <strong>Should</strong> identify search by identifier and identifier type list
	 * <strong>Should</strong> identify search by name or identifier
	 * <strong>Should</strong> identify search by name and identifier
	 */
	PatientSearchMode getSearchMode(String name, String identifier, List<PatientIdentifierType> identifierTypes,
	        boolean searchOnNamesOrIdentifiers) {
		if (searchOnNamesOrIdentifiers) {
			return PatientSearchMode.PATIENT_SEARCH_BY_NAME_OR_IDENTIFIER;
		}

		if (!StringUtils.isBlank(name) && StringUtils.isBlank(identifier) && CollectionUtils.isEmpty(identifierTypes)) {
			return PatientSearchMode.PATIENT_SEARCH_BY_NAME;
		}

		// de Morgan's law coming to fruition: (!A||!B) <=> !(A&&B)
		//
		if (StringUtils.isBlank(name) && !(StringUtils.isBlank(identifier) && CollectionUtils.isEmpty(identifierTypes))) {
			return PatientSearchMode.PATIENT_SEARCH_BY_IDENTIFIER;
		}

		return PatientSearchMode.PATIENT_SEARCH_BY_NAME_AND_IDENTIFIER;
	}
	
	private Join<Patient, PersonName> addAliasForName(CriteriaBuilder cb, Join<Encounter, Patient> patientJoin, boolean orderByNames, QueryResult queryResult) {
		Join<Patient, PersonName> nameJoin = patientJoin.join("names");
		if (orderByNames) {
			queryResult.addOrder(cb.asc(nameJoin.get("givenName")));
			queryResult.addOrder(cb.asc(nameJoin.get("middleName")));
			queryResult.addOrder(cb.asc(nameJoin.get("familyName")));
		}
		return nameJoin;
	}

	private Join<Patient, PatientIdentifier> addAliasForIdentifiers(Join<Encounter, Patient> patientJoin) {
		return patientJoin.join("identifiers", JoinType.LEFT);
	}


	/**
	 * Utility method to add identifier expression to an existing criteria
	 *
	 * @param cb        the CriteriaBuilder to build the criteria
	 * @param idsJoin   the join from Patient to PatientIdentifier
	 * @param identifier
	 * @param identifierTypes
	 * @param matchIdentifierExactly
	 * @param includeVoided true/false whether or not to included voided patients
	 */
	private Predicate preparePredicateForIdentifier(CriteriaBuilder cb, Join<Patient, PatientIdentifier> idsJoin,
	        String identifier, List<PatientIdentifierType> identifierTypes, boolean matchIdentifierExactly,
	        boolean includeVoided) {
		
		identifier = HibernateUtil.escapeSqlWildcards(identifier, sessionFactory);
		List<Predicate> predicates = new ArrayList<>();

		if (!includeVoided) {
			predicates.add(cb.isFalse(idsJoin.get("voided")));
		}
		// do the identifier restriction
		if (identifier != null) {
			// if the user wants an exact search, match on that.
			if (matchIdentifierExactly) {
				if (Context.getAdministrationService().isDatabaseStringComparisonCaseSensitive()) {
					predicates.add(cb.equal(cb.lower(idsJoin.get("identifier")), identifier.toLowerCase()));
				} else {
					predicates.add(cb.equal(idsJoin.get("identifier"), identifier));
				}
			} else {
				AdministrationService adminService = Context.getAdministrationService();
				String regex = adminService.getGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_IDENTIFIER_REGEX, "");
				String patternSearch = adminService.getGlobalProperty(
				OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_IDENTIFIER_SEARCH_PATTERN, "");

				// remove padding from identifier search string
				if (Pattern.matches("^\\^.{1}\\*.*$", regex)) {
					identifier = removePadding(identifier, regex);
				}

				if (org.springframework.util.StringUtils.hasLength(patternSearch)) {
					predicates.add(splitAndGetSearchPattern(cb, idsJoin, identifier, patternSearch));
				}
				// if the regex is empty, default to a simple "like" search or if
				// we're in hsql world, also only do the simple like search (because
				// hsql doesn't know how to deal with 'regexp'
				else if ("".equals(regex) || HibernateUtil.isHSQLDialect(sessionFactory)) {
					predicates.add(getPredicateForSimpleSearch(cb, idsJoin, identifier, adminService));
				}
				// if the regex is present, search on that
				else {
					regex = replaceSearchString(regex, identifier);
					predicates.add(cb.isTrue(cb.function("regexp", Boolean.class, idsJoin.get("identifier"),
						cb.literal(regex))));
				}
			}
		}

		// do the type restriction
		if (!CollectionUtils.isEmpty(identifierTypes)) {
			predicates.add(idsJoin.get("identifierType").in(identifierTypes));
		}

		return cb.and(predicates.toArray(predicates.toArray(new Predicate[]{})));
	}

	/**
	 * Utility method to add identifier expression to an existing criteria
	 *
	 * @param cb        the CriteriaBuilder to build the criteria
	 * @param idsJoin   the join from Patient to PatientIdentifier
	 * @param identifier
	 * @param identifierTypes
	 * @param matchIdentifierExactly
	 */
	private Predicate preparePredicateForIdentifier(CriteriaBuilder cb, Join<Patient, PatientIdentifier> idsJoin,
	        String identifier, List<PatientIdentifierType> identifierTypes, boolean matchIdentifierExactly) {
		return preparePredicateForIdentifier(cb, idsJoin, identifier, identifierTypes, matchIdentifierExactly, false);
	}

	/**
	 * Utility method to add prefix and suffix like expression
	 *
	 * @param cb        the CriteriaBuilder to build the criteria
	 * @param idsJoin   the join from Patient to PatientIdentifier
	 * @param identifier
	 * @param adminService
	 */
	private Predicate getPredicateForSimpleSearch(CriteriaBuilder cb, Join<Patient, PatientIdentifier> idsJoin,
	        String identifier, AdministrationService adminService) {
		String prefix = adminService.getGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_IDENTIFIER_PREFIX, "");
		String suffix = adminService.getGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_IDENTIFIER_SUFFIX, "");
		String matchPattern = (prefix + identifier + suffix).toLowerCase();
		return cb.like(cb.lower(idsJoin.get("identifier")), matchPattern);
	}

	/**
	 * Utility method to add search pattern expression to identifier.
	 *
	 * @param cb
	 * @param idsJoin
	 * @param identifier
	 * @param patternSearch
	 */
	private Predicate splitAndGetSearchPattern(CriteriaBuilder cb, Join<Patient, PatientIdentifier> idsJoin,
											   String identifier, String patternSearch) {
		CriteriaBuilder.In<String> inClause = cb.in(idsJoin.get("identifier"));
		// replace the @SEARCH@, etc in all elements
		for (String pattern : patternSearch.split(",")) {
			inClause.value(replaceSearchString(pattern, identifier));
		}
		return inClause;
	}



	/**
	 * Utility method to remove padding from the identifier.
	 *
	 * @param identifier
	 * @param regex
	 * @return identifier without the padding.
	 */
	private String removePadding(String identifier, String regex) {
		String padding = regex.substring(regex.indexOf("^") + 1, regex.indexOf("*"));
		Pattern pattern = Pattern.compile("^" + padding + "+");
		identifier = pattern.matcher(identifier).replaceFirst("");
		return identifier;
	}

	/**
	 * Utility method to add name expressions to criteria.
	 *
	 * @param cb
	 * @param nameJoin
	 * @param name
	 * @param matchExactly
	 * @param includeVoided true/false whether or not to included voided patients
	 */
	private Predicate preparePredicateForName(CriteriaBuilder cb, Join<Patient, PersonName> nameJoin, String name,
	        Boolean matchExactly, boolean includeVoided) {
		name = HibernateUtil.escapeSqlWildcards(name, sessionFactory);
		List<Predicate> predicates = new ArrayList<>();

		String[] nameParts = getQueryParts(name);
		if (nameParts.length > 0) {
			StringBuilder multiName = new StringBuilder(nameParts[0]);

			for (int i = 0; i < nameParts.length; i++) {
				String singleName = nameParts[i];

				if (singleName != null && !singleName.isEmpty()) {
					Predicate singleNamePredicate = getPredicateForName(cb, nameJoin, singleName, matchExactly, includeVoided);

					if (i > 0) {
						multiName.append(" ");
						multiName.append(singleName);
						Predicate multiNamePredicate = getPredicateForName(cb, nameJoin, multiName.toString(), matchExactly, includeVoided);
						singleNamePredicate = cb.or(singleNamePredicate, multiNamePredicate);
					}

					predicates.add(singleNamePredicate);
				}
			}
		}

		return cb.and(predicates.toArray(new Predicate[]{}));
	}

	/**
	 * Utility method to add name expressions to criteria.
	 *
	 * @param cb        the CriteriaBuilder to build the criteria
	 * @param nameJoin  the join from Patient to PersonName
	 * @param name
	 */
	private Predicate preparePredicateForName(CriteriaBuilder cb, Join<Patient, PersonName> nameJoin, String name) {
		return preparePredicateForName(cb, nameJoin, name, null, false);
	}
	

	private Predicate preparePredicateForName(CriteriaBuilder cb, Join<Patient, PersonName> nameJoin, String name, boolean includeVoided) {
		return preparePredicateForName(cb, nameJoin, name, null, includeVoided);
	}

	/**
	 * <strong>Should</strong> process simple space as separator
	 * <strong>Should</strong> process comma as separator
	 * <strong>Should</strong> process mixed separators
	 * <strong>Should</strong> not return empty name parts
	 * <strong>Should</strong> reject null as name
	 **/
	String[] getQueryParts(String query) {
		if (query == null) {
			throw new IllegalArgumentException("query must not be null");
		}

		query = query.replace(",", " ");
		String[] queryPartArray = query.split(" ");

		List<String> queryPartList = new ArrayList<>();
		for (String queryPart : queryPartArray) {
			if (queryPart.trim().length() > 0) {
				queryPartList.add(queryPart);
			}
		}

		return queryPartList.toArray(new String[0]);
	}

	/**
	 * Returns a criteria object comparing the given string to each part of the name. <br>
	 * <br>
	 * This criteria is essentially:
	 * <p>
	 *
	 * <pre>
	 * ... where voided = false &amp;&amp; name in (familyName2, familyName, middleName, givenName)
	 * </pre>
	 *
	 * Except when the name provided is less than min characters (usually 3) then we will look for
	 * an EXACT match by default
	 *
	 * @param cb        the CriteriaBuilder to build the criteria
	 * @param nameJoin  the join from Patient to PersonName
	 * @param name
	 * @param matchExactly
	 * @param includeVoided true/false whether or not to included voided patients
	 * @return {@link Predicate}
	 */
	private Predicate getPredicateForName(CriteriaBuilder cb, Join<Patient, PersonName> nameJoin, String name, Boolean matchExactly, boolean includeVoided) {
		if (isShortName(name)) {
			return getPredicateForShortName(cb, nameJoin, name, includeVoided);
		} else {
			if (matchExactly != null) {
				if (matchExactly) {
					return getPredicateForShortName(cb, nameJoin, name, includeVoided);
				}
				return getPredicateForNoExactName(cb, nameJoin, name, includeVoided);
			}
			return getPredicateForLongName(cb, nameJoin, name, includeVoided);
		}
	}


	/**
	 * <strong>Should</strong> recognise short name
	 * <strong>Should</strong> recognise long name
	 */
	Boolean isShortName(String name) {
		Integer minChars = Context.getAdministrationService().getGlobalPropertyValue(
		OpenmrsConstants.GLOBAL_PROPERTY_MIN_SEARCH_CHARACTERS,
		OpenmrsConstants.GLOBAL_PROPERTY_DEFAULT_MIN_SEARCH_CHARACTERS);

		if (name != null && name.length() < minChars) {
			return Boolean.TRUE;

		} else {
			return Boolean.FALSE;
		}
	}

	private Predicate getPredicateForShortName(CriteriaBuilder cb, Join<Patient, PersonName> nameJoin, String name, boolean includeVoided) {
		Predicate givenNamePredicate = cb.and(
			cb.isNotNull(nameJoin.get("givenName")),
			cb.equal(cb.lower(nameJoin.get("givenName")), name.toLowerCase())
		);

		Predicate middleNamePredicate = cb.and(
			cb.isNotNull(nameJoin.get("middleName")),
			cb.equal(cb.lower(nameJoin.get("middleName")), name.toLowerCase())
		);

		Predicate familyNamePredicate = cb.and(
			cb.isNotNull(nameJoin.get("familyName")),
			cb.equal(cb.lower(nameJoin.get("familyName")), name.toLowerCase())
		);

		Predicate familyName2Predicate = cb.and(
			cb.isNotNull(nameJoin.get("familyName2")),
			cb.equal(cb.lower(nameJoin.get("familyName2")), name.toLowerCase())
		);

		Predicate namePredicate = cb.or(givenNamePredicate, middleNamePredicate, familyNamePredicate, familyName2Predicate);

		if (!includeVoided) {
			Predicate nonVoidedPredicate = cb.isFalse(nameJoin.get("voided"));
			namePredicate = cb.and(namePredicate, nonVoidedPredicate);
		}

		return namePredicate;
	}

	private Predicate getPredicateForLongName(CriteriaBuilder cb, Join<Patient, PersonName> nameJoin, String name, boolean includeVoided) {
		String pattern = getMatchMode().toCaseSensitivePattern(name);

		Predicate givenNamePredicate = cb.like(nameJoin.get("givenName"), pattern);
		Predicate middleNamePredicate = cb.like(nameJoin.get("middleName"), pattern);
		Predicate familyNamePredicate = cb.like(nameJoin.get("familyName"), pattern);
		Predicate familyName2Predicate = cb.like(nameJoin.get("familyName2"), pattern);

		Predicate namePredicate = cb.or(givenNamePredicate, middleNamePredicate, familyNamePredicate, familyName2Predicate);

		if (!includeVoided) {
			Predicate nonVoidedPredicate = cb.isFalse(nameJoin.get("voided"));
			namePredicate = cb.and(namePredicate, nonVoidedPredicate);
		}

		return namePredicate;
	}

	private Predicate getPredicateForNoExactName(CriteriaBuilder cb, Join<Patient, PersonName> nameJoin, String name, boolean includeVoided) {
		String pattern = getMatchMode().toCaseSensitivePattern(name);
		Predicate givenNamePredicate = cb.and(
			cb.isNotNull(nameJoin.get("givenName")),
			cb.like(nameJoin.get("givenName"), pattern)
		);
		Predicate middleNamePredicate = cb.and(
			cb.isNotNull(nameJoin.get("middleName")),
			cb.like(nameJoin.get("middleName"), pattern)
		);
		Predicate familyNamePredicate = cb.and(
			cb.isNotNull(nameJoin.get("familyName")),
			cb.like(nameJoin.get("familyName"), pattern)
		);
		Predicate familyName2Predicate = cb.and(
			cb.isNotNull(nameJoin.get("familyName2")),
			cb.like(nameJoin.get("familyName2"), pattern)
		);

		Predicate namePredicates = cb.or(givenNamePredicate, middleNamePredicate, familyNamePredicate, familyName2Predicate);

		Predicate notGivenName = cb.or(cb.isNull(nameJoin.get("givenName")), cb.notEqual(nameJoin.get("givenName"), name));
		Predicate notMiddleName = cb.or(cb.isNull(nameJoin.get("middleName")), cb.notEqual(nameJoin.get("middleName"), name));
		Predicate notFamilyName = cb.or(cb.isNull(nameJoin.get("familyName")), cb.notEqual(nameJoin.get("familyName"), name));
		Predicate notFamilyName2 = cb.or(cb.isNull(nameJoin.get("familyName2")), cb.notEqual(nameJoin.get("familyName2"), name));

		Predicate combinedPredicate = cb.and(namePredicates, notGivenName, notMiddleName, notFamilyName, notFamilyName2);

		if (!includeVoided) {
			combinedPredicate = cb.and(combinedPredicate, cb.isFalse(nameJoin.get("voided")));
		}

		return combinedPredicate;
	}


	/**
	 * <strong>Should</strong> return start as default match mode
	 * <strong>Should</strong> return start as configured match mode
	 * <strong>Should</strong> return anywhere as configured match mode
	 */
	MatchMode getMatchMode() {
		String matchMode = Context.getAdministrationService().getGlobalProperty(
		OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_SEARCH_MATCH_MODE,
		OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_SEARCH_MATCH_START);

		if (matchMode.equalsIgnoreCase(OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_SEARCH_MATCH_ANYWHERE)) {
			return MatchMode.ANYWHERE;
		}
		return MatchMode.START;
	}

	/**
	 * Puts @SEARCH@, @SEARCH-1@, and @CHECKDIGIT@ into the search string
	 *
	 * @param regex the admin-defined search string containing the @..@'s to be replaced
	 * @param identifierSearched the user entered search string
	 * @return substituted search strings.
	 */
	private String replaceSearchString(String regex, String identifierSearched) {
		String returnString = regex.replaceAll("@SEARCH@", identifierSearched);
		if (identifierSearched.length() > 1) {
			// for 2 or more character searches, we allow regex to use last character as check digit
			returnString = returnString.replaceAll("@SEARCH-1@",
			    identifierSearched.substring(0, identifierSearched.length() - 1));
			returnString = returnString.replaceAll("@CHECKDIGIT@",
			    identifierSearched.substring(identifierSearched.length() - 1));
		} else {
			returnString = returnString.replaceAll("@SEARCH-1@", "");
			returnString = returnString.replaceAll("@CHECKDIGIT@", "");
		}
		return returnString;
	}

	private Predicate prepareCriterionForAttribute(CriteriaBuilder cb, Join<Patient, Attribute> attributeJoin, Join<Attribute, AttributeType> attributeTypeJoin, String query, boolean includeVoided) {
		query = HibernateUtil.escapeSqlWildcards(query, sessionFactory);

		MatchMode matchMode = personSearchCriteria.getAttributeMatchMode();
		List<Predicate> predicates = new ArrayList<>();
		
		String[] queryParts = getQueryParts(query);
		for (String queryPart : queryParts) {
			predicates.add(personSearchCriteria.preparePredicateForAttribute(cb, attributeJoin, attributeTypeJoin, queryPart, includeVoided, matchMode));
		}

		return cb.and(predicates.toArray(new Predicate[]{}));
	}
}
