package org.openmrs.web.controller.patientset;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.openmrs.api.context.Context;
import org.openmrs.reporting.PatientSet;
import org.springframework.web.servlet.mvc.SimpleFormController;

public class MultipleSummariesController extends SimpleFormController {

	/**
	 * 
	 * This is called prior to displaying a form for the first time.  It tells Spring
	 *   the form/command object to load into the request
	 * 
	 * @see org.springframework.web.servlet.mvc.AbstractFormController#formBackingObject(javax.servlet.http.HttpServletRequest)
	 */
    protected Object formBackingObject(HttpServletRequest request) throws ServletException {

		PatientSet ps = null;
			
		if (Context.isAuthenticated()) {
			String source = request.getParameter("source");
			if ("myPatientSet".equals(source))
				ps = Context.getPatientSetService().getMyPatientSet();
		}
		
		if (ps == null)
			ps = new PatientSet();
		
        return ps;
    }
	
}
