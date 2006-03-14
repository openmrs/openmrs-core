package org.openmrs.web.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.api.ConceptService;
import org.openmrs.api.ObsService;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.web.WebConstants;

public class QuickReportServlet extends HttpServlet {

	public static final long serialVersionUID = 1231231L;
	private Log log = LogFactory.getLog(this.getClass());
	
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html");
		
		String reportType = request.getParameter("reportType");
		HttpSession session = request.getSession();
		Context context = (Context)session.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		
		if (reportType == null || reportType.length()==0 ) {
			session.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "error.null");
			return;
		}
		if (context == null || !context.hasPrivilege(OpenmrsConstants.PRIV_VIEW_PATIENTS)) {
			session.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "Privilege required: " + OpenmrsConstants.PRIV_VIEW_PATIENTS);
			session.setAttribute(WebConstants.OPENMRS_LOGIN_REDIRECT_HTTPSESSION_ATTR, request.getRequestURI() + "?" + request.getQueryString());
			response.sendRedirect(request.getContextPath() + "/login.htm");
			return;
		}
		
		ObsService os = context.getObsService();
		PatientService ps = context.getPatientService();
		ConceptService cs = context.getConceptService();
		Locale locale = context.getLocale();
		DateFormat dateFormat = new SimpleDateFormat(WebConstants.OPENMRS_LOCALE_DATE_PATTERNS().get(context.getLocale().toString().toLowerCase()), context.getLocale());
		PrintWriter report = response.getWriter();
		
		try {
			Velocity.init();
		} catch (Exception e) {
			log.error("Error initializing Velocity engine", e);
		}
		VelocityContext velocityContext = new VelocityContext();
		velocityContext.put("date", dateFormat);
		
		if (reportType.equals("RETURN VISIT DATE THIS WEEK")) {
			Concept c = cs.getConcept(new Integer("5096")); // RETURN VISIT DATE
			Calendar cal = Calendar.getInstance();
			
			String date = request.getParameter("reportDate");
			if (date != null && date.length() != 0) {
				try {
					cal.setTime(dateFormat.parse(date));
				}
				catch (ParseException e) {
					throw new ServletException("Error parsing 'reportDate'", e);
				}
			}
			else
				cal.setTime(new Date());
			while (cal.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
				cal.add(Calendar.DAY_OF_MONTH, -1);
			}
			Date start = cal.getTime();
			cal.add(Calendar.DAY_OF_MONTH, 7);
			Date end = cal.getTime();
			
			Set<Obs> obs = os.getObservations(c, "valueDatetime");
			
			for (Obs o : obs) {
				if (o.getValueDatetime().before(start) || o.getValueDatetime().after(end))
					obs.remove(o);
			}
			
			if (obs != null) {
				velocityContext.put("observations", obs);
			}
			else {
				report.append("No Observations found");
			}
		}

		try {
			Velocity.evaluate(velocityContext, report, this.getClass().getName(), getTemplate(reportType));
		}
		catch (Exception e) {
			log.error("Error evaluating report type " + reportType, e);
		}
		
	}
	
	// TODO temporary placement of template string
	private String getTemplate(String reportType) {
		
		String template = "<table>\n";
		if (reportType.equals("RETURN VISIT DATE THIS WEEK")) {
			template += "#foreach($o in $observations)\n";
			template += " <tr>\n";
			template += "  <td>$!{o.Patient.PatientName.GivenName} $!{o.Patient.PatientName.MiddleName} $!{o.Patient.PatientName.FamilyName}</td>\n";
			template += "  <td>$!{date.format($o.ValueDatetime)}</td>\n";
			template += " </tr>\n";
			template += "#end\n";
		}
		template += "</table>\n";
		
		return template;
	}
}