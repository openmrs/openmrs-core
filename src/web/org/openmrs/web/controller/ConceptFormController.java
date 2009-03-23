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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.collections.FactoryUtils;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.ConceptAnswer;
import org.openmrs.ConceptComplex;
import org.openmrs.ConceptDescription;
import org.openmrs.ConceptMap;
import org.openmrs.ConceptName;
import org.openmrs.ConceptNameTag;
import org.openmrs.ConceptNumeric;
import org.openmrs.ConceptSet;
import org.openmrs.Form;
import org.openmrs.api.APIException;
import org.openmrs.api.ConceptService;
import org.openmrs.api.ConceptsLockedException;
import org.openmrs.api.context.Context;
import org.openmrs.propertyeditor.ConceptAnswersEditor;
import org.openmrs.propertyeditor.ConceptClassEditor;
import org.openmrs.propertyeditor.ConceptDatatypeEditor;
import org.openmrs.propertyeditor.ConceptSetsEditor;
import org.openmrs.propertyeditor.ConceptSourceEditor;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.web.WebConstants;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

/**
 * This is the controlling class for hte conceptForm.jsp page. It initBinder and formBackingObject
 * are called before page load. After submission, formBackingObject (because we're not a session
 * form), processFormSubmission, and onSubmit methods are called
 * 
 * @see org.openmrs.Concept
 */
public class ConceptFormController extends SimpleFormController {
	
	/** Logger for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());
	
	/**
	 * Allows for other Objects to be used as values in input tags. Normally, only strings and lists
	 * are expected
	 * 
	 * @see org.springframework.web.servlet.mvc.BaseCommandController#initBinder(javax.servlet.http.HttpServletRequest,
	 *      org.springframework.web.bind.ServletRequestDataBinder)
	 */
	protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
		super.initBinder(request, binder);
		
		NumberFormat nf = NumberFormat.getInstance(Context.getLocale());
		binder.registerCustomEditor(java.lang.Integer.class, new CustomNumberEditor(java.lang.Integer.class, nf, true));
		binder.registerCustomEditor(java.lang.Double.class, new CustomNumberEditor(java.lang.Double.class, nf, true));
		binder.registerCustomEditor(java.util.Date.class, new CustomDateEditor(SimpleDateFormat.getDateInstance(
		    SimpleDateFormat.SHORT, Context.getLocale()), true));
		binder.registerCustomEditor(org.openmrs.ConceptClass.class, new ConceptClassEditor());
		binder.registerCustomEditor(org.openmrs.ConceptDatatype.class, new ConceptDatatypeEditor());
		binder.registerCustomEditor(java.util.Collection.class, "concept.conceptSets", new ConceptSetsEditor());
		binder.registerCustomEditor(java.util.Collection.class, "concept.answers", new ConceptAnswersEditor());
		binder.registerCustomEditor(org.openmrs.ConceptSource.class, new ConceptSourceEditor());
	}
	
	/**
	 * @see org.springframework.web.servlet.mvc.AbstractFormController#processFormSubmission(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse, java.lang.Object,
	 *      org.springframework.validation.BindException)
	 */
	protected ModelAndView processFormSubmission(HttpServletRequest request, HttpServletResponse response, Object object,
	                                             BindException errors) throws Exception {
		
		Concept concept = ((ConceptFormBackingObject) object).getConcept();
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
		
		return super.processFormSubmission(request, response, object, errors);
	}
	
	/**
	 * The onSubmit function receives the form/command object that was modified by the input form
	 * and saves it to the db
	 * 
	 * @see org.springframework.web.servlet.mvc.SimpleFormController#onSubmit(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse, java.lang.Object,
	 *      org.springframework.validation.BindException)
	 * @should display numeric values from table
	 * @should copy numeric values into numeric concepts
	 */
	protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object obj,
	                                BindException errors) throws Exception {
		
		HttpSession httpSession = request.getSession();
		ConceptService cs = Context.getConceptService();
		if (Context.isAuthenticated()) {
			
			ConceptFormBackingObject conceptBackingObject = (ConceptFormBackingObject) obj;
			
			MessageSourceAccessor msa = getMessageSourceAccessor();
			String action = request.getParameter("action");
			
			if (action.equals(msa.getMessage("Concept.delete", "Delete Concept"))) {
				Concept concept = conceptBackingObject.getConcept();
				try {
					cs.purgeConcept(concept);
					httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "Concept.deleted");
					return new ModelAndView(new RedirectView("index.htm"));
				}
				catch (ConceptsLockedException cle) {
					log.error("Tried to delete concept while concepts were locked", cle);
					httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "Concept.concepts.locked");
				}
				catch (DataIntegrityViolationException e) {
					log.error("Unable to delete a concept because it is in use: " + concept, e);
					httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "Concept.cannot.delete");
				}
				catch (Exception e) {
					log.error("Unable to delete concept because an error occurred: " + concept, e);
					httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "Concept.cannot.delete");
				}
				// return to the edit screen because an error was thrown
				return new ModelAndView(new RedirectView(getSuccessView() + "?conceptId=" + concept.getConceptId()));
			} else {
				Concept concept = conceptBackingObject.getConceptFromFormData();
				
				try {
					cs.saveConcept(concept);
					httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "Concept.saved");
					return new ModelAndView(new RedirectView(getSuccessView() + "?conceptId=" + concept.getConceptId()));
				}
				catch (ConceptsLockedException cle) {
					log.error("Tried to save concept while concepts were locked", cle);
					httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "Concept.concepts.locked");
					errors.reject("concept", "Concept.concepts.locked");
				}
				catch (APIException e) {
					log.error("Error while trying to save concept", e);
					httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "Concept.cannot.save");
					errors.reject("concept", "Concept.cannot.save");
				}
				// return to the edit form because an error was thrown
				return showForm(request, response, errors);
			}
		}
		
		return new ModelAndView(new RedirectView(getFormView()));
	}
	
	/**
	 * This is called prior to displaying a form for the first time. It tells Spring the
	 * form/command object to load into the request
	 * 
	 * @see org.springframework.web.servlet.mvc.AbstractFormController#formBackingObject(javax.servlet.http.HttpServletRequest)
	 */
	protected ConceptFormBackingObject formBackingObject(HttpServletRequest request) throws ServletException {
		
		String conceptId = request.getParameter("conceptId");
		if (conceptId == null) {
			return new ConceptFormBackingObject(new Concept());
		} else {
			ConceptService cs = Context.getConceptService();
			Concept concept = cs.getConcept(Integer.valueOf(conceptId));
			return new ConceptFormBackingObject(concept);
		}
		
	}
	
	/**
	 * Called prior to form display. Allows for data to be put in the request to be used in the view
	 * 
	 * @see org.springframework.web.servlet.mvc.SimpleFormController#referenceData(javax.servlet.http.HttpServletRequest)
	 */
	protected Map<String, Object> referenceData(HttpServletRequest request) throws Exception {
		
		Map<String, Object> map = new HashMap<String, Object>();
		
		ConceptService cs = Context.getConceptService();
		
		String defaultVerbose = "false";
		if (Context.isAuthenticated())
			defaultVerbose = Context.getAuthenticatedUser().getUserProperty(OpenmrsConstants.USER_PROPERTY_SHOW_VERBOSE);
		map.put("defaultVerbose", defaultVerbose.equals("true") ? true : false);
		
		map.put("tags", cs.getAllConceptNameTags());
		
		//get complete class and datatype lists 
		map.put("classes", cs.getAllConceptClasses());
		map.put("datatypes", cs.getAllConceptDatatypes());
		
		//get complex handlers
		map.put("handlers", Context.getObsService().getHandlers());
		
		// make spring locale available to jsp
		map.put("locale", Context.getLocale()); // should be same string format as conceptNamesByLocale map keys
		
		return map;
	}
	
	/**
	 * Class that represents all data on this form
	 */
	public class ConceptFormBackingObject {
		
		public Concept concept = null;
		
		public List<Locale> locales = null;
		
		public Map<Locale, ConceptName> namesByLocale = new HashMap<Locale, ConceptName>();
		
		public Map<Locale, ConceptName> shortNamesByLocale = new HashMap<Locale, ConceptName>();
		
		public Map<Locale, List<ConceptName>> synonymsByLocale = new HashMap<Locale, List<ConceptName>>();
		
		public Map<Locale, ConceptDescription> descriptionsByLocale = new HashMap<Locale, ConceptDescription>();
		
		public List<ConceptMap> mappings; // a "lazy list" version of the concept.getMappings() list
		
		public Double hiAbsolute;
		
		public Double lowAbsolute;
		
		public Double lowCritical;
		
		public Double hiCritical;
		
		public Double lowNormal;
		
		public Double hiNormal;
		
		public boolean precise = false;
		
		public String units;
		
		public String handlerKey;
		
		/**
		 * Default constructor must take in a Concept object to create itself
		 * 
		 * @param concept The concept for this page
		 */
		@SuppressWarnings("unchecked")
		public ConceptFormBackingObject(Concept concept) {
			this.concept = concept;
			this.locales = Context.getAdministrationService().getAllowedLocales();
			for (Locale locale : locales) {
				namesByLocale.put(locale, concept.getPreferredName(locale));
				shortNamesByLocale.put(locale, concept.getShortNameInLocale(locale));
				synonymsByLocale.put(locale, (List<ConceptName>) concept.getSynonyms(locale));
				descriptionsByLocale.put(locale, concept.getDescription(locale, true));
				
				// put in default values so the binding doesn't fail
				if (namesByLocale.get(locale) == null)
					namesByLocale.put(locale, new ConceptName(null, locale));
				if (shortNamesByLocale.get(locale) == null)
					shortNamesByLocale.put(locale, new ConceptName(null, locale));
				if (descriptionsByLocale.get(locale) == null)
					descriptionsByLocale.put(locale, new ConceptDescription(null, locale));
				
				synonymsByLocale.put(locale, ListUtils.lazyList(synonymsByLocale.get(locale), FactoryUtils
				        .instantiateFactory(ConceptName.class)));
				
			}
			
			// turn the list objects into lazy lists
			mappings = ListUtils.lazyList(new Vector(concept.getConceptMappings()), FactoryUtils
			        .instantiateFactory(ConceptMap.class));
			
			if (concept.isNumeric()) {
				ConceptNumeric cn = (ConceptNumeric) concept;
				this.hiAbsolute = cn.getHiAbsolute();
				this.lowAbsolute = cn.getLowAbsolute();
				this.lowCritical = cn.getLowCritical();
				this.hiCritical = cn.getHiCritical();
				this.lowNormal = cn.getLowNormal();
				this.hiNormal = cn.getHiNormal();
				this.precise = cn.getPrecise();
				this.units = cn.getUnits();
			} else if (concept.isComplex()) {
				ConceptComplex complex = (ConceptComplex) concept;
				this.handlerKey = complex.getHandler();
			}
		}
		
		/**
		 * This method takes all the form data from the input boxes and puts it onto the concept
		 * object so that it can be saved to the database
		 * 
		 * @return the concept to be saved to the database
		 */
		public Concept getConceptFromFormData() {
			ConceptNameTag preferredTag = Context.getConceptService().getConceptNameTagByName(ConceptNameTag.PREFERRED);
			ConceptNameTag shortTag = Context.getConceptService().getConceptNameTagByName(ConceptNameTag.SHORT);
			ConceptNameTag synonymTag = Context.getConceptService().getConceptNameTagByName(ConceptNameTag.SYNONYM);
			
			// add all the new names/descriptions to the concept
			for (Locale locale : locales) {
				ConceptName preferredNameInLocale = namesByLocale.get(locale);
				if (StringUtils.hasLength(preferredNameInLocale.getName())
				        && !concept.getNames().contains(preferredNameInLocale)) {
					preferredNameInLocale.addTag(preferredTag);
					concept.addName(preferredNameInLocale);
				}
				ConceptName shortNameInLocale = shortNamesByLocale.get(locale);
				if (StringUtils.hasLength(shortNameInLocale.getName()) && !concept.getNames().contains(shortNameInLocale)
				        && !concept.hasName(shortNameInLocale.getName(), locale)) {
					shortNameInLocale.addTag(shortTag);
					concept.addName(shortNameInLocale);
				}
				for (ConceptName synonym : synonymsByLocale.get(locale)) {
					if (synonym != null && synonym.getName() != null && !concept.getNames().contains(synonym)
					        && !concept.hasName(synonym.getName(), locale)) {
						synonym.addTag(synonymTag);
						synonym.setLocale(locale);
						concept.addName(synonym);
					}
				}
				ConceptDescription descInLocale = descriptionsByLocale.get(locale);
				if (StringUtils.hasLength(descInLocale.getDescription())
				        && !concept.getDescriptions().contains(descInLocale)) {
					concept.addDescription(descInLocale);
				}
			}
			
			// add in all the mappings
			for (ConceptMap map : mappings) {
				if (map != null) {
					if (map.getSourceCode() == null) {
						// because of the _mappings[x].sourceCode input name in the jsp, the sourceCode will be empty for 
						// deleted mappings.  remove those from the concept object now.
						concept.removeConceptMapping(map);
					} else if (!concept.getConceptMappings().contains(map)) {
						// assumes null sources also don't get here
						concept.addConceptMapping(map);
					}
				}
			}
			
			// if the user unchecked the concept sets box, erase past saved sets
			if (!concept.isSet())
				concept.setConceptSets(null);
			
			// add in subobject specific code
			if (concept.getDatatype().getName().equals("Numeric")) {
				ConceptNumeric cn;
				if (concept instanceof ConceptNumeric)
					cn = (ConceptNumeric) concept;
				else {
					cn = new ConceptNumeric(concept);
				}
				cn.setHiAbsolute(hiAbsolute);
				cn.setLowAbsolute(lowAbsolute);
				cn.setHiCritical(hiCritical);
				cn.setLowCritical(lowCritical);
				cn.setHiNormal(hiNormal);
				cn.setLowNormal(lowNormal);
				cn.setPrecise(precise);
				cn.setUnits(units);
				
				concept = cn;
				
			} else if (concept.getDatatype().getName().equals("Complex")) {
				ConceptComplex complexConcept;
				if (concept instanceof ConceptNumeric)
					complexConcept = (ConceptComplex) concept;
				else {
					complexConcept = new ConceptComplex(concept);
				}
				complexConcept.setHandler(handlerKey);
				concept = complexConcept;
			}
			
			return concept;
		}
		
		/**
		 * @return the concept
		 */
		public Concept getConcept() {
			return concept;
		}
		
		/**
		 * @param concept the concept to set
		 */
		public void setConcept(Concept concept) {
			this.concept = concept;
		}
		
		/**
		 * @return the locales
		 */
		public List<Locale> getLocales() {
			return locales;
		}
		
		/**
		 * @param locales the locales to set
		 */
		public void setLocales(List<Locale> locales) {
			this.locales = locales;
		}
		
		/**
		 * @return the namesByLocale
		 */
		public Map<Locale, ConceptName> getNamesByLocale() {
			return namesByLocale;
		}
		
		/**
		 * @param namesByLocale the namesByLocale to set
		 */
		public void setNamesByLocale(Map<Locale, ConceptName> namesByLocale) {
			this.namesByLocale = namesByLocale;
		}
		
		/**
		 * @return the shortNamesByLocale
		 */
		public Map<Locale, ConceptName> getShortNamesByLocale() {
			return shortNamesByLocale;
		}
		
		/**
		 * @param shortNamesByLocale the shortNamesByLocale to set
		 */
		public void setShortNamesByLocale(Map<Locale, ConceptName> shortNamesByLocale) {
			this.shortNamesByLocale = shortNamesByLocale;
		}
		
		/**
		 * @return the descriptionsByLocale
		 */
		public Map<Locale, ConceptDescription> getDescriptionsByLocale() {
			return descriptionsByLocale;
		}
		
		/**
		 * @param descriptionsByLocale the descriptionsByLocale to set
		 */
		public void setDescriptionsByLocale(Map<Locale, ConceptDescription> descriptionsByLocale) {
			this.descriptionsByLocale = descriptionsByLocale;
		}
		
		/**
		 * @return the mappings
		 */
		public List<ConceptMap> getMappings() {
			return mappings;
		}
		
		/**
		 * @param mappings the mappings to set
		 */
		public void setMappings(List<ConceptMap> mappings) {
			this.mappings = mappings;
		}
		
		/**
		 * @return the synonymsByLocale
		 */
		public Map<Locale, List<ConceptName>> getSynonymsByLocale() {
			return synonymsByLocale;
		}
		
		/**
		 * @param synonymsByLocale the synonymsByLocale to set
		 */
		public void setSynonymsByLocale(Map<Locale, List<ConceptName>> synonymsByLocale) {
			this.synonymsByLocale = synonymsByLocale;
		}
		
		/**
		 * @return the hiAbsolute
		 */
		public Double getHiAbsolute() {
			return hiAbsolute;
		}
		
		/**
		 * @param hiAbsolute the hiAbsolute to set
		 */
		public void setHiAbsolute(Double hiAbsolute) {
			this.hiAbsolute = hiAbsolute;
		}
		
		/**
		 * @return the lowAbsolute
		 */
		public Double getLowAbsolute() {
			return lowAbsolute;
		}
		
		/**
		 * @param lowAbsolute the lowAbsolute to set
		 */
		public void setLowAbsolute(Double lowAbsolute) {
			this.lowAbsolute = lowAbsolute;
		}
		
		/**
		 * @return the lowCritical
		 */
		public Double getLowCritical() {
			return lowCritical;
		}
		
		/**
		 * @param lowCritical the lowCritical to set
		 */
		public void setLowCritical(Double lowCritical) {
			this.lowCritical = lowCritical;
		}
		
		/**
		 * @return the hiCritical
		 */
		public Double getHiCritical() {
			return hiCritical;
		}
		
		/**
		 * @param hiCritical the hiCritical to set
		 */
		public void setHiCritical(Double hiCritical) {
			this.hiCritical = hiCritical;
		}
		
		/**
		 * @return the lowNormal
		 */
		public Double getLowNormal() {
			return lowNormal;
		}
		
		/**
		 * @param lowNormal the lowNormal to set
		 */
		public void setLowNormal(Double lowNormal) {
			this.lowNormal = lowNormal;
		}
		
		/**
		 * @return the hiNormal
		 */
		public Double getHiNormal() {
			return hiNormal;
		}
		
		/**
		 * @param hiNormal the hiNormal to set
		 */
		public void setHiNormal(Double hiNormal) {
			this.hiNormal = hiNormal;
		}
		
		/**
		 * @return the precise
		 */
		public boolean isPrecise() {
			return precise;
		}
		
		/**
		 * @param precise the precise to set
		 */
		public void setPrecise(boolean precise) {
			this.precise = precise;
		}
		
		/**
		 * @return the units
		 */
		public String getUnits() {
			return units;
		}
		
		/**
		 * @param units the units to set
		 */
		public void setUnits(String units) {
			this.units = units;
		}
		
		/**
		 * @return the handlerKey
		 */
		public String getHandlerKey() {
			return handlerKey;
		}
		
		/**
		 * @param handlerKey the handlerKey to set
		 */
		public void setHandlerKey(String handlerKey) {
			this.handlerKey = handlerKey;
		}
		
		/**
		 * Get the forms that this concept is declared to be used in
		 * 
		 * @return
		 */
		public List<Form> getFormsInUse() {
			return Context.getFormService().getFormsContainingConcept(concept);
		}
		
		/**
		 * Get the other concept questions that this concept is declared as an answer for
		 * 
		 * @return
		 */
		public List<Concept> getQuestionsAnswered() {
			return Context.getConceptService().getConceptsByAnswer(concept);
		}
		
		/**
		 * Get the sets that this concept is declared to be a child member of
		 * 
		 * @return
		 */
		public List<ConceptSet> getContainedInSets() {
			return Context.getConceptService().getSetsContainingConcept(concept);
		}
		
		/**
		 * Get the answers for this concept with decoded names. The keys to this map are the
		 * conceptIds or the conceptIds^drugId if applicable
		 * 
		 * @return
		 */
		public Map<String, String> getConceptAnswers() {
			Map<String, String> conceptAnswers = new TreeMap<String, String>();
			// get concept answers with locale decoded names
			for (ConceptAnswer answer : concept.getAnswers(true)) {
				log.debug("getting answers");
				String key = answer.getAnswerConcept().getConceptId().toString();
				ConceptName cn = answer.getAnswerConcept().getName(Context.getLocale());
				String name = "";
				if (cn != null)
					name = cn.toString();
				if (answer.getAnswerDrug() != null) {
					// if this answer is a drug, append the drug id information
					key = key + "^" + answer.getAnswerDrug().getDrugId();
					name = answer.getAnswerDrug().getFullName(Context.getLocale());
				}
				if (answer.getAnswerConcept().isRetired())
					name = "<span class='retired'>" + name + "</span>";
				conceptAnswers.put(key, name);
			}
			
			return conceptAnswers;
		}
	}
	
}
