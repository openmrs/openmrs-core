package org.openmrs.web.controller.concept;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.ConceptProposal;
import org.openmrs.ConceptWord;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.web.Util;
import org.openmrs.web.WebConstants;
import org.openmrs.web.dwr.ConceptListItem;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

public class ConceptProposalFormController extends SimpleFormController {
	
    /** Logger for this class and subclasses */
    protected final Log log = LogFactory.getLog(getClass());

    /**
	 * @see org.springframework.web.servlet.mvc.SimpleFormController#processFormSubmission(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.Object, org.springframework.validation.BindException)
	 */
	@Override
	protected ModelAndView processFormSubmission(HttpServletRequest request, HttpServletResponse response, Object obj, BindException errors) throws Exception {
		
		HttpSession httpSession = request.getSession();
		Context context = (Context) httpSession.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		ConceptProposal cp = (ConceptProposal)obj;
		String action = request.getParameter("action");
		
		if (context != null) {
			Concept c = null;
			if (StringUtils.hasText(request.getParameter("conceptId")))
				c = context.getConceptService().getConcept(Integer.valueOf(request.getParameter("conceptId")));
			cp.setMappedConcept(c);
		}
		
		MessageSourceAccessor msa = getMessageSourceAccessor();
		if (action.equals(msa.getMessage("ConceptProposal.update"))) {
			if (cp.getState().equals(OpenmrsConstants.CONCEPT_PROPOSAL_CONCEPT) || cp.getState().equals(OpenmrsConstants.CONCEPT_PROPOSAL_SYNONYM)) {
				errors.rejectValue("state", "ConceptProposal.state.error");
			}
		}
		else {
			
			if (cp.getMappedConcept() == null)
				errors.rejectValue("mappedConcept", "ConceptProposal.mappedConcept.error");
			else {
				if (action.equals(msa.getMessage("ConceptProposal.saveAsConcept")) ) {
					if (cp.getState().equals(OpenmrsConstants.CONCEPT_PROPOSAL_REJECT)) {
						errors.rejectValue("state", "ConceptProposal.state.error");
					}
					cp.setState(OpenmrsConstants.CONCEPT_PROPOSAL_CONCEPT);
				}
				if (action.equals(msa.getMessage("ConceptProposal.saveAsSynonym"))) {
					if (cp.getState().equals(OpenmrsConstants.CONCEPT_PROPOSAL_REJECT)) {
						errors.rejectValue("state", "ConceptProposal.state.error");
					}
					if (cp.getMappedConcept() == null)
						errors.rejectValue("mappedConcept", "ConceptProposal.mappedConcept.error");
					if (!StringUtils.hasText(cp.getFinalText()))
						errors.rejectValue("finalText", "error.null");
					cp.setState(OpenmrsConstants.CONCEPT_PROPOSAL_SYNONYM);
				}
			}
		}
		return super.processFormSubmission(request, response, cp, errors);
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
		Context context = (Context) httpSession.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		String view = getFormView();
		
		if (context != null && context.isAuthenticated()) {
			ConceptProposal cp = (ConceptProposal)obj;
			Concept c = null;
			if (StringUtils.hasText(request.getParameter("conceptId")))
				c = context.getConceptService().getConcept(Integer.valueOf(request.getParameter("conceptId")));
			
			context.getAdministrationService().mapConceptProposalToConcept(cp, c);
			view = getSuccessView();
			httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "ConceptProposal.saved");
		}
		
		return new ModelAndView(new RedirectView(view));
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
		Context context = (Context) httpSession.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		
		ConceptProposal cp = null;
		
		if (context != null && context.isAuthenticated()) {
			ConceptService cs = context.getConceptService();
			String id = request.getParameter("conceptProposalId");
	    	if (id != null)
	    		cp = cs.getConceptProposal(Integer.valueOf(id));	
		}
		
		if (cp == null)
			cp = new ConceptProposal();
    	
        return cp;
    }
    
	protected Map referenceData(HttpServletRequest request, Object object, Errors errors) throws Exception {
		HttpSession httpSession = request.getSession();
		Context context = (Context) httpSession.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		
		Map<String, Object> map = new HashMap<String, Object>();
		
		ConceptProposal cp = (ConceptProposal)object;
		Locale locale = Util.getLocale(request);
		List<ConceptListItem> possibleConceptsListItems = new Vector<ConceptListItem>();
		ConceptListItem listItem = null;
		
		if (cp.getObsConcept() != null)
			listItem = new ConceptListItem(cp.getObsConcept(), locale);
		map.put("obsConcept", listItem);
		
		String defaultVerbose = "false";
		if (context != null && context.isAuthenticated()){
			defaultVerbose = context.getAuthenticatedUser().getProperty(OpenmrsConstants.USER_PROPERTY_SHOW_VERBOSE);
			String phrase = cp.getOriginalText();
			if (phrase.length() > 3)
				phrase = phrase.substring(0, 3);
			List<ConceptWord> possibleConcepts = context.getConceptService().findConcepts(phrase, locale, false);
			if (possibleConcepts != null)
				for (ConceptWord word : possibleConcepts)
					possibleConceptsListItems.add(new ConceptListItem(word));
		}
		map.put("possibleConcepts", possibleConceptsListItems);
		map.put("defaultVerbose", defaultVerbose.equals("true") ? true : false);
		map.put("states", OpenmrsConstants.CONCEPT_PROPOSAL_STATES());
		
		return map;
	}
    
}