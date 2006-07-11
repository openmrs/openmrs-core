package org.openmrs.web.servlet;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.openmrs.api.context.Context;
import org.openmrs.reporting.PatientSet;
import org.openmrs.reporting.ReportService;
import org.openmrs.reporting.export.DataExportReportObject;
import org.openmrs.reporting.export.DataExportUtility;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.web.WebConstants;

public class DataExportServlet extends HttpServlet {

	public static final long serialVersionUID = 1231222L;
	private Log log = LogFactory.getLog(this.getClass());
	
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		
		String reportId = request.getParameter("dataExportId");
		String[] patientIds = request.getParameterValues("patientId");
		HttpSession session = request.getSession();
		Context context = (Context)session.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		
		if (reportId == null || reportId.length()==0 ) {
			session.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "error.null");
			return;
		}
		if (context == null || !context.hasPrivilege(OpenmrsConstants.PRIV_VIEW_PATIENTS)) {
			session.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "Privilege required: " + OpenmrsConstants.PRIV_VIEW_PATIENTS);
			session.setAttribute(WebConstants.OPENMRS_LOGIN_REDIRECT_HTTPSESSION_ATTR, request.getRequestURI() + "?" + request.getQueryString());
			response.sendRedirect(request.getContextPath() + "/login.htm");
			return;
		}
		
		try {
			Velocity.init();
		} catch (Exception e) {
			log.error("Error initializing Velocity engine", e);
		}
		
		ReportService rs = context.getReportService();
		DataExportReportObject dataExport = (DataExportReportObject)rs.getReportObject(Integer.valueOf(reportId));
		
		response.setContentType("application/vnd.ms-excel");
		String s = new SimpleDateFormat("yyyyMMdd_Hm").format(new Date());
		String filename = dataExport.getName().replace(" ", "_") + "-" + s + ".xls";
		response.setHeader("Content-Disposition", "attachment; filename=" + filename);
		response.setHeader("Pragma", "no-cache");
		
		VelocityContext velocityContext = new VelocityContext();
		Writer report = new StringWriter();
		
		// Set up velocity utils
		Locale locale = context.getLocale();
		velocityContext.put("locale", locale);
		
		// Set up functions used in the report ( $!{fn:...} )
		DataExportUtility functions = new DataExportUtility(context);
		velocityContext.put("fn", functions);
		
		report.append("Report: " + dataExport.getName() + "\n\n");
		
		// Set up list of patients
		PatientSet patientSet = new PatientSet();
		if (patientIds == null || patientIds.length == 0)
			patientSet = dataExport.generatePatientSet(context);
		else {
			// list of patient ids was passed through the request
			for (String id : patientIds)
				patientSet.add(Integer.valueOf(id));
		}
		
		velocityContext.put("patientSet", patientSet);
		
		String template = dataExport.generateTemplate();
		
		if (log.isDebugEnabled())
			log.debug("Template: " + template.substring(0, template.length() < 3500 ? template.length() : 3500) + "...");
		
		try {
			Velocity.evaluate(velocityContext, report, this.getClass().getName(), template);
		}
		catch (Exception e) {
			log.error("Error evaluating data export " + dataExport.getReportObjectId(), e);
			log.error("Template: " + template.substring(0, template.length() < 3500 ? template.length() : 3500) + "...");
			report.append("\n\nError: \n" + e.toString());
		}
		finally {
			context.endTransaction();
			response.getWriter().write(report.toString());
		}
	}
	
}