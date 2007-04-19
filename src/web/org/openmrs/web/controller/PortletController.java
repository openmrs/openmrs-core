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
import java.util.TreeSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.DrugOrder;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.Relationship;
import org.openmrs.RelationshipType;
import org.openmrs.User;
import org.openmrs.api.ConceptService;
import org.openmrs.api.ObsService;
import org.openmrs.api.context.Context;
import org.openmrs.order.RegimenSuggestion;
import org.openmrs.reporting.PatientSet;
import org.openmrs.util.Format;
import org.openmrs.util.OpenmrsConstants;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

public class PortletController implements Controller {

	protected Log log = LogFactory.getLog(this.getClass());

	/**
	 * This method produces a model containing the following mappings:
	 * 	   (always)
	 * 			(java.util.Date) now
	 *     		(String) size
	 *     		(other parameters)
	 *     (if there's currently an authenticated user)
	 *         	(User) authenticatedUser
	 *         	(Locale) locale
	 *     (if the request has a patientId attribute)
	 *     		(Integer) patientId
	 *        	(Patient) patient
	 *         	(Set<Obs>) patientObs
	 *          (Set<Encounter>) patientEncounters
	 *          (Set<DrugOrder>) patientDrugOrders
	 *          (Set<DrugOrder>) currentDrugOrders
	 *          (Set<DrugOrder>) completedDrugOrders
	 *          (List<Relationship>) patientRelationships
	 *          (Map<RelationshipType, List<Relationship>>) patientRelationshipsByType
	 *          (Integer) personId
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
		Map<String, Object> model = new HashMap<String, Object>();
		
		if (uri != null) {
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
			model.putAll(params);
			if (moreParams != null) {
				model.putAll(moreParams);
			}
				
			model.put("authenticatedUser", Context.getAuthenticatedUser());
			model.put("locale", Context.getLocale());
			
			// if a patient id is available, put "patient" and "patientObs" in the request
			Object o = request.getAttribute("org.openmrs.portlet.patientId");
			if (o != null) {
				String patientVariation = "";
				Integer patientId = (Integer) o;
				// we can't continue if the user can't view patients
				if (Context.hasPrivilege(OpenmrsConstants.PRIV_VIEW_PATIENTS)) {
					Patient p = Context.getPatientService().getPatient(patientId);
					model.put("patient", p);
					
					// add encounters if this user can view them
					if (Context.hasPrivilege(OpenmrsConstants.PRIV_VIEW_ENCOUNTERS))
						model.put("patientEncounters", Context.getEncounterService().getEncounters(p));
					
					if (Context.hasPrivilege(OpenmrsConstants.PRIV_VIEW_OBS))
						model.put("patientObs", Context.getObsService().getObservations(p));
					else
						model.put("patientObs", new HashSet<Obs>());

					// information about whether or not the patient has exited care
					String reasonForExitText = "";
					String dateOfExitText = "";
					Concept reasonForExitConcept = Context.getConceptService().getConceptByIdOrName(Context.getAdministrationService().getGlobalProperty("concept.reasonExitedCare"));
					if ( reasonForExitConcept != null ) {
						Set<Obs> patientExitObs = Context.getObsService().getObservations(p, reasonForExitConcept);
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
						
						model.put("patientRelationships", relationships);
						model.put("patientRelationshipsByType", relationshipsByType);
						model.put("patientId", patientId);
						if (p != null)
							model.put("personId", p.getPatientId());
					}
					
					model.put("patientVariation", patientVariation);
				}
			}
			
			// if an encounter id is available, put "encounter" and "encounterObs" in the request
			o = request.getAttribute("org.openmrs.portlet.encounterId");
			if (o != null) {
				if (Context.hasPrivilege(OpenmrsConstants.PRIV_VIEW_ENCOUNTERS)) {
					Encounter e = Context.getEncounterService().getEncounter((Integer) o);
					model.put("encounter", e);
					if (Context.hasPrivilege(OpenmrsConstants.PRIV_VIEW_OBS))
						model.put("encounterObs", Context.getObsService().getObservations(e));
				}
				model.put("encounterId", (Integer) o);
			}
			
			// if a user id is available, put "user" in the model
			o = request.getAttribute("org.openmrs.portlet.userId");
			if (o != null) {
				if (Context.hasPrivilege(OpenmrsConstants.PRIV_VIEW_USERS)) {
					User u = Context.getUserService().getUser((Integer) o);
					model.put("user", u);
				}
				model.put("userId", (Integer) o);
			}
			
			// if a list of patient ids is available, make a patientset out of it
			o = request.getAttribute("org.openmrs.portlet.patientIds");
			if (o != null && !"".equals(o)) {
				log.debug("Found patientIds attribute: " + o);
				PatientSet ps = PatientSet.parseCommaSeparatedPatientIds((String) o);
				model.put("patientSet", ps);
				model.put("patientIds", (String) o);
			}
			
			o = model.get("conceptIds");
			if (o != null && !"".equals(o)) {
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

			// TODO: This should check a different privilege
			// TODO: If we really need to do this we should write a service method getAnswerFrequency() or something
			if (Context.hasPrivilege(OpenmrsConstants.PRIV_MANAGE_RELATIONSHIPS)) {
				//String arvGroups =  (String)Context.getAdministrationService().getGlobalProperty("arv_groups");
				List<Obs> treatmentGroupObs = Context.getObsService().getObservations(Context.getConceptService().getConceptByName("ANTIRETROVIRAL TREATMENT GROUP"), null, ObsService.PATIENT);
				if ( treatmentGroupObs != null ) {
					TreeSet<String> treatmentGroupSet = new TreeSet<String>();
					log.debug("tgo is size " + treatmentGroupObs.size());
					for ( Obs ob : treatmentGroupObs ) {
						String group = ob.getValueText();
						if ( group != null ) {
							if ( group.length() > 0 ) {
								// hack to order items properly
								if ( group.length() == 1 ) group = "0" + group;
								treatmentGroupSet.add(group);
							}
						}
					}

					String arvGroups = "";

					for ( String s : treatmentGroupSet ) {
						if ( arvGroups.length() > 0 ) arvGroups += ",";
						if ( s.startsWith("0")) s = s.substring(1);
						arvGroups += s;
					}

					model.put("arvGroups", arvGroups);
				} else {
					log.debug("tgo is null");
				}
			}
			
			populateModel(request, model);
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
