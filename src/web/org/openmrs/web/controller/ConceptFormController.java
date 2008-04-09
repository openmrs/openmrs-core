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
package org.openmrs.web.controller;

import static org.openmrs.util.OpenmrsConstants.OPENMRS_CONCEPT_LOCALES;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.ConceptAnswer;
import org.openmrs.ConceptName;
import org.openmrs.ConceptNumeric;
import org.openmrs.ConceptSet;
import org.openmrs.ConceptSynonym;
import org.openmrs.Form;
import org.openmrs.api.APIException;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.web.WebConstants;
import org.openmrs.propertyeditor.ConceptAnswersEditor;
import org.openmrs.propertyeditor.ConceptClassEditor;
import org.openmrs.propertyeditor.ConceptDatatypeEditor;
import org.openmrs.propertyeditor.ConceptSetsEditor;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

public class ConceptFormController extends SimpleFormController {
	
    /** Logger for this class and subclasses */
    protected final Log log = LogFactory.getLog(getClass());
    
    /**
	 * 
	 * Allows for other Objects to be used as values in input tags.
	 *   Normally, only strings and lists are expected 
	 * 
	 * @see org.springframework.web.servlet.mvc.BaseCommandController#initBinder(javax.servlet.http.HttpServletRequest, org.springframework.web.bind.ServletRequestDataBinder)
	 */
	protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
		super.initBinder(request, binder);
		
		
        NumberFormat nf = NumberFormat.getInstance(Context.getLocale());
        binder.registerCustomEditor(java.lang.Integer.class,
                new CustomNumberEditor(java.lang.Integer.class, nf, true));
        binder.registerCustomEditor(java.lang.Double.class,
                new CustomNumberEditor(java.lang.Double.class, nf, true));
        binder.registerCustomEditor(java.util.Date.class, 
        		new CustomDateEditor(SimpleDateFormat.getDateInstance(SimpleDateFormat.SHORT, Context.getLocale()), true));
        binder.registerCustomEditor(org.openmrs.ConceptClass.class, 
        		new ConceptClassEditor());
        binder.registerCustomEditor(org.openmrs.ConceptDatatype.class, 
        		new ConceptDatatypeEditor());
        /*binder.registerCustomEditor(java.util.Collection.class, "synonyms", 
        		new ConceptSynonymsEditor(locale)); */
        binder.registerCustomEditor(java.util.Collection.class, "conceptSets", 
        		new ConceptSetsEditor());
        binder.registerCustomEditor(java.util.Collection.class, "answers", 
        		new ConceptAnswersEditor());

	}

	/**
	 * @see org.springframework.web.servlet.mvc.AbstractFormController#processFormSubmission(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.Object, org.springframework.validation.BindException)
	 */
	protected ModelAndView processFormSubmission(HttpServletRequest request, HttpServletResponse response, Object object, BindException errors) throws Exception {
	
		Concept concept = (Concept)object;
		ConceptService cs = Context.getConceptService();
		
		// check to see if they clicked next/previous concept:
		String jumpAction = request.getParameter("jumpAction");
		if (jumpAction != null) {
			Concept newConcept = null;
			if ("previous".equals(jumpAction))
				newConcept = cs.getPrevConcept(concept);
			else if ("next".equals(jumpAction))
				newConcept = cs.getNextConcept(concept);
			if (newConcept != null)
				return new ModelAndView(new RedirectView(getSuccessView() + "?conceptId=" + newConcept.getConceptId()));
			else
				return new ModelAndView(new RedirectView(getSuccessView()));
		}
		
		if (Context.isAuthenticated()) {
			
			MessageSourceAccessor msa = getMessageSourceAccessor();
			String action = request.getParameter("action");
			
			if (!action.equals(msa.getMessage("Concept.delete"))) {
	
				String isSet = ServletRequestUtils.getStringParameter(request, "conceptSet", "");
				if (isSet.equals(""))
					concept.setSet(false);
				else
					concept.setSet(true);
				log.debug("isSet: '" + isSet + "' ");
				log.debug("concept.set: '" + concept.isSet() + "'");
				
				// ==== Concept Synonyms ====
					Collection<ConceptSynonym> originalSyns = concept.getSynonyms();
					for (Locale l : OPENMRS_CONCEPT_LOCALES()) {
						// the attribute *must* be named differently than the property, otherwise
						//   spring will modify the property as a text array
						String localeName = l.toString();
						log.debug("newSynonyms: " + request.getParameter("newSynonyms_" + localeName));
						String[] tempSyns = request.getParameter("newSynonyms_" + localeName).split(",");
						log.debug("tempSyns: ");
						for (String s : tempSyns)
							log.debug(s);
						Set<ConceptSynonym> parameterSyns = new HashSet<ConceptSynonym>();
						
						//set up parameter Synonym Set for easier add/delete functions
						// and removal of duplicates
						for (String syn : tempSyns) {
							syn = syn.trim();
							if (!syn.equals(""))
								parameterSyns.add(new ConceptSynonym(concept, syn.toUpperCase(), l));
						}
						
						log.debug("initial originalSyns: ");
						for (ConceptSynonym s : originalSyns)
							log.debug(s);
						
						// Union the originalSyns and parameterSyns to get the 'clean' synonyms
						//   remove synonym from originalSynonym if 'clean' (already in db)
						Set<ConceptSynonym> originalSynsCopy = new HashSet<ConceptSynonym>();
						originalSynsCopy.addAll(originalSyns);
						for (ConceptSynonym o : originalSynsCopy) {
							if (o.getLocale().equals(l.getLanguage().substring(0, 2)) &&
								!parameterSyns.contains(o)) {  // .contains() is only usable because we overrode .equals()
								originalSyns.remove(o);
							}
						}
						
						// add all new syns from parameter set
						for (ConceptSynonym p : parameterSyns) {
							if (!originalSyns.contains(p)) {  // .contains() is only usable because we overrode .equals()
								originalSyns.add(p);
							}
						}
						
						log.debug("evaluated parameterSyns: ");
						for (ConceptSynonym s : parameterSyns)
							log.debug(s);
						
						log.debug("evaluated originalSyns: ");
						for (ConceptSynonym s : originalSyns)
							log.debug(s);
						
					}
					concept.setSynonyms(originalSyns);
					
				// ====zero out conceptSets====
					String conceptSets = request.getParameter("conceptSets");
					if (conceptSets == null)
						concept.setConceptSets(null); 
				
				// ====set concept_name properties for locales in this page
					int numberOfNamesSpecified = 0;
					for (Locale l : OPENMRS_CONCEPT_LOCALES()) {
						String localeName = l.toString();
						String conceptName = request.getParameter("name_" + localeName).toUpperCase();
						String shortName = request.getParameter("shortName_" + localeName);
						String description = request.getParameter("description_" + localeName);
						if ((shortName.length() > 0 || description.length() > 0) && conceptName.length() < 1) {
							errors.reject("dictionary.error.needName");
						}
						ConceptName cn = concept.getName(l, true);
						if (cn != null) {
							if (conceptName.length() > 0) {
								++numberOfNamesSpecified;
								cn.setName(conceptName);
								cn.setShortName(shortName);
								cn.setDescription(description);
							} else {
								concept.removeName(cn);
							}
						} else {
							if (conceptName.length() > 0) {
								++numberOfNamesSpecified;
								concept.addName(new ConceptName(conceptName, shortName, description, l));
							}
						}
					}
					if (numberOfNamesSpecified == 0) { 
						errors.reject("error.names.length");
					}

			}
		}
		else {
			errors.reject("auth.invalid");
		}
		
		return super.processFormSubmission(request, response, concept, errors); 
	}

	/**
	 * 
	 * The onSubmit function receives the form/command object that was modified
	 *   by the input form and saves it to the db
	 * 
	 * @see org.springframework.web.servlet.mvc.SimpleFormController#onSubmit(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.Object, org.springframework.validation.BindException)
	 */
	protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object obj, BindException errors) throws Exception {
		
		HttpSession httpSession = request.getSession();
		ConceptService cs = Context.getConceptService();
		
		if (Context.isAuthenticated()) {
			
			Concept concept = (Concept)obj;
			
			MessageSourceAccessor msa = getMessageSourceAccessor();
			String action = request.getParameter("action");
			
			if (action.equals(msa.getMessage("Concept.delete"))) {
				try {
					cs.deleteConcept(concept);
					httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "Concept.deleted");
					return new ModelAndView(new RedirectView("index.htm"));
				}
				catch (APIException e) {
					log.error(e);
					httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "Concept.cannot.delete");
					return new ModelAndView(new RedirectView(getSuccessView() + "?conceptId=" + concept.getConceptId().toString()));
				}
			}
			else {
				String isSet = ServletRequestUtils.getStringParameter(request, "conceptSet", "");
				if (isSet.equals(""))
					concept.setSet(false);
				else
					concept.setSet(true);
				
				boolean isNew = false;
				try {
					if (concept.getConceptId() == null) {
						isNew = true;
						concept.setConceptId(cs.getNextAvailableId());
						if (concept.getDatatype() != null && concept.getDatatype().getName().equals("Numeric")) {
							ConceptNumeric cn = getConceptNumeric(concept, request);
							cs.createConcept(cn);
						}
						else {
							cs.createConcept(concept);
						}
					}
					else {
						if (concept.getDatatype() != null && concept.getDatatype().getName().equals("Numeric")) {
							ConceptNumeric cn = getConceptNumeric(concept, request);
							cs.updateConcept(cn);
						}
						else {
							cs.updateConcept(concept);
						}
					}
					httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "Concept.saved");
				}
				catch (APIException e) {
					log.error("Error while trying to save concept", e);
					httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "Concept.cannot.save");
					if (isNew) {
						errors.reject("concept", "Concept.cannot.save");
						return new ModelAndView(new RedirectView(getSuccessView()));
					}
				}

				return new ModelAndView(new RedirectView(getSuccessView() + "?conceptId=" + concept.getConceptId()));
			}
		}
		
		return new ModelAndView(new RedirectView(getFormView()));
	}

	/**
	 * 
	 * This is called prior to displaying a form for the first time.  It tells Spring
	 *   the form/command object to load into the request
	 * 
	 * @see org.springframework.web.servlet.mvc.AbstractFormController#formBackingObject(javax.servlet.http.HttpServletRequest)
	 */
    protected Object formBackingObject(HttpServletRequest request) throws ServletException {

		Concept concept = null;
		
		ConceptService cs = Context.getConceptService();
		String conceptId = request.getParameter("conceptId");
		if (conceptId == null) {

		}
		if (conceptId != null) {
    		concept = cs.getConcept(Integer.valueOf(conceptId));
    		//if (concept.isNumeric())
    		//	concept = (ConceptNumeric)concept;
    	}
		
		if (concept == null)
			concept = new Concept();
		
		return concept;
    }
    
	/**
	 * 
	 * Called prior to form display.  Allows for data to be put 
	 * 	in the request to be used in the view
	 * 
	 * @see org.springframework.web.servlet.mvc.SimpleFormController#referenceData(javax.servlet.http.HttpServletRequest)
	 */
	protected Map referenceData(HttpServletRequest request) throws Exception {
		
		Locale locale = Context.getLocale();
		Map<String, Object> map = new HashMap<String, Object>();
		String defaultVerbose = "false";
		
		ConceptService cs = Context.getConceptService();
		String conceptId = request.getParameter("conceptId");
		ConceptName conceptName = new ConceptName();
		Collection<ConceptSynonym> conceptSynonyms = new Vector<ConceptSynonym>();
		Map<String, ConceptName> conceptNamesByLocale = new HashMap<String, ConceptName>();
		Map<Locale, Collection<ConceptSynonym>> conceptSynonymsByLocale = new HashMap<Locale, Collection<ConceptSynonym>>();
		//Map<String, ConceptName> conceptSets = new TreeMap<String, ConceptName>();
		Map<Double, Object[]> conceptSets = new TreeMap<Double, Object[]>();
		Map<String, String> conceptAnswers = new TreeMap<String, String>();
		Collection<Form> forms = new HashSet<Form>();
		Map<Integer, String> questionsAnswered = new TreeMap<Integer, String>();
		Map<Integer, String> containedInSets = new TreeMap<Integer, String>();
		
		boolean isNew = true;
		
		if (conceptId != null) {
			Concept concept = cs.getConcept(Integer.valueOf(conceptId));
			
			if (concept != null) {
				isNew = false;
				// get conceptNames for all locales 
				for (Locale l : OPENMRS_CONCEPT_LOCALES()) {
					ConceptName cn = concept.getName(l, true);
					if (cn == null) {
						cn = new ConceptName();
					}
					conceptNamesByLocale.put(l.toString(), cn);
				}
				
				// get conceptSynonyms for all locales
				for (Locale l : OPENMRS_CONCEPT_LOCALES()) { 
					conceptSynonymsByLocale.put(l, concept.getSynonyms(l));
				}
				
				// get locale specific conceptName object
				conceptName = concept.getName(locale);
				if (conceptName == null) 
					conceptName = new ConceptName();
				
				// get locale specific synonyms
				conceptSynonyms = concept.getSynonyms(locale);
	    		
				// get concept sets with locale decoded names
		    	for (ConceptSet set : concept.getConceptSets()) {
		    		Object[] arr = {set.getConcept().getConceptId().toString(), set.getConcept().getName(locale)}; 
		    		conceptSets.put(set.getSortWeight(), arr);
		    	}
				
		    	// get concept answers with locale decoded names
		    	for (ConceptAnswer answer : concept.getAnswers(true)) {
		    		log.debug("getting answers");
		    		String key = answer.getAnswerConcept().getConceptId().toString();
		    		ConceptName cn = answer.getAnswerConcept().getName(locale);
		    		String name = "";
		    		if (cn != null)
		    			name = cn.toString();
		    		if (answer.getAnswerDrug() != null) {
		    			// if this answer is a drug, append the drug id information
		    			key = key + "^" + answer.getAnswerDrug().getDrugId();
		    			name = answer.getAnswerDrug().getFullName(locale);
		    		}
		    		if (answer.getAnswerConcept().isRetired())
		    			name = "<span class='retired'>" + name + "</span>";
		    		conceptAnswers.put(key, name);
		    	}

		    	forms = Context.getFormService().getForms(concept);
		    	
		    	for (Concept c : Context.getConceptService().getQuestionsForAnswer(concept)) {
		    		ConceptName cn = c.getName(locale);
		    		if (cn == null)
		    			questionsAnswered.put(c.getConceptId(), "No Name Defined");
		    		else
		    			questionsAnswered.put(c.getConceptId(), cn.getName());
		    	}
		    	
		    	for (ConceptSet set : Context.getConceptService().getSetsContainingConcept(concept)) {
		    		Concept c = set.getConceptSet();
		    		ConceptName cn = c.getName(locale);
		    		if (cn == null)
		    			containedInSets.put(c.getConceptId(), "No Name Defined");
		    		else
		    			containedInSets.put(c.getConceptId(), cn.getName());
		    	}
			}
			
			if (Context.isAuthenticated())
				defaultVerbose = Context.getAuthenticatedUser().getUserProperty(OpenmrsConstants.USER_PROPERTY_SHOW_VERBOSE);
		}
			 
		if (isNew) {
			for (Locale l : OPENMRS_CONCEPT_LOCALES()) {
				conceptNamesByLocale.put(l.toString(), new ConceptName());
			}
			
			// get conceptSynonyms for all locales
			for (Locale l : OPENMRS_CONCEPT_LOCALES()) { 
				conceptSynonymsByLocale.put(l, new HashSet<ConceptSynonym>());
			}
		}
		
		map.put("locales", OPENMRS_CONCEPT_LOCALES());
		map.put("conceptName", conceptName);
    	for (Map.Entry<String, ConceptName> e : conceptNamesByLocale.entrySet()) {
    		map.put("conceptName_" + e.getKey(), e.getValue());
    	}
    	map.put("conceptSynonyms", conceptSynonyms);
    	map.put("conceptSynonymsByLocale", conceptSynonymsByLocale);
    	map.put("conceptSets", conceptSets);
    	map.put("conceptAnswers", conceptAnswers);
    	map.put("formsInUse", forms);
    	map.put("questionsAnswered", questionsAnswered);
    	map.put("containedInSets", containedInSets);
		
    	//get complete class and datatype lists 
		map.put("classes", cs.getConceptClasses());
		map.put("datatypes", cs.getConceptDatatypes());
		
		// make spring locale available to jsp
		map.put("locale", locale.getLanguage().substring(0, 2));
			
		
		map.put("defaultVerbose", defaultVerbose.equals("true") ? true : false);
		
		return map;
	} 
	
	private ConceptNumeric getConceptNumeric(Concept concept, HttpServletRequest request) {
		
		ConceptNumeric cn = null;
		if (concept instanceof ConceptNumeric)
			cn = (ConceptNumeric)concept;
		else
			cn = new ConceptNumeric(concept);
		
		String d = null;
		
		d = request.getParameter("hiAbsolute");
		if (d != null && d.length() > 0)
			cn.setHiAbsolute(new Double(d));
		d = request.getParameter("hiCritical");
		if (d != null && d.length() > 0)
			cn.setHiCritical(new Double(d));
		d = request.getParameter("hiNormal");
		if (d != null && d.length() > 0)
				cn.setHiNormal(new Double(d));
		
		d = request.getParameter("lowAbsolute");
		if (d != null && d.length() > 0)
				cn.setLowAbsolute(new Double(d));
		d = request.getParameter("lowCritical");
		if (d != null && d.length() > 0)
				cn.setLowCritical(new Double(d));
		d = request.getParameter("lowNormal");
		if (d != null && d.length() > 0)
				cn.setLowNormal(new Double(d));
		
		cn.setUnits(request.getParameter("units"));
		
		Boolean precise = false;
		if (request.getParameter("precise") != null)
			precise = true;
		cn.setPrecise(precise);
		
		return cn;
	}
}
