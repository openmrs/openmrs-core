package org.openmrs.web.controller.concept;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.ConceptProposal;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.web.WebConstants;
import org.springframework.web.servlet.mvc.SimpleFormController;

public class ConceptProposalListController extends SimpleFormController {
	
    /** Logger for this class and subclasses */
    protected final Log log = LogFactory.getLog(getClass());

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
		
		//default empty Object
		List<ConceptProposal> cdList = new Vector<ConceptProposal>();
		
		//only fill the Object is the user has authenticated properly
		if (context != null && context.isAuthenticated()) {
			ConceptService cs = context.getConceptService();
			log.debug("tmp value: " + request.getParameter("includeCompleted"));
			boolean b = new Boolean(request.getParameter("includeCompleted"));
			log.debug("b value: " + b);
	    	cdList = cs.getConceptProposals(b);
		}
    	
        return cdList;
    }

	protected Map referenceData(HttpServletRequest request) throws Exception {
		
		Map<String, Object> map = new HashMap<String, Object>();
		
		map.put("unmapped", OpenmrsConstants.CONCEPT_PROPOSAL_UNMAPPED);
		map.put("states", OpenmrsConstants.CONCEPT_PROPOSAL_STATES());
		
		return map;
	}
    
    
    
}