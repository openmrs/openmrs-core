package org.openmrs.formentry;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.openmrs.Form;
import org.openmrs.Patient;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsUtil;
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
			HttpSession httpSession) throws ServletException, IOException {
		
		Integer formId = null;
		Integer patientId = null;

		try {
			formId = Integer.parseInt(request.getParameter("formId"));
			patientId = Integer.parseInt(request.getParameter("patientId"));
		}
		catch (NumberFormatException e) {
			log.warn("Invalid formId or patientId parameter: formId: \""
			    + request.getParameter("formId") + "\" patientId: "
			    + request.getParameter("patientId") + "\"", e);
			return;
		}

		Patient patient = Context.getFormEntryService().getPatient(patientId);
		Form form = Context.getFormEntryService().getForm(formId);
		String url = FormEntryUtil.getFormAbsoluteUrl(form);

		String title = form.getName() + "(" + FormEntryUtil.getFormUriWithoutExtension(form) + ")";
		title = title.replaceAll(" ", "_");

		// Set up a VelocityContext in which to evaluate the template's default
		// values
		try {
			Velocity.init();
		}
		catch (Exception e) {
			log.error("Error initializing Velocity engine", e);
		}
		VelocityContext velocityContext = new VelocityContext();
		velocityContext.put("form", form);
		velocityContext.put("url", url);
		User user = Context.getAuthenticatedUser();
		String enterer;
		if (user != null)
			enterer = user.getUserId() + "^" + user.getFirstName() + " " + user.getLastName();
		else
			enterer = "";
		String dateEntered = FormUtil.dateToString(new Date());
		velocityContext.put("enterer", enterer);
		velocityContext.put("dateEntered", dateEntered);
		velocityContext.put("patient", patient);
		velocityContext.put("timestamp", new SimpleDateFormat("yyyyMMdd'T'HH:mm:ss.SSSZ"));
		velocityContext.put("date", new SimpleDateFormat("yyyyMMdd"));
		velocityContext.put("time", new SimpleDateFormat("HH:mm:ss"));
		velocityContext.put("sessionId", httpSession.getId());

		String template = form.getTemplate();
		// just in case template has not been assigned, generate it on the fly
		if (template == null)
			template = new FormXmlTemplateBuilder(form, url).getXmlTemplate(true);

		String xmldoc = null;
		try {
			StringWriter w = new StringWriter();
			Velocity.evaluate(velocityContext, w, this.getClass().getName(), template);
			xmldoc = w.toString();
		}
		catch (Exception e) {
			log.error("Error evaluating default values for form " + form.getName() + "["
			    + form.getFormId() + "]", e);
			throw new ServletException("Error while evaluating velocity defaults", e);
		}

		// set up keepalive for formentry 
		// first remove a pre-existing keepalive
		// it's ok if they are working with multiple forms, too
		if ( httpSession.getAttribute(WebConstants.OPENMRS_DYNAMIC_FORM_KEEPALIVE) != null ) {
			httpSession.removeAttribute(WebConstants.OPENMRS_DYNAMIC_FORM_KEEPALIVE);
		}
		
		httpSession.setAttribute(WebConstants.OPENMRS_DYNAMIC_FORM_KEEPALIVE, new Date());
		
		response.setHeader("Content-Type", "application/ms-infopath.xml; charset=utf-8");
		response.setHeader("Content-Disposition", "attachment; filename=" + title + ".infopathxml");
		response.getOutputStream().print(xmldoc);
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
	    throws ServletException, IOException {

		Integer formId = null;
		String target = request.getParameter("target");
		HttpSession httpSession = request.getSession();

		if (Context.isAuthenticated() == false) {
			httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "auth.session.expired");
			response.sendRedirect(request.getContextPath() + "/logout");
			return;
		}
		
		try {
			formId = Integer.parseInt(request.getParameter("formId"));
		}
		catch (NumberFormatException e) {
			log.warn("Invalid formId parameter: \"" + request.getParameter("formId") + "\"", e);
			return;
		}

		if ("formEntry".equals(target)) {

			// Download from /openmrs/formentry/patientSummary.form (most
			// likely)
			doFormEntryGet(request, response, httpSession);

		}
		else if ("rebuild".equals(target)) {

			// Download the XSN and Upload it again
			Form form = Context.getFormEntryService().getForm(formId);
			InputStream formStream = FormEntryUtil.getCurrentXSN(form);
			if (formStream == null)
				response.sendError(500);
			
			PublishInfoPath.publishXSN(formStream);
			httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "Form.rebuildXSN.success");
			response.sendRedirect(request.getHeader("referer"));

		}
		else {
			// Downloading from openmrs/admin/forms/form(Edit|SchemaDesign).form
			response.setHeader("Content-Type", "application/octect-stream; charset=utf-8");

			// Load form object and default form url
			Form form = Context.getFormEntryService().getForm(formId);
			String url = FormEntryUtil.getFormAbsoluteUrl(form);

			// Payload to return if desired form is string conversion capable
			String payload = null;
			if ("schema".equalsIgnoreCase(target)) {
				payload = new FormSchemaBuilder(form).getSchema();
				setFilename(response, FormEntryConstants.FORMENTRY_DEFAULT_SCHEMA_NAME);
			}
			else if ("template".equalsIgnoreCase(target)) {
				payload = new FormXmlTemplateBuilder(form, url).getXmlTemplate(false);
				setFilename(response, FormEntryConstants.FORMENTRY_DEFAULT_TEMPLATE_NAME);
			}
			else if ("xsn".equalsIgnoreCase(target)) {
				// Download full xsn for editing (if exists) Otherwise, get
				// starter XSN. Inserts new template and schema

				// Set the form filename in the response
				String filename = FormEntryUtil.getFormUri(form);
				log.debug("Download of XSN for form #" + form.getFormId() + " (" + filename
				    + ") requested");

				// generate the filename if they haven't defined a URI
				if (filename == null || filename.equals(""))
					filename = "starter_template.xsn";

				setFilename(response, filename);

				FileInputStream formStream = FormEntryUtil.getCurrentXSN(form);

				if (formStream != null)
					OpenmrsUtil.copyFile(formStream, response.getOutputStream());
				else {
					log.error("Could not return an xsn");
					response.sendError(500);
				}

			}
			else if (target == null) {
				// Download full xsn for formentry (if exists)
				// Does not alter the xsn at all
				try {
					FileInputStream formStream = new FileInputStream(url);
					OpenmrsUtil.copyFile(formStream, response.getOutputStream());
				}
				catch (FileNotFoundException e) {
					log
					    .error(
					    	"The XSN for form '"
					            + form.getFormId()
					            + "' cannot be found.  More than likely the XSN has not been uploaded (via Upload XSN in form administration).",
					        e);
				}

			}
			else {
				log.warn("Invalid target parameter: \"" + target + "\"");
				return;
			}

			// If the stream wasn't directly written to, print the payload
			if (payload != null)
				response.getOutputStream().print(payload);
		}
	}

	private void setFilename(HttpServletResponse response, String filename) {
		response.setHeader("Content-Disposition", "attachment; filename=" + filename);
	}
}
