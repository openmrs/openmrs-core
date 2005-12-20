package org.openmrs.web.dwr;

import java.util.List;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.PatientAddress;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.web.Constants;

import uk.ltd.getahead.dwr.ExecutionContext;

public class DWRPatientService {

	protected final Log log = LogFactory.getLog(getClass());
	
	public Vector findPatients(String searchValue, boolean includeVoided) {
		
		Vector patientList = new Vector();

		Context context = (Context) ExecutionContext.get().getSession()
				.getAttribute(Constants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		try {
			PatientService ps = context.getPatientService();
			List<Patient> patients;
			
			patients = ps.findPatients(searchValue, includeVoided);
			
			patientList = new Vector(patients.size());
			for (Patient p : patients) {
				patientList.add(new PatientListItem(p));
			}
		} catch (Exception e) {
			log.error(e);
		}
		return patientList;
	}
	
	public PatientListItem getPatient(Integer patientId) {
		Context context = (Context) ExecutionContext.get().getSession().getAttribute(Constants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		PatientService ps = context.getPatientService();
		Patient p = ps.getPatient(patientId);
		PatientListItem pli = new PatientListItem(p);
		pli.setAddress((PatientAddress)p.getAddresses().toArray()[0]);
		return pli;
	}

}
