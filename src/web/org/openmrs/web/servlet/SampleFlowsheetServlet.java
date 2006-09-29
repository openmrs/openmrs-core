package org.openmrs.web.servlet;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.ConceptName;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.web.WebConstants;

public class SampleFlowsheetServlet extends HttpServlet {

	private static final long serialVersionUID = -2794221430160461220L;

	private Log log = LogFactory.getLog(this.getClass());

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		log.debug("Getting sample flowsheet");
		
		response.setContentType("text/html");
		HttpSession session = request.getSession();

		String pid = request.getParameter("pid");
		if (pid == null || pid.length() == 0) {
			session.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "error.null");
			return;
		}

		if (Context.isAuthenticated() == false
				|| !Context.hasPrivilege(OpenmrsConstants.PRIV_VIEW_PATIENTS)
				|| !Context.hasPrivilege(OpenmrsConstants.PRIV_VIEW_OBS)) {
			session.setAttribute(WebConstants.OPENMRS_ERROR_ATTR,
					"Privileges required: "
							+ OpenmrsConstants.PRIV_VIEW_PATIENTS + " and "
							+ OpenmrsConstants.PRIV_VIEW_OBS);
			session.setAttribute(
					WebConstants.OPENMRS_LOGIN_REDIRECT_HTTPSESSION_ATTR,
					request.getRequestURI() + "?" + request.getQueryString());
			response.sendRedirect(request.getContextPath() + "/login.htm");
			return;
		}

		ServletOutputStream out = response.getOutputStream();

		Integer patientId = Integer.parseInt(pid);
		Patient patient = Context.getPatientService().getPatient(patientId);
		Set<Obs> obsList = Context.getObsService().getObservations(patient);

		if (obsList == null || obsList.size() < 1) {
			out.print("No observations found");
			return;
		}

		out.println("<style>");
		out
				.println(".header { font-family:Arial; font-weight:bold; text-align: center; font-size: 1.5em;}");
		out
				.println(".label { font-family:Arial; text-align:right; color:#808080; font-style:italic; font-size: 0.6em; vertical-align: top;}");
		out
				.println(".value { font-family:Arial; text-align:left; vertical-align:top; }");
		out.println("</style>");
		out.println("<table cellspacing=0 cellpadding=3>");
		Locale locale = Context.getLocale();
		Calendar date = new GregorianCalendar(1900, Calendar.JANUARY, 1);
		Calendar obsDate = new GregorianCalendar();
		for (Obs obs : obsList) {
			obsDate.setTime(obs.getObsDatetime());
			if (Math.abs(obsDate.getTimeInMillis() - date.getTimeInMillis()) > 86400000) {
				date = obsDate;
				out.println("<tr><td class=header colspan=2>"
						+ dateFormat.format(date.getTime()) + "</td></tr>");
			}
			StringBuffer s = new StringBuffer("<tr><td class=label>");
			s.append(getName(obs, locale));
			s.append("</td><td class=value>");
			s.append(getValue(obs, locale));
			s.append("</td></tr>");
			out.println(s.toString());
		}
		out.println("</table>");
	}

	private String getName(Obs obs, Locale locale) {
		return getName(obs.getConcept(), locale);
	}

	private String getName(Concept concept, Locale locale) {
		ConceptName name = concept.getName(locale);
		if (name.getShortName() != null && name.getShortName().length() > 0)
			return name.getShortName();
		return name.getName();
	}

	private static final DateFormat dateFormat = new SimpleDateFormat(
			"MM/dd/yyyy");
	private static final DateFormat timeFormat = new SimpleDateFormat("HH:mm");

	private String getDate(Obs obs) {
		return dateFormat.format(obs.getObsDatetime());
	}

	private String getValue(Obs obs, Locale locale) {
		return obs.getValueAsString(locale);
	}
}
