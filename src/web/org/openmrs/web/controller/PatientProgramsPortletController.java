package org.openmrs.web.controller;

import java.util.List;
import java.util.Map;

import org.openmrs.Program;
import org.openmrs.api.context.Context;


public class PatientProgramsPortletController extends PortletController {

	@SuppressWarnings("unchecked")
	protected void populateModel(Context context, Map model) {
		if (context != null) {
			List<Program> programs = context.getProgramWorkflowService().getPrograms();
			model.put("programs", programs);
		}
	}

}
