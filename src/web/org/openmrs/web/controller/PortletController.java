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
package org.openmrs.web.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.DrugOrder;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.Relationship;
import org.openmrs.RelationshipType;
import org.openmrs.User;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.order.RegimenSuggestion;
import org.openmrs.reporting.PatientSet;
import org.openmrs.util.Format;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.web.WebConstants;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

public class PortletController implements Controller {

	protected Log log = LogFactory.getLog(this.getClass());

	/**
	 * This method produces a model containing the following mappings:
	 * 	   (always)
	 * 			(java.util.Date) now
	 *     		(String) size
	 *         	(Locale) locale
	 *     		(other parameters)
	 *     (if there's currently an authenticated user)
	 *         	(User) authenticatedUser
	 *          (PatientSet) myPatientSet (the user's selected patient set, PatientSetService.getMyPatientSet())
	 *     (if the request has a patientId attribute)
	 *     		(Integer) patientId
	 *        	(Patient) patient
	 *         	(Set<Obs>) patientObs
	 *          (Set<Encounter>) patientEncounters
	 *          (Set<DrugOrder>) patientDrugOrders
	 *          (Set<DrugOrder>) currentDrugOrders
	 *          (Set<DrugOrder>) completedDrugOrders
	 *          (Integer) personId
	 *     (if the request has a personId or patientId attribute)
	 *     		(Person) person
	 *          (List<Relationship>) personRelationships
	 *          (Map<RelationshipType, List<Relationship>>) personRelationshipsByType
	 *     (if the request has an encounterId attribute)
	 *     		(Integer) encounterId
	 *         	(Encounter) encounter
	 *         	(Set<Obs>) encounterObs
	 *     (if the request has a userId attribute)
	 *     		(Integer) userId
	 *         	(User) user
	 *     (if the request has a patientIds attribute, which should be a (String) comma-separated list of patientIds)
	 *     		(PatientSet) patientSet
	 *     		(String) patientIds
	 *     (if the request has a conceptIds attribute, which should be a (String) commas-separated list of conceptIds)
	 *     		(Map<Integer, Concept>) conceptMap
	 *     		(Map<String, Concept>) conceptMapByStringIds
	 */
	@SuppressWarnings("unchecked")
	public ModelAndView handleRequest(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		//HttpSession httpSession = request.getSession();
		//

		// find the portlet that was identified in the openmrs:portlet taglib
		Object uri = request.getAttribute("javax.servlet.include.servlet_path");
		String portletPath = "";
		Map<String, Object> model = null;
		{
			HttpSession session = request.getSession();
			String uniqueRequestId = (String) request.getAttribute(WebConstants.INIT_REQ_UNIQUE_ID);
			String lastRequestId = (String) session.getAttribute(WebConstants.OPENMRS_PORTLET_LAST_REQ_ID);
			if (uniqueRequestId.equals(lastRequestId))
				model = (Map<String, Object>) session.getAttribute(WebConstants.OPENMRS_PORTLET_CACHED_MODEL);
			if (model == null) {
				log.debug("creating new portlet model");
				model = new HashMap<String, Object>();
				session.setAttribute(WebConstants.OPENMRS_PORTLET_LAST_REQ_ID, uniqueRequestId);
				session.setAttribute(WebConstants.OPENMRS_PORTLET_CACHED_MODEL, model);
			}
		}
		
		if (uri != null) {
			long timeAtStart = System.currentTimeMillis();
			portletPath = uri.toString();

			// Allowable extensions are '' (no extension) and '.portlet'
			if (portletPath.endsWith("portlet"))
				portletPath = portletPath.replace(".portlet", "");
			else if (portletPath.endsWith("jsp"))
				throw new ServletException("Illegal extension used for portlet: '.jsp'. Allowable extensions are '' (no extension) and '.portlet'");

			log.debug("Loading portlet: " + portletPath);
			
			String id = (String) request.getAttribute("org.openmrs.portlet.id"); 
			String size = (String)request.getAttribute("org.openmrs.portlet.size");
			Map<String, Object> params = (Map<String, Object>)request.getAttribute("org.openmrs.portlet.parameters");
			Map<String, Object> moreParams = (Map<String, Object>) request.getAttribute("org.openmrs.portlet.parameterMap");
			
			model.put("now", new Date());
			model.put("id", id);
			model.put("size", size);
			model.put("locale", Context.getLocale());
			model.putAll(params);
			if (moreParams != null) {
				model.putAll(moreParams);
			}

			// if there's an authenticated user, put them, and their patient set, in the model
			if (Context.getAuthenticatedUser() != null) {
				model.put("authenticatedUser", Context.getAuthenticatedUser());
				model.put("myPatientSet", Context.getPatientSetService().getMyPatientSet());
			}
			
			Integer personId = null;
			
			// if a patient id is available, put patient data documented above in the model
			Object o = request.getAttribute("org.openmrs.portlet.patientId");
			if (o != null) {
				String patientVariation = "";
				Integer patientId = (Integer) o;
				if (!model.containsKey("patient")) {
					// we can't continue if the user can't view patients
					if (Context.hasPrivilege(OpenmrsConstants.PRIV_VIEW_PATIENTS)) {
						Patient p = Context.getPatientService().getPatient(patientId);
						model.put("patient", p);
						
						// add encounters if this user can view them
						if (Context.hasPrivilege(OpenmrsConstants.PRIV_VIEW_ENCOUNTERS))
							model.put("patientEncounters", Context.getEncounterService().getEncounters(p));
						
						if (Context.hasPrivilege(OpenmrsConstants.PRIV_VIEW_OBS))
							model.put("patientObs", Context.getObsService().getObservations(p, false));
						else
							model.put("patientObs", new HashSet<Obs>());
	
						// information about whether or not the patient has exited care
						String reasonForExitText = "";
						String dateOfExitText = "";
						Concept reasonForExitConcept = Context.getConceptService().getConceptByIdOrName(Context.getAdministrationService().getGlobalProperty("concept.reasonExitedCare"));
						if ( reasonForExitConcept != null ) {
							Set<Obs> patientExitObs = Context.getObsService().getObservations(p, reasonForExitConcept, false);
							if ( patientExitObs != null ) {
								log.debug("Exit obs is size " + patientExitObs.size() );
								if ( patientExitObs.size() == 1 ) {
									Obs exitObs = patientExitObs.iterator().next();
									Concept exitReason = exitObs.getValueCoded();
									Date exitDate = exitObs.getObsDatetime();
									if ( exitReason != null && exitDate != null ) {
										reasonForExitText = exitReason.getName(Context.getLocale()).getName();
										dateOfExitText = Format.format(exitDate);
										patientVariation = "Exited";
									}
								} else {
									if ( patientExitObs.size() == 0 ) {
										log.debug("Patient has no reason for exit");
									} else {
										log.error("Too many reasons for exit - not putting data into model");
									}
								}
							}
						}
						model.put("patientReasonForExit", reasonForExitText);
						model.put("patientDateOfExit", dateOfExitText);
						
						if (Context.hasPrivilege(OpenmrsConstants.PRIV_VIEW_ORDERS)) {
							List<DrugOrder> drugOrderList = Context.getOrderService().getDrugOrdersByPatient(p);
							model.put("patientDrugOrders", drugOrderList);
							List<DrugOrder> currentDrugOrders = new ArrayList<DrugOrder>();
							List<DrugOrder> discontinuedDrugOrders = new ArrayList<DrugOrder>();
							for (Iterator<DrugOrder> iter = drugOrderList.iterator(); iter.hasNext(); ) {
								DrugOrder next = iter.next();
								if (next.isCurrent() || next.isFuture()) currentDrugOrders.add(next);
								if (next.isDiscontinued()) discontinuedDrugOrders.add(next); 
							}
							model.put("currentDrugOrders", currentDrugOrders);
							model.put("completedDrugOrders", discontinuedDrugOrders);
					
							List<RegimenSuggestion> standardRegimens = Context.getOrderService().getStandardRegimens();
							if ( standardRegimens != null )
								model.put("standardRegimens", standardRegimens);
						}
						
						if (Context.hasPrivilege(OpenmrsConstants.PRIV_VIEW_PROGRAMS)) {
							model.put("patientPrograms", Context.getProgramWorkflowService().getPatientPrograms(p));
							model.put("patientCurrentPrograms", Context.getProgramWorkflowService().getCurrentPrograms(p, null));
						}

						model.put("patientId", patientId);
						if (p != null) {
							personId = p.getPatientId();
							model.put("personId", personId);
						}
						
						model.put("patientVariation", patientVariation);
					}
				}
			}
			
			// if a person id is available, put person and relationships in the model
			if (personId == null) {
				o = request.getAttribute("org.openmrs.portlet.personId");
				if (o != null) {
					personId = (Integer) o;
					model.put("personId", personId);
				}
			}
			if (personId != null) {
				if (!model.containsKey("person")) {
					Person p = (Person) model.get("patient");
					if (p == null)
						p = Context.getPersonService().getPerson(personId);
					model.put("person", p);
					
					if (Context.hasPrivilege(OpenmrsConstants.PRIV_MANAGE_RELATIONSHIPS)) {
						List<Relationship> relationships = new ArrayList<Relationship>();
						relationships.addAll(Context.getPersonService().getRelationships(p, false));
						Map<RelationshipType, List<Relationship>> relationshipsByType = new HashMap<RelationshipType, List<Relationship>>();
						for (Relationship rel : relationships) {
							List<Relationship> list = relationshipsByType.get(rel.getRelationshipType());
							if (list == null) {
								list = new ArrayList<Relationship>();
								relationshipsByType.put(rel.getRelationshipType(), list);
							}
							list.add(rel);
						}
						
						model.put("personRelationships", relationships);
						model.put("personRelationshipsByType", relationshipsByType);
					}
				}
			}
			
			// if an encounter id is available, put "encounter" and "encounterObs" in the model
			o = request.getAttribute("org.openmrs.portlet.encounterId");
			if (o != null && !model.containsKey("encounterId")) {
				if (!model.containsKey("encounter")) {
					if (Context.hasPrivilege(OpenmrsConstants.PRIV_VIEW_ENCOUNTERS)) {
						Encounter e = Context.getEncounterService().getEncounter((Integer) o);
						model.put("encounter", e);
						if (Context.hasPrivilege(OpenmrsConstants.PRIV_VIEW_OBS))
							model.put("encounterObs", Context.getObsService().getObservations(e));
					}
					model.put("encounterId", (Integer) o);
				}
			}
			
			// if a user id is available, put "user" in the model
			o = request.getAttribute("org.openmrs.portlet.userId");
			if (o != null) {
				if (!model.containsKey("user")) {
					if (Context.hasPrivilege(OpenmrsConstants.PRIV_VIEW_USERS)) {
						User u = Context.getUserService().getUser((Integer) o);
						model.put("user", u);
					}
					model.put("userId", (Integer) o);
				}
			}
			
			// if a list of patient ids is available, make a patientset out of it
			o = request.getAttribute("org.openmrs.portlet.patientIds");
			if (o != null && !"".equals(o) && !model.containsKey("patientIds")) {
				if (!model.containsKey("patientSet")) {
					PatientSet ps = PatientSet.parseCommaSeparatedPatientIds((String) o);
					model.put("patientSet", ps);
					model.put("patientIds", (String) o);
				}
			}
			
			o = model.get("conceptIds");
			if (o != null && !"".equals(o)) {
				if (!model.containsKey("conceptMap")) {
					log.debug("Found conceptIds parameter: " + o);
					Map<Integer, Concept> concepts = new HashMap<Integer, Concept>();
					Map<String, Concept> conceptsByStringIds = new HashMap<String, Concept>();
					String conceptIds = (String) o;
					ConceptService cs = Context.getConceptService();
					String[] ids = conceptIds.split(",");
					for (String cId : ids) {
						try {
							Integer i = Integer.valueOf(cId);
							Concept c = cs.getConcept(i);
							concepts.put(i, c);
							conceptsByStringIds.put(i.toString(), c);
						} catch (Exception ex) { }
					}
					model.put("conceptMap", concepts);
					model.put("conceptMapByStringIds", conceptsByStringIds);
				}
			}
			
			populateModel(request, model);
			log.debug(portletPath + " took " + (System.currentTimeMillis() - timeAtStart) + " ms");
		}

		return new ModelAndView(portletPath, "model", model);

	}
	
	/**
	 * Subclasses should override this to put more data into the model.
	 * This will be called AFTER handleRequest has put mappings in the model as described in its javadoc.
	 * Note that context could be null when this method is called.  
	 */
	protected void populateModel(HttpServletRequest request, Map<String, Object> model) { }
	
}
