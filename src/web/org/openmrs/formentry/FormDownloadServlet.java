package org.openmrs.formentry;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Form;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.web.WebConstants;

public class FormDownloadServlet extends HttpServlet {

	public static final long serialVersionUID = 123423L;

	private Log log = LogFactory.getLog(this.getClass());

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
			log.warn("Invalid parameter for formDownload request (formId='"
					+ request.getParameter("formId") + "', patientId='"
					+ request.getParameter("patientId") + "')", e);
			return ;
		}
		
		Context context = (Context)httpSession.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		if (context == null) {
			httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "auth.session.expired");
			response.sendRedirect(request.getContextPath() + "/logout");
			return;
		}

		Patient patient = context.getFormEntryService().getPatient(patientId);
		Form form = context.getFormEntryService().getForm(formId);
		
		String title = form.getName();
		
		title += " (" + form.getVersion();
		if (form.getBuild() != null)
			title += "-" + form.getBuild();
		title += ")";
		
		title = title.replaceAll(" ", "_");
		
		String url = request.getRequestURL().toString();
		url = url.substring(0, url.lastIndexOf("/"));
		url = url.substring(0, url.lastIndexOf("/"));
		url += "/formentry/forms/";
	
		url += form.getUri();
		
		String xmldoc = new FormXmlTemplateBuilder(context, form, url).getXmlTemplate(patient);
		
		xmldoc = xmldoc.replaceAll("@SESSION@", httpSession.getId());
		
		response.setHeader("Content-Type", "application/ms-infopath.xml");
		response.setHeader("Content-Disposition", "attachment; filename=" + title + ".xml");
		response.getOutputStream().println(xmldoc.toString());
		
	}
	
}

