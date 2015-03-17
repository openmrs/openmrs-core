/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.controller.encounter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Encounter;
import org.openmrs.EncounterRole;
import org.openmrs.EncounterType;
import org.openmrs.Form;
import org.openmrs.FormField;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.Provider;
import org.openmrs.Visit;
import org.openmrs.api.APIException;
import org.openmrs.api.EncounterService;
import org.openmrs.api.FormService;
import org.openmrs.api.ProviderService;
import org.openmrs.api.context.Context;
import org.openmrs.propertyeditor.EncounterTypeEditor;
import org.openmrs.propertyeditor.FormEditor;
import org.openmrs.propertyeditor.LocationEditor;
import org.openmrs.propertyeditor.VisitEditor;
import org.openmrs.util.MetadataComparator;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.OpenmrsUtil;
import org.openmrs.util.PrivilegeConstants;
import org.openmrs.validator.EncounterValidator;
import org.openmrs.web.WebConstants;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.ServletRequestUtils;
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
		binder.registerCustomEditor(java.util.Date.class, new CustomDateEditor(Context.getDateTimeFormat(), true));
		binder.registerCustomEditor(EncounterType.class, new EncounterTypeEditor());
		binder.registerCustomEditor(Location.class, new LocationEditor());
		binder.registerCustomEditor(Form.class, new FormEditor());
		binder.registerCustomEditor(Visit.class, new VisitEditor());
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
				Context.addProxyPrivilege(PrivilegeConstants.VIEW_USERS);
				Context.addProxyPrivilege(PrivilegeConstants.VIEW_PATIENTS);
				
				if (encounter.getEncounterId() == null && StringUtils.hasText(request.getParameter("patientId"))) {
					encounter.setPatient(Context.getPatientService().getPatient(
					    Integer.valueOf(request.getParameter("patientId"))));
				}
				if (encounter.isVoided()) {
					ValidationUtils.rejectIfEmptyOrWhitespace(errors, "voidReason", "error.null");
				}
				
				String[] providerIdsArray = ServletRequestUtils.getStringParameters(request, "providerIds");
				if (ArrayUtils.isEmpty(providerIdsArray)) {
					errors.reject("Encounter.provider.atleastOneProviderRequired");
				}
				
				String[] roleIdsArray = ServletRequestUtils.getStringParameters(request, "encounterRoleIds");
				
				ProviderService ps = Context.getProviderService();
				EncounterService es = Context.getEncounterService();
				if (providerIdsArray != null && roleIdsArray != null) {
					//list to store role provider mappings to be used below to detect removed providers
					ArrayList<String> unremovedRoleAndProviders = new ArrayList<String>();
					for (int i = 0; i < providerIdsArray.length; i++) {
						if (StringUtils.hasText(providerIdsArray[i]) && StringUtils.hasText(roleIdsArray[i])) {
							unremovedRoleAndProviders.add(roleIdsArray[i] + "-" + providerIdsArray[i]);
							Provider provider = ps.getProvider(Integer.valueOf(providerIdsArray[i]));
							EncounterRole encounterRole = es.getEncounterRole(Integer.valueOf(roleIdsArray[i]));
							//if this is an existing provider, don't create a new one to avoid losing existing
							//details like dateCreated, creator, uuid etc in the encounter_provider table
							if (encounter.getProvidersByRole(encounterRole).contains(provider)) {
								continue;
							}
							
							//this is a new provider
							encounter.addProvider(encounterRole, provider);
						}
					}
					//Get rid of the removed ones
					for (Map.Entry<EncounterRole, Set<Provider>> entry : encounter.getProvidersByRoles().entrySet()) {
						for (Provider p : entry.getValue()) {
							if (!unremovedRoleAndProviders.contains(entry.getKey().getEncounterRoleId() + "-"
							        + p.getProviderId())) {
								encounter.removeProvider(entry.getKey(), p);
							}
						}
					}
				}
				
				ValidationUtils.invokeValidator(new EncounterValidator(), encounter, errors);
			}
		}
		finally {
			Context.removeProxyPrivilege(PrivilegeConstants.VIEW_USERS);
			Context.removeProxyPrivilege(PrivilegeConstants.VIEW_PATIENTS);
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
	 *
	 * @should transfer encounter to another patient when encounter patient was changed
	 */
	@Override
	protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object obj,
	        BindException errors) throws Exception {
		
		HttpSession httpSession = request.getSession();
		
		String view = getFormView();
		
		try {
			Context.addProxyPrivilege(PrivilegeConstants.VIEW_USERS);
			Context.addProxyPrivilege(PrivilegeConstants.VIEW_PATIENTS);
			
			if (Context.isAuthenticated()) {
				Encounter encounter = (Encounter) obj;
				
				Integer patientId = null;
				
				if (request.getParameter("patientId") != null) {
					patientId = Integer.valueOf(request.getParameter("patientId"));
				}
				
				if (encounter.getEncounterId() == null) {
					// if this is a new encounter, they can specify a patient.  add it
					if (patientId != null) {
						encounter.setPatient(Context.getPatientService().getPatient(patientId));
					}
				} else {
					Patient oldPatient = encounter.getPatient();
					Patient newPatient = Context.getPatientService().getPatient(patientId);
					if (newPatient != null && oldPatient != null && !newPatient.equals(oldPatient)) {
						encounter = Context.getEncounterService().transferEncounter(encounter, newPatient);
					}
				}
				
				if (encounter.isVoided() && encounter.getVoidedBy() == null) {
					// if this is a "new" voiding, call voidEncounter to set appropriate attributes
					Context.getEncounterService().voidEncounter(encounter, encounter.getVoidReason());
				} else if (!encounter.isVoided() && encounter.getVoidedBy() != null) {
					// if this was just unvoided, call unvoidEncounter to unset appropriate attributes
					Context.getEncounterService().unvoidEncounter(encounter);
				} else {
					Context.getEncounterService().saveEncounter(encounter);
				}
				
				view = getSuccessView();
				view = view + "?encounterId=" + encounter.getEncounterId();
				
				httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "Encounter.saved");
			}
		}
		catch (APIException e) {
			log.error("Error while trying to save the encounter", e);
			httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "Encounter.cannot.save");
			errors.reject("encounter", "Encounter.cannot.save");
			// return to the form because an exception was thrown
			return showForm(request, response, errors);
		}
		finally {
			Context.removeProxyPrivilege(PrivilegeConstants.VIEW_USERS);
			Context.removeProxyPrivilege(PrivilegeConstants.VIEW_PATIENTS);
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
		
		if (encounter == null) {
			encounter = new Encounter();
		}
		
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
		
		// the map returned to the form
		// This is a mapping between the formfield and a list of the Obs/ObsGroup in that field
		// This mapping is sorted according to the comparator in FormField.java
		SortedMap<FormField, List<Obs>> obsMapToReturn = null;
		String sortType = Context.getAdministrationService().getGlobalProperty(
		    OpenmrsConstants.GLOBAL_PROPERTY_ENCOUNTER_FORM_OBS_SORT_ORDER);
		if ("weight".equals(sortType)) {
			obsMapToReturn = new TreeMap<FormField, List<Obs>>(); // use FormField.compareTo
		} else {
			obsMapToReturn = new TreeMap<FormField, List<Obs>>(new NumberingFormFieldComparator()); // use custom comparator
		}
		
		// this maps the obs to form field objects for non top-level obs
		// it is keyed on obs so that when looping over an exploded obsGroup
		// the formfield can be fetched easily (in order to show the field numbers etc)
		Map<Obs, FormField> otherFormFields = new HashMap<Obs, FormField>();
		
		if (Context.isAuthenticated()) {
			EncounterService es = Context.getEncounterService();
			FormService fs = Context.getFormService();
			
			// used to restrict the form field lookup
			Form form = encounter.getForm();
			
			List<EncounterType> encTypes = es.getAllEncounterTypes();
			// Non-retired types first
			Collections.sort(encTypes, new MetadataComparator(Context.getLocale()));
			map.put("encounterTypes", encTypes);
			
			map.put("encounterRoles", es.getAllEncounterRoles(false));
			map.put("forms", Context.getFormService().getAllForms());
			// loop over the encounter's observations to find the edited obs
			for (Obs o : encounter.getObsAtTopLevel(true)) {
				
				// only edited obs has previous version
				if (o.hasPreviousVersion()) {
					editedObs.add(o.getObsId());
				}
				
				// get the formfield for this obs
				FormField ff = fs.getFormField(form, o.getConcept(), obsMapToReturn.keySet(), false);
				if (ff == null) {
					ff = new FormField();
				}
				
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
		
		if (log.isDebugEnabled()) {
			log.debug("setting obsMap in page context (size: " + obsMapToReturn.size() + ")");
		}
		map.put("obsMap", obsMapToReturn);
		
		map.put("otherFormFields", otherFormFields);
		
		map.put("locale", Context.getLocale());
		map.put("editedObs", editedObs);
		if (encounter.getPatient() != null) {
			map.put("patientVisits", Context.getVisitService().getVisitsByPatient(encounter.getPatient()));
		}
		
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
			if (temp == 0) {
				temp = OpenmrsUtil.compareWithNullAsGreatest(formField.getFieldNumber(), other.getFieldNumber());
			}
			if (temp == 0) {
				temp = OpenmrsUtil.compareWithNullAsGreatest(formField.getFieldPart(), other.getFieldPart());
			}
			if (temp == 0 && formField.getPageNumber() == null && formField.getFieldNumber() == null
			        && formField.getFieldPart() == null) {
				temp = OpenmrsUtil.compareWithNullAsGreatest(formField.getSortWeight(), other.getSortWeight());
			}
			if (temp == 0) {
				// to prevent ties
				temp = OpenmrsUtil.compareWithNullAsGreatest(formField.getFormFieldId(), other.getFormFieldId());
			}
			return temp;
		}
		
	}
	
}
