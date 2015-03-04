/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.taglib;

import java.io.IOException;

import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;

public class PatientWidgetTag extends TagSupport {
	
	public static final long serialVersionUID = 112341L;
	
	private final Log log = LogFactory.getLog(getClass());
	
	private Integer patientId;
	
	private String size = "normal";
	
	public Integer getPatientId() {
		return patientId;
	}
	
	public void setPatientId(Integer patientId) {
		this.patientId = patientId;
	}
	
	public String getSize() {
		return size;
	}
	
	public void setSize(String size) {
		this.size = size;
	}
	
	public int doStartTag() {
		
		/*
		WebApplicationContext messageSource = WebApplicationContextUtils.getWebApplicationContext(pageContext.getServletContext());
		if (messageSource == null) {
			log.error("servletContext = " + pageContext.getServletContext());
			log.error("WebApplicationContextUtils.getWebApplicationContext(pageContext.getServletContext()) returns null");
		}
		*/
		boolean showNames = "full".equals(size);
		boolean showPatientInfo = !"compact".equals(size);
		Patient patient = null;
		if (showPatientInfo) {
			patient = Context.getPatientService().getPatient(patientId);
		}
		
		try {
			JspWriter w = pageContext.getOut();
			if (showNames && patient != null) {
				w.print(patient.getNames().iterator().next());
			} else {
				w.print("patient_id " + patientId);
			}
			if (showPatientInfo) {
				if (patient.getGender() != null) {
					//String s = messageSource.getMessage("Patient.gender." + (patient.getGender().toLowerCase().startsWith("m") ? "male" : "female"), null, locale);
					String s = patient.getGender().toLowerCase().startsWith("m") ? "Male" : "Female";
					w.print(", " + s);
				}
				if (patient.getAge() != null) {
					//Object[] msgArgs = { patient.getAge() };
					//w.print(", " + messageSource.getMessage("Person.age.yearsOld", msgArgs, locale));
					w.print(", " + patient.getAge() + " year(s) old");
				}
			}
		}
		catch (IOException ex) {
			log.error("Error while starting patientWidget tag", ex);
		}
		return SKIP_BODY;
	}
	
	public int doEndTag() {
		patientId = null;
		return EVAL_PAGE;
	}
	
}
