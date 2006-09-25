package org.openmrs.arden;

import java.util.Locale;



import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.context.*;

public interface ArdenDataSource {

	public Obs getPatientObsForConcept(Context context, Concept concept, Patient patient);
}
