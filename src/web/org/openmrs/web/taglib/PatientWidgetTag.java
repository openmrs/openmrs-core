package org.openmrs.web.taglib;

import java.io.IOException;
import java.util.Locale;

import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.web.WebConstants;

public class PatientWidgetTag extends TagSupport {

	public static final long serialVersionUID = 1L;
	private final Log log = LogFactory.getLog(getClass());
	
	private Integer patientId;

	public Integer getPatientId() {
		return patientId;
	}

	public void setPatientId(Integer patientId) {
		this.patientId = patientId;
	}
	
	public int doStartTag() {
		log.error("doStartTag()");
		Context context = (Context)pageContext.getSession().getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		Locale locale = context.getLocale();
		Patient patient = context.getPatientService().getPatient(patientId);
		log.error("patient is " + patient);
		
		try {
			JspWriter w = pageContext.getOut();
			w.print("patient_id " + patientId);
			if (patient.getGender() != null) {
				String s = patient.getGender().toLowerCase().equals("m") ? "Male" : "Female";
				w.print(", " + s);
			}
			if (patient.getAge() != null) {
				w.print(", " + patient.getAge() + " year(s) old");
			}
		} catch (IOException ex) {
			log.error("Error while starting patientWidget tag", ex);
		}
		return SKIP_BODY;
	}
	
	public int doEndTag() {
		patientId = null;
		return EVAL_PAGE;
	}
	
}
