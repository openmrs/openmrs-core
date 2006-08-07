package org.openmrs.web.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.DrugOrder;
import org.openmrs.Encounter;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.Relationship;
import org.openmrs.RelationshipType;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.reporting.PatientSet;
import org.openmrs.web.WebConstants;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

public class PortletController implements Controller {

	protected Log log = LogFactory.getLog(this.getClass());

	/**
	 * This method produces a model containing the following mappings:
	 * 	   (always)
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
	 *          (List<Relationship>) patientRelationships
	 *          (Map<RelationshipType, List<Relationship>>) patientRelationshipsByType
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
	 */
	@SuppressWarnings("unchecked")
	public ModelAndView handleRequest(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		HttpSession httpSession = request.getSession();
		Context context = (Context) httpSession.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		
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
			
			model.put("id", id);
			model.put("size", size);
			model.putAll(params);
			if (moreParams != null) {
				model.putAll(moreParams);
			}
			
			if (context != null) {
				model.put("authenticatedUser", context.getAuthenticatedUser());
				model.put("locale", context.getLocale());
				
				// if a patient id is available, put "patient" and "patientObs" in the request
				Object o = request.getAttribute("org.openmrs.portlet.patientId");
				if (o != null) {
					Patient p = context.getPatientService().getPatient((Integer) o);
					model.put("patient", p);
					model.put("patientObs", context.getObsService().getObservations(p));
					model.put("patientEncounters", context.getEncounterService().getEncounters(p));
					Set<DrugOrder> drugOrders = new HashSet<DrugOrder>();
					drugOrders.addAll(context.getOrderService().getDrugOrdersByPatient(p));
					model.put("patientDrugOrders", drugOrders);
					model.put("patientPrograms", context.getProgramWorkflowService().getPatientPrograms(p));
					model.put("patientCurrentPrograms", context.getProgramWorkflowService().getCurrentPrograms(p, null));
					List<Relationship> relationships = new ArrayList<Relationship>();
					relationships.addAll(context.getPatientService().getRelationships(new Person(p)));
					Map<RelationshipType, List<Relationship>> relationshipsByType = new HashMap<RelationshipType, List<Relationship>>();
					for (Relationship rel : relationships) {
						List<Relationship> list = relationshipsByType.get(rel.getRelationship());
						if (list == null) {
							list = new ArrayList<Relationship>();
							relationshipsByType.put(rel.getRelationship(), list);
						}
						list.add(rel);
					}
					model.put("patientRelationships", relationships);
					model.put("patientRelationshipsByType", relationshipsByType);
					model.put("patientId", (Integer) o);
				}
				
				// if an encounter id is available, put "encounter" and "encounterObs" in the request
				o = request.getAttribute("org.openmrs.portlet.encounterId");
				if (o != null) {
					Encounter e = context.getEncounterService().getEncounter((Integer) o);
					model.put("encounter", e);
					model.put("encounterObs", context.getObsService().getObservations(e));
					model.put("encounterId", (Integer) o);
				}
				
				// if a user id is available, put "user" in the model
				o = request.getAttribute("org.openmrs.portlet.userId");
				if (o != null) {
					User u = context.getUserService().getUser((Integer) o);
					model.put("user", u);
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
				
			}

			populateModel(request, context, model);
		}

		return new ModelAndView(portletPath, "model", model);

	}
	
	/**
	 * Subclasses should override this to put more data into the model.
	 * This will be called AFTER handleRequest has put mappings in the model as described in its javadoc.
	 * Note that context could be null when this method is called.  
	 */
	protected void populateModel(HttpServletRequest request, Context context, Map<String, Object> model) { }
	
}
