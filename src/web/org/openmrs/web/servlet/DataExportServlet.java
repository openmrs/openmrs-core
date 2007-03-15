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
import org.apache.velocity.app.event.EventCartridge;
import org.apache.velocity.app.event.MethodExceptionEventHandler;
import org.openmrs.api.context.Context;
import org.openmrs.reporting.PatientSet;
import org.openmrs.reporting.ReportService;
import org.openmrs.reporting.export.DataExportReportObject;
import org.openmrs.reporting.export.DataExportUtility;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.web.WebConstants;

public class DataExportServlet extends HttpServlet {

	public static final long serialVersionUID = 1231222L;
	private static Log log = LogFactory.getLog(DataExportServlet.class);
	
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		
		String reportId = request.getParameter("dataExportId");
		String[] patientIds = request.getParameterValues("patientId");
		HttpSession session = request.getSession();
		
		
		if (reportId == null || reportId.length()==0 ) {
			session.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "error.null");
			return;
		}
		if (!Context.hasPrivilege(OpenmrsConstants.PRIV_VIEW_PATIENTS)) {
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
		
		ReportService rs = Context.getReportService();
		DataExportReportObject dataExport = (DataExportReportObject)rs.getReportObject(Integer.valueOf(reportId));
		
		response.setContentType("application/vnd.ms-excel");
		String s = new SimpleDateFormat("yyyyMMdd_Hm").format(new Date());
		String filename = dataExport.getName().replace(" ", "_") + "-" + s + ".xls";
		response.setHeader("Content-Disposition", "attachment; filename=" + filename);
		response.setHeader("Pragma", "no-cache");
		
		VelocityContext velocityContext = new VelocityContext();
		Writer report = new StringWriter();
		
		// Set up velocity utils
		Locale locale = Context.getLocale();
		velocityContext.put("locale", locale);
		
		// Set up functions used in the report ( $!{fn:...} )
		DataExportUtility functions = new DataExportUtility();
		velocityContext.put("fn", functions);
		
		report.append("Report: " + dataExport.getName() + "\n\n");
		
		// Set up list of patients
		PatientSet patientSet = new PatientSet();
		if (patientIds == null || patientIds.length == 0)
			patientSet = dataExport.generatePatientSet();
		else {
			// list of patient ids was passed through the request
			for (String id : patientIds)
				patientSet.add(Integer.valueOf(id));
		}
		
		// add the error handler
		EventCartridge ec = new EventCartridge();
		ec.addEventHandler(new VelocityExceptionHandler());
		velocityContext.attachEventCartridge(ec);
		
		velocityContext.put("patientSet", patientSet);
		
		String template = dataExport.generateTemplate();
		
		if (log.isDebugEnabled())
			log.debug("Template: " + template.substring(0, template.length() < 3500 ? template.length() : 3500) + "...");
		
		try {
			Velocity.evaluate(velocityContext, response.getWriter(), this.getClass().getName(), template);
		}
		catch (Exception e) {
			log.error("Error evaluating data export " + dataExport.getReportObjectId(), e);
			log.error("Template: " + template.substring(0, template.length() < 3500 ? template.length() : 3500) + "...");
			response.getWriter().print("\n\nError: \n" + e.toString() + "\n Stacktrace: \n");
			e.printStackTrace(response.getWriter());
		}
		finally {
			patientSet = null;
			functions = null;
			velocityContext = null;
			Context.clearSession();
			//Context.closeSession();
			//.write(report.toString());
		}
	}
	
	protected class VelocityExceptionHandler implements MethodExceptionEventHandler {

		private Log log = LogFactory.getLog(this.getClass());
		
		/**
		 * When a user-supplied method throws an exception, the MethodExceptionEventHandler 
		 * is invoked with the Class, method name and thrown Exception. The handler can 
		 * either return a valid Object to be used as the return value of the method call, 
		 * or throw the passed-in or new Exception, which will be wrapped and propogated to 
		 * the user as a MethodInvocationException
		 * 
		 * @see org.apache.velocity.app.event.MethodExceptionEventHandler#methodException(java.lang.Class, java.lang.String, java.lang.Exception)
		 */
		public Object methodException(Class claz, String method, Exception e) throws Exception {
			
			log.debug("Claz: " + claz.getName() + " method: " + method, e);
			
			// if formatting a date (and probably getting an "IllegalArguementException")
			if ("format".equals(method))
				return null;
			
			// keep the default behaviour
			throw e;
		}

	}
	
}