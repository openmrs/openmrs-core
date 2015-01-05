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

import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Conjunction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.LogicalExpression;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.SimpleExpression;
import org.openmrs.PatientIdentifierType;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsConstants;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * The PatientSearchCriteria class. It has API to return a criteria from the Patient Name and
 * identifier.
 */
public class PatientSearchCriteria {
	
	private final SessionFactory sessionFactory;
	
	private final Criteria criteria;
	
	/**
	 * @param sessionFactory
	 * @param criteria
	 */
	public PatientSearchCriteria(SessionFactory sessionFactory, Criteria criteria) {
		this.sessionFactory = sessionFactory;
		this.criteria = criteria;
	}
	
	/**
	 * Prepare a hibernate criteria using the patient identifier.
	 * 
	 * @param name
	 * @param identifier
	 * @param identifierTypes
	 * @param matchIdentifierExactly
	 * @param searchOnNamesOrIdentifiers specifies if the logic should find patients that match the
	 *            name or identifier otherwise find patients that match both the name and identifier
	 * @return {@link Criteria}
	 */
	public Criteria prepareCriteria(String name, String identifier, List<PatientIdentifierType> identifierTypes,
	        boolean matchIdentifierExactly, boolean orderByNames, boolean searchOnNamesOrIdentifiers) {
		
		//Find patients that match either the name or identifier if only
		//the name or identifier was passed in to this method
		if (searchOnNamesOrIdentifiers && (name == null || identifier == null)) {
			if (name == null)
				name = identifier;
			else
				identifier = name;
		}
		
		name = HibernateUtil.escapeSqlWildcards(name, sessionFactory);
		identifier = HibernateUtil.escapeSqlWildcards(identifier, sessionFactory);
		
		criteria.createAlias("names", "name");
		if (orderByNames) {
			criteria.addOrder(Order.asc("name.givenName"));
			criteria.addOrder(Order.asc("name.middleName"));
			criteria.addOrder(Order.asc("name.familyName"));
		}
		
		// get only distinct patients
		criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
		Criterion nameCriterion = null;
		if (name != null) {
			nameCriterion = prepareNameCriterion(name);
		}
		
		Criterion identifierCriterion = null;
		// do the restriction on either identifier string or types
		if (identifier != null || identifierTypes.size() > 0) {
			identifierCriterion = prepareIdentifierCriterion(identifier, identifierTypes, matchIdentifierExactly,
			    searchOnNamesOrIdentifiers);
		}
		
		if (searchOnNamesOrIdentifiers) {
			criteria.add(Restrictions.or(nameCriterion, identifierCriterion));
		} else {
			if (nameCriterion != null) {
				criteria.add(nameCriterion);
			}
			if (identifierCriterion != null) {
				criteria.add(identifierCriterion);
			}
		}
		
		// TODO add junit test for searching on voided patients
		
		// make sure the patient object isn't voided
		criteria.add(Restrictions.eq("voided", false));
		
		return criteria;
	}
	
	/**
	 * Utility method to add identifier expression to an existing criteria
	 * 
	 * @param criteria
	 * @param identifier
	 * @param identifierTypes
	 * @param matchIdentifierExactly
	 */
	private Criterion prepareIdentifierCriterion(String identifier, List<PatientIdentifierType> identifierTypes,
	        boolean matchIdentifierExactly, boolean searchOnNamesOrIdentifiers) {
		
		Conjunction conjunction = Restrictions.conjunction();
		// TODO add junit test for searching on voided identifiers
		
		// add the join on the identifiers table
		criteria.createAlias("identifiers", "ids");
		
		conjunction.add(Restrictions.eq("ids.voided", false));
		// do the identifier restriction
		if (identifier != null) {
			// if the user wants an exact search, match on that.
			if (matchIdentifierExactly) {
				SimpleExpression matchIdentifier = Restrictions.eq("ids.identifier", identifier);
				if (Context.getAdministrationService().isDatabaseStringComparisonCaseSensitive()) {
					matchIdentifier.ignoreCase();
				}
				conjunction.add(matchIdentifier);
			} else {
				AdministrationService adminService = Context.getAdministrationService();
				String regex = adminService.getGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_IDENTIFIER_REGEX, "");
				String patternSearch = adminService.getGlobalProperty(
				    OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_IDENTIFIER_SEARCH_PATTERN, "");
				
				// remove padding from identifier search string
				if (Pattern.matches("^\\^.{1}\\*.*$", regex)) {
					identifier = removePadding(identifier, regex);
				}
				
				if (StringUtils.hasLength(patternSearch)) {
					conjunction.add(splitAndGetSearchPattern(identifier, patternSearch));
				}
				// if the regex is empty, default to a simple "like" search or if
				// we're in hsql world, also only do the simple like search (because
				// hsql doesn't know how to deal with 'regexp'
				else if (regex.equals("") || HibernateUtil.isHSQLDialect(sessionFactory)) {
					conjunction.add(getCriterionForSimpleSearch(identifier, adminService));
				}
				// if the regex is present, search on that
				else {
					regex = replaceSearchString(regex, identifier);
					conjunction.add(Restrictions.sqlRestriction("identifier regexp ?", regex, Hibernate.STRING));
				}
			}
		}
		
		// TODO add a junit test for patientIdentifierType restrictions
		
		// do the type restriction
		if (identifierTypes.size() > 0) {
			criteria.add(Restrictions.in("ids.identifierType", identifierTypes));
		}
		
		return conjunction;
	}
	
	/**
	 * Utility method to add prefix and suffix like expression
	 * 
	 * @param identifier
	 * @param adminService
	 */
	private Criterion getCriterionForSimpleSearch(String identifier, AdministrationService adminService) {
		String prefix = adminService.getGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_IDENTIFIER_PREFIX, "");
		String suffix = adminService.getGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_IDENTIFIER_SUFFIX, "");
		StringBuffer likeString = new StringBuffer(prefix).append(identifier).append(suffix);
		return Restrictions.ilike("ids.identifier", likeString.toString());
	}
	
	/**
	 * Utility method to add search pattern expression to identifier.
	 * 
	 * @param identifier
	 * @param patternSearch
	 */
	private Criterion splitAndGetSearchPattern(String identifier, String patternSearch) {
		// split the pattern before replacing in case the user searched on a comma
		List<String> searchPatterns = new ArrayList<String>();
		// replace the @SEARCH@, etc in all elements
		for (String pattern : patternSearch.split(","))
			searchPatterns.add(replaceSearchString(pattern, identifier));
		return Restrictions.in("ids.identifier", searchPatterns);
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
	 * @param criteria
	 * @param name
	 */
	private Criterion prepareNameCriterion(String name) {
		
		Conjunction conjuction = Restrictions.conjunction();
		
		// TODO simple name search to start testing, will need to make "real"
		// name search
		// i.e. split on whitespace, guess at first/last name, etc
		// TODO return the matched name instead of the primary name
		// possible solution: "select new" org.openmrs.PatientListItem and
		// return a list of those
		
		name = name.replaceAll("  ", " ");
		name = name.replace(", ", " ");
		String[] names = name.split(" ");
		
		// TODO add junit test for searching on voided patient names
		if (names.length > 0) {
			String nameSoFar = names[0];
			for (int i = 0; i < names.length; i++) {
				String n = names[i];
				if (n != null && n.length() > 0) {
					LogicalExpression oneNameSearch = getNameSearch(n);
					LogicalExpression searchExpression = oneNameSearch;
					if (i > 0) {
						nameSoFar += " " + n;
						LogicalExpression fullNameSearch = getNameSearch(nameSoFar);
						searchExpression = Restrictions.or(oneNameSearch, fullNameSearch);
					}
					conjuction.add(searchExpression);
				}
			}
		}
		
		return conjuction;
	}
	
	/**
	 * Returns a criteria object comparing the given string to each part of the name. <br/>
	 * <br/>
	 * This criteria is essentially:
	 * <p/>
	 * 
	 * <pre>
	 * ... where voided = false &amp;&amp; name in (familyName2, familyName, middleName, givenName)
	 * </pre>
	 * 
	 * Except when the name provided is less than min characters (usually 3) then we will look for
	 * an EXACT match by default
	 * 
	 * @param name
	 * @return {@link LogicalExpression}
	 */
	private LogicalExpression getNameSearch(String name) {
		String givenNameProperty = "name.givenName";
		String middleNameProperty = "name.middleName";
		String familyNameProperty = "name.familyName";
		String familyName2Property = "name.familyName2";
		
		SimpleExpression givenName;
		SimpleExpression middleName;
		SimpleExpression familyName;
		SimpleExpression familyName2;
		
		Integer minChars = Context.getAdministrationService().getGlobalPropertyValue(
		    OpenmrsConstants.GLOBAL_PROPERTY_MIN_SEARCH_CHARACTERS,
		    OpenmrsConstants.GLOBAL_PROPERTY_DEFAULT_MIN_SEARCH_CHARACTERS);
		if (name != null && name.length() < minChars) {
			givenName = Expression.eq(givenNameProperty, name).ignoreCase();
			middleName = Expression.eq(middleNameProperty, name).ignoreCase();
			familyName = Expression.eq(familyNameProperty, name).ignoreCase();
			familyName2 = Expression.eq(familyName2Property, name).ignoreCase();
		} else {
			MatchMode mode = MatchMode.START;
			String matchModeConstant = OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_SEARCH_MATCH_MODE;
			String modeGp = Context.getAdministrationService().getGlobalProperty(matchModeConstant);
			
			if (OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_SEARCH_MATCH_ANYWHERE.equalsIgnoreCase(modeGp)) {
				mode = MatchMode.ANYWHERE;
			}
			
			givenName = Expression.like(givenNameProperty, name, mode);
			middleName = Expression.like(middleNameProperty, name, mode);
			familyName = Expression.like(familyNameProperty, name, mode);
			familyName2 = Expression.like(familyName2Property, name, mode);
		}
		
		return Expression.and(Expression.eq("name.voided", false), Expression.or(familyName2, Expression.or(familyName,
		    Expression.or(middleName, givenName))));
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
			returnString = returnString.replaceAll("@SEARCH-1@", identifierSearched.substring(0,
			    identifierSearched.length() - 1));
			returnString = returnString.replaceAll("@CHECKDIGIT@", identifierSearched
			        .substring(identifierSearched.length() - 1));
		} else {
			returnString = returnString.replaceAll("@SEARCH-1@", "");
			returnString = returnString.replaceAll("@CHECKDIGIT@", "");
		}
		return returnString;
	}
}
