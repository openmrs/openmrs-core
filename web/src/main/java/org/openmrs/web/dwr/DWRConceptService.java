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
package org.openmrs.web.dwr;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.ConceptAnswer;
import org.openmrs.ConceptClass;
import org.openmrs.ConceptDatatype;
import org.openmrs.ConceptDescription;
import org.openmrs.ConceptName;
import org.openmrs.ConceptNumeric;
import org.openmrs.ConceptSearchResult;
import org.openmrs.ConceptSet;
import org.openmrs.Drug;
import org.openmrs.Field;
import org.openmrs.User;
import org.openmrs.api.APIException;
import org.openmrs.api.ConceptService;
import org.openmrs.api.ConceptsLockedException;
import org.openmrs.api.FormService;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.OpenmrsUtil;

/**
 * This class exposes some of the methods in org.openmrs.api.ConceptService via the dwr package
 */
public class DWRConceptService {
	
	protected static final Log log = LogFactory.getLog(DWRConceptService.class);
	
	/**
	 * Gets a list of conceptListItems matching the given arguments
	 * 
	 * @param phrase the concept name string to match against
	 * @param includeRetired boolean if false, will exclude retired concepts
	 * @param includeClassNames List of ConceptClasses to restrict to
	 * @param excludeClassNames List of ConceptClasses to leave out of results
	 * @param includeDatatypeNames List of ConceptDatatypes to restrict to
	 * @param excludeDatatypeNames List of ConceptDatatypes to leave out of results
	 * @param includeDrugConcepts Specifies if drugs with matching conceptNames should be included
	 * @return a list of conceptListItems matching the given arguments
	 */
	public List<Object> findConcepts(String phrase, boolean includeRetired, List<String> includeClassNames,
	        List<String> excludeClassNames, List<String> includeDatatypeNames, List<String> excludeDatatypeNames,
	        boolean includeDrugConcepts) {
		return findBatchOfConcepts(phrase, includeRetired, includeClassNames, excludeClassNames, includeDatatypeNames,
		    excludeDatatypeNames, null, null);
	}
	
	/**
	 * Gets a list of conceptListItems matching the given arguments
	 * 
	 * @param phrase the concept name string to match against
	 * @param includeRetired boolean if false, will exclude retired concepts
	 * @param includeClassNames List of ConceptClasses to restrict to
	 * @param excludeClassNames List of ConceptClasses to leave out of results
	 * @param includeDatatypeNames List of ConceptDatatypes to restrict to
	 * @param excludeDatatypeNames List of ConceptDatatypes to leave out of results
	 * @param start the beginning index
	 * @param length the number of matching concepts to return
	 * @return a list of conceptListItems matching the given arguments
	 * @since 1.8
	 */
	public List<Object> findBatchOfConcepts(String phrase, boolean includeRetired, List<String> includeClassNames,
	        List<String> excludeClassNames, List<String> includeDatatypeNames, List<String> excludeDatatypeNames,
	        Integer start, Integer length) {
		//TODO factor out the reusable code in this and findCountAndConcepts methods to a single utility method
		// List to return
		// Object type gives ability to return error strings
		Vector<Object> objectList = new Vector<Object>();
		
		// TODO add localization for messages
		
		User currentUser = Context.getAuthenticatedUser();
		
		Locale defaultLocale = Context.getLocale();
		
		// get the list of locales to search on from the user's
		// defined proficient locales (if applicable)
		List<Locale> localesToSearchOn = null;
		if (currentUser != null)
			localesToSearchOn = currentUser.getProficientLocales();
		
		if (localesToSearchOn == null)
			// we're working with an anonymous user right now or
			// with a user that has not defined any proficient locales
			localesToSearchOn = new Vector<Locale>();
		
		// add the user's locale
		if (localesToSearchOn.size() == 0) {
			localesToSearchOn.add(defaultLocale);
			
			// if country is specified, also add the generic language locale
			if (!"".equals(defaultLocale.getCountry())) {
				localesToSearchOn.add(new Locale(defaultLocale.getLanguage()));
			}
			
		}
		
		// debugging output
		if (log.isDebugEnabled()) {
			StringBuffer searchLocalesString = new StringBuffer();
			for (Locale loc : localesToSearchOn) {
				searchLocalesString.append(loc.toString() + " ");
			}
			log.debug("searching locales: " + searchLocalesString);
		}
		
		if (includeClassNames == null)
			includeClassNames = new Vector<String>();
		if (excludeClassNames == null)
			excludeClassNames = new Vector<String>();
		if (includeDatatypeNames == null)
			includeDatatypeNames = new Vector<String>();
		if (excludeDatatypeNames == null)
			excludeDatatypeNames = new Vector<String>();
		
		try {
			ConceptService cs = Context.getConceptService();
			List<ConceptSearchResult> searchResults = new Vector<ConceptSearchResult>();
			
			if (phrase.matches("\\d+")) {
				// user searched on a number. Insert concept with
				// corresponding conceptId
				Concept c = cs.getConcept(Integer.valueOf(phrase));
				if (c != null && (!c.isRetired() || includeRetired)) {
					ConceptName cn = c.getName(defaultLocale);
					ConceptSearchResult searchResult = new ConceptSearchResult(phrase, c, cn);
					searchResults.add(searchResult);
				}
			}
			
			if (!StringUtils.isBlank(phrase)) {
				// turn classnames into class objects
				List<ConceptClass> includeClasses = new Vector<ConceptClass>();
				for (String name : includeClassNames)
					if (!"".equals(name))
						includeClasses.add(cs.getConceptClassByName(name));
				
				// turn classnames into class objects
				List<ConceptClass> excludeClasses = new Vector<ConceptClass>();
				for (String name : excludeClassNames)
					if (!"".equals(name))
						excludeClasses.add(cs.getConceptClassByName(name));
				
				// turn classnames into class objects
				List<ConceptDatatype> includeDatatypes = new Vector<ConceptDatatype>();
				for (String name : includeDatatypeNames)
					if (!"".equals(name))
						includeDatatypes.add(cs.getConceptDatatypeByName(name));
				
				// turn classnames into class objects
				List<ConceptDatatype> excludeDatatypes = new Vector<ConceptDatatype>();
				for (String name : excludeDatatypeNames)
					if (!"".equals(name))
						excludeDatatypes.add(cs.getConceptDatatypeByName(name));
				
				// perform the search
				searchResults.addAll(cs.getConcepts(phrase, localesToSearchOn, includeRetired, includeClasses,
				    excludeClasses, includeDatatypes, excludeDatatypes, null, start, length));
				
				//TODO Should we still include drugs, if yes, smartly harmonize the paging between the two different DB tables
				//look ups to match the values of start and length not to go over the value of count of matches returned to the search widget
				//List<Drug> drugs = null;
				//if (includeDrugConcepts)
				//	drugs = cs.getDrugs(phrase, null, false, includeRetired, null, null);
				
			}
			
			if (searchResults.size() < 1) {
				objectList.add(Context.getMessageSourceService()
				        .getMessage("general.noMatchesFoundInLocale",
				            new Object[] { "<b>" + phrase + "</b>", OpenmrsUtil.join(localesToSearchOn, ", ") },
				            Context.getLocale()));
			} else {
				// turn searchResults into concept list items
				// if user wants drug concepts included, append those
				for (ConceptSearchResult searchResult : searchResults)
					objectList.add(new ConceptListItem(searchResult));
			}
		}
		catch (Exception e) {
			log.error("Error while finding concepts + " + e.getMessage(), e);
			objectList.add(Context.getMessageSourceService().getMessage("Concept.search.error") + " - " + e.getMessage());
		}
		
		if (objectList.size() == 0)
			objectList.add(Context.getMessageSourceService().getMessage("general.noMatchesFoundInLocale",
			    new Object[] { "<b>" + phrase + "</b>", defaultLocale }, Context.getLocale()));
		
		return objectList;
	}
	
	/**
	 * Get a {@link ConceptListItem} by its internal database id.
	 * 
	 * @param conceptId the id to look for
	 * @return a {@link ConceptListItem} or null if conceptId is not found
	 */
	public ConceptListItem getConcept(Integer conceptId) {
		Locale locale = Context.getLocale();
		ConceptService cs = Context.getConceptService();
		Concept c = cs.getConcept(conceptId);
		if (c == null)
			return null;
		
		ConceptName cn = c.getName(locale);
		
		return new ConceptListItem(c, cn, locale);
	}
	
	public List<ConceptListItem> findProposedConcepts(String text) {
		Locale locale = Context.getLocale();
		ConceptService cs = Context.getConceptService();
		
		List<Concept> concepts = cs.getProposedConcepts(text);
		List<ConceptListItem> cli = new Vector<ConceptListItem>();
		for (Concept c : concepts) {
			ConceptName cn = c.getName(locale);
			cli.add(new ConceptListItem(c, cn, locale));
		}
		return cli;
	}
	
	/**
	 * Find a list of {@link ConceptListItem} or {@link ConceptDrugListItem}s that are answers to
	 * the given question. The given question is determined by the given <code>conceptId</code>
	 * 
	 * @param text the text to search for within the answers
	 * @param conceptId the conceptId of the question concept
	 * @param includeVoided (this argument is ignored now. searching for voided answers is not
	 *            logical)
	 * @param includeDrugConcepts if true, drug concepts are searched too
	 * @return list of {@link ConceptListItem} or {@link ConceptDrugListItem} answers that match the
	 *         query
	 * @throws Exception if given conceptId is not found
	 */
	public List<Object> findConceptAnswers(String text, Integer conceptId, boolean includeVoided, boolean includeDrugConcepts)
	        throws Exception {
		
		if (includeVoided == true)
			throw new APIException("You should not include voideds in the search.");
		
		Locale locale = Context.getLocale();
		ConceptService cs = Context.getConceptService();
		
		Concept concept = cs.getConcept(conceptId);
		
		if (concept == null)
			throw new Exception("Unable to find a concept with id: " + conceptId);
		
		List<ConceptSearchResult> searchResults = cs.findConceptAnswers(text, locale, concept);
		
		List<Drug> drugAnswers = new Vector<Drug>();
		for (ConceptAnswer conceptAnswer : concept.getAnswers(false)) {
			if (conceptAnswer.getAnswerDrug() != null)
				drugAnswers.add(conceptAnswer.getAnswerDrug());
		}
		
		List<Object> items = new Vector<Object>();
		for (ConceptSearchResult searchResult : searchResults) {
			items.add(new ConceptListItem(searchResult));
			// add drugs for concept if desired
			if (includeDrugConcepts) {
				Integer classId = searchResult.getConcept().getConceptClass().getConceptClassId();
				if (classId.equals(OpenmrsConstants.CONCEPT_CLASS_DRUG))
					for (Drug d : cs.getDrugsByConcept(searchResult.getConcept())) {
						if (drugAnswers.contains(d))
							items.add(new ConceptDrugListItem(d, locale));
					}
			}
		}
		
		return items;
	}
	
	public List<Object> getConceptSet(Integer conceptId) {
		Locale locale = Context.getLocale();
		ConceptService cs = Context.getConceptService();
		FormService fs = Context.getFormService();
		
		Concept concept = cs.getConcept(conceptId);
		
		List<Object> returnList = new Vector<Object>();
		
		if (concept.isSet()) {
			for (ConceptSet set : concept.getConceptSets()) {
				Field field = null;
				ConceptName cn = set.getConcept().getName(locale);
				ConceptDescription description = set.getConcept().getDescription(locale);
				for (Field f : fs.getFieldsByConcept(set.getConcept())) {
					if (f.getName().equals(cn.getName()) && f.getDescription().equals(description.getDescription())
					        && f.isSelectMultiple().equals(false))
						field = f;
				}
				if (field == null)
					returnList.add(new ConceptListItem(set.getConcept(), cn, locale));
				else
					returnList.add(new FieldListItem(field, locale));
			}
		}
		
		return returnList;
	}
	
	public List<ConceptListItem> getQuestionsForAnswer(Integer conceptId) {
		Locale locale = Context.getLocale();
		ConceptService cs = Context.getConceptService();
		
		Concept concept = cs.getConcept(conceptId);
		
		List<Concept> concepts = cs.getConceptsByAnswer(concept);
		
		List<ConceptListItem> items = new Vector<ConceptListItem>();
		for (Concept c : concepts) {
			ConceptName cn = c.getName(locale);
			items.add(new ConceptListItem(c, cn, locale));
		}
		
		return items;
	}
	
	public ConceptDrugListItem getDrug(Integer drugId) {
		Locale locale = Context.getLocale();
		ConceptService cs = Context.getConceptService();
		Drug d = cs.getDrug(drugId);
		
		return d == null ? null : new ConceptDrugListItem(d, locale);
	}
	
	public List<Object> getDrugs(Integer conceptId, boolean showConcept) {
		Locale locale = Context.getLocale();
		ConceptService cs = Context.getConceptService();
		Concept concept = cs.getConcept(conceptId);
		
		List<Object> items = new Vector<Object>();
		
		// Add this concept as the first option in the list
		// If there are no drugs to choose from, this will be automatically
		// selected
		// by the openmrsSearch.fillTable(objs) function
		if (showConcept == true) {
			ConceptDrugListItem thisConcept = new ConceptDrugListItem(null, conceptId, concept.getName(locale, false)
			        .getName());
			items.add(thisConcept);
		}
		
		// find drugs for this concept
		List<Drug> drugs = null;
		
		// if there are drugs to choose from, add some instructions
		if (drugs.size() > 0 && showConcept == true)
			items.add("Or choose a form of " + concept.getName(locale, false).getName());
		
		// miniaturize our drug objects
		for (Drug drug : drugs) {
			items.add(new ConceptDrugListItem(drug, locale));
		}
		
		return items;
	}
	
	public List<Object> findDrugs(String phrase, boolean includeRetired) throws APIException {
		if (includeRetired == true)
			throw new APIException("You should not include voideds in the search.");
		Locale locale = Context.getLocale();
		ConceptService cs = Context.getConceptService();
		
		List<Object> items = new Vector<Object>();
		
		// find drugs for this concept
		List<Drug> drugs = cs.getDrugs(phrase);
		
		// miniaturize our drug objects
		for (Drug drug : drugs) {
			items.add(new ConceptDrugListItem(drug, locale));
		}
		
		return items;
	}
	
	public boolean isValidNumericValue(Float value, Integer conceptId) {
		ConceptNumeric conceptNumeric = Context.getConceptService().getConceptNumeric(conceptId);
		
		return OpenmrsUtil.isValidNumericValue(value, conceptNumeric);
	}
	
	public String getConceptNumericUnits(Integer conceptId) {
		ConceptNumeric conceptNumeric = Context.getConceptService().getConceptNumeric(conceptId);
		
		return conceptNumeric.getUnits();
	}
	
	public List<ConceptListItem> getAnswersForQuestion(Integer conceptId) {
		Vector<ConceptListItem> ret = new Vector<ConceptListItem>();
		Concept c = Context.getConceptService().getConcept(conceptId);
		Collection<ConceptAnswer> answers = c.getAnswers(false);
		// TODO: deal with concept answers (e.g. drug) whose answer concept is null. (Not sure if this actually ever happens)
		Locale locale = Context.getLocale();
		for (ConceptAnswer ca : answers)
			if (ca.getAnswerConcept() != null) {
				ConceptName cn = ca.getAnswerConcept().getName(locale);
				ret.add(new ConceptListItem(ca.getAnswerConcept(), cn, locale));
			}
		return ret;
	}
	
	/**
	 * Converts the datatype of a concept that already has Obs referencing it from boolean to coded
	 * to support addition of more coded answers
	 * 
	 * @param conceptId the conceptId of the concept to be converted
	 * @return String to act as a signal if successfully converted or an error message
	 */
	public String convertBooleanConceptToCoded(Integer conceptId) {
		
		try {
			Context.getConceptService().convertBooleanConceptToCoded(Context.getConceptService().getConcept(conceptId));
			//this particular message isn't displayed in the browser rather it acts as
			//a signal that the concept was successfully converted and should refresh page. 
			return "refresh";
		}
		catch (ConceptsLockedException cle) {
			log.error("Tried to save/convert concept while concepts were locked", cle);
			return Context.getMessageSourceService().getMessage("Concept.concepts.locked");
		}
		catch (APIException e) {
			log.error("Error while trying to change the datatype of concept", e);
			return Context.getMessageSourceService().getMessage("Concept.cannot.save");
		}
	}
	
	/**
	 * Returns a map of results with the values as count of matches and a partial list of the
	 * matching concepts (depending on values of start and length parameters) while the keys are are
	 * 'count' and 'objectList' respectively, if the length parameter is not specified, then all
	 * matches will be returned from the start index if specified.
	 * 
	 * @param phrase concept name or conceptId
	 * @param includeRetired boolean if false, will exclude retired concepts
	 * @param includeClassNames List of ConceptClasses to restrict to
	 * @param excludeClassNames List of ConceptClasses to leave out of results
	 * @param includeDatatypeNames List of ConceptDatatypes to restrict to
	 * @param excludeDatatypeNames List of ConceptDatatypes to leave out of results
	 * @param start the beginning index
	 * @param length the number of matching concepts to return
	 * @param getMatchCount Specifies if the count of matches should be included in the returned map
	 * @return a map of results
	 * @throws APIException
	 * @since 1.8
	 */
	public Map<String, Object> findCountAndConcepts(String phrase, boolean includeRetired, List<String> includeClassNames,
	        List<String> excludeClassNames, List<String> includeDatatypeNames, List<String> excludeDatatypeNames,
	        Integer start, Integer length, boolean getMatchCount) throws APIException {
		//Map to return
		Map<String, Object> resultsMap = new HashMap<String, Object>();
		Vector<Object> objectList = new Vector<Object>();
		User currentUser = Context.getAuthenticatedUser();
		Locale defaultLocale = Context.getLocale();
		
		// get the list of locales to search on from the user's
		// defined proficient locales (if applicable)
		List<Locale> localesToSearchOn = null;
		if (currentUser != null)
			localesToSearchOn = currentUser.getProficientLocales();
		
		if (localesToSearchOn == null)
			// we're working with an anonymous user right now or
			// with a user that has not defined any proficient locales
			localesToSearchOn = new Vector<Locale>();
		
		// add the user's locale
		if (localesToSearchOn.size() == 0) {
			localesToSearchOn.add(defaultLocale);
			
			// if country is specified, also add the generic language locale
			if (!"".equals(defaultLocale.getCountry())) {
				localesToSearchOn.add(new Locale(defaultLocale.getLanguage()));
			}
			
		}
		
		// debugging output
		if (log.isDebugEnabled()) {
			StringBuffer searchLocalesString = new StringBuffer();
			for (Locale loc : localesToSearchOn) {
				searchLocalesString.append(loc.toString() + " ");
			}
			log.debug("searching locales: " + searchLocalesString);
		}
		
		if (includeClassNames == null)
			includeClassNames = new Vector<String>();
		if (excludeClassNames == null)
			excludeClassNames = new Vector<String>();
		if (includeDatatypeNames == null)
			includeDatatypeNames = new Vector<String>();
		if (excludeDatatypeNames == null)
			excludeDatatypeNames = new Vector<String>();
		
		try {
			ConceptService cs = Context.getConceptService();
			
			if (!StringUtils.isBlank(phrase)) {
				// turn classnames into class objects
				List<ConceptClass> includeClasses = new Vector<ConceptClass>();
				for (String name : includeClassNames)
					if (!"".equals(name))
						includeClasses.add(cs.getConceptClassByName(name));
				
				// turn classnames into class objects
				List<ConceptClass> excludeClasses = new Vector<ConceptClass>();
				for (String name : excludeClassNames)
					if (!"".equals(name))
						excludeClasses.add(cs.getConceptClassByName(name));
				
				// turn classnames into class objects
				List<ConceptDatatype> includeDatatypes = new Vector<ConceptDatatype>();
				for (String name : includeDatatypeNames)
					if (!"".equals(name))
						includeDatatypes.add(cs.getConceptDatatypeByName(name));
				
				// turn classnames into class objects
				List<ConceptDatatype> excludeDatatypes = new Vector<ConceptDatatype>();
				for (String name : excludeDatatypeNames)
					if (!"".equals(name))
						excludeDatatypes.add(cs.getConceptDatatypeByName(name));
				
				int matchCount = 0;
				if (getMatchCount) {
					//get the count of matches
					matchCount += cs.getCountOfConcepts(phrase, localesToSearchOn, includeRetired, includeClasses,
					    excludeClasses, includeDatatypes, excludeDatatypes, null);
					if (phrase.matches("\\d+")) {
						// user searched on a number. Insert concept with
						// corresponding conceptId
						Concept c = cs.getConcept(Integer.valueOf(phrase));
						if (c != null && (!c.isRetired() || includeRetired))
							matchCount++;
						
					}
					
					//if (includeDrugs)
					//	matchCount += cs.getCountOfDrugs(phrase, null, false, includeRetired);
				}
				
				//if we have any matches or this isn't the first ajax call when the caller
				//requests for the count
				if (matchCount > 0 || !getMatchCount) {
					objectList.addAll(findBatchOfConcepts(phrase, includeRetired, includeClassNames, excludeClassNames,
					    includeDatatypeNames, excludeDatatypeNames, start, length));
				}
				
				resultsMap.put("count", matchCount);
				resultsMap.put("objectList", objectList);
			} else {
				resultsMap.put("count", 0);
				objectList.add(Context.getMessageSourceService().getMessage("searchWidget.noMatchesFound"));
			}
			
		}
		catch (Exception e) {
			log.error("Error while searching for concepts", e);
			objectList.clear();
			objectList.add(Context.getMessageSourceService().getMessage("Concept.search.error") + " - " + e.getMessage());
			resultsMap.put("count", 0);
			resultsMap.put("objectList", objectList);
		}
		
		return resultsMap;
	}
}
