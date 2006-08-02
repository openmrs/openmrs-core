package org.openmrs.web.controller.program;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.PatientProgram;
import org.openmrs.Program;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.web.WebConstants;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.view.RedirectView;

public class PatientProgramFormController implements Controller {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
		// can't do anything without a method
		return null;
	}
	
	public ModelAndView enroll(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession httpSession = request.getSession();
		Context context = (Context) httpSession.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		
		if (context == null) {
			httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "auth.session.expired");
			response.sendRedirect(request.getContextPath() + "/logout");
			return null;
		}
		
		String returnPage = request.getParameter("returnPage");
		if (returnPage == null) {
			throw new IllegalArgumentException("must specify a returnPage parameter in a call to enroll()");
		}
		
		String patientIdStr = request.getParameter("patientId");
		String programIdStr = request.getParameter("programId");
		String enrollmentDateStr = request.getParameter("dateEnrolled");
		
		log.debug("enroll " + patientIdStr + " in " + programIdStr + " on " + enrollmentDateStr);
		
		// make sure we parse dates the same was as if we were using the initBinder + property editor method 
		CustomDateEditor cde = new CustomDateEditor(new SimpleDateFormat(OpenmrsConstants.OPENMRS_LOCALE_DATE_PATTERNS().get(context.getLocale().toString().toLowerCase()), context.getLocale()), true, 10);
		cde.setAsText(enrollmentDateStr);
		Date enrollmentDate = (Date) cde.getValue();
		Patient patient = context.getPatientService().getPatient(Integer.valueOf(patientIdStr));
		Program program = context.getProgramWorkflowService().getProgram(Integer.valueOf(programIdStr));
		context.getProgramWorkflowService().enrollPatientInProgram(patient, program, enrollmentDate);

		return new ModelAndView(new RedirectView(returnPage));
	}
	
	public ModelAndView complete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession httpSession = request.getSession();
		Context context = (Context) httpSession.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		
		if (context == null) {
			httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "auth.session.expired");
			response.sendRedirect(request.getContextPath() + "/logout");
			return null;
		}
		
		String returnPage = request.getParameter("returnPage");
		if (returnPage == null) {
			throw new IllegalArgumentException("must specify a returnPage parameter in a call to enroll()");
		}
		
		String patientProgramIdStr = request.getParameter("patientProgramId");
		String dateCompletedStr = request.getParameter("dateCompleted");
		
		// make sure we parse dates the same was as if we were using the initBinder + property editor method 
		CustomDateEditor cde = new CustomDateEditor(new SimpleDateFormat(OpenmrsConstants.OPENMRS_LOCALE_DATE_PATTERNS().get(context.getLocale().toString().toLowerCase()), context.getLocale()), true, 10);
		cde.setAsText(dateCompletedStr);
		Date dateCompleted = (Date) cde.getValue();

		PatientProgram p = context.getProgramWorkflowService().getPatientProgram(Integer.valueOf(patientProgramIdStr));
		p.setDateCompleted(dateCompleted);
		context.getProgramWorkflowService().updatePatientProgram(p);

		return new ModelAndView(new RedirectView(returnPage));
	}

}
