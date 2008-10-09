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

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
import org.openmrs.ConceptDescription;
import org.openmrs.ConceptMap;
import org.openmrs.ConceptName;
import org.openmrs.ConceptNameTag;
import org.openmrs.ConceptNumeric;
import org.openmrs.ConceptSet;
import org.openmrs.ConceptSource;
import org.openmrs.Form;
import org.openmrs.User;
import org.openmrs.api.APIAuthenticationException;
import org.openmrs.api.APIException;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.propertyeditor.ConceptAnswersEditor;
import org.openmrs.propertyeditor.ConceptClassEditor;
import org.openmrs.propertyeditor.ConceptDatatypeEditor;
import org.openmrs.propertyeditor.ConceptSetsEditor;
import org.openmrs.propertyeditor.ConceptSourceEditor;
import org.openmrs.util.LocaleUtility;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.web.WebConstants;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

/**
 * This is the controlling class for hte conceptForm.jsp page.
 * 
 * It initBinder and formBackingObject are called before page load.
 * 
 * After submission, formBackingObject (because we're not a session 
 * form), processFormSubmission, and onSubmit methods are called
 * 
 * @see org.openmrs.Concept
 */
public class ConceptFormController extends SimpleFormController {

	/** Logger for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());

	/**
	 * 
	 * Allows for other Objects to be used as values in input tags. Normally,
	 * only strings and lists are expected
	 * 
	 * @see org.springframework.web.servlet.mvc.BaseCommandController#initBinder(javax.servlet.http.HttpServletRequest,
	 *      org.springframework.web.bind.ServletRequestDataBinder)
	 */
	protected void initBinder(HttpServletRequest request,
	        ServletRequestDataBinder binder) throws Exception {
		super.initBinder(request, binder);

		NumberFormat nf = NumberFormat.getInstance(Context.getLocale());
		binder.registerCustomEditor(java.lang.Integer.class,
		        new CustomNumberEditor(java.lang.Integer.class, nf, true));
		binder.registerCustomEditor(java.lang.Double.class,
		        new CustomNumberEditor(java.lang.Double.class, nf, true));
		binder.registerCustomEditor(java.util.Date.class, new CustomDateEditor(
		        SimpleDateFormat.getDateInstance(SimpleDateFormat.SHORT,
		                Context.getLocale()), true));
		binder.registerCustomEditor(org.openmrs.ConceptClass.class,
		        new ConceptClassEditor());
		binder.registerCustomEditor(org.openmrs.ConceptDatatype.class,
		        new ConceptDatatypeEditor());
		/*
		 * binder.registerCustomEditor(java.util.Collection.class, "synonyms",
		 * new ConceptSynonymsEditor(locale));
		 */
		binder.registerCustomEditor(java.util.Collection.class, "conceptSets",
		        new ConceptSetsEditor());
		binder.registerCustomEditor(java.util.Collection.class, "answers",
		        new ConceptAnswersEditor());
		binder.registerCustomEditor(org.openmrs.ConceptSource.class,
		        new ConceptSourceEditor());

	}

	/**
	 * @see org.springframework.web.servlet.mvc.AbstractFormController#processFormSubmission(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse, java.lang.Object,
	 *      org.springframework.validation.BindException)
	 */
	protected ModelAndView processFormSubmission(HttpServletRequest request,
	        HttpServletResponse response, Object object, BindException errors)
	        throws Exception {

		Concept concept = (Concept) object;
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
				return new ModelAndView(new RedirectView(getSuccessView()
				        + "?conceptId=" + newConcept.getConceptId()));
			else
				return new ModelAndView(new RedirectView(getSuccessView()));
		}

		if (Context.isAuthenticated()) {

			MessageSourceAccessor msa = getMessageSourceAccessor();
			String action = request.getParameter("action");

			if (!action.equals(msa.getMessage("Concept.delete"))) {
				
				Collection<Locale> conceptLocales = cs.getLocalesOfConceptNames();
                String newLocaleSpec = request.getParameter("newLocaleAdded");
                if (newLocaleSpec != null) {
                	Locale newLocale = LocaleUtility.fromSpecification(newLocaleSpec);
                	conceptLocales.add(newLocale);
                }

				String isSet = ServletRequestUtils.getStringParameter(request,
				        "conceptSet", "");
				if (isSet.equals(""))
					concept.setSet(false);
				else
					concept.setSet(true);
				log.debug("isSet: '" + isSet + "' ");
				log.debug("concept.set: '" + concept.isSet() + "'");

				int numberOfNewConceptNames = 0;
				
				// ==== Concept Synonyms ====
				Collection<ConceptName> originalSyns = concept.getNames();
				if (originalSyns == null)
					originalSyns = new HashSet<ConceptName>();

				for (Locale l : conceptLocales) {
					// the attribute *must* be named differently than the
					// property, otherwise
					// spring will modify the property as a text array
					String localeName = l.toString();
					log
					        .debug("newSynonyms: "
					                + request.getParameter("newSynonyms_"
					                        + localeName));
					String[] tempSyns = request.getParameter(
					        "newSynonyms_" + localeName).split(",");
					log.debug("tempSyns: ");
					for (String s : tempSyns)
						log.debug(s);
					Set<ConceptName> parameterSyns = new HashSet<ConceptName>();

					// set up parameter Synonym Set for easier add/delete
					// functions
					// and removal of duplicates
					for (String syn : tempSyns) {
						syn = syn.trim();
						if (!syn.equals("")) {
							ConceptName anotherSynonym = new ConceptName(syn
							        .toUpperCase(), l);
							anotherSynonym.setConcept(concept);
							parameterSyns.add(anotherSynonym);
						}
					}
					
					if (log.isDebugEnabled()) {
						log.debug("initial originalSyns: ");
						for (ConceptName s : originalSyns)
							log.debug(s);
					}
					// Union the originalSyns and parameterSyns to get the
					// 'clean' synonyms
					// remove synonym from originalSynonym if 'clean' (already
					// in db)
					Set<ConceptName> originalSynsCopy = new HashSet<ConceptName>();
					originalSynsCopy.addAll(originalSyns);
					for (ConceptName o : originalSynsCopy) {
						if (o.getLocale().equals(l)
//						        l.getLanguage().substring(0, 2)) ABKTODO -- FIX THIS!!!
						        && !parameterSyns.contains(o)) { // .contains()
																	// is only
																	// usable
																	// because
																	// we
																	// overrode
																	// .equals()
							originalSyns.remove(o);
						}
						
						if (log.isDebugEnabled()) {
							log.debug("evaluated parameterSyns: ");
							for (ConceptName s : parameterSyns)
								log.debug(s);
							
							log.debug("evaluated originalSyns: ");
							for (ConceptName s : originalSyns)
								log.debug(s);
						}
						
					}

					
					// add all new syns from parameter set
					for (ConceptName p : parameterSyns) {
						if (!originalSyns.contains(p)) { // .contains() is
															// only usable
															// because we
															// overrode
															// .equals()
							originalSyns.add(p);
							++numberOfNewConceptNames;
						}
					}

					log.debug("evaluated parameterSyns: ");
					for (ConceptName s : parameterSyns)
						log.debug(s);

					log.debug("evaluated originalSyns: ");
					for (ConceptName s : originalSyns)
						log.debug(s);

				}
				concept.setNames(originalSyns);

				// ====zero out conceptSets====
				String conceptSets = request.getParameter("conceptSets");
				if (conceptSets == null)
					concept.setConceptSets(null);

				// ====set concept_name properties for locales in this page
				User currentUser = Context.getAuthenticatedUser();
				int numberOfNamesSpecified = 0;
				for (Locale l : conceptLocales) {
					String localeName = l.toString();
					String conceptName = request.getParameter(
					        "name_" + localeName).toUpperCase();
					String shortName = request.getParameter("shortName_"
					        + localeName);
					String description = request.getParameter("description_"
					        + localeName);
					if ((shortName.length() > 0 || description.length() > 0)
					        && conceptName.length() < 1) {
						errors.reject("dictionary.error.needName");
					}
					ConceptName preferredName = concept.getName(l, true);
					if (preferredName != null) {
						if (conceptName.length() > 0) {
							++numberOfNamesSpecified;
							preferredName.setName(conceptName);
						} else {
							concept.removeName(preferredName);
						}
					} else {
						if (conceptName.length() > 0) {
							++numberOfNamesSpecified;
							preferredName = new ConceptName(conceptName, l);
							concept.setPreferredName(l, preferredName);
							++numberOfNewConceptNames;
						}
					}
					/*
					 * ABK: ConceptName.ShortName has been promoted to a name,
					 * tagged as a short name.
					 */
					if (shortName.length() > 0) {
						ConceptName conceptShortName = new ConceptName(shortName, l);
						concept.setShortName(l, conceptShortName);
						++numberOfNewConceptNames;
					}
					/*
					 * ABK: ConceptName.Description is now an independent entity
					 * (was a field of ConceptName).
					 */
					if (description.length() > 0) {
						ConceptDescription cd = concept.getDescription(l, true);
						if (cd != null) {
							if (!cd.getDescription().equals(description)) {
								cd.setDescription(description);
								cd.setChangedBy(currentUser);
								cd.setDateChanged(new Date());
							}
						} else {
							cd = new ConceptDescription(description, l);
							concept.addDescription(cd);
						}
					}
				} // end loop over concept locales

				if (numberOfNamesSpecified == 0) {
					errors.reject("error.names.length");
				}

				// remove deleted concept mappings from this concept (ignoring
				// just added ones)
				// must do this before adding new ones to avoid the set be
				// rearranged
				int i = 0;
				List<ConceptMap> conceptMappingsToDelete = new ArrayList<ConceptMap>();
				Collection<ConceptMap> currentConceptMappings = concept.getConceptMappings();
				if (currentConceptMappings != null)
				{
					for (ConceptMap mapping : currentConceptMappings) {
						String sourceCode = request.getParameter("conceptMappings["
						        + i++ + "].sourceCode");
	
						// if there isn't a query param by this name, it was removed
						// (via js)
						if (sourceCode == null) {
							if (currentConceptMappings.contains(mapping))
								conceptMappingsToDelete.add(mapping);
						}
					}
				}

				// add new concept mappings to this concept's mappings set
				String[] sourceCodes = ServletRequestUtils.getStringParameters(
				        request, "newConceptMappingSourceCode");
				String[] sources = ServletRequestUtils.getStringParameters(
				        request, "newConceptMappingSource");
				for (int x = 0; x < sourceCodes.length; x++) {
					String sourceCode = sourceCodes[x];
					if (sourceCode.length() == 0) break; // ABKTODO: this is obviously a hack
					String sourceString = sources[x];

					// both code and source are required, skip this one
					// if they aren't both filled in
					if (sourceCode.length() < 1 || sourceString.length() < 1)
						continue;

					// create the new map object
					ConceptMap newConceptMap = new ConceptMap();
					newConceptMap.setSourceCode(sourceCode);
					ConceptSource source = cs.getConceptSource(Integer
					        .valueOf(sourceString));
					newConceptMap.setSource(source);

					concept.addConceptMapping(newConceptMap);
				}

				// perform this after addition of new mappings so users can't
				// delete and
				// add a mapping in one go
				// ABKTODO: another hack! yay me!
				currentConceptMappings = concept.getConceptMappings(); // ABKTODO: get the latest, which may still be null
				if (currentConceptMappings != null)
				{
					concept.getConceptMappings().removeAll(conceptMappingsToDelete);
				}
				
				/** Resolve any new tags which may have been added. */
				resolveConceptNameTags(originalSyns);

			} // end "if action != delete"
		} else {
			errors.reject("auth.invalid");
		}

		return super.processFormSubmission(request, response, concept, errors);
	}

	/**
	 * Reviews all the tags applied to a set of concept names, assigning the
	 * correct id for existing tags, and creating new tags.
	 * 
	 * @param conceptNames
	 */
	private void resolveConceptNameTags(Collection<ConceptName> conceptNames) {

		ConceptService cs = Context.getConceptService();

		for (ConceptName cn : conceptNames) {
			for (ConceptNameTag tag : cn.getTags()) {
				ConceptNameTag existingTag = cs.getConceptNameTagByName(tag
				        .getTag());
				if (existingTag == null) {
					cs.saveConceptNameTag(tag);
				} else {
					cn.removeTag(tag);
					cn.addTag(existingTag);
				}

			}
		}
	}

	/**
	 * 
	 * The onSubmit function receives the form/command object that was modified
	 * by the input form and saves it to the db
	 * 
	 * @see org.springframework.web.servlet.mvc.SimpleFormController#onSubmit(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse, java.lang.Object,
	 *      org.springframework.validation.BindException)
	 */
	protected ModelAndView onSubmit(HttpServletRequest request,
	        HttpServletResponse response, Object obj, BindException errors)
	        throws Exception {

		HttpSession httpSession = request.getSession();
		ConceptService cs = Context.getConceptService();

		if (Context.isAuthenticated()) {

			Concept concept = (Concept) obj;

			MessageSourceAccessor msa = getMessageSourceAccessor();
			String action = request.getParameter("action");

			if (action.equals(msa.getMessage("Concept.delete"))) {
				try {
					cs.purgeConcept(concept);
					httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR,
					        "Concept.deleted");
					return new ModelAndView(new RedirectView("index.htm"));
				} catch (APIException e) {
					log.error(e);
					httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR,
					        "Concept.cannot.delete");
					return new ModelAndView(
					        new RedirectView(getSuccessView() + "?conceptId="
					                + concept.getConceptId().toString()));
				}
			} else {
				String isSet = ServletRequestUtils.getStringParameter(request,
				        "conceptSet", "");
				if (isSet.equals(""))
					concept.setSet(false);
				else
					concept.setSet(true);

				boolean isNew = false;
				try {
					if (concept.getConceptId() == null) {
						isNew = true;
						if (concept.getDatatype() != null && concept.getDatatype().getName().equals("Numeric")) {
							concept = getConceptNumeric(concept, request);
						}
						cs.saveConcept(concept);
					}
					else {
						if (concept.getDatatype() != null && concept.getDatatype().getName().equals("Numeric")) {
							concept = getConceptNumeric(concept, request);
						}

						cs.saveConcept(concept);
					}
					httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR,
					        "Concept.saved");
				} catch (APIException e) {
					log.error("Error while trying to save concept", e);
					httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR,
					        "Concept.cannot.save");
					if (isNew) {
						errors.reject("concept", "Concept.cannot.save");
						return new ModelAndView(new RedirectView(
						        getSuccessView()));
					}
				}

				return new ModelAndView(new RedirectView(getSuccessView()
				        + "?conceptId=" + concept.getConceptId()));
			}
		}

		return new ModelAndView(new RedirectView(getFormView()));
	}
	
	/**
	 * This is called prior to displaying a form for the first time. It tells
	 * Spring the form/command object to load into the request
	 * 
	 * @see org.springframework.web.servlet.mvc.AbstractFormController#formBackingObject(javax.servlet.http.HttpServletRequest)
	 */
	protected Object formBackingObject(HttpServletRequest request)
	        throws ServletException {

		Concept concept = null;

		ConceptService cs = Context.getConceptService();
		String conceptId = request.getParameter("conceptId");
		if (conceptId == null) {
			// do nothing
		}
		else if (conceptId != null) {
    		concept = cs.getConcept(Integer.valueOf(conceptId));
    	}
		
		if (concept == null)
			concept = new Concept();

		return concept;
	}

	/**
	 * 
	 * Called prior to form display. Allows for data to be put in the request to
	 * be used in the view
	 * 
	 * @see org.springframework.web.servlet.mvc.SimpleFormController#referenceData(javax.servlet.http.HttpServletRequest)
	 */
	protected Map<String, Object> referenceData(HttpServletRequest request) throws Exception {

		Locale locale = Context.getLocale();
		Map<String, Object> map = new HashMap<String, Object>();
		String defaultVerbose = "false";

		ConceptService cs = Context.getConceptService();
		String conceptId = request.getParameter("conceptId");
		ConceptName conceptName = new ConceptName();
		Collection<ConceptName> conceptSynonyms = new Vector<ConceptName>();
		Map<String, ConceptName> conceptNamesByLocale = new HashMap<String, ConceptName>();
		Map<String, ConceptName> conceptShortNamesByLocale = new HashMap<String, ConceptName>();
		HashMap<String, ConceptDescription> conceptDescriptionsByLocale = new HashMap<String, ConceptDescription>();
		Map<Locale, Collection<ConceptName>> conceptSynonymsByLocale = new HashMap<Locale, Collection<ConceptName>>();
		Map<Double, Object[]> conceptSets = new TreeMap<Double, Object[]>();
		Map<String, String> conceptAnswers = new TreeMap<String, String>();
		Collection<Form> forms = new HashSet<Form>();
		Map<Integer, String> questionsAnswered = new TreeMap<Integer, String>();
		Map<Integer, String> containedInSets = new TreeMap<Integer, String>();

		boolean isNew = true;
		
		Collection<Locale> conceptLocales = cs.getLocalesOfConceptNames();
		
		if (conceptId != null) {
			Concept concept = null;
			try {
				concept = cs.getConcept(Integer.valueOf(conceptId));
			} catch (APIAuthenticationException ex) {
				// pass
			}

			if (concept != null) {
				isNew = false;
				// get conceptNames for all locales
				for (Locale l : conceptLocales) {
					ConceptName cn = concept.getName(l, true);
					if (cn == null) {
						cn = new ConceptName();
					}
					conceptNamesByLocale.put(l.toString(), cn);
				}

				// get conceptShortNames for all locales
				for (Locale l : conceptLocales) {
					ConceptName cn = concept.getBestShortName(l);
					if (cn == null) {
						cn = new ConceptName();
					}
					conceptShortNamesByLocale.put(l.toString(), cn);
				}

				// get conceptDescriptions for all locales
				for (Locale l : conceptLocales) {
					ConceptDescription cd = concept.getDescription(l, true);
					if (cd == null) {
						cd = new ConceptDescription();
					}
					conceptDescriptionsByLocale.put(l.toString(), cd);
				}
				// get concept names for all locales
				for (Locale l : conceptLocales) {
					conceptSynonymsByLocale.put(l, concept.getNames(l));
				}

				// get locale specific preferred conceptName object
				conceptName = concept.getName(locale);
				if (conceptName == null)
					conceptName = new ConceptName();

				// get locale specific names
				conceptSynonyms = concept.getNames(locale);

				// get concept sets with locale decoded names
				for (ConceptSet set : concept.getConceptSets()) {
					Object[] arr = {
					        set.getConcept().getConceptId().toString(),
					        set.getConcept().getName(locale) };
					conceptSets.put(set.getSortWeight(), arr);
				}

				// get concept answers with locale decoded names
				for (ConceptAnswer answer : concept.getAnswers(true)) {
					log.debug("getting answers");
					String key = answer.getAnswerConcept().getConceptId()
					        .toString();
					ConceptName cn = answer.getAnswerConcept().getName(locale);
					String name = "";
					if (cn != null)
						name = cn.toString();
					if (answer.getAnswerDrug() != null) {
						// if this answer is a drug, append the drug id
						// information
						key = key + "^" + answer.getAnswerDrug().getDrugId();
						name = answer.getAnswerDrug().getFullName(locale);
					}
					if (answer.getAnswerConcept().isRetired())
						name = "<span class='retired'>" + name + "</span>";
					conceptAnswers.put(key, name);
				}

				forms = Context.getFormService().getForms(concept);

				for (Concept c : Context.getConceptService()
				        .getQuestionsForAnswer(concept)) {
					ConceptName cn = c.getName(locale);
					if (cn == null)
						questionsAnswered.put(c.getConceptId(),
						        "No Name Defined");
					else
						questionsAnswered.put(c.getConceptId(), cn.getName());
				}

				for (ConceptSet set : Context.getConceptService()
				        .getSetsContainingConcept(concept)) {
					Concept c = set.getConceptSet();
					ConceptName cn = c.getName(locale);
					if (cn == null)
						containedInSets
						        .put(c.getConceptId(), "No Name Defined");
					else
						containedInSets.put(c.getConceptId(), cn.getName());
				}
			}

			if (Context.isAuthenticated())
				defaultVerbose = Context.getAuthenticatedUser()
				        .getUserProperty(
				                OpenmrsConstants.USER_PROPERTY_SHOW_VERBOSE);
		}

		if (isNew) {
			for (Locale l : conceptLocales) {
				conceptNamesByLocale.put(l.toString(), new ConceptName());
				conceptShortNamesByLocale.put(l.toString(), new ConceptName());
				conceptDescriptionsByLocale.put(l.toString(), new ConceptDescription());
			}

			// get conceptSynonyms for all locales
			for (Locale l : conceptLocales) {
				conceptSynonymsByLocale.put(l, new HashSet<ConceptName>());
			}
		}
		
		map.put("locales", conceptLocales);
		map.put("conceptName", conceptName);
		for (Map.Entry<String, ConceptName> e : conceptNamesByLocale.entrySet()) {
			map.put("conceptName_" + e.getKey(), e.getValue());
		}
		for (Map.Entry<String, ConceptName> e : conceptShortNamesByLocale
		        .entrySet()) {
			map.put("conceptShortName_" + e.getKey(), e.getValue());
		}
		for (Map.Entry<String, ConceptDescription> e : conceptDescriptionsByLocale
		        .entrySet()) {
			map.put("conceptDescription_" + e.getKey(), e.getValue());
		}
		map.put("conceptSynonyms", conceptSynonyms);
		map.put("conceptSynonymsByLocale", conceptSynonymsByLocale);
		map.put("conceptSets", conceptSets);
		map.put("conceptAnswers", conceptAnswers);
		map.put("formsInUse", forms);
		map.put("questionsAnswered", questionsAnswered);
		map.put("containedInSets", containedInSets);

    	//get complete class and datatype lists 
		map.put("classes", cs.getAllConceptClasses());
		map.put("datatypes", cs.getAllConceptDatatypes());
		
		// make spring locale available to jsp
		map.put("locale", locale.getLanguage().substring(0, 2));

		map.put("defaultVerbose", defaultVerbose.equals("true") ? true : false);

		return map;
	}
	
	/**
	 * Convenience method to get the ConceptNumeric specific values out of 
	 * the request and put them onto an object
	 * 
	 * @param concept
	 * @param request
	 * @return
	 */
	private ConceptNumeric getConceptNumeric(Concept concept, HttpServletRequest request) {
		
		ConceptNumeric cn = null;
		if (concept instanceof ConceptNumeric)
			cn = (ConceptNumeric)concept;
		else {
			cn = new ConceptNumeric(concept);
		}

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
