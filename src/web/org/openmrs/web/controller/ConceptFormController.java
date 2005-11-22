package org.openmrs.web.controller;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.ConceptName;
import org.openmrs.ConceptNumeric;
import org.openmrs.ConceptSynonym;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.ConceptService;
import org.openmrs.web.Constants;
import org.openmrs.web.propertyeditor.ConceptAnswersEditor;
import org.openmrs.web.propertyeditor.ConceptClassEditor;
import org.openmrs.web.propertyeditor.ConceptDatatypeEditor;
import org.openmrs.web.propertyeditor.ConceptSetsEditor;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.support.RequestContextUtils;
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
		Context context = (Context) request.getSession().getAttribute(Constants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		Concept concept = null;
		String conceptId = request.getParameter("conceptId");
		
        NumberFormat nf = NumberFormat.getInstance(new Locale("en_US"));
        Locale locale = RequestContextUtils.getLocale(request);
        binder.registerCustomEditor(java.lang.Integer.class,
                new CustomNumberEditor(java.lang.Integer.class, nf, true));
		//binder.registerCustomEditor(java.lang.Integer.class, 
		//		new CustomNumberEditor(java.lang.Integer.class, true));
        binder.registerCustomEditor(java.util.Date.class, 
        		new CustomDateEditor(DateFormat.getDateInstance(DateFormat.SHORT), true));
        binder.registerCustomEditor(org.openmrs.ConceptClass.class, 
        		new ConceptClassEditor(context));
        binder.registerCustomEditor(org.openmrs.ConceptDatatype.class, 
        		new ConceptDatatypeEditor(context));
        /*binder.registerCustomEditor(java.util.Collection.class, "synonyms", 
        		new ConceptSynonymsEditor(locale)); */
        binder.registerCustomEditor(java.util.Collection.class, "conceptSets", 
        		new ConceptSetsEditor(context));
        binder.registerCustomEditor(java.util.Collection.class, "answers", 
        		new ConceptAnswersEditor(context));

	}

	/**
	 * @see org.springframework.web.servlet.mvc.AbstractFormController#processFormSubmission(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.Object, org.springframework.validation.BindException)
	 */
	protected ModelAndView processFormSubmission(HttpServletRequest request, HttpServletResponse response, Object object, BindException errors) throws Exception {
	
		HttpSession httpSession = request.getSession();
		Context context = (Context) httpSession.getAttribute(Constants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		Concept concept = (Concept)object;
		Locale locale = RequestContextUtils.getLocale(request);
		
		if (context != null && context.isAuthenticated()) {
			ConceptService cs = context.getConceptService();

			// ==== Concept Synonyms ====
				// the attribute *must* be named differently than the property, otherwise
				//   spring will modify the property as text array
				String[] tempSyns = request.getParameter("syns").split("\n");
				Collection<ConceptSynonym> originalSyns = concept.getSynonyms();
				Set<ConceptSynonym> parameterSyns = new HashSet<ConceptSynonym>();
				Set<ConceptSynonym> newSyns = new HashSet<ConceptSynonym>();
				//set up parameter Synonym Set for easier add/delete functions
				// and removal of duplicates
				for (String syn : tempSyns) {
					syn = syn.trim();
					if (!syn.equals(""))
						parameterSyns.add(new ConceptSynonym(concept, syn, locale));
				}
				
				// Union the originalSyns and parameterSyns to get the 'clean' synonyms
				//   remove synonym from parameterSynonym if 'clean'
				for (ConceptSynonym c : originalSyns) {
					if (parameterSyns.contains(c)) {  //.contains only possible because we overrode .equals
						newSyns.add(c);
						parameterSyns.remove(c);
					}
				}
				
				//add all remaining parameter synonyms
				for (ConceptSynonym syn : parameterSyns) {
						newSyns.add(syn);
				}
				
				concept.setSynonyms(newSyns);
				
				//zero out conceptSets
				String conceptSets = request.getParameter("conceptSets");
				if (conceptSets == null)
					concept.setConceptSets(null); 
				
				//set concept_name properties to the correct/current locale
				String conceptName = request.getParameter("name");
				String shortName = request.getParameter("shortName");
				//String description = request.getParameter("description");
				ConceptName cn = concept.getName(locale);
				if (cn != null) {
					cn.setName(conceptName);
					cn.setShortName(shortName);
					//cn.setDescription(description);
				}
				else {
					//TODO add description
					concept.addName(new ConceptName(conceptName, shortName, locale));
				}
						
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
		Context context = (Context) httpSession.getAttribute(Constants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		Concept concept = (Concept)obj;
				
		if (context != null && context.isAuthenticated()) {
			
			
			boolean isNew = (concept.getConceptId() == null);
			
			context.getConceptService().updateConcept(concept);
			
			String view = getSuccessView();
						
			httpSession.setAttribute(Constants.OPENMRS_MSG_ATTR, "Concept.saved");
			return new ModelAndView(new RedirectView(getSuccessView() + "?conceptId=" + concept.getConceptId().toString()));
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

		HttpSession httpSession = request.getSession();
		Context context = (Context) httpSession.getAttribute(Constants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		
		Concept concept = null;
		
		if (context != null && context.isAuthenticated()) {
			ConceptService cs = context.getConceptService();
			String conceptId = request.getParameter("conceptId");
	    	if (conceptId != null)
	    		concept = cs.getConcept(Integer.valueOf(conceptId));
		}
		
		if (concept == null)
			concept = new Concept();
		
		if (concept.isNumeric() && concept.getConceptNumeric() == null)
			concept.setConceptNumeric(new ConceptNumeric());
		
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
		
		HttpSession httpSession = request.getSession();
		Context context = (Context) httpSession.getAttribute(Constants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		
		Locale locale = RequestContextUtils.getLocale(request);
		Map<String, Object> map = new HashMap<String, Object>();
		
		if (context != null && context.isAuthenticated()) {
			ConceptService cs = context.getConceptService();
			
			// get conceptName object
			String conceptId = request.getParameter("conceptId");
			ConceptName cn = null;
	    	if (conceptId != null) {
	    		Concept concept = cs.getConcept(Integer.valueOf(conceptId));
	    		cn = concept.getName(locale);
	    		if (cn == null)
	    			cn = concept.getName(new Locale("en_US"));
	    	}
	    	if (cn == null)
	    		cn = new ConceptName();
	    	map.put("conceptName", cn);
	    	//get class and datatype lists 
			map.put("classes", cs.getConceptClasses());
			map.put("datatypes", cs.getConceptDatatypes());
		}
		
		return map;
	} 
}