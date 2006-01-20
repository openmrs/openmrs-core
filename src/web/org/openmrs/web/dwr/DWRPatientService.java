package org.openmrs.web.dwr;

import java.util.List;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.PatientAddress;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.web.WebConstants;

import uk.ltd.getahead.dwr.ExecutionContext;

public class DWRPatientService {

	protected final Log log = LogFactory.getLog(getClass());
	
	public Vector findPatients(String searchValue, boolean includeVoided) {
		
		Vector patientList = new Vector();

		HttpServletRequest request = ExecutionContext.get().getHttpServletRequest();
		
		Context context = (Context) ExecutionContext.get().getSession()
				.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);

		if (context == null) {
			patientList.add("Your session has expired.");
			patientList.add("Please <a href='" + request.getContextPath() + "/logout'>log in</a> again.");
		}
		else {
			try {
				PatientService ps = context.getPatientService();
				List<Patient> patients;
				
				patients = ps.findPatients(searchValue, includeVoided);
				patientList = new Vector(patients.size());
				for (Patient p : patients) {
					patientList.add(new PatientListItem(p));
				}
				
				// only 2 results found ( TODO change to "no results found")
				// decapitated search
				if (patientList.size() < 3) {
					String[] names = searchValue.split(" ");
					String newSearch = "";
					for (String name : names) {
						if (name.length() > 3)
							name = name.substring(0, 4);
						newSearch += " " + name;
					}
					
					List<Patient> newPatients = ps.findPatients(newSearch, includeVoided);
					newPatients.removeAll(patients);
					if (newPatients.size() > 0) {
						patientList.add("Minimal patients returned. Results for <b>" + newSearch + "</b>");
						for (Patient p : newPatients) {
							PatientListItem pi = new PatientListItem(p);
							patientList.add(pi);
						}
					}
				}
				
			} catch (Exception e) {
				log.error(e);
			}
		}
		return patientList;
	}
	
	public PatientListItem getPatient(Integer patientId) {
		Context context = (Context) ExecutionContext.get().getSession().getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		PatientService ps = context.getPatientService();
		Patient p = ps.getPatient(patientId);
		PatientListItem pli = new PatientListItem(p);
		if (p.getAddresses() != null && p.getAddresses().size() > 0) {
			PatientAddress pa = (PatientAddress)p.getAddresses().toArray()[0];
			pli.setAddress1(pa.getAddress1());
			pli.setAddress2(pa.getAddress2());
		}
		return pli;
	}
	
	public Vector getSimilarPatients(String name, String birthyear, String gender) {
		Vector patientList = new Vector();

		Context context = (Context) ExecutionContext.get().getSession()
				.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		
		HttpServletRequest request = ExecutionContext.get().getHttpServletRequest();
		
		if (context == null) {
			patientList.add("Your session has expired.");
			patientList.add("Please <a href='" + request.getContextPath() + "/logout'>log in</a> again.");
		}
		else {
			PatientService ps = context.getPatientService();
			List<Patient> patients = new Vector<Patient>();
			
			Integer d = null;
			if (birthyear.length() > 3)
				d = Integer.valueOf(birthyear);
			
			if (gender.length() < 1)
				gender = null;
			
			patients.addAll(ps.getSimilarPatients(name, d, gender));
			
			patientList = new Vector(patients.size());
			for (Patient p : patients) {
				patientList.add(new PatientListItem(p));
			}
		}
		return patientList;
	}

}
