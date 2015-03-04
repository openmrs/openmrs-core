/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.controller.observation;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.Drug;
import org.openmrs.Encounter;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.Order;
import org.openmrs.Person;
import org.openmrs.api.APIException;
import org.openmrs.api.EncounterService;
import org.openmrs.api.ObsService;
import org.openmrs.api.context.Context;
import org.openmrs.obs.ComplexData;
import org.openmrs.obs.ComplexObsHandler;
import org.openmrs.propertyeditor.ConceptEditor;
import org.openmrs.propertyeditor.DrugEditor;
import org.openmrs.propertyeditor.EncounterEditor;
import org.openmrs.propertyeditor.LocationEditor;
import org.openmrs.propertyeditor.OrderEditor;
import org.openmrs.propertyeditor.PersonEditor;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.web.WebConstants;
import org.springframework.beans.propertyeditors.CustomBooleanEditor;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

/**
 * This controller gives the backing object and does the saving for the obs.form page. The jsp for
 * this page is located in /web/WEB-INF/view/admin/observations/obsForm.jsp
 */
public class ObsFormController extends SimpleFormController {
	
	/** Logger for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());
	
	/**
	 * Allows for Integers to be used as values in input tags. Normally, only strings and lists are
	 * expected
	 *
	 * @see org.springframework.web.servlet.mvc.BaseCommandController#initBinder(javax.servlet.http.HttpServletRequest,
	 *      org.springframework.web.bind.ServletRequestDataBinder)
	 */
	protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
		super.initBinder(request, binder);
		
		binder.registerCustomEditor(java.lang.Integer.class, new CustomNumberEditor(java.lang.Integer.class, true));
		binder.registerCustomEditor(java.util.Date.class, new CustomDateEditor(Context.getDateFormat(), true));
		binder.registerCustomEditor(java.util.Date.class, "valueDatetime", new CustomDateEditor(Context.getDateTimeFormat(),
		        true));
		binder.registerCustomEditor(java.util.Date.class, "valueTime", new CustomDateEditor(Context.getTimeFormat(), true));
		binder.registerCustomEditor(Location.class, new LocationEditor());
		binder.registerCustomEditor(java.lang.Boolean.class, new CustomBooleanEditor(true)); //allow for an empty boolean value
		binder.registerCustomEditor(Person.class, new PersonEditor());
		binder.registerCustomEditor(Order.class, new OrderEditor());
		binder.registerCustomEditor(Concept.class, new ConceptEditor());
		binder.registerCustomEditor(Location.class, new LocationEditor());
		binder.registerCustomEditor(Encounter.class, new EncounterEditor());
		binder.registerCustomEditor(Drug.class, new DrugEditor());
	}
	
	/**
	 * @see org.springframework.web.servlet.mvc.BaseCommandController#onBind(javax.servlet.http.HttpServletRequest,
	 *      java.lang.Object)
	 */
	@Override
	protected void onBind(HttpServletRequest request, Object command) throws Exception {
		
		Obs obs = (Obs) command;
		
		// set the answer concept if only the answer concept name is set
		if (obs.getValueCoded() == null && obs.getValueCodedName() != null) {
			obs.setValueCoded(obs.getValueCodedName().getConcept());
		}
		
		if (obs.getConcept() != null && obs.getConcept().isComplex()) {
			InputStream complexDataInputStream = setComplexData(obs, request);
			if (complexDataInputStream != null) {
				complexDataInputStream.close();
			}
		}
		
		super.onBind(request, command);
	}
	
	/**
	 * The onSubmit function receives the form/command object that was modified by the input form
	 * and saves it to the db
	 *
	 * @see org.springframework.web.servlet.mvc.SimpleFormController#onSubmit(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse, java.lang.Object,
	 *      org.springframework.validation.BindException)
	 */
	protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object obj,
	        BindException errors) throws Exception {
		
		HttpSession httpSession = request.getSession();
		
		if (Context.isAuthenticated()) {
			Obs obs = (Obs) obj;
			Obs newlySavedObs = null; // to be populated when os.saveObs is called
			ObsService os = Context.getObsService();
			
			try {
				// if the user is just editing the observation
				if (request.getParameter("saveObs") != null) {
					String reason = request.getParameter("editReason");
					if (obs.getObsId() != null && (reason == null || reason.length() == 0)) {
						errors.reject("editReason", "Obs.edit.reason.empty");
						return showForm(request, response, errors);
					}
					
					if (obs.getConcept().isComplex()) {
						if (request instanceof MultipartHttpServletRequest) {
							InputStream complexDataInputStream = setComplexData(obs, request);
							
							// the handler on the obs.concept is called with the given complex data
							newlySavedObs = os.saveObs(obs, reason);
							
							if (complexDataInputStream != null) {
								complexDataInputStream.close();
							}
						}
					} else {
						newlySavedObs = os.saveObs(obs, reason);
					}
					
					httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "Obs.saved");
				}

				// if the user is voiding out the observation
				else if (request.getParameter("voidObs") != null) {
					String voidReason = request.getParameter("voidReason");
					if (obs.getObsId() != null && (voidReason == null || voidReason.length() == 0)) {
						errors.reject("voidReason", "Obs.void.reason.empty");
						return showForm(request, response, errors);
					}
					
					os.voidObs(obs, voidReason);
					httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "Obs.voidedSuccessfully");
				}

				// if this obs is already voided and needs to be unvoided
				else if (request.getParameter("unvoidObs") != null) {
					os.unvoidObs(obs);
					httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "Obs.unvoidedSuccessfully");
				}
				
			}
			catch (IllegalArgumentException e) {
				errors.reject("invalidImage", "Obs.invalidImage");
				return showForm(request, response, errors);
			}
			catch (APIException e) {
				httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, e.getMessage());
				return showForm(request, response, errors);
			}
			
			// redirect to the main encounter page
			if (obs.getEncounter() != null) {
				String view = getSuccessView() + "?encounterId=" + obs.getEncounter().getEncounterId() + "&phrase="
				        + request.getParameter("phrase");
				return new ModelAndView(new RedirectView(view));
			} else {
				return new ModelAndView(new RedirectView("obs.form?obsId=" + newlySavedObs.getObsId()));
			}
		}
		
		return showForm(request, response, errors);
	}
	
	/**
	 * This is called prior to displaying a form for the first time. It tells Spring the
	 * form/command object to load into the request
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
			
			if (obsId != null) {
				obs = os.getObs(Integer.valueOf(obsId));
			} else if (StringUtils.hasText(encounterId)) {
				Encounter e = es.getEncounter(Integer.valueOf(encounterId));
				obs = new Obs();
				obs.setEncounter(e);
				obs.setPerson(e.getPatient());
				obs.setLocation(e.getLocation());
				obs.setObsDatetime(e.getEncounterDatetime());
			}
		}
		
		if (obs == null) {
			obs = new Obs();
		}
		
		return obs;
	}
	
	/**
	 * The other things shown on the obs form that are in the database
	 *
	 * @see org.springframework.web.servlet.mvc.SimpleFormController#referenceData(javax.servlet.http.HttpServletRequest,
	 *      java.lang.Object, org.springframework.validation.Errors)
	 */
	protected Map<String, Object> referenceData(HttpServletRequest request, Object obj, Errors errs) throws Exception {
		
		Obs obs = (Obs) obj;
		
		Map<String, Object> map = new HashMap<String, Object>();
		String defaultVerbose = "false";
		
		if (Context.isAuthenticated()) {
			map.put("forms", Context.getFormService().getAllForms());
			
			if (obs.getConcept() != null) {
				// Reload concept because it can be dettached
				Concept concept = Context.getConceptService().getConcept(obs.getConcept().getConceptId());
				map.put("conceptName", concept.getName(request.getLocale()));
				
				ObsService os = Context.getObsService();
				Integer obsId = obs.getObsId();
				if (obsId != null && obs.getConcept().isComplex()) {
					ComplexObsHandler handler = os.getHandler(obs);
					
					// HTML view (i.e. inlining)
					if (handler.supportsView(ComplexObsHandler.HTML_VIEW)) {
						Obs complexObs_html = handler.getObs(obs, ComplexObsHandler.HTML_VIEW);
						
						Assert.notNull(complexObs_html, "Handler declares HTML view support but fails to deliver");
						
						ComplexData complexData = complexObs_html.getComplexData();
						map.put("htmlView", complexData.getData());
					}
					// Hyperlink view (provide URL)
					if (handler.supportsView(ComplexObsHandler.URI_VIEW)) {
						Obs complexObs_uri = handler.getObs(obs, ComplexObsHandler.URI_VIEW);
						
						Assert.notNull(complexObs_uri, "Handler declares URL view support but fails to deliver");
						
						ComplexData complexData = complexObs_uri.getComplexData();
						map.put("hyperlinkView", complexData.getData());
					}
				}
			}
			
			defaultVerbose = Context.getAuthenticatedUser().getUserProperty(OpenmrsConstants.USER_PROPERTY_SHOW_VERBOSE);
			
		}
		map.put("defaultVerbose", defaultVerbose.equals("true") ? true : false);
		
		String editReason = request.getParameter("editReason");
		if (editReason == null) {
			editReason = "";
		}
		
		map.put("editReason", editReason);
		
		return map;
	}
	
	/**
	 * Sets the value of a complex obs from an http request.
	 *
	 * @param obs the complex obs whose value to set.
	 * @param request the http request.
	 * @return the complex data input stream.
	 */
	private InputStream setComplexData(Obs obs, HttpServletRequest request) throws IOException {
		InputStream complexDataInputStream = null;
		
		if (request instanceof MultipartHttpServletRequest) {
			MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
			MultipartFile complexDataFile = multipartRequest.getFile("complexDataFile");
			if (complexDataFile != null && !complexDataFile.isEmpty()) {
				complexDataInputStream = complexDataFile.getInputStream();
				
				ComplexData complexData = new ComplexData(complexDataFile.getOriginalFilename(), complexDataInputStream);
				
				obs.setComplexData(complexData);
			}
		}
		
		return complexDataInputStream;
	}
}
