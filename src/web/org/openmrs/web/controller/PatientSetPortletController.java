package org.openmrs.web.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.openmrs.api.context.Context;
import org.openmrs.reporting.PatientSet;
import org.openmrs.web.WebConstants;

public class PatientSetPortletController extends PortletController {

	@SuppressWarnings("unchecked")
	protected void populateModel(HttpServletRequest request, Context context, Map model) {
		HttpSession httpSession = request.getSession();
		if (httpSession != null) {
			PatientSet patientSet = (PatientSet) httpSession.getAttribute(WebConstants.OPENMRS_PATIENT_SET_ATTR);
			if (patientSet == null) {
				patientSet = new PatientSet();
			}
			model.put("patientSet", patientSet);
		}
	}
	
}
