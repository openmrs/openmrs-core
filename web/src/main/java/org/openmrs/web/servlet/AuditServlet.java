/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.web.servlet;

import java.io.IOException;
import java.util.Collections;
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
import org.openmrs.patient.IdentifierValidator;
import org.openmrs.web.WebConstants;

public class AuditServlet extends HttpServlet {
	
	public static final long serialVersionUID = 1231231L;
	
	private Log log = LogFactory.getLog(this.getClass());
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String audit = request.getParameter("audit");
		HttpSession session = request.getSession();
		
		if (audit == null || audit.length() == 0) {
			session.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "error.null");
			return;
		}
		
		PatientService ps = Context.getPatientService();
		
		if (audit.equals("patientIdentifiers")) {
			
			PatientIdentifierType newType = ps.getPatientIdentifierType(new Integer("4"));
			List<PatientIdentifier> identifiers = new Vector<PatientIdentifier>();
			
			for (PatientIdentifierType pit : ps.getAllPatientIdentifierTypes()) {
				//If the new identifier type is defined as having a check digit as well
				if (pit.hasValidator() && !pit.equals(newType)) {
					identifiers.addAll(ps.getPatientIdentifiers(null, Collections.singletonList(pit), null, null, null));
				}
			}
			
			// Running count of the number of updates performed
			Integer count = 0;
			
			for (PatientIdentifier identifier : identifiers) {
				boolean updateNeeded = true;
				try {
					IdentifierValidator piv = Context.getPatientService().getIdentifierValidator(
					    identifier.getIdentifierType().getValidator());
					updateNeeded = !piv.isValid(identifier.getIdentifier());
				}
				catch (Exception e) {
					log.error("Patient #" + identifier.getPatient().getPatientId() + " Bad identifier: '"
					        + identifier.getIdentifier() + "'");
				}
				
				if (updateNeeded) {
					identifier.setIdentifierType(newType);
					ps.savePatient(identifier.getPatient());
					count++;
				}
			}
			
			session.setAttribute(WebConstants.OPENMRS_MSG_ATTR, count + " updates performed");
			response.sendRedirect(request.getContextPath() + "/admin/maintenance/auditPatientIdentifiers.htm");
			return;
		}
		
	}
}
