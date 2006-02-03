package org.openmrs.web;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.openmrs.Form;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.form.FormXmlTemplateBuilder;

public class FormDownloadServlet extends HttpServlet {

	public static final long serialVersionUID = 123423L;

	/**
	 * 
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		Integer formId;
		Integer patientId;
		HttpSession httpSession = request.getSession();
		
		try {
			formId  = Integer.valueOf(request.getParameter("formId"));
			patientId = Integer.valueOf(request.getParameter("patientId"));
		}
		catch (NumberFormatException e) {
			return ;
		}
		
		Context context = (Context)httpSession.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		if (context == null) {
			httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "auth.session.expired");
			response.sendRedirect(request.getContextPath() + "/logout");
			return;
		}
		
		Patient patient = context.getPatientService().getPatient(patientId);
		Form form = context.getFormService().getForm(formId);
		String server = request.getServerName();
		Integer port = request.getServerPort();
		String path = request.getContextPath();
		String url = server + ":" + port + path + "/formentry/forms/";
		String type = "";
		
		if (formId == 14) {
			type = "adultReturn";
			url += "adult_return_form";
		}
		
		String xmldoc = new FormXmlTemplateBuilder(context, form).getXmlTemplate(patient);
		
		
		response.setHeader("Content-Type", "application/ms-infopath.xml");
		response.setHeader("Content-Disposition", "attachment; filename=" + type + ".xml");
		response.getOutputStream().println(xmldoc.toString());
		
	}
	
}

