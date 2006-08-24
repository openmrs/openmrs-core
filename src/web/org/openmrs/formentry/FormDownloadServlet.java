package org.openmrs.formentry;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
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
		String url = FormEntryUtil.getFormAbsoluteUrl(form);

		String title = form.getName() + "(" + FormEntryUtil.getFormUriWithoutExtension(form) + ")";
		title = title.replaceAll(" ", "_");

		// Set up a VelocityContext in which to evaluate the template's default values
		try {
			Velocity.init();
		} catch (Exception e) {
			log.error("Error initializing Velocity engine", e);
		}
		VelocityContext velocityContext = new VelocityContext();
		velocityContext.put("form", form);
		velocityContext.put("url", url);
		User user = context.getAuthenticatedUser();
		String enterer;
		if (user != null)
			enterer = user.getUserId() + "^" + user.getFirstName() + " "
					+ user.getLastName();
		else
			enterer = "";
		String dateEntered = FormUtil.dateToString(new Date());
		velocityContext.put("enterer", enterer);
		velocityContext.put("dateEntered", dateEntered);
		velocityContext.put("patient", patient);
		velocityContext.put("timestamp", new SimpleDateFormat(
				"yyyyMMdd'T'HH:mm:ss.SSSZ"));
		velocityContext.put("date", new SimpleDateFormat("yyyyMMdd"));
		velocityContext.put("time", new SimpleDateFormat("HH:mm:ss"));
		velocityContext.put("sessionId", httpSession.getId());

		String template = form.getTemplate();
		// just in case template has not been assigned, generate it on the fly
		if (template == null)
			template = new FormXmlTemplateBuilder(context, form, url)
				.getXmlTemplate(true);
		
		String xmldoc = null;
		try {
			StringWriter w = new StringWriter();
			Velocity.evaluate(velocityContext, w, this.getClass().getName(),
					template);
			xmldoc = w.toString();
		} catch (Exception e) {
			log.error("Error evaluating default values for form "
					+ form.getName() + "[" + form.getFormId() + "]", e);
		}

		response.setHeader("Content-Type", "application/ms-infopath.xml");
		response.setHeader("Content-Disposition", "attachment; filename="
				+ title + ".infopathxml");
		response.getOutputStream().print(xmldoc);
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
			String url = FormEntryUtil.getFormAbsoluteUrl(form);

			// Payload to return if desired form is string conversion capable
			String payload = null;
			if ("schema".equalsIgnoreCase(target)) {
				payload = new FormSchemaBuilder(context, form).getSchema();
				setFilename(response,
						FormEntryConstants.FORMENTRY_DEFAULT_SCHEMA_NAME);
			} else if ("template".equalsIgnoreCase(target)) {
				payload = new FormXmlTemplateBuilder(context, form, url)
						.getXmlTemplate(false);
				// payload = payload.replaceAll("@SESSION@", "");
				setFilename(response,
						FormEntryConstants.FORMENTRY_DEFAULT_TEMPLATE_NAME);
			} else if ("xsn".equalsIgnoreCase(target)) {
				// Download full xsn for editing (if exists)

				// Set the form filename in the response
				String filename = FormEntryUtil.getFormUri(form);
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

					if (!filename.equals("") && !filename.toLowerCase().endsWith(".xsn"))
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
								: File.separator) + FormEntryUtil.getFormUri(form);

				FileInputStream formStream = getCurrentXSN(context, form,
						formFilePath);

				if (formStream != null)
					OpenmrsUtil.copyFile(formStream, response.getOutputStream());
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

		if (!new File(formFilePath).exists())
			return null;
//		try {
//			FileInputStream formStream = new FileInputStream(formFilePath);
//		} catch (FileNotFoundException e) {
//			log.warn(e);
//			return null;
//		}

		// Get Constants
		String schemaFilename = FormEntryConstants.FORMENTRY_DEFAULT_SCHEMA_NAME;
		String templateFilename = FormEntryConstants.FORMENTRY_DEFAULT_TEMPLATE_NAME;
		String sampleDataFilename = FormEntryConstants.FORMENTRY_DEFAULT_SAMPLEDATA_NAME;
		String defaultsFilename = FormEntryConstants.FORMENTRY_DEFAULT_DEFAULTS_NAME;
		String url = FormEntryUtil.getFormAbsoluteUrl(form);

		// Expand the xsn
		File tmpXSN = FormEntryUtil.expandXsn(formFilePath);

		// Generate the schema and template.xml
		FormXmlTemplateBuilder fxtb = new FormXmlTemplateBuilder(context, form, url);
		String template = fxtb.getXmlTemplate(false);
		String templateWithDefaultScripts = fxtb.getXmlTemplate(true);
		String schema = new FormSchemaBuilder(context, form).getSchema();
		
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

		// replace defautls.xml with the xml template, including default scripts
		File defaultsFile = FormEntryUtil.findFile(tmpXSN, defaultsFilename);
		if (defaultsFile == null)
			throw new IOException("Defaults: '" + defaultsFilename + "' cannot be null");
		FileWriter defaultsOutput = new FileWriter(defaultsFile, false);
		defaultsOutput.write(templateWithDefaultScripts);
		defaultsOutput.close();

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
		// TODO Refactored (jmiranda)
		//FormEntryUtil.createDdf(tmpXSN, tmpXSN.getAbsolutePath(), "new.xsn");
		FormEntryUtil.makeCab(tmpXSN, tmpXSN.getAbsolutePath(), "new.xsn");

		File xsn = FormEntryUtil.findFile(tmpXSN, "new.xsn");
		FileInputStream xsnInputStream = new FileInputStream(xsn);

		return xsnInputStream;
	}

}