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
package org.openmrs.web.controller.observation;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Encounter;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.api.APIException;
import org.openmrs.api.EncounterService;
import org.openmrs.api.ObsService;
import org.openmrs.api.context.Context;
import org.openmrs.propertyeditor.LocationEditor;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.OpenmrsUtil;
import org.openmrs.web.WebConstants;
import org.springframework.beans.propertyeditors.CustomBooleanEditor;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

/**
 * This controller gives the backing object and does the saving for the
 * obs.form page.  The jsp for this page is located in 
 * /web/WEB-INF/view/admin/observations/obsForm.jsp
 * 
 */
public class ObsFormController extends SimpleFormController {
	
    /** Logger for this class and subclasses */
    protected final Log log = LogFactory.getLog(getClass());
    
	/**
	 * 
	 * Allows for Integers to be used as values in input tags.
	 *   Normally, only strings and lists are expected 
	 * 
	 * @see org.springframework.web.servlet.mvc.BaseCommandController#initBinder(javax.servlet.http.HttpServletRequest, org.springframework.web.bind.ServletRequestDataBinder)
	 */
	protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
		super.initBinder(request, binder);
		
        binder.registerCustomEditor(java.lang.Integer.class,
                new CustomNumberEditor(java.lang.Integer.class, true));
        binder.registerCustomEditor(java.util.Date.class, 
        		new CustomDateEditor(OpenmrsUtil.getDateFormat(), true));
        binder.registerCustomEditor(Location.class, new LocationEditor());
        binder.registerCustomEditor(java.lang.Boolean.class,
        		new CustomBooleanEditor(true)); //allow for an empty boolean value
	}

	/**
	 * @see org.springframework.web.servlet.mvc.SimpleFormController#processFormSubmission(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.Object, org.springframework.validation.BindException)
	 */
	protected ModelAndView processFormSubmission(HttpServletRequest request, HttpServletResponse reponse, Object obj, BindException errors) throws Exception {
		
		// sets the objects in case edit Reason is rejected
		Obs obs = (Obs)obj;
		obs = setObjects(obs, request);
    	
    	String reason = request.getParameter("editReason");
    	if (obs.getObsId() != null && (reason == null || reason.length() == 0))
    		errors.reject("editReason", "Obs.edit.reason.empty");

    	if (obs.getConcept() == null)
    		errors.rejectValue("concept", "error.null");
    	
		return super.processFormSubmission(request, reponse, obs, errors);
	}
	
	/**
     * @see org.springframework.web.servlet.mvc.BaseCommandController#onBind(javax.servlet.http.HttpServletRequest, java.lang.Object, org.springframework.validation.BindException)
     */
    @Override
    protected void onBind(HttpServletRequest request, Object command, BindException errors) throws Exception {
    	if (Context.isAuthenticated()) {
		    Obs obs = (Obs) command;
		    setObjects(obs, request);
    	}
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
		String view = getFormView();
		
		if (Context.isAuthenticated()) {
			Obs obs = (Obs)obj;
			ObsService os = Context.getObsService();
			String reason = request.getParameter("editReason");
	    	
			try {
				os.saveObs(obs, reason);
			}
			catch (APIException e) {
				httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, e.getMessage());
				return showForm(request, response, errors);
			}

			httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "Obs.saved");
			
			if (obs.getEncounter() != null)
				view = getSuccessView() + "?encounterId=" + obs.getEncounter().getEncounterId() + "&phrase=" + request.getParameter("phrase");
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

		Obs obs = null;
		
		if (Context.isAuthenticated()) {
			ObsService os = Context.getObsService();
			EncounterService es = Context.getEncounterService();
			
			String obsId = request.getParameter("obsId");
	    	String encounterId = request.getParameter("encounterId");
	    	
			if (obsId != null)
	    		obs = os.getObs(Integer.valueOf(obsId));
	    	else if (StringUtils.hasText(encounterId)) {
	    		Encounter e = es.getEncounter(Integer.valueOf(encounterId));
	    		obs = new Obs();
	    		obs.setEncounter(e);
	    		obs.setPerson(e.getPatient());
	    		obs.setLocation(e.getLocation());
	    		obs.setObsDatetime(e.getEncounterDatetime());
	    	}
		}
		
		if (obs == null)
			obs = new Obs();
    	
        return obs;
    }

	/**
	 * The other things shown on the obs form that are in the database
	 * 
	 * @see org.springframework.web.servlet.mvc.SimpleFormController#referenceData(javax.servlet.http.HttpServletRequest, java.lang.Object, org.springframework.validation.Errors)
	 */
	protected Map<String, Object> referenceData(HttpServletRequest request, Object obj, Errors errs) throws Exception {
		
		Obs obs = (Obs)obj;
		
		Map<String, Object> map = new HashMap<String, Object>();
		String defaultVerbose = "false";
		
		if (Context.isAuthenticated()) {
			map.put("forms", Context.getFormService().getForms());
			if (obs.getConcept() != null)
				map.put("conceptName", obs.getConcept().getName(request.getLocale()));
			defaultVerbose = Context.getAuthenticatedUser().getUserProperty(OpenmrsConstants.USER_PROPERTY_SHOW_VERBOSE);
		}
		map.put("defaultVerbose", defaultVerbose.equals("true") ? true : false);
		
		String editReason = request.getParameter("editReason");
		if (editReason == null)
			editReason = "";
		
		map.put("editReason", editReason);
		
		return map;
	}
	
	/**
	 * Convenience method used when saving the object to populate the object with 
	 * full-fledged objects
	 * 
	 * @param obs
	 * @param request
	 * @return
	 */
	private Obs setObjects(Obs obs, HttpServletRequest request) {

		if (Context.isAuthenticated()) {
			if (obs.getObsId() == null) { //patient/order/concept/encounter only change when adding a new observation
				if (StringUtils.hasText(request.getParameter("personId")))
					obs.setPerson(Context.getPatientService().getPatient(Integer.valueOf(request.getParameter("personId"))));
				else
					obs.setPerson(null);
				if (StringUtils.hasText(request.getParameter("orderId")))
					obs.setOrder(Context.getOrderService().getOrder(Integer.valueOf(request.getParameter("orderId"))));
				else
					obs.setOrder(null);
				if (StringUtils.hasText(request.getParameter("conceptId")))
					obs.setConcept(Context.getConceptService().getConcept(Integer.valueOf(request.getParameter("conceptId"))));
				else
					obs.setConcept(null);
				if (StringUtils.hasText(request.getParameter("encounterId")))
					obs.setEncounter(Context.getEncounterService().getEncounter(Integer.valueOf(request.getParameter("encounterId"))));
				else
					obs.setEncounter(null);
				
			}
			
			if (StringUtils.hasText(request.getParameter("valueCodedId")))
				obs.setValueCoded(Context.getConceptService().getConcept(Integer.valueOf(request.getParameter("valueCodedId"))));
			else
				obs.setValueCoded(null);
			if (StringUtils.hasText(request.getParameter("valueDrugId")))
				obs.setValueDrug(Context.getConceptService().getDrug(Integer.valueOf(request.getParameter("valueDrugId"))));
			else
				obs.setValueDrug(null);
		}
		
		return obs;

	}

}
