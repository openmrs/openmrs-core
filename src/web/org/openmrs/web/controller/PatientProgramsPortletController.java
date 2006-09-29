package org.openmrs.web.controller;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.openmrs.Program;
import org.openmrs.api.context.Context;

public class PatientProgramsPortletController extends PortletController {

	@SuppressWarnings("unchecked")
	protected void populateModel(HttpServletRequest request, Map model) {
		List<Program> programs = Context.getProgramWorkflowService().getPrograms();
		model.put("programs", programs);
	}

}
