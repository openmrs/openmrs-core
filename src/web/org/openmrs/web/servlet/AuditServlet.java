package org.openmrs.web.servlet;

import java.io.IOException;
import java.util.List;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsUtil;
import org.openmrs.web.WebConstants;

public class AuditServlet extends HttpServlet {

	public static final long serialVersionUID = 1231231L;
	private Log log = LogFactory.getLog(this.getClass());
	
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		
		String audit = request.getParameter("audit");
		HttpSession session = request.getSession();
		
		
		if (audit == null || audit.length()==0 ) {
			session.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "error.null");
			return;
		}
		
		PatientService ps = Context.getPatientService();
		
		if (audit.equals("patientIdentifiers")) {
			
			PatientIdentifierType newType = ps.getPatientIdentifierType(new Integer("4"));
			List<PatientIdentifier> identifiers = new Vector<PatientIdentifier>();
			
			for (PatientIdentifierType pit : ps.getPatientIdentifierTypes()) {
				 //If the new identifier type is defined as having a check digit as well
				if (pit.hasCheckDigit() && !pit.equals(newType)) {
					identifiers.addAll(ps.getPatientIdentifiers(pit));
				}
			}
			
			// Running count of the number of updates performed
			Integer count = 0;
			
			for (PatientIdentifier identifier : identifiers) {
				boolean updateNeeded = true;
				try {
					updateNeeded = !OpenmrsUtil.isValidCheckDigit(identifier.getIdentifier());
				} catch (Exception e) {
					log.error("Patient #" + identifier.getPatient().getPatientId() + " Bad identifier: '" + identifier.getIdentifier() + "'");
				}
				
				if (updateNeeded) {
					identifier.setIdentifierType(newType);
					ps.updatePatientIdentifier(identifier);
					count++;
				}
			}
			
			session.setAttribute(WebConstants.OPENMRS_MSG_ATTR, count + " updates performed");
			response.sendRedirect(request.getContextPath() + "/admin/maintenance/auditPatientIdentifiers.htm");
			return;
		}
		
	}
}