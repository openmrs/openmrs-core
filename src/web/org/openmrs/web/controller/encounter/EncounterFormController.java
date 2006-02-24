package org.openmrs.web.controller.encounter;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Form;
import org.openmrs.Location;
import org.openmrs.api.EncounterService;
import org.openmrs.api.context.Context;
import org.openmrs.web.WebConstants;
import org.openmrs.web.propertyeditor.EncounterTypeEditor;
import org.openmrs.web.propertyeditor.FormEditor;
import org.openmrs.web.propertyeditor.LocationEditor;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

public class EncounterFormController extends SimpleFormController {
	
    /** Logger for this class and subclasses */
    protected final Log log = LogFactory.getLog(getClass());
    
    Locale locale = Locale.UK;
    String datePattern = "dd/MM/yyyy";
    
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
        //NumberFormat nf = NumberFormat.getInstance(new Locale("en_US"));
        binder.registerCustomEditor(java.lang.Integer.class,
                new CustomNumberEditor(java.lang.Integer.class, true));
        binder.registerCustomEditor(java.util.Date.class, 
        		new CustomDateEditor(new SimpleDateFormat(datePattern, locale), true));
        binder.registerCustomEditor(EncounterType.class, new EncounterTypeEditor(context));
        binder.registerCustomEditor(Location.class, new LocationEditor(context));
        binder.registerCustomEditor(Form.class, new FormEditor(context));
	}

	protected ModelAndView processFormSubmission(HttpServletRequest request, HttpServletResponse reponse, Object obj, BindException errors) throws Exception {
		
		HttpSession httpSession = request.getSession();
		Context context = (Context) httpSession.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		
		Encounter encounter = (Encounter)obj;
		
		if (context != null && context.isAuthenticated()) {
			if (request.getParameter("patientId") != null)
				encounter.setPatient(context.getPatientService().getPatient(Integer.valueOf(request.getParameter("patientId"))));
			if (request.getParameter("providerId") != null)
				encounter.setProvider(context.getUserService().getUser(Integer.valueOf(request.getParameter("providerId"))));
		}
		return super.processFormSubmission(request, reponse, encounter, errors);
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
			Encounter encounter = (Encounter)obj;
			encounter.setPatient(context.getPatientService().getPatient(Integer.valueOf(request.getParameter("patientId"))));
			encounter.setProvider(context.getUserService().getUser(Integer.valueOf(request.getParameter("providerId"))));
			context.getEncounterService().updateEncounter(encounter);
			view = getSuccessView();
			httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "Encounter.saved");
			view = view + "?autoJump=false&phrase=" + request.getParameter("phrase");
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
		
		Encounter encounter = null;
		
		if (context != null && context.isAuthenticated()) {
			EncounterService es = context.getEncounterService();
			String encounterId = request.getParameter("encounterId");
	    	if (encounterId != null) {
	    		encounter = es.getEncounter(Integer.valueOf(encounterId));
	    		//encounter.getObs();
	    	}
		}
		
		if (encounter == null)
			encounter = new Encounter();
    	
        return encounter;
    }

	protected Map referenceData(HttpServletRequest request) throws Exception {
		
		HttpSession httpSession = request.getSession();
		Context context = (Context) httpSession.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);

		Map<String, Object> map = new HashMap<String, Object>();
		
		if (context != null && context.isAuthenticated()) {
			EncounterService es = context.getEncounterService();
			map.put("encounterTypes", es.getEncounterTypes());
			map.put("forms", context.getFormService().getForms());
		}
		
		return map;
	}
    
}