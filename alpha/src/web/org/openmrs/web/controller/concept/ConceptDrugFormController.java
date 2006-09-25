package org.openmrs.web.controller.concept;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Drug;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.web.WebConstants;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

public class ConceptDrugFormController extends SimpleFormController {
	
    /** Logger for this class and subclasses */
    protected final Log log = LogFactory.getLog(getClass());
    
    SimpleDateFormat dateFormat;
    
	/**
	 * 
	 * Allows for Integers to be used as values in input tags.
	 *   Normally, only strings and lists are expected 
	 * 
	 * @see org.springframework.web.servlet.mvc.BaseCommandController#initBinder(javax.servlet.http.HttpServletRequest, org.springframework.web.bind.ServletRequestDataBinder)
	 */
	protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
		super.initBinder(request, binder);
		Context context = (Context) request.getSession().getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		
		dateFormat = new SimpleDateFormat(OpenmrsConstants.OPENMRS_LOCALE_DATE_PATTERNS().get(context.getLocale().toString().toLowerCase()), context.getLocale());
		
        //NumberFormat nf = NumberFormat.getInstance(new Locale("en_US"));
        binder.registerCustomEditor(java.lang.Integer.class,
                new CustomNumberEditor(java.lang.Integer.class, true));
        binder.registerCustomEditor(java.lang.Double.class,
                new CustomNumberEditor(java.lang.Double.class, true));
        
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
			Drug drug = (Drug)obj;
			drug.setConcept(context.getConceptService().getConcept(Integer.valueOf(request.getParameter("conceptId"))));
			context.getConceptService().updateDrug(drug);
			view = getSuccessView();
			httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "ConceptDrug.saved");
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
		
		Drug drug = null;
		
		if (context != null && context.isAuthenticated()) {
			ConceptService cs = context.getConceptService();
			String id = request.getParameter("drugId");
	    	if (id != null) {
	    		drug = cs.getDrug(Integer.valueOf(id));
	    	}
		}
		
		if (drug == null)
			drug = new Drug();
    	
        return drug;
    }
    
    protected Map referenceData(HttpServletRequest request, Object obj, Errors errs) throws Exception {
		
		HttpSession httpSession = request.getSession();
		Context context = (Context) httpSession.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);

		Drug drug = (Drug)obj;
		
		Map<String, Object> map = new HashMap<String, Object>();
		String defaultVerbose = "false";
		
		if (context != null && context.isAuthenticated()) {
			ConceptService cs = context.getConceptService();
			
			if (drug.getConcept() != null)
				map.put("conceptName", drug.getConcept().getName(request.getLocale()));
			defaultVerbose = context.getAuthenticatedUser().getProperty(OpenmrsConstants.USER_PROPERTY_SHOW_VERBOSE);
		}
		map.put("datePattern", dateFormat.toLocalizedPattern().toLowerCase());

		map.put("defaultVerbose", defaultVerbose.equals("true") ? true : false);
		
		String editReason = request.getParameter("editReason");
		if (editReason == null)
			editReason = "";
		
		map.put("editReason", editReason);
		
		return map;
	}

}