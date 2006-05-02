package org.openmrs.formentry;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
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
import org.openmrs.util.Helper;
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

	protected void doFormEntryGet(HttpServletRequest request,
			HttpServletResponse response, Context context,
			HttpSession httpSession) throws ServletException, IOException {

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
		String url = FormUtil.getFormAbsoluteUrl(form);

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
			// Download from /openmrs/formentry/patientSummary.form (most
			// likely)
			doFormEntryGet(request, response, context, httpSession);
		} else {
			// Downloading from /openmrs/admin/forms/form.form (most likely)
			response.setHeader("Content-Type", "application/ms-infopath.xml");

			// Load form object and default form url
			Form form = context.getFormEntryService().getForm(formId);
			String url = FormUtil.getFormAbsoluteUrl(form);

			// Payload to return if desired form is string conversion capable
			String payload = null;
			if ("schema".equalsIgnoreCase(target)) {
				payload = new FormSchemaBuilder(context, form).getSchema();
				setFilename(response,
						FormEntryConstants.FORMENTRY_DEFAULT_SCHEMA_NAME);
			} else if ("template".equalsIgnoreCase(target)) {
				payload = new FormXmlTemplateBuilder(context, form, url)
						.getXmlTemplate((Patient) null);
				payload = payload.replaceAll("@SESSION@", "");
				setFilename(response,
						FormEntryConstants.FORMENTRY_DEFAULT_TEMPLATE_NAME);
			} else if ("xsn".equalsIgnoreCase(target)) {
				// Download full xsn for editing (if exists)

				// Set the form filename in the response
				String filename = form.getUri();
				log.debug("Download of XSN for form #" + form.getFormId()
						+ " (" + filename + ") requested");

				// generate the filename if they haven't defined a URI
				if (filename == null || filename.equals("")) {
					filename = "";
					if (form.getEncounterType() != null)
						filename = form.getEncounterType().getName() + "-";
					if (form.getVersion() != null)
						filename += form.getVersion();
					if (form.getBuild() != null)
						filename += "-" + form.getBuild();

					if (!filename.equals(""))
						filename += ".xsn";
					else
						// the default download name
						filename = "starter_template.xsn";
				}
				setFilename(response, filename);

				// Find the form file data
				String formDir = FormEntryConstants.FORMENTRY_INFOPATH_OUTPUT_DIR;
				String formFilePath = formDir
						+ (formDir.endsWith(File.separator) ? ""
								: File.separator) + form.getUri();

				FileInputStream formStream = getCurrentXSN(context, form,
						formFilePath);

				if (formStream != null)
					Helper.copyFile(formStream, response.getOutputStream());
				else {
					// the xsn wasn't on the disk. Return the starter xsn
					log.debug("Xsn not found, returning starter xsn");
					FormStarterXSN starter = new FormStarterXSN(context, form,
							url);
					starter.copyXSNToStream(response.getOutputStream());
				}

			} else {
				log.warn("Invalid target parameter: \"" + target + "\"");
				return;
			}

			// If the stream wasn't directly written to, print the payload
			if (payload != null)
				response.getOutputStream().print(payload);
		}
	}

	private void setFilename(HttpServletResponse response, String filename) {
		response.setHeader("Content-Disposition", "attachment; filename="
				+ filename);
	}

	private Context getContext(HttpSession httpSession) {
		return (Context) httpSession
				.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
	}

	private FileInputStream getCurrentXSN(Context context, Form form,
			String formFilePath) throws IOException {
		log.debug("Attempting to open xsn from: " + formFilePath);

		try {
			FileInputStream formStream = new FileInputStream(formFilePath);
		} catch (FileNotFoundException e) {
			log.warn(e);
			return null;
		}

		// Get Constants
		String schemaFilename = FormEntryConstants.FORMENTRY_DEFAULT_SCHEMA_NAME;
		String templateFilename = FormEntryConstants.FORMENTRY_DEFAULT_TEMPLATE_NAME;
		String sampleDataFilename = FormEntryConstants.FORMENTRY_DEFAULT_SAMPLEDATA_NAME;
		String url = FormUtil.getFormAbsoluteUrl(form);

		// Expand the xsn
		File tmpXSN = FormEntryUtil.expandXsn(formFilePath);

		// Generate the schema and template.xml
		String schema = new FormSchemaBuilder(context, form).getSchema();
		String template = new FormXmlTemplateBuilder(context, form, url)
				.getXmlTemplate((Patient) null);
		template = template.replaceAll("@SESSION@", "");

		// Generate and overwrite the schema
		File schemaFile = FormEntryUtil.findFile(tmpXSN, schemaFilename);
		if (schemaFile == null)
			throw new IOException("Schema: '" + schemaFilename
					+ "' cannot be null");
		FileWriter schemaOutput = new FileWriter(schemaFile, false);
		schemaOutput.write(schema);
		schemaOutput.close();

		// replace template.xml with the generated xml
		File templateFile = FormEntryUtil.findFile(tmpXSN, templateFilename);
		if (templateFile == null)
			throw new IOException("Template: '" + templateFilename
					+ "' cannot be null");
		FileWriter templateOutput = new FileWriter(templateFile, false);
		templateOutput.write(template);
		templateOutput.close();

		// replace sampleData.xml with the generated xml
		File sampleDataFile = FormEntryUtil
				.findFile(tmpXSN, sampleDataFilename);
		if (sampleDataFile == null)
			throw new IOException("Template: '" + sampleDataFilename
					+ "' cannot be null");
		FileWriter sampleDataOutput = new FileWriter(sampleDataFile, false);
		sampleDataOutput.write(template);
		sampleDataOutput.close();

		// File tmpOutputDir = FormEntryUtil.createTempDirectory("xsnoutput");
		FormEntryUtil.createDdf(tmpXSN, tmpXSN.getAbsolutePath(), "new.xsn");
		FormEntryUtil.makeCab(tmpXSN);

		File xsn = FormEntryUtil.findFile(tmpXSN, "new.xsn");
		FileInputStream xsnInputStream = new FileInputStream(xsn);

		return xsnInputStream;
	}

}