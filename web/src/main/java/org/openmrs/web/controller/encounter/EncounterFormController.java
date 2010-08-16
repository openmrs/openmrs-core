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
package org.openmrs.web.controller.encounter;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
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
import org.openmrs.propertyeditor.EncounterTypeEditor;
import org.openmrs.propertyeditor.FormEditor;
import org.openmrs.propertyeditor.LocationEditor;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.OpenmrsUtil;
import org.openmrs.web.WebConstants;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

/**
 * This class controls the encounter.form jsp page. See
 * /web/WEB-INF/view/admin/encounters/encounterForm.jsp
 */
public class EncounterFormController extends SimpleFormController {
	
	/** Logger for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());
	
	/**
	 * Allows for Integers to be used as values in input tags. Normally, only strings and lists are
	 * expected
	 * 
	 * @see org.springframework.web.servlet.mvc.BaseCommandController#initBinder(javax.servlet.http.HttpServletRequest,
	 *      org.springframework.web.bind.ServletRequestDataBinder)
	 */
	@Override
    protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
		super.initBinder(request, binder);
		
		binder.registerCustomEditor(java.lang.Integer.class, new CustomNumberEditor(java.lang.Integer.class, true));
		binder.registerCustomEditor(java.util.Date.class, new CustomDateEditor(Context.getDateFormat(), true));
		binder.registerCustomEditor(EncounterType.class, new EncounterTypeEditor());
		binder.registerCustomEditor(Location.class, new LocationEditor());
		binder.registerCustomEditor(Form.class, new FormEditor());
	}
	
	/**
	 * @see org.springframework.web.servlet.mvc.SimpleFormController#processFormSubmission(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse, java.lang.Object,
	 *      org.springframework.validation.BindException)
	 */
	@Override
    protected ModelAndView processFormSubmission(HttpServletRequest request, HttpServletResponse reponse, Object obj,
	                                             BindException errors) throws Exception {
		
		Encounter encounter = (Encounter) obj;
		
		try {
			if (Context.isAuthenticated()) {
				Context.addProxyPrivilege(OpenmrsConstants.PRIV_VIEW_USERS);
				Context.addProxyPrivilege(OpenmrsConstants.PRIV_VIEW_PATIENTS);
				
				if (StringUtils.hasText(request.getParameter("patientId")))
					encounter.setPatient(Context.getPatientService().getPatient(
					    Integer.valueOf(request.getParameter("patientId"))));
				if (StringUtils.hasText(request.getParameter("providerId")))
					encounter.setProvider(Context.getPersonService().getPerson(
					    Integer.valueOf(request.getParameter("providerId"))));
				if (encounter.isVoided())
					ValidationUtils.rejectIfEmptyOrWhitespace(errors, "voidReason", "error.null");
				
				ValidationUtils.rejectIfEmptyOrWhitespace(errors, "patient", "error.null");
				ValidationUtils.rejectIfEmptyOrWhitespace(errors, "provider", "error.null");
				ValidationUtils.rejectIfEmptyOrWhitespace(errors, "encounterDatetime", "error.null");
				
			}
		}
		finally {
			Context.removeProxyPrivilege(OpenmrsConstants.PRIV_VIEW_USERS);
			Context.removeProxyPrivilege(OpenmrsConstants.PRIV_VIEW_PATIENTS);
		}
		
		return super.processFormSubmission(request, reponse, encounter, errors);
	}
	
	/**
	 * The onSubmit function receives the form/command object that was modified by the input form
	 * and saves it to the db
	 * 
	 * @see org.springframework.web.servlet.mvc.SimpleFormController#onSubmit(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse, java.lang.Object,
	 *      org.springframework.validation.BindException)
	 */
	@Override
    protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object obj,
	                                BindException errors) throws Exception {
		
		HttpSession httpSession = request.getSession();
		
		String view = getFormView();
		
		try {
			Context.addProxyPrivilege(OpenmrsConstants.PRIV_VIEW_USERS);
			Context.addProxyPrivilege(OpenmrsConstants.PRIV_VIEW_PATIENTS);
			
			if (Context.isAuthenticated()) {
				Encounter encounter = (Encounter) obj;
				
				// if this is a new encounter, they can specify a patient.  add it
				if (request.getParameter("patientId") != null)
					encounter.setPatient(Context.getPatientService().getPatient(
					    Integer.valueOf(request.getParameter("patientId"))));
				
				// set the provider if they changed it
				encounter.setProvider(Context.getPersonService().getPerson(Integer.valueOf(request.getParameter("providerId"))));
				
				if (encounter.isVoided() && encounter.getVoidedBy() == null)
					// if this is a "new" voiding, call voidEncounter to set appropriate attributes
					Context.getEncounterService().voidEncounter(encounter, encounter.getVoidReason());
				else if (!encounter.isVoided() && encounter.getVoidedBy() != null)
					// if this was just unvoided, call unvoidEncounter to unset appropriate attributes
					Context.getEncounterService().unvoidEncounter(encounter);
				else
					Context.getEncounterService().saveEncounter(encounter);
				
				view = getSuccessView();
				view = view + "?encounterId=" + encounter.getEncounterId();
				
				httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "Encounter.saved");
			}
		}
		finally {
			Context.removeProxyPrivilege(OpenmrsConstants.PRIV_VIEW_USERS);
			Context.removeProxyPrivilege(OpenmrsConstants.PRIV_VIEW_PATIENTS);
		}
		
		return new ModelAndView(new RedirectView(view));
	}
	
	/**
	 * This is called prior to displaying a form for the first time. It tells Spring the
	 * form/command object to load into the request
	 * 
	 * @see org.springframework.web.servlet.mvc.AbstractFormController#formBackingObject(javax.servlet.http.HttpServletRequest)
	 */
	@Override
    protected Object formBackingObject(HttpServletRequest request) throws ServletException {
		
		Encounter encounter = null;
		
		if (Context.isAuthenticated()) {
			EncounterService es = Context.getEncounterService();
			String encounterId = request.getParameter("encounterId");
			if (encounterId != null) {
				encounter = es.getEncounter(Integer.valueOf(encounterId));
			}
		}
		
		if (encounter == null)
			encounter = new Encounter();
		
		return encounter;
	}
	
	/**
	 * @see org.springframework.web.servlet.mvc.SimpleFormController#referenceData(javax.servlet.http.HttpServletRequest,
	 *      java.lang.Object, org.springframework.validation.Errors)
	 */
	@Override
    protected Map<String, Object> referenceData(HttpServletRequest request, Object obj, Errors error) throws Exception {
		
		Encounter encounter = (Encounter) obj;
		
		// the generic returned key-value pair mapping
		Map<String, Object> map = new HashMap<String, Object>();
		
		// obsIds of obs that were edited
		List<Integer> editedObs = new Vector<Integer>();
		List<Integer> obsAfterEncounter = new Vector<Integer>();
		
		// the map returned to the form
		// This is a mapping between the formfield and a list of the Obs/ObsGroup in that field
		// This mapping is sorted according to the comparator in FormField.java
		SortedMap<FormField, List<Obs>> obsMapToReturn = null;
		String sortType = Context.getAdministrationService().getGlobalProperty(
		    OpenmrsConstants.GLOBAL_PROPERTY_ENCOUNTER_FORM_OBS_SORT_ORDER);
		if ("weight".equals(sortType))
			obsMapToReturn = new TreeMap<FormField, List<Obs>>(); // use FormField.compareTo
		else
			obsMapToReturn = new TreeMap<FormField, List<Obs>>(new NumberingFormFieldComparator()); // use custom comparator
			
		// this maps the obs to form field objects for non top-level obs
		// it is keyed on obs so that when looping over an exploded obsGroup
		// the formfield can be fetched easily (in order to show the field numbers etc)
		Map<Obs, FormField> otherFormFields = new HashMap<Obs, FormField>();
		
		if (Context.isAuthenticated()) {
			EncounterService es = Context.getEncounterService();
			FormService fs = Context.getFormService();
			
			// used to restrict the form field lookup
			Form form = encounter.getForm();
			
			map.put("encounterTypes", es.getAllEncounterTypes());
			map.put("forms", Context.getFormService().getAllForms());
			// loop over the encounter's observations to find the edited obs
			String reason = "";
			for (Obs o : encounter.getObsAtTopLevel(true)) {
				
				//get the obs that was not created with the original encounter
				Encounter en = o.getEncounter();
				if(o.getDateCreated().compareTo(en.getDateCreated())!=0){
					obsAfterEncounter.add(o.getId());
				}
				
				// only the voided obs have been edited
				if (o.isVoided()) {
					// assumes format of: ".* (new obsId: \d*)"
					reason = o.getVoidReason();
					int start = reason.lastIndexOf(" ") + 1;
					int end = reason.length() - 1;
					try {
						reason = reason.substring(start, end);
						editedObs.add(Integer.valueOf(reason));
					}
					catch (Exception e) {}
				}
				
				// get the formfield for this obs
				FormField ff = fs.getFormField(form, o.getConcept(), obsMapToReturn.keySet(), false);
				if (ff == null)
					ff = new FormField();
				
				// we only put the top-level obs in the obsMap.  Those would
				// be the obs that don't have an obs grouper 
				if (o.getObsGroup() == null) {
					// populate the obs map with this formfield and obs
					List<Obs> list = obsMapToReturn.get(ff);
					if (list == null) {
						list = new Vector<Obs>();
						obsMapToReturn.put(ff, list);
					}
					list.add(o);
				} else {
					// this is not a top-level obs, just put the formField
					// in a separate list and be done with it
					otherFormFields.put(o, ff);
				}
			}
		}
		
		if (log.isDebugEnabled())
			log.debug("setting obsMap in page context (size: " + obsMapToReturn.size() + ")");
		map.put("obsMap", obsMapToReturn);
		
		map.put("otherFormFields", otherFormFields);
		
		map.put("locale", Context.getLocale());
		map.put("editedObs", editedObs);
	
		map.put("obsAfterEncounter", obsAfterEncounter);

		return map;
	}
	
	/**
	 * Comparator to sort the FormFields by page+fieldNumber+fieldPart/sortWeight. This allows obs
	 * to be sorted/displayed strictly according to numbering. The FormField default comparator
	 * sorts on sortWeight first, then other numbers.
	 * 
	 * @see FormField#compareTo(FormField)
	 * @see EncounterDisplayController
	 */
	public class NumberingFormFieldComparator implements Comparator<FormField> {
		
		/**
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		public int compare(FormField formField, FormField other) {
			int temp = OpenmrsUtil.compareWithNullAsGreatest(formField.getPageNumber(), other.getPageNumber());
			if (temp == 0)
				temp = OpenmrsUtil.compareWithNullAsGreatest(formField.getFieldNumber(), other.getFieldNumber());
			if (temp == 0)
				temp = OpenmrsUtil.compareWithNullAsGreatest(formField.getFieldPart(), other.getFieldPart());
			if (temp == 0 && formField.getPageNumber() == null && formField.getFieldNumber() == null
			        && formField.getFieldPart() == null)
				temp = OpenmrsUtil.compareWithNullAsGreatest(formField.getSortWeight(), other.getSortWeight());
			if (temp == 0) // to prevent ties
				temp = OpenmrsUtil.compareWithNullAsGreatest(formField.getFormFieldId(), other.getFormFieldId());
			return temp;
		}
		
	}
	
}
