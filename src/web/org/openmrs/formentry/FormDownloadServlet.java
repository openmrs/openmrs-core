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

/**
 * Provides form download services, including download of the form template to
 * trigger the form application (e.g., Microsoft&reg; InfoPath&trade;) on the
 * client, download of an empty template, and download of a form schema.
 *  
 * @author Ben Wolfe
 * @author Burke Mamlin
 * @version 1.0
 */
public class FormDownloadServlet extends HttpServlet {

	public static final long serialVersionUID = 123423L;

	private Log log = LogFactory.getLog(this.getClass());

	protected void doFormEntryGet(HttpServletRequest request, HttpServletResponse response, 
			Context context, HttpSession httpSession) throws ServletException, IOException {

		Integer formId = null;
		Integer patientId = null;
		
		try {
			formId = Integer.parseInt(request.getParameter("formId"));
			patientId = Integer.parseInt(request.getParameter("patientId"));
		} catch (NumberFormatException e) {
			log.warn("Invalid formId or patientId parameter: formId: \""
					+ request.getParameter("formId") + "\" patientId: "
					+ request.getParameter("patientId") + "\"", e);
			return;
		}

		Patient patient = context.getFormEntryService().getPatient(patientId);
		Form form = context.getFormEntryService().getForm(formId);
		String url = FormUtil.getFormAbsoluteUrl(request.getRequestURL().toString(), form);

		String title = form.getName();
		title += " (" + form.getVersion();
		if (form.getBuild() != null)
			title += "-" + form.getBuild();
		title += ")";
		title = title.replaceAll(" ", "_");

		String xmldoc = new FormXmlTemplateBuilder(context, form, url)
				.getXmlTemplate(patient);

		xmldoc = xmldoc.replaceAll("@SESSION@", httpSession.getId());

		response.setHeader("Content-Type", "application/ms-infopath.xml");
		response.setHeader("Content-Disposition", "attachment; filename="
				+ title + ".infopathxml");
		response.getOutputStream().print(xmldoc.toString());

	}

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		
		Integer formId = null;
		String target = request.getParameter("target");
		HttpSession httpSession = request.getSession();

		Context context = getContext(httpSession);
		if (context == null) {
			httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR,
					"auth.session.expired");
			response.sendRedirect(request.getContextPath() + "/logout");
			return;
		}

		try {
			formId = Integer.parseInt(request.getParameter("formId"));
		} catch (NumberFormatException e) {
			log.warn("Invalid formId parameter: \""
					+ request.getParameter("formId") + "\"", e);
			return;
		}
		
		if (target.equals("formEntry")) {
			doFormEntryGet(request, response, context, httpSession);
		}
		else {
			Form form = context.getFormEntryService().getForm(formId);
			String url = FormUtil.getFormAbsoluteUrl(request.getRequestURL().toString(), form);
			
			String payload;
			String filename;
			if ("schema".equalsIgnoreCase(target)) {
				payload = new FormSchemaBuilder(context, form).getSchema();
				filename = FormEntryConstants.FORMENTRY_DEFAULT_SCHEMA_NAME;
			} else if ("template".equalsIgnoreCase(target)) {
				payload = new FormXmlTemplateBuilder(context, form, url)
						.getXmlTemplate(null);
				payload = payload.replaceAll("@SESSION@", "");
				filename = FormEntryConstants.FORMENTRY_DEFAULT_TEMPLATE_NAME;
			} else {
				log.warn("Invalid template parameter: \"" + target + "\"");
				return;
			}
	
			response.setHeader("Content-Type", "application/ms-infopath.xml");
			response.setHeader("Content-Disposition", "attachment; filename="
					+ filename);
			response.getOutputStream().print(payload);
		}
	}

	private Context getContext(HttpSession httpSession) {
		return (Context) httpSession
				.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
	}
}
