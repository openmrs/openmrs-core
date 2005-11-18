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
import org.openmrs.ConceptSynonym;
import org.openmrs.api.ConceptService;
import org.openmrs.context.Context;
import org.openmrs.web.Constants;
import org.openmrs.web.propertyeditor.ConceptClassEditor;
import org.openmrs.web.propertyeditor.ConceptDatatypeEditor;
import org.openmrs.web.propertyeditor.ConceptSynonymsEditor;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.validation.BindException;
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
		Context context = (Context) request.getSession().getAttribute(Constants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
        NumberFormat nf = NumberFormat.getInstance(new Locale("en_US"));
        Locale locale = request.getLocale();
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
	}

	/**
	 * @see org.springframework.web.servlet.mvc.AbstractFormController#processFormSubmission(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.Object, org.springframework.validation.BindException)
	 */
	protected ModelAndView processFormSubmission(HttpServletRequest request, HttpServletResponse response, Object object, BindException errors) throws Exception {
	
		HttpSession httpSession = request.getSession();
		Context context = (Context) httpSession.getAttribute(Constants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		Concept concept = (Concept)object;
		Locale locale = request.getLocale();
		
		if (context != null && context.isAuthenticated()) {
			ConceptService cs = context.getConceptService();

			
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
			return new ModelAndView(new RedirectView(view));
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
		
		Map<String, Object> map = new HashMap<String, Object>();
		
		if (context != null && context.isAuthenticated()) {
			ConceptService cs = context.getConceptService();
			
			// get conceptName object
			String conceptId = request.getParameter("conceptId");
	    	if (conceptId != null) {
	    		Concept concept = cs.getConcept(Integer.valueOf(conceptId));
	    		map.put("conceptName", concept.getName(request.getLocale()));
	    	}
	    	else
	    		map.put("conceptName", new ConceptName());
	    	//get class and datatype lists 
			map.put("classes", cs.getConceptClasses());
			map.put("datatypes", cs.getConceptDatatypes());
		}
		
		return map;
	} 
}