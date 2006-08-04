package org.openmrs.web.controller.patientset;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.reporting.PatientSet;
import org.openmrs.web.WebConstants;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.view.RedirectView;

public class PatientSetController implements Controller {

	protected final Log log = LogFactory.getLog(getClass());
	
    public ModelAndView handleRequest(HttpServletRequest request,
    		HttpServletResponse response) throws ServletException, IOException {
    	
    	return setPatientSet(request, response);
    }
    
    /**
     * Sets the PatientSet in the user's session to be the comma-separated list of patientIds 
     */
    public ModelAndView setPatientSet(HttpServletRequest request,
    		HttpServletResponse response) throws ServletException, IOException {

    	HttpSession httpSession = request.getSession();
		Context context = (Context) httpSession.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		if (context == null) {
			httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "auth.session.expired");
			response.sendRedirect(request.getContextPath() + "/logout");
			return null;
		}
		
		String url = request.getParameter("url");
		String ps = request.getParameter("patientIds");
		if (ps == null) {
			ps = "";
		}
		
		PatientSet patientSet = PatientSet.parseCommaSeparatedPatientIds(ps);
		httpSession.setAttribute(WebConstants.OPENMRS_PATIENT_SET_ATTR, patientSet);
		log.debug("Set session PatientSet (" + patientSet.size() + " patients)");
		return new ModelAndView(new RedirectView(url));
	}

    /**
     * Clears the PatientSet in the user's session 
     */
    public ModelAndView clearPatientSet(HttpServletRequest request,
    		HttpServletResponse response) throws ServletException, IOException {

    	HttpSession httpSession = request.getSession();
		Context context = (Context) httpSession.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		if (context == null) {
			httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "auth.session.expired");
			response.sendRedirect(request.getContextPath() + "/logout");
			return null;
		}
		
		String url = request.getParameter("url");
		
		httpSession.setAttribute(WebConstants.OPENMRS_PATIENT_SET_ATTR, null);
		log.debug("Cleared session PatientSet");
		return new ModelAndView(new RedirectView(url));
    }
    
    /**
     * Adds to the PatientSet in the user's session.
     * Adds a single patientId from the "patientId" parameter, or a comma-separated list from the "patientIds" parameter. 
     */
    public ModelAndView addToSet(HttpServletRequest request,
    		HttpServletResponse response) throws ServletException, IOException {

    	HttpSession httpSession = request.getSession();
		Context context = (Context) httpSession.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		if (context == null) {
			httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "auth.session.expired");
			response.sendRedirect(request.getContextPath() + "/logout");
			return null;
		}
		
		String url = request.getParameter("url");
		String id = request.getParameter("patientId");
		String ids = request.getParameter("patientIds");
		
		PatientSet patientSet = (PatientSet) httpSession.getAttribute(WebConstants.OPENMRS_PATIENT_SET_ATTR);
		if (patientSet == null) {
			patientSet = new PatientSet();
			httpSession.setAttribute(WebConstants.OPENMRS_PATIENT_SET_ATTR, patientSet);
		}
		
		if (id != null) {
			try {
				patientSet.add(Integer.valueOf(id.trim()));
			} catch (NumberFormatException ex) { }
		}
		
		if (ids != null) {
			for (String s : ids.split(",")) {
				try {
					patientSet.add(Integer.valueOf(s.trim()));
				} catch (NumberFormatException ex) { }
			}
		}

		return new ModelAndView(new RedirectView(url));
	}

   
    /**
     * Removes patients from the PatientSet in the user's session.
     * Removes a single patientId from the "patientId" parameter, or a comma-separated list from the "patientIds" parameter. 
     */
    public ModelAndView removeFromSet(HttpServletRequest request,
    		HttpServletResponse response) throws ServletException, IOException {

    	HttpSession httpSession = request.getSession();
		Context context = (Context) httpSession.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		if (context == null) {
			httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "auth.session.expired");
			response.sendRedirect(request.getContextPath() + "/logout");
			return null;
		}
		
		String url = request.getParameter("url");
		String id = request.getParameter("patientId");
		String ids = request.getParameter("patientIds");
		
		PatientSet patientSet = (PatientSet) httpSession.getAttribute(WebConstants.OPENMRS_PATIENT_SET_ATTR);
		if (patientSet == null) {
			patientSet = new PatientSet();
			httpSession.setAttribute(WebConstants.OPENMRS_PATIENT_SET_ATTR, patientSet);
		}
		
		if (id != null) {
			try {
				patientSet.remove(Integer.valueOf(id.trim()));
			} catch (NumberFormatException ex) { }
		}
		
		if (ids != null) {
			for (String s : ids.split(",")) {
				try {
					patientSet.remove(Integer.valueOf(s.trim()));
				} catch (NumberFormatException ex) { }
			}
		}

		return new ModelAndView(new RedirectView(url));
	}

}
