package org.openmrs.web.dwr;

import java.util.List;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.api.PatientService;
import org.openmrs.context.Context;
import org.openmrs.web.Constants;

import uk.ltd.getahead.dwr.ExecutionContext;

public class DWRPatientService {

	protected final Log log = LogFactory.getLog(getClass());
	
	public Vector findPatients(String searchValue, String searchType, boolean includeVoided) {
		
		Vector patientList = new Vector();

		Context context = (Context) ExecutionContext.get().getSession()
				.getAttribute(Constants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		try {
			PatientService ps = context.getPatientService();
			List<Patient> patients;
			
			if (searchType != null && searchType.equals("identifier"))
				patients = ps.getPatientsByIdentifier(searchValue);
			else
				patients = ps.getPatientsByName(searchValue);
			
			patientList = new Vector(patients.size());
			for (Patient p : patients) {
				patientList.add(new PatientListItem(p));
			}
		} catch (Exception e) {
			log.error(e);
			e.printStackTrace();
		}
		return patientList;
	}
	
	public PatientListItem getPatient(Integer patientId) {
		Context context = (Context) ExecutionContext.get().getSession().getAttribute(Constants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		PatientService ps = context.getPatientService();
		Patient p = ps.getPatient(patientId);
		
		return new PatientListItem(p);
	}

}
