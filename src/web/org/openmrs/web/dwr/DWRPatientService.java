package org.openmrs.web.dwr;

import java.util.List;

import org.openmrs.Patient;
import org.openmrs.api.PatientService;
import org.openmrs.context.Context;
import org.openmrs.web.Constants;

import uk.ltd.getahead.dwr.ExecutionContext;

public class DWRPatientService {

	public PatientListItem[] getPatientByName(String name) {
		PatientListItem[] patientList = null;

		Context context = (Context) ExecutionContext.get().getSession()
				.getAttribute(Constants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		try {
			if (!context.isAuthenticated())
				context.authenticate("USER-1", "test");
			PatientService ps = context.getPatientService();
			List<Patient> patients = ps.getPatientByName(name);
			patientList = new PatientListItem[patients.size()];
			int i = 0;
			for (Patient p : patients) {
				patientList[i] = new PatientListItem(p);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return patientList;
	}

}
