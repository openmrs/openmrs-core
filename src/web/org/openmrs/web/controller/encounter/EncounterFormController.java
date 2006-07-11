package org.openmrs.web.controller.encounter;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Form;
import org.openmrs.FormField;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.api.EncounterService;
import org.openmrs.api.FormService;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.web.WebConstants;
import org.openmrs.web.propertyeditor.EncounterTypeEditor;
import org.openmrs.web.propertyeditor.FormEditor;
import org.openmrs.web.propertyeditor.LocationEditor;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

public class EncounterFormController extends SimpleFormController {
	
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
		
        binder.registerCustomEditor(java.lang.Integer.class,
                new CustomNumberEditor(java.lang.Integer.class, true));
        binder.registerCustomEditor(java.util.Date.class, 
        		new CustomDateEditor(dateFormat, true));
        binder.registerCustomEditor(EncounterType.class, new EncounterTypeEditor(context));
        binder.registerCustomEditor(Location.class, new LocationEditor(context));
        binder.registerCustomEditor(Form.class, new FormEditor(context));
	}

	protected ModelAndView processFormSubmission(HttpServletRequest request, HttpServletResponse reponse, Object obj, BindException errors) throws Exception {
		
		HttpSession httpSession = request.getSession();
		Context context = (Context) httpSession.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		
		Encounter encounter = (Encounter)obj;
		
		context.addProxyPrivilege(OpenmrsConstants.PRIV_VIEW_USERS);
		context.addProxyPrivilege(OpenmrsConstants.PRIV_VIEW_PATIENTS);
		try {
			if (context != null && context.isAuthenticated()) {
				if (request.getParameter("patientId") != null)
					encounter.setPatient(context.getPatientService().getPatient(Integer.valueOf(request.getParameter("patientId"))));
				if (request.getParameter("providerId") != null)
					encounter.setProvider(context.getUserService().getUser(Integer.valueOf(request.getParameter("providerId"))));
				if (encounter.isVoided())
					ValidationUtils.rejectIfEmptyOrWhitespace(errors, "voidReason", "error.null");
				
			}
		} finally {
			context.removeProxyPrivilege(OpenmrsConstants.PRIV_VIEW_USERS);
			context.removeProxyPrivilege(OpenmrsConstants.PRIV_VIEW_PATIENTS);
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

		context.addProxyPrivilege(OpenmrsConstants.PRIV_VIEW_USERS);
		context.addProxyPrivilege(OpenmrsConstants.PRIV_VIEW_PATIENTS);
		try {
			if (context != null && context.isAuthenticated()) {
				Encounter encounter = (Encounter)obj;
				
				// if this is a new encounter, they can specify a patient.  add it
				if (request.getParameter("patientId") != null)
					encounter.setPatient(context.getPatientService().getPatient(Integer.valueOf(request.getParameter("patientId"))));
				
				// set the provider if they changed it
				encounter.setProvider(context.getUserService().getUser(Integer.valueOf(request.getParameter("providerId"))));
				
				if (encounter.isVoided() && encounter.getVoidedBy() == null)
					// if this is a "new" voiding, call voidEncounter to set appropriate attributes
					context.getEncounterService().voidEncounter(encounter, encounter.getVoidReason());
				else if (!encounter.isVoided() && encounter.getVoidedBy() != null)
					// if this was just unvoided, call unvoidEncounter to unset appropriate attributes
					context.getEncounterService().unvoidEncounter(encounter);
				else
					context.getEncounterService().updateEncounter(encounter);
				
				view = getSuccessView();
				view = view + "?encounterId=" + encounter.getEncounterId();
				
				httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "Encounter.saved");
			}
		} finally {
			context.removeProxyPrivilege(OpenmrsConstants.PRIV_VIEW_USERS);
			context.removeProxyPrivilege(OpenmrsConstants.PRIV_VIEW_PATIENTS);
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

	protected Map referenceData(HttpServletRequest request, Object obj, Errors error) throws Exception {
		
		HttpSession httpSession = request.getSession();
		Context context = (Context) httpSession.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		Encounter encounter = (Encounter)obj;
		
		Map<String, Object> map = new HashMap<String, Object>();
		List<Integer> editedObs = new Vector<Integer>();

		// The map returned to the form
		Map<Obs, FormField> obsMap = new HashMap<Obs, FormField>();
		// User for sorting
		Map<Obs, FormField> obsMapTemp = new HashMap<Obs, FormField>();
		
		// temporary list to hold the sorted obs
		List<FormField> formFields = new Vector<FormField>();
		
		List<Obs> observations = new Vector<Obs>();
		
		Form form = encounter.getForm();
		
		if (context != null && context.isAuthenticated()) {
			EncounterService es = context.getEncounterService();
			FormService fs = context.getFormService();
			
			map.put("encounterTypes", es.getEncounterTypes());
			map.put("forms", context.getFormService().getForms());
			// loop over the encounter's observations to find the edited obs
			String reason = "";
			if (encounter.getObs() != null && !encounter.getObs().isEmpty()) {
				for (Obs o : encounter.getObs()) {
					// only the voided obs have been edited
					if (o.isVoided()){
						// assumes format of: ".* (new obsId: \d*)"
						reason = o.getVoidReason();
						int start = reason.lastIndexOf(" ") + 1;
						int end = reason.length() - 1;
						try {
							reason = reason.substring(start, end);
							editedObs.add(Integer.valueOf(reason));
						} catch (Exception e) {}
					}
					
					// populate the obs map so we can 
					//  1) sort the obs according to FormField
					//  2) look up the formField by the obs object
					FormField ff = fs.getFormField(form, o.getConcept());
					if (ff == null)
						ff = new FormField();
					formFields.add(ff);
					obsMap.put(o, ff);
					obsMapTemp.put(o, ff);
				}
				
				// sort the temp list according the the FormFields.compare() method
				Collections.sort(formFields);
				
				// loop over the sorted formFields to add the corresponding
				//  obs to the returned obs list
				for (FormField f : formFields) {
					observations.add(popObsFromMap(obsMapTemp, f));
				}
			}
		}
		
		log.debug("setting sorted observations in page context (size: " + observations.size() + ")");
		map.put("observations", observations);
		
		log.debug("setting obsMap in page context (size: " + obsMap.size() + ")");
		map.put("obsMap", obsMap);
		
		log.debug("setting datePattern in page context");
		map.put("datePattern", dateFormat.toLocalizedPattern().toLowerCase());
		
		log.debug("setting locale in page context: " + context.getLocale());
		map.put("locale", context.getLocale());
		
		log.debug("setting edited obs in page context: " + editedObs);
		map.put("editedObs", editedObs);
		
		return map;
	}
    
	/**
	 * Searches the given map for the given FormField
	 * 
	 * @param map
	 * @param f
	 * @return
	 */
	private Obs popObsFromMap(Map<Obs, FormField> map, FormField f) {
		for (Map.Entry<Obs, FormField> entry : map.entrySet()) {
			if (entry.getValue() == f) {
				Obs o = entry.getKey();
				map.remove(o);
				return o;
			}
		}
		
		return null;
	}
	
}
	