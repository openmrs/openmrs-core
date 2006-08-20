package org.openmrs.web.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.openmrs.api.context.Context;
import org.openmrs.reporting.PatientAnalysis;
import org.openmrs.reporting.PatientSet;

public class PatientSetPortletController extends PortletController {

	@SuppressWarnings("unchecked")
	protected void populateModel(HttpServletRequest request, Context context, Map<String, Object> model) {
		String myAnalysis = (String) model.get("myAnalysis");
		String attrToUse = (String) model.get("fromAttribute");
		log.debug("model.fromAttribute = " + model.get("fromAttribute"));
		HttpSession httpSession = request.getSession();
		if (httpSession != null) {
			PatientSet patientSet = null;
			
			if (myAnalysis != null) {
				patientSet = context.getPatientSetService().getMyPatientAnalysis().runFilters(context, null);
			} else if (attrToUse != null) {
				Object o = httpSession.getAttribute(attrToUse);
				if (model.get("mutable") != null) {
					model.put("mutable", model.get("mutable").toString().toLowerCase().startsWith("t"));
				} else {
					model.put("mutable", Boolean.FALSE);
				}
				if (model.get("droppable") != null) {
					model.put("droppable", model.get("droppable").toString().toLowerCase().startsWith("t"));
				} else {
					model.put("droppable", Boolean.FALSE);
				}
				
				if (o instanceof PatientSet) {
					patientSet = (PatientSet) o;
				} else if (o instanceof PatientAnalysis && context != null) {
					patientSet = ((PatientAnalysis) o).runFilters(context, null);
				} else if (o == null) {
					log.debug("attribute " + attrToUse + " is null");
				} else {
					log.debug("Don't know how to handle " + o.getClass());
				}
			} else {
				// use PatientSetService.
				model.put("patientSetSize", context.getPatientSetService().getMyPatientSet().size());
			}
			model.put("patientSet", patientSet);
		}
	}

}
