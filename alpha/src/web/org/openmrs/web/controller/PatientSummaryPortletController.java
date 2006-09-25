package org.openmrs.web.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.openmrs.api.context.Context;
import org.openmrs.summary.PatientSummarySpecification;

public class PatientSummaryPortletController extends PortletController {

	protected void populateModel(HttpServletRequest request, Context context, Map model) {
		if (context != null) {
			model.put("patientSummarySpecification", PatientSummarySpecification.getInstance());
		}
	}
	
}
